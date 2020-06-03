package co.conexia.negociacion.services.negociacion.control;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.RiasEnum;
import com.conexia.contratacion.commons.dto.FiltroAnexoTarifarioDto;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.Serializable;

public class GestionNegociacionControl implements Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@PersistenceContext(unitName = "contractualDS")
        private EntityManager em;


	public String generarSelectMedicamentosCapita(Long negociacionId) {
        StringBuilder query = new StringBuilder();
        query.append("select CONCAT(sp.codigo_prestador, '-', sp.codigo_sede) AS codHabilitacionSede, ");
        query.append("sp.nombre_sede AS sedePrestador, ");
        query.append("case sncm.macro_categoria_medicamento_id ");
        query.append("when 1 then 'Hospitalario' ");
        query.append("when 2 then 'Ambulatorio' ");
        query.append("when 3 then 'Urgencias' ");
        query.append("when 4 then 'PyP' ");
        query.append("when 5 then 'Oxigeno' ");
        query.append("when 6 then 'Post - Hospitalario' ");
        query.append("end as categoria, ");
        query.append("round(sncm.valor_negociado) as valorNegociado, ");
        query.append("round(sncm.porcentaje_negociado,3) as porcentajeNegociado, ");
        query.append("n.poblacion as poblacion, n.valor_upc_mensual as upcNegociada ");
        query.append("from contratacion.sede_negociacion_categoria_medicamento sncm ");
        query.append("join contratacion.sedes_negociacion sn on sncm.sede_negociacion_id = sn.id ");
        query.append("join contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id ");
        query.append("join contratacion.negociacion n on n.id = sn.negociacion_id ");
        query.append("where sn.negociacion_id = ").append(negociacionId);
        query.append(" order by codHabilitacionSede");
        return query.toString();
    }

    public String generarSelectMedicamentosEvento(FiltroAnexoTarifarioDto filtros) {
        Long negociacionId = filtros.getNegociacionId();
        NegociacionModalidadEnum modalidad = filtros.getModalidad();
        boolean conRecuperacion = filtros.isConRecuperacion();
        StringBuilder select = new StringBuilder();
        select.append("SELECT DISTINCT ")
                .append(" CONCAT(sp.codigo_prestador, '-', sp.codigo_sede) AS codHabilitacionSede,  ")
                .append(" sp.nombre_sede AS sedePrestador, ");
        if (NegociacionModalidadEnum.RIAS_CAPITA.equals(modalidad) || NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(modalidad)) {
            select.append(" ria.descripcionRia, a.descripcion actividad, ");
            if (NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(modalidad)) {
                select.append(" nrrp.poblacion as poblacion, nrrp.upc_negociada as upcNegociada, ");
            } else {
                select.append(" n.poblacion as poblacion, n.valor_upc_mensual as upcNegociada, ");
            }
        } else {
            select.append(" null as descripcionRia, ").append(" null as actividad, ").append(" null as poblacion, ").append("null as upcNegociada, ");
        }
        select.append("  cm.codigo as codigoGrupoTerapeutico, cm.nombre as descripcionGrupoTerapeutico, m.codigo as cum, m.atc as atc, m.descripcion_atc as descripcionAtc,  ")
                .append("  CONCAT(m.descripcion_invima,' - ',m.principio_activo,' - ',m.concentracion,' - ',m.forma_farmaceutica,' - ',m.descripcion) as nombreProducto,")
                .append("  m.titular_registro as titularRegistroSanitario, ")
                .append("  tp.descripcion AS categoriaPos, m.regulado as regulado, snm.valor_negociado as tarifaNegociada, snm.porcentaje_negociado,")
                .append("  c.fecha_inicio as fechaInicial, c.fecha_fin as fechaFinal, ")
                .append("  m.estado_medicamento_id as estadoMedicamento")
                .append("  FROM contratacion.sedes_negociacion sn ")
                .append("  INNER JOIN contratacion.negociacion n ON n.id = sn.negociacion_id ")
                .append("  INNER JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id ")
                .append("  INNER JOIN contratacion.sede_negociacion_medicamento snm ON snm.sede_negociacion_id = sn.id ");

        if (filtros.getActividades() != null && !filtros.getActividades().isEmpty()) {
            select.append(" and snm.actividad_id in (:actividades) ");
        }

        select.append("  INNER JOIN maestros.medicamento m ON m.id = snm.medicamento_id  ")
                .append(" LEFT JOIN contratacion.categoria_medicamento cm ON cm.id = m.categoria_id ")
                .append(" LEFT JOIN maestros.tipo_ppm tp on tp.id = m.tipo_ppm_id     ")
                .append(" left join contratacion.solicitud_contratacion sol on sol.negociacion_id = n.id ")
                .append(" left join contratacion.contrato c on c.solicitud_contratacion_id  = sol.id ");
        if (NegociacionModalidadEnum.RIAS_CAPITA.equals(modalidad) || NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(modalidad)) {
            select.append("     JOIN (SELECT rn.negociacion_id,nrr.id negociacion_ria_rango_poblacion_id, r.descripcion ||' - '|| rp.descripcion descripcionRia")
                    .append(" 			FROM maestros.ria r ")
                    .append(" 			JOIN contratacion.negociacion_ria rn on rn.ria_id = r.id ")
                    .append(" 			JOIN contratacion.negociacion_ria_rango_poblacion nrr on nrr.negociacion_ria_id = rn.id ")
                    .append(" 			JOIN maestros.rango_poblacion rp on rp.id = nrr.rango_poblacion_id ")
                    .append(" 			WHERE rn.negociacion_id = ").append(negociacionId);
            if (filtros.isIncluirRecuperacion()) {
                if (conRecuperacion) {
                    select.append(" 			AND EXISTS (SELECT null");
                } else {
                    select.append(" 			AND NOT EXISTS (SELECT null");
                }
                select.append(" 							FROM maestros.ria r2")
                        .append(" 					 		WHERE r2.id = r.id and r2.codigo ='")
                        .append(RiasEnum.RECUPERACION.getCodigo()).append("')");
            }
            select.append(" 			) ria on ria.negociacion_id = n.id and ria.negociacion_ria_rango_poblacion_id = snm.negociacion_ria_rango_poblacion_id")
                    .append("   LEFT JOIN maestros.actividad a on a.id = snm.actividad_id ")
                    .append(" INNER JOIN contratacion.negociacion_ria_rango_poblacion nrrp	on nrrp.id= ria.negociacion_ria_rango_poblacion_id ");
        }
        StringBuilder where = new StringBuilder();
        where.append("WHERE n.id = ");
        where.append(negociacionId);
        if (conRecuperacion && filtros.isIncluirRecuperacion()) {
            where.append(" AND m.codigo not like 'MED-%' ");
        }
        StringBuilder query = select.append(where);
        query.append(" order by descripcionRia, codHabilitacionSede, nombreProducto ");
        return query.toString();
    }


	public String generarSelectMedicamentos(FiltroAnexoTarifarioDto filtros) {

		Long negociacionId = filtros.getNegociacionId();
		NegociacionModalidadEnum modalidad = filtros.getModalidad();

		StringBuilder select = new StringBuilder();
		select.append("SELECT DISTINCT ")
				.append(" CONCAT(sp.codigo_prestador, '-', sp.codigo_sede) AS codHabilitacionSede,  ")
				.append(" sp.nombre_sede AS sedePrestador, ");
		if (NegociacionModalidadEnum.RIAS_CAPITA.equals(modalidad)
				|| NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(modalidad)) {
			select.append(" ria.descripcionRia, a.descripcion actividad, ");
			if (NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(modalidad)) {
				select.append(" nrrp.poblacion as poblacion, nrrp.upc_negociada as valorNegociado, ");
			} else {
				select.append(" n.poblacion as poblacion, n.valor_upc_mensual as valorNegociado, ");
			}
		} else {
			select.append(" '' descripcionRia, ").append(" '' actividad, ").append(" 0 poblacion, ")
					.append(" 0 valorNegociado, ");
		}
		select.append(
				"  cm.codigo as codigoGrupoTerapeutico, cm.nombre as descripcionGrupoTerapeutico, m.codigo as cum, m.atc as atc, m.descripcion_atc as descripcionAtc,  ")
				.append("  CONCAT(m.descripcion_invima,' - ',m.principio_activo,' - ',m.concentracion,' - ',m.forma_farmaceutica,' - ',m.descripcion) as nombreProducto,")
				.append("  m.titular_registro as titularRegistroSanitario, ")
				.append("  tp.descripcion AS categoriaPos, m.regulado as regulado, snm.valor_negociado as tarifaNegociada, snm.porcentaje_negociado,")
				.append("  c.fecha_inicio as fechaInicial, c.fecha_fin as fechaFinal ")
				.append("  FROM contratacion.sedes_negociacion sn ")
				.append("  INNER JOIN contratacion.negociacion n ON n.id = sn.negociacion_id ")
				.append("  INNER JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id ")
				.append("  INNER JOIN contratacion.sede_negociacion_medicamento snm ON snm.sede_negociacion_id = sn.id ");
		if (filtros.getActividades() != null && !filtros.getActividades().isEmpty()) {
			select.append(" and snm.actividad_id in (:actividades) ");
		}

		select.append("  INNER JOIN maestros.medicamento m ON m.id = snm.medicamento_id  ")
				.append(" LEFT JOIN contratacion.categoria_medicamento cm ON cm.id = m.categoria_id ")
				.append(" LEFT JOIN maestros.tipo_ppm tp on tp.id = m.tipo_ppm_id     ")
				.append(" left join contratacion.solicitud_contratacion sol on sol.negociacion_id = n.id ")
				.append(" left join contratacion.contrato c on c.solicitud_contratacion_id  = sol.id ");
		if (NegociacionModalidadEnum.RIAS_CAPITA.equals(modalidad)
				|| NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(modalidad)) {
			select.append(
					" JOIN (SELECT rn.negociacion_id,nrr.id negociacion_ria_rango_poblacion_id, r.descripcion ||' - '|| rp.descripcion descripcionRia")
					.append(" 			FROM maestros.ria r ")
					.append(" 			JOIN contratacion.negociacion_ria rn on rn.ria_id = r.id ")
					.append(" 			JOIN contratacion.negociacion_ria_rango_poblacion nrr on nrr.negociacion_ria_id = rn.id ")
					.append(" 			JOIN maestros.rango_poblacion rp on rp.id = nrr.rango_poblacion_id ")
					.append(" 			WHERE rn.negociacion_id = ").append(negociacionId)
					.append(" 			) ria on ria.negociacion_id = n.id and ria.negociacion_ria_rango_poblacion_id = snm.negociacion_ria_rango_poblacion_id")
					.append("   LEFT JOIN maestros.actividad a on a.id = snm.actividad_id ")
					.append(" INNER JOIN contratacion.negociacion_ria_rango_poblacion nrrp	on nrrp.id= ria.negociacion_ria_rango_poblacion_id ");
		}
		StringBuilder where = new StringBuilder();
		where.append("WHERE n.id = ");
		where.append(negociacionId);
		StringBuilder query = select.append(where);
		query.append(" order by descripcionRia, codHabilitacionSede, nombreProducto ");
		return query.toString();
	}

    public String generarSelectCategoriasMedicamentosRiasCapita(Long negociacionId, NegociacionModalidadEnum modalidad) {
    	StringBuilder select = new StringBuilder();
    	/*select.append(" SELECT DISTINCT  CONCAT(sp.codigo_prestador, '-', sp.codigo_sede) AS codHabilitacionSede,                                                                                              ")
		.append(" 		sp.nombre_sede AS sedePrestador,                                                                                                                                                       ")
		.append(" 		rra.ria_rango descripcionRia,                                                                                                                                                                         ")
		.append(" 		rra.actividad,                                                                                                                                                                         ")
		.append(" 		null as codigoGrupoTerapeutico,                                                                                                                                                        ")
		.append(" 		null as descripcionGrupoTerapeutico,                                                                                                                                                   ")
		.append(" 		lpad(cast(macro_categoria.id as varchar), 2, '0') cum,                                                                                                                                 ")
		.append(" 		null as atc,                                                                                                                                                                           ")
		.append(" 		null as descripcionAtc,                                                                                                                                                                ")
		.append(" 		macro_categoria.descripcion as nombreProducto,                                                                                                                                         ")
		.append(" 		null as titularRegistroSanitario,                                                                                                                                                      ")
		.append(" 		null AS categoriaPos,                                                                                                                                                                  ")
		.append(" 		null as regulado,                                                                                                                                                                      ")
		.append(" 		sncm.valor_negociado as tarifaNegociada,                                                                                                                                               ")
		.append(" 		sncm.porcentaje_negociado                                                                                                                                                              ")
		.append(" FROM contratacion.sedes_negociacion sn                                                                                                                                                       ")
		.append(" INNER JOIN contratacion.negociacion n ON n.id = sn.negociacion_id                                                                                                                            ")
		.append(" INNER JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id                                                                                                                    ")
		.append(" INNER JOIN contratacion.sede_negociacion_categoria_medicamento sncm ON sncm.sede_negociacion_id = sn.id                                                                                      ")
		.append(" inner join (select id, descripcion                                                                                                                                                           ")
		.append("  		from (values ((1), ('Hospitalario')), ((2), ('Ambulatorio')),                                                                                                                          ")
		.append("  						((3), ('Urgencias')), ((4), ('PyP')),                                                                                                                                  ")
		.append("  						((5), ('Oxigeno')), ((6), ('Post - Hospitalario'))) as macro_categorias (id, descripcion)) macro_categoria on macro_categoria.id = sncm.macro_categoria_medicamento_id ")
		.append(" inner join (select distinct concat(ria.descripcion, ' - ', rp.descripcion) ria_rango, actividad.descripcion actividad                                                                        ")
		.append(" 		from maestros.ria_contenido rc                                                                                                                                                         ")
		.append(" 		inner join maestros.ria ria on ria.id = rc.ria_id and ria.id = "+RiasEnum.RECUPERACION.getId()+"                                                                                                                  ")
		.append(" 		inner join maestros.actividad actividad on actividad.id = rc.actividad_id and actividad.id = "+42+"                                                                              ")
		.append(" 		inner join maestros.rango_poblacion rp on rp.id = rc.rango_poblacion_id) rra on 1 = 1                                                                                                  ")
		.append(" where n.id = "+negociacionId+" ");
    	*/
    	select.append(" SELECT DISTINCT  CONCAT(sp.codigo_prestador,'-', sp.codigo_sede) AS codHabilitacionSede, ");
    	select.append("     sp.nombre_sede AS sedePrestador, rra.ria_rango descripcionRia, ");
    	select.append("		rra.actividad, null as codigoGrupoTerapeutico, null as descripcionGrupoTerapeutico, ");
    	select.append("		m.codigo cum, null as atc, null as descripcionAtc, m.codigo as nombreProducto, ");
    	if(modalidad.equals(NegociacionModalidadEnum.RIAS_CAPITA)) {
    		select.append("     n.poblacion as poblacion, n.valor_upc_mensual as upcNegociada, c.fecha_inicio as fechaInicial, c.fecha_fin as fechaFinal, m.estado_medicamento_id as estadoMedicamento, ");
    	}else {
    		select.append("     nrp.poblacion as poblacion, nrp.upc_negociada as upcNegociada, c.fecha_inicio as fechaInicial, c.fecha_fin as fechaFinal, ");
    	}
    	select.append("		null as titularRegistroSanitario, null AS categoriaPos, m.regulado as regulado, snm.valor_negociado as tarifaNegociada, snm.porcentaje_negociado  ");
    	select.append(" FROM contratacion.sede_negociacion_medicamento snm ");
    	select.append(" JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id ");
    	select.append(" join contratacion.negociacion n on n.id = sn.negociacion_id ");
    	select.append(" JOIN maestros.medicamento m ON snm.medicamento_id = m.id ");
    	select.append(" JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id ");
    	select.append(" JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id and snm.negociacion_ria_rango_poblacion_id = nrp.id ");
    	select.append(" JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id ");
    	select.append(" left join contratacion.solicitud_contratacion sol on sol.negociacion_id = n.id ");
    	select.append(" left join contratacion.contrato c on c.solicitud_contratacion_id = sol.id ");
    	select.append(" INNER JOIN ( ");
    	select.append("		select distinct concat(ria.descripcion, ' - ', rp.descripcion) ria_rango, actividad.descripcion actividad ");
    	select.append(" 	from  maestros.ria_contenido rc ");
    	select.append("		inner join maestros.ria ria on ria.id = rc.ria_id and ria.id ="+RiasEnum.RECUPERACION.getId()+" ");
    	select.append("		inner join maestros.actividad actividad on actividad.id = rc.actividad_id and actividad.id = 42 ");
    	select.append("		inner join maestros.rango_poblacion rp on rp.id = rc.rango_poblacion_id ");
    	select.append(" ) rra on 1 = 1 ");
    	select.append(" WHERE sn.negociacion_id ="+negociacionId+" AND m.codigo like 'MED-%' ");


    	return select.toString();
    }



}
