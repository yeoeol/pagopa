import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL, PATH, jsonHeaders } from './config.js';

const PRODUCT_POOL_SIZE = 100;

export function pickOrderableProductId(token) {
    const res = http.get(
        `${BASE_URL}${PATH.productList}?size=${PRODUCT_POOL_SIZE}`,
        jsonHeaders(token)
    );
    check(res, {
        '상품 조회 200': (r) => r.status === 200,
    });

    const products = (res.json('data.content') || [])
        .filter((p) => p.stockQuantity > 0);
    check(res, {
        '주문 가능 상품 1개 이상': () => products.length > 0,
    });

    return products.length > 0
        ? products[Math.floor(Math.random() * products.length)].productId
        : null;
}

export function getStock(token, productId) {
    const res = http.get(`${BASE_URL}${PATH.productDetail(productId)}`, jsonHeaders(token));
    check(res, {
        '상품 상세 200': (r) => r.status === 200
    });
    return res.json('data.stockQuantity');
}
