package com.commerce.pagopa.seller.application.admin;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.role.domain.model.enums.RoleCode;
import com.commerce.pagopa.role.domain.repository.RoleRepository;
import com.commerce.pagopa.seller.application.admin.dto.response.AdminSellerPageResponseDto;
import com.commerce.pagopa.seller.domain.model.Seller;
import com.commerce.pagopa.seller.domain.model.enums.SellerStatus;
import com.commerce.pagopa.seller.domain.repository.SellerRepository;
import com.commerce.pagopa.userrole.domain.model.UserRole;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminSellerService {

	private final SellerRepository sellerRepository;
	private final RoleRepository roleRepository;

	@Transactional(readOnly = true)
	public AdminSellerPageResponseDto getPendingSellers(Pageable pageable) {
		Pageable pageRequest = PageRequest.of(
				pageable.getPageNumber(),
				pageable.getPageSize(),
				Sort.by(Sort.Direction.DESC, "statusChangedAt")
						.and(Sort.by(Sort.Direction.DESC, "id"))
		);
		Page<Seller> sellers = sellerRepository
				.findPendingRequests(SellerStatus.PENDING, pageRequest);

		return AdminSellerPageResponseDto.from(sellers);
	}

	@Transactional
	public void approve(Long sellerId) {
		Seller seller = sellerRepository.findByIdOrThrow(sellerId);
		seller.activate(Instant.now());

		Role role = roleRepository.findByCode(RoleCode.ROLE_SELLER)
				.orElseThrow(() -> new BusinessException(ErrorCode.ROLE_NOT_FOUND));

		seller.getUser().addUserRole(UserRole.create(seller.getUser(), role));

		sellerRepository.save(seller);
	}

	@Transactional
	public void reject(Long sellerId) {
		Seller seller = sellerRepository.findByIdOrThrow(sellerId);
		seller.reject(Instant.now());
	}
}
