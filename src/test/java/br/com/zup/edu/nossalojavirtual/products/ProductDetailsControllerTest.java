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
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductDetailsControllerTest extends NossaLojaVirtualApplicationTest {
    @Autowired
    UserRepository userRepository;

    @Autowired
    QuestionRepository questionRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CategoryRepository categoryRepository;

    @Autowired
    ProductOpinionRepository productOpinionRepository;

    User user;

    Category category;

    PreProduct preProduct;

    List<Photo> photos = new ArrayList<>();

    Set<Characteristic> characteristic = new HashSet<>();

    Product product;

    Question question;

    ProductOpinion productOpinion;

    @AfterEach
    void deleteAll(){
        questionRepository.deleteAll();
        productOpinionRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
        questionRepository.deleteAll();
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
        this.productOpinion = new ProductOpinion(5,"Nice Computer","I liked this computer a lot",this.product,this.user);
        productOpinionRepository.save(this.productOpinion);
        this.question = new Question("This is a nice notebook?",this.user,this.product);
        questionRepository.save(question);
    }

    @Test
    @DisplayName("Must detail the product")
    void test1() throws Exception{

        mockMvc.perform(GET("/api/products/{id}", product.getId())
                        .with(jwt().jwt(jwt -> {
                                            jwt.claim("email", "joao@zup.com.br");
                                        })
                                        .authorities(new SimpleGrantedAuthority("SCOPE_products:read"))
                        ))
                .andExpect(status().isOk());

        List<Question> questions = questionRepository.findAll();
        List<Product> products = productRepository.findAll();
        List<ProductOpinion> opinions = productOpinionRepository.findAll();

        assertEquals(1, products.size());
        assertEquals(1,opinions.size());
        assertEquals(1,questions.size());
        assertEquals(300.00,product.getPrice().doubleValue());
        assertEquals("xbox game",product.getDescription());
        assertEquals(5,productOpinion.getRating());
        assertEquals(5,product.getStockQuantity());
        assertEquals(3,product.getPhotos().size());
        assertEquals(3,product.getCharacteristics().size());
        assertEquals(1,product.getCategoriesHierarchy().size());
    }

    @Test
    @DisplayName("Should not detail the product if product Id is invalid")
    void test2() throws Exception{

        mockMvc.perform(GET("/api/products/{id}", UUID.randomUUID())
                        .with(jwt().jwt(jwt -> {
                                            jwt.claim("email", "joao@zup.com.br");
                                        })
                                        .authorities(new SimpleGrantedAuthority("SCOPE_products:read"))
                        ))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$").doesNotExist());

    }

    @Test
    @DisplayName("Should not detail a product without token")
    void test3() throws Exception {

        mockMvc.perform(
                GET("/api/products/{id}", product.getId())
        ).andExpect(
                status().isUnauthorized()
        );
    }

    @Test
    @DisplayName("Should not detail a product without scope token")
    void test4() throws Exception {

        mockMvc.perform(
                GET("/api/products/{id}", product.getId())
                        .with(jwt())
        ).andExpect(
                status().isForbidden()
        );
    }

}