alter table contratacion.contrato drop column regimen;

alter table contratacion.solicitud_contratacion add column regimen character varying(50);


ALTER TABLE contratacion.solicitud_contratacion
  DROP CONSTRAINT uk_solicitud_contratacion_negociacion;
ALTER TABLE contratacion.solicitud_contratacion
  ADD CONSTRAINT uk_solicitud_contratacion_negociacion UNIQUE (negociacion_id, regimen);


  
CREATE OR REPLACE FUNCTION contratacion.insertar_solicitud_contratacion()
  RETURNS trigger AS
$BODY$ 
  DECLARE
  v_nuevo_contrato boolean;
  v_cont INTEGER;
  BEGIN 
	IF NEW.estado_negociacion='FINALIZADA' then
	IF NEW.nuevo_contrato is NULL THEN 
		v_nuevo_contrato:=TRUE;
	end if;
	IF NEW.regimen<>'AMBOS' THEN 
		SELECT COUNT(*) INTO v_cont from contratacion.solicitud_contratacion where negociacion_id = NEW.id and regimen=NEW.regimen;
		IF v_cont = 0 THEN
			INSERT INTO contratacion.solicitud_contratacion(fecha_creacion_solicitud, prestador_id, tipo_modalidad_negociacion,
			nuevo_contrato, user_id, estado_parametrizacion_id, negociacion_id, estado_legalizacion_id, regimen)
			values(current_timestamp, NEW.prestador_id, NEW.tipo_modalidad_negociacion, v_nuevo_contrato, NEW.user_id, 'PEND_PARAMETRIZACION', NEW.id, 'PEND_LEGALIZACION',NEW.regimen);
		END IF;
	ELSE
		SELECT COUNT(*) INTO v_cont from contratacion.solicitud_contratacion where negociacion_id = NEW.id and regimen='CONTRIBUTIVO';
		IF v_cont = 0 THEN
			INSERT INTO contratacion.solicitud_contratacion(fecha_creacion_solicitud, prestador_id, tipo_modalidad_negociacion,
			nuevo_contrato, user_id, estado_parametrizacion_id, negociacion_id, estado_legalizacion_id, regimen)
			values(current_timestamp, NEW.prestador_id, NEW.tipo_modalidad_negociacion, v_nuevo_contrato, NEW.user_id, 'PEND_PARAMETRIZACION', NEW.id, 'PEND_LEGALIZACION','CONTRIBUTIVO');
		end if;
		SELECT COUNT(*) INTO v_cont from contratacion.solicitud_contratacion where negociacion_id = NEW.id and regimen='SUBSIDIADO';
		IF v_cont = 0 THEN
			INSERT INTO contratacion.solicitud_contratacion(fecha_creacion_solicitud, prestador_id, tipo_modalidad_negociacion,
			nuevo_contrato, user_id, estado_parametrizacion_id, negociacion_id, estado_legalizacion_id, regimen)
			values(current_timestamp, NEW.prestador_id, NEW.tipo_modalidad_negociacion, v_nuevo_contrato, NEW.user_id, 'PEND_PARAMETRIZACION', NEW.id, 'PEND_LEGALIZACION','SUBSIDIADO');
		end if;
		RETURN NEW;
	END IF;
	END IF;
	RETURN NEW;
  END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION contratacion.insertar_solicitud_contratacion()
  OWNER TO postgres;

  
CREATE TRIGGER insertar_solicitud_contratacion
  AFTER INSERT OR UPDATE
  ON contratacion.negociacion
  FOR EACH ROW
  EXECUTE PROCEDURE contratacion.insertar_solicitud_contratacion();

  alter table contratacion.legalizacion_contrato add column contenido text;

