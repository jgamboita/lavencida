package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;

import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.GestionTecnologiasNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contratacion.commons.constants.enums.TipoAsignacionTarifaMedEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.negociacion.GrupoTerapeuticoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import co.conexia.negociacion.wap.controller.common.CommonController;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionMedicamentoSSFacade;

/**
 *
 * @author clozano
 *
 */

@Named
@ViewScoped
@URLMapping(id = "negociacionMedicamentoPGP", pattern = "/negociacionMedicamentoPGP", viewId = "/negociacion/modalidad/sedeasede/medicamentos/negociacionMedicamentoPGP.page")
public class NegociacionMedicamentosPgpSSController implements Serializable {


	/**
	 *
	 */
	private static final long serialVersionUID = 6685579532188689209L;

	@Inject
    private Log logger;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    private FacesUtils facesUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private PreContractualExceptionUtils exceptionUtils;

    @Inject
    private NegociacionMedicamentoSSFacade negociacionMedicamentoSSFacade;

    @Inject
	private CommonController commonController;

    @Inject
    /**
     * Se injecta el controler padre que contiene todos los tabs de tecnologías
     * y la negociación sacada de sesion. *
     */
    private TecnologiasSSController tecnologiasController;

    @Inject
    private NegociacionGrupoTerapeuticoPGPController negociacionGrupoController;



    private List<MedicamentoNegociacionDto> medicamentosNegociacion;

    private List<MedicamentoNegociacionDto> medicamentosNegociacionOriginal;

    private List<MedicamentoNegociacionDto> medicamentosNegociacionSeleccionados;

    private GestionTecnologiasNegociacionEnum gestionSeleccionada;

    private TipoAsignacionTarifaMedEnum tipoAsignacionSeleccionado;

    private List<Boolean> listToggler;

    private GrupoTerapeuticoNegociacionDto grupoTerapeutico;

    /**
     * Para aplicar franja riesgo masivo
     */
    private BigDecimal franjaInicio;
    private BigDecimal franjaFin;


    @PostConstruct
    @Trace(dispatcher = true)
    public void postconstruct() {

    	NewRelic.setTransactionName("NegociacionMedicamentosPgpSSController","CargarMedicamentosGrupoTerapeuticoPgp");

    	FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);

		try {
			if (tecnologiasController.getNegociacion() != null) {

	        	grupoTerapeutico = (GrupoTerapeuticoNegociacionDto) session.getAttribute(NegociacionSessionEnum.GRUPO_TERAPEUTICO_ID.toString());

	        	if(Objects.nonNull(grupoTerapeutico)) {
	        		this.medicamentosNegociacion = negociacionMedicamentoSSFacade.
	                		consultarMedicamentosNegociacionPGPNoSedesByNegociacionAndGrupoId(tecnologiasController.getNegociacion().getId(),
	                				grupoTerapeutico.getCategoriaMedicamento().getId());
	                medicamentosNegociacionOriginal = new ArrayList<>();
	                this.medicamentosNegociacionSeleccionados = new ArrayList<>();
	                if(Objects.nonNull(medicamentosNegociacion)){
	                	this.medicamentosNegociacionOriginal.addAll(medicamentosNegociacion);
	                }
	        	}

	            //Se agrega la configuracion por defecto
	            listToggler = Arrays.asList(true, true, false, true, true, false, true, true, true, true, true, true, true, true);
	        }
		} catch (ConexiaBusinessException ex) {
			this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
			logger.error("Error al consultar los medicamentos por negociación y grupo terapéutico", ex);
		}

    }

    /**
     * Permite gestionar los medicamentos para realizar acciones de selección, deselección y eliminación
     * @param nombreTabla
     * @param nombreComboGestion
     */
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

    /**
     * Asigna las tarifas del referente guardado en la negociación en los medicamentos seleccionados
     */
    public void asignarTarifasMedicamento() {
        if (Objects.nonNull(this.tipoAsignacionSeleccionado) && !medicamentosNegociacionSeleccionados.isEmpty()) {
        	try {
        		//Se valida que exista población asignada a la negociación

		            switch (this.tipoAsignacionSeleccionado) {
		                case APLICAR_REFERENTE:
		                	if(this.tecnologiasController.getNegociacion().getPoblacion() > 0) {
			                	medicamentosNegociacionSeleccionados.stream().filter((dto) ->
			    					(Objects.nonNull(Objects.nonNull(dto.getCostoMedioUsuarioReferente())))
			    					 && Objects.nonNull(dto.getFrecuenciaReferente()))
			    					.forEach((dto)->{
			    						dto.setFrecuencia(dto.getFrecuenciaReferente());
			    						dto.setCostoMedioUsuario(dto.getCostoMedioUsuarioReferente());
			    						dto.setValorNegociado(this.tecnologiasController.calcularValorNegociadoPGP(
			    								dto.getCostoMedioUsuario()
			    								,BigDecimal.valueOf(dto.getFrecuencia())
			    								,BigDecimal.valueOf(this.tecnologiasController.getNegociacion().getPoblacion())));
			    						dto.setPorcentajeNegociado(BigDecimal.ZERO);
			    					    dto.setNegociado(true);
			    					});


									this.negociacionMedicamentoSSFacade.guardarValorReferenteMedicamentosPGP(
				                			this.tecnologiasController.getNegociacion().getId(),
				                			grupoTerapeutico.getCategoriaMedicamento().getId(),
				                			medicamentosNegociacionSeleccionados,
				                			this.tecnologiasController.getUser().getId());

									Ajax.oncomplete("PF('negociacionMedicamentosPGP').unselectAllRows();");
					                this.medicamentosNegociacionSeleccionados.clear();

					                negociacionGrupoController.calcularValorTotal();
		                	} else {
		    					this.facesMessagesUtils.addWarning("Por favor asigne una población a la negociación");
		    				}
		                	break;
		                case FRANJA_RIESGO:
		                	if(tecnologiasController.validarRangoFranjaRiesgo(franjaInicio, franjaFin)) {

		    					List<Long> medicamentoIds = new ArrayList<Long>();

		    					medicamentosNegociacionSeleccionados.stream().forEach(
		    								med ->{
		    									medicamentoIds.add(med.getMedicamentoDto().getId());
		    								}
		    							);

		    					if(Objects.nonNull(medicamentoIds) && medicamentoIds.size() > 0) {
		    						negociacionMedicamentoSSFacade.guardarMedicamentosFranjaPGP(
		    								this.tecnologiasController.getNegociacion().getId(),
		    								medicamentoIds,
		    								this.grupoTerapeutico.getCategoriaMedicamento().getId(),
		    								franjaInicio,
		    								franjaFin,
		    								this.tecnologiasController.getUser().getId());

		    						this.franjaInicio=null;
		    			            this.franjaFin=null;

		    					}


		    				} else {
		    					this.facesMessagesUtils.addWarning("Por favor indique un rango de franja riesgo válido");
		    				}
		                	break;
		                default:
		                	break;
		            }
		            this.tipoAsignacionSeleccionado = null;
		            this.medicamentosNegociacion = negociacionMedicamentoSSFacade.
		            		consultarMedicamentosNegociacionPGPNoSedesByNegociacionAndGrupoId(tecnologiasController.getNegociacion().getId(),
		            				grupoTerapeutico.getCategoriaMedicamento().getId());
		            this.facesMessagesUtils.addInfo("Se han modificado los medicamentos exitosamente");
		            this.medicamentosNegociacionSeleccionados.clear();
		            Ajax.update("negociacionMedicamentoPGPForm:negociacionMedicamentosPGP");

			} catch (ConexiaBusinessException e) {
				this.facesMessagesUtils.addError("Error al guardar el valor de los medicamentos");
				logger.error("Error al aplicar masivos en medicamentos seleccionados", e);
			}
        } else {
            this.facesMessagesUtils.addWarning("Por favor seleccione al menos un medicamento");
        }

    }


    /**
	 * Almacena el calculo de valores PGP
	 * @param medicamentoDto
	 */
    public void guardarValorMedicamento(MedicamentoNegociacionDto medicamentoDto) {
		try {
	    	if (Objects.nonNull(this.tecnologiasController.getNegociacion()) && (this.medicamentosNegociacion != null || this.grupoTerapeutico != null)) {
	    		medicamentoDto.setValorNegociado(this.tecnologiasController.calcularValorNegociadoPGP(
	    				medicamentoDto.getCostoMedioUsuario()
						,BigDecimal.valueOf(medicamentoDto.getFrecuencia())
						,BigDecimal.valueOf(this.tecnologiasController.getNegociacion().getPoblacion())));
	    		medicamentoDto.setNegociado(Objects.nonNull(medicamentoDto.getValorNegociado()) ? true : false);
	    		List<MedicamentoNegociacionDto> listTemporal = new ArrayList<MedicamentoNegociacionDto>();
	    		listTemporal.add(medicamentoDto);

	    				this.negociacionMedicamentoSSFacade.guardarValorReferenteMedicamentosPGP(
	                			this.tecnologiasController.getNegociacion().getId(),
	                			grupoTerapeutico.getCategoriaMedicamento().getId(),
	                			listTemporal,
	                			this.tecnologiasController.getUser().getId());

	    		negociacionGrupoController.calcularValorTotal();
				facesMessagesUtils.addInfo("Operación realizada con éxito!");
			}
		} catch (ConexiaBusinessException e) {
			facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
			logger.error("Error al aplicar valores referente al medicamento en negociación", e);
		}
    }


    /**
     * Permite asignar el rango franja riesgo al medicamento en negociación PGP
     * @param medicamentoDto
     * @param opt -- campo origen (franjaInicio, franjaFin)
     */
    public void guardarFranjaMedicamento(MedicamentoNegociacionDto medicamentoDto, Integer opt) {
    	if (Objects.nonNull(this.tecnologiasController.getNegociacion()) && Objects.nonNull(grupoTerapeutico)) {

    		if(tecnologiasController.validarRangoFranjaRiesgo(medicamentoDto.getFranjaInicio(), medicamentoDto.getFranjaFin())) {
    			aplicarFranja(medicamentoDto);
    		} else {
    			try {
    				BigDecimal valor = tecnologiasController.validarRangoFranjaByCampo(opt, medicamentoDto.getFranjaInicio(), medicamentoDto.getFranjaFin());

    				if(valor.compareTo(BigDecimal.ZERO) == 1) {
        				//Si la validación arroja un valor diferente de cero se asigna al campo contrario al campo de origen el valor
    					//arrojado por la función de validación
        				switch(opt) {
        					case 1:
        						medicamentoDto.setFranjaFin(valor);
        						break;
        					case 2:
        						medicamentoDto.setFranjaInicio(valor);
        						break;
        					default:
        						break;
        				}
        				aplicarFranja(medicamentoDto);

        			} else {
        				facesMessagesUtils.addWarning("Por favor asigne un rango de franja riesgo válido");
        			}
				} catch (NullPointerException e) {
					logger.error("Error al validar la aplicación de franja riesgo uno a uno en medicamento: "+medicamentoDto.getMedicamentoDto().getId(), e);
				}
    		}

		}
    }

    /**
     * Encargado de aplicar la franja al medicamento
     * @param medicamentoDto
     */
    private void aplicarFranja(MedicamentoNegociacionDto medicamentoDto) {
    	List<Long> listTemporal = new ArrayList<Long>();
		listTemporal.add(medicamentoDto.getMedicamentoDto().getId());

			try {
				negociacionMedicamentoSSFacade.guardarMedicamentosFranjaPGP(
						this.tecnologiasController.getNegociacion().getId(),
						listTemporal,
						grupoTerapeutico.getCategoriaMedicamento().getId(),
						medicamentoDto.getFranjaInicio(),
						medicamentoDto.getFranjaFin(),
						this.tecnologiasController.getUser().getId());
			} catch (ConexiaBusinessException e) {
				facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
				logger.error("Error al aplicar valor franja al medicamento en negociación: "+medicamentoDto.getMedicamentoDto().getId(), e);
			}
    }


    /**
     * Metodo encargado de eliminar una lista de medicamentos selecionados
     * (Masiva)
     */
    public void eliminarMedicamentosMasivo(String nombreTabla) {
    	if(tecnologiasController.getNegociacion().getEstadoLegalizacion() != EstadoLegalizacionEnum.LEGALIZADA) {
    		try {
                if ((null != medicamentosNegociacionSeleccionados) && (medicamentosNegociacionSeleccionados.size() > 0)) {

                	List<Long> medicamentoIds = new ArrayList<Long>();

                    for (Iterator<MedicamentoNegociacionDto> iter = medicamentosNegociacionSeleccionados.listIterator(); iter.hasNext();) {
                        MedicamentoNegociacionDto dto = iter.next();
                        medicamentoIds.add(dto.getMedicamentoDto().getId());
                        this.medicamentosNegociacion.remove(dto);
                    }

                    if(Objects.nonNull(medicamentoIds)) {
                    	this.eliminarMedicamento(medicamentoIds);

                        medicamentosNegociacionOriginal.clear();
                        medicamentosNegociacionOriginal.addAll(medicamentosNegociacion);
                    }

                } else {
                    facesMessagesUtils.addInfo(resourceBundle.getString("medicamento_msj_val_med_sel"));
                }
            } catch (Exception e) {
                logger.error("Error al eliminar de la negociación los medicamentos seleccionados", e);
                facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
    	}

    }

    /**
     * Metodo encargado de eliminar un medicamento de la negociación (Uno a uno)
     */
    private void eliminarMedicamento(List<Long> medicamentoIds) {
    	if(tecnologiasController.getNegociacion().getEstadoLegalizacion() != EstadoLegalizacionEnum.LEGALIZADA) {
    		if (medicamentoIds == null) {
                this.facesUtils.urlRedirect("/bandejaPrestador");
            } else {
                try {
                	Integer sinMedicamentos = negociacionMedicamentoSSFacade.eliminarByNegociacionAndMedicamento(
                            tecnologiasController.getNegociacion().getId(),
                            medicamentoIds,
                            grupoTerapeutico.getCategoriaMedicamento().getId(),
                            tecnologiasController.getUser().getId());
                    		negociacionGrupoController.calcularValorTotal();

                    if(sinMedicamentos == 0) {
                    	volverAMedicamentos();
                    } else {
                    	facesMessagesUtils.addInfo(resourceBundle.getString("medicamento_msj_eliminacion_ok"));
    	                medicamentosNegociacionSeleccionados.clear();
    	                negociacionGrupoController.calcularValorTotal();
    					Ajax.update("negociacionMedicamentoPGPForm");
                    }
                } catch (Exception e) {
                    logger.error("Error al eliminar de la negociación los medicamentos", e);
                    facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
                }

            }
    	}

    }

    /**
     * Permite regresar a la visualización de tecnologías negociadas
     */
    public void volverAMedicamentos() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext()
				.getSession(true);
		Long negociacionId = (Long) session
				.getAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString());
		session.setAttribute(NegociacionSessionEnum.NEGOCIACION_ID.toString(),
				negociacionId);

		this.facesUtils.urlRedirect("/sedesasede/detalle");

	}



	public List<MedicamentoNegociacionDto> getMedicamentosNegociacion() {
		return medicamentosNegociacion;
	}


	public void setMedicamentosNegociacion(List<MedicamentoNegociacionDto> medicamentosNegociacion) {
		this.medicamentosNegociacion = medicamentosNegociacion;
	}


	public List<MedicamentoNegociacionDto> getMedicamentosNegociacionOriginal() {
		return medicamentosNegociacionOriginal;
	}


	public void setMedicamentosNegociacionOriginal(List<MedicamentoNegociacionDto> medicamentosNegociacionOriginal) {
		this.medicamentosNegociacionOriginal = medicamentosNegociacionOriginal;
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


	public GestionTecnologiasNegociacionEnum getGestionSeleccionada() {
		return gestionSeleccionada;
	}


	public void setGestionSeleccionada(GestionTecnologiasNegociacionEnum gestionSeleccionada) {
		this.gestionSeleccionada = gestionSeleccionada;
	}


	public TipoAsignacionTarifaMedEnum getTipoAsignacionSeleccionado() {
		return tipoAsignacionSeleccionado;
	}


	public void setTipoAsignacionSeleccionado(TipoAsignacionTarifaMedEnum tipoAsignacionSeleccionado) {
		this.tipoAsignacionSeleccionado = tipoAsignacionSeleccionado;
	}


	public List<Boolean> getListToggler() {
		return listToggler;
	}


	public void setListToggler(List<Boolean> listToggler) {
		this.listToggler = listToggler;
	}


	public GrupoTerapeuticoNegociacionDto getGrupoTerapeutico() {
		return grupoTerapeutico;
	}


	public void setGrupoTerapeutico(GrupoTerapeuticoNegociacionDto grupoTerapeutico) {
		this.grupoTerapeutico = grupoTerapeutico;
	}

	public TipoAsignacionTarifaMedEnum[] getTipoAsignacionTarifaMedicamentoEnum() {

		return this.tecnologiasController.getNegociacion().getTipoModalidadNegociacion() == null
                ? TipoAsignacionTarifaMedEnum.values()
                : TipoAsignacionTarifaMedEnum.values(this.tecnologiasController.getNegociacion().getTipoModalidadNegociacion());

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



}
