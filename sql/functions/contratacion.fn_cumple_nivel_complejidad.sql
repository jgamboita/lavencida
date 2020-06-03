CREATE OR REPLACE FUNCTION contratacion.fn_cumple_nivel_complejidad(in_negociacion_id bigint, in_sedes_negociacion_id bigint, in_codigo_portafolio character varying)
 RETURNS bigint
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
DECLARE
    total bigint;
BEGIN
    

    SELECT count(ps.id) into total
    FROM contratacion.servicio_salud ss
             JOIN contratacion.negociacion n ON n.id = in_negociacion_id
             JOIN contratacion.sedes_negociacion sn ON n.id = sn.negociacion_id AND sn.id = in_sedes_negociacion_id
             JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id
             INNER JOIN maestros.procedimiento p ON p.codigo_emssanar = in_codigo_portafolio
             INNER JOIN maestros.procedimiento_servicio ps ON p.id = ps.procedimiento_id AND ss.id = ps.servicio_id
             LEFT JOIN maestros.servicios_reps sr ON sr.codigo_habilitacion = sp.codigo_habilitacion AND
                                                     sr.numero_sede = cast(sp.codigo_sede AS int) AND sr.ind_habilitado AND
                                                     sr.servicio_codigo = cast(ss.codigo AS int)
             LEFT JOIN maestros.servicios_no_reps snr
                       ON snr.sede_prestador_id = sp.id AND snr.servicio_id = ss.id AND snr.estado_servicio
    WHERE 1 = CASE WHEN sr.id IS NOT NULL THEN 1 WHEN snr.id IS NOT NULL THEN 1 END
      AND least(CASE
                    WHEN n.complejidad = 'ALTA' THEN 3
                    WHEN n.complejidad = 'MEDIA' THEN 2
                    ELSE 1 END,
                CASE
                    WHEN sr.id IS NOT NULL THEN
                        CASE
                            WHEN sr.complejidad_alta = 'SI' THEN 3
                            WHEN sr.complejidad_media = 'SI' THEN 2
                            WHEN sr.complejidad_baja = 'SI' THEN 1 END
                    WHEN snr.id IS NOT NULL THEN snr.nivel_complejidad END) >= ps.complejidad;

      return total;

      EXCEPTION WHEN OTHERS THEN
        RAISE NOTICE '% %', SQLERRM, SQLSTATE;
        RETURN 0;
     

END;
$function$
;
