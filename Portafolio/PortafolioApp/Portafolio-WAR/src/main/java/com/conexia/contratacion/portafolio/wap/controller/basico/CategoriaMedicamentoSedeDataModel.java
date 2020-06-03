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
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.conexia.contratacion.commons.dto.capita.CategoriaMedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.contratacion.portafolio.wap.facade.basico.MedicamentoPortafolioCapitaFacade;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.webutils.FacesMessagesUtils;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 *
 * @author <a href="dgarcia@conexia.com">David Garcia</a>
 */

@Named("categoriaMedicamentoModel")
@ViewScoped
@URLMapping(id = "categoriaMedicamentoSede", pattern = "/gestion/categoriamedicamento", viewId = "/basico/categoriaMedicamentoSede.page")
public class CategoriaMedicamentoSedeDataModel extends
		LazyDataModel<CategoriaMedicamentoPortafolioSedeDto> {

	private static final long serialVersionUID = -5821142040501439547L;
	private final Logger logger = LoggerFactory
			.getLogger(CategoriaMedicamentoSedeDataModel.class);

	@Inject
	private FacesMessagesUtils facesMessagesUtils;

	@Inject
	private MedicamentoPortafolioCapitaFacade medicamentoPortafolioFacade;

	@Inject
	private ContenedorEditarTecnologiasController contenedorTecnologiasController;

	private List<CategoriaMedicamentoPortafolioSedeDto> categoriasMedicamento;

	private List<CategoriaMedicamentoPortafolioSedeDto> categoriasMedicamentoSeleccion;

	private CategoriaMedicamentoPortafolioSedeDto categoriaModificada;

	private Boolean estadoSeleccionMasiva;

	private Map<String, Object> filters;
	
	/**
	 * Constructor por defecto
	 */
	public CategoriaMedicamentoSedeDataModel() {
	}

	/**
	 * Metodo postConstruct
	 */
	@PostConstruct
	public void postConstruct() {

	}

	@Override
	public List<CategoriaMedicamentoPortafolioSedeDto> load(int offset,
			int tamanioPagina, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {

		try {
			this.filters = filters;
			
			PortafolioSedeDto portafolio = contenedorTecnologiasController
					.getPortafolio();

			setRowCount(medicamentoPortafolioFacade
					.contarCategoriaMedicamentosPortafolio(portafolio, filters));

			categoriasMedicamento = medicamentoPortafolioFacade
					.listarCategoriaMedicamentosPortafolio(portafolio, offset,
							tamanioPagina, filters);

			categoriasMedicamentoSeleccion = categoriasMedicamento.stream()
					.filter(s -> Boolean.TRUE.equals(s.getHabilitado()))
					.collect(Collectors.toList());

			return categoriasMedicamento;
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Ocurrio un error obteniendo informacion de las categorias.");
		}

		return new ArrayList<CategoriaMedicamentoPortafolioSedeDto>();
	}

	/* Getter & Setter */

	public List<CategoriaMedicamentoPortafolioSedeDto> getCategoriasMedicamentoSeleccion() {
		return categoriasMedicamentoSeleccion;
	}

	public void setCategoriasMedicamentoSeleccion(
			List<CategoriaMedicamentoPortafolioSedeDto> categoriasMedicamentoSeleccion) {
		this.categoriasMedicamentoSeleccion = categoriasMedicamentoSeleccion;
	}

	public void setCategoriaModificada(
			CategoriaMedicamentoPortafolioSedeDto categoriaModificada) {
		this.categoriaModificada = categoriaModificada;
	}

	/***/

	public void deseleccionarFila(UnselectEvent event) {
		setCategoriaModificada((CategoriaMedicamentoPortafolioSedeDto) event
				.getObject());
	}

	public void seleccionarFila(SelectEvent event) {
		try {
			categoriaModificada = (CategoriaMedicamentoPortafolioSedeDto) event
					.getObject();

			categoriaModificada.setHabilitado(Boolean.TRUE);
			medicamentoPortafolioFacade
					.cambiarEstadoCategoriaMedicamentoPortafolio(categoriaModificada);
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Se ha presentado un error actualizando la categoria.");
			logger.error(
					"Se ha presentado un error actualizando la categoria.", e);
		}
	}

	public void toggleSelect(ToggleSelectEvent event) {
		estadoSeleccionMasiva = event.isSelected();
	}

	public void confirmacionDeseleccion(Boolean confirmacion) {
		if (Boolean.FALSE.equals(confirmacion)) {
			categoriaModificada.setHabilitado(true);
			categoriasMedicamentoSeleccion.add(categoriaModificada);
		} else {
			try {
				categoriaModificada.setHabilitado(Boolean.FALSE);
				medicamentoPortafolioFacade
						.cambiarEstadoCategoriaMedicamentoPortafolio(categoriaModificada);
			} catch (ConexiaBusinessException | ConexiaSystemException e) {
				facesMessagesUtils
						.addError("Se ha presentado un error al cargar la informaci\u00f3n.");
			}
		}
	}

	public void confirmacionSeleccionMasiva() {
		try {
			filters.put("nuevoEstado", estadoSeleccionMasiva);
			
			medicamentoPortafolioFacade
					.cambiarEstadoTodasCategoriasMedicamentoPortafolio(
							contenedorTecnologiasController.getPortafolio(),
							filters);

			facesMessagesUtils.addInfo("Categorias medicamentos modificadas con \u00E9xito.");
			
		} catch (ConexiaBusinessException | ConexiaSystemException e) {
			facesMessagesUtils
					.addError("Se ha presentado un error al cargar la informaci\u00f3n.");
		}
	}

	
	@Override
	public CategoriaMedicamentoPortafolioSedeDto getRowData(String rowKey) {
		Optional<CategoriaMedicamentoPortafolioSedeDto> optionalResult = categoriasMedicamento
				.stream()
				.filter(s -> Integer.valueOf(rowKey).equals(s.getId()))
				.findFirst();

		return optionalResult.orElse(null);
	}
}
