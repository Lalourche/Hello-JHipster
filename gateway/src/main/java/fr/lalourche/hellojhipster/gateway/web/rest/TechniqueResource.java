package fr.lalourche.hellojhipster.gateway.web.rest;

import fr.lalourche.hellojhipster.gateway.domain.Technique;
import fr.lalourche.hellojhipster.gateway.repository.TechniqueRepository;
import fr.lalourche.hellojhipster.gateway.repository.search.TechniqueSearchRepository;
import fr.lalourche.hellojhipster.gateway.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.reactive.ResponseUtil;

/**
 * REST controller for managing {@link fr.lalourche.hellojhipster.gateway.domain.Technique}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class TechniqueResource {

    private final Logger log = LoggerFactory.getLogger(TechniqueResource.class);

    private static final String ENTITY_NAME = "technique";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final TechniqueRepository techniqueRepository;

    private final TechniqueSearchRepository techniqueSearchRepository;

    public TechniqueResource(TechniqueRepository techniqueRepository, TechniqueSearchRepository techniqueSearchRepository) {
        this.techniqueRepository = techniqueRepository;
        this.techniqueSearchRepository = techniqueSearchRepository;
    }

    /**
     * {@code POST  /techniques} : Create a new technique.
     *
     * @param technique the technique to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new technique, or with status {@code 400 (Bad Request)} if the technique has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/techniques")
    public Mono<ResponseEntity<Technique>> createTechnique(@Valid @RequestBody Technique technique) throws URISyntaxException {
        log.debug("REST request to save Technique : {}", technique);
        if (technique.getId() != null) {
            throw new BadRequestAlertException("A new technique cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return techniqueRepository
            .save(technique)
            .flatMap(techniqueSearchRepository::save)
            .map(
                result -> {
                    try {
                        return ResponseEntity
                            .created(new URI("/api/techniques/" + result.getId()))
                            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
            );
    }

    /**
     * {@code PUT  /techniques/:id} : Updates an existing technique.
     *
     * @param id the id of the technique to save.
     * @param technique the technique to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated technique,
     * or with status {@code 400 (Bad Request)} if the technique is not valid,
     * or with status {@code 500 (Internal Server Error)} if the technique couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/techniques/{id}")
    public Mono<ResponseEntity<Technique>> updateTechnique(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Technique technique
    ) throws URISyntaxException {
        log.debug("REST request to update Technique : {}, {}", id, technique);
        if (technique.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, technique.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return techniqueRepository
            .existsById(id)
            .flatMap(
                exists -> {
                    if (!exists) {
                        return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                    }

                    return techniqueRepository
                        .save(technique)
                        .flatMap(techniqueSearchRepository::save)
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                        .map(
                            result ->
                                ResponseEntity
                                    .ok()
                                    .headers(
                                        HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, result.getId().toString())
                                    )
                                    .body(result)
                        );
                }
            );
    }

    /**
     * {@code PATCH  /techniques/:id} : Partial updates given fields of an existing technique, field will ignore if it is null
     *
     * @param id the id of the technique to save.
     * @param technique the technique to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated technique,
     * or with status {@code 400 (Bad Request)} if the technique is not valid,
     * or with status {@code 404 (Not Found)} if the technique is not found,
     * or with status {@code 500 (Internal Server Error)} if the technique couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/techniques/{id}", consumes = "application/merge-patch+json")
    public Mono<ResponseEntity<Technique>> partialUpdateTechnique(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Technique technique
    ) throws URISyntaxException {
        log.debug("REST request to partial update Technique partially : {}, {}", id, technique);
        if (technique.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, technique.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return techniqueRepository
            .existsById(id)
            .flatMap(
                exists -> {
                    if (!exists) {
                        return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                    }

                    Mono<Technique> result = techniqueRepository
                        .findById(technique.getId())
                        .map(
                            existingTechnique -> {
                                if (technique.getDescription() != null) {
                                    existingTechnique.setDescription(technique.getDescription());
                                }

                                return existingTechnique;
                            }
                        )
                        .flatMap(techniqueRepository::save)
                        .flatMap(
                            savedTechnique -> {
                                techniqueSearchRepository.save(savedTechnique);

                                return Mono.just(savedTechnique);
                            }
                        );

                    return result
                        .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND)))
                        .map(
                            res ->
                                ResponseEntity
                                    .ok()
                                    .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, res.getId().toString()))
                                    .body(res)
                        );
                }
            );
    }

    /**
     * {@code GET  /techniques} : get all the techniques.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of techniques in body.
     */
    @GetMapping("/techniques")
    public Mono<List<Technique>> getAllTechniques() {
        log.debug("REST request to get all Techniques");
        return techniqueRepository.findAll().collectList();
    }

    /**
     * {@code GET  /techniques} : get all the techniques as a stream.
     * @return the {@link Flux} of techniques.
     */
    @GetMapping(value = "/techniques", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Technique> getAllTechniquesAsStream() {
        log.debug("REST request to get all Techniques as a stream");
        return techniqueRepository.findAll();
    }

    /**
     * {@code GET  /techniques/:id} : get the "id" technique.
     *
     * @param id the id of the technique to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the technique, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/techniques/{id}")
    public Mono<ResponseEntity<Technique>> getTechnique(@PathVariable Long id) {
        log.debug("REST request to get Technique : {}", id);
        Mono<Technique> technique = techniqueRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(technique);
    }

    /**
     * {@code DELETE  /techniques/:id} : delete the "id" technique.
     *
     * @param id the id of the technique to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/techniques/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteTechnique(@PathVariable Long id) {
        log.debug("REST request to delete Technique : {}", id);
        return techniqueRepository
            .deleteById(id)
            .then(techniqueSearchRepository.deleteById(id))
            .map(
                result ->
                    ResponseEntity
                        .noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
            );
    }

    /**
     * {@code SEARCH  /_search/techniques?query=:query} : search for the technique corresponding
     * to the query.
     *
     * @param query the query of the technique search.
     * @return the result of the search.
     */
    @GetMapping("/_search/techniques")
    public Mono<List<Technique>> searchTechniques(@RequestParam String query) {
        log.debug("REST request to search Techniques for query {}", query);
        return techniqueSearchRepository.search(query).collectList();
    }
}
