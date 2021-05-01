package fr.lalourche.hellojhipster.gateway.repository;

import static org.springframework.data.relational.core.query.Criteria.where;
import static org.springframework.data.relational.core.query.Query.query;

import fr.lalourche.hellojhipster.gateway.domain.Ingredient;
import fr.lalourche.hellojhipster.gateway.domain.Recipe;
import fr.lalourche.hellojhipster.gateway.domain.Step;
import fr.lalourche.hellojhipster.gateway.domain.enumeration.Cooking;
import fr.lalourche.hellojhipster.gateway.repository.rowmapper.RecipeRowMapper;
import fr.lalourche.hellojhipster.gateway.service.EntityManager;
import fr.lalourche.hellojhipster.gateway.service.EntityManager.LinkTable;
import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.BiFunction;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.sql.Column;
import org.springframework.data.relational.core.sql.Expression;
import org.springframework.data.relational.core.sql.Select;
import org.springframework.data.relational.core.sql.SelectBuilder.SelectFromAndJoin;
import org.springframework.data.relational.core.sql.Table;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Spring Data SQL reactive custom repository implementation for the Recipe entity.
 */
@SuppressWarnings("unused")
class RecipeRepositoryInternalImpl implements RecipeRepositoryInternal {

    private final DatabaseClient db;
    private final R2dbcEntityTemplate r2dbcEntityTemplate;
    private final EntityManager entityManager;

    private final RecipeRowMapper recipeMapper;

    private static final Table entityTable = Table.aliased("recipe", EntityManager.ENTITY_ALIAS);

    private static final EntityManager.LinkTable ingredientsLink = new LinkTable("rel_recipe__ingredients", "recipe_id", "ingredients_id");
    private static final EntityManager.LinkTable stepsLink = new LinkTable("rel_recipe__steps", "recipe_id", "steps_id");

    public RecipeRepositoryInternalImpl(R2dbcEntityTemplate template, EntityManager entityManager, RecipeRowMapper recipeMapper) {
        this.db = template.getDatabaseClient();
        this.r2dbcEntityTemplate = template;
        this.entityManager = entityManager;
        this.recipeMapper = recipeMapper;
    }

    @Override
    public Flux<Recipe> findAllBy(Pageable pageable) {
        return findAllBy(pageable, null);
    }

    @Override
    public Flux<Recipe> findAllBy(Pageable pageable, Criteria criteria) {
        return createQuery(pageable, criteria).all();
    }

    RowsFetchSpec<Recipe> createQuery(Pageable pageable, Criteria criteria) {
        List<Expression> columns = RecipeSqlHelper.getColumns(entityTable, EntityManager.ENTITY_ALIAS);
        SelectFromAndJoin selectFrom = Select.builder().select(columns).from(entityTable);

        String select = entityManager.createSelect(selectFrom, Recipe.class, pageable, criteria);
        String alias = entityTable.getReferenceName().getReference();
        String selectWhere = Optional
            .ofNullable(criteria)
            .map(
                crit ->
                    new StringBuilder(select)
                        .append(" ")
                        .append("WHERE")
                        .append(" ")
                        .append(alias)
                        .append(".")
                        .append(crit.toString())
                        .toString()
            )
            .orElse(select); // TODO remove once https://github.com/spring-projects/spring-data-jdbc/issues/907 will be fixed
        return db.sql(selectWhere).map(this::process);
    }

    @Override
    public Flux<Recipe> findAll() {
        return findAllBy(null, null);
    }

    @Override
    public Mono<Recipe> findById(Long id) {
        return createQuery(null, where("id").is(id)).one();
    }

    @Override
    public Mono<Recipe> findOneWithEagerRelationships(Long id) {
        return findById(id);
    }

    @Override
    public Flux<Recipe> findAllWithEagerRelationships() {
        return findAll();
    }

    @Override
    public Flux<Recipe> findAllWithEagerRelationships(Pageable page) {
        return findAllBy(page);
    }

    private Recipe process(Row row, RowMetadata metadata) {
        Recipe entity = recipeMapper.apply(row, "e");
        return entity;
    }

    @Override
    public <S extends Recipe> Mono<S> insert(S entity) {
        return entityManager.insert(entity);
    }

    @Override
    public <S extends Recipe> Mono<S> save(S entity) {
        if (entity.getId() == null) {
            return insert(entity).flatMap(savedEntity -> updateRelations(savedEntity));
        } else {
            return update(entity)
                .map(
                    numberOfUpdates -> {
                        if (numberOfUpdates.intValue() <= 0) {
                            throw new IllegalStateException("Unable to update Recipe with id = " + entity.getId());
                        }
                        return entity;
                    }
                )
                .then(updateRelations(entity));
        }
    }

    @Override
    public Mono<Integer> update(Recipe entity) {
        //fixme is this the proper way?
        return r2dbcEntityTemplate.update(entity).thenReturn(1);
    }

    @Override
    public Mono<Void> deleteById(Long entityId) {
        return deleteRelations(entityId)
            .then(r2dbcEntityTemplate.delete(Recipe.class).matching(query(where("id").is(entityId))).all().then());
    }

    protected <S extends Recipe> Mono<S> updateRelations(S entity) {
        Mono<Void> result = entityManager
            .updateLinkTable(ingredientsLink, entity.getId(), entity.getIngredients().stream().map(Ingredient::getId))
            .then();
        result = result.and(entityManager.updateLinkTable(stepsLink, entity.getId(), entity.getSteps().stream().map(Step::getId)));
        return result.thenReturn(entity);
    }

    protected Mono<Void> deleteRelations(Long entityId) {
        return entityManager.deleteFromLinkTable(ingredientsLink, entityId).and(entityManager.deleteFromLinkTable(stepsLink, entityId));
    }
}

class RecipeSqlHelper {

    static List<Expression> getColumns(Table table, String columnPrefix) {
        List<Expression> columns = new ArrayList<>();
        columns.add(Column.aliased("id", table, columnPrefix + "_id"));
        columns.add(Column.aliased("name", table, columnPrefix + "_name"));
        columns.add(Column.aliased("cooking", table, columnPrefix + "_cooking"));
        columns.add(Column.aliased("cooking_time", table, columnPrefix + "_cooking_time"));
        columns.add(Column.aliased("picture", table, columnPrefix + "_picture"));
        columns.add(Column.aliased("picture_content_type", table, columnPrefix + "_picture_content_type"));

        return columns;
    }
}
