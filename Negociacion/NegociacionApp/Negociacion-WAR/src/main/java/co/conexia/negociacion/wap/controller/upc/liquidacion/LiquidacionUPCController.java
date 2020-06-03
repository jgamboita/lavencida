package co.conexia.negociacion.wap.controller.upc.liquidacion;

import java.io.Serializable;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaLiquidacionUPCDto;
import com.conexia.contratacion.commons.dto.norma.GrupoNormaDto;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador que maneja el tema de liquidacion de la UPC
 * @author mcastro
 *
 */
@Named
@ViewScoped
@URLMapping(id = "liquidacionUPC", pattern = "/liquidacionUPC", viewId = "/UPC/liquidacion/liquidacionUPC.page")
public class LiquidacionUPCController implements Serializable{

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
    
    private FiltroBandejaLiquidacionUPCDto filtro;
    
    private List<GrupoNormaDto> lsGrupoNorma;
    
    private List<GrupoNormaDto> lsDatosDepartamento;
	
	/**
	 * Constructor por defecto
	 */
	public LiquidacionUPCController(){}

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
        gn1.setPorcPrima(10D);
        gn1.setValorAño(10D);
        gn1.setValorMensual(12500D);
        lsGrupoNorma.add(gn1);
        
        GrupoNormaDto gn2 = new GrupoNormaDto();
        gn2.setNombreGrupo("1 - 5");
        gn2.setPorcPrima(10D);
        gn2.setValorAño(10D);
        gn2.setValorMensual(12500D);
        lsGrupoNorma.add(gn2);
        
        lsDatosDepartamento = new ArrayList<GrupoNormaDto>();
        GrupoNormaDto tn1 = new GrupoNormaDto();
        tn1.setNombreGrupo("Cundinamarca");
        tn1.setPorcPrima(10D);
        tn1.setValorAño(10D);
        tn1.setValorMensual(12500D);
        lsDatosDepartamento.add(tn1);
        
        GrupoNormaDto tn2 = new GrupoNormaDto();
        tn2.setNombreGrupo("Valle del Cauca");
        tn2.setPorcPrima(7D);
        tn2.setValorAño(7D);
        tn2.setValorMensual(18500D);
        lsDatosDepartamento.add(tn2);
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

    public List<GrupoNormaDto> getLsDatosDepartamento() {
        return lsDatosDepartamento;
    }

    public void setLsDatosDepartamento(List<GrupoNormaDto> lsDatosDepartamento) {
        this.lsDatosDepartamento = lsDatosDepartamento;
    }

    public FiltroBandejaLiquidacionUPCDto getFiltro() {
        return filtro;
    }

    public void setFiltro(FiltroBandejaLiquidacionUPCDto filtro) {
        this.filtro = filtro;
    }
    
}
