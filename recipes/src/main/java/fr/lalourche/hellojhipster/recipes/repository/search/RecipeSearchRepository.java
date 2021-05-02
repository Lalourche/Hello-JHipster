package fr.lalourche.hellojhipster.recipes.repository.search;

import fr.lalourche.hellojhipster.recipes.domain.Recipe;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Recipe} entity.
 */
public interface RecipeSearchRepository extends ElasticsearchRepository<Recipe, Long> {}
