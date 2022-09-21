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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerTest extends NossaLojaVirtualApplicationTest {
    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    PhotoUploader photoUploader;

    @Autowired
    UserRepository userRepository;

    Category category;

    User user;

    List<String> photos = new ArrayList<>();

    List<NewCharacteristicRequest> characteristicRequests = new ArrayList<>();

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
        this.category = new Category("Computers");
        categoryRepository.save(category);
        this.user = new User("joao@zup.com.br", Password.encode("123456"));
        userRepository.save(user);
        this.photos = List.of(
                "google.com.br/images/inteli5-front",
                "google.com.br/images/inteli5-back",
                "google.com.br/images/inteli5-360"
        );

        this.characteristicRequests = List.of(
                new NewCharacteristicRequest("Nice1", "Nice Notebook1"),
                new NewCharacteristicRequest("Nice2", "Nice Notebook2"),
                new NewCharacteristicRequest("Nice3", "Nice Notebook3")
        );
    }

    @Test
    @DisplayName("Must register a product correctly")
    void test1() throws Exception {
        NewProductRequest newProductRequest = new NewProductRequest(
                "Intel i5",
                BigDecimal.valueOf(1500),
                5,
                photos,
                characteristicRequests,
                "Best Note",
                category.getId()
        );

        mockMvc.perform(
                        POST("/api/products", newProductRequest)
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
                        redirectedUrlPattern("/api/products/*")
                );

        List<Product> products = productRepository.findAll();
        assertEquals(1, products.size());
    }

    @Test
    @DisplayName("Should not register a product with missing data")
    void test2() throws Exception {
        NewProductRequest newProductRequest = new NewProductRequest(
                null,
                BigDecimal.valueOf(1500.00),
                5,
                photos,
                characteristicRequests,
                null,
                category.getId()
        );

        Exception exception = mockMvc.perform(
                        POST("/api/products", newProductRequest)
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


        List<Product> products = productRepository.findAll();

        assertEquals(0, products.size());
        assertThat(errorMessages, containsInAnyOrder(
                "name must not be blank",
                "description must not be blank"
        ));
    }

    @Test
    @DisplayName("Should not register a product with a non-existent category id")
    void test3() throws Exception {
        NewProductRequest newProductRequest = new NewProductRequest(
                "Notebook",
                BigDecimal.valueOf(1500),
                5,
                photos,
                characteristicRequests,
                "Best Notebook Ever",
                5L
        );

        Exception exception = mockMvc.perform(
                        POST("/api/products", newProductRequest)
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

        assertEquals(errorMessages, "Category categoryId is not registered");
    }

    @Test
    @DisplayName("Should not register a product with photo size less than 1 and characterisct size less than 3")
    void test4() throws Exception {

        List<String> photos = List.of();

        List<NewCharacteristicRequest> characteristicRequests = List.of();

        NewProductRequest newProductRequest = new NewProductRequest(
                "Notebook",
                BigDecimal.valueOf(1500),
                5,
                photos,
                characteristicRequests,
                "The Best Notebook",
                category.getId()
        );

        Exception exception = mockMvc.perform(
                        POST("/api/products", newProductRequest)
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


        List<Product> products = productRepository.findAll();

        assertEquals(0, products.size());
        assertThat(errorMessages, containsInAnyOrder(
                "photos size must be between 1 and 2147483647",
                "characteristics size must be between 3 and 2147483647"
        ));
    }

    @Test
    @DisplayName("Should not register a product with negative stock and price")
    void test5() throws Exception {
        NewProductRequest newProductRequest = new NewProductRequest(
                "Notebook",
                BigDecimal.valueOf(-1500),
                -5,
                photos,
                characteristicRequests,
                "The best notebook",
                category.getId()
        );

        Exception exception = mockMvc.perform(
                        POST("/api/products", newProductRequest)
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


        List<Product> products = productRepository.findAll();

        assertEquals(0, products.size());
        assertThat(errorMessages, containsInAnyOrder(
                "price must be greater than or equal to 0.01",
                "stockQuantity must be greater than or equal to 0"
        ));
    }

    @Test
    @DisplayName("Should not register a product without token")
    void test6() throws Exception {
        mockMvc.perform(
                POST("/api/products", 1)
        ).andExpect(
                status().isUnauthorized()
        );
    }

    @Test
    @DisplayName("Should not register a product without scope token")
    void test7() throws Exception {
        mockMvc.perform(
                POST("/api/products", 1)
                        .with(jwt())
        ).andExpect(
                status().isForbidden()
        );
    }

    @Test
    @DisplayName("Should not register a product without authenticated user")
    void test8() throws Exception {
        mockMvc.perform(
                POST("/api/products", 1)
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_products:write")))
        ).andExpect(
                status().isBadRequest()
        );
    }
}