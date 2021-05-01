package fr.lalourche.hellojhipster.gateway.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;

import fr.lalourche.hellojhipster.gateway.IntegrationTest;
import fr.lalourche.hellojhipster.gateway.domain.Recipe;
import fr.lalourche.hellojhipster.gateway.domain.enumeration.Cooking;
import fr.lalourche.hellojhipster.gateway.repository.RecipeRepository;
import fr.lalourche.hellojhipster.gateway.repository.search.RecipeSearchRepository;
import fr.lalourche.hellojhipster.gateway.service.EntityManager;
import java.time.Duration;
import java.util.ArrayList;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.Base64Utils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Integration tests for the {@link RecipeResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureWebTestClient
@WithMockUser
class RecipeResourceIT {

    private static final String DEFAULT_NAME = "AAAAAAAAAA";
    private static final String UPDATED_NAME = "BBBBBBBBBB";

    private static final Cooking DEFAULT_COOKING = Cooking.WITH_COOKING;
    private static final Cooking UPDATED_COOKING = Cooking.WITHOUT_COOKING;

    private static final Double DEFAULT_COOKING_TIME = 0D;
    private static final Double UPDATED_COOKING_TIME = 1D;

    private static final byte[] DEFAULT_PICTURE = TestUtil.createByteArray(1, "0");
    private static final byte[] UPDATED_PICTURE = TestUtil.createByteArray(1, "1");
    private static final String DEFAULT_PICTURE_CONTENT_TYPE = "image/jpg";
    private static final String UPDATED_PICTURE_CONTENT_TYPE = "image/png";

    private static final String ENTITY_API_URL = "/api/recipes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";
    private static final String ENTITY_SEARCH_API_URL = "/api/_search/recipes";

    private static Random random = new Random();
    private static AtomicLong count = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private RecipeRepository recipeRepository;

    @Mock
    private RecipeRepository recipeRepositoryMock;

    /**
     * This repository is mocked in the fr.lalourche.hellojhipster.gateway.repository.search test package.
     *
     * @see fr.lalourche.hellojhipster.gateway.repository.search.RecipeSearchRepositoryMockConfiguration
     */
    @Autowired
    private RecipeSearchRepository mockRecipeSearchRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private WebTestClient webTestClient;

    private Recipe recipe;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Recipe createEntity(EntityManager em) {
        Recipe recipe = new Recipe()
            .name(DEFAULT_NAME)
            .cooking(DEFAULT_COOKING)
            .cookingTime(DEFAULT_COOKING_TIME)
            .picture(DEFAULT_PICTURE)
            .pictureContentType(DEFAULT_PICTURE_CONTENT_TYPE);
        return recipe;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Recipe createUpdatedEntity(EntityManager em) {
        Recipe recipe = new Recipe()
            .name(UPDATED_NAME)
            .cooking(UPDATED_COOKING)
            .cookingTime(UPDATED_COOKING_TIME)
            .picture(UPDATED_PICTURE)
            .pictureContentType(UPDATED_PICTURE_CONTENT_TYPE);
        return recipe;
    }

    public static void deleteEntities(EntityManager em) {
        try {
            em.deleteAll("rel_recipe__ingredients").block();
            em.deleteAll("rel_recipe__steps").block();
            em.deleteAll(Recipe.class).block();
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
        recipe = createEntity(em);
    }

    @Test
    void createRecipe() throws Exception {
        int databaseSizeBeforeCreate = recipeRepository.findAll().collectList().block().size();
        // Configure the mock search repository
        when(mockRecipeSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Create the Recipe
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(recipe))
            .exchange()
            .expectStatus()
            .isCreated();

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll().collectList().block();
        assertThat(recipeList).hasSize(databaseSizeBeforeCreate + 1);
        Recipe testRecipe = recipeList.get(recipeList.size() - 1);
        assertThat(testRecipe.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testRecipe.getCooking()).isEqualTo(DEFAULT_COOKING);
        assertThat(testRecipe.getCookingTime()).isEqualTo(DEFAULT_COOKING_TIME);
        assertThat(testRecipe.getPicture()).isEqualTo(DEFAULT_PICTURE);
        assertThat(testRecipe.getPictureContentType()).isEqualTo(DEFAULT_PICTURE_CONTENT_TYPE);

        // Validate the Recipe in Elasticsearch
        verify(mockRecipeSearchRepository, times(1)).save(testRecipe);
    }

    @Test
    void createRecipeWithExistingId() throws Exception {
        // Create the Recipe with an existing ID
        recipe.setId(1L);

        int databaseSizeBeforeCreate = recipeRepository.findAll().collectList().block().size();

        // An entity with an existing ID cannot be created, so this API call must fail
        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(recipe))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll().collectList().block();
        assertThat(recipeList).hasSize(databaseSizeBeforeCreate);

        // Validate the Recipe in Elasticsearch
        verify(mockRecipeSearchRepository, times(0)).save(recipe);
    }

    @Test
    void checkNameIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipeRepository.findAll().collectList().block().size();
        // set the field null
        recipe.setName(null);

        // Create the Recipe, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(recipe))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Recipe> recipeList = recipeRepository.findAll().collectList().block();
        assertThat(recipeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void checkCookingIsRequired() throws Exception {
        int databaseSizeBeforeTest = recipeRepository.findAll().collectList().block().size();
        // set the field null
        recipe.setCooking(null);

        // Create the Recipe, which fails.

        webTestClient
            .post()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(recipe))
            .exchange()
            .expectStatus()
            .isBadRequest();

        List<Recipe> recipeList = recipeRepository.findAll().collectList().block();
        assertThat(recipeList).hasSize(databaseSizeBeforeTest);
    }

    @Test
    void getAllRecipesAsStream() {
        // Initialize the database
        recipeRepository.save(recipe).block();

        List<Recipe> recipeList = webTestClient
            .get()
            .uri(ENTITY_API_URL)
            .accept(MediaType.APPLICATION_NDJSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentTypeCompatibleWith(MediaType.APPLICATION_NDJSON)
            .returnResult(Recipe.class)
            .getResponseBody()
            .filter(recipe::equals)
            .collectList()
            .block(Duration.ofSeconds(5));

        assertThat(recipeList).isNotNull();
        assertThat(recipeList).hasSize(1);
        Recipe testRecipe = recipeList.get(0);
        assertThat(testRecipe.getName()).isEqualTo(DEFAULT_NAME);
        assertThat(testRecipe.getCooking()).isEqualTo(DEFAULT_COOKING);
        assertThat(testRecipe.getCookingTime()).isEqualTo(DEFAULT_COOKING_TIME);
        assertThat(testRecipe.getPicture()).isEqualTo(DEFAULT_PICTURE);
        assertThat(testRecipe.getPictureContentType()).isEqualTo(DEFAULT_PICTURE_CONTENT_TYPE);
    }

    @Test
    void getAllRecipes() {
        // Initialize the database
        recipeRepository.save(recipe).block();

        // Get all the recipeList
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
            .value(hasItem(recipe.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].cooking")
            .value(hasItem(DEFAULT_COOKING.toString()))
            .jsonPath("$.[*].cookingTime")
            .value(hasItem(DEFAULT_COOKING_TIME.doubleValue()))
            .jsonPath("$.[*].pictureContentType")
            .value(hasItem(DEFAULT_PICTURE_CONTENT_TYPE))
            .jsonPath("$.[*].picture")
            .value(hasItem(Base64Utils.encodeToString(DEFAULT_PICTURE)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRecipesWithEagerRelationshipsIsEnabled() {
        when(recipeRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(recipeRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllRecipesWithEagerRelationshipsIsNotEnabled() {
        when(recipeRepositoryMock.findAllWithEagerRelationships(any())).thenReturn(Flux.empty());

        webTestClient.get().uri(ENTITY_API_URL + "?eagerload=true").exchange().expectStatus().isOk();

        verify(recipeRepositoryMock, times(1)).findAllWithEagerRelationships(any());
    }

    @Test
    void getRecipe() {
        // Initialize the database
        recipeRepository.save(recipe).block();

        // Get the recipe
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, recipe.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.id")
            .value(is(recipe.getId().intValue()))
            .jsonPath("$.name")
            .value(is(DEFAULT_NAME))
            .jsonPath("$.cooking")
            .value(is(DEFAULT_COOKING.toString()))
            .jsonPath("$.cookingTime")
            .value(is(DEFAULT_COOKING_TIME.doubleValue()))
            .jsonPath("$.pictureContentType")
            .value(is(DEFAULT_PICTURE_CONTENT_TYPE))
            .jsonPath("$.picture")
            .value(is(Base64Utils.encodeToString(DEFAULT_PICTURE)));
    }

    @Test
    void getNonExistingRecipe() {
        // Get the recipe
        webTestClient
            .get()
            .uri(ENTITY_API_URL_ID, Long.MAX_VALUE)
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNotFound();
    }

    @Test
    void putNewRecipe() throws Exception {
        // Configure the mock search repository
        when(mockRecipeSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        recipeRepository.save(recipe).block();

        int databaseSizeBeforeUpdate = recipeRepository.findAll().collectList().block().size();

        // Update the recipe
        Recipe updatedRecipe = recipeRepository.findById(recipe.getId()).block();
        updatedRecipe
            .name(UPDATED_NAME)
            .cooking(UPDATED_COOKING)
            .cookingTime(UPDATED_COOKING_TIME)
            .picture(UPDATED_PICTURE)
            .pictureContentType(UPDATED_PICTURE_CONTENT_TYPE);

        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, updatedRecipe.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(updatedRecipe))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll().collectList().block();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);
        Recipe testRecipe = recipeList.get(recipeList.size() - 1);
        assertThat(testRecipe.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRecipe.getCooking()).isEqualTo(UPDATED_COOKING);
        assertThat(testRecipe.getCookingTime()).isEqualTo(UPDATED_COOKING_TIME);
        assertThat(testRecipe.getPicture()).isEqualTo(UPDATED_PICTURE);
        assertThat(testRecipe.getPictureContentType()).isEqualTo(UPDATED_PICTURE_CONTENT_TYPE);

        // Validate the Recipe in Elasticsearch
        verify(mockRecipeSearchRepository).save(testRecipe);
    }

    @Test
    void putNonExistingRecipe() throws Exception {
        int databaseSizeBeforeUpdate = recipeRepository.findAll().collectList().block().size();
        recipe.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, recipe.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(recipe))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll().collectList().block();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Recipe in Elasticsearch
        verify(mockRecipeSearchRepository, times(0)).save(recipe);
    }

    @Test
    void putWithIdMismatchRecipe() throws Exception {
        int databaseSizeBeforeUpdate = recipeRepository.findAll().collectList().block().size();
        recipe.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(recipe))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll().collectList().block();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Recipe in Elasticsearch
        verify(mockRecipeSearchRepository, times(0)).save(recipe);
    }

    @Test
    void putWithMissingIdPathParamRecipe() throws Exception {
        int databaseSizeBeforeUpdate = recipeRepository.findAll().collectList().block().size();
        recipe.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .put()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.APPLICATION_JSON)
            .bodyValue(TestUtil.convertObjectToJsonBytes(recipe))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll().collectList().block();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Recipe in Elasticsearch
        verify(mockRecipeSearchRepository, times(0)).save(recipe);
    }

    @Test
    void partialUpdateRecipeWithPatch() throws Exception {
        // Initialize the database
        recipeRepository.save(recipe).block();

        int databaseSizeBeforeUpdate = recipeRepository.findAll().collectList().block().size();

        // Update the recipe using partial update
        Recipe partialUpdatedRecipe = new Recipe();
        partialUpdatedRecipe.setId(recipe.getId());

        partialUpdatedRecipe.name(UPDATED_NAME).cookingTime(UPDATED_COOKING_TIME);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedRecipe.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedRecipe))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll().collectList().block();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);
        Recipe testRecipe = recipeList.get(recipeList.size() - 1);
        assertThat(testRecipe.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRecipe.getCooking()).isEqualTo(DEFAULT_COOKING);
        assertThat(testRecipe.getCookingTime()).isEqualTo(UPDATED_COOKING_TIME);
        assertThat(testRecipe.getPicture()).isEqualTo(DEFAULT_PICTURE);
        assertThat(testRecipe.getPictureContentType()).isEqualTo(DEFAULT_PICTURE_CONTENT_TYPE);
    }

    @Test
    void fullUpdateRecipeWithPatch() throws Exception {
        // Initialize the database
        recipeRepository.save(recipe).block();

        int databaseSizeBeforeUpdate = recipeRepository.findAll().collectList().block().size();

        // Update the recipe using partial update
        Recipe partialUpdatedRecipe = new Recipe();
        partialUpdatedRecipe.setId(recipe.getId());

        partialUpdatedRecipe
            .name(UPDATED_NAME)
            .cooking(UPDATED_COOKING)
            .cookingTime(UPDATED_COOKING_TIME)
            .picture(UPDATED_PICTURE)
            .pictureContentType(UPDATED_PICTURE_CONTENT_TYPE);

        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, partialUpdatedRecipe.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(partialUpdatedRecipe))
            .exchange()
            .expectStatus()
            .isOk();

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll().collectList().block();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);
        Recipe testRecipe = recipeList.get(recipeList.size() - 1);
        assertThat(testRecipe.getName()).isEqualTo(UPDATED_NAME);
        assertThat(testRecipe.getCooking()).isEqualTo(UPDATED_COOKING);
        assertThat(testRecipe.getCookingTime()).isEqualTo(UPDATED_COOKING_TIME);
        assertThat(testRecipe.getPicture()).isEqualTo(UPDATED_PICTURE);
        assertThat(testRecipe.getPictureContentType()).isEqualTo(UPDATED_PICTURE_CONTENT_TYPE);
    }

    @Test
    void patchNonExistingRecipe() throws Exception {
        int databaseSizeBeforeUpdate = recipeRepository.findAll().collectList().block().size();
        recipe.setId(count.incrementAndGet());

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, recipe.getId())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(recipe))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll().collectList().block();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Recipe in Elasticsearch
        verify(mockRecipeSearchRepository, times(0)).save(recipe);
    }

    @Test
    void patchWithIdMismatchRecipe() throws Exception {
        int databaseSizeBeforeUpdate = recipeRepository.findAll().collectList().block().size();
        recipe.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL_ID, count.incrementAndGet())
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(recipe))
            .exchange()
            .expectStatus()
            .isBadRequest();

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll().collectList().block();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Recipe in Elasticsearch
        verify(mockRecipeSearchRepository, times(0)).save(recipe);
    }

    @Test
    void patchWithMissingIdPathParamRecipe() throws Exception {
        int databaseSizeBeforeUpdate = recipeRepository.findAll().collectList().block().size();
        recipe.setId(count.incrementAndGet());

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        webTestClient
            .patch()
            .uri(ENTITY_API_URL)
            .contentType(MediaType.valueOf("application/merge-patch+json"))
            .bodyValue(TestUtil.convertObjectToJsonBytes(recipe))
            .exchange()
            .expectStatus()
            .isEqualTo(405);

        // Validate the Recipe in the database
        List<Recipe> recipeList = recipeRepository.findAll().collectList().block();
        assertThat(recipeList).hasSize(databaseSizeBeforeUpdate);

        // Validate the Recipe in Elasticsearch
        verify(mockRecipeSearchRepository, times(0)).save(recipe);
    }

    @Test
    void deleteRecipe() {
        // Configure the mock search repository
        when(mockRecipeSearchRepository.deleteById(anyLong())).thenReturn(Mono.empty());
        // Initialize the database
        recipeRepository.save(recipe).block();

        int databaseSizeBeforeDelete = recipeRepository.findAll().collectList().block().size();

        // Delete the recipe
        webTestClient
            .delete()
            .uri(ENTITY_API_URL_ID, recipe.getId())
            .accept(MediaType.APPLICATION_JSON)
            .exchange()
            .expectStatus()
            .isNoContent();

        // Validate the database contains one less item
        List<Recipe> recipeList = recipeRepository.findAll().collectList().block();
        assertThat(recipeList).hasSize(databaseSizeBeforeDelete - 1);

        // Validate the Recipe in Elasticsearch
        verify(mockRecipeSearchRepository, times(1)).deleteById(recipe.getId());
    }

    @Test
    void searchRecipe() {
        // Configure the mock search repository
        when(mockRecipeSearchRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        // Initialize the database
        recipeRepository.save(recipe).block();
        when(mockRecipeSearchRepository.search("id:" + recipe.getId())).thenReturn(Flux.just(recipe));

        // Search the recipe
        webTestClient
            .get()
            .uri(ENTITY_SEARCH_API_URL + "?query=id:" + recipe.getId())
            .exchange()
            .expectStatus()
            .isOk()
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.[*].id")
            .value(hasItem(recipe.getId().intValue()))
            .jsonPath("$.[*].name")
            .value(hasItem(DEFAULT_NAME))
            .jsonPath("$.[*].cooking")
            .value(hasItem(DEFAULT_COOKING.toString()))
            .jsonPath("$.[*].cookingTime")
            .value(hasItem(DEFAULT_COOKING_TIME.doubleValue()))
            .jsonPath("$.[*].pictureContentType")
            .value(hasItem(DEFAULT_PICTURE_CONTENT_TYPE))
            .jsonPath("$.[*].picture")
            .value(hasItem(Base64Utils.encodeToString(DEFAULT_PICTURE)));
    }
}
