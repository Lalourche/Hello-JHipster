package fr.lalourche.hellojhipster.gateway.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import fr.lalourche.hellojhipster.gateway.domain.Technique;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link Technique} entity.
 */
public interface TechniqueSearchRepository extends ReactiveElasticsearchRepository<Technique, Long>, TechniqueSearchRepositoryInternal {}

interface TechniqueSearchRepositoryInternal {
    Flux<Technique> search(String query);
}

class TechniqueSearchRepositoryInternalImpl implements TechniqueSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    TechniqueSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Technique> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return reactiveElasticsearchTemplate.search(nativeSearchQuery, Technique.class).map(SearchHit::getContent);
    }
}
