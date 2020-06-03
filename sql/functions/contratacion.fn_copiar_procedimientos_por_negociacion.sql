CREATE OR REPLACE FUNCTION contratacion.fn_copiar_procedimientos_por_negociacion(_negociacion_actual_id bigint, _negociacion_anterior_id bigint)
 RETURNS character varying
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    _row_count text;
BEGIN

    CREATE TEMPORARY TABLE servicio_minimo_negociacion
    (
        servicio_id            integer NOT NULL,
        servicio_codigo        character varying(20),
        complejidad_reps       integer,
        tarifa_diferencial     boolean,
        tarifario_contrato_id  integer,
        tarifario_negociado_id integer,
        porcentaje_contrato    numeric,
        porcentaje_negociado   numeric,
        valor_contrato         numeric,
        valor_negociado        numeric,
        negociado              boolean,
        es_transporte          boolean,
        poblacion              integer
    ) ON COMMIT DROP;

    CREATE TEMPORARY TABLE procedimiento_minimo_negociacion
    (
        procedimiento_id                   integer NOT NULL,
        tarifa_diferencial                 boolean,
        tarifario_contrato_id              integer,
        tarifario_negociado_id             integer,
        porcentaje_contrato                numeric,
        porcentaje_negociado               numeric,
        valor_contrato                     numeric,
        valor_negociado                    numeric,
        negociado                          boolean,
        requiere_autorizacion              boolean,
        requiere_autorizacion_ambulatorio  character varying(20),
        requiere_autorizacion_hospitalario character varying(20),
        complejidad                        integer
    ) ON COMMIT DROP;

    CREATE TEMPORARY TABLE servicios_habilitados
    (
        sede_negociacion_id integer,
        servicio_id         integer,
        complejidad         integer,
        estado_servicio     boolean,
        codigo_habilitacion varchar(50),
        codigo_sede         varchar(60),
        complejidad_minimo  integer
    ) ON COMMIT DROP;

    INSERT INTO servicio_minimo_negociacion (servicio_id, servicio_codigo, complejidad_reps, tarifa_diferencial,
                                             tarifario_contrato_id, tarifario_negociado_id, porcentaje_contrato,
                                             porcentaje_negociado, valor_contrato, valor_negociado, negociado,
                                             es_transporte, poblacion)
    SELECT ss.id     AS id,
           ss.codigo AS codigo,
           CASE
               WHEN sr.complejidad_baja = 'SI' THEN 1
               WHEN sr.complejidad_media = 'SI' THEN 2
               WHEN sr.complejidad_alta = 'SI' THEN 3
               END   AS complejidad_reps,
           sns.tarifa_diferencial,
           min(sns.tarifario_contrato_id),
           --sns.tarifario_propuesto_id,--No esta en uso
           min(sns.tarifario_negociado_id),
           min(sns.porcentaje_contrato),
           --sns.porcentaje_propuesto,--No esta en uso
           min(sns.porcentaje_negociado),
           min(sns.valor_contrato),
           min(sns.valor_negociado),
           --sns.valor_upc_contrato,--capita
           sns.negociado,
           sns.es_transporte,
           sns.poblacion
           --sns.frecuencia_referente,--PGP
           --sns.costo_medio_usuario_referente,--PGP
           --sns.costo_medio_usuario,--PGP
    FROM contratacion.sedes_negociacion sn
             INNER JOIN contratacion.sede_prestador sp ON sn.sede_prestador_id = sp.id
             INNER JOIN contratacion.sede_negociacion_servicio sns ON sn.id = sns.sede_negociacion_id
             INNER JOIN contratacion.servicio_salud ss ON sns.servicio_id = ss.id
             LEFT JOIN maestros.servicios_reps sr ON sr.codigo_habilitacion = sp.codigo_habilitacion AND
                                                      sr.numero_sede = sp.codigo_sede::integer AND
                                                      ss.codigo = sr.servicio_codigo::varchar
			 LEFT JOIN maestros.servicios_no_reps snr ON snr.codigo_habilitacion = sp.codigo_habilitacion and 			  											 
			  											 snr.numero_sede = sp.codigo_sede::integer and 
														 snr.servicio_id = ss.id                                                           
    WHERE sn.negociacion_id = _negociacion_anterior_id
      AND sp.estado = 4
      AND ((sr.ind_habilitado and sr.habilitado = 'SI') or snr.estado_servicio)
    GROUP BY ss.id, ss.codigo, sr.complejidad_baja, sr.complejidad_media, sr.complejidad_alta,
             sns.tarifa_diferencial, sns.negociado, sns.es_transporte, sns.poblacion;
    GET DIAGNOSTICS _row_count = ROW_COUNT;
    RAISE NOTICE 'Se insertaron % servicios mínimos', _row_count;

    INSERT INTO procedimiento_minimo_negociacion (procedimiento_id, tarifa_diferencial, tarifario_contrato_id,
                                                  tarifario_negociado_id, porcentaje_contrato, porcentaje_negociado,
                                                  valor_contrato, valor_negociado, negociado, requiere_autorizacion,
                                                  requiere_autorizacion_ambulatorio, requiere_autorizacion_hospitalario,
                                                  complejidad)
    SELECT --snp.sede_negociacion_servicio_id,
           snp.procedimiento_id,
           snp.tarifa_diferencial,
           min(snp.tarifario_contrato_id),
           --snp.tarifario_propuesto_id,--No esta en uso
           min(snp.tarifario_negociado_id),
           min(snp.porcentaje_contrato),
           --snp.porcentaje_propuesto,--No esta en uso
           min(snp.porcentaje_negociado),
           min(snp.valor_contrato),
           --snp.valor_propuesto,--no esta en uso
           min(snp.valor_negociado),
           snp.negociado,
           snp.requiere_autorizacion,
           --snp.frecuencia_referente,--PGP
           --snp.costo_medio_usuario_referente,--PGP
           --snp.costo_medio_usuario,--PGP
           --snp.peso_porcentual_referente,--RIAS_CAPITA, RIAS_CAPITA_GRUPO_ETAREO
           --snp.valor_referente,--RIAS_CAPITA
           --snp.negociacion_ria_rango_poblacion_id,--RIAS_CAPITA, RIAS_CAPITA_GRUPO_ETAREO
           --snp.actividad_id,--RIAS_CAPITA, PAGO_GLOBAL_PROSPECTIVO, RIAS_CAPITA_GRUPO_ETAREO
           --snp.user_id,
           snp.requiere_autorizacion_ambulatorio,
           snp.requiere_autorizacion_hospitalario,
           --snp.user_parametrizador_id,
           --snp.fecha_parametrizacion,
           --snp.sede_negociacion_capitulo_id,--?
           --snp.pto_id,--?
           --snp.frecuencia,--?
           --snp.franja_inicio,--?
           --snp.franja_fin,--?
           --snp.valor_contrato_anterior
           ps.complejidad
    FROM contratacion.sedes_negociacion sn
             INNER JOIN contratacion.sede_negociacion_servicio sns ON sn.id = sns.sede_negociacion_id
             INNER JOIN contratacion.sede_negociacion_procedimiento snp ON sns.id = snp.sede_negociacion_servicio_id
             INNER JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id AND estado = 1
             INNER JOIN maestros.procedimiento p ON ps.procedimiento_id = p.id AND p.estado_procedimiento_id = 1
    WHERE sn.negociacion_id = _negociacion_anterior_id
    GROUP BY snp.procedimiento_id, snp.tarifa_diferencial, snp.negociado, snp.requiere_autorizacion,
             snp.requiere_autorizacion_ambulatorio, snp.requiere_autorizacion_hospitalario, ps.complejidad;
    GET DIAGNOSTICS _row_count = ROW_COUNT;
    RAISE NOTICE 'Se insertaron % procedimientos mínimos', _row_count;

    INSERT INTO servicios_habilitados (sede_negociacion_id, servicio_id, complejidad, estado_servicio,
                                       codigo_habilitacion, codigo_sede, complejidad_minimo)
    SELECT sn.id                                                                             AS sede_negociacion_id,
           ss.id                                                                             AS servicio_id,
           CASE
               WHEN sr.id IS NOT NULL THEN CASE
                                               WHEN sr.complejidad_alta = 'SI' THEN 3
                                               WHEN sr.complejidad_media = 'SI' THEN 2
                                               WHEN sr.complejidad_baja = 'SI' THEN 1 END
               WHEN snr.id IS NOT NULL THEN snr.nivel_complejidad END                        AS complejidad,
           CASE WHEN sr.id IS NOT NULL THEN sr.ind_habilitado ELSE snr.estado_servicio END   AS estado_servicio,
           sp.codigo_habilitacion                                                            AS codigo_habilitacion,
           sp.codigo_sede                                                                    AS codigo_sede,
           least(CASE
                     WHEN n.complejidad = 'ALTA' THEN 3
                     WHEN n.complejidad = 'MEDIA' THEN 2
                     ELSE 1 END, CASE
                                     WHEN sr.id IS NOT NULL THEN
                                         CASE
                                             WHEN sr.complejidad_alta = 'SI' THEN 3
                                             WHEN sr.complejidad_media = 'SI' THEN 2
                                             WHEN sr.complejidad_baja = 'SI' THEN 1 END
                                     WHEN snr.id IS NOT NULL THEN snr.nivel_complejidad END) AS complejidad_minimo
    FROM contratacion.servicio_salud ss
             JOIN contratacion.negociacion n ON n.id = _negociacion_actual_id
             JOIN contratacion.sedes_negociacion sn ON n.id = sn.negociacion_id
             JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id
             LEFT JOIN maestros.servicios_reps sr ON sr.codigo_habilitacion = sp.codigo_habilitacion AND
                                                     sr.numero_sede = cast(sp.codigo_sede AS int) AND
                                                     sr.ind_habilitado AND
                                                     sr.servicio_codigo = cast(ss.codigo AS int)
             LEFT JOIN maestros.servicios_no_reps snr
                       ON snr.sede_prestador_id = sp.id AND snr.servicio_id = ss.id AND snr.estado_servicio
    WHERE 1 = CASE WHEN sr.id IS NOT NULL THEN 1 WHEN snr.id IS NOT NULL THEN 1 END;
    GET DIAGNOSTICS _row_count = ROW_COUNT;
    RAISE NOTICE 'Se insertaron % servicios habilitados', _row_count;

    INSERT INTO contratacion.sede_negociacion_servicio (sede_negociacion_id, servicio_id, tarifa_diferencial,
                                                        tarifario_contrato_id, tarifario_negociado_id,
                                                        porcentaje_contrato,
                                                        porcentaje_negociado, negociado, valor_contrato, es_transporte,
                                                        valor_negociado, poblacion)
    SELECT sn_actual.id                AS sede_negociacion_id,
           shna.servicio_id            AS servicio_id,
           shna.tarifa_diferencial     AS tarifa_diferencial,
           shna.tarifario_negociado_id AS tarifario_contrato_id,
           shna.tarifario_negociado_id AS tarifario_negociado_id,
           shna.porcentaje_negociado   AS porcentaje_contrato,
           shna.porcentaje_negociado   AS porcentaje_negociado,
           shna.negociado              AS negociado,
           shna.valor_negociado        AS valor_contrato,
           shna.es_transporte          AS es_transporte,
           shna.valor_negociado        AS valor_negociado,
           shna.poblacion              AS poblacion
    FROM contratacion.negociacion n_actual
             INNER JOIN contratacion.sedes_negociacion sn_actual ON n_actual.id = sn_actual.negociacion_id
             INNER JOIN servicios_habilitados h ON h.sede_negociacion_id = sn_actual.id
             INNER JOIN servicio_minimo_negociacion shna ON shna.servicio_id = h.servicio_id
    WHERE n_actual.id = _negociacion_actual_id;
    GET DIAGNOSTICS _row_count = ROW_COUNT;
    RAISE NOTICE 'Se insertaron % servicios en la nueva negociación', _row_count;

    INSERT INTO contratacion.sede_negociacion_procedimiento (sede_negociacion_servicio_id,
                                                             procedimiento_id,
                                                             tarifa_diferencial,
                                                             negociado,
                                                             requiere_autorizacion,
                                                             requiere_autorizacion_ambulatorio,
                                                             requiere_autorizacion_hospitalario)
    SELECT sns.id,
           ps.id,
           FALSE,
           FALSE,
           FALSE,
           p.parametrizacion_ambulatoria,
           p.parametrizacion_hospitalaria
    FROM contratacion.sedes_negociacion sn
             INNER JOIN contratacion.sede_negociacion_servicio sns ON sn.id = sns.sede_negociacion_id
             INNER JOIN maestros.procedimiento_servicio ps ON sns.servicio_id = ps.servicio_id AND ps.estado = 1
             INNER JOIN maestros.procedimiento p ON ps.procedimiento_id = p.id AND p.estado_procedimiento_id = 1
             			and p.tipo_procedimiento_id <> 2
             INNER JOIN servicio_minimo_negociacion smn ON smn.servicio_id = sns.servicio_id
             INNER JOIN procedimiento_minimo_negociacion tx ON ps.id = tx.procedimiento_id
             INNER JOIN servicios_habilitados sh ON sn.id = sh.sede_negociacion_id AND sns.servicio_id = sh.servicio_id
    WHERE sn.negociacion_id = _negociacion_actual_id
      AND sh.complejidad_minimo >= ps.complejidad;
    GET DIAGNOSTICS _row_count = ROW_COUNT;
    RAISE NOTICE 'Se insertaron % procedimientos en la nueva negociación', _row_count;

    UPDATE contratacion.sede_negociacion_procedimiento snp
    SET tarifa_diferencial                 = pmn.tarifa_diferencial,
        tarifario_contrato_id              = pmn.tarifario_negociado_id,
        tarifario_negociado_id             = pmn.tarifario_negociado_id,
        porcentaje_contrato                = pmn.porcentaje_negociado,
        porcentaje_negociado               = pmn.porcentaje_negociado,
        valor_contrato                     = pmn.valor_negociado,
        valor_negociado                    = pmn.valor_negociado,
        negociado                          = TRUE,
        requiere_autorizacion              = pmn.requiere_autorizacion,
        requiere_autorizacion_ambulatorio  = pmn.requiere_autorizacion_ambulatorio,
        requiere_autorizacion_hospitalario = pmn.requiere_autorizacion_hospitalario
    FROM contratacion.sedes_negociacion sn
             INNER JOIN contratacion.sede_negociacion_servicio sns ON sn.id = sns.sede_negociacion_id
             INNER JOIN contratacion.sede_negociacion_procedimiento snp_udp
                        ON sns.id = snp_udp.sede_negociacion_servicio_id
             INNER JOIN procedimiento_minimo_negociacion pmn ON snp_udp.procedimiento_id = pmn.procedimiento_id
    WHERE sn.negociacion_id = _negociacion_actual_id
      AND snp.id = snp_udp.id;
    GET DIAGNOSTICS _row_count = ROW_COUNT;
    RAISE NOTICE 'Se actulizaron % procedimientos en la nueva negociación', _row_count;

    SELECT count(procedimiento_id)
    INTO _row_count
    FROM procedimiento_minimo_negociacion;
    RAISE NOTICE 'Se insertaron % procedimientos', _row_count;
    RETURN concat('Procedimientos clonados de la negociacion', _negociacion_actual_id, _negociacion_anterior_id);
END;
$function$
;
