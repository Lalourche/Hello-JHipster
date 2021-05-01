package fr.lalourche.hellojhipster.gateway.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import fr.lalourche.hellojhipster.gateway.domain.Recipe;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link Recipe} entity.
 */
public interface RecipeSearchRepository extends ReactiveElasticsearchRepository<Recipe, Long>, RecipeSearchRepositoryInternal {}

interface RecipeSearchRepositoryInternal {
    Flux<Recipe> search(String query);
}

class RecipeSearchRepositoryInternalImpl implements RecipeSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    RecipeSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Recipe> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return reactiveElasticsearchTemplate.search(nativeSearchQuery, Recipe.class).map(SearchHit::getContent);
    }
}
