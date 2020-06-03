package com.conexia.contratacion.portafolio.wap.facade.basico;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contratacion.commons.constants.enums.TipoDocumentoEnum;
import com.conexia.contratacion.commons.dto.DocumentoAdjuntoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.HorarioAtencionDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.capita.OfertaPrestadorDto;
import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.PrestadorTransactionalServiceRemote;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.SedePrestadorTransactionalRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.PrestadorViewServiceRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.SedePrestadorViewRemote;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.servicefactory.CnxService;

public class SedePrestadorFacade implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5084749152689828840L;

	@Inject
	@CnxService
	private SedePrestadorViewRemote sedePrestadorView;

	@Inject
	@CnxService
	private SedePrestadorTransactionalRemote sedePrestadorTransactional;

	@Inject
	@CnxService
	private PrestadorViewServiceRemote prestadorView;

	@Inject
	@CnxService
	private PrestadorTransactionalServiceRemote prestadorTransactional;

	/**
	 * Obtiene las sedes del prestador con identificador igual al pasado como
	 * parametro
	 * 
	 * @param prestadorId
	 *            - El identificador unico de prestador
	 * @param pageSize
	 * @param first
	 * @return Una lista con instancias de {@link SedePrestadorDto} o vacia
	 */
	public List<SedePrestadorDto> listarSedesPorPrestadorId(Long prestadorId,
			int offset, int tamanioPagina) throws ConexiaBusinessException {

		return sedePrestadorView.listarSedesPorPrestadorUsuarioId(prestadorId,
				offset, tamanioPagina);
	}

	public Integer contarSedesPorPrestadorId(Long prestadorId)
			throws ConexiaBusinessException {
		return sedePrestadorView.contarSedesPorPrestadorId(prestadorId);
	}

	/**
	 * Obtiene la informacion del prestador al usuario identificado con el id
	 * pasado como parametro
	 * 
	 * @param usuarioId
	 *            - El identificador unico de usuario relacionado con el
	 *            prestador
	 * 
	 * @return Una instancia de {@link PrestadorDto} con la informacion obtenida
	 *         del modelo o vacia si no existe el prestador
	 *
	 * @throws ConexiaBusinessException
	 */
	public PrestadorDto obtenerPrestadorPorUsuarioId(Integer usuarioId)
			throws ConexiaBusinessException {

		return prestadorView.obtenerPorUsuarioId(usuarioId);
	}

	/**
	 * Permite obtener los documentos adjuntados por para la sede con id unico
	 * igual al pasado como parametro
	 * 
	 * @param long1
	 *            - Identificador unico de sede
	 * @param tipoDocumento
	 *            - El tipo de documento que se desea recuperar para la sede
	 * 
	 * @return Una lista de instancias de {@link DocumentoAdjuntoDto}
	 * @throws ConexiaBusinessException
	 */
	public List<DocumentoAdjuntoDto> obtenerDocumentosSedeId(Long sedeId,
			TipoDocumentoEnum tipoDocumento) throws ConexiaBusinessException {
		return sedePrestadorView.obtenerDocumentosSedeId(sedeId, tipoDocumento);
	}

	public void guardarDocumentoSede(DocumentoAdjuntoDto adjunto, Long sedeId)
			throws ConexiaBusinessException {
		sedePrestadorTransactional.guardarDocumentoSede(adjunto, sedeId);
	}

	public void eliminarDocumentoSede(DocumentoAdjuntoDto adjunto, Long sedeId)
			throws ConexiaBusinessException {
		sedePrestadorTransactional.eliminarDocumentoSede(adjunto, sedeId);
	}

	public HorarioAtencionDto obtenerHorarioSedeId(Long sedeId)
			throws ConexiaBusinessException {
		return sedePrestadorView.obtenerHorarioSedeId(sedeId);
	}

	public void actualizarHorarioAtencion(SedePrestadorDto sedePrestador)
			throws ConexiaBusinessException {
		sedePrestadorTransactional.actualizarHorarioAtencion(sedePrestador);
	}

	public SedePrestadorDto crearSedePretador(
			OfertaPrestadorDto ofertaPrestador, SedePrestadorDto sedePrestador)
			throws ConexiaBusinessException {

		return sedePrestadorTransactional.crearSedePrestador(ofertaPrestador,
				sedePrestador);
	}

	public void eliminarSede(Integer ofertaPrestadorId, Long sedePrestadorId)
			throws ConexiaBusinessException {

		sedePrestadorTransactional.eliminarSede(ofertaPrestadorId,
				sedePrestadorId);
	}

	public OfertaPrestadorDto obtenerOfertaPresentarPorPrestadorIdYModalidadId(
			Long prestadorId, Integer modalidadId)
			throws ConexiaBusinessException {

		return prestadorView.obtenerOfertaPresentarPorPrestadorIdYModalidadId(
				prestadorId, modalidadId);
	}

	public PortafolioSedeDto obtenerPortafolioPorOfertaIdYSedeId(
			Integer ofertaPrestadorId, Long sedePrestadorId)
			throws ConexiaBusinessException {

		return sedePrestadorView.obtenerPortafolioPorOfertaIdYSedeId(
				ofertaPrestadorId, sedePrestadorId);
	}

	public boolean modificarOferta(OfertaPrestadorDto oferta)
			throws ConexiaBusinessException {
		return prestadorTransactional.modificarOferta(oferta);
	}

	public SedePrestadorDto actualizarSedePretador(
			SedePrestadorDto sedePrestadorAnterior,
			SedePrestadorDto sedePrestadorNueva)
			throws ConexiaBusinessException {

		return sedePrestadorTransactional.actualizarSedePrestador(
				sedePrestadorAnterior, sedePrestadorNueva);
	}

}
