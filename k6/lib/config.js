export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

export const PATH = {
    productList: '/api/v1/products/cursor',
    productDetail: (productId) => `/api/v1/products/${productId}`,
    cart: '/api/v1/cart',
    cartAdd: '/api/v1/cart/items',
    orderCreate: '/api/v1/orders',
    orderFromCart: '/api/v1/orders/cart',
    orderCancel: (orderId) => `/api/v1/orders/${orderId}/cancel`,
    paymentRequest: '/api/v1/payments/request',
};

export const DELIVERY = {
    requestMemo: '테스트 요청사항',
    zipcode: '12345',
    address: '테스트 주소',
    detailAddress: '테스트 상세주소',
};

export function jsonHeaders(token) {
    const headers = { 'Content-Type': 'application/json' };
    if (token) headers.Authorization = `Bearer ${token}`;
    return { headers };
}
