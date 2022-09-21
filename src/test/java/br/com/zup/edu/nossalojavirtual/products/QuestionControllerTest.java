package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.NossaLojaVirtualApplicationTest;
import br.com.zup.edu.nossalojavirtual.categories.Category;
import br.com.zup.edu.nossalojavirtual.categories.CategoryRepository;
import br.com.zup.edu.nossalojavirtual.security.ExceptionUtil;
import br.com.zup.edu.nossalojavirtual.shared.email.EmailRepository;
import br.com.zup.edu.nossalojavirtual.users.Password;
import br.com.zup.edu.nossalojavirtual.users.User;
import br.com.zup.edu.nossalojavirtual.users.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


class QuestionControllerTest extends NossaLojaVirtualApplicationTest {

    @Autowired
    UserRepository userRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    EmailRepository emailRepository;

    User user;

    Category category;

    PreProduct preProduct;

    List<Photo> photos = new ArrayList<>();

    Set<Characteristic> characteristic = new HashSet<>();

    Product product;

    @Autowired
    ObjectMapper mapper;

    @AfterEach
    void deleteAll(){
        emailRepository.deleteAll();
        questionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        emailRepository.deleteAll();
        questionRepository.deleteAll();
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
        productRepository.save(this.product);
    }


    @Test
    @DisplayName("Must register a question to a product correctly")
    void test1() throws Exception {

        NewQuestionRequest newQuestionRequest = new NewQuestionRequest("Why this computer is better than AMD?");

        String payloadResponse = mockMvc.perform(
                        POST("/api/products/{id}/questions", newQuestionRequest, product.getId())
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
                        redirectedUrlPattern("/api/products/*/questions/*")
                ).andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        TypeFactory typeFactory = mapper.getTypeFactory();
        List<QuestionResponse> questionResponse = mapper.readValue(payloadResponse, typeFactory.constructCollectionType(List.class, QuestionResponse.class));
        List<Question> questions = questionRepository.findAll();

        assertEquals(newQuestionRequest.getTitle(), questionResponse.get(0).getTitle());
        assertEquals(user.getUsername(), questionResponse.get(0).getUser());
        assertTrue(questionResponse.get(0).getCreatedAt().isBefore(LocalDateTime.now()));
        assertNotNull(payloadResponse);
        assertEquals(1, questions.size());
    }

    @Test
    @DisplayName("Should not register a question to a product when question is blank/empty/null")
    void test2() throws Exception {

        NewQuestionRequest newQuestionRequest = new NewQuestionRequest(null);

        Exception exception = mockMvc.perform(
                        POST("/api/products/{id}/questions", newQuestionRequest, this.product.getId())
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

        List<Question> questions = questionRepository.findAll();
        assertEquals(0, questions.size());

        assertThat(errorMessages, containsInAnyOrder(
                "title must not be blank"
        ));
    }

    @Test
    @DisplayName("Should not register a question to a product when possible product is empty")
    void test3() throws Exception {

        NewQuestionRequest newQuestionRequest = new NewQuestionRequest("Are you ok?");

        mockMvc.perform(
                        POST("/api/products/{id}/questions", newQuestionRequest, UUID.randomUUID())
                                .with(jwt().jwt(jwt -> {
                                                    jwt.claim("email", "joao@zup.com.br");
                                                })
                                                .authorities(new SimpleGrantedAuthority("SCOPE_products:write"))
                                )
                )
                .andExpect(
                        status().isNotFound());


        List<Question> questions = questionRepository.findAll();
        assertEquals(0, questions.size());
    }

    @Test
    @DisplayName("Should not register a question to a product without token")
    void test4() throws Exception {

        mockMvc.perform(
                POST("/api/products/{id}/questions", 1, this.product.getId())
        ).andExpect(
                status().isUnauthorized()
        );
    }

    @Test
    @DisplayName("Should not register a question to a product without scope token")
    void test5() throws Exception {

        mockMvc.perform(
                POST("/api/products/{id}/questions", 1, this.product.getId())
                        .with(jwt())
        ).andExpect(
                status().isForbidden()
        );
    }

    @Test
    @DisplayName("Should not register a question to a product without authenticated user")
    void test6() throws Exception {
        NewQuestionRequest newQuestionRequest = new NewQuestionRequest("Are you ok?");

        Exception exception = mockMvc.perform(
                POST("/api/products/{id}/questions", newQuestionRequest, this.product.getId())
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_products:write")))
        ).andExpect(
                status().isForbidden()
        ).andReturn().getResolvedException();

        ResponseStatusException resolvedException = (ResponseStatusException) exception;

        String errorMessage = resolvedException.getReason();

        assertEquals("User not authenticated", errorMessage);
    }

}