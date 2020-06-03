package com.conexia.contratacion.portafolio.wap.controller.commons;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.cdi.ViewScoped;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.conexia.contratacion.commons.constants.enums.TipoDocumentoEnum;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.DescriptivoDto;
import com.conexia.contratacion.commons.dto.DocumentoAdjuntoDto;
import com.conexia.contratacion.portafolio.wap.facade.commons.ArchivosAdjuntosFacade;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.webutils.FacesMessagesUtils;

@Named
@ViewScoped
public class ArchivosAdjuntosController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7436393747815632058L;
	private static final Logger logger = LoggerFactory.getLogger(ArchivosAdjuntosController.class);
	
	@Inject
	private FacesMessagesUtils messagesUtils;

	@Inject
	private ArchivosAdjuntosFacade adjuntosFacade;

	private List<DocumentoAdjuntoDto> documentoAdjunto = new ArrayList<DocumentoAdjuntoDto>();

	private TipoDocumentoEnum tipoDocumentoEnum;

	private StreamedContent adjuntoActual;

	private DocumentoAdjuntoDto adjunto;

	private UploadedFile uploadedFile;

	public void prepararDescarga(DocumentoAdjuntoDto adjunto) {
		try {
			adjunto = adjuntosFacade.obtenerDocumentoAdjunto(adjunto);
			adjuntoActual = new DefaultStreamedContent(
					new ByteArrayInputStream(adjunto.getBytesStream()),
					adjunto.getTipoMime(), adjunto.getNombreOriginal());

		} catch (ConexiaBusinessException e) {
			messagesUtils.addError(e.getMessage());
			e.printStackTrace();
		} catch (ConexiaSystemException e) {
			messagesUtils.addError(e.getMessage());
			e.printStackTrace();
		}
	}

	public StreamedContent getDescarga() {
		return adjuntoActual;
	}

	public void subirArchivo(FileUploadEvent event) {
		try {
			DocumentoAdjuntoDto adjunto = crearAdjunto(event.getFile());

			adjunto.setTipoAdjunto(new DescriptivoDto(
					tipoDocumentoEnum.getId(), tipoDocumentoEnum
							.getDescripcion()));

			documentoAdjunto.add(adjunto);

		} catch (ConexiaSystemException e) {
			messagesUtils.addError(e.getMessage());
			e.printStackTrace();
		}
	}

	public void quitarAdjunto() {
		documentoAdjunto.remove(adjunto);
	}

	/**
	 * Getter de lista para adjuntos de fase .
	 *
	 * @return la lista para adjuntos de fase.
	 */
	public List<DocumentoAdjuntoDto> getDocumentoAdjunto() {
		return documentoAdjunto;
	}

	/**
	 * Setter de la lista para adjuntos de fase a establecer.
	 *
	 * @param DocumentoAdjuntoDtos
	 *            la lista para adjuntos de fase a establecer.
	 */
	public void setDocumentoAdjunto(
			List<DocumentoAdjuntoDto> DocumentoAdjuntoDtos) {
		this.documentoAdjunto = DocumentoAdjuntoDtos;
	}

	/**
	 * Getter de tipo de adjunto.
	 *
	 * @return el tipo de adjunto.
	 */
	public TipoDocumentoEnum getTipoDocumentoEnum() {
		return tipoDocumentoEnum;
	}

	/**
	 * Setter de tipo de adjunto a establecer.
	 *
	 * @param tipoDocumentoEnum
	 *            el tipo de adjunto a establecer.
	 */
	public void setTipoDocumentoEnum(TipoDocumentoEnum tipoDocumentoEnum) {
		this.tipoDocumentoEnum = tipoDocumentoEnum;
	}

	/**
	 * Getter de adjunto.
	 *
	 * @return el adjunto.
	 */
	public DocumentoAdjuntoDto getAdjunto() {
		return adjunto;
	}

	/**
	 * Setter de adjunto a establecer.
	 *
	 * @param adjunto
	 *            el adjunto a establecer.
	 */
	public void setAdjunto(DocumentoAdjuntoDto adjunto) {
		this.adjunto = adjunto;
	}

	/**
	 * Getter de uploadedFile.
	 *
	 * @return el uploadedFile.
	 */
	public UploadedFile getUploadedFile() {
		return uploadedFile;
	}

	/**
	 * Setter de uploadedFile a establecer.
	 *
	 * @param uploadedFile
	 *            el uploadedFile a establecer.
	 */
	public void setUploadedFile(UploadedFile uploadedFile) {
		this.uploadedFile = uploadedFile;
	}

	
	/* Utilitarias */
	
	/**
	 * Obtiene el archivo cargado, y lo convierte en un objeto de negocio
	 * 
	 * @param archivo
	 *            - una instancia de {@link UploadedFile}
	 * @return Una instancia de {@link AdjuntoTutelaDto} que representa el
	 *         archivo cargado
	 */
	public DocumentoAdjuntoDto crearAdjunto(UploadedFile archivo) {
		try {
			InputStream is = archivo.getInputstream();
			byte[] bytesStream = new byte[is.available()];
			
			is.read(bytesStream);
			
			DocumentoAdjuntoDto adjunto = new DocumentoAdjuntoDto();
			adjunto.setNombreOriginal(archivo.getFileName());
			adjunto.setBytesStream(bytesStream);
			adjunto.setTipoMime(archivo.getContentType());
			
			return adjunto;
		} catch (IOException e){
			logger.error("No se pudo leer el archivo del servidor.", e);
			throw new ConexiaSystemException(
					PreContractualMensajeErrorEnum.SYSTEM_ERROR,
					"No se pudo leer el archivo del servidor.");
		}
	}
}
