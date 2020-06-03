CREATE OR REPLACE FUNCTION contratacion.sp_find_legalizacion_contrato_by_solicitud_contratacion(p_solicitud_contratacion_id bigint, p_negociacion_id bigint, p_negociacion_padre_id bigint, p_negociacion_origen_ide bigint, p_regimen character varying, p_tipo_modalidad character varying)
 RETURNS TABLE(id_contrato integer, numero_contrato character varying, regional_id integer, codigo character varying, descripcion character varying, tipo_subsidiado character varying, tipo_contrato character varying, fecha_inicio date, fecha_fin date, nivel_contrato character varying, nombre_archivo character varying, legalizacion_contrato_id integer, legalizacion_minuta_id integer, legalizacion_minuta_nombre character varying, legalizacion_minuta_descripcion character varying, objeto_contrato_id integer, valor_fiscal double precision, valor_poliza double precision, dias_plazo integer, departamento_responsable_id integer, municipio_responsable_id integer, fecha_firma_contrato date, responsable_firma_contrato_id integer, departamento_id integer, departamento_descripcion character varying, municipio_id integer, municipio_descripcion character varying, direccion character varying, nombre character varying, municipio_recepcion character varying, contenido text, estado_legalizacion_id character varying, fecha_elaboracion date, fecha_inicio_otro_si date, fecha_fin_otro_si date, minuta_id integer, minuta_nombre character varying, minuta_descripcion character varying, observacion_otro_si text, fecha_elaboracion_otrosi date)
 LANGUAGE plpgsql
AS $function$
BEGIN
	RETURN QUERY
	 SELECT cast(c.id as integer) as id,
     c.numero_contrato as numero_contrato,
     cast(c.regional_id as integer) as regional_id,
     (select r.codigo from maestros.regional r where r.id = c.regional_id ) as codigo,
     (select r.descripcion from maestros.regional r where r.id = c.regional_id ) as descripcion,
     c.tipo_subsidiado as tipo_subsidiado,
     c.tipo_contrato as tipo_contrato,
     case when n.observacion = 'TECNOLOGÍA CON PRÓRROGA POR OTRO SI' then
            ((  select cast(c2.fecha_inicio as date)
                from contratacion.contrato c2
                inner join contratacion.solicitud_contratacion sc2 on c2.solicitud_contratacion_id = sc2.id
                where sc2.id = (
                    select scon.id
                    from contratacion.solicitud_contratacion scon
                    inner join contratacion.negociacion ng3 on scon.negociacion_id = ng3.id
                    where ng3.id = p_negociacion_origen_ide order by scon.regimen desc limit 1
          )
             )
            ) else (cast(c.fecha_inicio as date) ) end  as fecha_inicio,
        case when n.observacion = 'TECNOLOGÍA CON PRÓRROGA POR OTRO SI' then
          ((  select cast(c2.fecha_fin  as date)
                from contratacion.contrato c2
                inner join contratacion.solicitud_contratacion sc2 on c2.solicitud_contratacion_id = sc2.id
                where sc2.id = (
                  select scon.id
                    from contratacion.solicitud_contratacion scon
                    inner join contratacion.negociacion ng3 on scon.negociacion_id = ng3.id
                    where ng3.id = p_negociacion_padre_id order by scon.regimen desc limit 1
              )
           )
          ) else (cast(c.fecha_fin as date) ) end  as fecha_fin,
          c.nivel_contrato as nivel_contrato,
          c.nombre_archivo as nombre_archivo,
          cast(l.id as integer) as id,
          cast(l.minuta_id as integer) as legalizacion_minuta_id,
          (select distinct (mi2.nombre)
           from contratacion.legalizacion_contrato lc2
           inner join contratacion.minuta mi2 on lc2.minuta_id = mi2.id
           where lc2.minuta_id = l.minuta_id ) as legalizacion_minuta_nombre,
          (select distinct (mi2.descripcion )
           from contratacion.legalizacion_contrato lc2
           inner join contratacion.minuta mi2 on lc2.minuta_id = mi2.id
           where lc2.minuta_id = l.minuta_id ) as legalizacion_minuta_descripcion,
          cast(l.objeto_contrato_id as integer) as objeto_contrato,
          l.valor_fiscal as valor_fiscal,
          l.valor_poliza as valor_poliza,
          l.dias_plazo as dias_plazo,
      (select cast(de.id as integer)
         from contratacion.legalizacion_contrato lc
         inner join maestros.municipio mu on lc.municipio_responsable_id = mu.id
         inner join maestros.departamento de on mu.departamento_id = de.id
         where lc.id = l.id) as id,
      (select cast(mu.id  as integer)
         from contratacion.legalizacion_contrato lc
         inner join maestros.municipio mu on lc.municipio_responsable_id = mu.id
         where lc.id = l.id) as id,
      cast(l.fecha_firma_contrato as date) as fecha_firma_contrato,
      cast(l.responsable_firma_contrato_id as integer) as responsable_firma_contrato_id,
      (select cast(de.id  as integer)
         from contratacion.legalizacion_contrato lc
         inner join maestros.municipio mu on lc.municipio_id = mu.id
         inner join maestros.departamento de on mu.departamento_id = de.id
         where lc.id = l.id) as id,
      (select de.descripcion
         from contratacion.legalizacion_contrato lc
         inner join maestros.municipio mu on lc.municipio_id = mu.id
         inner join maestros.departamento de on mu.departamento_id = de.id
         where lc.id = l.id) as descripcion,
      (select cast(mu.id  as integer)
         from contratacion.legalizacion_contrato lc
         inner join maestros.municipio mu on lc.municipio_id = mu.id
         where lc.id = l.id) as id,
      (select mu.descripcion
         from contratacion.legalizacion_contrato lc
         inner join maestros.municipio mu on lc.municipio_id = mu.id
         where lc.id = l.id) as descripcion,
      l.direccion as direccion,
      (select rc.nombre
      from contratacion.legalizacion_contrato lc
      inner join contratacion.responsable_contrato rc on lc.responsable_firma_contrato_id = rc.id
      where lc.id = l.id) as nombre,
      (select rc.municipio_recepcion
      from contratacion.legalizacion_contrato lc
      inner join contratacion.responsable_contrato rc on lc.responsable_firma_contrato_id = rc.id
      where lc.id = l.id) as municipio_recepcion,
      l.contenido as contenido,
      sc.estado_legalizacion_id  as estado_legalizacion_id,
      case when n.observacion='TECNOLOGÍA CON PRÓRROGA POR OTRO SI' or n.observacion='TECNOLOGÍA CON PRÓRROGA POR OTRO SI + TECNOLOGÍA MODIFICADA/CORREGIDA POR OTRO SI' then
          ((  select cast(c2.fecha_elaboracion  as date)
                from contratacion.contrato c2
                inner join contratacion.solicitud_contratacion sc2 on c2.solicitud_contratacion_id = sc2.id
                where sc2.id = (
                  select scon.id
                    from contratacion.solicitud_contratacion scon
                    inner join contratacion.negociacion ng3 on scon.negociacion_id = ng3.id
                    where ng3.id = p_negociacion_padre_id order by scon.regimen desc limit 1
              )
           )
          )
          else (cast(c.fecha_elaboracion as date) ) end as fecha_elaboracion,
      case when n.observacion='TECNOLOGÍA CON PRÓRROGA POR OTRO SI' then
          ((  select cast(c2.fecha_inicio  as date)
                from contratacion.contrato c2
                inner join contratacion.solicitud_contratacion sc2 on c2.solicitud_contratacion_id = sc2.id
                where sc2.id = (
                  select scon.id
                    from contratacion.solicitud_contratacion scon
                    inner join contratacion.negociacion ng3 on scon.negociacion_id = ng3.id
                    where ng3.id = p_negociacion_id order by scon.regimen desc limit 1
              )
           )
          )when n.observacion='TECNOLOGÍA CON PRÓRROGA POR OTRO SI + TECNOLOGÍA MODIFICADA/CORREGIDA POR OTRO SI' then
          ((  select cast(c2.fecha_inicio  as date)
                from contratacion.contrato c2
                inner join contratacion.solicitud_contratacion sc2 on c2.solicitud_contratacion_id = sc2.id
                where sc2.id = (
                  select scon.id
                    from contratacion.solicitud_contratacion scon
                    inner join contratacion.negociacion ng3 on scon.negociacion_id = ng3.id
                    where ng3.id = p_negociacion_id order by scon.regimen desc limit 1
              )
           )
          )
          else (cast(c.fecha_inicio_otro_si as date) ) end as fecha_inicio_otro_si,
          case when n.observacion='TECNOLOGÍA CON PRÓRROGA POR OTRO SI' then
          ((  select cast(c2.fecha_fin  as date)
                from contratacion.contrato c2
                inner join contratacion.solicitud_contratacion sc2 on c2.solicitud_contratacion_id = sc2.id
                where sc2.id = (
                  select scon.id
                    from contratacion.solicitud_contratacion scon
                    inner join contratacion.negociacion ng3 on scon.negociacion_id = ng3.id
                    where ng3.id = p_negociacion_id order by scon.regimen desc limit 1
              )
           )
          ) when n.observacion='TECNOLOGÍA CON PRÓRROGA POR OTRO SI + TECNOLOGÍA MODIFICADA/CORREGIDA POR OTRO SI' then
          ((  select cast(c2.fecha_fin  as date)
                from contratacion.contrato c2
                inner join contratacion.solicitud_contratacion sc2 on c2.solicitud_contratacion_id = sc2.id
                where sc2.id = (
                  select scon.id
                    from contratacion.solicitud_contratacion scon
                    inner join contratacion.negociacion ng3 on scon.negociacion_id = ng3.id
                    where ng3.id = p_negociacion_id order by scon.regimen desc limit 1
              )
           )
          )
          when n.negociacion_origen is not null then (
              case when n.observacion is null then
              ((  select cast(c2.fecha_fin_otro_si  as date)
                from contratacion.contrato c2
                inner join contratacion.solicitud_contratacion sc2 on c2.solicitud_contratacion_id = sc2.id
                where sc2.id = (
                  select scon.id
                    from contratacion.solicitud_contratacion scon
                    inner join contratacion.negociacion ng3 on scon.negociacion_id = ng3.id
                    where ng3.id = p_negociacion_origen_ide order by scon.regimen desc limit 1
                  )
               )
              ) end
          ) else (cast(c.fecha_fin_otro_si  as date)) end as fecha_fin_otro_si,
          cast(mi.id as integer) as id,
          mi.nombre as nombre,
          mi.descripcion as descripcion,
          l.observacion_otro_si as observacion_otro_si,
           case when n.observacion='TECNOLOGÍA CON PRÓRROGA POR OTRO SI' or n.observacion='TECNOLOGÍA CON PRÓRROGA POR OTRO SI + TECNOLOGÍA MODIFICADA/CORREGIDA POR OTRO SI' then
          ((  select cast(c2.fecha_elaboracion  as date)
                from contratacion.contrato c2
                inner join contratacion.solicitud_contratacion sc2 on c2.solicitud_contratacion_id = sc2.id
                where sc2.id = (
                  select scon.id
                    from contratacion.solicitud_contratacion scon
                    inner join contratacion.negociacion ng3 on scon.negociacion_id = ng3.id
                    where ng3.id = p_negociacion_id order by scon.regimen desc limit 1
              )
           )
          )
          else (cast(c.fecha_elaboracion as date) ) end as fecha_elaboracion_otrosi
FROM contratacion.legalizacion_contrato l
inner join contratacion.contrato c on l.contrato_id = c.id
left join contratacion.minuta mi on l.minuta_otro_si_id = mi.id
inner join contratacion.solicitud_contratacion sc on c.solicitud_contratacion_id = sc.id
inner join contratacion.negociacion n on sc.negociacion_id = n.id
where sc.id = p_solicitud_contratacion_id
and sc.regimen = p_regimen
and c.tipo_modalidad = p_tipo_modalidad;
END;
$function$
;
