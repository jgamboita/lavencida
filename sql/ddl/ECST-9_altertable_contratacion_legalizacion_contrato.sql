
ALTER TABLE contratacion.legalizacion_contrato ADD COLUMN minuta_otro_si_id integer
        constraint "fk_legalizacion_minuta_otro_si"
            references contratacion.minuta;

ALTER TABLE contratacion.legalizacion_contrato ADD COLUMN observacion_otro_si text;


CREATE INDEX CONCURRENTLY ix_legalizacion_contrato_minuta_otro_si_id
    ON contratacion.legalizacion_contrato (minuta_otro_si_id);

