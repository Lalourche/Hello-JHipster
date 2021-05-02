package fr.lalourche.hellojhipster.recipes.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * A Step.
 */
@Entity
@Table(name = "step")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "step")
public class Step implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "action", nullable = false)
    private String action;

    @ManyToMany(mappedBy = "steps")
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JsonIgnoreProperties(value = { "ingredients", "steps" }, allowSetters = true)
    private Set<Recipe> recipes = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Step id(Long id) {
        this.id = id;
        return this;
    }

    public String getAction() {
        return this.action;
    }

    public Step action(String action) {
        this.action = action;
        return this;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public Set<Recipe> getRecipes() {
        return this.recipes;
    }

    public Step recipes(Set<Recipe> recipes) {
        this.setRecipes(recipes);
        return this;
    }

    public Step addRecipe(Recipe recipe) {
        this.recipes.add(recipe);
        recipe.getSteps().add(this);
        return this;
    }

    public Step removeRecipe(Recipe recipe) {
        this.recipes.remove(recipe);
        recipe.getSteps().remove(this);
        return this;
    }

    public void setRecipes(Set<Recipe> recipes) {
        if (this.recipes != null) {
            this.recipes.forEach(i -> i.removeSteps(this));
        }
        if (recipes != null) {
            recipes.forEach(i -> i.addSteps(this));
        }
        this.recipes = recipes;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Step)) {
            return false;
        }
        return id != null && id.equals(((Step) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Step{" +
            "id=" + getId() +
            ", action='" + getAction() + "'" +
            "}";
    }
}
