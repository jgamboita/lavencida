package com.conexia.contratacion.portafolio.services.view.prestador.boundary;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.conexia.contratacion.commons.dto.capita.ProcedimientoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.capita.ProcedimientoServicioPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;
import com.conexia.contratacion.portafolio.definitions.view.prestador.ProcedimientoServicioPortafolioViewRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.SedePrestadorViewRemote;
import com.conexia.contratacion.portafolio.prestador.control.ProcedimientoServicioPortafolioViewControl;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;

/**
 *
 * @author <a href="dgarcia@conexia.com">David Garcia H</a>
 */
@Stateless
@Remote(ProcedimientoServicioPortafolioViewRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ProcedimientoServicioPortafolioViewBoundary implements
		ProcedimientoServicioPortafolioViewRemote {

	@Inject
	private Log logger;

	/**
	 *
	 */
	private static final long serialVersionUID = -3082638973563894943L;

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;

	@Inject
	private ProcedimientoServicioPortafolioViewControl procedimientoServicioControl;


	@Override
	public  List<ProcedimientoServicioPortafolioSedeDto> listarProcedimientosPorServicio(
			ServicioPortafolioSedeDto servicio, Map<String, Object> filtros,
			int offset, int tamanioPagina) throws ConexiaBusinessException {

		StringBuilder queryString = new StringBuilder();
		procedimientoServicioControl.generarSelectProcedimientosServicio(queryString);

		Map<String, Object> parametros = procedimientoServicioControl
				.generarWhereSedesOferta(queryString, servicio, filtros);

		procedimientoServicioControl
				.generarOrderProcedimientosServicio(queryString);

		TypedQuery<ProcedimientoServicioPortafolioSedeDto> query = em.createQuery(
				queryString.toString(), ProcedimientoServicioPortafolioSedeDto.class);

		for (Entry<String, Object> parametro : parametros.entrySet()) {
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		return query.setFirstResult(offset).setMaxResults(tamanioPagina)
				.getResultList();
	}

	/**
	 * @see {@link SedePrestadorViewRemote#contarSedesPorPrestadorId(Long)}
	 */
	@Override
	public Integer contarProcedimientosPorServicio(
			ServicioPortafolioSedeDto servicio, Map<String, Object> filtros)
			throws ConexiaBusinessException {

		StringBuilder queryString = new StringBuilder();
		procedimientoServicioControl.generarContarProcedimientosServicio(queryString);

		Map<String, Object> parametros = procedimientoServicioControl
				.generarWhereSedesOferta(queryString, servicio, filtros);

		TypedQuery<Integer> query = em.createQuery(queryString.toString(),
				Integer.class);

		for (Entry<String, Object> parametro : parametros.entrySet()) {
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		return query.getSingleResult();
	}

	@Override
	public List<ProcedimientoPortafolioReporteDto> listarProcedimientosPorSede(Long sedeId) {
		Query query = em.createNativeQuery("SELECT concat(ss.codigo, :guion, ss.nombre) AS servicio, " +
				"         p.codigo, " +
				"         p.codigo_emssanar, " +
				"         coalesce(pp.codigo_interno, '') AS codigo_ips, " +
				"         p.descripcion, " +
				"         REPLACE(pp.modalidad, :underscore, :espacio) AS modalidad, " +
				"         t.descripcion AS tarifario, " +
				"         pp.porcentaje_negociacion, " +
				"         pp.valor as valor, " +
				"         array_to_string(ARRAY (select distinct(tarifarios.descripcion) " +
				"         						FROM contratacion.tarifarios_procedimientos tp " +
				"								INNER JOIN contratacion.tarifarios " +
				"                 				ON tarifarios.codigo = tp.tarifarios_codigo " +
				"         				WHERE tp.cups = p.codigo AND tp.peso > 0 UNION SELECT :tarifaPropia " +
				"         				ORDER BY 1), :coma) AS tarifarios_procedimiento, " +
				"    	 :no AS eliminar " +
				" 		from contratacion.grupo_servicio gs " +
				" JOIN contratacion.servicio_salud ss ON ss.id = gs.servicio_salud_id " +
				" JOIN contratacion.procedimiento_portafolio pp ON pp.grupo_servicio_id = gs.id " +
				" JOIN maestros.procedimiento_servicio ps ON ps.id = pp.procedimiento_id " +
				" JOIN maestros.procedimiento p ON p.id = ps.procedimiento_id " +
				" INNER JOIN contratacion.sede_prestador sp ON sp.portafolio_id = gs.portafolio_id " +
				" LEFT JOIN contratacion.tarifarios t ON t.id = pp.tarifario_id " +
				" WHERE sp.id = :sedeId"
				, "ProcedimientoPortafolio.procedimientoPortafolioReporteDto"
				);
		List<ProcedimientoPortafolioReporteDto> result = null;
		try {
			result = query
					.setParameter("sedeId", sedeId)
					.setParameter("guion", " - ")
					.setParameter("underscore", "_")
					.setParameter("espacio", " ")
					.setParameter("coma", ", ")
					.setParameter("tarifaPropia", "TARIFA PROPIA")
					.setParameter("no", "NO")
			.getResultList();
		} catch(Exception e) {
			logger.error("No se pudo consultar la sede: "
				 + sedeId, e);
		}
		return result;
	}

}
