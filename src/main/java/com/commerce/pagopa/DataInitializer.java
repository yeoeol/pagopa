package com.commerce.pagopa;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.category.domain.repository.CategoryRepository;
import com.commerce.pagopa.category.infrastructure.persistence.CategoryJpaRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
@Profile("!prod")
public class DataInitializer implements CommandLineRunner {

    private final CategoryJpaRepository categoryJpaRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) throws Exception {
        if (categoryJpaRepository.count() == 0) {
            Category root1 = Category.createRoot("가구");
            Category child1 = root1.addChild("침대");
            Category child2 = root1.addChild("소파");
            Category leaf1 = child1.addChild("침대프레임");
            Category leaf2 = child2.addChild("일반소파");

            categoryRepository.save(root1);

            Category root2 = Category.createRoot("주방용품");
            Category child3 = root2.addChild("그릇/식기");
            Category child4 = root2.addChild("조리도구");
            Category leaf3 = child3.addChild("대접");
            Category leaf4 = child4.addChild("조리도구세트");

            categoryRepository.save(root2);
        }
    }
}
