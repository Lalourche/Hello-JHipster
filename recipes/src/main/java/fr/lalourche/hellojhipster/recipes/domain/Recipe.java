package fr.lalourche.hellojhipster.recipes.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.lalourche.hellojhipster.recipes.domain.enumeration.Cooking;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;
import javax.validation.constraints.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * A Recipe.
 */
@Entity
@Table(name = "recipe")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@org.springframework.data.elasticsearch.annotations.Document(indexName = "recipe")
public class Recipe implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "name", nullable = false)
    private String name;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "cooking", nullable = false)
    private Cooking cooking;

    @DecimalMin(value = "0")
    @DecimalMax(value = "65535")
    @Column(name = "cooking_time")
    private Double cookingTime;

    @Lob
    @Column(name = "picture")
    private byte[] picture;

    @Column(name = "picture_content_type")
    private String pictureContentType;

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(
        name = "rel_recipe__ingredients",
        joinColumns = @JoinColumn(name = "recipe_id"),
        inverseJoinColumns = @JoinColumn(name = "ingredients_id")
    )
    @JsonIgnoreProperties(value = { "recipes" }, allowSetters = true)
    private Set<Ingredient> ingredients = new HashSet<>();

    @ManyToMany
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    @JoinTable(
        name = "rel_recipe__steps",
        joinColumns = @JoinColumn(name = "recipe_id"),
        inverseJoinColumns = @JoinColumn(name = "steps_id")
    )
    @JsonIgnoreProperties(value = { "recipes" }, allowSetters = true)
    private Set<Step> steps = new HashSet<>();

    // jhipster-needle-entity-add-field - JHipster will add fields here
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Recipe id(Long id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return this.name;
    }

    public Recipe name(String name) {
        this.name = name;
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Cooking getCooking() {
        return this.cooking;
    }

    public Recipe cooking(Cooking cooking) {
        this.cooking = cooking;
        return this;
    }

    public void setCooking(Cooking cooking) {
        this.cooking = cooking;
    }

    public Double getCookingTime() {
        return this.cookingTime;
    }

    public Recipe cookingTime(Double cookingTime) {
        this.cookingTime = cookingTime;
        return this;
    }

    public void setCookingTime(Double cookingTime) {
        this.cookingTime = cookingTime;
    }

    public byte[] getPicture() {
        return this.picture;
    }

    public Recipe picture(byte[] picture) {
        this.picture = picture;
        return this;
    }

    public void setPicture(byte[] picture) {
        this.picture = picture;
    }

    public String getPictureContentType() {
        return this.pictureContentType;
    }

    public Recipe pictureContentType(String pictureContentType) {
        this.pictureContentType = pictureContentType;
        return this;
    }

    public void setPictureContentType(String pictureContentType) {
        this.pictureContentType = pictureContentType;
    }

    public Set<Ingredient> getIngredients() {
        return this.ingredients;
    }

    public Recipe ingredients(Set<Ingredient> ingredients) {
        this.setIngredients(ingredients);
        return this;
    }

    public Recipe addIngredients(Ingredient ingredient) {
        this.ingredients.add(ingredient);
        ingredient.getRecipes().add(this);
        return this;
    }

    public Recipe removeIngredients(Ingredient ingredient) {
        this.ingredients.remove(ingredient);
        ingredient.getRecipes().remove(this);
        return this;
    }

    public void setIngredients(Set<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public Set<Step> getSteps() {
        return this.steps;
    }

    public Recipe steps(Set<Step> steps) {
        this.setSteps(steps);
        return this;
    }

    public Recipe addSteps(Step step) {
        this.steps.add(step);
        step.getRecipes().add(this);
        return this;
    }

    public Recipe removeSteps(Step step) {
        this.steps.remove(step);
        step.getRecipes().remove(this);
        return this;
    }

    public void setSteps(Set<Step> steps) {
        this.steps = steps;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Recipe)) {
            return false;
        }
        return id != null && id.equals(((Recipe) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Recipe{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", cooking='" + getCooking() + "'" +
            ", cookingTime=" + getCookingTime() +
            ", picture='" + getPicture() + "'" +
            ", pictureContentType='" + getPictureContentType() + "'" +
            "}";
    }
}
