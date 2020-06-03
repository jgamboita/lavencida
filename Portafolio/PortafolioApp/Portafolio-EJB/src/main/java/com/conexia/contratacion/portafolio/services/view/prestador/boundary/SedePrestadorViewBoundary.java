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
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TipoDocumentoEnum;
import com.conexia.contratacion.commons.dto.DocumentoAdjuntoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.HorarioAtencionDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.contratacion.portafolio.definitions.view.prestador.SedePrestadorViewRemote;
import com.conexia.contratacion.portafolio.prestador.control.SedePrestadorControl;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 *
 * @author <a href="dgarcia@conexia.com">David Garcia H</a>
 */
@Stateless
@Remote(SedePrestadorViewRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class SedePrestadorViewBoundary implements SedePrestadorViewRemote {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2414269468468219089L;

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;

	@Inject
	private SedePrestadorControl sedePrestadorControl;

	/**
	 * @see SedePrestadorViewRemote#listarSedesPorPrestadorUsuarioId(Integer)
	 */
	public List<SedePrestadorDto> listarSedesPorPrestadorUsuarioId(
			Long prestadorId, int offset, int tamanioPagina)
			throws ConexiaBusinessException {

		StringBuilder queryString = new StringBuilder();
		sedePrestadorControl.generarSelectSedesOferta(queryString);

		Map<String, Object> parametros = sedePrestadorControl
				.generarWhereSedesOferta(queryString, prestadorId,
						NegociacionModalidadEnum.CAPITA.getId());

		TypedQuery<SedePrestadorDto> query = em.createQuery(
				queryString.toString(), SedePrestadorDto.class);

		for (Entry<String, Object> parametro : parametros.entrySet()) {
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		return query.setFirstResult(offset).setMaxResults(tamanioPagina)
				.getResultList();
	}

	/**
	 * @see {@link SedePrestadorViewRemote#contarSedesPorPrestadorId(Long)}
	 */
	public Integer contarSedesPorPrestadorId(Long prestadorId)
			throws ConexiaBusinessException {

		StringBuilder queryString = new StringBuilder();
		sedePrestadorControl.generarContarSedesOferta(queryString);

		Map<String, Object> parametros = sedePrestadorControl
				.generarWhereSedesOferta(queryString, prestadorId,
						NegociacionModalidadEnum.CAPITA.getId());

		TypedQuery<Integer> query = em.createQuery(queryString.toString(),
				Integer.class);

		for (Entry<String, Object> parametro : parametros.entrySet()) {
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		return query.getSingleResult();
	}

	/**
	 * @see SedePrestadorViewRemote#obtenerDocumentosSedeId(String)
	 */
	public List<DocumentoAdjuntoDto> obtenerDocumentosSedeId(Long sedeId,
			TipoDocumentoEnum tipoDocumento) throws ConexiaBusinessException {

		StringBuilder queryString = new StringBuilder();
		sedePrestadorControl.generarSelectDocumentos(queryString);
		Map<String, Object> parametros = sedePrestadorControl
				.generarWhereDocumentos(queryString, tipoDocumento, sedeId);

		TypedQuery<DocumentoAdjuntoDto> query = em.createQuery(
				queryString.toString(), DocumentoAdjuntoDto.class);

		for (Entry<String, Object> parametro : parametros.entrySet()) {
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		return query.getResultList();
	}

	/**
	 * @see {@link SedePrestadorViewRemote#obtenerHorarioSedeId(Long)}
	 */
	public HorarioAtencionDto obtenerHorarioSedeId(Long sedeId)
			throws ConexiaBusinessException {

		try {
			return em
					.createQuery(
							"SELECT new com.conexia.contratacion.commons.dto.contractual.parametrizacion.HorarioAtencionDto("
									+ "h.id, h.lunesInicial, h.lunesFinal, h.martesInicial, h.martesFinal, h.miercolesInicial, "
									+ "h.miercolesFinal, h.juevesInicial, h.juevesFinal, h.viernesInicial, h.viernesFinal, "
									+ "h.sabadoInicial, h.sabadoFinal, h.domingoInicial, h.domingoFinal, h.festivoInicial, h.festivoFinal) "
									+ "FROM SedePrestador s JOIN s.horarioAtencion h WHERE s.id = :sedeId",
							HorarioAtencionDto.class)
					.setParameter("sedeId", sedeId).getSingleResult();
		} catch (NoResultException e) {
			return new HorarioAtencionDto();
		}
	}

	@Override
	public PortafolioSedeDto obtenerPortafolioPorOfertaIdYSedeId(
			Integer ofertaPrestadorId, Long sedePrestadorId)
			throws ConexiaBusinessException {

		return em.createNamedQuery(
						"PortafolioSede.obtenerPortafolioPorOfertaIdYSedeId",
						PortafolioSedeDto.class)
				.setParameter("ofertaPrestadorId", ofertaPrestadorId)
				.setParameter("sedePrestadorId", sedePrestadorId)
				.getSingleResult();
	}
}
