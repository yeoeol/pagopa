import http from 'k6/http';
import { check, group } from 'k6';
import { BASE_URL, PATH, jsonHeaders } from './config.js';

export function runOrderFlow(token) {
    let productId;
    let orderId;

    group('상품 목록 조회', () => {
        const res = http.get(`${BASE_URL}${PATH.productList}`, jsonHeaders(token));
        check(res, {
            '상품 조회 200': (r) => r.status === 200,
            '상품 1개 이상': (r) => r.json('data.content').length > 0,
        });
        productId = res.json('data.content.0.productId');
    })

    group('장바구니 담기', () => {
        const res = http.post(
            `${BASE_URL}${PATH.cartAdd}`,
            JSON.stringify({ productId: productId, quantity: 1 }),
            jsonHeaders(token)
        );
        check(res, {
            '장바구니 담기 성공': (r) => r.status === 200 || r.status === 201,
        });
    });

    group('주문 생성', () => {
        const delivery =
            { requestMemo: '테스트 요청사항', zipcode: '12345', address: '테스트 주소', detailAddress: '테스트 상세주소' };
        const products = [
            { productId: productId, quantity: 1 },
        ];
        const res = http.post(
            `${BASE_URL}${PATH.orderCreate}`,
            JSON.stringify({ delivery: delivery, products: products }),
            jsonHeaders(token)
        );
        check(res, {
            '주문 생성 성공': (r) => r.status === 200 || r.status === 201,
            'orderId 존재': (r) => !!r.json('data.orderId'),
        });
        orderId = res.json('data.orderId');
    });

    group('결제 준비', () => {
        const res = http.post(
            `${BASE_URL}${PATH.paymentRequest}`,
            JSON.stringify({ orderId: orderId, paymentMethod: '카드', provider: 'TOSS_PAYMENTS' }),
            jsonHeaders(token)
        );
        check(res, {
            '결제 준비 200': (r) => r.status === 200,
        });
    });
}
