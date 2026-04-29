package com.commerce.pagopa.domain.admin.category.service;

import com.commerce.pagopa.domain.admin.category.dto.request.CategoryUpdateRequestDto;
import com.commerce.pagopa.domain.admin.category.dto.response.CategoryResponseDto;
import com.commerce.pagopa.domain.admin.category.dto.response.CategorySimpleResponseDto;
import com.commerce.pagopa.domain.admin.category.dto.request.ChildCategoryCreateRequestDto;
import com.commerce.pagopa.domain.admin.category.dto.request.RootCategoryCreateRequestDto;
import com.commerce.pagopa.domain.category.entity.Category;
import com.commerce.pagopa.domain.category.repository.CategoryRepository;
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
        Category parent = categoryRepository.findByIdOrThrow(requestDto.parentId());

        Category child = parent.createChild(requestDto.name());
        Category savedChild = categoryRepository.save(child);

        return CategoryResponseDto.from(savedChild);
    }

    @Transactional
    public CategorySimpleResponseDto update(Long categoryId, CategoryUpdateRequestDto requestDto) {
        Category category = categoryRepository.findByIdOrThrow(categoryId);
        category.update(requestDto.name());

        return CategorySimpleResponseDto.from(category);
    }

    @Transactional
    public void delete(Long categoryId) {
        categoryRepository.deleteById(categoryId);
    }
}
