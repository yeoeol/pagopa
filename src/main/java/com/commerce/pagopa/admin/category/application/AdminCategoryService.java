package com.commerce.pagopa.admin.category.application;

import com.commerce.pagopa.admin.category.application.dto.request.CategoryUpdateRequestDto;
import com.commerce.pagopa.admin.category.application.dto.response.CategoryResponseDto;
import com.commerce.pagopa.admin.category.application.dto.response.CategorySimpleResponseDto;
import com.commerce.pagopa.admin.category.application.dto.request.ChildCategoryCreateRequestDto;
import com.commerce.pagopa.admin.category.application.dto.request.RootCategoryCreateRequestDto;
import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.category.domain.repository.CategoryRepository;
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
