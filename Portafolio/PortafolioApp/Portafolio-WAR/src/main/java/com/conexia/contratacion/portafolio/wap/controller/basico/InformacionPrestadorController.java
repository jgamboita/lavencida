package com.conexia.contratacion.portafolio.wap.controller.basico;

import java.io.IOException;
import java.io.Serializable;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.portafolio.wap.facade.basico.InformacionPrestadorFacade;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 * Controlador que maneja la pantalla de cargue de los datos del prestador
 * @author mcastro
 *
 */
@ViewScoped
@Named("informacionPrestador")
@URLMapping(id = "formularioInformacionPrestador", pattern = "/gestion/informacionprestador", viewId = "/basico/formularioInformacionPrestador.page")
public class InformacionPrestadorController implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5821142040501439547L;

	@Inject
	private Log logger;

	@Inject
	private FacesMessagesUtils facesMessagesUtils;

	@Inject
	@CnxI18n
	transient ResourceBundle resourceBundle;

	@Inject
	private InformacionPrestadorFacade informacionPrestadorFacade;
	
	@Inject
    @UserInfo
    private UserApp usuario;
	
    private PrestadorDto prestador;
 

	/**
	 * Metodo postConstruct
	 */
	@PostConstruct
	public void postConstruct(){
		
		try {
			prestador = informacionPrestadorFacade.prestadorPorUsuarioId(689/*usuario.getId()*/);
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils.addError(
					"Se ha presentado un error al cargar la informaci\u00f3n.");
		}
		
	}
    
	/* Acciones */
	
    public void verPortafolioCapita(){
        try{
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext context = facesContext.getExternalContext();
            context.redirect("/wap/portafolio/basico/datosPortafolio.page");
            facesContext.responseComplete();
        }
        catch (IOException e){
            logger.error("Error al redireccionar al portafolio capita.", e);
            facesMessagesUtils.addError(resourceBundle.getString("error_redirec_portafolio_capita"));
        }
                
    }
    
    /* Getter & Setter */

	public PrestadorDto getPrestador() {
		return prestador;
	}

	public void setPrestador(PrestadorDto prestador) {
		this.prestador = prestador;
	}    
      
}
