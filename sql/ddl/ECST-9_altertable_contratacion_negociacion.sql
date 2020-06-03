ALTER TABLE contratacion.negociacion ADD COLUMN tipo_otro_si integer;

ALTER TABLE contratacion.negociacion ADD COLUMN numero_otro_si integer;

ALTER TABLE contratacion.negociacion ADD COLUMN negociacion_origen integer
        constraint "FK_negociacion_negociacion_origen"
            references contratacion.negociacion;

ALTER TABLE contratacion.contrato
 ADD COLUMN fecha_elaboracion_otrosi date;
