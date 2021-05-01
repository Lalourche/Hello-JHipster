package fr.lalourche.hellojhipster.steps.repository.search;

import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Configuration;

/**
 * Configure a Mock version of {@link TechniqueSearchRepository} to test the
 * application without starting Elasticsearch.
 */
@Configuration
public class TechniqueSearchRepositoryMockConfiguration {

    @MockBean
    private TechniqueSearchRepository mockTechniqueSearchRepository;
}
