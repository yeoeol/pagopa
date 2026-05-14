package com.commerce.pagopa.global.seeder;

import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Order ↔ Delivery ↔ SellerOrder ↔ OrderProduct ↔ Payment 통합 시드
 *
 * 정합성 - 결정론적 사전 계산으로 INSERT 시점에 합계 직접 채움
 *  - Order.total_amount = sum(SellerOrder.seller_total_amount)
 *  - SellerOrder.seller_total_amount = sum(OrderProduct.price * quantity)
 *  - Payment.amount = Order.total_amount
 *
 * 분배 - Order 100만(짝수) 기준 페어 단위
 *  - 짝수 Order: SellerOrder 1, OrderProduct 2
 *  - 홀수 Order: SellerOrder 2, OrderProduct 4
 *
 * 상태 분포 - 동일 Order 내 SellerOrder는 모두 같은 status
 */
@Profile("local")
@Order(6)
@Component
@RequiredArgsConstructor
class OrderAggregateSeeder implements Seeder {

    private static final int OP_PER_SO = 2;

    private final JdbcTemplate jdbc;
    private final Faker faker;
    private final SeedProperties props;
    private final BatchInsertExecutor batch;

    @Override
    public String name() {
        return "orders+deliveries+seller_orders+order_product+payments";
    }

    @Override
    public boolean shouldRun() {
        return zero("orders") && zero("deliveries") && zero("seller_orders")
                && zero("order_product") && zero("payments");
    }

    private boolean zero(String table) {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM " + table, Integer.class);
        return n != null && n == 0;
    }

    @Override
    public void seed() {
        List<Long> buyerIds = jdbc.queryForList(
                "SELECT user_id FROM users WHERE role = 'ROLE_USER' AND user_status = 'ACTIVE' ORDER BY user_id",
                Long.class);
        List<Long> sellerIds = jdbc.queryForList(
                "SELECT user_id FROM users WHERE role = 'ROLE_SELLER' AND user_status = 'ACTIVE' ORDER BY user_id",
                Long.class);
        List<ProductInfo> products = jdbc.query(
                "SELECT product_id, price, name FROM products ORDER BY product_id",
                (rs, n) -> new ProductInfo(rs.getLong(1), rs.getBigDecimal(2), rs.getString(3))
        );
        if (buyerIds.isEmpty() || sellerIds.isEmpty() || products.isEmpty()) {
            throw new IllegalStateException("buyer/seller/product 부족 - 선행 시더 확인 필요");
        }

        int orderTotal = props.counts().orders();
        if (orderTotal % 2 != 0) {
            throw new IllegalStateException("app.seed.counts.orders는 짝수여야 함 (페어 분배 전제)");
        }
        int soTotal = orderTotal / 2 * 3;
        int opTotal = soTotal * OP_PER_SO;
        int batchSize = props.batchSize();

        Aggregator agg = new Aggregator(products);

        // 1. Delivery 100만 (Order와 1:1)
        seedDeliveries(orderTotal, batchSize);
        List<Long> deliveryIds = batch.loadIds("deliveries", "delivery_id");

        // 2. Order 100만
        seedOrders(orderTotal, batchSize, buyerIds, deliveryIds, agg);
        List<Long> orderIds = batch.loadIds("orders", "order_id");

        // 3. SellerOrder 150만
        seedSellerOrders(soTotal, batchSize, sellerIds, orderIds, agg);
        List<Long> soIds = batch.loadIds("seller_orders", "seller_order_id");

        // 4. OrderProduct 300만
        seedOrderProducts(opTotal, batchSize, soIds, agg);

        // 5. Payment 100만 (Order와 1:1)
        seedPayments(orderTotal, batchSize, orderIds, agg);
    }

    private void seedDeliveries(int total, int batchSize) {
        String sql = """
                INSERT INTO deliveries(zipcode, address, detail_address,
                                       recipient_name, recipient_phone,
                                       delivery_request_memo, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        batch.batchInsert(sql, total, batchSize, (ps, i) -> {
            ps.setString(1, faker.address().zipCode());
            ps.setString(2, faker.address().fullAddress());
            ps.setString(3, faker.address().secondaryAddress());
            ps.setString(4, faker.name().fullName());
            ps.setString(5, faker.phoneNumber().cellPhone());
            ps.setString(6, i % 3 == 0 ? null : faker.lorem().sentence(5));
            ps.setTimestamp(7, now);
            ps.setTimestamp(8, now);
        });
    }

    private void seedOrders(int total, int batchSize, List<Long> buyerIds,
                            List<Long> deliveryIds, Aggregator agg) {
        String sql = """
                INSERT INTO orders(order_number, order_name, total_amount, status,
                                   payment_method, user_id, delivery_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        int buyerSize = buyerIds.size();

        batch.batchInsert(sql, total, batchSize, (ps, i) -> {
            StatusPick p = agg.pickStatus(i);
            ps.setString(1, "ORD-%d".formatted(i));
            ps.setString(2, agg.deriveOrderName(i));
            ps.setBigDecimal(3, agg.calcOrderTotal(i));
            ps.setString(4, p.orderStatus());
            ps.setString(5, i % 2 == 0 ? "CARD" : "CASH");
            ps.setLong(6, buyerIds.get(i % buyerSize));
            ps.setLong(7, deliveryIds.get(i));
            ps.setTimestamp(8, now);
            ps.setTimestamp(9, now);
        });
    }

    private void seedSellerOrders(int total, int batchSize, List<Long> sellerIds,
                                  List<Long> orderIds, Aggregator agg) {
        String sql = """
                INSERT INTO seller_orders(seller_order_number, status, seller_total_amount,
                                          order_id, seller_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        int sellerSize = sellerIds.size();

        batch.batchInsert(sql, total, batchSize, (ps, j) -> {
            int orderIdx = agg.orderIdxForSellerOrder(j);
            StatusPick p = agg.pickStatus(orderIdx);
            ps.setString(1, "SOR-%d".formatted(j));
            ps.setString(2, p.sellerOrderStatus());
            ps.setBigDecimal(3, agg.calcSellerOrderTotal(j));
            ps.setLong(4, orderIds.get(orderIdx));
            // seller_id는 j 기반으로 분산 - 다른 시드와 비결정적 충돌 회피
            ps.setLong(5, sellerIds.get((int) ((long) j * 11 % sellerSize)));
            ps.setTimestamp(6, now);
            ps.setTimestamp(7, now);
        });
    }

    private void seedOrderProducts(int total, int batchSize, List<Long> soIds, Aggregator agg) {
        String sql = """
                INSERT INTO order_product(quantity, price, seller_order_id, product_id)
                VALUES (?, ?, ?, ?)
                """;

        batch.batchInsert(sql, total, batchSize, (ps, k) -> {
            int soIdx = k / OP_PER_SO;
            int positionInSo = k % OP_PER_SO;
            ProductInfo pi = agg.pickProduct(soIdx, positionInSo);
            int quantity = agg.pickQuantity(soIdx, positionInSo);

            ps.setInt(1, quantity);
            ps.setBigDecimal(2, pi.price());
            ps.setLong(3, soIds.get(soIdx));
            ps.setLong(4, pi.id());
        });
    }

    private void seedPayments(int total, int batchSize, List<Long> orderIds, Aggregator agg) {
        String sql = """
                INSERT INTO payments(amount, cancelled_amount, status, payment_key,
                                     order_id, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());

        batch.batchInsert(sql, total, batchSize, (ps, i) -> {
            StatusPick p = agg.pickStatus(i);
            BigDecimal amount = agg.calcOrderTotal(i);

            ps.setBigDecimal(1, amount);
            ps.setBigDecimal(2, BigDecimal.ZERO);
            ps.setString(3, p.paymentStatus());
            // payment_key - 토스 호출 우회 시드용 가짜 키, PAID에만 부여
            ps.setString(4, "PAID".equals(p.paymentStatus()) ? "toss_seed_%d".formatted(i) : null);
            ps.setLong(5, orderIds.get(i));
            ps.setTimestamp(6, now);
            ps.setTimestamp(7, now);
        });
    }

    // ===== 결정론적 매핑 / 합계 계산 =====

    private record ProductInfo(long id, BigDecimal price, String name) {}

    private record StatusPick(String orderStatus, String sellerOrderStatus, String paymentStatus) {}

    private static final class Aggregator {
        private final List<ProductInfo> products;
        private final int productSize;

        Aggregator(List<ProductInfo> products) {
            this.products = products;
            this.productSize = products.size();
        }

        /** Order i의 자식 SellerOrder/OrderProduct 전체 합 */
        BigDecimal calcOrderTotal(int i) {
            BigDecimal total = BigDecimal.ZERO;
            int pairIdx = i / 2;
            boolean even = (i % 2 == 0);
            int soStart = even ? pairIdx * 3 : pairIdx * 3 + 1;
            int soCount = even ? 1 : 2;
            for (int j = soStart; j < soStart + soCount; j++) {
                total = total.add(calcSellerOrderTotal(j));
            }
            return total;
        }

        BigDecimal calcSellerOrderTotal(int j) {
            BigDecimal total = BigDecimal.ZERO;
            for (int p = 0; p < OP_PER_SO; p++) {
                ProductInfo pi = pickProduct(j, p);
                int quantity = pickQuantity(j, p);
                total = total.add(pi.price().multiply(BigDecimal.valueOf(quantity)));
            }
            return total;
        }

        /** SellerOrder j → Order index 역매핑 */
        int orderIdxForSellerOrder(int j) {
            int pairIdx = j / 3;
            int positionInPair = j % 3;
            return positionInPair == 0 ? pairIdx * 2 : pairIdx * 2 + 1;
        }

        ProductInfo pickProduct(int soIdx, int positionInSo) {
            int idx = (int) (((long) soIdx * 7 + (long) positionInSo * 13) % productSize);
            return products.get(idx);
        }

        int pickQuantity(int soIdx, int positionInSo) {
            return (soIdx + positionInSo) % 4 + 1;
        }

        /** "{첫 상품명} 외 N건" 또는 단일 상품명 */
        String deriveOrderName(int orderIdx) {
            int pairIdx = orderIdx / 2;
            boolean even = (orderIdx % 2 == 0);
            int soStart = even ? pairIdx * 3 : pairIdx * 3 + 1;
            int totalOp = even ? OP_PER_SO : 2 * OP_PER_SO;
            ProductInfo first = pickProduct(soStart, 0);
            return totalOp > 1
                    ? "%s 외 %d건".formatted(first.name(), totalOp - 1)
                    : first.name();
        }

        /** 상태 분포 - Order 단위로 결정, 자식 SellerOrder/Payment 일괄 적용 */
        StatusPick pickStatus(int orderIdx) {
            int r = orderIdx % 100;
            if (r < 10) return new StatusPick("CANCELLED",  "CANCELLED",  "CANCELLED"); // 만료 취소
            if (r < 30) return new StatusPick("PAID",       "READY",      "PAID");
            if (r < 50) return new StatusPick("DELIVERING", "DELIVERING", "PAID");
            if (r < 90) return new StatusPick("COMPLETED",  "COMPLETED",  "PAID");
            return         new StatusPick("CANCELLED",  "CANCELLED",  "CANCELLED");     // 사용자 취소
        }
    }
}
