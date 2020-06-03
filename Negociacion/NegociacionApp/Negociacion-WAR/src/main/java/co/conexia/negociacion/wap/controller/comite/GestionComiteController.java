package co.conexia.negociacion.wap.controller.comite;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JRException;

import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.StreamedContent;

import co.conexia.negociacion.wap.facade.comite.GestionComiteFacade;

import com.conexia.contratacion.commons.constants.enums.ComiteSessionEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoComiteEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorComiteEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.comite.AsistenteComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.TablaPrestadoresComiteDto;
import com.conexia.contratacion.commons.dto.comparacion.ResumenGestionCasosDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 * Controller creado para la gestion de comités
 * 
 * @author jtorres
 *
 */
@Named
@ViewScoped
@URLMapping(id = "gestionarComite", pattern = "/gestionarComite", viewId = "/comite/gestionarComite.page")
public class GestionComiteController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5821142040506999547L;

	@Inject
	private Log logger;

	@Inject
	private FacesMessagesUtils facesMessagesUtils;

	@Inject
	private FacesUtils faceUtils;

	@Inject
	@CnxI18n
	transient ResourceBundle resourceBundle;

	@Inject
	private PreContractualExceptionUtils exceptionUtils;

	@Inject
	private GestionComiteFacade gestionComiteFacade;

	@Inject
	private ActaComiteController actaComiteController;

	@Inject
	private ConceptoComiteController conceptoComiteController;

	@Inject
	@UserInfo
	private UserApp user;
	
	/** Lista de asistentes al comite seleccionado. **/
	private List<AsistenteComitePrecontratacionDto> asistentes;

	private ComitePrecontratacionDto comiteActual;

	private List<TablaPrestadoresComiteDto> prestadoresComite;

	private ResumenGestionCasosDto resumenGestionCasos;

	private boolean puedeTramitar = false;

	private Date fechaAplazaComite = null;

	
	
	/**
	 * Constructor por defecto
	 */
	public GestionComiteController() {
	}

	/**
	 * Metodo postConstruct
	 */
	@PostConstruct
	public void postConstruct() {
		this.resumenGestionCasos = new ResumenGestionCasosDto();
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext()
				.getSession(true);
		this.comiteActual = (ComitePrecontratacionDto) session
				.getAttribute(ComiteSessionEnum.COMITE_PRECONTRATACION
						.toString());
		if (comiteActual == null) {
			this.faceUtils.urlRedirect("/bandejaComite");
		} else {
			// Se busca los asistentes al comite..
			this.buscarAsistentesComite();
			// Se busca los prestadores asociados a este comite...
			this.buscarPrestadoresComite();
			// calculo de numero de casos gestionados
			this.calcularCasosGestionadosComite();
			// Se evalua si el comite está finalizado se muestra la tabla de
			// prestadores...
			if (this.comiteActual.getEstadoComite().equals(
					EstadoComiteEnum.FINALIZADO)) {
				this.puedeTramitar = true;
			}
			
			fechaAplazaComite = comiteActual.getFechaComite();
		}
	}

	/**
	 * Verifica si el rol esta capacitado para gestionar comites.
	 * 
	 * @return
	 */
	public boolean validarRolGestionarComites() {
		// TODO: Traer el rol apropiado.
		return true;
	}

	/**
	 * Método que busca los comites creados en el sistema.
	 */
	private List<AsistenteComitePrecontratacionDto> buscarAsistentesComite() {
		try {
			asistentes = this.gestionComiteFacade
					.buscarAsistentesComite(comiteActual.getId());
		} catch (Exception e) {
			logger.error(
					"Error al realizar la busqueda de asistentes al comite: "
							+ this.comiteActual.getId(), e);
			facesMessagesUtils.addError(exceptionUtils
					.createSystemErrorMessage(resourceBundle));
		}
		return asistentes;
	}

	/**
	 * Método que busca los comites creados en el sistema.
	 */
	private List<TablaPrestadoresComiteDto> buscarPrestadoresComite() {
		try {
			prestadoresComite = this.gestionComiteFacade
					.buscarPrestadoresComiteByComiteId(comiteActual.getId());
		} catch (Exception e) {
			logger.error(
					"Error al realizar la busqueda de prestadores al comite: "
							+ this.comiteActual.getId(), e);
			facesMessagesUtils.addError(exceptionUtils
					.createSystemErrorMessage(resourceBundle));
		}
		return prestadoresComite;
	}

	/**
	 * Maneja el evento de ediar una celda de la tabla
	 * 
	 * @param event
	 */
	public void onRowEdit(RowEditEvent event) {
		AsistenteComitePrecontratacionDto asistente = null;
		try {
			asistente = (AsistenteComitePrecontratacionDto) event.getObject();
			this.gestionComiteFacade.actualizarAsistenteComite(asistente,
					asistente.getId());
			facesMessagesUtils.addInfo(resourceBundle
					.getString("tbl_asistentes_comite_msg_edit_exito"));
		} catch (Exception e) {
			logger.error(
					"Error al actualizasr el asistente " + event.toString()
							+ " al comite: " + this.comiteActual.getId(), e);
			facesMessagesUtils.addError(exceptionUtils
					.createSystemErrorMessage(resourceBundle));
		}

	}

	/**
	 * Maneja la acción de cancelar la edición de una celda de la tabla.
	 * 
	 * @param event
	 */
	public void onRowCancel(RowEditEvent event) {
		facesMessagesUtils.addInfo(resourceBundle
				.getString("tbl_asistentes_comite_msg_edit_cancel"));
	}

	/**
	 * Aplaza el comite actual
	 */
	public void aplazarComite() {
		EstadoComiteEnum estadoFinalAplazado = EstadoComiteEnum.APLAZADO;

		// Inicia el iterador para recorrer el ciclo
		for (Iterator<TablaPrestadoresComiteDto> prestadorComite = prestadoresComite
				.iterator();
			// Comprueban que existan iteraciones y que el estado no haya sido
			// modificado
			prestadorComite.hasNext()
				&& estadoFinalAplazado.equals(EstadoComiteEnum.APLAZADO);) {

			EstadoPrestadorComiteEnum estadoPrestadorComite = prestadorComite.next().getEstado();
					
			// Si el comite prestador fue aprobado o negado el comite no se
			// úedefinalizar como reprogramado
			if (estadoPrestadorComite
					.equals(EstadoPrestadorComiteEnum.APROBADO)
					|| estadoPrestadorComite
							.equals(EstadoPrestadorComiteEnum.NEGADO)) {

				estadoFinalAplazado = EstadoComiteEnum.FINALIZADO;
			}
		}

		try {
			ComitePrecontratacionDto comiteNuevo = new ComitePrecontratacionDto();
			comiteNuevo.setEstadoComite(estadoFinalAplazado);
			comiteNuevo.setFechaComite(getFechaAplazaComite());
			
			gestionComiteFacade.aplazarComite(comiteActual, comiteNuevo, user.getId());
			
			facesMessagesUtils.addInfo(resourceBundle
					.getString("gestionar_comite_msg_exito_actualizar"));
			
			this.faceUtils.urlRedirect("/bandejaComite");
		} catch (Exception e) {
			logger.error(
					"Error al aplazar el comite: " + this.comiteActual.getId(),
					e);
			facesMessagesUtils.addError(exceptionUtils
					.createSystemErrorMessage(resourceBundle));
		}

	}

	/**
	 * Reinicia la fecha de aplazamiento cuando se cancela la accion aplazar
	 * para que siempre inicie con la fecha actual del comite
	 */
	public void cancelarAplazarComite(){
		setFechaAplazaComite(comiteActual.getFechaComite());
	}
	
	/**
	 * Habilita el proceso de tramitar comite.
	 */
	public void tramitarComite() {
		boolean asisteAlguno = false;
		for (AsistenteComitePrecontratacionDto dto : asistentes) {
			if (dto.getSiAsiste()) {
				asisteAlguno = true;
			}
		}
		
		this.puedeTramitar = asisteAlguno;
		if (!this.puedeTramitar) {
			facesMessagesUtils.addError(resourceBundle
					.getString("gestionar_comite_msg_tramitar_error"));
		}
	}

	/**
	 * Termina o finaliza un comite seleccionado para pasar sus prestadores a
	 * negociacion o simplemente cerrar el comite como tal.
	 */
	public void terminarComite() {
		try {
			this.gestionComiteFacade.finalizarComite(this.comiteActual.getId(), user.getId());
			facesMessagesUtils.addInfo(resourceBundle
					.getString("gestionar_comite_msg_exito_actualizar"));
			this.faceUtils.urlRedirect("/bandejaComite");
		} catch (Exception e) {
			logger.error(
					"Error al terminar el comite: " + this.comiteActual.getId(),
					e);
			facesMessagesUtils.addError(exceptionUtils
					.createSystemErrorMessage(resourceBundle));
		}

	}

	public void imprimirActa() {
		if (!gestionComiteFacade.validarExisteActaComite(comiteActual.getId())) {
			faceUtils.urlRedirect("/actaComite");
		}
	}

	public StreamedContent getActaComite() throws JRException, IOException,
			ConexiaBusinessException {
		return actaComiteController.imprimirActa();
	}

	/**
	 * Muestra el portafolio del prestador seleccionado
	 * 
	 * @param prestadorId
	 */
	public void verPortafolio(Long prestadorId) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext()
				.getSession(true);
		session.setAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString(),
				prestadorId);
		this.faceUtils.urlRedirect("/detallePortafolio");
	}

	/**
	 * Guarda el prestadorComiteId en la session
	 * 
	 * @param prestadorComite
	 *            Dto de {@link - TablaPrestadoresComiteDto}
	 */
	public void gestionarComite(TablaPrestadoresComiteDto prestadorComite) {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext()
				.getSession(true);
		session.setAttribute(
				NegociacionSessionEnum.PRESTADOR_COMITE.toString(),
				prestadorComite);
		this.conceptoComiteController.cargarInformacion();
	}

	public void comparacionTarifas(Long prestadorId) {
		if (prestadorId == null) {
			this.faceUtils.urlRedirect("/gestionNecesidad/bandeja");
		} else {
			try {
				FacesContext facesContext = FacesContext.getCurrentInstance();
				HttpSession session = (HttpSession) facesContext
						.getExternalContext().getSession(true);
				session.setAttribute(
						NegociacionSessionEnum.PRESTADOR_ID.toString(),
						prestadorId);
				this.faceUtils.urlRedirect("/gestionNecesidad/comparar");
			} catch (Exception e) {
				logger.error("Error al consultar al prestador con Id:"
						+ prestadorId, e);
				facesMessagesUtils.addError(exceptionUtils
						.createSystemErrorMessage(resourceBundle));
			}
		}
	}
        
        public void regresar(){
		faceUtils.urlRedirect("/bandejaComite");
	}

	/**
	 * Calcula los casos gestionados en el comite
	 */
	public void calcularCasosGestionadosComite() {
		// Aplazados
		this.resumenGestionCasos
				.setNumAplazados(this.prestadoresComite
						.stream()
						.filter(pc -> pc.getEstado() == EstadoPrestadorComiteEnum.APLAZADO)
						.collect(Collectors.toList()).size());
		// Negados
		this.resumenGestionCasos
				.setNumNegados(this.prestadoresComite
						.stream()
						.filter(pc -> pc.getEstado() == EstadoPrestadorComiteEnum.NEGADO)
						.collect(Collectors.toList()).size());
		// Aprobados
		this.resumenGestionCasos
				.setNumAprobados(this.prestadoresComite
						.stream()
						.filter(pc -> pc.getEstado() == EstadoPrestadorComiteEnum.APROBADO)
						.collect(Collectors.toList()).size());
		// Total
		this.resumenGestionCasos
				.setNumTotalCasos(this.prestadoresComite.size());
	}

	
	public void onSeleccionFechaAplazarComite(SelectEvent event) {
		Date fechaAplazaComite = (Date) event.getObject();
        if( !(fechaAplazaComite.equals(getComiteActual().getFechaComite()))
        		&& gestionComiteFacade.evaluarFechaDisponible(fechaAplazaComite)){
        	
        	setFechaAplazaComite(fechaAplazaComite);
        } else {
			facesMessagesUtils.addWarning(resourceBundle
					.getString("gestionar_comite_msg_rep_fecha_no_valida"));
        }
    }
	
	public boolean getMostrarAplazar(){
		return getResumenGestionCasos().getNumTotalGestionados()
					.equals(getResumenGestionCasos().getNumAplazados());
	}
	
	/**
	 * @return the asistentes
	 */
	public List<AsistenteComitePrecontratacionDto> getAsistentes() {
		return asistentes;
	}

	/**
	 * @param asistentes
	 *            the asistentes to set
	 */
	public void setAsistentes(List<AsistenteComitePrecontratacionDto> asistentes) {
		this.asistentes = asistentes;
	}

	/**
	 * @return the puedeTramitar
	 */
	public boolean isPuedeTramitar() {
		return puedeTramitar;
	}

	/**
	 * @param puedeTramitar
	 *            the puedeTramitar to set
	 */
	public void setPuedeTramitar(boolean puedeTramitar) {
		this.puedeTramitar = puedeTramitar;
	}

	/**
	 * @return the comiteActual
	 */
	public ComitePrecontratacionDto getComiteActual() {
		return comiteActual;
	}

	/**
	 * @param comiteActual
	 *            the comiteActual to set
	 */
	public void setComiteActual(ComitePrecontratacionDto comiteActual) {
		this.comiteActual = comiteActual;
	}

	public List<TablaPrestadoresComiteDto> getPrestadoresComite() {
		return prestadoresComite;
	}

	public void setPrestadoresComite(
			List<TablaPrestadoresComiteDto> prestadoresComite) {
		this.prestadoresComite = prestadoresComite;
	}

	public ResumenGestionCasosDto getResumenGestionCasos() {
		return resumenGestionCasos;
	}

	public Date getFechaAplazaComite() {
		return fechaAplazaComite;
	}

	public void setFechaAplazaComite(Date fechaAplazaComite) {
		this.fechaAplazaComite = fechaAplazaComite;
	}

	public Date getFechaMinimaAplazamiento(){
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(getComiteActual().getFechaLimitePrestadores());
		calendar.add(Calendar.DAY_OF_MONTH, 1);
		
		return calendar.getTime();
	}
}
