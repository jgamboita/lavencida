
create or replace function soporte.fn_sincronizar_medicamentos_inactivos_generar_reportes_ref(codigos varchar(200)[])
returns void
language plpgsql
as
$$
    begin

        /*
         Reporte de medicamentos pertenecientes a referente rias capita que se inactivan (maestros.medicamento_inactivo_referente)
         Reporte de negociaciones que se ven afectadas (maestros.medicamento_inactivo_referente_negociacion)
         */
        PERFORM maestros.sincronizar_medicamentos_inactivos_rpt_referente(codigos);
        PERFORM maestros.sincronizar_medicamentos_inactivos_rpt_referente_negociacion(codigos);

    end;
$$;
