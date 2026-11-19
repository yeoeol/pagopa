import { createTestUserToken } from './lib/auth.js';
import { runOrderFlow } from './lib/order-flow.js';

export const options = {
    vus: 1,
    iterations: 1,
    thresholds: { checks: ['rate==1.0'] },
};

export default function () {
    runOrderFlow(createTestUserToken());
}
