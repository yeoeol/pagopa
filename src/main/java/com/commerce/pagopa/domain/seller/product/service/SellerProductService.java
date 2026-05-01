package com.commerce.pagopa.domain.seller.product.service;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.category.domain.repository.CategoryRepository;
import com.commerce.pagopa.domain.product.dto.response.ProductResponseDto;
import com.commerce.pagopa.domain.seller.product.dto.request.ProductRegisterRequestDto;
import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.product.entity.ProductImage;
import com.commerce.pagopa.domain.product.repository.ProductRepository;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.repository.UserRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SellerProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public Page<ProductResponseDto> findAll(Long userId, Pageable pageable) {
        Page<Product> pageProduct = productRepository.findAllBySellerId(userId, pageable);
        return pageProduct.map(ProductResponseDto::from);
    }

    @Transactional(readOnly = true)
    public ProductResponseDto find(Long productId) {
        Product product = productRepository.findByIdOrThrow(productId);
        return ProductResponseDto.from(product);
    }

    @Transactional
    public ProductResponseDto register(Long sellerId, ProductRegisterRequestDto requestDto) {
        User seller = userRepository.findByIdOrThrow(sellerId);

        Category category = categoryRepository.findByIdOrThrow(requestDto.categoryId());

        // 소분류에만 상품 등록이 가능
        if (!category.isLeaf()) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_LEVEL);
        }

        Product product = Product.create(
                requestDto.name(),
                requestDto.description(),
                requestDto.price(),
                requestDto.price(),
                requestDto.stock(),
                category,
                seller
        );

        for (int i = 0; i < requestDto.imageUrls().size(); i++) {
            boolean isThumbnail = (i == 0);
            ProductImage productImage = ProductImage.create(requestDto.imageUrls().get(i), i + 1, isThumbnail);
            product.addImage(productImage);
        }

        return ProductResponseDto.from(productRepository.save(product));
    }
}
