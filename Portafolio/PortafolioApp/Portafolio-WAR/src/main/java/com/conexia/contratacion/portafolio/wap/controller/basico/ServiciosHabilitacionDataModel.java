/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conexia.contratacion.portafolio.wap.controller.basico;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;
import com.conexia.contratacion.portafolio.wap.facade.basico.ServicioHabilitacionCapitaFacade;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.webutils.FacesMessagesUtils;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 *
 * @author <a href="dgarcia@conexia.com">David Garcia</a>
 */

@Named("serviciosHabilitacionModel")
@ViewScoped
@URLMapping(id = "serviciosHabilitacionSede", pattern = "/gestion/serviciossede", viewId = "/basico/serviciosHabilitacionSede.page")
public class ServiciosHabilitacionDataModel extends
		LazyDataModel<ServicioPortafolioSedeDto> {

	private static final long serialVersionUID = -5821142040501439547L;
	private final Logger logger = LoggerFactory
			.getLogger(ServiciosHabilitacionDataModel.class);

	@Inject
	private FacesMessagesUtils facesMessagesUtils;

	@Inject
	private ContenedorEditarTecnologiasController contenerdorController;

	@Inject
	private ServicioHabilitacionCapitaFacade servicioHabilitacionFacade;

	private List<ServicioPortafolioSedeDto> serviciosSeleccionados;

	private List<ServicioPortafolioSedeDto> serviciosHabilitacion;

	private ServicioPortafolioSedeDto servicioModificado;

	private Boolean estadoSeleccionMasiva;

	private Map<String, Object> filters;

	/**
	 * Constructor por defecto
	 */
	public ServiciosHabilitacionDataModel() {
	}

	/**
	 * Metodo postConstruct
	 */
	@PostConstruct
	public void postConstruct() {
	}

	@Override
	public List<ServicioPortafolioSedeDto> load(int first, int pageSize,
			String sortField, SortOrder sortOrder, Map<String, Object> filters) {

		try {
			this.filters = filters;

			PortafolioSedeDto portafolio = contenerdorController
					.getPortafolio();

			setRowCount(servicioHabilitacionFacade
					.contarServiciosHabilitacionPortafolio(portafolio, filters));

			serviciosHabilitacion = servicioHabilitacionFacade
					.listarServiciosHabilitacionPortafolio(portafolio, first,
							pageSize, filters);

			serviciosSeleccionados = serviciosHabilitacion.stream()
					.filter(s -> Boolean.TRUE.equals(s.getHabilitado()))
					.collect(Collectors.toList());

			return serviciosHabilitacion;
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Se ha presentado un error al cargar la informaci\u00f3n.");
		}

		return new ArrayList<ServicioPortafolioSedeDto>();
	}

	/* Getter & Setter */

	private void setServicioModificado(
			ServicioPortafolioSedeDto servicioModificado) {
		this.servicioModificado = servicioModificado;
	}

	public HttpSession obtenerSession() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return (HttpSession) facesContext.getExternalContext().getSession(true);
	}

	public List<ServicioPortafolioSedeDto> getServiciosSeleccionados() {
		return serviciosSeleccionados;
	}

	public void setServiciosSeleccionados(
			List<ServicioPortafolioSedeDto> serviciosSeleccionados) {
		this.serviciosSeleccionados = serviciosSeleccionados;
	}

	@Override
	public Object getRowKey(ServicioPortafolioSedeDto servicioPortafolioCapita) {
		return servicioPortafolioCapita.getId();
	}

	@Override
	public ServicioPortafolioSedeDto getRowData(String rowKey) {
		Optional<ServicioPortafolioSedeDto> optionalResult = serviciosHabilitacion
				.stream()
				.filter(s -> Integer.valueOf(rowKey).equals(s.getId()))
				.findFirst();

		return optionalResult.orElse(null);
	}

	public void deseleccionarFila(UnselectEvent event) {
		setServicioModificado((ServicioPortafolioSedeDto) event.getObject());
	}

	public void seleccionarFila(SelectEvent event) {
		try {
			servicioModificado = (ServicioPortafolioSedeDto) event.getObject();

			servicioModificado.setHabilitado(Boolean.TRUE);
			servicioHabilitacionFacade
					.cambiarEstadoServicioHabilitacion(servicioModificado);
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Se ha presentado un error al cargar la informaci\u00f3n.");
			logger.error(
					"Se ha presentado un error al cargar la informaci\u00f3n.",
					e);
		}
	}

	public void toggleSelect(ToggleSelectEvent event) {
		estadoSeleccionMasiva = event.isSelected();
	}

	public void confirmacionDeseleccion(Boolean confirmacion) {
		if (Boolean.FALSE.equals(confirmacion)) {
			servicioModificado.setHabilitado(true);
			serviciosSeleccionados.add(servicioModificado);
		} else {
			try {
				servicioModificado.setHabilitado(Boolean.FALSE);
				servicioHabilitacionFacade
						.cambiarEstadoServicioHabilitacion(servicioModificado);
			} catch (ConexiaBusinessException | ConexiaSystemException e) {
				facesMessagesUtils
						.addError("Se ha presentado un error al cargar la informaci\u00f3n.");
			}
		}
	}

	public void confirmacionSeleccionMasiva() {
		try {
			filters.put("nuevoEstado", estadoSeleccionMasiva);
			servicioHabilitacionFacade
					.cambiarEstadoTodosServicioHabilitacionPortafolio(
							contenerdorController.getPortafolio(), filters);
			
			facesMessagesUtils.addInfo("Servicios modificados con \u00E9xito.");
			
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Se ha presentado un error al cargar la informaci\u00f3n.");
		}
	}
}
