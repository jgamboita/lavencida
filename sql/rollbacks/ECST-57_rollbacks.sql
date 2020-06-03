drop function if exists soporte.fn_sincronizar_medicamentos_inactivos_generar_reportes_ref;
drop function if exists soporte.fn_consulta_rpt_med_referente_inactivos_negociacion;
drop function if exists soporte.fn_consulta_rpt_med_referente_inactivos;


create or replace function maestros.sincronizar_medicamentos_inactivos()
    returns TABLE
            (
                var_actualizados integer
            )
    language plpgsql
as
$$
DECLARE
BEGIN

    /*Función que sincroniza los medicamentos de la maestra que otorga por el area de Salud
     * este archivo se guarda como csv por hoja, en esta función se maneja la hoja de los medicamentos inactivos
     * que se debe guardar en la ruta: /archivos_adjuntos/archivos_novedades/medicamentos_inactivos.csv,
     * se agrega la fecha de inactivo a los medicamentos,
     * se le pone por defecto la fecha '10/11/2006' a los registros que no tengan una fecha definida*/

    UPDATE maestros.medicamento me
    SET fecha_inactivo = to_date(
            CASE WHEN mi.fecha_inactivo = '0' THEN '10/11/2006'::varchar ELSE mi.fecha_inactivo END, 'MM/dd/yyyy')
    FROM maestros.medicamento_inactivo mi
    WHERE me.codigo = mi.cum_ajustado;

    GET DIAGNOSTICS var_actualizados = ROW_COUNT;

    RETURN query SELECT var_actualizados;

END
$$;


drop function if exists maestros.sincronizar_medicamentos_inactivos_rpt_referente_negociacion;
drop function if exists maestros.sincronizar_medicamentos_inactivos_rpt_referente;
drop table if exists maestros.medicamento_inactivo_referente_negociacion;
drop table if exists  maestros.medicamento_inactivo_referente;
