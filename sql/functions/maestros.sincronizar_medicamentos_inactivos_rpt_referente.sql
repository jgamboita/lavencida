
create or replace function maestros.sincronizar_medicamentos_inactivos_rpt_referente(codigos varchar(200)[])
returns void
language plpgsql
as
$$
    /***
      Reporte medicamentos que se inactivan y pertenecen al referente capita rias
     */
    begin

        truncate table  maestros.medicamento_inactivo_referente;

        insert into maestros.medicamento_inactivo_referente(referente, medicamento_id, codigo, descripcion, nombre_alterno, estado_medicamento_id, ruta, actividad, poblacion)
        select distinct
               r.descripcion as referente,
               m.id as medicamento_id,
               m.codigo,
               m.descripcion,
               m.nombre_alterno,
               estado_medicamento_id,
               ria.descripcion as ruta,
               a.descripcion as actividad,
               rp.descripcion as poblacion
        from (select * from contratacion.referente where tipo_referente_id = 1 order by id desc limit 1) r --ultimo referente capita rias
        join contratacion.referente_medicamento_ria_capita rm on rm.referente_id = r.id
        join maestros.ria ria on ria.id = rm.ria_id
        join maestros.medicamento m on m.id = rm.medicamento_id
        left join maestros.actividad a on a.id = rm.actividad_id
        left join maestros.rango_poblacion rp on rp.id = rm.rango_poblacion_id
        where m.codigo = any(codigos);

    end;
$$;
