--DROP FK
ALTER TABLE contratacion.negociacion DROP CONSTRAINT "FK_negociacion_negociacion_referente";
--DROP FK
ALTER TABLE contratacion.negociacion_referente DROP CONSTRAINT "FK_negociacion_referente_negociacion";
