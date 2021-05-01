package fr.lalourche.hellojhipster.gateway.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import fr.lalourche.hellojhipster.gateway.IntegrationTest;
import fr.lalourche.hellojhipster.gateway.domain.Technique;
import fr.lalourche.hellojhipster.gateway.repository.TechniqueRepository;
import fr.lalourche.hellojhipster.gateway.repository.search.TechniqueSearchRepository;
import fr.lalourche.hellojhipster.gateway.service.EntityManager;
import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link TechniqueResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
class TechniqueResourceIT {

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/techniques";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/techniques";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private TechniqueRepository techniqueRepository;

    /**
     * This repository is mocked in the fr.lalourche.hellojhipster.gateway.repository.search test package.
     *
     * @see fr.lalourche.hellojhipster.gateway.repository.search.TechniqueSearchRepositoryMockConfiguration
     */
    @Autowired
    private TechniqueSearchRepository mockTechniqueSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Technique technique;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Technique createEntity(EntityManager em) {
        Technique technique = new Technique().description(DEFAULT_DESCRIPTION);
        return technique;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Technique createUpdatedEntity(EntityManager em) {
        Technique technique = new Technique().description(UPDATED_DESCRIPTION);
        return technique;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Technique.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        technique = createEntity(em);
    }

    @Test
    void createTechnique() throws Exception {
        int databaseSizeBeforeCreate = techniqueRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockTechniqueSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the Technique
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(technique))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll().collectList().block();
        assertThat(techniqueList).hasSize(databaseSizeBeforeCreate + 1);
        Technique testTechnique = techniqueList.get(techniqueList.size() - 1);
        assertThat(testTechnique.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(1)).save(testTechnique);
    }

    @Test
    void createTechniqueWithExistingId() throws Exception {
        // Create the Technique with an existing ID
        technique.setId(1L);

        int databaseSizeBeforeCreate = techniqueRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(technique))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll().collectList().block();
        assertThat(techniqueList).hasSize(databaseSizeBeforeCreate);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(0)).save(technique);
    }

    @Test
    void checkDescriptionIsRequired() throws Exception {
        int databaseSizeBeforeTest = techniqueRepository.findAll().collectList().block().size();
        // set the field null
        technique.setDescription(null);

        // Create the Technique, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(technique))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Technique> techniqueList = techniqueRepository.findAll().collectList().block();
        assertThat(techniqueList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllTechniquesAsStream() {
        // Initialize the database
        techniqueRepository.save(technique).block();

        List<Technique> techniqueList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Technique.class)
            .getResponseBody()
            .filter(technique::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(techniqueList).isNotNull();
        assertThat(techniqueList).hasSize(1);
        Technique testTechnique = techniqueList.get(0);
        assertThat(testTechnique.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void getAllTechniques() {
        // Initialize the database
        techniqueRepository.save(technique).block();

        // Get all the techniqueList
        webTestClient
            .get()
            .uri(ENTITY_API_URL + "?sort=id,desc")
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(technique.getId().intValue()))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION));
    }

    @Test
    void getTechnique() {
        // Initialize the database
        techniqueRepository.save(technique).block();

        // Get the technique
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, technique.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(technique.getId().intValue()))
            .jsonPath("$.description")
            .value(is(DEFAULT_DESCRIPTION));
    }

    @Test
    void getNonExistingTechnique() {
        // Get the technique
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewTechnique() throws Exception {
        // Configure the mock search repository
        when(mockTechniqueSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        techniqueRepository.save(technique).block();

        int databaseSizeBeforeUpdate = techniqueRepository.findAll().collectList().block().size();

        // Update the technique
        Technique updatedTechnique = techniqueRepository.findById(technique.getId()).block();
        updatedTechnique.description(UPDATED_DESCRIPTION);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedTechnique.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedTechnique))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll().collectList().block();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);
        Technique testTechnique = techniqueList.get(techniqueList.size() - 1);
        assertThat(testTechnique.getDescription()).isEqualTo(UPDATED_DESCRIPTION);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository).save(testTechnique);
    }

    @Test
    void putNonExistingTechnique() throws Exception {
        int databaseSizeBeforeUpdate = techniqueRepository.findAll().collectList().block().size();
        technique.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, technique.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(technique))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll().collectList().block();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(0)).save(technique);
    }

    @Test
    void putWithIdMismatchTechnique() throws Exception {
        int databaseSizeBeforeUpdate = techniqueRepository.findAll().collectList().block().size();
        technique.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(technique))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll().collectList().block();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(0)).save(technique);
    }

    @Test
    void putWithMissingIdPathParamTechnique() throws Exception {
        int databaseSizeBeforeUpdate = techniqueRepository.findAll().collectList().block().size();
        technique.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(technique))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll().collectList().block();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(0)).save(technique);
    }

    @Test
    void partialUpdateTechniqueWithPatch() throws Exception {
        // Initialize the database
        techniqueRepository.save(technique).block();

        int databaseSizeBeforeUpdate = techniqueRepository.findAll().collectList().block().size();

        // Update the technique using partial update
        Technique partialUpdatedTechnique = new Technique();
        partialUpdatedTechnique.setId(technique.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedTechnique.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedTechnique))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll().collectList().block();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);
        Technique testTechnique = techniqueList.get(techniqueList.size() - 1);
        assertThat(testTechnique.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void fullUpdateTechniqueWithPatch() throws Exception {
        // Initialize the database
        techniqueRepository.save(technique).block();

        int databaseSizeBeforeUpdate = techniqueRepository.findAll().collectList().block().size();

        // Update the technique using partial update
        Technique partialUpdatedTechnique = new Technique();
        partialUpdatedTechnique.setId(technique.getId());

        partialUpdatedTechnique.description(UPDATED_DESCRIPTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedTechnique.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedTechnique))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll().collectList().block();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);
        Technique testTechnique = techniqueList.get(techniqueList.size() - 1);
        assertThat(testTechnique.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void patchNonExistingTechnique() throws Exception {
        int databaseSizeBeforeUpdate = techniqueRepository.findAll().collectList().block().size();
        technique.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, technique.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(technique))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll().collectList().block();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(0)).save(technique);
    }

    @Test
    void patchWithIdMismatchTechnique() throws Exception {
        int databaseSizeBeforeUpdate = techniqueRepository.findAll().collectList().block().size();
        technique.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(technique))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll().collectList().block();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(0)).save(technique);
    }

    @Test
    void patchWithMissingIdPathParamTechnique() throws Exception {
        int databaseSizeBeforeUpdate = techniqueRepository.findAll().collectList().block().size();
        technique.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(technique))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll().collectList().block();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(0)).save(technique);
    }

    @Test
    void deleteTechnique() {
        // Configure the mock search repository
        when(mockTechniqueSearchRepository.deleteById(anyLong())).thenReturn(Mono.empty());
        // Initialize the database
        techniqueRepository.save(technique).block();

        int databaseSizeBeforeDelete = techniqueRepository.findAll().collectList().block().size();

        // Delete the technique
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, technique.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Technique> techniqueList = techniqueRepository.findAll().collectList().block();
        assertThat(techniqueList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(1)).deleteById(technique.getId());
    }

    @Test
    void searchTechnique() {
        // Configure the mock search repository
        when(mockTechniqueSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        techniqueRepository.save(technique).block();
        when(mockTechniqueSearchRepository.search("id:" + technique.getId())).thenReturn(Flux.just(technique));

        // Search the technique
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + technique.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(technique.getId().intValue()))
            .jsonPath("$.[*].description")
            .value(hasItem(DEFAULT_DESCRIPTION));
    }
}
