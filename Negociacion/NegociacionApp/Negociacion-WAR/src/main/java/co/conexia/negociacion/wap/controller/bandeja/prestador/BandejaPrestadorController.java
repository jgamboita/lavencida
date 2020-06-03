package co.conexia.negociacion.wap.controller.bandeja.prestador;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.conexia.contratacion.commons.constants.enums.TipoTecnologiaEnum;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaPrestadorDto;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesMessagesUtils;
import com.conexia.webutils.FacesUtils;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import co.conexia.negociacion.wap.facade.bandeja.prestador.BandejaPrestadorFacade;

/**
 * Controller creado para la bandeja de prestadores
 *
 * @author jjoya
 *
 */
@Named
@ViewScoped
@URLMapping(id = "bandejaPrestador", pattern = "/bandejaPrestador", viewId = "/bandeja/prestador/bandejaPrestador.page")
public class BandejaPrestadorController  extends LazyDataModel<PrestadorDto> {

	@Inject
    private Log logger;

    @Inject
    private FacesMessagesUtils facesMessagesUtils;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject
    private BandejaPrestadorFacade bandejaPrestadorFacade;
    
    @Inject
    private FacesUtils facesUtils;

    private FiltroBandejaPrestadorDto filtro;
    private List<PrestadorDto> prestadores;

    @PostConstruct
    public void onload() {
        this.setFiltro(new FiltroBandejaPrestadorDto());
        prestadores = new ArrayList<PrestadorDto>();
        limpiarSession();
    }
    
    @Override
    public List<PrestadorDto> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        getFiltro().setPagina(first);
        getFiltro().setCantidadRegistros(pageSize);
        getFiltro().setAscendente(sortOrder == SortOrder.ASCENDING);
        getFiltro().setFiltros(filters);
        setRowCount(Math.toIntExact(this.bandejaPrestadorFacade.contarPrestadoresNegociacion(filtro)));
        prestadores = bandejaPrestadorFacade.buscarPrestador(getFiltro());
        return prestadores;
    }

    public void limpiarSession() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        for (NegociacionSessionEnum negociacionEnum : NegociacionSessionEnum.values()) {
            session.removeAttribute(negociacionEnum.toString());
        }
    }

    public void reset() {
    	facesUtils.urlRedirect("/bandejaPrestador");
    }

    public void loadViewVerNegociaciones(long idPrestador) {
        filtro = new FiltroBandejaPrestadorDto();
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString(), idPrestador);
    }

    public void redirecccionDetallePortafolio(Long prestadorId) {
        try {
            guardarPrestadorIdSession(prestadorId);
            FacesContext facesContext = FacesContext.getCurrentInstance();
            ExternalContext context = facesContext.getExternalContext();
            context.redirect("/wap/negociacion/detallePortafolio");
            facesContext.responseComplete();
        } catch (IOException e) {
            logger.error("Error al redireccionar al detalle portafolio.", e);
            facesMessagesUtils.addError(resourceBundle.getString("error_redireccion_detalle_portafolio"));
        }
    }

    public void guardarPrestadorIdSession(Long prestadorId) {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        session.setAttribute(NegociacionSessionEnum.PRESTADOR_ID.toString(), prestadorId);
    }

    //<editor-fold desc="Getters && Setters">
    public List<TipoTecnologiaEnum> getTiposTecnologias() {
        return Arrays.asList(TipoTecnologiaEnum.PROCEDIMIENTOS, TipoTecnologiaEnum.MEDICAMENTOS, TipoTecnologiaEnum.PAQUETES);
    }

    public List<NegociacionModalidadEnum> getModalidades() {
        return Arrays.asList(NegociacionModalidadEnum.values());
    }
    
    public FiltroBandejaPrestadorDto getFiltro() {
        return filtro;
    }

    public List<PrestadorDto> getPrestadores() {
        return prestadores;
    }

    public void setPrestadores(List<PrestadorDto> prestadores) {
        this.prestadores = prestadores;
    }

	public void setFiltro(FiltroBandejaPrestadorDto filtro) {
		this.filtro = filtro;
	}
    //</editor-fold>
}
