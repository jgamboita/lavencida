
create or replace function maestros.sincronizar_medicamentos_inactivos_rpt_referente_negociacion(codigos varchar(200)[])
returns void
language plpgsql
as
$$
    /***
      Reporte negociaciones afectadas por medicamentos que se inactivan y pertenecen al referente capita rias
     */
    begin

        truncate table  maestros.medicamento_inactivo_referente_negociacion;

        insert into maestros.medicamento_inactivo_referente_negociacion(negociacion_id, referente, codigo_prestador, numero_documento, prestador, tipo_modalidad_negociacion, estado_negociacion, regimen, numero_contrato, fecha_inicio, fecha_fin, estado_legalizacion, nombre_sede, codigo_sede, codigo_medicamento)
        select distinct
               n.id as negociacion_id,
               r.descripcion as referente,
               p.codigo_prestador,
               p.numero_documento,
               p.nombre as prestador,
               n.tipo_modalidad_negociacion,
               n.estado_negociacion,
               n.regimen,
               c.numero_contrato,
               c.fecha_inicio,
               c.fecha_fin,
               sc.estado_legalizacion_id as estado_legalizacion,
               sp.nombre_sede,
               sp.codigo_sede,
               m.codigo as codigo_medicamento
        from contratacion.negociacion n
        join (select * from contratacion.referente where tipo_referente_id = 1 order by id desc limit 1) r on r.id = n.referente_id --ultimo referente capita rias
        join contratacion.prestador p on p.id = n.prestador_id
        left join contratacion.solicitud_contratacion sc on sc.negociacion_id = n.id
        left join contratacion.contrato c on c.solicitud_contratacion_id = sc.id
        join contratacion.sedes_negociacion sn on sn.negociacion_id = n.id
        join contratacion.sede_negociacion_medicamento snm on snm.sede_negociacion_id = sn.id
        join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id
        join maestros.medicamento m on m.id = snm.medicamento_id
        where m.codigo = any(codigos);

    end;
$$;
