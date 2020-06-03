package com.conexia.contratacion.portafolio.services.transactional.prestador.boundary;

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

import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.ServicioHabilitacionPortafolioTransactionalRemote;
import com.conexia.contratacion.portafolio.services.transactional.prestador.controls.ServicioHabilitacionTransactionalControl;
import com.conexia.exceptions.ConexiaBusinessException;

@Stateless
@Remote(ServicioHabilitacionPortafolioTransactionalRemote.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ServicioHabilitacionPortafolioTransactionalBoundary implements
		ServicioHabilitacionPortafolioTransactionalRemote {

	private static final long serialVersionUID = 1381374563215153975L;

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;

	@Inject
	private ServicioHabilitacionTransactionalControl servicioHabilitacionTransactional;

	public void cambiarEstadoServicioHabilitacion(
			ServicioPortafolioSedeDto servicioModificado)
			throws ConexiaBusinessException {
		try{
			int actualizados = em
					.createQuery(
							"UPDATE ServicioPortafolioSede s SET s.habilitado = :habilitado WHERE s.id = :servicioPortafolioId")
					.setParameter("servicioPortafolioId",
							servicioModificado.getId())
					.setParameter("habilitado", servicioModificado.getHabilitado())
					.executeUpdate();
	
			if (actualizados > 0) {
				em.createQuery(
						"UPDATE ProcedimientoServicioPortafolioSede s SET s.habilitado = :habilitado WHERE s.servicioPortafolioSede.id = :servicioPortafolioId")
						.setParameter("servicioPortafolioId",
								servicioModificado.getId())
						.setParameter("habilitado",
								servicioModificado.getHabilitado()).executeUpdate();
			}
		}catch(Exception e){
			throw e;
		}
	}

	public void cambiarEstadoTodosServicioHabilitacionPortafolio(
			PortafolioSedeDto portafolio, Map<String, Object> filters)
			throws ConexiaBusinessException {

		StringBuilder queryString = new StringBuilder();
		Map<String, Object> parametros = servicioHabilitacionTransactional
				.generarUpdateServicioSalud(queryString, filters);

		servicioHabilitacionTransactional.generarWhereServicioSalud(
				queryString, portafolio, filters, parametros);

		Query query = em.createQuery(queryString.toString());

		for (Entry<String, Object> parametro : parametros.entrySet()) {
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		int actualizados = query.executeUpdate();

		if (actualizados > 0) {
			queryString = new StringBuilder();
			parametros = servicioHabilitacionTransactional
					.generarUpdateProcedimientosServicio(queryString, filters);

			servicioHabilitacionTransactional
					.generarWhereProcedimientosServicio(queryString,
							portafolio, filters, parametros);

			query = em.createQuery(queryString.toString());

			for (Entry<String, Object> parametro : parametros.entrySet()) {
				query.setParameter(parametro.getKey(), parametro.getValue());
			}

			query.executeUpdate();
		}
	}

}
