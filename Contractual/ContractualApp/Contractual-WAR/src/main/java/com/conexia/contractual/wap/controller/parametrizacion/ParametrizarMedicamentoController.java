package com.conexia.contractual.wap.controller.parametrizacion;

import com.conexia.contratacion.commons.constants.enums.GestionTecnologiasParametrizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contratacion.commons.constants.enums.OpcionesParametrizacionEnum;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.wap.facade.parametrizacion.ParametrizacionFacade;
import com.conexia.contractual.wap.facade.parametros.ParametrosFacade;
import com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroMedicamentoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionMedicamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.omnifaces.util.Ajax;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 *
 * @author Andrés Mise Olivera
 */
@Named
@ViewScoped
@URLMapping(id = "parametrizarMedicamento", pattern = "/parametrizacion/parametrizarMedicamento", viewId = "/parametrizarMedicamento.page")
public class ParametrizarMedicamentoController extends LazyDataModel<MedicamentosDto> {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private List<CategoriaMedicamentoDto> categoriasMedicamento;

	private int currentLevel = 1;

	/**
	 * Dto que almacena los filtros de la ventana.
	 */
	private FiltroMedicamentoDto filtroMedicamento;

	private List<MedicamentosDto> medicamentos;

	private List<SedeNegociacionMedicamentoDto> medicamentosPorParametrizar;

	/**
	 * Facade de parametrizacion.
	 */
	@Inject
	private ParametrizacionFacade parametrizacionFacade;

	 @Inject
	 @UserInfo
	 private UserApp user;

	/**
	 * Facade de parametros.
	 */
	@Inject
	private ParametrosFacade parametrosFacade;

	@Inject
	private FacesMessagesUtils facesMessagesUtils;

	@Inject
	private ConexiaExceptionUtils exceptionUtils;

	@Inject
	@CnxI18n
	transient ResourceBundle resourceBundle;

	private SedeNegociacionMedicamentoDto sedeNegociacionMedicamentoDto;

	private GestionTecnologiasParametrizacionEnum gestionSeleccionada;

	private List<SedeNegociacionMedicamentoDto> medicamentosParametrizarSeleccionados;

	private SedeNegociacionMedicamentoDto grupoMedicamentoSelecionado;

	private MedicamentosDto medicamentoSeleccionado;

	private List<MedicamentosDto> medicamentosSeleccionados;

	private List<OpcionesParametrizacionEnum> opcionesParametrizacion;

	private OpcionesParametrizacionEnum opcionAmbulatorio;

	private OpcionesParametrizacionEnum opcionHospitalario;

	private OpcionesParametrizacionEnum opcionAmbulatorioMx;

	private OpcionesParametrizacionEnum opcionHospitalarioMx;

	private String estadoParametrizacion;

	/**
	 * Cargue de datos del bean.
	 */
	@PostConstruct
	public void onload() {
		this.categoriasMedicamento = this.parametrosFacade.listarCategoriasMedicamento();
		this.setOpcionesParametrizacion(Arrays.asList(OpcionesParametrizacionEnum.values()));
		FacesContext facesContext = FacesContext.getCurrentInstance();
	    HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
	    estadoParametrizacion = (String)  session.getAttribute(NegociacionSessionEnum.ESTADO_PARAMETRIZACION.toString());
	}

	public void asociar() {
		try {
			if ((!this.medicamentosParametrizarSeleccionados.isEmpty())
					&& (this.medicamentosParametrizarSeleccionados != null)
					&& (this.medicamentosParametrizarSeleccionados.size() != 0)) {
				if(Objects.isNull(opcionAmbulatorio) && Objects.isNull(opcionHospitalario)){
        			FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR,"Por favor selecione una opción de parametrización",""));
        		}
				else{
					for(SedeNegociacionMedicamentoDto sm : medicamentosParametrizarSeleccionados){
						if (Objects.nonNull(opcionAmbulatorio)) {
							sm.setRequiereAutorizacionAmbulatorio(opcionAmbulatorio.toString());
						}
						if (Objects.nonNull(opcionHospitalario)) {
							sm.setRequiereAutorizacionHospitalario(opcionHospitalario.toString());
						}
						this.parametrizacionFacade.parametrizarCategoriaMedicamentos(sm, user.getId());
					}
					FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
							"Las categorias de medicamentos se ha parametrizado exitosamente", ""));
				}
			}
			else{
				FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR,"Por favor selecione las categorias ha parametrizar",""));
			}
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ha ocurrido un error al guardar", ""));
		}
	}

	public void asociarPorMedicamento() {
		try {
			for(MedicamentosDto mx : medicamentosSeleccionados){
				if (Objects.nonNull(opcionAmbulatorioMx)) {
					mx.setRequiereAutorizacionAmbulatorio(opcionAmbulatorioMx.toString());
				}
				if (Objects.nonNull(opcionHospitalarioMx)) {
					mx.setRequiereAutorizacionHospitalario(opcionHospitalarioMx.toString());
				}
				this.parametrizacionFacade.parametrizarMedicamentos(sedeNegociacionMedicamentoDto, mx, user.getId());
			}
			Ajax.oncomplete("PF('tblMedicamentosW').unselectAllRows();");
			Ajax.update("formMedicamentos:gestionMxParam");
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage("Los medicamentos se han parametrizado exitosamente."));
		} catch (Exception e) {
			FacesContext.getCurrentInstance().addMessage(null,
					new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ha ocurrido un error al guardar", ""));
		}
	}

	public void asociarMedicamentoIndividualAmbulatorio(ValueChangeEvent event) {
		MedicamentosDto medicamentoSeleccionado = (MedicamentosDto)event.getComponent().getAttributes().get("medicamento");
		grupoMedicamentoSelecionado = sedeNegociacionMedicamentoDto;
		Object opcParam  = event.getNewValue();
		String mensaje = null;
		if(!opcParam.equals(medicamentoSeleccionado.getRequiereAutorizacionAmbulatorio())){
			medicamentoSeleccionado.setRequiereAutorizacionAmbulatorio(opcParam.toString());
			mensaje = "REQUIERE AUTORIZACIÓN AMBULATORIO - "+ opcParam.toString() +"";
		}
		this.parametrizacionFacade.parametrizarMedicamentos(grupoMedicamentoSelecionado, medicamentoSeleccionado,user.getId());
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage("La medicamento se ha parametrizado exitosamente como: " + mensaje +""));
	}

	public void asociarMedicamentoIndividualHospitalario(ValueChangeEvent event) {
		MedicamentosDto medicamentoSeleccionado = (MedicamentosDto)event.getComponent().getAttributes().get("medicamento");
		grupoMedicamentoSelecionado = sedeNegociacionMedicamentoDto;
		Object opcParam  = event.getNewValue();
		String mensaje = null;
		if(!opcParam.equals(medicamentoSeleccionado.getRequiereAutorizacionHospitalario())){
			medicamentoSeleccionado.setRequiereAutorizacionHospitalario(opcParam.toString());
			mensaje = "REQUIERE AUTORIZACIÓN HOSPITALARIO - "+ opcParam.toString() +"";
		}
		this.parametrizacionFacade.parametrizarMedicamentos(grupoMedicamentoSelecionado, medicamentoSeleccionado,user.getId());
		FacesContext.getCurrentInstance().addMessage(null,
				new FacesMessage("La medicamento se ha parametrizado exitosamente como: " + mensaje +""));
	}


	public void gestionarMedicamentos(String nombreTabla) {
		if (this.gestionSeleccionada.equals(GestionTecnologiasParametrizacionEnum.SELECCIONAR_TODOS)) {
			Ajax.oncomplete("PF('" + nombreTabla + "').selectAllRows();");
		} else if (this.gestionSeleccionada.equals(GestionTecnologiasParametrizacionEnum.DESELECCIONAR_TODOS)) {
			Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRows();");
			Ajax.update("formMedicamentos:gestionMedicamentosParam");
		}
	}

	@Override
	public List<MedicamentosDto> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		if (sedeNegociacionMedicamentoDto != null) {
			try {
				sedeNegociacionMedicamentoDto.setPagina(first);
				sedeNegociacionMedicamentoDto.setCantidadRegistros(pageSize);
				sedeNegociacionMedicamentoDto.setAscendente(sortOrder == SortOrder.ASCENDING);
				sedeNegociacionMedicamentoDto.setFiltros(filters);
				this.setRowCount(
						this.parametrizacionFacade.contarMedicamentosPorParametrizar(sedeNegociacionMedicamentoDto));
				this.medicamentos = this.parametrizacionFacade
						.listarMedicamentosPorParametrizar(sedeNegociacionMedicamentoDto);
			} catch (final Exception e) {
				facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
			}
		}
		return this.medicamentos;
	}

	public void detalleCategoriaMedicamento(SedeNegociacionMedicamentoDto sedeNegociacionMedicamento){
		this.medicamentos =  new ArrayList<MedicamentosDto>();
		this.medicamentos = this.parametrizacionFacade.listarMedicamentosPorParametrizar(sedeNegociacionMedicamento);
	}

	@SuppressWarnings("unchecked")
	@Override
	public MedicamentosDto getRowData(String strRowKey) {
		if (Objects.nonNull(strRowKey)) {
			Long rowKey = strRowKey != null && Long.valueOf(strRowKey) > 0 ? Long.valueOf(strRowKey) : new Long(0);
			for (MedicamentosDto MedicamentosDto : (List<MedicamentosDto>) getWrappedData()) {
				if (MedicamentosDto.getId().compareTo(rowKey) == 0) {
					return MedicamentosDto;
				}
			}
		}
		return null;
	}

	public List<CategoriaMedicamentoDto> getCategoriasMedicamento() {
		return categoriasMedicamento;
	}

	public void setCategoriasMedicamento(List<CategoriaMedicamentoDto> categoriasMedicamento) {
		this.categoriasMedicamento = categoriasMedicamento;
	}

	public int getCurrentLevel() {
		return currentLevel;
	}

	public void setCurrentLevel(int currentLevel) {
		this.currentLevel = currentLevel;
	}

	public FiltroMedicamentoDto getFiltroMedicamento() {
		return filtroMedicamento;
	}

	public void setFiltroMedicamento(FiltroMedicamentoDto filtroMedicamento) {
		this.filtroMedicamento = filtroMedicamento;
	}

	public List<SedeNegociacionMedicamentoDto> getMedicamentosPorParametrizar() {
		medicamentosPorParametrizar = this.parametrizacionFacade.listarMedicamentosPorParametrizar(filtroMedicamento);
		return medicamentosPorParametrizar;
	}

	public List<MedicamentosDto> getMedicamentos() {
		if (sedeNegociacionMedicamentoDto != null) {
			this.medicamentos = this.parametrizacionFacade
					.listarMedicamentosPorParametrizar(sedeNegociacionMedicamentoDto);
			return this.medicamentos;
		} else {
			return null;
		}

	}

	/**
	 * Cuenta los grupos de medicamento por parametrizar.
	 *
	 * @return conteo.
	 */
	public int contarGrupoMedicamentoPorParametrizar() {
		return this.parametrizacionFacade.contarGrupoMedicamentoPorParametrizar(filtroMedicamento);
	}

	public void setMedicamentos(List<SedeNegociacionMedicamentoDto> medicamentosPorParametrizar) {
		this.medicamentosPorParametrizar = medicamentosPorParametrizar;
	}

	public SedeNegociacionMedicamentoDto getSedeNegociacionMedicamentoDto() {
		return sedeNegociacionMedicamentoDto;
	}

	public void setSedeNegociacionMedicamentoDto(SedeNegociacionMedicamentoDto sedeNegociacionMedicamentoDto) {
		this.sedeNegociacionMedicamentoDto = sedeNegociacionMedicamentoDto;
	}

	/**
	 * @return the totalMedicamentosPorParametrizar
	 */
	public int getTotalTecnologiasPorParametrizar() {
		return this.parametrizacionFacade.validarMedicamentosPorParametrizar(this.filtroMedicamento);
	}

	/**
	 * @param totalMedicamentosPorParametrizar
	 *            the totalMedicamentosPorParametrizar to set
	 */
	public void setTotalTecnologiasPorParametrizar(int totalTecnologiasPorParametrizar) {
	}

	public GestionTecnologiasParametrizacionEnum getGestionSeleccionada() {
		return gestionSeleccionada;
	}

	public void setGestionSeleccionada(GestionTecnologiasParametrizacionEnum gestionSeleccionada) {
		this.gestionSeleccionada = gestionSeleccionada;
	}

	public GestionTecnologiasParametrizacionEnum[] getGestionTecnologiasParametrizacion() {
		return GestionTecnologiasParametrizacionEnum.values();
	}

	public List<SedeNegociacionMedicamentoDto> getMedicamentosParametrizarSeleccionados() {
		return medicamentosParametrizarSeleccionados;
	}

	public void setMedicamentosParametrizarSeleccionados(
			List<SedeNegociacionMedicamentoDto> medicamentosParametrizarSeleccionados) {
		this.medicamentosParametrizarSeleccionados = medicamentosParametrizarSeleccionados;
	}

	public SedeNegociacionMedicamentoDto getGrupoMedicamentoSelecionado() {
		return grupoMedicamentoSelecionado;
	}

	public void setGrupoMedicamentoSelecionado(SedeNegociacionMedicamentoDto grupoMedicamentoSelecionado) {
		this.grupoMedicamentoSelecionado = grupoMedicamentoSelecionado;
	}

	public List<MedicamentosDto> getMedicamentosSeleccionados() {
		return medicamentosSeleccionados;
	}

	public void setMedicamentosSeleccionados(List<MedicamentosDto> medicamentosSeleccionados) {
		this.medicamentosSeleccionados = medicamentosSeleccionados;
	}

	public MedicamentosDto getMedicamentoSeleccionado() {
		return medicamentoSeleccionado;
	}

	public void setMedicamentoSeleccionado(MedicamentosDto medicamentoSeleccionado) {
		this.medicamentoSeleccionado = medicamentoSeleccionado;
	}

	public List<OpcionesParametrizacionEnum> getOpcionesParametrizacion() {
		return opcionesParametrizacion;
	}

	public void setOpcionesParametrizacion(List<OpcionesParametrizacionEnum> opcionesParametrizacion) {
		this.opcionesParametrizacion = opcionesParametrizacion;
	}

	public OpcionesParametrizacionEnum getOpcionAmbulatorio() {
		return opcionAmbulatorio;
	}

	public void setOpcionAmbulatorio(OpcionesParametrizacionEnum opcionAmbulatorio) {
		this.opcionAmbulatorio = opcionAmbulatorio;
	}

	public OpcionesParametrizacionEnum getOpcionHospitalario() {
		return opcionHospitalario;
	}

	public void setOpcionHospitalario(OpcionesParametrizacionEnum opcionHospitalario) {
		this.opcionHospitalario = opcionHospitalario;
	}

	public OpcionesParametrizacionEnum getOpcionAmbulatorioMx() {
		return opcionAmbulatorioMx;
	}

	public void setOpcionAmbulatorioMx(OpcionesParametrizacionEnum opcionAmbulatorioMx) {
		this.opcionAmbulatorioMx = opcionAmbulatorioMx;
	}

	public OpcionesParametrizacionEnum getOpcionHospitalarioMx() {
		return opcionHospitalarioMx;
	}

	public void setOpcionHospitalarioMx(OpcionesParametrizacionEnum opcionHospitalarioMx) {
		this.opcionHospitalarioMx = opcionHospitalarioMx;
	}

	public String getEstadoParametrizacion() {
		return estadoParametrizacion;
	}

	public void setEstadoParametrizacion(String estadoParametrizacion) {
		this.estadoParametrizacion = estadoParametrizacion;
	}


}
