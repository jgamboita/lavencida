CREATE OR REPLACE FUNCTION contratacion.fn_actualizar_procedimientos_con_parametrizador(_paquete_actual_id bigint, _codigo_paquete character varying)
 RETURNS character varying
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    _total_count bigint;
BEGIN
    INSERT INTO temp_procedimientos (id, paquete_id, procedimiento_id, principal, etiqueta, cantidad, cantidad_maxima,
                                     cantidad_minima, costo_unitario, costo_total, observacion, ingreso_aplica,
                                     ingreso_cantidad,
                                     frecuencia_unidad, frecuencia_cantidad, tipo_carga, user_id)
    SELECT paquete.id                  AS id,
           paquete.paquete_id          AS paquete_id,
           paquete.procedimiento_id    AS procedimiento_id,
           paquete.principal           AS principal,
           paquete.etiqueta            AS etiqueta,
           paquete.cantidad            AS cantidad,
           paquete.cantidad_maxima     AS cantidad_maxima,
           paquete.cantidad_minima     AS cantidad_minima,
           paquete.costo_unitario      AS costo_unitario,
           paquete.costo_total         AS costo_total,
           paquete.observacion         AS observacion,
           paquete.ingreso_aplica      AS ingreso_aplica,
           paquete.ingreso_cantidad    AS ingreso_cantidad,
           paquete.frecuencia_unidad   AS frecuencia_unidad,
           paquete.frecuencia_cantidad AS frecuencia_cantidad,
           paquete.tipo_carga          AS tipo_carga,
           paquete.user_id             AS user_id
    FROM contratacion.v_paquete_portafolio_servicio vpps
             INNER JOIN contratacion.procedimiento_paquete paquete ON vpps.id = paquete.paquete_id
    WHERE vpps.codigo = _codigo_paquete
      AND vpps.sede_prestador_id ISNULL;
    GET DIAGNOSTICS _total_count = ROW_COUNT;
    RAISE NOTICE 'Se insertaron % procedimientos en la tabla temporal', _total_count;

    DELETE
    FROM contratacion.procedimiento_paquete pp
    WHERE pp.paquete_id = _paquete_actual_id
      AND pp.procedimiento_id NOT IN (SELECT p.procedimiento_id FROM temp_procedimientos p);
    GET DIAGNOSTICS _total_count = ROW_COUNT;
    RAISE NOTICE 'Se borraron % procedimientos', _total_count;

    UPDATE contratacion.procedimiento_paquete paquete_update
    SET principal           = pp2.principal,
        etiqueta            = pp2.etiqueta,
        cantidad            = pp2.cantidad,
        cantidad_maxima     = pp2.cantidad_maxima,
        cantidad_minima     = pp2.cantidad_minima,
        costo_unitario      = pp2.costo_unitario,
        costo_total         = pp2.costo_total,
        observacion         = pp2.observacion,
        ingreso_aplica      = pp2.ingreso_aplica,
        ingreso_cantidad    = pp2.ingreso_cantidad,
        frecuencia_unidad   = pp2.frecuencia_unidad,
        frecuencia_cantidad = pp2.frecuencia_cantidad,
        tipo_carga          = pp2.tipo_carga,
        user_id             = pp2.user_id
    FROM temp_procedimientos pp2
    WHERE paquete_update.paquete_id = _paquete_actual_id
      AND paquete_update.procedimiento_id = pp2.procedimiento_id;
    GET DIAGNOSTICS _total_count = ROW_COUNT;
    RAISE NOTICE 'Se actualizarion % procedimientos', _total_count;

    INSERT INTO contratacion.procedimiento_paquete (paquete_id, procedimiento_id, principal, etiqueta,
                                                    cantidad,
                                                    cantidad_maxima, cantidad_minima, costo_unitario,
                                                    costo_total,
                                                    observacion, ingreso_aplica, ingreso_cantidad,
                                                    frecuencia_unidad,
                                                    frecuencia_cantidad, tipo_carga, user_id)
    SELECT _paquete_actual_id AS paquete_id,
           pp3.procedimiento_id,
           pp3.principal,
           pp3.etiqueta,
           pp3.cantidad,
           pp3.cantidad_maxima,
           pp3.cantidad_minima,
           pp3.costo_unitario,
           pp3.costo_total,
           pp3.observacion,
           pp3.ingreso_aplica,
           pp3.ingreso_cantidad,
           pp3.frecuencia_unidad,
           pp3.frecuencia_cantidad,
           pp3.tipo_carga,
           pp3.user_id
    FROM temp_procedimientos pp3
             LEFT OUTER JOIN contratacion.procedimiento_paquete p2
                             ON pp3.procedimiento_id = p2.procedimiento_id AND
                                p2.paquete_id = _paquete_actual_id
    WHERE p2.id ISNULL;
    GET DIAGNOSTICS _total_count = ROW_COUNT;
    RAISE NOTICE 'Se insertaron % procedimientos', _total_count;

    TRUNCATE temp_procedimientos;
    RETURN concat('Procedimientos clonados de la negociacion', _paquete_actual_id, _codigo_paquete);
END;
$function$
;


