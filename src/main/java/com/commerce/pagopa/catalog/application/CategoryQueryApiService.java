package com.commerce.pagopa.catalog.application;

import java.util.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.commerce.pagopa.catalog.api.CategoryOption;
import com.commerce.pagopa.catalog.api.CategoryQueryApi;
import com.commerce.pagopa.catalog.domain.Category;
import com.commerce.pagopa.catalog.domain.CategoryRepository;

@Service
@RequiredArgsConstructor
public class CategoryQueryApiService implements CategoryQueryApi {

    private final CategoryRepository categoryRepository;

    private static final Comparator<Category> CATEGORY_ORDER = Comparator.comparing(Category::getName)
            .thenComparing(Category::getId);

    @Override
    @Transactional(readOnly = true)
    public List<CategoryOption> findProductCategoryOptions() {
        List<Category> categories = categoryRepository.findAll();

        Map<Long, List<Category>> childrenByParentId = groupChildrenByParentId(categories);

        List<CategoryOption> options = new ArrayList<>();

        appendOptions(
                null,
                null,
                childrenByParentId,
                options
        );

        return List.copyOf(options);
    }

    private Map<Long, List<Category>> groupChildrenByParentId(List<Category> categories) {
        Map<Long, List<Category>> childrenByParentId = new HashMap<>();

        categories.forEach(category -> {
            Long parentId = category.getParent() == null
                    ? null
                    : category.getParent()
                            .getId();
            childrenByParentId.computeIfAbsent(
                    parentId,
                    ignored -> new ArrayList<>()
            )
                    .add(category);
        });

        childrenByParentId.values()
                .forEach(children -> children.sort(CATEGORY_ORDER));

        return childrenByParentId;
    }

    private void appendOptions(
            Long parentId,
            String parentPath,
            Map<Long, List<Category>> childrenByParentId,
            List<CategoryOption> options
    ) {
        List<Category> children = childrenByParentId.getOrDefault(
                parentId,
                List.of()
        );

        children.forEach(category -> {
            String path = parentPath == null ? category.getName() : parentPath + " > " + category.getName();

            options.add(
                    new CategoryOption(
                            category.getId(),
                            path
                    )
            );

            appendOptions(
                    category.getId(),
                    path,
                    childrenByParentId,
                    options
            );
        });
    }
}
