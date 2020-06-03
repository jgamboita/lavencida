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

import com.conexia.contratacion.commons.dto.capita.CategoriaMedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.MedicamentoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.capita.MedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.contratacion.portafolio.definitions.view.prestador.MedicamentoPortafolioViewRemote;
import com.conexia.contratacion.portafolio.prestador.control.MedicamentosPortafolioViewControl;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;

/**
 *
 * @author <a href="dgarcia@conexia.com">David Garcia H</a>
 */
@Stateless
@Remote(MedicamentoPortafolioViewRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class MedicamentoPortafolioViewBoundary implements
		MedicamentoPortafolioViewRemote {

	@Inject
	private Log logger;

	/**
	 *
	 */
	private static final long serialVersionUID = -3781474853705694221L;

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;

	@Inject
	private MedicamentosPortafolioViewControl medicamentosPortafolioControl;

	public int contarMedicamentosPortafolio(
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento,
			Map<String, Object> filters) throws ConexiaBusinessException {

		StringBuilder queryString = new StringBuilder();
		medicamentosPortafolioControl
				.generarContarMedicamentoPortafolio(queryString);
		Map<String, Object> parametros = medicamentosPortafolioControl
				.generarWhereMedicamentoPortafolio(queryString,
						categoriaMedicamento, filters);

		TypedQuery<Integer> query = em.createQuery(queryString.toString(),
				Integer.class);

		for (Entry<String, Object> parametro : parametros.entrySet()) {
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		return query.getSingleResult();
	}

	public List<MedicamentoPortafolioSedeDto> listarMedicamentosPortafolio(
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento,
			Integer offset, Integer tamanioPagina, Map<String, Object> filters)
			throws ConexiaBusinessException {

		StringBuilder queryString = new StringBuilder();
		medicamentosPortafolioControl
				.generarSelectMedicamentoPortafolio(queryString);
		Map<String, Object> parametros = medicamentosPortafolioControl
				.generarWhereMedicamentoPortafolio(queryString,
						categoriaMedicamento, filters);

		TypedQuery<MedicamentoPortafolioSedeDto> query = em.createQuery(
				queryString.toString(), MedicamentoPortafolioSedeDto.class);

		for (Entry<String, Object> parametro : parametros.entrySet()) {
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		if (offset != null) {
			query.setFirstResult(offset);
		}

		if (tamanioPagina != null) {
			query.setMaxResults(tamanioPagina);
		}

		return query.getResultList();
	}

	public int contarCategoriaMedicamentosPortafolio(
			PortafolioSedeDto portafolio, Map<String, Object> filters)
			throws ConexiaBusinessException {

		StringBuilder queryString = new StringBuilder();
		medicamentosPortafolioControl
				.generarContarCategoriaMedicamento(queryString);
		Map<String, Object> parametros = medicamentosPortafolioControl
				.generarWhereCategoriaMedicamento(queryString, portafolio,
						filters);

		Query query = em.createNativeQuery(queryString.toString());

		for (Entry<String, Object> parametro : parametros.entrySet()) {
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		return (Integer) query.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	public List<CategoriaMedicamentoPortafolioSedeDto> listarCategoriaMedicamentosPortafolio(
			PortafolioSedeDto portafolio, Integer offset,
			Integer tamanioPagina, Map<String, Object> filters)
			throws ConexiaBusinessException {

		StringBuilder queryString = new StringBuilder();
		medicamentosPortafolioControl
				.generarSelectCategoriaMedicamento(queryString);
		Map<String, Object> parametros = medicamentosPortafolioControl
				.generarWhereCategoriaMedicamento(queryString, portafolio,
						filters);

		medicamentosPortafolioControl
				.generarWhereCategoriaMedicamento(queryString);

		Query query = em.createNativeQuery(queryString.toString(),
				"CategoriaMedicamentoPortafolioSedeDtoTotalizado");

		for (Entry<String, Object> parametro : parametros.entrySet()) {
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		if (offset != null) {
			query.setFirstResult(offset);
		}

		if (tamanioPagina != null) {
			query.setMaxResults(tamanioPagina);
		}

		return query.getResultList();
	}

	@Override
	public List<MedicamentoPortafolioReporteDto> listarMedicamentosPorSede(Long sedeId) {
		Query query = em.createNativeQuery("select m.codigo, " +
				"m.principio_activo, m.concentracion, " +
				"m.titular_registro, m.registro_sanitario, m.descripcion AS descripcion_comercial, " +
				"m.atc, m.descripcion_atc, m.forma_farmaceutica, " +
				"coalesce(CASE mp.es_capita " +
				"	WHEN TRUE THEN :si " +
				"	WHEN FALSE THEN :no " +
				"	END, '')  as capitado, coalesce(mp.codigo_interno, '') AS codigo_ips, mp.valor " +
				"FROM maestros.medicamento m " +
				"INNER JOIN contratacion.categoria_medicamento cm ON cm.id = m.categoria_id " +
				"INNER JOIN contratacion.medicamento_portafolio mp ON mp.medicamento_id = m.id " +
				"INNER JOIN contratacion.sede_prestador sp ON sp.portafolio_id = mp.portafolio_id " +
				"WHERE sp.id = :sedeId", "MedicamentoPortafolio.medicamentoPortafolioReporteDto");
		List<MedicamentoPortafolioReporteDto> medicamentos = null;
		try {
			medicamentos = query
					.setParameter("sedeId", sedeId)
					.setParameter("si", "Si")
					.setParameter("no", "NO")
					.getResultList();
		} catch (Exception e) {
			logger.error("No se pudo consultar la sede: "
					 + sedeId, e);
		}
		return medicamentos;
	}

	@Override
	public Long obtenerPortafolioDeSede(Long sedeId) {
		Query query = em.createNativeQuery("select portafolio_id from contratacion.sede_prestador sp where sp.id = :sedeId");
		List resultado = null;
		try {
			resultado = query
					.setParameter("sedeId", sedeId)
					.getResultList();
		} catch (Exception e) {
			logger.error("No se pudo consultar la sede: "
					 + sedeId, e);
		}
		return resultado.size() > 0 ? ((Number)resultado.get(0)).longValue() : null;
	}

}
