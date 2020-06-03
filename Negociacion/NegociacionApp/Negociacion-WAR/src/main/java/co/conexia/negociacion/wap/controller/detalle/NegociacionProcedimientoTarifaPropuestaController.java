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

import co.conexia.negociacion.wap.facade.detalle.NegociacionProcedimientoTarifaPropuestaFacade;

import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.maestros.TarifaPropuestaProcedimientoDto;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 * Muestra el detalle de la comparacion de procedimientos con el contrato 
 * anterior
 * @author etorres
 */
@Named
@ViewScoped
@URLMapping(id  = "negociacionProcedimientoTarifaPropuesta", 
        pattern = "/servicio/procedimiento/tarifapropuesta", 
        viewId  = "/negociacion/detalle/detalleProcedimientoContratoOferta.page")
public class NegociacionProcedimientoTarifaPropuestaController implements Serializable{
    
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
    private NegociacionProcedimientoTarifaPropuestaFacade negociacionProcedimientoFacade;
    
    private Long negociacionId;
    
    private Long servicioId;
    
    private List<TarifaPropuestaProcedimientoDto> tarifasPropuestas;
    
    private String tituloComparacion;

    public NegociacionProcedimientoTarifaPropuestaController() {
    }
    
    @PostConstruct
    public void postConstruct() {        
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        negociacionId = (Long) session.getAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString());
        servicioId = (Long) session.getAttribute(NegociacionSessionEnum.SERVICIO_ID.toString());
        
        if(negociacionId != null &&  servicioId != null && negociacionId != null){
            listarProcedimientosTarifaPropuesta(negociacionId, servicioId);
            this.tituloComparacion = (String) session.getAttribute(NegociacionSessionEnum.TITULO_COMPARACION.toString());
        }else{
            this.facesUtils.urlRedirect("/bandejaPrestador");
        }        
    }
    
    public void listarProcedimientosTarifaPropuesta(final Long negociacionId, final Long servicioId) {
        try {
            tarifasPropuestas = negociacionProcedimientoFacade.consultarTarifasPropuestasProcedimiento(negociacionId, servicioId);
        } catch (Exception e) {
             this.logger.error("Error al listar los procedimientos con las tarifas propuestas para el prestador id" + this.negociacionId, e);
             this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public Long getPrestadorId() {
        return negociacionId;
    }

    public void setPrestadorId(Long prestadorId) {
        this.negociacionId = prestadorId;
    }

    public Long getServicioId() {
        return servicioId;
    }

    public void setServicioId(Long servicioId) {
        this.servicioId = servicioId;
    }

    public List<TarifaPropuestaProcedimientoDto> getTarifasPropuestas() {
        return tarifasPropuestas;
    }

    public void setTarifasPropuestas(List<TarifaPropuestaProcedimientoDto> tarifasPropuestas) {
        this.tarifasPropuestas = tarifasPropuestas;
    }

    public String getTituloComparacion() {
        return tituloComparacion;
    }

    public void setTituloComparacion(String tituloComparacion) {
        this.tituloComparacion = tituloComparacion;
    }
    
}
