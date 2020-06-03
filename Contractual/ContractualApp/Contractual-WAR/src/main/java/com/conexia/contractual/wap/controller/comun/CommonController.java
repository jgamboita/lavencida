package com.conexia.contractual.wap.controller.comun;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contractual.wap.facade.parametros.ParametrosFacade;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.RangoPoblacionDto;
import com.conexia.contratacion.commons.dto.maestros.RiaDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;

/**
 * Controlador comun para consultas generales de la aplicacion
 * 
 * @author jjoya
 *
 */
public class CommonController implements Serializable{

    /**
     * 
     */
    private static final long serialVersionUID = 5372428534418793338L;
    
    @Inject
    private ParametrosFacade parametrosFacade;
    
	@Inject
    private FacesUtils faceUtils;

    @Inject
    private Log logger;
    
    @Inject
    private FacesMessagesUtils facesMessagesUtils;
    
    @Inject
    private PreContractualExceptionUtils exceptionUtils;
    
    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;
    
    
    
    
    @Produces
    @Named("listaTiposDocumento")
    @SessionScoped
    public List<TipoIdentificacionDto> getTiposDocumento() {
        return this.parametrosFacade.listarTiposIdentificacion();
    }
    
    @Produces
    @Named("listaRangoPoblacion")
    @SessionScoped
    public List<RangoPoblacionDto> getRangospoblacion() {
        return this.parametrosFacade.listarRangoPoblacion();
    }
    
    @Produces
    @Named("listaRias")
    @SessionScoped
    public List<RiaDto> getRias() {
        return this.parametrosFacade.listarRias();
    }
    
	public PrestadorDto getPrestador(Long prestadorId) {
		PrestadorDto prestador = null;

		try {
			prestador = this.parametrosFacade
					.consultarPrestador(prestadorId);
		} catch (Exception e) {
			this.faceUtils.urlRedirect("/bandejaPrestador");
			logger.error("Error al consultar los datos del prestador.", e);
			facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
		return prestador;
	}
    
}
