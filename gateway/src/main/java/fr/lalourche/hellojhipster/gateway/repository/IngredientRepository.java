package fr.lalourche.hellojhipster.gateway.repository;

import fr.lalourche.hellojhipster.gateway.domain.Ingredient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Ingredient entity.
 */
@SuppressWarnings("unused")
@Repository
public interface IngredientRepository extends R2dbcRepository<Ingredient, Long>, IngredientRepositoryInternal {
    // just to avoid having unambigous methods
    @Override
    Flux<Ingredient> findAll();

    @Override
    Mono<Ingredient> findById(Long id);

    @Override
    <S extends Ingredient> Mono<S> save(S entity);
}

interface IngredientRepositoryInternal {
    <S extends Ingredient> Mono<S> insert(S entity);
    <S extends Ingredient> Mono<S> save(S entity);
    Mono<Integer> update(Ingredient entity);

    Flux<Ingredient> findAll();
    Mono<Ingredient> findById(Long id);
    Flux<Ingredient> findAllBy(Pageable pageable);
    Flux<Ingredient> findAllBy(Pageable pageable, Criteria criteria);
}
