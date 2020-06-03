CREATE OR REPLACE FUNCTION contratacion.fn_copiar_paquete_por_negociacion(_negociacion_actual_id bigint, _negociacion_anterior_id bigint, _codigo_portafolio character varying)
 RETURNS character varying
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    _row_conut bigint;
   _total bigint;
   _sede_negociacion_id bigint;
  r record;
begin

    INSERT INTO paquete_maximo_sede (sede_prestador_id, nombre_sede, codigo_paquete, maximo_paquete_id)
    SELECT sp.id, sp.nombre_sede, pp.codigo, max(p_pa.id) maximo_paquete_id
    FROM contratacion.sedes_negociacion sn
             INNER JOIN contratacion.sede_prestador sp ON sn.sede_prestador_id = sp.id
             INNER JOIN contratacion.portafolio p ON sp.portafolio_id = p.id
             INNER JOIN contratacion.portafolio p_pa ON p.id = p_pa.portafolio_padre_id
             INNER JOIN contratacion.paquete_portafolio pp ON p_pa.id = pp.portafolio_id
    WHERE sn.negociacion_id = _negociacion_anterior_id
      AND pp.codigo = _codigo_portafolio
    GROUP BY sp.id, sp.nombre_sede, pp.codigo;
    GET DIAGNOSTICS _row_conut = ROW_COUNT;
    RAISE NOTICE 'Se insertaron % paquetes en la tabla temporal', _row_conut;

    PERFORM contratacion.fn_actualizar_procedimientos_con_parametrizador(maximo_paquete_id, codigo_paquete)
    FROM paquete_maximo_sede;
    RAISE NOTICE 'Se actualizaron los procedimientos del prestador';

    PERFORM contratacion.fn_actualizar_medicamentos_con_parametrizador(maximo_paquete_id, codigo_paquete)
    FROM paquete_maximo_sede;
    RAISE NOTICE 'Se actualizaron los medicamentos del prestador';

    PERFORM contratacion.fn_actualizar_insumos_con_parametrizador(maximo_paquete_id, codigo_paquete)
    FROM paquete_maximo_sede;
    RAISE NOTICE 'Se actualizaron los insumos del prestador';


    FOR r in select sn.id
			    from contratacion.negociacion n
			    inner join contratacion.sedes_negociacion sn on sn.negociacion_id = n.id
			    where n.id = _negociacion_actual_id  LOOP

		select * into _total
    from contratacion.fn_cumple_nivel_complejidad(_negociacion_actual_id, r.id, _codigo_portafolio);

    if (_total > 0) then

	    INSERT INTO contratacion.sede_negociacion_paquete (sede_negociacion_id,
	                                                       paquete_id,
	                                                       valor_contrato,
	                                                       valor_propuesto,
	                                                       valor_negociado,
	                                                       negociado,
	                                                       requiere_autorizacion,
	                                                       user_id,
	                                                       requiere_autorizacion_ambulatorio,
	                                                       requiere_autorizacion_hospitalario,
	                                                       user_parametrizador_id,
	                                                       fecha_parametrizacion)
	    SELECT sn_actual.id                  AS sede_negociacion_id,
	           paque_porta.maximo_paquete_id AS paquete_id,
	           snp_anterior.valor_negociado,
	           snp_anterior.valor_propuesto,
	           snp_anterior.valor_negociado,
	           snp_anterior.negociado,
	           snp_anterior.requiere_autorizacion,
	           snp_anterior.user_id,
	           snp_anterior.requiere_autorizacion_ambulatorio,
	           snp_anterior.requiere_autorizacion_hospitalario,
	           snp_anterior.user_parametrizador_id,
	           snp_anterior.fecha_parametrizacion
	    FROM contratacion.sedes_negociacion sn_anterior
	             INNER JOIN contratacion.sede_negociacion_paquete snp_anterior
	                        ON sn_anterior.id = snp_anterior.sede_negociacion_id
	             INNER JOIN contratacion.paquete_portafolio pp_anterior
	                        ON snp_anterior.paquete_id = pp_anterior.portafolio_id
	             INNER JOIN contratacion.sedes_negociacion sn_actual ON sn_actual.negociacion_id = _negociacion_actual_id
	             INNER JOIN contratacion.sede_prestador sp_actual
	                        ON sn_actual.sede_prestador_id = sp_actual.id AND sn_anterior.sede_prestador_id = sp_actual.id
	             INNER JOIN paquete_maximo_sede paque_porta ON sp_actual.id = paque_porta.sede_prestador_id
	             LEFT JOIN maestros.servicios_reps sr ON sp_actual.codigo_habilitacion = sr.codigo_habilitacion AND
	                                                      sp_actual.codigo_sede::integer = sr.numero_sede
                 LEFT JOIN maestros.servicios_no_reps snr ON snr.codigo_habilitacion = sp_actual.codigo_habilitacion AND
                                                          snr.numero_sede = sp_actual.codigo_sede::integer
                 LEFT join contratacion.servicio_salud ss on ss.id = snr.servicio_id
	    WHERE sn_anterior.negociacion_id = _negociacion_anterior_id
	      AND pp_anterior.codigo = _codigo_portafolio
	      AND ((sr.habilitado = 'SI' and sr.ind_habilitado) or snr.estado_servicio)
	      AND exists(
	            SELECT ps.id
	            FROM maestros.procedimiento_servicio ps
	            WHERE ps.estado = 1
	              AND ((ps.reps_cups = sr.servicio_codigo) or (ps.reps_cups::varchar = ss.codigo))
	              AND ps.codigo_cliente = _codigo_portafolio
	        )
	    GROUP BY 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12;
	    GET DIAGNOSTICS _row_conut = ROW_COUNT;
	    RAISE NOTICE 'Se insertaron % paquetes en la negociacion actual', _row_conut;

	end if;

    END LOOP;

    PERFORM contratacion.fn_copiar_procedimientos_por_paquete(_negociacion_actual_id, _negociacion_anterior_id,
                                                              sede_prestador_id, codigo_paquete)
    FROM paquete_maximo_sede;
    RAISE NOTICE 'Se insertaron los procedimientos a la negociacion actual';

    PERFORM contratacion.fn_copiar_medicamentos_por_paquete(_negociacion_actual_id, _negociacion_anterior_id,
                                                            sede_prestador_id, codigo_paquete)
    FROM paquete_maximo_sede;
    RAISE NOTICE 'Se insertaron los medicamentos a la negociacion actual';

    PERFORM contratacion.fn_copiar_insumos_por_paquete(_negociacion_actual_id, _negociacion_anterior_id,
                                                       sede_prestador_id, codigo_paquete)
    FROM paquete_maximo_sede;
    RAISE NOTICE 'Se insertaron los insumos a la negociacion actual';

    TRUNCATE paquete_maximo_sede;
    RETURN concat('Procedimientos clonados de la negociacion', _negociacion_actual_id, _negociacion_anterior_id,
                  _codigo_portafolio);

END;
$function$
;
