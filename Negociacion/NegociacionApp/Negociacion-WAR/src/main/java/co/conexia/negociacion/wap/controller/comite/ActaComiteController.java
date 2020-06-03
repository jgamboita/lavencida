package co.conexia.negociacion.wap.controller.comite;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRBeanArrayDataSource;

import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import co.conexia.negociacion.wap.facade.comite.ActaComiteFacade;
import co.conexia.negociacion.wap.facade.comite.GestionComiteFacade;

import com.conexia.contratacion.commons.constants.enums.ComiteSessionEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.comite.ActaComiteDto;
import com.conexia.contratacion.commons.dto.comite.AsistenteComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.CompromisoComiteDto;
import com.conexia.contratacion.commons.dto.comparacion.ResumenGestionCasosDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 * Controller creado para la gestion de comités
 * @author jtorres
 *
 */
@Named("actaComite")
@ViewScoped
@URLMapping(id = "actaComite", pattern = "/actaComite", viewId = "/comite/acta.page")
public class ActaComiteController implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5821142040506999547L;

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
	
	@Inject private GestionComiteFacade gestionComiteFacade;
	
	@Inject private ActaComiteFacade actaComiteFacade;
	
	@Inject private GestionComiteController gestionComiteController;
		
	/** Lista de asistentes al comite seleccionado. **/
	private List<AsistenteComitePrecontratacionDto> asistentes;
	
	private ComitePrecontratacionDto comiteActual;
        
    private ResumenGestionCasosDto resumenGestionCasos;
    
    private ActaComiteDto acta = new ActaComiteDto(); 
	
	
	/**
	 * Constructor por defecto
	 */
	public ActaComiteController(){}

	/**
	 * Metodo postConstruct
	 */
	/**
	 * Metodo postConstruct
	 */
	@PostConstruct
	public void postConstruct(){
		this.resumenGestionCasos = new ResumenGestionCasosDto();
		getActa().setFechaActa(new Date()); 
		
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
		this.comiteActual = (ComitePrecontratacionDto) session.getAttribute(ComiteSessionEnum.COMITE_PRECONTRATACION.toString());
		if(comiteActual==null){
			regresar();
		}else{
			//Se busca los asistentes al comite..
			this.buscarAsistentesComite();
			getActa().setComite(comiteActual);
		}
	}	
	
	/**
	 * Verifica si el rol esta capacitado para gestionar comites.
	 * @return
	 */
	public boolean validarRolGestionarComites(){
		//TODO: Traer el rol apropiado.
		return true;
	}
	
	/**
	 * Método que busca los comites creados en el sistema.
	 */
	private List<AsistenteComitePrecontratacionDto> buscarAsistentesComite(){
		try {
			asistentes = gestionComiteFacade.buscarAsistentesComite(comiteActual.getId());
		}catch(Exception e){
			logger.error("Error al realizar la busqueda de asistentes al comite: "+this.comiteActual.getId(), e);
			facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
		
		return asistentes;
	}

	
	/* Acciones */
	
	public void adicionarCompromiso(){
		getActa().getCompromisos().add(new CompromisoComiteDto());
	}
	
	public void eliminarCompromiso(CompromisoComiteDto compromiso){
		getActa().getCompromisos().remove(compromiso);
                facesMessagesUtils.addInfo(resourceBundle.getString("acta_comite_msg_eliminar_compromiso_ok"));
	}
	
	public void regresar(){
		faceUtils.urlRedirect("/gestionarComite");
	}
	
	public void generarActa(){		
		try {
			actaComiteFacade.registrarActaComite(getActa());
                        facesMessagesUtils.addInfo(resourceBundle.getString("acta_comite_msg_acta_ok"));
			regresar();			
		} catch (ConexiaBusinessException e) {
			for(String msg : e.getParams()){
				facesMessagesUtils.addWarning(msg);
			}
		}
	}
	
	public StreamedContent imprimirActa(){
		StreamedContent content = null;
		
		try {			
			Long comiteId = getComiteActual().getId();			
			
			ActaComiteDto actaComite = actaComiteFacade.obtenerActaPorComiteId(comiteId);
			actaComite.setParticipantes(gestionComiteFacade.buscarAsistentesComite(comiteId));
			actaComite.setSolicitudesComite(gestionComiteFacade.buscarPrestadoresComiteByComiteId(comiteId));
			actaComite.setCompromisos(actaComiteFacade.obtenerCompromisosPorComiteId(comiteId));
			
			/* Segmento que reutiliza logica del metodo */
			gestionComiteController.setPrestadoresComite(actaComite.getSolicitudesComite());
			actaComite.setResumen(gestionComiteController.getResumenGestionCasos());
			
			InputStream in = FacesContext
					.getCurrentInstance()
					.getExternalContext()
					.getResourceAsStream(
							"/reports/actacomite/acta_comite.jasper");
			
			InputStream imgStream = FacesContext
					.getCurrentInstance()
					.getExternalContext()
					.getResourceAsStream(
							"/reports/logoemssanar.png");
			
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			Map<String, Object> parametros = new HashMap<String, Object>();
			parametros.put("comiteId", comiteId);
			parametros.put("imgStream", imgStream);
			parametros.put("SUBREPORT_DIR", FacesContext
					.getCurrentInstance()
					.getExternalContext()
					.getResource("/reports/actacomite/").getFile());
			
			JasperPrint jasperPrint = JasperFillManager.fillReport(in,
					parametros, new JRBeanArrayDataSource(
							new ActaComiteDto[] { actaComite }));
			
			JasperExportManager.exportReportToPdfStream(jasperPrint, out);
				
			in.close();		
			in = new ByteArrayInputStream(out.toByteArray());
			
	        content = new DefaultStreamedContent(in, "application/pdf", "acta_comite_" + comiteId + ".pdf");
				
	        in.close();
	        imgStream.close();
	        out.close();
        
		} catch(JRException | IOException e){
			facesMessagesUtils.addError("Error inesperado en el servidor.",
					e.getMessage());
		}
		
		return content;		
	}
	
    /**
	 * @return the asistentes
	 */
	public List<AsistenteComitePrecontratacionDto> getAsistentes() {
		return asistentes;
	}
	
	/**
	 * @param asistentes the asistentes to set
	 */
	public void setAsistentes(List<AsistenteComitePrecontratacionDto> asistentes) {
		this.asistentes = asistentes;
	}
	
	/**
	 * @return the comiteActual
	 */
	public ComitePrecontratacionDto getComiteActual() {
		return comiteActual;
	}

	/**
	 * @param comiteActual the comiteActual to set
	 */
	public void setComiteActual(ComitePrecontratacionDto comiteActual) {
		this.comiteActual = comiteActual;
	}

	public ResumenGestionCasosDto getResumenGestionCasos() {
		return resumenGestionCasos;
	}

	public ActaComiteDto getActa() {
		return acta;
	}

	public void setActa(ActaComiteDto acta) {
		this.acta = acta;
	}
}
