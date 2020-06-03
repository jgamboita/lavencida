package co.conexia.negociacion.wap.controller.portafolio.basico;

import co.conexia.negociacion.wap.controller.configuracion.*;
import java.io.Serializable;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.norma.GrupoNormaDto;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador que maneja la pantalla de cargue de la norma
 * @author mcastro
 *
 */
@Named
@ViewScoped
@URLMapping(id = "portafolioBasico", pattern = "/portafolioBasico", viewId = "/portafolio/basico/portafolioBasico.page")
public class PortafolioBasicoController implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5821142040501439547L;

	@Inject
	private Log logger;

	@Inject
	private FacesMessagesUtils facesMessagesUtils;

	@Inject
	@CnxI18n
	transient ResourceBundle resourceBundle;

	@Inject
	private PreContractualExceptionUtils exceptionUtils;
    
    private List<GrupoNormaDto> lsGrupoNorma;
    
    private PrestadorDto prestadorDto;
	
	/**
	 * Constructor por defecto
	 */
	public PortafolioBasicoController(){}

	/**
	 * Metodo postConstruct
	 */
	@PostConstruct
	public void postConstruct(){
		limpiarSession();
        dummyPreCarga();
	}
    
    private void dummyPreCarga(){
        lsGrupoNorma = new ArrayList<GrupoNormaDto>();
        GrupoNormaDto gn1 = new GrupoNormaDto();
        gn1.setNombreGrupo("< 1");
        gn1.setPorcPrima(5D);
        gn1.setValorAño(12500D);
        gn1.setValorMensual(gn1.getValorAño()/12);
        gn1.setValorPrima(gn1.getPorcPrima()*gn1.getValorAño());
        lsGrupoNorma.add(gn1);
        
        GrupoNormaDto gn2 = new GrupoNormaDto();
        gn2.setNombreGrupo("1 - 5");
        gn2.setPorcPrima(7D);
        gn2.setValorAño(18500D);
        gn2.setValorMensual(gn2.getValorAño()/12);
        gn2.setValorPrima(gn2.getPorcPrima()*gn2.getValorAño());
        lsGrupoNorma.add(gn2);
    }
	
	public void limpiarSession(){
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
		/*for(NegociacionSessionEnum negociacionEnum :NegociacionSessionEnum.values()){
			session.removeAttribute(negociacionEnum.toString());
		}*/
	}

    public List<GrupoNormaDto> getLsGrupoNorma() {
        return lsGrupoNorma;
    }

    public void setLsGrupoNorma(List<GrupoNormaDto> lsGrupoNorma) {
        this.lsGrupoNorma = lsGrupoNorma;
    }

    public PrestadorDto getPrestadorDto() {
        return prestadorDto;
    }

    public void setPrestadorDto(PrestadorDto prestadorDto) {
        this.prestadorDto = prestadorDto;
    }
    
}
