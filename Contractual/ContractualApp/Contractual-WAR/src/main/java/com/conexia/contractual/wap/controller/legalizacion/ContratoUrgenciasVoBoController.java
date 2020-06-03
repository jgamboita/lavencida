package com.conexia.contractual.wap.controller.legalizacion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.event.ToggleEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.Visibility;

import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contratacion.commons.constants.enums.TypeUserContractUrgencyEnum;
import com.conexia.contractual.wap.controller.contratourgencias.BandejaContratoUrgenciasController;
import com.conexia.contractual.wap.facade.contratourgencias.BandejaContratoUrgenciasFacade;
import com.conexia.contractual.wap.facade.legalizacion.ContratoUrgenciasVoBoFacade;
import com.conexia.contractual.wap.facade.parametros.ParametrosFacade;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoUrgenciasDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroContratoUrgenciasDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesUtils;
import com.ocpsoft.pretty.faces.annotation.URLMapping;


/*
 *  Clase que muestra y controla las acciones de los contratos de urgencias
 *  que esten a pasar a visto bueno, es decir, al estado legalizado
 *
 * @author: r.caballero
 * @version: 2018/02/28
 */
@Named
@ViewScoped
@URLMapping(id = "consultaContratosUrgenciasParaVistoBueno", pattern = "/contratourgencias/legalizacion/consultaContratosUrgenciasParaVistoBueno", viewId = "/contratourgencias/legalizacion/bandejaContratoUrgenciasVistoBueno.page")
public class ContratoUrgenciasVoBoController extends LazyDataModel<LegalizacionContratoUrgenciasDto> {

    @Inject
    private FacesUtils facesUtils;

    @Inject
    private ParametrosFacade parametrosFacade;

    @Inject
    private ContratoUrgenciasVoBoFacade contratoUrgenciasVoBoFacade;

    private List<LegalizacionContratoUrgenciasDto> contratoXVistoBueno;
    private List<LegalizacionContratoUrgenciasDto> filteredContratosUrgenciasVoBo;
    private List<NegociacionModalidadEnum> modalidadesNegociacion;
    private List<EstadoLegalizacionEnum> estadosLegalizacion;
    private List<TipoIdentificacionDto> tipoIdentificacion;

    /**
     * El filtro usado en el front para filtrar las busquedas *
     */
    private FiltroContratoUrgenciasDto filtro;

    private String typeUserCode;

    @Inject
    @UserInfo
    private UserApp user;

    @Inject
    private BandejaContratoUrgenciasFacade bandejaContratoUrgenciasFacade;

    private List<Boolean> listToggler;

    @PostConstruct
    public void onload() {
        this.filtro = new FiltroContratoUrgenciasDto();
        this.tipoIdentificacion = this.getParametrosFacade().listarTiposIdentificacion();
        this.contratoXVistoBueno = new ArrayList<>();
        this.userClassification();
        limpiarSession();
        //Se agrega la configuracion por defecto
        listToggler = Arrays.asList(true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true, true);
    }

    public List<LegalizacionContratoUrgenciasDto> load(int first, int pageSize, String sortField,
                                                       SortOrder sortOrder, Map<String, Object> filters) {
        this.getFiltro().setPagina(first);
        this.getFiltro().setCantidadRegistros(pageSize);
        this.getFiltro().setAscendente(sortOrder == SortOrder.ASCENDING);
        this.getFiltro().setFiltros(filters);
        this.setRowCount(this.contratoUrgenciasVoBoFacade.contarContratosUrgenciasParaVistoBueno(getFiltro(), this.typeUserCode));
        this.contratoXVistoBueno = this.contratoUrgenciasVoBoFacade.consultarContratosUrgenciasParaVistoBueno(getFiltro(), this.typeUserCode);
        return contratoXVistoBueno;
    }

    public void limpiarSession() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        for (NegociacionSessionEnum negociacionEnum : NegociacionSessionEnum.values()) {
            session.removeAttribute(negociacionEnum.toString());
        }
    }


    private void userClassification() {
        Map<String, Integer> userClassification = bandejaContratoUrgenciasFacade.getUserClassification(user.getId());

        if (userClassification.get("isUserContratacion") == 1) {
            if (userClassification.get("regional") == 1 || userClassification.get("regional") == 0) {
                typeUserCode = TypeUserContractUrgencyEnum.GERENCIA_SALUD.getCode();
            } else {
                typeUserCode = TypeUserContractUrgencyEnum.GERENCIA_REGIONAL.getCode();
            }
        }

        if (userClassification.get("isUserCuentasMedicas") == 1) {
            typeUserCode = TypeUserContractUrgencyEnum.CUENTAS_MEDICAS.getCode();
        }

    }

    public void gestionaVistoBueno(final Long id) {
        facesUtils.urlRedirect("/contratourgencias/legalizacion/generarVistoBueno/" + id + "/");
    }

    public void limpiar() {
        facesUtils.urlRedirect("/contratourgencias/legalizacion/consultaContratosUrgenciasParaVistoBueno");
    }

    public void onToggle(ToggleEvent e) {
        listToggler.set((Integer) e.getData(), e.getVisibility() == Visibility.VISIBLE);
    }

    public List<NegociacionModalidadEnum> getModalidadesNegociacion() {
        return modalidadesNegociacion;
    }

    public void setModalidadesNegociacion(List<NegociacionModalidadEnum> modalidadesNegociacion) {
        this.modalidadesNegociacion = modalidadesNegociacion;
    }

    public List<EstadoLegalizacionEnum> getEstadosLegalizacion() {
        return estadosLegalizacion;
    }

    public void setEstadosLegalizacion(List<EstadoLegalizacionEnum> estadosLegalizacion) {
        this.estadosLegalizacion = estadosLegalizacion;
    }

    public List<TipoIdentificacionDto> getTipoIdentificacion() {
        return tipoIdentificacion;
    }

    public void setTipoIdentificacion(List<TipoIdentificacionDto> tipoIdentificacion) {
        this.tipoIdentificacion = tipoIdentificacion;
    }

    public ParametrosFacade getParametrosFacade() {
        return parametrosFacade;
    }

    public void setParametrosFacade(ParametrosFacade parametrosFacade) {
        this.parametrosFacade = parametrosFacade;
    }

    public FiltroContratoUrgenciasDto getFiltro() {
        return filtro;
    }

    public void setFiltro(FiltroContratoUrgenciasDto filtro) {
        this.filtro = filtro;
    }

    public String getTypeUserCode() {
        return typeUserCode;
    }

    public void setTypeUserCode(String typeUserCode) {
        this.typeUserCode = typeUserCode;
    }

    public List<LegalizacionContratoUrgenciasDto> getFilteredContratosUrgenciasVoBo() {
        return filteredContratosUrgenciasVoBo;
    }

    public void setFilteredContratosUrgenciasVoBo(List<LegalizacionContratoUrgenciasDto> filteredContratosUrgenciasVoBo) {
        this.filteredContratosUrgenciasVoBo = filteredContratosUrgenciasVoBo;
    }

    public List<Boolean> getListToggler() {
        return listToggler;
    }

    public void setListToggler(List<Boolean> listToggler) {
        this.listToggler = listToggler;
    }

}
