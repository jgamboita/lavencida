CREATE OR REPLACE FUNCTION contratacion.fn_copiar_medicamentos_por_paquete(_negociacion_actual_id bigint, _negociacion_anterior_id bigint, _sede_prestador_id integer, _codigo_portafolio character varying)
 RETURNS character varying
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    _total_count bigint;
BEGIN
    INSERT INTO contratacion.sede_negociacion_paquete_medicamento (medicamento_id, sede_negociacion_paquete_id,
                                                                   cantidad, cantidad_minima, cantidad_maxima,
                                                                   costo_unitario, costo_total, observacion,
                                                                   ingreso_aplica, ingreso_cantidad, frecuencia_unidad,
                                                                   frecuencia_cantidad)
    SELECT snpp_anterior.medicamento_id,
           snp_actual.id AS sede_negociacion_paquete_id,
           snpp_anterior.cantidad,
           snpp_anterior.cantidad_minima,
           snpp_anterior.cantidad_maxima,
           snpp_anterior.costo_unitario,
           snpp_anterior.costo_total,
           snpp_anterior.observacion,
           snpp_anterior.ingreso_aplica,
           snpp_anterior.ingreso_cantidad,
           snpp_anterior.frecuencia_unidad,
           snpp_anterior.frecuencia_cantidad
    FROM contratacion.sedes_negociacion sn_anterior
             INNER JOIN contratacion.sede_prestador sp_anterior ON sn_anterior.sede_prestador_id = sp_anterior.id
             INNER JOIN contratacion.sede_negociacion_paquete snp_anterior
                        ON sn_anterior.id = snp_anterior.sede_negociacion_id
             INNER JOIN contratacion.portafolio p_anterior ON snp_anterior.paquete_id = p_anterior.id
             INNER JOIN contratacion.paquete_portafolio pp_anterior ON p_anterior.id = pp_anterior.portafolio_id
             INNER JOIN contratacion.sede_negociacion_paquete_medicamento snpp_anterior
                        ON snp_anterior.id = snpp_anterior.sede_negociacion_paquete_id,
         contratacion.sedes_negociacion sn_actual
             INNER JOIN contratacion.sede_prestador sp_actual
                        ON sn_actual.sede_prestador_id = sp_actual.id AND sp_actual.id = _sede_prestador_id
             INNER JOIN contratacion.sede_negociacion_paquete snp_actual
                        ON sn_actual.id = snp_actual.sede_negociacion_id
             INNER JOIN contratacion.portafolio p_actual ON snp_actual.paquete_id = p_actual.id
             INNER JOIN contratacion.paquete_portafolio pp_actual ON p_actual.id = pp_actual.portafolio_id
    WHERE sn_anterior.negociacion_id = _negociacion_anterior_id
      AND sn_actual.negociacion_id = _negociacion_actual_id
      AND sp_anterior.id = sp_actual.id
      AND pp_anterior.codigo = _codigo_portafolio
      AND pp_actual.codigo = _codigo_portafolio;
    GET DIAGNOSTICS _total_count = ROW_COUNT;
    RAISE NOTICE 'Se insertaron % medicamentos en la negociacion', _total_count;
    RETURN concat('Medicamentos clonados de la negociacion', _negociacion_actual_id, _negociacion_anterior_id);
END;
$function$
;
