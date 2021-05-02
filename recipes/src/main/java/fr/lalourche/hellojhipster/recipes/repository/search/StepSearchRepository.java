package fr.lalourche.hellojhipster.recipes.repository.search;

import fr.lalourche.hellojhipster.recipes.domain.Step;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Step} entity.
 */
public interface StepSearchRepository extends ElasticsearchRepository<Step, Long> {}
