package com.conexia.contractual.wap.controller.contratourgencias;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contratacion.commons.constants.enums.PrestadorRedEnum;
import com.conexia.contratacion.commons.constants.enums.TypeUserContractUrgencyEnum;
import com.conexia.contractual.wap.facade.contratourgencias.BandejaContratoUrgenciasFacade;
import com.conexia.contractual.wap.facade.parametros.ParametrosFacade;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaContratoUrgenciaDto;
import com.conexia.seguridad.UserInfo;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.webutils.FacesUtils;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

@Named
@ViewScoped
@URLMapping(id = "bandejaContratoUrgencias", pattern = "/contratourgencias/bandejaContratoUrgencias", viewId = "/bandejaContratoUrgencias.page")
public class BandejaContratoUrgenciasController extends LazyDataModel<PrestadorDto> {

    @Inject
    @UserInfo
    private UserApp user;
    @Inject
    private BandejaContratoUrgenciasFacade bandejaContratoUrgenciasFacade;
    @Inject
    private FacesUtils facesUtils;
    @Inject
    private ParametrosFacade parametrosFacade;

    private FiltroBandejaContratoUrgenciaDto filtro;
    private List<PrestadorDto> prestadores;
    private List<DepartamentoDto> listDepartamentos;
    private List<MunicipioDto> listMunicipios;
    private List<TipoIdentificacionDto> listTypesIdentification;
    private String typeUserCode;

    @PostConstruct
    public void onload() {
        this.filtro = new FiltroBandejaContratoUrgenciaDto();
        this.prestadores = new ArrayList<>();
        this.listMunicipios = new ArrayList<>();

        this.listTypesIdentification = parametrosFacade.listarTiposIdentificacion();
        this.listDepartamentos = parametrosFacade.listarDepartamentos();
        userClassification();
        limpiarSession();
    }

    @Override
    public List<PrestadorDto> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
        getFiltro().setPagina(first);
        getFiltro().setCantidadRegistros(pageSize);
        getFiltro().setAscendente(sortOrder == SortOrder.ASCENDING);
        getFiltro().setFiltros(filters);
        this.setRowCount(this.bandejaContratoUrgenciasFacade.contarPrestadoresNegociacion(this.filtro, typeUserCode));
        prestadores = bandejaContratoUrgenciasFacade.buscarPrestador(this.filtro, typeUserCode);
        return prestadores;
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

    private void limpiarSession() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        for (NegociacionSessionEnum negociacionEnum : NegociacionSessionEnum.values()) {
            session.removeAttribute(negociacionEnum.toString());
        }
    }

    public void reset() {
        facesUtils.urlRedirect("/contratourgencias/bandejaContratoUrgencias.page");
    }

    public void buscarMunicipios() {
        if (Objects.nonNull(filtro) &&
                Objects.nonNull(filtro.getDepartamentoDto()) &&
                Objects.nonNull(filtro.getDepartamentoDto().getId())) {
            listMunicipios = parametrosFacade.listarMunicipiosPorDepartameto(filtro.getDepartamentoDto().getId());
        } else {
            listMunicipios = new ArrayList<MunicipioDto>();
        }
    }

    //<editor-fold desc="Getters && Setters">
    public List<NegociacionModalidadEnum> getModalidades() {
        return Collections.singletonList(NegociacionModalidadEnum.EVENTO);
    }

    public List<PrestadorRedEnum> getPrestadorEsRed() {
        return Arrays.asList(PrestadorRedEnum.values());
    }

    public FiltroBandejaContratoUrgenciaDto getFiltro() {
        return filtro;
    }

    public void setFiltro(FiltroBandejaContratoUrgenciaDto filtro) {
        this.filtro = filtro;
    }

    public List<DepartamentoDto> getListDepartamentos() {
        return listDepartamentos;
    }

    public void setListDepartamentos(List<DepartamentoDto> listDepartamentos) {
        this.listDepartamentos = listDepartamentos;
    }

    public List<MunicipioDto> getListMunicipios() {
        return listMunicipios;
    }

    public void setListMunicipios(List<MunicipioDto> listMunicipios) {
        this.listMunicipios = listMunicipios;
    }

    public String getTypeUserCode() {
        return typeUserCode;
    }

    public void setTypeUserCode(String typeUserCode) {
        this.typeUserCode = typeUserCode;
    }

    public List<TipoIdentificacionDto> getListTypesIdentification() {
        return listTypesIdentification;
    }

    public void setListTypesIdentification(List<TipoIdentificacionDto> listTypesIdentification) {
        this.listTypesIdentification = listTypesIdentification;
    }
    //</editor-fold>

}
