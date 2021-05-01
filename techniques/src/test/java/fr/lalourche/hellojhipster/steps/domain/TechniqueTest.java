package fr.lalourche.hellojhipster.steps.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.lalourche.hellojhipster.steps.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class TechniqueTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Technique.class);
        Technique technique1 = new Technique();
        technique1.setId("id1");
        Technique technique2 = new Technique();
        technique2.setId(technique1.getId());
        assertThat(technique1).isEqualTo(technique2);
        technique2.setId("id2");
        assertThat(technique1).isNotEqualTo(technique2);
        technique1.setId(null);
        assertThat(technique1).isNotEqualTo(technique2);
    }
}
