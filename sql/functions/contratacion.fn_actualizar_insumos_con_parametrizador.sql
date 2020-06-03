CREATE OR REPLACE FUNCTION contratacion.fn_actualizar_insumos_con_parametrizador(_paquete_actual_id bigint, _codigo_paquete character varying)
 RETURNS character varying
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    _total_count bigint;
BEGIN
    INSERT INTO temp_insumos (id, insumo_id, portafolio_id, codigo_interno, valor, cantidad, etiqueta, observacion,
                              cantidad_maxima, cantidad_minima, costo_unitario, costo_total, ingreso_aplica,
                              ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad, tipo_carga, user_id)
    SELECT insumo.id                  AS id,
           insumo.insumo_id           AS insumo_id,
           insumo.portafolio_id       AS portafolio_id,
           insumo.codigo_interno      AS codigo_interno,
           insumo.valor               AS valor,
           insumo.cantidad            AS cantidad,
           insumo.etiqueta            AS etiqueta,
           insumo.observacion         AS observacion,
           insumo.cantidad_maxima     AS cantidad_maxima,
           insumo.cantidad_minima     AS cantidad_minima,
           insumo.costo_unitario      AS costo_unitario,
           insumo.costo_total         AS costo_total,
           insumo.ingreso_aplica      AS ingreso_aplica,
           insumo.ingreso_cantidad    AS ingreso_cantidad,
           insumo.frecuencia_unidad   AS frecuencia_unidad,
           insumo.frecuencia_cantidad AS frecuencia_cantidad,
           insumo.tipo_carga          AS tipo_carga,
           insumo.user_id             AS user_id
    FROM contratacion.v_paquete_portafolio_servicio vpps
             INNER JOIN contratacion.insumo_portafolio insumo ON vpps.id = insumo.portafolio_id
    WHERE vpps.codigo = _codigo_paquete
      AND vpps.sede_prestador_id ISNULL;
    GET DIAGNOSTICS _total_count = ROW_COUNT;
    RAISE NOTICE 'Se insertaron % insumos en la tabla temporal', _total_count;

    DELETE
    FROM contratacion.insumo_portafolio pp
    WHERE pp.portafolio_id = _paquete_actual_id
      AND pp.insumo_id NOT IN (SELECT p.insumo_id FROM temp_insumos p);
    GET DIAGNOSTICS _total_count = ROW_COUNT;
    RAISE NOTICE 'Se borraron % insumos', _total_count;

    UPDATE contratacion.insumo_portafolio paquete_update
    SET codigo_interno      = pp2.codigo_interno,
        valor               = pp2.valor,
        cantidad            = pp2.cantidad,
        etiqueta            = pp2.etiqueta,
        observacion         = pp2.observacion,
        cantidad_maxima     = pp2.cantidad_maxima,
        cantidad_minima     = pp2.cantidad_minima,
        costo_unitario      = pp2.costo_unitario,
        costo_total         = pp2.costo_total,
        ingreso_aplica      = pp2.ingreso_aplica,
        ingreso_cantidad    = pp2.ingreso_cantidad,
        frecuencia_unidad   = pp2.frecuencia_unidad,
        frecuencia_cantidad = pp2.frecuencia_cantidad,
        tipo_carga          = pp2.tipo_carga,
        user_id             = pp2.user_id
    FROM temp_insumos pp2
    WHERE paquete_update.portafolio_id = _paquete_actual_id
      AND paquete_update.insumo_id = pp2.insumo_id;
    GET DIAGNOSTICS _total_count = ROW_COUNT;
    RAISE NOTICE 'Se actualizarion % insumos', _total_count;

    INSERT INTO contratacion.insumo_portafolio (insumo_id, portafolio_id, codigo_interno, valor, cantidad, etiqueta,
                                                observacion, cantidad_maxima, cantidad_minima, costo_unitario,
                                                costo_total, ingreso_aplica, ingreso_cantidad, frecuencia_unidad,
                                                frecuencia_cantidad, tipo_carga, user_id)
    SELECT pp3.insumo_id,
           _paquete_actual_id AS paquete_id,
           pp3.codigo_interno,
           pp3.valor,
           pp3.cantidad,
           pp3.etiqueta,
           pp3.observacion,
           pp3.cantidad_maxima,
           pp3.cantidad_minima,
           pp3.costo_unitario,
           pp3.costo_total,
           pp3.ingreso_aplica,
           pp3.ingreso_cantidad,
           pp3.frecuencia_unidad,
           pp3.frecuencia_cantidad,
           pp3.tipo_carga,
           pp3.user_id
    FROM temp_insumos pp3
             LEFT OUTER JOIN contratacion.insumo_portafolio p2
                             ON pp3.insumo_id = p2.insumo_id AND
                                p2.portafolio_id = _paquete_actual_id
    WHERE p2.id ISNULL;
    GET DIAGNOSTICS _total_count = ROW_COUNT;
    RAISE NOTICE 'Se insertaron % insumos', _total_count;

    TRUNCATE temp_insumos;
    RETURN concat('Procedimientos clonados de la negociacion', _paquete_actual_id, _codigo_paquete);
END;
$function$
;
