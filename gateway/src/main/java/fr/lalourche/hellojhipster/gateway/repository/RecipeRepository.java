package fr.lalourche.hellojhipster.gateway.repository;

import fr.lalourche.hellojhipster.gateway.domain.Recipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Recipe entity.
 */
@SuppressWarnings("unused")
@Repository
public interface RecipeRepository extends R2dbcRepository<Recipe, Long>, RecipeRepositoryInternal {
    @Override
    Mono<Recipe> findOneWithEagerRelationships(Long id);

    @Override
    Flux<Recipe> findAllWithEagerRelationships();

    @Override
    Flux<Recipe> findAllWithEagerRelationships(Pageable page);

    @Override
    Mono<Void> deleteById(Long id);

    @Query(
        "SELECT entity.* FROM recipe entity JOIN rel_recipe__ingredients joinTable ON entity.id = joinTable.recipe_id WHERE joinTable.ingredients_id = :id"
    )
    Flux<Recipe> findByIngredients(Long id);

    @Query(
        "SELECT entity.* FROM recipe entity JOIN rel_recipe__steps joinTable ON entity.id = joinTable.recipe_id WHERE joinTable.steps_id = :id"
    )
    Flux<Recipe> findBySteps(Long id);

    // just to avoid having unambigous methods
    @Override
    Flux<Recipe> findAll();

    @Override
    Mono<Recipe> findById(Long id);

    @Override
    <S extends Recipe> Mono<S> save(S entity);
}

interface RecipeRepositoryInternal {
    <S extends Recipe> Mono<S> insert(S entity);
    <S extends Recipe> Mono<S> save(S entity);
    Mono<Integer> update(Recipe entity);

    Flux<Recipe> findAll();
    Mono<Recipe> findById(Long id);
    Flux<Recipe> findAllBy(Pageable pageable);
    Flux<Recipe> findAllBy(Pageable pageable, Criteria criteria);

    Mono<Recipe> findOneWithEagerRelationships(Long id);

    Flux<Recipe> findAllWithEagerRelationships();

    Flux<Recipe> findAllWithEagerRelationships(Pageable page);

    Mono<Void> deleteById(Long id);
}
