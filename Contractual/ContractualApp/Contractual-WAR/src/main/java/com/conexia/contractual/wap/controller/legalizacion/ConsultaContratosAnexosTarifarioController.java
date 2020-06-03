package com.conexia.contractual.wap.controller.legalizacion;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutionException;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;

import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.wap.facade.parametrizacion.ParametrizacionFacade;
import com.conexia.contractual.wap.facade.parametros.ParametrosFacade;
import com.conexia.contractual.wap.rest.client.negociacion.NegociacionRestClient;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroConsultaSolicitudDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesUtils;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import okhttp3.ResponseBody;

/**
 * @author icruz
 */
@Named
@ViewScoped
@URLMapping(id = "consultaContratosYAnexos", pattern = "/legalizacion/consultaContratosYAnexos", viewId = "/consultaContratosYAnexos.page")
public class ConsultaContratosAnexosTarifarioController extends LazyDataModel<SolicitudContratacionParametrizableDto> {

    @Inject
    private ParametrosFacade parametrosFacade;

    @Inject
    private ParametrizacionFacade parametrizacionFacade;

    @Inject
    private Log logger;

    @Inject
    private FacesUtils facesUtils;

    @Inject
    @UserInfo
    private UserApp user;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;


    private boolean negociacionEsCapita;
    private FiltroConsultaSolicitudDto filtroConsultaSolicitudDto;
    private NegociacionRestClient negociacionRestClient;
    private StreamedContent file;

    @PostConstruct
    public void onload() {
        filtroConsultaSolicitudDto = new FiltroConsultaSolicitudDto();
        filtroConsultaSolicitudDto.setEstadoLegalizacionEnum(EstadoLegalizacionEnum.LEGALIZADA);
        filtroConsultaSolicitudDto.addEstadoPermitido(EstadoLegalizacionEnum.LEGALIZADA);

        String hostAndPort = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("server-web-service-negociacion");
        negociacionRestClient = new NegociacionRestClient();
        negociacionRestClient.build(hostAndPort);
    }

    @Override
    public List<SolicitudContratacionParametrizableDto> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        getFiltroConsultaSolicitudDto().setPagina(first);
        getFiltroConsultaSolicitudDto().setCantidadRegistros(pageSize);
        getFiltroConsultaSolicitudDto().setFiltros(filters);
        this.setRowCount(this.parametrizacionFacade.contarContratos(filtroConsultaSolicitudDto).intValue());
        return this.parametrizacionFacade.listarContratos(getFiltroConsultaSolicitudDto());
    }

    public void descargarAnexoExcel(final SolicitudContratacionParametrizableDto contratoYAnexo) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString(), contratoYAnexo.getNumeroNegociacion());
        session.setAttribute(NegociacionSessionEnum.MODALIDAD_CONTRATACION.toString(), contratoYAnexo.getModalidadNegociacionEnum().getId());
        session.setAttribute(NegociacionSessionEnum.NUMERO_CONTRATO.toString(), contratoYAnexo.getNumeroContrato());
        RequestContext context = RequestContext.getCurrentInstance();
        negociacionEsCapita = contratoYAnexo.getModalidadNegociacionEnum().equals(NegociacionModalidadEnum.CAPITA);
        context.update("anexoDialog");
        context.execute("PF('anexoTarifarioDlg').show();");
    }

    public void descargarAnexoExcelEvento(String servicio) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Long negociacionId = (Long) session.getAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString());
        String numeroContrato = (String) session.getAttribute(NegociacionSessionEnum.NUMERO_CONTRATO.toString());
        try {
            String fileName = negociacionRestClient.getFileName(negociacionId, servicio, numeroContrato).get().getFileName();
            ResponseBody responseBody = negociacionRestClient.descargarAnexoTarifario1(negociacionId, servicio, user.getId(), numeroContrato);
            if (responseBody != null) {
                byte[] contenido = responseBody.bytes();
                this.file = new DefaultStreamedContent(new ByteArrayInputStream(contenido),"application/vnd.ms-excel", fileName);
            }
            String hostAndPort = FacesContext.getCurrentInstance().getExternalContext().getInitParameter("server-web-service-negociacion");
            logger.info("---> URL ANEXO: " + hostAndPort + "/wap/negociacion/rest/reporteAnexoTarifario?negociacion=" + negociacionId + "&servicio=" + servicio);

        } catch (IOException e) {
            logger.error("Error al descargar el anexo Tarifario negociacion: " + negociacionId, e);
            this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        } catch (InterruptedException e) {
            logger.error("Se interumpio el proceso de descarga del anexo tarifario: " + negociacionId, e);
            this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        } catch (ExecutionException e) {
            logger.error("Error al ejecutar el proceso de descarga del anexo tarifario: " + negociacionId, e);
            this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public void limpiar() {
        facesUtils.urlRedirect("/legalizacion/consultaContratosYAnexos.page");
    }

    public Boolean tienePaquetes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Long negociacion = (Long) session.getAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString());
        if (negociacion == null) {
            return true;
        }
        return parametrosFacade.tienePaquetes(negociacion);
    }

    public Boolean tieneProcedimientos() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Long negociacion = (Long) session.getAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString());
        Integer modalidadId = (Integer) session.getAttribute(NegociacionSessionEnum.MODALIDAD_CONTRATACION.toString());
        if (negociacion == null) {
            return true;
        }
        return parametrosFacade.tieneProcedimientos(negociacion, modalidadId);
    }

    public Boolean tieneMedicamentos() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Long negociacion = (Long) session.getAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString());
        Integer modalidadId = (Integer) session.getAttribute(NegociacionSessionEnum.MODALIDAD_CONTRATACION.toString());
        if (negociacion == null) {
            return true;
        }
        return parametrosFacade.tieneMedicamentos(negociacion, modalidadId);
    }

    public Boolean tieneAnexosSinTarifarioProcedimientos() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Long negociacion = (Long) session.getAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString());
        Integer modalidadId = (Integer) session.getAttribute(NegociacionSessionEnum.MODALIDAD_CONTRATACION.toString());
        return (Objects.nonNull(negociacion) && tieneProcedimientos() && !NegociacionModalidadEnum.RIAS_CAPITA.getId().equals(modalidadId));
    }

    public Boolean tienePoblacion() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Long negociacion = (Long) session.getAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString());
        Integer modalidadId = (Integer) session.getAttribute(NegociacionSessionEnum.MODALIDAD_CONTRATACION.toString());
        return negociacion == null || parametrosFacade.tienePoblacion(negociacion) && NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.getId().equals(modalidadId);
    }

    //<editor-fold desc="Getters && Setters">
    public FiltroConsultaSolicitudDto getFiltroConsultaSolicitudDto() {
        return filtroConsultaSolicitudDto;
    }

    public void setFiltroConsultaSolicitudDto(FiltroConsultaSolicitudDto filtroConsultaSolicitudDto) {
        this.filtroConsultaSolicitudDto = filtroConsultaSolicitudDto;
    }

    public ParametrosFacade getParametrosFacade() {
        return parametrosFacade;
    }

    public void setParametrosFacade(ParametrosFacade parametrosFacade) {
        this.parametrosFacade = parametrosFacade;
    }

    public List<NegociacionModalidadEnum> getModalidadesNegociacion() {
        return Arrays.asList(NegociacionModalidadEnum.values());
    }

    public List<EstadoLegalizacionEnum> getEstadosLegalizacion() {
        return Collections.singletonList(EstadoLegalizacionEnum.LEGALIZADA);
    }

    public List<TipoIdentificacionDto> getTipoIdentificacion() {
        try {
            return parametrosFacade.listarTiposIdentificacion();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    public boolean isNegociacionEsCapita() {
        return negociacionEsCapita;
    }

    public void setNegociacionEsCapita(boolean negociacionEsCapita) {
        this.negociacionEsCapita = negociacionEsCapita;
    }

    public StreamedContent getFile() {
        return file;
    }

    public void setFile(StreamedContent file) {
        this.file = file;
    }
    //</editor-fold>
}
