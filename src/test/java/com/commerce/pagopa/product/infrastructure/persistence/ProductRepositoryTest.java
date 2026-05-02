package com.commerce.pagopa.product.infrastructure.persistence;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.category.domain.repository.CategoryRepository;
import com.commerce.pagopa.product.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.model.enums.Provider;
import com.commerce.pagopa.user.domain.model.enums.Role;
import com.commerce.pagopa.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    UserRepository userRepository;

    private Category category;
    private User user;
    private List<Product> products = new ArrayList<>();

    @BeforeEach
    void setUp() {
        Category rootCategory = Category.createRoot("루트카테고리");
        Category child1 = rootCategory.createChild("자식카테고리1");
        Category child2 = child1.createChild("자식카테고리2");
        categoryRepository.save(rootCategory);
        categoryRepository.save(child1);
        category = categoryRepository.save(child2);

        User initUser = User.create(
                "emailA",
                "nicknameA",
                "profileImageA",
                Provider.GOOGLE,
                "unique_providerIdA",
                Role.ROLE_SELLER
        );
        user = userRepository.save(initUser);

        Product product1 = Product.create(
                "productA", "descA",
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(500),
                10, category, user
        );
        Product product2 = Product.create(
                "productB", "descB",
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(1000),
                20, category, user
        );
        Product product3 = Product.create(
                "productC", "descC",
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(1500),
                30, category, user
        );
        products.add(productRepository.save(product1));
        products.add(productRepository.save(product2));
        products.add(productRepository.save(product3));
    }

    @Test
    void increaseStock_success() {
        Product product = products.getFirst();

        product.increaseStock(1);
        Optional<Product> optionalProduct = productRepository.findById(product.getId());
        assertThat(optionalProduct).isPresent();

        Product getProduct = optionalProduct.get();
        assertThat(getProduct.getStock()).isEqualTo(11);
    }

    @Test
    void decreaseStock_success() {
        Product product = products.getFirst();

        product.decreaseStock(1);
        Optional<Product> optionalProduct = productRepository.findById(product.getId());
        assertThat(optionalProduct).isPresent();

        Product getProduct = optionalProduct.get();
        assertThat(getProduct.getStock()).isEqualTo(9);
    }

    @Test
    void searchProducts_one() {
        List<Product> results = productRepository.searchProducts(new ProductSearchCondition("A"));
        assertThat(results).hasSize(1);
        assertThat(results.getFirst()).isEqualTo(products.getFirst());
    }

    @Test
    void searchProducts_List() {
        List<Product> results = productRepository.searchProducts(new ProductSearchCondition("roduc"));
        assertThat(results).hasSize(products.size());
    }
}
