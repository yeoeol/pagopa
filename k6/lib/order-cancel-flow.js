import http from 'k6/http';
import { check, group } from 'k6';
import { BASE_URL, PATH, DELIVERY, jsonHeaders } from './config.js';
import { getStock, pickOrderableProductId } from './product.js';

const ORDER_QUANTITY = 1;

export function runOrderCancelFlow(token, { verifyStock = false } = {}) {
    let productId;
    let stockBefore;
    let orderId;

    group('상품 선택', () => {
        productId = pickOrderableProductId(token);
        if (verifyStock) stockBefore = getStock(token, productId);
    });

    group('주문 생성', () => {
        const res = http.post(
            `${BASE_URL}${PATH.orderCreate}`,
            JSON.stringify({ delivery: DELIVERY, products: [{ productId, quantity: ORDER_QUANTITY }] }),
            jsonHeaders(token)
        );
        check(res, {
            '주문 생성 201': (r) => r.status === 201,
            '주문 상태 PENDING_PAYMENT': (r) => r.json('data.status.status') === 'PENDING_PAYMENT',
        });
        orderId = res.json('data.orderId');

        if (verifyStock) {
            const stockAfterOrder = getStock(token, productId);
            check(stockAfterOrder, { '주문 후 재고 차감': (s) => s === stockBefore - ORDER_QUANTITY });
        }
    });

    group('주문 취소', () => {
        const res = http.patch(
            `${BASE_URL}${PATH.orderCancel(orderId)}`,
            null,
            jsonHeaders(token)
        );
        check(res, {
            '주문 취소 200': (r) => r.status === 200,
            '주문 상태 CANCELED': (r) => r.json('data.status.status') === 'CANCELED',
            'canceledAt 존재': (r) => !!r.json('data.canceledAt'),
        });

        if (verifyStock) {
            const stockAfterCancel = getStock(token, productId);
            check(stockAfterCancel, { '취소 후 재고 복구': (s) => s === stockBefore });
        }
    });

    group('중복 취소 거부', () => {
        const res = http.patch(
            `${BASE_URL}${PATH.orderCancel(orderId)}`,
            null,
            { ...jsonHeaders(token), responseCallback: http.expectedStatuses(400) }
        );
        check(res, { '이미 취소된 주문 재취소 400': (r) => r.status === 400 });
    });
}
