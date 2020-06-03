CREATE OR REPLACE FUNCTION contratacion.fn_actualizar_medicamentos_con_parametrizador(_paquete_actual_id bigint, _codigo_paquete character varying)
 RETURNS character varying
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    _total_count bigint;
BEGIN
    INSERT INTO temp_medicamentos (id, portafolio_id, medicamento_id, codigo_interno, valor, cantidad, etiqueta,
                                   es_capita, cantidad_maxima, cantidad_minima, costo_unitario, costo_total,
                                   observacion, ingreso_aplica, ingreso_cantidad, frecuencia_unidad,
                                   frecuencia_cantidad, en_negociacion, tipo_carga, user_id)
    SELECT medicamento.id                  AS id,
           medicamento.portafolio_id       AS portafolio_id,
           medicamento.medicamento_id      AS medicamento_id,
           medicamento.codigo_interno      AS codigo_interno,
           medicamento.valor               AS valor,
           medicamento.cantidad            AS cantidad,
           medicamento.etiqueta            AS etiqueta,
           medicamento.es_capita           AS es_capita,
           medicamento.cantidad_maxima     AS cantidad_maxima,
           medicamento.cantidad_minima     AS cantidad_minima,
           medicamento.costo_unitario      AS costo_unitario,
           medicamento.costo_total         AS costo_total,
           medicamento.observacion         AS observacion,
           medicamento.ingreso_aplica      AS ingreso_aplica,
           medicamento.ingreso_cantidad    AS ingreso_cantidad,
           medicamento.frecuencia_unidad   AS frecuencia_unidad,
           medicamento.frecuencia_cantidad AS frecuencia_cantidad,
           medicamento.en_negociacion      AS en_negociacion,
           medicamento.tipo_carga          AS tipo_carga,
           medicamento.user_id             AS user_id
    FROM contratacion.v_paquete_portafolio_servicio vpps
             INNER JOIN contratacion.medicamento_portafolio medicamento ON vpps.id = medicamento.portafolio_id
    WHERE vpps.codigo = _codigo_paquete
      AND vpps.sede_prestador_id ISNULL;
    GET DIAGNOSTICS _total_count = ROW_COUNT;
    RAISE NOTICE 'Se insertaron % medicamentos en la tabla temporal', _total_count;

    DELETE
    FROM contratacion.medicamento_portafolio mp
    WHERE mp.portafolio_id = _paquete_actual_id
      AND mp.medicamento_id NOT IN (SELECT p.medicamento_id FROM temp_medicamentos p);
    GET DIAGNOSTICS _total_count = ROW_COUNT;
    RAISE NOTICE 'Se borraron % medicamentos', _total_count;

    UPDATE contratacion.medicamento_portafolio mx_upd
    SET codigo_interno      = pp2.codigo_interno,
        valor               = pp2.valor,
        cantidad            = pp2.cantidad,
        etiqueta            = pp2.etiqueta,
        es_capita           = pp2.es_capita,
        cantidad_maxima     = pp2.cantidad_maxima,
        cantidad_minima     = pp2.cantidad_minima,
        costo_unitario      = pp2.costo_unitario,
        costo_total         = pp2.costo_total,
        observacion         = pp2.observacion,
        ingreso_aplica      = pp2.ingreso_aplica,
        ingreso_cantidad    = pp2.ingreso_cantidad,
        frecuencia_unidad   = pp2.frecuencia_unidad,
        frecuencia_cantidad = pp2.frecuencia_cantidad,
        en_negociacion      = pp2.en_negociacion,
        tipo_carga          = pp2.tipo_carga,
        user_id             = pp2.user_id
    FROM temp_medicamentos pp2
    WHERE mx_upd.portafolio_id = _paquete_actual_id
      AND mx_upd.medicamento_id = pp2.medicamento_id;
    GET DIAGNOSTICS _total_count = ROW_COUNT;
    RAISE NOTICE 'Se actualizarion % medicamentos', _total_count;

    INSERT INTO contratacion.medicamento_portafolio (portafolio_id, medicamento_id, codigo_interno, valor, cantidad,
                                                     etiqueta, es_capita, cantidad_maxima, cantidad_minima,
                                                     costo_unitario, costo_total, observacion, ingreso_aplica,
                                                     ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad,
                                                     en_negociacion, tipo_carga, user_id)
    SELECT _paquete_actual_id AS portafolio_id,
           pp3.medicamento_id,
           pp3.codigo_interno,
           pp3.valor,
           pp3.cantidad,
           pp3.etiqueta,
           pp3.es_capita,
           pp3.cantidad_maxima,
           pp3.cantidad_minima,
           pp3.costo_unitario,
           pp3.costo_total,
           pp3.observacion,
           pp3.ingreso_aplica,
           pp3.ingreso_cantidad,
           pp3.frecuencia_unidad,
           pp3.frecuencia_cantidad,
           pp3.en_negociacion,
           pp3.tipo_carga,
           pp3.user_id
    FROM temp_medicamentos pp3
             LEFT OUTER JOIN contratacion.medicamento_portafolio p2
                             ON pp3.medicamento_id = p2.medicamento_id AND
                                p2.portafolio_id = _paquete_actual_id
    WHERE p2.id ISNULL;
    GET DIAGNOSTICS _total_count = ROW_COUNT;
    RAISE NOTICE 'Se insertaron % medicamentos', _total_count;

    TRUNCATE temp_medicamentos;
    RETURN concat('Procedimientos clonados de la negociacion', _paquete_actual_id, _codigo_paquete);
END;
$function$
;
