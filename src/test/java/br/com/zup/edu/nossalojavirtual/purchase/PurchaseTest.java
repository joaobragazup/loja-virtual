package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.NossaLojaVirtualApplicationTest;
import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.products.*;
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

import static br.com.zup.edu.nossalojavirtual.purchase.PaymentGateway.PAGSEGURO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PurchaseTest extends NossaLojaVirtualApplicationTest {
    @Autowired
    PurchaseRepository purchaseRepository;

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

    Purchase purchase;

    Set<Payment> paymentAttempts = new HashSet<>();

    PaymentReturn paymentReturn;


    @AfterEach
    void deleteAll(){
        purchaseRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        purchaseRepository.deleteAll();
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
        this.purchase = new Purchase(user,product,2,PAGSEGURO);
        purchaseRepository.save(purchase);
    }

    @Test
    @DisplayName("Should not register a purchase when throw IllegalArgumentException because of quantity less than 0")
    void test1() {

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            this.purchase = new Purchase(user, product, -5, PAGSEGURO);
            purchaseRepository.save(purchase);
        },"Expected IllegalArgumentException to be thrown, but nothing was thrown.");

        assertEquals("quantity must not be less than 0",thrown.getMessage());
    }

    @Test
    @DisplayName("Should not register a purchase when throw IllegalStateException because processing the same purchase twice ")
    void test2() {

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            this.paymentReturn = new PaymentReturn(purchase.getId(), "1001", "SUCESSO");
            purchase.process(this.paymentReturn);
            purchase.process(this.paymentReturn);
        }, "Expected IllegalStateException to be thrown, but nothing was thrown");

        assertEquals("A finished Purchase cannot be paid again",thrown.getMessage());
    }

    @Test
    @DisplayName("Should not register a purchase when throw IllegalStateException because paying not confirmed ")
    void test3() {

        IllegalStateException thrown = assertThrows(IllegalStateException.class, () -> {
            this.paymentReturn = new PaymentReturn(purchase.getId(), "1001", "ERROR");
            purchase.paymentConfirmedTime();
        }, "Expected IllegalArgumentException to be thrown, but nothing was thrown");

        assertEquals("An unfinished Purchase does not have a payment confirmation timestamp",thrown.getMessage());
    }
}