CREATE OR REPLACE FUNCTION contratacion.fn_copiar_medicamentos_por_negociacion(_negociacion_actual_id bigint, _negociacion_anterior_id bigint)
 RETURNS character varying
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    _row_count text;
BEGIN
    WITH sede_medicamentos_anterior AS (
        SELECT snm.medicamento_id,
               snm.valor_contrato,               
               snm.valor_negociado,
               snm.negociado,
               snm.requiere_autorizacion,
               snm.frecuencia_referente,
               snm.costo_medio_usuario_referente,
               snm.costo_medio_usuario,
               snm.negociacion_ria_rango_poblacion_id,
               snm.actividad_id,
               snm.porcentaje_negociado,
               snm.peso_porcentual_referente,
               snm.valor_referente,
               snm.user_id,
               snm.requiere_autorizacion_ambulatorio,
               snm.requiere_autorizacion_hospitalario,
               snm.user_parametrizador_id,
               snm.fecha_parametrizacion,
               snm.sede_neg_grupo_t_id,
               snm.pgp,
               snm.frecuencia,
               snm.franja_inicio,
               snm.franja_fin,
               snm.valor_importado,
               snm.valor_contrato_anterior
        FROM contratacion.sedes_negociacion sn
                 INNER JOIN contratacion.sede_prestador sp ON sn.sede_prestador_id = sp.id
                 LEFT JOIN maestros.servicios_reps sr ON sr.codigo_habilitacion = sp.codigo_habilitacion AND
                                                          sr.numero_sede = sp.codigo_sede::integer AND
                                                          sr.servicio_codigo::varchar = '714'
                 LEFT JOIN maestros.servicios_no_reps snr ON snr.codigo_habilitacion = sp.codigo_habilitacion AND
                                                          snr.numero_sede = sp.codigo_sede::integer                                                           
                 LEFT join contratacion.servicio_salud ss on ss.id = snr.servicio_id  and 
                                                          ss.codigo::varchar = '714'
                 INNER JOIN contratacion.sede_negociacion_medicamento snm ON sn.id = snm.sede_negociacion_id
        WHERE sn.negociacion_id = _negociacion_anterior_id
          AND sp.estado = 4          
          AND ((sr.ind_habilitado and sr.habilitado = 'SI') or snr.estado_servicio)
        GROUP BY snm.medicamento_id, snm.valor_contrato, snm.valor_negociado, snm.negociado,
                 snm.requiere_autorizacion, snm.frecuencia_referente, snm.costo_medio_usuario_referente,
                 snm.costo_medio_usuario, snm.negociacion_ria_rango_poblacion_id, snm.actividad_id,
                 snm.porcentaje_negociado, snm.peso_porcentual_referente, snm.valor_referente, snm.user_id,
                 snm.requiere_autorizacion_ambulatorio, snm.requiere_autorizacion_hospitalario,
                 snm.user_parametrizador_id, snm.fecha_parametrizacion, snm.sede_neg_grupo_t_id, snm.pgp,
                 snm.frecuencia, snm.franja_inicio, snm.franja_fin, snm.valor_importado, snm.valor_contrato_anterior
    ),
         insertar_medicamentos AS (
             INSERT INTO contratacion.sede_negociacion_medicamento (sede_negociacion_id, medicamento_id, valor_contrato,
                                                                    valor_negociado, negociado,
                                                                    requiere_autorizacion, frecuencia_referente,
                                                                    costo_medio_usuario_referente, costo_medio_usuario,
                                                                    negociacion_ria_rango_poblacion_id, actividad_id,
                                                                    porcentaje_negociado, peso_porcentual_referente,
                                                                    valor_referente, user_id,
                                                                    requiere_autorizacion_ambulatorio,
                                                                    requiere_autorizacion_hospitalario,
                                                                    user_parametrizador_id, fecha_parametrizacion,
                                                                    sede_neg_grupo_t_id, pgp, frecuencia, franja_inicio,
                                                                    franja_fin, valor_importado)
                 SELECT sn_actual.id AS sede_negociacion_id,
                        sma.medicamento_id,
                        sma.valor_negociado,                        
                        sma.valor_negociado,
                        sma.negociado,
                        sma.requiere_autorizacion,
                        sma.frecuencia_referente,
                        sma.costo_medio_usuario_referente,
                        sma.costo_medio_usuario,
                        sma.negociacion_ria_rango_poblacion_id,
                        sma.actividad_id,
                        sma.porcentaje_negociado,
                        sma.peso_porcentual_referente,
                        sma.valor_referente,
                        sma.user_id,
                        sma.requiere_autorizacion_ambulatorio,
                        sma.requiere_autorizacion_hospitalario,
                        sma.user_parametrizador_id,
                        sma.fecha_parametrizacion,
                        sma.sede_neg_grupo_t_id,
                        sma.pgp,
                        sma.frecuencia,
                        sma.franja_inicio,
                        sma.franja_fin,
                        sma.valor_importado
                 FROM sede_medicamentos_anterior sma
                          INNER JOIN maestros.medicamento m ON sma.medicamento_id = m.id
                          INNER JOIN contratacion.sedes_negociacion sn_actual
                                     ON sn_actual.negociacion_id = _negociacion_actual_id
                 WHERE m.estado_medicamento_id = 1
                 RETURNING id
         )
    SELECT count(*)
    INTO _row_count
    FROM insertar_medicamentos;
    RAISE NOTICE 'Se insertaron % medicamentos', _row_count;
    RETURN concat('Procedimientos clonados de la negociacion ', _negociacion_actual_id, _negociacion_anterior_id);
END;
$function$
;

