package com.conexia.contractual.model;

import org.hibernate.boot.MetadataBuilder;
import org.hibernate.boot.spi.MetadataBuilderContributor;
import org.hibernate.dialect.function.StandardSQLFunction;
import org.hibernate.type.StandardBasicTypes;

import static com.conexia.contractual.model.PostgreSQLDialect.*;

public class SqlFunctionsMetadataBuilderContributor implements MetadataBuilderContributor {


    @Override
    public void contribute(MetadataBuilder metadataBuilder) {
        metadataBuilder.applySqlFunction(
                STRING_AGG,
                new StandardSQLFunction(
                        STRING_AGG,
                        StandardBasicTypes.STRING
                )
        );
        metadataBuilder.applySqlFunction(
                ARRAY_AGG,
                new StandardSQLFunction(
                        ARRAY_AGG,
                        StandardBasicTypes.STRING
                )
        );
        metadataBuilder.applySqlFunction(
                ARRAY_LENGTH,
                new StandardSQLFunction(
                        ARRAY_LENGTH,
                        StandardBasicTypes.LONG
                )
        );
    }
}
