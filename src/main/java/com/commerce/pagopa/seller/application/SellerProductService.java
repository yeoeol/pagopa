package com.commerce.pagopa.seller.application;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.category.domain.repository.CategoryRepository;
import com.commerce.pagopa.product.application.dto.response.ProductResponseDto;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.model.ProductImage;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.seller.application.dto.product.request.ProductRegisterRequestDto;
import com.commerce.pagopa.seller.domain.model.Seller;
import com.commerce.pagopa.seller.domain.repository.SellerRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final SellerRepository sellerRepository;

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
        Seller seller = sellerRepository.findByIdOrThrow(sellerId);

        Category category = categoryRepository.findByIdOrThrow(requestDto.categoryId());

        Product product = Product.create(
                requestDto.name(),
                requestDto.description(),
                requestDto.price(),
                requestDto.stockQuantity(),
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
