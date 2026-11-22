import { sleep } from 'k6';
import { createTestUserToken } from './lib/auth.js';
import { runOrderFlow } from './lib/order-flow.js';
import { runOrderCancelFlow } from "./lib/order-cancel-flow.js";
import { runCartOrderFlow } from "./lib/cart-order-flow.js";

export const options = {
    scenarios: {
        order_flow: {
            executor: 'ramping-vus',
            exec: 'orderFlow',
            stages: [
                { duration: '1m', target: 10 },
                { duration: '3m', target: 20 },
                { duration: '1m', target: 0 },
            ],
        },
        order_cancel_flow: {
            executor: 'ramping-vus',
            exec: 'orderCancelFlow',
            stages: [
                { duration: '1m', target: 10 },
                { duration: '3m', target: 20 },
                { duration: '1m', target: 0 },
            ],
        },
        cart_order_flow: {
            executor: 'ramping-vus',
            exec: 'cartOrderFlow',
            stages: [
                { duration: '1m', target: 10 },
                { duration: '3m', target: 20 },
                { duration: '1m', target: 0 },
            ],
        }
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<800'],
    },
};

export function orderFlow() {
    runOrderFlow(createTestUserToken());
    sleep(1);
}
export function orderCancelFlow() {
    runOrderCancelFlow(createTestUserToken());
    sleep(1);
}
export function cartOrderFlow() {
    runCartOrderFlow(createTestUserToken());
    sleep(1);
}
