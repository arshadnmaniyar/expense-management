package org.example.ingestionpipeline.domain;

import com.pgvector.PGvector;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.sql.SQLException;

/**
 * JPA AttributeConverter for PGvector type.
 * Converts between PGvector object and its String representation for database persistence.
 */
@Converter(autoApply = false) // We will apply this converter explicitly
public class PGVectorConverter implements AttributeConverter<PGvector, String> {

    @Override
    public String convertToDatabaseColumn(PGvector attribute) {
        if (attribute == null) {
            return null;
        }
        // PGvector's toString() method provides the correct format for the database
        return attribute.toString();
    }

    @Override
    public PGvector convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        // PGvector constructor can parse the string representation from the database
        try {
            return new PGvector(dbData);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
