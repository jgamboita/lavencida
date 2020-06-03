
ALTER TABLE contratacion.sede_negociacion_procedimiento ADD COLUMN valor_contrato_anterior numeric;

ALTER TABLE contratacion.sede_negociacion_procedimiento ADD COLUMN item_visible boolean;

ALTER TABLE contratacion.sede_negociacion_procedimiento ADD COLUMN tipo_modificacion_otro_si_id integer;

ALTER TABLE contratacion.sede_negociacion_procedimiento ADD COLUMN fecha_inicio_otro_si date;

ALTER TABLE contratacion.sede_negociacion_procedimiento ADD COLUMN fecha_fin_otro_si date;

ALTER TABLE contratacion.sede_negociacion_procedimiento ADD COLUMN tipo_adicion_otro_si integer;

