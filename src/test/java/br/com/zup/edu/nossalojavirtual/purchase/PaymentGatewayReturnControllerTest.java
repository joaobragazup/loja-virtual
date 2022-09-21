package br.com.zup.edu.nossalojavirtual.purchase;

import br.com.zup.edu.nossalojavirtual.NossaLojaVirtualApplicationTest;
import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.products.*;
import br.com.zup.edu.nossalojavirtual.security.ExceptionUtil;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static br.com.zup.edu.nossalojavirtual.purchase.PaymentGateway.PAGSEGURO;
import static br.com.zup.edu.nossalojavirtual.purchase.PaymentGateway.PAYPAL;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class PaymentGatewayReturnControllerTest extends NossaLojaVirtualApplicationTest {

    @Mock
    SendPurchaseConfirmationToInvoiceSystem sendPurchaseConfirmationToInvoiceSystem;
    @Mock
    SendPurchaseConfirmationToSellersSystem sendPurchaseConfirmationToSellersSystem;
    @Mock
    SendPurchaseEmailConfirmation sendPurchaseEmailConfirmation;
    @Mock
    SendPurchaseFailEmail sendPurchaseFailEmail;

    @SpyBean
    PaymentGatewayReturnController paymentGatewayReturnController;

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

    PaymentReturn paymentReturn;

    Purchase purchase;

    @AfterEach()
    void deleteAll(){
        purchaseRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
//        MockitoAnnotations.initMocks(this);
//        this.mockMvc = MockMvcBuilders.standaloneSetup(paymentGatewayReturnController).build();
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
        this.purchase = new Purchase(user, product, 2, PAGSEGURO);
        purchaseRepository.save(purchase);
    }

    @Test
    @DisplayName("Must register a payment return correctly with PAGSEGURO status equals SUCESS")
    void test1() throws Exception {

        this.paymentReturn = new PaymentReturn(purchase.getId(), "1000", "SUCESSO");

        mockMvc.perform(POST("/api/purchases/confirm-payment", paymentReturn)
                        .with(jwt().jwt(jwt -> {
                                            jwt.claim("email", "joao@zup.com.br");
                                        })
                                        .authorities(new SimpleGrantedAuthority("SCOPE_purchase:write"))
                        )
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Must register a payment return correctly with PAYPAL status equals SUCESS")
    void test2() throws Exception {
        this.purchase = new Purchase(user,product,2,PAYPAL);
        purchaseRepository.save(purchase);

        this.paymentReturn = new PaymentReturn(purchase.getId(), "1000", "2");

        mockMvc.perform(POST("/api/purchases/confirm-payment", paymentReturn)
                        .with(jwt().jwt(jwt -> {
                                            jwt.claim("email", "joao@zup.com.br");
                                        })
                                        .authorities(new SimpleGrantedAuthority("SCOPE_purchase:write"))
                        )
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Must register a payment return correctly with PAGSEGURO status equals SUCESS")
    void test3() throws Exception {

        this.paymentReturn = new PaymentReturn(purchase.getId(), "1000", "ERROR");

        mockMvc.perform(POST("/api/purchases/confirm-payment", paymentReturn)
                        .with(jwt().jwt(jwt -> {
                                            jwt.claim("email", "joao@zup.com.br");
                                        })
                                        .authorities(new SimpleGrantedAuthority("SCOPE_purchase:write"))
                        )
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Must register a payment return correctly with PAYPAL status equals SUCESS")
    void test4() throws Exception {
        this.purchase = new Purchase(user,product,2,PAYPAL);
        purchaseRepository.save(purchase);

        this.paymentReturn = new PaymentReturn(purchase.getId(), "1000", "1");

        mockMvc.perform(POST("/api/purchases/confirm-payment", paymentReturn)
                        .with(jwt().jwt(jwt -> {
                                            jwt.claim("email", "joao@zup.com.br");
                                        })
                                        .authorities(new SimpleGrantedAuthority("SCOPE_purchase:write"))
                        )
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Should not register a payment return with null paymentId and status")
    void test5() throws Exception {

        this.paymentReturn = new PaymentReturn(purchase.getId(), null, null);

        Exception exception = mockMvc.perform(POST("/api/purchases/confirm-payment", paymentReturn)
                        .with(jwt().jwt(jwt -> {
                                            jwt.claim("email", "joao@zup.com.br");
                                        })
                                        .authorities(new SimpleGrantedAuthority("SCOPE_purchase:write"))
                        )
                )
                .andExpect(status().isBadRequest()).andReturn().getResolvedException();

        MethodArgumentNotValidException resolvedException = (MethodArgumentNotValidException) exception;

        List<String> errorMessages = resolvedException
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(ExceptionUtil::getFieldAndDefaultErrorMessage)
                .toList();

        assertThat(errorMessages, containsInAnyOrder(
                "paymentId must not be blank",
                "status must not be blank"
        ));
    }

    @Test
    @DisplayName("Should not register a payment return with invalid purchase id")
    void test6() throws Exception {

        this.paymentReturn = new PaymentReturn(9999L, "1001", "SUCESSO");

        Exception exception = mockMvc.perform(POST("/api/purchases/confirm-payment", paymentReturn)
                        .with(jwt().jwt(jwt -> {
                                            jwt.claim("email", "joao@zup.com.br");
                                        })
                                        .authorities(new SimpleGrantedAuthority("SCOPE_purchase:write"))
                        )
                )
                .andExpect(status().isBadRequest()).andReturn().getResolvedException();

        MethodArgumentNotValidException resolvedException = (MethodArgumentNotValidException) exception;

        String errorMessages = resolvedException.getFieldError().getDefaultMessage();

        assertEquals(errorMessages,"Category purchaseId is not registered");
    }

    @Test
    @DisplayName("Should not return a payment gateway without token")
    void test7() throws Exception {
        mockMvc.perform(
                POST("/api/purchases/confirm-payment", 99999)
        ).andExpect(
                status().isUnauthorized()
        );
    }

    @Test
    @DisplayName("Should not return a payment gateway without scope token")
    void test8() throws Exception {
        mockMvc.perform(
                POST("/api/purchases/confirm-payment", 9999)
                        .with(jwt())
        ).andExpect(
                status().isForbidden()
        );
    }

    @Test
    @DisplayName("Should not return a payment gateway without authenticated user")
    void test9() throws Exception {
        mockMvc.perform(
                POST("/api/purchases/confirm-payment", null)
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_purchase:write")))
        ).andExpect(
                status().isBadRequest()
        );
    }
}