package fr.lalourche.hellojhipster.gateway.repository.rowmapper;

import fr.lalourche.hellojhipster.gateway.domain.Ingredient;
import fr.lalourche.hellojhipster.gateway.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Ingredient}, with proper type conversions.
 */
@Service
public class IngredientRowMapper implements BiFunction<Row, String, Ingredient> {

    private final ColumnConverter converter;

    public IngredientRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Ingredient} stored in the database.
     */
    @Override
    public Ingredient apply(Row row, String prefix) {
        Ingredient entity = new Ingredient();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        return entity;
    }
}
