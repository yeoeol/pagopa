package com.commerce.pagopa.order.infrastructure.persistence;

import com.commerce.pagopa.order.domain.model.SellerOrder;
import com.commerce.pagopa.order.domain.repository.SellerOrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerOrderJpaRepository extends JpaRepository<SellerOrder, Long>, SellerOrderRepository {

    @Override
    Page<SellerOrder> findBySellerId(Long sellerId, Pageable pageable);

    @Override
    Optional<SellerOrder> findByIdAndSellerId(Long id, Long sellerId);
}
