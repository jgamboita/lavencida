package com.conexia.contractual.wap.controller.parametrizacion;

import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.wap.facade.parametrizacion.ParametrizacionFacade;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroConsultaSolicitudDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Controller creado para la opcion de consulta de solicitudes
 *
 * @author jalvarado
 */
@Named
@ViewScoped
@URLMapping(id = "consultaSolicitudes", pattern = "/parametrizacion/consultaSolicitudes", viewId = "/consultaSolicitudes.page")
public class ConsultaSolicitudController extends LazyDataModel<SolicitudContratacionParametrizableDto> {

    /**
     * Facade de parametrizacion.
     */
    @Inject
    private ParametrizacionFacade parametrizacionFacade;


    /**
     * Face utils.
     */
    @Inject
    private FacesUtils facesUtils;


    /**
     * Dto que almacena los filtros de la ventana.
     */
    private FiltroConsultaSolicitudDto filtroConsultaSolicitudDto;

    /**
     * Modalidades
     */
    private List<NegociacionModalidadEnum> modalidadesNegociacion;

    /**
     * Listado de tramites.
     */
    private List<TramiteEnum> tramites;

    /**
     * Estados de parametrizacion.
     */
    private List<EstadoParametrizacionEnum> estadoParametrizacionEnums;

    private NegociacionDto dtoNeg;

    private Boolean gestionarOtroSi = false;

    private SolicitudContratacionParametrizableDto solicitudParametrizableDto;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    private Log log;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    /**
     * Cargue de datos del bean.
     */
    @PostConstruct
    public void onload() {
        filtroConsultaSolicitudDto = new FiltroConsultaSolicitudDto();
        filtroConsultaSolicitudDto.addEstadoPermitido(Arrays.asList(EstadoLegalizacionEnum.values()));
        this.modalidadesNegociacion = Arrays.asList(NegociacionModalidadEnum.values());
        this.tramites = Arrays.asList(TramiteEnum.values());
        this.estadoParametrizacionEnums = Arrays.asList(EstadoParametrizacionEnum.values());
    }

    @Override
    public List<SolicitudContratacionParametrizableDto> load(int first, int pageSize, String sortField,
                                                             SortOrder sortOrder, Map<String, Object> filters)
    {
        try {
            filtroConsultaSolicitudDto.setPagina(first);
            filtroConsultaSolicitudDto.setCantidadRegistros(pageSize);
            filtroConsultaSolicitudDto.setAscendente(sortOrder == SortOrder.ASCENDING);
            filtroConsultaSolicitudDto.setFiltros(filters);
            this.setRowCount(this.parametrizacionFacade.contarSolicitudesPorParametrizar(filtroConsultaSolicitudDto, dtoNeg));
            return this.parametrizacionFacade.listarSolicitudesPorParametrizar(filtroConsultaSolicitudDto, dtoNeg);
        }catch (Exception e) {
            log.error("Error load : ", e);
            facesMessagesUtils.addError(this.exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
        return null;
    }


    public void gestionaSolicitud(final Long id) {
        facesUtils.urlRedirect("/parametrizacion/parametrizarContrato/" + id + "/");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(
                NegociacionSessionEnum.ESTADO_PARAMETRIZACION.toString(),
                solicitudParametrizableDto.getEstadoParametrizacion().toUpperCase()
        );

        HttpSession sessionLeg = (HttpSession) facesContext.getExternalContext().getSession(true);
        sessionLeg.setAttribute(
                NegociacionSessionEnum.ESTADO_LEGALIZACION.toString(),
                solicitudParametrizableDto.getEstadoLegalizacion().toUpperCase()
        );

        HttpSession sessionNeg = (HttpSession) facesContext.getExternalContext().getSession(true);
        sessionNeg.setAttribute(
                NegociacionSessionEnum.NEGOCIACION_ID.toString(),
                solicitudParametrizableDto.getNumeroNegociacion().toString()
        );

    }

    public void limpiar() {
        this.filtroConsultaSolicitudDto = new FiltroConsultaSolicitudDto();
    }

    //<editor-fold defaultstate="collapsed" desc="getters & setters">

    /**
     * Dto del filtro de consulta de solicitud.
     *
     * @return the filtroConsultaSolicitudDto
     */
    public FiltroConsultaSolicitudDto getFiltroConsultaSolicitudDto() {
        return filtroConsultaSolicitudDto;
    }

    /**
     * @param filtroConsultaSolicitudDto the filtroConsultaSolicitudDto to set
     */
    public void setFiltroConsultaSolicitudDto(
            FiltroConsultaSolicitudDto filtroConsultaSolicitudDto) {
        this.filtroConsultaSolicitudDto = filtroConsultaSolicitudDto;
    }

    /**
     * Retorna el listado de enums de tramites.
     *
     * @return the tramites
     */
    public List<TramiteEnum> getTramites() {
        return tramites;
    }

    /**
     * Setea el listado de enums de tramites.
     *
     * @param tramites the tramites to set
     */
    public void setTramites(List<TramiteEnum> tramites) {
        this.tramites = tramites;
    }

    /**
     * Retorna los estados de parametrizacion.
     *
     * @return the estadosParametrizacion
     */
    public List<EstadoParametrizacionEnum> getEstadoParametrizacionEnums() {
        return estadoParametrizacionEnums;
    }

    /**
     * Setea los estados de parametrizacion.
     *
     * @param estadoParametrizacionEnums the estadosParametrizacion to set
     */
    public void estadoParametrizacionEnums(
            List<EstadoParametrizacionEnum> estadoParametrizacionEnums) {
        this.estadoParametrizacionEnums = estadoParametrizacionEnums;
    }

    /**
     * @return the modalidadesNegociacion
     */
    public List<NegociacionModalidadEnum> getModalidadesNegociacion() {
        return modalidadesNegociacion;
    }

    /**
     * @param modalidadesNegociacion the modalidadesNegociacion to set
     */
    public void setModalidadesNegociacion(List<NegociacionModalidadEnum> modalidadesNegociacion) {
        this.modalidadesNegociacion = modalidadesNegociacion;
    }
    //</editor-fold>
}
                
