package com.conexia.contratacion.portafolio.services.transactional.prestador.boundary;

import java.io.Serializable;
import java.text.SimpleDateFormat;
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

import com.conexia.contractual.model.contratacion.DocumentacionSede;
import com.conexia.contractual.model.contratacion.HorarioAtencion;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.model.contratacion.SedePrestador;
import com.conexia.contractual.model.maestros.Municipio;
import com.conexia.contractual.model.maestros.Zona;
import com.conexia.contractual.model.modalidad.OfertaPrestador;
import com.conexia.contractual.model.modalidad.OfertaSedePrestador;
import com.conexia.contractual.model.modalidad.PortafolioSede;
import com.conexia.contratacion.commons.dto.DocumentoAdjuntoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.HorarioAtencionDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.capita.OfertaPrestadorDto;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.SedePrestadorTransactionalRemote;
import com.conexia.contratacion.portafolio.services.transactional.prestador.controls.SedePrestadorTransactionalControl;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 *
 * @author <a href="dgarcia@conexia.com">David Garcia H</a>
 */
@Stateless
@Remote(SedePrestadorTransactionalRemote.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class SedePrestadorTransactionalBoundary implements Serializable,
		SedePrestadorTransactionalRemote {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8885643536216415506L;

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;
	
	@Inject
	private SedePrestadorTransactionalControl sedePrestadorTransactionalControl;

	/**
	 * @see {@link SedePrestadorTransactionalRemote#guardarDocumentoSede(DocumentoAdjuntoDto, Long)}
	 */
	public void guardarDocumentoSede(DocumentoAdjuntoDto adjunto, Long sedeId)
			throws ConexiaBusinessException {

		DocumentacionSede documentacionSede = new DocumentacionSede();
		documentacionSede.setNombreArchivo(adjunto.getNombreOriginal());
		documentacionSede.setNombreArchivoServidor(adjunto
				.getNombreRepositorio());
		documentacionSede
				.setTipoArchivo(adjunto.getTipoAdjunto() != null ? adjunto
						.getTipoAdjunto().getDescripcion() : null);
		documentacionSede.setSede(new SedePrestador(sedeId));

		em.persist(documentacionSede);
	}

	/**
	 * @see {@link SedePrestadorTransactionalRemote#eliminarDocumentoSede(DocumentoAdjuntoDto, Long)}
	 */
	public void eliminarDocumentoSede(DocumentoAdjuntoDto adjunto, Long sedeId)
			throws ConexiaBusinessException {

		em.createQuery(
				"DELETE FROM DocumentacionSede d WHERE d.sede.id = :sedeId AND d.nombreArchivo = :nombreArchivo")
				.setParameter("sedeId", sedeId)
				.setParameter("nombreArchivo", adjunto.getNombreOriginal())
				.executeUpdate();
	}

	public void actualizarHorarioAtencion(SedePrestadorDto sedePrestador)
			throws ConexiaBusinessException {

		HorarioAtencionDto horarioAtencionDto = sedePrestador
				.getHorarioAtencion();

		HorarioAtencion horarioAtencion = null;
		if (horarioAtencionDto.getId() != null) {
			horarioAtencion = em.getReference(HorarioAtencion.class,
					horarioAtencionDto.getId());
		}

		if (horarioAtencion == null) {
			horarioAtencion = new HorarioAtencion();
			em.persist(horarioAtencion);

			em.createQuery(
					"UPDATE SedePrestador s SET s.horarioAtencion.id = :horarioAtencion WHERE s.id = :sedeId")
					.setParameter("sedeId", sedePrestador.getId())
					.setParameter("horarioAtencion", horarioAtencion.getId())
					.executeUpdate();

		}

		SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

		if (horarioAtencionDto.getLunesFinal() != null) {
			horarioAtencion.setLunesInicial(timeFormat
					.format(horarioAtencionDto.getLunesInicial()));

			horarioAtencion.setLunesFinal(timeFormat.format(horarioAtencionDto
					.getLunesFinal()));
		}

		if (horarioAtencionDto.getMartesFinal() != null) {
			horarioAtencion.setMartesInicial(timeFormat
					.format(horarioAtencionDto.getMartesInicial()));

			horarioAtencion.setMartesFinal(timeFormat.format(horarioAtencionDto
					.getMartesFinal()));
		}

		if (horarioAtencionDto.getMiercolesFinal() != null) {
			horarioAtencion.setMiercolesInicial(timeFormat
					.format(horarioAtencionDto.getMiercolesInicial()));

			horarioAtencion.setMiercolesFinal(timeFormat
					.format(horarioAtencionDto.getMiercolesFinal()));
		}

		if (horarioAtencionDto.getJuevesFinal() != null) {
			horarioAtencion.setJuevesInicial(timeFormat
					.format(horarioAtencionDto.getJuevesInicial()));

			horarioAtencion.setJuevesFinal(timeFormat.format(horarioAtencionDto
					.getJuevesFinal()));
		}

		if (horarioAtencionDto.getViernesFinal() != null) {
			horarioAtencion.setViernesInicial(timeFormat
					.format(horarioAtencionDto.getViernesInicial()));

			horarioAtencion.setViernesFinal(timeFormat
					.format(horarioAtencionDto.getViernesFinal()));
		}

		if (horarioAtencionDto.getSabadoFinal() != null) {
			horarioAtencion.setSabadoInicial(timeFormat
					.format(horarioAtencionDto.getSabadoInicial()));

			horarioAtencion.setSabadoFinal(timeFormat.format(horarioAtencionDto
					.getSabadoFinal()));
		}

		if (horarioAtencionDto.getDomingoFinal() != null) {
			horarioAtencion.setDomingoInicial(timeFormat
					.format(horarioAtencionDto.getDomingoInicial()));

			horarioAtencion.setDomingoFinal(timeFormat
					.format(horarioAtencionDto.getDomingoFinal()));
		}

		if (horarioAtencionDto.getFestivoFinal() != null) {
			horarioAtencion.setFestivoInicial(timeFormat
					.format(horarioAtencionDto.getFestivoInicial()));

			horarioAtencion.setFestivoFinal(timeFormat
					.format(horarioAtencionDto.getFestivoFinal()));
		}
	}

	/**
	 * @see {@link SedePrestadorTransactionalRemote#persistirSedePrestador(SedePrestadorDto)}
	 */
	@Override
	public SedePrestadorDto actualizarSedePrestador(			
			SedePrestadorDto sedePrestadorAnterior,
			SedePrestadorDto sedePrestadorNueva)
			throws ConexiaBusinessException {

		StringBuilder queryString = new StringBuilder();
		Map<String, Object> parametros = sedePrestadorTransactionalControl
				.generarUpdateSedePrestador(queryString, sedePrestadorAnterior,
						sedePrestadorNueva);
		
		sedePrestadorTransactionalControl.generarWhereSedePrestador(
				queryString, sedePrestadorAnterior, parametros);
		
		Query query = em.createQuery(queryString.toString());		
		for(Entry<String, Object> parametro : parametros.entrySet()){
			query.setParameter(parametro.getKey(), parametro.getValue());
		}
		
		query.executeUpdate();
		
		return sedePrestadorNueva;
	}
	
	@Override
	public SedePrestadorDto crearSedePrestador(
			OfertaPrestadorDto ofertaPrestadorDto,
			SedePrestadorDto sedePrestadorDto) throws ConexiaBusinessException{
		
		SedePrestador sedePrestador = new SedePrestador();
		/*EstadoServicioPortafolioEnum.PENDIENTE_APROBACION*/
		sedePrestador.setEnumStatus(2);
		sedePrestador.setPrestador(em.getReference(Prestador.class,
				sedePrestadorDto.getPrestador().getId()));
	
		
		sedePrestador.setNombreSede(sedePrestadorDto.getNombreSede());
		sedePrestador.setSedePrincipal(sedePrestadorDto.getSedePrincipal());
		sedePrestador.setCodigoPrestador(sedePrestadorDto.getCodigoPrestador());
		sedePrestador.setCodigoSede(sedePrestadorDto.getCodigoSede());
		sedePrestador.setBarrio(sedePrestadorDto.getBarrio());
		sedePrestador.setDireccion(sedePrestadorDto.getDireccion());
		sedePrestador.setTelefonoCitas(sedePrestadorDto.getTelefonoCitas());
		sedePrestador.setFax(sedePrestadorDto.getFax());
		sedePrestador.setTelefonoAdministrativo(sedePrestadorDto
				.getTelefonoAdministrativo());
		sedePrestador.setCorreo(sedePrestadorDto.getCorreo());
		sedePrestador.setGerente(sedePrestadorDto.getGerente());

		if (sedePrestadorDto.getMunicipio() != null) {
			sedePrestador.setMunicipio(new Municipio());
			sedePrestador.getMunicipio().setId(
					sedePrestadorDto.getMunicipio().getId());
		}

		if (sedePrestadorDto.getZonaId() != null) {
			sedePrestador.setZona(new Zona());
			sedePrestador.getZona().setId(sedePrestadorDto.getZonaId());
		}
		
		/* Se crea el nuevo registro de la sede */
		em.persist(sedePrestador);
		sedePrestadorDto.setId(sedePrestador.getId());
		
		/* Se crea el portafolio por defecto */
		PortafolioSede portafolio = new PortafolioSede();
		portafolio.setPrestador(sedePrestador.getPrestador());
		em.persist(portafolio);
		
		/* Se asocian la sede y el portafolio, en la oferta */		
		OfertaPrestador oferta = new OfertaPrestador();
		oferta.setId(ofertaPrestadorDto.getId());
		
		OfertaSedePrestador ofertaSedePrestador = new OfertaSedePrestador();
		ofertaSedePrestador.setSedePrestador(sedePrestador);
		ofertaSedePrestador.setOfertaPrestador(oferta);
		ofertaSedePrestador.setPortafolio(portafolio);
		em.persist(ofertaSedePrestador);
		
		return sedePrestadorDto;
	}
	
	@Override
	public void eliminarSede(Integer ofertaPrestadorId, Long sedePrestadorId)
			throws ConexiaBusinessException {
		
		em.createQuery("DELETE FROM OfertaSedePrestador o "
					  + "WHERE o.sedePrestador.id = :sedePrestadorId "
					  + "  AND o.ofertaPrestador.id = :ofertaPrestadorId")
			.setParameter("sedePrestadorId", sedePrestadorId)
			.setParameter("ofertaPrestadorId", ofertaPrestadorId)
			.executeUpdate();
	}
}