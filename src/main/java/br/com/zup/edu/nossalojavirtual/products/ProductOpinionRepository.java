package br.com.zup.edu.nossalojavirtual.products;

import org.springframework.data.repository.Repository;

import java.util.List;

interface ProductOpinionRepository extends Repository<ProductOpinion, Long> {

    ProductOpinion save(ProductOpinion productOpinion);

    List<ProductOpinion> findAll();

    void deleteAll();
}
