package co.conexia.negociacion.wap.controller.portafolio;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;

import co.conexia.negociacion.wap.controller.common.CommonController;
import co.conexia.negociacion.wap.facade.portafolio.DetallePortafolioFacade;

import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.DiagnosticoPortafolioDto;
import com.conexia.contratacion.commons.dto.GrupoTransporteDto;
import com.conexia.contratacion.commons.dto.InsumoPortafolioDto;
import com.conexia.contratacion.commons.dto.MedicamentoPortafolioDto;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.ProcedimientoPortafolioDto;
import com.conexia.contratacion.commons.dto.ProcedimientoPropioPortafolioDto;
import com.conexia.contratacion.commons.dto.TransportePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.GrupoServicioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoPaqueteDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 * Controller para el detalle del portafolio
 * @author jjoya
 *
 */
@Named
@ViewScoped
@URLMapping(id = "detallePortafolio", pattern = "/detallePortafolio", viewId = "/portafolio/detallePortafolio.page")
public class DetallePortafolioController implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3296319479625049730L;
	
	@Inject
	private CommonController commonController;
	
	@Inject
	private DetallePortafolioFacade facade;
	
	@Inject
	private Log logger;
	
	@Inject
	private FacesMessagesUtils facesMessagesUtils;
	
	@Inject
	@CnxI18n
	transient ResourceBundle resourceBundle;
	
	@Inject
    private FacesUtils faceUtils;
	
	@Inject
	private PreContractualExceptionUtils exceptionUtils;
	
	private PrestadorDto prestadorSeleccionado;
	private List<SedePrestadorDto> sedesPrestador;
	private SedePrestadorDto sedeSeleccionada;
	
	private List<GrupoServicioDto> servicioHabilitacion;
	
	private List<CategoriaMedicamentoDto> categoriasMedicamento;
	private CategoriaMedicamentoDto categoriaSeleccionada;
	
	private List<MedicamentoPortafolioDto> medicamentos;
	
	private List<ProcedimientoPropioPortafolioDto> serviciosPropio;
	
	private List<PaquetePortafolioDto> paquetes;
	
	private List<GrupoTransporteDto> grupoTraslados;
	private GrupoTransporteDto grupoTransporteSeleccionado;
	private List<TransportePortafolioDto> traslados;
	
	private GrupoServicioDto servicioSeleccionado;
	private List<ProcedimientoPortafolioDto> procedimientos;
	private PaquetePortafolioDto paqueteSeleccionado;
	
	private List<DiagnosticoPortafolioDto> diagnosticosPortafolio;
	
	private List<ProcedimientoPaqueteDto> procedimientosPaquete;
	private List<MedicamentoPortafolioDto> medicamentosPaquete;
	private List<InsumoPortafolioDto> insumosPaquete;
	private List<TransportePortafolioDto> trasladosPaquete;
	
	@PostConstruct
	private void postConstruct(){
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
		Long prestadorId = (Long) session.getAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString());
		if(prestadorId == null){
			this.faceUtils.urlRedirect("/bandejaPrestador");
		}else{
			prestadorSeleccionado = commonController.getPrestador(prestadorId);
			this.cargarSedesPrestador();
		}
	}
	
	/**
	 * Consulta las sedes del prestador y sus tecnologias
	 */
	private void cargarSedesPrestador(){
		this.sedesPrestador = this.facade
				.consultarTecnologiasSedesByPrestadorId(this.prestadorSeleccionado
						.getId());
	}
	
	/**
	 * Metodo que consulta y descarga todos los documentos
	 * del prestador
	 */
	public void descargarDocumentosPrestador(){
		try{
			FacesContext fc = FacesContext.getCurrentInstance();
		    ExternalContext ec = fc.getExternalContext();
		    
		    //Consulta de los archivos
		    byte[] data = this.facade.obtenerByteArrayZipDocumentosPorPrestador(prestadorSeleccionado.getId());
	
		    ec.responseReset();
		    ec.setResponseContentType("application/octet-stream");
		    ec.setResponseContentLength(data.length); 
		    ec.setResponseHeader("Content-Disposition",	"attachment; filename=\"documentos_sedes_"+ prestadorSeleccionado.getNumeroDocumento() + ".zip\""); 
	
		    OutputStream output = ec.getResponseOutputStream();
		    
		    final InputStream is = new ByteArrayInputStream(data);
	        IOUtils.copy(is, output);
	
		    fc.responseComplete();
		}catch(ConexiaBusinessException | ConexiaSystemException e){
			this.logger.error("Error al descargarDocumentosPrestador", e);
			this.facesMessagesUtils.addError(this.exceptionUtils.createMessage(resourceBundle, e));
		}catch(IOException e){
			this.logger.error("Error al descargarDocumentosPrestador", e);
			this.facesMessagesUtils.addError(resourceBundle.getString("consultar_documentos_error"));
		}catch (Exception e) {
			this.logger.error("Error al descargarDocumentosPrestador", e);
			this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}
	
	/**
	 * Metodo que consulta y descarga todos los documentos
	 * del prestador
	 */
	public void descargarDocumentosSede(SedePrestadorDto sede){
		sedeSeleccionada = sede;
		try{
			FacesContext fc = FacesContext.getCurrentInstance();
		    ExternalContext ec = fc.getExternalContext();
		    
		    //Consulta de los archivos
		    byte[] data = this.facade.obtenerByteArrayZipDocumentosPorSede(sedeSeleccionada.getId());

		    if(Objects.isNull(data)){
                this.facesMessagesUtils.addError("La sede del prestador no posee documentos adjuntos");
                return;
            }

		    ec.responseReset();
		    ec.setResponseContentType("application/octet-stream");
		    ec.setResponseContentLength(data.length); 
		    ec.setResponseHeader("Content-Disposition",	"attachment; filename=\"documentos_sedes_"+ sedeSeleccionada.getNombreSede() + ".zip\""); 
	
		    OutputStream output = ec.getResponseOutputStream();
		    
		    final InputStream is = new ByteArrayInputStream(data);
	        IOUtils.copy(is, output);
	
		    fc.responseComplete();
		}catch(ConexiaBusinessException | ConexiaSystemException e){
			this.logger.error("Error al descargarDocumentosPrestador", e);
			this.facesMessagesUtils.addError(this.exceptionUtils.createMessage(resourceBundle, e));
		}catch(IOException e){
			this.logger.error("Error al descargarDocumentosPrestador", e);
			this.facesMessagesUtils.addError(resourceBundle.getString("consultar_documentos_error"));
		}catch (Exception e) {
			this.logger.error("Error al descargarDocumentosPrestador", e);
			this.facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
	}
	
	/**
	 * Abre el pop-up con los servicios de habilitacion de la sede
	 * @param sede Sede que se quiere ver
	 * @param tecnologia 
	 * 	1.Servicios
	 * 	2.Medicamentos
	 * 	3.Servicios propios
	 * 	4.paquetes
	 * 	5.traslados
	 */
	public void verTecnologia(SedePrestadorDto sede, Integer tecnologia) {
		this.sedeSeleccionada = sede;
		switch (tecnologia) {
		case 1:
			this.servicioHabilitacion = this.facade
					.consultarServiciosHabilitacionBySedeId(sede.getId());
			break;
		case 2:
			this.categoriasMedicamento = this.facade
					.consultarCategoriasMedicamento(sede.getId());
			break;
		case 3:
			this.serviciosPropio = this.facade
					.consultarServiciosPropiosBySedeId(sede.getId());
			break;
		case 4:
			this.paquetes = this.facade.consultarPaquetesBySedeId(sede.getId());
			break;
		case 5:
			this.grupoTraslados = this.facade.consultarGrupoTrasaladosBySedeId(sede.getId());
			break;
		}
	}
	
	/**
	 * Consulta los medicamentos asociados a una sede y categoria
	 */
	public void consultarMedicamentos(){
		this.medicamentos = this.facade
				.consultarMedicamentosBySedeIdAndCategoriaId(
						this.sedeSeleccionada.getId(),
						categoriaSeleccionada.getId());
	}
	
	/**
	 * Consulta los medicamentos asociados a una sede y grupo
	 */
	public void consultarTraslados(){
		this.traslados = this.facade.consultarTrasladosBySedeIdAndGrupoId(
				this.sedeSeleccionada.getId(),
				this.grupoTransporteSeleccionado.getId());
	}
	
	/**
	 * Consulta los procediientos asociados a un grupo servicio<<<
	 * @param servicio
	 */
	public void verProcedimientos(GrupoServicioDto servicio) {
		servicioSeleccionado = servicio;
		this.procedimientos = this.facade
				.consultarProcedimientoByGrupoServicioId(servicio.getId());
	}
	
	/**
	 * Consulta el detalle de los paquetes
	 * @param paquete
	 */
	public void verDetallePaquetes(PaquetePortafolioDto paquete){
		paqueteSeleccionado = paquete;
		this.diagnosticosPortafolio = this.facade.consultarDiagnosticosPortafolio(paquete.getId());
		this.procedimientosPaquete = this.facade.consultarProcedimientosPaquete(paquete.getId());
		this.medicamentosPaquete = this.facade.consultarMedicamentosPaquete(paquete.getId());
		this.insumosPaquete = this.facade.consultarInsumosPaquete(paquete.getId());
		this.trasladosPaquete = this.facade.consultarTrasladosPaquete(paquete.getId());
	}
	
	public void limpiarMedicamentos() {
		this.medicamentos = null;
	}
	
	public void limpiarTraslados() {
		this.traslados = null;
	}

	public PrestadorDto getPrestadorSeleccionado() {
		return prestadorSeleccionado;
	}

	public void setPrestadorSeleccionado(PrestadorDto prestadorSeleccionado) {
		this.prestadorSeleccionado = prestadorSeleccionado;
	}

	public List<SedePrestadorDto> getSedesPrestador() {
		return sedesPrestador;
	}

	public void setSedesPrestador(List<SedePrestadorDto> sedesPrestador) {
		this.sedesPrestador = sedesPrestador;
	}

	public SedePrestadorDto getSedeSeleccionada() {
		return sedeSeleccionada;
	}

	public void setSedeSeleccionada(SedePrestadorDto sedeSeleccionada) {
		this.sedeSeleccionada = sedeSeleccionada;
	}

	public List<GrupoServicioDto> getServicioHabilitacion() {
		return servicioHabilitacion;
	}

	public void setServicioHabilitacion(List<GrupoServicioDto> servicioHabilitacion) {
		this.servicioHabilitacion = servicioHabilitacion;
	}

	public List<CategoriaMedicamentoDto> getCategoriasMedicamento() {
		return categoriasMedicamento;
	}

	public void setCategoriasMedicamento(
			List<CategoriaMedicamentoDto> categoriasMedicamento) {
		this.categoriasMedicamento = categoriasMedicamento;
	}

	public CategoriaMedicamentoDto getCategoriaSeleccionada() {
		return categoriaSeleccionada;
	}

	public void setCategoriaSeleccionada(
			CategoriaMedicamentoDto categoriaSeleccionada) {
		this.categoriaSeleccionada = categoriaSeleccionada;
	}

	public List<MedicamentoPortafolioDto> getMedicamentos() {
		return medicamentos;
	}

	public void setMedicamentos(List<MedicamentoPortafolioDto> medicamentos) {
		this.medicamentos = medicamentos;
	}

	public List<ProcedimientoPropioPortafolioDto> getServiciosPropio() {
		return serviciosPropio;
	}

	public void setServiciosPropio(
			List<ProcedimientoPropioPortafolioDto> serviciosPropio) {
		this.serviciosPropio = serviciosPropio;
	}

	public List<PaquetePortafolioDto> getPaquetes() {
		return paquetes;
	}

	public void setPaquetes(List<PaquetePortafolioDto> paquetes) {
		this.paquetes = paquetes;
	}

	public List<GrupoTransporteDto> getGrupoTraslados() {
		return grupoTraslados;
	}

	public void setGrupoTraslados(List<GrupoTransporteDto> grupoTraslados) {
		this.grupoTraslados = grupoTraslados;
	}

	public GrupoTransporteDto getGrupoTransporteSeleccionado() {
		return grupoTransporteSeleccionado;
	}

	public void setGrupoTransporteSeleccionado(
			GrupoTransporteDto grupoTransporteSeleccionado) {
		this.grupoTransporteSeleccionado = grupoTransporteSeleccionado;
	}

	public List<TransportePortafolioDto> getTraslados() {
		return traslados;
	}

	public void setTraslados(List<TransportePortafolioDto> traslados) {
		this.traslados = traslados;
	}

	public GrupoServicioDto getServicioSeleccionado() {
		return servicioSeleccionado;
	}

	public void setServicioSeleccionado(GrupoServicioDto servicioSeleccionado) {
		this.servicioSeleccionado = servicioSeleccionado;
	}

	public List<ProcedimientoPortafolioDto> getProcedimientos() {
		return procedimientos;
	}

	public void setProcedimientos(List<ProcedimientoPortafolioDto> procedimientos) {
		this.procedimientos = procedimientos;
	}

	public PaquetePortafolioDto getPaqueteSeleccionado() {
		return paqueteSeleccionado;
	}

	public void setPaqueteSeleccionado(PaquetePortafolioDto paqueteSeleccionado) {
		this.paqueteSeleccionado = paqueteSeleccionado;
	}

	public List<DiagnosticoPortafolioDto> getDiagnosticosPortafolio() {
		return diagnosticosPortafolio;
	}

	public void setDiagnosticosPortafolio(
			List<DiagnosticoPortafolioDto> diagnosticosPortafolio) {
		this.diagnosticosPortafolio = diagnosticosPortafolio;
	}

	public List<ProcedimientoPaqueteDto> getProcedimientosPaquete() {
		return procedimientosPaquete;
	}

	public void setProcedimientosPaquete(
			List<ProcedimientoPaqueteDto> procedimientosPaquete) {
		this.procedimientosPaquete = procedimientosPaquete;
	}

	public List<MedicamentoPortafolioDto> getMedicamentosPaquete() {
		return medicamentosPaquete;
	}

	public void setMedicamentosPaquete(
			List<MedicamentoPortafolioDto> medicamentosPaquete) {
		this.medicamentosPaquete = medicamentosPaquete;
	}

	public List<InsumoPortafolioDto> getInsumosPaquete() {
		return insumosPaquete;
	}

	public void setInsumosPaquete(List<InsumoPortafolioDto> insumosPaquete) {
		this.insumosPaquete = insumosPaquete;
	}

	public List<TransportePortafolioDto> getTrasladosPaquete() {
		return trasladosPaquete;
	}

	public void setTrasladosPaquete(List<TransportePortafolioDto> trasladosPaquete) {
		this.trasladosPaquete = trasladosPaquete;
	}
}
