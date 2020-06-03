package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasPgpDto;
import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.xml.sax.SAXException;

import com.conexia.contratacion.commons.constants.enums.ArchivosNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.ErrorTecnologiasNegociacionPgpEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.GestionTecnologiasNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contratacion.commons.constants.enums.TipoAsignacionTarifaMedEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.negociacion.ArchivoTecnologiasNegociacionPgpDto;
import com.conexia.contratacion.commons.dto.negociacion.ErroresImportTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.GrupoTerapeuticoNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;
import com.conexia.utils.Constantes;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.newrelic.api.agent.NewRelic;
import com.newrelic.api.agent.Trace;

import co.conexia.negociacion.wap.controller.common.CommonController;
import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionMedicamentoSSFacade;

/**
 *
 * @author clozano
 *
 */

@Named
@ViewScoped
public class NegociacionGrupoTerapeuticoPGPController implements Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 7581739861056259721L;

	private static final String NOMBRE_IMPORTAR_PROCEDIMIENTOS_PGP = "Formato_importar_tecnologias_negociacion_Pgp.xlsx";

    @Inject
    private Log logger;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
	private FacesUtils facesUtils;;

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
	private NegociacionFacade negociacionFacade;

	@Inject
	private ValidadorImportTecnologiasPaqueteDetalleSS validator;

    @Inject
    /**
     * Se injecta el controler padre que contiene todos los tabs de tecnologías
     * y la negociación sacada de sesion. *
     */
    private TecnologiasSSController tecnologiasController;

    private List<GrupoTerapeuticoNegociacionDto> gruposNegociacion;
    private List<GrupoTerapeuticoNegociacionDto> gruposNegociacionOriginal;
    private List<GrupoTerapeuticoNegociacionDto> gruposNegociacionSeleccionados;

    private GestionTecnologiasNegociacionEnum gestionSeleccionada;

    private TipoAsignacionTarifaMedEnum tipoAsignacionSeleccionado;


    private StreamedContent formatoDownload;

    private ArchivosNegociacionEnum typeImport;

    private List<ErroresImportTecnologiasPgpDto> listadoErrores;

    /**
     * Para franja riesgo
     */
    private BigDecimal franjaInicio;
    private BigDecimal franjaFin;



    @PostConstruct
    @Trace(dispatcher = true)
    public void postconstruct() {

    	NewRelic.setTransactionName("NegociacionGrupoTerapeuticoPGPController","CargarGruposTerapeuticosTotalNegociacion");

    	try {
    		if (tecnologiasController.getNegociacion() != null) {

    			if(tecnologiasController.getNegociacion().getEsRia() == Boolean.FALSE) {
    				calcularValorTotal();
					consultarGruposTerapeuticosNegociacion();
				}
    		}
		} catch (Exception e) {
			this.logger.error(
					"Error al postConstruct NegociacionGrupoTerapeuticoPGPController",
					e);
			this.facesMessagesUtils.addError(exceptionUtils
					.createSystemErrorMessage(resourceBundle));
		}

    }

    /**
     * Consulta los gruposTerapeuticos de la negociación
     * @throws ConexiaBusinessException
     */
    @Trace(dispatcher = true)
    public void consultarGruposTerapeuticosNegociacion() throws ConexiaBusinessException {

    	NewRelic.setTransactionName("NegociacionGrupoTerapeuticoPGPController","CargarGruposTerapeuticosNegociacion");

    	this.gruposNegociacion = negociacionMedicamentoSSFacade.
    			consultarGruposNegociacionNoSedesByNegociacionId(tecnologiasController.getNegociacion());
    	this.gruposNegociacionOriginal = new ArrayList<>();
    	if(Objects.nonNull(gruposNegociacion)){
    		this.gruposNegociacionOriginal.addAll(gruposNegociacion);
    		Ajax.update("tecnologiasSSForm");
    	}
    }

    /**
     * Recalcula y actualiza el valor total de la negociación
     */
    public void calcularValorTotal() {
    	this.tecnologiasController.getNegociacion().setValorTotal(this.negociacionFacade.sumValorTotalPGP(tecnologiasController.getNegociacion().getId()));
    	Ajax.update("tecnologiasSSForm");
    }


    public void asignarTarifasGrupos() {
		try {
			if (this.tipoAsignacionSeleccionado != null) {
				aplicarValorMasivo();
			}
		} catch (ConexiaBusinessException e) {
			logger.error("Error asignarTarifasServicio", e);
			this.facesMessagesUtils.addError(this.exceptionUtils.createMessage(resourceBundle, e));
		} catch (Exception e) {
			this.logger.error("Error al asignarTarifasServicios", e);
			this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}

    /**
     * Aplica los valores del referente a los medicamentos y grupo terapéutico seleccionado
     * @throws ConexiaBusinessException
     */
    protected void aplicarValorMasivo() throws ConexiaBusinessException {


    		if (Objects.nonNull(gruposNegociacionSeleccionados) && gruposNegociacionSeleccionados.size() > 0) {

    			switch(tipoAsignacionSeleccionado) {
    				case APLICAR_REFERENTE:
    					if(this.tecnologiasController.getNegociacion().getPoblacion() > 0) {
	    					for (GrupoTerapeuticoNegociacionDto dto : gruposNegociacionSeleccionados) {
	    						asignarTarifarioNegociadoPGP(dto);
	    					}
	    					try {
	    						this.negociacionMedicamentoSSFacade.guardarGrupoTerapeuticoPGP(
	    								this.tecnologiasController.getNegociacion().getId(),
	    			                    this.gruposNegociacionSeleccionados,
	    			                    this.tecnologiasController.getNegociacion().getPoblacion(),
	    			                    this.tecnologiasController.getUser().getId());
	    					    	 	facesMessagesUtils.addInfo("se han guardado correctamente los valores");

	    					            this.consultarGruposTerapeuticosNegociacion();

	    					            if(tecnologiasController.getNegociacion().getEsRia() == Boolean.FALSE){
	    					            	this.calcularValorTotal();
	    				 			    }

	    					} catch (ConexiaBusinessException e) {
	    						this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
	    						logger.error("Error al aplicar masivos sobre los grupos terapéuticos de la negociación", e);
	    					}
    					} else {
    			    		this.facesMessagesUtils.addWarning("Por favor asigne una población a la negociación");
    			    	}
    					break;
    				case FRANJA_RIESGO:
    					if(tecnologiasController.validarRangoFranjaRiesgo(franjaInicio, franjaFin)) {
							try {
								this.negociacionMedicamentoSSFacade.guardarFranjaPGP(
					                    this.tecnologiasController.getNegociacion().getId(),
					                    this.gruposNegociacionSeleccionados,
					                    franjaInicio,
					                    franjaFin,
					                    this.tecnologiasController.getUser().getId());
							    	 	facesMessagesUtils.addInfo("Se han aplicado los valores correctamente");


							    	 	this.consultarGruposTerapeuticosNegociacion();
							            this.franjaInicio=null;
							            this.franjaFin=null;
							} catch (ConexiaBusinessException e) {
								this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
								logger.error("Error al aplicar franja riesgo masivo sobre los grupos terapéuticos de la negociación", e);
							}
						} else {
							this.facesMessagesUtils.addWarning("Por favor indique un rango de franja riesgo válido");
						}
    					break;
    				default:
    					break;
    			}
    			this.tipoAsignacionSeleccionado = null;
    			this.gruposNegociacionSeleccionados.clear();
				Ajax.update("tecnologiasSSForm:tabsTecnologias:negociacionGruposPGP");
			} else {
				facesMessagesUtils.addInfo(resourceBundle
						.getString("negociacion_servicio_msj_val_sel"));
			}
	}


	/**
	 * Asigna las tarifas al grupo terpeutico en base al referente
	 * @param dto
	 */
	private void asignarTarifarioNegociadoPGP(GrupoTerapeuticoNegociacionDto dto) {
		if (tipoAsignacionSeleccionado.equals(TipoAsignacionTarifaMedEnum.APLICAR_REFERENTE)) {

			if(Objects.nonNull(dto.getFrecuenciaReferente()) && Objects.nonNull(dto.getCostoMedioUsuarioReferente())) {
				dto.setFrecuenciaNegociado(dto.getFrecuenciaReferente());
				dto.setCostoMedioUsuarioNegociado(dto.getCostoMedioUsuarioReferente());
				dto.setValorNegociado(
						Objects.nonNull(dto.getCostoMedioUsuarioNegociado()) ? dto.getCostoMedioUsuarioNegociado()
						.multiply(BigDecimal.valueOf(dto.getFrecuenciaNegociado())
						.multiply(BigDecimal.valueOf(tecnologiasController.getNegociacion().getPoblacion())))
						.setScale(0, BigDecimal.ROUND_HALF_UP): BigDecimal.ZERO);
				dto.setNegociado(true);
			}

		}
	}


    /**
     * Método que redirecciona a la pagina del detalle de medicamentos del
     * grupo terapético negociado seleccionado.
     * @param servicio
     */
    public void verMedicamentosNegociados(GrupoTerapeuticoNegociacionDto grupoNegociacion){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(NegociacionSessionEnum.GRUPO_TERAPEUTICO_ID.toString(),grupoNegociacion);
        session.setAttribute(NegociacionSessionEnum.PGP.toString(), true);
        this.facesUtils.urlRedirect("/negociacionMedicamentoPGP");
    }


    /**
	 * Permite gestionar los capitulos en la tabla para seleccionar, deseleccionar y borrar
	 * @param nombreTabla
	 * @param nombreComboGestion
	 */
	public void gestionarGrupos(String nombreTabla, String nombreComboGestion) {
		if (this.gestionSeleccionada != null) {
			if (this.gestionSeleccionada
					.equals(GestionTecnologiasNegociacionEnum.BORRAR_SELECCIONADOS)) {
				if ((Objects.nonNull(gruposNegociacionSeleccionados))
						&& (gruposNegociacionSeleccionados.size() > 0)) {
					RequestContext.getCurrentInstance().execute(
							"PF('cdDeleteGrupo').show();");
				} else {
					facesMessagesUtils.addInfo(resourceBundle
							.getString("negociacion_grupo_msj_val_sel"));
				}
			} else if (this.gestionSeleccionada
					.equals(GestionTecnologiasNegociacionEnum.SELECCIONAR_TODOS)) {
				gruposNegociacionSeleccionados = new ArrayList<GrupoTerapeuticoNegociacionDto>();
				gruposNegociacionSeleccionados.addAll(gruposNegociacion);
				Ajax.oncomplete("PF('" + nombreTabla + "').selectAllRows();");
			} else if (this.gestionSeleccionada
					.equals(GestionTecnologiasNegociacionEnum.DESELECCIONAR_TODOS)) {
				gruposNegociacionSeleccionados.clear();
				Ajax.oncomplete("PF('" + nombreTabla + "').unselectAllRows();");
			}
			this.gestionSeleccionada = null;
			Ajax.update(nombreComboGestion);

		}
	}

	/**
	 * Elimina los grupos terapeuticos de la negociacion
	 */
	public void eliminarGruposMasivo() {
		if(tecnologiasController.getNegociacion().getEstadoLegalizacion() != EstadoLegalizacionEnum.LEGALIZADA) {
			try {
                if (Objects.nonNull(gruposNegociacion)  && gruposNegociacion.size() > 0) {

                		List<Long> gruposId = new ArrayList<>();

                        this.gruposNegociacionSeleccionados.stream().forEach((c) -> {
                        	gruposId.add(c.getCategoriaMedicamento().getId());
                        });

                        gruposId.addAll(this.gruposNegociacionSeleccionados
                                        .stream().map(c -> c.getCategoriaMedicamento().getId())
                                        .collect(Collectors.toList()));

                        //Elimina los medicamentos del grupo terapéutico, el grupo y actualiza los valores
                        this.negociacionMedicamentoSSFacade.eliminarByNegociacionAndGruposAllMedicamentos(
                        		tecnologiasController.getNegociacion().getId(),
                        		gruposId,
                        		tecnologiasController.getUser().getId());

                        gruposNegociacion = gruposNegociacion.stream()
                                .filter(c -> !gruposId.contains(c.getCategoriaMedicamento().getId())).collect(Collectors.toList());
                        gruposNegociacionOriginal.clear();
                        gruposNegociacionOriginal.addAll(gruposNegociacion);

                        //this.tecnologiasController.eliminarSedesSinTecnologiasByNegociacionPGP(tecnologiasController.getNegociacion().getId());

                        facesMessagesUtils.addInfo(resourceBundle
                                        .getString("negociacion_grupo_eliminacion_ok"));
                        gruposNegociacionSeleccionados.clear();

                        this.calcularValorTotal();

                    Ajax.update("tecnologiasSSForm:tabsTecnologias:negociacionGruposPGP");
                } else {
                        facesMessagesUtils.addInfo(resourceBundle.getString("negociacion_servicio_msj_val_sel"));
                }
            } catch (Exception e) {
                this.logger.error("Error al eliminar de la negociación los paquetes seleccionados", e);
                this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
		}

	}

	@Trace(dispatcher = true)
	public void importFiles(FileUploadEvent event) throws InvalidFormatException, IOException, SAXException {

		NewRelic.setTransactionName("NegociacionGrupoTerapeuticoPGPController","ImportarMedicamentosNegociacionPgp");

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
					 libro =  WorkbookFactory.create(new FileInputStream(file));
					try {
						gestionarImportMedicamentosPGP(libro, typeImport);
					} catch (ConexiaBusinessException e) {
						logger.error("Error al consultar capítulos de la negociación", e);
					}

				} catch (IOException | ConexiaSystemException e) {
					logger.error("Error general al importar el archivo", e);
					facesMessagesUtils.addError(resourceBundle.getString("form_label_error_archivo"));
				}
			} else {
				this.facesMessagesUtils.addWarning("Por favor seleccione y guarde un referente para la negociación antes de importar medicamentos");
			}
		} catch (ConexiaBusinessException e) {
			logger.error("Error al consultar referente de la negociación: " + tecnologiasController.getNegociacion().getId(), e);
			this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}


	}


	public void descargarFormatoTecnologiasPGP() {
		try {
			InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(Constantes.PATH_FORMATOS_REPORTES + NOMBRE_IMPORTAR_PROCEDIMIENTOS_PGP);
			this.formatoDownload = new DefaultStreamedContent(stream, "application/vnd.ms-excel", NOMBRE_IMPORTAR_PROCEDIMIENTOS_PGP);
		} catch (Exception ex) {
			this.facesMessagesUtils.addError("No se pudo descargar el formato de importar medicamentos para negociación PGP");
			logger.error("No se ha podido descargar el formato de importar medicamentos para negociación PGP", ex);
		}
	}

	/**
	 * Para gestionar la importación de los medicamentos a una negociación PGP
	 * @param libro
	 * @param typeImport
	 */
	private void gestionarImportMedicamentosPGP(Workbook libro, ArchivosNegociacionEnum typeImport) throws ConexiaBusinessException {
		DataFormatter formatter = new DataFormatter();
		List<ArchivoTecnologiasNegociacionPgpDto> medicamentosPgp = new ArrayList<ArchivoTecnologiasNegociacionPgpDto>();
		List<ErroresImportTecnologiasDto> listErrorFormat = new ArrayList<ErroresImportTecnologiasDto>();
		List<ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum>> errores
				= new ArrayList<ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum>>();
		Sheet hoja = libro.getSheetAt(0);
		listadoErrores = new ArrayList<>();

		listErrorFormat = validator.validateFormat(hoja, typeImport);

		if (listErrorFormat.isEmpty()) {
			Iterator<Row> iterador = hoja.iterator();
			while (iterador.hasNext()) {
				Row fila = iterador.next();
				if (fila.getRowNum() == 0) {
					continue;
				}

				if(validator.validarCamposFila(fila, typeImport)) {
					ArchivoTecnologiasNegociacionPgpDto ptoPgp = new ArchivoTecnologiasNegociacionPgpDto();
					ptoPgp.setCodigoTecnologiaUnicaEmssanar(formatter.formatCellValue(fila.getCell(0)));
					ptoPgp.setDescripcionCodigoTecnologiaUnicaEmssanar(formatter.formatCellValue(fila.getCell(1)));
					ptoPgp.setFrecuencia(fila.getCell(2).toString());
					ptoPgp.setCmu(fila.getCell(3).toString());
					ptoPgp.setFranjaInicio(fila.getCell(4).toString());
					ptoPgp.setFranjaFin(fila.getCell(5).toString());
					ptoPgp.setEliminarTecnologia(formatter.formatCellValue(fila.getCell(6)));
					ptoPgp.setLineaArchivo(fila.getRowNum());

					medicamentosPgp.add(ptoPgp);
				}

			}
			if (medicamentosPgp.isEmpty()) {
				facesMessagesUtils.addInfo("El archivo se encuentra vacio");
			} else {
				try {
					errores = negociacionMedicamentoSSFacade
								.validarMedicamentoNegociacionPgp(medicamentosPgp, tecnologiasController.getNegociacion(), tecnologiasController.getUser().getId());
					//negociacionMedicamentoSSFacade.clerMapsImportMedicamentos();
				} catch (ConexiaBusinessException e) {
					this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
					logger.error("Error al importar medicamentos a la negoación: " + tecnologiasController.getNegociacion().getId(), e);
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
				            "El archivo importado se ha procesado sin errores, se han actualizado los medicamentos.");
				}
			}

			if(!listadoErrores.isEmpty()){
				RequestContext.getCurrentInstance().execute("PF('dlgMedicamentosNoProcesadosW').show()");

			}
			consultarGruposTerapeuticosNegociacion();
			calcularValorTotal();
			Ajax.update("tecnologiasSSForm");

		} else {
			facesMessagesUtils.addInfo(
					"El archivo no cumple con el formato, verifique que el archivo corresponda a " + typeImport.getDescripcion());
		}

	}


	public List<GrupoTerapeuticoNegociacionDto> getGruposNegociacion() {
		return gruposNegociacion;
	}



	public void setGruposNegociacion(List<GrupoTerapeuticoNegociacionDto> gruposNegociacion) {
		this.gruposNegociacion = gruposNegociacion;
	}



	public List<GrupoTerapeuticoNegociacionDto> getGruposNegociacionOriginal() {
		return gruposNegociacionOriginal;
	}



	public void setGruposNegociacionOriginal(List<GrupoTerapeuticoNegociacionDto> gruposNegociacionOriginal) {
		this.gruposNegociacionOriginal = gruposNegociacionOriginal;
	}



	public List<GrupoTerapeuticoNegociacionDto> getGruposNegociacionSeleccionados() {
		return gruposNegociacionSeleccionados;
	}



	public void setGruposNegociacionSeleccionados(List<GrupoTerapeuticoNegociacionDto> gruposNegociacionSeleccionados) {
		this.gruposNegociacionSeleccionados = gruposNegociacionSeleccionados;
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

	public GestionTecnologiasNegociacionEnum[] getGestionTecnologiasNegociacion() {
		return GestionTecnologiasNegociacionEnum.values();
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

	public StreamedContent getFormatoDownload() {
		return formatoDownload;
	}

	public void setFormatoDownload(StreamedContent formatoDownload) {
		this.formatoDownload = formatoDownload;
	}

	public List<ErroresImportTecnologiasPgpDto> getListadoErrores() {
		return listadoErrores;
	}

	public void setListadoErrores(List<ErroresImportTecnologiasPgpDto> listadoErrores) {
		this.listadoErrores = listadoErrores;
	}

	public TecnologiasSSController getTecnologiasController() {
		return tecnologiasController;
	}

	public void setTecnologiasController(TecnologiasSSController tecnologiasController) {
		this.tecnologiasController = tecnologiasController;
	}




}
