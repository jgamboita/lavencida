package com.conexia.contractual.model;

import org.hibernate.dialect.PostgreSQL95Dialect;
import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.type.StandardBasicTypes;

public class PostgreSQLDialect extends PostgreSQL95Dialect {

    public static final String STRING_AGG = "string_agg";
    public static final String ARRAY_AGG = "array_agg";
    public static final String ARRAY_LENGTH = "array_length";

    public PostgreSQLDialect() {
        super();
        registerFunction(STRING_AGG, new SQLFunctionTemplate(StandardBasicTypes.STRING, "string_agg(?1, ?2)"));
        registerFunction(ARRAY_AGG, new SQLFunctionTemplate(StandardBasicTypes.STRING, "array_agg(DISTINCT ?1)"));
        registerFunction(ARRAY_LENGTH, new SQLFunctionTemplate(StandardBasicTypes.STRING, "array_length(?1, ?2)"));
    }
}
