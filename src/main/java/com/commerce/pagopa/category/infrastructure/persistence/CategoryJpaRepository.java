package com.commerce.pagopa.category.infrastructure.persistence;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.category.domain.repository.CategoryRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<Category, Long>, CategoryRepository {

    @Override
    @Query(value =
            "SELECT c " +
            "FROM Category c " +
            "WHERE c.parent is NULL " +
                "AND c.depth = 0")
    List<Category> findRootCategories();
}
