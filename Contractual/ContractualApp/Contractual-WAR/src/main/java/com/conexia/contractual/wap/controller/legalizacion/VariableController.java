package com.conexia.contractual.wap.controller.legalizacion;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TipoVariableEnum;
import com.conexia.contractual.model.contratacion.legalizacion.VariableModalidad;
import com.conexia.contractual.wap.facade.legalizacion.ParametrizacionMinutaFacade;
import com.conexia.contratacion.commons.dto.negociacion.ModalidadNegociacionDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.VariableDto;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;

/**
 *
 * @author Andrés Mise Olivera
 */
@Named
@ViewScoped
@URLMapping(id = "asociarVariable", pattern = "/legalizacion/asociarVariable", viewId = "/legalizacion/asociarVariable.page")
public class VariableController implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject
    private ParametrizacionMinutaFacade minutaFacade;
    private ModalidadNegociacionDto modalidad;
    private NegociacionModalidadEnum modalidadVarible;
    private Long minutaId;
    private Integer modalidadNegociacion;
    private Integer estadoAsociar = 1;
    private Integer estadoDisponible = 0;
    private VariableModalidad variableModalidad;
    
    private List<VariableDto> source;
    private DualListModel<VariableDto> variables;
    private TipoVariableEnum tipoVariable;

    /*public void asociarVariables() {
        try {
            this.minutaFacade.mergeMinuta(minuta);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO,
                            "Las variables se asociaron correctamente a la modalidad.", ""));
        } catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ha ocurrido un error al guardar", ""));
        }
    }*/

    public void onTransfer(TransferEvent event) {

        try {
            
            for(Object item : event.getItems()) {
                String variableId = ((VariableDto) item).getId().toString();
                if (event.isRemove()){
                    this.minutaFacade.asociarVariables(estadoDisponible, Long.parseLong(variableId), modalidadVarible.getId()); 
                }else{
                    this.minutaFacade.asociarVariables(estadoAsociar, Long.parseLong(variableId), modalidadVarible.getId());
                }
            }

            FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO,
                                "Las variables se asociaron correctamente a la modalidad.", ""));
            
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ha ocurrido un error al guardar", ""));
        }
    }

    public void buscarVariablesModalidad() {
        try {  
            modalidad = new ModalidadNegociacionDto();
            modalidad.setVariables(this.minutaFacade.listarVariablesPorModalidad(tipoVariable, modalidadVarible.getId(),estadoAsociar));
            modalidad.setId(modalidadVarible.getId());
            List<VariableDto> source = this.minutaFacade.listarVariablesPorModalidad(tipoVariable, modalidadVarible.getId(),estadoDisponible);
            this.source.addAll(source);
            this.source.addAll(modalidad.getVariables());
            variables = new DualListModel<VariableDto>(source, modalidad.getVariables());
        }
        catch (Exception ex) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ha ocurrido un error al consultar", ""));
        }
    }

    public ModalidadNegociacionDto getModalidad() {
        return modalidad;
    }

    public void setModalidad(ModalidadNegociacionDto modalidad) {
        this.modalidad = modalidad;
    }

    public Long getMinutaId() {
        return minutaId;
    }

    public void setMinutaId(Long minutaId) {
        this.minutaId = minutaId;
    }

    public DualListModel<VariableDto> getVariables() {
        return variables;
    }

    public void setVariables(DualListModel<VariableDto> variables) {
        if ((!variables.getSource().isEmpty())&&(!variables.getTarget().isEmpty())){
            this.variables = variables;
        }
        
    }

    public List<VariableDto> getSource() {
        return source;
    }

    public void setSource(List<VariableDto> source) {
        this.source = source;
    }

    public TipoVariableEnum getTipoVariable() {
        return tipoVariable;
    }

    public void setTipoVariable(TipoVariableEnum tipoVariable) {
        this.tipoVariable = tipoVariable;
    }

    public Integer getModalidadNegociacion() {
        return modalidadNegociacion;
    }

    public void setModalidadNegociacion(Integer modalidadNegociacion) {
        this.modalidadNegociacion = modalidadNegociacion;
    }

    public VariableModalidad getVariableModalidad() {
        return variableModalidad;
    }

    public void setVariableModalidad(VariableModalidad variableModalidad) {
        this.variableModalidad = variableModalidad;
    }


    public NegociacionModalidadEnum getModalidadVarible() {
        return modalidadVarible;
    }

    public void setModalidadVarible(NegociacionModalidadEnum modalidadVarible) {
        this.modalidadVarible = modalidadVarible;
    }

    @PostConstruct
    private void init() {
        source = new ArrayList<VariableDto>();
        variables = new DualListModel();
    }

}
