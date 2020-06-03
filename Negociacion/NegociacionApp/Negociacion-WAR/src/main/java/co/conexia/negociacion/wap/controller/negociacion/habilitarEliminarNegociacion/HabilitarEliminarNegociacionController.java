package co.conexia.negociacion.wap.controller.negociacion.habilitarEliminarNegociacion;

import co.conexia.negociacion.wap.facade.negociacion.habilitarEliminarNegociacion.HabilitarEliminarNegociacionFacade;
import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaConsultaContratoDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionConsultaContratoDto;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import org.omnifaces.cdi.ViewScoped;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import org.primefaces.context.RequestContext;

/**
 * @author emedina
 */
@Named
@ViewScoped
@URLMapping(id = "HabilitarEliminarNegociacion", pattern = "/HabilitarEliminarNegociacion", viewId = "/negociacion/HabilitarEliminarNegociacion/HabilitarEliminarNegociacion.page")
public class HabilitarEliminarNegociacionController implements Serializable {

    @Inject
    private FacesUtils facesUtils;

    @Inject
    private HabilitarEliminarNegociacionFacade habilitarEliminarNegociacionFacade;

    @Inject
    private Log logger;
    @Inject
    private FacesMessagesUtils facesMessagesUtils;
    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    private FiltroBandejaConsultaContratoDto filtroBandejaConsultaContratoDto;
    private List<NegociacionConsultaContratoDto> negociacionConsultaContratoDto;
    private NegociacionConsultaContratoDto selectionNegociacionConsultaContratoDto;
    private Boolean usuarioAdministrador;
    private Boolean usuarioAuxiliar;
     private String msgConfirmacion;

    @PostConstruct
    public void init() {
        this.setFiltroBandejaConsultaContratoDto(new FiltroBandejaConsultaContratoDto());
        this.setUsuarioAdministrador(false);
        this.setUsuarioAuxiliar(false);

    }

    public void buscaContratos() {
        try {
            if (validaciones(filtroBandejaConsultaContratoDto)) {
                negociacionConsultaContratoDto = habilitarEliminarNegociacionFacade.buscaContratos(filtroBandejaConsultaContratoDto);
            }

        } catch (Exception e) {
           facesMessagesUtils.addError(resourceBundle.getString("system_error"));
        }
    }

     public void habilitarNegociacion(NegociacionConsultaContratoDto selDTo) {
        Boolean confirmarEliminar  = false;
        try {
             RequestContext context = RequestContext.getCurrentInstance();
            if (selDTo.getEstadoLegalizacion().equals(EstadoLegalizacionEnum.LEGALIZADA.name())) {
                if (habilitarEliminarNegociacionFacade.habilitarContrato(selDTo)) {
                    confirmarEliminar = true;
                    this.setMsgConfirmacion(null);
                    this.remove(selectionNegociacionConsultaContratoDto);
                } else {
                    confirmarEliminar = true;
                    this.setMsgConfirmacion(resourceBundle.getString("negociacion_HabilitarEliminarNegociacion_no_se_habilita_negociacion"));
                }
            } else {
                confirmarEliminar = true;
                this.setMsgConfirmacion(resourceBundle.getString("negociacion_HabilitarEliminarNegociacion_habilitar_negociacion"));
            }
            if(confirmarEliminar){
             context.execute("PF('dialogHabilitaNegociacionConfirmacion').show();");
            }else{
            context.execute("PF('dialogHabilitaNegociacionConfirmacion').hide();");
            }
        } catch (Exception e) {
           facesMessagesUtils.addError(resourceBundle.getString("system_error"));
        }
    }

    public void eliminarNegociacion(NegociacionConsultaContratoDto eliminarContratoDto) {
        Boolean confirmacionEliminado = false;
        RequestContext context = RequestContext.getCurrentInstance();
        if (validacionEliminarContrato(eliminarContratoDto)) {
            String valida = habilitarEliminarNegociacionFacade.eliminarContrato(eliminarContratoDto);
            if (valida.equals("oK")) {
                if (eliminarContratoDto.getNumeroContrato() != null) {
                    this.setMsgConfirmacion("Contrato");
                    confirmacionEliminado = true;
                } else {
                    confirmacionEliminado = true;
                    this.setMsgConfirmacion("Negociacion");
                }
                this.remove(selectionNegociacionConsultaContratoDto);
            } else if (valida.equals("validaAutorizacionesAsociadas")) {
                confirmacionEliminado = true;
                this.setMsgConfirmacion(resourceBundle.getString("negociacion_HabilitarEliminarNegociacion_tiene_autorizaciones_asociadas"));
            } else if (valida.equals("validaRadicacionesAsociadas")) {
                confirmacionEliminado = true;
                this.setMsgConfirmacion(resourceBundle.getString("negociacion_HabilitarEliminarNegociacion_tiene_radicaciones_asociadas"));
            } else {
                confirmacionEliminado = true;
                this.setMsgConfirmacion(resourceBundle.getString("negociacion_HabilitarEliminarNegociacion_error_al_eliminar_contrato"));
            }

        }
        if(confirmacionEliminado){
        context.execute("PF('dialogHabilitaNegociacionConfirmacionEliminar').show();");
        }else{
           context.execute("PF('dialogHabilitaNegociacionConfirmacionEliminar').hide();");
        }
    }

    public void limpiar() {
        facesUtils.urlRedirect("/negociacion/HabilitarEliminarNegociacion/HabilitarEliminarNegociacion.page");
    }

    public Boolean validacionEliminarContrato(NegociacionConsultaContratoDto eliminarContratoDto) {
        Boolean validaciones = false;
        try {
            if (eliminarContratoDto.getNumeroNegociacion() != null && eliminarContratoDto.getEstadoLegalizacion() != null) {
                if (eliminarContratoDto.getEstadoLegalizacion().equals(EstadoLegalizacionEnum.LEGALIZADA.name())
                        || eliminarContratoDto.getEstadoLegalizacion().equals(EstadoLegalizacionEnum.LEGALIZACION_PRELIMINAR.name())
                        || eliminarContratoDto.getEstadoLegalizacion().equals(EstadoLegalizacionEnum.CONTRATO_SIN_VB.name())) {
                    validaciones = true;
                } else {
                    this.msg(resourceBundle.getString("negociacion_HabilitarEliminarNegociacion_estado_eliminar_contrato"));
                }

            } else {
                validaciones = true;
            }
        } catch (Exception e) {
            facesMessagesUtils.addError(resourceBundle.getString("system_error"));
        }
        return validaciones;
    }

     public Boolean validaciones(FiltroBandejaConsultaContratoDto filtro) {
        Boolean valida = false;

            if (!filtro.getNumeroContrato().isEmpty() || !filtro.getRazonSocial().isEmpty() || Objects.nonNull(filtro.getNumeroDocumento()) || filtro.getNumeroNegociacion() != null) {
                valida = true;
            } else if (!filtro.getNumeroContrato().isEmpty()
                    || !filtro.getRazonSocial().isEmpty()
                    || Objects.nonNull(filtro.getNumeroDocumento())
                    || filtro.getNumeroNegociacion() != null
                    || filtro.getModalidad() != null
                    || filtro.getEstadoContrato() != null) {
                valida = true;
            } else {

                valida = false;
                this.msg(resourceBundle.getString("negociacion_HabilitarEliminarNegociacion_validaciones_habilitar_negociacion"));

            }

        return valida;
    }

    public String remove(NegociacionConsultaContratoDto selectionNegociacionConsultaContratoDto) {
        this.negociacionConsultaContratoDto.remove(selectionNegociacionConsultaContratoDto);
        return null;
    }

    public void msg(String msg) {
        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null);
        FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public List<NegociacionModalidadEnum> getModalidades() {
        return Arrays.asList(NegociacionModalidadEnum.values());
    }
    //<editor-fold desc="Getters && Setters">
      public String getMsgConfirmacion() {
        return msgConfirmacion;
    }

    public void setMsgConfirmacion(String msgConfirmacion) {
        this.msgConfirmacion = msgConfirmacion;
    }
    public FiltroBandejaConsultaContratoDto getFiltroBandejaConsultaContratoDto() {
        return filtroBandejaConsultaContratoDto;
    }

    public void setFiltroBandejaConsultaContratoDto(FiltroBandejaConsultaContratoDto filtroBandejaConsultaContratoDto) {
        this.filtroBandejaConsultaContratoDto = filtroBandejaConsultaContratoDto;
    }

    public List<NegociacionConsultaContratoDto> getNegociacionConsultaContratoDto() {
        return negociacionConsultaContratoDto;
    }

    public void setNegociacionConsultaContratoDto(List<NegociacionConsultaContratoDto> negociacionConsultaContratoDto) {
        this.negociacionConsultaContratoDto = negociacionConsultaContratoDto;
    }

    public Boolean getUsuarioAdministrador() {
        return usuarioAdministrador;
    }

    public void setUsuarioAdministrador(Boolean usuarioAdministrador) {
        this.usuarioAdministrador = usuarioAdministrador;
    }

    public Boolean getUsuarioAuxiliar() {
        return usuarioAuxiliar;
    }

    public void setUsuarioAuxiliar(Boolean usuarioAuxiliar) {
        this.usuarioAuxiliar = usuarioAuxiliar;
    }

    public NegociacionConsultaContratoDto getSelectionNegociacionConsultaContratoDto() {
        return selectionNegociacionConsultaContratoDto;
    }

    public void setSelectionNegociacionConsultaContratoDto(NegociacionConsultaContratoDto selectionNegociacionConsultaContratoDto) {
        this.selectionNegociacionConsultaContratoDto = selectionNegociacionConsultaContratoDto;
    }
    //</editor-fold>
}
