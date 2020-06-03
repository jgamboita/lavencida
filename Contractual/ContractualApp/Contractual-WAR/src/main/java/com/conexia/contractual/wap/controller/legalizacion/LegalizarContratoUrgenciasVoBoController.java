package com.conexia.contractual.wap.controller.legalizacion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;

import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NivelContratoEnum;
import com.conexia.contratacion.commons.constants.enums.TypeUserContractUrgencyEnum;
import com.conexia.contractual.utils.DateUtils;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.wap.controller.parametrizacion.ParametrizacionAsincronaRunnable;
import com.conexia.contractual.wap.facade.legalizacion.ContratoUrgenciasFacade;
import com.conexia.contractual.wap.facade.legalizacion.ContratoUrgenciasVoBoFacade;
import com.conexia.contractual.wap.facade.legalizacion.LegalizacionFacade;
import com.conexia.contractual.wap.facade.parametros.ParametrosFacade;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoUrgenciasDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ResponsableContratoDto;
import com.conexia.contratacion.commons.dto.maestros.RegionalDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import com.ocpsoft.pretty.faces.annotation.URLMappings;

@Named
@ViewScoped
@URLMappings(mappings = {
		@URLMapping(id = "generarVistoBuenoContratoUrgencias", pattern = "/contratourgencias/legalizacion/generarVistoBueno/#{legalizarContratoUrgenciasVoBoController.idContratoUrgencias}/", viewId = "/contratourgencias/legalizacion/generarVistoBueno.page")
})
public class LegalizarContratoUrgenciasVoBoController implements Serializable{

	private static final long serialVersionUID = 1L;

	private Long idContratoUrgencias;

    @Inject
    private FacesUtils facesUtils;

    @Inject
    private FacesContext context;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    @Inject
    private ParametrosFacade parametrosFacade;

    @Inject
    @UserInfo
    private UserApp user;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private DateUtils dateUtils;

    private LegalizacionContratoUrgenciasDto legalizarContratoUrgenciasVoBo;

	@Inject
	private ContratoUrgenciasVoBoFacade contratoUrgenciasVoBoFacade;

    private List<RegionalDto> regionales;

    /**
     * Responsables contrato Vo Bo.
     */
    private List<ResponsableContratoDto> responsablesContratoVoBo = new ArrayList<>();

    @Inject
    private LegalizacionFacade legalizacionFacade;

    @Inject
    private ContratoUrgenciasFacade contratoUrgenciasFacade;

    @Inject
    private ContratoUrgenciasVoBoController contratoUrgenciasVoBo;

    private Boolean renderUserGR;

    @Inject
    private Log logger;


	public void loadIni() {
		try {
			if (context.getPartialViewContext().isAjaxRequest()) {
				return;
			};

			this.consultarContratoUrgenciasByIdContrato();
			this.buscarResponsables();
			this.validateUserGR();
		} catch (final Exception e) {
			this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}

    public void limpiar() {
        facesUtils.urlRedirect("/contratourgencias/legalizacion/generarVistoBueno/" + idContratoUrgencias + "/");
    }

    public void regresar() {
        facesUtils.urlRedirect("/contratourgencias/legalizacion/consultaContratosUrgenciasParaVistoBueno");
    }

    public void buscarResponsables() {
        //this.setRegionales(this.parametrosFacade.listarRegionalesUsuario(user.getRegionales()));
        this.setRegionales(this.parametrosFacade.listarRegionales());
        final Integer regionalId = this.legalizarContratoUrgenciasVoBo.getContratoUrgencias().getRegionalDto().getId();
        this.responsablesContratoVoBo = this.legalizacionFacade.listarResponsablesVoBo(regionalId);
    }

    private void consultarContratoUrgenciasByIdContrato(){
    	this.setLegalizarContratoUrgenciasVoBo(this.contratoUrgenciasVoBoFacade.consultarContratoUrgenciasByIdContrato(idContratoUrgencias));
    	this.calculaDuracionContrato();
    }

    public void calculaDuracionContrato() {
		if (getLegalizarContratoUrgenciasVoBo().getContratoUrgencias().getFechaFinVigencia() != null
				&& getLegalizarContratoUrgenciasVoBo().getContratoUrgencias().getFechaInicioVigencia() != null) {
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(getLegalizarContratoUrgenciasVoBo().getContratoUrgencias().getFechaFinVigencia());
			calendar.set(Calendar.HOUR,24);
			getLegalizarContratoUrgenciasVoBo().getContratoUrgencias()
					.setDuracionContrato(dateUtils.calcularFechaLetras(
							getLegalizarContratoUrgenciasVoBo().getContratoUrgencias().getFechaInicioVigencia(),
							calendar.getTime()));
		}
    }

    public void asignarFirmaVoBo(){
        FacesContext context = FacesContext.getCurrentInstance();

        if (Objects.nonNull(this.legalizarContratoUrgenciasVoBo)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se asigno "
                      + "correctamente la firma del visto bueno al contrato.", ""));
            context.getExternalContext().getFlash().setKeepMessages(true);
            this.contratoUrgenciasVoBoFacade.asignarFirmaVoBoContratoUrgencias(this.legalizarContratoUrgenciasVoBo);
            this.getLegalizarContratoUrgenciasVoBo().setEstadoLegalizacion(EstadoLegalizacionEnum.LEGALIZADA);
            this.contratoUrgenciasFacade.guardarHistorialContrato(user.getId(), legalizarContratoUrgenciasVoBo.getContratoUrgencias().getId(), "LEGALIZAR");
        } else {
            RequestContext.getCurrentInstance().execute("PF('errorsDialog').show()");
            Ajax.update("validacionesLegalizacionForm");
        }
    }

    public void validateUserGR() {
    	try {
    		if(this.contratoUrgenciasVoBo.getTypeUserCode().equalsIgnoreCase(TypeUserContractUrgencyEnum.GERENCIA_REGIONAL.getCode())
    			&& this.legalizarContratoUrgenciasVoBo.getContratoUrgencias().getPrestador().getPrestadorRed().equalsIgnoreCase("RED")
    			&& !(this.legalizarContratoUrgenciasVoBo.getContratoUrgencias().getNivelContrato()==NivelContratoEnum.BAJA_COMPLEJIDAD)) {
    			this.setRenderUserGR(false);
    			facesMessagesUtils.addWarning(resourceBundle.getString("ctto_urgencias_message_validate_user_gr"));
    		}else {
    			this.setRenderUserGR(true);
    		}
		} catch (Exception e) {
			// TODO: handle exception
			logger.error("Error al validar el tipo de usuario para asiganr visto bueno", e);
		}
    }


	public Long getIdContratoUrgencias() {
		return idContratoUrgencias;
	}

	public void setIdContratoUrgencias(Long idContratoUrgencias) {
		this.idContratoUrgencias = idContratoUrgencias;
	}

	public List<RegionalDto> getRegionales() {
		return regionales;
	}

	public void setRegionales(List<RegionalDto> regionales) {
		this.regionales = regionales;
	}

	public LegalizacionContratoUrgenciasDto getLegalizarContratoUrgenciasVoBo() {
		return legalizarContratoUrgenciasVoBo;
	}

	public void setLegalizarContratoUrgenciasVoBo(LegalizacionContratoUrgenciasDto legalizarContratoUrgenciasVoBo) {
		this.legalizarContratoUrgenciasVoBo = legalizarContratoUrgenciasVoBo;
	}

	public List<ResponsableContratoDto> getResponsablesContratoVoBo() {
		return responsablesContratoVoBo;
	}

	public void setResponsablesContratoVoBo(List<ResponsableContratoDto> responsablesContratoVoBo) {
		this.responsablesContratoVoBo = responsablesContratoVoBo;
	}

	public Boolean getRenderUserGR() {
		return renderUserGR;
	}

	public void setRenderUserGR(Boolean renderUserGR) {
		this.renderUserGR = renderUserGR;
	}



}
