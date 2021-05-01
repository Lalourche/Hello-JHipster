package fr.lalourche.hellojhipster.gateway.repository.rowmapper;

import fr.lalourche.hellojhipster.gateway.domain.Technique;
import fr.lalourche.hellojhipster.gateway.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Technique}, with proper type conversions.
 */
@Service
public class TechniqueRowMapper implements BiFunction<Row, String, Technique> {

    private final ColumnConverter converter;

    public TechniqueRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Technique} stored in the database.
     */
    @Override
    public Technique apply(Row row, String prefix) {
        Technique entity = new Technique();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        return entity;
    }
}
