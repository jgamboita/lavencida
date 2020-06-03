package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.omnifaces.util.Ajax;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.selectbooleancheckbox.SelectBooleanCheckbox;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ToggleSelectEvent;

import com.conexia.contratacion.commons.constants.enums.AlcanceTecnologiasCapitaEnum;
import com.conexia.contratacion.commons.constants.enums.GestionTecnologiasNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contratacion.commons.constants.enums.OpcionesValorCapitaEnum;
import com.conexia.contratacion.commons.constants.enums.TipoNegociacionEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ProcedimientoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ServicioNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.i18n.CnxI18n;

import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import co.conexia.negociacion.wap.facade.negociacion.capita.NegociacionServiciosCapitaFacade;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionServicioProcedimientoSSFacade;

/**
 *
 * @author Andrés Mise Olivera
 */
@Named
@ViewScoped
public class NegociacionServiciosCapitaSSController implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5687189113154806784L;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    private NegociacionServiciosCapitaFacade negociacionServiciosCapitaFacade;

    @Inject
    private NegociacionServicioProcedimientoSSFacade negociacionProcedimientoFacade;

    @Inject
    private TecnologiasSSController tecnologiasController;

    @Inject

    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private Log logger;

    @Inject
    protected PreContractualExceptionUtils exceptionUtils;

    @Inject
    protected NegociacionFacade negociacionFacade;

    protected NegociacionDto negociacion;

    private Integer anio;
    private Integer currentLevel;
    private ServicioNegociacionDto currentServicio;
    private GestionTecnologiasNegociacionEnum gestionSeleccionada;
    private List<ProcedimientoNegociacionDto> procedimientos;
    private List<ProcedimientoNegociacionDto> procedimientosSeleccionados;
    private List<ProcedimientoNegociacionDto> procedimientosNegociados;
	private List<ServicioNegociacionDto> servicios;
    private List<ServicioNegociacionDto> serviciosSeleccionados;
    private BigDecimal totalNegociacion;
    private BigDecimal totalPorcentajeNegociado;
	private Long zonaCapitaId;

	private BigDecimal totalPorcentajeUpc;

	// Opciones masivas
    private Double otroValor;
    private OpcionesValorCapitaEnum opcionValor;

    //Validaciones
    private boolean showPoblacion; // Atributo para validar si se debe mostrar la columna poblacion o no

    // Poblacion más alta
    private Integer poblacionMayor = 0;

    private Boolean distribuirPorcentajeNegociado;

    private static final int DECIMALES_APROXIMACION = 3;

    private Function<List<ServicioSaludDto>, String> funcionListaTecnologiasSinNegociar = (l -> l
            .stream()
            .map(s -> s.getCodigo().concat(
                    "(" + s.getNumeroProcedimientos() + ")"))
            .collect(Collectors.joining(" , "+System.lineSeparator())));


    @PostConstruct
    public void init() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        negociacion = (NegociacionDto) session.getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
        this.validarMostrarPoblacion();
        if (!Objects.isNull(negociacion.getZonaCapita())
                && !Objects.isNull(negociacion.getValorUpcAnual())) {
            this.anio = LocalDate.now().getYear();
            this.currentLevel = 1;
            this.procedimientosSeleccionados = new ArrayList<>(0);
            this.serviciosSeleccionados = new ArrayList<>(0);
            this.zonaCapitaId = negociacion.getZonaCapita().getId().longValue();
            this.servicios = new ArrayList<>();
            if(negociacion.getZonaCapita().getRias()){
	            // Completar información Rias
	            negociacion = this.negociacionFacade.consultarRias(negociacion);
            }
            try {
                this.servicios.addAll(negociacionServiciosCapitaFacade.consultarServiciosNegociacionCapita(negociacion, this.anio));
            } catch (ConexiaBusinessException e) {
                facesMessagesUtils.addWarning(this.exceptionUtils.createMessage(resourceBundle, e));
                logger.error(e.getMessage(), e);
            }
            this.totalNegociacion = this.calcularTotalNegociacion(BigDecimal.ZERO);
        } else {
            this.servicios = new ArrayList<>();
            this.totalNegociacion = BigDecimal.ZERO;
        }
        this.otroValor = null;
    }

    /**
     * Con base en reglas verifica si se muestra la columna poblacion
     * en la tabla de servicios
     */
    private void validarMostrarPoblacion() {
        if (negociacion.getTipoModalidadNegociacion() == NegociacionModalidadEnum.CAPITA
                && negociacion.getPoblacionServicio()
                && negociacion.getTipoNegociacion() == TipoNegociacionEnum.SEDE_A_SEDE) {
            showPoblacion = true;
        }
    }

    /**
     * Asignacion de tarifas masiva
     */
    public void asignarTarifasServicios() {
        if (this.serviciosSeleccionados.isEmpty()) {
            this.facesMessagesUtils.addWarning("Debe seleccionar al menos un procedimiento");
            return;
        }
        switch (this.opcionValor) {
            case OTRO_VALOR:
            	  if (this.otroValor > 0) {
            		if(otroValor > negociacion.getValorUpcMensual().doubleValue()){
            			facesMessagesUtils.addError("El valor indicado es mayor al valor de la upc");
            		}
            		else{
            			for (ServicioNegociacionDto dto : serviciosSeleccionados) {
            				dto.setValorNegociado(BigDecimal.valueOf(this.otroValor).setScale(0, BigDecimal.ROUND_HALF_UP));
            				dto.setPorcentajeNegociado(
								dto.getValorNegociado().multiply(BigDecimal.valueOf(100D), MathContext.DECIMAL64)
										.divide(negociacion.getValorUpcMensual(), MathContext.DECIMAL64)
										.setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP));
            				this.negociacionServiciosCapitaFacade.asignarValorServicio(negociacion, dto, this.tecnologiasController.getUser().getId());
            			}
            		}
                  } else {
                      this.facesMessagesUtils.addWarning("El valor debe ser mayor a 0");
                  }
                  break;
            case PORCENTAJE:
                if (this.otroValor < 101 && this.otroValor > -101) {
                    this.negociacionServiciosCapitaFacade.asignarValorPorPorcentaje(
                            this.getIds(), BigDecimal.valueOf(1 + (this.otroValor / 100)), this.zonaCapitaId,
                            	this.tecnologiasController.getUser().getId());
                } else {
                    this.facesMessagesUtils.addWarning("El valor del porcentaje no es valido");
                }
                break;
            case VALOR_CONTRATO_ANTERIOR:
            	this.negociacionServiciosCapitaFacade.asignarValoresContratoAnteriorNegociadoServicio(serviciosSeleccionados,  negociacion,
            			this.tecnologiasController.getUser().getId());
                break;
            case VALOR_REFERENTE:
                this.negociacionServiciosCapitaFacade.asignarValorReferente(
                        this.getIds(),
                        negociacion,
                        this.tecnologiasController.getUser().getId());
                break;
        }
        updateTotal();
		RequestContext.getCurrentInstance().execute("PF('negociacionServiciosSS').filter();");
    }

    public BigDecimal calcularTotalNegociacion(BigDecimal tn) {
    	this.poblacionMayor = 0;
        for (ServicioNegociacionDto s : this.servicios) {
            if (s.getValorNegociado() != null) {
                tn = tn.add(s.getValorNegociado());
            }
            if(Objects.nonNull(s.getPoblacion())){
            	this.poblacionMayor = this.poblacionMayor <= s.getPoblacion() ? s.getPoblacion() : this.poblacionMayor ;
            }
        }
        return tn;
    }

    public void updateTotal(){
    	this.tecnologiasController.consultarPorcentajeTemasCapita();
    	Ajax.update("tecnologiasSSForm:tabsTecnologias:negociacionServiciosSS, tecnologiasSSForm:totalNegociacion,tecnologiasSSForm:panelTemaCapita");
    }
    public BigDecimal calcularPorcentajeTotalUpc() {
        BigDecimal total = BigDecimal.ZERO;
        for (ServicioNegociacionDto s : this.servicios) {
            total = total.add(s.getServicioSalud().getLiquidacionServicio().getPorcentaje());
        }
        return total;
    }

    public void eliminarProcedimientos() {
        if (!this.procedimientosSeleccionados.isEmpty()
                && this.procedimientos.size() == this.procedimientosSeleccionados.size()
                && this.servicios.size() > 1) {
            this.negociacionServiciosCapitaFacade.eliminarServicios(this.currentServicio.getSedesNegociacionServicioIds(),
            		this.tecnologiasController.getUser().getId());
            this.init();
            this.tecnologiasController.eliminarSedesNegociacion(new HashSet<>(Arrays.asList(this.currentServicio.getSedeNegociacionId())));
            Ajax.update("tecnologiasSIForm:tabsTecnologias:master");
            this.facesMessagesUtils.addInfo("Se ha eliminado correctamente el servicio");
        } else if (!this.procedimientosSeleccionados.isEmpty()
                && this.procedimientos.size() == this.procedimientosSeleccionados.size()
                && this.servicios.size() == 1) {
            this.facesMessagesUtils.addWarning("No se puede eliminar todos los procedimientos ya que solo queda un servicio");
        } else {
            List<Long> procedimientosId = new ArrayList<>();
            for (ProcedimientoNegociacionDto p : procedimientosSeleccionados) {
                procedimientosId.add(p.getProcedimientoDto().getId());
            }
            this.negociacionProcedimientoFacade.eliminarProcedimientosNegociacionByIdAndNegociacionAndServicio(
                    negociacion.getId(),
                    this.currentServicio.getServicioSalud().getId(), procedimientosId,
                    this.tecnologiasController.getUser().getId());
            this.procedimientos.removeAll(this.procedimientosSeleccionados);
            BigDecimal porcentajeReferente = new BigDecimal(0);
            for (ProcedimientoNegociacionDto p : this.procedimientos) {
                porcentajeReferente = porcentajeReferente.add(
                        p.getLiquidacionProcedimiento().getPorcentaje());
            }
            if(Objects.nonNull(this.currentServicio.getValorNegociado()) && Objects.nonNull(this.currentServicio.getPorcentajeNegociado())){
            	this.negociacionServiciosCapitaFacade.asignarValor(negociacion,
						this.currentServicio.getSedesNegociacionServicioIds(),
						this.currentServicio.getValorNegociado(),
						this.currentServicio.getPorcentajeNegociado(),
						this.tecnologiasController.getUser().getId());
            }
            this.loadProcedimientos(this.currentServicio);
			this.facesMessagesUtils.addInfo("Se han eliminado correctamente los procedimientos seleccionados");
            this.procedimientosSeleccionados.clear();
        }
        //RequestContext.getCurrentInstance().execute("PF('negociacionProcedimientosServicioWidget').filter();");
    }

    public void eliminarProcedimientosSinRestribuir() {
        if (!this.procedimientosSeleccionados.isEmpty()
                && this.procedimientosSeleccionados.size() == this.procedimientos.size()
                && this.servicios.size() > 1) {
            this.negociacionServiciosCapitaFacade.eliminarServicios(this.currentServicio.getSedesNegociacionServicioIds(),
            		this.tecnologiasController.getUser().getId());
            this.init();
            this.tecnologiasController.eliminarSedesNegociacion(new HashSet<>(Arrays.asList(this.currentServicio.getSedeNegociacionId())));
            Ajax.update("tecnologiasSIForm:tabsTecnologias:master");
            this.facesMessagesUtils.addInfo("Se ha eliminado correctamente el servicio");
        } else if (!this.procedimientosSeleccionados.isEmpty()
                && this.procedimientos.size() == this.procedimientosSeleccionados.size()
                && this.servicios.size() == 1) {
            this.facesMessagesUtils.addWarning("No se puede eliminar todos los procedimientos ya que solo queda un servicio");
        } else {
            List<Long> procedimientosId = new ArrayList<>();
            for (ProcedimientoNegociacionDto p : procedimientosSeleccionados) {
                procedimientosId.add(p.getProcedimientoDto().getId());
            }
            this.negociacionProcedimientoFacade.eliminarProcedimientosNegociacionByIdAndNegociacionAndServicio(
                    negociacion.getId(),
                    this.currentServicio.getServicioSalud().getId(), procedimientosId, this.currentServicio.getSedesNegociacionServicioIds(),
                    this.tecnologiasController.getUser().getId());
            this.procedimientos.removeAll(this.procedimientosSeleccionados);
            BigDecimal porcentajeReferente = BigDecimal.ZERO;
            BigDecimal nuevoValor = BigDecimal.ZERO;
            for (ProcedimientoNegociacionDto p : this.procedimientos) {
            	if((p.getValorNegociado() == null) != (p.getLiquidacionProcedimiento() == null)){
            	}else{
            		nuevoValor = nuevoValor.add(p.getValorNegociado());
					porcentajeReferente = porcentajeReferente.add(p.getPorcentajeNegociado());
            	}
            }
            if((BigDecimal.ZERO.compareTo(nuevoValor) != 0 && BigDecimal.ZERO.compareTo(porcentajeReferente) != 0)){
				this.currentServicio.setValorNegociado(nuevoValor);
				this.currentServicio.setPorcentajeNegociado(porcentajeReferente);
				this.currentServicio.setNegociado(true);
				///this.negociacionServiciosCapitaFacade.asignarValor(negociacion,
						//this.currentServicio.getSedesNegociacionServicioIds(), nuevoValor);
            }
            this.loadProcedimientos(this.currentServicio);
			this.facesMessagesUtils.addInfo("Se han eliminado correctamente los procedimientos seleccionados");
            this.tecnologiasController.consultarPorcentajeTemasCapita();
        }
        this.init();
		RequestContext.getCurrentInstance().update(Arrays
				.asList(new String[] {
						"tecnologiasSSForm:tabsTecnologias:negociacionServiciosSS",
						"tecnologiasSSForm:totalNegociacion",
						"tecnologiasSSForm:porcentajeRecuparacion",
						"tecnologiasSSForm:porcentajePYP"}));

        //RequestContext.getCurrentInstance().execute("PF('negociacionProcedimientosServicioWidget').filter();");
    }

    public void eliminarServicios() {
        if ((this.serviciosSeleccionados.size() == this.servicios.size() || this.servicios
                .size() == 1)
                && this.tecnologiasController
                        .getNegociacionMedicamentoCapitaController()
                        .getMedicamentosNegociacion().size() == 0) {
            this.facesMessagesUtils.addWarning("No se puede eliminar el ultima servicio a negociar");
        } else {
            List<Long> serviciosId = new ArrayList<>();
            this.serviciosSeleccionados.stream().forEach((sn) -> {
                serviciosId.addAll(sn.getSedesNegociacionServicioIds());
            });
            this.negociacionServiciosCapitaFacade.eliminarServicios(serviciosId,
            		this.tecnologiasController.getUser().getId());
            this.serviciosSeleccionados.clear();
            this.init();
            this.facesMessagesUtils.addInfo("Se han eliminado correctamente los servicios seleccionados");
        }
    }

    public void gestionarProcedimientos() {
        if (this.gestionSeleccionada != null) {
            switch (this.gestionSeleccionada) {
                case BORRAR_SELECCIONADOS:
                    if (this.procedimientosSeleccionados.isEmpty()) {
                        this.facesMessagesUtils.addWarning("Debe seleccionar al menos un servicio");
                    } else {
                        RequestContext.getCurrentInstance().execute("PF('confirmDelete').show();");
                    }
                    break;
                case DESELECCIONAR_TODOS:
                    this.procedimientosSeleccionados.clear();
                    break;
                case SELECCIONAR_TODOS:
                    this.procedimientosSeleccionados.addAll(this.procedimientos);
                    break;
            }
            this.gestionSeleccionada = null;
        }
    }

    public void gestionarServicios() {
        if (this.gestionSeleccionada != null) {
            switch (this.gestionSeleccionada) {
                case BORRAR_SELECCIONADOS:
                    if (this.serviciosSeleccionados.isEmpty()) {
                        this.facesMessagesUtils.addWarning("Debe seleccionar al menos un procedimiento");
                    } else {
                        RequestContext.getCurrentInstance().execute("PF('confirmDeleteServicio').show();");
                    }
                    break;
                case DESELECCIONAR_TODOS:
                    this.serviciosSeleccionados.clear();
                    break;
                case SELECCIONAR_TODOS:
                    this.serviciosSeleccionados.addAll(this.servicios);
                    break;
            }
            this.gestionSeleccionada = null;
        }
    }

    /**
     *
     * @param negociacion
     */
    public void guardarServicioPorcentaje(ServicioNegociacionDto negociacion) {
    	negociacion.setNegociaPorcentaje(true);
    	guardarServicio(negociacion);
    	this.updateTotal();
    }

    /**
     *
     * @param negociacion
     */
    public void guardarServicioValor(ServicioNegociacionDto negociacion) {
    	negociacion.setNegociaPorcentaje(false);
    	guardarServicio(negociacion);
    	this.updateTotal();
    }

    /**
     * Guarda el servicio sin actualizar la interfaz
     * @param servicio
     */
    private void guardarServicioPorcentajeSinActualizarInterfaz(ServicioNegociacionDto servicio) {
    	servicio.setNegociaPorcentaje(true);
    	guardarServicio(servicio, false);
    }

    private void guardarServicio(ServicioNegociacionDto servicio) {
    	guardarServicio(servicio, true);
    }

    private void guardarServicio(ServicioNegociacionDto servicio, boolean actualizarInterfaz) {
    	BigDecimal valorNegociado = BigDecimal.ZERO;
		if (servicio.isNegociaPorcentaje() && Objects.nonNull(servicio.getPorcentajeNegociado())) {
			valorNegociado = negociacion.getValorUpcMensual()
					.multiply((servicio.getPorcentajeNegociado()).divide(BigDecimal.valueOf(100),MathContext.DECIMAL64), MathContext.DECIMAL64)
					.setScale(0, BigDecimal.ROUND_HALF_UP);
		} else if (Objects.isNull(servicio.isNegociaPorcentaje()) || !servicio.isNegociaPorcentaje()) {
			valorNegociado = servicio.getValorNegociado();
			servicio.setPorcentajeNegociado(servicio.getValorNegociado().multiply(BigDecimal.valueOf(100D), MathContext.DECIMAL64)
					.divide(negociacion.getValorUpcMensual(),MathContext.DECIMAL64)
					.setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP));
		}
		servicio.setValorNegociado(valorNegociado);
    	if(Objects.nonNull(this.distribuirPorcentajeNegociado) && this.distribuirPorcentajeNegociado){
    		this.negociacionServiciosCapitaFacade.asignarValor(this.negociacion,
    				servicio.getSedesNegociacionServicioIds(),
    				servicio.getValorNegociado(),
    				servicio.getPorcentajeNegociado(),
    				this.tecnologiasController.getUser().getId());
    	}else{
    		this.negociacionServiciosCapitaFacade.asignarValorServicio(this.negociacion, servicio, this.tecnologiasController.getUser().getId());
    		this.totalNegociacion = this.calcularTotalNegociacion(BigDecimal.ZERO);
    	}
    	if (actualizarInterfaz) {
    		RequestContext.getCurrentInstance().execute("if (PF('negociacionServiciosSS')) { PF('negociacionServiciosSS').filter(); }");
    	}
    }

    public void loadProcedimientos(ServicioNegociacionDto negociacion) {
        this.procedimientos = this.negociacionServiciosCapitaFacade.
                consultarProcedimientosNegociacionCapita(
                        negociacion.getSedesNegociacionServicioIds(),
                        this.negociacion);
        this.procedimientosSeleccionados.clear();
        RequestContext.getCurrentInstance().execute("PF('negociacionProcedimientosServicioWidget').filter();");
    }

    public boolean mostrarValor() {
        return OpcionesValorCapitaEnum.OTRO_VALOR.equals(this.opcionValor)
                || OpcionesValorCapitaEnum.PORCENTAJE.equals(this.opcionValor);
    }

    @SuppressWarnings("unchecked")
	public void seleccionarMasivoServicios(ToggleSelectEvent event) {
    	if (event.isSelected()) {
    		this.serviciosSeleccionados = (List<ServicioNegociacionDto>) ((DataTable) event.getComponent()).getSelection();
        } else {
            this.serviciosSeleccionados.clear();
        }
    }

    private List<Long> getIds() {
        List<Long> ids = new ArrayList<>();
        for (ServicioNegociacionDto sn : this.serviciosSeleccionados) {
            ids.addAll(sn.getSedesNegociacionServicioIds());
        }
        return ids;
    }

    public boolean validarServicios() {
        List<ServicioSaludDto> servicios = this.negociacionServiciosCapitaFacade
                .validarServiciosNegociados(
                        tecnologiasController.getNegociacion(), false);

        if(!servicios.isEmpty()){
            this.facesMessagesUtils
                    .addInfo(resourceBundle
                            .getString("negociacion_servicio_msj_validacion_no")
                            + funcionListaTecnologiasSinNegociar
                                    .apply(servicios));

            return false;
        }
        return true;
    }

    /**
     * Asigna la poblacion a las sedes servicio por servicio
     * @param sedesNegociacionServicioIds Identificador de {@link - SedeNegociacionServicio}
     * @param poblacion poblacion a asignar
     */
    public void asignarPoblacionPorServicio(
            List<Long> sedesNegociacionServicioIds, Integer poblacion) {
        this.negociacionServiciosCapitaFacade.asignarPoblacionPorServicio(
                sedesNegociacionServicioIds, poblacion);
        this.updateTotal();
    }

    /**
     * Actualiza los servicios cuando el valor UPC ha cambiado y actualiza la interfaz
     */
    public void actualizarServicios() {
    	for (ServicioNegociacionDto servicio : servicios) {
    		if (servicio.getPorcentajeNegociado() != null && servicio.getPorcentajeNegociado().compareTo(new BigDecimal("0")) != 0) {
    			this.guardarServicioPorcentajeSinActualizarInterfaz(servicio);
    		}
    	}
    	RequestContext.getCurrentInstance().execute("if (PF('negociacionServiciosSS')) { PF('negociacionServiciosSS').filter(); }");
    }

    public void seleccionDistribuir(AjaxBehaviorEvent event){
    	this.distribuirPorcentajeNegociado = ((SelectBooleanCheckbox)event.getSource()).isSelected();
    }

    public List<OpcionesValorCapitaEnum> getOpcionesValorCapitaEnum() {
        return Arrays.asList(OpcionesValorCapitaEnum.values());
    }

    public List<AlcanceTecnologiasCapitaEnum> getAlcanceTecnologiasCapita() {
        return Arrays.asList(AlcanceTecnologiasCapitaEnum.values());
    }

    public Integer getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(Integer currentLevel) {
        this.currentLevel = currentLevel;
    }

    public ServicioNegociacionDto getCurrentServicio() {
        return currentServicio;
    }

    public void setCurrentServicio(ServicioNegociacionDto currentServicio) {
        this.currentServicio = currentServicio;
    }

    public GestionTecnologiasNegociacionEnum getGestionSeleccionada() {
        return gestionSeleccionada;
    }

    public void setGestionSeleccionada(GestionTecnologiasNegociacionEnum gestionSeleccionada) {
        this.gestionSeleccionada = gestionSeleccionada;
    }

    public List<ProcedimientoNegociacionDto> getProcedimientos() {
        return procedimientos;
    }

    public void setProcedimientos(List<ProcedimientoNegociacionDto> procedimientos) {
        this.procedimientos = procedimientos;
    }

    public List<ProcedimientoNegociacionDto> getProcedimientosSeleccionados() {
        return procedimientosSeleccionados;
    }

    public void setProcedimientosSeleccionados(List<ProcedimientoNegociacionDto> procedimientosSeleccionados) {
        this.procedimientosSeleccionados = procedimientosSeleccionados;
    }

    public List<ServicioNegociacionDto> getServicios() {
        return servicios;
    }

    public void setServicios(List<ServicioNegociacionDto> servicios) {
        this.servicios = servicios;
    }

    public List<ServicioNegociacionDto> getServiciosSeleccionados() {
        return serviciosSeleccionados;
    }

    public void setServiciosSeleccionados(List<ServicioNegociacionDto> serviciosSeleccionados) {
        this.serviciosSeleccionados = serviciosSeleccionados;
    }

    public OpcionesValorCapitaEnum getOpcionValor() {
        return opcionValor;
    }

    public void setOpcionValor(OpcionesValorCapitaEnum opcionValor) {
        this.otroValor = null;
        this.opcionValor = opcionValor;
    }

    public Double getOtroValor() {
        return otroValor;
    }

    public void setOtroValor(Double otroValor) {
        this.otroValor = (double) Math.round(otroValor);
    }

    public BigDecimal getTotalNegociacion() {
        return totalNegociacion;
    }

    public void setTotalNegociacion(BigDecimal totalNegociacion) {
        this.totalNegociacion = totalNegociacion;
    }

    public boolean isShowPoblacion() {
        return showPoblacion;
    }

	public Integer getPoblacionMayor() {
		return poblacionMayor;
	}

	public void setPoblacionMayor(Integer poblacionMayor) {
		this.poblacionMayor = poblacionMayor;
	}

	public BigDecimal getTotalPorcentajeNegociado() {
		return totalPorcentajeNegociado;
	}

	public void setTotalPorcentajeNegociado(BigDecimal totalPorcentajeNegociado) {
		this.totalPorcentajeNegociado = totalPorcentajeNegociado;
	}

	public List<ProcedimientoNegociacionDto> getProcedimientosNegociados() {
		return procedimientosNegociados;
	}

	public void setProcedimientosNegociados(List<ProcedimientoNegociacionDto> procedimientosNegociados) {
		this.procedimientosNegociados = procedimientosNegociados;
	}

	public BigDecimal getTotalPorcentajeUpc() {
		return totalPorcentajeUpc;
	}

	public void setTotalPorcentajeUpc(BigDecimal totalPorcentajeUpc) {
		this.totalPorcentajeUpc = totalPorcentajeUpc;
	}

	public void resetProcedimientos(){
		this.procedimientos = new ArrayList<ProcedimientoNegociacionDto>();
	}

	public Boolean getDistribuirPorcentajeNegociado() {
		return distribuirPorcentajeNegociado;
	}

	public void setDistribuirPorcentajeNegociado(Boolean distribuirPorcentajeNegociado) {
		this.distribuirPorcentajeNegociado = distribuirPorcentajeNegociado;
	}
}
