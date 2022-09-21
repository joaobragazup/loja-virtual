package br.com.zup.edu.nossalojavirtual.categories;

import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends Repository<Category, Long> {

    Optional<Category> findCategoryById(long id);

    Category save(Category category);

    boolean existsByName(String name);

    boolean existsById(Long id);

    void deleteAll();

    List<Category> findAll();
}
