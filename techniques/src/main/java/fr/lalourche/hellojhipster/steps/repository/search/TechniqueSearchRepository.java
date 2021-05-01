package fr.lalourche.hellojhipster.steps.repository.search;

import fr.lalourche.hellojhipster.steps.domain.Technique;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Spring Data Elasticsearch repository for the {@link Technique} entity.
 */
public interface TechniqueSearchRepository extends ElasticsearchRepository<Technique, String> {}
