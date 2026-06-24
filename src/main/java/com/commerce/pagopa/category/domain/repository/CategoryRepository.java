package com.commerce.pagopa.category.domain.repository;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.global.exception.BusinessException;

import java.util.List;
import java.util.Optional;

import static com.commerce.pagopa.global.response.ErrorCode.CATEGORY_NOT_FOUND;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(Long id);

    void deleteById(Long id);

    List<Category> findRootCategories();

    default Category findByIdOrThrow(Long id) {
        return findById(id).orElseThrow(() -> new BusinessException(CATEGORY_NOT_FOUND));
    }
}
