CREATE OR REPLACE FUNCTION contratacion.insertar_contrato()
 RETURNS trigger
 LANGUAGE plpgsql
AS $function$
DECLARE
    es_otro_si boolean;
BEGIN
    es_otro_si := (SELECT * FROM contratacion.validacion_es_otro_si(new.negociacion_id));
    IF es_otro_si = TRUE THEN
        INSERT INTO contratacion.contrato (numero_contrato, fecha_inicio, fecha_fin, tipo_modalidad,
                                           user_id, fecha_creacion, solicitud_contratacion_id, poblacion, regional_id,
                                           tipo_subsidiado,
                                           tipo_contrato, nivel_contrato, nombre_archivo, fecha_elaboracion,
                                           nombre_original_archivo, fecha_elaboracion_otrosi
        )
        SELECT c.numero_contrato,
               c.fecha_inicio,
               c.fecha_fin,
               c.tipo_modalidad,
               new.user_id,
               now(),
               new.id,
               c.poblacion,
               c.regional_id,
               c.tipo_subsidiado,
               c.tipo_contrato,
               c.nivel_contrato,
               c.nombre_archivo,
               now() ,
               c.nombre_original_archivo,
               c.fecha_elaboracion_otrosi
        FROM contratacion.negociacion n
                 INNER JOIN contratacion.solicitud_contratacion sc ON (n.id = sc.negociacion_id)
                 INNER JOIN contratacion.contrato c ON (sc.id = c.solicitud_contratacion_id)
        WHERE n.id = (SELECT negociacion_padre_id FROM contratacion.negociacion WHERE id = new.negociacion_id)
          AND new.regimen = sc.regimen;
    END IF;
    RETURN NEW;
END;
$function$
;
