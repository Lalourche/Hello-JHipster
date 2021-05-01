package fr.lalourche.hellojhipster.gateway.repository;

import fr.lalourche.hellojhipster.gateway.domain.Step;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Step entity.
 */
@SuppressWarnings("unused")
@Repository
public interface StepRepository extends R2dbcRepository<Step, Long>, StepRepositoryInternal {
    // just to avoid having unambigous methods
    @Override
    Flux<Step> findAll();

    @Override
    Mono<Step> findById(Long id);

    @Override
    <S extends Step> Mono<S> save(S entity);
}

interface StepRepositoryInternal {
    <S extends Step> Mono<S> insert(S entity);
    <S extends Step> Mono<S> save(S entity);
    Mono<Integer> update(Step entity);

    Flux<Step> findAll();
    Mono<Step> findById(Long id);
    Flux<Step> findAllBy(Pageable pageable);
    Flux<Step> findAllBy(Pageable pageable, Criteria criteria);
}
