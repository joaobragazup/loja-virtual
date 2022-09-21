package br.com.zup.edu.nossalojavirtual.categories;

import br.com.zup.edu.nossalojavirtual.NossaLojaVirtualApplicationTest;
import br.com.zup.edu.nossalojavirtual.shared.validators.UniqueFieldValidator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CategoryUniqueNameValidatorTest extends NossaLojaVirtualApplicationTest {
    @Autowired
    CategoryRepository categoryRepository;

    Category category;

    @Mock
    Errors errors;

    @SpyBean
    CategoryUniqueNameValidator categoryUniqueNameValidator;

    @BeforeEach
    void setUp() {
        categoryRepository.deleteAll();
        this.category = new Category("Computer");
        categoryRepository.save(category);
    }

    @AfterEach
    void deleteAll() {
        categoryRepository.deleteAll();
    }


    @Test
    @DisplayName("Must throw the error because name is already registered")
    void test1() throws Exception{
        var newCategory = new NewCategoryRequest("Computer", null);

        categoryUniqueNameValidator.validate(newCategory, errors);

        List<Category> categories = categoryRepository.findAll();

        Mockito.verify(errors,Mockito.times(1)).rejectValue("name","category.name.alreadyExists","this category is already registered");
    }

    @Test
    @DisplayName("Should not throw the error because name is diferrent")
    void test2() throws Exception{
        var newCategory = new NewCategoryRequest("Notebook", null);

        categoryUniqueNameValidator.validate(newCategory, errors);

        List<Category> categories = categoryRepository.findAll();

        Mockito.verify(errors,Mockito.never()).rejectValue("","","");
    }


}