package fr.lalourche.hellojhipster.gateway.web.rest;

import fr.lalourche.hellojhipster.gateway.domain.Step;
import fr.lalourche.hellojhipster.gateway.repository.StepRepository;
import fr.lalourche.hellojhipster.gateway.repository.search.StepSearchRepository;
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
 * REST controller for managing {@link fr.lalourche.hellojhipster.gateway.domain.Step}.
 */
@RestController
@RequestMapping("/api")
@Transactional
public class StepResource {

    private final Logger log = LoggerFactory.getLogger(StepResource.class);

    private static final String ENTITY_NAME = "step";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final StepRepository stepRepository;

    private final StepSearchRepository stepSearchRepository;

    public StepResource(StepRepository stepRepository, StepSearchRepository stepSearchRepository) {
        this.stepRepository = stepRepository;
        this.stepSearchRepository = stepSearchRepository;
    }

    /**
     * {@code POST  /steps} : Create a new step.
     *
     * @param step the step to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new step, or with status {@code 400 (Bad Request)} if the step has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("/steps")
    public Mono<ResponseEntity<Step>> createStep(@Valid @RequestBody Step step) throws URISyntaxException {
        log.debug("REST request to save Step : {}", step);
        if (step.getId() != null) {
            throw new BadRequestAlertException("A new step cannot already have an ID", ENTITY_NAME, "idexists");
        }
        return stepRepository
            .save(step)
            .flatMap(stepSearchRepository::save)
            .map(
                result -> {
                    try {
                        return ResponseEntity
                            .created(new URI("/api/steps/" + result.getId()))
                            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId().toString()))
                            .body(result);
                    } catch (URISyntaxException e) {
                        throw new RuntimeException(e);
                    }
                }
            );
    }

    /**
     * {@code PUT  /steps/:id} : Updates an existing step.
     *
     * @param id the id of the step to save.
     * @param step the step to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated step,
     * or with status {@code 400 (Bad Request)} if the step is not valid,
     * or with status {@code 500 (Internal Server Error)} if the step couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/steps/{id}")
    public Mono<ResponseEntity<Step>> updateStep(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody Step step
    ) throws URISyntaxException {
        log.debug("REST request to update Step : {}, {}", id, step);
        if (step.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, step.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return stepRepository
            .existsById(id)
            .flatMap(
                exists -> {
                    if (!exists) {
                        return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                    }

                    return stepRepository
                        .save(step)
                        .flatMap(stepSearchRepository::save)
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
     * {@code PATCH  /steps/:id} : Partial updates given fields of an existing step, field will ignore if it is null
     *
     * @param id the id of the step to save.
     * @param step the step to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated step,
     * or with status {@code 400 (Bad Request)} if the step is not valid,
     * or with status {@code 404 (Not Found)} if the step is not found,
     * or with status {@code 500 (Internal Server Error)} if the step couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/steps/{id}", consumes = "application/merge-patch+json")
    public Mono<ResponseEntity<Step>> partialUpdateStep(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody Step step
    ) throws URISyntaxException {
        log.debug("REST request to partial update Step partially : {}, {}", id, step);
        if (step.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, step.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        return stepRepository
            .existsById(id)
            .flatMap(
                exists -> {
                    if (!exists) {
                        return Mono.error(new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound"));
                    }

                    Mono<Step> result = stepRepository
                        .findById(step.getId())
                        .map(
                            existingStep -> {
                                if (step.getAction() != null) {
                                    existingStep.setAction(step.getAction());
                                }

                                return existingStep;
                            }
                        )
                        .flatMap(stepRepository::save)
                        .flatMap(
                            savedStep -> {
                                stepSearchRepository.save(savedStep);

                                return Mono.just(savedStep);
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
     * {@code GET  /steps} : get all the steps.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of steps in body.
     */
    @GetMapping("/steps")
    public Mono<List<Step>> getAllSteps() {
        log.debug("REST request to get all Steps");
        return stepRepository.findAll().collectList();
    }

    /**
     * {@code GET  /steps} : get all the steps as a stream.
     * @return the {@link Flux} of steps.
     */
    @GetMapping(value = "/steps", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<Step> getAllStepsAsStream() {
        log.debug("REST request to get all Steps as a stream");
        return stepRepository.findAll();
    }

    /**
     * {@code GET  /steps/:id} : get the "id" step.
     *
     * @param id the id of the step to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the step, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/steps/{id}")
    public Mono<ResponseEntity<Step>> getStep(@PathVariable Long id) {
        log.debug("REST request to get Step : {}", id);
        Mono<Step> step = stepRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(step);
    }

    /**
     * {@code DELETE  /steps/:id} : delete the "id" step.
     *
     * @param id the id of the step to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/steps/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public Mono<ResponseEntity<Void>> deleteStep(@PathVariable Long id) {
        log.debug("REST request to delete Step : {}", id);
        return stepRepository
            .deleteById(id)
            .then(stepSearchRepository.deleteById(id))
            .map(
                result ->
                    ResponseEntity
                        .noContent()
                        .headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id.toString()))
                        .build()
            );
    }

    /**
     * {@code SEARCH  /_search/steps?query=:query} : search for the step corresponding
     * to the query.
     *
     * @param query the query of the step search.
     * @return the result of the search.
     */
    @GetMapping("/_search/steps")
    public Mono<List<Step>> searchSteps(@RequestParam String query) {
        log.debug("REST request to search Steps for query {}", query);
        return stepSearchRepository.search(query).collectList();
    }
}
