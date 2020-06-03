package co.conexia.negociacion.wap.controller.comparacion.tarifas;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.comparacion.FiltroComparacionTarifa;
import com.conexia.contratacion.commons.dto.comparacion.ReporteComparacionTarifasDto;
import com.conexia.contratacion.commons.dto.comparacion.ResumenComparacionDto;
import com.conexia.contratacion.commons.dto.comparacion.TablaComparacionTarifaDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.MacroServicioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.RangoPoblacionDto;
import com.conexia.contratacion.commons.dto.maestros.RiaDto;
import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import co.conexia.negociacion.wap.facade.comite.GestionComiteFacade;
import co.conexia.negociacion.wap.facade.comparacion.negociacion.ComparacionNegociacionFacade;
import co.conexia.negociacion.wap.facade.comparacion.tarifas.ComparacionTarifasFacade;

/**
 * Controller utilizado en la bandeja de comparacion de tarifas
 * @author etorres
 */
@Named
@ViewScoped
@URLMapping(id = "comparacionTarifas", pattern = "/gestionNecesidad/comparar", viewId = "/bandeja/comparacion/comparacionTarifa.page")
public class ComparacionTarifasController implements Serializable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 4474131147118363712L;
	
	
	private final String TECNOLOGIA_PROCEDIMIENTOS = "Procedimientos";
    private final String TECNOLOGIA_MEDICAMENTOS = "Medicamentos";
    private final String TECNOLOGIA_PAQUETES = "Paquetes";
    private final String TECNOLOGIA_TRASLADOS = "Traslados";
    
    @Inject
    private FacesUtils faceUtils;

    @Inject
    private Log logger;
    
    @Inject
    private FacesMessagesUtils facesMessagesUtils;
	
    @Inject
    private GestionComiteFacade gestionComite;
    
    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;
    
    @Inject
    private PreContractualExceptionUtils exceptionUtils;
    
    @Inject
    @UserInfo
    private UserApp user;
    
    @Inject
    private ComparacionTarifasFacade comparacionTarifasFacade;
    
    @Inject
    private ComparacionNegociacionFacade comparacionNegociacionFacade;
    
    private PrestadorDto prestadorSeleccionado;
    
    private String tecnologiaSeleccionada;
    
    private FiltroComparacionTarifa filtroComparacionTarifa;
    
    private List<SedePrestadorDto> sedesPrestador;    
    private List<SedePrestadorDto> sedesPrestadorSeleccionadas;
    
    private List<CategoriaMedicamentoDto> categoriasMedicamento;
    private List<CategoriaMedicamentoDto> categoriasMedicamentoSeleccionadas;
    
    private List<MacroServicioDto> gruposHabilitacion;
    private List<MacroServicioDto> gruposHabilitacionSeleccionados;
    
    private List<ServicioSaludDto> serviciosSalud;
    private List<ServicioSaludDto> serviciosSaludSeleccionados;
    
    private List<CapituloProcedimientoDto> capitulosProcedimientoDto;
    private List<CapituloProcedimientoDto> capitulosProcedimientoSeleccionados;
    
    private List<CategoriaProcedimientoDto> categoriasProcedimientos;
    private List<CategoriaProcedimientoDto> categoriasSeleccionados;
    
    
    List<TablaComparacionTarifaDto> tablaComparacion;
    private List<TablaComparacionTarifaDto> tablaComparacionSeleccionados;
    private List<ReporteComparacionTarifasDto> tablaComparacionCapita;
    
    private List<ResumenComparacionDto> reportesDescargados;
    
    private StreamedContent reporteComparacionFile;
    
    private List<RiaDto> riasSeleccionados;
    private List<RangoPoblacionDto> rangosSeleccionados;
    
    private boolean ria;
    
    private String reporteSeleccionado = "gEvento";
    


    
    /**
     * Determina si se compara el prestador con las negociaciones existentes
     */
    private Boolean compararConNegociacion;
    
    @PostConstruct
    public void postConstruct(){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        ria = false;
        tablaComparacion = new ArrayList<>();
        reportesDescargados = new ArrayList<>();        
        if(Objects.nonNull(session.getAttribute(NegociacionSessionEnum.RIAS.toString()))){
        	ria = (boolean) session.getAttribute(NegociacionSessionEnum.RIAS.toString());        	
        }        
        if(Objects.nonNull(session.getAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString()))){
	        Long prestadorId = (Long) session.getAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString());
	        if(!ria){
		        if(prestadorId==null){
		            this.faceUtils.urlRedirect("/gestionNecesidad/bandeja");
		        }else{	        	
		            this.prestadorSeleccionado = comparacionTarifasFacade.findPrestadorById(prestadorId);
		        }
	        }
        }
    }
    
    public void enviarNegociacion() {
        gestionComite.actualizarEstadoPrestador(EstadoPrestadorEnum.EN_NEGOCIACION,this.prestadorSeleccionado.getId(),user.getId());
        this.facesMessagesUtils.addInfo("Se ha enviado correctamente el prestador a la negociacion");
    }
    
    /**
     * Metodo para limpiar el formulario
     */
    public void reset(){
        tecnologiaSeleccionada = "";
        sedesPrestador = new ArrayList<>();
        sedesPrestadorSeleccionadas = new ArrayList<>();
        gruposHabilitacion = new ArrayList<>();
        gruposHabilitacionSeleccionados = new ArrayList<>();
        categoriasMedicamento = new ArrayList<>();
        categoriasMedicamentoSeleccionadas = new ArrayList<>();
        serviciosSalud = new ArrayList<>();
        serviciosSaludSeleccionados = new ArrayList<>();
        reportesDescargados = new ArrayList<>();
        tablaComparacionSeleccionados = new ArrayList<>();
        riasSeleccionados = new ArrayList<>();
        rangosSeleccionados = new ArrayList<>();
        compararConNegociacion= false;
    }
    
    /**
     * De acuerdo a la tecnologia seleccionada carga las sedes disponibles
     * para esa tecnologia
     */
    public void seleccionarTecnologia(){        
        sedesPrestador = comparacionTarifasFacade.findSedePrestadorByPrestadorIdAndTecnologia(
                prestadorSeleccionado.getId(),
                tecnologiaSeleccionada.trim());
        gruposHabilitacion = new ArrayList<>();
        gruposHabilitacionSeleccionados = new ArrayList<>();
        categoriasMedicamento = new ArrayList<>();
        categoriasMedicamentoSeleccionadas = new ArrayList<>();
        serviciosSalud = new ArrayList<>();
        serviciosSaludSeleccionados = new ArrayList<>();
        sedesPrestadorSeleccionadas = new ArrayList<>();
        tablaComparacion = new ArrayList<>();
        tablaComparacionSeleccionados = new ArrayList<>();
    }
    
    
    public void seleccionarReporte() { 	
    	reset();

    	
    }
    
    
    
    
    /**
     * De acuerdo a las sedes seleccionadas carga los grupos para
     * las tecnologias seleccionadas
     */
    public void seleccionarSedesXTecnologia(){   
    	if(sedesPrestadorSeleccionadas == null || sedesPrestadorSeleccionadas.size() == 0){
    		sedesPrestadorSeleccionadas = sedesPrestador;
    	}
        if(sedesPrestadorSeleccionadas != null && sedesPrestadorSeleccionadas.size() > 0){
            
            List<Long> sedesId = new ArrayList<>();
            for(SedePrestadorDto sede : sedesPrestadorSeleccionadas){
                sedesId.add(sede.getId());
            }
            
            try{
					if (tecnologiaSeleccionada.equals(TECNOLOGIA_PROCEDIMIENTOS) && sedesId.size() > 0) {
						if (reporteSeleccionado.equals("gEvento")) {
						gruposHabilitacion = comparacionTarifasFacade
								.findMacroServiciosByPrestadorIdAndSedesId(prestadorSeleccionado.getId(), sedesId);}
						else {
							capitulosProcedimientoDto = comparacionTarifasFacade.findCapituloProcedureBySede(prestadorSeleccionado.getId(), sedesId);	
						}
					} else if (tecnologiaSeleccionada.equals(TECNOLOGIA_MEDICAMENTOS) && sedesId.size() > 0) {
						categoriasMedicamento = comparacionTarifasFacade
								.findCategoriasByPrestadorIdAndSedesId(prestadorSeleccionado.getId(), sedesId);
					}
            }catch(Exception e){
                this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
        }else{
            gruposHabilitacion = new ArrayList<>();
            categoriasMedicamento = new ArrayList<>();
            serviciosSalud = new ArrayList<>();
        }
    }
    
    
    /**
     * Realiza la seleccion de grupos habilitacion y busca los servicios
     * de habilitacion por los grupos, sedes y prestador seleccionados.
     */
    public void seleccionarGruposHabilitacion(){       
        if(gruposHabilitacionSeleccionados == null || gruposHabilitacionSeleccionados.size() == 0){
        	gruposHabilitacionSeleccionados = gruposHabilitacion;
        }
        if(sedesPrestadorSeleccionadas != null 
            && sedesPrestadorSeleccionadas.size() > 0 
            && gruposHabilitacionSeleccionados != null 
            && gruposHabilitacionSeleccionados.size() > 0){
            
            List<Long> sedesId = new ArrayList<>();
            List<Long> gruposHabilitacionId = new ArrayList<>();
            
            for(SedePrestadorDto sede : sedesPrestadorSeleccionadas){
                sedesId.add(sede.getId());
            }
            
            for(MacroServicioDto grupo : gruposHabilitacionSeleccionados){
                gruposHabilitacionId.add(grupo.getId());
            }
            
            try{
                serviciosSalud = comparacionTarifasFacade.findServiciosSaludByPrestadorIdSedesIdAndMacroServId(
                        prestadorSeleccionado.getId(), sedesId, gruposHabilitacionId);
            }catch(Exception e){
                this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
        }else{
            serviciosSalud = new ArrayList<>();
        }
        
    }
    
    
    public void seleccionarCapitulos(){       
        if(capitulosProcedimientoSeleccionados == null || capitulosProcedimientoSeleccionados.size() == 0){
        	capitulosProcedimientoSeleccionados = capitulosProcedimientoDto;
        }
        if(sedesPrestadorSeleccionadas != null 
            && sedesPrestadorSeleccionadas.size() > 0 
            && capitulosProcedimientoSeleccionados != null 
            && capitulosProcedimientoSeleccionados.size() > 0){
            
            List<Long> sedesId = new ArrayList<>();
            List<Long> capitulosId = new ArrayList<>();
            
            for(SedePrestadorDto sede : sedesPrestadorSeleccionadas){
                sedesId.add(sede.getId());
            }
            
            for(CapituloProcedimientoDto capitulo : capitulosProcedimientoSeleccionados){
            	capitulosId.add(capitulo.getId());
            }
            
            try{
            	categoriasProcedimientos = comparacionTarifasFacade.findCategoriaByPrestadorIdSedesIdAndCapituloId(
                        prestadorSeleccionado.getId(), sedesId, capitulosId);
            	
            	categoriasProcedimientos = categoriasProcedimientos.stream().map(c ->{
            		     if(c.getDescripcion().length() > 50) {   
            			 c.setDescripcion(c.getDescripcion().substring(0, 50));}
            			 return c;
            	         })
            			.collect(Collectors.toList());
            	
            }catch(Exception e){
                this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
        }else{
        	categoriasProcedimientos = new ArrayList<>();
        }
        
    }
    
    
    
    
    /**
     * De acuerdo a los datos seleccionados para cada filtro en el formulario 
     * realiza la comparacion de tarifas.
     */
	public void realizarComparacionTarifas() {

		FacesContext facesContext = FacesContext.getCurrentInstance();

			if (ria && Objects.nonNull(riasSeleccionados) && !riasSeleccionados.isEmpty()) {
				List<Integer> riasId = new ArrayList<>();
				for (RiaDto ria : riasSeleccionados) {
					riasId.add(ria.getId());
				}

				List<Integer> rangoPoblacionId = new ArrayList<>();
				for (RangoPoblacionDto rangoPoblacion : rangosSeleccionados) {
					rangoPoblacionId.add(rangoPoblacion.getId());
				}
				tablaComparacion = comparacionTarifasFacade.coberturaSedePrestador(null, null, null, null, true, true,
						false, riasId, rangoPoblacionId, true);

			} else if ((tecnologiaSeleccionada != null && sedesPrestadorSeleccionadas != null
					&& sedesPrestadorSeleccionadas.size() > 0)) {
				List<Long> sedesId = new ArrayList<>();
				for (SedePrestadorDto sede : sedesPrestadorSeleccionadas) {
					sedesId.add(sede.getId());
				}

				if (tecnologiaSeleccionada.equals(TECNOLOGIA_PROCEDIMIENTOS)) {
					if (reporteSeleccionado.equals("gPgp")) {
						realizarComparacionTarifasPgp();
					} else {
					
						if(gruposHabilitacionSeleccionados != null && gruposHabilitacionSeleccionados.size() > 0 
							&& serviciosSaludSeleccionados != null && serviciosSaludSeleccionados.size() > 0) {
	
								List<Long> gruposHabilitacionId = new ArrayList<>();
								List<Long> serviciosHabilitacionId = new ArrayList<>();
			
								for (MacroServicioDto grupo : gruposHabilitacionSeleccionados) {
									gruposHabilitacionId.add(grupo.getId());
								}
			
								for (ServicioSaludDto servicio : serviciosSaludSeleccionados) {
									serviciosHabilitacionId.add(servicio.getId());
								}
			
								tablaComparacion = comparacionTarifasFacade.coberturaSedePrestador(prestadorSeleccionado.getId(),
										sedesId, gruposHabilitacionId, serviciosHabilitacionId,
										serviciosSalud.size() == serviciosSaludSeleccionados.size(),
										gruposHabilitacion.size() == gruposHabilitacionSeleccionados.size(), compararConNegociacion,
										null, null, false);
						}
					}
				} else if (tecnologiaSeleccionada.equals(TECNOLOGIA_MEDICAMENTOS)
						&& categoriasMedicamentoSeleccionadas != null
						&& categoriasMedicamentoSeleccionadas.size() > 0) {

					List<Long> categoriasMedicamentoId = new ArrayList<>();
					for (CategoriaMedicamentoDto categoria : categoriasMedicamentoSeleccionadas) {
						categoriasMedicamentoId.add(categoria.getId());
					}
					
					if (reporteSeleccionado.equals("gPgp")) {
						compararConNegociacion= true;
					}

					tablaComparacion = comparacionTarifasFacade.coberturaMedicamentosSedePrestador(
							prestadorSeleccionado.getId(), sedesId, categoriasMedicamentoId,
							categoriasMedicamento.size() == categoriasMedicamentoSeleccionadas.size(),
							compararConNegociacion, reporteSeleccionado);

				} else if (tecnologiaSeleccionada.equals(TECNOLOGIA_PAQUETES)) {

					tablaComparacion = comparacionTarifasFacade
							.coberturaSedePrestadorPaquetes(prestadorSeleccionado.getId(), sedesId);

				} else if (tecnologiaSeleccionada.equals(TECNOLOGIA_TRASLADOS)) {

					tablaComparacion = comparacionTarifasFacade
							.coberturaSedeTrasladosPrestador(prestadorSeleccionado.getId(), sedesId);

				} else {
					this.facesMessagesUtils.addWarning(facesContext,
							"Por favor seleccione todos los datos solicitados en los criterios de comparacion.");
				}
			} else {
				if (ria) {
					this.facesMessagesUtils.addWarning(facesContext,
							"Por favor seleccione la ruta integral de salud a comparar.");
				} else {
					this.facesMessagesUtils.addWarning(facesContext, "Por favor seleccione la(s) sede(s) a comparar.");
				}
			}
	}

	
	private void realizarComparacionTarifasPgp() {

		if (tecnologiaSeleccionada.equals(TECNOLOGIA_PROCEDIMIENTOS) && capitulosProcedimientoSeleccionados != null
				&& capitulosProcedimientoSeleccionados.size() > 0 && categoriasSeleccionados != null
				&& categoriasSeleccionados.size() > 0) {

			List<Long> capitulosId = new ArrayList<>();
			List<Long> categoriasId = new ArrayList<>();

			if ((tecnologiaSeleccionada != null && sedesPrestadorSeleccionadas != null
					&& sedesPrestadorSeleccionadas.size() > 0)) {
				List<Long> sedesId = new ArrayList<>();
				for (SedePrestadorDto sede : sedesPrestadorSeleccionadas) {
					sedesId.add(sede.getId());
				}

				for (CapituloProcedimientoDto capitulo : capitulosProcedimientoSeleccionados) {
					capitulosId.add(capitulo.getId());
				}

				for (CategoriaProcedimientoDto categoria : categoriasSeleccionados) {
					categoriasId.add(categoria.getId());
				}

				tablaComparacion = comparacionTarifasFacade.coberturaSedePrestadorPGP(prestadorSeleccionado.getId(),
						sedesId, capitulosId, categoriasId);

			}
		}

	}

	public void realizarComparacionTarifasCapita(){
        
        tablaComparacionCapita = new ArrayList<ReporteComparacionTarifasDto>();
        ReporteComparacionTarifasDto dto1 = new ReporteComparacionTarifasDto();
        dto1.setNombreSede("A");
        dto1.setNombreServicio("MEDICINA GENERAL");
        dto1.setCodigoServicio("01579400");
        dto1.setNombreEmssanar("INSERCION DE CATETER URINARIO ( VESICAL ) SOD +");
        tablaComparacionCapita.add(dto1);
        
        ReporteComparacionTarifasDto dto2 = new ReporteComparacionTarifasDto();
        dto2.setNombreSede("A");
        dto2.setNombreServicio("ENFERMERIA");
        dto2.setCodigoServicio("01965500");
        dto2.setNombreEmssanar("LIMPIEZA Y CUIDADOS DE TRAQUEOSTOMIA SOD");
        tablaComparacionCapita.add(dto2);
    }
    
    public void verPortafolio(){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Long prestadorId = (Long) session.getAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString());
        tablaComparacion = new ArrayList<>();
        if(prestadorId==null){
            this.faceUtils.urlRedirect("/gestionNecesidad/bandeja");
        }else{
            this.faceUtils.urlRedirect("/detallePortafolio");
        }
    }
    
    public void pasarComite(){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Long prestadorId = (Long) session.getAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString());
        tablaComparacion = new ArrayList<>();
        if(prestadorId==null){
            this.faceUtils.urlRedirect("/gestionNecesidad/bandeja");
        }else if (reportesDescargados==null||reportesDescargados.isEmpty()){
            facesMessagesUtils.addInfo("Por favor descargue al menos un reporte.");
        }else{
            session.setAttribute(NegociacionSessionEnum.REPORTES_DESCARGADOS.toString(), reportesDescargados);
            this.faceUtils.urlRedirect("/gestionNecesidad/pasarComite");
        }
    }
    
    public void pasarComiteCapita(){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        Long prestadorId = (Long) session.getAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString());
        tablaComparacion = new ArrayList<>();
        if(prestadorId==null){
            this.faceUtils.urlRedirect("/gestionNecesidad/bandeja");
        }else{
            this.faceUtils.urlRedirect("/bandeja/comparacion/gestionNecesidadCapita.page");
        }
    }
    
    
    public void descargarReporteComparacion(){
        FacesContext facesContext = FacesContext.getCurrentInstance();
        
        ByteArrayInputStream datosReporte = null;
        String nombreArchivo = null;
        byte[] datos;
        
        if(ria && Objects.nonNull(riasSeleccionados) && !riasSeleccionados.isEmpty()){            	
        	List<Integer> riasId = new ArrayList<>();
        	for(RiaDto ria : riasSeleccionados){
        		riasId.add(ria.getId());
            }
        	
        	List<Integer> rangoPoblacionId = new ArrayList<>();
        	for(RangoPoblacionDto rangoPoblacion : rangosSeleccionados){
        		rangoPoblacionId.add(rangoPoblacion.getId());
            }
        	// Obtiene los datos del reporte
            try {
				datos = comparacionTarifasFacade.generarExcelComparacionProcedimientos(tablaComparacionSeleccionados, 
				            null, null, null, null, 
				            true, 
				            true, 
				            "RIAS", false, riasId, rangoPoblacionId, true);
				nombreArchivo = "ComparacionTarifasProcedimientos_RIAS.xls";
	            datosReporte = new ByteArrayInputStream(datos);
	            
	            //Agrego a la lista
	            agregarResumenDescarga(TECNOLOGIA_PROCEDIMIENTOS);
	            
	            if(nombreArchivo != null && datosReporte != null){
                    reporteComparacionFile = new  DefaultStreamedContent(datosReporte, "application/vnd.ms-excel", nombreArchivo);
                }
			} catch (IOException e) {
				this.facesMessagesUtils.addWarning(facesContext, "Ocurrio un error descargando el reporte de comparacion de tarifas Rias.");
                logger.error("Error descargando el reporte de comparacion de tarifas Rias", e);
			}
        }else 
        if(tecnologiaSeleccionada != null
            && sedesPrestadorSeleccionadas != null 
            && tablaComparacion.size() > 0){
            
            List<Long> sedesId = new ArrayList<>();
            sedesPrestadorSeleccionadas.stream().forEach((sede) -> {
                sedesId.add(sede.getId());
            });
            
            if(tablaComparacionSeleccionados == null || tablaComparacionSeleccionados.isEmpty()){
                this.facesMessagesUtils.addWarning(facesContext, 
                        "Por favor seleccione minimo un prestador para realizar la comparacion y descargar el reporte.");
            }
            
            try{
                if(tecnologiaSeleccionada.equals(TECNOLOGIA_PROCEDIMIENTOS)){ 
                	
            		if (reporteSeleccionado.equals("gPgp")) {
            			datos = descargaReporteComparacionPgp(sedesId, tecnologiaSeleccionada, reporteSeleccionado);
            	        nombreArchivo = "ComparacionNegociacionProcedimientos_" + prestadorSeleccionado.getNumeroDocumento() +".xls";
            	        datosReporte = new ByteArrayInputStream(datos); 
            	        //Agrego a la lista
            	        agregarResumenDescarga(TECNOLOGIA_PROCEDIMIENTOS);
            			
            		} else {
                	
                    List<Long> gruposHabilitacionId = new ArrayList<>();
                    List<Long> serviciosHabilitacionId = new ArrayList<>();

                    gruposHabilitacionSeleccionados.stream().forEach((grupo) -> {
                        gruposHabilitacionId.add(grupo.getId());
                    });

                    serviciosSaludSeleccionados.stream().forEach((servicio) -> {
                        serviciosHabilitacionId.add(servicio.getId());
                    });

                    // Obtiene los datos del reporte
                    datos = comparacionTarifasFacade.generarExcelComparacionProcedimientos(tablaComparacionSeleccionados, 
	                            prestadorSeleccionado.getId(), sedesId, gruposHabilitacionId, serviciosHabilitacionId, 
	                            serviciosSalud.size() == serviciosSaludSeleccionados.size(), 
	                            gruposHabilitacion.size() == gruposHabilitacionSeleccionados.size(), 
	                            prestadorSeleccionado.getNumeroDocumento(), compararConNegociacion,
	                            null, null,false);
                    
                    nombreArchivo = "ComparacionTarifasProcedimientos_" + prestadorSeleccionado.getNumeroDocumento() +".xls";
                    datosReporte = new ByteArrayInputStream(datos);
                    
                    //Agrego a la lista
                    agregarResumenDescarga(TECNOLOGIA_PROCEDIMIENTOS);
            		}
                    
                }else if(tecnologiaSeleccionada.equals(TECNOLOGIA_MEDICAMENTOS)){               	
                    List<Long> categoriasMedicamentoId = new ArrayList<>();
                    categoriasMedicamentoSeleccionadas.stream().forEach((categoria) -> {
                        categoriasMedicamentoId.add(categoria.getId());
                    });
                    
                    datos = comparacionTarifasFacade.generarExcelComparacionMedicamentos(
                            tablaComparacionSeleccionados, prestadorSeleccionado.getId(), sedesId,  categoriasMedicamentoId, 
                            categoriasMedicamento.size() == categoriasMedicamentoSeleccionadas.size(), 
                            prestadorSeleccionado.getNumeroDocumento(),compararConNegociacion, reporteSeleccionado);
                    
                    nombreArchivo = "ComparacionTarifasMedicamentos_" + prestadorSeleccionado.getNumeroDocumento() +".xls";
                    datosReporte = new ByteArrayInputStream(datos);
                    
                    //Agrego a la lista
                    agregarResumenDescarga(TECNOLOGIA_MEDICAMENTOS);
				

                }else if(tecnologiaSeleccionada.equals(TECNOLOGIA_PAQUETES)){
                    
                    datos = comparacionTarifasFacade.generarExcelComparacionPaquetes(tablaComparacionSeleccionados, 
                            prestadorSeleccionado.getId(), sedesId, prestadorSeleccionado.getNumeroDocumento(), compararConNegociacion);
        
                    nombreArchivo = "ComparacionTarifasPaquetes_" + prestadorSeleccionado.getNumeroDocumento() +".xls";
                    datosReporte = new ByteArrayInputStream(datos);
                    
                    //Agrego a la lista
                    agregarResumenDescarga(TECNOLOGIA_PAQUETES);

                }else if(tecnologiaSeleccionada.equals(TECNOLOGIA_TRASLADOS)){
                    
                    datos = comparacionTarifasFacade.generarExcelComparacionTraslados(tablaComparacionSeleccionados, 
                            prestadorSeleccionado.getId(), sedesId, prestadorSeleccionado.getNumeroDocumento());
        
                    nombreArchivo = "ComparacionTarifasTraslados_" + prestadorSeleccionado.getNumeroDocumento() +".xls";
                    datosReporte = new ByteArrayInputStream(datos);
                    
                    //Agrego a la lista
                    agregarResumenDescarga(TECNOLOGIA_TRASLADOS);

                }else{
                    this.facesMessagesUtils.addWarning(facesContext, "Por favor seleccione todos los datos solicitados en los criterios de comparacion.");
                    return ;
                }
                
                if(nombreArchivo != null && datosReporte != null){
                    reporteComparacionFile = new  DefaultStreamedContent(datosReporte, "application/vnd.ms-excel", nombreArchivo);
                }
                
            }catch(Exception e){
                this.facesMessagesUtils.addWarning(facesContext, 
                        "Ocurrio un error descargando el reporte de comparacion de tarifas.");
                logger.error("Error descargando el reporte de comparacion de tarifas", e);
            }
        }
    }
    
    
  private byte[] descargaReporteComparacionPgp(List<Long> sedesId, String tecnologia, String reporteSeleccionado) throws IOException {

		byte[] datos = null;
		List<Long> capitulosId = new ArrayList<>();
		List<Long> categoriasId = new ArrayList<>();

		if (tecnologia.equals(TECNOLOGIA_PROCEDIMIENTOS)) {
			for (CapituloProcedimientoDto capitulo : capitulosProcedimientoSeleccionados) {
				capitulosId.add(capitulo.getId());
			}

			for (CategoriaProcedimientoDto categoria : categoriasSeleccionados) {
				categoriasId.add(categoria.getId());
			}

			// Obtiene los datos del reporte
			datos = comparacionNegociacionFacade.generarExcelComparacionProcedimientosPGP(tablaComparacionSeleccionados,
					prestadorSeleccionado.getId(), sedesId, capitulosId, categoriasId,
					capitulosProcedimientoDto.size() == capitulosProcedimientoSeleccionados.size(),
					categoriasProcedimientos.size() == categoriasSeleccionados.size(),
					prestadorSeleccionado.getNumeroDocumento());
		}


		return datos;
	}

    
	/**
     * Metodo encargado de agregar a la lista de descargas el respectivo resumen
     * 
     * @param tipoReporte 
     */
    private void agregarResumenDescarga(String tipoReporte){
        ResumenComparacionDto dto = new ResumenComparacionDto();
        if ((sedesPrestadorSeleccionadas!=null)&&(sedesPrestadorSeleccionadas.size() >0))
            dto.setSedesPrestadorSeleccionadas(sedesPrestadorSeleccionadas);
        if ((gruposHabilitacionSeleccionados!=null)&&(gruposHabilitacionSeleccionados.size() >0))
            dto.setGruposHabilitacionSeleccionados(gruposHabilitacionSeleccionados);
        if ((categoriasMedicamentoSeleccionadas!=null)&&(categoriasMedicamentoSeleccionadas.size() >0))
            dto.setCategoriasMedicamentoSeleccionadas(categoriasMedicamentoSeleccionadas);
        if ((serviciosSaludSeleccionados!=null)&&(serviciosSaludSeleccionados.size() >0))
            dto.setServiciosSaludSeleccionados(serviciosSaludSeleccionados);
        if ((tablaComparacionSeleccionados!=null)&&(tablaComparacionSeleccionados.size() >0))
            dto.setTablaComparacionSeleccionados(tablaComparacionSeleccionados);  
        if((capitulosProcedimientoSeleccionados!= null)&& (capitulosProcedimientoSeleccionados.size() >0))
        	dto.setCapitulosSeleccionados(capitulosProcedimientoSeleccionados);
        
        dto.setTecnologia(tipoReporte);
        
        reportesDescargados.add(dto);
    }

    public PrestadorDto getPrestadorSeleccionado() {
        return prestadorSeleccionado;
    }

    public void setPrestadorSeleccionado(PrestadorDto prestadorSeleccionado) {
        this.prestadorSeleccionado = prestadorSeleccionado;
    }

    public String getTecnologiaSeleccionada() {
        return tecnologiaSeleccionada;
    }

    public void setTecnologiaSeleccionada(String tecnologiaSeleccionada) {
        this.tecnologiaSeleccionada = tecnologiaSeleccionada;
    }

    public FiltroComparacionTarifa getFiltroComparacionTarifa() {
        return filtroComparacionTarifa;
    }

    public void setFiltroComparacionTarifa(FiltroComparacionTarifa filtroComparacionTarifa) {
        this.filtroComparacionTarifa = filtroComparacionTarifa;
    }

    public List<SedePrestadorDto> getSedesPrestador() {
        return sedesPrestador;
    }

    public void setSedesPrestador(List<SedePrestadorDto> sedesPrestador) {
        this.sedesPrestador = sedesPrestador;
    }

    public List<SedePrestadorDto> getSedesPrestadorSeleccionadas() {
        return sedesPrestadorSeleccionadas;
    }

    public void setSedesPrestadorSeleccionadas(List<SedePrestadorDto> sedesPrestadorSeleccionadas) {
        this.sedesPrestadorSeleccionadas = sedesPrestadorSeleccionadas;
    }

    public List<CategoriaMedicamentoDto> getCategoriasMedicamento() {
        return categoriasMedicamento;
    }

    public void setCategoriasMedicamento(List<CategoriaMedicamentoDto> categoriasMedicamento) {
        this.categoriasMedicamento = categoriasMedicamento;
    }

    public List<CategoriaMedicamentoDto> getCategoriasMedicamentoSeleccionadas() {
        return categoriasMedicamentoSeleccionadas;
    }

    public void setCategoriasMedicamentoSeleccionadas(List<CategoriaMedicamentoDto> categoriasMedicamentoSeleccionadas) {
        this.categoriasMedicamentoSeleccionadas = categoriasMedicamentoSeleccionadas;
    }

    public List<MacroServicioDto> getGruposHabilitacion() {
        return gruposHabilitacion;
    }

    public void setGruposHabilitacion(List<MacroServicioDto> gruposHabilitacion) {
        this.gruposHabilitacion = gruposHabilitacion;
    }

    public List<MacroServicioDto> getGruposHabilitacionSeleccionados() {
        return gruposHabilitacionSeleccionados;
    }

    public void setGruposHabilitacionSeleccionados(List<MacroServicioDto> gruposHabilitacionSeleccionados) {
        this.gruposHabilitacionSeleccionados = gruposHabilitacionSeleccionados;
    }

    public List<ServicioSaludDto> getServiciosSalud() {
        return serviciosSalud;
    }

    public void setServiciosSalud(List<ServicioSaludDto> serviciosSalud) {
        this.serviciosSalud = serviciosSalud;
    }

    public List<ServicioSaludDto> getServiciosSaludSeleccionados() {
        return serviciosSaludSeleccionados;
    }

    public void setServiciosSaludSeleccionados(List<ServicioSaludDto> serviciosSaludSeleccionados) {
        this.serviciosSaludSeleccionados = serviciosSaludSeleccionados;
    }    

    public List<TablaComparacionTarifaDto> getTablaComparacion() {
        return tablaComparacion;
    }

    public void setTablaComparacion(List<TablaComparacionTarifaDto> tablaComparacion) {
        this.tablaComparacion = tablaComparacion;
    }

    public List<TablaComparacionTarifaDto> getTablaComparacionSeleccionados() {
        return tablaComparacionSeleccionados;
    }

    public void setTablaComparacionSeleccionados(List<TablaComparacionTarifaDto> tablaComparacionSeleccionados) {
        this.tablaComparacionSeleccionados = tablaComparacionSeleccionados;
    }
    
    public StreamedContent getReporteComparacionFile() {
		return reporteComparacionFile;
    }

    public void setReporteComparacionFile(StreamedContent reporteComparacionFile) {
        this.reporteComparacionFile = reporteComparacionFile;
    }

    public List<ResumenComparacionDto> getReportesDescargados() {
        return reportesDescargados;
    }

    public void setReportesDescargados(List<ResumenComparacionDto> reportesDescargados) {
        this.reportesDescargados = reportesDescargados;
    }

    public List<ReporteComparacionTarifasDto> getTablaComparacionCapita() {
        return tablaComparacionCapita;
    }

    public void setTablaComparacionCapita(List<ReporteComparacionTarifasDto> tablaComparacionCapita) {
        this.tablaComparacionCapita = tablaComparacionCapita;
    }

	public Boolean getCompararConNegociacion() {
		return compararConNegociacion;
	}

	public void setCompararConNegociacion(Boolean compararConNegociacion) {
		this.compararConNegociacion = compararConNegociacion;
	}

	public List<RiaDto> getRiasSeleccionados() {
		return riasSeleccionados;
	}

	public void setRiasSeleccionados(List<RiaDto> riasSeleccionados) {
		this.riasSeleccionados = riasSeleccionados;
	}

	public List<RangoPoblacionDto> getRangosSeleccionados() {
		return rangosSeleccionados;
	}

	public void setRangosSeleccionados(List<RangoPoblacionDto> rangosSeleccionados) {
		this.rangosSeleccionados = rangosSeleccionados;
	}

	public boolean isRia() {
		return ria;
	}

	public void setRia(boolean ria) {
		this.ria = ria;
	}

	public String getReporteSeleccionado() {
		return reporteSeleccionado;
	}

	public void setReporteSeleccionado(String reporteSeleccionado) {
		this.reporteSeleccionado = reporteSeleccionado;
	}

	public List<CapituloProcedimientoDto> getCapitulosProcedimientoDto() {
		return capitulosProcedimientoDto;
	}

	public void setCapitulosProcedimientoDto(List<CapituloProcedimientoDto> capitulosProcedimientoDto) {
		this.capitulosProcedimientoDto = capitulosProcedimientoDto;
	}

	public List<CapituloProcedimientoDto> getCapitulosProcedimientoSeleccionados() {
		return capitulosProcedimientoSeleccionados;
	}

	public void setCapitulosProcedimientoSeleccionados(List<CapituloProcedimientoDto> capitulosProcedimientoSeleccionados) {
		this.capitulosProcedimientoSeleccionados = capitulosProcedimientoSeleccionados;
	}

	public List<CategoriaProcedimientoDto> getCategoriasSeleccionados() {
		return categoriasSeleccionados;
	}

	public void setCategoriasSeleccionados(List<CategoriaProcedimientoDto> categoriasSeleccionados) {
		this.categoriasSeleccionados = categoriasSeleccionados;
	}

	public List<CategoriaProcedimientoDto> getCategoriasProcedimientos() {
		return categoriasProcedimientos;
	}

	public void setCategoriasProcedimientos(List<CategoriaProcedimientoDto> categoriasProcedimientos) {
		this.categoriasProcedimientos = categoriasProcedimientos;
	}

}
