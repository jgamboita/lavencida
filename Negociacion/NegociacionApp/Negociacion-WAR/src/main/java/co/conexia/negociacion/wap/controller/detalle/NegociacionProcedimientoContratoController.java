package co.conexia.negociacion.wap.controller.detalle;

import java.io.Serializable;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ProcedimientoContratoDto;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import co.conexia.negociacion.wap.facade.detalle.NegociacionProcedimientoContratoFacade;

/**
 * Muestra el detalle de la comparacion de procedimientos con el contrato 
 * anterior
 * @author etorres
 */
@Named
@ViewScoped
@URLMapping(id  = "negociacionProcedimientoContrato", pattern = "/servicio/procedimiento/contrato", viewId  = "/negociacion/detalle/detalleProcedimientosContrato.page")
public class NegociacionProcedimientoContratoController implements Serializable{
    
    @Inject
    private Log logger;
    
    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;
    
    @Inject
    private FacesUtils facesUtils;
    
    @Inject
    private FacesMessagesUtils facesMessagesUtils;
    
    @Inject
    private PreContractualExceptionUtils exceptionUtils;
    
    @Inject
    private NegociacionProcedimientoContratoFacade negociacionProcedimientoFacade;
    
    private Long prestadorId;
    
    private Long servicioId;
    
    private List<ProcedimientoContratoDto> contratoProcedimientos;
    
    private String tituloComparacion;

    public NegociacionProcedimientoContratoController() {
    }
    
    @PostConstruct
    public void postConstruct() {        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Long negociacionId = (Long) session.getAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString());        
        prestadorId = (Long) session.getAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString());
        servicioId = (Long) session.getAttribute(NegociacionSessionEnum.SERVICIO_ID.toString());
        
        if(prestadorId != null &&  servicioId != null && negociacionId != null){
            listarProcedimientosContratoAnterior(prestadorId, servicioId);
            this.tituloComparacion = (String) session.getAttribute(NegociacionSessionEnum.TITULO_COMPARACION.toString());
        }else{
            this.facesUtils.urlRedirect("/bandejaPrestador");
        }        
    }
    
    public void listarProcedimientosContratoAnterior(final Long prestadorId, final Long servicioId) {
        try {
            contratoProcedimientos = negociacionProcedimientoFacade
                    .consultarProcedimientosContratoAnterior(prestadorId, servicioId);
        } catch (Exception e) {
             this.logger.error("Error al listar los procedimientos del contrato anterior para el prestador id" + prestadorId, e);
             this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public Long getPrestadorId() {
        return prestadorId;
    }

    public void setPrestadorId(Long prestadorId) {
        this.prestadorId = prestadorId;
    }

    public Long getServicioId() {
        return servicioId;
    }

    public void setServicioId(Long servicioId) {
        this.servicioId = servicioId;
    }

    public List<ProcedimientoContratoDto> getContratoProcedimientos() {
        return contratoProcedimientos;
    }

    public void setContratoProcedimientos(List<ProcedimientoContratoDto> contratoProcedimientos) {
        this.contratoProcedimientos = contratoProcedimientos;
    }

    public String getTituloComparacion() {
        return tituloComparacion;
    }

    public void setTituloComparacion(String tituloComparacion) {
        this.tituloComparacion = tituloComparacion;
    }
    
}
