package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import co.conexia.negociacion.wap.controller.common.CommonController;
import co.conexia.negociacion.wap.controller.negociacion.NegociacionController;
import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionMedicamentoSSFacade;
import co.conexia.negociacion.wap.util.GestionarImportarXlxs;
import co.conexia.negociacion.wap.util.XlxsFiles;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.negociacion.ArchivoTecnologiasNegociacionEventoDto;
import com.conexia.contratacion.commons.dto.negociacion.ErroresImportTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasEventoDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.UploadedFile;
import org.primefaces.model.Visibility;
import org.xml.sax.SAXException;
import javax.annotation.PostConstruct;
import javax.faces.component.UIData;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Controlador de una negociacion de medicamentos sede a sede
 *
 * @author dprieto
 */
@Named
@ViewScoped
public class NegociacionMedicamentoSSController implements Serializable {

    private static final long serialVersionUID = 4482278288235375130L;

    @Inject
    private Log logger;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    private FacesUtils faceUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private PreContractualExceptionUtils exceptionUtils;

    @Inject
    private NegociacionMedicamentoSSFacade negociacionMedicamentoSSFacade;

    @Inject    
    private NegociacionFacade negociacionFacade;
    
    @Inject
    private CommonController commonController;

    @Inject
    private ValidadorImportTecnologiasPaqueteDetalleSS validator;

    @Inject
    /**
     * Se injecta el controler padre que contiene todos los tabs de tecnologías
     * y la negociación sacada de sesion. *
     */
    private TecnologiasSSController tecnologiasController;

    private List<MedicamentoNegociacionDto> medicamentosNegociacion;

    private List<MedicamentoNegociacionDto> medicamentosNegociacionOriginal;

    private List<MedicamentoNegociacionDto> medicamentosNegociacionSeleccionados;

    private List<SedesNegociacionDto> listaSedesImportarMedicamentos = new ArrayList<>();

    private List<MedicamentoNegociacionDto> medicamentosImportar = new ArrayList<>();

    private List<MedicamentosDto> medicamentosAgregar = new ArrayList<>();
	private List<MedicamentosDto> medicamentosAgregarSeleccionados = new ArrayList<>();
	private MedicamentosDto medicamentoAgregar = new MedicamentosDto();
	private Function<List<MedicamentosDto>, String> funcionListaCodigosMx = (l -> l
			.stream().map(s -> s.getCums())
			.collect(Collectors.joining(" , " + System.lineSeparator())));

    private List<MedicamentoNegociacionDto> medicamentosReguladosValorInvalido = new ArrayList<>();

    private GestionTecnologiasNegociacionEnum gestionSeleccionada;

    private TipoAsignacionTarifaMedEnum tipoAsignacionSeleccionado;

    private TipoAsignacionTarifaMedReguladoEnum tipoAsignacionImportar;

    private Double porcentajeValor = 0.0;

    private String filtroValor;

    private String filtroPorcentaje;

    private String filtroValorReferencia;

    private String filtroPorcentajeReferencia;

    private FiltroEspecialEnum filtroEspecialSeleccionadoValor;
    private FiltroEspecialEnum filtroEspecialSeleccionadoPorcentaje;

    private FiltroEspecialEnum filtroEspecialSeleccionadoValorReferencia;
    private FiltroEspecialEnum filtroEspecialSeleccionadoPorcentajeReferencia;

    private TiposDiferenciaNegociacionEnum diferenciaValor = TiposDiferenciaNegociacionEnum.DIFERENCIA_VALOR;
    private TiposDiferenciaNegociacionEnum diferenciaPorcentaje = TiposDiferenciaNegociacionEnum.DIFERENCIA_PORCENTAJE;
    private TiposDiferenciaNegociacionEnum diferenciaValorReferencia = TiposDiferenciaNegociacionEnum.DIFERENCIA_VALOR_REFERENCIA;
    private TiposDiferenciaNegociacionEnum diferenciaPorcentajeReferencia = TiposDiferenciaNegociacionEnum.DIFERENCIA_PORCENTAJE_REFERENCIA;


    private List<Boolean> listToggler;

    private ArchivosNegociacionEnum typeImport;

    private List<ErroresImportTecnologiasEventoDto> listadoErrores;

    @Inject
    private XlxsFiles xlxsFiles;

    @Inject
    private GestionarImportarXlxs gestionarImportarXlxs;

    private static final String READER_MEDICAMENTOS_XML = "medicamentosReader.xml";

    /*
	 * Valida si se trata de una negociación PGP
	 */
	private Boolean pgp = Boolean.FALSE;

	private Boolean existenRegulados = false;

	private List<ArchivoTecnologiasNegociacionEventoDto> medicamentosRegulados;

	private List<MedicamentoNegociacionDto> medicamentosReguladosNegociacion;

        private final List<Message> messages = new ArrayList<>();
    /**
     * Constructor por defecto *
     */
    public NegociacionMedicamentoSSController() {
    }

    /**
     * Metodo de ejecucion PostConstruct
     */
    @PostConstruct
    @Trace(dispatcher = true)
    public void postconstruct() {
    	NewRelic.setTransactionName("NegociacionMedicamentoSSController","CargarMedicamentosNegociacion");
        listToggler = Arrays.asList(true, true, false, true, true, false, false, true, true, true, true, true, true, true, true, true, true);
        refreshData();
    }

    public void refreshData(){
        if (tecnologiasController.getNegociacion() != null) {
            this.medicamentosNegociacion = negociacionMedicamentoSSFacade.consultarMedicamentosNegociacionNoSedesByNegociacionId(tecnologiasController.getNegociacion());
            medicamentosNegociacionOriginal = new ArrayList<>();
            this.medicamentosNegociacionSeleccionados = new ArrayList<>();
            pgp = NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.equals(tecnologiasController.getNegociacion().getTipoModalidadNegociacion());
            if(Objects.nonNull(medicamentosNegociacion)){
            	this.medicamentosNegociacionOriginal.addAll(medicamentosNegociacion);
            }
            for (int i = 0; i < medicamentosNegociacion.size(); i++) {
                marcarDesmarcarNegociado(medicamentosNegociacion.get(i));
            }
        }
        Ajax.update("tecnologiasSSForm:tabsTecnologias:negociacionMedicamentosSS");
        Ajax.update("tecnologiasSSForm:tabsTecnologias");
    }
    
    /**
     * Permite verificar los valores negociados de los medicamentos regulados al intentar finalizar la negociación
     * @return
     */
    public List<MedicamentoNegociacionDto> validarMedicamentosReguladosNegociacion() {
        medicamentosReguladosValorInvalido.clear();
        if (medicamentosNegociacion.size() > 0) {
            validarMedicamentosRegulados(medicamentosNegociacion, false);
        }
        return medicamentosReguladosValorInvalido;
    }

    /**
     * Permite filtrar los medicamentos que sean regulados y tengan un valor negociado mayor al valor referente
     * @param medicamentos
     * @return lista de medicamentos que pasan la validación
     */
    public List<MedicamentoNegociacionDto> validarMedicamentosRegulados(List<MedicamentoNegociacionDto> medicamentos, Boolean removerErroneos)
    		throws ConexiaSystemException {

        List<MedicamentoNegociacionDto> medicamentosValidos = new ArrayList<>();
        medicamentosReguladosValorInvalido.clear();
        if (medicamentos.size() > 0 && Objects.nonNull(tipoAsignacionSeleccionado)) {
            medicamentosValidos = medicamentos;
            medicamentos.forEach(mx -> {
                switch (this.tipoAsignacionSeleccionado) {
                    case APLICAR_PORCENTAJE:
                        asignarValorNegociado(mx, this.tipoAsignacionSeleccionado);
                        if (mx.getMedicamentoDto().getRegulado() && Objects.nonNull(mx.getValorNegociado())) {
                            if (mx.getValorNegociado().compareTo(mx.getValorReferencia()) == 1) {
                                medicamentosReguladosValorInvalido.add(mx);
                            }
                        }
                        break;
                    //No se aplica el case TARIFA_EMSSANAR porque trae el valor referente para aplicarlo al negociado por lo que no se
                    //incumple la regla
                    case CONTRATO_ANTERIOR:
                        if (mx.getMedicamentoDto().getRegulado() && Objects.nonNull(mx.getValorContratoAnterior())) {
                            if (mx.getValorContratoAnterior().compareTo(mx.getValorReferencia()) == 1) {
                                medicamentosReguladosValorInvalido.add(mx);
                            }
                        }
                        break;
                    case TARIFA_EMSSANAR:
                        medicamentosReguladosValorInvalido.clear();
                        break;
                    default:
                        if (mx.getMedicamentoDto().getRegulado() && (Objects.nonNull(mx.getValorPropuestoPortafolio()) ? mx.getValorPropuestoPortafolio() : BigDecimal.ZERO).compareTo(mx.getValorReferencia()) == 1) {
                            medicamentosReguladosValorInvalido.add(mx);
                        }
                }
            });

            if (removerErroneos) {
                switch (this.tipoAsignacionSeleccionado) {
                    case APLICAR_PORCENTAJE:
                        if (Objects.nonNull(this.tipoAsignacionSeleccionado.getPropiedad())) {
                            removerMedicamentosReguladosErroneos(medicamentosValidos);
                        }
                        break;
                    //No se aplica el case TARIFA_EMSSANAR porque trae el valor referente para aplicarlo al negociado por lo que no se
                    //incumple la regla
                    case CONTRATO_ANTERIOR:
                        removerMedicamentosReguladosErroneos(medicamentosValidos);
                        break;
                    case TARIFA_EMSSANAR:
                        return medicamentosValidos;
                    default:
                        if (Objects.nonNull(this.tipoAsignacionSeleccionado.getPropiedad())) {
                            removerMedicamentosReguladosErroneos(medicamentosValidos);
                        }
                        break;
                }
            }

        }

        return medicamentosValidos;
    }

    /**
     * Para validar medicamentos regulados al momento de importar
     * @param medicamentos
     * @return
     * @throws ConexiaSystemException
     * @throws ConexiaBusinessException
     */
    public List<ArchivoTecnologiasNegociacionEventoDto> asignarValoresMedicamentosReguladosImportar
    	(List<ArchivoTecnologiasNegociacionEventoDto> medicamentos) throws ConexiaSystemException, ConexiaBusinessException {
    	List<ArchivoTecnologiasNegociacionEventoDto> medicamentosRegulados = this.negociacionMedicamentoSSFacade.consultarValorReferenteMedicamentos(medicamentos);
    	List<ArchivoTecnologiasNegociacionEventoDto> medicamentosReguladosImportar = new ArrayList<>();
    	int count = 0;
    	if(tipoAsignacionImportar.equals(TipoAsignacionTarifaMedReguladoEnum.VALOR_REFERENTE_MAXIMO)){
    		for(ArchivoTecnologiasNegociacionEventoDto mx : medicamentosRegulados){
    			mx.setCodigoMedicamento(mx.getCodigoMedicamento());
    			mx.setValorNegociado(mx.getValorReguladoMaximo().toString());
    			mx.setValorImportado(medicamentos.get(count).getValorNegociado());
    			medicamentosReguladosImportar.add(mx);
    			count++;
    		}
    	}
    	else if(tipoAsignacionImportar.equals(TipoAsignacionTarifaMedReguladoEnum.VALOR_REFERENTE_MINIMO)){
    		for(ArchivoTecnologiasNegociacionEventoDto mx : medicamentosRegulados){
    			mx.setCodigoMedicamento(mx.getCodigoMedicamento());
    			mx.setValorNegociado(mx.getValorReguladoMinimo().toString());
    			mx.setValorImportado(medicamentos.get(count).getValorNegociado());
    			medicamentosReguladosImportar.add(mx);
    			count++;
    		}
    	}
    	else{
    		medicamentosReguladosImportar = new ArrayList<>();
    	}
    	return medicamentosReguladosImportar;
    }


    /**
     * Remueve de una lista de medicamentos aquellos que son regulados que no cumplen con la regla de que el valor negociado no sobrepase
     * el valor referente
     * @param medicamentos
     */
    private void removerMedicamentosReguladosErroneos(List<MedicamentoNegociacionDto> medicamentos) throws ConexiaSystemException {

    	switch (this.tipoAsignacionSeleccionado) {
	        case APLICAR_PORCENTAJE:
	        	medicamentos.removeIf(mx-> mx.getMedicamentoDto().getRegulado()
	        			&& mx.getValorNegociado().compareTo(mx.getValorReferencia()) == 1);
	            break;
	            //No se aplica el case TARIFA_EMSSANAR porque trae el valor referente para aplicarlo al negociado por lo que no se
	            //incumple la regla
	        case CONTRATO_ANTERIOR:
	        	medicamentos.removeIf(mx-> (mx.getMedicamentoDto().getRegulado() && Objects.nonNull(mx.getValorContratoAnterior())) ?
	    										mx.getValorContratoAnterior().compareTo(mx.getValorReferencia()) == 1 : false);
	        	break;
	        default:
	        	medicamentos.removeIf(mx-> mx.getMedicamentoDto().getRegulado() && (Objects.nonNull(mx.getValorPropuestoPortafolio()) ? mx.getValorPropuestoPortafolio() : BigDecimal.ZERO).compareTo(mx.getValorReferencia()) == 1);
	        	break;
	    }

    }

    /**
     * Método invocado para aplicar y guardar valor contrato anterior a
     * medicamentos seleccionados
     *
     * @param tipo
     * @param table
     */
    public void aplicarValorMasivo(final TipoAsignacionTarifaMedEnum tipo, UIData table) {
        try {
            if ((null != medicamentosNegociacionSeleccionados) && (medicamentosNegociacionSeleccionados.size() > 0)) {
                for (MedicamentoNegociacionDto dto : medicamentosNegociacionSeleccionados) {
                    asignarValorNegociado(dto, tipo);
                    dto.setNegociado(true);
                }

                facesMessagesUtils.addInfo(resourceBundle.getString("medicamento_msj_actualizacion_ok"));
            } else {
                facesMessagesUtils.addInfo(resourceBundle.getString("medicamento_msj_val_med_sel"));
            }
        } catch (Exception e) {
            logger.error("Error al aplicar el valor tipo " + tipo.toString() + " a medicamentos seleccionados.", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Método invocado para aumentar/disminuir la tarifa en un porcentaje dado a
     * los medicamentos seleccionados
     *
     * @param porcentaje Porcentaje a aumentar/disminuir del valor negociado
     * @param table
     */
    public void aplicarPorcentajeMasivo(final Double porcentaje, UIData table) {
        try {
            if ((null != medicamentosNegociacionSeleccionados && (medicamentosNegociacionSeleccionados.size() > 0)) && (null != porcentaje)) {
                for (Iterator<MedicamentoNegociacionDto> iter = medicamentosNegociacionSeleccionados.listIterator(); iter.hasNext();) {
                    MedicamentoNegociacionDto dto = iter.next();
                    BigDecimal valorNegociado
                            = BigDecimal.valueOf(((dto.getValorNegociado() != null ? dto.getValorNegociado() : BigDecimal.ZERO)
                                    .doubleValue() * porcentaje) / 100.0).setScale(2, RoundingMode.HALF_DOWN);
                    dto.setValorNegociado(dto.getValorNegociado() != null ? dto.getValorNegociado().add(valorNegociado) : BigDecimal.ZERO);
                    dto.setNegociado(true);
                    /*if (negociacionMedicamentoSSFacade.actualizarValorNegociado(dto.getIdSedeNegociacionMedicamento(), dto.getValorNegociado()) > 0) {
                     }*/
                }
                facesMessagesUtils.addInfo(resourceBundle.getString("medicamento_msj_actualizacion_ok"));
            } else {
                facesMessagesUtils.addInfo(resourceBundle.getString("medicamento_msj_val_med_sel"));
            }
        } catch (Exception e) {
            logger.error("Error al aplicar el aumento/disminución del %" + porcentaje + " a los medicamentos seleccionados.", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Método encargado de realizar la operación de valor asignado de acuerdo al
     * tipo
     *
     * @param dto
     * @param tipo
     */
    private void asignarValorNegociado(MedicamentoNegociacionDto dto, final TipoAsignacionTarifaMedEnum tipo) {
        if (tipo.equals(TipoAsignacionTarifaMedEnum.CONTRATO_ANTERIOR)) {
            dto.setValorNegociado(dto.getValorContratoAnterior() != null ? dto.getValorContratoAnterior() : BigDecimal.ZERO);
        } else if (tipo.equals(TipoAsignacionTarifaMedEnum.PORTAFOLIO_PROPUESTO)) {
            dto.setValorNegociado(dto.getValorPropuestoPortafolio() != null ? dto.getValorPropuestoPortafolio() : BigDecimal.ZERO);
        } else if (tipo.equals(TipoAsignacionTarifaMedEnum.TARIFA_EMSSANAR)) {
            dto.setValorNegociado(dto.getValorReferencia() != null ? dto.getValorReferencia() : BigDecimal.ZERO);
        } else if (tipo.equals(TipoAsignacionTarifaMedEnum.APLICAR_PORCENTAJE)){
        	BigDecimal valorNegociado =
					BigDecimal.valueOf(((
							dto.getValorNegociado()!=null?dto.getValorNegociado():BigDecimal.ZERO)
							.doubleValue()*this.porcentajeValor)/100.0).setScale(2, RoundingMode.HALF_DOWN);
			dto.setValorNegociado(dto.getValorNegociado()!=null?dto.getValorNegociado().add(valorNegociado):BigDecimal.ZERO);
        } else if (TipoAsignacionTarifaMedEnum.APLICAR_REFERENTE.equals(tipo)){
        	if(Objects.nonNull(dto.getCostoMedioUsuario()) &&
        			Objects.nonNull(dto.getFrecuenciaReferente())){
	        	dto.setCostoMedioUsuario(dto.getCostoMedioUsuarioReferente());
				dto.setValorNegociado(dto.getCostoMedioUsuario()
						.multiply(BigDecimal.valueOf(dto.getFrecuenciaReferente())
						.multiply(BigDecimal.valueOf(this.tecnologiasController.getNegociacion().getPoblacion()))));
				dto.setNegociado(true);
        	}
        }
    }

    /**
     * Metodo encargado de eliminar una lista de medicamentos selecionados
     * (Masiva)
     */
    public void eliminarMedicamentosMasivo() {
        try {           
            if (medicamentosNegociacionSeleccionados!=null && medicamentosNegociacionSeleccionados.size() > 0) {
                for (int i = 0; i < medicamentosNegociacionSeleccionados.size(); i++) {
                     MedicamentoNegociacionDto dto = medicamentosNegociacionSeleccionados.get(i);
                        this.eliminarMedicamento(dto.getMedicamentoDto().getId());
                        this.medicamentosNegociacion.remove(dto);
                }
                    medicamentosNegociacionOriginal.clear();
                    medicamentosNegociacionOriginal.addAll(medicamentosNegociacion);
                    medicamentosNegociacionSeleccionados=new ArrayList<>();
                    messages.add(new Message(Message.INFO, resourceBundle.getString("medicamento_msj_eliminacion_ok")));
                } else {
                    messages.add(new Message(Message.INFO, resourceBundle.getString("medicamento_msj_val_med_sel")));
                }
                refreshData();
        } catch (Exception e) {
            logger.error("Error al eliminar de la negociación los medicamentos seleccionados", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }

    }

    /**
     * Metodo encargado de eliminar un medicamento de la negociación (Uno a uno)
     */
    private void eliminarMedicamento(Long medicamentoId) {
        if (medicamentoId == null) {
            this.faceUtils.urlRedirect("/bandejaPrestador");
        } else {
            try {
                negociacionMedicamentoSSFacade.eliminarByNegociacionAndMedicamento(
                        tecnologiasController.getNegociacion().getId(), medicamentoId,
                        this.tecnologiasController.getUser().getId());
            } catch (Exception e) {
                logger.error("Error al eliminar de la negociación el medicamento No." + medicamentoId, e);
                facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }

        }
    }

    public void asignarTarifasMedicamento() {
        if (this.tipoAsignacionSeleccionado != null && !medicamentosNegociacionSeleccionados.isEmpty()) {
            List<Long> ids = new ArrayList<>(0);

            //Si la negociación es EVENTO se filtran los medicamentos regulados que tengan un valor negociado mayor al valor
            //referente
            if(tecnologiasController.getNegociacion().getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.EVENTO)) {
            	try {
            		medicamentosNegociacionSeleccionados = validarMedicamentosRegulados(medicamentosNegociacionSeleccionados, true);
				} catch (ConexiaSystemException e) {
					facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
					logger.error("Error al validar medicamentos regulados en la negociación: " + tecnologiasController.getNegociacion().getId(), e);
				}

            }

            this.medicamentosNegociacionSeleccionados.forEach((m) -> {
                ids.add(m.getMedicamentoDto().getId());
            });

            if(ids.size() > 0 && !ids.isEmpty()) {
            	switch (this.tipoAsignacionSeleccionado) {
	                case APLICAR_PORCENTAJE:
	                    this.negociacionMedicamentoSSFacade.asignarTarifas(ids,this.tecnologiasController.getNegociacion().getId(),this.tipoAsignacionSeleccionado.getPropiedad()
	                            .replace("porcentaje", porcentajeValor.toString()), this.tecnologiasController.getUser().getId());
	                    break;
	                case TARIFA_EMSSANAR:
	                    this.negociacionMedicamentoSSFacade.asignarTarifas(ids, this.tecnologiasController.getNegociacion().getId(), this.tecnologiasController.getUser().getId());
	                    break;
	                case CONTRATO_ANTERIOR:                        
                            Long negociacionReferenteId = this.negociacionFacade.obtenerNegociacionReferenteId(this.tecnologiasController.getNegociacion().getId());    
                            if (Objects.nonNull(negociacionReferenteId))
                            {
                                this.negociacionMedicamentoSSFacade.asignarValorContratoAnteriorByNegociacionReferente(this.tecnologiasController.getNegociacion().getId(),
                                                                                                                       negociacionReferenteId,
                                                                                                                       medicamentosNegociacionSeleccionados, 
                                                                                                                       this.tecnologiasController.getUser().getId());
                            }else {
                                this.negociacionMedicamentoSSFacade.asignarValorContratoAnterior(this.tecnologiasController.getNegociacion().getId(), 
                                                                                                 medicamentosNegociacionSeleccionados, 
                                                                                                 this.tecnologiasController.getUser().getId());
                            }	                	
	                	break;
	                case APLICAR_REFERENTE:
	                	this.negociacionMedicamentoSSFacade.asignarValorCostoMedio(this.tecnologiasController.getNegociacion().getId(),
	                			medicamentosNegociacionSeleccionados, this.tecnologiasController.getNegociacion().getPoblacion(), true, this.tecnologiasController.getUser().getId());
	                	consultarTotalRias();
	                	Ajax.update("tecnologiasSSForm");
	                	break;
	                default:
	                	this.negociacionMedicamentoSSFacade.asignarTarifas(ids,this.tecnologiasController.getNegociacion().getId(),this.tipoAsignacionSeleccionado.getPropiedad(), this.tecnologiasController.getUser().getId());
	            }

            	this.medicamentosNegociacion = this.negociacionMedicamentoSSFacade.consultarMedicamentosNegociacionNoSedesByNegociacionId(this.tecnologiasController.getNegociacion());
                messages.add(new Message(Message.INFO, "Se han modificado los medicamentos exitosamente"));
            }
            if(!medicamentosReguladosValorInvalido.isEmpty()){
				RequestContext.getCurrentInstance().execute("PF('dialogoMxReguladosNoProcesados').show()");
			}
        } else {
            messages.add(new Message(Message.WARN, "Por favor seleccione medicamentos y una opción para aplicar"));
        }
        refreshData();
    }
    /**
     *
     */
    public void consultarTotalRias(){
    	commonController.calcularTotalRiasByNegociacion(tecnologiasController.getNegociacion().getId());
		tecnologiasController.setListTotalesRias(commonController.consultarTotalRiasByNegociacion(tecnologiasController.getNegociacion().getId()));
	}

    public void gestionarMedicamentos(String nombreTabla, String nombreComboGestion) {
        if (this.gestionSeleccionada != null) {
            if (this.gestionSeleccionada.equals(GestionTecnologiasNegociacionEnum.BORRAR_SELECCIONADOS)) {
                if ((null != medicamentosNegociacionSeleccionados) && (medicamentosNegociacionSeleccionados.size() > 0)) {
                    RequestContext.getCurrentInstance().execute("PF('cdDeleteMed').show();");
                } else {
                    facesMessagesUtils.addInfo(resourceBundle.getString("medicamento_msj_val_med_sel"));
                }
            } else if (this.gestionSeleccionada.equals(GestionTecnologiasNegociacionEnum.SELECCIONAR_TODOS)) {
                Ajax.oncomplete("PF('" + nombreTabla + "').selectAllRows();");
                medicamentosNegociacionSeleccionados = new ArrayList<MedicamentoNegociacionDto>();
                this.medicamentosNegociacionSeleccionados.addAll(this.medicamentosNegociacion);
            } else if (this.gestionSeleccionada.equals(GestionTecnologiasNegociacionEnum.DESELECCIONAR_TODOS)) {
                Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRows();");
                this.medicamentosNegociacionSeleccionados.clear();
            }
            this.gestionSeleccionada = null;
            Ajax.update(nombreComboGestion);
        }
    }

    private boolean validacion() {
        for (Iterator<MedicamentoNegociacionDto> iter = medicamentosNegociacion.listIterator(); iter.hasNext();) {
            MedicamentoNegociacionDto dto = iter.next();
            if ((!dto.getNegociado()) || (dto.getValorNegociado() == null)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Método que guarda el estado actual de los medicamentos.
     * @param agregarMensaje
     */
    public void guardarEstadoMedicamentos(boolean agregarMensaje) {
        try {
            negociacionMedicamentoSSFacade.guardarMedicamentosNegociados(medicamentosNegociacion, tecnologiasController.getNegociacion().getId(), this.tecnologiasController.getUser().getId());
            medicamentosNegociacionOriginal.clear();
            medicamentosNegociacionOriginal.addAll(medicamentosNegociacion);
            if (agregarMensaje) {
                facesMessagesUtils.addInfo("Operación realizada con éxito!");
            }
        } catch (Exception e) {
            logger.error("Error al guardar los medicamentos de la negociacion.", e);
            facesMessagesUtils.addError(resourceBundle.getString("system_error"));
        }

    }

    public void guardarMedicamento(MedicamentoNegociacionDto medicamento) {
        if (Objects.nonNull(medicamento.getMedicamentoDto().getRegulado())) {
            if (medicamento.getMedicamentoDto().getRegulado()) {
                if(medicamento.getValorNegociado()!=null){
                    if(medicamento.getValorNegociado().compareTo(medicamento.getValorReferencia())==1){
                        medicamento.setValorNegociado(medicamento.getValorReferencia());
                        //facesMessagesUtils.addWarning("El valor negociado no puede superar el valor referente para un medicamento regulado");
                        messages.add(new Message(Message.WARN, "El valor negociado no puede superar el valor referente para un medicamento regulado"));
                    }
                }
            }
            guardarValorNegociadoMedicamento(medicamento);
        }
        refreshData();
    }

    /**
     * ALmacena el valor negociado del medicamento y cambia el estado a negociado
     * @param medicamento
     */
    public void guardarValorNegociadoMedicamento(MedicamentoNegociacionDto medicamento) {
        this.negociacionMedicamentoSSFacade.guardarMedicamentoNegociado(medicamento, this.tecnologiasController.getNegociacion().getId(), this.tecnologiasController.getUser().getId());
        messages.add(new Message(Message.INFO,"Se ha guardado correctamente" ));
    }

    /**
     * Almacena los cambios realizados sobre el costo medio usuario de Medicamentos
     * @param medicamento
     */
    public void guardarMedicamentoPgp(MedicamentoNegociacionDto medicamento) {
    	if(Objects.nonNull(medicamento.getCostoMedioUsuario()) &&
    			Objects.nonNull(medicamento.getFrecuenciaReferente())){
    		medicamento.setValorNegociado(medicamento.getCostoMedioUsuario()
    				.multiply(BigDecimal.valueOf(medicamento.getFrecuenciaReferente())
    				.multiply(BigDecimal.valueOf(this.tecnologiasController.getNegociacion().getPoblacion()))));
    		medicamento.setNegociado(Objects.nonNull(medicamento.getValorNegociado()));
    		this.negociacionMedicamentoSSFacade.guardarMedicamentoNegociadoPgp(medicamento, this.tecnologiasController.getNegociacion().getId(), this.tecnologiasController.getUser().getId());
    		consultarTotalRias();
    		Ajax.update("tecnologiasSSForm");
        	facesMessagesUtils.addInfo("Se ha guardado correctamente");
    	}else{
    		facesMessagesUtils.addError("No se puede asigna un valor negociado.");
    	}
    }

    /**
     * Metodo que activa la función de guardar y validar las tarifas negociadas.
     * @param agregarMensaje
     * @return 
     */
    public boolean guardarValidarMedicamentos(boolean agregarMensaje) {
        this.guardarEstadoMedicamentos(agregarMensaje);
        boolean isValid = validacion();
        if (isValid) {
            facesMessagesUtils.addInfo(resourceBundle.getString("medicamento_msj_validacion_no"));
        }
        return !isValid;
    }


    /**
     * Permite consultar los medicamentos asociados al criterio de búsqueda en el aplicativo:
     * ATC
     * Descripción
     * Cum
     */
    public void consultarMedicamentosAgregar() {
        try {
            if ((Objects.nonNull(medicamentoAgregar.getCums()) && medicamentoAgregar.getCums().length() > 0)
                    || (Objects.nonNull(medicamentoAgregar.getAtc()) && medicamentoAgregar.getAtc().length() > 0)
                    || (Objects.nonNull(medicamentoAgregar.getDescripcion()) && medicamentoAgregar.getDescripcion().length() > 0)) {

                medicamentosAgregar.clear();
                medicamentosAgregarSeleccionados.clear();
                medicamentosAgregar.addAll(negociacionMedicamentoSSFacade.consultarMedicamentosAgregar(medicamentoAgregar, tecnologiasController.getNegociacion()));
                if (medicamentosAgregar.size() > 0) {
                    RequestContext.getCurrentInstance().execute("PF('medicamentosAgregarDlg').show()");
                    Ajax.oncomplete("PF('medicamentoAgregarTableWV').filter();");
                } else {
                    messages.add(new Message(Message.WARN, resourceBundle.getString("negociacion_procedimiento_agregar_no_registros")));
                }
            } else {
                messages.add(new Message(Message.WARN, resourceBundle.getString("negociacion_medicamento_agregar_seleccionar")));
            }
            refreshData();
        } catch (ConexiaBusinessException e) {
            logger.error("Error al consultarProcedimientosAgregar", e);
            facesMessagesUtils.addWarning(exceptionUtils.createMessage(resourceBundle, e));
        } catch (Exception e) {
            logger.error("Error al consultarProcedimientosAgregar", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    /**
     * Permite añadir medicamentos en la negociación
     * @param medicamentos
     */
    public void agregarMedicamentoNegociacion(
            List<MedicamentosDto> medicamentos) {
        try {
            if (medicamentos != null && medicamentos.size() > 0) {
                List<Long> ids = medicamentos.stream().map(mx -> mx.getId()).collect(Collectors.toList());
                Long negociacionReferenteId = this.negociacionFacade.obtenerNegociacionReferenteId(this.tecnologiasController.getNegociacion().getId());
                this.negociacionMedicamentoSSFacade.agregarMedicamentosNegociacion(ids, this.tecnologiasController.getNegociacion().getId(), 
                                                                                   this.tecnologiasController.getUser().getId(), negociacionReferenteId);
                messages.add(new Message(Message.INFO, resourceBundle.getString("negociacion_medicamentos_agregar_masivo_ok") + this.funcionListaCodigosMx.apply(medicamentos)));
                RequestContext.getCurrentInstance().execute("PF('medicamentosAgregarDlg').hide();");
                RequestContext.getCurrentInstance().update("tecnologiasSSForm:tabsTecnologias:tabMedicamentos");
                RequestContext.getCurrentInstance().update("tecnologiasSSForm:panelTecnologiasDetalle");
            } else {
                messages.add(new Message(Message.WARN, resourceBundle.getString("negociacion_medicamento_agregar_seleccionar")));
            }
            refreshData();
        } catch (Exception e) {
            logger.error("Error al agregar a la negociación los medicamentos seleccionados", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }

    public void limpiarPanelAgregar(){
		this.medicamentoAgregar = new MedicamentosDto();
	}


    public void importar(FileUploadEvent event) {
        try {
            importar(event, READER_MEDICAMENTOS_XML);
        } catch (IOException | ConexiaSystemException | SAXException | InvalidFormatException e) {
        	logger.error("Error general al importar el archivo", e);
            facesMessagesUtils.addError(resourceBundle.getString("form_label_error_archivo"));
        }
    }

    /**
     * Importa archivos de Medicamentos a la negociacion
     * @param event
     * @param xmlReader
     * @param modalidad
     * @throws IOException
     * @throws ConexiaSystemException
     * @throws SAXException
     * @throws InvalidFormatException
     */
    @Trace(dispatcher = true)
    public void importar(FileUploadEvent event, String xmlReader)throws  IOException, ConexiaSystemException, SAXException, InvalidFormatException{

    	NewRelic.setTransactionName("NegociacionMedicamentoSSController","ImportarMedicamentosNegociacionNoPgp");

    	try {
            UploadedFile file = event.getFile();
            ConcurrentHashMap<String, Object> beans = new ConcurrentHashMap<>();
            List<MedicamentoNegociacionDto> portafolioMedicamentos = new ArrayList<>();
            // Los medicamentos se comportan con procedimientos para capita por Rias
            beans.put("tecnologiasOfertadas", portafolioMedicamentos);
            xlxsFiles.importar(file, xmlReader, beans);
            List<SedesNegociacionDto> listSedeHabilitadasMedicamentos = negociacionMedicamentoSSFacade.consultarSedeNegociacionMedicamentosByNegociacionId(tecnologiasController.getNegociacion().getId());
        	if(Objects.isNull(listSedeHabilitadasMedicamentos) || listSedeHabilitadasMedicamentos.isEmpty()){
                    messages.add(new Message(Message.ERROR,resourceBundle.getString("form_label_negociacion_no_tiene_sedes_habilitadas")));
        	}else{
        		List<NegociacionModalidadEnum> modalidadesRiasCapita = new ArrayList<>(Arrays.asList(NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO, NegociacionModalidadEnum.RIAS_CAPITA));

        		if((modalidadesRiasCapita.contains(tecnologiasController.getNegociacion().getTipoModalidadNegociacion()) &&
        				(portafolioMedicamentos.stream().filter(objMedicamentos->objMedicamentos.getActividad().getDescripcion()==null
        				|| objMedicamentos.getRangoPoblacion().getRangoPoblacion().getDescripcion()==null
        				|| objMedicamentos.getMedicamentoDto().getCums()==null
        				|| objMedicamentos.getRangoPoblacion().getNegociacionRia().getRia().getDescripcion()==null).count()==0))
        			|| !modalidadesRiasCapita.contains(tecnologiasController.getNegociacion().getTipoModalidadNegociacion())) {

        			Integer sizeInicial = 0, sizeFinal = 0;
                	sizeInicial = portafolioMedicamentos.size();

                	//Filtrar mx para la modalidad evento que no cumplan con los requisitos de valor negociado establecidos
                	switch(tecnologiasController.getNegociacion().getTipoModalidadNegociacion()) {
                		case EVENTO:

                			portafolioMedicamentos.removeIf(mx -> (
                						Objects.isNull(mx.getValorPropuestoPortafolio()) ||
                						mx.getValorPropuestoPortafolio().compareTo(new BigDecimal("0")) == 0 ||
                						mx.getValorPropuestoPortafolio().compareTo(new BigDecimal("0")) == -1
                					));

                			break;
                		default:
                			break;
                	}

                	sizeFinal = portafolioMedicamentos.size();

                	if(Objects.nonNull(portafolioMedicamentos) && portafolioMedicamentos.size() > 0) {

               		if(sizeInicial > sizeFinal) {
                                    	messages.add(new Message(Message.ERROR,"Existen medicamentos con valores erróneos en el valor negociado"));
        	        	}
                		this.medicamentosImportar = portafolioMedicamentos;
                		this.listaSedesImportarMedicamentos = listSedeHabilitadasMedicamentos;

                		switch(tecnologiasController.getNegociacion().getTipoModalidadNegociacion()) {
                			case EVENTO:
                				if(existenRegulados) {
                					RequestContext.getCurrentInstance().update("dialogoMxReguladosConfirmarImport");
                	        		RequestContext.getCurrentInstance().execute("PF('dialogoMxReguladosConfirmarImport').show()");
                				} else {
                					this.medicamentosImportar.get(0).setTarifaImportar(TipoAsignacionTarifaMedReguladoEnum.VALOR_ARCHIVO);
                					gestionarMedicamentosImportar();
                				}
                				break;
                			default:
                				gestionarMedicamentosImportar();
                				break;
                		}

                	} else {
                                messages.add(new Message(Message.WARN,"El archivo contiene valores erróneos en el valor negociado de los medicamentos, por favor verifique el archivo"));
                	}
        		}else {
        		        messages.add(new Message(Message.WARN,resourceBundle.getString("error_datos_vacios_importar_medicamentos")));
        		}
        	}
                refreshData();
        } catch (IOException | ConexiaSystemException | SAXException | InvalidFormatException e) {
        	logger.error("Error general al importar el archivo", e);
            facesMessagesUtils.addError(resourceBundle.getString("form_label_error_archivo"));
        }
    }

    public void importFiles(FileUploadEvent event) throws InvalidFormatException, IOException, SAXException, ConexiaBusinessException {

		try {
			typeImport = (ArchivosNegociacionEnum) event.getComponent().getAttributes().get("foo");
			Workbook libro = null;
			File file = null;
			file = File.createTempFile(event.getFile().getFileName(), ".xlsx");
			FileOutputStream fos = new FileOutputStream(file.getAbsolutePath());
			fos.write(event.getFile().getContents());
			fos.close();
			libro = WorkbookFactory.create(new FileInputStream(file));
			try {
				this.gestionarImportMedicamentosEvento(libro, typeImport);
				refreshData();
				Ajax.update("tecnologiasSSForm:tabsTecnologias:erroresMxEvento");
			} catch (ConexiaBusinessException e) {
				logger.error("Error al consultar capítulos de la negociación", e);
			}

		} catch (IOException | ConexiaSystemException e) {
			logger.error("Error general al importar el archivo", e);
			facesMessagesUtils.addError(resourceBundle.getString("form_label_error_archivo"));
		}
	}

    private void gestionarImportMedicamentosEvento(Workbook libro, ArchivosNegociacionEnum typeImport) throws ConexiaBusinessException {
		DataFormatter formatter = new DataFormatter();
		List<ArchivoTecnologiasNegociacionEventoDto> medicamentosEvento = new ArrayList<ArchivoTecnologiasNegociacionEventoDto>();
		List<ErroresImportTecnologiasDto> listErrorFormat = new ArrayList<ErroresImportTecnologiasDto>();
		List<ConcurrentHashMap<ArchivoTecnologiasNegociacionEventoDto, ErrorTecnologiasNegociacionEventoEnum>> errores
				= new ArrayList<ConcurrentHashMap<ArchivoTecnologiasNegociacionEventoDto, ErrorTecnologiasNegociacionEventoEnum>>();
		Sheet hoja = libro.getSheetAt(0);
		listadoErrores = new ArrayList<>();
		Map<String,Long> registrosRepetidos = new HashMap<String, Long>();
		listErrorFormat = validator.validateFormat(hoja, typeImport);

		if (listErrorFormat.isEmpty()) {
			Iterator<Row> iterador = hoja.iterator();
			while (iterador.hasNext()) {
				Row fila = iterador.next();
				if (fila.getRowNum() == 0) {
					continue;
				}

				if(validator.validarCamposFila(fila, typeImport)) {
					ArchivoTecnologiasNegociacionEventoDto mxEvento = new ArchivoTecnologiasNegociacionEventoDto();
					mxEvento.setCodigoMedicamento(formatter.formatCellValue(fila.getCell(0)));
					mxEvento.setValorNegociado(formatter.formatCellValue(fila.getCell(2)));
					mxEvento.setValorImportado(formatter.formatCellValue(fila.getCell(2)));
					mxEvento.setLineaArchivo(fila.getRowNum() + 1);
					medicamentosEvento.add(mxEvento);
				}

			}
			if (medicamentosEvento.isEmpty()) {
                            messages.add(new Message(Message.INFO, "El archivo se encuentra vacio"));
			} else {
				try {
					registrosRepetidos =  validarRegistrosDuplicadosImportarMxEvento(medicamentosEvento);
					if(registrosRepetidos.keySet().size() > 0){
                                            messages.add(new Message(Message.WARN, this.resourceBundle.getString("importacion_archivo_duplicado_msn")));
					}
					else{
						errores = negociacionMedicamentoSSFacade.validarMedicamentoNegociacionEvento(medicamentosEvento, tecnologiasController.getNegociacion(), tecnologiasController.getUser().getId(),Boolean.FALSE);
						medicamentosRegulados = negociacionMedicamentoSSFacade.retornarMedicamentosReguladosImport(medicamentosEvento);
					}

				} catch (ConexiaBusinessException e) {
					this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
					logger.error("Error al importar medicamentos a la negociaón: " + tecnologiasController.getNegociacion().getId(), e);
				}
				if(registrosRepetidos.keySet().isEmpty()){
					if(Objects.nonNull(errores) && !errores.isEmpty()){
						errores.forEach(errorLinea ->
										errorLinea.forEach((archivo, errorEnum) -> listadoErrores.add(
												new ErroresImportTecnologiasEventoDto(
														errorEnum.getCodigo(),
														errorEnum.getMensaje(),
														archivo.getCodigoHabilitacionSede(),
														archivo.getCodigoMedicamento(),
														archivo.getLineaArchivo()))));
						messages.add(new Message(Message.INFO, this.resourceBundle.getString("importacion_con_errores_msn")));
					}
					else {
						messages.add(new Message(Message.INFO, this.resourceBundle.getString("importacion_exitosa_msn")));
					}
				}

				if (!listadoErrores.isEmpty()) {
					RequestContext.getCurrentInstance().execute("PF('dlgMxNoProcesadosEventoW').show()");
				}
			}
		}
		else {
					messages.add(new Message(Message.WARN,this.resourceBundle.getString("importacion_archivo_no_formato_msn")));
		}

		if(Objects.nonNull(medicamentosRegulados)){
			if(!medicamentosRegulados.isEmpty()){
				RequestContext.getCurrentInstance().update("dialogoMxReguladosConfirmarImport");
	    		RequestContext.getCurrentInstance().execute("PF('dialogoMxReguladosConfirmarImport').show()");
			}
		}

	}

    private Map<String, Long> validarRegistrosDuplicadosImportarMxEvento(List<ArchivoTecnologiasNegociacionEventoDto> medicamentosEvento ){
    	Map<String, Long> registroCantidad = new HashMap<String, Long>();
		for(ArchivoTecnologiasNegociacionEventoDto mx : medicamentosEvento){

			if (!registroCantidad.containsKey(mx.getCodigoMedicamento())) {
				registroCantidad.put(mx.getCodigoMedicamento(), medicamentosEvento.stream()
						.filter(p -> p.getCodigoMedicamento().equals(mx.getCodigoMedicamento())).count());
			}

		}
		return (Map<String, Long>) registroCantidad.entrySet().stream().filter(v -> v.getValue() > 1L).collect(Collectors.toMap(v -> v.getKey(), v -> v.getValue()));
    }
    /**
     * Asigna la opción de tarifa a importar seleccionada por el usuario y llama el método
     * para el cargue de los medicamentos
     * @throws ConexiaBusinessException
     */
    public void importarMedicamentosTarifa() throws ConexiaBusinessException {
    	Boolean importRegulados = Boolean.TRUE;
    	List<ArchivoTecnologiasNegociacionEventoDto> medicamentosRegulados  = asignarValoresMedicamentosReguladosImportar(this.medicamentosRegulados);
    	List<ConcurrentHashMap<ArchivoTecnologiasNegociacionEventoDto, ErrorTecnologiasNegociacionEventoEnum>> errores
				= new ArrayList<ConcurrentHashMap<ArchivoTecnologiasNegociacionEventoDto, ErrorTecnologiasNegociacionEventoEnum>>();
    	if(medicamentosRegulados.isEmpty()){
    		this.facesMessagesUtils.addInfo(this.resourceBundle.getString("importacion_sin_accion_regulados"));
    	}
    	else{
        	errores = negociacionMedicamentoSSFacade.validarMedicamentoNegociacionEvento(medicamentosRegulados, tecnologiasController.getNegociacion(), tecnologiasController.getUser().getId(),importRegulados);
        	if (!listadoErrores.isEmpty()) {
    			RequestContext.getCurrentInstance().execute("PF('dlgMxNoProcesadosEventoW').show()");
    			this.facesMessagesUtils.addInfo(this.resourceBundle.getString("importacion_con_errores_msn"));
    		}
        	else {
        		this.facesMessagesUtils.addInfo(this.resourceBundle.getString("importacion_exitosa_msn"));
        	}
    	}
    	this.medicamentosRegulados.clear();
    	RequestContext.getCurrentInstance().update("dialogoMxReguladosConfirmarImport");
    	RequestContext.getCurrentInstance().execute("PF('dialogoMxReguladosConfirmarImport').hide()");
		this.postconstruct();
		Ajax.update("tecnologiasSSForm:tabsTecnologias:erroresMxEvento");
    }

    /**
     * Gestiona el cargue de los medicamentos que pasan las validaciones iniciales
     */
    public void gestionarMedicamentosImportar() {

		//Realiza la gestion de los medicamentos
		gestionarImportarXlxs.gestionarMedicamentosXlxs(this.medicamentosImportar,
					this.listaSedesImportarMedicamentos,
					this.tecnologiasController.getUser().getId(),
					tecnologiasController.getNegociacion(),
					tecnologiasController.getNegociacion().getTipoModalidadNegociacion());
		messages.add(new Message(Message.INFO, resourceBundle.getString("form_label_archivo_importado_correctamente")));

		this.medicamentosImportar.clear();
		this.listaSedesImportarMedicamentos.clear();

		postconstruct();
    }

    /**
     * Filtro especial de la tabla medicamentos
     *
     * @param tipo identifica por que campo filtrar
     */
    public void filtroEspecial(TiposDiferenciaNegociacionEnum tipo, String nombreTabla) {
        try {
            this.medicamentosNegociacion.clear();
            this.medicamentosNegociacion.addAll(medicamentosNegociacionOriginal);
            FiltroEspecialEnum seleccionado = null;
            switch(tipo) {
				case DIFERENCIA_PORCENTAJE:
					seleccionado = filtroEspecialSeleccionadoPorcentaje;
					break;
				case DIFERENCIA_PORCENTAJE_REFERENCIA:
					seleccionado = filtroEspecialSeleccionadoPorcentajeReferencia;
					break;
				case DIFERENCIA_VALOR:
					seleccionado = filtroEspecialSeleccionadoValor;
					break;
				case DIFERENCIA_VALOR_REFERENCIA:
					seleccionado = filtroEspecialSeleccionadoValorReferencia;
					break;
				default:
					break;

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
            // TODO:ajustar excepciones
        } catch (Exception e) {
            logger.error("Error en filtro especial", e);
            facesMessagesUtils.addError(exceptionUtils
                    .createSystemErrorMessage(resourceBundle));
        } finally {
            Ajax.update("tecnologiasSSForm:tabsTecnologias");
        }
    }

    /**
     * Filtra por un rango
     *
     * @param tipo
     */
    public void filtrarEntre(TiposDiferenciaNegociacionEnum tipo) {
        String[] filtros;
        switch(tipo) {
			case DIFERENCIA_PORCENTAJE:
				filtros = this.filtroPorcentaje.split(",");
	            this.medicamentosNegociacion = medicamentosNegociacion
	                    .stream()
	                    .filter(m -> (m.getDiferenciaPorcentajeContratado() != null
	                            && m.getDiferenciaPorcentajeContratado() < Double
	                            .parseDouble(filtros[1]) && m
	                            .getDiferenciaPorcentajeContratado() > Double
	                            .parseDouble(filtros[0])))
	                    .collect(Collectors.toList());
				break;
			case DIFERENCIA_PORCENTAJE_REFERENCIA:
				filtros = this.filtroPorcentajeReferencia.split(",");
	            this.medicamentosNegociacion = medicamentosNegociacion
	                    .stream()
	                    .filter(m -> (m.getDiferenciaPorcentajeReferencia() != null
	                            && m.getDiferenciaPorcentajeReferencia() < Double
	                            .parseDouble(filtros[1]) && m
	                            .getDiferenciaPorcentajeReferencia() > Double
	                            .parseDouble(filtros[0])))
	                    .collect(Collectors.toList());
				break;
			case DIFERENCIA_VALOR:
				filtros = this.filtroValor.split(",");
	            this.medicamentosNegociacion = medicamentosNegociacion
	                    .stream()
	                    .filter(m -> (m.getDiferenciaValorContratado() != null
	                            && m.getDiferenciaValorContratado() < Double
	                            .parseDouble(filtros[1]) && m
	                            .getDiferenciaValorContratado() > Double
	                            .parseDouble(filtros[0])))
	                    .collect(Collectors.toList());
				break;
			case DIFERENCIA_VALOR_REFERENCIA:
				filtros = this.filtroValorReferencia.split(",");
	            this.medicamentosNegociacion = medicamentosNegociacion
	                    .stream()
	                    .filter(m -> (m.getDiferenciaValorReferencia() != null
	                            && m.getDiferenciaValorReferencia() < Double
	                            .parseDouble(filtros[1]) && m
	                            .getDiferenciaValorReferencia() > Double
	                            .parseDouble(filtros[0])))
	                    .collect(Collectors.toList());
				break;
			default:
				break;

        }
    }

    /**
     * Filtra datos iguales
     *
     * @param tipo
     */
    public void filtrarIgual(TiposDiferenciaNegociacionEnum tipo) {
        switch(tipo) {
			case DIFERENCIA_PORCENTAJE:
				this.medicamentosNegociacion = medicamentosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaPorcentajeContratado() != null && m
                        .getDiferenciaPorcentajeContratado() == Double
                        .parseDouble(this.filtroPorcentaje)))
                .collect(Collectors.toList());
				break;
			case DIFERENCIA_PORCENTAJE_REFERENCIA:
				this.medicamentosNegociacion = medicamentosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaPorcentajeReferencia() != null && m
                        .getDiferenciaPorcentajeReferencia() == Double
                        .parseDouble(this.filtroPorcentajeReferencia)))
                .collect(Collectors.toList());
				break;
			case DIFERENCIA_VALOR:
				this.medicamentosNegociacion = medicamentosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaValorContratado() != null && m
                        .getDiferenciaValorContratado() == Double
                        .parseDouble(this.filtroValor)))
                .collect(Collectors.toList());
				break;
			case DIFERENCIA_VALOR_REFERENCIA:
				this.medicamentosNegociacion = medicamentosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaValorReferencia() != null && m.getDiferenciaValorReferencia() == Double.parseDouble(this.filtroValorReferencia)))
                .collect(Collectors.toList());
				break;
			default:
				break;

	    }
    }

    /**
     * Filtra datos mayores
     *
     * @param tipo
     */
    public void filtrarMayor(TiposDiferenciaNegociacionEnum tipo) {
        switch(tipo) {
			case DIFERENCIA_PORCENTAJE:
				this.medicamentosNegociacion = medicamentosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaPorcentajeContratado() != null && m
                        .getDiferenciaPorcentajeContratado() > Double
                        .parseDouble(filtroPorcentaje)))
                .collect(Collectors.toList());
				break;
			case DIFERENCIA_PORCENTAJE_REFERENCIA:
				this.medicamentosNegociacion = medicamentosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaPorcentajeReferencia() != null && m
                        .getDiferenciaPorcentajeReferencia() > Double
                        .parseDouble(filtroPorcentajeReferencia)))
                .collect(Collectors.toList());
				break;
			case DIFERENCIA_VALOR:
				this.medicamentosNegociacion = medicamentosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaValorContratado() != null && m
                        .getDiferenciaValorContratado() > Double
                        .parseDouble(filtroValor)))
                .collect(Collectors.toList());
				break;
			case DIFERENCIA_VALOR_REFERENCIA:
				this.medicamentosNegociacion = medicamentosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaValorReferencia() != null && m
                        .getDiferenciaValorReferencia() > Double
                        .parseDouble(filtroValorReferencia)))
                .collect(Collectors.toList());
				break;
			default:
				break;

	    }
    }

    /**
     * Filtra datos menores
     *
     * @param tipo
     */
    public void filtrarMenor(TiposDiferenciaNegociacionEnum tipo) {
        switch(tipo) {
			case DIFERENCIA_PORCENTAJE:
				this.medicamentosNegociacion = medicamentosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaPorcentajeContratado() != null && m
                        .getDiferenciaPorcentajeContratado() < Double
                        .parseDouble(filtroPorcentaje)))
                .collect(Collectors.toList());
				break;
			case DIFERENCIA_PORCENTAJE_REFERENCIA:
				this.medicamentosNegociacion = medicamentosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaPorcentajeReferencia() != null && m
                        .getDiferenciaPorcentajeReferencia() < Double
                        .parseDouble(filtroPorcentajeReferencia)))
                .collect(Collectors.toList());
				break;
			case DIFERENCIA_VALOR:
				this.medicamentosNegociacion = medicamentosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaValorContratado() != null && m
                        .getDiferenciaValorContratado() < Double
                        .parseDouble(filtroValor)))
                .collect(Collectors.toList());
				break;
			case DIFERENCIA_VALOR_REFERENCIA:
				this.medicamentosNegociacion = medicamentosNegociacion
                .stream()
                .filter(m -> (m.getDiferenciaValorReferencia() != null && m
                        .getDiferenciaValorReferencia() < Double
                        .parseDouble(filtroValorReferencia)))
                .collect(Collectors.toList());
				break;
			default:
				break;

	    }
    }
    /**
     * Actualiza el valor referente de los medicamentos con el cambio de Poblacion
     */
    public void actualizarValorNegociado(){
    	this.medicamentosNegociacionSeleccionados = this.medicamentosNegociacion;
    	this.negociacionMedicamentoSSFacade.asignarValorCostoMedio(this.tecnologiasController.getNegociacion().getId(),
    			medicamentosNegociacionSeleccionados, this.tecnologiasController.getNegociacion().getPoblacion(), false, this.tecnologiasController.getUser().getId());
    	this.medicamentosNegociacionSeleccionados = new ArrayList<>();
    }

    public void listarMedicamentosReguladosNegociacion(){
    	medicamentosReguladosNegociacion = this.negociacionMedicamentoSSFacade.listarMedicamentosReguladosNegociado(this.tecnologiasController.getNegociacion().getId());
    }

    public boolean getTieneServicioFarmaceuticoHabilitado(){
    	return negociacionMedicamentoSSFacade.tieneServicioFarmaceuticoHabilitado(tecnologiasController.getNegociacion().getId());
    }

    public void onToggle(ToggleEvent e) {
        listToggler.set((Integer) e.getData(), e.getVisibility() == Visibility.VISIBLE);
    }

    public List<MedicamentoNegociacionDto> getMedicamentosNegociacion() {
        return medicamentosNegociacion;
    }

    public void setMedicamentosNegociacion(
            List<MedicamentoNegociacionDto> medicamentosNegociacion) {
        this.medicamentosNegociacion = medicamentosNegociacion;
    }

    public List<MedicamentoNegociacionDto> getMedicamentosNegociacionSeleccionados() {
        return medicamentosNegociacionSeleccionados;
    }

    public void setMedicamentosNegociacionSeleccionados(
            List<MedicamentoNegociacionDto> medicamentosNegociacionSeleccionados) {
        this.medicamentosNegociacionSeleccionados = medicamentosNegociacionSeleccionados;
    }

    public GestionTecnologiasNegociacionEnum[] getGestionTecnologiasNegociacion() {
        return GestionTecnologiasNegociacionEnum.values();
    }

    public TipoAsignacionTarifaMedEnum[] getTipoAsignacionTarifaMedEnum() {
        return TipoAsignacionTarifaMedEnum.values();
    }

    public TipoAsignacionTarifaMedReguladoEnum[] getTipoAsignacionTarifaMedReguladoEnum() {
        return TipoAsignacionTarifaMedReguladoEnum.values();
    }

    public GestionTecnologiasNegociacionEnum getGestionSeleccionada() {
        return gestionSeleccionada;
    }

    public void setGestionSeleccionada(
            GestionTecnologiasNegociacionEnum gestionSeleccionada) {
        this.gestionSeleccionada = gestionSeleccionada;
    }

    public TipoAsignacionTarifaMedEnum getTipoAsignacionSeleccionado() {
        return tipoAsignacionSeleccionado;
    }

    public void setTipoAsignacionSeleccionado(
            TipoAsignacionTarifaMedEnum tipoAsignacionSeleccionado) {
        this.tipoAsignacionSeleccionado = tipoAsignacionSeleccionado;
    }

    public Double getPorcentajeValor() {
        return porcentajeValor;
    }

    public void setPorcentajeValor(Double porcentajeValor) {
        this.porcentajeValor = porcentajeValor;
    }

    public List<MedicamentoNegociacionDto> getMedicamentosNegociacionOriginal() {
        return medicamentosNegociacionOriginal;
    }

    public void setMedicamentosNegociacionOriginal(
            List<MedicamentoNegociacionDto> medicamentosNegociacionOriginal) {
        this.medicamentosNegociacionOriginal = medicamentosNegociacionOriginal;
    }

    /**
     * @return the filtroValor
     */
    public String getFiltroValor() {
        return filtroValor;
    }

    /**
     * @param filtroValor the filtroValor to set
     */
    public void setFiltroValor(String filtroValor) {
        this.filtroValor = filtroValor;
    }

    /**
     * @return the filtroPorcentaje
     */
    public String getFiltroPorcentaje() {
        return filtroPorcentaje;
    }

    /**
     * @param filtroPorcentaje the filtroPorcentaje to set
     */
    public void setFiltroPorcentaje(String filtroPorcentaje) {
        this.filtroPorcentaje = filtroPorcentaje;
    }

    /**
     * @return the diferenciaValor
     */
    public TiposDiferenciaNegociacionEnum getDiferenciaValor() {
        return diferenciaValor;
    }

    /**
     * @return the diferenciaPorcentaje
     */
    public TiposDiferenciaNegociacionEnum getDiferenciaPorcentaje() {
        return diferenciaPorcentaje;
    }

    public FiltroEspecialEnum[] getFiltroEspecialEnum() {
        return FiltroEspecialEnum.values();
    }

    /**
     * @return the filtroEspecialSeleccionadoValor
     */
    public FiltroEspecialEnum getFiltroEspecialSeleccionadoValor() {
        return filtroEspecialSeleccionadoValor;
    }

    /**
     * @param filtroEspecialSeleccionadoValor the
     * filtroEspecialSeleccionadoValor to set
     */
    public void setFiltroEspecialSeleccionadoValor(
            FiltroEspecialEnum filtroEspecialSeleccionadoValor) {
        this.filtroEspecialSeleccionadoValor = filtroEspecialSeleccionadoValor;
    }

    /**
     * @return the filtroEspecialSeleccionadoPorcentaje
     */
    public FiltroEspecialEnum getFiltroEspecialSeleccionadoPorcentaje() {
        return filtroEspecialSeleccionadoPorcentaje;
    }

    /**
     * @param filtroEspecialSeleccionadoPorcentaje the
     * filtroEspecialSeleccionadoPorcentaje to set
     */
    public void setFiltroEspecialSeleccionadoPorcentaje(
            FiltroEspecialEnum filtroEspecialSeleccionadoPorcentaje) {
        this.filtroEspecialSeleccionadoPorcentaje = filtroEspecialSeleccionadoPorcentaje;
    }

    public void marcarDesmarcarNegociado(MedicamentoNegociacionDto dto) {
        if (dto.getValorNegociado() != null) {
            dto.setNegociado(true);
        } else {
            dto.setNegociado(false);
        }
    }


    public List<Boolean> getListToggler() {
        return listToggler;
    }

    public void setListToggler(List<Boolean> listToggler) {
        this.listToggler = listToggler;
    }

	public Boolean getPgp() {
		return pgp;
	}

	public void setPgp(Boolean pgp) {
		this.pgp = pgp;
	}

	public List<MedicamentosDto> getMedicamentosAgregar() {
		return medicamentosAgregar;
	}

	public void setMedicamentosAgregar(List<MedicamentosDto> medicamentosAgregar) {
		this.medicamentosAgregar = medicamentosAgregar;
	}

	public List<MedicamentosDto> getMedicamentosAgregarSeleccionados() {
		return medicamentosAgregarSeleccionados;
	}

	public void setMedicamentosAgregarSeleccionados(List<MedicamentosDto> medicamentosAgregarSeleccionados) {
		this.medicamentosAgregarSeleccionados = medicamentosAgregarSeleccionados;
	}

	public MedicamentosDto getMedicamentoAgregar() {
		return medicamentoAgregar;
	}

	public void setMedicamentoAgregar(MedicamentosDto medicamentoAgregar) {
		this.medicamentoAgregar = medicamentoAgregar;
	}

	public Function<List<MedicamentosDto>, String> getFuncionListaCodigosMx() {
		return funcionListaCodigosMx;
	}

	public void setFuncionListaCodigosMx(Function<List<MedicamentosDto>, String> funcionListaCodigosMx) {
		this.funcionListaCodigosMx = funcionListaCodigosMx;
	}

	public List<ErroresImportTecnologiasEventoDto> getListadoErrores() {
		return listadoErrores;
	}

	public void setListadoErrores(List<ErroresImportTecnologiasEventoDto> listadoErrores) {
		this.listadoErrores = listadoErrores;
	}



	public List<MedicamentoNegociacionDto> getMedicamentosReguladosValorInvalido() {
		return medicamentosReguladosValorInvalido;
	}

	public void setMedicamentosReguladosValorInvalido(List<MedicamentoNegociacionDto> medicamentosReguladosValorInvalido) {
		this.medicamentosReguladosValorInvalido = medicamentosReguladosValorInvalido;
	}

	public TipoAsignacionTarifaMedReguladoEnum getTipoAsignacionImportar() {
		return tipoAsignacionImportar;
	}

	public void setTipoAsignacionImportar(TipoAsignacionTarifaMedReguladoEnum tipoAsignacionImportar) {
		this.tipoAsignacionImportar = tipoAsignacionImportar;
	}

	public TiposDiferenciaNegociacionEnum getDiferenciaValorReferencia() {
		return diferenciaValorReferencia;
	}

	public void setDiferenciaValorReferencia(TiposDiferenciaNegociacionEnum diferenciaValorReferencia) {
		this.diferenciaValorReferencia = diferenciaValorReferencia;
	}

	public TiposDiferenciaNegociacionEnum getDiferenciaPorcentajeReferencia() {
		return diferenciaPorcentajeReferencia;
	}

	public void setDiferenciaPorcentajeReferencia(TiposDiferenciaNegociacionEnum diferenciaPorcentajeReferencia) {
		this.diferenciaPorcentajeReferencia = diferenciaPorcentajeReferencia;
	}

	public FiltroEspecialEnum getFiltroEspecialSeleccionadoValorReferencia() {
		return filtroEspecialSeleccionadoValorReferencia;
	}

	public void setFiltroEspecialSeleccionadoValorReferencia(FiltroEspecialEnum filtroEspecialSeleccionadoValorReferencia) {
		this.filtroEspecialSeleccionadoValorReferencia = filtroEspecialSeleccionadoValorReferencia;
	}

	public FiltroEspecialEnum getFiltroEspecialSeleccionadoPorcentajeReferencia() {
		return filtroEspecialSeleccionadoPorcentajeReferencia;
	}

	public void setFiltroEspecialSeleccionadoPorcentajeReferencia(
			FiltroEspecialEnum filtroEspecialSeleccionadoPorcentajeReferencia) {
		this.filtroEspecialSeleccionadoPorcentajeReferencia = filtroEspecialSeleccionadoPorcentajeReferencia;
	}

	public String getFiltroValorReferencia() {
		return filtroValorReferencia;
	}

	public void setFiltroValorReferencia(String filtroValorReferencia) {
		this.filtroValorReferencia = filtroValorReferencia;
	}

	public String getFiltroPorcentajeReferencia() {
		return filtroPorcentajeReferencia;
	}

	public void setFiltroPorcentajeReferencia(String filtroPorcentajeReferencia) {
		this.filtroPorcentajeReferencia = filtroPorcentajeReferencia;
	}

	public List<ArchivoTecnologiasNegociacionEventoDto> getMedicamentosRegulados() {
		return medicamentosRegulados;
	}

	public void setMedicamentosRegulados(List<ArchivoTecnologiasNegociacionEventoDto> medicamentosRegulados) {
		this.medicamentosRegulados = medicamentosRegulados;
	}

	public List<MedicamentoNegociacionDto> getMedicamentosReguladosNegociacion() {
		return medicamentosReguladosNegociacion;
	}

	public void setMedicamentosReguladosNegociacion(List<MedicamentoNegociacionDto> medicamentosReguladosNegociacion) {
		this.medicamentosReguladosNegociacion = medicamentosReguladosNegociacion;
	}

    public void showMessages() {
        if (!messages.isEmpty()) {
            for (int i = 0; i < messages.size(); i++) {
                Message m = messages.get(i);
                if (m.tipo == Message.INFO) {
                    facesMessagesUtils.addInfo(m.message);
                } else if (m.tipo == Message.WARN) {
                    facesMessagesUtils.addWarning(m.message);
                }else if (m.tipo == Message.ERROR) {
                    facesMessagesUtils.addError(m.message);
                }
            }
            messages.clear();
        }
    }
        
        private static class Message {
            public static int INFO=1;
            public static int WARN=2;
            public static int ERROR=3;
            int tipo;
            String message;

        public Message(int tipo, String message) {
            this.tipo = tipo;
            this.message = message;
        }
            
            
        }
        
}
