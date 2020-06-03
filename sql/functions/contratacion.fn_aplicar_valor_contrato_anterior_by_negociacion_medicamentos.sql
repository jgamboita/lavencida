CREATE OR REPLACE FUNCTION contratacion.fn_aplicar_valor_contrato_anterior_by_negociacion_medicamentos(in_negociacion_id bigint, in_negociacion_referente_id bigint, in_list_medicamentos_id character varying, in_user_id integer)
 RETURNS character varying
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
declare

begin	
	
	UPDATE contratacion.sede_negociacion_medicamento SET 
	valor_negociado = contratoMedicamento.valor_negociado, 
        valor_contrato = contratoMedicamento.valor_negociado, 
	negociado = true, user_id = in_user_id
	FROM (SELECT snm.id, snm.valor_contrato, snm.valor_negociado, (
				SELECT snm3.medicamento_id from contratacion.sede_negociacion_medicamento snm3  
			          JOIN contratacion.sedes_negociacion sn3 ON snm3.sede_negociacion_id = sn3.id 
		                 where sn3.negociacion_id = in_negociacion_id and snm3.medicamento_id = snm.medicamento_id 
			         --  and sn3.sede_prestador_id = sn.sede_prestador_id 
			      order by 1 desc limit 1) as medIdActual  
            from contratacion.sede_negociacion_medicamento snm 
    		JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id 
	where sn.negociacion_id = in_negociacion_referente_id and snm.medicamento_id IN (select cast(re as int) from regexp_split_to_table(in_list_medicamentos_id,',') as re)) as contratoMedicamento 
	where contratacion.sede_negociacion_medicamento.medicamento_id = contratoMedicamento.medIdActual;
        
    RETURN '1'; ---> salio todo bien.

    EXCEPTION WHEN OTHERS THEN
      RAISE NOTICE '% %', SQLERRM, SQLSTATE;
    	RETURN '2'; --> y mostrar mensajes de error
    END;
$function$
;

