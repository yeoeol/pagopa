export const BASE_URL = 'http://localhost:8080';

export const PATH = {
    productList: '/api/v1/products/cursor',
    cartAdd: '/api/v1/cart/items',
    orderCreate: '/api/v1/orders',
    paymentRequest: '/api/v1/payments/request',
};

export function jsonHeaders(token) {
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers.Authorization = `Bearer ${token}`;
    return { headers };
}
