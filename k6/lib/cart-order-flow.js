import http from 'k6/http';
import { check, group } from 'k6';
import { BASE_URL, PATH, DELIVERY, jsonHeaders } from './config.js';
import { getStock, pickOrderableProductId } from './product.js';

export function runCartOrderFlow(token, { verifyStock = false } = {}) {
    let productId;
    let stockBefore;
    let cartItemId;
    let cartQuantity;
    let orderId;

    group('장바구니 초기화', () => {
        const cartRes = http.get(`${BASE_URL}${PATH.cart}`, jsonHeaders(token));
        check(cartRes, { '장바구니 조회 200': (r) => r.status === 200 });

        if (cartRes.json('data.cartItems').length > 0) {
            const res = http.del(`${BASE_URL}${PATH.cart}`, null, jsonHeaders(token));
            check(res, { '장바구니 비우기 200': (r) => r.status === 200 });
        }
    });

    group('상품 선택', () => {
        productId = pickOrderableProductId(token);
        if (verifyStock) stockBefore = getStock(token, productId);
    });

    group('장바구니 담기', () => {
        const res = http.post(
            `${BASE_URL}${PATH.cartAdd}`,
            JSON.stringify({ productId, quantity: 1 }),
            jsonHeaders(token)
        );
        check(res, {
            '장바구니 담기 201': (r) => r.status === 201,
            'cartItemId 존재': (r) => !!r.json('data.cartItemId'),
        });
        cartItemId = res.json('data.cartItemId');
        cartQuantity = res.json('data.cartQuantity');
    });

    group('장바구니 주문', () => {
        const res = http.post(
            `${BASE_URL}${PATH.orderFromCart}`,
            JSON.stringify({ delivery: DELIVERY, cartItemIds: [cartItemId] }),
            jsonHeaders(token)
        );
        check(res, {
            '장바구니 주문 201': (r) => r.status === 201,
            '주문 상태 PENDING_PAYMENT': (r) => r.json('data.status.status') === 'PENDING_PAYMENT',
            '주문 상품 일치': (r) =>
                (r.json('data.orderItems') || []).length === 1
                    && r.json('data.orderItems.0.product.productId') === productId
                    && r.json('data.orderItems.0.orderQuantity') === cartQuantity,
        });
        orderId = res.json('data.orderId');
    });

    group('장바구니 비워짐 확인', () => {
        const res = http.get(
            `${BASE_URL}${PATH.cart}`,
            jsonHeaders(token)
        );
        check(res, {
            '장바구니 조회 200': (r) => r.status === 200,
            '주문한 항목 제거': (r) =>
                !(r.json('data.cartItems') || []).some((item) => item.cartItemId === cartItemId),
        });

        if (verifyStock) {
            const stockAfter = getStock(token, productId);
            check(stockAfter, { '장바구니 수량만큼 재고 차감': (s) => s === stockBefore - cartQuantity });
        }
    });

    return orderId;
}
