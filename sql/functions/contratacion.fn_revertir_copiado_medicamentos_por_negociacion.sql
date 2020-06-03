CREATE OR REPLACE FUNCTION contratacion.fn_revertir_copiado_medicamentos_por_negociacion(_negociacion_actual_id bigint, _negociacion_anterior_id bigint)
 RETURNS character varying
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    DELETE
    FROM contratacion.sede_negociacion_medicamento snm
        USING contratacion.sedes_negociacion sn
    WHERE sn.negociacion_id = _negociacion_actual_id
      AND snm.sede_negociacion_id = sn.id;
    RETURN concat('Medicamentos clonados de la negociacion', _negociacion_actual_id, _negociacion_anterior_id);
END;
$function$
;
