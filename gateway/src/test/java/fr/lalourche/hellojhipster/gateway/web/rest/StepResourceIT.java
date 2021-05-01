package fr.lalourche.hellojhipster.gateway.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import fr.lalourche.hellojhipster.gateway.IntegrationTest;
import fr.lalourche.hellojhipster.gateway.domain.Recipe;
import fr.lalourche.hellojhipster.gateway.domain.Step;
import fr.lalourche.hellojhipster.gateway.repository.StepRepository;
import fr.lalourche.hellojhipster.gateway.repository.search.StepSearchRepository;
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
 * Integration tests for the {@link StepResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
class StepResourceIT {

    private static final String DEFAULT_ACTION = "AAAAAAAAAA";
    private static final String UPDATED_ACTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/steps";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/steps";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private StepRepository stepRepository;

    /**
     * This repository is mocked in the fr.lalourche.hellojhipster.gateway.repository.search test package.
     *
     * @see fr.lalourche.hellojhipster.gateway.repository.search.StepSearchRepositoryMockConfiguration
     */
    @Autowired
    private StepSearchRepository mockStepSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Step step;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Step createEntity(EntityManager em) {
        Step step = new Step().action(DEFAULT_ACTION);
        // Add required entity
        Recipe recipe;
        recipe = em.insert(RecipeResourceIT.createEntity(em)).block();
        step.getRecipes().add(recipe);
        return step;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Step createUpdatedEntity(EntityManager em) {
        Step step = new Step().action(UPDATED_ACTION);
        // Add required entity
        Recipe recipe;
        recipe = em.insert(RecipeResourceIT.createUpdatedEntity(em)).block();
        step.getRecipes().add(recipe);
        return step;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Step.class).block();
        } catch (Exception e) {
            // It can fail, if other entities are still referring this - it will be removed later.
        }
        RecipeResourceIT.deleteEntities(em);
    }

    @AfterEach
    public void cleanup() {
        deleteEntities(em);
    }

    @BeforeEach
    public void initTest() {
        deleteEntities(em);
        step = createEntity(em);
    }

    @Test
    void createStep() throws Exception {
        int databaseSizeBeforeCreate = stepRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockStepSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the Step
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(step))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll().collectList().block();
        assertThat(stepList).hasSize(databaseSizeBeforeCreate + 1);
        Step testStep = stepList.get(stepList.size() - 1);
        assertThat(testStep.getAction()).isEqualTo(DEFAULT_ACTION);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(1)).save(testStep);
    }

    @Test
    void createStepWithExistingId() throws Exception {
        // Create the Step with an existing ID
        step.setId(1L);

        int databaseSizeBeforeCreate = stepRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(step))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll().collectList().block();
        assertThat(stepList).hasSize(databaseSizeBeforeCreate);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(0)).save(step);
    }

    @Test
    void checkActionIsRequired() throws Exception {
        int databaseSizeBeforeTest = stepRepository.findAll().collectList().block().size();
        // set the field null
        step.setAction(null);

        // Create the Step, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(step))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Step> stepList = stepRepository.findAll().collectList().block();
        assertThat(stepList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllStepsAsStream() {
        // Initialize the database
        stepRepository.save(step).block();

        List<Step> stepList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Step.class)
            .getResponseBody()
            .filter(step::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(stepList).isNotNull();
        assertThat(stepList).hasSize(1);
        Step testStep = stepList.get(0);
        assertThat(testStep.getAction()).isEqualTo(DEFAULT_ACTION);
    }

    @Test
    void getAllSteps() {
        // Initialize the database
        stepRepository.save(step).block();

        // Get all the stepList
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
            .value(hasItem(step.getId().intValue()))
            .jsonPath("$.[*].action")
            .value(hasItem(DEFAULT_ACTION));
    }

    @Test
    void getStep() {
        // Initialize the database
        stepRepository.save(step).block();

        // Get the step
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, step.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(step.getId().intValue()))
            .jsonPath("$.action")
            .value(is(DEFAULT_ACTION));
    }

    @Test
    void getNonExistingStep() {
        // Get the step
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewStep() throws Exception {
        // Configure the mock search repository
        when(mockStepSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        stepRepository.save(step).block();

        int databaseSizeBeforeUpdate = stepRepository.findAll().collectList().block().size();

        // Update the step
        Step updatedStep = stepRepository.findById(step.getId()).block();
        updatedStep.action(UPDATED_ACTION);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedStep.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedStep))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll().collectList().block();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);
        Step testStep = stepList.get(stepList.size() - 1);
        assertThat(testStep.getAction()).isEqualTo(UPDATED_ACTION);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository).save(testStep);
    }

    @Test
    void putNonExistingStep() throws Exception {
        int databaseSizeBeforeUpdate = stepRepository.findAll().collectList().block().size();
        step.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, step.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(step))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll().collectList().block();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(0)).save(step);
    }

    @Test
    void putWithIdMismatchStep() throws Exception {
        int databaseSizeBeforeUpdate = stepRepository.findAll().collectList().block().size();
        step.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(step))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll().collectList().block();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(0)).save(step);
    }

    @Test
    void putWithMissingIdPathParamStep() throws Exception {
        int databaseSizeBeforeUpdate = stepRepository.findAll().collectList().block().size();
        step.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(step))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll().collectList().block();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(0)).save(step);
    }

    @Test
    void partialUpdateStepWithPatch() throws Exception {
        // Initialize the database
        stepRepository.save(step).block();

        int databaseSizeBeforeUpdate = stepRepository.findAll().collectList().block().size();

        // Update the step using partial update
        Step partialUpdatedStep = new Step();
        partialUpdatedStep.setId(step.getId());

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedStep.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedStep))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll().collectList().block();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);
        Step testStep = stepList.get(stepList.size() - 1);
        assertThat(testStep.getAction()).isEqualTo(DEFAULT_ACTION);
    }

    @Test
    void fullUpdateStepWithPatch() throws Exception {
        // Initialize the database
        stepRepository.save(step).block();

        int databaseSizeBeforeUpdate = stepRepository.findAll().collectList().block().size();

        // Update the step using partial update
        Step partialUpdatedStep = new Step();
        partialUpdatedStep.setId(step.getId());

        partialUpdatedStep.action(UPDATED_ACTION);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedStep.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedStep))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll().collectList().block();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);
        Step testStep = stepList.get(stepList.size() - 1);
        assertThat(testStep.getAction()).isEqualTo(UPDATED_ACTION);
    }

    @Test
    void patchNonExistingStep() throws Exception {
        int databaseSizeBeforeUpdate = stepRepository.findAll().collectList().block().size();
        step.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, step.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(step))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll().collectList().block();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(0)).save(step);
    }

    @Test
    void patchWithIdMismatchStep() throws Exception {
        int databaseSizeBeforeUpdate = stepRepository.findAll().collectList().block().size();
        step.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(step))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll().collectList().block();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(0)).save(step);
    }

    @Test
    void patchWithMissingIdPathParamStep() throws Exception {
        int databaseSizeBeforeUpdate = stepRepository.findAll().collectList().block().size();
        step.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(step))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll().collectList().block();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(0)).save(step);
    }

    @Test
    void deleteStep() {
        // Configure the mock search repository
        when(mockStepSearchRepository.deleteById(anyLong())).thenReturn(Mono.empty());
        // Initialize the database
        stepRepository.save(step).block();

        int databaseSizeBeforeDelete = stepRepository.findAll().collectList().block().size();

        // Delete the step
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, step.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Step> stepList = stepRepository.findAll().collectList().block();
        assertThat(stepList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(1)).deleteById(step.getId());
    }

    @Test
    void searchStep() {
        // Configure the mock search repository
        when(mockStepSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        stepRepository.save(step).block();
        when(mockStepSearchRepository.search("id:" + step.getId())).thenReturn(Flux.just(step));

        // Search the step
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + step.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(step.getId().intValue()))
            .jsonPath("$.[*].action")
            .value(hasItem(DEFAULT_ACTION));
    }
}
