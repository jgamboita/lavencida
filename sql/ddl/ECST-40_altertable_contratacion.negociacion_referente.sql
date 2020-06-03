ALTER TABLE contratacion.negociacion_referente ADD CONSTRAINT "FK_negociacion_referente_negociacion"
FOREIGN KEY (negociacion_id) REFERENCES contratacion.negociacion(id) on delete cascade ;
