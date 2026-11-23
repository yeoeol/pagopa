import http from 'k6/http';
import { check, group } from 'k6';
import {BASE_URL, PATH, jsonHeaders, DELIVERY} from './config.js';
import { pickOrderableProductId } from "./product.js";

export function runOrderFlow(token) {
    let productId;
    let orderId;

    group('상품 선택', () => {
        productId = pickOrderableProductId(token);
    });

    group('주문 생성', () => {
        const products = [
            { productId: productId, quantity: 1 },
        ];
        const res = http.post(
            `${BASE_URL}${PATH.orderCreate}`,
            JSON.stringify({ delivery: DELIVERY, products: products }),
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
