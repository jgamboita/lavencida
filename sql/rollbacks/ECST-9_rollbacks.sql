DROP TRIGGER insertar_contrato ON contratacion.solicitud_contratacion;

DROP TRIGGER insertar_legalizacion_contrato ON contratacion.contrato;


ALTER TABLE contratacion.contrato DROP COLUMN tipo_modificacion_otro_si_id ;

ALTER TABLE contratacion.contrato DROP COLUMN tipo_adicion_otro_si ;

ALTER TABLE contratacion.contrato DROP COLUMN fecha_inicio_otro_si ;

ALTER TABLE contratacion.contrato DROP COLUMN fecha_fin_otro_si ;


ALTER TABLE contratacion.negociacion DROP COLUMN tipo_otro_si ;

ALTER TABLE contratacion.negociacion DROP COLUMN numero_otro_si ;

ALTER TABLE contratacion.negociacion DROP COLUMN negociacion_origen ;


ALTER TABLE contratacion.sede_negociacion_medicamento DROP COLUMN valor_contrato_anterior;

ALTER TABLE contratacion.sede_negociacion_medicamento DROP COLUMN item_visible ;

ALTER TABLE contratacion.sede_negociacion_medicamento DROP COLUMN tipo_modificacion_otro_si_id ;

ALTER TABLE contratacion.sede_negociacion_medicamento DROP COLUMN fecha_inicio_otro_si ;

ALTER TABLE contratacion.sede_negociacion_medicamento DROP COLUMN fecha_fin_otro_si ;

ALTER TABLE contratacion.sede_negociacion_medicamento DROP COLUMN tipo_adicion_otro_si ;



ALTER TABLE contratacion.sede_negociacion_paquete DROP COLUMN valor_contrato_anterior ;

ALTER TABLE contratacion.sede_negociacion_paquete DROP COLUMN item_visible ;

ALTER TABLE contratacion.sede_negociacion_paquete DROP COLUMN tipo_modificacion_otro_si_id ;

ALTER TABLE contratacion.sede_negociacion_paquete DROP COLUMN fecha_inicio_otro_si ;

ALTER TABLE contratacion.sede_negociacion_paquete DROP COLUMN fecha_fin_otro_si ;

ALTER TABLE contratacion.sede_negociacion_paquete DROP COLUMN tipo_adicion_otro_si ;



ALTER TABLE contratacion.sede_negociacion_procedimiento DROP COLUMN valor_contrato_anterior ;

ALTER TABLE contratacion.sede_negociacion_procedimiento DROP COLUMN item_visible ;

ALTER TABLE contratacion.sede_negociacion_procedimiento DROP COLUMN tipo_modificacion_otro_si_id ;

ALTER TABLE contratacion.sede_negociacion_procedimiento DROP COLUMN fecha_inicio_otro_si ;

ALTER TABLE contratacion.sede_negociacion_procedimiento DROP COLUMN fecha_fin_otro_si ;

ALTER TABLE contratacion.sede_negociacion_procedimiento DROP COLUMN tipo_adicion_otro_si ;


ALTER TABLE contratacion.sede_negociacion_servicio DROP COLUMN item_visible ;



