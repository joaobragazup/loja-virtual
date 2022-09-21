package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.NossaLojaVirtualApplicationTest;
import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest extends NossaLojaVirtualApplicationTest {
    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    UserRepository userRepository;

    User user;

    Category category;

    PreProduct preProduct;

    List<Photo> photos = new ArrayList<>();

    Set<Characteristic> characteristic = new HashSet<>();

    Product product;

    @AfterEach
    void deleteAll(){
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
                new Characteristic("Looks good", "I liked")
        );
    }


    @Test
    @DisplayName("Should not register a product when throw IllegalArgumentException because of characteristic less than 3")
    void test1() {
        this.characteristic = Set.of(
                new Characteristic("Good", "Very Good"),
                new Characteristic("Play Hard", "Nice Gameplay")
        );

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.product = new Product(preProduct, photos, characteristic);
            productRepository.save(product);
        }, "Expected IllegalArgumentException to be thrown, but nothing was thrown.");

        assertEquals(exception.getMessage(), "product must have at least three characteristics");
    }


    @Test
    @DisplayName("Should not register a product when throw IllegalArgumentException because of price equals 0")
    void test2() {
        this.preProduct = new PreProduct(user, category, "Xbox", BigDecimal.ZERO, 5, "xbox game");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.product = new Product(preProduct, photos, characteristic);
            productRepository.save(product);
        }, "Expected IllegalArgumentException to be thrown, but nothing was thrown.");


        assertEquals(exception.getMessage(), "price must not be negative or zero");
    }

    @Test
    @DisplayName("Should not register a product when throw IllegalArgumentException because of stock quantity value less than 0")
    void test3() {
        this.preProduct = new PreProduct(user, category, "Xbox", BigDecimal.TEN, -1, "xbox game");

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            this.product = new Product(preProduct, photos, characteristic);
            productRepository.save(product);
        }, "Expected IllegalArgumentException to be thrown, but nothing was thrown.");


        assertEquals(exception.getMessage(), "stockQuantity must not be negative");
    }
}