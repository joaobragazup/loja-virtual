package br.com.zup.edu.nossalojavirtual.categories;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import static java.util.Objects.isNull;
import static javax.persistence.GenerationType.IDENTITY;
import static org.springframework.util.Assert.hasText;
import static org.springframework.util.Assert.notNull;

@Table(name = "categories")
@Entity
public class Category {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @NotEmpty
    @Column(name = "category_name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "super_category_id")
    private Category superCategory;

    /**
     * @deprecated frameworks eyes only
     */
    @Deprecated
    private Category() { }

    /**
     * if this category does not have a super category @see #Category(String name)
     *
     * @param name the category name
     * @param superCategory the category super category
     */
    public Category(@NotEmpty String name, @NotNull Category superCategory) {
        hasText(name, "name must not be empty");
        notNull(superCategory, "superCategory must not be null using this constructor");

        this.name = name;
        this.superCategory = superCategory;
    }

    /**
     * if this category has a super category @see #Category(String name, Category superCategory)
     *
     * @param name the category name
     */
    public Category(@NotEmpty String name) {
        hasText(name, "name must not be empty");

        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     *
     * @return a list of Categories from mother to this category itself
     */
    public List<Category> getCategoryHierarchy() {
        LinkedList<Category> categoriesOrder = new LinkedList<>();

        Category category = this;
        categoriesOrder.addFirst(category);

        while (!isNull(category.superCategory)) {
            category = category.superCategory;
            categoriesOrder.addFirst(category);
        }

        return categoriesOrder;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return name.equals(category.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Category.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .add("name='" + name + "'")
                .add("superCategory=" + superCategory)
                .toString();
    }
}
