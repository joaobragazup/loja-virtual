package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.NossaLojaVirtualApplicationTest;
import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.products.*;
import br.com.zup.edu.nossalojavirtual.security.ExceptionUtil;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static br.com.zup.edu.nossalojavirtual.purchase.PaymentGateway.PAGSEGURO;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PurchaseControllerTest extends NossaLojaVirtualApplicationTest {

    @Autowired
    private ObjectMapper mapper;

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
    }

    @Test
    @DisplayName("Must register a purchase correctly")
    void test1() throws Exception {

        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), 1, PAGSEGURO);

        String payloadResponse = mockMvc.perform(
                        POST("/api/purchase", newPurchaseRequest)
                                .with(jwt().jwt(jwt -> {
                                                    jwt.claim("email", "joao@zup.com.br");
                                                })
                                                .authorities(new SimpleGrantedAuthority("SCOPE_purchase:write"))
                                )
                )
                .andExpect(
                        status().isOk()
                ).andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);

        Map map = mapper.readValue(payloadResponse, Map.class);

        assertNotNull(map.get("paymentUrl"));

        List<Purchase> purchases = purchaseRepository.findAll();
        assertEquals(1, purchases.size());
    }


    @Test
    @DisplayName("Should not register a purchase when payment gateway and quantity is null")
    void test2() throws Exception {

        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), 0, null);

        Exception exception = mockMvc.perform(
                        POST("/api/purchase", newPurchaseRequest)
                                .with(jwt().jwt(jwt -> {
                                                    jwt.claim("email", "joao@zup.com.br");
                                                })
                                                .authorities(new SimpleGrantedAuthority("SCOPE_purchase:write"))
                                )
                )
                .andExpect(
                        status().isBadRequest()
                ).andReturn().getResolvedException();

        MethodArgumentNotValidException resolvedException = (MethodArgumentNotValidException) exception;

        List<String> errorMessages = resolvedException
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(ExceptionUtil::getFieldAndDefaultErrorMessage)
                .toList();

        List<Purchase> purchases = purchaseRepository.findAll();
        assertEquals(0, purchases.size());
        assertThat(errorMessages, containsInAnyOrder(
                "paymentGateway must not be null",
                "quantity must be greater than or equal to 1"
        ));
    }

    @Test
    @DisplayName("Should not register a purchase when quantity is greater than stock")
    void test3() throws Exception {

        NewPurchaseRequest newPurchaseRequest = new NewPurchaseRequest(product.getId(), 10, PAGSEGURO);

        mockMvc.perform(
                        POST("/api/purchase", newPurchaseRequest)
                                .with(jwt().jwt(jwt -> {
                                                    jwt.claim("email", "joao@zup.com.br");
                                                })
                                                .authorities(new SimpleGrantedAuthority("SCOPE_purchase:write"))
                                )
                )
                .andExpect(
                        status().isBadRequest()
                );

        List<Purchase> purchases = purchaseRepository.findAll();
        assertEquals(0, purchases.size());
    }

    @Test
    @DisplayName("Should not register a purchase without token")
    void test4() throws Exception {
        mockMvc.perform(
                POST("/api/purchase", 1)
        ).andExpect(
                status().isUnauthorized()
        );
    }

    @Test
    @DisplayName("Should not register a purchase without scope token")
    void test5() throws Exception {
        mockMvc.perform(
                POST("/api/purchase", 1)
                        .with(jwt())
        ).andExpect(
                status().isForbidden()
        );
    }

    @Test
    @DisplayName("Should not register a purchase without authenticated user")
    void test6() throws Exception {
        mockMvc.perform(
                POST("/api/purchase", 1)
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_purchase:write")))
        ).andExpect(
                status().isBadRequest()
        );
    }

}