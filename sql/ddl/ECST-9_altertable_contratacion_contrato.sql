
ALTER TABLE contratacion.contrato ADD COLUMN tipo_modificacion_otro_si_id integer;

ALTER TABLE contratacion.contrato ADD COLUMN tipo_adicion_otro_si smallint;

ALTER TABLE contratacion.contrato ADD COLUMN fecha_inicio_otro_si timestamp;

ALTER TABLE contratacion.contrato ADD COLUMN fecha_fin_otro_si timestamp;