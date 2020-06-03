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

import com.conexia.contratacion.commons.dto.capita.ProcedimientoServicioPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;
import com.conexia.contratacion.portafolio.SessionId;
import com.conexia.contratacion.portafolio.wap.facade.basico.ProcedimientoServicioFacade;
import com.conexia.contratacion.portafolio.wap.facade.basico.ServicioHabilitacionCapitaFacade;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.FlashScopeUtils;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 *
 * @author <a href="dgarcia@conexia.com">David Garcia</a>
 */

@Named("cupsServicioModel")
@ViewScoped
@URLMapping(id = "cupsServiciosHabilitacion", pattern = "/gestion/procedimientosservicio", viewId = "/basico/cupsServiciosHabilitacion.page")
public class CupsServiciosHabilitacionDataModel extends
		LazyDataModel<ProcedimientoServicioPortafolioSedeDto> {

	private static final long serialVersionUID = -5821142040501439547L;

	@Inject
	private FacesMessagesUtils facesMessagesUtils;

	@Inject
	private FacesUtils facesUtils;

	@Inject
    private FlashScopeUtils flashScopeUtils;
	
	@Inject
	private ServicioHabilitacionCapitaFacade servicioHabilitacionFacade;

	@Inject
	private ProcedimientoServicioFacade procedimientoServicioFacade;

	private List<ProcedimientoServicioPortafolioSedeDto> pxHabilitacion;
	private List<ProcedimientoServicioPortafolioSedeDto> pxSeleccionados;

	private ServicioPortafolioSedeDto seleccion;

	private Map<String, Boolean> mapaModalidadservicio;

	private String complejidadServicio;

	private Integer tipoProcedimientoVisible = new Integer(0);

	private final Integer MOSTRAR_TODOS_PROCEDIMIENTOS = new Integer(0);

	private Map<String, Object> filters;
	
	/**
	 * Constructor por defecto
	 */
	public CupsServiciosHabilitacionDataModel() {
	}

	/**
	 * Metodo postConstruct
	 */
	@PostConstruct
	public void postConstruct() {
		HttpSession session = obtenerSession();
		seleccion = (ServicioPortafolioSedeDto) session
				.getAttribute(SessionId.SERVICIO_PORTAFOLIO_SEDE_ID);

		if (seleccion == null) {
			facesUtils.urlRedirect("/gestion/serviciossede");
			return;
		}

		try {
			mapaModalidadservicio = servicioHabilitacionFacade
					.obtenerModalidadServicio(seleccion);

			complejidadServicio = servicioHabilitacionFacade
					.calcularComplejidadServicio(seleccion);
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Se ha presentado un error al cargar la informaci\u00f3n.");
		}
	}

	@Override
	public List<ProcedimientoServicioPortafolioSedeDto> load(int first,
			int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		try {
			
			this.filters = filters;
			if (!(MOSTRAR_TODOS_PROCEDIMIENTOS
					.equals(getTipoProcedimientoVisible()))) {
				filters.put("tipoPPM", getTipoProcedimientoVisible());
			}

			setRowCount(procedimientoServicioFacade
					.contarProcedimientosPorServicio(seleccion, filters));

			pxHabilitacion = procedimientoServicioFacade
					.listarProcedimientosPorServicio(seleccion, filters, first,
							pageSize);

			pxSeleccionados = pxHabilitacion.stream()
					.filter(s -> Boolean.TRUE.equals(s.getHabilitado()))
					.collect(Collectors.toList());

			return pxHabilitacion;

		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Se ha presentado un error al cargar la informaci\u00f3n.");
		}

		return new ArrayList<ProcedimientoServicioPortafolioSedeDto>();
	}

	/* Getter & Setter */
	public HttpSession obtenerSession() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return (HttpSession) facesContext.getExternalContext().getSession(true);
	}

	public ServicioPortafolioSedeDto getSeleccion() {
		return seleccion;
	}

	public void setSeleccion(ServicioPortafolioSedeDto seleccion) {
		this.seleccion = seleccion;
	}

	public String activarModalidad(String nombreModalidad) {
		return Boolean.TRUE.equals(mapaModalidadservicio.get(nombreModalidad)) ? "active"
				: "";
	}

	public List<ProcedimientoServicioPortafolioSedeDto> getPxHabilitacion() {
		return pxHabilitacion;
	}

	public void setPxHabilitacion(
			List<ProcedimientoServicioPortafolioSedeDto> pxHabilitacion) {
		this.pxHabilitacion = pxHabilitacion;
	}

	public List<ProcedimientoServicioPortafolioSedeDto> getPxSeleccionados() {
		return pxSeleccionados;
	}

	public void setPxSeleccionados(
			List<ProcedimientoServicioPortafolioSedeDto> pxSeleccionados) {
		this.pxSeleccionados = pxSeleccionados;
	}

	public String getComplejidadServicio() {
		return complejidadServicio;
	}

	public void setComplejidadServicio(String complejidadServicio) {
		this.complejidadServicio = complejidadServicio;
	}

	public Integer getTipoProcedimientoVisible() {
		return tipoProcedimientoVisible;
	}

	public void setTipoProcedimientoVisible(Integer tipoProcedimientoVisible) {
		this.tipoProcedimientoVisible = tipoProcedimientoVisible;
	}

	/*********************************/

	@Override
	public Object getRowKey(
			ProcedimientoServicioPortafolioSedeDto servicioPortafolioCapita) {
		return servicioPortafolioCapita.getId();
	}

	@Override
	public ProcedimientoServicioPortafolioSedeDto getRowData(String rowKey) {
		Optional<ProcedimientoServicioPortafolioSedeDto> optionalResult = pxHabilitacion
				.stream()
				.filter(s -> Integer.valueOf(rowKey).equals(s.getId()))
				.findFirst();

		return optionalResult.orElse(null);
	}

	/*********************************/
	/* Acciones */

	public void seleccionarFila(SelectEvent event) {
		try {

			ProcedimientoServicioPortafolioSedeDto pxModificado = (ProcedimientoServicioPortafolioSedeDto) event
					.getObject();
			pxModificado.setHabilitado(Boolean.TRUE);
			pxModificado.setServicioPortafolioSede(seleccion);
			
			procedimientoServicioFacade
					.cambiarEstadoProcedimiento(pxModificado);

		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Ocurrio un error actualizando el procedimiento.");
		}
	}

	public void deseleccionarFila(UnselectEvent event) {
		try {

			ProcedimientoServicioPortafolioSedeDto pxModificado = (ProcedimientoServicioPortafolioSedeDto) event
					.getObject();
			pxModificado.setHabilitado(Boolean.FALSE);
			pxModificado.setServicioPortafolioSede(seleccion);
			
			procedimientoServicioFacade
					.cambiarEstadoProcedimiento(pxModificado);

		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Ocurrio un error actualizando el procedimiento.");
		}
	}

	public void seleccionarMasivaProcedimientos(ToggleSelectEvent event) {

		try {			
			seleccion.setHabilitado(event.isSelected());
			procedimientoServicioFacade
					.cambiarEstadoTodosProcedimeitos(seleccion, filters);
			
			facesMessagesUtils.addInfo("Procedimientos modificados con \u00E9xito.");
			
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Ocurrio un error actualizando el servicio de habilitacion.");
		}

	}

	public void actualizarCodigoInterno(
			ProcedimientoServicioPortafolioSedeDto procedimientoModificado) {

		try {

		procedimientoServicioFacade
					.cambiarCodigoInternoProcedimiento(procedimientoModificado);

		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Ocurrio un error actualizando el servicio de habilitacion.");
		}
	}

	/* Acciones */
	public String regresar() {
		flashScopeUtils.setParam("activeTab", 0);
		return FacesUtils.redirection("/basico/contenedorEditarTecnologias.page");
	}
}
