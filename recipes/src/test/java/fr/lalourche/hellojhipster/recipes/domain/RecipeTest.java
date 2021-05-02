package fr.lalourche.hellojhipster.recipes.domain;

import static org.assertj.core.api.Assertions.assertThat;

import fr.lalourche.hellojhipster.recipes.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class RecipeTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Recipe.class);
        Recipe recipe1 = new Recipe();
        recipe1.setId(1L);
        Recipe recipe2 = new Recipe();
        recipe2.setId(recipe1.getId());
        assertThat(recipe1).isEqualTo(recipe2);
        recipe2.setId(2L);
        assertThat(recipe1).isNotEqualTo(recipe2);
        recipe1.setId(null);
        assertThat(recipe1).isNotEqualTo(recipe2);
    }
}
