package com.commerce.pagopa.domain.admin.category.service;

import com.commerce.pagopa.domain.admin.category.dto.response.CategoryResponseDto;
import com.commerce.pagopa.domain.admin.category.dto.response.CategorySimpleResponseDto;
import com.commerce.pagopa.domain.admin.category.dto.request.ChildCategoryCreateRequestDto;
import com.commerce.pagopa.domain.admin.category.dto.request.RootCategoryCreateRequestDto;
import com.commerce.pagopa.domain.category.entity.Category;
import com.commerce.pagopa.domain.category.repository.CategoryRepository;
import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminCategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public CategorySimpleResponseDto createRoot(RootCategoryCreateRequestDto requestDto) {
        Category rootCategory = Category.createRoot(requestDto.name());
        Category savedCategory = categoryRepository.save(rootCategory);

        return CategorySimpleResponseDto.from(savedCategory);
    }

    @Transactional
    public CategoryResponseDto createChild(ChildCategoryCreateRequestDto requestDto) {
        Category parent = categoryRepository.findById(requestDto.parentId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CATEGORY_NOT_FOUND));

        Category child = parent.createChild(requestDto.name());
        Category savedChild = categoryRepository.save(child);

        return CategoryResponseDto.from(savedChild);
    }
}
