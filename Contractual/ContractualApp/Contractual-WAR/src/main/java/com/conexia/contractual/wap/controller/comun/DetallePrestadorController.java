package com.conexia.contractual.wap.controller.comun;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contractual.wap.facade.parametros.ParametrosFacade;
import com.conexia.contractual.wap.facade.prestador.PrestadorFacade;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.ocpsoft.pretty.faces.annotation.URLMapping;


/**
 * Controller creado comun para los datos del prestador.
 *
 * @author jLopez
 */
@Named
@ViewScoped
@URLMapping(id = "detallePrestador", pattern = "/comun/detallePrestador", viewId = "/detallePrestador.page")
public class DetallePrestadorController implements Serializable{

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1526929041950670012L;

    @Inject
    private ParametrosFacade parametrosFacade;

    @Inject
    private PrestadorFacade prestadorFacade;

    /**
     * Dtos de tipos de identificacion.
     */
    private List<TipoIdentificacionDto> tiposIdentificacionDto;

    /**
     * Tipo de identificacion seleccionado.
     */
    private TipoIdentificacionDto tipoIdentificacionDto;

    /**
     * Prestador.
     */
    private PrestadorDto prestador;

    /**
     * Indica si el prestador ya tiene prefijo asignado.
     */
    private Boolean tienePrefijo = false;

    /**
     * Indica si se puede editar el prestador.
     */
    private Boolean puedeEditar = true;

    /**
     * muestra el detalle de la ubicacion del prestador.
     */
    private Boolean mostrarUbicacion = true;

    /**
     * Muestra el detalle del representante del prestador.
     */
    private Boolean mostrarRepresentante;

    /**
     * Departamentos.
     */
    private List<DepartamentoDto> departamentos = new ArrayList<>();

    /**
     * Departamento Seleccionado.
     */
    private DepartamentoDto departamento;

    /**
     * Lista de municipios.
     */
    private List<MunicipioDto> municipios = new ArrayList<>();

    /**
     * Municipio Seleccionado.
     */
    private MunicipioDto municipioDto;

    /**
     * Direccion del prestador.
     */
    private String direccion;

    private Long negociacionId;

    /**
     * Constructor.
     */
    public DetallePrestadorController() {
    }


    /**
     * Obtiene El prestador.
     *
     * @return the prestador
     */
    public PrestadorDto getPrestador() {
        return prestador;
    }

    /**
     * Setea el prestador.
     *
     * @param prestador the prestador to set
     */
    public void setPrestador(PrestadorDto prestador) {
        this.prestador = prestador;
        this.setTiposIdentificacionDto(this.parametrosFacade.listarTiposIdentificacionIPS());
        this.setDepartamentos(this.parametrosFacade.listarDepartamentos());
        if(Objects.nonNull(negociacionId)) {
        	this.prestador.setNombre(this.prestadorFacade.sedePrincipal(negociacionId));
        }

        if (this.getPrestador().getPrefijo() != null && this.getPrestador().getPrefijo().length() > 0) {
                tienePrefijo = true;
        }
        if (this.getPrestador() != null && this.getPrestador().getTipoIdentificacionRepLegal() != null) {
            final TipoIdentificacionDto tiDto = this.getTiposIdentificacionDto().stream()
                .filter((TipoIdentificacionDto ti) -> ti.equals(this.getPrestador()
                        .getTipoIdentificacionRepLegal()))
                        .findFirst()
                        .get();
            if (tiDto != null) {
                this.getPrestador().setTipoIdentificacionRepLegal(tiDto);
            }
        }
    }

    /**
     * Metodo que actualiza la informacion registrada del prestador.
     */
    public void guardarPrestador() {
        try {
            if (!this.getTienePrefijo() && !this.prestadorFacade.validarCodigoPrestador(this.getPrestador())) {
                this.getPrestador().setPrefijo("");
                FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "El código ingresado ya esta registrado con otro prestador.", ""));
            } else {
                this.prestadorFacade.actualizarPrestador(this.getPrestador());
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Ha ocurrido un error al validar el prestador.", ""));
        }
    }

    public void buscaMunicipios(){
        this.municipios =  this.parametrosFacade.listarMunicipiosPorDepartameto(departamento.getId());
    }

    //<editor-fold desc="Getters && Setters">
    /**
     * @return the tiposIdentificacionDto
     */
    public List<TipoIdentificacionDto> getTiposIdentificacionDto() {
        return tiposIdentificacionDto;
    }

    /**
     * @param tiposIdentificacionDto the tiposIdentificacionDto to set
     */
    public void setTiposIdentificacionDto(List<TipoIdentificacionDto> tiposIdentificacionDto) {
        this.tiposIdentificacionDto = tiposIdentificacionDto;
    }

    /**
     * @return the tipoIdentificacionDto
     */
    public TipoIdentificacionDto getTipoIdentificacionDto() {
        return tipoIdentificacionDto;
    }

    /**
     * @param tipoIdentificacionDto the tipoIdentificacionDto to set
     */
    public void setTipoIdentificacionDto(TipoIdentificacionDto tipoIdentificacionDto) {
        this.tipoIdentificacionDto = tipoIdentificacionDto;
    }

    /**
     * @return the tienePrefijo
     */
    public Boolean getTienePrefijo() {
        return tienePrefijo;
    }

    /**
     * @param tienePrefijo the tienePrefijo to set
     */
    public void setTienePrefijo(Boolean tienePrefijo) {
        this.tienePrefijo = tienePrefijo;
    }

    /**
     * @return the puedeEditar
     */
    public Boolean getPuedeEditar() {
        return puedeEditar;
    }

    /**
     * @param puedeEditar the puedeEditar to set
     */
    public void setPuedeEditar(Boolean puedeEditar) {
        this.puedeEditar = puedeEditar;
    }

    /**
     * @return the mostrarUbicacion
     */
    public Boolean getMostrarUbicacion() {
        return mostrarUbicacion;
    }

    /**
     * @param mostrarUbicacion the mostrarUbicacion to set
     */
    public void setMostrarUbicacion(Boolean mostrarUbicacion) {
        this.mostrarUbicacion = mostrarUbicacion;
    }

    /**
     * @return the mostrarRepresentante
     */
    public Boolean getMostrarRepresentante() {
        return mostrarRepresentante;
    }

    /**
     * @param mostrarRepresentante the mostrarRepresentante to set
     */
    public void setMostrarRepresentante(Boolean mostrarRepresentante) {
        this.mostrarRepresentante = mostrarRepresentante;
    }

    /**
     * @return the departamentos
     */
    public List<DepartamentoDto> getDepartamentos() {
        return departamentos;
    }

    /**
     * @param departamentos the departamentos to set
     */
    public void setDepartamentos(List<DepartamentoDto> departamentos) {
        this.departamentos = departamentos;
    }

    /**
     * @return the departamento
     */
    public DepartamentoDto getDepartamento() {
        return departamento;
    }

    /**
     * @param departamento the departamento to set
     */
    public void setDepartamento(DepartamentoDto departamento) {
        this.departamento = departamento;
    }

    /**
     * @return the municipios
     */
    public List<MunicipioDto> getMunicipios() {
        return municipios;
    }

    /**
     * @param municipios the municipios to set
     */
    public void setMunicipios(List<MunicipioDto> municipios) {
        this.municipios = municipios;
    }

    /**
     * @return the municipioDto
     */
    public MunicipioDto getMunicipioDto() {
        return municipioDto;
    }

    /**
     * @param municipioDto the municipioDto to set
     */
    public void setMunicipioDto(MunicipioDto municipioDto) {
        this.municipioDto = municipioDto;
    }

    /**
     * @return the direccion
     */
    public String getDireccion() {
        return direccion;
    }

    /**
     * @param direccion the direccion to set
     */
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Long getNegociacionId() {
        return negociacionId;
    }

    public void setNegociacionId(Long negociacionId) {
        this.negociacionId = negociacionId;
    }
    //</editor-fold>
}
