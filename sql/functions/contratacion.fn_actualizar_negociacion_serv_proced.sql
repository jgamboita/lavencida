CREATE OR REPLACE FUNCTION contratacion.fn_actualizar_negociacion_serv_proced(in_negociacion_id bigint, in_negociacion_referente_id bigint, in_servicio_id bigint, in_procedimiento_id bigint, in_user_id integer)
 RETURNS character varying
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
declare

v_valor_negociado numeric;
v_negociado boolean;
v_porcentaje_negociado numeric;
v_tarifario_negociado_id integer;
v_tarifa_diferencial boolean;
  
begin
	
	select snp.valor_negociado, snp.negociado, snp.porcentaje_negociado, 
		   snp.tarifario_negociado_id, snp.tarifa_diferencial
	into   v_valor_negociado, v_negociado, v_porcentaje_negociado, 
		   v_tarifario_negociado_id, v_tarifa_diferencial
	from contratacion.sedes_negociacion sn 
	inner join contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id 
	inner join contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id
	inner join contratacion.negociacion n on n.id = sn.negociacion_id 
	where n.id = in_negociacion_referente_id and snp.procedimiento_id = in_procedimiento_id;
	
UPDATE contratacion.sede_negociacion_procedimiento SET 
	valor_negociado = v_valor_negociado, 
	valor_contrato = v_valor_negociado, 
	negociado = v_negociado, 
    porcentaje_negociado = v_porcentaje_negociado, 
    porcentaje_contrato = v_porcentaje_negociado, 
    tarifario_negociado_id = v_tarifario_negociado_id, 
    tarifario_contrato_id = v_tarifario_negociado_id, 
    tarifa_diferencial = v_tarifa_diferencial, 
    user_id = in_user_id 
WHERE sede_negociacion_servicio_id IN (
		SELECT sns.id FROM contratacion.sede_negociacion_servicio sns 
		JOIN contratacion.sedes_negociacion sn on sns.sede_negociacion_id = sn.id
		join contratacion.servicio_salud ss on sns.servicio_id = ss.id 
		WHERE sn.negociacion_id = in_negociacion_id AND ss.id = in_servicio_id
	) and procedimiento_id = in_procedimiento_id;
      
        
    RETURN '1'; ---> salio todo bien.

    EXCEPTION WHEN OTHERS THEN
      RAISE NOTICE '% %', SQLERRM, SQLSTATE;
    	RETURN '2'; --> y mostrar mensajes de error
    END;
$function$
;
