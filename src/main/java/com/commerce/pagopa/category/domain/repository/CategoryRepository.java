package com.commerce.pagopa.category.domain.repository;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.global.exception.CategoryNotFoundException;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(Long id);

    void deleteById(Long id);

    List<Category> findRootCategories();

    default Category findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(CategoryNotFoundException::new);
    }
}
