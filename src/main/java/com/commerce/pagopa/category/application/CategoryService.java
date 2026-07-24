package com.commerce.pagopa.category.application;

import com.commerce.pagopa.category.application.dto.response.CategorySimpleResponseDto;
import com.commerce.pagopa.category.application.dto.response.CategoryTreeResponseDto;
import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.category.domain.repository.CategoryRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

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
    public List<CategorySimpleResponseDto> getChildren(Long categoryId) {
        Category parent = categoryRepository.findByIdOrThrow(categoryId);
        return parent.getChildren().stream()
                .map(CategorySimpleResponseDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CategoryTreeResponseDto> getDescendants(Long categoryId) {
        List<Category> descendants = categoryRepository.findDescendantsByParent(categoryId);
        return buildTree(descendants);
    }

    private List<CategoryTreeResponseDto> buildTree(List<Category> categories) {
        Map<Long, CategoryTreeResponseDto> categoriesById = new LinkedHashMap<>();
        for (Category category : categories) {
            categoriesById.put(
                    category.getId(),
                    CategoryTreeResponseDto.init(category)
            );
        }

        List<CategoryTreeResponseDto> roots = new ArrayList<>();
        for (Category category : categories) {
            CategoryTreeResponseDto current = categoriesById.get(category.getId());
            CategoryTreeResponseDto parent = categoriesById.get(category.getParent().getId());

            if (parent == null) {
                roots.add(current);
                continue;
            }

            parent.addChild(current);
        }

        return roots;
    }
}
