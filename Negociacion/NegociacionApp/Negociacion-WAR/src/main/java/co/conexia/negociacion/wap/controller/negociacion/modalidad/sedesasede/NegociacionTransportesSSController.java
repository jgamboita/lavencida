package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import co.conexia.negociacion.wap.controller.common.CommonController;
import co.conexia.negociacion.wap.controller.negociacion.NegociacionController;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionServiciosSSFacade;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;
import com.conexia.contratacion.commons.dto.maestros.TipoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ServicioNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controller que maneja el tab de los transportes
 *
 * @author jjoya
 *
 */
@Named
@ViewScoped
public class NegociacionTransportesSSController implements Serializable{

    /**
     *
     */
    private static final long serialVersionUID = -2153884917446091808L;

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
    /** Se injecta el controler padre que contiene todos los tabs de tecnologías y la negociación sacada de sesion. **/
    private TecnologiasSSController tecnologiasController;

    @Inject
    private NegociacionServiciosSSFacade facade;

    @Inject
    private CommonController commonController;
    
    @Inject
    private NegociacionController negociacionController;

    private List<ServicioNegociacionDto> serviciosNegociacion;
    private List<ServicioNegociacionDto> serviciosNegociacionOriginal;
    private List<ServicioNegociacionDto> serviciosNegociacionSeleccionados;

    private TipoAsignacionTarifaServicioEnum tipoAsignacionSeleccionado;
    private GestionTecnologiasNegociacionEnum gestionSeleccionada;

    private FiltroEspecialEnum filtroEspecialSeleccionadoPorcentaje;

    private String filtroValor;
    private String filtroPorcentaje;
    private Double porcentajeValor = 0.0;

    private TiposDiferenciaNegociacionEnum diferenciaPorcentaje = TiposDiferenciaNegociacionEnum.DIFERENCIA_PORCENTAJE;

    private TipoTarifarioDto tarifarioAsignar;
    private Double porcentajeAsignar;
    private Double porcentajeAumentoPropia;
    private Double valorTarifaPropia;

    private Function<List<ServicioSaludDto>, String> funcionListaTecnologiasSinNegociar = (l -> l
            .stream()
            .map(s -> s.getCodigo().concat(
                    "(" + s.getNumeroProcedimientos() + ")"))
            .collect(Collectors.joining(" , "+System.lineSeparator())));

    /**
     * Metodo de ejecucion PostConstruct
     */
    @PostConstruct
    public void postconstruct() {
        try {
            if (tecnologiasController.getNegociacion() != null) {
                consultarServiciosNegociacion();
            }
        } catch (Exception e) {
            this.logger.error(
                    "Error al postConstruct NegociacionServiciosSSController",
                    e);
            this.facesMessagesUtils.addError(exceptionUtils
                    .createSystemErrorMessage(resourceBundle));

        }

    }

    /**
     * Consulta de los servicios que estan en negociacion
     */
    public void consultarServiciosNegociacion(){
        this.serviciosNegociacion = facade
                .consultarServiciosNegociacionNoSedesByNegociacionId(tecnologiasController.getNegociacion());
        this.serviciosNegociacionOriginal = new ArrayList<ServicioNegociacionDto>();
        this.serviciosNegociacionOriginal.addAll(serviciosNegociacion);
    }

    public void asignarTarifasServicios() {
        try {
            if (this.tipoAsignacionSeleccionado != null) {
                if (this.validarCamposTarifa()) {
                    aplicarValorMasivo();
                    Ajax.update("tecnologiasSSForm:tabsTecnologias:negociacionTrasladosSS");
                }
            }
        } catch (ConexiaBusinessException e) {
            logger.error("Error asignarTarifasServicio", e);
            this.facesMessagesUtils.addError(this.exceptionUtils.createMessage(
                    resourceBundle, e));
        } catch (Exception e) {
            this.logger.error("Error al asignarTarifasServicios", e);
            this.facesMessagesUtils.addError(exceptionUtils
                    .createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Valida los campos dependiendo de la seleccion
     *
     * @return
     */
    private boolean validarCamposTarifa() {
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

    private void aplicarValorMasivo() throws ConexiaBusinessException {
        if ((null != serviciosNegociacionSeleccionados)
                && (serviciosNegociacionSeleccionados.size() > 0)) {

            for (Iterator<ServicioNegociacionDto> iter = serviciosNegociacionSeleccionados
                    .listIterator(); iter.hasNext();) {
                ServicioNegociacionDto dto = iter.next();
                asignarTarifarioNegociado(dto);
            }            
            Long negociacionReferenteId = this.negociacionController.obtenerNegociacionReferenteId(this.tecnologiasController.getNegociacion().getId());    
            String mensaje = this.facade.guardar(
                    this.tecnologiasController.getNegociacion().getId(),
                    this.serviciosNegociacionOriginal, null, true, this.tecnologiasController.getNegociacion().getPoblacion(),false,
                    this.tecnologiasController.getUser().getId(), negociacionReferenteId);
            facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_servicio_msj_guardar_ok"));
            if(mensaje.length() > 0){
            	facesMessagesUtils.addWarning(mensaje);
            }
            this.consultarServiciosNegociacion();
        } else {
            facesMessagesUtils.addInfo(resourceBundle
                    .getString("negociacion_servicio_msj_val_sel"));
        }
    }

    /**
     * Asigna el tarifario dependiendo de la opcion
     *
     * @param dto
     * @throws ConexiaBusinessException
     */
    private void asignarTarifarioNegociado(ServicioNegociacionDto dto)
            throws ConexiaBusinessException {
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

            if (dto.getTarifarioContratoAnterior().getId() != null
                    && dto.getTarifarioContratoAnterior().getDescripcion()
                            .equals("TARIFA PROPIA")) {
                dto.setPorcentajeNegociado(new BigDecimal(0));
                dto.setValorPropia(dto.getValorContratoAnterior() != null ? dto
                        .getValorContratoAnterior().doubleValue() : 0L);
            }

        } else if (tipoAsignacionSeleccionado
                .equals(TipoAsignacionTarifaServicioEnum.PORTAFOLIO_PROPUESTO)) {
            dto.setTarifarioNegociado(dto.getTarifarioPropuestoPortafolio()
                    .getId() != null ? dto.getTarifarioPropuestoPortafolio()
                    : dto.getTarifarioNegociado());
            dto.setPorcentajeNegociado(dto.getPorcentajePropuestoPortafolio() != null ? dto
                    .getPorcentajePropuestoPortafolio() : dto
                    .getPorcentajeNegociado());
            dto.setValorPropia(-9999D);
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
            } else {
                throw new ConexiaBusinessException(
                        PreContractualMensajeErrorEnum.TARIFARIO_NO_ENCONTRADO);
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
                            "PF('cdDeleteTraslados').show();");
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
                serviciosNegociacionSeleccionados.clear();
                Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRows();");
            }
            this.gestionSeleccionada = null;
            Ajax.update(nombreComboGestion);

        }
    }

    /**
     * Elimina los servicios de la negociacion
     */
    public void eliminarServiciosMasivo() {
        try {
            if ((null != serviciosNegociacionSeleccionados) && (serviciosNegociacionSeleccionados.size() > 0)) {
                List<Long> serviciosId = new ArrayList<>();
                Set<Long> sedes = new HashSet<>();
                this.serviciosNegociacionSeleccionados.stream().forEach((s) -> {
                    serviciosId.add(s.getServicioSalud().getId());
                    sedes.add(s.getSedeNegociacionId());
                });
                this.facade.eliminarByNegociacionAndServicios(
                        tecnologiasController.getNegociacion().getId(),
                        serviciosId,
                        this.tecnologiasController.getUser().getId());
                serviciosNegociacion = serviciosNegociacion
                        .stream()
                        .filter(s -> !serviciosId.contains(s.getServicioSalud()
                                .getId())).collect(Collectors.toList());
                serviciosNegociacionOriginal.clear();
                serviciosNegociacionOriginal.addAll(serviciosNegociacion);
               // this.tecnologiasController.eliminarSedesSinTecnologiasByNegociacion(tecnologiasController.getNegociacion().getId());
                facesMessagesUtils.addInfo(resourceBundle
                        .getString("negociacion_servicio_eliminacion_ok"));
                serviciosNegociacionSeleccionados.clear();
                Ajax.update("tecnologiasSSForm:tabsTecnologias:negociacionTrasladosSS");
            } else {
                facesMessagesUtils.addInfo(resourceBundle
                        .getString("negociacion_servicio_msj_val_sel"));
            }
        } catch (Exception e) {
            logger.error(
                    "Error al eliminar de la negociación los paquetes seleccionados",
                    e);
            facesMessagesUtils.addError(exceptionUtils
                    .createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Filtro especial de la tabla paquetes
     *
     * @param tipo
     *            identifica por que campo filtrar
     */
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
            // TODO:ajustar excepciones
        } catch (Exception e) {
            logger.error("Error en filtro especial", e);
            facesMessagesUtils.addError(exceptionUtils
                    .createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Filtra por un rango
     *
     * @param tipo
     */
    public void filtrarEntre() {
        String[] filtros;
        filtros = this.filtroPorcentaje.split(",");
        this.serviciosNegociacion = serviciosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaPorcentajeContratado() != null
                        && m.getDiferenciaPorcentajeContratado() < Double
                                .parseDouble(filtros[1]) && m
                        .getDiferenciaPorcentajeContratado() > Double
                        .parseDouble(filtros[0]))).collect(Collectors.toList());
    }

    /**
     * Filtra datos iguales
     *
     * @param tipo
     */
    public void filtrarIgual() {
        this.serviciosNegociacion = serviciosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaPorcentajeContratado() != null && m
                        .getDiferenciaPorcentajeContratado() == Double
                        .parseDouble(this.filtroPorcentaje)))
                .collect(Collectors.toList());
    }

    /**
     * Filtra datos mayores
     *
     * @param tipo
     */
    public void filtrarMayor() {

        this.serviciosNegociacion = serviciosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaPorcentajeContratado() != null && m
                        .getDiferenciaPorcentajeContratado() > Double
                        .parseDouble(filtroPorcentaje)))
                .collect(Collectors.toList());
    }

    /**
     * Filtra datos menores
     *
     * @param tipo
     */
    public void filtrarMenor() {
        this.serviciosNegociacion = serviciosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaPorcentajeContratado() != null && m
                        .getDiferenciaPorcentajeContratado() < Double
                        .parseDouble(filtroPorcentaje)))
                .collect(Collectors.toList());
    }



        /**
         * Abre una ventana emergente con el detalle de los procedimientos del
         * contrato anterior.
        * @param macroServicio
        * @param nombreServicio
        * @param servicioId
        * @param codigoServicio
         */
        public void verDetalleContratoAnterior(ServicioNegociacionDto servicioNegociacion){
        	ServicioSaludDto servicioSalud =  servicioNegociacion.getServicioSalud();

        	if(servicioSalud == null){
        		facesMessagesUtils.addError("Servicio de salud no encontrado");
        		return;
        	}

        	String macroServicio = servicioSalud.getMacroservicio() != null ?
        				servicioSalud.getMacroservicio().getNombre() : null;

			String nombreServicio = servicioSalud.getNombre();
            String codigoServicio = servicioSalud.getCodigo();
            Long servicioId	= servicioSalud.getId();

            try{
                String tituloComparacion = macroServicio + " - " + codigoServicio + " " + nombreServicio;
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                session.setAttribute(NegociacionSessionEnum.SERVICIO_ID.toString(), servicioId);
                session.setAttribute(NegociacionSessionEnum.TITULO_COMPARACION.toString(),tituloComparacion);

                // Abrir en otra ventana
                ExternalContext context = facesContext.getExternalContext();
                context.redirect("/wap/negociacion/servicio/procedimiento/contrato");
                facesContext.responseComplete();
            }catch(Exception e){
                logger.error("Error en ver detalle de procedimiento contrato anterior", e);
                facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
        }

        /**
         * Abre una ventana emergente con las tarifas propuestas de los procedimientos
         * asociados al servicio seleccionado
         * @param macroServicio
         * @param nombreServicio
         * @param codigoServicio
         * @param servicioId
         */
        public void verDetalleTarifasPropuestas(ServicioNegociacionDto servicioNegociacion){
        	ServicioSaludDto servicioSalud =  servicioNegociacion.getServicioSalud();

        	if(servicioSalud == null){
        		facesMessagesUtils.addError("Servicio de salud no encontrado");
        		return;
        	}

        	String macroServicio = servicioSalud.getMacroservicio() != null ?
        				servicioSalud.getMacroservicio().getNombre() : null;

			String nombreServicio = servicioSalud.getNombre();
            String codigoServicio = servicioSalud.getCodigo();
            Long servicioId	= servicioSalud.getId();

            try{
                String tituloComparacion = macroServicio + " - " + codigoServicio + " " + nombreServicio;
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                session.setAttribute(NegociacionSessionEnum.SERVICIO_ID.toString(), servicioId);
                session.setAttribute(NegociacionSessionEnum.TITULO_COMPARACION.toString(), tituloComparacion);

                // Abrir en otra ventana
                ExternalContext context = facesContext.getExternalContext();
                context.redirect("/wap/negociacion/servicio/procedimiento/tarifapropuesta");
                facesContext.responseComplete();
            }catch(Exception e){
                logger.error("Error en ver detalle de procedimiento contrato anterior", e);
                facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
        }

    /**
     * Check negociado cuando cumple las condiciones
     * @param dto
     */
    public void marcarDesmarcarNegociado(ServicioNegociacionDto dto) {
        if (dto.getPorcentajeNegociado() != null
                && dto.getTarifarioNegociado() != null) {
            dto.setNegociado(true);
        } else {
            dto.setNegociado(false);
        }
    }

    /**
     * Condiciones para cuando se ejecuta el cambio de tarifas
     *
     * @param dto
     */
    public void cambiarTarifa(ServicioNegociacionDto dto) {
        marcarDesmarcarNegociado(dto);
        if (dto.getTarifarioNegociado() == null || dto.getTarifarioNegociado().getDescripcion()
                .equals("TARIFA PROPIA")) {
            dto.setPorcentajeNegociado(BigDecimal.ZERO);
        }
    }

    /**
     * Guarda el estado de los servicios
     */
    public void guardar(boolean agregarMensaje){
        try{
            Long negociacionReferenteId = this.negociacionController.obtenerNegociacionReferenteId(this.tecnologiasController.getNegociacion().getId());    
            String mensaje = this.facade.guardar(
                    this.tecnologiasController.getNegociacion().getId(),
                    this.serviciosNegociacionOriginal, null, true,
                    this.tecnologiasController.getNegociacion().getPoblacion(),false,
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

    /**
     * Valida que los servicios queden negociados
     */
    public boolean validarServicios(boolean agregarMensaje) {
        List<ServicioSaludDto> servicios = this.facade.validarServiciosNegociados(tecnologiasController.getNegociacion(), true);
        if (!servicios.isEmpty()) {
            this.facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_servicio_traslado_msj_validacion_no") + funcionListaTecnologiasSinNegociar.apply(servicios));
            return false;
        }
        return true;
    }

    public TipoAsignacionTarifaServicioEnum[] getTipoAsignacionTarifaServicioEnum() {
        return TipoAsignacionTarifaServicioEnum.values();
    }

    public GestionTecnologiasNegociacionEnum[] getGestionTecnologiasNegociacion() {
        return GestionTecnologiasNegociacionEnum.values();
    }

    public FiltroEspecialEnum[] getFiltroEspecialEnum() {
        return FiltroEspecialEnum.values();
    }

    /**
     * @return the filtroEspecialSeleccionadoPorcentaje
     */
    public FiltroEspecialEnum getFiltroEspecialSeleccionadoPorcentaje() {
        return filtroEspecialSeleccionadoPorcentaje;
    }

    /**
     * @param filtroEspecialSeleccionadoPorcentaje
     *            the filtroEspecialSeleccionadoPorcentaje to set
     */
    public void setFiltroEspecialSeleccionadoPorcentaje(
            FiltroEspecialEnum filtroEspecialSeleccionadoPorcentaje) {
        this.filtroEspecialSeleccionadoPorcentaje = filtroEspecialSeleccionadoPorcentaje;
    }

    public TipoAsignacionTarifaServicioEnum getTipoAsignacionSeleccionado() {
        return tipoAsignacionSeleccionado;
    }

    public void setTipoAsignacionSeleccionado(
            TipoAsignacionTarifaServicioEnum tipoAsignacionSeleccionado) {
        this.tipoAsignacionSeleccionado = tipoAsignacionSeleccionado;
    }

    public Double getPorcentajeValor() {
        return porcentajeValor;
    }

    public void setPorcentajeValor(Double porcentajeValor) {
        this.porcentajeValor = porcentajeValor;
    }

    public GestionTecnologiasNegociacionEnum getGestionSeleccionada() {
        return gestionSeleccionada;
    }

    public void setGestionSeleccionada(
            GestionTecnologiasNegociacionEnum gestionSeleccionada) {
        this.gestionSeleccionada = gestionSeleccionada;
    }

    public List<ServicioNegociacionDto> getServiciosNegociacion() {
        return serviciosNegociacion;
    }

    public void setServiciosNegociacion(
            List<ServicioNegociacionDto> serviciosNegociacion) {
        this.serviciosNegociacion = serviciosNegociacion;
    }

    public List<ServicioNegociacionDto> getServiciosNegociacionSeleccionados() {
        return serviciosNegociacionSeleccionados;
    }

    public void setServiciosNegociacionSeleccionados(
            List<ServicioNegociacionDto> serviciosNegociacionSeleccionados) {
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

    public void setServiciosNegociacionOriginal(
            List<ServicioNegociacionDto> serviciosNegociacionOriginal) {
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

}
