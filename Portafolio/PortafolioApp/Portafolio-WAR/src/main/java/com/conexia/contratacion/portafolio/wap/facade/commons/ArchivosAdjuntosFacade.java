package com.conexia.contratacion.portafolio.wap.facade.commons;

import java.io.Serializable;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.DocumentoAdjuntoDto;
import com.conexia.contratacion.portafolio.definitions.commons.CommonViewServiceRemote;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.servicefactory.CnxService;

public class ArchivosAdjuntosFacade implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5954892281796254954L;

	@Inject
	@CnxService
	private CommonViewServiceRemote commonView;

	public DocumentoAdjuntoDto obtenerDocumentoAdjunto(DocumentoAdjuntoDto adjunto)
			throws ConexiaBusinessException {
		
		return commonView.obtenerDocumentoAdjunto(adjunto);
	}

	public DocumentoAdjuntoDto crearAdjunto(DocumentoAdjuntoDto adjunto)
			throws ConexiaBusinessException {
		
		return commonView.crearAdjunto(adjunto);
	}

	public void eliminarAdjunto(DocumentoAdjuntoDto adjunto)
			throws ConexiaBusinessException {
		
		commonView.eliminarAdjunto(adjunto);
	}

}
