CREATE OR REPLACE FUNCTION contratacion.fn_aplicar_tarifas_contrato_by_negociacion_servicios_proced(in_negociacion_id bigint, in_negociacion_referente_id bigint, in_list_servicio_id character varying, in_user_id integer)
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
v_procedimiento_id bigint;
registro RECORD;
procedimiento RECORD;
resultado character varying;

begin

     for registro in select cast(re as int) as id from regexp_split_to_table(in_list_servicio_id,',') as re loop
     
	     	select sns.valor_negociado, sns.negociado, sns.porcentaje_negociado, 
			   	   sns.tarifario_negociado_id, sns.tarifa_diferencial	
	          into v_valor_negociado, v_negociado, v_porcentaje_negociado, 
			   	   v_tarifario_negociado_id, v_tarifa_diferencial
		     from contratacion.sedes_negociacion sn 
		      inner join contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id 	
		      inner join contratacion.servicio_salud ss on sns.servicio_id = ss.id 
		      inner join contratacion.negociacion n on n.id = sn.negociacion_id 
		     where n.id = in_negociacion_referente_id and ss.id = registro.id;	

			UPDATE contratacion.sede_negociacion_servicio sns2 SET 
					tarifario_negociado_id = v_tarifario_negociado_id, 
					tarifario_contrato_id = v_tarifario_negociado_id, 
		            porcentaje_negociado = v_porcentaje_negociado, 
		            porcentaje_contrato = v_porcentaje_negociado, 
		            negociado = case when v_negociado is null then false else v_negociado end, 
		            user_id = in_user_id 
		            FROM contratacion.negociacion n 
		            INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id 
		            INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id 
		            where n.id = in_negociacion_id 
		            AND sns.servicio_id IN (registro.id)
		           and sns.id = sns2.id;
     
	     	for procedimiento in select snp.procedimiento_id as procId	
				       from contratacion.sedes_negociacion sn 
				      inner join contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id 
				      inner join contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id
				      inner join contratacion.negociacion n on n.id = sn.negociacion_id 
				      where n.id = in_negociacion_referente_id loop
						
				select * into resultado 
				from contratacion.fn_actualizar_negociacion_serv_proced(
						in_negociacion_id, in_negociacion_referente_id,	registro.id, procedimiento.procId, in_user_id);							
			end loop;					
     end loop;
          
        
    RETURN '1'; ---> salio todo bien.

    EXCEPTION WHEN OTHERS THEN
      RAISE NOTICE '% %', SQLERRM, SQLSTATE;
    	RETURN '2'; --> y mostrar mensajes de error
    END;
$function$
;

