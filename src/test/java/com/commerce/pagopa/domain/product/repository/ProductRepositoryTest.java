package com.commerce.pagopa.domain.product.repository;

import com.commerce.pagopa.domain.category.entity.Category;
import com.commerce.pagopa.domain.category.repository.CategoryRepository;
import com.commerce.pagopa.domain.product.entity.Product;
import com.commerce.pagopa.domain.user.entity.User;
import com.commerce.pagopa.domain.user.entity.enums.Provider;
import com.commerce.pagopa.domain.user.entity.enums.Role;
import com.commerce.pagopa.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    }

    @Test
    void increaseStock() {
        Product product = Product.create(
                "productA",
                "descA",
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(500),
                10,
                category,
                user
        );
        Product save = productRepository.save(product);

        int updatedRows = productRepository.increaseStock(save.getId(), 1);
        Optional<Product> optionalProduct = productRepository.findById(save.getId());
        assertThat(optionalProduct).isPresent();
        assertThat(updatedRows).isEqualTo(1);

        Product getProduct = optionalProduct.get();
        assertThat(getProduct.getStock()).isEqualTo(product.getStock()+1);
    }

    @Test
    void decreaseStock() {
        Product product = createProduct(
                "productA", "descA",
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(500),
                10, category, user
        );
        Product save = productRepository.save(product);

        int updatedRows = productRepository.decreaseStock(save.getId(), 1);
        Optional<Product> optionalProduct = productRepository.findById(save.getId());
        assertThat(optionalProduct).isPresent();
        assertThat(updatedRows).isEqualTo(1);

        Product getProduct = optionalProduct.get();
        assertThat(getProduct.getStock()).isEqualTo(product.getStock()-1);
    }

    @Test
    void searchProducts() {
        Product product1 = createProduct(
                "productA", "descA",
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(500),
                10, category, user
        );
        Product product2 = createProduct(
                "productB", "descB",
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(1000),
                20, category, user
        );
        Product product3 = createProduct(
                "productC", "descC",
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(1500),
                30, category, user
        );
        productRepository.saveAll(List.of(product1, product2, product3));

        List<Product> results = productRepository.searchProducts("A");
        assertThat(results).hasSize(1);
        assertThat(results.getFirst()).isEqualTo(product1);
    }

    @Test
    void searchProducts_List() {
        Product product1 = createProduct(
                "productA", "descA",
                BigDecimal.valueOf(1000),
                BigDecimal.valueOf(500),
                10, category, user
        );
        Product product2 = createProduct(
                "productB", "descB",
                BigDecimal.valueOf(2000),
                BigDecimal.valueOf(1000),
                20, category, user
        );
        Product product3 = createProduct(
                "productC", "descC",
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(1500),
                30, category, user
        );
        productRepository.saveAll(List.of(product1, product2, product3));

        List<Product> results = productRepository.searchProducts("roduc");
        assertThat(results).hasSize(3);
    }

    private Product createProduct(
            String name, String description,
            BigDecimal price, BigDecimal discountPrice,
            int stock, Category category, User seller
    ) {
        return Product.create(name, description, price, discountPrice, stock, category, seller);
    }
}