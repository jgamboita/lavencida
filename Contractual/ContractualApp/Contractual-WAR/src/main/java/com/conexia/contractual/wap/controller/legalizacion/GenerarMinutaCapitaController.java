package com.conexia.contractual.wap.controller.legalizacion;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.context.RequestContext;
import org.primefaces.model.StreamedContent;

import com.conexia.contratacion.commons.constants.enums.EstadoEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NivelAtencionEnum;
import com.conexia.contratacion.commons.constants.enums.NivelContratoEnum;
import com.conexia.contratacion.commons.constants.enums.TipoCondicionEnum;
import com.conexia.contratacion.commons.constants.enums.TipoDescuentoEnum;
import com.conexia.contratacion.commons.constants.enums.TipoObjetoContratoEnum;
import com.conexia.contratacion.commons.constants.enums.TipoValorEnum;
import com.conexia.contractual.utils.DateUtils;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.wap.action.legalizacion.GenerarMinutaAction;
import com.conexia.contractual.wap.controller.comun.DetallePrestadorController;
import com.conexia.contractual.wap.facade.legalizacion.LegalizacionFacade;
import com.conexia.contractual.wap.facade.legalizacion.ParametrizacionMinutaFacade;
import com.conexia.contractual.wap.facade.parametrizacion.ParametrizacionFacade;
import com.conexia.contractual.wap.facade.parametros.ParametrosFacade;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.DescuentoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ResponsableContratoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.RegionalDto;
import com.conexia.contratacion.commons.dto.negociacion.IncentivoModeloDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 * Controller creado para la opcion de consulta de solicitudes a legalizar
 *
 * @author jlopez
 */
@Named
@ViewScoped
@URLMapping(id = "generarMinutaCapita", pattern = "/legalizacion/generarMinutaCapita/#{generarMinutaCapitaController.idSolicitudContratacion}/", viewId = "/legalizacion/generarMinutaCapita.page")
public class GenerarMinutaCapitaController implements Serializable {

    /**
	 *
	 */
	private static final long serialVersionUID = -646387459294840034L;

	@Inject
    private FacesContext context;

    @Inject
    private DetallePrestadorController prestadorController;

    @Inject
    private ParametrizacionFacade parametrizacionFacade;

    @Inject
    private ParametrosFacade parametrosFacade;

    @Inject
    private DateUtils dateUtils;

    @Inject
    private ParametrizacionMinutaFacade parametrizacionMinutaFacade;

    @Inject
    private LegalizacionFacade legalizacionFacade;

    @Inject
    private GenerarMinutaAction generarMinutaAction;

    @Inject
    @UserInfo
    private UserApp user;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    private FacesUtils facesUtils;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    @Inject
    @CnxI18n
    private ResourceBundle resourceBundle;

    /**
     * Dto de legalizacion de contrato.
     */
    private LegalizacionContratoDto legalizacionContratoDto;

    /**
     * Lista de minutas.
     */
    private List<MinutaDto> minutas;

    /**
     * Lista de objetos del contrato.
     */
    private List<TipoObjetoContratoEnum> tiposContrato;

    /**
     * Departamentos.
     */
    private List<DepartamentoDto> departamentosFirma;

    /**
     * Lista de municipios.
     */
    private List<MunicipioDto> municipiosFirma = new ArrayList<>();

    /**
     * Lista de Tipos descuento.
     */
    private List<TipoDescuentoEnum> tiposDescuento;

    /**
     * Lista de Tipos valor.
     */
    private List<TipoValorEnum> tiposValor;

    /**
     * Lista de Tipos condicion.
     */
    private List<TipoCondicionEnum> tiposCondicion;

    /**
     * Niveles de contrato.
     */
    private List<NivelContratoEnum> nivelesContrato;

    /**
     * Nivel de contrato. Regional de la legalizacion del contrato.
     */
    private List<RegionalDto> regionales;

    /**
     * Responsables contrato firma.
     */
    private List<ResponsableContratoDto> responsablesContratoFirma = new ArrayList<>();

    /**
     * Responsables contrato Vo Bo.
     */
    private List<ResponsableContratoDto> responsablesContratoVoBo = new ArrayList<>();

    private List<String> incentivo = new ArrayList<String>();

    private List<IncentivoModeloDto> incentivos;

    private List<IncentivoModeloDto> modelos;

    private StreamedContent pdfFile;

    private Long idSolicitudContratacion;

    public void loadIni() {
        try {
            if (context.getPartialViewContext().isAjaxRequest()) {
                return;
            }
            incentivo.add("Ok");

            SolicitudContratacionParametrizableDto solicitudDto = this.cargarGeneracionMinuta();
            if (solicitudDto.getIdSolicitudContratacion() == null) {
                setLegalizacionContratoDto(new LegalizacionContratoDto());
                this.getLegalizacionContratoDto().setContratoDto(new ContratoDto());
                this.getLegalizacionContratoDto().getContratoDto().setSolicitudContratacionParametrizableDto(solicitudDto);
            } else {
                //TODO Hacer consulta para cargar la legalizacion del contrato.
                try {
                    this.setLegalizacionContratoDto(this.legalizacionFacade.buscarLegalizacionContrato(solicitudDto));
                    if (this.getLegalizacionContratoDto().getDescuentos().size() > 0) {
                        this.getLegalizacionContratoDto().setAplicaDecuento(Boolean.TRUE);
                        this.aplicarDescuento();
                    }
                    this.buscarMunicipios();
                    this.buscarResponsables();
                    this.calculaDuracionContrato();
                    this.getLegalizacionContratoDto().getContratoDto().setSolicitudContratacionParametrizableDto(solicitudDto);
                    this.prestadorController.setDepartamento(this.getLegalizacionContratoDto().getDepartamentoPrestadorDto());
                    this.prestadorController.buscaMunicipios();
                    this.prestadorController.setMunicipioDto(this.getLegalizacionContratoDto().getMunicipioPrestadorDto());
                    this.prestadorController.setDireccion(this.getLegalizacionContratoDto().getDireccionPrestador());
                } catch (final ConexiaBusinessException e) {
                    setLegalizacionContratoDto(new LegalizacionContratoDto());
                    this.getLegalizacionContratoDto().setContratoDto(new ContratoDto());
                    this.getLegalizacionContratoDto().getContratoDto().setSolicitudContratacionParametrizableDto(solicitudDto);
                }
            }
            this.getLegalizacionContratoDto().getContratoDto().setSolicitudContratacionParametrizableDto(solicitudDto);
            prestadorController.setPrestador(solicitudDto.getPrestadorDto());
            prestadorController.setMostrarUbicacion(Boolean.TRUE);
            prestadorController.setMostrarRepresentante(Boolean.TRUE);
            this.cargarCombos();
        } catch (final ConexiaBusinessException e) {
            this.facesMessagesUtils.addError(exceptionUtils.createMessage(resourceBundle, e));
        } catch (final Exception e) {
            this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    private SolicitudContratacionParametrizableDto cargarGeneracionMinuta() throws ConexiaBusinessException {
        return this.parametrizacionFacade.obtenerSolicitud(this.idSolicitudContratacion);
    }

    private void cargarCombos() {
        this.minutas = this.parametrizacionMinutaFacade.listarMinutas(EstadoEnum.ACTIVO,
                this.legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum());
        this.tiposContrato = Arrays.asList(TipoObjetoContratoEnum.values());
        this.departamentosFirma = this.parametrosFacade.listarDepartamentos();
        this.tiposDescuento = Arrays.asList(TipoDescuentoEnum.values());
        this.tiposCondicion = Arrays.asList(TipoCondicionEnum.values());
        this.tiposValor = Arrays.asList(TipoValorEnum.values());
        this.nivelesContrato = NivelContratoEnum.values(
                NivelAtencionEnum.getNivelAtencionByDescripcion(
                        prestadorController.getPrestador().getNivelAtencion()));
        this.setRegionales(this.parametrosFacade.listarRegionalesUsuario(user));
        if (minutas.isEmpty()) {
            this.facesMessagesUtils.addWarning("No existen minutas para la modalidad, por favor cree una minuta antes de continuar");
        }
        if (legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum().getDescripcion().equalsIgnoreCase(NegociacionModalidadEnum.CAPITA.getDescripcion())){
            Long negociacionId = legalizacionContratoDto.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion();
            incentivos = legalizacionFacade.listarIncentivosPorNegociacionId(negociacionId);
            modelos = legalizacionFacade.listarModelosPorNegociacionId(negociacionId);
//            NegociacionDto negociacionDto = legalizacionFacade.consultarNegociacionById(negociacionId);
//            legalizacionContratoDto.setTipoUpcNegociacion(negociacionDto.getTipoUpc());
//            legalizacionContratoDto.setValorAnualUpc(negociacionDto.getValorUpcAnual());
//            legalizacionContratoDto.setValorMensualUpc(negociacionDto.getValorUpcMensual());
//            legalizacionContratoDto.setPorcentajeUpcTotal(negociacionDto.getPorcentajeRecuperacion());
//            legalizacionContratoDto.setPorcentajeUpcPyp(negociacionDto.getPorcentajePYP());
        }
    }

    public void calculaDuracionContrato() {
        if (getLegalizacionContratoDto().getContratoDto().getFechaFinVigencia()
                != null && getLegalizacionContratoDto().getContratoDto().getFechaInicioVigencia() != null) {
        	Calendar calendar = Calendar.getInstance();
			calendar.setTime(getLegalizacionContratoDto().getContratoDto().getFechaFinVigencia());
			calendar.set(Calendar.HOUR,24);
			getLegalizacionContratoDto().getContratoDto().setFechaFinVigencia(calendar.getTime());
            getLegalizacionContratoDto().getContratoDto().setDuracionContrato(
                    dateUtils.calcularFechaLetras(getLegalizacionContratoDto().getContratoDto().getFechaInicioVigencia(),
                    		getLegalizacionContratoDto().getContratoDto().getFechaFinVigencia()));
        }
    }

    public void buscarDepartamentos() {
        this.departamentosFirma = (legalizacionContratoDto.getContratoDto().getRegionalDto() == null)
                ? this.parametrosFacade.listarDepartamentos() :
                this.parametrosFacade.listarDepartamentosPorRegional(legalizacionContratoDto.getContratoDto().getRegionalDto().getId());
    }

    public void buscarMunicipios() {
        this.municipiosFirma = this.parametrosFacade.listarMunicipiosPorDepartameto(this.getLegalizacionContratoDto().getDepartamentoFirma().getId());
    }

    public void buscarResponsables() {
        final Integer regionalId = this.getLegalizacionContratoDto().getContratoDto().getRegionalDto().getId();
        this.responsablesContratoFirma = this.legalizacionFacade.listarResponsablesFirma(regionalId);
        this.responsablesContratoVoBo = this.legalizacionFacade.listarResponsablesVoBo(regionalId);
        this.buscarDepartamentos();
    }

    public void agregarDescuento() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Boolean validaError = false;
        if (this.getLegalizacionContratoDto().getDescuento().getTipoDescuento() == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "El tipo de descuento es requerido.", "Error"));
            validaError = true;
        }
        if (this.getLegalizacionContratoDto().getDescuento().getValorCondicion() == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "El valor de la condición es obligatorio.", "Error"));
            validaError = true;
        }
        if (this.getLegalizacionContratoDto().getDescuento().getTipoCondicion() == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Por favor seleccione el tipo condición.", "Error"));
            validaError = true;
        }
        if (this.getLegalizacionContratoDto().getDescuento().getValorDescuento() == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "El valor del descuento es obligatorio.", "Error"));
            validaError = true;
        }
        if (this.getLegalizacionContratoDto().getDescuento().getTipoValor() == null) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Por favor seleccione el tipo de valor.", "Error"));
            validaError = true;
        }
        if (this.getLegalizacionContratoDto().getDescuento().getDetalle() == null ||
                "".equals(this.getLegalizacionContratoDto().getDescuento().getDetalle())) {
            facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "El detalle es obligatorio.", "Error"));
            validaError = true;
        }
        if (!validaError) {
            this.getLegalizacionContratoDto().getDescuentos().add(getLegalizacionContratoDto().getDescuento());
            getLegalizacionContratoDto().setDescuento(new DescuentoDto());
        }
    }

    public void limpiar() {
        facesUtils.urlRedirect("/legalizacion/generarMinutaCapita/" + this.idSolicitudContratacion + "/");
    }

    public void guardarLegalizacionContrato() {
        try {
            prestadorController.guardarPrestador();
            this.getLegalizacionContratoDto().setUserId(user.getId());
            this.getLegalizacionContratoDto().getContratoDto().setUserId(user.getId());
            this.getLegalizacionContratoDto().setMunicipioPrestadorDto(this.prestadorController.getMunicipioDto());
            this.getLegalizacionContratoDto().setDireccionPrestador(this.prestadorController.getDireccion());
            if(legalizacionContratoDto.getEstadoLegalizacion().getDescripcion().equals("Legalizada")){
            	this.getLegalizacionContratoDto().setEstadoLegalizacion(EstadoLegalizacionEnum.CONTRATO_SIN_VB);
            	this.getLegalizacionContratoDto().getContratoDto().getSolicitudContratacionParametrizableDto().setEstadoLegalizacion(EstadoLegalizacionEnum.CONTRATO_SIN_VB.getDescripcion());
            	this.legalizacionFacade.actualizarEstadoContrato(legalizacionContratoDto);
            }
            this.setLegalizacionContratoDto(this.generarMinutaAction.guardarLegalizacionContrato(this.getLegalizacionContratoDto()));
            FacesContext context = FacesContext.getCurrentInstance();
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Se guardo "
                        + "correctamente la legalización del contrato ahora puede descargarla.", ""));
                context.getExternalContext().getFlash().setKeepMessages(true);
            this.limpiar();
        } catch (ConexiaBusinessException ex) {
            Logger.getLogger(GenerarMinutaCapitaController.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException e) {
        	Logger.getLogger(GenerarMinutaCapitaController.class.getName()).log(Level.SEVERE, null, e);
		}
    }

    public void generarMinuta() throws UnsupportedEncodingException {
        try {
            this.pdfFile = this.generarMinutaAction.generarMinuta(this.getLegalizacionContratoDto());
        } catch (ConexiaBusinessException ex) {
            Logger.getLogger(GenerarMinutaCapitaController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void goTo(String url) {
        facesUtils.urlRedirect(url);
    }

    public void aplicarDescuento() {
        if (this.getLegalizacionContratoDto().getAplicaDecuento()) {
            RequestContext.getCurrentInstance().execute("PF('panelDescuento').toggle()");
        } else {
            RequestContext.getCurrentInstance().execute("PF('panelDescuento').collapse()");
        }
    }

    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">

    public Date getToday() {
        LocalDate date = LocalDate.now();
        return Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * @return the minutas
     */
    public List<MinutaDto> getMinutas() {
        return minutas;
    }

    /**
     * @param minutas the minutas to set
     */
    public void setMinutas(List<MinutaDto> minutas) {
        this.minutas = minutas;
    }

    /**
     * @return the tipoContrato
     */
    public List<TipoObjetoContratoEnum> getTiposContrato() {
        return tiposContrato;
    }

    /**
     * @param tipoContrato the tipoContrato to set
     */
    public void setTiposContrato(List<TipoObjetoContratoEnum> tipoContrato) {
        this.tiposContrato = tipoContrato;
    }

    /**
     * @return the departamentosFirma
     */
    public List<DepartamentoDto> getDepartamentosFirma() {
        return departamentosFirma;
    }

    /**
     * @param departamentosFirma the departamentosFirma to set
     */
    public void setDepartamentosFirma(List<DepartamentoDto> departamentosFirma) {
        this.departamentosFirma = departamentosFirma;
    }

    /**
     * @return the municipiosFirma
     */
    public List<MunicipioDto> getMunicipiosFirma() {
        return municipiosFirma;
    }

    /**
     * @param municipiosFirma the municipiosFirma to set
     */
    public void setMunicipiosFirma(List<MunicipioDto> municipiosFirma) {
        this.municipiosFirma = municipiosFirma;
    }

    /**
     * @return the tiposDescuento
     */
    public List<TipoDescuentoEnum> getTiposDescuento() {
        return tiposDescuento;
    }

    /**
     * @param tiposDescuento the tiposDescuento to set
     */
    public void setTiposDescuento(List<TipoDescuentoEnum> tiposDescuento) {
        this.tiposDescuento = tiposDescuento;
    }

    /**
     * @return the tiposValor
     */
    public List<TipoValorEnum> getTiposValor() {
        return tiposValor;
    }

    /**
     * @param tiposValor the tiposValor to set
     */
    public void setTiposValor(List<TipoValorEnum> tiposValor) {
        this.tiposValor = tiposValor;
    }

    /**
     * @return the tiposCondicion
     */
    public List<TipoCondicionEnum> getTiposCondicion() {
        return tiposCondicion;
    }

    /**
     * @param tiposCondicion the tiposCondicion to set
     */
    public void setTiposCondicion(List<TipoCondicionEnum> tiposCondicion) {
        this.tiposCondicion = tiposCondicion;
    }

    /**
     * @return the nivelesContrato
     */
    public List<NivelContratoEnum> getNivelesContrato() {
        return nivelesContrato;
    }

    /**
     * @param nivelesContrato the nivelesContrato to set
     */
    public void setNivelesContrato(List<NivelContratoEnum> nivelesContrato) {
        this.nivelesContrato = nivelesContrato;
    }

    /**
     * @return the legalizacionContratoDto
     */
    public LegalizacionContratoDto getLegalizacionContratoDto() {
        return legalizacionContratoDto;
    }

    /**
     * @param legalizacionContratoDto the legalizacionContratoDto to set
     */
    public void setLegalizacionContratoDto(LegalizacionContratoDto legalizacionContratoDto) {
        this.legalizacionContratoDto = legalizacionContratoDto;
    }

    /**
     * @return the regionales
     */
    public List<RegionalDto> getRegionales() {
        return regionales;
    }

    /**
     * @param regionales the regionales to set
     */
    public void setRegionales(List<RegionalDto> regionales) {
        this.regionales = regionales;
    }

    /**
     * @return the responsablesContratoFirma
     */
    public List<ResponsableContratoDto> getResponsablesContratoFirma() {
        return responsablesContratoFirma;
    }

    /**
     * @param responsablesContratoFirma the responsablesContratoFirma to set
     */
    public void setResponsablesContratoFirma(List<ResponsableContratoDto> responsablesContratoFirma) {
        this.responsablesContratoFirma = responsablesContratoFirma;
    }

    /**
     * @return the responsablesContratoVoBo
     */
    public List<ResponsableContratoDto> getResponsablesContratoVoBo() {
        return responsablesContratoVoBo;
    }

    /**
     * @param responsablesContratoVoBo the responsablesContratoVoBo to set
     */
    public void setResponsablesContratoVoBo(List<ResponsableContratoDto> responsablesContratoVoBo) {
        this.responsablesContratoVoBo = responsablesContratoVoBo;
    }

    /**
     * @return the pdfFile
     */
    public StreamedContent getPdfFile() {
        return pdfFile;
    }

    /**
     * @param pdfFile the pdfFile to set
     */
    public void setPdfFile(StreamedContent pdfFile) {
        this.pdfFile = pdfFile;
    }

    public Long getIdSolicitudContratacion() {
        return this.idSolicitudContratacion;
    }

    public void setIdSolicitudContratacion(final Long idSolicitudContratacion) {
        this.idSolicitudContratacion = idSolicitudContratacion;
    }

	public List<IncentivoModeloDto> getIncentivos() {
		return incentivos;
	}

	public void setIncentivos(List<IncentivoModeloDto> incentivos) {
		this.incentivos = incentivos;
	}

	public List<IncentivoModeloDto> getModelos() {
		return modelos;
	}

	public void setModelos(List<IncentivoModeloDto> modelos) {
		this.modelos = modelos;
	}


    //</editor-fold>

}
