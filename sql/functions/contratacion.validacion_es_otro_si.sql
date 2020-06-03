create or replace function contratacion.validacion_es_otro_si(negociacion_id bigint) returns boolean
    language plpgsql
as
$$
BEGIN
    RETURN (
        SELECT CASE WHEN negociacion_padre_id IS NULL THEN FALSE ELSE TRUE END
        FROM contratacion.negociacion
        WHERE id = negociacion_id
    );
END
$$;