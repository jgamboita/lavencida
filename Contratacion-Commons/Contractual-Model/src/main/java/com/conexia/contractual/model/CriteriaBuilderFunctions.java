package com.conexia.contractual.model;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Path;

import static com.conexia.contractual.model.PostgreSQLDialect.*;

public abstract class CriteriaBuilderFunctions {

    public static Expression<?> functionStringAgg(CriteriaBuilder cb, Path<Object> toJoin, String delimiter) {
        return cb.function(STRING_AGG,
                String.class,
                toJoin,
                cb.literal(delimiter)
        );
    }

    public static Expression<?> functionArrayAgg(CriteriaBuilder cb, Path<Object> toJoin) {
        return cb.function(ARRAY_AGG,
                String.class,
                toJoin
        );
    }

    public static Expression<? extends Number> functionArrayLength(CriteriaBuilder cb, Expression<?> expression, Integer position) {
        return cb.function(ARRAY_LENGTH,
                Number.class,
                expression,
                cb.literal(position)
        );
    }
}