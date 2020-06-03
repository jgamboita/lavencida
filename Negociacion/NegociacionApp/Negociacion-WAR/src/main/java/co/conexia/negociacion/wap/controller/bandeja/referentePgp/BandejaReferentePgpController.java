package co.conexia.negociacion.wap.controller.bandeja.referentePgp;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.conexia.contratacion.commons.constants.enums.FiltroReferentePgpEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.TipoTecnologiaEnum;
import com.conexia.contractual.utils.exceptions.PreContractualExceptionUtils;
import com.conexia.contratacion.commons.dto.referente.FiltroBandejaReferentePgpDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import co.conexia.negociacion.wap.facade.bandeja.referentePgp.BandejaReferentePgpFacade;

/**
 * Controller creado para la bandeja de referentes pgp
 *
 * @author dmora
 *
 */
@Named
@ViewScoped
@URLMapping(id = "bandejaReferentePgp", pattern = "/bandejaReferentePgp", viewId = "/bandeja/referentePgp/bandejaReferentePgp.page")
public class BandejaReferentePgpController  extends LazyDataModel<ReferenteDto >{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


    @Inject
    private FacesUtils faceUtils;

    @Inject
    private BandejaReferentePgpFacade bandejaReferenteFacade;

    @Inject
    private Log logger;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private PreContractualExceptionUtils exceptionUtils;

    private FiltroBandejaReferentePgpDto filtro;

    private Long numeroReferente;

    private RegimenNegociacionEnum regimenSelecionado;

    private String descripcion;

    private FiltroReferentePgpEnum filtroSeleccionado;

    private int crearReferente;



    @PostConstruct
    public void onload(){
    	this.setFiltro(new FiltroBandejaReferentePgpDto());
    }

    @Override
    public List<ReferenteDto> load (int first, int pageSize, String sortField,
            SortOrder sortOrder, Map<String, Object> filters){
    	getFiltro().setPagina(first);
        getFiltro().setCantidadRegistros(pageSize);
        getFiltro().setAscendente(sortOrder == SortOrder.ASCENDING);
        getFiltro().setFiltros(filters);
        this.setRowCount(this.bandejaReferenteFacade.contarReferentePgp(filtro));
        return this.bandejaReferenteFacade.listarReferentePgp(getFiltro());
    }


	public void loadViewGestionarPgp() {
		crearReferente = 1;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
		session.setAttribute(NegociacionSessionEnum.CREA_REFERENTE_ID.toString(), crearReferente);
        this.faceUtils.urlRedirect("/referentePGP/gestionReferentePgp.page");
    }

	public void loadViewVerReferente(Long referenteId){
		crearReferente = 0;
		FacesContext facesContext = FacesContext.getCurrentInstance();
		HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
		session.setAttribute(NegociacionSessionEnum.REFERENTE_ID.toString(), referenteId);
		session.setAttribute(NegociacionSessionEnum.CREA_REFERENTE_ID.toString(), crearReferente);
	}

	/**
	 * Permite eliminar un referentePGP que no este relacionado en ninguna negociación
	 * @param referenteId
	 */
	public void eliminarReferentePgp(long referenteId) {

            try {
            	if(this.bandejaReferenteFacade.contarReferentePgpNegociacionById(referenteId).compareTo(BigInteger.ZERO) == 0) {
            		if(this.bandejaReferenteFacade.eliminarReferentePgp(referenteId) > 0) {
            			facesMessagesUtils.addInfo(resourceBundle.getString("referente_eliminado_mensaje_ok_inicio") + " " + referenteId + " " + resourceBundle.getString("referente_eliminado_mensaje_ok_fin"));
            		} else {
            			facesMessagesUtils.addError(resourceBundle.getString("referente_eliminado_mensaje_error") + " " + referenteId);
            		}
            	} else {
            		facesMessagesUtils.addError(resourceBundle.getString("referente_eliminado_mensaje_error_negociacion"));
            	}
            } catch (Exception e) {
                logger.error("Error al eliminar la negociación No." + referenteId, e);
                facesMessagesUtils.addError(exceptionUtils.createSystemErrorMessage(resourceBundle));
            }

	}

	public void habilitarReferente(Long referenteId){
		try {
			this.bandejaReferenteFacade.habilitarReferente(referenteId);
			facesMessagesUtils.addInfo(resourceBundle.getString("referente_habilitado_mensaje_ok"));
		} catch (Exception e) {
			logger.error("Error al habilitar referente" , e);
		}
	}

	public List<RegimenNegociacionEnum> getRegimenes() {
        return Arrays.asList(RegimenNegociacionEnum.SUBSIDIADO,
        		RegimenNegociacionEnum.CONTRIBUTIVO);
    }

	public List<FiltroReferentePgpEnum> getFiltroReferente() {
        return Arrays.asList(FiltroReferentePgpEnum.POR_UBICACION,
        		FiltroReferentePgpEnum.POR_PRESTADOR);
    }

	public List<TipoTecnologiaEnum> getTiposTecnologias() {
        return Arrays.asList(TipoTecnologiaEnum.PROCEDIMIENTOS,
        		TipoTecnologiaEnum.MEDICAMENTOS);
    }

	public Long getNumeroReferente() {
		return numeroReferente;
	}

	public void setNumeroReferente(Long numeroReferente) {
		this.numeroReferente = numeroReferente;
	}

	public RegimenNegociacionEnum getRegimenSelecionado() {
		return regimenSelecionado;
	}

	public void setRegimenSelecionado(RegimenNegociacionEnum regimenSelecionado) {
		this.regimenSelecionado = regimenSelecionado;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public FiltroReferentePgpEnum getFiltroSeleccionado() {
		return filtroSeleccionado;
	}

	public void setFiltroSeleccionado(FiltroReferentePgpEnum filtroSeleccionado) {
		this.filtroSeleccionado = filtroSeleccionado;
	}

	public FiltroBandejaReferentePgpDto getFiltro() {
		return filtro;
	}

	public void setFiltro(FiltroBandejaReferentePgpDto filtro) {
		this.filtro = filtro;
	}

}
