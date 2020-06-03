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

import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;
import com.conexia.contratacion.portafolio.definitions.view.prestador.ServicioHabilitacionPortafolioViewRemote;
import com.conexia.contratacion.portafolio.prestador.control.ServicioHabilitacionPortafolioControl;
import com.conexia.exceptions.ConexiaBusinessException;

@Stateless
@Remote(ServicioHabilitacionPortafolioViewRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ServicioHabilitacionPortafolioViewBoundary implements
		ServicioHabilitacionPortafolioViewRemote {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6013628269805623268L;

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;

	@Inject
	private ServicioHabilitacionPortafolioControl servicioPortafolioControl;

	@Override
	public int contarServiciosHabilitacionPortafolio(
			PortafolioSedeDto portafolio, Map<String, Object> filters)
			throws ConexiaBusinessException {

		StringBuilder queryString = new StringBuilder();
		servicioPortafolioControl.generarContarServiciosPortafolio(queryString,
				portafolio.getId());

		Map<String, Object> parametros = servicioPortafolioControl
				.generarWhereServiciosPortafolio(queryString,
						portafolio, filters);

		Query query = em.createNativeQuery(queryString.toString());

		for (Entry<String, Object> parametro : parametros.entrySet()) {
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		return (Integer) query.getSingleResult();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<ServicioPortafolioSedeDto> listarServiciosHabilitacionPortafolio(
			PortafolioSedeDto portafolio, Integer offset, Integer tamanioPagina,
			Map<String, Object> filters) throws ConexiaBusinessException {

		StringBuilder queryString = new StringBuilder();
		servicioPortafolioControl.generarSelectServiciosPortafolio(queryString,
				portafolio);

		Map<String, Object> parametros = servicioPortafolioControl
				.generarWhereServiciosPortafolio(queryString, portafolio, filters);
		
		servicioPortafolioControl.generarOrderServiciosPortafolio(queryString);
		
		Query query = em.createNativeQuery(queryString.toString(),
				"ServicioPortafolioSedeDtoTotalizado");

		for (Entry<String, Object> parametro : parametros.entrySet()) {
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		return query.setFirstResult(offset).setMaxResults(tamanioPagina)
				.getResultList();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map<String, Boolean> obtenerModalidadServicio(
			ServicioPortafolioSedeDto servicioPortafolioSede)
			throws ConexiaBusinessException {

		TypedQuery<Map> query = em
				.createQuery(
						"SELECT new map(s.extramuralDomiciliario AS extramuralDomiciliario, s.extramuralOtras AS extramuralOtras, s.extramuralUnidadMovil AS extramuralUnidadMovil, s.intramuralAmbulatorio AS intramuralAmbulatorio, s.intramuralHospitalario AS intramuralHospitalario, s.telemedicinaCentroRef AS telemedicinaCentroRef, s.telemedicinaInstitucionRemisora AS telemedicinaInstitucionRemisora) FROM ServicioPortafolioSede s WHERE s.id = :servicioPortafolioSedeId",
						Map.class).setParameter("servicioPortafolioSedeId",
						servicioPortafolioSede.getId());

		return query.getSingleResult();
	}

	@Override
	public String calcularComplejidadServicio(
			ServicioPortafolioSedeDto servicioPortafolioSede)
			throws ConexiaBusinessException {

		return em
				.createQuery(
						"SELECT CASE MAX(ps.complejidad) WHEN 3 THEN 'Alta' WHEN 2 THEN 'Media' WHEN 1 THEN 'Baja' END FROM ProcedimientoServicioPortafolioSede psps JOIN psps.procedimientoServicio ps WHERE psps.servicioPortafolioSede.id = :servicioPortafolioSedeId",
						String.class)
				.setParameter("servicioPortafolioSedeId",
						servicioPortafolioSede.getId()).getSingleResult();
	}
}
