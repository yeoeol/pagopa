package com.commerce.pagopa.domain.category.repository;

import com.commerce.pagopa.domain.category.entity.Category;
import com.commerce.pagopa.global.exception.CategoryNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    @Query(value =
            "SELECT c " +
            "FROM Category c " +
            "WHERE c.parent is NULL " +
                "AND c.depth = 0")
    List<Category> findRootCategories();

    @Query(value =
            "SELECT c " +
            "FROM Category c " +
            "WHERE c.parent is NULL " +
            "AND c.depth = 0")
    Page<Category> findRootCategories(Pageable pageable);

    default Category getById(Long id) {
        return findById(id).orElseThrow(CategoryNotFoundException::new);
    }
}
