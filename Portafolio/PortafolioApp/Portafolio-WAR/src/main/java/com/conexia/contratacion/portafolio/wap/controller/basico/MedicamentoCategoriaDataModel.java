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

import com.conexia.contratacion.commons.dto.capita.CategoriaMedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.MedicamentoPortafolioSedeDto;
import com.conexia.contratacion.portafolio.SessionId;
import com.conexia.contratacion.portafolio.wap.facade.basico.MedicamentoPortafolioCapitaFacade;
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

@Named("listaMedicamentoModel")
@ViewScoped
@URLMapping(id = "medicamentoCategoriaModel", pattern = "/gestion/medicamentocategoria", viewId = "/basico/listadoMedicamentoCategoria.page")
public class MedicamentoCategoriaDataModel extends
		LazyDataModel<MedicamentoPortafolioSedeDto> {

	private static final long serialVersionUID = -5821142040501439547L;

	@Inject
	private FacesMessagesUtils facesMessagesUtils;

	@Inject
	private MedicamentoPortafolioCapitaFacade medicamentoPortafolioFacade;

	@Inject
	private FacesUtils facesUtils;
	
	@Inject
    private FlashScopeUtils flashScopeUtils;
	
	private List<MedicamentoPortafolioSedeDto> medicamentos;

	private List<MedicamentoPortafolioSedeDto> medicamentoSeleccion;

	private CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento;
	
	private Map<String, Object> filters;

	/**
	 * Constructor por defecto
	 */
	public MedicamentoCategoriaDataModel() {
	}

	/**
	 * Metodo postConstruct
	 */
	@PostConstruct
	public void postConstruct() {
		CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento = 
				(CategoriaMedicamentoPortafolioSedeDto) obtenerSession()
					.getAttribute(SessionId.CATEGORIA_MEDICAMENTO_ID);

		if(categoriaMedicamento == null){
			facesUtils.urlRedirect("/gestion/sedesprestador");
			return;
		}
			
		
		setCategoriaMedicamento(categoriaMedicamento);
	}

	@Override
	public List<MedicamentoPortafolioSedeDto> load(int offset,
			int tamanioPagina, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {

		try {
			
			this.filters = filters;
			medicamentos = medicamentoPortafolioFacade
					.listarMedicamentosPortafolio(getCategoriaMedicamento(),
							offset, tamanioPagina, filters);

			setRowCount(medicamentoPortafolioFacade
					.contarMedicamentosPortafolio(getCategoriaMedicamento(), filters));

			medicamentoSeleccion = medicamentos.stream()
					.filter(s -> Boolean.TRUE.equals(s.getHabilitado()))
					.collect(Collectors.toList());

			return medicamentos;
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Se ha presentado un error al cargar la informaci\u00f3n.");
		}

		return new ArrayList<MedicamentoPortafolioSedeDto>();
	}

	/* Getter & Setter */

	public List<MedicamentoPortafolioSedeDto> getMedicamentos() {
		return medicamentos;
	}

	public void setMedicamentos(List<MedicamentoPortafolioSedeDto> medicamentos) {
		this.medicamentos = medicamentos;
	}

	public List<MedicamentoPortafolioSedeDto> getMedicamentoSeleccion() {
		return medicamentoSeleccion;
	}

	public void setMedicamentoSeleccion(
			List<MedicamentoPortafolioSedeDto> medicamentoSeleccion) {
		this.medicamentoSeleccion = medicamentoSeleccion;
	}

	public CategoriaMedicamentoPortafolioSedeDto getCategoriaMedicamento() {
		return categoriaMedicamento;
	}

	public void setCategoriaMedicamento(
			CategoriaMedicamentoPortafolioSedeDto categoriaMedicamento) {
		this.categoriaMedicamento = categoriaMedicamento;
	}

	/**********************************************/

	@Override
	public Object getRowKey(MedicamentoPortafolioSedeDto medicamentoPortafolio) {
		return medicamentoPortafolio.getId();
	}

	@Override
	public MedicamentoPortafolioSedeDto getRowData(String rowKey) {
		Optional<MedicamentoPortafolioSedeDto> optionalResult = medicamentos
				.stream()
				.filter(s -> Integer.valueOf(rowKey).equals(s.getId()))
				.findFirst();

		return optionalResult.orElse(null);
	}

	public HttpSession obtenerSession() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		return (HttpSession) facesContext.getExternalContext().getSession(true);
	}

	/*********************************/
	/* Acciones */

	public void seleccionarFila(SelectEvent event) {		
		try {
			MedicamentoPortafolioSedeDto medicamento = (MedicamentoPortafolioSedeDto) event
					.getObject();
			
			cambiarEstadoMedicamento(medicamento, true);
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Ocurrio un error actualizando el medicamento.");
		}
	}

	public void deseleccionarFila(UnselectEvent event) {
		try {
			MedicamentoPortafolioSedeDto medicamento = (MedicamentoPortafolioSedeDto) event
					.getObject();
			
			cambiarEstadoMedicamento(medicamento, false);
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Ocurrio un error actualizando el procedimiento.");
		}
	}

	void cambiarEstadoMedicamento(MedicamentoPortafolioSedeDto medicamento,
			Boolean nuevoEstado) throws ConexiaBusinessException {

		medicamento.setHabilitado(nuevoEstado);
		medicamento
				.setCategoriaMedicamentoPortafolio(getCategoriaMedicamento());

		medicamentoPortafolioFacade
				.cambiarEstadoMedicamentoPortafolio(medicamento);
	}

	public void seleccionMasiva(ToggleSelectEvent event) {

		try {
			
			getCategoriaMedicamento().setHabilitado(event.isSelected());
			medicamentoPortafolioFacade
					.cambiarEstadoMedicamentosCategoriaPortafolio(getCategoriaMedicamento(), filters);
			
			facesMessagesUtils.addInfo("Medicamentos modificados con \u00E9xito.");
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Ocurrio un error actualizando el servicio de habilitacion.");
		}

	}
	
	public String regresar(){
		flashScopeUtils.setParam("activeTab", 1);
		return FacesUtils.redirection("/basico/contenedorEditarTecnologias.page");
	}
}
