package com.conexia.contractual.wap.controller.legalizacion;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.Visibility;

import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.wap.facade.parametrizacion.ParametrizacionFacade;
import com.conexia.contractual.wap.facade.parametros.ParametrosFacade;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroConsultaSolicitudDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.conexia.webutils.FacesUtils;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 * Controller creado para la opcion de consulta de contratos para emitir el visto bueno
 *
 * @author dmora
 */
@Named
@ViewScoped
@URLMapping(id = "consultaContratosVistoBueno", pattern = "/legalizacion/consultaContratosVistoBueno", viewId = "/consultaContratosVistoBueno.page")
public class ConsultaContratosVistoBuenoController  extends LazyDataModel<SolicitudContratacionParametrizableDto> {

	@Inject
	private FacesUtils facesUtils;

	@Inject
	private ParametrosFacade parametrosFacade;

	@Inject
    private ParametrizacionFacade parametrizacionFacade;

	private FiltroConsultaSolicitudDto filtroConsultaSolicitudDto;
    private List<Boolean> listToggler;

    private NegociacionDto dtoNeg;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    private Log log;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

	@PostConstruct
	public void onload() {
		filtroConsultaSolicitudDto = new FiltroConsultaSolicitudDto();
		filtroConsultaSolicitudDto.addEstadoPermitido(Arrays.asList(EstadoLegalizacionEnum.PEND_LEGALIZACION,  EstadoLegalizacionEnum.CONTRATO_SIN_VB, EstadoLegalizacionEnum.LEGALIZACION_PRELIMINAR));
	}

	public List<SolicitudContratacionParametrizableDto> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters)
    {
        try
        {
            getFiltroConsultaSolicitudDto().setPagina(first);
            getFiltroConsultaSolicitudDto().setCantidadRegistros(pageSize);
            getFiltroConsultaSolicitudDto().setFiltros(filters);
            dtoNeg = new NegociacionDto();
            dtoNeg.setNegociacionPadre(new NegociacionDto());
            this.setRowCount(this.parametrizacionFacade.contarContratosVistoBueno(filtroConsultaSolicitudDto, dtoNeg));
            return this.parametrizacionFacade.listarContratosParaVistoBueno(getFiltroConsultaSolicitudDto(), dtoNeg);
        }catch (Exception e) {
            log.error("Error load : ", e);
            facesMessagesUtils.addError(this.exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
        return null;
	}

	public void onToggle(ToggleEvent e) {
        listToggler.set((Integer) e.getData(), e.getVisibility() == Visibility.VISIBLE);
    }

    public void gestionaVistoBueno(final Long negociacionId, final Long id) {
        facesUtils.urlRedirect("/legalizacion/generarVistoBueno/" + id + "/");
    	FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString(), negociacionId);
    }

    public void limpiar() {
        facesUtils.urlRedirect("/legalizacion/consultaContratosVistoBueno.page");
    }

    //<editor-fold desc="Getters && Setters">
    public FiltroConsultaSolicitudDto getFiltroConsultaSolicitudDto() {
		return filtroConsultaSolicitudDto;
	}

	public void setFiltroConsultaSolicitudDto(FiltroConsultaSolicitudDto filtroConsultaSolicitudDto) {
		this.filtroConsultaSolicitudDto = filtroConsultaSolicitudDto;
	}

	public List<NegociacionModalidadEnum> getModalidadesNegociacion() {
		return Arrays.asList(NegociacionModalidadEnum.values());
	}

	public List<EstadoLegalizacionEnum> getEstadosLegalizacion() {
		return Arrays.asList(EstadoLegalizacionEnum.PEND_LEGALIZACION,  EstadoLegalizacionEnum.CONTRATO_SIN_VB, EstadoLegalizacionEnum.LEGALIZACION_PRELIMINAR);
	}

	public ParametrosFacade getParametrosFacade() {
		return parametrosFacade;
	}

	public void setParametrosFacade(ParametrosFacade parametrosFacade) {
		this.parametrosFacade = parametrosFacade;
	}

	public List<TipoIdentificacionDto> getTipoIdentificacion() {
        try {
            return parametrosFacade.listarTiposIdentificacion();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
    //</editor-fold>

}
