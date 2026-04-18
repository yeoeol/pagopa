package com.commerce.pagopa.domain.product.service;

import com.commerce.pagopa.domain.category.entity.Category;
import com.commerce.pagopa.domain.category.repository.CategoryRepository;
import com.commerce.pagopa.domain.product.dto.request.ProductRegisterRequestDto;
import com.commerce.pagopa.domain.product.dto.request.ProductSearch;
import com.commerce.pagopa.domain.product.dto.response.ProductResponseDto;
import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.product.entity.ProductImage;
import com.commerce.pagopa.domain.product.repository.ProductRepository;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.repository.UserRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.exception.CategoryNotFoundException;
import com.commerce.pagopa.global.exception.ProductNotFoundException;
import com.commerce.pagopa.global.exception.UserNotFoundException;
import com.commerce.pagopa.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProductResponseDto register(Long sellerId, ProductRegisterRequestDto requestDto) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(UserNotFoundException::new);

        Category category = categoryRepository.findById(requestDto.categoryId())
                .orElseThrow(CategoryNotFoundException::new);

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

    @Transactional(readOnly = true)
    public List<ProductResponseDto> findAll() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponseDto find(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(ProductNotFoundException::new);
        return ProductResponseDto.from(product);
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDto> search(ProductSearch productSearch) {
        return productRepository.searchProducts(
                productSearch.productName()
        ).stream()
                .map(ProductResponseDto::from)
                .toList();
    }
}