package co.conexia.negociacion.wap.controller.comparacion.tarifas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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

import org.primefaces.context.RequestContext;

import com.conexia.contratacion.commons.constants.enums.EstadoComiteEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaComparacionDto;
import com.conexia.logfactory.Log;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import co.conexia.negociacion.wap.facade.bandeja.comite.BandejaComiteFacade;
import co.conexia.negociacion.wap.facade.bandeja.comparacion.BandejaComparacionFacade;
import co.conexia.negociacion.wap.facade.common.CommonFacade;

/**
 * Controller utilizado en la bandeja de comparacion de tarifas
 *
 * @author etorres
 */
@Named
@ViewScoped
@URLMapping(id = "bandejaComparacionTarifas", pattern = "/gestionNecesidad/bandeja", viewId = "/bandeja/comparacion/bandejaComparacionTarifa.page")
public class BandejaComparacionTarifasController implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -7446528827340640621L;

    @Inject
    private FacesUtils faceUtils;

    @Inject
    private Log logger;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private BandejaComparacionFacade bandejaComparacionFacade;
    
    @Inject
    private CommonFacade commonFacade;
        
    @Inject
    private BandejaComiteFacade bandejaComiteFacade;

    @Inject
    private PreContractualExceptionUtils exceptionUtils;

    @Inject
    @UserInfo
    private UserApp user;    

    /**
     * El filtro usado en el front para filtrar las busquedas *
     */
    private FiltroBandejaComparacionDto filtro;

    /**
     * Lista de prestadores obtenidos para la tabla general de prestadores. *
     */
    private List<PrestadorDto> prestadores;
    private List<PrestadorDto> prestadoresSeleccionados;
    
    private boolean verRias;

    @PostConstruct
    public void postConstruct() {
        filtro = new FiltroBandejaComparacionDto();
        prestadores = new ArrayList<PrestadorDto>();
    }

    public FiltroBandejaComparacionDto getFiltro() {
        return filtro;
    }

    public void setFiltro(FiltroBandejaComparacionDto filtro) {
        this.filtro = filtro;
    }

    public List<PrestadorDto> getPrestadores() {
        return prestadores;
    }

    public void setPrestadores(List<PrestadorDto> prestadores) {
        this.prestadores = prestadores;
    }

    public List<PrestadorDto> getPrestadoresSeleccionados() {
        return prestadoresSeleccionados;
    }

    public void setPrestadoresSeleccionados(
            List<PrestadorDto> prestadoresSeleccionados) {
        this.prestadoresSeleccionados = prestadoresSeleccionados;
    }
    
    /**
     * devuelve la lista de modalidades con las que puede negociar
     * @return lista de {@link - NegociacionModalidadEnumg}
     */
    public List<NegociacionModalidadEnum> getModalidades() {
        return Arrays.asList(NegociacionModalidadEnum.EVENTO,
                NegociacionModalidadEnum.CAPITA);
    }

    /**
     * Método invocado para lanzar la busqueda de prestadores.
     */
    public void buscarPrestador() {
        try {
            RequestContext.getCurrentInstance().reset("filtrosPrestador");
            prestadores = bandejaComparacionFacade.buscarPrestador(filtro);
        } catch (Exception e) {
            logger.error("Error al realizar la busqueda de prestadores.", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }

    }

    /**
     * Metodo para limpiar el formulario
     */
    public void reset() {
        filtro = new FiltroBandejaComparacionDto();
        if(Objects.nonNull(prestadores)){
            prestadores = new ArrayList<>();
        }
        if(Objects.nonNull(prestadoresSeleccionados)){
            prestadoresSeleccionados = new ArrayList<>();
        }
    }

    /**
     * Metodo que redirecciona para ver los datos del prestador
     */
    public void verPrestador(Long prestadorId) {
        if (prestadorId == null) {
            this.faceUtils.urlRedirect("/gestionNecesidad/bandeja");
        } else {
            try {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                session.setAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString(), prestadorId);
                session.setAttribute(NegociacionSessionEnum.RIAS.toString(), false);
                this.faceUtils.urlRedirect("/gestionNecesidad/comparar");
            } catch (Exception e) {
                logger.error("Error al consultar la prestador con Id:" + prestadorId, e);
                facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
        }
    }
    
    public void compararRias(){
    	FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(NegociacionSessionEnum.RIAS.toString(), true);
        this.faceUtils.urlRedirect("/gestionNecesidad/comparar");
    }
    
    /**
     * Metodo que redirecciona para ver los datos del prestador
     */
    public void verPrestadorCapita(Long prestadorId) {
        if (prestadorId == null) {
            this.faceUtils.urlRedirect("/gestionNecesidad/bandeja");
        } else {
            try {
                FacesContext facesContext = FacesContext.getCurrentInstance();
                HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
                session.setAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString(), prestadorId);
                session.setAttribute(NegociacionSessionEnum.RIAS.toString(), false);
                this.faceUtils.urlRedirect("/bandeja/comparacion/comparacionTarifaCapita.page");
            } catch (Exception e) {
                logger.error("Error al consultar la prestador con Id:" + prestadorId, e);
                facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }
        }
    }
    
    /**
     * Envia un prestador directamente a negociacion
     * Cambia el estado del prestador
     */
    public void enviarNegociacion(){
        try {
            
            if(prestadoresSeleccionados.size() > 0){
                //Se crea un nuevo comite
                ComitePrecontratacionDto comite = new ComitePrecontratacionDto();
                comite.setFechaComite(new Date());
                comite.setFechaLimitePrestadores(new Date());
                comite.setCantidadPrestadores(0);
                comite.setLimitePrestadores(0);
                comite.setEstadoComite(EstadoComiteEnum.FINALIZADO);
                comite.setCantidadPrestadores(prestadoresSeleccionados.size());
                
                Long comiteId = this.bandejaComiteFacade
                        .guardarComiteWithPrestadores(comite, user.getId(),
                                prestadoresSeleccionados);
                
                List<Long> prestadoresId = this.prestadoresSeleccionados.stream()
                        .parallel().map(p -> p.getId())
                        .collect(Collectors.toList());
                this.commonFacade.actualizarEstadoPrestadoresByIds(
                        EstadoPrestadorEnum.EN_NEGOCIACION, prestadoresId);
                this.buscarPrestador();
                facesMessagesUtils.addInfo("Se ha enviado "+prestadoresSeleccionados.size()+" prestador(es) a negociacion, Se asignaron al comite "+comiteId);
            }
        } catch (Exception e) {
            logger.error("Error al enviar prestadores a negociacion:", e);
            facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
        }
    }
    
    public void onChangeModalidad(){
    	if(Objects.nonNull(filtro) && 
    			Objects.nonNull(filtro.getModalidadNegociacion()) && 
    			NegociacionModalidadEnum.CAPITA.equals(filtro.getModalidadNegociacion())){
    		verRias = true; 
    	}
    	verRias = false;    	
    }

	public boolean isVerRias() {
		return verRias;
	}

	public void setVerRias(boolean verRias) {
		this.verRias = verRias;
	}
    
    

}
