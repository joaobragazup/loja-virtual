package br.com.zup.edu.nossalojavirtual.categories;

import br.com.zup.edu.nossalojavirtual.NossaLojaVirtualApplicationTest;
import br.com.zup.edu.nossalojavirtual.security.ExceptionUtil;
import br.com.zup.edu.nossalojavirtual.users.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.validation.Errors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CategoryControllerTest extends NossaLojaVirtualApplicationTest {

    @Autowired
    CategoryRepository categoryRepository;

    Category category;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        this.category = new Category("Computer");
        categoryRepository.save(category);
    }

    @AfterEach
    void deleteAll(){
        categoryRepository.deleteAll();
    }

    @Test
    @DisplayName("Must register a category correctly")
    void test1() throws Exception {
        categoryRepository.deleteAll();
        var newCategory = new NewCategoryRequest("Notebook", null);

        mockMvc.perform(
                POST("/api/categories", newCategory)
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_categories:write")))
        ).andExpect(
                status().isCreated()
        ).andExpect(
                redirectedUrlPattern("/api/categories/*")
        );

        List<Category> categories = categoryRepository.findAll();
        assertEquals(1, categories.size());
    }

    @Test
    @DisplayName("Must register a sub category if category hierarchy works")
    void test2() throws Exception {
        var newCategory = new NewCategoryRequest("Notebook", category.getId());

        mockMvc.perform(
                POST("/api/categories", newCategory)
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_categories:write")))
        ).andExpect(
                status().isCreated()
        ).andExpect(
                redirectedUrlPattern("/api/categories/*")
        );

        List<Category> categories = categoryRepository.findAll();
        assertEquals(2, categories.size());
    }

    @Test
    @DisplayName("Should not register a category with invalid data")
    void test3() throws Exception {
        categoryRepository.deleteAll();
        var newCategory = new NewCategoryRequest("", null);

        Exception exception = mockMvc.perform(
                POST("/api/categories", newCategory)
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_categories:write")))
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


        List<Category> categories = categoryRepository.findAll();
        assertEquals(0, categories.size());
        assertThat(errorMessages, containsInAnyOrder(
                "name must not be empty"

        ));
    }

    @Test
    @DisplayName("Should not register a category if super category not exist")
    void test4() throws Exception {
        categoryRepository.deleteAll();
        var newCategory = new NewCategoryRequest("Computer", 9999L);

        Exception exception = mockMvc.perform(
                POST("/api/categories", newCategory)
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_categories:write")))
        ).andExpect(
                status().isBadRequest()
        ).andReturn().getResolvedException();

        MethodArgumentNotValidException resolvedException = (MethodArgumentNotValidException) exception;

        String errorMessages = resolvedException.getFieldError().getDefaultMessage();


        List<Category> categories = categoryRepository.findAll();
        assertEquals(0, categories.size());
        assertEquals(errorMessages,"The super category does not exist");
    }

    @Test
    @DisplayName("Should not register a category if name already exists")
    void test5() throws Exception {
        var newCategory = new NewCategoryRequest("Computer", null);

        Exception exception = mockMvc.perform(
                POST("/api/categories", newCategory)
                        .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_categories:write")))
        ).andExpect(
                status().isBadRequest()
        ).andReturn().getResolvedException();

        MethodArgumentNotValidException resolvedException = (MethodArgumentNotValidException) exception;

        String errorMessages = resolvedException.getFieldError().getDefaultMessage();

        List<Category> categories = categoryRepository.findAll();
        assertEquals(1, categories.size());
        assertEquals("this category is already registered",errorMessages);
    }

    @Test
    @DisplayName("Should not register a category without token")
    void test6() throws Exception {
        mockMvc.perform(
                POST("/api/categories", 1)
        ).andExpect(
                status().isUnauthorized()
        );
    }

    @Test
    @DisplayName("Should not register a category without scope token")
    void test7() throws Exception {
        mockMvc.perform(
                POST("/api/categories", 1)
                        .with(jwt())
        ).andExpect(
                status().isForbidden()
        );
    }
}