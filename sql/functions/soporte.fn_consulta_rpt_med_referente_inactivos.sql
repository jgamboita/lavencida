
create or replace function soporte.fn_consulta_rpt_med_referente_inactivos(p_referente varchar(100))
returns table
        (
            referente             varchar(100),
            medicamento_id        integer,
            codigo                varchar(20),
            descripcion           varchar(1500),
            nombre_alterno        text,
            estado_medicamento_id integer,
            ruta                  varchar(100),
            actividad             varchar(300),
            poblacion             varchar(100)
        )
language plpgsql
as
$$
    begin

        return query
        select
            mir.referente,
            mir.medicamento_id,
            mir.codigo,
            mir.descripcion,
            mir.nombre_alterno,
            mir.estado_medicamento_id,
            mir.ruta,
            mir.actividad,
            mir.poblacion
        from maestros.medicamento_inactivo_referente mir
        where mir.referente = p_referente;

    end;
$$;