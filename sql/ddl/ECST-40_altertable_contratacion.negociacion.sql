ALTER TABLE contratacion.negociacion ADD CONSTRAINT "FK_negociacion_negociacion_referente"
FOREIGN KEY (negociacion_referente_id) REFERENCES contratacion.negociacion_referente (id);
