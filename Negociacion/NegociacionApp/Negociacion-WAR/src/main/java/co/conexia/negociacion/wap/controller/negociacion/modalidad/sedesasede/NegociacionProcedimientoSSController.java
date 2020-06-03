package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import co.conexia.negociacion.wap.controller.common.CommonController;
import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionServicioProcedimientoSSFacade;
import com.conexia.contratacion.commons.constants.CommonConstants;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroNegociacionTecnologiaDto;
import com.conexia.contratacion.commons.dto.maestros.AbstractProcedimiento;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.TipoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.CapitulosNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ProcedimientoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ServicioNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.ToggleSelectEvent;
import org.primefaces.event.UnselectEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Controller que maneja los procedimientos
 *
 * @author mcastro
 *
 */
@Named
@ViewScoped
@URLMapping(id = "negociacionProcedimiento", pattern = "/negociacionProcedimiento", viewId = "/negociacion/modalidad/sedeasede/servicios/procedimientos/negociacionProcedimiento.page")
public class NegociacionProcedimientoSSController extends LazyDataModel<ProcedimientoNegociacionDto> {

	@Inject
	private Log logger;

	@Inject
	private FacesMessagesUtils facesMessagesUtils;

	@Inject
	private CommonController commonController;

	@Inject
	@CnxI18n
	transient ResourceBundle resourceBundle;

	@Inject
	private PreContractualExceptionUtils exceptionUtils;

	@Inject
	private FacesUtils facesUtils;

	@Inject
	private NegociacionServicioProcedimientoSSFacade negociacionProcedimientoFacade;

	@Inject
	private NegociacionServiciosSSController negociacionServicioController;

	@Inject
	private NegociacionTransportesSSController negociacionTrasladoController;
        
        @Inject
        private NegociacionFacade negociacionFacade;        

        @Inject
        private TecnologiasSSController tecnologiasController;

	private NegociacionDto negociacion;
	private ServicioNegociacionDto servicioSaludNegociacion;
	private CapitulosNegociacionDto capituloNegociacionPGP;
	private Double porcentajeValor = 0.0;
	private Double valorTarifaPropia = 0.0;
	private GestionTecnologiasNegociacionEnum gestionSeleccionada;
	private List<ProcedimientoNegociacionDto> procedimientosNegociacion;
	private List<ProcedimientoNegociacionDto> procedimientosNegociacionOriginal;
	private List<ProcedimientoNegociacionDto> procedimientosNegociacionSeleccionados;
	private TipoAsignacionTarifaProcedimientoEnum tipoAsignacionSeleccionado = TipoAsignacionTarifaProcedimientoEnum.NO_APLICA;
	private String filtroValor;
	private String filtroPorcentaje;
	private TipoTarifarioDto tarifarioAsignar;
	private FiltroEspecialEnum filtroEspecialSeleccionadoValor;
	private FiltroEspecialEnum filtroEspecialSeleccionadoPorcentaje;
	private TiposDiferenciaNegociacionEnum diferenciaValor = TiposDiferenciaNegociacionEnum.DIFERENCIA_VALOR;
	private TiposDiferenciaNegociacionEnum diferenciaPorcentaje = TiposDiferenciaNegociacionEnum.DIFERENCIA_PORCENTAJE;
	private boolean esServicioTransporte;
	private boolean aplicarPorNegociacion;
	private Double porcentajeAumentoPropia;
	private ProcedimientoDto procedimientoAgregar = new ProcedimientoDto();
	private List<ProcedimientoDto> procedimientosAgregar;
	private List<ProcedimientoDto> procedimientosAgregarSeleccionados;
	private Function<List<ProcedimientoDto>, String> funcionListaCodigosPx = (l -> l
			.stream().map(AbstractProcedimiento::getCodigoCliente)
			.collect(Collectors.joining(" , " + System.lineSeparator())));
	private FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia;
	private List<ProcedimientoNegociacionDto> listadoPaginado;
	private boolean eliminarProcedimientosPorNegociacion;
	private String tituloServicioCapitulo;
	private Boolean pgp = Boolean.FALSE;
	private BigDecimal franjaInicio;
	private BigDecimal franjaFin;

    @Inject
    @UserInfo
    private UserApp user;

	@PostConstruct
	public void init() {
		this.limpiarModal();
		this.setPageSize(1);
		this.setRowCount(1);
		procedimientosAgregar = new ArrayList<>();
		procedimientosAgregarSeleccionados = new ArrayList<>();
		filtroNegociacionTecnologia = new FiltroNegociacionTecnologiaDto();
		asignarValoresPorDefecto();
	}

	@Trace(dispatcher = true)
	public List<ProcedimientoNegociacionDto> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        NewRelic.setTransactionName("NegociacionProcedimientoSSController", "CargarProcedimientosNegociación");
        listadoPaginado = new ArrayList<>();
        if (negociacion != null) {
            if (negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)) { //Para consultar procedimientos por capítulo pgp
                if (this.capituloNegociacionPGP != null) {
                    try {
                        mapeoFiltro(filters);
                        asignarValoresPorDefecto();
                        filtroNegociacionTecnologia.setPagina(first);
                        filtroNegociacionTecnologia.setCantidadRegistros(pageSize);
                        filtroNegociacionTecnologia.setAscendente(sortOrder == SortOrder.ASCENDING);
                        filtroNegociacionTecnologia.setFiltros(filters);
                        this.setRowCount(negociacionProcedimientoFacade.contarProcedimientosNegociacionNoSedesByNegociacionAndCapitulo(filtroNegociacionTecnologia, negociacion));
                        listadoPaginado = negociacionProcedimientoFacade.consultarProcedimientosNegociacionNoSedesByNegociacionAndCapitulo(negociacion, filtroNegociacionTecnologia);
                        if (Objects.isNull(procedimientosNegociacion)) {
                            procedimientosNegociacion = new ArrayList<>();
                            procedimientosNegociacion.addAll(negociacionProcedimientoFacade
                                    .consultarProcedimientosNegociacionNoSedesByNegociacionAndCapitulo(negociacion, filtroNegociacionTecnologia.getNegociacionId(), filtroNegociacionTecnologia.getCapituloNegociacionId(), filtroNegociacionTecnologia));
                            this.procedimientosNegociacionOriginal = new ArrayList<>();
                            this.procedimientosNegociacionOriginal.addAll(procedimientosNegociacion);
                        } else {
                            procedimientosNegociacionSeleccionados = new ArrayList<>();
                            for (ProcedimientoNegociacionDto paginados : listadoPaginado) {
                                for (ProcedimientoNegociacionDto procedimientoNegociacion : procedimientosNegociacion) {
                                    if (procedimientoNegociacion.getProcedimientoDto().getId().compareTo(paginados.getProcedimientoDto().getId()) == 0 && procedimientoNegociacion.isSeleccionado()) {
                                        procedimientosNegociacionSeleccionados.add(procedimientoNegociacion);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        this.logger.error("Error al consultar los procedimientos para el capítulo: " + this.capituloNegociacionPGP.getCapituloProcedimiento().getId(), e);
                        this.facesMessagesUtils.addError(this.exceptionUtils.createSystemErrorMessage(resourceBundle));
                    }
                }
            } else { //Para consultar los procedimientos de los demás tipos de negociación
                if (this.servicioSaludNegociacion != null) {
                    try {
                        mapeoFiltro(filters);
                        asignarValoresPorDefecto();
                        filtroNegociacionTecnologia.setPagina(first);
                        filtroNegociacionTecnologia.setCantidadRegistros(pageSize);
                        filtroNegociacionTecnologia.setAscendente(sortOrder == SortOrder.ASCENDING);
                        filtroNegociacionTecnologia.setFiltros(filters);
                        this.setRowCount(negociacionProcedimientoFacade.contarProcedimientosNegociacionNoSedesByNegociacionAndServicio(filtroNegociacionTecnologia, negociacion));
                        listadoPaginado = negociacionProcedimientoFacade.consultarProcedimientosNegociacionNoSedesByNegociacionAndServicio(negociacion, filtroNegociacionTecnologia);
                        if (Objects.isNull(procedimientosNegociacion)) {
                            procedimientosNegociacion = new ArrayList<>();
                            procedimientosNegociacion.addAll(negociacionProcedimientoFacade.consultarProcedimientosNegociacionNoSedesByNegociacionAndServicio(negociacion, filtroNegociacionTecnologia.getNegociacionId(), filtroNegociacionTecnologia.getServicioNegociacionId(), filtroNegociacionTecnologia));
                            this.procedimientosNegociacionOriginal = new ArrayList<>();
                            this.procedimientosNegociacionOriginal.addAll(procedimientosNegociacion);
                        } else {
                            procedimientosNegociacionSeleccionados = new ArrayList<>();
                            for (ProcedimientoNegociacionDto paginados : listadoPaginado) {
                                for (ProcedimientoNegociacionDto procedimientoNegociacion : procedimientosNegociacion) {
                                    if (procedimientoNegociacion.getProcedimientoDto().getId().compareTo(paginados.getProcedimientoDto().getId()) == 0 && procedimientoNegociacion.isSeleccionado()) {
                                        procedimientosNegociacionSeleccionados.add(procedimientoNegociacion);
                                    }
                                }
                            }
                        }
                    } catch (Exception e) {
                        this.logger.error("Error al consultar los procedimientos para el servicio: " + this.servicioSaludNegociacion.getServicioSalud().getId(), e);
                        this.facesMessagesUtils.addError(this.exceptionUtils.createSystemErrorMessage(resourceBundle));
                    }
                }
            }
        }
        return listadoPaginado;
	}

	private void asignarValoresPorDefecto(){
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
		negociacion = (NegociacionDto) session.getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
		servicioSaludNegociacion = (ServicioNegociacionDto) session.getAttribute(NegociacionSessionEnum.SERVICIO_SALUD_NEGOCIACION.toString());
		capituloNegociacionPGP = (CapitulosNegociacionDto) session.getAttribute(NegociacionSessionEnum.CAPITULO_NEGOCIACION.toString());
		pgp = (Boolean) session.getAttribute(NegociacionSessionEnum.PGP.toString());
		if (negociacion != null) {
			filtroNegociacionTecnologia.setNegociacionId(negociacion.getId());
		}
		if (this.servicioSaludNegociacion != null) {
			filtroNegociacionTecnologia.setServicioNegociacionId(this.servicioSaludNegociacion.getServicioSalud().getId());
		}
		if (this.capituloNegociacionPGP != null) {
			filtroNegociacionTecnologia.setCapituloNegociacionId(this.capituloNegociacionPGP.getCapituloProcedimiento().getId());
		}
		if (this.pgp != null){
			filtroNegociacionTecnologia.setPgp(this.pgp);
		}else{
			pgp = false;
			filtroNegociacionTecnologia.setPgp(false);
		}
		onTituloServicioCapitulo();
	}

	private void mapeoFiltro(Map<String, Object> filters){
		if(Objects.nonNull(filters)){
			try {
				filtroNegociacionTecnologia = new FiltroNegociacionTecnologiaDto();
				for(Iterator<String> it = filters.keySet().iterator();it.hasNext();){
					String filterProperty = it.next();
					Object filterValue = filters.get(filterProperty);
					switch(filterProperty){
					case "procedimientoDto.cups":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().setCups((String)filterValue);
						break;
					case "procedimientoDto.codigoCliente":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().setCodigoCliente((String)filterValue);
						break;
					case "procedimientoDto.descripcion":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().setDescripcion((String)filterValue);
						break;
					case "procedimientoDto.complejidad":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().setComplejidad(new Integer((String)filterValue));
						break;
					case "procedimientoDto.categoriaPos":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getProcedimientoDto().setCategoriaPos((String)filterValue);
						break;
					case "tarifarioContratoAnterior":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().setTarifarioContratoAnterior((String)filterValue);
						break;
					case "porcentajeContratoAnterior":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().setPorcentajeContratoAnterior(new BigDecimal((String)filterValue));
						break;
					case "valorContratoAnterior":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().setValorContratoAnterior(new BigDecimal((String)filterValue));
						break;
					case "tarifarioPropuestoPortafolio":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().setTarifarioContratoAnterior((String)filterValue);
						break;
					case "porcentajePropuestoPortafolio":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().setPorcentajePropuestoPortafolio(new BigDecimal((String)filterValue));
						break;
					case "valorPropuestoPortafolio":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().setValorPropuestoPortafolio(new BigDecimal((String)filterValue));
						break;
					case "valorTarifaPlena":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().setValorTarifaPlena(new BigDecimal((String)filterValue));
						break;
					case "tarifarioNegociado.descripcion":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().getTarifarioNegociado().setDescripcion((String)filterValue);
						break;
					case "porcentajeNegociado":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().setPorcentajeNegociado(new BigDecimal((String)filterValue));
						break;
					case "valorNegociado":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().setValorNegociado(new BigDecimal((String)filterValue));
						break;
					case "frecuenciaReferente":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().setFrecuenciaReferente(new Double((String)filterValue));
						break;
					case "costoMedioUsuarioReferente":
						filtroNegociacionTecnologia.getProcedimientoNegociacionDto().setCostoMedioUsuarioReferente(new BigDecimal((String)filterValue));
						break;
					}
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			}
		}
	}

	public void verDetalleNegociado(ServicioNegociacionDto servicioNegociacion, boolean esServicioTransporte) {
		this.esServicioTransporte = esServicioTransporte;
		this.limpiarModal();
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext()
				.getSession(true);
		session.setAttribute(
				NegociacionSessionEnum.SERVICIO_SALUD_NEGOCIACION.toString(),
				servicioNegociacion);

		negociacion = (NegociacionDto) session
				.getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
		servicioSaludNegociacion = (ServicioNegociacionDto) session
				.getAttribute(NegociacionSessionEnum.SERVICIO_SALUD_NEGOCIACION
						.toString());
		if (negociacion != null && this.servicioSaludNegociacion != null) {
			// Se llama al método que llena la lista de procedimientos...
			consultarProcedimientosPorServicioNegociacion();
		}

		RequestContext context = RequestContext.getCurrentInstance();
		Ajax.oncomplete("PF('negociacionProcedimientosSSWidget').filter();");
		context.execute("PF('procedimientosDlg').show();");
	}

	public void verProcedimientosNegociados(ServicioNegociacionDto servicioNegociacion, boolean esServicioTransporte) {
		this.esServicioTransporte = esServicioTransporte;
		this.limpiarModal();
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext()
				.getSession(true);
		session.setAttribute(
				NegociacionSessionEnum.SERVICIO_SALUD_NEGOCIACION.toString(),
				servicioNegociacion);

		negociacion = (NegociacionDto) session
				.getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
		servicioSaludNegociacion = (ServicioNegociacionDto) session
				.getAttribute(NegociacionSessionEnum.SERVICIO_SALUD_NEGOCIACION
						.toString());
		session.setAttribute(NegociacionSessionEnum.PGP.toString(), false);
		if (negociacion != null && this.servicioSaludNegociacion != null) {
			// Se llama al método que llena la lista de procedimientos...
			consultarProcedimientosPorServicioNegociacion();
		}

		this.facesUtils.urlRedirect("/negociacionProcedimiento");
		// RequestContext context = RequestContext.getCurrentInstance();
		// Ajax.oncomplete("PF('negociacionProcedimientosSSWidget').filter();");
		// context.execute("PF('procedimientosDlg').show();");
	}

	@Trace(dispatcher = true)
	public void consultarProcedimientosPorServicioNegociacion() {

		NewRelic.setTransactionName("NegociacionProcedimientoSSController","ConsultarProcedimientosPorServicioNegociacion");

		// Agrega el procedimiento al listado Total de PRocedimientos
		if(Objects.nonNull(filtroNegociacionTecnologia)
				&& Objects.nonNull(filtroNegociacionTecnologia.getNegociacionId())
				&& Objects.nonNull(filtroNegociacionTecnologia.getServicioNegociacionId())) {
			procedimientosNegociacion = new ArrayList<ProcedimientoNegociacionDto>();
			procedimientosNegociacion.addAll(negociacionProcedimientoFacade
					.consultarProcedimientosNegociacionNoSedesByNegociacionAndServicio(negociacion,filtroNegociacionTecnologia.getNegociacionId(), filtroNegociacionTecnologia.getServicioNegociacionId(), filtroNegociacionTecnologia));
		}

	}

	public void onTituloServicioCapitulo() {
		tituloServicioCapitulo =
				this.pgp ?
				"Capítulo: "
				+this.capituloNegociacionPGP.getCapituloProcedimiento().getId()
				+"-"+this.capituloNegociacionPGP.getCapituloProcedimiento().getDescripcion()
				:
			    "Servicio: "
				+this.servicioSaludNegociacion.getServicioSalud().getCodigo()
				+"-"+this.servicioSaludNegociacion.getServicioSalud().getMacroservicio().getNombre()
				+"-"+this.servicioSaludNegociacion.getServicioSalud().getNombre();
	}

	public List<ProcedimientoNegociacionDto> getProcedimientosNegociacion() {
		return procedimientosNegociacion;
	}

	public void setProcedimientosNegociacion(List<ProcedimientoNegociacionDto> procedimientosNegociacion) {
		this.procedimientosNegociacion = procedimientosNegociacion;
	}

	public void asignarTarifasProcedimiento() throws ConexiaBusinessException {
		if (this.tipoAsignacionSeleccionado != null) {
			aplicarTarifas();
			deseleccionarTodos("negociacionProcedimientosSSWidget");
			Ajax.update("negociacionProcedimientoForm:negociacionProcedimientosSS");
		}
	}

	public void cerrarModal() {
		this.limpiarModal();
		if (esServicioTransporte) {
			this.negociacionTrasladoController.consultarServiciosNegociacion();
			Ajax.update("tecnologiasSSForm:tabsTecnologias:negociacionTrasladosSS");
		} else {
			this.negociacionServicioController.consultarServiciosNegociacion();
		}
		RequestContext context = RequestContext.getCurrentInstance();
		context.execute("PF('procedimientosDlg').hide();");
	}

	private void limpiarModal() {
		tipoAsignacionSeleccionado = TipoAsignacionTarifaProcedimientoEnum.NO_APLICA;
		tarifarioAsignar = null;
		porcentajeValor = 0.0;
		valorTarifaPropia = 0.0;
	}

    public boolean desactivarPorcNegciado(ProcedimientoNegociacionDto dto) {
             return Objects.nonNull(dto.getTarifarioNegociado())&&dto.getTarifarioNegociado().getDescripcion().equals("TARIFA PROPIA");
    }

	public void liquidarValorNegociadoByTarifario(ProcedimientoNegociacionDto dto) {

		int procedimientosSinAsignar = 0;

		try {

			if(Objects.nonNull(dto.getTarifarioNegociado()) && Objects.nonNull(dto.getPorcentajeNegociado())) {
				// Se asigna la tarifa masiva y se valida que se pueda
				// asignar...
				if (!this.negociacionProcedimientoFacade.asignarTarifarioMasivo(dto,  dto.getTarifarioNegociado(), dto.getPorcentajeNegociado().doubleValue())) {
					procedimientosSinAsignar++;
				}
				// Se verifica si queda con tarifa diferencial
				this.negociacionProcedimientoFacade.asignarVerificarTarifaDiferencial(dto,this.servicioSaludNegociacion.getTarifarioNegociado(), dto.getTarifarioNegociado());

				if (procedimientosSinAsignar == procedimientosNegociacion.size()) {
					facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_no_ok"));
				} else if (procedimientosSinAsignar > 0) {
					String mensajeWithErrors = resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_ok_with_errors");
					mensajeWithErrors = mensajeWithErrors.replaceAll("##1",	String.valueOf(1 - procedimientosSinAsignar));
					mensajeWithErrors = mensajeWithErrors.replaceAll("##2", String.valueOf(procedimientosSinAsignar));
					facesMessagesUtils.addInfo(mensajeWithErrors);
				} else {
					facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_ok"));
				}
				dto.setSeleccionado(true);
				guardarEstadoProcedimientoEvento(dto);
                                desactivarPorcNegciado(dto);
			}

		} catch (ConexiaBusinessException e) {
			facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
			logger.error("Error liquidando valor del procedimiento: " + dto.getProcedimientoDto().getCodigoCliente(), e);
		}
	}

	private void aplicarTarifas() throws ConexiaBusinessException {
		if (Objects.nonNull(procedimientosNegociacion)
				&& (procedimientosNegociacion.stream().filter(m->m.isSeleccionado()).count() > 0)) {
			// Asigna los procedimientos Seleccionados (seleccionado = true)
			//procedimientosNegociacionSeleccionados = procedimientosNegociacion;
			// Contador para los procedimientos que no se puede aplicar la
			// tarifa.
			int procedimientosSinAsignar = 0;
			// Valido que se den los requerimientos mínimos para asignar valor
			if ((tipoAsignacionSeleccionado.equals(TipoAsignacionTarifaProcedimientoEnum.TARIFARIO))
					&& (tarifarioAsignar != null) && (porcentajeValor != null)) {
				int procedimientosSeleccionados = 0;
				for (ProcedimientoNegociacionDto dto :  procedimientosNegociacion) {
					if(dto.isSeleccionado()){
						procedimientosSeleccionados++;
						// Se verifica si se desea replicar la tarifa
						dto.setReplicarOtros(this.aplicarPorNegociacion);

						// Se asigna la tarifa masiva y se valida que se pueda
						// asignar...
						if (!this.negociacionProcedimientoFacade.asignarTarifarioMasivo(dto, this.tarifarioAsignar,this.porcentajeValor)) {
							procedimientosSinAsignar++;
						}
						// Se verifica si queda con tarifa diferencial
						this.negociacionProcedimientoFacade.asignarVerificarTarifaDiferencial(dto,this.servicioSaludNegociacion.getTarifarioNegociado(), tarifarioAsignar);
					}
				}
				if (procedimientosSinAsignar == procedimientosNegociacion.size()) {
					facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_no_ok"));
				} else if (procedimientosSinAsignar > 0) {
					String mensajeWithErrors = resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_ok_with_errors");
					mensajeWithErrors = mensajeWithErrors.replaceAll("##1",	String.valueOf(procedimientosSeleccionados - procedimientosSinAsignar));
					mensajeWithErrors = mensajeWithErrors.replaceAll("##2", String.valueOf(procedimientosSinAsignar));
					facesMessagesUtils.addInfo(mensajeWithErrors);
				} else {
					facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_ok"));
				}
				guardarEstadoProcedimientos();
			} else if ((tipoAsignacionSeleccionado.equals(TipoAsignacionTarifaProcedimientoEnum.VALOR_TARIFA_PROPIA))
					&& (valorTarifaPropia != null)) {

				procedimientosNegociacion.stream().parallel()
					.filter((dto) -> (dto.isSeleccionado())).forEach((dto) ->
						{	try {
								// Se verifica si se desea replicar la tarifa
								dto.setReplicarOtros(this.aplicarPorNegociacion);
								asignarTarifaPropiaMasiva(dto);
							} catch (Exception e) {
								logger.error("Error al aplicar tarifa propia a los procedimientos.", e);
								facesMessagesUtils.addError(resourceBundle.getString("system_error"));
							}
						}
					);

				facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_ok"));
				guardarEstadoProcedimientos();
			}
			else if((tipoAsignacionSeleccionado.equals(TipoAsignacionTarifaProcedimientoEnum.INCREMENTO_TARIFA))){
				long procedimientoSeleccionados = procedimientosNegociacion.stream().filter(dto -> dto.isSeleccionado()).count();
				for (ProcedimientoNegociacionDto dto : procedimientosNegociacion) {
					if(dto.isSeleccionado()){
						if (Objects.nonNull(dto.getTarifarioNegociado()) && Objects.nonNull(dto.getValorNegociado()) &&
								dto.getTarifarioNegociado().getDescripcion().equals(CommonConstants.DESC_TARIFARIO_NO_NORMATIVO)) {
							dto.setPorcentajeNegociado(BigDecimal.ZERO);
							dto.setValorNegociado((dto.getValorNegociado()
									.multiply(new BigDecimal(porcentajeAumentoPropia).divide(BigDecimal.valueOf(100),MathContext.DECIMAL64), MathContext.DECIMAL64)
									.add(dto.getValorNegociado()).setScale(0, BigDecimal.ROUND_HALF_UP)).setScale(-2, BigDecimal.ROUND_HALF_UP));
						}else{
							procedimientosSinAsignar+=1;
						}
					}
				}
				if (procedimientosSinAsignar == procedimientoSeleccionados) {
					facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_no_ok"));
				}else{
					if(procedimientosSinAsignar > 0){
						String mensajeWithErrors = resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_ok_with_errors");
						mensajeWithErrors = mensajeWithErrors.replaceAll("##1",	String.valueOf(procedimientoSeleccionados - procedimientosSinAsignar));
						mensajeWithErrors = mensajeWithErrors.replaceAll("##2", String.valueOf(procedimientosSinAsignar));
						facesMessagesUtils.addInfo(mensajeWithErrors);
					}else{
						facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_ok"));
					}
					guardarEstadoProcedimientos();
				}
			}
			else if ((tipoAsignacionSeleccionado.equals(TipoAsignacionTarifaProcedimientoEnum.CONTRATO_ANTERIOR))){
				int procedimientosSeleccionados = 0;
				for (ProcedimientoNegociacionDto dto :  procedimientosNegociacion) {
					if(dto.isSeleccionado()){
						procedimientosSeleccionados++;
						// Se verifica si se desea replicar la tarifa
						dto.setReplicarOtros(this.aplicarPorNegociacion);

						// Se asigna la tarifa masiva y se valida que se pueda
						// asignar...
						if (!this.negociacionProcedimientoFacade.asignarValoresContratoAnterioProcedimiento(dto)) {
							procedimientosSinAsignar++;
						}
						// Se verifica si queda con tarifa diferencial
						this.negociacionProcedimientoFacade.asignarVerificarTarifaDiferencial(dto,this.servicioSaludNegociacion.getTarifarioNegociado(), tarifarioAsignar);
					}
				}
				if (procedimientosSinAsignar == procedimientosNegociacion.size()) {
					facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_no_ok"));
				} else if (procedimientosSinAsignar > 0) {
					String mensajeWithErrors = resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_ok_with_errors");
					mensajeWithErrors = mensajeWithErrors.replaceAll("##1",	String.valueOf(procedimientosSeleccionados - procedimientosSinAsignar));
					mensajeWithErrors = mensajeWithErrors.replaceAll("##2", String.valueOf(procedimientosSinAsignar));
					facesMessagesUtils.addInfo(mensajeWithErrors);
				} else {
					facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_ok"));
				}
				guardarEstadoProcedimientos();
			}
			else if ((tipoAsignacionSeleccionado.equals(TipoAsignacionTarifaProcedimientoEnum.PORTAFOLIO_PROPUESTO))){
				procedimientosNegociacion.stream().filter((dto) ->
					dto.isSeleccionado() && Objects.nonNull(dto.getTarifarioPropuestoPortafolio()))
				.forEach((dto)->{
					TipoTarifarioDto tarifario = this.commonController.getTiposTarifarios().stream()
							.filter(tt -> tt.getDescripcion().equals(dto.getTarifarioPropuestoPortafolio()))
							.collect(Collectors.toList()).get(0);
					dto.setReplicarOtros(this.aplicarPorNegociacion);
					dto.setValorNegociado(dto.getValorPropuestoPortafolio());

					if(CommonConstants.COD_TARIFARIO_NO_NORMATIVO.equals(tarifario.getCodigo()) &&
							Objects.nonNull(dto.getValorPropuestoPortafolio())){
						if(dto.getProcedimientoDto().getTipoProcedimiento()==TipoProcedimientoEnum.PROCEDIMIENTO.getId().intValue()){
							dto.setValorNegociado(dto.getValorPropuestoPortafolio().setScale(-1, BigDecimal.ROUND_HALF_UP));
						}else{
							dto.setValorNegociado(dto.getValorPropuestoPortafolio().setScale(-2, BigDecimal.ROUND_HALF_UP));
						}
					}else{
						dto.setValorNegociado(dto.getValorPropuestoPortafolio());
					}

					dto.setPorcentajeNegociado(Objects.nonNull(dto.getPorcentajePropuestoPortafolio()) ?
								dto.getPorcentajePropuestoPortafolio() : BigDecimal.ZERO);
					dto.setTarifarioNegociado(tarifario);
				    dto.setNegociado(true);
				});
				facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_ok"));
				guardarEstadoProcedimientos();
			}else if(tipoAsignacionSeleccionado.equals(TipoAsignacionTarifaProcedimientoEnum.APLICAR_REFERENTE)){

				//Se valida que exista población asignada a la negociación
				if(this.negociacion.getPoblacion() > 0) {
					procedimientosNegociacion.stream().filter((dto) ->
					dto.isSeleccionado() &&
						(Objects.nonNull(Objects.nonNull(dto.getCostoMedioUsuarioReferente())))
						 && Objects.nonNull(dto.getFrecuenciaReferente()))
						.forEach((dto)->{
							dto.setFrecuenciaUsuario(dto.getFrecuenciaReferente());
							dto.setCostoMedioUsuario(dto.getCostoMedioUsuarioReferente());
							dto.setValorNegociado(this.tecnologiasController.calcularValorNegociadoPGP(
									dto.getCostoMedioUsuario()
									,BigDecimal.valueOf(dto.getFrecuenciaUsuario())
									,BigDecimal.valueOf(this.negociacion.getPoblacion())));
							dto.setPorcentajeNegociado(BigDecimal.ZERO);
						    dto.setNegociado(true);
						});
					facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_ok"));
					guardarEstadoProcedimientosPGP();
					this.tipoAsignacionSeleccionado = null;

					if(pgp) {
						negociacionServicioController.calcularValorTotal();
					} else {
						this.commonController.calcularTotalRiasByNegociacionPGP(this.negociacion.getId());
					}
				} else {
					this.facesMessagesUtils.addWarning("Por favor asigne una población a la negociación");
				}

			} else if(tipoAsignacionSeleccionado.equals(TipoAsignacionTarifaProcedimientoEnum.FRANJA_RIESGO)) {
				if(tecnologiasController.validarRangoFranjaRiesgo(franjaInicio, franjaFin)) {

					List<Long> procedimientoIds = new ArrayList<Long>();

					procedimientosNegociacion.stream().filter((dto) ->
					dto.isSeleccionado())
						.forEach((dto)->{
							procedimientoIds.add(dto.getProcedimientoDto().getId());
						});

					if(Objects.nonNull(procedimientoIds) && procedimientoIds.size() > 0) {
						negociacionProcedimientoFacade.guardarProcedimientosFranjaPGP(
								this.negociacion.getId(),
								procedimientoIds,
								this.capituloNegociacionPGP.getCapituloProcedimiento().getId(),
								franjaInicio,
								franjaFin,
								user.getId());

						this.franjaInicio=null;
			            this.franjaFin=null;
			            this.tipoAsignacionSeleccionado = null;

					}


				} else {
					this.facesMessagesUtils.addWarning("Por favor indique un rango de franja riesgo válido");
				}
			} else{
				facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_val_sel_tipo"));
			}
		}else {
			facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_val_sel"));
		}
	}

	private void asignarTarifaPropiaMasiva(ProcedimientoNegociacionDto dto) throws ConexiaBusinessException {
		dto.setValorNegociado(null);
		if (this.commonController.getTiposTarifarios().stream()
				.filter(tt -> tt.getDescripcion().equals("TARIFA PROPIA"))
				.collect(Collectors.toList()).size() > 0) {
			TipoTarifarioDto tarifario = this.commonController.getTiposTarifarios().stream()
					.filter(tt -> tt.getDescripcion().equals("TARIFA PROPIA"))
					.collect(Collectors.toList()).get(0);
			dto.setTarifarioNegociado(tarifario);
			if(dto.getProcedimientoDto().getTipoProcedimiento()==TipoProcedimientoEnum.PROCEDIMIENTO.getId().intValue()){
				dto.setValorNegociado(BigDecimal.valueOf(this.valorTarifaPropia).setScale(-2, BigDecimal.ROUND_HALF_UP));
			}else{
				dto.setValorNegociado(BigDecimal.valueOf(this.valorTarifaPropia).setScale(-2, BigDecimal.ROUND_HALF_UP));
			}
			dto.setPorcentajeNegociado(BigDecimal.ZERO);
			dto.setNegociado(true);
		} else {
			throw new ConexiaBusinessException(
					PreContractualMensajeErrorEnum.TARIFARIO_NO_ENCONTRADO);
		}
	}

	public void guardarEstadoProcedimientos() {
		try {
			if (this.negociacion != null && this.servicioSaludNegociacion != null) {
                            Long negociacionReferenteId = this.negociacionFacade.obtenerNegociacionReferenteId(this.tecnologiasController.getNegociacion().getId());    
                            negociacionProcedimientoFacade.guardarProcedimientosNegociados(procedimientosNegociacion,
                                                this.negociacion.getId(), this.servicioSaludNegociacion.getServicioSalud().getId(),
                                                servicioSaludNegociacion.getTarifarioNegociado(), this.user.getId(), negociacionReferenteId, tipoAsignacionSeleccionado);
                            
				
				facesMessagesUtils.addInfo("Operación realizada con éxito!");
			} else {
				this.facesUtils.urlRedirect("/bandejaPrestador");
			}

		} catch (Exception e) {
			logger.error("Error al guardar el estado de los procedimientos en negociación.", e);
			facesMessagesUtils.addError(resourceBundle.getString("system_error"));
		}
	}

	public void guardarEstadoProcedimientoEvento(ProcedimientoNegociacionDto dto) {
		try {
			if (this.negociacion != null && this.servicioSaludNegociacion != null) {
				List<ProcedimientoNegociacionDto> pxNegociacion = new ArrayList<>();
				pxNegociacion.add(dto);
				negociacionProcedimientoFacade.guardarProcedimientosNegociados(pxNegociacion,
						this.negociacion.getId(), this.servicioSaludNegociacion.getServicioSalud().getId(),
						servicioSaludNegociacion.getTarifarioNegociado(), this.user.getId(), null, tipoAsignacionSeleccionado);
				facesMessagesUtils.addInfo("Operación realizada con éxito!");
			} else {
				this.facesUtils.urlRedirect("/bandejaPrestador");
			}

		} catch (Exception e) {
			logger.error("Error al guardar el estado del procedimiento" + dto.getProcedimientoDto().getCodigoCliente() + " en negociación.", e);
			facesMessagesUtils.addError(resourceBundle.getString("system_error"));
		}
	}

	public void guardarEstadoProcedimientosPGP() {
		try {
			if (this.negociacion != null && this.capituloNegociacionPGP != null) {
				negociacionProcedimientoFacade.guardarProcedimientosNegociadosPGP(procedimientosNegociacion,
						this.negociacion.getId(), this.capituloNegociacionPGP.getCapituloProcedimiento().getId(), this.user.getId());
				this.procedimientosNegociacion.stream().forEach(
							procedimiento -> {
								procedimiento.setSeleccionado(false);
							}
						);
				facesMessagesUtils.addInfo("Operación realizada con éxito!");
			} else {
				this.facesUtils.urlRedirect("/bandejaPrestador");
			}

		} catch (ConexiaBusinessException e) {
			logger.error("Error al guardar el estado de los procedimientos en negociación.", e);
			facesMessagesUtils.addError(resourceBundle.getString("system_error"));
		}
	}

	public void actualizarValorNegociadoPorPoblacion(){
		procedimientosNegociacion.stream().filter((dto) ->
			(Objects.nonNull(Objects.nonNull(dto.getCostoMedioUsuario())))
			 && Objects.nonNull(dto.getFrecuenciaReferente()))
			.forEach((dto)->{
				dto.setCostoMedioUsuario(dto.getCostoMedioUsuario());
				dto.setValorNegociado(dto.getCostoMedioUsuario()
						.multiply(BigDecimal.valueOf(dto.getFrecuenciaReferente())
						.multiply(BigDecimal.valueOf(this.negociacion.getPoblacion()))));
				dto.setPorcentajeNegociado(BigDecimal.ZERO);
			    dto.setNegociado(true);
			});
		facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_actualizacion_ok"));
		guardarEstadoProcedimientosPGP();
		this.commonController.calcularTotalRiasByNegociacion(this.negociacion.getId());

	}

    public void guardarValorProcedimiento(ProcedimientoNegociacionDto procedimientoDto) {
    	if (this.negociacion != null && (this.servicioSaludNegociacion != null || this.capituloNegociacionPGP != null)) {
    		procedimientoDto.setSeleccionado(true);
    		procedimientoDto.setValorNegociado(this.tecnologiasController.calcularValorNegociadoPGP(
    				procedimientoDto.getCostoMedioUsuario()
					,BigDecimal.valueOf(procedimientoDto.getFrecuenciaUsuario())
					,BigDecimal.valueOf(this.negociacion.getPoblacion())));
    		procedimientoDto.setNegociado(Objects.nonNull(procedimientoDto.getValorNegociado()) ? true : false);
    		List<ProcedimientoNegociacionDto> listTemporal = new ArrayList<ProcedimientoNegociacionDto>();
    		listTemporal.add(procedimientoDto);

    		if(pgp) {
    			try {
					negociacionProcedimientoFacade.guardarProcedimientosNegociadosPGP(listTemporal, this.negociacion.getId(),
							this.capituloNegociacionPGP.getCapituloProcedimiento().getId(), this.user.getId());
				} catch (ConexiaBusinessException e) {
					facesMessagesUtils.addError(e.getParams()[0]);
				}
    		} else {
    			negociacionProcedimientoFacade.guardarProcedimientosNegociados(listTemporal, this.negociacion.getId(),
    					this.servicioSaludNegociacion.getServicioSalud().getId(), this.user.getId());
    		}

    		negociacionServicioController.calcularValorTotal();
			facesMessagesUtils.addInfo("Operación realizada con éxito!");
		}
    }

    public void guardarFranjaProcedimiento(ProcedimientoNegociacionDto procedimientoDto, Integer opt) {
    	if (Objects.nonNull(negociacion) && Objects.nonNull(capituloNegociacionPGP)) {

    		if(tecnologiasController.validarRangoFranjaRiesgo(procedimientoDto.getFranjaInicio(), procedimientoDto.getFranjaFin())) {
    			aplicarFranja(procedimientoDto);
    		} else {
    			try {
    				BigDecimal valor = tecnologiasController.validarRangoFranjaByCampo(opt, procedimientoDto.getFranjaInicio(), procedimientoDto.getFranjaFin());

    				if(valor.compareTo(BigDecimal.ZERO) == 1) {
        				//Si la validación arroja un valor diferente de cero se asigna al campo contrario al campo de origen el valor
    					//arrojado por la función de validación
        				switch(opt) {
        					case 1:
        						procedimientoDto.setFranjaFin(valor);
        						break;
        					case 2:
        						procedimientoDto.setFranjaInicio(valor);
        						break;
        					default:
        						break;
        				}

        				aplicarFranja(procedimientoDto);

        			} else {
        				facesMessagesUtils.addWarning("Por favor asigne un rango de franja riesgo válido");
        			}
				} catch (NullPointerException e) {
					logger.error("Error al validar la aplicación de franja riesgo uno a uno en procedimiento: "+procedimientoDto.getProcedimientoDto().getId(), e);
				}

    		}

		}
    }

    private void aplicarFranja(ProcedimientoNegociacionDto procedimientoDto) {
    	procedimientoDto.setSeleccionado(true);
		List<Long> listTemporal = new ArrayList<Long>();
		listTemporal.add(procedimientoDto.getProcedimientoDto().getId());
    	try {
			negociacionProcedimientoFacade.guardarProcedimientosFranjaPGP(
					negociacion.getId(),
					listTemporal,
					capituloNegociacionPGP.getCapituloProcedimiento().getId(),
					procedimientoDto.getFranjaInicio(),
					procedimientoDto.getFranjaFin(),
					user.getId());
		} catch (ConexiaBusinessException e) {
			facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
			logger.error("Error al aplicar valor franja al procedimiento en negociación: "+procedimientoDto.getId(), e);
		}
    }

	public void marcarProcedimientoNegociado() {

	}

	public void marcarDesmarcarNegociado(ProcedimientoNegociacionDto dto) {
		if ((dto.getPorcentajeNegociado() != null)
				&& (dto.getTarifarioNegociado() != null)) {
			dto.setNegociado(true);
			dto.setValorNegociado(this.negociacionProcedimientoFacade
					.calcularValorProcedimiento(dto.getProcedimientoDto()
							.getCups(), dto.getTarifarioNegociado().getId()
							.intValue(), dto.getPorcentajeNegociado()
							.doubleValue()));
		} else {
			dto.setNegociado(false);
			dto.setValorNegociado(null);
		}

		if (dto.getTarifarioNegociado().getDescripcion()
				.equals("TARIFA PROPIA")) {
			dto.setPorcentajeNegociado(BigDecimal.ZERO);
		}
	}

	public void filtroEspecial() {
		System.out.println("Pago");
	}

	public void filtroEspecial(TiposDiferenciaNegociacionEnum tipo) {
		try {
			this.procedimientosNegociacion.clear();
			this.procedimientosNegociacion
					.addAll(procedimientosNegociacionOriginal);
			FiltroEspecialEnum seleccionado = null;
			seleccionado = filtroEspecialSeleccionadoPorcentaje;
			if (tipo.equals(TiposDiferenciaNegociacionEnum.DIFERENCIA_VALOR)) {
				seleccionado = filtroEspecialSeleccionadoValor;
			} else {
				seleccionado = filtroEspecialSeleccionadoPorcentaje;
			}
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
			// TODO:ajustar excepciones
		} catch (ConexiaBusinessException e) {
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
		 * Se comprueba la valides del rango, de ser un rango valido se optiene
		 * los filtros para continuar con el filtrado de los registros
		 */
		BigDecimal[] rango = validarFiltroEntre(filtros);

		this.procedimientosNegociacion = procedimientosNegociacion
				.stream()
				.filter(m -> (m.getDiferenciaPorcentajeContratado() != null
						&& (new BigDecimal(m
								.getDiferenciaPorcentajeContratado()).setScale(
								2, RoundingMode.UP).compareTo(rango[1]) <= 0) && (new BigDecimal(
						m.getDiferenciaPorcentajeContratado()).setScale(2,
						RoundingMode.UP).compareTo(rango[0]) >= 0)))
				.collect(Collectors.toList());
	}

	BigDecimal[] validarFiltroEntre(String[] filtros) throws ConexiaBusinessException {

		Pattern patron = Pattern.compile("([\\d]+[,][\\d]+)");
		Matcher mat = patron.matcher(filtroPorcentaje);

		if (filtros.length < 2 || !mat.matches()) {
			throw new ConexiaBusinessException(
					PreContractualMensajeErrorEnum.BUSINESS_ERROR,
					"El valor del filtro debe contener dos valores separados por \",\"");
		}

		final BigDecimal[] rango = new BigDecimal[] {
				new BigDecimal(filtros[0]).setScale(2, RoundingMode.FLOOR),
				new BigDecimal(filtros[1]).setScale(2, RoundingMode.FLOOR) };

		if (rango[0].compareTo(rango[1]) > 0) {
			throw new ConexiaBusinessException(
					PreContractualMensajeErrorEnum.BUSINESS_ERROR,
					"El valor antes de \",\" debe ser menor al valor posterior.");
		}

		if (rango[0].compareTo(rango[1]) == 0) {
			throw new ConexiaBusinessException(
					PreContractualMensajeErrorEnum.BUSINESS_ERROR,
					"Los valores del rango no pueden ser iguales.");
		}

		return rango;
	}

	public void filtrarIgual() {
		this.procedimientosNegociacion = procedimientosNegociacion
				.stream()
				.filter(m -> (m.getDiferenciaPorcentajeContratado() != null && m
						.getDiferenciaPorcentajeContratado() == Double
						.parseDouble(this.filtroPorcentaje)))
				.collect(Collectors.toList());
	}

	public void filtrarMayor() {

		this.procedimientosNegociacion = procedimientosNegociacion
				.stream()
				.filter(m -> (m.getDiferenciaPorcentajeContratado() != null && m
						.getDiferenciaPorcentajeContratado() > Double
						.parseDouble(filtroPorcentaje)))
				.collect(Collectors.toList());
	}

	public void filtrarMenor() {
		this.procedimientosNegociacion = procedimientosNegociacion.stream()
				.filter(m -> (m.getDiferenciaPorcentajeContratado() != null && m
						.getDiferenciaPorcentajeContratado() < Double
						.parseDouble(filtroPorcentaje)))
				.collect(Collectors.toList());
	}

	public void gestionarProcedimientos(String nombreTabla, String nombreComboGestion) {
		if (this.gestionSeleccionada != null) {
			if (this.gestionSeleccionada.equals(GestionTecnologiasNegociacionEnum.BORRAR_SELECCIONADOS)) {

				if(Objects.nonNull(procedimientosNegociacion) && procedimientosNegociacion.stream()
						.filter(m -> (m.isSeleccionado()))
						.collect(Collectors.toList()).size() > 0){
					RequestContext.getCurrentInstance().execute("PF('cdDeleteProd').show();");
				} else {
					facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_msj_val_sel"));
				}
			} else if (this.gestionSeleccionada.equals(GestionTecnologiasNegociacionEnum.SELECCIONAR_TODOS)) {
				procedimientosNegociacionSeleccionados = new ArrayList<ProcedimientoNegociacionDto>();
				if(Objects.nonNull(filtroNegociacionTecnologia)
						&& Objects.nonNull(filtroNegociacionTecnologia.getNegociacionId())
						&& Objects.nonNull(filtroNegociacionTecnologia.getServicioNegociacionId())) {
				}
				procedimientosNegociacionSeleccionados.addAll(procedimientosNegociacion);
				for(ProcedimientoNegociacionDto seleccionado: procedimientosNegociacionSeleccionados){
					seleccionado.setSeleccionado(true);
				}
				Ajax.oncomplete("PF('" + nombreTabla + "').selectAllRowsOnPage();");
			} else if (this.gestionSeleccionada.equals(GestionTecnologiasNegociacionEnum.DESELECCIONAR_TODOS)) {
				deseleccionarTodos(nombreTabla);
			}
			this.gestionSeleccionada = null;
			Ajax.update(nombreComboGestion);
		}
	}

	private void deseleccionarTodos(String nombreTabla){
		procedimientosNegociacionSeleccionados.clear();
		for(ProcedimientoNegociacionDto seleccionado: procedimientosNegociacionSeleccionados){
			seleccionado.setSeleccionado(false);
		}
		// Lo coloca como no seleccionado en los procedimientos de la negociacion
		if(Objects.nonNull(procedimientosNegociacion) && !procedimientosNegociacion.isEmpty()){
			for (ProcedimientoNegociacionDto dtoNegociacion : procedimientosNegociacion) {
				dtoNegociacion.setSeleccionado(false);
			}
		}
		Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRowsOnPage();");
	}

	public void eliminarProcedimientosMasivo() {
		if(negociacion.getEstadoLegalizacion() != EstadoLegalizacionEnum.LEGALIZADA) {
			try {
				List<Long> procedimientoIds = new ArrayList<Long>();
				if (Objects.nonNull(procedimientosNegociacion)) {

					procedimientosNegociacion.stream().forEach(
									px -> {
										if(px.isSeleccionado()) {
											if(pgp) {
												procedimientoIds.add(px.getProcedimientoDto().getId());
											} else {
												eliminarProcedimientos();
											}
										}
									}
								);

				} else {
					facesMessagesUtils.addInfo(resourceBundle
							.getString("negociacion_procedimiento_msj_val_sel"));
				}

				if(pgp) {//Se valida si aún existen procedimientos para el capítulo en cuestión, si no hay se procede a eliminar el capítulo

					if(Objects.nonNull(procedimientoIds) && procedimientoIds.size() > 0) {
						eliminarProcedimientoNegociacionPGP(procedimientoIds);
					}
				} else {
					mostrarMensajeProcedimientosEliminados();
					Ajax.update("negociacionProcedimientoForm");
				}

			} catch (Exception e) {
				logger.error(
						"Error al eliminar de la negociación los procedimientos seleccionados",
						e);
				facesMessagesUtils.addError(exceptionUtils
						.createSystemErrorMessage(resourceBundle));
			}
		}

	}

	private void eliminarProcedimientos() {

		if(!this.eliminarProcedimientosPorNegociacion){
			this.eliminarProcedimientosPorServicio();
		}else{
			this.eliminarProcedimientosPorNegociacion();
		}

	}

	private void eliminarProcedimientosPorNegociacion() {

		List<String> codigosEliminar = new ArrayList<>();
		codigosEliminar
				.addAll(this.procedimientosNegociacionSeleccionados
						.stream()
						.map(p -> p.getProcedimientoDto().getCodigoCliente())
						.collect(Collectors.toList()));

			this.negociacionProcedimientoFacade
			.eliminarProcedimientosNegociacionByIdAndNegociacion(
					this.negociacion.getId(), codigosEliminar,
					this.user.getId());

	}

	private void eliminarProcedimientoNegociacionPGP(List<Long> procedimientoIds) {
		if(negociacion.getEstadoLegalizacion() != EstadoLegalizacionEnum.LEGALIZADA) {
			Integer sinProcedimientos = this.negociacionProcedimientoFacade
					.eliminarProcedimientosNegociacionByIdAndNegociacionPGP(
							this.negociacion.getId(),
							procedimientoIds,
							capituloNegociacionPGP,
							tecnologiasController.getUser().getId());
			negociacionServicioController.calcularValorTotal();


			if(sinProcedimientos == 0) {
				volverAServicios();
			} else {
				mostrarMensajeProcedimientosEliminados();
				Ajax.update("negociacionProcedimientoForm");
			}
		}

	}

	private void eliminarProcedimientosPorServicio() {

		List<Long> procedimientosAEliminarId = new ArrayList<>();
		procedimientosAEliminarId
				.addAll(this.procedimientosNegociacion
						.stream()
						.filter(p -> p.isSeleccionado())
						.map(p -> p.getProcedimientoDto().getId())
						.collect(Collectors.toList()));

			this.negociacionProcedimientoFacade
			.eliminarProcedimientosNegociacionByIdAndNegociacionAndServicio(
					this.negociacion.getId(),
					this.servicioSaludNegociacion
							.getServicioSalud().getId(),
							procedimientosAEliminarId,
							this.user.getId());

	}

	private void mostrarMensajeProcedimientosEliminados() {
		consultarProcedimientosPorServicioNegociacion();
		facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_eliminacion_ok"));
		procedimientosNegociacionSeleccionados.clear();
	}

	public void volverAServicios() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext()
				.getSession(true);
		Long negociacionId = (Long) session
				.getAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString());
		session.setAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString(),
				negociacionId);

		this.facesUtils.urlRedirect("/sedesasede/detalle");

	}

    public void consultarProcedimientosAgregar() {
        try {
            if (pgp == false) {
                procedimientoAgregar.setServicioHabilitacion(this.servicioSaludNegociacion.getServicioSalud().getCodigo());
            }
            procedimientosAgregar.clear();
            procedimientosAgregarSeleccionados.clear();
            if (pgp) {
                procedimientosAgregar.addAll(this.negociacionProcedimientoFacade.consultarProcedimientosAgregarPGP(procedimientoAgregar,
                                this.negociacion, this.capituloNegociacionPGP.getCapituloProcedimiento().getId()));
            } else {                
                if (procedimientoAgregar.getDescripcion().isEmpty() && procedimientoAgregar.getCodigoCliente().isEmpty()) {
                    facesMessagesUtils.addWarning(resourceBundle.getString("negociacion_procedimiento_agregar_no_criterios"));
                    return ;
                }   
                procedimientosAgregar.addAll(this.negociacionProcedimientoFacade.consultarProcedimientosAgregar(procedimientoAgregar,
                                this.negociacion));
                procedimientosAgregar.stream().forEach(data -> data.setServicioId(this.servicioSaludNegociacion.getServicioSalud().getId().intValue()));                
            }
            if (procedimientosAgregar.size() == 1) {
                this.agregarProcedimientoNegociacion(this.procedimientosAgregar);
                Ajax.oncomplete("PF('negociacionProcedimientosSSWidget').filter();");
            } else if (procedimientosAgregar.size() != 0) {
                RequestContext.getCurrentInstance().execute("PF('procedimientosAgregarDlg').show();");
                Ajax.oncomplete("PF('procedimientosAgregarTableWV').filter();");
            } else {
                facesMessagesUtils.addWarning(resourceBundle.getString("negociacion_procedimiento_agregar_no_registros"));
            }
        } catch (ConexiaBusinessException e) {
            this.facesMessagesUtils.addWarning(this.exceptionUtils.createMessage(resourceBundle, e));
        } catch (Exception e) {
            logger.error("Error al consultarProcedimientosAgregar", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

	public void agregarProcedimientoNegociacion(List<ProcedimientoDto> procedimientos) {
		try {
			if(procedimientos != null && procedimientos.size() > 0){
				List<Long> ids = procedimientos.stream().map(px -> px.getId()).collect(Collectors.toList());
				if(pgp) {
//					this.negociacionProcedimientoFacade
//					.agregarProcedimientosNegociacionPGP(ids, this.negociacion,
//							this.capituloNegociacionPGP.getCapituloProcedimiento().getId(), this.user.getId());
//
//					consultarProcedimientosPorCapituloNegociacion();
				} else {
                                        Long negociacionReferenteId = this.negociacionFacade.obtenerNegociacionReferenteId(this.tecnologiasController.getNegociacion().getId());    
					this.negociacionProcedimientoFacade.agregarProcedimientosNegociacion(ids, this.negociacion,
							this.servicioSaludNegociacion.getServicioSalud().getCodigo(), 
                                                        procedimientos, this.user.getId(), negociacionReferenteId);
					consultarProcedimientosPorServicioNegociacion();
				}
				this.facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_procedimiento_agregar_masivo_ok")
						+ this.funcionListaCodigosPx.apply(procedimientos));
				RequestContext.getCurrentInstance().execute("PF('procedimientosAgregarDlg').hide();");
			}else{
				facesMessagesUtils.addWarning(resourceBundle.getString("negociacion_procedimiento_agregar_seleccionar"));
			}
		} catch (Exception e) {
			logger.error("Error al eliminar de la negociación los procedimientos seleccionados",e);
			facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcedimientoNegociacionDto getRowData(String strRowKey) {
		if(Objects.nonNull(strRowKey) && isNumeric(strRowKey)){
			Long rowKey = strRowKey != null && Long.valueOf(strRowKey) > 0 ?	Long.valueOf(strRowKey) : new Long(0);
			for(ProcedimientoNegociacionDto procedimientoNegociacion :(List<ProcedimientoNegociacionDto>)getWrappedData()){
				if(procedimientoNegociacion.getProcedimientoDto().getId().compareTo(rowKey) == 0){
					return procedimientoNegociacion;
				}
			}
		}
		return null;
	}

	private boolean isNumeric(String str) {
        return (str.matches("[+-]?\\d*(\\.\\d+)?") && !str.equals(""));
    }

	@Override
	public Object getRowKey(ProcedimientoNegociacionDto procedimientoNegociacionDto) {
		return procedimientoNegociacionDto.getProcedimientoDto().getId();
	}

	public void unRowSelect(UnselectEvent event){
		if(Objects.nonNull(event) && Objects.nonNull(event.getObject())){
			ProcedimientoNegociacionDto unSelectedItem = (ProcedimientoNegociacionDto)event.getObject();
			// Lo coloca como no seleccionado en los procedimientos de la
			// negociacion
			if (Objects.nonNull(procedimientosNegociacion) && !procedimientosNegociacion.isEmpty()) {
				for (ProcedimientoNegociacionDto dtoNegociacion : procedimientosNegociacion) {
					if (dtoNegociacion.getProcedimientoDto().getId()
							.compareTo(unSelectedItem.getProcedimientoDto().getId()) == 0) {
						dtoNegociacion.setSeleccionado(false);
					}
				}
			}
		}

	}

	public void rowSelect(SelectEvent event){
		if(Objects.nonNull(event) && Objects.nonNull(event.getObject())){
			ProcedimientoNegociacionDto selectedItem = (ProcedimientoNegociacionDto)event.getObject();
			if(Objects.nonNull(procedimientosNegociacionSeleccionados) && !procedimientosNegociacionSeleccionados.isEmpty()){
				for(ProcedimientoNegociacionDto dto: procedimientosNegociacionSeleccionados){
					if(dto.getProcedimientoDto().getId().compareTo(selectedItem.getProcedimientoDto().getId()) == 0){
						dto.setSeleccionado(true);
					}
				}
			}
			// Lo coloca como seleccionado en los procedimientos de la negociacion
			if (Objects.nonNull(procedimientosNegociacion) && !procedimientosNegociacion.isEmpty()) {
				for (ProcedimientoNegociacionDto dtoNegociacion : procedimientosNegociacion) {
					if (dtoNegociacion.getProcedimientoDto().getId()
							.compareTo(selectedItem.getProcedimientoDto().getId()) == 0) {
						dtoNegociacion.setSeleccionado(true);
					}
				}
			}
		}
	}

	public void toggleSelect(ToggleSelectEvent event){
		if (Objects.nonNull(event) && !event.isSelected()) {
			if (Objects.nonNull(procedimientosNegociacion) && !procedimientosNegociacion.isEmpty()
					&& Objects.nonNull(listadoPaginado) && !listadoPaginado.isEmpty()) {
				for (ProcedimientoNegociacionDto unSelect : listadoPaginado) {
					for (ProcedimientoNegociacionDto dtoNegociacion : procedimientosNegociacion) {
						if (dtoNegociacion.getProcedimientoDto().getId()
								.compareTo(unSelect.getProcedimientoDto().getId()) == 0) {
							dtoNegociacion.setSeleccionado(false);
						}
					}
				}
			}
		}else if (Objects.nonNull(event) && event.isSelected()) {
			if (Objects.nonNull(procedimientosNegociacion) && !procedimientosNegociacion.isEmpty()
					&& Objects.nonNull(listadoPaginado) && !listadoPaginado.isEmpty()) {
				for (ProcedimientoNegociacionDto unSelect : listadoPaginado) {
					for (ProcedimientoNegociacionDto dtoNegociacion : procedimientosNegociacion) {
						if (dtoNegociacion.getProcedimientoDto().getId()
								.compareTo(unSelect.getProcedimientoDto().getId()) == 0) {
							dtoNegociacion.setSeleccionado(true);
						}
					}
				}
			}
		}
	}

	public void limpiarPanelAgregar(){
		this.procedimientoAgregar = new ProcedimientoDto();
	}

    //<editor-fold desc="Getters && Setters">
    public TipoAsignacionTarifaProcedimientoEnum getTipoAsignacionSeleccionado() {
		return tipoAsignacionSeleccionado;
	}

	public void setTipoAsignacionSeleccionado(TipoAsignacionTarifaProcedimientoEnum tipoAsignacionSeleccionado) {
		this.tipoAsignacionSeleccionado = tipoAsignacionSeleccionado;
	}

	public TipoAsignacionTarifaProcedimientoEnum[] getTipoAsignacionTarifaProcedimientoEnum() {
		return this.tecnologiasController.getNegociacion().getTipoModalidadNegociacion() == null
                ? TipoAsignacionTarifaProcedimientoEnum.values()
                : TipoAsignacionTarifaProcedimientoEnum.values(this.tecnologiasController.getNegociacion().getTipoModalidadNegociacion());
	}

	public Double getPorcentajeValor() {
		return porcentajeValor;
	}

	public void setPorcentajeValor(Double porcentajeValor) {
		this.porcentajeValor = porcentajeValor;
	}

	public List<ProcedimientoNegociacionDto> getProcedimientosNegociacionSeleccionados() {
		return procedimientosNegociacionSeleccionados;
	}

	public void setProcedimientosNegociacionSeleccionados(List <ProcedimientoNegociacionDto> procedimientosNegociacionSeleccionados) {
		for(ProcedimientoNegociacionDto dto: procedimientosNegociacionSeleccionados){
			dto.setSeleccionado(true);
		}
		this.procedimientosNegociacionSeleccionados = procedimientosNegociacionSeleccionados;
	}

	public FiltroEspecialEnum[] getFiltroEspecialEnum() {
		return FiltroEspecialEnum.values();
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

	public List<ProcedimientoNegociacionDto> getProcedimientosNegociacionOriginal() {
		return procedimientosNegociacionOriginal;
	}

	public void setProcedimientosNegociacionOriginal(List<ProcedimientoNegociacionDto> procedimientosNegociacionOriginal) {
		this.procedimientosNegociacionOriginal = procedimientosNegociacionOriginal;
	}

	public TiposDiferenciaNegociacionEnum getDiferenciaValor() {
		return diferenciaValor;
	}

	public void setDiferenciaValor(TiposDiferenciaNegociacionEnum diferenciaValor) {
		this.diferenciaValor = diferenciaValor;
	}

	public TiposDiferenciaNegociacionEnum getDiferenciaPorcentaje() {
		return diferenciaPorcentaje;
	}

	public void setDiferenciaPorcentaje(TiposDiferenciaNegociacionEnum diferenciaPorcentaje) {
		this.diferenciaPorcentaje = diferenciaPorcentaje;
	}

	public Double getValorTarifaPropia() {
		return valorTarifaPropia;
	}

	public void setValorTarifaPropia(Double valorTarifaPropia) {
		this.valorTarifaPropia = valorTarifaPropia;
	}

	public GestionTecnologiasNegociacionEnum[] getGestionTecnologiasNegociacion() {
		return GestionTecnologiasNegociacionEnum.values();
	}

	public GestionTecnologiasNegociacionEnum getGestionSeleccionada() {
		return gestionSeleccionada;
	}

	public void setGestionSeleccionada(GestionTecnologiasNegociacionEnum gestionSeleccionada) {
		this.gestionSeleccionada = gestionSeleccionada;
	}

	public TipoTarifarioDto getTarifarioAsignar() {
		return tarifarioAsignar;
	}

	public void setTarifarioAsignar(TipoTarifarioDto tarifarioAsignar) {
		this.tarifarioAsignar = tarifarioAsignar;
	}

	public NegociacionDto getNegociacion() {
		return negociacion;
	}

	public ServicioNegociacionDto getServicioSaludNegociacion() {
		return servicioSaludNegociacion;
	}

	public Locale getLocale() {
		return new Locale("en", "US");
	}

	public boolean isAplicarPorNegociacion() {
		return aplicarPorNegociacion;
	}

	public void setAplicarPorNegociacion(boolean aplicarPorNegociacion) {
		this.aplicarPorNegociacion = aplicarPorNegociacion;
	}

	public ProcedimientoDto getProcedimientoAgregar() {
		return procedimientoAgregar;
	}

	public void setProcedimientoAgregar(ProcedimientoDto procedimientoAgregar) {
		this.procedimientoAgregar = procedimientoAgregar;
	}

	public List<ProcedimientoDto> getProcedimientosAgregar() {
		return procedimientosAgregar;
	}

	public void setProcedimientosAgregar(List<ProcedimientoDto> procedimientosAgregar) {
		this.procedimientosAgregar = procedimientosAgregar;
	}

	public List<ProcedimientoDto> getProcedimientosAgregarSeleccionados() {
		return procedimientosAgregarSeleccionados;
	}

	public void setProcedimientosAgregarSeleccionados(List<ProcedimientoDto> procedimientosAgregarSeleccionados) {
		this.procedimientosAgregarSeleccionados = procedimientosAgregarSeleccionados;
	}

	public boolean isEliminarProcedimientosPorNegociacion() {
		return eliminarProcedimientosPorNegociacion;
	}

	public void setEliminarProcedimientosPorNegociacion(boolean eliminarProcedimientosPorNegociacion) {
		this.eliminarProcedimientosPorNegociacion = eliminarProcedimientosPorNegociacion;
	}

	public Double getPorcentajeAumentoPropia() {
		return porcentajeAumentoPropia;
	}

	public void setPorcentajeAumentoPropia(Double porcentajeAumentoPropia) {
		this.porcentajeAumentoPropia = porcentajeAumentoPropia;
	}

	public boolean isPgp() {
		return pgp;
	}

	public void setPgp(boolean pgp) {
		this.pgp = pgp;
	}

	public CapitulosNegociacionDto getCapituloNegociacion() {
		return capituloNegociacionPGP;
	}

	public void setCapituloNegociacion(CapitulosNegociacionDto capituloNegociacion) {
		this.capituloNegociacionPGP = capituloNegociacion;
	}

	public String getTituloServicioCapitulo() {
		return tituloServicioCapitulo;
	}

	public void setTituloServicioCapitulo(String tituloServicioCapitulo) {
		this.tituloServicioCapitulo = tituloServicioCapitulo;
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

	public TecnologiasSSController getTecnologiasController() {
		return tecnologiasController;
	}

	public void setTecnologiasController(TecnologiasSSController tecnologiasController) {
		this.tecnologiasController = tecnologiasController;
	}

    public List<TipoTarifarioDto> getTipoTarifarioAsignacionMasiva() {
        List<TipoTarifarioDto> tipoTarifarios = commonController.getTiposTarifarios();
        tipoTarifarios.removeIf(f -> f.getDescripcion().equals("TARIFA PROPIA"));
        return tipoTarifarios;
    }
    //</editor-fold>

}
