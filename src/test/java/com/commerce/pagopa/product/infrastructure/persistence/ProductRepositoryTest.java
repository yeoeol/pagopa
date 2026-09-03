package com.commerce.pagopa.product.infrastructure.persistence;

import com.commerce.pagopa.category.domain.model.Category;
import com.commerce.pagopa.category.domain.repository.CategoryRepository;
import com.commerce.pagopa.product.application.dto.request.ProductSearchCondition;
import com.commerce.pagopa.product.domain.model.Product;
import com.commerce.pagopa.product.domain.model.enums.ProductStatus;
import com.commerce.pagopa.product.domain.repository.ProductRepository;
import com.commerce.pagopa.role.domain.model.Role;
import com.commerce.pagopa.role.domain.repository.RoleRepository;
import com.commerce.pagopa.seller.domain.model.Seller;
import com.commerce.pagopa.seller.domain.repository.SellerRepository;
import com.commerce.pagopa.support.fixture.*;
import com.commerce.pagopa.support.fixture.CategoryFixture.CategoryTree;
import com.commerce.pagopa.support.testcontainers.TestcontainersConfig;
import com.commerce.pagopa.user.domain.model.User;
import com.commerce.pagopa.user.domain.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
@SpringBootTest
@Import(TestcontainersConfig.class)
class ProductRepositoryTest {

    @Autowired
    ProductRepository productRepository;
    @Autowired
    CategoryRepository categoryRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
	RoleRepository roleRepository;
    @Autowired
    SellerRepository sellerRepository;

    private Category rootCategory;
    private Category middleCategory;
    private Category category;
    private User user;
    private final List<Product> products = new ArrayList<>();

    @BeforeEach
    void setUp() {
        roleRepository.save(RoleFixture.aRoleAdmin());
        Role sellerRole = roleRepository.save(RoleFixture.aRoleSeller());
        Role userRole = roleRepository.save(RoleFixture.aRoleUser());

        CategoryTree tree = CategoryFixture.aTree();
        categoryRepository.save(tree.root());
        rootCategory = tree.root();
        category = tree.leaf();
        middleCategory = category.getParent();

        user = UserFixture.aUser("product-repo-test");
        user.addUserRole(UserRoleFixture.aUserRole(user, userRole));
        user.addUserRole(UserRoleFixture.aUserRole(user, sellerRole));
        userRepository.save(user);

        Seller seller = sellerRepository.save(SellerFixture.aSeller(user));

        // 검색 테스트가 productA/B/C name으로 매칭하므로 fixture 디폴트 대신 명시 생성
        Product product1 = ProductFixture.aProduct(
                "productA",
                "descA",
                1000,
                10,
                category,
                seller
        );
        Product product2 = ProductFixture.aProduct(
                "productB",
                "descB",
                2000,
                20,
                category,
                seller
        );
        Product product3 = ProductFixture.aProduct(
                "productC",
                "descC",
                3000,
                30,
                category,
                seller
        );
        products.add(productRepository.save(product1));
        products.add(productRepository.save(product2));
        products.add(productRepository.save(product3));
    }

    @Test
    void searchProducts_one() {
        List<Product> results = productRepository.searchProducts(
                new ProductSearchCondition("A")
        );
        assertThat(results).hasSize(1);
        assertThat(results.getFirst()).isEqualTo(products.getFirst());
    }

    @Test
    void searchProducts_List() {
        List<Product> results = productRepository.searchProducts(
                new ProductSearchCondition("roduc")
        );
        assertThat(results).hasSize(products.size());
    }

    @Test
    void findAllByCategoryOrAncestorCategoryIdAndStatusIn_rootCategory() {
        Page<Product> results = productRepository.findAllByCategoryOrAncestorCategoryIdAndStatusIn(
                rootCategory.getId(),
                List.of(ProductStatus.ACTIVE, ProductStatus.SOLD_OUT),
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent())
                .extracting(Product::getId)
                .containsExactlyInAnyOrderElementsOf(productIds());
    }

    @Test
    void findAllByCategoryOrAncestorCategoryIdAndStatusIn_middleCategory() {
        Page<Product> results = productRepository.findAllByCategoryOrAncestorCategoryIdAndStatusIn(
                middleCategory.getId(),
                List.of(ProductStatus.ACTIVE, ProductStatus.SOLD_OUT),
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent())
                .extracting(Product::getId)
                .containsExactlyInAnyOrderElementsOf(productIds());
    }

    @Test
    void findAllByCategoryOrAncestorCategoryIdAndStatusIn_leafCategory() {
        Page<Product> results = productRepository.findAllByCategoryOrAncestorCategoryIdAndStatusIn(
                category.getId(),
                List.of(ProductStatus.ACTIVE, ProductStatus.SOLD_OUT),
                PageRequest.of(0, 10)
        );

        assertThat(results.getContent())
                .extracting(Product::getId)
                .containsExactlyInAnyOrderElementsOf(productIds());
    }

    @Test
    void findAllByCategoryOrAncestorCategoryIdAndStatusIn_notFoundCategory_returnsEmptyPage() {
        Page<Product> results = productRepository.findAllByCategoryOrAncestorCategoryIdAndStatusIn(
                -1L,
                List.of(ProductStatus.ACTIVE, ProductStatus.SOLD_OUT),
                PageRequest.of(0, 10)
        );

        assertThat(results).isEmpty();
    }

    private List<Long> productIds() {
        return products.stream()
                .map(Product::getId)
                .toList();
    }
}
