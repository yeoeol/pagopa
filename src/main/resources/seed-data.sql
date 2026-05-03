SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- 1. categories
-- =========================
INSERT INTO categories (category_id, depth, parent_id, name) VALUES
 (1,1,NULL,'전자제품'),
 (2,1,NULL,'패션'),
 (3,1,NULL,'식품'),
 (4,2,1,'스마트폰'),
 (5,2,1,'노트북'),
 (6,2,2,'남성의류'),
 (7,2,2,'여성의류'),
 (8,2,3,'간편식'),
 (9,3,4,'안드로이드'),
 (10,3,4,'아이폰'),
 (11,3,5,'게이밍노트북'),
 (12,3,5,'사무용노트북');

-- =========================
-- 2. users
-- =========================
INSERT INTO users (user_id, email, nickname, provider_id, provider, role, user_status, created_at)
VALUES
    (1,'user1@test.com','user1','p1','KAKAO','ROLE_USER','ACTIVE',NOW()),
    (2,'user2@test.com','user2','p2','GOOGLE','ROLE_USER','ACTIVE',NOW()),
    (3,'seller1@test.com','seller1','p3','NAVER','ROLE_SELLER','ACTIVE',NOW()),
    (4,'seller2@test.com','seller2','p4','KAKAO','ROLE_SELLER','ACTIVE',NOW()),
    (5,'admin@test.com','admin','p5','GOOGLE','ROLE_ADMIN','ACTIVE',NOW()),
    (6,'user3@test.com','user3','p6','NAVER','ROLE_USER','ACTIVE',NOW()),
    (7,'user4@test.com','user4','p7','KAKAO','ROLE_USER','ACTIVE',NOW()),
    (8,'user5@test.com','user5','p8','GOOGLE','ROLE_USER','ACTIVE',NOW()),
    (9,'seller3@test.com','seller3','p9','NAVER','ROLE_SELLER','ACTIVE',NOW()),
    (10,'user6@test.com','user6','p10','KAKAO','ROLE_USER','ACTIVE',NOW());

-- =========================
-- 3. products (30+)
-- =========================
INSERT INTO products (product_id, price, discount_price, stock, category_id, user_id, name, status, created_at)
VALUES
    (1,1000000,900000,10,9,3,'갤럭시 S21','ACTIVE',NOW()),
    (2,1200000,1100000,8,10,3,'아이폰 14','ACTIVE',NOW()),
    (3,1500000,NULL,5,11,4,'게이밍 노트북 A','ACTIVE',NOW()),
    (4,800000,NULL,7,12,4,'사무용 노트북 B','ACTIVE',NOW()),

    (5,20000,NULL,100,6,3,'남성 티셔츠1','ACTIVE',NOW()),
    (6,21000,NULL,90,6,3,'남성 티셔츠2','ACTIVE',NOW()),
    (7,22000,NULL,80,6,3,'남성 티셔츠3','ACTIVE',NOW()),
    (8,23000,NULL,70,6,3,'남성 티셔츠4','ACTIVE',NOW()),
    (9,24000,NULL,60,6,3,'남성 티셔츠5','ACTIVE',NOW()),

    (10,30000,NULL,50,7,4,'여성 블라우스1','ACTIVE',NOW()),
    (11,31000,NULL,50,7,4,'여성 블라우스2','ACTIVE',NOW()),
    (12,32000,NULL,50,7,4,'여성 블라우스3','ACTIVE',NOW()),
    (13,33000,NULL,50,7,4,'여성 블라우스4','ACTIVE',NOW()),
    (14,34000,NULL,50,7,4,'여성 블라우스5','ACTIVE',NOW()),

    (15,5000,NULL,200,8,3,'컵라면1','ACTIVE',NOW()),
    (16,5100,NULL,200,8,3,'컵라면2','ACTIVE',NOW()),
    (17,5200,NULL,200,8,3,'컵라면3','ACTIVE',NOW()),
    (18,5300,NULL,200,8,3,'컵라면4','ACTIVE',NOW()),
    (19,5400,NULL,200,8,3,'컵라면5','ACTIVE',NOW()),

    (20,10000,NULL,100,8,4,'즉석밥1','ACTIVE',NOW()),
    (21,11000,NULL,100,8,4,'즉석밥2','ACTIVE',NOW()),
    (22,12000,NULL,100,8,4,'즉석밥3','ACTIVE',NOW()),
    (23,13000,NULL,100,8,4,'즉석밥4','ACTIVE',NOW()),
    (24,14000,NULL,100,8,4,'즉석밥5','ACTIVE',NOW()),

    (25,700000,NULL,3,11,9,'게이밍 노트북 B','ACTIVE',NOW()),
    (26,750000,NULL,3,11,9,'게이밍 노트북 C','ACTIVE',NOW()),
    (27,780000,NULL,3,11,9,'게이밍 노트북 D','ACTIVE',NOW()),
    (28,800000,NULL,3,11,9,'게이밍 노트북 E','ACTIVE',NOW()),
    (29,820000,NULL,3,11,9,'게이밍 노트북 F','ACTIVE',NOW()),
    (30,850000,NULL,3,11,9,'게이밍 노트북 G','ACTIVE',NOW());

-- =========================
-- 4. deliveries
-- =========================
INSERT INTO deliveries (delivery_id, recipient_name, recipient_phone, address, zipcode, created_at)
VALUES
    (1,'홍길동','01011112222','서울 강남','12345',NOW()),
    (2,'김철수','01022223333','서울 서초','12346',NOW()),
    (3,'이영희','01033334444','서울 송파','12347',NOW()),
    (4,'박민수','01044445555','서울 마포','12348',NOW()),
    (5,'최지은','01055556666','서울 종로','12349',NOW()),
    (6,'강감찬','01066667777','서울 강서','12350',NOW()),
    (7,'신사임당','01077778888','서울 노원','12351',NOW()),
    (8,'유관순','01088889999','서울 중구','12352',NOW()),
    (9,'윤봉길','01099990000','서울 성동','12353',NOW()),
    (10,'안중근','01000001111','서울 광진','12354',NOW());

-- =========================
-- 5. orders
-- =========================
INSERT INTO orders (order_id, user_id, delivery_id, order_name, order_number, payment_method, created_at)
VALUES
    (1,1,1,'주문1','ORD-001','CARD',NOW()),
    (2,2,2,'주문2','ORD-002','CARD',NOW()),
    (3,6,3,'주문3','ORD-003','CASH',NOW()),
    (4,7,4,'주문4','ORD-004','CARD',NOW()),
    (5,8,5,'주문5','ORD-005','CARD',NOW()),
    (6,10,6,'주문6','ORD-006','CARD',NOW()),
    (7,1,7,'주문7','ORD-007','CARD',NOW()),
    (8,2,8,'주문8','ORD-008','CARD',NOW()),
    (9,6,9,'주문9','ORD-009','CASH',NOW()),
    (10,7,10,'주문10','ORD-010','CARD',NOW());

-- =========================
-- 6. payments
-- =========================
INSERT INTO payments (order_id, amount, status, payment_key, created_at)
VALUES
    (1,10000,'PAID','pay_1',NOW()),
    (2,20000,'PAID','pay_2',NOW()),
    (3,15000,'READY','pay_3',NOW()),
    (4,18000,'PAID','pay_4',NOW()),
    (5,22000,'FAILED','pay_5',NOW()),
    (6,30000,'PAID','pay_6',NOW()),
    (7,40000,'CANCELLED','pay_7',NOW()),
    (8,12000,'PAID','pay_8',NOW()),
    (9,9000,'PAID','pay_9',NOW()),
    (10,5000,'READY','pay_10',NOW());

-- =========================
-- 7. refresh_tokens
-- =========================
INSERT INTO refresh_tokens (user_id, token)
VALUES
    (1,'rt1'),(2,'rt2'),(3,'rt3'),(4,'rt4'),(5,'rt5'),
    (6,'rt6'),(7,'rt7'),(8,'rt8'),(9,'rt9'),(10,'rt10');

-- =========================
-- 8. carts
-- =========================
INSERT INTO carts (quantity, user_id, product_id, created_at)
VALUES
    (1,1,1,NOW()),(2,2,2,NOW()),(1,3,3,NOW()),(3,4,4,NOW()),
    (2,5,5,NOW()),(1,6,6,NOW()),(4,7,7,NOW()),(2,8,8,NOW()),
    (1,9,9,NOW()),(5,10,10,NOW());

-- =========================
-- 9. seller_orders
-- =========================
INSERT INTO seller_orders (seller_order_id, seller_total_amount, order_id, seller_id, seller_order_number, status, created_at)
VALUES
    (1, 10000,1,3,'SO-1','READY',NOW()),
    (2, 20000,2,3,'SO-2','READY',NOW()),
    (3, 15000,3,4,'SO-3','PENDING_PAYMENT',NOW()),
    (4, 18000,4,4,'SO-4','READY',NOW()),
    (5, 22000,5,9,'SO-5','READY',NOW()),
    (6, 30000,6,9,'SO-6','READY',NOW()),
    (7, 40000,7,3,'SO-7','READY',NOW()),
    (8, 12000,8,3,'SO-8','READY',NOW()),
    (9, 9000,9,4,'SO-9','READY',NOW()),
    (10, 5000,10,4,'SO-10','READY',NOW());

-- =========================
-- 10. order_product
-- =========================
INSERT INTO order_product (order_product_id, price, quantity, product_id, seller_order_id)
VALUES
    (1, 10000,1,1,1),
    (2, 20000,2,2,2),
    (3, 15000,1,3,3),
    (4, 18000,1,4,4),
    (5, 22000,1,5,5),
    (6, 30000,2,6,6),
    (7, 40000,1,7,7),
    (8, 12000,1,8,8),
    (9,  9000,1,9,9),
    (10, 5000,1,10,10);

-- =========================
-- 11. reviews
-- =========================
INSERT INTO reviews (review_id, rating, order_product_id, user_id, content, created_at)
VALUES
    (1, 5,1,1,'좋아요',NOW()),
    (2, 4,2,2,'괜찮아요',NOW()),
    (3, 3,3,6,'보통',NOW()),
    (4, 5,4,7,'추천',NOW()),
    (5, 2,5,8,'별로',NOW()),
    (6, 5,6,10,'최고',NOW()),
    (7, 4,7,1,'굿',NOW()),
    (8, 5,8,2,'만족',NOW()),
    (9, 3,9,6,'그냥',NOW()),
    (10, 5,10,7,'좋음',NOW());

-- =========================
-- 12. product_images
-- =========================
INSERT INTO product_images (
    product_image_id, image_url, display_order, is_thumbnail, product_id
)
WITH RECURSIVE seq AS (
    SELECT 1 AS n
    UNION ALL
    SELECT n + 1 FROM seq WHERE n < 30
)
SELECT
    n,
    CONCAT('https://picsum.photos/seed/pagopa-product-', LPAD(n, 2, '0'), '/900/900'),
    1,
    1,
    n
FROM seq
ON DUPLICATE KEY UPDATE
     image_url = VALUES(image_url),
     display_order = VALUES(display_order),
     is_thumbnail = VALUES(is_thumbnail),
     product_id = VALUES(product_id);


-- =========================
-- 13. review_images
-- =========================
INSERT INTO review_images (display_order, review_id, image_url)
VALUES
    (1,1,'https://picsum.photos/seed/pagopa-review-01/900/900'),(1,2,'https://picsum.photos/seed/pagopa-review-02/900/900'),(1,3,'https://picsum.photos/seed/pagopa-review-03/900/900'),(1,4,'https://picsum.photos/seed/pagopa-review-04/900/900'),
    (1,5,'https://picsum.photos/seed/pagopa-review-05/900/900'),(1,6,'https://picsum.photos/seed/pagopa-review-06/900/900'),(1,7,'https://picsum.photos/seed/pagopa-review-07/900/900'),(1,8,'https://picsum.photos/seed/pagopa-review-08/900/900'),
    (1,9,'https://picsum.photos/seed/pagopa-review-09/900/900'),(1,10,'https://picsum.photos/seed/pagopa-review-10/900/900');

SET FOREIGN_KEY_CHECKS = 1;