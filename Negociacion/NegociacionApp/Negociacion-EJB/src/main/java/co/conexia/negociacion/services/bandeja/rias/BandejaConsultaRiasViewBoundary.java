package co.conexia.negociacion.services.bandeja.rias;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conexia.contratacion.commons.constants.enums.EstadoNegociacionEnum;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.RegionalDto;
import com.conexia.contratacion.commons.dto.maestros.RiaDto;
import com.conexia.negociacion.definitions.bandeja.rias.BandejaConsultaRiasViewServiceRemote;

@Stateless
@Remote(BandejaConsultaRiasViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class BandejaConsultaRiasViewBoundary implements BandejaConsultaRiasViewServiceRemote{

	@PersistenceContext(unitName = "contractualDS")
    private EntityManager em;


	@SuppressWarnings("unchecked")
	public List<RiaDto> buscarRias(List<RiaDto> rias, DepartamentoDto departamento, MunicipioDto municipio, RegionalDto regional){
		StringBuilder select = new StringBuilder();
		List<Integer> riasIds = new ArrayList<>();
		rias.stream().parallel().forEach(ria -> {
			riasIds.add(ria.getId());
		});
		select.append("WITH RIA AS (SELECT "
							+ "	r.id prestador,"
							+ " r.descripcion as ruta_salud ,"
							+ " rc.procedimiento_servicio_id,"
							+ " ps.reps_cups,"
							+ " ss.nombre as descripcion_servicio,"
							+ " ps.cups,"
							+ " ps.codigo_cliente,"
							+ " ps.descripcion  "
							+ " FROM maestros.ria r"
							+ " JOIN maestros.ria_contenido rc on rc.ria_id = r.id "
							+ " JOIN maestros.rango_poblacion rp on rc.rango_poblacion_id = rp.id"
							+ " JOIN maestros.procedimiento_servicio ps ON rc.procedimiento_servicio_id = ps.id "
							+ " JOIN contratacion.servicio_salud ss ON ps.servicio_id = ss.id ");
		select.append(" WHERE r.id in (");
		select.append(riasIds.toString().replace("[","").replace("]",""));
		select.append(") )");

		select.append("SELECT	ria.prestador, ria.ruta_salud, ria.procedimiento_servicio_id, ria.reps_cups, "
							+ " ria.descripcion_servicio, ria.cups, ria.codigo_cliente, ria.descripcion, "
							+ " COALESCE(procedimientoContratados.negociacion_id, 0) as negociacion_id, "
							+ " COALESCE(procedimientoContratados.numero_documento, ' ') as numero_documento, "
							+ " COALESCE(procedimientoContratados.nombre, ' ') as nombre, "
							+ " COALESCE(procedimientoContratados.nombre_sede , ' ') as nombre_sede, "
							+ " COALESCE(procedimientoContratados.descripcion, ' ') as municipio_cobertura "
							+ "	FROM RIA ria "
							+ " LEFT JOIN ( "
							+ "		SELECT	snp.procedimiento_id, p.numero_documento, p.nombre, sn.negociacion_id, "
							+ "				d.id as departamento_id, m.descripcion, sp.nombre_sede "
							+ "		FROM contratacion.negociacion n "
							+ "		JOIN contratacion.sedes_negociacion sn  ON sn.negociacion_id = n.id "
							+ " 	JOIN contratacion.sede_prestador sp  ON sn.sede_prestador_id = sp.id "
							+ "		JOIN contratacion.area_cobertura_sedes acs  ON acs.sede_negociacion_id = sn.id "
							+ "		JOIN contratacion.prestador p  ON n.prestador_id = p.id "
							+ "		JOIN contratacion.sede_negociacion_servicio sns  ON sns.sede_negociacion_id = sn.id "
							+ "		JOIN contratacion.sede_negociacion_procedimiento snp  ON snp.sede_negociacion_servicio_id = sns.id "
							+ "		JOIN maestros.municipio m  ON acs.municipio_id = m.id "
							+ "		JOIN maestros.departamento d  ON m.departamento_id = d.id "
							+ "		JOIN maestros.regional r  ON d.regional_id = r.id ");
			if(Objects.isNull(departamento) && Objects.isNull(municipio)){
					select.append(" WHERE n.estado_negociacion = :estadoNegociacion  and r.id =:regionalId   ");
			}else if(Objects.nonNull(departamento) && Objects.isNull(municipio)){
					select.append(" WHERE n.estado_negociacion = :estadoNegociacion  and r.id =:regionalId  and d.id = " + departamento.getId() );
			}else{
					select.append(" WHERE n.estado_negociacion = :estadoNegociacion  and r.id =:regionalId  and d.id = " + departamento.getId());
					select.append(" and m.id = " + municipio.getId() );
			}
			select.append(" AND snp.procedimiento_id in (select procedimiento_servicio_id from RIA) "
						+ " )procedimientoContratados ON procedimientoContratados.procedimiento_id = ria.procedimiento_servicio_id ");

			select.append("order by ria.prestador, ria.reps_cups, ria.cups");
		return em.createNativeQuery(select.toString(), "Ria.rutaSaludReporteMapping")
				.setParameter("estadoNegociacion", EstadoNegociacionEnum.FINALIZADA.toString())
				.setParameter("regionalId", regional.getId())
				.getResultList();
	}

}
