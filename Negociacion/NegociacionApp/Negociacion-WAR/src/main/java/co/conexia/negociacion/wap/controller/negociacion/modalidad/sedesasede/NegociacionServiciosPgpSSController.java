package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.omnifaces.util.Ajax;

import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.CapitulosNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;

import co.conexia.negociacion.wap.controller.configuracion.NegociacionServicioQualifier;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionServiciosSSFacade;

/**
 * Controller que maneja el tab de los servicios para PGP
 * @author icruz
 *
 */
@Named
@ViewScoped
@NegociacionServicioQualifier
public class NegociacionServiciosPgpSSController  extends NegociacionServiciosSSController {

	@Inject
	private Log logger;

	@Inject
	private FacesMessagesUtils facesMessagesUtils;

	@Inject
	@CnxI18n
	transient ResourceBundle resourceBundle;

	@Inject
	private PreContractualExceptionUtils exceptionUtils;

	@Inject
	/** Se injecta el controler padre que contiene todos los tabs de tecnologías y la negociación sacada de sesion. **/
	private TecnologiasSSController tecnologiasController;

	@Inject
	private NegociacionServiciosSSFacade facade;

    @Inject
	private FacesUtils facesUtils;

	/**
	 * Consulta los servicios asociados a la negociacion
	 */
    @PostConstruct
	public void init() {
		try {
			setCapitulosAgregar(new ArrayList<CapituloProcedimientoDto>());
			setCapitulosAgregarSeleccionados(new ArrayList<>());
			validarMostrarPoblacion();
			if (tecnologiasController.getNegociacion() != null) {
				consultarCapitulosNegociacion();
			}
		} catch (Exception e) {
			this.logger.error(
					"Error al postConstruct NegociacionServiciosPgpSSController",
					e);
			this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}

	/**
	 * COnsulta los captulos que estan en negociacion
	 */
	public void consultarCapitulosNegociacion(){
	    try {
	    	List<CapitulosNegociacionDto> capitulosExistentes = new ArrayList<CapitulosNegociacionDto>();
	    	capitulosExistentes = facade.consultarCapitulosNegociacionSedesPgpByNegociacionId(tecnologiasController.getNegociacion());

	    	setCapitulosNegociacion(capitulosExistentes);
			setCapitulosNegociacionOriginal(new ArrayList<CapitulosNegociacionDto>());
			getCapitulosNegociacionOriginal().addAll(getCapitulosNegociacion());

			if(capitulosExistentes.size() == 0) {
	    		facesMessagesUtils.addWarning("El referente seleccionado no tiene procedimientos asociados para la negociación");
	    	}
	    	Ajax.update("tecnologiasSSForm");

		} catch (ConexiaBusinessException e) {
			this.logger.error(
					"Error al consultar capítulos de la negociación",
					e);
		}

	}

    /**
     * Método que redirecciona a la pagina del detalle de procedimientos del
     * capítulo negociado seleccionado.
     * @param servicio
     */
    public void verProcedimientosNegociados(CapitulosNegociacionDto capituloNegociacion){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(NegociacionSessionEnum.CAPITULO_NEGOCIACION.toString(),capituloNegociacion);
        session.setAttribute(NegociacionSessionEnum.PGP.toString(), true);
        this.facesUtils.urlRedirect("/negociacionProcedimiento");
    }
}
