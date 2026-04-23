-- Seed data for local frontend development
-- Reserved ID ranges
-- categories: 1001+
-- users: 2001+
-- products: 3001+
-- product_images: 4001+
-- deliveries: 5001+
-- orders: 6001+
-- order_product: 7001+
-- payments: 8001+
-- reviews: 9001+
-- review_images: 9501+
-- carts: 10001+
-- scraps: 11001+
-- search_histories: 12001+
-- refresh_tokens: 13001+

USE pagopa;

INSERT INTO categories (category_id, name, depth, parent_id)
VALUES
    (1001, '라이프스타일', 0, NULL),
    (1002, '홈리빙', 1, 1001),
    (1003, '키친', 1, 1001),
    (1004, '패션', 1, 1001),
    (1005, '조명', 2, 1002),
    (1006, '수납', 2, 1002),
    (1007, '식기', 2, 1003),
    (1008, '조리도구', 2, 1003),
    (1009, '가방', 2, 1004),
    (1010, '액세서리', 2, 1004)
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    depth = VALUES(depth),
    parent_id = VALUES(parent_id);

INSERT INTO users (
    user_id, email, nickname, profile_image, provider, provider_id,
    role, user_status, withdrawn_at, ban_end_date, created_at, updated_at
)
VALUES
    (2001, 'admin@pagopa.local', 'admin_master', 'https://picsum.photos/seed/pagopa-user-01/240/240', 'GOOGLE', 'google-admin-2001', 'ROLE_ADMIN', 'ACTIVE', NULL, NULL, '2026-02-01 09:00:00', '2026-04-01 09:00:00'),
    (2002, 'mina.seller@pagopa.local', 'seller_mina', 'https://picsum.photos/seed/pagopa-user-02/240/240', 'GOOGLE', 'google-seller-2002', 'ROLE_SELLER', 'ACTIVE', NULL, NULL, '2026-02-03 09:00:00', '2026-04-02 09:00:00'),
    (2003, 'jiho.seller@pagopa.local', 'seller_jiho', 'https://picsum.photos/seed/pagopa-user-03/240/240', 'KAKAO', 'kakao-seller-2003', 'ROLE_SELLER', 'ACTIVE', NULL, NULL, '2026-02-05 09:00:00', '2026-04-03 09:00:00'),
    (2004, 'hyun.seller@pagopa.local', 'seller_hyun', 'https://picsum.photos/seed/pagopa-user-04/240/240', 'NAVER', 'naver-seller-2004', 'ROLE_SELLER', 'ACTIVE', NULL, NULL, '2026-02-07 09:00:00', '2026-04-04 09:00:00'),
    (2005, 'sora.seller@pagopa.local', 'seller_sora', 'https://picsum.photos/seed/pagopa-user-05/240/240', 'GOOGLE', 'google-seller-2005', 'ROLE_SELLER', 'ACTIVE', NULL, NULL, '2026-02-09 09:00:00', '2026-04-05 09:00:00'),
    (2006, 'dan.seller@pagopa.local', 'seller_dan', 'https://picsum.photos/seed/pagopa-user-06/240/240', 'KAKAO', 'kakao-seller-2006', 'ROLE_SELLER', 'ACTIVE', NULL, NULL, '2026-02-11 09:00:00', '2026-04-06 09:00:00'),
    (2007, 'yuna@pagopa.local', 'buyer_yuna', 'https://picsum.photos/seed/pagopa-user-07/240/240', 'GOOGLE', 'google-buyer-2007', 'ROLE_USER', 'ACTIVE', NULL, NULL, '2026-02-13 09:00:00', '2026-04-07 09:00:00'),
    (2008, 'min@pagopa.local', 'buyer_min', 'https://picsum.photos/seed/pagopa-user-08/240/240', 'KAKAO', 'kakao-buyer-2008', 'ROLE_USER', 'ACTIVE', NULL, NULL, '2026-02-15 09:00:00', '2026-04-08 09:00:00'),
    (2009, 'ara@pagopa.local', 'buyer_ara', 'https://picsum.photos/seed/pagopa-user-09/240/240', 'NAVER', 'naver-buyer-2009', 'ROLE_USER', 'ACTIVE', NULL, NULL, '2026-02-17 09:00:00', '2026-04-09 09:00:00'),
    (2010, 'joon@pagopa.local', 'buyer_joon', 'https://picsum.photos/seed/pagopa-user-10/240/240', 'GOOGLE', 'google-buyer-2010', 'ROLE_USER', 'ACTIVE', NULL, NULL, '2026-02-19 09:00:00', '2026-04-10 09:00:00'),
    (2011, 'bora@pagopa.local', 'buyer_bora', 'https://picsum.photos/seed/pagopa-user-11/240/240', 'KAKAO', 'kakao-buyer-2011', 'ROLE_USER', 'BANNED', NULL, '2026-12-31 23:59:59', '2026-02-21 09:00:00', '2026-04-11 09:00:00'),
    (2012, 'lina@pagopa.local', 'buyer_lina', 'https://picsum.photos/seed/pagopa-user-12/240/240', 'NAVER', 'naver-buyer-2012', 'ROLE_USER', 'WITHDRAWN', '2026-03-15 18:30:00', NULL, '2026-02-23 09:00:00', '2026-04-12 09:00:00')
ON DUPLICATE KEY UPDATE
    email = VALUES(email),
    nickname = VALUES(nickname),
    profile_image = VALUES(profile_image),
    provider = VALUES(provider),
    provider_id = VALUES(provider_id),
    role = VALUES(role),
    user_status = VALUES(user_status),
    withdrawn_at = VALUES(withdrawn_at),
    ban_end_date = VALUES(ban_end_date),
    created_at = VALUES(created_at),
    updated_at = VALUES(updated_at);

INSERT INTO products (
    product_id, name, description, price, discount_price, stock, status,
    category_id, user_id, created_at, updated_at
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 50
),
base_product_seed AS (
    SELECT
        n,
        3000 + n AS product_id,
        CASE MOD(n - 1, 6)
            WHEN 0 THEN CONCAT('포근한 무드 조명 ', LPAD(n, 2, '0'))
            WHEN 1 THEN CONCAT('슬림 수납 바스켓 ', LPAD(n, 2, '0'))
            WHEN 2 THEN CONCAT('데일리 세라믹 플레이트 ', LPAD(n, 2, '0'))
            WHEN 3 THEN CONCAT('실리콘 조리도구 세트 ', LPAD(n, 2, '0'))
            WHEN 4 THEN CONCAT('라이트 나일론 크로스백 ', LPAD(n, 2, '0'))
            ELSE CONCAT('미니멀 실버 액세서리 ', LPAD(n, 2, '0'))
        END AS name,
        CASE MOD(n - 1, 6)
            WHEN 0 THEN CONCAT('침실과 거실에 은은한 분위기를 더해주는 무드 조명입니다. 세트 번호 ', LPAD(n, 2, '0'))
            WHEN 1 THEN CONCAT('좁은 공간도 깔끔하게 정리할 수 있는 모듈형 수납 아이템입니다. 세트 번호 ', LPAD(n, 2, '0'))
            WHEN 2 THEN CONCAT('브런치와 홈카페 플레이팅에 잘 어울리는 세라믹 식기입니다. 세트 번호 ', LPAD(n, 2, '0'))
            WHEN 3 THEN CONCAT('주방을 산뜻하게 정리해주는 내열 실리콘 조리도구 구성입니다. 세트 번호 ', LPAD(n, 2, '0'))
            WHEN 4 THEN CONCAT('가볍고 수납력이 좋아 데일리로 들기 좋은 크로스백입니다. 세트 번호 ', LPAD(n, 2, '0'))
            ELSE CONCAT('심플한 스타일에 포인트를 더해주는 데일리 액세서리입니다. 세트 번호 ', LPAD(n, 2, '0'))
        END AS description,
        CAST(
            CASE MOD(n - 1, 6)
                WHEN 0 THEN 18900 + (n * 700)
                WHEN 1 THEN 15900 + (n * 600)
                WHEN 2 THEN 21900 + (n * 650)
                WHEN 3 THEN 24900 + (n * 800)
                WHEN 4 THEN 29900 + (n * 900)
                ELSE 12900 + (n * 500)
            END AS DECIMAL(10, 2)
        ) AS base_price,
        CASE MOD(n - 1, 6)
            WHEN 0 THEN 1005
            WHEN 1 THEN 1006
            WHEN 2 THEN 1007
            WHEN 3 THEN 1008
            WHEN 4 THEN 1009
            ELSE 1010
        END AS category_id,
        2002 + MOD(n - 1, 5) AS user_id,
        CASE
            WHEN MOD(n, 9) = 0 THEN 0
            ELSE 5 + MOD(n, 18)
        END AS stock,
        CASE
            WHEN MOD(n, 9) = 0 THEN 'SOLDOUT'
            ELSE 'ACTIVE'
        END AS status,
        DATE_ADD('2026-03-01 09:00:00', INTERVAL n DAY) AS created_at,
        DATE_ADD('2026-04-01 10:00:00', INTERVAL n HOUR) AS updated_at
    FROM seq
),
product_seed AS (
    SELECT
        product_id,
        name,
        description,
        base_price AS price,
        CASE
            WHEN MOD(n, 4) = 0 THEN CAST(base_price * 0.88 AS DECIMAL(10, 2))
            ELSE NULL
        END AS discount_price,
        stock,
        status,
        category_id,
        user_id,
        created_at,
        updated_at
    FROM base_product_seed
)
SELECT
    product_id, name, description, price, discount_price, stock, status,
    category_id, user_id, created_at, updated_at
FROM product_seed
ON DUPLICATE KEY UPDATE
    name = VALUES(name),
    description = VALUES(description),
    price = VALUES(price),
    discount_price = VALUES(discount_price),
    stock = VALUES(stock),
    status = VALUES(status),
    category_id = VALUES(category_id),
    user_id = VALUES(user_id),
    created_at = VALUES(created_at),
    updated_at = VALUES(updated_at);

INSERT INTO product_images (
    product_image_id, image_url, display_order, is_thumbnail, product_id
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 50
)
SELECT
    4000 + n,
    CONCAT('https://picsum.photos/seed/pagopa-product-', LPAD(n, 2, '0'), '/900/900'),
    1,
    1,
    3000 + n
FROM seq
ON DUPLICATE KEY UPDATE
    image_url = VALUES(image_url),
    display_order = VALUES(display_order),
    is_thumbnail = VALUES(is_thumbnail),
    product_id = VALUES(product_id);

INSERT INTO product_images (
    product_image_id, image_url, display_order, is_thumbnail, product_id
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 15
)
SELECT
    4100 + n,
    CONCAT('https://picsum.photos/seed/pagopa-product-detail-', LPAD(n, 2, '0'), '/900/900'),
    2,
    0,
    3000 + n
FROM seq
ON DUPLICATE KEY UPDATE
    image_url = VALUES(image_url),
    display_order = VALUES(display_order),
    is_thumbnail = VALUES(is_thumbnail),
    product_id = VALUES(product_id);

INSERT INTO deliveries (
    delivery_id, zipcode, address, detail_address, recipient_name, recipient_phone,
    delivery_request_memo, status, created_at, updated_at
)
VALUES
    (5001, '06123', '서울 강남구 테헤란로 12', '301호', '김유나', '010-1111-1111', '문 앞에 놓아주세요', 'READY', '2026-04-18 09:10:00', '2026-04-18 09:10:00'),
    (5002, '04788', '서울 성동구 서울숲길 44', '1203호', '박민', '010-2222-2222', '배송 전 연락 부탁드려요', 'READY', '2026-04-17 14:20:00', '2026-04-17 14:20:00'),
    (5003, '08511', '서울 금천구 가산디지털1로 88', '1705호', '이아라', '010-3333-3333', '경비실에 맡겨주세요', 'SHIPPED', '2026-04-16 11:30:00', '2026-04-20 08:10:00'),
    (5004, '13529', '경기 성남시 분당구 판교역로 18', '2102호', '최준', '010-4444-4444', '빠른 배송 부탁드립니다', 'DELIVERED', '2026-04-14 13:40:00', '2026-04-18 16:10:00'),
    (5005, '04052', '서울 마포구 양화로 77', '803호', '김유나', '010-1111-1111', '취소된 주문입니다', 'CANCELLED', '2026-04-13 10:00:00', '2026-04-13 13:40:00'),
    (5006, '48058', '부산 해운대구 센텀중앙로 97', '1507호', '박민', '010-2222-2222', '파손되지 않게 포장해주세요', 'DELIVERED', '2026-04-12 16:25:00', '2026-04-17 18:20:00'),
    (5007, '35209', '대전 서구 둔산로 125', '904호', '이아라', '010-3333-3333', '부재 시 문자 남겨주세요', 'READY', '2026-04-19 18:10:00', '2026-04-19 18:10:00'),
    (5008, '61947', '광주 서구 상무중앙로 45', '1101호', '최준', '010-4444-4444', '배송메시지 없음', 'READY', '2026-04-20 09:45:00', '2026-04-20 09:45:00')
ON DUPLICATE KEY UPDATE
    zipcode = VALUES(zipcode),
    address = VALUES(address),
    detail_address = VALUES(detail_address),
    recipient_name = VALUES(recipient_name),
    recipient_phone = VALUES(recipient_phone),
    delivery_request_memo = VALUES(delivery_request_memo),
    status = VALUES(status),
    created_at = VALUES(created_at),
    updated_at = VALUES(updated_at);

INSERT INTO orders (
    order_id, order_number, order_name, total_amount, status, payment_method,
    user_id, delivery_id, created_at, updated_at
)
VALUES
    (6001, 'PG202604230001', '포근한 무드 조명 01', 24900.00, 'ORDERED', 'CARD', 2007, 5001, '2026-04-18 09:12:00', '2026-04-18 09:12:00'),
    (6002, 'PG202604230002', '데일리 세라믹 플레이트 03 외 1건', 57800.00, 'PAID', 'CARD', 2008, 5002, '2026-04-17 14:25:00', '2026-04-17 14:31:00'),
    (6003, 'PG202604230003', '실리콘 조리도구 세트 16 외 1건', 86900.00, 'DELIVERING', 'CARD', 2009, 5003, '2026-04-16 11:35:00', '2026-04-20 08:10:00'),
    (6004, 'PG202604230004', '미니멀 실버 액세서리 24', 42800.00, 'COMPLETED', 'CARD', 2010, 5004, '2026-04-14 13:45:00', '2026-04-18 16:10:00'),
    (6005, 'PG202604230005', '포근한 무드 조명 25', 31900.00, 'CANCELLED', 'CARD', 2007, 5005, '2026-04-13 10:10:00', '2026-04-13 13:40:00'),
    (6006, 'PG202604230006', '실리콘 조리도구 세트 40 외 1건', 74600.00, 'COMPLETED', 'CASH', 2008, 5006, '2026-04-12 16:28:00', '2026-04-17 18:20:00'),
    (6007, 'PG202604230007', '데일리 세라믹 플레이트 45', 18900.00, 'PAID', 'CASH', 2009, 5007, '2026-04-19 18:15:00', '2026-04-19 18:19:00'),
    (6008, 'PG202604230008', '라이트 나일론 크로스백 50', 55900.00, 'ORDERED', 'CARD', 2010, 5008, '2026-04-20 09:48:00', '2026-04-20 09:52:00')
ON DUPLICATE KEY UPDATE
    order_number = VALUES(order_number),
    order_name = VALUES(order_name),
    total_amount = VALUES(total_amount),
    status = VALUES(status),
    payment_method = VALUES(payment_method),
    user_id = VALUES(user_id),
    delivery_id = VALUES(delivery_id),
    created_at = VALUES(created_at),
    updated_at = VALUES(updated_at);

INSERT INTO order_product (
    order_product_id, quantity, price, order_id, product_id
)
VALUES
    (7001, 1, 24900.00, 6001, 3001),
    (7002, 1, 27900.00, 6002, 3003),
    (7003, 1, 29900.00, 6002, 3010),
    (7004, 1, 35900.00, 6003, 3008),
    (7005, 1, 51000.00, 6003, 3016),
    (7006, 1, 42800.00, 6004, 3024),
    (7007, 1, 31900.00, 6005, 3025),
    (7008, 2, 18900.00, 6006, 3033),
    (7009, 1, 36800.00, 6006, 3040),
    (7010, 1, 18900.00, 6007, 3045),
    (7011, 1, 55900.00, 6008, 3050)
ON DUPLICATE KEY UPDATE
    quantity = VALUES(quantity),
    price = VALUES(price),
    order_id = VALUES(order_id),
    product_id = VALUES(product_id);

INSERT INTO payments (
    payment_id, amount, status, payment_key, order_id, created_at, updated_at
)
VALUES
    (8001, 24900.00, 'READY', NULL, 6001, '2026-04-18 09:12:10', '2026-04-18 09:12:10'),
    (8002, 57800.00, 'PAID', 'seed_payment_key_6002', 6002, '2026-04-17 14:25:10', '2026-04-17 14:31:10'),
    (8003, 86900.00, 'PAID', 'seed_payment_key_6003', 6003, '2026-04-16 11:35:10', '2026-04-16 11:41:30'),
    (8004, 42800.00, 'PAID', 'seed_payment_key_6004', 6004, '2026-04-14 13:45:10', '2026-04-14 13:48:50'),
    (8005, 31900.00, 'CANCELLED', NULL, 6005, '2026-04-13 10:10:10', '2026-04-13 13:40:10'),
    (8006, 74600.00, 'PAID', 'seed_payment_key_6006', 6006, '2026-04-12 16:28:10', '2026-04-12 16:35:00'),
    (8007, 18900.00, 'PAID', 'seed_payment_key_6007', 6007, '2026-04-19 18:15:10', '2026-04-19 18:18:30'),
    (8008, 55900.00, 'IN_PROGRESS', NULL, 6008, '2026-04-20 09:48:10', '2026-04-20 09:52:00')
ON DUPLICATE KEY UPDATE
    amount = VALUES(amount),
    status = VALUES(status),
    payment_key = VALUES(payment_key),
    order_id = VALUES(order_id),
    created_at = VALUES(created_at),
    updated_at = VALUES(updated_at);

INSERT INTO reviews (
    review_id, rating, content, user_id, order_product_id, created_at, updated_at
)
VALUES
    (9001, 5, '사진보다 실물이 더 예쁘고 배송도 빨랐어요. 침실 무드등으로 만족합니다.', 2010, 7006, '2026-04-19 12:10:00', '2026-04-19 12:10:00'),
    (9002, 4, '가성비가 좋고 수납이 잘 됩니다. 생각보다 튼튼해서 재구매 의사 있어요.', 2008, 7008, '2026-04-18 10:20:00', '2026-04-18 10:20:00'),
    (9003, 5, '주방 분위기가 확 살아났어요. 사진 올린 그대로 깔끔하게 잘 왔습니다.', 2008, 7009, '2026-04-18 10:35:00', '2026-04-18 10:35:00')
ON DUPLICATE KEY UPDATE
    rating = VALUES(rating),
    content = VALUES(content),
    user_id = VALUES(user_id),
    order_product_id = VALUES(order_product_id),
    created_at = VALUES(created_at),
    updated_at = VALUES(updated_at);

INSERT INTO review_images (
    review_image_id, image_url, display_order, review_id
)
VALUES
    (9501, 'https://picsum.photos/seed/pagopa-review-01/900/900', 1, 9001),
    (9502, 'https://picsum.photos/seed/pagopa-review-02/900/900', 1, 9002),
    (9503, 'https://picsum.photos/seed/pagopa-review-03/900/900', 1, 9003)
ON DUPLICATE KEY UPDATE
    image_url = VALUES(image_url),
    display_order = VALUES(display_order),
    review_id = VALUES(review_id);

INSERT INTO carts (
    cart_id, quantity, user_id, product_id, created_at, updated_at
)
VALUES
    (10001, 1, 2007, 3002, '2026-04-21 09:10:00', '2026-04-21 09:10:00'),
    (10002, 2, 2007, 3014, '2026-04-21 09:15:00', '2026-04-21 09:15:00'),
    (10003, 1, 2008, 3005, '2026-04-21 10:20:00', '2026-04-21 10:20:00'),
    (10004, 1, 2008, 3021, '2026-04-21 10:25:00', '2026-04-21 10:25:00'),
    (10005, 3, 2009, 3007, '2026-04-21 11:40:00', '2026-04-21 11:40:00'),
    (10006, 1, 2009, 3044, '2026-04-21 11:42:00', '2026-04-21 11:42:00'),
    (10007, 2, 2010, 3012, '2026-04-21 12:05:00', '2026-04-21 12:05:00'),
    (10008, 1, 2010, 3038, '2026-04-21 12:06:00', '2026-04-21 12:06:00')
ON DUPLICATE KEY UPDATE
    quantity = VALUES(quantity),
    user_id = VALUES(user_id),
    product_id = VALUES(product_id),
    created_at = VALUES(created_at),
    updated_at = VALUES(updated_at);

INSERT INTO scraps (
    scrap_id, target_id, target_type, user_id, created_at, updated_at
)
VALUES
    (11001, 3001, 'PRODUCT', 2007, '2026-04-20 09:00:00', '2026-04-20 09:00:00'),
    (11002, 3008, 'PRODUCT', 2007, '2026-04-20 09:05:00', '2026-04-20 09:05:00'),
    (11003, 3015, 'PRODUCT', 2008, '2026-04-20 09:10:00', '2026-04-20 09:10:00'),
    (11004, 3022, 'PRODUCT', 2008, '2026-04-20 09:15:00', '2026-04-20 09:15:00'),
    (11005, 3029, 'PRODUCT', 2009, '2026-04-20 09:20:00', '2026-04-20 09:20:00'),
    (11006, 3036, 'PRODUCT', 2009, '2026-04-20 09:25:00', '2026-04-20 09:25:00'),
    (11007, 3043, 'PRODUCT', 2010, '2026-04-20 09:30:00', '2026-04-20 09:30:00'),
    (11008, 3050, 'PRODUCT', 2010, '2026-04-20 09:35:00', '2026-04-20 09:35:00')
ON DUPLICATE KEY UPDATE
    target_id = VALUES(target_id),
    target_type = VALUES(target_type),
    user_id = VALUES(user_id),
    created_at = VALUES(created_at),
    updated_at = VALUES(updated_at);

INSERT INTO search_histories (
    search_history_id, user_id, session_id, keyword, last_searched_at, created_at, updated_at
)
VALUES
    (12001, 2007, NULL, '무드등', '2026-04-21 08:30:00', '2026-04-21 08:30:00', '2026-04-21 08:30:00'),
    (12002, 2007, NULL, '조리도구 세트', '2026-04-21 08:35:00', '2026-04-21 08:35:00', '2026-04-21 08:35:00'),
    (12003, 2008, NULL, '크로스백', '2026-04-21 08:40:00', '2026-04-21 08:40:00', '2026-04-21 08:40:00'),
    (12004, 2008, NULL, '세라믹 접시', '2026-04-21 08:45:00', '2026-04-21 08:45:00', '2026-04-21 08:45:00'),
    (12005, 2009, NULL, '수납 바구니', '2026-04-21 08:50:00', '2026-04-21 08:50:00', '2026-04-21 08:50:00'),
    (12006, 2010, NULL, '실버 목걸이', '2026-04-21 08:55:00', '2026-04-21 08:55:00', '2026-04-21 08:55:00'),
    (12007, NULL, 'guest-session-001', '홈카페 접시', '2026-04-21 09:00:00', '2026-04-21 09:00:00', '2026-04-21 09:00:00'),
    (12008, NULL, 'guest-session-002', '침실 조명', '2026-04-21 09:05:00', '2026-04-21 09:05:00', '2026-04-21 09:05:00')
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    session_id = VALUES(session_id),
    keyword = VALUES(keyword),
    last_searched_at = VALUES(last_searched_at),
    created_at = VALUES(created_at),
    updated_at = VALUES(updated_at);

INSERT INTO refresh_tokens (
    refresh_token_id, user_id, token
)
VALUES
    (13001, 2002, 'seed-refresh-token-seller-2002'),
    (13002, 2007, 'seed-refresh-token-buyer-2007'),
    (13003, 2008, 'seed-refresh-token-buyer-2008'),
    (13004, 2009, 'seed-refresh-token-buyer-2009'),
    (13005, 2010, 'seed-refresh-token-buyer-2010')
ON DUPLICATE KEY UPDATE
    user_id = VALUES(user_id),
    token = VALUES(token);
