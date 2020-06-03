package com.conexia.contractual.wap.controller.legalizacion;

import java.io.ByteArrayInputStream;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.webutils.i18n.CnxI18n;
import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;

import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.wap.facade.legalizacion.LegalizacionFacade;
import com.conexia.contractual.wap.facade.parametrizacion.ParametrizacionFacade;
import com.conexia.contractual.wap.facade.parametros.ParametrosFacade;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroConsultaSolicitudDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 * Controller creado para la opcion de consulta de solicitudes a legalizar
 *
 * @author jlopez
 */
@Named
@ViewScoped
@URLMapping(id = "consultaSolicitudesLegalizar", pattern = "/legalizacion/consultaSolicitudesLegalizar", viewId = "/consultaSolicitudesLegalizar.page")
public class ConsultaSolicitudesLegalizarController extends LazyDataModel<SolicitudContratacionParametrizableDto> {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Inject
    private ParametrosFacade parametrosFacade;

    @Inject
    private ParametrizacionFacade parametrizacionFacade;
    
    @Inject
    private FacesMessagesUtils facesMessagesUtils;
    
    @Inject
    private FacesUtils facesUtils;
    
    @Inject
    private LegalizacionFacade legalizacionFacade;
    
    private SolicitudContratacionParametrizableDto current;
    
    private StreamedContent file;
    
    @Inject
    private Log LOGGER;


    private Boolean gestionOtroSi = false;
    private NegociacionDto dtoNeg = new NegociacionDto();
    /**
     * Dto que almacena los filtros de la ventana.
     */
    private FiltroConsultaSolicitudDto filtroConsultaSolicitudDto;

    /**
     * Modalidades
     */
    private List<NegociacionModalidadEnum> modalidadesNegociacion;
    
    /**
     * Enums de estado de legalizacion.
     */
    private List<EstadoLegalizacionEnum> estadosLegalizacion;

    /**
     * Tipos de identificacion.
     */
    private List<TipoIdentificacionDto> tipoIdentificacion;

    private Long negociacionId;

    @Inject
    private Log log;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    /**
     * Constructor de la clase.
     */
    public ConsultaSolicitudesLegalizarController() {
        super();
    }

    /**
     * Cargue de datos del bean.
     */
    @PostConstruct
    public void onload() {
        filtroConsultaSolicitudDto = new FiltroConsultaSolicitudDto();
        filtroConsultaSolicitudDto.addEstadoPermitido(Arrays.asList(EstadoLegalizacionEnum.values()));
        this.setModalidadesNegociacion(Arrays.asList(NegociacionModalidadEnum.values()));
        this.setTipoIdentificacion(this.getParametrosFacade().listarTiposIdentificacion());
        this.setEstadosLegalizacion(Arrays.asList(EstadoLegalizacionEnum.values()));
    }

    @Override
    public List<SolicitudContratacionParametrizableDto> load(
            int first,
            int pageSize,
            String sortField,
            SortOrder sortOrder,
            Map<String, Object> filters)
    {
        try
        {
            getFiltroConsultaSolicitudDto().setPagina(first);
            getFiltroConsultaSolicitudDto().setCantidadRegistros(pageSize);
            getFiltroConsultaSolicitudDto().setFiltros(filters);
            this.setRowCount(this.parametrizacionFacade.contarSolicitudesPorParametrizar(filtroConsultaSolicitudDto,dtoNeg));
            List<SolicitudContratacionParametrizableDto> list;
            return this.parametrizacionFacade.listarSolicitudesPorParametrizar(getFiltroConsultaSolicitudDto(),dtoNeg);
        }catch (Exception e) {
            log.error("Error load : ", e);
            facesMessagesUtils.addError(this.exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
        return null;
    }

    public boolean tieneOtroSi(SolicitudContratacionParametrizableDto solicitud)
    {
        try
        {
            if(solicitud.getNumeroNegociacion()!=null)
            {
               return this.parametrizacionFacade.countOtroSiByNegociacion(solicitud.getNumeroNegociacion());
            }
            return false;
        }catch (Exception e) {
            log.error("Error tieneOtroSi : ", e);
            facesMessagesUtils.addError(this.exceptionUtils.createSystemErrorMessage(resourceBundle));
            return false;
        }
    }

    public void gestionaSolicitud(final Long negociacionId, final Long id) {
        facesUtils.urlRedirect("/legalizacion/generarMinuta/" + id + "/");
    	FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString(), negociacionId);
    }

    public void gestionaSolicitudCapita(final Long negociacionId, final Long id) {
        facesUtils.urlRedirect("/legalizacion/generarMinutaCapita/" + id + "/");
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString(), negociacionId);
    }
    
    public void limpiar() {
        facesUtils.urlRedirect("/legalizacion/consultaSolicitudesLegalizar.page");
    }
    
    public void descargarActa(String nombreArchivo, String numeroContrato, String nombreOriginalArchivo) {
        try {
            byte[] contenido = this.legalizacionFacade.descargarMinuta(nombreArchivo);
            if(Objects.nonNull(nombreOriginalArchivo) && !nombreOriginalArchivo.isEmpty()){
            	String formato = nombreOriginalArchivo.substring(nombreOriginalArchivo.length() -4);
            	if (formato.equals("docx") || formato.equals(".doc")){
                	this.file = new DefaultStreamedContent(new ByteArrayInputStream(contenido),
                            "application/vnd.openxmlformats-officedocument.wordprocessingml.document", numeroContrato + ".docx");
                }
            	else{
                	this.file = new DefaultStreamedContent(new ByteArrayInputStream(contenido),
                            "application/pdf", numeroContrato + ".pdf");
                }
            }
            else{
            	this.file = new DefaultStreamedContent(new ByteArrayInputStream(contenido),
                        "application/pdf", numeroContrato + ".pdf");
            }

        } catch (Exception e) {
            LOGGER.error("no se puede descargar el archivo " + nombreArchivo, e);
            facesMessagesUtils.addError("Ha ocurrido un error");
        }

    }
    
    public void handleFileUpload(FileUploadEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        byte[] contenido = event.getFile().getContents();
        String nombreArchivo = event.getFile().getFileName();
        try {
            legalizacionFacade.subirMinuta(this.current.getContratoId(), nombreArchivo, contenido);
            context.execute("PF('subirMinutaDlg').hide();");
            facesMessagesUtils.addInfo("Se ha cargado la minuta correctamente");
            Ajax.update("formConsultaSolicitudes:tblListaSolicitudesPorLegalizar");
        } catch (Exception e) {            
            facesMessagesUtils.addError("Ha ocurrido un error al cargar el archivo");
            LOGGER.error("Error al cargar el archivo ", e);
        }
    }


    /**
     * Para consultar las negociaciones otro si asociadas a una negociación padre
     * @param negociacion
     */
    public void consultarSolicitudesOtroSi(SolicitudContratacionParametrizableDto solicitud){
        try {
            dtoNeg.setNegociacionPadre(new NegociacionDto());
            dtoNeg.getNegociacionPadre().setId(solicitud.getNumeroNegociacion());
            dtoNeg.setNegociacionOrigen(new NegociacionDto());
            dtoNeg.getNegociacionOrigen().setId(solicitud.getNumeroNegociacion());
            dtoNeg.setEsOtroSi(true);
            dtoNeg.setRegimen(solicitud.getRegimenNegociacion());
            filtroConsultaSolicitudDto.setNumeroNegociacion(solicitud.getNumeroNegociacion());
            this.gestionOtroSi = true;
        } catch (Exception e) {
            LOGGER.error("Error al consultar las solicitudes de legalización otro si.", e);
            facesMessagesUtils.addError("Error al consultar las solicitudes de legalización otro si" );
        }
    }

    public void volverConsultaSolicitudesBase() {
        this.dtoNeg = new NegociacionDto();
        filtroConsultaSolicitudDto.setNumeroNegociacion(null);
        this.gestionOtroSi = false;
    }


    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
    
    public SolicitudContratacionParametrizableDto getCurrent() {
        return current;
    }

    
    public void setCurrent(SolicitudContratacionParametrizableDto current) {    
        this.current = current;
    }

    public StreamedContent getFile() {
        return file;
    }

    public void setFile(StreamedContent file) {
        this.file = file;
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

    /**
     * @return the tipoIdentificacion
     */
    public List<TipoIdentificacionDto> getTipoIdentificacion() {
        return tipoIdentificacion;
    }

    /**
     * @param tipoIdentificacion the tipoIdentificacion to set
     */
    public void setTipoIdentificacion(List<TipoIdentificacionDto> tipoIdentificacion) {
        this.tipoIdentificacion = tipoIdentificacion;

    }

    /**
     * @return the parametrosFacade
     */
    public ParametrosFacade getParametrosFacade() {
        return parametrosFacade;
    }

    /**
     * @param parametrosFacade the parametrosFacade to set
     */
    public void setParametrosFacade(ParametrosFacade parametrosFacade) {
        this.parametrosFacade = parametrosFacade;
    }

    /**
     * @return the filtroConsultaSolicitudDto
     */
    public FiltroConsultaSolicitudDto getFiltroConsultaSolicitudDto() {
        return filtroConsultaSolicitudDto;
    }

    /**
     * @param filtroConsultaSolicitudDto the filtroConsultaSolicitudDto to set
     */
    public void setFiltroConsultaSolicitudDto(FiltroConsultaSolicitudDto filtroConsultaSolicitudDto) {
        this.filtroConsultaSolicitudDto = filtroConsultaSolicitudDto;
    }
    
    /**
     * @return the estadosLegalizacion
     */
    public List<EstadoLegalizacionEnum> getEstadosLegalizacion() {
        return estadosLegalizacion;
    }

    /**
     * @param estadosLegalizacion the estadosLegalizacion to set
     */
    public void setEstadosLegalizacion(List<EstadoLegalizacionEnum> estadosLegalizacion) {
        this.estadosLegalizacion = estadosLegalizacion;
    }

	public Long getNegociacionId() {
		return negociacionId;
	}

	public void setNegociacionId(Long negociacionId) {
		this.negociacionId = negociacionId;
	}


    public NegociacionDto getDtoNeg () {
        return dtoNeg;
    }


    public void setDtoNeg (NegociacionDto dtoNeg) {
        this.dtoNeg = dtoNeg;
    }

    public Boolean getGestionOtroSi () {
        return gestionOtroSi;
    }

    public void setGestionOtroSi (Boolean gestionOtroSi) {
        this.gestionOtroSi = gestionOtroSi;
    }

    //</editor-fold>

    
}
