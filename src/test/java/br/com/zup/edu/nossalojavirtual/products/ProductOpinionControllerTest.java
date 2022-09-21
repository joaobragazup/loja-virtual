package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.NossaLojaVirtualApplicationTest;
import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.security.ExceptionUtil;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.NestedServletException;

import java.math.BigDecimal;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class ProductOpinionControllerTest extends NossaLojaVirtualApplicationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    ProductOpinionRepository productOpinionRepository;

    User user;

    Category category;

    PreProduct preProduct;

    List<Photo> photos = new ArrayList<>();

    Set<Characteristic> characteristic = new HashSet<>();

    Product product;

    @AfterEach
    void deleteAll(){
        productOpinionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        productOpinionRepository.deleteAll();
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
    @DisplayName("Must register an opinion for a product ")
    void test1() throws Exception {

        NewOpinionRequest newOpinionRequest = new NewOpinionRequest(3,
                "Like",
                "I liked a lot",
                product.getId()
        );

        mockMvc.perform(
                        POST("/api/opinions", newOpinionRequest)
                                .with(jwt().jwt(jwt -> {
                                                    jwt.claim("email", "joao@zup.com.br");
                                                })
                                                .authorities(new SimpleGrantedAuthority("SCOPE_products:write"))
                                )
                )
                .andExpect(
                        status().isCreated()
                )
                .andExpect(
                        redirectedUrlPattern("/api/opinions/*")
                );

        List<ProductOpinion> productOpinions = productOpinionRepository.findAll();
        assertEquals(1, productOpinions.size());
    }

    @Test
    @DisplayName("Should not register an opinion for a product when data is invalid ")
    void test2() throws Exception {

        NewOpinionRequest newOpinionRequest = new NewOpinionRequest(
                6,
                null,
                "Description does not need to be null",
                product.getId()
        );

        Exception exception = mockMvc.perform(
                        POST("/api/opinions", newOpinionRequest)
                                .with(jwt().jwt(jwt -> {
                                                    jwt.claim("email", "joao@zup.com.br");
                                                })
                                                .authorities(new SimpleGrantedAuthority("SCOPE_products:write"))
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

        List<ProductOpinion> productOpinions = productOpinionRepository.findAll();
        assertEquals(0, productOpinions.size());
        assertThat(errorMessages, containsInAnyOrder(
                "title must not be blank",
                "rating must be between 1 and 5"
        ));

    }

    @Test
    @DisplayName("Should not register an opinion for a product when product id is invalid")
    void test3() throws Exception {

        NewOpinionRequest newOpinionRequest = new NewOpinionRequest(
                5,
                "Good",
                "I liked a lot",
                UUID.randomUUID()
        );

        Exception exception = mockMvc.perform(
                        POST("/api/opinions", newOpinionRequest)
                                .with(jwt().jwt(jwt -> {
                                                    jwt.claim("email", "joao@zup.com.br");
                                                })
                                                .authorities(new SimpleGrantedAuthority("SCOPE_products:write"))
                                )
                )
                .andExpect(
                        status().isBadRequest()
                ).andReturn().getResolvedException();

        MethodArgumentNotValidException resolvedException = (MethodArgumentNotValidException) exception;

        String errorMessages = resolvedException.getFieldError().getDefaultMessage();

        assertEquals("Category productId is not registered",errorMessages);
    }

    //TODO - Verificar Teste com ID Nulo
    @Test
    @DisplayName("Should not register an opinion for a product when product id is nul")
    void test4() throws Exception {
            NewOpinionRequest newOpinionRequest = new NewOpinionRequest(
                    5,
                    "Good",
                    "I liked a lot",
                    null
            );

            Exception exception = mockMvc.perform(
                            POST("/api/opinions", newOpinionRequest)
                                    .with(jwt().jwt(jwt -> {
                                                        jwt.claim("email", "joao@zup.com.br");
                                                    })
                                                    .authorities(new SimpleGrantedAuthority("SCOPE_products:write"))
                                    )
                    )
                    .andExpect(
                            status().isBadRequest()
                    ).andReturn().getResolvedException();

        MethodArgumentNotValidException resolvedException = (MethodArgumentNotValidException) exception;

        String errorMessages = resolvedException.getFieldError().getDefaultMessage();

        assertEquals("must not be null",errorMessages);
    }

    @Test
    @DisplayName("Should not register an opinion for a product when description length is larger than 500 characteres")
    void test5() throws Exception {

        String description501 = "a".repeat(501);

        NewOpinionRequest newOpinionRequest = new NewOpinionRequest(
                5,
                "Good",
                description501,
                product.getId()
        );

        Exception exception = mockMvc.perform(
                        POST("/api/opinions", newOpinionRequest)
                                .with(jwt().jwt(jwt -> {
                                                    jwt.claim("email", "joao@zup.com.br");
                                                })
                                                .authorities(new SimpleGrantedAuthority("SCOPE_products:write"))
                                )
                )
                .andExpect(
                        status().isBadRequest()
                ).andReturn().getResolvedException();

        MethodArgumentNotValidException resolvedException = (MethodArgumentNotValidException) exception;

        String errorMessages = resolvedException.getFieldError().getDefaultMessage();

        assertEquals("length must be between 0 and 500",errorMessages);
    }

    @Test
    @DisplayName("Should not register an opinion for a product without token")
    void test6() throws Exception {
        mockMvc.perform(
                POST("/api/opinions", 1)
        ).andExpect(
                status().isUnauthorized()
        );
    }

    @Test
    @DisplayName("Should not register an opinion for a product without scope token")
    void test7() throws Exception {
        mockMvc.perform(
                POST("/api/opinions", 1)
                        .with(jwt())
        ).andExpect(
                status().isForbidden()
        );
    }

    @Test
    @DisplayName("Should not register an opinion for a product without authenticated user")
    void test8() throws Exception {

        NewOpinionRequest newOpinionRequest = new NewOpinionRequest(
                5,
                "Good",
                "Very Good",
                product.getId()
        );

        Exception exception = mockMvc.perform(
                POST("/api/opinions", 1)
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_products:write")))
        ).andExpect(
                status().isBadRequest()
        ).andReturn().getResolvedException();

        ResponseStatusException resolvedException = (ResponseStatusException) exception;

        String errorMessage = resolvedException.getReason();

        assertEquals("User not authenticated", errorMessage);
    }

}