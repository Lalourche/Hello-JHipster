package fr.lalourche.hellojhipster.recipes.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.lalourche.hellojhipster.recipes.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class StepTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Step.class);
        Step step1 = new Step();
        step1.setId(1L);
        Step step2 = new Step();
        step2.setId(step1.getId());
        assertThat(step1).isEqualTo(step2);
        step2.setId(2L);
        assertThat(step1).isNotEqualTo(step2);
        step1.setId(null);
        assertThat(step1).isNotEqualTo(step2);
    }
}
