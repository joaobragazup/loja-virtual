package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.NossaLojaVirtualApplicationTest;
import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.purchase.Purchase;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ProductOpinionTest extends NossaLojaVirtualApplicationTest {

    @Autowired
    ProductOpinionRepository productOpinionRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    ProductOpinion productOpinion;

    User user;

    Category category;

    PreProduct preProduct;

    List<Photo> photos = new ArrayList<>();

    Set<Characteristic> characteristic = new HashSet<>();

    Product product;

    @AfterEach
    void deleteAll() {
        productOpinionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
        this.user = new User("joao@zup.com.br", Password.encode("123456"));
        userRepository.save(user);
        this.category = new Category("Games");
        categoryRepository.save(category);
        this.preProduct = new PreProduct(user, category, "Xbox", BigDecimal.valueOf(300), 5, "xbox game");
        this.photos = List.of(
                new Photo("bit.ly/cats"),
                new Photo("bit.ly/dogs"),
                new Photo("bit.ly/birds")
        );
        this.characteristic = Set.of(
                new Characteristic("Good", "Very Good"),
                new Characteristic("Play Hard", "Nice Gameplay"),
                new Characteristic("Experience", "Best Experience Ever")
        );
        this.product = new Product(preProduct, photos, characteristic);
        productRepository.save(product);
    }

    @Test
    @DisplayName("Should not register a product opinion when throw IllegalArgumentException because rating need to be between 1 and 5 ")
    void test1() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.productOpinion = new ProductOpinion(9, "Ok", "A", product, user);
        }, "Expected IllegalArgumentException to be thrown, but nothing was thrown.");

        assertEquals("rating must be between 1 and 5", exception.getMessage());
    }

    @Test
    @DisplayName("Should not register a product opinion when throw IllegalArgumentException because length description")
    void test2() {

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.productOpinion = new ProductOpinion(5, "Ok", "A".repeat(1000), product, user);
        }, "Expected IllegalArgumentException to be thrown, but nothing was thrown.");

        assertEquals("description must have at most 500 characters", exception.getMessage());

    }
}