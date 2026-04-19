package com.commerce.pagopa.domain.category.service;

import com.commerce.pagopa.domain.category.dto.response.CategorySimpleResponseDto;
import com.commerce.pagopa.domain.category.dto.response.CategoryTreeResponseDto;
import com.commerce.pagopa.domain.category.entity.Category;
import com.commerce.pagopa.domain.category.repository.CategoryRepository;
import com.commerce.pagopa.global.exception.CategoryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategorySimpleResponseDto> findRootCategories() {
        List<Category> roots = categoryRepository.findRootCategories();
        return roots.stream()
                .map(CategorySimpleResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CategoryTreeResponseDto findChildCategories(Long categoryId) {
        Category root = categoryRepository.findById(categoryId)
                .orElseThrow(CategoryNotFoundException::new);

        return CategoryTreeResponseDto.from(root);
    }

    @Transactional(readOnly = true)
    public List<CategoryTreeResponseDto> findCategoryTree() {
        return categoryRepository.findRootCategories().stream()
                .map(CategoryTreeResponseDto::from)
                .toList();
    }
}
