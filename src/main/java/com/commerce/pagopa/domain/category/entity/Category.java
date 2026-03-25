package com.commerce.pagopa.domain.category.entity;

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
    private List<Category> children = new ArrayList<>();

    @Builder(access = AccessLevel.PRIVATE)
    private Category(String name, int depth, Category parent) {
        this.name = name;
        this.depth = depth;
        this.parent = parent;
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
}
