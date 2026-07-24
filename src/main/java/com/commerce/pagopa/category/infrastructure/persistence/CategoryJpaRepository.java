package com.commerce.pagopa.category.infrastructure.persistence;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.category.domain.repository.CategoryRepository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CategoryJpaRepository extends JpaRepository<Category, Long>, CategoryRepository {

    @Override
    @Query(value =
            "SELECT c " +
            "FROM Category c " +
            "WHERE c.parent is NULL")
    List<Category> findRootCategories();

    @Override
    @Query(value = """
		WITH RECURSIVE descendants AS (
			SELECT c.category_id, c.parent_id, c.name,
			       c.created_at, c.updated_at, 1 AS depth
			FROM category c
			WHERE c.parent_id = :parentId

			UNION ALL

			SELECT c.category_id, c.parent_id, c.name,
			       c.created_at, c.updated_at, d.depth + 1
			FROM category c
			JOIN descendants d ON c.parent_id = d.category_id
		)
		SELECT d.category_id, d.parent_id, d.name,
		       d.created_at, d.updated_at,
		FROM descendants d
		ORDER BY d.depth, d.category_id
	""", nativeQuery = true)
    List<Category> findDescendantsByParent(@Param("parentId") Long parentId);
}
