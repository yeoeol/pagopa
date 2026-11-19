import { createTestUserToken } from './lib/auth.js';
import { runOrderFlow } from './lib/order-flow.js';

export const options = {
    scenarios: {
        order_flow: {
            executor: 'ramping-vus',
            stages: [
                { duration: '1m', target: 10 },
                { duration: '3m', target: 20 },
                { duration: '1m', target: 0 },
            ],
        },
    },
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<800'],
    },
};

export default function () {
    runOrderFlow(createTestUserToken());
}
