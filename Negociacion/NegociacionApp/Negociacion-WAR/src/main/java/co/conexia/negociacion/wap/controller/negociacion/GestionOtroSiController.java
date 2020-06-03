/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.conexia.negociacion.wap.controller.negociacion;

import co.conexia.negociacion.wap.facade.common.CommonFacade;
import co.conexia.negociacion.wap.facade.negociacion.GestionNegociacionFacade;
import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;

/**
 *
 * @author aquintero
 */
@Named
@ViewScoped
public class GestionOtroSiController implements Serializable
{

    /**
     *
     */
    private static final long serialVersionUID = 9012880232387967757L;

    @Inject
    private GestionNegociacionFacade gestionNegociacionFacade;

    @Inject
    private CommonFacade commonFacade;

    @Inject
    private NegociacionFacade negociacionFacade;

    @Inject
    private GestionNegociacionController gestionNegociacionController;

    @Inject
    private FacesUtils faceUtils;

    @Inject
    private Log logger;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private PreContractualExceptionUtils exceptionUtils;

    @Inject
    @UserInfo
    private UserApp user;

    @Inject
    private CommonFacade facade;

    private NegociacionDto negociacionOtroSi;
    private TipoOtroSiEnum tipoOtroSi = TipoOtroSiEnum.NO_APLICA;
    private TipoModificacionOtroSiEnum tipoModificacionOtroSi = TipoModificacionOtroSiEnum.NO_APLICA;
    private List<Long> sedesCero = new ArrayList<>();
    private String cargarContenido;
    private Date fechaInicioProrroga;
    private Date fechaFinProrroga;
    private Date fechaInicioProrrogaModif;

    private List<SedePrestadorDto> sedesPrestador = new ArrayList<>();
    private List<SedePrestadorDto> sedesPrestadorSeleccionadas = new ArrayList<>();
    private List<MunicipioDto> zonasMunicipioCobertura;
    private boolean generarProrrogModif = false;
    private Boolean btnGenera = true;

    NegociacionDto negociacionOtrSiProrroga = null;

    Boolean lanzarDialogoSedeOtroSi = false;

    private static final TimeZone timeZone = TimeZone.getTimeZone("America/Colombia");

    List<SedesNegociacionDto> sedesNegociacion;

    public static Boolean btnRegresar;

    public GestionOtroSiController() {

    }

    public void reset() {
        generarProrrogModif = false;
        btnGenera = true;
        negociacionOtrSiProrroga = null;
        lanzarDialogoSedeOtroSi = false;
        tipoOtroSi = TipoOtroSiEnum.NO_APLICA;
        this.cargarContenido = null;
        this.tipoModificacionOtroSi = TipoModificacionOtroSiEnum.NO_APLICA;
    }

    @PostConstruct
    public void postConstruct() {
    }

    /**
     * Permite asignar la negociación a la que se crea Otro si
     */
    public void seleccionarNegociacionOtroSi(NegociacionDto negociacionDto) {
        GestionOtroSiController.btnRegresar = false;
        fechaFinProrroga = null;
        btnGenera = true;
        if (Objects.isNull(negociacionDto.getId())) {
            this.faceUtils.urlRedirect("/bandejaPrestador");
        } else {
            List<SedesNegociacionDto> listSedesNegociacionPadre = commonFacade.listarSedesNegociacionById(negociacionDto.getId());
            this.negociacionOtroSi = negociacionDto;
            this.negociacionOtroSi.setNegociacionPadre(negociacionDto);
            this.negociacionOtroSi.getNegociacionPadre().setSedesNegociacion(listSedesNegociacionPadre);
            this.negociacionOtroSi.setSedesNegociacion(listSedesNegociacionPadre);

        }
    }

    /**
     * Method to validate the state of each negotiation is CURRENT(VIGENTE)
     * @param state     Negotiation State
     * @return Boolean  True(It's correct), False(It isn't correct)
     */
    public Boolean validateContractDescriptionState(String state)
    {
        return ContractStateDescriptionEnum.CURRENT.getState().equals(state);
    }

    public String titleDialog() {
        return Objects.nonNull(this.negociacionOtroSi) ? (Objects.nonNull(this.negociacionOtroSi.getNegociacionOrigen())
                ? this.negociacionOtroSi.getNegociacionOrigen().getId() + " - " + this.negociacionOtroSi.getNumeroOtroSi()
                : "" + this.negociacionOtroSi.getId()) : "";
    }

    /*private void consultarSedesNegociacionOtroSi() {
        List<SedePrestadorDto> sedesPrestadores = negociacionFacade.consultarSedesPuedenNegociar(gestionNegociacionController.getPrestadorSeleccionado(), negociacionOtroSi);
        this.sedesNegociacion = this.negociacionFacade.listarSedesPorNegociacion(negociacionOtroSi.getId());

        for (int i = 0; i < sedesPrestadores.size(); i++) {
            for (int j = 0; j < this.sedesNegociacion.size(); j++) {
                if (sedesPrestadores.get(i).getId().equals(this.sedesNegociacion.get(j).getSedeId())) {
                    this.sedesPrestadorSeleccionadas.add(sedesPrestadores.get(i));
                    if (this.sedesNegociacion.get(j).getSedePrestador().getSedePrincipal()
                            == null ? false : this.sedesNegociacion.get(j).getSedePrestador().getSedePrincipal()) {
                        this.negociacionOtroSi.setSedePrincipal(new SedePrestadorDto());
                        this.negociacionOtroSi.getSedePrincipal().setCodigoHabilitacion(this.sedesNegociacion.get(j).getSedePrestador().getCodigoHabilitacion());
                        this.negociacionOtroSi.getSedePrincipal().setCodigoSede(this.sedesNegociacion.get(j).getSedePrestador().getCodigoSede());
                        this.negociacionOtroSi.getSedePrincipal().setNombreSede(this.sedesNegociacion.get(j).getSedePrestador().getNombreSede());
                        sedesPrestadores.get(i).setSedePrincipal(true);
                    }
                }
            }
            this.sedesPrestador.add(sedesPrestadores.get(i));
        }

    }*/

    /**
     * Valida que la fecha inicio de la prórroga sea un día después de la fecha
     * inicio del contrato de la negociación
     */
    public void validarFechaInicioProrroga() {

        Calendar c = Calendar.getInstance();

        this.fechaFinProrroga = null;
        this.tipoModificacionOtroSi = TipoModificacionOtroSiEnum.NO_APLICA;
        this.cargarContenido = null;

        negociacionOtroSi.setTipoOtroSi(tipoOtroSi);
        if (tipoOtroSi.equals(tipoOtroSi.MODIFICACION_Y_PRORROGA)
                || tipoOtroSi.equals(tipoOtroSi.PRORROGA)) {

            try {
                NegociacionDto negociacionFecha = this.negociacionFacade.consultarFechasVigenciaOtroSi(negociacionOtroSi.getId());
                if (negociacionFecha.getContrato().getFechaFinVigencia() == null) {
                    this.fechaInicioProrroga = this.negociacionFacade.consultarFechaFinContratoPadre(negociacionOtroSi);
                    fechaInicioProrrogaModif = this.negociacionFacade.consultarFechaFinContratoPadre(negociacionOtroSi);
                } else {
                    c.setTime(negociacionFecha.getContrato().getFechaFinVigencia());
                    c.add(Calendar.DATE, 1);
                    
                    this.fechaInicioProrroga = c.getTime();
                    this.fechaInicioProrrogaModif = c.getTime();
                }
            } catch (ConexiaBusinessException ex) {
                Logger.getLogger(GestionOtroSiController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public boolean showModificacion() {
        if (tipoOtroSi != null && tipoOtroSi.equals(TipoOtroSiEnum.MODIFICACION_Y_O_CORRECCION)
                || generarProrrogModif) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Permite validar la fecha fin de prórroga ingresada
     */
    public void validarFechaFinProrroga() {
        Boolean msg = false;
        Date fechaIni = null;
        String otrosiError = null;
        Boolean validaFechaFinModificacionProrroga = false;
        if (tipoOtroSi.equals(TipoOtroSiEnum.MODIFICACION_Y_PRORROGA)) {
            fechaIni = this.fechaInicioProrrogaModif;
            validaFechaFinModificacionProrroga = true;
        } else if (tipoOtroSi.equals(TipoOtroSiEnum.PRORROGA)) {
            fechaIni = this.fechaInicioProrroga;
        }
        if (Objects.nonNull(fechaIni)) {
            if (validaFechaFinModificacionProrroga) {
                if (Objects.nonNull(negociacionOtroSi.getContrato().getFechaFinVigencia())) {
                    if (!fechaFinProrroga.after(this.negociacionOtroSi.getContrato().getFechaFinVigencia())) {
                        msg = true;
                        this.setBtnGenera(true);
                        otrosiError = resourceBundle.getString("negociacion_otrosi_error_fecha_fin_prorroga_modificacion");
                    }
                }
            } else {
                if (!this.fechaFinProrroga.after(fechaIni)) {
                    msg = true;
                    this.setBtnGenera(true);
                    otrosiError = resourceBundle.getString("negociacion_otrosi_error_fecha_fin_fechainicio");
                }
            }
        }
        if (msg) {
            facesMessagesUtils.addWarning(otrosiError);
            Ajax.update("pnlConsultaTecnologiasContratadas");
            Ajax.update("pnlProrroga");
        } else {
            this.setBtnGenera(false);
        }
    }

    private boolean prorrogar(Date fechaProrroga) {
        Date fechaIniProrroga = fechaProrroga;
        try {
            if ((Objects.nonNull(fechaFinProrroga)
                    && this.fechaFinProrroga.after(fechaIniProrroga))) {
                negociacionOtroSi.setObservacion("TECNOLOGÍA CON PRÓRROGA POR OTRO SI");
                negociacionOtroSi = this.negociacionFacade.crearNegociacionOtroSi(negociacionOtroSi, user.getId(), tipoOtroSi);
                this.negociacionFacade.terminarCreacionNegociacionOtroSi(negociacionOtroSi, user.getId(), tipoOtroSi, tipoModificacionOtroSi, true, sedesCero);
                this.negociacionFacade.actualizarFechasProrrogaTecnologias(fechaIniProrroga, fechaFinProrroga, negociacionOtroSi.getId());
                this.negociacionFacade.finalizarNegociacion(negociacionOtroSi);
                this.negociacionFacade.cambiarFechaContratoByNegociacionId(fechaIniProrroga, fechaFinProrroga, negociacionOtroSi.getId());
                return true;
            } else {
                facesMessagesUtils.addWarning(resourceBundle.getString("negociacion_otrosi_error_fecha_fin_fechainicio"));
            }
        } catch (ConexiaBusinessException ex) {
            ex.printStackTrace();
            Logger.getLogger(GestionOtroSiController.class.getName()).log(Level.SEVERE, null, ex);
            facesMessagesUtils.addError(exceptionUtils.createMessage(resourceBundle, ex));
        }
        return false;
    }

    private NegociacionDto prorrogarOtroSi(Date fechaProrroga)
    {
        Date fechaIniProrroga = fechaProrroga;
        try {
            if ((Objects.nonNull(fechaFinProrroga)
                    && this.fechaFinProrroga.after(fechaIniProrroga))) {
                negociacionOtroSi.setObservacion("TECNOLOGÍA CON PRÓRROGA POR OTRO SI + TECNOLOGÍA MODIFICADA/CORREGIDA POR OTRO SI");
                negociacionOtrSiProrroga = this.negociacionFacade.crearNegociacionOtroSi(negociacionOtroSi, user.getId(), tipoOtroSi);
                this.negociacionFacade.terminarCreacionNegociacionOtroSi(negociacionOtrSiProrroga, user.getId(), tipoOtroSi, tipoModificacionOtroSi, true, sedesCero);
                this.negociacionFacade.actualizarFechasProrrogaTecnologias(fechaIniProrroga, fechaFinProrroga, negociacionOtrSiProrroga.getId());
                this.negociacionFacade.finalizarNegociacion(negociacionOtrSiProrroga);
                this.negociacionFacade.asignarFechasOtroSiContrato(fechaIniProrroga, fechaFinProrroga, negociacionOtrSiProrroga.getNegociacionPadre().getId());
                this.gestionNegociacionController.postConstruct();
                return negociacionOtrSiProrroga;
            } else {
                facesMessagesUtils.addWarning(resourceBundle.getString("negociacion_otrosi_error_fecha_fin_fechainicio"));
            }
        } catch (ConexiaBusinessException ex) {
            ex.printStackTrace();
            Logger.getLogger(GestionOtroSiController.class.getName()).log(Level.SEVERE, null, ex);
            facesMessagesUtils.addError(exceptionUtils.createMessage(resourceBundle, ex));
        }
        return null;
    }

    private void prorroga() {
        generarProrrogModif = false;
        if (prorrogar(this.fechaInicioProrroga)) {
            this.facesMessagesUtils.addInfo(this.resourceBundle.getString("negociacion_otrosi_prorroga"));
            gestionNegociacionController.consultarNegociacionesOtroSi(negociacionOtroSi.getNegociacionOrigen());
            RequestContext.getCurrentInstance().execute("PF('OtroSiDlg').hide()");
            RequestContext.getCurrentInstance().update("@([id$=tblNegociacionesPrestadorForm])");
        }
    }

    /*private void modificacionProrroga() {
        if (!generarProrrogModif) {
            negociacionOtrSiProrroga = prorrogarOtroSi(this.fechaInicioProrroga);
            if (negociacionOtrSiProrroga.getId() != null) {
                this.facesMessagesUtils.addInfo(this.resourceBundle.getString("negociacion_otrosi_modifica_prorroga"));
                generarProrrogModif = true;
            }
        } else {
            modifiCorrecciontarifas(negociacionOtrSiProrroga);
        }
    }*/

    /*private void modifiCorrecciontarifas(NegociacionDto negociacionOtrSiProrroga) {
        generarProrrogModif = false;
        if (Objects.nonNull(tipoModificacionOtroSi)) {
            switch (tipoModificacionOtroSi) {
                case GESTION_TARIFAS:
                    if (Objects.nonNull(cargarContenido)) {
                        if (cargarContenido.equals("Si")) {
                            try {
                                this.negociacionFacade.terminarCreacionNegociacionOtroSi(negociacionOtrSiProrroga, user.getId(), tipoOtroSi, tipoModificacionOtroSi, true, sedesCero);
                                RequestContext.getCurrentInstance().execute("PF('OtroSiDlg').hide()");
                            } catch (ConexiaBusinessException ex) {
                                ex.printStackTrace();
                                Logger.getLogger(GestionOtroSiController.class.getName()).log(Level.SEVERE, null, ex);
                                facesMessagesUtils.addError(exceptionUtils.createMessage(resourceBundle, ex));
                            }
                        } else if (cargarContenido.equals("No")) {
                            try {
                                this.negociacionFacade.terminarCreacionNegociacionOtroSi(negociacionOtrSiProrroga, user.getId(), tipoOtroSi, tipoModificacionOtroSi, false, sedesCero);
                                RequestContext.getCurrentInstance().execute("PF('OtroSiDlg').hide()");
                            } catch (ConexiaBusinessException ex) {
                                ex.printStackTrace();
                                Logger.getLogger(GestionOtroSiController.class.getName()).log(Level.SEVERE, null, ex);
                                facesMessagesUtils.addError(exceptionUtils.createMessage(resourceBundle, ex));
                            }
                        }
                    } else {
                        this.facesMessagesUtils.addWarning(this.resourceBundle.getString("negociacion_otro_si_error_cargue_contenido_erroneo"));
                    }
                    break;
                default:
                    break;
            }
            negociacionOtrSiProrroga.setNumeroOtroSi(this.negociacionFacade.obtenerNumeroOtroSi(negociacionOtrSiProrroga.getNegociacionPadre().getId()));
            negociacionOtrSiProrroga.setId(this.negociacionFacade.buscarIdNegOtroSi(negociacionOtrSiProrroga));
            this.faceUtils.urlRedirect("sedesasede/detalle");
        } else {
            this.facesMessagesUtils.addWarning(
                    this.resourceBundle.getString("negociacion_otrosi_error_sin_tipo_modificacion"));
        }
    }*/

    /*private void modifiCorrecciontarifas() {
        generarProrrogModif = false;
        if (Objects.nonNull(tipoModificacionOtroSi)) {
            switch (tipoModificacionOtroSi) {
                case GESTION_TARIFAS:
                    if (Objects.nonNull(cargarContenido)) {
                        if (cargarContenido.equals("Si")) {
                            try {
                                negociacionOtroSi = this.negociacionFacade.crearNegociacionOtroSi(negociacionOtroSi, user.getId(), tipoOtroSi);
                                this.negociacionFacade.terminarCreacionNegociacionOtroSi(negociacionOtroSi, user.getId(), tipoOtroSi, tipoModificacionOtroSi, true, sedesCero);
                                RequestContext.getCurrentInstance().execute("PF('OtroSiDlg').hide()");
                                this.faceUtils.urlRedirect("/sedesasede/detalle");
                            } catch (ConexiaBusinessException ex) {
                                ex.printStackTrace();
                                Logger.getLogger(GestionOtroSiController.class.getName()).log(Level.SEVERE, null, ex);
                                facesMessagesUtils.addError(exceptionUtils.createMessage(resourceBundle, ex));
                            }
                        } else if (cargarContenido.equals("No")) {
                            try {
                                negociacionOtroSi = this.negociacionFacade.crearNegociacionOtroSi(negociacionOtroSi, user.getId(), tipoOtroSi);
                                this.negociacionFacade.terminarCreacionNegociacionOtroSi(negociacionOtroSi, user.getId(), tipoOtroSi, tipoModificacionOtroSi, false, sedesCero);
                                RequestContext.getCurrentInstance().execute("PF('OtroSiDlg').hide()");
                                this.faceUtils.urlRedirect("/sedesasede/detalle");
                            } catch (ConexiaBusinessException ex) {
                                ex.printStackTrace();
                                Logger.getLogger(GestionOtroSiController.class.getName()).log(Level.SEVERE, null, ex);
                                facesMessagesUtils.addError(exceptionUtils.createMessage(resourceBundle, ex));
                            }
                        }
                    } else {
                        this.facesMessagesUtils.addWarning(this.resourceBundle.getString("negociacion_otro_si_error_cargue_contenido_erroneo"));
                    }
                    break;
                default:
                    break;
            }
            negociacionOtroSi.setNumeroOtroSi(this.negociacionFacade.obtenerNumeroOtroSi(negociacionOtroSi.getNegociacionPadre().getId()));
            negociacionOtroSi.setId(this.negociacionFacade.buscarIdNegOtroSi(negociacionOtroSi));
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
            session.setAttribute(NegociacionSessionEnum.NEGOCIACION.toString(), negociacionOtroSi);
            session.setAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString(), negociacionOtroSi.getId());
            session.setAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString(), negociacionOtroSi.getPrestador().getId());
        } else {
            this.facesMessagesUtils.addWarning(
                    this.resourceBundle.getString("negociacion_otrosi_error_sin_tipo_modificacion"));
        }
    }*/

    /**
     * Para gestionar la creación de una negociación Otro si
     */
    public void crearNegociacionOtroSi() throws ConexiaBusinessException {
        try {
            //Validar que la negociación padre no tenga negociaones otro si que no estén legalizadas
            if (this.negociacionFacade.validarNegociacionesOtrosSiSinLegalizar(negociacionOtroSi) || this.negociacionFacade.validarNegociacionesOtrosSiSinLegalizar(negociacionOtrSiProrroga)) {
                if (Objects.nonNull(tipoOtroSi)) {
                    negociacionOtroSi.setUsuarioCreacionId(user.getId());
                    negociacionOtroSi.setEsOtroSi(Boolean.TRUE);
                    negociacionOtroSi.setNegociacionPadre(new NegociacionDto());
                    negociacionOtroSi.getNegociacionPadre().setId(negociacionOtroSi.getId());
                    negociacionOtroSi.setFechaConcertacionMx(new Date());
                    negociacionOtroSi.setFechaConcertacionPx(new Date());
                    negociacionOtroSi.setFechaConcertacionPq(new Date());
                    switch (tipoOtroSi) {
                        case MODIFICACION_Y_O_CORRECCION:
                            //modifiCorrecciontarifas();
                            break;
                        case PRORROGA:
                            prorroga();
                            break;
                        case MODIFICACION_Y_PRORROGA:
                            //modificacionProrroga();
                            break;
                        default:
                            break;
                    }
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                    if (negociacionOtrSiProrroga != null) {
                        session.setAttribute(NegociacionSessionEnum.NEGOCIACION.toString(), negociacionOtrSiProrroga);
                    }
                    if (negociacionOtrSiProrroga != null && negociacionOtrSiProrroga.getId() != null) {
                        session.setAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString(), negociacionOtrSiProrroga.getId());
                    }
                    if (negociacionOtrSiProrroga != null && negociacionOtrSiProrroga.getPrestador() != null
                            && negociacionOtrSiProrroga.getPrestador().getId() != null) {
                        session.setAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString(), negociacionOtrSiProrroga.getPrestador().getId());
                    }
                } else {
                    this.facesMessagesUtils.addWarning(this.resourceBundle.getString("negociacion_otrosi_error_sin_tipo_otro_si"));
                }
            } else {
                this.facesMessagesUtils.addWarning(this.resourceBundle.getString("negociacion_otrosi_error_pendientes_legalizar"));
            }
        } catch (ConexiaBusinessException ex) {
            this.facesMessagesUtils.addError(exceptionUtils.createMessage(resourceBundle, ex));
            ex.printStackTrace();
            Logger.getLogger(GestionOtroSiController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean validarModOtroSi() {
        if (this.tipoOtroSi != null) {
            if (getTipoOtroSi().equals(TipoOtroSiEnum.PRORROGA)
                    || getTipoOtroSi().equals(TipoOtroSiEnum.MODIFICACION_Y_PRORROGA)
                    && !generarProrrogModif) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    /*public void crearNegociacionOtroSiSedes() {
        lanzarDialogoSedeOtroSi = true;
        if (generarProrrogModif) {
            this.negociacionOtroSi = this.negociacionOtrSiProrroga;
            facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_mensaje_creacion_otro_si_ok"));
        } else {
            crearNewNegociacionOtroSiSede();
        }

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(NegociacionSessionEnum.NEGOCIACION.toString(), this.negociacionOtroSi);
        session.setAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString(), this.negociacionOtroSi.getId());
        session.setAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString(), this.negociacionOtroSi.getPrestador().getId());

    }*/

    /*private void crearNewNegociacionOtroSiSede() {
        try {
            negociacionOtroSi.setUsuarioCreacionId(user.getId());
            negociacionOtroSi.setEsOtroSi(Boolean.TRUE);
            negociacionOtroSi.setNegociacionPadre(new NegociacionDto());
            negociacionOtroSi.getNegociacionPadre().setId(negociacionOtroSi.getId());
            negociacionOtroSi.setFechaConcertacionMx(new Date());
            negociacionOtroSi.setFechaConcertacionPx(new Date());
            negociacionOtroSi.setFechaConcertacionPq(new Date());
            negociacionOtroSi = this.negociacionFacade.crearNegociacionOtroSi(negociacionOtroSi, user.getId(), tipoOtroSi);
            this.negociacionFacade.crearSedePrincipalOtroSi(negociacionOtroSi);
            negociacionOtroSi.setNumeroOtroSi(this.negociacionFacade.obtenerNumeroOtroSi(negociacionOtroSi.getNegociacionPadre().getId()));
            negociacionOtroSi.setId(this.negociacionFacade.buscarIdNegOtroSi(negociacionOtroSi));
            facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_mensaje_creacion_otro_si_ok"));
        } catch (Exception e) {
            logger.error("Error al crear otro si sedes", e);
        }
    }*/

    /*public void terminarCreacionNegociacionOtroSiSedes() {
        try {
            if (Objects.nonNull(tipoOtroSi)) {
                switch (tipoModificacionOtroSi) {
                    case ADICIONAR_ELIMINAR_SEDES:
                        try {
                            List<Long> sedesPrestadorNegOtroSi = sedesPrestadorSeleccionadas.stream().parallel()
                                    .map(pp -> pp.getId())
                                    .collect(Collectors.toList());
                            this.negociacionOtroSi.setSedesNegociacion(this.negociacionFacade.consultarSedeNegociacionByNegociacionId(negociacionOtroSi.getId()));
                            this.negociacionFacade.terminarNegociacionOtroSiSedes(negociacionOtroSi, user.getId(),
                                    tipoModificacionOtroSi, sedesPrestadorNegOtroSi, cargarContenido.trim().equalsIgnoreCase("SI") ? true : false);
                            this.negociacionFacade.actualizarAdicionOtroSiDefault(negociacionOtroSi.getId());
                            this.negociacionFacade.borrarAfectacionSedePadreOtroSi(negociacionOtroSi);
                            this.negociacionFacade.igualarValorSedePadre(negociacionOtroSi, cargarContenido);
                            RequestContext.getCurrentInstance().execute("PF('OtroSiDlg').hide()");
                            RequestContext.getCurrentInstance().execute("PF('DlgSeleccionSedesOtroSi').hide()");
                            lanzarDialogoSedeOtroSi = false;
                            this.faceUtils.urlRedirect("/sedesasede/detalle");
                        } catch (ConexiaBusinessException ex) {
                            ex.printStackTrace();
                            Logger.getLogger(GestionOtroSiController.class.getName()).log(Level.SEVERE, null, ex);
                            facesMessagesUtils.addError(exceptionUtils.createMessage(resourceBundle, ex));
                        }
                        break;
                    default:
                        break;
                }
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                session.setAttribute(NegociacionSessionEnum.NEGOCIACION.toString(), negociacionOtroSi);
                session.setAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString(), negociacionOtroSi.getId());
                session.setAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString(), negociacionOtroSi.getPrestador().getId());
            }
        } catch (Exception e) {
            Logger.getLogger(GestionOtroSiController.class.getName()).log(Level.SEVERE, null, e);
        }
    }*/

    /*public void agregarSedesNegociacionOtroSi(AjaxBehaviorEvent e) {
        try {
            Boolean isCapitaOEventoPrimerNivel = (this.negociacionOtroSi.getTipoModalidadNegociacion() == NegociacionModalidadEnum.EVENTO);
            if (e instanceof ToggleSelectEvent) { // Toggle marcar/desmarcar por página
                if (this.negociacionOtroSi.getTipoAreaCobertura().equals(AreaCoberturaTipoEnum.REFERENCIA_ZONAL)
                        && (Objects.isNull(this.negociacionOtroSi.getZonaCobertura()) || this.negociacionOtroSi.getZonaCobertura().isEmpty())) {
                    facesMessagesUtils.addWarning(resourceBundle.getString("area_cobertura_mensaje_selec_zona"));
                } else {
                    if (((ToggleSelectEvent) e).isSelected()) {
                        for (SedePrestadorDto sedePrestador : sedesPrestadorSeleccionadas) {
                            SedesNegociacionDto sedeNeg = new SedesNegociacionDto();
                            sedeNeg.setNegociacionId(negociacionOtroSi.getId());
                            sedeNeg.setSedeId(sedePrestador.getId());
                            sedeNeg.setId(this.negociacionFacade.crearSedesNegociacionCoberturaPorDefecto(sedeNeg, isCapitaOEventoPrimerNivel, this.negociacionOtroSi.getTipoAreaCobertura(), this.negociacionOtroSi));
                        }
                        this.negociacionFacade.borrarDuplicidadSedesOtroSi(negociacionOtroSi.getId());
                        facesMessagesUtils.addInfo(resourceBundle.getString("sede_neg_mensaje_creacion_ok"));
                    } else {
                        this.negociacionFacade.eliminarSedeNegociacionByNegociacionId(negociacionOtroSi.getId());
                        this.negociacionFacade.crearSedePrincipalOtroSi(negociacionOtroSi);
                        facesMessagesUtils.addInfo(resourceBundle.getString("sede_neg_mensaje_desasignacion_ok"));
                    }
                }
            } else if (e instanceof SelectEvent) {//marcar sede
                if (this.negociacionOtroSi.getTipoAreaCobertura().equals(AreaCoberturaTipoEnum.REFERENCIA_ZONAL)
                        && (Objects.isNull(this.negociacionOtroSi.getZonaCobertura()) || !this.negociacionOtroSi.getZonaCobertura().isEmpty())) {
                    facesMessagesUtils.addWarning(resourceBundle.getString("area_cobertura_mensaje_selec_zona"));
                    SedePrestadorDto sedePrestador = (SedePrestadorDto) ((SelectEvent) e).getObject();
                    sedePrestador.setSeleccionado(false);
                } else {
                    SedePrestadorDto sedePrestador = (SedePrestadorDto) ((SelectEvent) e).getObject();
                    sedePrestador.setSeleccionado(true);
                    SedesNegociacionDto sedeNegociacion = new SedesNegociacionDto();
                    sedeNegociacion.setNegociacionId(negociacionOtroSi.getId());
                    sedeNegociacion.setSedeId(sedePrestador.getId());
                    sedeNegociacion.setId(this.negociacionFacade.crearSedesNegociacionCoberturaPorDefecto(sedeNegociacion, isCapitaOEventoPrimerNivel, this.negociacionOtroSi.getTipoAreaCobertura(), this.negociacionOtroSi));
                    facesMessagesUtils.addInfo(resourceBundle.getString("sede_neg_mensaje_creacion_ok"));
                }
            } else if (e instanceof UnselectEvent) {// Desmarcar sede
                if (this.negociacionOtroSi.getTipoAreaCobertura().equals(AreaCoberturaTipoEnum.REFERENCIA_ZONAL)
                        && (Objects.isNull(this.negociacionOtroSi.getZonaCobertura()) || !this.negociacionOtroSi.getZonaCobertura().isEmpty())) {
                    facesMessagesUtils.addWarning(resourceBundle.getString("area_cobertura_mensaje_selec_zona"));
                    SedePrestadorDto sedePrestador = (SedePrestadorDto) ((UnselectEvent) e).getObject();
                    sedePrestador.setSeleccionado(false);
                } else {
                    SedePrestadorDto sedePrestador = (SedePrestadorDto) ((UnselectEvent) e).getObject();
                    sedePrestador.setSeleccionado(false);
                    this.negociacionFacade.eliminarSedeNegociacionByNegociacionIdAndSedePrestador(negociacionOtroSi.getId(), sedePrestador.getId());
                    facesMessagesUtils.addInfo(resourceBundle.getString("sede_neg_mensaje_desasignacion_ok"));
                }
            }
        } catch (Exception ex) {
            logger.error("Error al adicionarSedeNegociacionOtroSi", ex);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }*/

    /*private void actualizarSedePrincipal() {
        for (SedesNegociacionDto sedesNegociacionDto : sedesNegociacion) {
            if (sedesNegociacionDto.getSedePrestador() != null
                    && sedesNegociacionDto.getSedePrestador().getSedePrincipal() != null
                    && sedesNegociacionDto.getSedePrestador().getSedePrincipal()) {
                this.negociacionOtroSi.setSedePrincipal(sedesNegociacionDto.getSedePrestador());
            }
        }
    }*/

    /*public void guardarOpcionCobertura() {
        actualizarSedePrincipal();
        if (Objects.isNull(this.negociacionOtroSi.getTipoAreaCobertura())) {
            facesMessagesUtils.addError("Por favor seleccione una opción de cobertura");
        } else if (this.negociacionOtroSi.getTipoAreaCobertura().equals(AreaCoberturaTipoEnum.REFERENCIA_ZONAL)
                && (Objects.isNull(this.negociacionOtroSi.getZonaCobertura()) || this.negociacionOtroSi.getZonaCobertura().isEmpty())) {
            facesMessagesUtils.addWarning("Debe seleccionar por lo menos una zona para aplicar cobertura");
        } else {
            this.negociacionFacade.actualizarOpcionCobertura(negociacionOtroSi.getTipoAreaCobertura(), negociacionOtroSi.getId());
            if (!generarProrrogModif) {
//                Boolean isCapitaOEventoPrimerNivel = (this.negociacionOtroSi.getTipoModalidadNegociacion() == NegociacionModalidadEnum.EVENTO);
                List<SedesNegociacionDto> sedesNegociacion = this.negociacionFacade
                        .consultarSedeNegociacionByNegociacionId(this.negociacionOtroSi.getId());
                List<Long> sedesId = sedesNegociacion.stream()
                        .map(sc -> sc.getId()).collect(Collectors.toList());
                if (this.negociacionFacade.consultarAreaCoberturaBySedeNegociacionId(sedesId).isEmpty()) {
                    this.negociacionFacade.crearAreaCoberturaOtroSi(this.negociacionOtroSi.getNegociacionOrigen().getId(),
                            this.negociacionOtroSi.getId());
                }

            }
            Ajax.update("@([id$=sedesPrestadorForm])");
            this.consultarSedesNegociacion(negociacionOtroSi.getPrestador().getId());
        }

    }*/

    /*public void consultarSedesNegociacion(Long prestadorId) {
        //Validar si puede continuar con la negociacion
        if (sedesPrestador.size() > 0) {
            List<SedesNegociacionDto> sedesNegociacion = this.negociacionFacade
                    .consultarSedeNegociacionByNegociacionId(this.negociacionOtroSi.getId());
            List<SedePrestadorDto> sedesSeleccionadas = sedesNegociacion.stream()
                    .map(sc -> sc.getSedePrestador()).collect(Collectors.toList());
            String mensajeSedesEliminadas = "";
            for (SedePrestadorDto sede : sedesSeleccionadas) {
                int index = sedesPrestador.indexOf(sede);
                if (index != -1) {
                    sedesPrestador.get(index).setSeleccionado(true);
                } else {
                    this.negociacionFacade.eliminarSedeNegociacionByNegociacionIdAndSedePrestador(negociacionOtroSi.getId(), sede.getId());
                    this.negociacionFacade.eliminarSedeNegociacionOtroSi(negociacionOtroSi.getId(), sede.getId());
                    mensajeSedesEliminadas += " " + sede.getNombreSede() + " - ";
                }
            }

            if (mensajeSedesEliminadas.length() > 0) {
                this.facesMessagesUtils.addInfo(this.resourceBundle.getString("sede_neg_mensaje_desasignacion_defecto_ok") + mensajeSedesEliminadas);
            }
        }
    }*/

    public void listarZonasCobertura() {
//    	if(this.negociacionOtroSi.getTipoAreaCobertura().equals(AreaCoberturaTipoEnum.REFERENCIA_ZONAL)){
        this.zonasMunicipioCobertura = this.facade.listarZonasCobertura();
        Ajax.update("@([id$=listZonasCobertura])");

    }

    /*public void lanzarDialogSedes() throws ConexiaBusinessException {
        if (this.negociacionFacade.validarNegociacionesOtrosSiSinLegalizar(negociacionOtroSi)
                || this.negociacionFacade.validarNegociacionesOtrosSiSinLegalizar(negociacionOtrSiProrroga)) {
            if (tipoModificacionOtroSi.equals(TipoModificacionOtroSiEnum.ADICIONAR_ELIMINAR_SEDES)) {
                this.consultarSedesNegociacionOtroSi();
                RequestContext.getCurrentInstance().execute("PF('DlgSeleccionSedesOtroSi').show()");
            }
        } else {
            this.facesMessagesUtils.addWarning(this.resourceBundle.getString("negociacion_otrosi_error_pendientes_legalizar"));
        }
    }*/

    public void eliminarNegociacion() {
        this.negociacionFacade.eliminarNegociacion(negociacionOtroSi.getId());
    }

    /**
     * Limpiar campos para volver a la selección de tipo de otro si
     */
    public void regresarTipoOtroSi() {
        tipoOtroSi = TipoOtroSiEnum.NO_APLICA;
        limpiarCamposModificacion();
    }

    public void limpiarFechaProrroga() {
        this.fechaFinProrroga = new Date();
    }

    /**
     * Permite limpiar los campos del modal
     */
    public void limpiarCampos() {
        this.tipoOtroSi = TipoOtroSiEnum.NO_APLICA;
        limpiarCamposModificacion();
        limpiarFechaProrroga();
    }

    /**
     * Permite limpiar los campos de selección para el tipo de modificación
     */
    public void limpiarCamposModificacion() {
        this.tipoModificacionOtroSi = TipoModificacionOtroSiEnum.NO_APLICA;
        this.cargarContenido = "";
    }

    public Date getMinFechaFinProrroga() {
        Date fechaIni = null;
        if (tipoOtroSi.equals(TipoOtroSiEnum.MODIFICACION_Y_PRORROGA)) {
            fechaIni = this.fechaInicioProrrogaModif;
        } else if (tipoOtroSi.equals(TipoOtroSiEnum.PRORROGA)) {
            fechaIni = this.fechaInicioProrroga;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(fechaIni);
        calendar.add(Calendar.DATE, 1);
        return calendar.getTime();
    }

    public NegociacionDto getNegociacionOtroSi() {
        return negociacionOtroSi;
    }

    public void setNegociacionOtroSi(NegociacionDto negociacionOtroSi) {
        this.negociacionOtroSi = negociacionOtroSi;
    }

    public TipoOtroSiEnum[] getTipoOtroSiEnumValues() {
        return TipoOtroSiEnum.values();
    }

    public TipoModificacionOtroSiEnum[] getTipoMoficacionOtroSiEnumValues() {
        return TipoModificacionOtroSiEnum.values();
    }

    public TipoOtroSiEnum getTipoOtroSi() {
        return tipoOtroSi;
    }

    public void setTipoOtroSi(TipoOtroSiEnum tipoOtroSi) {
        this.tipoOtroSi = tipoOtroSi;
    }

    public TipoModificacionOtroSiEnum getTipoModificacionOtroSi() {
        return tipoModificacionOtroSi;
    }

    public void setTipoModificacionOtroSi(TipoModificacionOtroSiEnum tipoModificacionOtroSi) {
        this.tipoModificacionOtroSi = tipoModificacionOtroSi;
    }

    public String getCargarContenido() {
        return cargarContenido;
    }

    public void setCargarContenido(String cargarContenido) {
        this.cargarContenido = cargarContenido;
    }

    public Date getFechaInicioProrroga() {
        return fechaInicioProrroga;
    }

    public void setFechaInicioProrroga(Date fechaInicioProrroga) {
        this.fechaInicioProrroga = fechaInicioProrroga;
    }

    public Date getFechaFinProrroga() {
        return fechaFinProrroga;
    }

    public void setFechaFinProrroga(Date fechaFinProrroga) {
        this.fechaFinProrroga = fechaFinProrroga;
    }

    public List<SedePrestadorDto> getSedesPrestador() {
        return sedesPrestador;
    }

    public void setSedesPrestador(List<SedePrestadorDto> sedesPrestador) {
        this.sedesPrestador = sedesPrestador;
    }

    public List<SedePrestadorDto> getSedesPrestadorSeleccionadas() {
        return sedesPrestadorSeleccionadas;
    }

    public void setSedesPrestadorSeleccionadas(List<SedePrestadorDto> sedesPrestadorSeleccionadas) {
        this.sedesPrestadorSeleccionadas = sedesPrestadorSeleccionadas;
    }

    public List<MunicipioDto> getZonasMunicipioCobertura() {
        return zonasMunicipioCobertura;
    }

    public void setZonasMunicipioCobertura(List<MunicipioDto> zonasMunicipioCobertura) {
        this.zonasMunicipioCobertura = zonasMunicipioCobertura;
    }

    public AreaCoberturaTipoEnum[] getAreaCoberturaTipoEnums() {
        return AreaCoberturaTipoEnum.values();
    }

    public Date getFechaInicioProrrogaModif() {
        return fechaInicioProrrogaModif;
    }

    public void setFechaInicioProrrogaModif(Date fechaInicioProrrogaModif) {
        this.fechaInicioProrrogaModif = fechaInicioProrrogaModif;
    }

    public boolean isGenerarProrrogModif() {
        return generarProrrogModif;
    }

    public void setGenerarProrrogModif(boolean generarProrrogModif) {
        this.generarProrrogModif = generarProrrogModif;
    }

    public Boolean getBtnGenera() {
        return btnGenera;
    }

    public void setBtnGenera(Boolean btnGenera) {
        this.btnGenera = btnGenera;
    }

    public Boolean getLanzarDialogoSedeOtroSi() {
        return lanzarDialogoSedeOtroSi;
    }

    public void setLanzarDialogoSedeOtroSi(Boolean lanzarDialogoSedeOtroSi) {
        this.lanzarDialogoSedeOtroSi = lanzarDialogoSedeOtroSi;
    }

}
