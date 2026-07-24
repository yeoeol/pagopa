import http from 'k6/http';
import {check, sleep} from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://host.docker.internal:8080';
const TEST_USERS = Number(__ENV.TEST_USERS || 60);
const PRODUCT_PAGE_SIZE = Number(__ENV.PRODUCT_PAGE_SIZE || 100);
const PRODUCT_PAGE_COUNT = Number(__ENV.PRODUCT_PAGE_COUNT || 1);

export const options = {
    scenarios: {
        ten_vu_1m: {
            executor: 'constant-vus',
            vus: 10,
            duration: '1m',
            exec: 'userFlow',
            gracefulStop: '30s',
        },
        fifty_vu_3m: {
            executor: 'constant-vus',
            vus: 50,
            duration: '3m',
            startTime: '1m10s',
            exec: 'userFlow',
            gracefulStop: '30s',
        },
    },
    thresholds: {
        'http_req_failed{name:product-list}': ['rate<0.01'],
        'http_req_failed{name:cart-add}': ['rate<0.01'],
        'http_req_failed{name:order-create}': ['rate<0.01'],
        'http_req_failed{name:order-detail}': ['rate<0.01'],
        'http_req_duration{name:product-list}': ['p(95)<500'],
        'http_req_duration{name:cart-add}': ['p(95)<800'],
        'http_req_duration{name:order-create}': ['p(95)<1500'],
        'http_req_duration{name:order-detail}': ['p(95)<800'],
    },
};

const jsonHeaders = {
    headers: {
        'Content-Type': 'application/json',
    },
};

function requestParams(name) {
    return {
        tags: { name },
    };
}

function authParams(token, name) {
    return {
        headers: {
            Authorization: `Bearer ${token}`,
            'Content-Type': 'application/json',
        },
        tags: { name },
    };
}

function parseData(response) {
    try {
        return JSON.parse(response.body).data;
    } catch (e) {
        return null;
    }
}

function selectProduct(products) {
    const candidates = products.filter((product) => product.stock > 0);
    if (candidates.length === 0) {
        return null;
    }

    const index = (__VU + __ITER) % candidates.length;
    return candidates[index];
}

export function setup() {
    const tokens = [];

    for (let i = 1; i <= TEST_USERS; i++) {
        const response = http.post(
            `${BASE_URL}/api/v1/local-test/auth/token`,
            JSON.stringify({ userKey: `vu-${i}` }),
            {
                headers: jsonHeaders.headers,
                tags: { name: 'token-issue' },
            }
        );

        const data = parseData(response);
        const ok = check(response, {
            'token issued': (res) => res.status === 200 && data && data.accessToken,
        });

        if (!ok) {
            throw new Error(`Failed to issue token for vu-${i}: ${response.status} ${response.body}`);
        }

        tokens.push(data.accessToken);
    }

    return { tokens };
}

export function userFlow(data) {
    const token = data.tokens[(__VU - 1) % data.tokens.length];
    const page = (__VU + __ITER) % PRODUCT_PAGE_COUNT;

    const productsResponse = http.get(
        `${BASE_URL}/api/v1/products?page=${page}&size=${PRODUCT_PAGE_SIZE}`,
        requestParams('product-list')
    );

    const productPage = parseData(productsResponse);
    const products = productPage && productPage.content ? productPage.content : [];
    const product = selectProduct(products);

    const productListOk = check(productsResponse, {
        'product list loaded': (res) => res.status === 200,
        'product selected': () => product !== null,
    });

    if (!productListOk) {
        sleep(1);
        return;
    }

    const cartResponse = http.post(
        `${BASE_URL}/api/v1/cart`,
        JSON.stringify({
            productId: product.productId,
            quantity: 1,
        }),
        authParams(token, 'cart-add')
    );

    const cart = parseData(cartResponse);
    const cartOk = check(cartResponse, {
        'cart added': (res) => res.status === 201 && cart && cart.cartId,
    });

    if (!cartOk) {
        sleep(1);
        return;
    }

    sleep(Math.random() * 2 + 1);

    const orderResponse = http.post(
        `${BASE_URL}/api/v1/orders/cart`,
        JSON.stringify({
            delivery: {
                recipientName: 'Load Test User',
                recipientPhone: '01012345678',
                zipcode: '12345',
                address: 'Seoul Test-gu',
                detailAddress: '101',
                deliveryRequestMemo: 'Leave at door',
            },
            cartIds: [cart.cartId],
        }),
        authParams(token, 'order-create')
    );

    const order = parseData(orderResponse);
    const orderOk = check(orderResponse, {
        'order created': (res) => res.status === 201 && order && order.orderId,
    });

    if (!orderOk) {
        sleep(1);
        return;
    }

    const detailResponse = http.get(
        `${BASE_URL}/api/v1/orders/${order.orderId}`,
        authParams(token, 'order-detail')
    );

    check(detailResponse, {
        'order detail loaded': (res) => res.status === 200,
    });

    sleep(Math.random() * 2 + 1);
}

export default function (data) {
    userFlow(data);
}
