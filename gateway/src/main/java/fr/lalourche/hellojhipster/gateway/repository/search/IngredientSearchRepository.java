package fr.lalourche.hellojhipster.gateway.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import fr.lalourche.hellojhipster.gateway.domain.Ingredient;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link Ingredient} entity.
 */
public interface IngredientSearchRepository extends ReactiveElasticsearchRepository<Ingredient, Long>, IngredientSearchRepositoryInternal {}

interface IngredientSearchRepositoryInternal {
    Flux<Ingredient> search(String query);
}

class IngredientSearchRepositoryInternalImpl implements IngredientSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    IngredientSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Ingredient> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return reactiveElasticsearchTemplate.search(nativeSearchQuery, Ingredient.class).map(SearchHit::getContent);
    }
}
