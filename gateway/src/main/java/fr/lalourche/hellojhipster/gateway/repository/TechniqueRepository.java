package fr.lalourche.hellojhipster.gateway.repository;

import fr.lalourche.hellojhipster.gateway.domain.Technique;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive repository for the Technique entity.
 */
@SuppressWarnings("unused")
@Repository
public interface TechniqueRepository extends R2dbcRepository<Technique, Long>, TechniqueRepositoryInternal {
    // just to avoid having unambigous methods
    @Override
    Flux<Technique> findAll();

    @Override
    Mono<Technique> findById(Long id);

    @Override
    <S extends Technique> Mono<S> save(S entity);
}

interface TechniqueRepositoryInternal {
    <S extends Technique> Mono<S> insert(S entity);
    <S extends Technique> Mono<S> save(S entity);
    Mono<Integer> update(Technique entity);

    Flux<Technique> findAll();
    Mono<Technique> findById(Long id);
    Flux<Technique> findAllBy(Pageable pageable);
    Flux<Technique> findAllBy(Pageable pageable, Criteria criteria);
}
