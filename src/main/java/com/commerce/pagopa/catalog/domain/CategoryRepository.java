package com.commerce.pagopa.catalog.domain;

import java.util.List;
import java.util.Optional;

import com.commerce.pagopa.global.exception.BusinessException;

import static com.commerce.pagopa.global.response.ErrorCode.CATEGORY_NOT_FOUND;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(Long id);

    List<Category> findAll();

    void deleteById(Long id);

    List<Category> findRootCategories();

    default Category findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
    }

    List<Category> findDescendantsByParent(Long categoryId);
}
