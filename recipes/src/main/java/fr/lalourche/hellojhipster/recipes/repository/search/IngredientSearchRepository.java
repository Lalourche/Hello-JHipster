package fr.lalourche.hellojhipster.recipes.repository.search;

import fr.lalourche.hellojhipster.recipes.domain.Ingredient;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Ingredient} entity.
 */
public interface IngredientSearchRepository extends ElasticsearchRepository<Ingredient, Long> {}
