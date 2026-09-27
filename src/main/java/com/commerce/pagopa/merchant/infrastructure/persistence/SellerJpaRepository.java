package com.commerce.pagopa.merchant.infrastructure.persistence;

import com.commerce.pagopa.merchant.domain.Seller;
import com.commerce.pagopa.merchant.domain.SellerRepository;
import com.commerce.pagopa.merchant.domain.SellerStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface SellerJpaRepository extends JpaRepository<Seller, Long>, SellerRepository {
	@Override
	@Query("""
			SELECT s
			FROM Seller s
			WHERE s.id = :sellerId
			""")
	Optional<Seller> findById(@Param("sellerId") Long sellerId);

	@Override
	@Query(value = """
					SELECT s
					FROM Seller s
					WHERE s.status = :status
					""",
		   countQuery = """
					SELECT COUNT(*)
					FROM Seller s
					WHERE s.status = :status
					"""
	)
	Page<Seller> findPendingRequests(
			@Param("status") SellerStatus status,
			Pageable pageable
	);
}
