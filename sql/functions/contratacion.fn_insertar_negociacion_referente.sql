CREATE OR REPLACE FUNCTION contratacion.fn_insertar_negociacion_referente(in_negociacion_id bigint, in_negociacion_referente_id bigint)
 RETURNS character varying
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
declare

id_result bigint;

BEGIN
        INSERT INTO contratacion.negociacion_referente (
          negociacion_id ,
          negociacion_referente_id ,
          fecha_creacion
        )values (
          in_negociacion_id,
          in_negociacion_referente_id,
          now()) returning id into id_result ;

         update contratacion.negociacion
         set negociacion_referente_id = id_result
         where id = in_negociacion_id;

    RETURN '1'; ---> salio todo bien.

    EXCEPTION WHEN OTHERS THEN
      RAISE NOTICE '% %', SQLERRM, SQLSTATE;
    	RETURN '0'; --> y mostrar mensajes de error
    END;
$function$
;
