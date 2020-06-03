package co.conexia.negociacion.wap.controller.comite;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.context.RequestContext;

import co.conexia.negociacion.wap.facade.comite.GestionComiteFacade;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorComiteEnum;
import com.conexia.contratacion.commons.constants.enums.MotivoRechazoComiteEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.TablaPrestadoresComiteDto;
import com.conexia.contratacion.commons.dto.comparacion.ResumenComparacionDto;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import java.io.ByteArrayInputStream;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

/**
 * Controller para la vista de concepto comite
 * @author etorres
 */
@Named
@ViewScoped
@URLMapping(id = "conceptoComite", pattern = "/gestionarComite/conceptoComite", viewId = "/comite/concepto/conceptoComitePrecontratacion.page")
public class ConceptoComiteController implements Serializable{
    
    private static final long serialVersionUID = -5821142040506912347L;

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
    private GestionComiteFacade gestionComiteFacade;
    
    @Inject
    private GestionComiteController gestionComiteController;
    private List<MotivoRechazoComiteEnum> motivosRechazoComite;
    private TablaPrestadoresComiteDto prestadorComite;
    private TablaPrestadoresComiteDto prestadorComiteOriginal;
    
    private List<ResumenComparacionDto> resumenComparacion;
    
    
    private StreamedContent documentoAdjunto;
    
    @PostConstruct
    public void postConstruct(){
    	this.prestadorComite = new TablaPrestadoresComiteDto();
    	this.resumenComparacion = new ArrayList<>();
    }
    
    public void buscarMotivos() {
        motivosRechazoComite = MotivoRechazoComiteEnum.values(prestadorComite.getEstado());
    }
    
    /**
     * carga la informacion inicial del prestador comite para la gestion
     */
    public void cargarInformacion(){
		try {
                        
            
			FacesContext facesContext = FacesContext.getCurrentInstance();
			HttpSession session = (HttpSession) facesContext
					.getExternalContext().getSession(true);
			prestadorComiteOriginal = (TablaPrestadoresComiteDto) session
                                .getAttribute(NegociacionSessionEnum.PRESTADOR_COMITE
							.toString());
			if(prestadorComiteOriginal == null){
				RequestContext context = RequestContext.getCurrentInstance();
				context.execute("PF('dialogConceptoComiteW').close();");
			}else{
                prestadorComite.setTipoModalidad(prestadorComiteOriginal.getTipoModalidad());
				prestadorComite = prestadorComiteOriginal.clone();
				this.resumenComparacion = this.gestionComiteFacade
						.consultarResumenComparacionByPrestadorComiteId(prestadorComite
								.getIdPrestadorComite());
			}
		} catch (CloneNotSupportedException e) {
			logger.error("error al clonar el obj", e);
			facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
		}
    }
    
    /**
     * Persiste los cambios en la BD 
     * donde se guarda el concepto del prestdor
     */
    public void guardarConcepto(){
    	try{
            this.gestionComiteFacade.guardarConceptoPrestadorComite(prestadorComite);
    		this.prestadorComiteOriginal.setEstado(prestadorComite.getEstado());
    		this.prestadorComiteOriginal.setConceptoComite(prestadorComite.getConceptoComite()!=null?prestadorComite.getConceptoComite():null);
            //Muestro mensaje dependiendo de la acción seleccionado
            if (prestadorComiteOriginal.getEstado().equals(EstadoPrestadorComiteEnum.APROBADO)){
                this.facesMessagesUtils.addInfo(this.resourceBundle.getString("gestionar_comite_guardar_solicitud_aprobada"));
            }else{
                this.facesMessagesUtils.addInfo(this.resourceBundle.getString("gestionar_comite_concepto_guardado_ok"));
            }
    		this.gestionComiteController.calcularCasosGestionadosComite();
    	}catch(Exception e){
    		logger.error("Error al guardar el concepto", e);
    		this.facesMessagesUtils.addError(this.exceptionUtils.createSystemErrorMessage(resourceBundle));
    	}
    }
	
	/**
	 * Devuelve la lista de estados del prestador comite
	 * @return lista de {@link - EstadoPrestadorComiteEnum}
	 */
	public List<EstadoPrestadorComiteEnum> getEstadosPrestadorComite(){
		List<EstadoPrestadorComiteEnum> estados = new ArrayList<>();
		estados.add(EstadoPrestadorComiteEnum.APROBADO);
		estados.add(EstadoPrestadorComiteEnum.APLAZADO);
		estados.add(EstadoPrestadorComiteEnum.NEGADO);
		return estados;
	}
	
        public List<MotivoRechazoComiteEnum> getMotivosRechazoComite() {
            motivosRechazoComite = MotivoRechazoComiteEnum.values(prestadorComite.getEstado());
            return this.motivosRechazoComite;
        }
        
    /**
     * Realiza la descarga del acta adjunta a un comite
     *
     * @param dto
     */
    public void descargarActa(ResumenComparacionDto dto) {
        try {
            /*byte[] contenidoActa = bandejaComiteFacade.obtenerContenidoActa(comite.getId());
            actaComite = new DefaultStreamedContent(new ByteArrayInputStream(contenidoActa),
                    "application/pdf", "ActaComite_" + comite.getId() + ".pdf");*/
        } catch (Exception e) {
            logger.error("Error descargando el acta", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }

    }
        
	public TablaPrestadoresComiteDto getPrestadorComite() {
		return prestadorComite;
	}

	public void setPrestadorComite(TablaPrestadoresComiteDto prestadorComite) {
		this.prestadorComite = prestadorComite;
	}

	public List<ResumenComparacionDto> getResumenComparacion() {
		return resumenComparacion;
	}

	public void setResumenComparacion(List<ResumenComparacionDto> resumenComparacion) {
		this.resumenComparacion = resumenComparacion;
	}
    
	
	/**
	 * Evalua si se debe mostrar el componente que permite seleccionar un motivo
	 * 
	 * @return true si el estado de la solicitud es diferente a 'Aprobado' o
	 *         'Programado' false de lo contrario
	 */
	public boolean getMostrarMotivo() {
		return (getPrestadorComite().getEstado()!= null) 
				&& !(getPrestadorComite().getEstado()
				.equals(EstadoPrestadorComiteEnum.APROBADO))
				
				&& !(getPrestadorComite().getEstado()
						.equals(EstadoPrestadorComiteEnum.PROGRAMADO));
	}

    public StreamedContent getDocumentoAdjunto() {
        return documentoAdjunto;
    }

    public void setDocumentoAdjunto(StreamedContent documentoAdjunto) {
        this.documentoAdjunto = documentoAdjunto;
    }

}
