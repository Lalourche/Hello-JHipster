package fr.lalourche.hellojhipster.steps.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import fr.lalourche.hellojhipster.steps.IntegrationTest;
import fr.lalourche.hellojhipster.steps.domain.Technique;
import fr.lalourche.hellojhipster.steps.repository.TechniqueRepository;
import fr.lalourche.hellojhipster.steps.repository.search.TechniqueSearchRepository;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
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

/**
 * Integration tests for the {@link TechniqueResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class TechniqueResourceIT {

    private static final String DEFAULT_DESCRIPTION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPTION = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/techniques";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/techniques";

    @Autowired
    private TechniqueRepository techniqueRepository;

    /**
     * This repository is mocked in the fr.lalourche.hellojhipster.steps.repository.search test package.
     *
     * @see fr.lalourche.hellojhipster.steps.repository.search.TechniqueSearchRepositoryMockConfiguration
     */
    @Autowired
    private TechniqueSearchRepository mockTechniqueSearchRepository;

    @Autowired
    private MockMvc restTechniqueMockMvc;

    private Technique technique;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Technique createEntity() {
        Technique technique = new Technique().description(DEFAULT_DESCRIPTION);
        return technique;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Technique createUpdatedEntity() {
        Technique technique = new Technique().description(UPDATED_DESCRIPTION);
        return technique;
    }

    @BeforeEach
    public void initTest() {
        techniqueRepository.deleteAll();
        technique = createEntity();
    }

    @Test
    void createTechnique() throws Exception {
        int databaseSizeBeforeCreate = techniqueRepository.findAll().size();
        // Create the Technique
        restTechniqueMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(technique)))
            .andExpect(status().isCreated());

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll();
        assertThat(techniqueList).hasSize(databaseSizeBeforeCreate + 1);
        Technique testTechnique = techniqueList.get(techniqueList.size() - 1);
        assertThat(testTechnique.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(1)).save(testTechnique);
    }

    @Test
    void createTechniqueWithExistingId() throws Exception {
        // Create the Technique with an existing ID
        technique.setId("existing_id");

        int databaseSizeBeforeCreate = techniqueRepository.findAll().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        restTechniqueMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(technique)))
            .andExpect(status().isBadRequest());

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll();
        assertThat(techniqueList).hasSize(databaseSizeBeforeCreate);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(0)).save(technique);
    }

    @Test
    void checkDescriptionIsRequired() throws Exception {
        int databaseSizeBeforeTest = techniqueRepository.findAll().size();
        // set the field null
        technique.setDescription(null);

        // Create the Technique, which fails.

        restTechniqueMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(technique)))
            .andExpect(status().isBadRequest());

        List<Technique> techniqueList = techniqueRepository.findAll();
        assertThat(techniqueList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllTechniques() throws Exception {
        // Initialize the database
        techniqueRepository.save(technique);

        // Get all the techniqueList
        restTechniqueMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(technique.getId())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }

    @Test
    void getTechnique() throws Exception {
        // Initialize the database
        techniqueRepository.save(technique);

        // Get the technique
        restTechniqueMockMvc
            .perform(get(ENTITY_API_URL_ID, technique.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(technique.getId()))
            .andExpect(jsonPath("$.description").value(DEFAULT_DESCRIPTION));
    }

    @Test
    void getNonExistingTechnique() throws Exception {
        // Get the technique
        restTechniqueMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    void putNewTechnique() throws Exception {
        // Initialize the database
        techniqueRepository.save(technique);

        int databaseSizeBeforeUpdate = techniqueRepository.findAll().size();

        // Update the technique
        Technique updatedTechnique = techniqueRepository.findById(technique.getId()).get();
        updatedTechnique.description(UPDATED_DESCRIPTION);

        restTechniqueMockMvc
            .perform(
                put(ENTITY_API_URL_ID, updatedTechnique.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(updatedTechnique))
            )
            .andExpect(status().isOk());

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);
        Technique testTechnique = techniqueList.get(techniqueList.size() - 1);
        assertThat(testTechnique.getDescription()).isEqualTo(UPDATED_DESCRIPTION);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository).save(testTechnique);
    }

    @Test
    void putNonExistingTechnique() throws Exception {
        int databaseSizeBeforeUpdate = techniqueRepository.findAll().size();
        technique.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTechniqueMockMvc
            .perform(
                put(ENTITY_API_URL_ID, technique.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(technique))
            )
            .andExpect(status().isBadRequest());

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(0)).save(technique);
    }

    @Test
    void putWithIdMismatchTechnique() throws Exception {
        int databaseSizeBeforeUpdate = techniqueRepository.findAll().size();
        technique.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTechniqueMockMvc
            .perform(
                put(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(TestUtil.convertObjectToJsonBytes(technique))
            )
            .andExpect(status().isBadRequest());

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(0)).save(technique);
    }

    @Test
    void putWithMissingIdPathParamTechnique() throws Exception {
        int databaseSizeBeforeUpdate = techniqueRepository.findAll().size();
        technique.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTechniqueMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(TestUtil.convertObjectToJsonBytes(technique)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(0)).save(technique);
    }

    @Test
    void partialUpdateTechniqueWithPatch() throws Exception {
        // Initialize the database
        techniqueRepository.save(technique);

        int databaseSizeBeforeUpdate = techniqueRepository.findAll().size();

        // Update the technique using partial update
        Technique partialUpdatedTechnique = new Technique();
        partialUpdatedTechnique.setId(technique.getId());

        restTechniqueMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTechnique.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTechnique))
            )
            .andExpect(status().isOk());

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);
        Technique testTechnique = techniqueList.get(techniqueList.size() - 1);
        assertThat(testTechnique.getDescription()).isEqualTo(DEFAULT_DESCRIPTION);
    }

    @Test
    void fullUpdateTechniqueWithPatch() throws Exception {
        // Initialize the database
        techniqueRepository.save(technique);

        int databaseSizeBeforeUpdate = techniqueRepository.findAll().size();

        // Update the technique using partial update
        Technique partialUpdatedTechnique = new Technique();
        partialUpdatedTechnique.setId(technique.getId());

        partialUpdatedTechnique.description(UPDATED_DESCRIPTION);

        restTechniqueMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedTechnique.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(partialUpdatedTechnique))
            )
            .andExpect(status().isOk());

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);
        Technique testTechnique = techniqueList.get(techniqueList.size() - 1);
        assertThat(testTechnique.getDescription()).isEqualTo(UPDATED_DESCRIPTION);
    }

    @Test
    void patchNonExistingTechnique() throws Exception {
        int databaseSizeBeforeUpdate = techniqueRepository.findAll().size();
        technique.setId(UUID.randomUUID().toString());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restTechniqueMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, technique.getId())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(technique))
            )
            .andExpect(status().isBadRequest());

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(0)).save(technique);
    }

    @Test
    void patchWithIdMismatchTechnique() throws Exception {
        int databaseSizeBeforeUpdate = techniqueRepository.findAll().size();
        technique.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTechniqueMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, UUID.randomUUID().toString())
                    .contentType("application/merge-patch+json")
                    .content(TestUtil.convertObjectToJsonBytes(technique))
            )
            .andExpect(status().isBadRequest());

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(0)).save(technique);
    }

    @Test
    void patchWithMissingIdPathParamTechnique() throws Exception {
        int databaseSizeBeforeUpdate = techniqueRepository.findAll().size();
        technique.setId(UUID.randomUUID().toString());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restTechniqueMockMvc
            .perform(
                patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(TestUtil.convertObjectToJsonBytes(technique))
            )
            .andExpect(status().isMethodNotAllowed());

        // Validate the Technique in the database
        List<Technique> techniqueList = techniqueRepository.findAll();
        assertThat(techniqueList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(0)).save(technique);
    }

    @Test
    void deleteTechnique() throws Exception {
        // Initialize the database
        techniqueRepository.save(technique);

        int databaseSizeBeforeDelete = techniqueRepository.findAll().size();

        // Delete the technique
        restTechniqueMockMvc
            .perform(delete(ENTITY_API_URL_ID, technique.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        List<Technique> techniqueList = techniqueRepository.findAll();
        assertThat(techniqueList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Technique in Elasticsearch
        verify(mockTechniqueSearchRepository, times(1)).deleteById(technique.getId());
    }

    @Test
    void searchTechnique() throws Exception {
        // Configure the mock search repository
        // Initialize the database
        techniqueRepository.save(technique);
        when(mockTechniqueSearchRepository.search(queryStringQuery("id:" + technique.getId())))
            .thenReturn(Collections.singletonList(technique));

        // Search the technique
        restTechniqueMockMvc
            .perform(get(ENTITY_SEARCH_API_URL + "?query=id:" + technique.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(technique.getId())))
            .andExpect(jsonPath("$.[*].description").value(hasItem(DEFAULT_DESCRIPTION)));
    }
}
