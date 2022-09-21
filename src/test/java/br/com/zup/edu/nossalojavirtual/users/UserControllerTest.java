package br.com.zup.edu.nossalojavirtual.users;

import br.com.zup.edu.nossalojavirtual.NossaLojaVirtualApplicationTest;
import br.com.zup.edu.nossalojavirtual.security.ExceptionUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class   UserControllerTest extends NossaLojaVirtualApplicationTest {
    @Autowired
    private UserRepository userRepository;


    @AfterEach
    void deleteAll(){
        userRepository.deleteAll();
    }

    @BeforeEach
    void setUp() {
       this.userRepository.deleteAll();
    }

    @Test
    @DisplayName("Must register a user correctly")
    void test1() throws Exception {
        var user = new NewUserRequest("joao.sbraga@hotmail.com", "123456");
        Password password = Password.encode(user.getPassword());

        mockMvc.perform(
                POST("/api/users", user)
        ).andExpect(
                status().isCreated()
        ).andExpect(
                redirectedUrlPattern("/api/users/*")
        );

        List<User> usuarios = userRepository.findAll();
        assertEquals(1,usuarios.size());
    }

    @Test
    @DisplayName("Should not register a with invalid data")
    void test2() throws Exception {
        var user = new NewUserRequest("", "");
        Password password = Password.encode(user.getPassword());

        Exception exception = mockMvc.perform(
                POST("/api/users", user)
        ).andExpect(
                status().isBadRequest()
        ).andReturn().getResolvedException();

        MethodArgumentNotValidException resolvedException = (MethodArgumentNotValidException) exception;

        List<String> errorMessages = resolvedException
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(ExceptionUtil::getFieldAndDefaultErrorMessage)
                .toList();

        assertThat(errorMessages, containsInAnyOrder(
                "password size must be between 6 and 2147483647",
                "login must not be empty"
        ));
    }

    @Test
    @DisplayName("Must not register a user with password shorter than 6 characters")
    void test3() throws Exception {
        var user = new NewUserRequest("teste@zup.com.br", "12345");
        Password password = Password.encode(user.getPassword());

        Exception exception = mockMvc.perform(
                POST("/api/users", user)
        ).andExpect(
                status().isBadRequest()
        ).andReturn().getResolvedException();

        MethodArgumentNotValidException resolvedException = (MethodArgumentNotValidException) exception;

        List<String> errorMessages = resolvedException
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(ExceptionUtil::getFieldAndDefaultErrorMessage)
                .toList();

        assertThat(errorMessages, containsInAnyOrder(
                "password size must be between 6 and 2147483647"
        ));
    }

    @Test
    @DisplayName("Must not register a user with email already registered")
    void test4() throws Exception {
        Password password = Password.encode("123456");
        User user2 = new User("teste@zup.com.br", password);
        var user1 = new NewUserRequest("teste@zup.com.br", "123456");

        userRepository.save(user2);

        Exception exception = mockMvc.perform(
                POST("/api/users", user1)
        ).andExpect(
                status().isBadRequest()
        ).andReturn().getResolvedException();

        MethodArgumentNotValidException resolvedException = (MethodArgumentNotValidException) exception;

        String errorMessages = resolvedException.getFieldError().getDefaultMessage();

        assertEquals(errorMessages,"login is already registered");
    }


}