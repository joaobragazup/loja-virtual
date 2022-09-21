package br.com.zup.edu.nossalojavirtual.products;

import br.com.zup.edu.nossalojavirtual.users.User;
import org.hibernate.validator.constraints.Length;
import org.hibernate.validator.constraints.Range;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static java.lang.String.format;

class NewOpinionRequest {

    @Range(min = 1, max = 5)
    private Integer rating;

    @NotBlank
    private String title;

    @Length(max = 500)
    private String description;

   @NotNull
    private UUID productId;

    public NewOpinionRequest(Integer rating, String title, String description, UUID productId) {
        this.rating = rating;
        this.title = title;
        this.description = description;
        this.productId = productId;
    }

    public NewOpinionRequest() {
    }

    public Integer getRating() {
        return rating;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public UUID getProductId() {
        return productId;
    }

    public ProductOpinion toProductOpinion(Function<UUID, Optional<Product>> findProductById, User user) {
        var product = findProductById.apply(productId)
                                     .orElseThrow(() -> new IllegalStateException(format("Product %s is not registered", productId)));

        return new ProductOpinion(rating, title, description, product, user);

    }
}
