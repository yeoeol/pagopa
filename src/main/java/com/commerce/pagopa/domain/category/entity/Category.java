package com.commerce.pagopa.domain.category.entity;

import com.commerce.pagopa.global.exception.BusinessException;
import com.commerce.pagopa.global.response.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    // 0:대분류, 1:중분류, 2:소분류
    @Column(nullable = false)
    private int depth;

    // Self-join
    // 대분류(depth=0) -> parent가 null;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Category> children = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Category(String name, int depth, Category parent) {
        this.name = name;
        this.depth = depth;
        this.parent = parent;
    }

    public static Category createRoot(String name) {
        return Category.builder()
                .name(name)
                .depth(0)
                .parent(null)
                .build();
    }

    public Category createChild(String name) {
        Category child = Category.builder()
                .name(name)
                .depth(this.depth + 1)
                .parent(this)
                .build();

        if (child.isGreaterThanLeaf()) {
            throw new BusinessException(ErrorCode.INVALID_CATEGORY_LEVEL_REQUEST);
        }
        this.addChild(child);

        return child;
    }

    private boolean isGreaterThanLeaf() {
        return this.depth > 2;
    }

    public void addChild(Category child) {
        this.children.add(child);
        child.assignParent(this);
    }

    private void assignParent(Category parent) {
        this.parent = parent;
    }

    // 소분류(depth=2) 검증
    public boolean isLeaf() {
        return this.depth == 2;
    }

    // 대분류(depth=0) 검증
    public boolean isRoot() {
        return this.depth == 0;
    }

    public void update(String name) {
        this.name = name;
    }
}
