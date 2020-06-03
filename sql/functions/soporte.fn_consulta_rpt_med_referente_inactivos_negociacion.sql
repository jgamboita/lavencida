
create or replace function soporte.fn_consulta_rpt_med_referente_inactivos_negociacion(p_referente varchar(100))
returns table
        (
            negociacion_id             integer,
            referente                  varchar(100),
            codigo_prestador           varchar(10),
            numero_documento           varchar(14),
            prestador                  varchar(300),
            tipo_modalidad_negociacion varchar(100),
            estado_negociacion         varchar(100),
            regimen                    varchar(100),
            numero_contrato            varchar(100),
            fecha_inicio               date,
            fecha_fin                  date,
            estado_legalizacion        varchar(30)
        )
language plpgsql
as
$$
    begin

        return query
        select
            mirn.negociacion_id,
            mirn.referente,
            mirn.codigo_prestador,
            mirn.numero_documento,
            mirn.prestador,
            mirn.tipo_modalidad_negociacion,
            mirn.estado_negociacion,
            mirn.regimen,
            mirn.numero_contrato,
            mirn.fecha_inicio,
            mirn.fecha_fin,
            mirn.estado_legalizacion
        from maestros.medicamento_inactivo_referente_negociacion mirn
        where mirn.referente = p_referente;

    end;
$$;

