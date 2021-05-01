package fr.lalourche.hellojhipster.gateway.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import fr.lalourche.hellojhipster.gateway.IntegrationTest;
import fr.lalourche.hellojhipster.gateway.domain.Ingredient;
import fr.lalourche.hellojhipster.gateway.domain.Recipe;
import fr.lalourche.hellojhipster.gateway.repository.IngredientRepository;
import fr.lalourche.hellojhipster.gateway.repository.search.IngredientSearchRepository;
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
 * Integration tests for the {@link IngredientResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
class IngredientResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final String ENTITY_API_URL = "/api/ingredients";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/ingredients";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private IngredientRepository ingredientRepository;

    /**
     * This repository is mocked in the fr.lalourche.hellojhipster.gateway.repository.search test package.
     *
     * @see fr.lalourche.hellojhipster.gateway.repository.search.IngredientSearchRepositoryMockConfiguration
     */
    @Autowired
    private IngredientSearchRepository mockIngredientSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Ingredient ingredient;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Ingredient createEntity(EntityManager em) {
        Ingredient ingredient = new Ingredient().name(DEFAULT_NAME);
        // Add required entity
        Recipe recipe;
        recipe = em.insert(RecipeResourceIT.createEntity(em)).block();
        ingredient.getRecipes().add(recipe);
        return ingredient;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Ingredient createUpdatedEntity(EntityManager em) {
        Ingredient ingredient = new Ingredient().name(UPDATED_NAME);
        // Add required entity
        Recipe recipe;
        recipe = em.insert(RecipeResourceIT.createUpdatedEntity(em)).block();
        ingredient.getRecipes().add(recipe);
        return ingredient;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll(Ingredient.class).block();
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
        ingredient = createEntity(em);
    }

    @Test
    void createIngredient() throws Exception {
        int databaseSizeBeforeCreate = ingredientRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockIngredientSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the Ingredient
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(ingredient))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Ingredient in the database
        List<Ingredient> ingredientList = ingredientRepository.findAll().collectList().block();
        assertThat(ingredientList).hasSize(databaseSizeBeforeCreate + 1);
        Ingredient testIngredient = ingredientList.get(ingredientList.size() - 1);
        assertThat(testIngredient.getName()).isEqualTo(DEFAULT_NAME);

        // Validate the Ingredient in Elasticsearch
        verify(mockIngredientSearchRepository, times(1)).save(testIngredient);
    }

    @Test
    void createIngredientWithExistingId() throws Exception {
        // Create the Ingredient with an existing ID
        ingredient.setId(1L);

        int databaseSizeBeforeCreate = ingredientRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(ingredient))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Ingredient in the database
        List<Ingredient> ingredientList = ingredientRepository.findAll().collectList().block();
        assertThat(ingredientList).hasSize(databaseSizeBeforeCreate);

        // Validate the Ingredient in Elasticsearch
        verify(mockIngredientSearchRepository, times(0)).save(ingredient);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = ingredientRepository.findAll().collectList().block().size();
        // set the field null
        ingredient.setName(null);

        // Create the Ingredient, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(ingredient))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Ingredient> ingredientList = ingredientRepository.findAll().collectList().block();
        assertThat(ingredientList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllIngredientsAsStream() {
        // Initialize the database
        ingredientRepository.save(ingredient).block();

        List<Ingredient> ingredientList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Ingredient.class)
            .getResponseBody()
            .filter(ingredient::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(ingredientList).isNotNull();
        assertThat(ingredientList).hasSize(1);
        Ingredient testIngredient = ingredientList.get(0);
        assertThat(testIngredient.getName()).isEqualTo(DEFAULT_NAME);
    }

    @Test
    void getAllIngredients() {
        // Initialize the database
        ingredientRepository.save(ingredient).block();

        // Get all the ingredientList
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
            .value(hasItem(ingredient.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME));
    }

    @Test
    void getIngredient() {
        // Initialize the database
        ingredientRepository.save(ingredient).block();

        // Get the ingredient
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, ingredient.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(ingredient.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME));
    }

    @Test
    void getNonExistingIngredient() {
        // Get the ingredient
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewIngredient() throws Exception {
        // Configure the mock search repository
        when(mockIngredientSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        ingredientRepository.save(ingredient).block();

        int databaseSizeBeforeUpdate = ingredientRepository.findAll().collectList().block().size();

        // Update the ingredient
        Ingredient updatedIngredient = ingredientRepository.findById(ingredient.getId()).block();
        updatedIngredient.name(UPDATED_NAME);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedIngredient.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedIngredient))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Ingredient in the database
        List<Ingredient> ingredientList = ingredientRepository.findAll().collectList().block();
        assertThat(ingredientList).hasSize(databaseSizeBeforeUpdate);
        Ingredient testIngredient = ingredientList.get(ingredientList.size() - 1);
        assertThat(testIngredient.getName()).isEqualTo(UPDATED_NAME);

        // Validate the Ingredient in Elasticsearch
        verify(mockIngredientSearchRepository).save(testIngredient);
    }

    @Test
    void putNonExistingIngredient() throws Exception {
        int databaseSizeBeforeUpdate = ingredientRepository.findAll().collectList().block().size();
        ingredient.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, ingredient.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(ingredient))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Ingredient in the database
        List<Ingredient> ingredientList = ingredientRepository.findAll().collectList().block();
        assertThat(ingredientList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Ingredient in Elasticsearch
        verify(mockIngredientSearchRepository, times(0)).save(ingredient);
    }

    @Test
    void putWithIdMismatchIngredient() throws Exception {
        int databaseSizeBeforeUpdate = ingredientRepository.findAll().collectList().block().size();
        ingredient.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(ingredient))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Ingredient in the database
        List<Ingredient> ingredientList = ingredientRepository.findAll().collectList().block();
        assertThat(ingredientList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Ingredient in Elasticsearch
        verify(mockIngredientSearchRepository, times(0)).save(ingredient);
    }

    @Test
    void putWithMissingIdPathParamIngredient() throws Exception {
        int databaseSizeBeforeUpdate = ingredientRepository.findAll().collectList().block().size();
        ingredient.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(ingredient))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Ingredient in the database
        List<Ingredient> ingredientList = ingredientRepository.findAll().collectList().block();
        assertThat(ingredientList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Ingredient in Elasticsearch
        verify(mockIngredientSearchRepository, times(0)).save(ingredient);
    }

    @Test
    void partialUpdateIngredientWithPatch() throws Exception {
        // Initialize the database
        ingredientRepository.save(ingredient).block();

        int databaseSizeBeforeUpdate = ingredientRepository.findAll().collectList().block().size();

        // Update the ingredient using partial update
        Ingredient partialUpdatedIngredient = new Ingredient();
        partialUpdatedIngredient.setId(ingredient.getId());

        partialUpdatedIngredient.name(UPDATED_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedIngredient.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedIngredient))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Ingredient in the database
        List<Ingredient> ingredientList = ingredientRepository.findAll().collectList().block();
        assertThat(ingredientList).hasSize(databaseSizeBeforeUpdate);
        Ingredient testIngredient = ingredientList.get(ingredientList.size() - 1);
        assertThat(testIngredient.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    void fullUpdateIngredientWithPatch() throws Exception {
        // Initialize the database
        ingredientRepository.save(ingredient).block();

        int databaseSizeBeforeUpdate = ingredientRepository.findAll().collectList().block().size();

        // Update the ingredient using partial update
        Ingredient partialUpdatedIngredient = new Ingredient();
        partialUpdatedIngredient.setId(ingredient.getId());

        partialUpdatedIngredient.name(UPDATED_NAME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedIngredient.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedIngredient))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Ingredient in the database
        List<Ingredient> ingredientList = ingredientRepository.findAll().collectList().block();
        assertThat(ingredientList).hasSize(databaseSizeBeforeUpdate);
        Ingredient testIngredient = ingredientList.get(ingredientList.size() - 1);
        assertThat(testIngredient.getName()).isEqualTo(UPDATED_NAME);
    }

    @Test
    void patchNonExistingIngredient() throws Exception {
        int databaseSizeBeforeUpdate = ingredientRepository.findAll().collectList().block().size();
        ingredient.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, ingredient.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(ingredient))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Ingredient in the database
        List<Ingredient> ingredientList = ingredientRepository.findAll().collectList().block();
        assertThat(ingredientList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Ingredient in Elasticsearch
        verify(mockIngredientSearchRepository, times(0)).save(ingredient);
    }

    @Test
    void patchWithIdMismatchIngredient() throws Exception {
        int databaseSizeBeforeUpdate = ingredientRepository.findAll().collectList().block().size();
        ingredient.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(ingredient))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Ingredient in the database
        List<Ingredient> ingredientList = ingredientRepository.findAll().collectList().block();
        assertThat(ingredientList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Ingredient in Elasticsearch
        verify(mockIngredientSearchRepository, times(0)).save(ingredient);
    }

    @Test
    void patchWithMissingIdPathParamIngredient() throws Exception {
        int databaseSizeBeforeUpdate = ingredientRepository.findAll().collectList().block().size();
        ingredient.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(ingredient))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Ingredient in the database
        List<Ingredient> ingredientList = ingredientRepository.findAll().collectList().block();
        assertThat(ingredientList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Ingredient in Elasticsearch
        verify(mockIngredientSearchRepository, times(0)).save(ingredient);
    }

    @Test
    void deleteIngredient() {
        // Configure the mock search repository
        when(mockIngredientSearchRepository.deleteById(anyLong())).thenReturn(Mono.empty());
        // Initialize the database
        ingredientRepository.save(ingredient).block();

        int databaseSizeBeforeDelete = ingredientRepository.findAll().collectList().block().size();

        // Delete the ingredient
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, ingredient.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Ingredient> ingredientList = ingredientRepository.findAll().collectList().block();
        assertThat(ingredientList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Ingredient in Elasticsearch
        verify(mockIngredientSearchRepository, times(1)).deleteById(ingredient.getId());
    }

    @Test
    void searchIngredient() {
        // Configure the mock search repository
        when(mockIngredientSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        ingredientRepository.save(ingredient).block();
        when(mockIngredientSearchRepository.search("id:" + ingredient.getId())).thenReturn(Flux.just(ingredient));

        // Search the ingredient
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + ingredient.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(ingredient.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME));
    }
}
