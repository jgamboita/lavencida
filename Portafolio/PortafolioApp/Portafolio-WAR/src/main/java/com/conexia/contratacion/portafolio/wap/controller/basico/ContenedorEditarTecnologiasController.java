package com.conexia.contratacion.portafolio.wap.controller.basico;

import java.io.Serializable;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.capita.CategoriaMedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.OfertaPrestadorDto;
import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;
import com.conexia.contratacion.portafolio.SessionId;
import com.conexia.contratacion.portafolio.wap.facade.basico.SedePrestadorFacade;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.FlashScopeUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 * Controlador que maneja la pantalla de cargue de los datos del prestador
 * 
 * @author mcastro
 *
 */
@Named("contenedorTecnologias")
@ViewScoped
@URLMapping(id = "contenedorEditarTecnologias", pattern = "/gestion/editartecnologias", viewId = "/basico/contenedorEditarTecnologias.page")
public class ContenedorEditarTecnologiasController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5821142040501439547L;

	@Inject
	private Log logger;

	@Inject
	private FacesMessagesUtils facesMessagesUtils;

	@Inject
	private FacesUtils facesUtils;

	@Inject
	@CnxI18n
	transient ResourceBundle resourceBundle;

	@Inject
	private SedePrestadorFacade sedePrestadorFacade;

	@Inject
    private FlashScopeUtils flashScopeUtils;
	
	private PortafolioSedeDto portafolio;

	private SedePrestadorDto sedePrestador;

	private Integer activeTab;

	/**
	 * Metodo postConstruct
	 */
	@PostConstruct
	public void postConstruct() {
		HttpSession session = obtenerSession();
		OfertaPrestadorDto ofertaPrestador = (OfertaPrestadorDto) session
				.getAttribute(SessionId.OFERTA_PRESTADOR_ID);
		sedePrestador = (SedePrestadorDto) session
				.getAttribute(SessionId.SEDE_PRESTADOR_ID);

		activeTab = flashScopeUtils.getParam("activeTab");
		
		try {
			if ((ofertaPrestador == null) || (sedePrestador == null)) {
				facesUtils.urlRedirect("/basico/listadoSedesPrestador.page");
				return;
			}

			portafolio = sedePrestadorFacade
					.obtenerPortafolioPorOfertaIdYSedeId(
							ofertaPrestador.getId(), sedePrestador.getId());

			session.setAttribute(SessionId.PORTAFOLIO_SEDE_ID, portafolio);
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Se ha presentado un error al cargar la informaci\u00f3n.");

			logger.error(
					"Se ha presentado un error al cargar la informaci\u00f3n.",
					e);
		}
	}

	/* Acciones */

	public String seleccionarSede(SedePrestadorDto sedePrestador) {
		HttpSession session = obtenerSession();
		session.setAttribute(SedePrestadorDto.class.getSimpleName(),
				sedePrestador);

		return FacesUtils.redirection("/basico/formularioSedePrestador.page");
	}

	public String seleccionarServicioSalud(
			ServicioPortafolioSedeDto servicioPortafolioSede) {
		HttpSession session = obtenerSession();
		session.setAttribute(SessionId.SERVICIO_PORTAFOLIO_SEDE_ID,
				servicioPortafolioSede);

		return FacesUtils.redirection("/basico/cupsServiciosHabilitacion.page");
	}

	public String seleccionarCategoriaMedicamento(
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento) {
		HttpSession session = obtenerSession();
		session.setAttribute(SessionId.CATEGORIA_MEDICAMENTO_ID,
				categoriaMedicamento);

		return FacesUtils
				.redirection("/basico/listadoMedicamentoCategoria.page");
	}

	/* Acciones */
	public String regresar() {
		return FacesUtils.redirection("/basico/listadoSedesPrestador.page");
	}

	/* Getter & Setter */

	public HttpSession obtenerSession() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return (HttpSession) facesContext.getExternalContext().getSession(true);
	}

	public PortafolioSedeDto getPortafolio() {
		return portafolio;
	}

	public SedePrestadorDto getSedePrestador() {
		return sedePrestador;
	}

	public void setSedePrestador(SedePrestadorDto sedePrestador) {
		this.sedePrestador = sedePrestador;
	}

	public Integer getActiveTab() {
		return activeTab;
	}

	public void setActiveTab(Integer activeTab) {
		this.activeTab = activeTab;
	}

}
