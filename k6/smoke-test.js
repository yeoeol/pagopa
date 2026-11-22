import { group } from 'k6';
import { createTestUserToken } from './lib/auth.js';
import { runOrderFlow } from './lib/order-flow.js';
import { runOrderCancelFlow } from "./lib/order-cancel-flow.js";
import { runCartOrderFlow } from "./lib/cart-order-flow.js";

export const options = {
    vus: 1,
    iterations: 1,
    thresholds: { checks: ['rate==1.0'] },
};

export default function () {
    const token = createTestUserToken();

    group('E2E: 주문 -> 결제 준비', () => runOrderFlow(token));
    group('E2E: 결제 전 주문 취소', () => runOrderCancelFlow(token, { verifyStock: true }));
    group('E2E: 장바구니 주문', () => runCartOrderFlow(token, { verifyStock: true }));
}
