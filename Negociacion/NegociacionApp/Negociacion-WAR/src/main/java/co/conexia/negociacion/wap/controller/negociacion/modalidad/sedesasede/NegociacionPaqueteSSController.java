package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import co.conexia.negociacion.wap.facade.common.CommonFacade;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionPaqueteSSFacade;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.filtro.FiltroSedeNegociacionPaquete;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.PaqueteNegociacionDto;
import com.conexia.contratacion.commons.dto.util.PaquetePortafolioServicioSaludDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controlador de una negociacion de paquetes sede a sede
 *
 * @author jtorres
 */
@Named
@ViewScoped
public class NegociacionPaqueteSSController implements Serializable {
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
    private NegociacionPaqueteSSFacade negociacionPaqueteFacade;
    @Inject
    private TecnologiasSSController tecnologiasController;
    @Inject
    private CommonFacade commonFacade;
    @Inject
    @UserInfo
    private UserApp user;

    private List<PaqueteNegociacionDto> paquetesNegociacion;
    private List<PaqueteNegociacionDto> paquetesNegociacionSeleccionados;
    private List<PaqueteNegociacionDto> paquetesNegociacionOriginal;
    private GestionTecnologiasNegociacionEnum gestionSeleccionada;
    private TipoAsignacionTarifaPaqueteEnum tipoAsignacionSeleccionado;
    private Double porcentajeValor = 0.0;
    private String filtroValor;
    private String filtroPorcentaje;
    private FiltroEspecialEnum filtroEspecialSeleccionadoValor;
    private FiltroEspecialEnum filtroEspecialSeleccionadoPorcentaje;
    private TiposDiferenciaNegociacionEnum diferenciaValor = TiposDiferenciaNegociacionEnum.DIFERENCIA_VALOR;
    private TiposDiferenciaNegociacionEnum diferenciaPorcentaje = TiposDiferenciaNegociacionEnum.DIFERENCIA_PORCENTAJE;
    private FiltroSedeNegociacionPaquete filtroSedeNegociacionPaquete;
    private List<PaquetePortafolioDto> paquetesAgregar = new ArrayList<>();
    private List<PaquetePortafolioDto> paquetesAgregarSeleccionados = new ArrayList<>();
    private List<SedePrestadorDto> sedesPrestadorAgregar = new ArrayList<>();
    private List<SedePrestadorDto> sedesPrestadorSeleccionadasAgregar = new ArrayList<>();
    private List<PaquetePortafolioServicioSaludDto> paquetesSinHabilitacion;
    private Function<List<PaquetePortafolioServicioSaludDto>, String> funcionListaCodigosPq = (l -> l
            .stream()
            .map(s -> s.getPaquetePortafolio().getCodigoPortafolio())
            .collect(Collectors.joining(" , " + System.lineSeparator())));
    private NegociacionDto negociacion;

    @PostConstruct
    public void postconstruct() {
        this.filtroSedeNegociacionPaquete = new FiltroSedeNegociacionPaquete();
        this.negociacion = Optional.ofNullable(this.tecnologiasController.getNegociacion())
                .orElse(new NegociacionDto());
        consultarPaquetesPorNegociacion();
        this.sedesPrestadorAgregar = this.commonFacade.getSedesPrestadorByNegociacion(this.negociacion);
        this.paquetesNegociacionSeleccionados = new ArrayList<>();
    }

    private void consultarPaquetesPorNegociacion() {
        this.paquetesNegociacion = negociacionPaqueteFacade.consultarPaquetesNegociacionNoSedesByNegociacionId(
                this.negociacion.getId());
        this.paquetesNegociacionOriginal = new ArrayList<>(this.paquetesNegociacion);
    }

    private void aplicarValorMasivo(final TipoAsignacionTarifaPaqueteEnum tipo) {
        try {
            if (paquetesNegociacionSeleccionados.isEmpty()) {
                facesMessagesUtils.addInfo(resourceBundle.getString("paquete_msj_val_med_sel"));
            } else {
                int paquetesAgregadosTarifa = 0;
                int paquetesNoAgregadosTarifa = 0;
                StringBuilder paquetesConTarifa = new StringBuilder();
                StringBuilder paquetesSinTarifa = new StringBuilder();
                for (PaqueteNegociacionDto dto : paquetesNegociacionSeleccionados) {
                    if (asignarValorNegociado(dto, tipo)) {
                        paquetesConTarifa.append(dto.getPaquetePortafolioDto()
                                .getCodigoSedePrestador() + ", ");
                        paquetesAgregadosTarifa++;
                    } else {
                        paquetesSinTarifa.append(dto.getPaquetePortafolioDto()
                                .getCodigoSedePrestador() + ", ");
                        paquetesNoAgregadosTarifa++;
                    }
                }
                if (paquetesAgregadosTarifa > 0)
                    facesMessagesUtils.addInfo(resourceBundle.getString("paquete_tarifa_aplicada")
                            + paquetesConTarifa.substring(0, paquetesConTarifa.length() - 2));
                if (paquetesNoAgregadosTarifa > 0)
                    facesMessagesUtils.addError(resourceBundle.getString("paquete_err_valor_referencia")
                            + paquetesSinTarifa.substring(0, paquetesSinTarifa.length() - 2));
            }
        } catch (Exception e) {
            logger.error("Error al aplicar el valor tipo " + tipo.toString() + " a los paquetes seleccionados.", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    private void aplicarPorcentajeMasivo(final Double porcentaje) {
        if (paquetesNegociacionSeleccionados.isEmpty()) {
            facesMessagesUtils.addInfo(resourceBundle.getString("paquete_msj_val_med_sel"));
        } else {
            paquetesNegociacionSeleccionados.forEach(dto -> {
                BigDecimal valorNegociado = Optional.ofNullable(dto.getValorNegociado())
                        .orElse(BigDecimal.ZERO)
                        .multiply(BigDecimal.valueOf(porcentaje))
                        .divide(BigDecimal.valueOf(100.0))
                        .setScale(2, RoundingMode.HALF_DOWN);
                dto.setValorNegociado(Optional.ofNullable(dto.getValorNegociado())
                        .orElse(BigDecimal.ZERO)
                        .add(valorNegociado));
                dto.setNegociado(true);
            });
            facesMessagesUtils.addInfo(resourceBundle.getString("paquete_msj_actualizacion_ok"));
        }
    }

    private boolean asignarValorNegociado(PaqueteNegociacionDto dto, final TipoAsignacionTarifaPaqueteEnum tipo) {
        if (tipo.equals(TipoAsignacionTarifaPaqueteEnum.CONTRATO_ANTERIOR)
                && dto.getValorContratoAnterior()
                .compareTo(BigDecimal.ZERO) > 0) {
            dto.setValorNegociado(dto.getValorContratoAnterior() != null ? dto.getValorContratoAnterior() : dto.getValorContratoAnterior());
            dto.setNegociado(dto.getValorContratoAnterior() != null ? true : true);
            return true;
        } else if (tipo.equals(TipoAsignacionTarifaPaqueteEnum.PORTAFOLIO_PROPUESTO)
                && dto.getValorPropuestoPortafolio()
                .compareTo(BigDecimal.ZERO) > 0) {
            dto.setValorNegociado(dto.getValorPropuestoPortafolio());
            dto.setNegociado(true);
            return true;
        }
        if (dto.getValorReferencia() != null) {
            if (tipo.equals(TipoAsignacionTarifaPaqueteEnum.TARIFA_EMSSANAR)
                    && dto.getValorReferencia()
                    .compareTo(BigDecimal.ZERO) > 0) {
                dto.setValorNegociado(dto.getValorReferencia());
                dto.setNegociado(true);
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void eliminarPaquetesMasivo() {
        try {
            if (paquetesNegociacionSeleccionados.isEmpty()) {
                facesMessagesUtils.addInfo(resourceBundle.getString("paquete_msj_val_med_sel"));
            } else {
                paquetesNegociacionSeleccionados.forEach(dto -> {
                    this.eliminarPaquete(dto.getPaquetePortafolioDto()
                            .getPortafolio()
                            .getId());
                    paquetesNegociacion.remove(dto);
                });
                paquetesNegociacionSeleccionados.clear();
                paquetesNegociacionOriginal.clear();
                actualizarTablaNegociacion();
                facesMessagesUtils.addInfo(resourceBundle.getString("paquete_msj_eliminacion_ok"));

            }
        } catch (Exception e) {
            logger.error("Error al eliminar de la negociación los paquetes seleccionados", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    private void eliminarPaquete(Long paqueteId) {
        try {
            negociacionPaqueteFacade.eliminarByNegociacionAndPaquete(this.negociacion.getId(), paqueteId, this.user.getId());
        } catch (Exception e) {
            logger.error("Error al eliminar de la negociación el medicamento No." + paqueteId, e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public void asignarTarifasPaquete() {
        Optional.ofNullable(this.tipoAsignacionSeleccionado)
                .ifPresent(tarifaPaqueteEnum -> {
                    switch (tarifaPaqueteEnum) {
                        case CONTRATO_ANTERIOR:
                        case PORTAFOLIO_PROPUESTO:
                        case TARIFA_EMSSANAR:
                            aplicarValorMasivo(tarifaPaqueteEnum);
                            break;
                        case APLICAR_PORCENTAJE:
                            aplicarPorcentajeMasivo(Optional.ofNullable(porcentajeValor)
                                    .orElse(0.0));
                            break;
                    }
                    this.guardarEstadoPaquetes(false);
                });
    }

    public void gestionarPaquetes(String nombreTabla) {
        Optional<GestionTecnologiasNegociacionEnum> gestionTecnologiasNegociacion = Optional.ofNullable(this.gestionSeleccionada);
        if (gestionTecnologiasNegociacion.isPresent()) {
            switch (this.gestionSeleccionada) {
                case BORRAR_SELECCIONADOS:
                    if (paquetesNegociacionSeleccionados.isEmpty()) {
                        facesMessagesUtils.addInfo(resourceBundle.getString("paquete_msj_val_med_sel"));
                    } else {
                        RequestContext.getCurrentInstance()
                                .execute("PF('cdDeletePq').show();");
                    }
                    break;
                case SELECCIONAR_TODOS:
                    Ajax.oncomplete("PF('" + nombreTabla + "').selectAllRows();");
                    this.paquetesNegociacionSeleccionados = new ArrayList<>(this.paquetesNegociacion);
                    paquetesNegociacion.parallelStream()
                            .forEach(paq -> paq.setSeleccionado(true));
                    break;
                case DESELECCIONAR_TODOS:
                    Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRows();");
                    paquetesNegociacionSeleccionados.clear();
                    paquetesNegociacion.parallelStream()
                            .forEach(paq -> paq.setSeleccionado(false));
                    break;
            }
            this.gestionSeleccionada = null;
            consultarPaquetesPorNegociacion();
        }
    }

    private boolean validacion() {
        return paquetesNegociacion.stream()
                .anyMatch(dto -> dto.getValorNegociado() == null);
    }

    private boolean existenPaqueteInsumoSinParametros() {
        return negociacionPaqueteFacade.existenPaqueteInsumoSinParametros(negociacion.getId());
    }

    private boolean existenPaqueteMedicamentosSinParametros() {
        return negociacionPaqueteFacade.existenPaqueteMedicamentosSinParametros(negociacion.getId());
    }

    private boolean existePaqueteProcedimientosSinParametros() {
        return negociacionPaqueteFacade.existePaqueteProcedimientosSinParametros(negociacion.getId());
    }

    private void guardarEstadoPaquetes(boolean agregarMensaje) {
        try {
            //redondeoValorNegociado();
            negociacionPaqueteFacade.guardarPaquetesNegociados(paquetesNegociacionSeleccionados, this.negociacion.getId(), this.user.getId());
            paquetesNegociacionOriginal.clear();
            paquetesNegociacionOriginal.addAll(paquetesNegociacion);
            if (agregarMensaje) {
                facesMessagesUtils.addInfo("Operación realizada con éxito!");
            }
        } catch (Exception e) {
            logger.error("Error al guardar el estado de los paquetes en negociación.", e);
            facesMessagesUtils.addError(resourceBundle.getString("system_error"));
        }
    }

    public void guardarValorPaquete(PaqueteNegociacionDto paqueteNegociacion) {
        if (precioEsValido(paqueteNegociacion)) {
            paqueteNegociacion.setValorNegociado(paqueteNegociacion.getValorNegociado()
                    .setScale(2, BigDecimal.ROUND_HALF_UP));
            paqueteNegociacion.setNegociado(true);
            negociacionPaqueteFacade.guardarPaqueteNegociado(paqueteNegociacion, this.negociacion.getId(), this.user.getId());
            facesMessagesUtils.addInfo("Operación realizada con éxito!");
        } else {
            facesMessagesUtils.addError("El valor es inferior que el mínimo permitido de '1'");
        }
    }

    public void guardarNegociado(PaqueteNegociacionDto paqueteNegociacion) {
        if (!paqueteNegociacion.getNegociado()) {
            paqueteNegociacion.setValorNegociado(BigDecimal.ZERO);
            negociacionPaqueteFacade.guardarPaqueteNegociado(paqueteNegociacion, this.negociacion.getId(), this.user.getId());
            facesMessagesUtils.addInfo("Operación realizada con éxito!");
        }
    }

    private void redondeoValorNegociado() {
        paquetesNegociacion.stream()
                .filter(paqueteNegociacion -> Objects.nonNull(paqueteNegociacion.getValorNegociado()))
                .forEach(paqueteNegociacion -> paqueteNegociacion.setValorNegociado(paqueteNegociacion.getValorNegociado()
                        .setScale(-2, BigDecimal.ROUND_HALF_UP)));
    }

    public boolean guardarValidarPaquetes(boolean agregarMensaje) {
        this.guardarEstadoPaquetes(agregarMensaje);
        if (existenPaqueteInsumoSinParametros()) {
            facesMessagesUtils.addInfo("Existen Insumos en paquetes sin parametrizar cantidad Máxima y cantidad Mínima");
            return false;
        }
        if (existenPaqueteMedicamentosSinParametros()) {
            facesMessagesUtils.addInfo("Existen Medicamentos en paquetes sin parametrizar cantidad Máxima y cantidad Mínima");
            return false;
        }
        if (existePaqueteProcedimientosSinParametros()) {
            facesMessagesUtils.addInfo("Existen Procedimientos en paquetes sin parametrizar cantidad Máxima y cantidad Mínima");
            return false;
        }
        if (validacion()) {
            facesMessagesUtils.addInfo(resourceBundle.getString("paquete_msj_validacion_no"));
            return false;
        }
        return true;
    }

	/**
	 * Filtro especial de la tabla paquetes
	 *
	 * @param tipo
	 *            identifica por que campo filtrar
	 */
	public void filtroEspecial(TiposDiferenciaNegociacionEnum tipo, String nombreTabla) {
		try {
			this.paquetesNegociacion.clear();
			this.paquetesNegociacion.addAll(paquetesNegociacionOriginal);
			FiltroEspecialEnum seleccionado = null;
			if (tipo.equals(TiposDiferenciaNegociacionEnum.DIFERENCIA_VALOR)) {
				seleccionado = filtroEspecialSeleccionadoValor;
			} else {
				seleccionado = filtroEspecialSeleccionadoPorcentaje;
			}
			if (seleccionado != null) {
				switch (seleccionado) {
				case ENTRE:
					this.filtrarEntre(tipo);
					break;
				case IGUAL:
					this.filtrarIgual(tipo);
					break;
				case MAYOR:
					this.filtrarMayor(tipo);
					break;
				case MENOR:
					this.filtrarMenor(tipo);
					break;
				}
			}
		} catch (Exception e) {
			logger.error("Error en filtro especial", e);
			facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}

	public void filtrarEntre(TiposDiferenciaNegociacionEnum tipo) {
		String[] filtros;
		if (tipo.equals(TiposDiferenciaNegociacionEnum.DIFERENCIA_PORCENTAJE)) {
			filtros = this.filtroPorcentaje.split(",");
			this.paquetesNegociacion = paquetesNegociacion
					.stream()
					.filter(m -> (m.getDiferenciaPorcentajeContratado() != null
							&& m.getDiferenciaPorcentajeContratado() < Double
									.parseDouble(filtros[1]) && m
							.getDiferenciaPorcentajeContratado() > Double
							.parseDouble(filtros[0])))
					.collect(Collectors.toList());
		} else if (tipo.equals(TiposDiferenciaNegociacionEnum.DIFERENCIA_VALOR)) {
			filtros = this.filtroValor.split(",");
			this.paquetesNegociacion = paquetesNegociacion
					.stream()
					.filter(m -> (m.getDiferenciaValorContratado() != null
							&& m.getDiferenciaValorContratado() < Double
									.parseDouble(filtros[1]) && m
							.getDiferenciaValorContratado() > Double
							.parseDouble(filtros[0])))
					.collect(Collectors.toList());
		}
	}

	public void filtrarIgual(TiposDiferenciaNegociacionEnum tipo) {
		if (tipo.equals(TiposDiferenciaNegociacionEnum.DIFERENCIA_PORCENTAJE)) {
			this.paquetesNegociacion = paquetesNegociacion
					.stream()
					.filter(m -> (m.getDiferenciaPorcentajeContratado() != null && m
							.getDiferenciaPorcentajeContratado() == Double
							.parseDouble(this.filtroPorcentaje)))
					.collect(Collectors.toList());
		} else if (tipo.equals(TiposDiferenciaNegociacionEnum.DIFERENCIA_VALOR)) {
			this.paquetesNegociacion = paquetesNegociacion
					.stream()
					.filter(m -> (m.getDiferenciaValorContratado() != null && m
							.getDiferenciaValorContratado() == Double
							.parseDouble(this.filtroValor)))
					.collect(Collectors.toList());
		}
	}

	public void filtrarMayor(TiposDiferenciaNegociacionEnum tipo) {
		if (tipo.equals(TiposDiferenciaNegociacionEnum.DIFERENCIA_PORCENTAJE)) {
			this.paquetesNegociacion = paquetesNegociacion
					.stream()
					.filter(m -> (m.getDiferenciaPorcentajeContratado() != null && m
							.getDiferenciaPorcentajeContratado() > Double
							.parseDouble(filtroPorcentaje)))
					.collect(Collectors.toList());
		} else if (tipo.equals(TiposDiferenciaNegociacionEnum.DIFERENCIA_VALOR)) {
			this.paquetesNegociacion = paquetesNegociacion
					.stream()
					.filter(m -> (m.getDiferenciaValorContratado() != null && m
							.getDiferenciaValorContratado() > Double
							.parseDouble(filtroValor)))
					.collect(Collectors.toList());
		}
	}

	public void filtrarMenor(TiposDiferenciaNegociacionEnum tipo) {
		if (tipo.equals(TiposDiferenciaNegociacionEnum.DIFERENCIA_PORCENTAJE)) {
			this.paquetesNegociacion = paquetesNegociacion
					.stream()
					.filter(m -> (m.getDiferenciaPorcentajeContratado() != null && m
							.getDiferenciaPorcentajeContratado() < Double
							.parseDouble(filtroPorcentaje)))
					.collect(Collectors.toList());
		} else if (tipo.equals(TiposDiferenciaNegociacionEnum.DIFERENCIA_VALOR)) {
			this.paquetesNegociacion = paquetesNegociacion
					.stream()
					.filter(m -> (m.getDiferenciaValorContratado() != null && m
							.getDiferenciaValorContratado() < Double
							.parseDouble(filtroValor)))
					.collect(Collectors.toList());
		}
	}

    public void verDetallePaquete(Long paqueteId) {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            HttpSession session = (HttpSession) facesContext.getExternalContext()
                    .getSession(true);
            session.setAttribute(NegociacionSessionEnum.PAQUETE_ID.toString(), paqueteId);
            session.setAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString(), this.negociacion.getPrestador()
                    .getId());
            ExternalContext context = facesContext.getExternalContext();
            context.redirect("/wap/negociacion/paquete/detalle");
            facesContext.responseComplete();
        } catch (Exception e) {
            logger.error("Error en ver detalle", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public void consultarPaquetesAgregar() {
        try {
            paquetesAgregar.clear();
            paquetesAgregarSeleccionados.clear();
            paquetesAgregar.addAll(this.negociacionPaqueteFacade.consultarPaquetesAgregar(this.filtroSedeNegociacionPaquete, this.negociacion));
            sedesPrestadorSeleccionadasAgregar.clear();
            paquetesAgregarSeleccionados = new ArrayList<>();
            if (paquetesAgregar.size() != 0) {
                validarInactivos();
                if (paquetesAgregar.size() != 0) {
                    RequestContext.getCurrentInstance().execute("PF('paquetesAgregarDlg').show();");
                    Ajax.oncomplete("PF('paquetesAgregarTableWV').filter();");
                }
            } else {
                facesMessagesUtils.addWarning(resourceBundle.getString("negociacion_procedimiento_agregar_no_registros"));
            }
        } catch (ConexiaBusinessException e) {
            logger.error("Error al eliminar de la negociación los procedimientos seleccionados", e);
            this.facesMessagesUtils.addWarning(this.exceptionUtils.createMessage(resourceBundle, e));
        } catch (Exception e) {
            logger.error("Error al eliminar de la negociación los procedimientos seleccionados", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    private void validarInactivos() {
        List<PaquetePortafolioDto> paquetesVigencia = new ArrayList<>(paquetesAgregar);
        paquetesVigencia.forEach(paquete -> {
            if (!paquete.getEstadoPaquete()) {
                facesMessagesUtils.addWarning(resourceBundle.getString("negociacion_tecnologia_inactiva"));
                paquetesAgregar.remove(paquete);
            }
        });
    }

    public void agregarPaquetesNegociacion(List<PaquetePortafolioDto> paquetes) {
        if (paquetes.isEmpty()) {
            facesMessagesUtils.addWarning(resourceBundle.getString("negociacion_paquete_agregar_seleccionar_err"));
            return;
        }
        descartarPaquetesContenidoInactivo(paquetes);
        descartarPaquetesSinServicio(paquetes);
        descartarPaquetesExistentesNegociacion(paquetes);
        try {
            if (paquetes.isEmpty()) {
                return;
            }
            Map<String, Long> paquetesRepetidos = validarPaquetesRepetidos(paquetes);
            if (paquetesRepetidos.keySet()
                    .size() > 0) {
                this.facesMessagesUtils.addWarning(resourceBundle.getString("negociacion_paquete_agregar_masivo_duplicado") + ": "
                        + paquetesRepetidos.keySet()
                        .stream()
                        .collect(Collectors.joining(" , " + System.lineSeparator())));
                return;
            }
            insertarPaquete(paquetes);
            filtroSedeNegociacionPaquete = new FiltroSedeNegociacionPaquete();
            paquetesAgregar.clear();
            paquetesAgregarSeleccionados.clear();
            sedesPrestadorSeleccionadasAgregar.clear();
            paquetesAgregar = new ArrayList<>();
            paquetesAgregarSeleccionados = new ArrayList<>();
            Ajax.update("paquetesAgregarForm");
            Ajax.update("paquetesAgregarForm:panelSedesPrestador");
            Ajax.update("paquetesAgregarForm:paquetesAgregarPanel");
            Ajax.update("paquetesAgregarForm:paquetesAgregarPanel:paquetesAgregarTable");
        } catch (Exception e) {
            logger.error("Error al agregar paquetes", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    private void descartarPaquetesExistentesNegociacion(List<PaquetePortafolioDto> paquetes) {
        List<PaquetePortafolioDto> paquetesH = new ArrayList<>(paquetes) ;
        for (PaquetePortafolioDto pq : paquetesH) {
            for (PaqueteNegociacionDto a : paquetesNegociacion) {                
                boolean existePaquete = a.getPaquetePortafolioDto().getCodigoSedePrestador().equals(pq.getCodigoPortafolio())
                        && validarExistePaqueteSede(a.getSedePrestadorDto().getId());                
                if (existePaquete && this.sedesPrestadorSeleccionadasAgregar.isEmpty()) 
                {
                    facesMessagesUtils.addWarning(resourceBundle.getString("paquete_ya_existe_negociacion"));
                    paquetes.remove(pq);
                    break;
                }
            }
        }
    }
    
    /**
     * Method to validate if exist the package in list sedesPrestador selected 
     * 
     * @param sedesPrestadorSeleccionadasAgregar    List of SedesPrestador to add
     * @param sedePrestadorId                       SedePrestador currently
     * @return boolean                              True(Exist), False(No exist)
     */
    private boolean validarExistePaqueteSede(Long sedePrestadorId)
    {
        boolean existeSede = Boolean.FALSE;
        if (Objects.isNull(sedePrestadorId)) return existeSede;        
        for (SedePrestadorDto sp : this.sedesPrestadorSeleccionadasAgregar)
        {
            if (sp.getId().equals(sedePrestadorId))
            {
                existeSede = Boolean.TRUE;
                this.sedesPrestadorSeleccionadasAgregar.remove(sp);
                break;
            }
        }
        return existeSede;
    }

    private void insertarPaquete(List<PaquetePortafolioDto> paquetes) {
        //verificacion paquete y servicios habilitacion
        List<PaquetePortafolioDto> paquetesVerificados = new ArrayList<>();
        List<SedePrestadorDto> sedesVerificadas = new ArrayList<>();

        List<PaquetePortafolioServicioSaludDto> errores = this.negociacionPaqueteFacade.consultaPaqueteServicioVsSedes(this.negociacion, paquetes, this.sedesPrestadorSeleccionadasAgregar);
        paquetes.stream()
                .forEach(paquete -> {
                    errores.stream()
                            .filter(resultado -> resultado.getPaquetePortafolio()
                                    .getCodigoPortafolio()
                                    .equals(paquete.getCodigoPortafolio()))
                            .forEach(dato -> dato.getPaquetePortafolio()
                                    .setId(paquete.getPortafolio()
                                            .getId()));
                });
        List<Integer> sedesIds = errores.stream()
                .filter(sede -> sede.getMensaje() == null)
                .map(id -> id.getSedePrestador()
                        .getId()
                        .intValue())
                .collect(Collectors.toList());
        this.paquetesSinHabilitacion = errores.stream()
                .filter(sede -> !sedesIds.contains(sede.getSedePrestador()
                        .getId()
                        .intValue()))
                .collect(Collectors.toList());
        List<PaquetePortafolioServicioSaludDto> paqueteInsertar = new ArrayList<>();
        errores.stream()
                .filter(sede -> sede.getMensaje() == null && !paqueteInsertar.contains(sede))
                .forEach(dato -> {
                    paqueteInsertar.add(dato);
                });
        this.negociacionPaqueteFacade.agregarPaquetesNegociacion(paqueteInsertar, this.negociacion, user.getId());
        actualizarTablaNegociacion();
        RequestContext.getCurrentInstance()
                .execute("PF('paquetesAgregarDlg').hide();");
        if (!this.paquetesSinHabilitacion.isEmpty()) {
            RequestContext.getCurrentInstance()
                    .execute("PF('paquetesNoCruzadosDlg').show();");
        }
        this.facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_paquete_agregar_masivo_ok") + this.funcionListaCodigosPq.apply(paqueteInsertar));
    }

    private void descartarPaquetesContenidoInactivo(List<PaquetePortafolioDto> paquetes) {
        List<PaquetePortafolioDto> paquetesTemp = new ArrayList<>(paquetes);
        paquetesTemp.forEach(paquete -> {
                    if (validarInactivosContenidoPaquete(paquete)) {
                        this.facesMessagesUtils.addError(String.format(resourceBundle.getString("negociacion_paquete_con_contenido_inactivo"), paquete.getCodigoPortafolio()));
                        paquetes.remove(paquete);
                    }
                }
        );
    }

    private void descartarPaquetesSinServicio(List<PaquetePortafolioDto> paquetes) {
        List<PaquetePortafolioDto> paquetesTemp = new ArrayList<>(paquetes);
        paquetesTemp.forEach(paquete -> {
                    if (!validarPaqueteSinServicio(paquete)) {
                        this.facesMessagesUtils.addError(String.format(resourceBundle.getString("negociacion_paquete_sin_servicio"), paquete.getCodigoPortafolio()));
                        paquetes.remove(paquete);
                    }
                }
        );
    }

    private boolean validarInactivosContenidoPaquete(PaquetePortafolioDto paquete) {
        return negociacionPaqueteFacade.consultarInactivosContenidoPaquetes(paquete);
    }

    private boolean validarPaqueteSinServicio(PaquetePortafolioDto paquete) {
        return negociacionPaqueteFacade.validarPaqueteSinServicio(paquete);
    }

    private void actualizarTablaNegociacion() {
        consultarPaquetesPorNegociacion();
        RequestContext requestContext = RequestContext.getCurrentInstance();
        requestContext.execute("PF('negociacionPaquetesSS').clearFilters()");
        Ajax.oncomplete("PF('negociacionPaquetesSS').filter();");
    }

    private List<Integer> eliminarIdsRepetidos(List<Integer> ids) {
        Set<Integer> hs = new HashSet<>();
        hs.addAll(ids);
        ids.clear();
        ids.addAll(hs);
        return ids;
    }

    private Map<String, Long> validarPaquetesRepetidos(List<PaquetePortafolioDto> paquetes) {
        Map<String, Long> paqueteCantidad = new HashMap<>();
        paquetes.stream()
                .filter(paquetePortafolioDto -> !paqueteCantidad.containsKey(paquetePortafolioDto.getCodigoPortafolio()))
                .forEach(dto -> paqueteCantidad.put(dto.getCodigoPortafolio(), paquetes.stream()
                        .filter(p -> p.getCodigoPortafolio()
                                .equals(dto.getCodigoPortafolio()))
                        .count()));
        return paqueteCantidad.entrySet()
                .stream()
                .filter(v -> v.getValue() > 1L)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public void unRowSelect(UnselectEvent event) {
        if (Objects.nonNull(event) && Objects.nonNull(event.getObject())) {
            PaqueteNegociacionDto unSelectedItem = (PaqueteNegociacionDto) event.getObject();

            if (!paquetesNegociacionSeleccionados.isEmpty()) {
                int removeIndex = -1;
                for (int i = 0; i < paquetesNegociacionSeleccionados.size(); i++) {
                    PaqueteNegociacionDto dto = paquetesNegociacionSeleccionados.get(i);
                    if (dto.getPaquetePortafolioDto()
                            .getId()
                            .compareTo(unSelectedItem.getPaquetePortafolioDto()
                                    .getId()) == 0) {
                        removeIndex = i;
                        break;
                    }
                }
                if (removeIndex > -1) {
                    paquetesNegociacionSeleccionados.remove(removeIndex);
                }
            }

            // Lo coloca como no seleccionado en los procedimientos de la negociacion
            if (Objects.nonNull(paquetesNegociacion) && !paquetesNegociacion.isEmpty()) {
                for (PaqueteNegociacionDto dtoNegociacion : paquetesNegociacion) {
                    if (dtoNegociacion.getPaquetePortafolioDto()
                            .getId()
                            .compareTo(unSelectedItem.getPaquetePortafolioDto()
                                    .getId()) == 0) {
                        dtoNegociacion.setSeleccionado(false);
                    }
                }
            }
        }

    }

    public void rowSelect(SelectEvent event) {
        if (Objects.nonNull(event) && Objects.nonNull(event.getObject())) {
            PaqueteNegociacionDto selectedItem = (PaqueteNegociacionDto) event.getObject();
            if (!paquetesNegociacionSeleccionados.isEmpty()) {
                paquetesNegociacionSeleccionados.stream()
                        .filter(dto -> dto.getPaquetePortafolioDto()
                                .getId()
                                .compareTo(selectedItem.getPaquetePortafolioDto()
                                        .getId()) == 0)
                        .forEach(dto -> dto.setSeleccionado(true));
            }
            // Lo coloca como seleccionado en los procedimientos de la negociacion
            if (Objects.nonNull(paquetesNegociacion) && !paquetesNegociacion.isEmpty()) {
                for (PaqueteNegociacionDto dtoNegociacion : paquetesNegociacion) {
                    if (dtoNegociacion.getPaquetePortafolioDto()
                            .getId()
                            .compareTo(selectedItem.getPaquetePortafolioDto()
                                    .getId()) == 0) {
                        dtoNegociacion.setSeleccionado(true);
                    }
                }
            }
        }
    }

    public void toggleSelect(ToggleSelectEvent event) {
        Optional.ofNullable(event)
                .ifPresent(toggleSelectEvent -> {
                    if (toggleSelectEvent.isSelected()) {
                        if (Objects.nonNull(paquetesNegociacion) && !paquetesNegociacion.isEmpty() && Objects.nonNull(paquetesNegociacionOriginal) && !paquetesNegociacionOriginal.isEmpty()) {
                            paquetesNegociacionOriginal.forEach(unSelect -> paquetesNegociacion.stream()
                                    .filter(dtoNegociacion -> dtoNegociacion.getPaquetePortafolioDto()
                                            .getId()
                                            .compareTo(unSelect.getPaquetePortafolioDto()
                                                    .getId()) == 0)
                                    .forEach(dtoNegociacion -> dtoNegociacion.setSeleccionado(true)));
                        }
                    } else {
                        if (Objects.nonNull(paquetesNegociacion) && !paquetesNegociacion.isEmpty() && Objects.nonNull(paquetesNegociacionOriginal) && !paquetesNegociacionOriginal.isEmpty()) {
                            paquetesNegociacionOriginal.forEach(unSelect -> paquetesNegociacion.stream()
                                    .filter(dtoNegociacion -> dtoNegociacion.getPaquetePortafolioDto()
                                            .getId()
                                            .compareTo(unSelect.getPaquetePortafolioDto()
                                                    .getId()) == 0)
                                    .forEach(dtoNegociacion -> dtoNegociacion.setSeleccionado(false)));
                        }
                    }
                });
    }

    public void limpiarPanelAgregar() {
        this.filtroSedeNegociacionPaquete = new FiltroSedeNegociacionPaquete();
    }

    private boolean precioEsValido(PaqueteNegociacionDto dto) {
        return Optional.ofNullable(dto)
                .map(PaqueteNegociacionDto::getValorNegociado)
                .filter(bigDecimal -> !Objects.equals(bigDecimal, BigDecimal.ZERO))
                .isPresent();
    }

    //<editor-fold desc="Getters && Setters">
    public List<PaqueteNegociacionDto> getPaquetesNegociacion() {
        return paquetesNegociacion;
    }

    public void setPaquetesNegociacion(List<PaqueteNegociacionDto> paquetesNegociacion) {
        this.paquetesNegociacion = paquetesNegociacion;
    }

    public List<PaqueteNegociacionDto> getPaquetesNegociacionSeleccionados() {
        return paquetesNegociacionSeleccionados;
    }

    public void setPaquetesNegociacionSeleccionados(List<PaqueteNegociacionDto> paquetesNegociacionSeleccionados) {
        this.paquetesNegociacionSeleccionados = paquetesNegociacionSeleccionados;
    }

    public GestionTecnologiasNegociacionEnum[] getGestionTecnologiasNegociacion() {
        return GestionTecnologiasNegociacionEnum.values();
    }

    public TipoAsignacionTarifaPaqueteEnum[] getTipoAsignacionTarifaPaqueteEnum() {
        return TipoAsignacionTarifaPaqueteEnum.values();
    }

    public GestionTecnologiasNegociacionEnum getGestionSeleccionada() {
        return gestionSeleccionada;
    }

    public void setGestionSeleccionada(GestionTecnologiasNegociacionEnum gestionSeleccionada) {
        this.gestionSeleccionada = gestionSeleccionada;
    }

    public TipoAsignacionTarifaPaqueteEnum getTipoAsignacionSeleccionado() {
        return tipoAsignacionSeleccionado;
    }

    public void setTipoAsignacionSeleccionado(TipoAsignacionTarifaPaqueteEnum tipoAsignacionSeleccionado) {
        this.tipoAsignacionSeleccionado = tipoAsignacionSeleccionado;
    }

    public Double getPorcentajeValor() {
        return porcentajeValor;
    }

    public void setPorcentajeValor(Double porcentajeValor) {
        this.porcentajeValor = porcentajeValor;
    }

    public String getFiltroValor() {
        return filtroValor;
    }

    public void setFiltroValor(String filtroValor) {
        this.filtroValor = filtroValor;
    }

    public String getFiltroPorcentaje() {
        return filtroPorcentaje;
    }

    public void setFiltroPorcentaje(String filtroPorcentaje) {
        this.filtroPorcentaje = filtroPorcentaje;
    }

    public TiposDiferenciaNegociacionEnum getDiferenciaValor() {
        return diferenciaValor;
    }

    public TiposDiferenciaNegociacionEnum getDiferenciaPorcentaje() {
        return diferenciaPorcentaje;
    }

    public FiltroEspecialEnum[] getFiltroEspecialEnum() {
        return FiltroEspecialEnum.values();
    }

    public FiltroEspecialEnum getFiltroEspecialSeleccionadoValor() {
        return filtroEspecialSeleccionadoValor;
    }

    public void setFiltroEspecialSeleccionadoValor(FiltroEspecialEnum filtroEspecialSeleccionadoValor) {
        this.filtroEspecialSeleccionadoValor = filtroEspecialSeleccionadoValor;
    }

    public FiltroEspecialEnum getFiltroEspecialSeleccionadoPorcentaje() {
        return filtroEspecialSeleccionadoPorcentaje;
    }

    public void setFiltroEspecialSeleccionadoPorcentaje(FiltroEspecialEnum filtroEspecialSeleccionadoPorcentaje) {
        this.filtroEspecialSeleccionadoPorcentaje = filtroEspecialSeleccionadoPorcentaje;
    }

    public List<PaqueteNegociacionDto> getPaquetesNegociacionOriginal() {
        return paquetesNegociacionOriginal;
    }

    public void setPaquetesNegociacionOriginal(List<PaqueteNegociacionDto> paquetesNegociacionOriginal) {
        this.paquetesNegociacionOriginal = paquetesNegociacionOriginal;
    }

    public FiltroSedeNegociacionPaquete getPaqueteAgregar() {
        return this.filtroSedeNegociacionPaquete;
    }

    public void setPaqueteAgregar(FiltroSedeNegociacionPaquete filtroSedeNegociacionPaquete) {
        this.filtroSedeNegociacionPaquete = filtroSedeNegociacionPaquete;
    }

    public List<PaquetePortafolioDto> getPaquetesAgregar() {
        return paquetesAgregar;
    }

    public void setPaquetesAgregar(List<PaquetePortafolioDto> paquetesAgregar) {
        this.paquetesAgregar = paquetesAgregar;
    }

    public List<PaquetePortafolioDto> getPaquetesAgregarSeleccionados() {
        return paquetesAgregarSeleccionados;
    }

    public void setPaquetesAgregarSeleccionados(List<PaquetePortafolioDto> paquetesAgregarSeleccionados) {
        this.paquetesAgregarSeleccionados = paquetesAgregarSeleccionados;
    }

    public List<SedePrestadorDto> getSedesPrestadorSeleccionadasAgregar() {
        return sedesPrestadorSeleccionadasAgregar;
    }

    public void setSedesPrestadorSeleccionadasAgregar(List<SedePrestadorDto> sedesPrestadorSeleccionadasAgregar) {
        this.sedesPrestadorSeleccionadasAgregar = sedesPrestadorSeleccionadasAgregar;
    }

    public List<SedePrestadorDto> getSedesPrestadorAgregar() {
        return sedesPrestadorAgregar;
    }

    public void setSedesPrestadorAgregar(List<SedePrestadorDto> sedesPrestadorAgregar) {
        this.sedesPrestadorAgregar = sedesPrestadorAgregar;
    }

    public List<PaquetePortafolioServicioSaludDto> getPaquetesSinHabilitacion() {
        return paquetesSinHabilitacion;
    }

    public void setPaquetesSinHabilitacion(List<PaquetePortafolioServicioSaludDto> paquetesSinHabilitacion) {
        this.paquetesSinHabilitacion = paquetesSinHabilitacion;
    }

    public ResourceBundle getResourceBundle() {
        return resourceBundle;
    }

    //</editor-fold>
}
