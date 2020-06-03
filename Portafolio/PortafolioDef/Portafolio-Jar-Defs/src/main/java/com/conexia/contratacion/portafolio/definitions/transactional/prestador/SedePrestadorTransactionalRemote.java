package com.conexia.contratacion.portafolio.definitions.transactional.prestador;

import com.conexia.contratacion.commons.dto.DocumentoAdjuntoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.capita.OfertaPrestadorDto;
import com.conexia.exceptions.ConexiaBusinessException;

public interface SedePrestadorTransactionalRemote {
	/**
	 * Permite registrar un nuevo documento para la sede
	 * 
	 * @param adjunto
	 *            - Una instancia de {@link DocumentoAdjuntoDto}
	 * @param sedeId
	 *            - El idientificador unico de sede para la que se creara el
	 *            adjunto
	 * 
	 * @throws ConexiaBusinessException
	 */
	void guardarDocumentoSede(DocumentoAdjuntoDto adjunto, Long sedeId)
			throws ConexiaBusinessException;

	/**
	 * Permite eliminar un documento asociado a la sede con identificador y
	 * nombre original de archivo a los pasados como parametros
	 * 
	 * @param adjunto
	 *            - Una instancia de {@link DocumentoAdjuntoDto}
	 * @param sedeId
	 *            - El idientificador unico de sede para la que se creara el
	 *            adjunto
	 * @throws ConexiaBusinessException
	 */
	public void eliminarDocumentoSede(DocumentoAdjuntoDto adjunto, Long sedeId)
			throws ConexiaBusinessException;

	/**
	 * Actualiza el horario de la sede pasada como parametro
	 * 
	 * @param sedePrestador
	 *            - Una insancia de {@link SedePrestadorDto}
	 */
	public void actualizarHorarioAtencion(SedePrestadorDto sedePrestador)
			throws ConexiaBusinessException;

	/**
	 * Permite eliminar una sede relacionado con el prestador
	 * 
	 * @param sedePrestador
	 *            - Una instancia de {@link SedePrestadorDto}
	 * @throws ConexiaBusinessException
	 */
	public void eliminarSede(Integer ofertaPrestadorId, Long sedePrestadorId)
			throws ConexiaBusinessException;

	public SedePrestadorDto crearSedePrestador(
			OfertaPrestadorDto ofertaPrestadorDto,
			SedePrestadorDto sedePrestadorDto) throws ConexiaBusinessException;

	SedePrestadorDto actualizarSedePrestador(
			SedePrestadorDto sedePrestadorAnterior,
			SedePrestadorDto sedePrestadorActal)
			throws ConexiaBusinessException;
}
