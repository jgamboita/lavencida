 -- DROP COLUMNS
 -- contratacion.negociacion - negociacion_referente_id
 ALTER TABLE contratacion.negociacion DROP COLUMN negociacion_referente_id;

  -- DROP COLUMNS
 -- contratacion.negociacion - negociacion_referente_id
 ALTER TABLE contratacion.sede_negociacion_medicamento DROP COLUMN valor_contrato_anterior;

 -- DROP TABLES
 -- contratacion.negociacion_referente
 drop table if exists contratacion.negociacion_referente;
