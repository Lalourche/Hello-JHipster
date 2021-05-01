package fr.lalourche.hellojhipster.gateway.repository.rowmapper;

import fr.lalourche.hellojhipster.gateway.domain.Step;
import fr.lalourche.hellojhipster.gateway.service.ColumnConverter;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Step}, with proper type conversions.
 */
@Service
public class StepRowMapper implements BiFunction<Row, String, Step> {

    private final ColumnConverter converter;

    public StepRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Step} stored in the database.
     */
    @Override
    public Step apply(Row row, String prefix) {
        Step entity = new Step();
        entity.setId(converter.fromRow(row, prefix + "_id", Long.class));
        entity.setAction(converter.fromRow(row, prefix + "_action", String.class));
        return entity;
    }
}
