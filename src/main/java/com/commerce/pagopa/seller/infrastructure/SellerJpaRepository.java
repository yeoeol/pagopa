package com.commerce.pagopa.seller.infrastructure;

import com.commerce.pagopa.seller.domain.model.Seller;
import com.commerce.pagopa.seller.domain.repository.SellerRepository;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerJpaRepository extends JpaRepository<Seller, Long>, SellerRepository {
}
