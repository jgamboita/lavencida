package co.conexia.negociacion.wap.controller.negociacion;

import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import co.conexia.negociacion.wap.util.GestionarNombreArchivoXlsx;
import com.conexia.contratacion.commons.constants.enums.TipoOtroSiEnum;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.InvitacionNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.logfactory.Log;
import com.conexia.notificaciones.dtos.DestinatarioDto;
import com.conexia.notificaciones.dtos.EmailDto;
import com.conexia.notificaciones.dtos.ParametroMensajeDto;
import com.conexia.notificaciones.dtos.PlantillaDto;
import com.conexia.notificaciones.enums.EstadoNotificacionEnum;
import com.conexia.notificaciones.enums.TipoDestinatarioEnum;
import com.conexia.notificaciones.enums.TipoMensajeEnum;
import com.conexia.notificaciones.remote.NotificacionesRemote;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.servicefactory.CnxService;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import co.conexia.negociacion.wap.controller.common.CommonController;
import co.conexia.negociacion.wap.facade.negociacion.GestionNegociacionFacade;
import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import net.sf.jasperreports.engine.JRException;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;

/**
 * Controller creado para la gestion de negociaciones
 *
 * @author jjoya
 *
 */
@Named
@ViewScoped
@URLMapping(id = "gestionNegociacion", pattern = "/gestionNegociaciones", viewId = "/negociacion/gestionNegociacion.page")
public class GestionNegociacionController implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -6451793638896773087L;

    @Inject
    private GestionNegociacionFacade gestionNegociacionFacade;

    @Inject
    private NegociacionFacade negociacionFacade;

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
    @CnxService
    private NotificacionesRemote notificacionesRemote;

    @Inject
    private CommonController commonController;

    @Inject
    private GestionarNombreArchivoXlsx gestionarNombreArchivoXlsx;

    private PrestadorDto prestadorSeleccionado;

    private List<NegociacionDto> negociaciones;

    private List<SedesNegociacionDto> sedesNegociaciones;

    private com.conexia.contratacion.commons.constants.enums.EstadoNegociacionEnum estadoNegSel;

    private com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum estadoLegalizacionNegSel;

    private InvitacionNegociacionDto invitacionNegociacion;

    private Long negSelDelete;

    private Long negSelHijo;

    private Date fechaActual;

    private String fechaHoraCitaSeleccionada;

    private SedePrestadorDto sedePrincipalNegociacion;

    private AnexoTarifarioDto anexoTarifarioDto;

    // Servicios REST
    private final String PORTAFOLIO_BASE = "/wap/portafolio/services/rest/portafolioBase/";

    private boolean crearPortafolioCapita = false;

    private NegociacionModalidadEnum modalidadNegociacion;

    private boolean negociacionEsRiasCapita;

    private boolean negociacionESRiasCapitaGrupoEtario;

    private List<Boolean> lstToogle;

    private final Integer TOTAL_COLUMNS_TABLE = 20;

    /**
     * Para otro si
     */
    private NegociacionDto dtoNeg = new NegociacionDto();
    private Boolean gestionarOtroSi = false;
    
    private Boolean validaOtroSi = true;


    
    private NegociacionDto negociacion;
    
    /**
     * Constructor por defecto
     */
    public GestionNegociacionController() {
    }

    @PostConstruct
    public void postConstruct() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        Long prestadorId = (Long) session.getAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString());
        dtoNeg.setPrestador(new PrestadorDto());
        dtoNeg.getPrestador().setId(prestadorId);
        dtoNeg.setNegociacionPadre(new NegociacionDto());
        if (prestadorId == null) {
            this.faceUtils.urlRedirect("/bandejaPrestador");
        } else {
            this.prestadorSeleccionado = commonController.getPrestador(prestadorId);
            consultarNegociacionesPrestador(prestadorId, dtoNeg);
            // Verfica si el prestador tiene o portafolio de capita habilitado
            crearPortafolioCapita = !negociacionFacade.verificarPortafolioCapitaByPrestador(prestadorId);
        }
        lstToogle = new ArrayList<>(TOTAL_COLUMNS_TABLE);
        for (int i = 0; i < TOTAL_COLUMNS_TABLE; i++) {
            lstToogle.add(true);
        }
    }

    /**
     * Metodo que redirecciona para crear una negociacion
     */
    public void crearNegociacion() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
            session.setAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString(), null);
            this.faceUtils.urlRedirect("/detalleNegociacion");
        } catch (Exception e) {
            this.logger.error("Error al crear nueva negociacion", e);
            this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Metodo que redirecciona para crear una negociacion
     */
    public void verNegociacion(NegociacionDto negociacion) {
        if (negociacion.getId() == null) {
            this.faceUtils.urlRedirect("/bandejaPrestador");
        }
        if(Objects.nonNull(negociacion.getTipoOtroSi()) ? negociacion.getTipoOtroSi().equals(TipoOtroSiEnum.PRORROGA) : false){
        	this.facesMessagesUtils.addWarning(this.resourceBundle.getString("negociacion_otro_si_con_prorroga"));
        }
        else {
            try {
                List<SolicitudContratacionParametrizableDto> solicitudes = this.negociacionFacade
                        .findSolicitudesFinalizadasContratacionByNegociacionId(negociacion
                                .getId(), EstadoLegalizacionEnum.LEGALIZADA);
                if (solicitudes.size() == 0) {
                    FacesContext facesContext = FacesContext
                            .getCurrentInstance();
                    HttpSession session = (HttpSession) facesContext
                            .getExternalContext().getSession(true);
                    session.setAttribute(
                            NegociacionSessionEnum.NEGOCIACION_ID.toString(),
                            negociacion.getId());
                    session.setAttribute(
                            NegociacionSessionEnum.NEGOCIACION.toString(),
                            negociacion);
                    session.setAttribute(
                            NegociacionSessionEnum.MODALIDAD_CONTRATACION.toString(),
                            negociacion.getTipoModalidadNegociacion());
                    session.setAttribute(
                            NegociacionSessionEnum.SEDES_NEGOCIACION.toString(),
                            negociacion.getNroSedes());
                    this.faceUtils.urlRedirect("/detalleNegociacion");
                } else {
                    this.facesMessagesUtils.addWarning(this.resourceBundle.getString("negociacion_con_legalizacion_finalizada"));
                }
            } catch (Exception e) {
                logger.error("Error al consultar la negociación No." + negociacion.getId(), e);
                facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
        }
    }

    /**
     * Consulta los datos basicos del prestador
     *
     * @param prestadorId identificador del prestador
     */
    public void consultarNegociacionesPrestador(Long prestadorId, NegociacionDto negociacion) {
        try {
            this.negociaciones = this.gestionNegociacionFacade.consultarNegociacionesPrestador(prestadorId, negociacion);
        } catch (Exception e) {
            logger.error("Error al consultar las negociaciones del prestador.", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Metodo que redirecciona para crear una negociacion
     *
     * @param negociacion
     */
    public void verSedesNegociadas(NegociacionDto negociacion) {
        if (Objects.isNull(negociacion.getId())) {
            this.faceUtils.urlRedirect("/bandejaPrestador");
        } else {
            try {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                session.setAttribute(NegociacionSessionEnum.NEGOCIACION.toString(), negociacion);
                sedesNegociaciones = negociacionFacade.consultarSedeNegociacionByNegociacionId(negociacion.getId());
                this.estadoNegSel = negociacion.getEstadoNegociacion();
                this.estadoLegalizacionNegSel = negociacion.getEstadoLegalizacion();
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('sedesNegociadasDialog').show();");
            } catch (Exception e) {
                logger.error("Error al intentar ver las sedes de la negociación No." + negociacion.getId(), e);
                facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
        }
    }

    /**
     * Metodo que redirecciona para descargar un anexo tarifario
     *
     * @param negociacion
     */
    public void descargarAnexoTarifario(NegociacionDto negociacion) {
        if (Objects.isNull(negociacion.getId())) {
            this.faceUtils.urlRedirect("/bandejaPrestador");
        } else {
            try {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                session.setAttribute(NegociacionSessionEnum.NEGOCIACION.toString(), negociacion);
                RequestContext context = RequestContext.getCurrentInstance();
                negociacionEsRiasCapita = negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.RIAS_CAPITA);
                negociacionESRiasCapitaGrupoEtario = negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO);
                context.update("anexoDialog");
                context.execute("PF('anexoTarifarioDlg').show();");
            } catch (Exception e) {
                logger.error("Error al intentar ver la ventana para descargar el anexo tarifario de la negociación No."
                        + negociacion.getId(), e);
                facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
        }
    }

    /**
     * Metodo que elimina una negociación
     */
    public void eliminarNegociacion() {
        Long negIdFinal = negSelDelete;
        if (Objects.nonNull(negSelHijo)) {
            negSelDelete = negSelHijo;
        }
        if (negSelDelete == null) {
            this.faceUtils.urlRedirect("/bandejaPrestador");
        } else {
            try {
                if (negociacionFacade.eliminarNegociacion(negSelDelete) > 0) {
                    negociacionFacade.guardarHistorialEliminarNegociacion(user.getId(), negSelDelete);
                    FacesContext facesContext = FacesContext.getCurrentInstance();
                    HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                    Long prestadorId = (Long) session.getAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString());
                    dtoNeg.setPrestador(new com.conexia.contratacion.commons.dto.maestros.PrestadorDto());
                    dtoNeg.getPrestador().setId(prestadorId);
                    consultarNegociacionesPrestador(prestadorId, dtoNeg);
                    facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_mensaje_eliminacion_ok") + " " + negSelDelete);
                }
            } catch (Exception e) {
                logger.error("Error al eliminar la negociación No." + negSelDelete, e);
                facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }

        }
    }

    /**
     * Metodo que guarda el número de la sede seleccionada
     *
     * @param negociacionId
     */
    public void seleccionarSedeEliminar(Long negociacionId, Long negociacionPadreId) {
        if (negociacionId == null) {
            this.faceUtils.urlRedirect("/bandejaPrestador");
        } else {
            try {
                negSelDelete = negociacionId;
                if(Objects.nonNull(negociacionPadreId)){
                    negSelHijo = negociacionId;
                    negSelDelete = negociacionPadreId;
                }
            } catch (Exception e) {
                logger.error("Error al eliminar la negociación No." + negociacionId, e);
                facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }

        }
    }

    /**
     * Metodo que redirecciona para crear una invitacion de negociacion
     */
    public void verInvitacionNegociacion(Long negociacionId, EstadoNegociacionEnum estadoNegociacion) {
        if (negociacionId == null) {
            this.faceUtils.urlRedirect("/bandejaPrestador");
        } else {
            try {
                InvitacionNegociacionDto invitacionExistente = this.negociacionFacade.consultarInvitacionNegociacionByNegociacionId(negociacionId);
                invitacionNegociacion = new InvitacionNegociacionDto();
                invitacionNegociacion.setNegociacionId(negociacionId);
                invitacionNegociacion.setMensaje(getMensajeBaseInvitacion());

                if (invitacionExistente.getId() != null) {
                    invitacionNegociacion = invitacionExistente;
                }

                sedePrincipalNegociacion = new SedePrestadorDto();
                for (SedesNegociacionDto sede : negociacionFacade.consultarSedeNegociacionByNegociacionId(negociacionId)) {
                    if (sede.getSedePrestador().getSedePrincipal() != null
                            && sede.getSedePrestador().getSedePrincipal()) {
                        sedePrincipalNegociacion = sede.getSedePrestador();
                        break;
                    }
                }

                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('invitacionNegociacionDlg').show();");

            } catch (Exception e) {
                logger.error("Error al intentar visualizar la invitación para la negociación No." + negociacionId, e);
                facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
        }
    }

    /**
     * Metodo que redirecciona para crear una invitacion de negociacion
     */
    public void crearInvitacionNegociacion() {
        try {
            if (invitacionNegociacion != null) {
                invitacionNegociacion.setUserId(user.getId());

                if (invitacionNegociacion.getCorreo() == null || invitacionNegociacion.getCorreo().isEmpty()) {
                    invitacionNegociacion.setCorreo(prestadorSeleccionado.getCorreoElectronico());
                }

                Long idInvitacion = negociacionFacade.crearInvitacionNegociacion(invitacionNegociacion);
                if (idInvitacion != null) {
                    enviarCorreo(invitacionNegociacion);
                    RequestContext context = RequestContext.getCurrentInstance();
                    context.execute("PF('invitacionNegociacionDlg').hide();");
                    facesMessagesUtils.addInfo(resourceBundle.getString("invitacion_neg_crear_ok"));
                }
            }
        } catch (Exception e) {
            logger.error(resourceBundle.getString("invitacion_neg_crear_error"), e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Metodo que redirecciona para crear una invitacion de negociacion
     */
    public void actualizarInvitacionNegociacion() {
        try {
            if (invitacionNegociacion != null) {
                invitacionNegociacion.setUserId(user.getId());

                if (prestadorSeleccionado.getCorreoElectronico() != null) {
                    invitacionNegociacion.setCorreo(prestadorSeleccionado.getCorreoElectronico());
                }

                Long idInvitacion = negociacionFacade.actualizarInvitacionNegociacion(invitacionNegociacion);
                if (idInvitacion != null) {
                    enviarCorreo(invitacionNegociacion);
                    RequestContext context = RequestContext.getCurrentInstance();
                    context.execute("PF('invitacionNegociacionDlg').hide();");
                    facesMessagesUtils.addInfo(resourceBundle.getString("invitacion_neg_actualizar_ok"));
                }
            }
        } catch (Exception e) {
            logger.error(resourceBundle.getString("invitacion_neg_actualizar_error"), e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Cancela la invitacion a negociar y cierra el dialog
     */
    public void cancelarInvitacionNegociacion() {
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('invitacionNegociacionDlg').hide();");
    }

    /**
     * Obtiene el mensaje base para el campo mensaje en la invitacion
     *
     * @return
     */
    public String getMensajeBaseInvitacion() {
        Calendar fecha = Calendar.getInstance();
        StringBuilder mensajeBase = new StringBuilder();
        mensajeBase.append("[ Departamento, Municipio ]").append("<br/><br/>")
                .append(fecha.get(Calendar.DAY_OF_MONTH))
                .append(" de ").append(getNombreMes(fecha.get(Calendar.MONTH)))
                .append(" del ").append(fecha.get(Calendar.YEAR)).append("<br/><br/>")
                .append("Señores ").append(prestadorSeleccionado.getNombre()).append("<br/><br/>")
                .append("[ Cuerpo del Mensaje ]");
        return mensajeBase.toString();
    }

    /**
     *
     * @param invitacionNegociacion
     * @throws Exception
     */
    public void enviarCorreo(InvitacionNegociacionDto invitacionNegociacion) throws Exception {
        EmailDto email = new EmailDto();
        List<DestinatarioDto> destinatarios = new ArrayList<>();
        destinatarios.add(new DestinatarioDto(null, invitacionNegociacion.getCorreo(), TipoDestinatarioEnum.PARA.getId()));
        StringBuffer mensajeHTML = new StringBuffer();

        for (String linea : invitacionNegociacion.getMensaje().split("\n")) {
            mensajeHTML.append(linea).append("<br/>");
        }

        email.setAsunto("Invitación a Negociar");
        email.setDestinatarios(destinatarios);
        email.setMensaje(mensajeHTML.toString());
        email.setPlantilla(new PlantillaDto(2L));
        email.setTipoMensaje(TipoMensajeEnum.EMAIL);
        email.setEstadoNotificacion(EstadoNotificacionEnum.PENDIENTE);
        email.setOrigen("igen.conexia@gmail.com"); // Por definir

        // Configuracion de Parametros
        String fechaHoraCita = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(invitacionNegociacion.getFechaHoraCita());
        String representanteLegal = (prestadorSeleccionado.getRepresentanteLegal() == null) ? "" : prestadorSeleccionado.getRepresentanteLegal();

        List<ParametroMensajeDto> parametros = new ArrayList<>();
        parametros.add(new ParametroMensajeDto("para", invitacionNegociacion.getCorreo()));
        parametros.add(new ParametroMensajeDto("representanteLegal", representanteLegal));
        parametros.add(new ParametroMensajeDto("fechaHoraCita", fechaHoraCita));
        parametros.add(new ParametroMensajeDto("nombreFuncionario", user.getPrimerNombre() + " " + user.getPrimerApellido()));
        parametros.add(new ParametroMensajeDto("cargoFuncionario", "GERENCIA DE SALUD"));
        parametros.add(new ParametroMensajeDto("nombreEPS", "EMSSANAR EPS-S"));

        if (sedePrincipalNegociacion.getSedePrincipal() != null && sedePrincipalNegociacion.getSedePrincipal()) {
            String sedeDireccion = (sedePrincipalNegociacion.getDireccion() == null) ? "" : sedePrincipalNegociacion.getDireccion();
            String sedeTelefono = (sedePrincipalNegociacion.getTelefonoAdministrativo() == null) ? "" : sedePrincipalNegociacion.getTelefonoAdministrativo();
            String sedeFaxDireccion = (sedePrincipalNegociacion.getFax() == null) ? "" : sedePrincipalNegociacion.getFax();
            String sedeMunicipio = (sedePrincipalNegociacion.getMunicipio().getDescripcion() == null) ? "" : sedePrincipalNegociacion.getMunicipio().getDescripcion();

            parametros.add(new ParametroMensajeDto("sedeDireccion", sedeDireccion));
            parametros.add(new ParametroMensajeDto("sedeTelefono", sedeTelefono));
            parametros.add(new ParametroMensajeDto("sedeFaxDireccion", sedeFaxDireccion));
            parametros.add(new ParametroMensajeDto("sedeMunicipio", sedeMunicipio));
        } else {
            parametros.add(new ParametroMensajeDto("sedeDireccion", ""));
            parametros.add(new ParametroMensajeDto("sedeTelefono", ""));
            parametros.add(new ParametroMensajeDto("sedeFaxDireccion", ""));
            parametros.add(new ParametroMensajeDto("sedeMunicipio", ""));
        }

        email.setParametros(parametros);

        notificacionesRemote.enviarMail(email);

    }

    public boolean crearPortafolioCapita() {
        if (Objects.nonNull(prestadorSeleccionado)) {
            try {
                URI uri = new URI(FacesContext.getCurrentInstance().getExternalContext().getRequest().toString());

                FacesContext.getCurrentInstance().getExternalContext()
                        .redirect(uri.getScheme() + "://" + uri.getHost() + (Objects.nonNull(uri.getPort()) && uri.getPort() != -1 ? ":" + uri.getPort() : "") + PORTAFOLIO_BASE + prestadorSeleccionado.getId());
                FacesContext.getCurrentInstance().responseComplete();
                logger.info("---> URL Creacion Portafolio: " + uri.getScheme() + "://" + uri.getHost() + (Objects.nonNull(uri.getPort()) && uri.getPort() != -1 ? ":" + uri.getPort() : "") + PORTAFOLIO_BASE + prestadorSeleccionado.getId());
                facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_crear_portafolio_ok"));
                return true;
            } catch (URISyntaxException | IOException e) {
                facesMessagesUtils.addError(resourceBundle.getString("negociacion_crear_portafolio_error"));
            } finally {
                facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_crear_portafolio_ok"));
            }
        }
        return false;
    }

    public PrestadorDto getPrestadorSeleccionado() {
        return prestadorSeleccionado;
    }

    public List<NegociacionDto> getNegociaciones() {
        return negociaciones;
    }

    public List<SedesNegociacionDto> getSedesNegociaciones() {
        return sedesNegociaciones;
    }

    public com.conexia.contratacion.commons.constants.enums.EstadoNegociacionEnum getEstadoNegSel() {
        return estadoNegSel;
    }

    public void setEstadoNegSel(com.conexia.contratacion.commons.constants.enums.EstadoNegociacionEnum estadoNegSel) {
        this.estadoNegSel = estadoNegSel;
    }

    public Long getNegSelDelete() {
        return negSelDelete;
    }

    public void setNegSelDelete(Long negSelDelete) {
        this.negSelDelete = negSelDelete;
    }

    public InvitacionNegociacionDto getInvitacionNegociacion() {
        return invitacionNegociacion;
    }

    public void setInvitacionNegociacion(InvitacionNegociacionDto invitacionNegociacion) {
        this.invitacionNegociacion = invitacionNegociacion;
    }

    public UserApp getUser() {
        return user;
    }

    public SedePrestadorDto getSedePrincipalNegociacion() {
        return sedePrincipalNegociacion;
    }

    public void setSedePrincipalNegociacion(SedePrestadorDto sedePrincipalNegociacion) {
        this.sedePrincipalNegociacion = sedePrincipalNegociacion;
    }

    public String getFechaHoraCitaSeleccionada() {
        return fechaHoraCitaSeleccionada;
    }

    public void setFechaHoraCitaSeleccionada(String fechaHoraCitaSeleccionada) {
        this.fechaHoraCitaSeleccionada = fechaHoraCitaSeleccionada;
    }

    public Date getFechaActual() {
        Calendar fecha = Calendar.getInstance();
        fecha.add(Calendar.DAY_OF_MONTH, 1);
        fecha.set(Calendar.HOUR, 0);
        fecha.set(Calendar.MINUTE, 0);
        fechaActual = new Date(fecha.getTimeInMillis());
        return fechaActual;
    }

    public void seleccionarFecha(SelectEvent event) {
        fechaHoraCitaSeleccionada = new SimpleDateFormat("yyy-MM-dd HH:mm").format(event.getObject());
    }

    public String getNombreMes(int mes) {
        String[] meses = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return meses[mes];
    }

    public void descargarAnexoExcel(String servicio)
            throws ClassNotFoundException, JRException, IOException, SQLException, ConexiaBusinessException {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        NegociacionDto negociacion = (NegociacionDto) session.getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());

        HttpServletRequest request = (HttpServletRequest) facesContext.getExternalContext().getRequest();
        gestionNegociacionFacade.descargarAnexoExcel(negociacion, servicio, user.getId(), negociacionFacade, request,
                                                     gestionarNombreArchivoXlsx.getFileName(servicio, negociacion));
        facesContext.responseComplete();
    }

    public void resetGestionNegociacion() {
        RequestContext.getCurrentInstance().reset("form:panelDatosPrestador");
    }

    public AnexoTarifarioDto getAnexoTarifarioDto() {
        return anexoTarifarioDto;
    }

    public Boolean tienePoblacion() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        NegociacionDto negociacion = (NegociacionDto) session
                .getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
        return negociacion == null || gestionNegociacionFacade.tienePoblacion(negociacion.getId())
                && NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.equals(negociacion.getTipoModalidadNegociacion());
    }

    public Boolean tienePaquetes() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        NegociacionDto negociacion = (NegociacionDto) session.getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
        return negociacion == null || gestionNegociacionFacade.tienePaquetes(negociacion.getId());
    }

    public Boolean tieneProcedimientos() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        NegociacionDto negociacion = (NegociacionDto) session
                .getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
        return negociacion == null || gestionNegociacionFacade.tieneProcedimientos(negociacion.getId(), negociacion.getTipoModalidadNegociacion());
    }

    public Boolean tieneAnexosSinTarifarioProcedimientos() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        NegociacionDto negociacion = (NegociacionDto) session.getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
        Boolean response = false;
        try {
            if (Objects.nonNull(negociacion)) {
                response = (Objects.nonNull(negociacion) && gestionNegociacionFacade.tieneProcedimientos(negociacion.getId(), negociacion.getTipoModalidadNegociacion())
                        && !NegociacionModalidadEnum.RIAS_CAPITA.equals(negociacion.getTipoModalidadNegociacion()) && !NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(negociacion.getTipoModalidadNegociacion())
                        && !NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.equals(negociacion.getTipoModalidadNegociacion()));
            }
        } catch (NullPointerException e) {
            logger.warn("Negociación nula en la bandeja prestadores", e);
        }
        return response;
    }

    public Boolean tieneMedicamentos() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        NegociacionDto negociacion = (NegociacionDto) session
                .getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
        return negociacion == null || gestionNegociacionFacade.tieneMedicamentos(negociacion.getId(), negociacion.getTipoModalidadNegociacion());
    }

    public Boolean tieneProcedimientosRecuperacion() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        NegociacionDto negociacion = (NegociacionDto) session
                .getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
        return negociacion == null || ((gestionNegociacionFacade.tieneProcedimientosRecuperacion(negociacion.getId())
                || gestionNegociacionFacade.tieneMedicamentosRecuperacion(negociacion.getId()))
                && (NegociacionModalidadEnum.RIAS_CAPITA.equals(negociacion.getTipoModalidadNegociacion())
                || NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(negociacion.getTipoModalidadNegociacion())));
    }

    public Boolean negociacionEsCapita() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        NegociacionDto negociacion = (NegociacionDto) session.getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
        return negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.CAPITA);
    }

    /**
     * Para consultar las negociaciones otro si asociadas a una negociación padre
     * @param negociacion
     */
    public void consultarNegociacionesOtroSi(NegociacionDto negociacion)
    {
        try
        {
            GestionOtroSiController.btnRegresar=false;
            dtoNeg.setNegociacionPadre(new NegociacionDto());
            dtoNeg.setNegociacionOrigen(new NegociacionDto());
            dtoNeg.getNegociacionPadre().setId(negociacion.getId());
            dtoNeg.getNegociacionOrigen().setId(negociacion.getId());
            this.negociaciones = this.gestionNegociacionFacade.consultarNegociacionesPrestador(dtoNeg.getPrestador().getId(), dtoNeg);
            
            for(NegociacionDto obj : negociaciones){
                if(obj.getEstadoLegalizacion()!=null && obj.getEstadoLegalizacion().toString().equals(EstadoLegalizacionEnum.LEGALIZADA.name())){
                    this.setValidaOtroSi(true);
                }else{
                  this.setValidaOtroSi(false);
                }
            }

            this.gestionarOtroSi = true;
            RequestContext context = RequestContext.getCurrentInstance();
            context.execute("PF('negociacionesTable').clearFilters()");
        } catch (Exception e) {
            logger.error("Error al consultar las negociaciones otro si.", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public Boolean esOtroSi() 
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        negociacion = (NegociacionDto) session
                        .getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());

        if (negociacion != null) {
                return negociacion.getEsOtroSi();
        }
        negociacion = new NegociacionDto();
        return false;
    }

    public void regresar() 
    {
       if (GestionOtroSiController.btnRegresar) {
            this.faceUtils.urlRedirect("/bandejaPrestador");
        } else {
            this.faceUtils.urlRedirect("/gestionNegociaciones");
            GestionOtroSiController.btnRegresar=true;
        }
    }


    public boolean isCrearPortafolioCapita() {
        return crearPortafolioCapita;
    }

    public void setCrearPortafolioCapita(boolean crearPortafolioCapita) {
        this.crearPortafolioCapita = crearPortafolioCapita;
    }

    public NegociacionModalidadEnum getModalidadNegociacion() {
        return modalidadNegociacion;
    }

    public boolean isNegociacionEsRiasCapita() {
        return negociacionEsRiasCapita;
    }

    public void setModalidadNegociacion(NegociacionModalidadEnum modalidadNegociacion) {
        this.modalidadNegociacion = modalidadNegociacion;
    }

    public void setNegociacionEsRiasCapita(boolean negociacionEsRiasCapita) {
        this.negociacionEsRiasCapita = negociacionEsRiasCapita;
    }

    public boolean isNegociacionESRiasCapitaGrupoEtario() {
        return negociacionESRiasCapitaGrupoEtario;
    }

    public void setNegociacionESRiasCapitaGrupoEtario(boolean negociacionESRiasCapitaGrupoEtario) {
        this.negociacionESRiasCapitaGrupoEtario = negociacionESRiasCapitaGrupoEtario;
    }

    public com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum getEstadoLegalizacionNegSel() {
        return estadoLegalizacionNegSel;
    }

    public void setEstadoLegalizacionNegSel(com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum estadoLegalizacionNegSel) {
        this.estadoLegalizacionNegSel = estadoLegalizacionNegSel;
    }

    public void onToggle(ToggleEvent e) {
        lstToogle.set((Integer) e.getData(), e.getVisibility() == Visibility.VISIBLE);
    }

    public List<Boolean> getLstToogle() {
        return lstToogle;
    }

    public void setLstToogle(List<Boolean> lstToogle) {
        this.lstToogle = lstToogle;
    }

    public NegociacionDto getDtoNeg() {
        return dtoNeg;
    }

    public void setDtoNeg(NegociacionDto dtoNeg) {
        this.dtoNeg = dtoNeg;
    }

    public Boolean getGestionarOtroSi() {
        return gestionarOtroSi;
    }

    public void setGestionarOtroSi(Boolean gestionarOtroSi) {
        this.gestionarOtroSi = gestionarOtroSi;
    }

    public Boolean getValidaOtroSi() {
        return validaOtroSi;
    }

    public void setValidaOtroSi(Boolean validaOtroSi) {
        this.validaOtroSi = validaOtroSi;
    }

    public NegociacionDto getNegociacion() {
        return negociacion;
    }

    public void setNegociacion(NegociacionDto negociacion) {
        this.negociacion = negociacion;
    }
}
