package com.conexia.contractual.wap.controller.comun;

import com.conexia.contractual.wap.facade.parametrizacion.ParametrizacionFacade;
import com.conexia.contratacion.commons.dto.DescriptivoPaginacionDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.AreaInfluenciaDto;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 * Controller creado comun para los datos de area de influecia.
 *
 * @author jLopez
 */
@Named
@ViewScoped
@URLMapping(id = "areaInfluencia", pattern = "/comun/areaInfluencia", viewId = "/areaInfluencia.page")
public class AreaInfluenciaController extends LazyDataModel<AreaInfluenciaDto> {

    /**
     * Filtros de area de influencia.
     */
    private DescriptivoPaginacionDto filtrosAreaInfluencia;

    @Inject
    private ParametrizacionFacade parametrizacionFacade;

    @Override
    public List<AreaInfluenciaDto> load(int first, int pageSize, String sortField,
            SortOrder sortOrder, Map<String, Object> filters) {
        List<AreaInfluenciaDto> areasInfluencia = new ArrayList<>();
        if (filtrosAreaInfluencia != null) {
            getFiltrosAreaInfluencia().setPagina(first);
            getFiltrosAreaInfluencia().setCantidadRegistros(pageSize);
            getFiltrosAreaInfluencia().setAscendente(sortOrder == SortOrder.ASCENDING);
            if(getFiltrosAreaInfluencia().getFiltros()!=null){
            	getFiltrosAreaInfluencia().getFiltros().putAll(filters);	
            }else{
            	getFiltrosAreaInfluencia().setFiltros(filters);;
            }
            
            try {
                this.setRowCount(this.getParametrizacionFacade().contarAreasCobertura(getFiltrosAreaInfluencia()));
                areasInfluencia = this.getParametrizacionFacade().listarAreasCobertura(getFiltrosAreaInfluencia());
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(e.getMessage()));
            }
        }
        return areasInfluencia;
    }

    /**
     * @return the filtrosAreaInfluencia
     */
    public DescriptivoPaginacionDto getFiltrosAreaInfluencia() {
        return filtrosAreaInfluencia;
    }

    /**
     * @param filtrosAreaInfluencia the filtrosAreaInfluencia to set
     */
    public void setFiltrosAreaInfluencia(DescriptivoPaginacionDto filtrosAreaInfluencia) {
        this.filtrosAreaInfluencia = filtrosAreaInfluencia;
    }

    /**
     * @return the parametrizacionFacade
     */
    public ParametrizacionFacade getParametrizacionFacade() {
        return parametrizacionFacade;
    }

    /**
     * @param parametrizacionFacade the parametrizacionFacade to set
     */
    public void setParametrizacionFacade(ParametrizacionFacade parametrizacionFacade) {
        this.parametrizacionFacade = parametrizacionFacade;
    }

}
