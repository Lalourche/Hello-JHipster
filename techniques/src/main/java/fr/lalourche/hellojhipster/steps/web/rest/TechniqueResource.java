package fr.lalourche.hellojhipster.steps.web.rest;

import static org.elasticsearch.index.query.QueryBuilders.*;

import fr.lalourche.hellojhipster.steps.domain.Technique;
import fr.lalourche.hellojhipster.steps.repository.TechniqueRepository;
import fr.lalourche.hellojhipster.steps.repository.search.TechniqueSearchRepository;
import fr.lalourche.hellojhipster.steps.web.rest.errors.BadRequestAlertException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link fr.lalourche.hellojhipster.steps.domain.Technique}.
 */
@RestController
@RequestMapping("/api")
public class TechniqueResource {

    private final Logger log = LoggerFactory.getLogger(TechniqueResource.class);

    private static final String ENTITY_NAME = "techniquesTechnique";

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
    public ResponseEntity<Technique> createTechnique(@Valid @RequestBody Technique technique) throws URISyntaxException {
        log.debug("REST request to save Technique : {}", technique);
        if (technique.getId() != null) {
            throw new BadRequestAlertException("A new technique cannot already have an ID", ENTITY_NAME, "idexists");
        }
        Technique result = techniqueRepository.save(technique);
        techniqueSearchRepository.save(result);
        return ResponseEntity
            .created(new URI("/api/techniques/" + result.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, true, ENTITY_NAME, result.getId()))
            .body(result);
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
    public ResponseEntity<Technique> updateTechnique(
        @PathVariable(value = "id", required = false) final String id,
        @Valid @RequestBody Technique technique
    ) throws URISyntaxException {
        log.debug("REST request to update Technique : {}, {}", id, technique);
        if (technique.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, technique.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!techniqueRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Technique result = techniqueRepository.save(technique);
        techniqueSearchRepository.save(result);
        return ResponseEntity
            .ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, technique.getId()))
            .body(result);
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
    public ResponseEntity<Technique> partialUpdateTechnique(
        @PathVariable(value = "id", required = false) final String id,
        @NotNull @RequestBody Technique technique
    ) throws URISyntaxException {
        log.debug("REST request to partial update Technique partially : {}, {}", id, technique);
        if (technique.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, technique.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!techniqueRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<Technique> result = techniqueRepository
            .findById(technique.getId())
            .map(
                existingTechnique -> {
                    if (technique.getDescription() != null) {
                        existingTechnique.setDescription(technique.getDescription());
                    }

                    return existingTechnique;
                }
            )
            .map(techniqueRepository::save)
            .map(
                savedTechnique -> {
                    techniqueSearchRepository.save(savedTechnique);

                    return savedTechnique;
                }
            );

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, true, ENTITY_NAME, technique.getId())
        );
    }

    /**
     * {@code GET  /techniques} : get all the techniques.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of techniques in body.
     */
    @GetMapping("/techniques")
    public List<Technique> getAllTechniques() {
        log.debug("REST request to get all Techniques");
        return techniqueRepository.findAll();
    }

    /**
     * {@code GET  /techniques/:id} : get the "id" technique.
     *
     * @param id the id of the technique to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the technique, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/techniques/{id}")
    public ResponseEntity<Technique> getTechnique(@PathVariable String id) {
        log.debug("REST request to get Technique : {}", id);
        Optional<Technique> technique = techniqueRepository.findById(id);
        return ResponseUtil.wrapOrNotFound(technique);
    }

    /**
     * {@code DELETE  /techniques/:id} : delete the "id" technique.
     *
     * @param id the id of the technique to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/techniques/{id}")
    public ResponseEntity<Void> deleteTechnique(@PathVariable String id) {
        log.debug("REST request to delete Technique : {}", id);
        techniqueRepository.deleteById(id);
        techniqueSearchRepository.deleteById(id);
        return ResponseEntity.noContent().headers(HeaderUtil.createEntityDeletionAlert(applicationName, true, ENTITY_NAME, id)).build();
    }

    /**
     * {@code SEARCH  /_search/techniques?query=:query} : search for the technique corresponding
     * to the query.
     *
     * @param query the query of the technique search.
     * @return the result of the search.
     */
    @GetMapping("/_search/techniques")
    public List<Technique> searchTechniques(@RequestParam String query) {
        log.debug("REST request to search Techniques for query {}", query);
        return StreamSupport
            .stream(techniqueSearchRepository.search(queryStringQuery(query)).spliterator(), false)
            .collect(Collectors.toList());
    }
}
