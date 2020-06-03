package com.conexia.contractual.wap.controller.parametrizacion;

import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.wap.facade.parametrizacion.ParametrizacionFacade;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroMedicamentoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroPaqueteDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroServicioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroTransporteDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import java.io.Serializable;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

/**
 * Controller creado para la opcion de Editar prestador y listar sus sedes.
 *
 * @author jLopez
 */
@Named
@ViewScoped
@URLMapping(id = "parametrizarTecnologias", pattern = "/parametrizacion/parametrizarTecnologias/#{parametrizarTecnologiasController.idSolicitudContratacion}/#{parametrizarTecnologiasController.idSede}/#{parametrizarTecnologiasController.totalSedesNegociadas}",
        viewId = "/parametrizacion/parametrizarTecnologias.page")
public class ParametrizarTecnologiasController implements Serializable {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1526929041950670012L;

    /**
     * Id de la sede seleccionada.
     */
    private Long idSede;

    /**
     * Total de sedes negociadas.
     */
    private Integer totalSedesNegociadas;

    private Long idSolicitudContratacion;

    /**
     * Sedes prestador a parametrizar.
     */
    private SedePrestadorDto sede;

    /**
     * Dto de la solicitud de contratacion que se esta modificando.
     */
    private SolicitudContratacionParametrizableDto solicitudContratacionParametrizableDto;

    /**
     * Controlador de medicamentos.
     */
    @Inject
    private ParametrizarMedicamentoController medicamentosController;

    /**
     * Controlador de Servicios.
     */
    @Inject
    private ParametrizarServicioController servicioController;

    @Inject
    private ParametrizarPaqueteController paqueteController;

    /**
     * Utilidades Faces.
     */
    @Inject
    private FacesUtils facesUtils;

    /**
     * Parametrizacion Facade
     */
    @Inject
    private ParametrizacionFacade parametrizacionFacade;

    @Inject
    @UserInfo
    private UserApp user;

    @Inject
    private Log log;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    /**
     * Flayer por si la solicitud ya esta parametrizada no;
     */
    private int solicitudContratacionSede;

    private String negociacionId;

    private SedePrestadorDto sedeSession;
    /**
     * Constructor.
     */
    public ParametrizarTecnologiasController() {
    }

    @PostConstruct
    public void onload(){
    	 FacesContext facesContext = FacesContext.getCurrentInstance();
         HttpSession sessionNeg = (HttpSession) facesContext.getExternalContext().getSession(true);
         negociacionId = (String) sessionNeg.getAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString());

         HttpSession sessionSed = (HttpSession) facesContext.getExternalContext().getSession(true);
         sedeSession  = (SedePrestadorDto) sessionSed.getAttribute(NegociacionSessionEnum.SEDE_PRESTADOR.toString());

    }

    public void goToSolicitud() {
        facesUtils.urlRedirect("/parametrizacion/parametrizarContrato/" + this.idSolicitudContratacion + "/");
    }

    /**
     *
     * @return true si el contrato se diligencio correctamente
     */

    public void setearFiltros() {
        FiltroServicioDto filtroServicio = new FiltroServicioDto();
        filtroServicio.setSedeNegociacionId(this.getSede().getSedeNegociacionId());
        servicioController.setFiltroServicioDto(filtroServicio);
        FiltroMedicamentoDto filtroMedicamento = new FiltroMedicamentoDto();
        filtroMedicamento.setSedeNegociacionId(this.getSede().getSedeNegociacionId());
        medicamentosController.setFiltroMedicamento(filtroMedicamento);
        FiltroTransporteDto filtroTraslado = new FiltroTransporteDto();
        filtroTraslado.setSedeNegociacionId(this.getSede().getSedeNegociacionId());
        FiltroPaqueteDto filtroPaqueteDto = new FiltroPaqueteDto();
        filtroPaqueteDto.setSedeNegociacionId(this.getSede().getSedeNegociacionId());
        paqueteController.setFiltroPaqueteDto(filtroPaqueteDto);
    }

    public void limpiar() {
        facesUtils.urlRedirect("/parametrizacion/parametrizarTecnologias/" + idSolicitudContratacion + "/" + idSede + "/" + this.totalSedesNegociadas);
    }

    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
    /**
     * @return the sede
     */
    public SedePrestadorDto getSede() {
        return sede;
    }

    /**
     * @param sede the sede to set
     */
    public void setSede(SedePrestadorDto sede) {
        this.sede = sede;

    }

    /**
     * @return the idSede
     */
    public Long getIdSede() {
        return idSede;
    }

    /**
     * @return the totalSedesNegociadas
     */
    public Integer getTotalSedesNegociadas() {
        return totalSedesNegociadas;
    }

    /**
     * @param totalSedesNegociadas the totalSedesNegociadas to set
     */
    public void setTotalSedesNegociadas(Integer totalSedesNegociadas) {
        this.totalSedesNegociadas = totalSedesNegociadas;
    }

    public Long getIdSolicitudContratacion() {
        return this.idSolicitudContratacion;
    }

    public void setIdSolicitudContratacion(final Long idSolicitudContratacion) {
        this.idSolicitudContratacion = idSolicitudContratacion;
    }

    /**
     * @param idSede the idSede to set
     */
    public void setIdSede(Long idSede)
    {
        try
        {
            this.idSede = idSede;
            if (sede == null) {
                this.sede = this.parametrizacionFacade.obtieneSedePorParametrizar(this.idSede);
                this.setSolicitudContratacionParametrizableDto(this.parametrizacionFacade.obtenerSolicitud(this.getIdSolicitudContratacion()));
                this.solicitudContratacionSede = this.parametrizacionFacade.cuentaSolicitudContratacionSede(this.sede.getSedeNegociacionId(), this.getIdSolicitudContratacion());
                setearFiltros();
                if (servicioController.contarServiciosPorParametrizar() == 0
                        && medicamentosController.contarGrupoMedicamentoPorParametrizar() == 0
                        && paqueteController.contarPaquetesPorParametrizar() == 0) {
                    this.goToSolicitud();
                }
            }
        }catch (Exception e) {
            log.error("Error tieneOtroSi : ", e);
            facesMessagesUtils.addError(this.exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * @return the solicitudContratacionParametrizableDto
     */
    public SolicitudContratacionParametrizableDto getSolicitudContratacionParametrizableDto() {
        return solicitudContratacionParametrizableDto;
    }

    /**
     * @param solicitudContratacionParametrizableDto the
     * solicitudContratacionParametrizableDto to set
     */
    public void setSolicitudContratacionParametrizableDto(SolicitudContratacionParametrizableDto solicitudContratacionParametrizableDto) {
        this.solicitudContratacionParametrizableDto = solicitudContratacionParametrizableDto;
    }

    public int getSolicitudContratacionSede() {
        return solicitudContratacionSede;
    }

    public void setSolicitudContratacionSede(int solicitudContratacionSede) {
        this.solicitudContratacionSede = solicitudContratacionSede;
    }

	public String getNegociacionId() {
		return negociacionId;
	}

	public void setNegociacionId(String negociacionId) {
		this.negociacionId = negociacionId;
	}

	public SedePrestadorDto getSedeSession() {
		return sedeSession;
	}

	public void setSedeSession(SedePrestadorDto sedeSession) {
		this.sedeSession = sedeSession;
	}


//</editor-fold>
}
