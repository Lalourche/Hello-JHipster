package fr.lalourche.hellojhipster.gateway.repository.search;

import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import fr.lalourche.hellojhipster.gateway.domain.Step;
import org.springframework.data.elasticsearch.core.ReactiveElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.repository.ReactiveElasticsearchRepository;
import reactor.core.publisher.Flux;

/**
 * Spring Data Elasticsearch repository for the {@link Step} entity.
 */
public interface StepSearchRepository extends ReactiveElasticsearchRepository<Step, Long>, StepSearchRepositoryInternal {}

interface StepSearchRepositoryInternal {
    Flux<Step> search(String query);
}

class StepSearchRepositoryInternalImpl implements StepSearchRepositoryInternal {

    private final ReactiveElasticsearchTemplate reactiveElasticsearchTemplate;

    StepSearchRepositoryInternalImpl(ReactiveElasticsearchTemplate reactiveElasticsearchTemplate) {
        this.reactiveElasticsearchTemplate = reactiveElasticsearchTemplate;
    }

    @Override
    public Flux<Step> search(String query) {
        NativeSearchQuery nativeSearchQuery = new NativeSearchQuery(queryStringQuery(query));
        return reactiveElasticsearchTemplate.search(nativeSearchQuery, Step.class).map(SearchHit::getContent);
    }
}
