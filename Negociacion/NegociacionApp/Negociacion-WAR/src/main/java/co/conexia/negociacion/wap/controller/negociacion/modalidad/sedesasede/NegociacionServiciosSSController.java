package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import co.conexia.negociacion.wap.controller.common.CommonController;
import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionServiciosSSFacade;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeindividual.NegociacionServicioProcedimientoSIFacade;
import co.conexia.negociacion.wap.util.GestionarImportarXlxs;
import co.conexia.negociacion.wap.util.XlxsFiles;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;
import com.conexia.contratacion.commons.dto.maestros.TipoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasEventoDto;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasPgpDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;
import com.conexia.utils.Constantes;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import org.xml.sax.SAXException;
import javax.annotation.PostConstruct;
import javax.enterprise.inject.Default;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Controller que maneja el tab de los servicios
 *
 * @author jjoya
 *
 */
@Named
@ViewScoped
@Default
public class NegociacionServiciosSSController implements Serializable
{

	private static final String NOMBRE_IMPORTAR_PROCEDIMIENTOS_PGP = "Formato_importar_tecnologias_negociacion_Pgp.xlsx";
	private static final String NOMBRE_IMPORTAR_PROCEDIMIENTOS_SEDE_INDIVIDUAL = "Formato_importacion_servicio_sedes.xlsx";
	private static final String NOMBRE_IMPORTAR_PROCEDIMIENTOS_TODAS_SEDES = "Formato_importacion_servicios.xlsx";
	private static final String KEY_PROPERTIES_NEGOCIATION_SERVICES_ADD_NO_FOUND = "negociacion_servicio_agregar_no_registros";
	private static final String KEY_PROPERTIES_SERVICE_MSG_VAL_SERVICE_NO_ENABLED =  "servicio_msj_val_servicio_no_hab";

	private enum FuenteServicio {MAESTROS}

	@Inject
	private Log logger;
	@Inject
	private FacesMessagesUtils facesMessagesUtils;
	@Inject
	@CnxI18n
	transient ResourceBundle resourceBundle;
    @Inject
    private FacesUtils facesUtils;
    @Inject
    private XlxsFiles xlxsFiles;
    @Inject
    private GestionarImportarXlxs gestionarImportarXlxs;
    @Inject
	private PreContractualExceptionUtils exceptionUtils;
	@Inject
	private TecnologiasSSController tecnologiasController;
	@Inject
	private ValidadorImportTecnologiasPaqueteDetalleSS validator;
	@Inject
	private NegociacionServicioProcedimientoSIFacade serviciosFacade;
	@Inject
	private NegociacionServiciosSSFacade facade;
	@Inject
	private NegociacionFacade negociacionFacade;
	@Inject
	private CommonController commonController;
        
	private List<ServicioNegociacionDto> serviciosNegociacion;
	private List<ServicioNegociacionDto> serviciosNegociacionOriginal;
	private List<ServicioNegociacionDto> serviciosNegociacionSeleccionados;
	private List<CapitulosNegociacionDto> capitulosNegociacion;
	private List<CapitulosNegociacionDto> capitulosNegociacionOriginal;
	private List<CapitulosNegociacionDto> capitulosNegociacionSeleccionados;
	private TipoAsignacionTarifaServicioEnum tipoAsignacionSeleccionado;
	private GestionTecnologiasNegociacionEnum gestionSeleccionada;
	private FiltroEspecialEnum filtroEspecialSeleccionadoPorcentaje;
	private String filtroValor;
	private String filtroPorcentaje;
	protected TiposDiferenciaNegociacionEnum diferenciaPorcentaje = TiposDiferenciaNegociacionEnum.DIFERENCIA_PORCENTAJE;
	private TipoTarifarioDto tarifarioAsignar;
	private Double porcentajeAsignar;
	private Double porcentajeAumentoPropia;
	private Double valorTarifaPropia;
	private BigDecimal franjaInicio;
	private BigDecimal franjaFin;
    private StreamedContent formatoDownload;
    private ArchivosNegociacionEnum typeImport;
    private List<ErroresImportTecnologiasPgpDto> listadoErrores;
    private List<ErroresImportTecnologiasEventoDto> listadoErroresEvento;
	private boolean showPoblacion;
	protected Function<List<ServicioSaludDto>, String> funcionListaTecnologiasSinNegociar = (l -> l
            .stream()
            .map(s -> s.getCodigo().concat(
                    "(" + s.getNumeroProcedimientos() + ")"))
            .collect(Collectors.joining(" , "+System.lineSeparator())));
	private ServicioSaludDto servicioAgregar = new ServicioSaludDto();
	private List<ServicioSaludDto> serviciosAgregar;
	private List<ServicioSaludDto> serviciosAgregarSeleccionados;
	private CapituloProcedimientoDto capituloAgregar = new CapituloProcedimientoDto();
	private List<CapituloProcedimientoDto> capitulosAgregar;
	private List<CapituloProcedimientoDto> capitulosAgregarSeleccionados;
	protected Function<HashSet<ServicioSaludDto>, String> funcionListaCodigosPx = (l -> l
			.stream().map(ServicioSaludDto::getCodigo)
			.collect(Collectors.joining(" , " + System.lineSeparator())));
    private FuenteServicio fuenteServicio;

	@PostConstruct
	public void postconstruct() {
		try {
			this.fuenteServicio = FuenteServicio.MAESTROS;
			this.serviciosAgregar = new ArrayList<>();
			this.serviciosAgregarSeleccionados = new ArrayList<>();
			this.capitulosAgregar = new ArrayList<>();
			this.capitulosAgregarSeleccionados = new ArrayList<>();
			validarMostrarPoblacion();
			if (this.tecnologiasController.getNegociacion() != null) {
				if(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.equals(this.tecnologiasController.getNegociacion().getTipoModalidadNegociacion())) {
					if(this.tecnologiasController.getNegociacion().getEsRia() == Boolean.FALSE) {
						this.calcularValorTotal();
						this.consultarCapitulosNegociacion();
					}
				} else {
					consultarServiciosNegociacion();
				}
			}
		} catch (Exception e) {
			this.logger.error("Error al postConstruct NegociacionServiciosSSController",e);
			this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}

	@Trace(dispatcher = true)
	public void consultarServiciosNegociacion(){

		NewRelic.setTransactionName("NegociacionServiciosSSController","ConsultarServiciosNegociacion");

	    this.serviciosNegociacion = facade
                .consultarServiciosNegociacionNoSedesByNegociacionId(tecnologiasController
                        .getNegociacion());
        this.serviciosNegociacionOriginal = new ArrayList<>();
        this.serviciosNegociacionOriginal.addAll(serviciosNegociacion);
	}

	@Trace(dispatcher = true)
	public void consultarCapitulosNegociacion() throws ConexiaBusinessException{

		NewRelic.setTransactionName("NegociacionServiciosSSController","ConsultarCapitulosNegociacion");

	    this.capitulosNegociacion = facade
                .consultarCapitulosNegociacionNoSedesByNegociacionId(tecnologiasController
                        .getNegociacion());
        this.capitulosNegociacionOriginal = new ArrayList<>();
        this.capitulosNegociacionOriginal.addAll(capitulosNegociacion);
	}

	public void consultarTotalRias(){
		commonController.calcularTotalRiasByNegociacion(tecnologiasController.getNegociacion().getId());
		tecnologiasController.setListTotalesRias(commonController.consultarTotalRiasByNegociacion(tecnologiasController.getNegociacion().getId()));
	}

        public List<TipoTarifarioDto> getTipoTarifarioAsignacionMasiva(){
           List<TipoTarifarioDto> tipoTarifarios = commonController.getTiposTarifarios();
           tipoTarifarios.removeIf(f->f.getDescripcion().equals("TARIFA PROPIA"));
           return tipoTarifarios;
        }
        
	public void asignarTarifasServicios() {
		try {
			if (this.tipoAsignacionSeleccionado != null) {
				if (this.validarCamposTarifa()) {
					aplicarValorMasivo();
					Ajax.update("tecnologiasSSForm:tabsTecnologias:negociacionServiciosSS");
				}
			}
		} catch (ConexiaBusinessException e) {
			logger.error("Error asignarTarifasServicio", e);
			this.facesMessagesUtils.addError(this.exceptionUtils.createMessage(resourceBundle, e));
		} catch (Exception e) {
			this.logger.error("Error al asignarTarifasServicios", e);
			this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}

	protected boolean validarCamposTarifa() {
		boolean response = true;
		if (tipoAsignacionSeleccionado
				.equals(TipoAsignacionTarifaServicioEnum.TARIFARIO)) {
			if (this.tarifarioAsignar == null || this.porcentajeAsignar == null) {
				this.facesMessagesUtils.addError(resourceBundle
						.getString("negociacion_servicio_msj_val_tarifario"));
				response = false;
			}
		} else if (tipoAsignacionSeleccionado
				.equals(TipoAsignacionTarifaServicioEnum.INCREMENTO_PROPIA)) {
			if (this.porcentajeAumentoPropia == null) {
				this.facesMessagesUtils
						.addError(resourceBundle
								.getString("negociacion_servicio_msj_val_incremento_propia"));
				response = false;
			}
		} else if (tipoAsignacionSeleccionado
				.equals(TipoAsignacionTarifaServicioEnum.VALOR_TARIFA_PROPIA)) {
			if (this.valorTarifaPropia == null) {
				this.facesMessagesUtils
						.addError(resourceBundle
								.getString("negociacion_servicio_msj_val_valor_propia"));
				response = false;
			}
		}
		return response;
	}

	protected void aplicarValorMasivo() throws ConexiaBusinessException {
		if ((null != serviciosNegociacionSeleccionados)
				&& (serviciosNegociacionSeleccionados.size() > 0) ||
			(null != capitulosNegociacionSeleccionados)
				&& (capitulosNegociacionSeleccionados.size() > 0)
			) {

			String mensaje="";

			if(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.equals(tecnologiasController.getNegociacion().getTipoModalidadNegociacion())) {

				switch(this.tipoAsignacionSeleccionado) {
					case  APLICAR_REFERENTE:
						if(this.tecnologiasController.getNegociacion().getPoblacion() > 0) {
							for (CapitulosNegociacionDto dto : capitulosNegociacionSeleccionados) {
								asignarTarifarioNegociadoPGP(dto);
							}
							try {
								mensaje = this.facade.guardarPGP(
					                    this.tecnologiasController.getNegociacion().getId(),
					                    this.capitulosNegociacionSeleccionados,
					                    tipoAsignacionSeleccionado,
					                    false,
					                    this.tecnologiasController.getNegociacion().getPoblacion(),
					                    false,
					                    this.tecnologiasController.getUser().getId());
							    	 	facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_servicio_msj_guardar_ok"));

							    	 	if(mensaje.length() > 0){
							                facesMessagesUtils.addWarning(mensaje);
							            }
							            this.consultarCapitulosNegociacion();
							            this.capitulosNegociacionSeleccionados.clear();
							            if(tecnologiasController.getNegociacion().getEsRia() == Boolean.FALSE){
						 			    	this.calcularValorTotal();
						 			    	this.tipoAsignacionSeleccionado = null;
						 			    	Ajax.update("tecnologiasSSForm");
						 			    }
							} catch (ConexiaBusinessException e) {
								this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
								logger.error("Error al guardar estado de capítulos y/o consultando capítulos de la negociación", e);
							}

						} else {
							this.facesMessagesUtils.addWarning("Por favor asigne una población a la negociación");
						}
						break;
					case FRANJA_RIESGO:
						try {
							if(tecnologiasController.validarRangoFranjaRiesgo(franjaInicio, franjaFin)) {
								try {
									this.facade.guardarFranjaPGP(
						                    this.tecnologiasController.getNegociacion().getId(),
						                    this.capitulosNegociacionSeleccionados,
						                    franjaInicio,
						                    franjaFin,
						                    this.tecnologiasController.getUser().getId());
								    	 	facesMessagesUtils.addInfo("Se han aplicado los valores correctamente");
								            this.consultarCapitulosNegociacion();
								            this.capitulosNegociacionSeleccionados.clear();
								            this.franjaInicio=null;
								            this.franjaFin=null;
								            this.tipoAsignacionSeleccionado = null;
								            Ajax.update("tecnologiasSSForm");
								} catch (ConexiaBusinessException e) {
									this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
									logger.error("Error al guardar estado de capítulos y/o consultando capítulos de la negociación", e);
								}
							} else {
								this.facesMessagesUtils.addWarning("Por favor indique un rango de franja riesgo válido");
							}
						} catch (NullPointerException e) {
							this.facesMessagesUtils.addWarning("Por favor diligencie el rango de franja riesgo");
						}

						break;
					default:
						break;
				}


			} else {
				for (ServicioNegociacionDto dto : serviciosNegociacionSeleccionados) {
					asignarTarifarioNegociado(dto);
				}
                                Long negociacionReferenteId = this.negociacionFacade.obtenerNegociacionReferenteId(this.tecnologiasController.getNegociacion().getId());    
				mensaje = this.facade.guardar(this.tecnologiasController.getNegociacion().getId(),
                                                              this.serviciosNegociacionSeleccionados, tipoAsignacionSeleccionado, false,
                                                              this.tecnologiasController.getNegociacion().getPoblacion(), false,
                                                              this.tecnologiasController.getUser().getId(), negociacionReferenteId);
			    	 	facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_servicio_msj_guardar_ok"));

			    	 	if(mensaje.length() > 0){
			                facesMessagesUtils.addWarning(mensaje);
			            }
			            this.consultarServiciosNegociacion();

			            if(this.tecnologiasController.getNegociacion().getEsRia() == Boolean.TRUE){
			            	if (tipoAsignacionSeleccionado.equals(TipoAsignacionTarifaServicioEnum.APLICAR_REFERENTE)){
			                	this.consultarTotalRias();
			                	Ajax.update("tecnologiasSSForm");
			                }
			            }

			}

		} else {
			facesMessagesUtils.addInfo(resourceBundle
					.getString("negociacion_servicio_msj_val_sel"));
		}
	}

	private void asignarTarifarioNegociado(ServicioNegociacionDto dto) throws ConexiaBusinessException {
		dto.setIncrementoPropia(null);
		dto.setValorPropia(null);
		if (tipoAsignacionSeleccionado
				.equals(TipoAsignacionTarifaServicioEnum.CONTRATO_ANTERIOR)) {

			dto.setTarifarioNegociado(dto.getTarifarioContratoAnterior()
					.getId() != null ? dto.getTarifarioContratoAnterior() : dto
					.getTarifarioNegociado());
			dto.setPorcentajeNegociado(dto.getPorcentajeContratoAnterior() != null ? dto
					.getPorcentajeContratoAnterior() : dto
					.getPorcentajeNegociado());
			dto.setNegociado(true);

			if (dto.getTarifarioContratoAnterior().getId() != null && dto.getTarifarioContratoAnterior().getDescripcion().equals("TARIFA PROPIA")
                    && Objects.nonNull(dto.getValorContratoAnterior())) {
			    dto.setPorcentajeNegociado(new BigDecimal(0));
			    dto.setValorPropia(dto.getValorContratoAnterior().doubleValue());
            }

		} else if (tipoAsignacionSeleccionado
				.equals(TipoAsignacionTarifaServicioEnum.PORTAFOLIO_PROPUESTO)) {
			dto.setTarifarioNegociado(dto.getTarifarioPropuestoPortafolio()
					.getId() != null ? dto.getTarifarioPropuestoPortafolio()
					: dto.getTarifarioNegociado());
			dto.setPorcentajeNegociado(dto.getPorcentajePropuestoPortafolio() != null ? dto
					.getPorcentajePropuestoPortafolio() : dto
					.getPorcentajeNegociado());
			dto.setNegociado(true);
		} else if (tipoAsignacionSeleccionado
				.equals(TipoAsignacionTarifaServicioEnum.TARIFARIO)) {
			dto.setTarifarioNegociado(tarifarioAsignar != null ? tarifarioAsignar
					: dto.getTarifarioNegociado());
			dto.setPorcentajeNegociado(BigDecimal
					.valueOf(this.porcentajeAsignar));
			dto.setNegociado(true);
		} else if (tipoAsignacionSeleccionado
				.equals(TipoAsignacionTarifaServicioEnum.INCREMENTO_PROPIA)) {
			if (dto.getTarifarioNegociado() != null
					&& dto.getTarifarioNegociado().getDescripcion()
							.equals("TARIFA PROPIA")) {
				dto.setIncrementoPropia(this.porcentajeAumentoPropia);
				dto.setPorcentajeNegociado(BigDecimal.ZERO);
				dto.setNegociado(true);
			}
		} else if (tipoAsignacionSeleccionado
				.equals(TipoAsignacionTarifaServicioEnum.VALOR_TARIFA_PROPIA)) {
			if (this.commonController.getTiposTarifarios().stream()
					.filter(tt -> tt.getDescripcion().equals("TARIFA PROPIA"))
					.collect(Collectors.toList()).size() > 0) {
				TipoTarifarioDto tarifario = this.commonController
						.getTiposTarifarios()
						.stream()
						.filter(tt -> tt.getDescripcion().equals(
								"TARIFA PROPIA")).collect(Collectors.toList())
						.get(0);
				dto.setTarifarioNegociado(tarifario);
				dto.setValorPropia(this.valorTarifaPropia);
				dto.setPorcentajeNegociado(new BigDecimal(0));
				dto.setNegociado(true);
			}else {
				throw new ConexiaBusinessException(
						PreContractualMensajeErrorEnum.TARIFARIO_NO_ENCONTRADO);
			}
		} else if (tipoAsignacionSeleccionado.equals(TipoAsignacionTarifaServicioEnum.APLICAR_REFERENTE)) {
			dto.setCostoMedioUsuario(dto.getCostoMedioUsuarioReferente());
			dto.setValorNegociado(
					Objects.nonNull(dto.getCostoMedioUsuarioReferente()) ? dto.getCostoMedioUsuarioReferente()
					.multiply(BigDecimal.valueOf(dto.getFrecuenciaReferente())
					.multiply(BigDecimal.valueOf(tecnologiasController.getNegociacion().getPoblacion())))
					.setScale(0, BigDecimal.ROUND_HALF_UP): BigDecimal.ZERO);
			dto.setPorcentajeNegociado(new BigDecimal(0));
			dto.setNegociado(true);
		}
	}

	private void asignarTarifarioNegociadoPGP(CapitulosNegociacionDto dto) {
		if (tipoAsignacionSeleccionado.equals(TipoAsignacionTarifaServicioEnum.APLICAR_REFERENTE)) {

			if(Objects.nonNull(dto.getFrecuenciaReferente()) && Objects.nonNull(dto.getCostoMedioUsuarioReferente())) {
				dto.setFrecuenciaNegociado(dto.getFrecuenciaReferente());
				dto.setCostoMedioUsuario(dto.getCostoMedioUsuarioReferente());
				dto.setValorNegociado(
						Objects.nonNull(dto.getCostoMedioUsuario()) ? dto.getCostoMedioUsuario()
						.multiply(BigDecimal.valueOf(dto.getFrecuenciaNegociado())
						.multiply(BigDecimal.valueOf(tecnologiasController.getNegociacion().getPoblacion())))
						.setScale(0, BigDecimal.ROUND_HALF_UP): BigDecimal.ZERO);
				dto.setNegociado(true);
			}
		}
	}

	public void gestionarServicios(String nombreTabla, String nombreComboGestion) {
		if (this.gestionSeleccionada != null) {
			if (this.gestionSeleccionada
					.equals(GestionTecnologiasNegociacionEnum.BORRAR_SELECCIONADOS)) {
				if ((null != serviciosNegociacionSeleccionados)
						&& (serviciosNegociacionSeleccionados.size() > 0)) {
					RequestContext.getCurrentInstance().execute(
							"PF('cdDeleteServ').show();");
				} else {
					facesMessagesUtils.addInfo(resourceBundle
							.getString("negociacion_servicio_msj_val_sel"));
				}
			} else if (this.gestionSeleccionada
					.equals(GestionTecnologiasNegociacionEnum.SELECCIONAR_TODOS)) {
				serviciosNegociacionSeleccionados = new ArrayList<ServicioNegociacionDto>();
				serviciosNegociacionSeleccionados.addAll(serviciosNegociacion);
				Ajax.oncomplete("PF('" + nombreTabla + "').selectAllRows();");
			} else if (this.gestionSeleccionada
					.equals(GestionTecnologiasNegociacionEnum.DESELECCIONAR_TODOS)) {
				if(serviciosNegociacionSeleccionados.size() > 0) {
					serviciosNegociacionSeleccionados.clear();
				}

				Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRows();");
			}
			this.gestionSeleccionada = null;
			Ajax.update(nombreComboGestion);

		}
	}

	public void gestionarCapitulos(String nombreTabla, String nombreComboGestion) {
		if (this.gestionSeleccionada != null) {
			if (this.gestionSeleccionada
					.equals(GestionTecnologiasNegociacionEnum.BORRAR_SELECCIONADOS)) {
				if ((null != capitulosNegociacionSeleccionados)
						&& (capitulosNegociacionSeleccionados.size() > 0)) {
					RequestContext.getCurrentInstance().execute(
							"PF('cdDeleteServ').show();");
				} else {
					facesMessagesUtils.addInfo(resourceBundle
							.getString("negociacion_servicio_msj_val_sel"));
				}
			} else if (this.gestionSeleccionada
					.equals(GestionTecnologiasNegociacionEnum.SELECCIONAR_TODOS)) {
				capitulosNegociacionSeleccionados = new ArrayList<CapitulosNegociacionDto>();
				capitulosNegociacionSeleccionados.addAll(capitulosNegociacion);
				Ajax.oncomplete("PF('" + nombreTabla + "').selectAllRows();");
			} else if (this.gestionSeleccionada
					.equals(GestionTecnologiasNegociacionEnum.DESELECCIONAR_TODOS)) {
				capitulosNegociacionSeleccionados.clear();
				Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRows();");
			}
			this.gestionSeleccionada = null;
			Ajax.update(nombreComboGestion);

		}
	}

	public void calcularValorTotal(){
		try {
			if(tecnologiasController.getNegociacion().getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)) {
				this.tecnologiasController.getNegociacion().setValorTotal(this.negociacionFacade.sumValorTotalPGP(tecnologiasController.getNegociacion().getId()));
			} else {
				this.tecnologiasController.getNegociacion().setValorTotal(this.negociacionFacade.sumValorTotal(tecnologiasController.getNegociacion().getId()));
				this.negociacionFacade.actualizarValorTotal(tecnologiasController.getNegociacion());
			}


		} catch (Exception e) {
			 this.logger.error("Error al calcular valor total de la negociacion", e);
		}

	}

	public void eliminarServiciosMasivo() {
		if(tecnologiasController.getNegociacion().getEstadoLegalizacion() != EstadoLegalizacionEnum.LEGALIZADA) {
			try {
                if (((null != serviciosNegociacionSeleccionados)
                        && (serviciosNegociacionSeleccionados.size() > 0)) ||
                        ((null != capitulosNegociacionSeleccionados)
                        && (capitulosNegociacionSeleccionados.size() > 0))) {

                	if(tecnologiasController.getNegociacion().getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)) {
	                		List<Long> capitulosId = new ArrayList<>();

	                        this.capitulosNegociacionSeleccionados.forEach((c) -> capitulosId.add(c.getCapituloProcedimiento().getId()));

	                        capitulosId.addAll(this.capitulosNegociacionSeleccionados
	                                        .stream().map(c -> c.getCapituloProcedimiento().getId())
	                                        .collect(Collectors.toList()));

	                        //Elimina los procedimientos del capítulo y el capítulo de la negociación
	                        this.facade.eliminarByNegociacionAndCapitulosAllProcedimientos(
	                        		this.tecnologiasController.getNegociacion().getId(),
	                        		capitulosId,
	                        		this.tecnologiasController.getUser().getId());

	                        capitulosNegociacion = capitulosNegociacion.stream()
	                                .filter(c -> !capitulosId.contains(c.getCapituloProcedimiento().getId())).collect(Collectors.toList());
	                        capitulosNegociacionOriginal.clear();
	                        capitulosNegociacionOriginal.addAll(capitulosNegociacion);

	                        facesMessagesUtils.addInfo(resourceBundle
	                                        .getString("negociacion_capitulo_eliminacion_ok"));
	                        capitulosNegociacionSeleccionados.clear();

	                        this.calcularValorTotal();
	                        Ajax.update("tecnologiasSSForm");
                	} else {
                		List<Long> serviciosId = new ArrayList<>();

                        Set<Long> sedes = new HashSet<>();

                        this.serviciosNegociacionSeleccionados.forEach((s) -> {
                            serviciosId.add(s.getServicioSalud().getId());
                            sedes.add(s.getSedeNegociacionId());
                        });

                        serviciosId.addAll(this.serviciosNegociacionSeleccionados
                                        .stream().map(s -> s.getServicioSalud().getId())
                                        .collect(Collectors.toList()));
                        this.facade.eliminarByNegociacionAndServicios(
                                        tecnologiasController.getNegociacion().getId(),
                                        serviciosId,
                                        this.tecnologiasController.getUser().getId());
                        serviciosNegociacion = serviciosNegociacion.stream()
                                .filter(s -> !serviciosId.contains(s.getServicioSalud()
                                        .getId())).collect(Collectors.toList());
                        serviciosNegociacionOriginal.clear();
                        serviciosNegociacionOriginal.addAll(serviciosNegociacion);
                        facesMessagesUtils.addInfo(resourceBundle
                                        .getString("negociacion_servicio_eliminacion_ok"));
                        serviciosNegociacionSeleccionados.clear();
                	}

                    consultarTotalRias();
                    Ajax.update("tecnologiasSSForm:tabsTecnologias:negociacionServiciosSS");
                } else {
                        facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_servicio_msj_val_sel"));
                }
            } catch (Exception e) {
                this.logger.error("Error al eliminar de la negociación los paquetes seleccionados", e);
                this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
		}

	}

	public void filtroEspecial() {
		try {
			this.serviciosNegociacion.clear();
			this.serviciosNegociacion.addAll(serviciosNegociacionOriginal);
			FiltroEspecialEnum seleccionado = null;
			seleccionado = filtroEspecialSeleccionadoPorcentaje;
			if (seleccionado != null) {
				switch (seleccionado) {
				case ENTRE:
					this.filtrarEntre();
					break;
				case IGUAL:
					this.filtrarIgual();
					break;
				case MAYOR:
					this.filtrarMayor();
					break;
				case MENOR:
					this.filtrarMenor();
					break;
				}
			}
		} catch(ConexiaBusinessException e){
			logger.error("Error en filtro especial", e);
			facesMessagesUtils.addWarning(e.getParams()[0]);
		} catch (Exception e) {
			logger.error("Error en filtro especial", e);
			facesMessagesUtils.addError(exceptionUtils
					.createSystemErrorMessage(resourceBundle));
		}
	}

	public void filtrarEntre() throws ConexiaBusinessException {
		String[] filtros;
		filtros = this.filtroPorcentaje.replace("\\s", "").split(",");

		/*
		 * Se comprueba la valides del rango, de ser un rango valido se
		 * optiene los filtros para continuar con el filtrado de los
		 * registros
		 */
		BigDecimal[] rango = validarFiltroEntre(filtros);

		this.serviciosNegociacion = serviciosNegociacion
				.stream()
				.filter(m -> (m.getDiferenciaPorcentajeContratado() != null
						&& (new BigDecimal(m.getDiferenciaPorcentajeContratado())
							.setScale(2, RoundingMode.UP)
							.compareTo(rango[1]) <= 0)
						&& (new BigDecimal(m.getDiferenciaPorcentajeContratado())
							.setScale(2, RoundingMode.UP)
							.compareTo(rango[0]) >= 0))
					).collect(Collectors.toList());
	}

	BigDecimal[] validarFiltroEntre(String[] filtros) throws ConexiaBusinessException {

        Pattern patron = Pattern.compile("([\\d]+[,][\\d]+)");
        Matcher mat = patron.matcher(filtroPorcentaje);

        if(filtros.length < 2 || !mat.matches()){
            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.BUSINESS_ERROR,
                    "El valor del filtro debe contener dos valores separados por \",\"");
        }

        final BigDecimal[] rango =
                new BigDecimal[]{
                    new BigDecimal(filtros[0])
                        .setScale(2, RoundingMode.FLOOR),
                    new BigDecimal(filtros[1])
                        .setScale(2, RoundingMode.FLOOR)};


        if(rango[0].compareTo(rango[1]) > 0){
            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.BUSINESS_ERROR,
                    "El valor antes de \",\" debe ser menor al valor posterior.");
        }

        if(rango[0].compareTo(rango[1]) == 0){
            throw new ConexiaBusinessException(
                    PreContractualMensajeErrorEnum.BUSINESS_ERROR,
                    "Los valores del rango no pueden ser iguales.");
        }

        return rango;
    }

	public void filtrarIgual() {
		this.serviciosNegociacion = serviciosNegociacion
				.stream()
				.filter(m -> (m.getDiferenciaPorcentajeContratado() != null && m
						.getDiferenciaPorcentajeContratado() == Double
						.parseDouble(this.filtroPorcentaje)))
				.collect(Collectors.toList());
	}

	public void filtrarMayor() {

		this.serviciosNegociacion = serviciosNegociacion
				.stream()
				.filter(m -> (m.getDiferenciaPorcentajeContratado() != null && m
						.getDiferenciaPorcentajeContratado() > Double
						.parseDouble(filtroPorcentaje)))
				.collect(Collectors.toList());
	}

	public void filtrarMenor() {
		this.serviciosNegociacion = serviciosNegociacion
				.stream()
				.filter(m -> (m.getDiferenciaPorcentajeContratado() != null && m
						.getDiferenciaPorcentajeContratado() < Double
						.parseDouble(filtroPorcentaje)))
				.collect(Collectors.toList());
	}

	public void marcarDesmarcarNegociado(ServicioNegociacionDto dto) {
		if (dto.getPorcentajeNegociado() != null
				&& dto.getTarifarioNegociado() != null) {
			dto.setNegociado(true);

            this.validarGuardarAut(dto);
		} else {
			dto.setNegociado(false);
		}
	}

	public void cambiarTarifa(ServicioNegociacionDto dto) {
		marcarDesmarcarNegociado(dto);
		if (dto.getTarifarioNegociado() == null || dto.getTarifarioNegociado().getDescripcion()
				.equals("TARIFA PROPIA")) {
			dto.setPorcentajeNegociado(BigDecimal.ZERO);
		}
        this.validarGuardarAut(dto);

	}

    private void validarGuardarAut(ServicioNegociacionDto dto){
        if ((dto.getTarifarioNegociado() != null)){
            if (dto.getTarifarioNegociado().getDescripcion().equals("TARIFA PROPIA")) {
                dto.setPorcentajeNegociado(BigDecimal.ZERO);
            }
            if(dto.getPorcentajeNegociado()!= null){
                //Acciones de guardar
                try{
                    List<ServicioNegociacionDto> lsServicio = new ArrayList<ServicioNegociacionDto>();
                    lsServicio.add(dto);
                    Long negociacionReferenteId = this.negociacionFacade.obtenerNegociacionReferenteId(this.tecnologiasController.getNegociacion().getId());    
                    String mensaje = this.facade.guardar(
                    		this.tecnologiasController.getNegociacion().getId(),lsServicio, null, false,
                    		this.tecnologiasController.getNegociacion().getPoblacion(), false,
                    		this.tecnologiasController.getUser().getId(), negociacionReferenteId);
                    facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_servicio_msj_guardar_ok"));

                    if(mensaje.length() > 0){
                        facesMessagesUtils.addWarning(mensaje);
                    }
                }catch(Exception e){
                    logger.error("Error al intentar guardar automáticamente", e);
                    facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
                }
            }

        }
    }

	public void guardar(boolean agregarMensaje){
		try{
                    Long negociacionReferenteId = this.negociacionFacade.obtenerNegociacionReferenteId(this.tecnologiasController.getNegociacion().getId());    
                    String mensaje = this.facade.guardar(
                                    this.tecnologiasController.getNegociacion().getId(),
                                    this.serviciosNegociacionOriginal, tipoAsignacionSeleccionado, false,
                                    this.tecnologiasController.getNegociacion().getPoblacion(), false,
                                    this.tecnologiasController.getUser().getId(), negociacionReferenteId);
                    if(agregarMensaje){
                    facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_servicio_msj_guardar_ok"));

                    if(mensaje.length() > 0){
                            facesMessagesUtils.addWarning(mensaje);
                    }
                    this.consultarServiciosNegociacion();
                    }
		}catch(Exception e){
			logger.error("Error en ver detalle de procedimiento contrato anterior", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}

    public boolean validarServicios() {
        List<ServicioSaludDto> servicios = this.facade.validarServiciosNegociados(tecnologiasController.getNegociacion(), false);
        if (!servicios.isEmpty()) {
            this.facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_servicio_msj_validacion_no") + funcionListaTecnologiasSinNegociar.apply(servicios));
            this.consultarServiciosNegociacion();
            return false;
        } else {
            this.facade.actualizaTarifasServiciosPxNegociados(tecnologiasController.getNegociacion().getId(), this.tecnologiasController.getUser().getId());
        }
        return true;
    }

    public void verProcedimientosNegociados(ServicioNegociacionDto servicioNegociacion){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(NegociacionSessionEnum.SERVICIO_SALUD_NEGOCIACION.toString(), servicioNegociacion);
        session.setAttribute(NegociacionSessionEnum.PGP.toString(), false);

        this.facesUtils.urlRedirect("/negociacionProcedimiento");

    }

    protected void validarMostrarPoblacion() {
        NegociacionDto negociacion = this.tecnologiasController.getNegociacion();

        if ((negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.EVENTO)
                && Boolean.TRUE.equals(negociacion.getPoblacionServicio())
                && negociacion.getComplejidad() == ComplejidadNegociacionEnum.BAJA)) {
            showPoblacion = true;
        }
    }

    @Trace(dispatcher = true)
    public void importarServicios(FileUploadEvent event, String reader) {

    	NewRelic.setTransactionName("NegociacionServiciosSSController","importarServiciosANegociacion");

    	try {
    		UploadedFile file = event.getFile();
        	List<ProcedimientoNegociacionDto> portafolioServicios = new ArrayList<>();
        	ConcurrentHashMap<String, Object>  beans = new ConcurrentHashMap<>();
        	beans.put("tecnologiasOfertadas", portafolioServicios);
        	xlxsFiles.importar(file, reader, beans);
        	List<SedesNegociacionDto> listaSedes = facade.consultarSedeNegociacionServiciosByNegociacionId(this.tecnologiasController.getNegociacion().getId());

        	Integer sizeInicial = 0, sizeFinal = 0;
        	sizeInicial = portafolioServicios.size();

        	//Filtrar px para la modalidad evento que no cumplan con los requisitos de tarifario, %negociado y valor negociado establecidos
        	switch(tecnologiasController.getNegociacion().getTipoModalidadNegociacion()) {
        		case EVENTO:

        			portafolioServicios.removeIf(pto -> (
        					Objects.isNull(pto.getTarifarioPropuestoPortafolio()) ||
        					(pto.getTarifarioPropuestoPortafolio().equals("TARIFA PROPIA") &&
        					//Se revisa que el %negociado sea 0 y que el valorNegociado sea mayor a 0
        					(Objects.isNull(pto.getValorPropuestoPortafolio()) || Objects.isNull(pto.getPorcentajePropuestoPortafolio()) ||
        							(pto.getValorPropuestoPortafolio().compareTo(new BigDecimal("0")) == 0 ||
        							pto.getValorPropuestoPortafolio().compareTo(new BigDecimal("0")) == -1) &&
        							(pto.getPorcentajePropuestoPortafolio().compareTo(new BigDecimal("0")) == 1 ||
        							pto.getPorcentajePropuestoPortafolio().compareTo(new BigDecimal("0")) == -1)))  ||
        					//Si es diferente de tarifa propia se revisa que el %negociado sea diferente a cero
	        				(!pto.getTarifarioPropuestoPortafolio().equals("TARIFA PROPIA") &&
	        						(Objects.isNull(pto.getPorcentajePropuestoPortafolio()) ||
	        								pto.getPorcentajePropuestoPortafolio().compareTo(new BigDecimal("0")) == 0))));

        			break;
        		default:
        			break;
        	}

        	sizeFinal = portafolioServicios.size();

        	if(Objects.nonNull(portafolioServicios) && portafolioServicios.size() > 0) {
        		if(portafolioServicios.stream().filter(objServicios->objServicios.getProcedimientoDto().getServicioSalud().getCodigo()==null
            			||objServicios.getProcedimientoDto().getServicioSalud().getCodigo()=="").count()==0) {
    	        	facade.agregarGrupoServicioPrestador(this.tecnologiasController.getNegociacion().getPrestador().getId());
    	        	// Agrega los servicios a la negociacion
    	        	switch(tecnologiasController.getNegociacion().getTipoModalidadNegociacion()) {
    	        		case RIAS_CAPITA_GRUPO_ETAREO:
    						try {
    							facade.almacenarServiciosArchivoImportadoAddEmpresariales(
    		        					portafolioServicios,
    		        					listaSedes,
    		        					this.tecnologiasController.getUser().getId(),
    		        					this.tecnologiasController.getNegociacion().getId());
    						} catch (ConexiaBusinessException e) {
    							this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
    							logger.error("Error al almacenar los servicios para la negociación: " + tecnologiasController.getNegociacion().getId(), e);
    						}
    	        			break;
    	        		default:
    	        			facade.almacenarServiciosArchivoImportado(portafolioServicios, listaSedes, this.tecnologiasController.getUser().getId());
    	        			break;
    	        	}

    	        	portafolioServicios = serviciosFacade.asignarTarifariosSoportados(portafolioServicios);

    	        	// Agrega los procedimientos a la negociación
    	        	gestionarImportarXlxs.gestionarProcedimientosXlxs(portafolioServicios, listaSedes, tecnologiasController.getNegociacion().getComplejidad(),
    	        			this.tecnologiasController.getUser().getId(), tecnologiasController.getNegociacion().getTipoModalidadNegociacion());
    	        	List<String> serviciosNoAgregados = new ArrayList<>();
    	        	facesMessagesUtils.addInfo(resourceBundle.getString("form_label_archivo_importado_correctamente"));
    	        	serviciosNoAgregados = this.facade.serviciosRepsNegociacion(portafolioServicios,
    	        			this.tecnologiasController.getNegociacion().getId());

    	        	//Actualiza a negociado los servicios que tengan todos sus procedimientos negociados
    	        	this.facade.actualizarNegociadoServicios(tecnologiasController.getNegociacion().getId());

    	        	if (!serviciosNoAgregados.isEmpty()) {
    	        		facesMessagesUtils.addWarning("Estos servicios no están en el REPS " + serviciosNoAgregados.toString());
    	        	}
    	        	if(sizeInicial > sizeFinal) {
    	        		facesMessagesUtils.addWarning("Existen tecnologías con valores erróneos en el tarifario y/o porcentaje, valor negociado");
    	        	}
            	}else {
            		facesMessagesUtils.addWarning("Se encontraron registros sin el dato del código del servicio. Por favor verifique el archivo que está importando.");
            	}
            	postconstruct();
        	} else {
        		facesMessagesUtils.addWarning("El archivo contiene valores erróneos en el tarifario y/o porcentaje, valor negociado de las tecnologías, por favor verifique el archivo");
        	}


		} catch (IOException | ConexiaSystemException | SAXException | InvalidFormatException e) {
        	logger.error("Error general al importar el archivo", e);
            facesMessagesUtils.addError(resourceBundle.getString("form_label_error_archivo"));
        }
    }

	public void asignarPoblacionPorServicio(ServicioNegociacionDto servicioNegociacion) {
		try {
			facade.asignarPoblacionPorServicio(servicioNegociacion,
					tecnologiasController.getNegociacion());
		} catch (ConexiaBusinessException e) {
			facesMessagesUtils.addError(e.getMessage());
			logger.error(e.getMessage(), e);
		}
	}

	public void consultarServiciosAgregar() {
		try {
			this.serviciosAgregar.clear();
			this.serviciosAgregarSeleccionados.clear();
			if (!servicioAgregar.getCodigo() .isEmpty() || !servicioAgregar.getNombre().isEmpty()) {
				if (this.fuenteServicio == FuenteServicio.MAESTROS) {
					this.serviciosAgregar.addAll(this.facade.consultarServiciosMaestros(this.servicioAgregar, this.tecnologiasController.getNegociacion()));
				}
				if (serviciosAgregar.size() != 0) {
					RequestContext.getCurrentInstance().execute("PF('serviciosAgregarDlg').show();");
					Ajax.oncomplete("PF('serviciosAgregarTableWV').filter();");
				} else {
                    validarExistenciaServiciosMaestros();
                }
			} else {
				facesMessagesUtils.addWarning(resourceBundle.getString("negociacion_procedimiento_datos_entrada"));
			}
		} catch (ConexiaBusinessException e) {
			this.facesMessagesUtils.addWarning(this.exceptionUtils.createMessage(resourceBundle, e));
		} catch (Exception e) {
			logger.error("Error al consultarServiciosAgregar", e);
			facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}

    /**
     * Method to validate if exist code in ServiciosMaestros (reps, no_reps)
     * and to know that message to show
     * @throws ConexiaBusinessException
     */
    private void validarExistenciaServiciosMaestros() throws ConexiaBusinessException
    {
        Boolean existeRepsNoReps = this.facade.consultarExistenciaServicioMaestros(this.servicioAgregar);
        if (!existeRepsNoReps) {
            facesMessagesUtils.addWarning(resourceBundle.getString(KEY_PROPERTIES_NEGOCIATION_SERVICES_ADD_NO_FOUND));
            return ;
        }
        facesMessagesUtils.addWarning(resourceBundle.getString(KEY_PROPERTIES_SERVICE_MSG_VAL_SERVICE_NO_ENABLED));
    }

    public void agregarServiciosNegociacion(List<ServicioSaludDto> servicios) {
		try {
			if (servicios != null && servicios.size() > 0) {
				if (this.fuenteServicio == FuenteServicio.MAESTROS) {
                                    Long negociacionReferenteId = this.negociacionFacade.obtenerNegociacionReferenteId(this.tecnologiasController.getNegociacion().getId());
					this.facade.agregarServiciosNegociacionMaestros(
							servicios,
							this.tecnologiasController.getNegociacion(),
							this.tecnologiasController.getUser().getId(),
                                                        negociacionReferenteId
					);
				}
				consultarServiciosNegociacion();
				this.facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_servicio_agregar_ok") + this.funcionListaCodigosPx.apply(new HashSet<>(servicios)));
				RequestContext.getCurrentInstance().execute("PF('serviciosAgregarDlg').hide();");
			} else {
				facesMessagesUtils.addWarning(resourceBundle.getString("negociacion_servicio_agregar_seleccionar"));
			}
		} catch (Exception e) {
			logger.error("Error al agregar servicios", e);
			facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}

	public Long rowKeyServiciosAgregar(ServicioSaludDto servicio){
		return Long.parseLong(servicio.getId().toString()
				.concat(servicio.getSedeNegociacionId().toString()));
	}

	public void limpiarPanelAgregar(){
		this.servicioAgregar = new ServicioSaludDto();
	}

	public void importFiles(FileUploadEvent event) throws InvalidFormatException, IOException {

		if(tecnologiasController.getNegociacion().getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)){
			try {
				Long referenteAsignado = this.negociacionFacade.consultarReferenteNegociacion(tecnologiasController.getNegociacion().getId());
				if(Objects.nonNull(referenteAsignado) && referenteAsignado > 0) {
					typeImport = (ArchivosNegociacionEnum) event.getComponent().getAttributes().get("foo");
					String ext = FilenameUtils.getExtension(event.getFile().getFileName()).toLowerCase();
					try {
						Workbook libro = null;
						File file = null;
						file = File.createTempFile(event.getFile().getFileName(), ".xlsx");
						FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
						fos.write(event.getFile().getContents());
						fos.close();
						libro = WorkbookFactory.create(new FileInputStream(file));
						try {
							gestionarImportProcedimientosPGP(libro, typeImport);
						} catch (ConexiaBusinessException e) {
							logger.error("Error al consultar capítulos de la negociación", e);
						}

					} catch (IOException | ConexiaSystemException e) {
						logger.error("Error general al importar el archivo", e);
						facesMessagesUtils.addError(resourceBundle.getString("form_label_error_archivo"));
					}
				} else {
					this.facesMessagesUtils.addWarning("Por favor seleccione y guarde un referente para la negociación antes de importar procedimientos");
				}
			} catch (ConexiaBusinessException e) {
				logger.error("Error al consultar referente de la negociación: " + tecnologiasController.getNegociacion().getId(), e);
				this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
			}
		}
		else if(tecnologiasController.getNegociacion().getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.EVENTO)){
			typeImport = (ArchivosNegociacionEnum) event.getComponent().getAttributes().get("foo");
			File file = File.createTempFile(event.getFile().getFileName(), ".xlsx");
			FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
			fos.write(event.getFile().getContents());
			fos.close();
			Workbook libro = WorkbookFactory.create(new FileInputStream(file));
			gestionarImportarProcedimientosEvento(libro, typeImport);
			this.postconstruct();
			Ajax.update("tecnologiasSSForm:tabsTecnologias:erroresPxEvento");
		}
	}

	public void descargarFormatoTecnologiasPGP() {
		try {
			InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(Constantes.PATH_FORMATOS_REPORTES + NOMBRE_IMPORTAR_PROCEDIMIENTOS_PGP);
			this.formatoDownload = new DefaultStreamedContent(stream, "application/vnd.ms-excel", NOMBRE_IMPORTAR_PROCEDIMIENTOS_PGP);
		} catch (Exception ex) {
			this.facesMessagesUtils.addError("No se pudo descargar el formato de importar procedimientos para negociación PGP");
			logger.error("No se ha podido descargar el formato de importar procedimientos para negociación PGP", ex);
		}
	}

	public void descargarFormatoTecnologiasEvento(){
		try {
			if(this.tecnologiasController.getNegociacion().getOpcionImportarSeleccionada().equals(OpcionesImportacionTecnologiaEnum.IMPORTAR_SEDE_A_SEDE)){
				InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(Constantes.PATH_FORMATOS_REPORTES + NOMBRE_IMPORTAR_PROCEDIMIENTOS_SEDE_INDIVIDUAL);
				this.formatoDownload = new DefaultStreamedContent(stream, "application/vnd.ms-excel", NOMBRE_IMPORTAR_PROCEDIMIENTOS_SEDE_INDIVIDUAL);
			}
			else if(this.tecnologiasController.getNegociacion().getOpcionImportarSeleccionada().equals(OpcionesImportacionTecnologiaEnum.IMPORTAR_TODAS_LAS_SEDES)){
				InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(Constantes.PATH_FORMATOS_REPORTES + NOMBRE_IMPORTAR_PROCEDIMIENTOS_TODAS_SEDES);
				this.formatoDownload = new DefaultStreamedContent(stream, "application/vnd.ms-excel", NOMBRE_IMPORTAR_PROCEDIMIENTOS_TODAS_SEDES);
			}
		} catch (Exception ex) {
			this.facesMessagesUtils.addError("No se pudo descargar el formato de importar procedimientos para negociación Evento");
			logger.error("No se ha podido descargar el formato de importar procedimientos para negociación Evento", ex);
		}
	}

	private void gestionarImportProcedimientosPGP(Workbook libro, ArchivosNegociacionEnum typeImport) throws ConexiaBusinessException {
		DataFormatter formatter = new DataFormatter();
		List<ArchivoTecnologiasNegociacionPgpDto> procedimientosPgp = new ArrayList<>();
        List<ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum>> errores = new ArrayList<>();
		Sheet hoja = libro.getSheetAt(0);
		listadoErrores = new ArrayList<>();

        List<ErroresImportTecnologiasDto> listErrorFormat = validator.validateFormat(hoja, typeImport);
        int filaEncabezado = 0;

		if (listErrorFormat.isEmpty()) {
		    hoja.forEach(fila -> {
                if (!fila.getCell(fila.getFirstCellNum()).getStringCellValue().isEmpty() && fila.getRowNum() != filaEncabezado) {
                    ArchivoTecnologiasNegociacionPgpDto ptoPgp = new ArchivoTecnologiasNegociacionPgpDto();
                    ptoPgp.setCodigoTecnologiaUnicaEmssanar(formatter.formatCellValue(fila.getCell(0)));
                    ptoPgp.setDescripcionCodigoTecnologiaUnicaEmssanar(formatter.formatCellValue(fila.getCell(1)));
                    ptoPgp.setFrecuencia(fila.getCell(2).toString());
                    ptoPgp.setCmu(fila.getCell(3).toString());
                    ptoPgp.setFranjaInicio(fila.getCell(4).toString());
                    ptoPgp.setFranjaFin(fila.getCell(5).toString());
                    ptoPgp.setEliminarTecnologia(formatter.formatCellValue(fila.getCell(6)));
                    ptoPgp.setLineaArchivo(fila.getRowNum());
                    procedimientosPgp.add(ptoPgp);
                }
            });
			if (procedimientosPgp.isEmpty()) {
				facesMessagesUtils.addInfo("El archivo se encuentra vacio");
			} else {
				try {
					errores = facade.validarProcedimientoNegociacionPgp(procedimientosPgp, tecnologiasController.getNegociacion(), tecnologiasController.getUser().getId());
				} catch (ConexiaBusinessException e) {
					this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
					logger.error("Error al importar los procedimientos a la negociación: " + tecnologiasController.getNegociacion().getId(), e);
				}


				if(Objects.nonNull(errores) && !errores.isEmpty()){
					errores.forEach(errorLinea ->
									errorLinea.forEach((archivo, errorEnum) -> listadoErrores.add(
											new ErroresImportTecnologiasPgpDto(
													errorEnum.getCodigo(),
													errorEnum.getMensaje(),
													archivo.getLineaArchivo(),
													archivo.getCodigoTecnologiaUnicaEmssanar(),
													archivo.getDescripcionCodigoTecnologiaUnicaEmssanar(),
													archivo.getFrecuencia(),
													archivo.getCmu(),
													archivo.getFranjaInicio(),
													archivo.getFranjaFin(),
													archivo.getEliminarTecnologia()))));
					facesMessagesUtils.addInfo(
				            "El archivo importado se ha procesado con errores y ahora estos son visibles en el botón 'No procesados'.");
				} else {
					facesMessagesUtils.addInfo(
				            "El archivo importado se ha procesado sin errores, se han actualizado los procedimientos.");
				}
			}

			if(!listadoErrores.isEmpty()){
				RequestContext.getCurrentInstance().execute("PF('dlgProcedimientosNoProcesadosW').show()");
			}
			this.consultarCapitulosNegociacion();
			calcularValorTotal();

		} else {
			facesMessagesUtils.addInfo(
					"El archivo no cumple con el formato, verifique que el archivo corresponda a " + typeImport.getDescripcion());
		}

	}

	public void gestionarImportarProcedimientosEvento(Workbook libro, ArchivosNegociacionEnum typeImport) {
		this.listadoErroresEvento = new ArrayList<>();
		DataFormatter formatter = new DataFormatter();
		List<ArchivoTecnologiasNegociacionEventoDto> procedimientosEvento = new ArrayList<>();
        List<ErroresImportTecnologiasEventoDto> erroresImportacion = new ArrayList<>();
		boolean registrosRepetidos = false;

		Sheet hoja = libro.getSheetAt(0);
		List<ErroresImportTecnologiasDto> listErrorFormato = validator.validateFormat(hoja, typeImport);

		if (listErrorFormato.isEmpty()) {
			for (Row fila : hoja) {
				if (fila.getRowNum() == 0) {
					continue;
				}
				if (validator.validarCamposFila(fila, typeImport)) {
					if (this.tecnologiasController.getNegociacion().getOpcionImportarSeleccionada().equals(OpcionesImportacionTecnologiaEnum.IMPORTAR_SEDE_A_SEDE)) {
						ArchivoTecnologiasNegociacionEventoDto ptoEvento = new ArchivoTecnologiasNegociacionEventoDto();
						ptoEvento.setCodigoHabilitacionSede(formatter.formatCellValue(fila.getCell(0)));
						ptoEvento.setCodigoServicio(formatter.formatCellValue(fila.getCell(1)));
						ptoEvento.setCodigoEmssanar(formatter.formatCellValue(fila.getCell(4)));
						ptoEvento.setTarifarioNegociado(formatter.formatCellValue(fila.getCell(6)));
						ptoEvento.setPorcentajeNegociado(formatter.formatCellValue(fila.getCell(7)));
						ptoEvento.setValorNegociado(formatter.formatCellValue(fila.getCell(8)));
						ptoEvento.setLineaArchivo(fila.getRowNum() + 1);
						procedimientosEvento.add(ptoEvento);
					} else if (this.tecnologiasController.getNegociacion().getOpcionImportarSeleccionada().equals(OpcionesImportacionTecnologiaEnum.IMPORTAR_TODAS_LAS_SEDES)) {
						ArchivoTecnologiasNegociacionEventoDto ptoEvento = new ArchivoTecnologiasNegociacionEventoDto();
						ptoEvento.setCodigoServicio(formatter.formatCellValue(fila.getCell(0)));
						ptoEvento.setCodigoEmssanar(formatter.formatCellValue(fila.getCell(3)));
						ptoEvento.setTarifarioNegociado(formatter.formatCellValue(fila.getCell(5)));
						ptoEvento.setPorcentajeNegociado(formatter.formatCellValue(fila.getCell(6)));
						ptoEvento.setValorNegociado(formatter.formatCellValue(fila.getCell(7)));
						ptoEvento.setLineaArchivo(fila.getRowNum() + 1);
						procedimientosEvento.add(ptoEvento);
					}
				}
			}
			if (procedimientosEvento.isEmpty()) {
				this.facesMessagesUtils.addInfo(this.resourceBundle.getString("importacion_archivo_vacio_msn"));
			} else {
				try {
					registrosRepetidos =  validarRegistrosDuplicadosImportarPxEvento(this.tecnologiasController.getNegociacion(),procedimientosEvento);
					if(registrosRepetidos){
						this.facesMessagesUtils.addWarning(this.resourceBundle.getString("importacion_archivo_duplicado_msn"));
					}
					else{
                        erroresImportacion = facade.validarProcedimientoNegociacionEvento(procedimientosEvento, tecnologiasController.getNegociacion(), tecnologiasController.getUser()                                .getId());
                    }

				} catch (ConexiaBusinessException e) {
					this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
					logger.error("Error al importar los procedimientos a la negociación: " + tecnologiasController.getNegociacion().getId(), e);
				}
				if(!registrosRepetidos){
					if (Objects.nonNull(erroresImportacion) && !erroresImportacion.isEmpty()) {
                        listadoErroresEvento.addAll(erroresImportacion);
                        this.facesMessagesUtils.addInfo(this.resourceBundle.getString("importacion_con_errores_msn"));
					}
					else {
						this.facesMessagesUtils.addInfo(this.resourceBundle.getString("importacion_exitosa_msn"));
					}
				}
			}
			if(!listadoErroresEvento.isEmpty()){
				RequestContext.getCurrentInstance().execute("PF('dlgNoProcesadosEventoW').show()");
			}
			this.consultarServiciosNegociacion();
		}
		else {
			this.facesMessagesUtils.addInfo(this.resourceBundle.getString("importacion_archivo_no_formato_msn"));
		}
	}

	private boolean validarRegistrosDuplicadosImportarPxEvento(NegociacionDto negociacion,List<ArchivoTecnologiasNegociacionEventoDto> procedimientosEvento ){
		switch (negociacion.getOpcionImportarSeleccionada()){
			case IMPORTAR_SEDE_A_SEDE:
				Map<String, Long> agrupamientoProcedimientoSedeASede = procedimientosEvento.parallelStream().collect(
						Collectors.groupingBy(
								ArchivoTecnologiasNegociacionEventoDto::getCodigoEmssanarMasCodigoServicioYCodigoHabilitacion,
								Collectors.counting()
						)
				);
				return agrupamientoProcedimientoSedeASede.values().stream().anyMatch(cantidadProcedimiento -> cantidadProcedimiento > 1);
			case IMPORTAR_TODAS_LAS_SEDES:
				Map<String, Long> agrupamientoProcedimientoTodasSedes = procedimientosEvento.parallelStream().collect(
						Collectors.groupingBy(
								ArchivoTecnologiasNegociacionEventoDto::getCodigoEmssanarYCodigoServicio,
								Collectors.counting()
						)
				);
				return agrupamientoProcedimientoTodasSedes.values().stream().anyMatch(cantidadProcedimiento -> cantidadProcedimiento > 1);
		}
		return false;
	}

	//<editor-fold desc="Getters && Setters">
	public TipoAsignacionTarifaServicioEnum[] getTipoAsignacionTarifaServicioEnum() {

		return this.tecnologiasController.getNegociacion().getTipoModalidadNegociacion() == null
                ? TipoAsignacionTarifaServicioEnum.values()
                : TipoAsignacionTarifaServicioEnum.values(this.tecnologiasController.getNegociacion().getTipoModalidadNegociacion());

	}

	public GestionTecnologiasNegociacionEnum[] getGestionTecnologiasNegociacion() {
		return GestionTecnologiasNegociacionEnum.values();
	}

	public FiltroEspecialEnum[] getFiltroEspecialEnum() {
		return FiltroEspecialEnum.values();
	}

	public FiltroEspecialEnum getFiltroEspecialSeleccionadoPorcentaje() {
		return filtroEspecialSeleccionadoPorcentaje;
	}

	public void setFiltroEspecialSeleccionadoPorcentaje(FiltroEspecialEnum filtroEspecialSeleccionadoPorcentaje) {
		this.filtroEspecialSeleccionadoPorcentaje = filtroEspecialSeleccionadoPorcentaje;
	}

	public TipoAsignacionTarifaServicioEnum getTipoAsignacionSeleccionado() {
		return tipoAsignacionSeleccionado;
	}

	public void setTipoAsignacionSeleccionado(TipoAsignacionTarifaServicioEnum tipoAsignacionSeleccionado) {
		this.tipoAsignacionSeleccionado = tipoAsignacionSeleccionado;
	}

	public GestionTecnologiasNegociacionEnum getGestionSeleccionada() {
		return gestionSeleccionada;
	}

	public void setGestionSeleccionada(GestionTecnologiasNegociacionEnum gestionSeleccionada) {
		this.gestionSeleccionada = gestionSeleccionada;
	}

	public List<ServicioNegociacionDto> getServiciosNegociacion() {
		return serviciosNegociacion;
	}

	public void setServiciosNegociacion(List<ServicioNegociacionDto> serviciosNegociacion) {
		this.serviciosNegociacion = serviciosNegociacion;
	}

	public List<ServicioNegociacionDto> getServiciosNegociacionSeleccionados() {
		return serviciosNegociacionSeleccionados;
	}

	public void setServiciosNegociacionSeleccionados(List<ServicioNegociacionDto> serviciosNegociacionSeleccionados) {
		this.serviciosNegociacionSeleccionados = serviciosNegociacionSeleccionados;
	}

	public TiposDiferenciaNegociacionEnum getDiferenciaPorcentaje() {
		return diferenciaPorcentaje;
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

	public List<ServicioNegociacionDto> getServiciosNegociacionOriginal() {
		return serviciosNegociacionOriginal;
	}

	public void setServiciosNegociacionOriginal(List<ServicioNegociacionDto> serviciosNegociacionOriginal) {
		this.serviciosNegociacionOriginal = serviciosNegociacionOriginal;
	}

	public TipoTarifarioDto getTarifarioAsignar() {
		return tarifarioAsignar;
	}

	public void setTarifarioAsignar(TipoTarifarioDto tarifarioAsignar) {
		this.tarifarioAsignar = tarifarioAsignar;
	}

	public Double getPorcentajeAsignar() {
		return porcentajeAsignar;
	}

	public void setPorcentajeAsignar(Double porcentajeAsignar) {
		this.porcentajeAsignar = porcentajeAsignar;
	}

	public Double getPorcentajeAumentoPropia() {
		return porcentajeAumentoPropia;
	}

	public void setPorcentajeAumentoPropia(Double porcentajeAumentoPropia) {
		this.porcentajeAumentoPropia = porcentajeAumentoPropia;
	}

	public Double getValorTarifaPropia() {
		return valorTarifaPropia;
	}

	public void setValorTarifaPropia(Double valorTarifaPropia) {
		this.valorTarifaPropia = valorTarifaPropia;
	}

	public boolean isShowPoblacion() {
		return showPoblacion;
	}

	public void setShowPoblacion(boolean showPoblacion) {
		this.showPoblacion = showPoblacion;
	}

	public ServicioSaludDto getServicioAgregar() {
		return servicioAgregar;
	}

	public void setServicioAgregar(ServicioSaludDto servicioAgregar) {
		this.servicioAgregar = servicioAgregar;
	}

	public List<ServicioSaludDto> getServiciosAgregar() {
		return serviciosAgregar;
	}

	public void setServiciosAgregar(List<ServicioSaludDto> serviciosAgregar) {
		this.serviciosAgregar = serviciosAgregar;
	}

	public List<ServicioSaludDto> getServiciosAgregarSeleccionados() {
		return serviciosAgregarSeleccionados;
	}

	public void setServiciosAgregarSeleccionados(List<ServicioSaludDto> serviciosAgregarSeleccionados) {
		this.serviciosAgregarSeleccionados = serviciosAgregarSeleccionados;
	}

	public TecnologiasSSController getTecnologiasController() {
		return tecnologiasController;
	}

	public List<CapitulosNegociacionDto> getCapitulosNegociacion() {
		return capitulosNegociacion;
	}

	public void setCapitulosNegociacion(List<CapitulosNegociacionDto> capitulosNegociacion) {
		this.capitulosNegociacion = capitulosNegociacion;
	}

	public List<CapitulosNegociacionDto> getCapitulosNegociacionOriginal() {
		return capitulosNegociacionOriginal;
	}

	public void setCapitulosNegociacionOriginal(List<CapitulosNegociacionDto> capitulosNegociacionOriginal) {
		this.capitulosNegociacionOriginal = capitulosNegociacionOriginal;
	}

	public List<CapitulosNegociacionDto> getCapitulosNegociacionSeleccionados() {
		return capitulosNegociacionSeleccionados;
	}

	public void setCapitulosNegociacionSeleccionados(List<CapitulosNegociacionDto> capitulosNegociacionSeleccionados) {
		this.capitulosNegociacionSeleccionados = capitulosNegociacionSeleccionados;
	}

	public CapituloProcedimientoDto getCapituloAgregar() {
		return capituloAgregar;
	}

	public void setCapituloAgregar(CapituloProcedimientoDto capituloAgregar) {
		this.capituloAgregar = capituloAgregar;
	}

	public List<CapituloProcedimientoDto> getCapitulosAgregar() {
		return capitulosAgregar;
	}

	public void setCapitulosAgregar(List<CapituloProcedimientoDto> capitulosAgregar) {
		this.capitulosAgregar = capitulosAgregar;
	}

	public List<CapituloProcedimientoDto> getCapitulosAgregarSeleccionados() {
		return capitulosAgregarSeleccionados;
	}

	public void setCapitulosAgregarSeleccionados(List<CapituloProcedimientoDto> capitulosAgregarSeleccionados) {
		this.capitulosAgregarSeleccionados = capitulosAgregarSeleccionados;
	}

	public BigDecimal getFranjaInicio() {
		return franjaInicio;
	}

	public void setFranjaInicio(BigDecimal franjaInicio) {
		this.franjaInicio = franjaInicio;
	}

	public BigDecimal getFranjaFin() {
		return franjaFin;
	}

	public void setFranjaFin(BigDecimal franjaFin) {
		this.franjaFin = franjaFin;
	}

	public List<ErroresImportTecnologiasPgpDto> getListadoErrores() {
		return listadoErrores;
	}

	public void setListadoErrores(List<ErroresImportTecnologiasPgpDto> listadoErrores) {
		this.listadoErrores = listadoErrores;
	}

	public StreamedContent getFormatoDownload() {
		return formatoDownload;
	}

	public void setFormatoDownload(StreamedContent formatoDownload) {
		this.formatoDownload = formatoDownload;
	}

	public OpcionesImportacionTecnologiaEnum[] getOpcionesImportacionTecnologiaEnums() {
		return OpcionesImportacionTecnologiaEnum.values();
	}

	public List<ErroresImportTecnologiasEventoDto> getListadoErroresEvento() {
		return listadoErroresEvento;
	}

	public void setListadoErroresEvento(List<ErroresImportTecnologiasEventoDto> listadoErroresEvento) {
		this.listadoErroresEvento = listadoErroresEvento;
	}

	public FuenteServicio getFuenteServicio() {
		return fuenteServicio;
	}

	public void setFuenteServicio(FuenteServicio fuenteServicio) {
		this.fuenteServicio = fuenteServicio;
	}
	
    public boolean desactivarPorcNegciado(ServicioNegociacionDto dto) {
             return Objects.nonNull(dto.getTarifarioNegociado())&&dto.getTarifarioNegociado().getDescripcion().equals("TARIFA PROPIA");
    }
}
