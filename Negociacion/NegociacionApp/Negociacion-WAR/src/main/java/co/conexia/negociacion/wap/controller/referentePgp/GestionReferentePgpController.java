package co.conexia.negociacion.wap.controller.referentePgp;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.omnifaces.util.Ajax;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.xml.sax.SAXException;

import com.conexia.contratacion.commons.constants.enums.ArchivosNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.FiltroReferentePgpEnum;
import com.conexia.contratacion.commons.constants.enums.GestionTecnologiasNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.TipoContratoEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.RegionalDto;
import com.conexia.contratacion.commons.dto.maestros.ZonaMunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoReferenteTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.ErroresImportTecnologiasDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteCapituloDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteCategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteMedicamentoDto;
import com.conexia.contratacion.commons.dto.referente.ReferentePrestadorDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteProcedimientoDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteUbicacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import co.conexia.negociacion.wap.facade.common.CommonFacade;
import co.conexia.negociacion.wap.facade.negociacion.NegociacionFacade;
import co.conexia.negociacion.wap.facade.referentePgp.GestionReferentePgpFacade;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.JRXlsExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleXlsExporterConfiguration;



/**
 *
 * Controller creado para la construccion y gestion del referente pgp
 * @author dmora
 *
 */

@Named
@ViewScoped
@URLMapping(id = "gestionReferentePgp", pattern = "/gestionReferentePgp", viewId = "/referentePGP/gestionReferentePgp.page")
public class GestionReferentePgpController  implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private List<DepartamentoDto> departamentos;

	private List<MunicipioDto> municipios;

	private List<RegionalDto> regionales;

	private List<ZonaMunicipioDto> zonasMunicipio;

    private List<SedePrestadorDto> sedesPrestador;

    private PrestadorDto prestador;

    private List<ReferenteCapituloDto> listarReferenteCapitulo;

    private List<ReferenteCategoriaMedicamentoDto> listarReferenteCategoriaMedicamento;

    private List<ReferenteMedicamentoDto> listarReferenteMedicamento;

    private List<ReferenteProcedimientoDto> listarReferenteProcedimiento;

    private List<CapituloProcedimientoDto> listCapitulos;

    private List<CategoriaProcedimientoDto> listCategorias;

    private List<CapituloProcedimientoDto> capitulosSeleccionados;

    private List<CategoriaProcedimientoDto> categoriasSeleccionadas;

    private GestionTecnologiasNegociacionEnum gestionSeleccionada;

	private List<ReferenteCapituloDto> referenteCapituloSeleccionados;

	private List<ReferenteProcedimientoDto> referenteProcedimientoSeleccionados;

	private List<ReferenteCategoriaMedicamentoDto> referenteCategoriaMedicamentosSeleccionados;

	private List<ReferenteMedicamentoDto> referenteMedicamentoSeleccionados;

    private ReferenteDto referente;

    private ReferenteUbicacionDto referenteUbicacion;

    private List<ReferentePrestadorDto> referentePrestador;

    private List<TipoContratoEnum> tipoContratoSeleccionados;

    private List<ReferentePrestadorDto> referentePrestadorSeleccionado;

    private Long referenteId;

    private Long referenteCapituloId;

    private Long referenteCategoriaMedicamentoId;

	private ArchivosNegociacionEnum typeImport;

	@Inject
	private ValidatorImportReferente validator;

	private List<ErroresImportTecnologiasDto> listErrors;


    /**
     * Facade de parametros.
     */
    @Inject
    private CommonFacade parametrosFacada;

    @Inject
    private NegociacionFacade negociacionFacade;

    @Inject
    private GestionReferentePgpFacade gestionReferenteFacade;

    @Inject
    private GestionTecnologiasReferentePgpController gestionTecnologiasController;

    @Inject
    private Log logger;

    @Inject
    protected FacesUtils facesUtils;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;


    private Integer tabIndex;


    @PostConstruct
    public void onload(){
    	regionales =  parametrosFacada.listarRegionales();
    	listCapitulos = parametrosFacada.listarCapitulos();
    	FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
        referenteId = (Long) session.getAttribute(NegociacionSessionEnum.REFERENTE_ID.toString());
        int creaReferente = (int) session.getAttribute(NegociacionSessionEnum.CREA_REFERENTE_ID.toString());
        if(Objects.nonNull(referenteId) && creaReferente == 0){
        	referente = this.parametrosFacada.buscarReferenteId(referenteId);
        	if(Objects.nonNull(referente.getFiltroReferente())){
        		if(referente.getFiltroReferente().equals(FiltroReferentePgpEnum.POR_UBICACION)){
            		referenteUbicacion =  this.parametrosFacada.buscarReferenteUbicacion(referenteId);
            	}
            	else{
            		referentePrestadorSeleccionado = this.parametrosFacada.buscarSedesReferente(referenteId);
            		prestador = new PrestadorDto();
            		this.prestador.setNumeroDocumento(referentePrestadorSeleccionado.get(0).getSedePrestador().getPrestador().getNumeroDocumento());
            		this.prestador.setNombre(referentePrestadorSeleccionado.get(0).getSedePrestador().getPrestador().getNombre());
            	}
    			if(this.gestionReferenteFacade.countReferenteCapitulo(referenteId) > 0){
    				listarCapitulosPGP();
    				listarGruposTerapeuticosPGP();
    			}
    			referente.setModalidad(this.parametrosFacada.modalidadesReferente(referenteId));
        	}
        }
        else{
        	referente = new ReferenteDto();
        	referente.setEsProcedimiento(Boolean.FALSE);
        	referente.setEsMedicamento(Boolean.FALSE);
        	referenteUbicacion = new ReferenteUbicacionDto();
        	prestador = new PrestadorDto();
        	sedesPrestador = new ArrayList<>();
        }

    }

    public void loadTipoReferente() {
    	switch(this.referente.getFiltroReferente()) {
    		case POR_UBICACION:
    			if(Objects.isNull(referenteUbicacion)) {
    				referenteUbicacion = new ReferenteUbicacionDto();
    			}
    			break;
    		case POR_PRESTADOR:
    			if(Objects.isNull(prestador)) {
    				prestador = new PrestadorDto();
                	sedesPrestador = new ArrayList<>();
    			}
    			break;
    		default:
    			break;
    	}
    }

    public void listarCapitulosPGP() {
    	listarReferenteCapitulo = this.gestionReferenteFacade.cargarReferenteCapituloPorReferente(referenteId);
		capitulosSeleccionados = new ArrayList<>();
		categoriasSeleccionadas =  new ArrayList<>();
		for(CapituloProcedimientoDto cap : this.gestionReferenteFacade.capitulosReferente(referenteId)){
			for (CapituloProcedimientoDto capituloProcedimientoDto : listCapitulos) {
				if(capituloProcedimientoDto.getId().equals(cap.getId())){
					capitulosSeleccionados.add(capituloProcedimientoDto);
					break;
				}
			}
		}
    }

    public void listarGruposTerapeuticosPGP() {
    	this.listCategorias = parametrosFacada.listarCategoriasPorCapitulo(capitulosSeleccionados);
		for(CategoriaProcedimientoDto cat : this.gestionReferenteFacade.categoriasReferente(referenteId)){
			for (CategoriaProcedimientoDto categoriaProcedimientoDto : listCategorias) {
				if(categoriaProcedimientoDto.getId().equals(cat.getId())){
					categoriasSeleccionadas.add(categoriaProcedimientoDto);
					break;
				}
			}
		}
    	listarReferenteCategoriaMedicamento = this.gestionReferenteFacade.cargarReferenteCategotiasReferente(referenteId);
    }

	public void listarCategorias(){
		this.listCategorias = parametrosFacada.listarCategoriasPorCapitulo(capitulosSeleccionados);
	}

	public void listarDepartamentos(){
		this.departamentos = parametrosFacada.listarDepartamentosPorRegional(referenteUbicacion.getRegional().getId());
	}

	public void listarZonas(){
		this.zonasMunicipio = parametrosFacada.listarZonas(referenteUbicacion.getRegional().getId());
		this.listarMunicipios();
	}

	private void listarMunicipios(){
		this.municipios = parametrosFacada.listarMunicipiosPorDepartametoZona(referenteUbicacion.getDepartamento().getId());
	}


	public void crearReferente(){
		try {
			this.gestionReferenteFacade.crearReferentePgp(referente.getDescripcion(), referente.getRegimen());
			referente.setId(this.gestionReferenteFacade.buscarReferenteCreado(referente.getDescripcion(),referente.getRegimen()));
			referenteId = this.gestionReferenteFacade.buscarReferenteCreado(referente.getDescripcion(),referente.getRegimen());
		}
		catch(ConexiaBusinessException cbe){
			facesMessagesUtils.addWarning(resourceBundle.getString("referente_existente_creacion_msn"));
		}
		catch (Exception e) {
			logger.error("error al crear referente",e);
		}
	}

	public void crearContenidoReferente(){
		try {
			Long referenteCargadoId = this.gestionReferenteFacade.buscarReferenteCreado(referente.getDescripcion(),referente.getRegimen());
			///elimina el contenido del referente, esto con el fin de construir uno nuevo, cada vez que generen refente
			if(Objects.nonNull(referenteId)){
				if(this.gestionReferenteFacade.countReferenteCapitulo(referenteCargadoId) > 0){
					this.borradoReferenteGeneral(referenteCargadoId);
				}
			}
			if(referente.getRegimen().equals(RegimenNegociacionEnum.AMBOS)){
				Integer poblacionFinal;
				//Carga poblacion para referente subsidiado
				referente.setRegimen(RegimenNegociacionEnum.SUBSIDIADO);
				referente.setPoblacionSubsidiado((this.parametrosFacada.poblacionTotalReferente(referente,referenteUbicacion, referentePrestadorSeleccionado)));
				//carga poblacion para referente contributivo
				referente.setRegimen(RegimenNegociacionEnum.CONTRIBUTIVO);
				referente.setPoblacionContributivo((this.parametrosFacada.poblacionTotalReferente(referente,referenteUbicacion, referentePrestadorSeleccionado)));
				poblacionFinal = referente.getPoblacionSubsidiado() + referente.getPoblacionContributivo();
				referente.setPoblacionTotal(poblacionFinal);
				referente.setRegimen(RegimenNegociacionEnum.AMBOS);

				//cargue detalle referente
				for(RegimenNegociacionEnum enm : RegimenNegociacionEnum.values()){
					referente.setRegimen(enm);
					Long refId  = this.gestionReferenteFacade.buscarReferenteCreado(referente.getDescripcion(),referente.getRegimen());
					this.gestionReferenteFacade.insertarCapitulosReferente(refId,this.generarCargueCapitulosReferente());
					this.gestionReferenteFacade.insertarProcedimientosReferente(refId,this.generarCargueProcedimientosReferente());
					this.gestionReferenteFacade.insertarReferenteSegunFiltro(refId,referente, referenteUbicacion, referentePrestadorSeleccionado);
					this.gestionReferenteFacade.actualizarDatosReferentel(refId,referente);
				}
				this.facesUtils.urlRedirect("/bandejaReferentePgp");
			}
			else{
				this.referente.setPoblacionTotal(this.parametrosFacada.poblacionTotalReferente(referente,referenteUbicacion, referentePrestadorSeleccionado));
				this.gestionReferenteFacade.actualizarDatosReferentel(referenteCargadoId,referente);
				//Pobla informacion relacionada con el detalle de tecnologias del referente
				if(this.referente.getPoblacionTotal() > 0) {
					this.gestionReferenteFacade.insertarCapitulosReferente(referenteCargadoId,this.generarCargueCapitulosReferente());
					this.gestionReferenteFacade.insertarProcedimientosReferente(referenteCargadoId,this.generarCargueProcedimientosReferente());
					this.gestionReferenteFacade.insertarReferenteSegunFiltro(referenteCargadoId,referente, referenteUbicacion, referentePrestadorSeleccionado);
					listarReferenteCapitulo = this.gestionReferenteFacade.cargarCapitulosPorReferente(referenteCargadoId);
				} else {
					this.facesMessagesUtils.addWarning("la zona y/o municipio seleccionado no tienen población asociada\n por favor seleccione otra combinación");
				}
			}
		} catch (Exception e) {
			logger.error("error al crear contenido referente",e);
		}
	}

	public void mostrarDetalleCapituloReferente( ReferenteCapituloDto referenteCapitulo){
		try {
			Ajax.update("tablaProcedimientosRefente");
			referenteCapituloId = referenteCapitulo.getId();
			listarReferenteProcedimiento = this.gestionReferenteFacade.cargarReferenteProcedimientoPorCapitulo(referenteCapitulo.getId());
		} catch (Exception e) {
			logger.error("error al mostar detalle del capitulo",e);
		}
	}

	public void mostrarDetalleCategoriaReferente(ReferenteCategoriaMedicamentoDto referenteCategoria){
		try {
			Ajax.update("tablaMedicamentosReferente");
			referenteCategoriaMedicamentoId = referenteCategoria.getId();
			listarReferenteMedicamento = this.gestionReferenteFacade.cargarReferenteMedicamentoPorCategoria(referenteCategoria.getId());
		} catch (Exception e) {
			logger.error("error al mostar detalle del capitulo",e);
		}
	}

	public void finalizarReferente(){
		try {
			if(Objects.nonNull(referente.getId())){
				if(this.gestionReferenteFacade.countReferenteCapitulo(referente.getId()) <= 0){
					facesMessagesUtils.addWarning(resourceBundle.getString("referente_sin_tecnologia"));
				}
				else{
					this.gestionReferenteFacade.finalizarReferente(referente.getId());
					this.facesUtils.urlRedirect("/bandejaReferentePgp");
				}
			}
			else{
				this.gestionReferenteFacade.finalizarReferente(referente.getId());
				facesMessagesUtils.addInfo(resourceBundle.getString("referente_finalizado_mensaje_ok"));
				this.facesUtils.urlRedirect("/bandejaReferentePgp");
			}
		} catch (Exception e) {
			logger.error("error al finalizar referente",e);
		}
	}

	public void gestionarCapitulos(String nombreTabla,
			String nombreComboGestion){
		try {
			this.gestionTecnologiasController.gestionarCapitulos(nombreTabla, nombreComboGestion);
		} catch (Exception e) {
			logger.error("error al gestionar capitulos",e);
		}
	}

	public void gestionarProcedimientos(String nombreTabla,
			String nombreComboGestion){
		try {
			this.gestionTecnologiasController.gestionarProcedimientos(nombreTabla, nombreComboGestion);
		} catch (Exception e) {
			logger.error("error al gestionar capitulos",e);
		}
	}

	public void eliminarCapitulosReferente(){
		try{
			this.gestionTecnologiasController.eliminarCapitulosReferenteMasivo();
			facesMessagesUtils.addInfo(resourceBundle.getString("referente_eliminar_capitulos_mensaje_ok"));
			listarReferenteCapitulo = this.gestionReferenteFacade.cargarReferenteCapituloPorReferente(referenteId);
		}catch (Exception e) {
			logger.error("error al eliminar capitulos",e);
		}
	}

	public void eliminarProcedimientosReferente(){
		try{
			this.gestionTecnologiasController.eliminarProcedimientosReferenteMasivo();
			facesMessagesUtils.addInfo(resourceBundle.getString("referente_eliminar_procedimientos_ok"));
			listarReferenteProcedimiento = this.gestionReferenteFacade.cargarReferenteProcedimientoPorCapitulo(referenteCapituloId);
		}catch (Exception e) {
			logger.error("error al eliminar capitulos",e);
		}
	}

	public void gestionarGrupoMedicamentos(String nombreTabla,
			String nombreComboGestion){
		try {
			this.gestionTecnologiasController.gestionarGrupoMedicamentos(nombreTabla, nombreComboGestion);
		} catch (Exception e) {
			logger.error("error al gestionar capitulos",e);
		}
	}

	public void gestionarMedicamentos(String nombreTabla,
			String nombreComboGestion){
		try {
			this.gestionTecnologiasController.gestionarMedicamentos(nombreTabla, nombreComboGestion);
		} catch (Exception e) {
			logger.error("error al gestionar medicamentos",e);
		}
	}

	public void eliminarGruposMedicamentosRefente(){
		try{
			this.gestionTecnologiasController.eliminarReferenteGrupoMedicamentoMasivo();
			facesMessagesUtils.addInfo(resourceBundle.getString("referente_eliminar_grupo_mensaje_ok"));
			listarReferenteCategoriaMedicamento = this.gestionReferenteFacade.cargarReferenteCategotiasReferente(referenteId);
		}catch (Exception e) {
			logger.error("error al eliminar capitulos",e);
		}
	}

	public void eliminarMedicamentosReferente(){
		try{
			this.gestionTecnologiasController.eliminarReferenteMedicamentosMasivo();
			facesMessagesUtils.addInfo(resourceBundle.getString("referente_eliminar_medicamentos_ok"));
			Ajax.update("formGestionReferente");
		}catch (Exception e) {
			logger.error("error al eliminar capitulos",e);
		}
	}


	private List<ReferenteCapituloDto> generarCargueCapitulosReferente(){
		List<ReferenteCapituloDto> listRefCapitulo = new ArrayList<>();
		try {
			listRefCapitulo = this.gestionReferenteFacade.listarCapitulosReferenteNuevo(referente.getFechaInicio(), referente.getFechaFin(),
					referente.getRegimen(), referenteUbicacion.getRegional(), referenteUbicacion.getZonaMunicipio(),referenteUbicacion.getDepartamento(),
					referenteUbicacion.getMunicipio(), referentePrestadorSeleccionado,capitulosSeleccionados,
					categoriasSeleccionadas,referente.getFiltroReferente(), referente,tipoContratoSeleccionados);
		} catch (Exception e) {
			logger.error("error al cargar capitulos",e);
		}
		return listRefCapitulo;

	}

	private List<ReferenteProcedimientoDto> generarCargueProcedimientosReferente(){
		List<ReferenteProcedimientoDto> listRefProcedimientos = new ArrayList<>();
		try {
			listRefProcedimientos = this.gestionReferenteFacade.listarProcedimientosPorCapituloReferenteNuevo(referente.getFechaInicio(), referente.getFechaFin(),
					referente.getRegimen(),referenteUbicacion.getRegional(), referenteUbicacion.getZonaMunicipio(),referenteUbicacion.getDepartamento(),
					referenteUbicacion.getMunicipio(), referentePrestadorSeleccionado,capitulosSeleccionados,
					categoriasSeleccionadas,referente.getFiltroReferente(),referente,tipoContratoSeleccionados);
		} catch (Exception e) {
			logger.error("error al cargar procedimientos",e);
		}
		return listRefProcedimientos;

	}

	private void borradoReferenteGeneral(Long referenteId){
		try {
			this.gestionReferenteFacade.borrarReferenteProcedeimientoGeneral(referenteId);
			this.gestionReferenteFacade.borrarReferenteCapituloGeneral(referenteId);
			this.gestionReferenteFacade.borrarReferenteSegunFiltro(referenteId);
		} catch (Exception e) {
			logger.error("error al borrar referente",e);
		}
	}



	/**
	 * Permite setear el listado de sedes del prestador en el list sedesprestador
	 * @param prestadorNumeroDocumento
	 */
	public void obtenerSedesVigentesDelPrestador() {
		this.referentePrestador = this.negociacionFacade.consultarSedesVigentesPrestador(prestador.getNumeroDocumento());
		if(Objects.nonNull(referentePrestador) && referentePrestador.size() > 0) {
			this.prestador.setNombre(referentePrestador.get(0).getSedePrestador().getPrestador().getNombre());
		} else {
			this.prestador.setNombre("");
		}
	}

    public void importFiles(FileUploadEvent event) throws InvalidFormatException, IOException, SAXException {

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
 				Sheet hoja = libro.getSheetAt(0);

 			switch(typeImport){
 				case  PROCEDURE_REF_FILE:
 					manageProcedureRef(hoja, typeImport);
 					break;
 				case MEDICINE_REF_FILE:
 					manageMedicineRef(hoja, typeImport);
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


 	private void manageProcedureRef(Sheet hoja, ArchivosNegociacionEnum typeImport) {

 		List<ReferenteProcedimientoDto> listPxReferente = validator.validateProcedures(hoja, typeImport, referente);

 		RequestContext context = RequestContext.getCurrentInstance();

 		if (Objects.nonNull(listPxReferente) && !listPxReferente.isEmpty()) {
 			try {
 				gestionReferenteFacade.insertProceduresReferenteImport(listPxReferente, referente);
 				listarReferenteCapitulo = this.gestionReferenteFacade.cargarReferenteCapituloPorReferente(referenteId);
 				if(referenteCapituloId != null) {
 				listarReferenteProcedimiento = this.gestionReferenteFacade.cargarReferenteProcedimientoPorCapitulo(referenteCapituloId);
 				}
 				this.facesMessagesUtils.addInfo("Importación éxitosa, se han guardado correctamente los datos.");
 			} catch (Exception ex) {
 				this.facesMessagesUtils
 						.addError("Fallo la importación, se generó un error guardando Referente Procedimientos.");
 			}

 		}
 	}


 	private void manageMedicineRef(Sheet hoja, ArchivosNegociacionEnum typeImport) {

 		List<ReferenteMedicamentoDto> listMedReferente = validator.validateMedicines(hoja, typeImport, referente);

 		RequestContext context = RequestContext.getCurrentInstance();

 		if (Objects.nonNull(listMedReferente) && !listMedReferente.isEmpty()) {
 			try {
 				gestionReferenteFacade.insertMedicinesReferenteImport(listMedReferente, referente);
 				listarReferenteCategoriaMedicamento = this.gestionReferenteFacade.cargarReferenteCategotiasReferente(referenteId);
 				if(referenteCategoriaMedicamentoId!= null) {
 				listarReferenteMedicamento = this.gestionReferenteFacade.cargarReferenteMedicamentoPorCategoria(referenteCategoriaMedicamentoId);
 				}
 				this.facesMessagesUtils.addInfo("Importación éxitosa, se han guardado correctamente los datos.");
 			} catch (Exception ex) {
 				this.facesMessagesUtils
 						.addError("Fallo la importación, se generó un error guardando Referente Medicamentos.");
 			}

 		}
 	}

    /**
     * Permite exportar el archivo excel con las tecnologías (medicamentos o procedimientos)
     * @param servicio
     * @throws ClassNotFoundException
     * @throws JRException
     * @throws IOException
     * @throws SQLException
     */
    public void ExportarTecnologiasReferentePGP(String servicio) throws ClassNotFoundException, JRException, IOException, SQLException {
    	FacesContext.getCurrentInstance().responseComplete();
		String filename = "";
		String rutaJasper = "";

		FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(false);
		Long referenteId = (Long) session.getAttribute(NegociacionSessionEnum.REFERENTE_ID.toString());

		switch(servicio) {
			case "procedimientos":
				rutaJasper = "reports/ReporteProcedimientosReferentePGP.jasper";
				filename = "ProcedimientosReferente_"+referente.getDescripcion();
				break;
			case "medicamentos":
				rutaJasper = "reports/ReporteMedicamentosReferentePGP.jasper";
				filename = "MedicamentosReferente_"+referente.getDescripcion();
				break;
			default:
				break;
		}

		//String rutaReporte = FacesContext.getCurrentInstance().getExternalContext().getRealPath(rutaJasper);

		HttpServletResponse httpServletResponse = (HttpServletResponse) FacesContext.getCurrentInstance()
				.getExternalContext().getResponse();
		httpServletResponse.addHeader("Content-disposition", "attachment; filename=" + filename + ".xls");
		httpServletResponse.setContentType("application/vnd.ms-excel");
		ServletOutputStream servletOutputStream = httpServletResponse.getOutputStream();

		switch(servicio) {
			case "procedimientos":
				generarReporteProcedimientosReferentePGP(referenteId, rutaJasper, servletOutputStream, null);
				break;
			case "medicamentos":
				generarReporteMedicamentosReferentePGP(referenteId, rutaJasper, servletOutputStream, null);
				break;
			default:
				break;
		}


		// Registra descargar de anexo tarifario
		//negociacionFacade.registrarDescargaAnexo(idNegociacion, user.getId(), filename);
	}

    /**
     * Genera el reporte de procedimientos del referente PGP
     * @param referenteId
     * @param rutaReporte
     * @param servletOutputStream
     * @param byteArrayOutput
     * @throws JRException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void generarReporteProcedimientosReferentePGP(Long referenteId,
			String rutaReporte, ServletOutputStream servletOutputStream, ByteArrayOutputStream byteArrayOutput)
			throws JRException, IOException, ClassNotFoundException, SQLException {

    	AnexoReferenteTecnologiasDto anexoProcedimientosPGP = new AnexoReferenteTecnologiasDto();
    	anexoProcedimientosPGP.setListaProcedimientos(this.gestionReferenteFacade.exportarReferenteProcedimientoCapituloPGP(referenteId));
		ArrayList<AnexoReferenteTecnologiasDto> dataList = new ArrayList<>();
		dataList.add(anexoProcedimientosPGP);
		JRBeanCollectionDataSource beanDataSource = new JRBeanCollectionDataSource(dataList, true);
		Map<String, Object> parametros = new HashMap<String, Object>();

		if ((null != anexoProcedimientosPGP.getListaProcedimientos())
				&& (anexoProcedimientosPGP.getListaProcedimientos().size() > 0)) {
			parametros.put("procedimientos", anexoProcedimientosPGP.getListaProcedimientos());
		}
		InputStream is = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(rutaReporte);
		JasperPrint jasperPrint = JasperFillManager.fillReport(is, parametros, beanDataSource);

		printReport(jasperPrint, servletOutputStream, byteArrayOutput);
	}



    /**
     * Genera el reporte de medicamentos del referente PGP
     * @param referenteId
     * @param rutaReporte
     * @param servletOutputStream
     * @param byteArrayOutput
     * @throws JRException
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public void generarReporteMedicamentosReferentePGP(Long referenteId,
			String rutaReporte, ServletOutputStream servletOutputStream, ByteArrayOutputStream byteArrayOutput)
			throws JRException, IOException, ClassNotFoundException, SQLException {

    	AnexoReferenteTecnologiasDto anexoMedicamentosPGP = new AnexoReferenteTecnologiasDto();
    	anexoMedicamentosPGP.setListaMedicamentos(this.gestionReferenteFacade.exportarReferenteMedicamentosPGP(referenteId));
		ArrayList<AnexoReferenteTecnologiasDto> dataList = new ArrayList<>();
		dataList.add(anexoMedicamentosPGP);
		JRBeanCollectionDataSource beanDataSource = new JRBeanCollectionDataSource(dataList, true);
		Map<String, Object> parametros = new HashMap<String, Object>();

		if ((Objects.nonNull(anexoMedicamentosPGP.getListaMedicamentos()))
				&& (anexoMedicamentosPGP.getListaMedicamentos().size() > 0)) {
			parametros.put("medicamentos", anexoMedicamentosPGP.getListaMedicamentos());
		}
		InputStream is = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream(rutaReporte);
		JasperPrint jasperPrint = JasperFillManager.fillReport(is, parametros, beanDataSource);

		printReport(jasperPrint, servletOutputStream, byteArrayOutput);
	}


    /**
     * Encargado de imprimir el reporte en un archivo Excel
     * @param jasperPrint
     * @param servletOutputStream
     * @param byteArrayOutput
     * @throws JRException
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void printReport(JasperPrint jasperPrint, ServletOutputStream servletOutputStream, ByteArrayOutputStream byteArrayOutput)
    		throws JRException, IOException, ClassNotFoundException {
    	JRXlsExporter xlsExporter = new JRXlsExporter();
		xlsExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
		xlsExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(Objects.nonNull(servletOutputStream) ? servletOutputStream : byteArrayOutput));

		SimpleXlsExporterConfiguration configuration = new SimpleXlsExporterConfiguration();
		xlsExporter.setConfiguration(configuration);

		jasperPrint.setBottomMargin(113);
		jasperPrint.setLeftMargin(56);
		jasperPrint.setRightMargin(42);
		jasperPrint.setTopMargin(113);
		jasperPrint.setPageHeight(12);

		xlsExporter.exportReport();


		if(Objects.nonNull(servletOutputStream)){
			servletOutputStream.flush();
			servletOutputStream.close();
		}else if(Objects.nonNull(byteArrayOutput)){
			byteArrayOutput.flush();
			byteArrayOutput.close();
		}
    }

    public void setActiveTab(Integer tab) {
    	tabIndex = tab;
    	switch(tab) {
    		case 0:
    			Ajax.update("tablaCapituloReferente");
    			break;
    		case 1:
    			Ajax.update("tablaCategoriasMedicamento");
    			break;
    		default:
    			break;
    	}

    }

    public void validarRangoFechas() {
    	if(Objects.nonNull(this.referente) && Objects.nonNull(this.referente.getFechaFin())
    			 && Objects.nonNull(this.referente.getFechaInicio())) {
    		if(this.referente.getFechaFin().before(this.referente.getFechaInicio())) {
    			this.facesMessagesUtils.addWarning("La fecha fin debe ser mayor a fecha inicio");
    			this.referente.setFechaFin(null);
    		}
    	}
    }

    public List<RegimenNegociacionEnum> getRegimenes() {
        return Arrays.asList(RegimenNegociacionEnum.SUBSIDIADO,
        		RegimenNegociacionEnum.CONTRIBUTIVO,
        		RegimenNegociacionEnum.AMBOS);
    }

    public List<NegociacionModalidadEnum> getModalidades() {
        return Arrays.asList(NegociacionModalidadEnum.EVENTO,
                NegociacionModalidadEnum.CAPITA,
                NegociacionModalidadEnum.RIAS_CAPITA,
                NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO,
                NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO);
    }


    public List<TipoContratoEnum> getTipoContrato() {
        return Arrays.asList(TipoContratoEnum.PERMANENTE,
        		TipoContratoEnum.URGENCIA);
    }

    public List<DepartamentoDto> getDepartamentos() {
		return departamentos;
	}

	public void setDepartamentos(List<DepartamentoDto> departamentos) {
		this.departamentos = departamentos;
	}

	public List<MunicipioDto> getMunicipios() {
		return municipios;
	}

	public void setMunicipios(List<MunicipioDto> municipios) {
		this.municipios = municipios;
	}

	public List<RegionalDto> getRegionales() {
		return regionales;
	}

	public void setRegionales(List<RegionalDto> regionales) {
		this.regionales = regionales;
	}

	public List<ZonaMunicipioDto> getZonasMunicipio() {
		return zonasMunicipio;
	}

	public void setZonasMunicipio(List<ZonaMunicipioDto> zonasMunicipio) {
		this.zonasMunicipio = zonasMunicipio;
	}

	public FiltroReferentePgpEnum[] getFiltroReferentePgpEnum() {
		 return new FiltroReferentePgpEnum[]{FiltroReferentePgpEnum.POR_UBICACION, FiltroReferentePgpEnum.POR_PRESTADOR};
	}

	public FiltroReferentePgpEnum[] getFiltroReferentePgpFechasEnum() {
		 return new FiltroReferentePgpEnum[]{FiltroReferentePgpEnum.FECHA_PRESTACION, FiltroReferentePgpEnum.FECHA_RADICACION};
	}

	public List<SedePrestadorDto> getSedesPrestador() {
        return sedesPrestador;
    }

    public void setSedesPrestador(List<SedePrestadorDto> sedesPrestador) {
        this.sedesPrestador = sedesPrestador;
    }

	public PrestadorDto getPrestador() {
		return prestador;
	}

	public void setPrestador(PrestadorDto prestador) {
		this.prestador = prestador;
	}

	public List<ReferenteCapituloDto> getListarReferenteCapitulo() {
		return listarReferenteCapitulo;
	}

	public void setListarReferenteCapitulo(List<ReferenteCapituloDto> listarReferenteCapitulo) {
		this.listarReferenteCapitulo = listarReferenteCapitulo;
	}

	public List<ReferenteProcedimientoDto> getListarReferenteProcedimiento() {
		return listarReferenteProcedimiento;
	}

	public void setListarReferenteProcedimiento(List<ReferenteProcedimientoDto> listarReferenteProcedimiento) {
		this.listarReferenteProcedimiento = listarReferenteProcedimiento;
	}

	public List<CapituloProcedimientoDto> getListCapitulos() {
		return listCapitulos;
	}

	public void setListCapitulos(List<CapituloProcedimientoDto> listCapitulos) {
		this.listCapitulos = listCapitulos;
	}

	public List<CategoriaProcedimientoDto> getListCategorias() {
		return listCategorias;
	}

	public void setListCategorias(List<CategoriaProcedimientoDto> listCategorias) {
		this.listCategorias = listCategorias;
	}

	public List<CapituloProcedimientoDto> getCapitulosSeleccionados() {
		return capitulosSeleccionados;
	}

	public void setCapitulosSeleccionados(List<CapituloProcedimientoDto> capitulosSeleccionados) {
		this.capitulosSeleccionados = capitulosSeleccionados;
	}

	public List<CategoriaProcedimientoDto> getCategoriasSeleccionadas() {
		return categoriasSeleccionadas;
	}

	public void setCategoriasSeleccionadas(List<CategoriaProcedimientoDto> categoriasSeleccionadas) {
		this.categoriasSeleccionadas = categoriasSeleccionadas;
	}

	public ReferenteDto getReferente() {
		return referente;
	}

	public void setReferente(ReferenteDto referente) {
		this.referente = referente;
	}

	public GestionTecnologiasNegociacionEnum[] getGestionTecnologias() {
		return GestionTecnologiasNegociacionEnum.values();
	}

	public List<ReferenteProcedimientoDto> getReferenteProcedimientoSeleccionados() {
		return referenteProcedimientoSeleccionados;
	}

	public void setReferenteProcedimientoSeleccionados(
			List<ReferenteProcedimientoDto> referenteProcedimientoSeleccionados) {
		this.referenteProcedimientoSeleccionados = referenteProcedimientoSeleccionados;
	}

	public GestionTecnologiasNegociacionEnum getGestionSeleccionada() {
		return gestionSeleccionada;
	}

	public void setGestionSeleccionada(GestionTecnologiasNegociacionEnum gestionSeleccionada) {
		this.gestionSeleccionada = gestionSeleccionada;
	}

	public List<ReferenteCategoriaMedicamentoDto> getListarReferenteCategoriaMedicamento() {
		return listarReferenteCategoriaMedicamento;
	}

	public void setListarReferenteCategoriaMedicamento(
			List<ReferenteCategoriaMedicamentoDto> listarReferenteCategoriaMedicamento) {
		this.listarReferenteCategoriaMedicamento = listarReferenteCategoriaMedicamento;
	}

	public List<ReferenteMedicamentoDto> getListarReferenteMedicamento() {
		return listarReferenteMedicamento;
	}

	public void setListarReferenteMedicamento(List<ReferenteMedicamentoDto> listarReferenteMedicamento) {
		this.listarReferenteMedicamento = listarReferenteMedicamento;
	}

	public List<ErroresImportTecnologiasDto> getListErrors() {
		return listErrors;
	}

	public void setListErrors(List<ErroresImportTecnologiasDto> listErrors) {
		this.listErrors = listErrors;
	}

	public ValidatorImportReferente getValidator() {
		return validator;
	}

	public void setValidator(ValidatorImportReferente validator) {
		this.validator = validator;
	}

	public ReferenteUbicacionDto getReferenteUbicacion() {
		return referenteUbicacion;
	}

	public void setReferenteUbicacion(ReferenteUbicacionDto referenteUbicacion) {
		this.referenteUbicacion = referenteUbicacion;
	}

	public List<ReferentePrestadorDto> getReferentePrestador() {
		return referentePrestador;
	}

	public void setReferentePrestador(List<ReferentePrestadorDto> referentePrestador) {
		this.referentePrestador = referentePrestador;
	}

	public List<ReferentePrestadorDto> getReferentePrestadorSeleccionado() {
		return referentePrestadorSeleccionado;
	}

	public void setReferentePrestadorSeleccionado(List<ReferentePrestadorDto> referentePrestadorSeleccionado) {
		this.referentePrestadorSeleccionado = referentePrestadorSeleccionado;
	}

	public List<ReferenteCapituloDto> getReferenteCapituloSeleccionados() {
		return referenteCapituloSeleccionados;
	}

	public void setReferenteCapituloSeleccionados(List<ReferenteCapituloDto> referenteCapituloSeleccionados) {
		this.referenteCapituloSeleccionados = referenteCapituloSeleccionados;
	}

	public List<ReferenteCategoriaMedicamentoDto> getReferenteCategoriaMedicamentosSeleccionados() {
		return referenteCategoriaMedicamentosSeleccionados;
	}

	public void setReferenteCategoriaMedicamentosSeleccionados(
			List<ReferenteCategoriaMedicamentoDto> referenteCategoriaMedicamentosSeleccionados) {
		this.referenteCategoriaMedicamentosSeleccionados = referenteCategoriaMedicamentosSeleccionados;
	}

	public List<ReferenteMedicamentoDto> getReferenteMedicamentoSeleccionados() {
		return referenteMedicamentoSeleccionados;
	}

	public void setReferenteMedicamentoSeleccionados(List<ReferenteMedicamentoDto> referenteMedicamentoSeleccionados) {
		this.referenteMedicamentoSeleccionados = referenteMedicamentoSeleccionados;
	}

	public Integer getTabIndex() {
		return tabIndex;
	}

	public void setTabIndex(Integer tabIndex) {
		this.tabIndex = tabIndex;
	}

	public List<TipoContratoEnum> getTipoContratoSeleccionados() {
		return tipoContratoSeleccionados;
	}

	public void setTipoContratoSeleccionados(List<TipoContratoEnum> tipoContratoSeleccionados) {
		this.tipoContratoSeleccionados = tipoContratoSeleccionados;
	}

}
