package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionPaqueteDetalleSSFacade;
import com.conexia.contratacion.commons.constants.CommonMaps;
import com.conexia.contratacion.commons.constants.enums.ArchivosNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.DescriptivoDto;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.*;
import com.conexia.contratacion.commons.dto.maestros.*;
import com.conexia.contratacion.commons.dto.negociacion.AnexosIngresadosDto;
import com.conexia.contratacion.commons.dto.negociacion.ErroresImportTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.TecnologiasIngresadasDto;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.FlashScopeUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.omnifaces.util.Ajax;
import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller de el paquete detalle
 *
 * @author jjoya
 *
 */
@Named
@ViewScoped
@URLMapping(id = "negociacionPaqueteDetalleSS", pattern = "/paquete/detalle", viewId = "/negociacion/modalidad/sedeasede/paquetes/detalle/detalleNegociacionPaquete.page")
public class NegociacionPaqueteDetalleSSController implements Serializable {

	@Inject
	private NegociacionPaqueteDetalleSSFacade detallePaquetefacade;
	@Inject
	private ValidadorImportTecnologiasPaqueteDetalleSS validator;

	@Inject
	private FacesUtils facesUtils;
	@Inject
	private FlashScopeUtils flashScopeUtils;
	@Inject
	private Log logger;
	@Inject
	private FacesMessagesUtils facesMessagesUtils;
	@Inject
	@CnxI18n
	transient ResourceBundle resourceBundle;
	@Inject
	private PreContractualExceptionUtils exceptionUtils;

	private Long paqueteId;
	private Long negociacionId;
	private Long prestadorId;
	private PaquetePortafolioDto paquete;
	private List<SedeNegociacionPaqueteObservacionDto> observacionPaquete;
	private List<SedeNegociacionPaqueteExclusionDto> exclusionPaquete;
	private List<SedeNegociacionPaqueteCausaRupturaDto> cauraRupturaPaquete;
	private List<SedeNegociacionPaqueteRequerimientoDto> requerimientoPaquete;
	private List<SedeNegociacionPaqueteProcedimientoDto> paqueteProcedimientoNegociacion;
	private List<SedeNegociacionPaqueteMedicamentoDto> paqueteMedicamentoNegociacion;
	private List<SedeNegociacionPaqueteInsumoDto> paqueteInsumoNegociacion;
	private List<SedeNegociacionPaqueteProcedimientoDto> procedimientosPaquetePrestador;
	private List<SedeNegociacionPaqueteMedicamentoDto> medicamentosPaquetePrestador;
	private List<SedeNegociacionPaqueteInsumoDto> insumosPaquetesPrestador;
	private MedicamentosDto filtroMedicamentoDto;
	private InsumosDto filtroInsumoDto;
	private ProcedimientoDto filtroProcedimientoDto;
	private SedeNegociacionPaqueteProcedimientoDto procedimientoSeleccionado;
	private SedeNegociacionPaqueteMedicamentoDto medicamentoSeleccionado;
	private SedeNegociacionPaqueteInsumoDto insumoSeleccionado;
	private List<DescriptivoDto> opcionesIngresoAplica;
	private List<DescriptivoDto> opcionesFrecuenciaUnidad;
	private Integer opcionesTipoProcedimiento;
	private List<ProcedimientoDto> procedimientosSeleccionados;
	private List<MedicamentosDto> medicamentosSeleccionados;
	private List<InsumosDto> insumosSeleccionados;
	private String tipoProcedimiento;
	private String observacion;
	private String exclusion;
	private String causaRuptura;
	private String requerimientoTecnico;
	private SedeNegociacionPaqueteObservacionDto observacionSeleccionada;
	private SedeNegociacionPaqueteExclusionDto exclusionSeleccionada;
	private SedeNegociacionPaqueteCausaRupturaDto causaRupturaSeleccionada;
	private SedeNegociacionPaqueteRequerimientoDto requerimientoTecnicoSeleccionado;
	private List<ProcedimientoDto> procedimientosVentanaModal;
	private List<MedicamentosDto> medicamentosVentanaModal;
	private List<InsumosDto> insumosVentanaModal;
	private SedePrestadorDto SedePrestadorSeleccionada;
	private List<ErroresImportTecnologiasDto> listErrors;
	private List<ErroresImportTecnologiasDto> listErrorsAnexos;
	private List<TecnologiasIngresadasDto> tecnologiasIngresadas;
	private ArchivosNegociacionEnum typeImport;

	@PostConstruct
	public void postConstruct() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
		negociacionId = (Long) session.getAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString());
		paqueteId = (Long) session.getAttribute(NegociacionSessionEnum.PAQUETE_ID.toString());
		prestadorId = (Long) session.getAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString());
        if (Objects.isNull(SedePrestadorSeleccionada)) {
            SedePrestadorSeleccionada = flashScopeUtils.getParam("sedePrestador");
        }

		if (paqueteId != null && negociacionId != null) {
			consultarInformacionBasica();
			datosAdicionalesPaquetePrestador();
			listarMedicamentosPorPaqueteIdAndNegociacionId(negociacionId);
			listarProcedimientosPorPaqueteIdAndNegociacionId(negociacionId);
			listarInsumosPorPaqueteIdAndNegociacionId(negociacionId);

			procedimientosPaquetePrestador = detallePaquetefacade.listarProcedimientosPaquetePrestador(paquete.getCodigoPortafolio(), SedePrestadorSeleccionada, paqueteId, prestadorId, negociacionId);
			medicamentosPaquetePrestador = detallePaquetefacade.listarMedicamentosPaquetePrestador(paquete.getCodigoPortafolio(), paqueteId, prestadorId, negociacionId);
			insumosPaquetesPrestador = detallePaquetefacade.listarInsumosPaquetePrestador(paquete.getCodigoPortafolio(), paqueteId, prestadorId, negociacionId);

		} else {
			this.facesUtils.urlRedirect("/bandejaPrestador");
		}
        obtenerOpcionesIngreso();
        obtenerFrecuenciaUnidad();
        filtroProcedimientoDto = new ProcedimientoDto();
		filtroMedicamentoDto = new MedicamentosDto();
		filtroMedicamentoDto.setCategoriaMedicamento(new CategoriaMedicamentoDto());
		filtroInsumoDto = new InsumosDto();
		filtroInsumoDto.setGrupoInsumo(new GrupoInsumoDto());
		filtroInsumoDto.setCategoriaInsumo(new CategoriaInsumoDto());

	}

    private void obtenerFrecuenciaUnidad() {
        opcionesFrecuenciaUnidad = new ArrayList<>();
        opcionesFrecuenciaUnidad.add(new DescriptivoDto("SN", "SEGUN NECESIDAD"));
        opcionesFrecuenciaUnidad.add(new DescriptivoDto("INGR", "INGRESO"));
        opcionesFrecuenciaUnidad.add(new DescriptivoDto("DIA", "DIARIA"));
        opcionesFrecuenciaUnidad.add(new DescriptivoDto("SEMN", "SEMANAL"));
        opcionesFrecuenciaUnidad.add(new DescriptivoDto("MES", "MENSUAL"));
        opcionesFrecuenciaUnidad.add(new DescriptivoDto("BMES", "BIMENSUAL"));
        opcionesFrecuenciaUnidad.add(new DescriptivoDto("TMES", "TRIMESTRAL"));
        opcionesFrecuenciaUnidad.add(new DescriptivoDto("SMES", "SEMESTRAL"));
        opcionesFrecuenciaUnidad.add(new DescriptivoDto("ANL", "ANUAL"));
    }

    private void obtenerOpcionesIngreso() {
        opcionesIngresoAplica = new ArrayList<>();
        opcionesIngresoAplica.add(new DescriptivoDto("NA", "NO APLICA"));
        opcionesIngresoAplica.add(new DescriptivoDto("SI", "SI"));
        opcionesIngresoAplica.add(new DescriptivoDto("SN", "SI SEGUN NECESIDAD"));
    }

    public void consultarInformacionBasica() {
		try {
			paquete = detallePaquetefacade.consultarInformacionBasicaByPaqueteId(paqueteId);
			paquete.setOrigen(detallePaquetefacade.consultaOrigenPaqueteId(paqueteId));
			paquete.setServiciosSalud(detallePaquetefacade.consultaServiciosOrigenPaqueteId(paqueteId, negociacionId));
		} catch (Exception e) {
			this.logger.error("Error al consultar informacion basica del paquete", e);
			this.facesMessagesUtils.addError(this.exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}

	public void listarMedicamentosPorPaqueteIdAndNegociacionId(final Long negId) {
		try {
			if ((negId != null) && (paqueteId != null)) {
				paqueteMedicamentoNegociacion = detallePaquetefacade.listarMedicamentosPorPaqueteIdAndNegociacionId(paquete.getCodigoPortafolio(), negId, paqueteId);
			} else {
				this.facesUtils.urlRedirect("/bandejaPrestador");
			}
		} catch (Exception e) {
			this.logger.error("Error al listar los medicamentos del paquete con id" + paqueteId, e);
			this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}

	public void listarProcedimientosPorPaqueteIdAndNegociacionId(final Long negId) {
		try {
			if ((negId != null) && (paqueteId != null)) {
				paqueteProcedimientoNegociacion = detallePaquetefacade.listarProcedimientosPorPaqueteIdAndNegociacionId(SedePrestadorSeleccionada, negId, paqueteId);
			} else {
				this.facesUtils.urlRedirect("/bandejaPrestador");
			}
		} catch (Exception e) {
			this.logger.error("Error al listar los procedimientos del paquete con id" + paqueteId, e);
			this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}

	public void listarInsumosPorPaqueteIdAndNegociacionId(final Long negId) {
		try {
			if ((negId != null) && (paqueteId != null)) {
				paqueteInsumoNegociacion = detallePaquetefacade.listarInsumosPorPaqueteIdAndNegociacionId(paquete.getCodigoPortafolio(), negId, paqueteId);
			} else {
				this.facesUtils.urlRedirect("/bandejaPrestador");
			}
		} catch (Exception e) {
			this.logger.error("Error al listar los traslados del paquete con id" + paqueteId, e);
			this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}

	public void datosAdicionalesPaquetePrestador() {
		try {
			observacionPaquete = detallePaquetefacade.observacionPaquetePrestador(negociacionId, paqueteId);
			exclusionPaquete = detallePaquetefacade.exclusionPaquetePrestador(negociacionId, paqueteId);
			cauraRupturaPaquete = detallePaquetefacade.causaRupturatoPaquetePrestador(negociacionId, paqueteId);
			requerimientoPaquete = detallePaquetefacade.requerimientoPaquetePrestador(negociacionId, paqueteId);
		} catch (Exception e) {
			this.logger.error("Error al traer datos adicionales de paquetes" + paqueteId, e);
		}
	}

	public void actualizarObservacion(String observacion, Integer observacionPaqueteId) {
		try {
			this.detallePaquetefacade.actualizarObservacionSedeNegociacion(observacion, observacionPaqueteId);
		} catch (Exception e) {
			this.logger.error("Error al actualizar la observacion" + paqueteId, e);
		}
	}

	public void actualizarExclusion(String exclusion, Integer observacionPaqueteId) {
		try {
			this.detallePaquetefacade.actualizarExclusionSedeNegociacion(exclusion, observacionPaqueteId);
		} catch (Exception e) {
			this.logger.error("Error al actualizar la exclusión" + paqueteId, e);
		}
	}

	public void actualizarCausaRuptura(String causaRuptura, Integer causaPaqueteId) {
		try {
			this.detallePaquetefacade.actualizarCausaRupturaSedeNegociacion(causaRuptura, causaPaqueteId);
		} catch (Exception e) {
			this.logger.error("Error al actualizar la causa de ruptura" + paqueteId, e);
		}
	}

	public void actualizarRequerimientoTecnico(String requerimientoTecnico, Integer requerimientoPaqueteId) {
		try {
			this.detallePaquetefacade.actualizarRequerimientoTecnicoSedeNegociacion(requerimientoTecnico,
					requerimientoPaqueteId);
		} catch (Exception e) {
			this.logger.error("Error al actualizar el requerimiento técnico" + paqueteId, e);
		}
	}

	public void eliminarObservacion() {
		addMessage("Observación " + observacionSeleccionada.getId() + " eliminada");
		borrarObservacion(observacionSeleccionada.getId());
	}

	public void deleteAllProcedures() {
		this.detallePaquetefacade.deleteAllProcedures(negociacionId, paqueteId);
		facesMessagesUtils.addError(resourceBundle.getString("paquete_msj_confirm_eliminar_px"));
		paqueteProcedimientoNegociacion = detallePaquetefacade.listarProcedimientosPorPaqueteIdAndNegociacionId(SedePrestadorSeleccionada, negociacionId, paqueteId);
	}

	public void confirmDeletePx() {
		if (paqueteProcedimientoNegociacion.isEmpty() || Objects.isNull(paqueteProcedimientoNegociacion)) {
			facesMessagesUtils.addError(resourceBundle.getString("mesagge_no_existen_px"));
		} else {
			Ajax.oncomplete("PF('confirmDeleteAllPx').show();");
		}
	}

	public void deleteAllMedicamentos() {
		this.detallePaquetefacade.deleteAllMedicamentos(negociacionId, paqueteId);
		facesMessagesUtils.addError(resourceBundle.getString("paquete_msj_confirm_eliminar_med"));
		paqueteMedicamentoNegociacion = detallePaquetefacade.listarMedicamentosPorPaqueteIdAndNegociacionId(
				paquete.getCodigoPortafolio(), negociacionId, paqueteId);
	}

	public void confirmDeleteMed() {
		if (paqueteMedicamentoNegociacion.isEmpty() || Objects.isNull(paqueteMedicamentoNegociacion)) {
			facesMessagesUtils.addError(resourceBundle.getString("mesagge_no_existen_med"));
		} else {
			Ajax.oncomplete("PF('confirmDeleteAllMed').show();");
		}
	}

	public void deleteAllInsumos() {
		this.detallePaquetefacade.deleteAllInsumos(negociacionId, paqueteId);
		facesMessagesUtils.addError(resourceBundle.getString("paquete_msj_confirm_eliminar_ins"));
		paqueteInsumoNegociacion = detallePaquetefacade
				.listarInsumosPorPaqueteIdAndNegociacionId(paquete.getCodigoPortafolio(), negociacionId, paqueteId);
	}

	public void confirmDeleteIns() {
		if (paqueteInsumoNegociacion.isEmpty() || Objects.isNull(paqueteInsumoNegociacion)) {
			facesMessagesUtils.addError(resourceBundle.getString("mesagge_no_existen_ins"));
		} else {
			Ajax.oncomplete("PF('confirmDeleteAllIns').show();");
		}
	}

	public void eliminarProcedimiento(ActionEvent event) {
		Object procedimientoObj = event.getComponent().getAttributes().get("procedimientoBoton");
		addMessage("Procedimiento eliminado");
		detallePaquetefacade.borrarProcedimiento(negociacionId, paqueteId, (SedeNegociacionPaqueteProcedimientoDto) procedimientoObj);
		paqueteProcedimientoNegociacion = detallePaquetefacade.listarProcedimientosPorPaqueteIdAndNegociacionId(SedePrestadorSeleccionada, negociacionId, paqueteId);
	}

	public void borrarObservacion(Integer observacionPaqueteId) {
		try {
			this.detallePaquetefacade.borrarObservacionesSedeNegociacion(observacionPaqueteId);
			observacionPaquete = detallePaquetefacade.observacionPaquetePrestador(negociacionId, paqueteId);
		} catch (Exception e) {
			this.logger.error("Error al borrar la observacion" + paqueteId, e);
		}
	}

	public void eliminarExclusion() {
		addMessage("Exclusión " + exclusionSeleccionada.getId() + " eliminada");
		borrarExclusion(exclusionSeleccionada.getId());
	}

	public void borrarExclusion(Integer exclusionPaqueteId) {
		try {
			this.detallePaquetefacade.borrarExclusionSedeNegociacion(exclusionPaqueteId);
			exclusionPaquete = detallePaquetefacade.exclusionPaquetePrestador(negociacionId, paqueteId);
		} catch (Exception e) {
			this.logger.error("Error al borrar la exclusión" + paqueteId, e);
		}
	}

	public void eliminarCausaRuptura() {
		addMessage("Causa Ruptura " + causaRupturaSeleccionada.getId() + " eliminada");
		borrarCausaRuptura(causaRupturaSeleccionada.getId());
	}

	public void borrarCausaRuptura(Integer causaPaqueteId) {
		try {
			this.detallePaquetefacade.borrarCausaRupturaSedeNegociacion(causaPaqueteId);
			cauraRupturaPaquete = detallePaquetefacade.causaRupturatoPaquetePrestador(negociacionId, paqueteId);
		} catch (Exception e) {
			this.logger.error("Error al borrar causa de ruptura" + paqueteId, e);
		}
	}

	public void eliminarRequerimientoTecnico() {
		addMessage("Requerimiento Tecnico " + requerimientoTecnicoSeleccionado.getId() + " eliminado");
		borrarRequerimientoTecnico(requerimientoTecnicoSeleccionado.getId());
	}

	public void borrarRequerimientoTecnico(Integer requerimientoPaqueteId) {
		try {
			this.detallePaquetefacade.borrarRequerimientoTecnicoSedeNegociacion(requerimientoPaqueteId);
			requerimientoPaquete = detallePaquetefacade.requerimientoPaquetePrestador(negociacionId, paqueteId);
		} catch (Exception e) {
			this.logger.error("Error al borrar el requerimiento técnico" + paqueteId, e);
		}
	}

	public void buscarMedicamentos() {
		try {
			if ((filtroMedicamentoDto.getCums().isEmpty()) && (filtroMedicamentoDto.getAtc().isEmpty())
					&& (filtroMedicamentoDto.getPrincipioActivo().isEmpty())
					&& (filtroMedicamentoDto.getCategoriaMedicamento().getId() == null)) {
				facesMessagesUtils.addError("Seleccione por lo menos un filtro de busqueda");
			} else {
				List<String> codigosNoPermitidos = medicamentosPaquetePrestador.stream()
						.map(mpp -> mpp.getMedicamento().getCums()).collect(Collectors.toList());
				codigosNoPermitidos.addAll(paqueteMedicamentoNegociacion.stream()
						.map(mpp -> mpp.getMedicamento().getCums()).collect(Collectors.toList()));
				medicamentosVentanaModal = detallePaquetefacade.listarMedicamentos(filtroMedicamentoDto,
						codigosNoPermitidos);
				Ajax.oncomplete("PF('agregarMedicamentosDlg').show();");
			}
		} catch (Exception e) {
			this.logger.error("Error al buscar medicamentos" + e, e);
		}
	}

	public void buscarProcedimientos() {
		try {
			filtroProcedimientoDto.setTipoProcedimiento(opcionesTipoProcedimiento);
			if ((filtroProcedimientoDto.getCodigoCliente().isEmpty())
					&& (filtroProcedimientoDto.getDescripcion().isEmpty())
					&& (filtroProcedimientoDto.getTipoProcedimiento()).intValue() == 0) {
				facesMessagesUtils.addError("Seleccione por lo menos un filtro de busqueda");
			} else {
				List<String> codigosNoPermitidos = procedimientosPaquetePrestador.stream()
						.map(ppp -> ppp.getProcedimiento().getCodigoCliente()).collect(Collectors.toList());
				codigosNoPermitidos.addAll(paqueteProcedimientoNegociacion.stream()
						.map(ppp -> ppp.getProcedimiento().getCodigoCliente()).collect(Collectors.toList()));
				procedimientosVentanaModal = detallePaquetefacade.listarProcedimientos(filtroProcedimientoDto,
						codigosNoPermitidos);
				Ajax.oncomplete("PF('agregarProcedimientosDlg').show()");
			}

		} catch (Exception e) {
			this.logger.error("Error al buscar procedimientos" + e, e);
		}
	}

	public void buscarInsumos() {
		try {
			if ((filtroInsumoDto.getCodigo().isEmpty()) && (filtroInsumoDto.getDescripcion().isEmpty())) {
				facesMessagesUtils.addError("Seleccione por lo menos un filtro de busqueda");
			} else {
				List<String> codigosNoPermitidos = insumosPaquetesPrestador.stream()
						.map(ppp -> ppp.getInsumo().getCodigo()).collect(Collectors.toList());
				codigosNoPermitidos.addAll(paqueteInsumoNegociacion.stream().map(ppp -> ppp.getInsumo().getCodigo())
						.collect(Collectors.toList()));
				insumosVentanaModal = detallePaquetefacade.listarInsumos(filtroInsumoDto, codigosNoPermitidos);
				Ajax.oncomplete("PF('agregarInsumosDlg').show();");
			}
		} catch (Exception e) {
			this.logger.error("Error al buscar insumos" + e, e);
		}
	}

	public void gestionarProcedimientoPaqueteBasico(AjaxBehaviorEvent event) {
		try {
			procedimientoSeleccionado = (SedeNegociacionPaqueteProcedimientoDto) event.getComponent().getAttributes()
					.get("procedimiento");
			if (((SelectBooleanCheckbox) event.getSource()).isSelected()) {
				this.detallePaquetefacade.borrarProcedimiento(negociacionId, paqueteId, procedimientoSeleccionado);
				addMessage("El procedimiento del paquete basico, ha salido del detalle del paquete en la negociación");
			} else {
				this.detallePaquetefacade.insertarProcedimientoDetallePrestador(negociacionId, paqueteId, procedimientoSeleccionado);
				addMessage("El procedimiento del paquete basico, ha sido agregado al detalle del paquete");
			}
			procedimientosPaquetePrestador = detallePaquetefacade.listarProcedimientosPaquetePrestador(
					paquete.getCodigoPortafolio(), SedePrestadorSeleccionada, paqueteId, prestadorId, negociacionId);
		} catch (Exception e) {
			this.logger.error("Error al gestionar el procedimiento del paquete basico" + e, e);
		}
	}

	public void actualizarFilaProcedimiento(SedeNegociacionPaqueteProcedimientoDto procedimientoObj) {
		try {
			procedimientoSeleccionado = procedimientoObj;
			if (procedimientoSeleccionado.getCantidadMinima() == null
					|| procedimientoSeleccionado.getCantidadMaxima() == null) {
				addErrorMessage("Los campos cantidad minima y cantidad maxima son obligatorios");
				return;
			} else if (procedimientoSeleccionado.getCantidadMinima() > procedimientoSeleccionado.getCantidadMaxima()) {
				addErrorMessage("La cantidad mínima debe ser menor o igual a la máxima.");
				return;
			}
			this.detallePaquetefacade.actualizarProcedimiento(negociacionId, paqueteId, procedimientoSeleccionado);
			addMessage("Se guardan las modificaciones hechas al procedimiento seleccionado");
		} catch (Exception e) {
			this.logger.error("Error al actualizar el procedimiento del paquete basico" + e, e);
			addErrorMessage("Error al actualizar el procedimiento del paquete basico");
		}

	}

	public void gestionarMedicamentoPaqueteBasico(AjaxBehaviorEvent event) {
		try {
			medicamentoSeleccionado = (SedeNegociacionPaqueteMedicamentoDto) event.getComponent().getAttributes()
					.get("medicamento");
			if (((SelectBooleanCheckbox) event.getSource()).isSelected()) {
				this.detallePaquetefacade.borrarMedicamento(negociacionId, paqueteId, medicamentoSeleccionado);
				addMessage("El medicamento del paquete basico, ha salido del detalle del paquete en la negociación");
			} else {
				this.detallePaquetefacade.insertarMedicamentoDetallePrestador(negociacionId, paqueteId, medicamentoSeleccionado);
				addMessage("El medicamento del paquete basico, ha sido agregado al detalle del paquete");
			}
			medicamentosPaquetePrestador = detallePaquetefacade.listarMedicamentosPaquetePrestador(
					paquete.getCodigoPortafolio(), paqueteId, prestadorId, negociacionId);
		} catch (Exception e) {
			this.logger.error("Error al gestionar el medicamento del paquete basico" + e, e);
		}

	}

	public void actualizarFilaMedicamento(SedeNegociacionPaqueteMedicamentoDto medicamentoSeleccionado) {
		try {
			if (medicamentoSeleccionado.getCantidadMinima() == null
					|| medicamentoSeleccionado.getCantidadMaxima() == null) {
				addErrorMessage("Los campos cantidad minima y cantidad maxima son obligatorios");
				return;
			} else if (medicamentoSeleccionado.getCantidadMinima() > medicamentoSeleccionado.getCantidadMaxima()) {
				addErrorMessage("La cantidad mínima debe ser menor o igual a la máxima.");
				return;
			}
			this.detallePaquetefacade.actualizarMedicamento(negociacionId, paqueteId, medicamentoSeleccionado);
			addMessage("Se guardan las modificaciones hechas al medicamento seleccionado");
		} catch (Exception e) {
			this.logger.error("Error al actualizar el medicamento" + e, e);
		}
	}

	public void actualizarMedicamentoNegociacion(SedeNegociacionPaqueteMedicamentoDto medicamento) {
		try {
			if (medicamento.getCantidadMinima() == null || medicamento.getCantidadMaxima() == null) {
				addErrorMessage("Los campos cantidad minima y cantidad maxima son obligatorios");
				return;
			}
			if (medicamento.getCantidadMinima() > medicamento.getCantidadMaxima()) {
				addErrorMessage("La cantidad mínima debe ser menor o igual a la máxima.");
				return;
			}
			medicamentoSeleccionado = new SedeNegociacionPaqueteMedicamentoDto();
			medicamentoSeleccionado.setCantidadMinima(medicamento.getCantidadMinima());
			medicamentoSeleccionado.setCantidadMaxima(medicamento.getCantidadMaxima());
			medicamentoSeleccionado.setObservacion(medicamento.getObservacion());
			medicamentoSeleccionado.setIngresoAplica(medicamento.getIngresoAplica());
			medicamentoSeleccionado.setIngresoCantidad(medicamento.getIngresoCantidad());
			medicamentoSeleccionado.setFrecuenciaUnidad(medicamento.getFrecuenciaUnidad());
			medicamentoSeleccionado.setFrecuenciaCantidad(medicamento.getFrecuenciaCantidad());
			medicamentoSeleccionado.setMedicamento(medicamento.getMedicamento());
			this.detallePaquetefacade.actualizarMedicamento(negociacionId, paqueteId, medicamentoSeleccionado);
			addMessage("Se guardan las modificaciones hechas al medicamento seleccionado");
		} catch (Exception e) {
			this.logger.error("Error al actualizar el medicamento" + e, e);
		}
	}

	public void borrarMedicamento(ActionEvent event) {
		try {
			SedeNegociacionPaqueteMedicamentoDto medicamento = (SedeNegociacionPaqueteMedicamentoDto) event
					.getComponent().getAttributes().get("medicamentoBorrar");
			medicamentoSeleccionado = new SedeNegociacionPaqueteMedicamentoDto();
			medicamentoSeleccionado.setMedicamento(medicamento.getMedicamento());
			this.detallePaquetefacade.borrarMedicamento(negociacionId, paqueteId, medicamentoSeleccionado);
			addMessage("El medicamento seleccionado ha salido del detalle del paquete en la negociación");
			this.listarMedicamentosPorPaqueteIdAndNegociacionId(negociacionId);
			this.medicamentosPaquetePrestador = detallePaquetefacade.listarMedicamentosPaquetePrestador(
					paquete.getCodigoPortafolio(), paqueteId, prestadorId, negociacionId);
		} catch (Exception e) {
			this.logger.error("Error al borrar el medicamento" + e, e);
		}

	}

	public void gestionarInsumoPaqueteBasico(AjaxBehaviorEvent event) {
		try {
			insumoSeleccionado = (SedeNegociacionPaqueteInsumoDto) event.getComponent().getAttributes().get("insumo");
			if (((SelectBooleanCheckbox) event.getSource()).isSelected()) {
				this.detallePaquetefacade.borrarInsumo(negociacionId, paqueteId, insumoSeleccionado);
				addMessage("El insumo del paquete basico, ha salido del detalle del paquete en la negociación");
			} else {
				this.detallePaquetefacade.insertarInsumoDetallePrestador(negociacionId, paqueteId, insumoSeleccionado);
				addMessage("El insumo del paquete basico, ha sido agregado al detalle del paquete");
			}
			insumosPaquetesPrestador = detallePaquetefacade.listarInsumosPaquetePrestador(paquete.getCodigoPortafolio(),
					paqueteId, prestadorId, negociacionId);
		} catch (Exception e) {
			this.logger.error("Error al gestionar el insumo del paquete basico" + e, e);
		}
	}

	public void actualizarFilaInsumo(SedeNegociacionPaqueteInsumoDto insumoSeleccionado) {
		try {
			if (insumoSeleccionado.getCantidadMinima() == null || insumoSeleccionado.getCantidadMaxima() == null) {
				addErrorMessage("Los campos cantidad minima y cantidad maxima son obligatorios");
				return;
			} else if (insumoSeleccionado.getCantidadMinima() > insumoSeleccionado.getCantidadMaxima()) {
				addErrorMessage("La cantidad mínima debe ser menor o igual a la máxima.");
				return;
			}
			this.detallePaquetefacade.actualizarFilaInsumo(negociacionId, paqueteId, insumoSeleccionado);
			addMessage("Se guardan las modificaciones hechas al insumo seleccionado");
		} catch (Exception e) {
			this.logger.error("Error al actualizar el medicamento del paquete basico" + e, e);
		}
	}

	public void actualizarInsumoNegociacion(SedeNegociacionPaqueteInsumoDto insumo) {
		try {
			if (insumo.getCantidadMinima() == null || insumo.getCantidadMaxima() == null) {
				addErrorMessage("Los campos cantidad minima y cantidad maxima son obligatorios");
				return;
			} else if (insumo.getCantidadMinima() > insumo.getCantidadMaxima()) {
				addErrorMessage("La cantidad mínima debe ser menor o igual a la máxima.");
				return;
			}
			insumoSeleccionado = new SedeNegociacionPaqueteInsumoDto();
			insumoSeleccionado.setCantidadMinima(insumo.getCantidadMinima());
			insumoSeleccionado.setCantidadMaxima(insumo.getCantidadMaxima());
			insumoSeleccionado.setObservacion(insumo.getObservacion());
			insumoSeleccionado.setIngresoAplica(insumo.getIngresoAplica());
			insumoSeleccionado.setIngresoCantidad(insumo.getIngresoCantidad());
			insumoSeleccionado.setFrecuenciaUnidad(insumo.getFrecuenciaUnidad());
			insumoSeleccionado.setFrecuenciaCantidad(insumo.getFrecuenciaCantidad());
			insumoSeleccionado.setInsumo(insumo.getInsumo());
			this.detallePaquetefacade.actualizarFilaInsumo(negociacionId, paqueteId, insumoSeleccionado);
			addMessage("Se guardan las modificaciones hechas al insumo seleccionado");
		} catch (Exception e) {
			this.logger.error("Error al actualizar el medicamento del paquete basico" + e, e);
		}
	}

	public void borrarInsumo(ActionEvent event) {
		try {
			SedeNegociacionPaqueteInsumoDto insumo = (SedeNegociacionPaqueteInsumoDto) event.getComponent()
					.getAttributes().get("insumoBorrar");
			insumoSeleccionado = new SedeNegociacionPaqueteInsumoDto();
			insumoSeleccionado.setInsumo(insumo.getInsumo());
			this.detallePaquetefacade.borrarInsumo(negociacionId, paqueteId, insumoSeleccionado);
			addMessage("El insumo seleccionado, ha salido del detalle del paquete en la negociación");
			this.listarInsumosPorPaqueteIdAndNegociacionId(negociacionId);
			this.insumosPaquetesPrestador = detallePaquetefacade.listarInsumosPaquetePrestador(
					paquete.getCodigoPortafolio(), paqueteId, prestadorId, negociacionId);
		} catch (Exception e) {
			this.logger.error("Error al borrar insumo" + e, e);
		}
	}

    public void agregarObservacion() {
        try {
            if (Objects.nonNull(observacion) && !observacion.isEmpty()) {
                this.detallePaquetefacade.agregarObservacionSedeNegociacion(observacion, negociacionId, paqueteId);
                observacionPaquete = detallePaquetefacade.observacionPaquetePrestador(negociacionId, paqueteId);
                observacion = null;
            }
        } catch (Exception e) {
            this.logger.error("Error al agregar la observacion" + paqueteId, e);
        }
    }

    public void agregarExclusion() {
        try {
            if (Objects.nonNull(exclusion) && !exclusion.isEmpty()) {
                this.detallePaquetefacade.agregarExclusionSedeNegociacion(exclusion, negociacionId, paqueteId);
                exclusionPaquete = detallePaquetefacade.exclusionPaquetePrestador(negociacionId, paqueteId);
                exclusion = null;
            }
        } catch (Exception e) {
            this.logger.error("Error al agregar la exclusion" + paqueteId, e);
        }
    }

    public void agregarCausaDeRuptura() {
        try {
            if (Objects.nonNull(causaRuptura) && !causaRuptura.isEmpty()) {
                this.detallePaquetefacade.agregarCausaRupturaSedeNegociacion(causaRuptura, negociacionId, paqueteId);
                cauraRupturaPaquete = detallePaquetefacade.causaRupturatoPaquetePrestador(negociacionId, paqueteId);
                causaRuptura = null;
            }
        } catch (Exception e) {
            this.logger.error("Error al agregar la causa ruptura" + paqueteId, e);

        }
    }

    public void agregarRequerimientoTecnico() {
        try {
            if (Objects.nonNull(requerimientoTecnico) && !requerimientoTecnico.isEmpty()) {
                this.detallePaquetefacade.agregarRequerimientoTecnicoSedeNegociacion(requerimientoTecnico, negociacionId, paqueteId);
                requerimientoPaquete = detallePaquetefacade.requerimientoPaquetePrestador(negociacionId, paqueteId);
                requerimientoTecnico = null;
            }
        } catch (Exception e) {
            this.logger.error("Error al agregar el requerimiento tecnico" + paqueteId, e);
        }
    }

	public void addMessage(String summary) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, summary, null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void addErrorMessage(String summary) {
		FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, summary, null);
		FacesContext.getCurrentInstance().addMessage(null, message);
	}

	public void insertarMedicamentos() {
		try {
			if (medicamentosSeleccionados.size() == 0) {
				facesMessagesUtils.addError("Por favor seleccione el medicamento ha agregar");
			} else {
				String error = detallePaquetefacade.insertarMedicamentos(negociacionId, paqueteId, medicamentosSeleccionados, paquete.getCodigoPortafolio());
				if(!error.isEmpty()) {
					addErrorMessage("Los siguientes medicamentos no fueron insertados: ["+error+"]");
				}else {
					addMessage("Medicamentos insertados");
				}
				listarMedicamentosPorPaqueteIdAndNegociacionId(negociacionId);
				medicamentosPaquetePrestador = detallePaquetefacade.listarMedicamentosPaquetePrestador(
						paquete.getCodigoPortafolio(), paqueteId, prestadorId, negociacionId);
				medicamentosSeleccionados = new ArrayList<>();
				Ajax.oncomplete("PF('agregarMedicamentosDlg').hide()");
			}
		} catch (Exception e) {
			this.logger.error("Error al agregar medicamentos " + paqueteId, e);
		}
	}

	public void insertarInsumos() {
		try {
			if (insumosSeleccionados.size() == 0) {
				facesMessagesUtils.addError("Por favor seleccione el insumo ha agregar");
			} else {
				String error = detallePaquetefacade.insertarInsumos(negociacionId, paqueteId, insumosSeleccionados, paquete.getCodigoPortafolio());
				if(!error.isEmpty()) {
					addErrorMessage("Los siguientes insumos no fueron insertados: ["+error+"]");
				}else {
					addMessage("insumos insertados");
				}
				listarInsumosPorPaqueteIdAndNegociacionId(negociacionId);
				insumosPaquetesPrestador = detallePaquetefacade.listarInsumosPaquetePrestador(paquete.getCodigoPortafolio(), paqueteId, prestadorId, negociacionId);
				insumosSeleccionados = new ArrayList<>();
				Ajax.oncomplete("PF('agregarInsumosDlg').hide()");
			}
		} catch (Exception e) {
			this.logger.error("Error al agregar insumos " + paqueteId, e);
		}
	}

	public void insertarProcedimientos(List<ProcedimientoDto> procedimientos) {
		try {
			if (procedimientos.isEmpty()) {
				facesMessagesUtils.addError("Por favor seleccione el procedimiento ha agregar");
			} else {
				String error = detallePaquetefacade.insertarProcedimientos(negociacionId, paqueteId, procedimientosSeleccionados, paquete.getCodigoPortafolio());
				if(!error.isEmpty()) {
					addErrorMessage("Los siguientes procedimientos no fueron insertados: ["+error+"]");
				}else {
					addMessage("Procedimientos insertados");
				}
				listarProcedimientosPorPaqueteIdAndNegociacionId(negociacionId);
				procedimientosPaquetePrestador = detallePaquetefacade.listarProcedimientosPaquetePrestador(paquete.getCodigoPortafolio(), SedePrestadorSeleccionada, paqueteId, prestadorId, negociacionId);
				procedimientosSeleccionados = new ArrayList<>();
				Ajax.oncomplete("PF('agregarProcedimientosDlg').hide()");
				Ajax.update("formTecnologiasTabs:tecnologiaTabs:procedimientosPaqueteDlgTabla formTecnologiasTabs:tecnologiaTabs:procedimientosPaquete formTecnologiasTabs:tecnologiaTabs:procedimientosPaqueteIps");

			}
		} catch (Exception e) {
			this.logger.error("Error al agregar procedimientos " + paqueteId, e);
		}
	}

    public void importFiles(FileUploadEvent event) {
        typeImport = (ArchivosNegociacionEnum) event.getComponent().getAttributes().get("foo");
        try {
            File file = File.createTempFile(event.getFile().getFileName(), ".xlsx");
            FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
            fos.write(event.getFile().getContents());
            fos.close();
            Workbook libro = WorkbookFactory.create(new FileInputStream(file));
            switch (typeImport) {
                case TECHNOLOGIES_FILE:
                    manageTechnologies(libro, typeImport);
                    break;
                case ANNEXES_FILE:
                    manageAnnexes(libro, typeImport);
                    break;
                default:
                    logger.error("Error al importar el archivo: Tipo de archivo no definido", new Exception("Tipo de archivo no definido"));
                    facesMessagesUtils.addError(resourceBundle.getString("form_label_error_archivo"));
                    break;
            }
        } catch (IOException | ConexiaSystemException e) {
            logger.error("Error general al importar el archivo", e);
            facesMessagesUtils.addError(resourceBundle.getString("form_label_error_archivo"));
        }
    }

	private void manageTechnologies(Workbook libro, ArchivosNegociacionEnum typeImport) {
		List<TecnologiasIngresadasDto> listTecnologiasIngresadas = new ArrayList<>();
        Sheet hoja = libro.getSheetAt(libro.getActiveSheetIndex());
        List<ErroresImportTecnologiasDto> listErrorFormat = validator.validateFormat(hoja, typeImport);
		if (listErrorFormat.isEmpty()) {
		    hoja.forEach(cells -> {
                if (!Objects.equals(cells.getRowNum(), 0)) {
                    ConvertidorFilaPoi convertidorFilaPoi = new ConvertidorFilaPoi();
                    listTecnologiasIngresadas.add(convertidorFilaPoi.convertirFilaATecnologiaDto(cells));
                }
            });
            if (listTecnologiasIngresadas.isEmpty()) {
                facesMessagesUtils.addInfo("El archivo se encuentra vacio");
            } else {
                validationsImportTechnologies(listTecnologiasIngresadas);
            }
        } else {
			facesMessagesUtils.addInfo("El archivo no cumple con el formato, verifique que el archivo corresponda a cargue de tecnologías");
		}
	}

    private void manageAnnexes(Workbook libro, ArchivosNegociacionEnum typeImport) {
        List<AnexosIngresadosDto> listAnexosIngresados = new ArrayList<>();
        Sheet hoja = libro.getSheetAt(libro.getActiveSheetIndex());
        List<ErroresImportTecnologiasDto> listErrorFormat = validator.validateFormat(hoja, typeImport);
		if (listErrorFormat.isEmpty()) {
		    hoja.forEach(cells -> {
                if (!Objects.equals(cells.getRowNum(), 0)) {
                    ConvertidorFilaPoi convertidorFilaPoi = new ConvertidorFilaPoi();
                    listAnexosIngresados.add(convertidorFilaPoi.convertirFilaAAnexoDto(cells));
                }
            });
			if (listAnexosIngresados.isEmpty()) {
				facesMessagesUtils.addInfo("El archivo se encuentra vacio");
			} else {
				agregarAnexosImportados(listAnexosIngresados);
			}
		} else {
			facesMessagesUtils.addInfo("El archivo no cumple con el formato, verifique que el archivo corresponda a cargue de anexos");
		}
	}

	private void agregarAnexosImportados(List<AnexosIngresadosDto> anexosIngresados) {
		RequestContext context = RequestContext.getCurrentInstance();
		listErrorsAnexos = validator.validateAnexosStructures(anexosIngresados);
		if (listErrorsAnexos.isEmpty()) {
			anexosIngresados.forEach(an -> {
                switch (an.getTipoAnexo().toUpperCase()) {
                    case "OBSERVACION":
                        observacion = an.getDetalle();
                        agregarObservacion();
                        break;
                    case "EXCLUSION":
                        exclusion = an.getDetalle();
                        agregarExclusion();
                        break;
                    case "CAUSA":
                        causaRuptura = an.getDetalle();
                        agregarCausaDeRuptura();
                        break;
                    case "REQUERIMIENTO":
                        requerimientoTecnico = an.getDetalle();
                        agregarRequerimientoTecnico();
                        break;
                }
			});
			facesMessagesUtils.addInfo(resourceBundle.getString("form_label_archivo_importado_correctamente"));
			datosAdicionalesPaquetePrestador();
		} else {
			context.execute("PF('errorsImport').show();");
		}

	}

	private void validationsImportTechnologies(List<TecnologiasIngresadasDto> tecnologiasIngresadas) {
		CommonMaps.createMaps();
		listErrors = validator.validateStructure(tecnologiasIngresadas);
		RequestContext context = RequestContext.getCurrentInstance();
		// Errors Structure
		if (!listErrors.isEmpty()) {
			context.execute("PF('errorsImport').show();");
		} else {
			// Errors Coherence
			Object[] lists = validator.validateCoherence(tecnologiasIngresadas, negociacionId, paqueteId, prestadorId);
			listErrors = (List<ErroresImportTecnologiasDto>) lists[0];
			tecnologiasIngresadas = (List<TecnologiasIngresadasDto>) lists[1];

			if (!listErrors.isEmpty()) {
				context.execute("PF('errorsImport').show();");
			} else {
				manageTecnologies(tecnologiasIngresadas);
				postConstruct();
				facesMessagesUtils.addInfo(resourceBundle.getString("form_label_archivo_importado_correctamente"));
			}
		}
	}

	public void manageTecnologies(List<TecnologiasIngresadasDto> tecnologiasIngresadas) {
		for (TecnologiasIngresadasDto tec : tecnologiasIngresadas) {
            switch (tec.getTipoTecnologia()) {
                case 1:
                    manageMedicine(tec);
                    break;
                case 2:
                    manageProcedure(tec);
                    break;
                case 3:
                    manageInsumos(tec);
                    break;
            }
		}
	}

    private void manageProcedure(TecnologiasIngresadasDto tec) {
        List<ProcedimientoDto> listPxInsert = new ArrayList<>();
        List<ProcedimientoPaqueteDto> listPxPaq = new ArrayList<>();
        ProcedimientoDto procedimiento = new ProcedimientoDto();
        procedimiento.setId(tec.getIdTecnologia());
        ProcedimientoPaqueteDto procedimientoIngresado = new ProcedimientoPaqueteDto();
        procedimientoIngresado.setId(tec.getIdTecnologia());
        procedimientoIngresado.setCantidadMinima(Integer.parseInt(tec.getCantidadMinima()));
        procedimientoIngresado.setCantidadMaxima(Integer.parseInt(tec.getCantidadMaxima()));
        procedimientoIngresado.setObservacion(tec.getObservacion());
        if (tec.getIngresoPrograma() != null && !tec.getIngresoPrograma().isEmpty()) {
            procedimientoIngresado.setIngresoAplica(tec.getIngresoPrograma());
        } else {
            procedimientoIngresado.setIngresoAplica("NA");
        }
        if (tec.getIngresoCantidad() != null && !tec.getIngresoCantidad().isEmpty()) {
            procedimientoIngresado.setIngresoCantidad(Double.parseDouble(tec.getIngresoCantidad()));
        } else {
            procedimientoIngresado.setIngresoCantidad(null);
        }
        if (tec.getIngresoPrograma() != null && !tec.getIngresoPrograma().isEmpty()) {
            procedimientoIngresado.setFrecuenciaUnidad(tec.getFrecuenciaUnidad());
        } else {
            procedimientoIngresado.setFrecuenciaUnidad("NA");
        }
        if (tec.getFrecuenciaCantidad() != null && !tec.getFrecuenciaCantidad().isEmpty()) {
            procedimientoIngresado.setFrecuenciaCantidad(Double.parseDouble(tec.getFrecuenciaCantidad()));
        } else {
            procedimientoIngresado.setFrecuenciaCantidad(null);
        }
        procedimientoIngresado.setProcedimiento(procedimiento);

        Long pxIdInIps = paqueteProcedimientoNegociacion.stream()
                .filter(px -> px.getProcedimiento().getId().equals(tec.getIdTecnologia()))
                .findAny()
                .map(p -> p.getProcedimiento().getId())
                .orElse(null);

        if (pxIdInIps != null) {
            this.detallePaquetefacade.actualizarProcedimiento(negociacionId, paqueteId, procedimientoIngresado);
        } else {
            listPxInsert.add(procedimiento);
            listPxPaq.add(procedimientoIngresado);
        }
        if (!listPxInsert.isEmpty()) {
            this.detallePaquetefacade.insertarProcedimientos(negociacionId, paqueteId, listPxInsert, paquete.getCodigoPortafolio());
            listPxPaq.forEach(pxp -> this.detallePaquetefacade.actualizarProcedimiento(negociacionId, paqueteId, pxp));
        }
    }

	private void manageMedicine(TecnologiasIngresadasDto tec) {
        List<MedicamentosDto> listMedInsert = new ArrayList<>();
        List<SedeNegociacionPaqueteMedicamentoDto> listMedPaq = new ArrayList<>();
        MedicamentosDto medicamento = new MedicamentosDto();
        medicamento.setId(tec.getIdTecnologia());
        SedeNegociacionPaqueteMedicamentoDto medicamentoIngresado = new SedeNegociacionPaqueteMedicamentoDto();
        medicamentoIngresado.setId(tec.getIdTecnologia());
        medicamentoIngresado.setCantidadMinima(Integer.parseInt(tec.getCantidadMinima()));
        medicamentoIngresado.setCantidadMaxima(Integer.parseInt(tec.getCantidadMaxima()));
        medicamentoIngresado.setObservacion(tec.getObservacion());
        medicamentoIngresado.setIngresoAplica(tec.getIngresoPrograma());
        if (tec.getIngresoCantidad() != null && !tec.getIngresoCantidad().isEmpty()) {
            medicamentoIngresado.setIngresoCantidad(Double.parseDouble(tec.getIngresoCantidad()));
        }
        medicamentoIngresado.setFrecuenciaUnidad(tec.getFrecuenciaUnidad());
        if (tec.getFrecuenciaCantidad() != null && !tec.getFrecuenciaCantidad().isEmpty()) {
            medicamentoIngresado.setFrecuenciaCantidad(Double.parseDouble(tec.getFrecuenciaCantidad()));
        }
        medicamentoIngresado.setMedicamento(medicamento);

        // Portafolio
        Long medIdInIps = paqueteMedicamentoNegociacion.stream()
                .filter(mx -> mx.getMedicamento().getId().equals(tec.getIdTecnologia()))
                .findAny()
                .map(m -> m.getMedicamento().getId())
                .orElse(null);

        if (medIdInIps != null) {
            this.detallePaquetefacade.actualizarMedicamento(negociacionId, paqueteId, medicamentoIngresado);
        } else {
            listMedInsert.add(medicamento);
            listMedPaq.add(medicamentoIngresado);
        }
        if (!listMedInsert.isEmpty()) {
            this.detallePaquetefacade.insertarMedicamentos(negociacionId, paqueteId, listMedInsert, null);
            listMedPaq.forEach(medp -> this.detallePaquetefacade.actualizarMedicamento(negociacionId, paqueteId, medp));
        }
    }

	private void manageInsumos(TecnologiasIngresadasDto tec) {
        List<InsumosDto> listInsInsert = new ArrayList<>();
        List<SedeNegociacionPaqueteInsumoDto> listInsPaq = new ArrayList<>();
        InsumosDto insumo = new InsumosDto();
        insumo.setId(tec.getIdTecnologia());
        SedeNegociacionPaqueteInsumoDto insumoIngresado = new SedeNegociacionPaqueteInsumoDto();
        insumoIngresado.setId(tec.getIdTecnologia());
        insumoIngresado.setCantidadMinima(Integer.parseInt(tec.getCantidadMinima()));
        insumoIngresado.setCantidadMaxima(Integer.parseInt(tec.getCantidadMaxima()));
        insumoIngresado.setObservacion(tec.getObservacion());
        insumoIngresado.setIngresoAplica(tec.getIngresoPrograma());
        if (tec.getIngresoCantidad() != null && !tec.getIngresoCantidad().isEmpty()) {
            insumoIngresado.setIngresoCantidad(Double.parseDouble(tec.getIngresoCantidad()));
        }
        insumoIngresado.setFrecuenciaUnidad(tec.getFrecuenciaUnidad());
        if (tec.getFrecuenciaCantidad() != null && !tec.getFrecuenciaCantidad().isEmpty()) {
            insumoIngresado.setFrecuenciaCantidad(Double.parseDouble(tec.getFrecuenciaCantidad()));
        }
        insumoIngresado.setInsumo(insumo);
        // Portafolio
        Long insIdInIps = paqueteInsumoNegociacion.stream()
                .filter(ix -> ix.getInsumo().getId().equals(tec.getIdTecnologia()))
                .findAny()
                .map(i -> i.getInsumo().getId())
                .orElse(null);
        if (insIdInIps != null) {
            this.detallePaquetefacade.actualizarFilaInsumo(negociacionId, paqueteId, insumoIngresado);
        } else {
            listInsInsert.add(insumo);
            listInsPaq.add(insumoIngresado);
        }
        if (!listInsInsert.isEmpty()) {
            this.detallePaquetefacade.insertarInsumos(negociacionId, paqueteId, listInsInsert, null);
            listInsPaq.forEach(insp -> this.detallePaquetefacade.actualizarFilaInsumo(negociacionId, paqueteId, insp));
        }
    }

	public void setProcedimientosPaquetePrestador(List<SedeNegociacionPaqueteProcedimientoDto> procedimientosPaquetePrestador) {
		this.procedimientosPaquetePrestador = procedimientosPaquetePrestador;
	}

    //<editor-fold desc="Getters && Setters">
    public List<SedeNegociacionPaqueteMedicamentoDto> getMedicamentosPaquetePrestador() {
		return medicamentosPaquetePrestador;
	}

	public void setMedicamentosPaquetePrestador(List<SedeNegociacionPaqueteMedicamentoDto> medicamentosPaquetePrestador) {
		this.medicamentosPaquetePrestador = medicamentosPaquetePrestador;
	}

	public List<SedeNegociacionPaqueteInsumoDto> getInsumosPaquetesPrestador() {
		return insumosPaquetesPrestador;
	}

	public void setInsumosPaquetesPrestador(List<SedeNegociacionPaqueteInsumoDto> insumosPaquetesPrestador) {
		this.insumosPaquetesPrestador = insumosPaquetesPrestador;
	}

	public Long getPrestadorId() {
		return prestadorId;
	}

	public void setPrestadorId(Long prestadorId) {
		this.prestadorId = prestadorId;
	}

	public List<SedeNegociacionPaqueteObservacionDto> getObservacionPaquete() {
		return observacionPaquete;
	}

	public void setObservacionPaquete(List<SedeNegociacionPaqueteObservacionDto> observacionPaquete) {
		this.observacionPaquete = observacionPaquete;
	}

	public List<SedeNegociacionPaqueteExclusionDto> getExclusionPaquete() {
		return exclusionPaquete;
	}

	public void setExclusionPaquete(List<SedeNegociacionPaqueteExclusionDto> exclusionPaquete) {
		this.exclusionPaquete = exclusionPaquete;
	}

	public List<SedeNegociacionPaqueteCausaRupturaDto> getCauraRupturaPaquete() {
		return cauraRupturaPaquete;
	}

	public void setCauraRupturaPaquete(List<SedeNegociacionPaqueteCausaRupturaDto> cauraRupturaPaquete) {
		this.cauraRupturaPaquete = cauraRupturaPaquete;
	}

	public List<SedeNegociacionPaqueteRequerimientoDto> getRequerimientoPaquete() {
		return requerimientoPaquete;
	}

	public void setRequerimientoPaquete(List<SedeNegociacionPaqueteRequerimientoDto> requerimientoPaquete) {
		this.requerimientoPaquete = requerimientoPaquete;
	}

	public Long getNegociacionId() {
		return negociacionId;
	}

	public void setNegociacionId(Long negociacionId) {
		this.negociacionId = negociacionId;
	}

	public MedicamentosDto getFiltroMedicamentoDto() {
		return filtroMedicamentoDto;
	}

	public void setFiltroMedicamentoDto(MedicamentosDto filtroMedicamentoDto) {
		this.filtroMedicamentoDto = filtroMedicamentoDto;
	}

	public InsumosDto getFiltroInsumoDto() {
		return filtroInsumoDto;
	}

	public void setFiltroInsumoDto(InsumosDto filtroInsumoDto) {
		this.filtroInsumoDto = filtroInsumoDto;
	}

	public ProcedimientoDto getFiltroProcedimientoDto() {
		return filtroProcedimientoDto;
	}

	public void setFiltroProcedimientoDto(ProcedimientoDto filtroProcedimientoDto) {
		this.filtroProcedimientoDto = filtroProcedimientoDto;
	}

	public Long getPaqueteId() {
		return paqueteId;
	}

	public void setPaqueteId(Long paqueteId) {
		this.paqueteId = paqueteId;
	}

	public PaquetePortafolioDto getPaquete() {
		return paquete;
	}

	public List<SedeNegociacionPaqueteMedicamentoDto> getPaqueteMedicamentoNegociacion() {
		return paqueteMedicamentoNegociacion;
	}

	public void setPaqueteMedicamentoNegociacion(List<SedeNegociacionPaqueteMedicamentoDto> paqueteMedicamentoNegociacion) {
		this.paqueteMedicamentoNegociacion = paqueteMedicamentoNegociacion;
	}

	public List<SedeNegociacionPaqueteProcedimientoDto> getPaqueteProcedimientoNegociacion() {
		return paqueteProcedimientoNegociacion;
	}

	public void setPaqueteProcedimientoNegociacion(List<SedeNegociacionPaqueteProcedimientoDto> paqueteProcedimientoNegociacion) {
		this.paqueteProcedimientoNegociacion = paqueteProcedimientoNegociacion;
	}

	public List<SedeNegociacionPaqueteInsumoDto> getPaqueteInsumoNegociacion() {
		return paqueteInsumoNegociacion;
	}

	public void setPaqueteInsumoNegociacion(List<SedeNegociacionPaqueteInsumoDto> paqueteInsumoNegociacion) {
		this.paqueteInsumoNegociacion = paqueteInsumoNegociacion;
	}

	public List<SedeNegociacionPaqueteProcedimientoDto> getProcedimientosPaquetePrestador() {
		return procedimientosPaquetePrestador;
	}

	public SedeNegociacionPaqueteProcedimientoDto getProcedimientoSeleccionado() {
		return procedimientoSeleccionado;
	}

	public void setProcedimientoSeleccionado(SedeNegociacionPaqueteProcedimientoDto procedimientoSeleccionado) {
		this.procedimientoSeleccionado = procedimientoSeleccionado;
	}

	public List<DescriptivoDto> getOpcionesIngresoAplica() {
		return opcionesIngresoAplica;
	}

	public void setOpcionesIngresoAplica(List<DescriptivoDto> opcionesIngresoAplica) {
		this.opcionesIngresoAplica = opcionesIngresoAplica;
	}

	public List<DescriptivoDto> getOpcionesFrecuenciaUnidad() {
		return opcionesFrecuenciaUnidad;
	}

	public void setOpcionesFrecuenciaUnidad(List<DescriptivoDto> opcionesFrecuenciaUnidad) {
		this.opcionesFrecuenciaUnidad = opcionesFrecuenciaUnidad;
	}

	public SedeNegociacionPaqueteMedicamentoDto getMedicamentoSeleccionado() {
		return medicamentoSeleccionado;
	}

	public void setMedicamentoSeleccionado(SedeNegociacionPaqueteMedicamentoDto medicamentoSeleccionado) {
		this.medicamentoSeleccionado = medicamentoSeleccionado;
	}

	public List<ProcedimientoDto> getProcedimientosSeleccionados() {
		return procedimientosSeleccionados;
	}

	public void setProcedimientosSeleccionados(List<ProcedimientoDto> procedimientosSeleccionados) {
		this.procedimientosSeleccionados = procedimientosSeleccionados;
	}

	public List<MedicamentosDto> getMedicamentosSeleccionados() {
		return medicamentosSeleccionados;
	}

	public void setMedicamentosSeleccionados(List<MedicamentosDto> medicamentosSeleccionados) {
		this.medicamentosSeleccionados = medicamentosSeleccionados;
	}

	public List<InsumosDto> getInsumosSeleccionados() {
		return insumosSeleccionados;
	}

	public void setInsumosSeleccionados(List<InsumosDto> insumosSeleccionados) {
		this.insumosSeleccionados = insumosSeleccionados;
	}

	public Integer getOpcionesTipoProcedimiento() {
		return opcionesTipoProcedimiento;
	}

	public void setOpcionesTipoProcedimiento(Integer opcionesTipoProcedimiento) {
		this.opcionesTipoProcedimiento = opcionesTipoProcedimiento;
	}

	public String getTipoProcedimiento() {
		return tipoProcedimiento;
	}

	public void setTipoProcedimiento(String tipoProcedimiento) {
		this.tipoProcedimiento = tipoProcedimiento;
	}

	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}

	public String getExclusion() {
		return exclusion;
	}

	public void setExclusion(String exclusion) {
		this.exclusion = exclusion;
	}

	public String getCausaRuptura() {
		return causaRuptura;
	}

	public void setCausaRuptura(String causaRuptura) {
		this.causaRuptura = causaRuptura;
	}

	public String getRequerimientoTecnico() {
		return requerimientoTecnico;
	}

	public void setRequerimientoTecnico(String requerimientoTecnico) {
		this.requerimientoTecnico = requerimientoTecnico;
	}

	public SedeNegociacionPaqueteObservacionDto getObservacionSeleccionada() {
		return observacionSeleccionada;
	}

	public void setObservacionSeleccionada(SedeNegociacionPaqueteObservacionDto observacionSeleccionada) {
		this.observacionSeleccionada = observacionSeleccionada;
	}

	public SedeNegociacionPaqueteExclusionDto getExclusionSeleccionada() {
		return exclusionSeleccionada;
	}

	public void setExclusionSeleccionada(SedeNegociacionPaqueteExclusionDto exclusionSeleccionada) {
		this.exclusionSeleccionada = exclusionSeleccionada;
	}

	public SedeNegociacionPaqueteCausaRupturaDto getCausaRupturaSeleccionada() {
		return causaRupturaSeleccionada;
	}

	public void setCausaRupturaSeleccionada(SedeNegociacionPaqueteCausaRupturaDto causaRupturaSeleccionada) {
		this.causaRupturaSeleccionada = causaRupturaSeleccionada;
	}

	public SedeNegociacionPaqueteRequerimientoDto getRequerimientoTecnicoSeleccionado() {
		return requerimientoTecnicoSeleccionado;
	}

	public void setRequerimientoTecnicoSeleccionado(SedeNegociacionPaqueteRequerimientoDto requerimientoTecnicoSeleccionado) {
		this.requerimientoTecnicoSeleccionado = requerimientoTecnicoSeleccionado;
	}

	public List<ProcedimientoDto> getProcedimientosVentanaModal() {
		return procedimientosVentanaModal;
	}

	public void setProcedimientosVentanaModal(List<ProcedimientoDto> procedimientosVentanaModal) {
		this.procedimientosVentanaModal = procedimientosVentanaModal;
	}

	public List<MedicamentosDto> getMedicamentosVentanaModal() {
		return medicamentosVentanaModal;
	}

	public void setMedicamentosVentanaModal(List<MedicamentosDto> medicamentosVentanaModal) {
		this.medicamentosVentanaModal = medicamentosVentanaModal;
	}

	public List<InsumosDto> getInsumosVentanaModal() {
		return insumosVentanaModal;
	}

	public void setInsumosVentanaModal(List<InsumosDto> insumosVentanaModal) {
		this.insumosVentanaModal = insumosVentanaModal;
	}

	public List<CategoriaMedicamentoDto> getCategoriaMedicamento() {
        try {
            return detallePaquetefacade.listarCategoriaMedicamento();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

	public SedeNegociacionPaqueteInsumoDto getInsumoSeleccionado() {
		return insumoSeleccionado;
	}

	public void setInsumoSeleccionado(SedeNegociacionPaqueteInsumoDto insumoSeleccionado) {
		this.insumoSeleccionado = insumoSeleccionado;
	}

	public void setSedePrestadorSeleccionada(SedePrestadorDto sedePrestadorSeleccionada) {
		SedePrestadorSeleccionada = sedePrestadorSeleccionada;
	}

	public SedePrestadorDto getSedePrestadorSeleccionada() {
		return SedePrestadorSeleccionada;
	}

	public List<ErroresImportTecnologiasDto> getListErrors() {
		return listErrors;
	}

	public void setListErrors(List<ErroresImportTecnologiasDto> listErrors) {
		this.listErrors = listErrors;
	}

	public List<TecnologiasIngresadasDto> getTecnologiasIngresadas() {
		return tecnologiasIngresadas;
	}

	public void setTecnologiasIngresadas(List<TecnologiasIngresadasDto> tecnologiasIngresadas) {
		this.tecnologiasIngresadas = tecnologiasIngresadas;
	}

	public ArchivosNegociacionEnum getTypeImport() {
		return typeImport;
	}

	public void setTypeImport(ArchivosNegociacionEnum typeImport) {
		this.typeImport = typeImport;
	}

	public List<ErroresImportTecnologiasDto> getListErrorsAnexos() {
		return listErrorsAnexos;
	}

	public void setListErrorsAnexos(List<ErroresImportTecnologiasDto> listErrorsAnexos) {
		this.listErrorsAnexos = listErrorsAnexos;
	}
    //</editor-fold>
}
