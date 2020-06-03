package com.conexia.contratacion.portafolio.services.transactional.prestador.boundary;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.jboss.ejb3.annotation.TransactionTimeout;

import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contratacion.commons.dto.capita.CategoriaMedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.MedicamentoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.capita.MedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ResultadoPortafolioReporteDto;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.MedicamentoPortafolioTransactionalRemote;
import com.conexia.contratacion.portafolio.services.transactional.prestador.controls.MedicamentoPortafolioTransactionalControl;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;

@Stateless
@Remote(MedicamentoPortafolioTransactionalRemote.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class MedicamentoPortafolioTransactionalBoundary implements
		MedicamentoPortafolioTransactionalRemote {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6415069572945316132L;

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;
	
	@Inject
	private Log logger;

	@Inject
	private MedicamentoPortafolioTransactionalControl medicamentoPortafolioTransactional;

	@Override
	public void cambiarEstadoMedicamentosCategoriaPortafolio(
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento,
			Map<String, Object> filters) throws ConexiaBusinessException {

		filters.put("nuevoEstado", categoriaMedicamento.getHabilitado());
		
		StringBuilder queryString = new StringBuilder();
		Map<String, Object> parametros = medicamentoPortafolioTransactional
				.generarUpdateMedicamentosCategoria(queryString,
						filters);

		medicamentoPortafolioTransactional.generarWhereMedicamentosCategoria(
				queryString, categoriaMedicamento, filters, parametros);

		Query query = em.createQuery(queryString.toString());

		for (Entry<String, Object> parametro : parametros.entrySet()) {
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		actualizarCategoriaMedicamento(query.executeUpdate(), categoriaMedicamento);
	}

	private void actualizarCategoriaMedicamento(int registrosActualizados,
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento) {
		
		/*
		 * Solo si modifico por lo menos un registro se actualiza el estado del
		 * servicio
		 */
		if (registrosActualizados > 0) {
			/*
			 * Se valida cuantos medicamentos tiene habilitado la categoria
			 */
			int conteoMedicamentos = em
					.createQuery(
							"SELECT CAST(COUNT(m.id) as integer) "
									+ "FROM MedicamentoPortafolioSede m "
									+ "WHERE m.habilitado IS TRUE "
									+ "AND m.categoriaMedicamentoPortafolio.id = :categoriaMedicamentoId ",
							Integer.class)
					.setParameter("categoriaMedicamentoId",
							categoriaMedicamento.getId())
					.getSingleResult();

			/*
			 * Si el conteo es mayor a cero se habilitara la categoria, por lo
			 * contrario la deshabilitara
			 */
			em.createQuery("  UPDATE CategoriaMedicamentoPortafolioSede c "
							+ "  SET c.habilitado = :habilitado "
							+ "WHERE c.id = :categoriaMedicamentoId")
					.setParameter("categoriaMedicamentoId", categoriaMedicamento.getId())
					.setParameter("habilitado", conteoMedicamentos > 0)
					.executeUpdate();
		}
	}
	

	public void cambiarEstadoCategoriaMedicamentoPortafolio(
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento)
			throws ConexiaBusinessException {

		int actualizados = em
				.createQuery(
						"UPDATE CategoriaMedicamentoPortafolioSede c SET c.habilitado = :habilitado WHERE c.id = :categoriaMedicamentoId")
				.setParameter("categoriaMedicamentoId",
						categoriaMedicamento.getId())
				.setParameter("habilitado",
						categoriaMedicamento.getHabilitado()).executeUpdate();

		if (actualizados > 0) {
			em.createQuery(
					"UPDATE MedicamentoPortafolioSede m SET m.habilitado = :habilitado WHERE m.categoriaMedicamentoPortafolio.id = :categoriaMedicamentoId")
					.setParameter("categoriaMedicamentoId",
							categoriaMedicamento.getId())
					.setParameter("habilitado",
							categoriaMedicamento.getHabilitado())
					.executeUpdate();
		}
	}

	public void cambiarEstadoTodasCategoriasMedicamentoPortafolio(
			PortafolioSedeDto portafolio, Map<String, Object> filters)
			throws ConexiaBusinessException {

		StringBuilder queryString = new StringBuilder();
		Map<String, Object> parametros = medicamentoPortafolioTransactional
				.generarUpdateCategoriaMedicamento(queryString, filters);

		medicamentoPortafolioTransactional.generarWhereCategoriaServicio(
				queryString, portafolio, filters, parametros);

		Query query = em.createQuery(queryString.toString());

		for (Entry<String, Object> parametro : parametros.entrySet()) {
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		if (query.executeUpdate() > 0) {
			queryString = new StringBuilder();
			parametros = medicamentoPortafolioTransactional
					.generarUpdateMedicamentosCategoria(queryString, filters);

			medicamentoPortafolioTransactional
					.generarWhereMedicamentosCategoria(queryString, portafolio,
							filters, parametros);

			query = em.createQuery(queryString.toString());

			for (Entry<String, Object> parametro : parametros.entrySet()) {
				query.setParameter(parametro.getKey(), parametro.getValue());
			}

			query.executeUpdate();
		}

	}

	public void cambiarEstadoMedicamentoPortafolio(
			MedicamentoPortafolioSedeDto medicamentoPortafolio)
			throws ConexiaBusinessException {

		int registrosActualizados = em.createQuery(
				"UPDATE MedicamentoPortafolioSede m SET m.habilitado = :habilitado WHERE m.id = :medicamentoPortafolioId")
				.setParameter("medicamentoPortafolioId",
						medicamentoPortafolio.getId())
				.setParameter("habilitado",
						medicamentoPortafolio.getHabilitado()).executeUpdate();

		actualizarCategoriaMedicamento(registrosActualizados,
				medicamentoPortafolio.getCategoriaMedicamentoPortafolio());

	}

	@Override
	public void eliminarMedicamentoPortafolioByPrestador(Long prestadorId) throws ConexiaBusinessException {
		em.createNamedQuery("MedicamentoPortafolio.eliminarMedicamentoPortafolio")
		.setParameter("prestadorId", prestadorId)
		.executeUpdate();	
	}

	@Override
	@TransactionTimeout(unit = TimeUnit.MINUTES, value = 70)
	public int insertarMedicamentoPortafolio(List<MedicamentoNegociacionDto> listMedicamentoPortafolioPropuesto)throws ConexiaBusinessException {
		Query query = em.createNamedQuery("MedicamentoPortafolio.insertarMedicamentoPortafolio");
		Integer contador = 0;
		listMedicamentoPortafolioPropuesto.forEach(medicamentoPortafolio->{
			try{
				query.setParameter("prestadorId", medicamentoPortafolio.getSedePrestador().getPrestador().getId())
				.setParameter("sedePrestadorId", medicamentoPortafolio.getSedePrestador().getId())
				.setParameter("medicamentoId", medicamentoPortafolio.getMedicamentoDto().getId())
				.setParameter("valorNegociado", medicamentoPortafolio.getValorNegociado())
				.executeUpdate();
			}catch(Exception e){
				logger.error("No se inserto el medicamento portafolio: " 
					 + " prestadorId:"+ medicamentoPortafolio.getSedePrestador().getPrestador().getId()
					 + " sedePrestadorId: "+ medicamentoPortafolio.getSedePrestador().getId() 
					 + " medicamentoId:"+ medicamentoPortafolio.getMedicamentoDto().getId() 
					 + " valorNegociado:" + medicamentoPortafolio.getValorNegociado(),e);				
			}
		});
		return contador;
	}
	
	@Override
	@TransactionTimeout(unit = TimeUnit.MINUTES, value = 70)
	public int insertarMedicamentoPortafolioSede(List<MedicamentoNegociacionDto> listMedicamentoPortafolioPropuesto)throws ConexiaBusinessException {
		Query query = em.createNamedQuery("MedicamentoPortafolioSede.insertarMedicamentoPortafolioSede");
		Integer contador = 0;
		listMedicamentoPortafolioPropuesto.forEach(medicamentoPortafolio->{
			try{
				query.setParameter("prestadorId", medicamentoPortafolio.getSedePrestador().getPrestador().getId())
				.setParameter("sedePrestadorId", medicamentoPortafolio.getSedePrestador().getId())
				.setParameter("medicamentoId", medicamentoPortafolio.getMedicamentoDto().getId())
				.executeUpdate();
			}catch(Exception e){
				logger.error("No se inserto el Medicamento portafolio Sede: " 
					 + " prestadorId:"+ medicamentoPortafolio.getSedePrestador().getPrestador().getId()
					 + " sedePrestadorId: "+ medicamentoPortafolio.getSedePrestador().getId() 
					 + " medicamento_id:" + medicamentoPortafolio.getMedicamentoDto().getId(),e);				
			}
		});
		return contador;
	}

	@Override
	public ResultadoPortafolioReporteDto actualizarMedicamentoPortafolio(MedicamentoPortafolioReporteDto medicamento, Long portafolioId) {
		Query queryObtenerMedicamentoId = em.createNativeQuery("select m.id " + 
				"from maestros.medicamento m " + 
				"where m.codigo = :codigo " + 
				"order by m.id desc limit 1 ");

		queryObtenerMedicamentoId.setParameter("codigo", medicamento.getCodigo());
		if (queryObtenerMedicamentoId.getResultList().size() < 1) {
			return new ResultadoPortafolioReporteDto(medicamento, "Medicamento no encontrado", false);
		} else {
			Integer medicamentoId = ((Number)queryObtenerMedicamentoId.getSingleResult()).intValue();
			Query queryExisteEnPortafolio = em.createNativeQuery("select count(0) from contratacion.medicamento_portafolio mp "
					+ " where mp.portafolio_id = :portafolioId and mp.medicamento_id = :medicamentoId");
			queryExisteEnPortafolio.setParameter("portafolioId", portafolioId);
			queryExisteEnPortafolio.setParameter("medicamentoId", medicamentoId);
			Integer tienePortafolio = ((Number)queryExisteEnPortafolio.getSingleResult()).intValue();

			if (tienePortafolio > 0) {
				
				StringBuilder queryUpdatePortafolioString = new StringBuilder("update contratacion.medicamento_portafolio ")
						.append(" set valor = :valor, codigo_interno = :codigoInterno");
				if ("SI".equalsIgnoreCase(medicamento.getCapitado())) {
					queryUpdatePortafolioString.append(", es_capita = true ");
				} else if ("NO".equalsIgnoreCase(medicamento.getCapitado())) {
					queryUpdatePortafolioString.append(", es_capita = false ");
				} else {
					queryUpdatePortafolioString.append(", es_capita = null ");
				}
				queryUpdatePortafolioString.append("where portafolio_id = :portafolioId and medicamento_id = :medicamentoId");
				Query queryUpdatePortafolio = em.createNativeQuery(queryUpdatePortafolioString.toString()); 
				queryUpdatePortafolio.setParameter("portafolioId", portafolioId);
				queryUpdatePortafolio.setParameter("medicamentoId", medicamentoId);
				queryUpdatePortafolio.setParameter("codigoInterno", medicamento.getCodigoIps());
				queryUpdatePortafolio.setParameter("valor", Double.parseDouble(medicamento.getValor()));
				int result = queryUpdatePortafolio.executeUpdate();
				return new ResultadoPortafolioReporteDto(medicamento, "Resultado " + result, true);
			} else {
				StringBuilder queryInsertPortafolioString = new StringBuilder("insert into contratacion.medicamento_portafolio (portafolio_id, medicamento_id, codigo_interno, valor");
				if ("SI".equalsIgnoreCase(medicamento.getCapitado()) || "NO".equalsIgnoreCase(medicamento.getCapitado())) {
					queryInsertPortafolioString.append(", es_capita");
				} 
				
				queryInsertPortafolioString.append(") values (:portafolioId, :medicamentoId, :codigoInterno, :valor");
				if ("SI".equalsIgnoreCase(medicamento.getCapitado())) {
					queryInsertPortafolioString.append(", true");
				} else if ("NO".equalsIgnoreCase(medicamento.getCapitado())) {
					queryInsertPortafolioString.append(", false");
				}
				queryInsertPortafolioString.append(")");
				Query queryInsertPortafolio = em.createNativeQuery(queryInsertPortafolioString.toString());
				queryInsertPortafolio.setParameter("portafolioId", portafolioId);
				queryInsertPortafolio.setParameter("medicamentoId", medicamentoId);
				queryInsertPortafolio.setParameter("codigoInterno", medicamento.getCodigoIps());
				queryInsertPortafolio.setParameter("valor", Double.parseDouble(medicamento.getValor()));
				int result = queryInsertPortafolio.executeUpdate();
				return new ResultadoPortafolioReporteDto(medicamento, "Resultado " + result, true);
			}
		}
	}
}
