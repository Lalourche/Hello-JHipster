package fr.lalourche.hellojhipster.recipes.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import fr.lalourche.hellojhipster.recipes.IntegrationTest;
import fr.lalourche.hellojhipster.recipes.domain.Recipe;
import fr.lalourche.hellojhipster.recipes.domain.Step;
import fr.lalourche.hellojhipster.recipes.repository.StepRepository;
import fr.lalourche.hellojhipster.recipes.repository.search.StepSearchRepository;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import javax.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link StepResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
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
     * This repository is mocked in the fr.lalourche.hellojhipster.recipes.repository.search test package.
     *
     * @see fr.lalourche.hellojhipster.recipes.repository.search.StepSearchRepositoryMockConfiguration
     */
    @Autowired
    private StepSearchRepository mockStepSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restStepMockMvc;

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
        if (TestUtil.findAll(em, Recipe.class).isEmpty()) {
            recipe = RecipeResourceIT.createEntity(em);
            em.persist(recipe);
            em.flush();
        } else {
            recipe = TestUtil.findAll(em, Recipe.class).get(0);
        }
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
        if (TestUtil.findAll(em, Recipe.class).isEmpty()) {
            recipe = RecipeResourceIT.createUpdatedEntity(em);
            em.persist(recipe);
            em.flush();
        } else {
            recipe = TestUtil.findAll(em, Recipe.class).get(0);
        }
        step.getRecipes().add(recipe);
        return step;
    }

    @BeforeEach
    public void initTest() {
        step = createEntity(em);
    }

    @Test
    @Transactional
    void createStep() throws Exception {
        int databaseSizeBeforeCreate = stepRepository.findAll().size();
        // Create the Step
        restStepMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(step)))
            .andExpect(status().isCreated());

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll();
        assertThat(stepList).hasSize(databaseSizeBeforeCreate + 1);
        Step testStep = stepList.get(stepList.size() - 1);
        assertThat(testStep.getAction()).isEqualTo(DEFAULT_ACTION);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(1)).save(testStep);
    }

    @Test
    @Transactional
    void createStepWithExistingId() throws Exception {
        // Create the Step with an existing ID
        step.setId(1L);

        int databaseSizeBeforeCreate = stepRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restStepMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(step)))
            .andExpect(status().isBadRequest());

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll();
        assertThat(stepList).hasSize(databaseSizeBeforeCreate);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(0)).save(step);
    }

    @Test
    @Transactional
    void checkActionIsRequired() throws Exception {
        int databaseSizeBeforeTest = stepRepository.findAll().size();
        // set the field null
        step.setAction(null);

        // Create the Step, which fails.

        restStepMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(step)))
            .andExpect(status().isBadRequest());

        List<Step> stepList = stepRepository.findAll();
        assertThat(stepList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllSteps() throws Exception {
        // Initialize the database
        stepRepository.saveAndFlush(step);

        // Get all the stepList
        restStepMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(step.getId().intValue())))
            .andExpect(jsonPath("$.[*].action").value(hasItem(DEFAULT_ACTION)));
    }

    @Test
    @Transactional
    void getStep() throws Exception {
        // Initialize the database
        stepRepository.saveAndFlush(step);

        // Get the step
        restStepMockMvc
            .perform(get(ENTITY_API_URL_ID, step.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(step.getId().intValue()))
            .andExpect(jsonPath("$.action").value(DEFAULT_ACTION));
    }

    @Test
    @Transactional
    void getNonExistingStep() throws Exception {
        // Get the step
        restStepMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putNewStep() throws Exception {
        // Initialize the database
        stepRepository.saveAndFlush(step);

        int databaseSizeBeforeUpdate = stepRepository.findAll().size();

        // Update the step
        Step updatedStep = stepRepository.findById(step.getId()).get();
        // Disconnect from session so that the updates on updatedStep are not directly saved in db
        em.detach(updatedStep);
        updatedStep.action(UPDATED_ACTION);

        restStepMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedStep.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedStep))
            )
            .andExpect(status().isOk());

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);
        Step testStep = stepList.get(stepList.size() - 1);
        assertThat(testStep.getAction()).isEqualTo(UPDATED_ACTION);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository).save(testStep);
    }

    @Test
    @Transactional
    void putNonExistingStep() throws Exception {
        int databaseSizeBeforeUpdate = stepRepository.findAll().size();
        step.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restStepMockMvc
            .perform(
                put(ENTITY_API_URL_ID, step.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(step))
            )
            .andExpect(status().isBadRequest());

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(0)).save(step);
    }

    @Test
    @Transactional
    void putWithIdMismatchStep() throws Exception {
        int databaseSizeBeforeUpdate = stepRepository.findAll().size();
        step.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStepMockMvc
            .perform(
                put(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(step))
            )
            .andExpect(status().isBadRequest());

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(0)).save(step);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamStep() throws Exception {
        int databaseSizeBeforeUpdate = stepRepository.findAll().size();
        step.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStepMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(step)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(0)).save(step);
    }

    @Test
    @Transactional
    void partialUpdateStepWithPatch() throws Exception {
        // Initialize the database
        stepRepository.saveAndFlush(step);

        int databaseSizeBeforeUpdate = stepRepository.findAll().size();

        // Update the step using partial update
        Step partialUpdatedStep = new Step();
        partialUpdatedStep.setId(step.getId());

        restStepMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedStep.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedStep))
            )
            .andExpect(status().isOk());

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);
        Step testStep = stepList.get(stepList.size() - 1);
        assertThat(testStep.getAction()).isEqualTo(DEFAULT_ACTION);
    }

    @Test
    @Transactional
    void fullUpdateStepWithPatch() throws Exception {
        // Initialize the database
        stepRepository.saveAndFlush(step);

        int databaseSizeBeforeUpdate = stepRepository.findAll().size();

        // Update the step using partial update
        Step partialUpdatedStep = new Step();
        partialUpdatedStep.setId(step.getId());

        partialUpdatedStep.action(UPDATED_ACTION);

        restStepMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedStep.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedStep))
            )
            .andExpect(status().isOk());

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);
        Step testStep = stepList.get(stepList.size() - 1);
        assertThat(testStep.getAction()).isEqualTo(UPDATED_ACTION);
    }

    @Test
    @Transactional
    void patchNonExistingStep() throws Exception {
        int databaseSizeBeforeUpdate = stepRepository.findAll().size();
        step.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restStepMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, step.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(step))
            )
            .andExpect(status().isBadRequest());

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(0)).save(step);
    }

    @Test
    @Transactional
    void patchWithIdMismatchStep() throws Exception {
        int databaseSizeBeforeUpdate = stepRepository.findAll().size();
        step.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStepMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, count.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(step))
            )
            .andExpect(status().isBadRequest());

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(0)).save(step);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamStep() throws Exception {
        int databaseSizeBeforeUpdate = stepRepository.findAll().size();
        step.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restStepMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(step)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Step in the database
        List<Step> stepList = stepRepository.findAll();
        assertThat(stepList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(0)).save(step);
    }

    @Test
    @Transactional
    void deleteStep() throws Exception {
        // Initialize the database
        stepRepository.saveAndFlush(step);

        int databaseSizeBeforeDelete = stepRepository.findAll().size();

        // Delete the step
        restStepMockMvc
            .perform(delete(ENTITY_API_URL_ID, step.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Step> stepList = stepRepository.findAll();
        assertThat(stepList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Step in Elasticsearch
        verify(mockStepSearchRepository, times(1)).deleteById(step.getId());
    }

    @Test
    @Transactional
    void searchStep() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        stepRepository.saveAndFlush(step);
        when(mockStepSearchRepository.search(queryStringQuery("id:" + step.getId()))).thenReturn(Collections.singletonList(step));

        // Search the step
        restStepMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + step.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(step.getId().intValue())))
            .andExpect(jsonPath("$.[*].action").value(hasItem(DEFAULT_ACTION)));
    }
}
