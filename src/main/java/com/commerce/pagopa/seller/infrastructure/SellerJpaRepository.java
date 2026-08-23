package com.commerce.pagopa.seller.infrastructure;

import com.commerce.pagopa.seller.domain.model.Seller;
import com.commerce.pagopa.seller.domain.repository.SellerRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SellerJpaRepository extends JpaRepository<Seller, Long>, SellerRepository {
	@Override
	@Query("""
			SELECT s
			FROM Seller s
			JOIN FETCH s.user u
			WHERE s.id = :sellerId
			""")
	Optional<Seller> findById(@Param("sellerId") Long sellerId);
}
