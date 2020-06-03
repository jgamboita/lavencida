package co.conexia.negociacion.wap.controller.bandeja.comite;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import co.conexia.negociacion.wap.facade.bandeja.comite.BandejaComiteFacade;
import co.conexia.negociacion.wap.facade.comite.GestionComiteFacade;

import com.conexia.contratacion.commons.constants.enums.ComiteSessionEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoComiteEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.TrazabilidadDto;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 * Controller creado para la bandeja de comites
 *
 * @author jtorres
 *
 */
@Named
@ViewScoped
@URLMapping(id = "bandejaComite", pattern = "/bandejaComite", viewId = "/bandeja/comite/bandejaComite.page")
public class BandejaComiteController implements Serializable {

    private static final long serialVersionUID = -5821142040506999547L;

    @Inject
    private Log logger;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private FacesUtils faceUtils;

    @Inject
    private PreContractualExceptionUtils exceptionUtils;

    @Inject
    private BandejaComiteFacade bandejaComiteFacade;

    @Inject
    private GestionComiteFacade gestionComiteFacade;
    
    @Inject
    @UserInfo
    private UserApp user;

    /**
     * Lista de comites obtenidos para la tabla general de comites. *
     */
    private List<ComitePrecontratacionDto> comites;

    private List<TrazabilidadDto> historicoComite;

    /**
     * Fecha actual del sistema *
     */
    private Date fechaActual;

    /**
     * Variables para autorización del sistema *
     */
    private Boolean puedeProgramarComite;

    private Boolean puedeGestionarComite;

    /////// Campos nuevo comite //////////////////////
    private Date fechaComiteNuevo;
    private Date fechaLimitePrestadoresNuevo;
    private Integer limiteCantidadPrestadores;

    private Long comiteSeleccionadoId;
    private StreamedContent actaComite;

    /**
     * Constructor por defecto
     */
    public BandejaComiteController() {
    }

    /**
     * Metodo postConstruct
     */
    @PostConstruct
    public void postConstruct() {
        comites = this.buscarComites();
        fechaActual = new Date();
        puedeGestionarComite = this.validarRolGestionarComites();
        puedeProgramarComite = this.validarRolProgramadorComites();
    }

    /**
     * Verifica si el rol esta capacitado para gestionar comites.
     *
     * @return
     */
    public boolean validarRolGestionarComites() {
        //TODO: Traer el rol apropiado.
        return true;
    }

    /**
     * Verifica si el rol esta capacitado para programar comites.
     *
     * @return
     */
    public boolean validarRolProgramadorComites() {
        //TODO: Traer el rol apropiado.
        return true;
    }

    /**
     * Método que busca los comites creados en el sistema.
     */
    private List<ComitePrecontratacionDto> buscarComites() {
        try {
            comites = bandejaComiteFacade.buscarComitesPrecontratacion();
            //comites = cargarDatosDummy();
        } catch (Exception e) {
            logger.error("Error al realizar la busqueda de prestadores.", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
        return comites;
    }

    /**
     * Método que invoca la pagina para crear un nuevo prestador.
     */
    public void crearNuevoComite() {
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('nuevoComiteDlg').show();");
    }

    /**
     * Guarda un nuevo comite dado los datos de creación.
     */
    public void guardarNuevoComite() {
        ComitePrecontratacionDto nuevo = new ComitePrecontratacionDto();
        nuevo.setFechaComite(fechaComiteNuevo);
        nuevo.setFechaLimitePrestadores(fechaLimitePrestadoresNuevo);
        nuevo.setLimitePrestadores(limiteCantidadPrestadores);
        nuevo.setEstadoComite(EstadoComiteEnum.PROGRAMADO);

        nuevo.setCantidadPrestadores(0);
        //Primero debe pasar las validaciones...
        try {
            if (bandejaComiteFacade.validarNuevoComite(nuevo, comites)) {
                //Si el comite es válido se puede guardar...
                int prestadoresAsociados = bandejaComiteFacade.guardarNuevoComite(nuevo, user.getId());
                String message = (prestadoresAsociados == 0)
                        ? resourceBundle.getString("bandeja_comite_nuevo_crear_ok")
                        : "Se ha creado el nuevo comit\u00e9 y se le han asociado " + prestadoresAsociados + " solicitudes";
                RequestContext context = RequestContext.getCurrentInstance();
                context.execute("PF('nuevoComiteDlg').hide();");
                this.buscarComites();
                facesMessagesUtils.addInfo(message);
            }
        } catch (ConexiaBusinessException e) {
            this.facesMessagesUtils.addWarning(this.exceptionUtils.createMessage(
                    resourceBundle, e));
        } catch (Exception e) {
            logger.error("Error al guardar nuevo comité.", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Reprograma un comite ya creado en una fecha válida.
     *
     * @param comite
     * @param value
     */
    public void reProgramarComite(ComitePrecontratacionDto comite, Date value) {
        try {
            this.bandejaComiteFacade.esFechaValida(comite,
                    value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            this.bandejaComiteFacade.validarComiteExistente(comite, comites);
            this.bandejaComiteFacade.actualizarComite(comite);
            this.facesMessagesUtils.addInfo(resourceBundle.getString("bandeja_comite_reprogramar_ok"));
        } catch (ConexiaBusinessException e) {
            this.facesMessagesUtils.addWarning(this.exceptionUtils.createMessage(
                    resourceBundle, e));
        } catch (Exception e) {
            logger.error("Error al guardar nuevo comité.", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        } finally {
            this.buscarComites();
        }
    }

    /**
     * Valida el estado del comite para hablitar la edición del calendario.
     *
     * @return <b>True</b> Si el estado no es valido para edición, <b>False</b>
     * de lo contrario.
     */
    public boolean validarEstadoComite(ComitePrecontratacionDto comite) {
        if (comite.getEstadoComite().equals(EstadoComiteEnum.APLAZADO) || comite.getEstadoComite().equals(EstadoComiteEnum.FINALIZADO)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Reprograma la fecha limite para asociar prestadores a un comite ya creado
     * en una fecha válida.
     *
     * @param comite
     * @param value
     */
    public void reProgramarFechaLimitePrestadores(
            ComitePrecontratacionDto comite, Date value) {
        //Se valida el nuevo comite.
        try {
            this.bandejaComiteFacade.esFechaLimiteValida(comite,
                    value.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());
            this.bandejaComiteFacade.validarComiteExistente(comite, comites);
            //se actualiza los datos...
            this.bandejaComiteFacade.actualizarComite(comite);
            facesMessagesUtils.addInfo(resourceBundle.getString("bandeja_comite_reprogramar_ok"));
        } catch (ConexiaBusinessException e) {
            this.facesMessagesUtils.addWarning(this.exceptionUtils.createMessage(
                    resourceBundle, e));
        } catch (Exception e) {
            logger.error("Error al guardar nuevo comité.", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        } finally {
            this.buscarComites();
        }
    }

    /**
     * Carga la vista para gestionar un comite seleccionado.
     *
     * @param comite
     */
    public void loadViewVerComite(ComitePrecontratacionDto comite) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(ComiteSessionEnum.COMITE_PRECONTRATACION.toString(), comite);
        this.faceUtils.urlRedirect("/gestionarComite");
    }

    public void cargarHistorico(ComitePrecontratacionDto comite) {
        try {
            setHistoricoComite(bandejaComiteFacade.obtenerHistoricoPorComite(comite));
        } catch (Exception e) {
            logger.error("Error al guardar nuevo comité.", e);
            facesMessagesUtils.addError(exceptionUtils
                    .createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * @return the comites
     */
    public List<ComitePrecontratacionDto> getComites() {
        return comites;
    }

    /**
     * @param comites the comites to set
     */
    public void setComites(List<ComitePrecontratacionDto> comites) {
        this.comites = comites;
    }

    /**
     * @return the fechaActual
     */
    public Date getFechaActual() {
        return fechaActual;
    }

    /**
     * @param fechaActual the fechaActual to set
     */
    public void setFechaActual(Date fechaActual) {
        this.fechaActual = fechaActual;
    }

    /**
     * @return the puedeProgramarComite
     */
    public Boolean getPuedeProgramarComite() {
        return puedeProgramarComite;
    }

    /**
     * @param puedeProgramarComite the puedeProgramarComite to set
     */
    public void setPuedeProgramarComite(Boolean puedeProgramarComite) {
        this.puedeProgramarComite = puedeProgramarComite;
    }

    /**
     * @return the puedeGestionarComite
     */
    public Boolean getPuedeGestionarComite() {
        return puedeGestionarComite;
    }

    /**
     * @param puedeGestionarComite the puedeGestionarComite to set
     */
    public void setPuedeGestionarComite(Boolean puedeGestionarComite) {
        this.puedeGestionarComite = puedeGestionarComite;
    }

    /**
     * @return the fechaComiteNuevo
     */
    public Date getFechaComiteNuevo() {
        return fechaComiteNuevo;
    }

    /**
     * @param fechaComiteNuevo the fechaComiteNuevo to set
     */
    public void setFechaComiteNuevo(Date fechaComiteNuevo) {
        this.fechaComiteNuevo = fechaComiteNuevo;
    }

    /**
     * @return the fechaLimitePrestadoresNuevo
     */
    public Date getFechaLimitePrestadoresNuevo() {
        return fechaLimitePrestadoresNuevo;
    }

    /**
     * @param fechaLimitePrestadoresNuevo the fechaLimitePrestadoresNuevo to set
     */
    public void setFechaLimitePrestadoresNuevo(Date fechaLimitePrestadoresNuevo) {
        this.fechaLimitePrestadoresNuevo = fechaLimitePrestadoresNuevo;
    }

    /**
     * @return the limiteCantidadPrestadores
     */
    public Integer getLimiteCantidadPrestadores() {
        return limiteCantidadPrestadores;
    }

    /**
     * @param limiteCantidadPrestadores the limiteCantidadPrestadores to set
     */
    public void setLimiteCantidadPrestadores(Integer limiteCantidadPrestadores) {
        this.limiteCantidadPrestadores = limiteCantidadPrestadores;
    }

    public Long getComiteSeleccionadoId() {
        return comiteSeleccionadoId;
    }

    public void setComiteSeleccionadoId(Long comiteSeleccionadoId) {
        this.comiteSeleccionadoId = comiteSeleccionadoId;
    }

    public StreamedContent getActaComite() {
        return actaComite;
    }

    public void setActaComite(StreamedContent actaComite) {
        this.actaComite = actaComite;
    }

    /**
     * Muestra la ventana modal para subir el acta del comite
     *
     * @param comite
     */
    public void verSubirActa(ComitePrecontratacionDto comite) {
        comiteSeleccionadoId = comite.getId();
        RequestContext context = RequestContext.getCurrentInstance();
        context.execute("PF('subirActaDlg').show();");
    }

    /**
     * Realiza la descarga del acta adjunta a un comite
     *
     * @param comite
     */
    public void descargarActa(ComitePrecontratacionDto comite) {
        try {
            byte[] contenidoActa = bandejaComiteFacade.obtenerContenidoActa(comite.getId());
            actaComite = new DefaultStreamedContent(new ByteArrayInputStream(contenidoActa),
                    "application/pdf", "ActaComite_" + comite.getId() + ".pdf");
        } catch (Exception e) {
            logger.error("Error descargando el acta", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }

    }

    /**
     * Metodo para manejar el cargue del acta
     *
     * @param event
     */
    public void handleFileUpload(FileUploadEvent event) {
        RequestContext context = RequestContext.getCurrentInstance();
        byte[] contenido = event.getFile().getContents();
        String nombreArchivo = event.getFile().getFileName();
        try {
            bandejaComiteFacade.guardarActaComite(comiteSeleccionadoId, nombreArchivo, contenido);
            context.execute("PF('subirActaDlg').hide();");
            facesMessagesUtils.addInfo("Se ha cargado el acta correctamente");
        } catch (Exception e) {
            logger.error("Error cargando el acta", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public List<TrazabilidadDto> getHistoricoComite() {
        return historicoComite;
    }

    public void setHistoricoComite(List<TrazabilidadDto> historicoComite) {
        this.historicoComite = historicoComite;
    }

    public boolean renderSubirActa(ComitePrecontratacionDto comite){
    	return gestionComiteFacade.validarExisteActaComite(comite.getId()) 
    			&& comite.getEstadoComite().equals(EstadoComiteEnum.FINALIZADO);
    }
}
