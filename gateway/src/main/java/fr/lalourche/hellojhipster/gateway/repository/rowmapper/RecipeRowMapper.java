package fr.lalourche.hellojhipster.gateway.repository.rowmapper;

import fr.lalourche.hellojhipster.gateway.domain.Recipe;
import fr.lalourche.hellojhipster.gateway.domain.enumeration.Cooking;
import fr.lalourche.hellojhipster.gateway.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Recipe}, with proper type conversions.
 */
@Service
public class RecipeRowMapper implements BiFunction<Row, String, Recipe> {

    private final ColumnConverter converter;

    public RecipeRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Recipe} stored in the database.
     */
    @Override
    public Recipe apply(Row row, String prefix) {
        Recipe entity = new Recipe();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setName(converter.fromRow(row, prefix + "_name", String.class));
        entity.setCooking(converter.fromRow(row, prefix + "_cooking", Cooking.class));
        entity.setCookingTime(converter.fromRow(row, prefix + "_cooking_time", Double.class));
        entity.setPictureContentType(converter.fromRow(row, prefix + "_picture_content_type", String.class));
        entity.setPicture(converter.fromRow(row, prefix + "_picture", byte[].class));
        return entity;
    }
}
