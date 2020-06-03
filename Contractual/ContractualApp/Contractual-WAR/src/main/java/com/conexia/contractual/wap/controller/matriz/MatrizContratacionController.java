package com.conexia.contractual.wap.controller.matriz;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.primefaces.model.StreamedContent;

import com.conexia.contratacion.commons.constants.enums.TipoServicioEnum;
import com.conexia.contractual.wap.action.legalizacion.GenerarMinutaAction;
import com.conexia.contractual.wap.facade.matriz.MatrizContratacionFacade;
import com.conexia.contractual.wap.facade.parametros.ParametrosFacade;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.AreaInfluenciaDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MacroServicioDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.RegionalDto;
import com.conexia.contratacion.commons.dto.matriz.FiltroMatrizDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.webutils.FacesUtils;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 * Controller creado para la opcion de consulta de solicitudes a legalizar
 *
 * @author jlopez
 */
@Named
@ViewScoped
@URLMapping(id = "matrizContratacion", pattern = "/matriz/matrizContratacion", viewId = "/matriz/matrizContratacion.page")
public class MatrizContratacionController implements Serializable{

    private static final long serialVersionUID = -1770767268027118039L;

    @Inject
    private FacesUtils facesUtils;

    @Inject
    private GenerarMinutaAction generarMinutaAction;

    @Inject
    private ParametrosFacade parametrosFacade;

    @Inject
    private MatrizContratacionFacade matrizFacade;

    private LegalizacionContratoDto current;
    private List<DepartamentoDto> departamentos;
    private FiltroMatrizDto filtroMatriz;
    private List<MunicipioDto> municipios;
    private StreamedContent pdfFile;
    private List<RegionalDto> regionales;
    private SedePrestadorDto sedeSelected;
    private List<MacroServicioDto> grupos;

    //Dto con los datos de la tabla
    private List<LegalizacionContratoDto> contratos;

    public void buscarMunicipios() {
        this.municipios = this.parametrosFacade.listarMunicipiosPorDepartameto(
                this.filtroMatriz.getDepartamento().getId());
    }

    public void generarMinuta() throws UnsupportedEncodingException {
        try {
            this.pdfFile = this.generarMinutaAction.generarMinuta(this.current);
        } catch (ConexiaBusinessException ex) {
            Logger.getLogger(MatrizContratacionController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public List<AreaInfluenciaDto> getAreas() {
        return (this.sedeSelected != null) ? this.matrizFacade.listarAreaPorSedeContrato(this.sedeSelected.getSedeContratoId()) : null;
    }

    public List<DepartamentoDto> getDepartamentos() {
        return departamentos;
    }

    public List<MunicipioDto> getMunicipios() {
        return municipios;
    }

    public List<RegionalDto> getRegionales() {
        return regionales;
    }

    public List<SedePrestadorDto> getSedes() {
        if (this.current != null) {
            return this.matrizFacade.listarSedesPorContrato(this.current);
        }
        return null;
    }

    public LegalizacionContratoDto getCurrent() {
        return current;
    }

    public void setCurrent(LegalizacionContratoDto current) {
        this.current = current;
        if (this.current.getDescuentos().isEmpty()) {
            this.current.setDescuentos(
                    this.matrizFacade.listarDescuentoPorContrato(this.current));
        }
    }

    public FiltroMatrizDto getFiltroMatriz() {
        return filtroMatriz;
    }

    public void setFiltroMatriz(FiltroMatrizDto filtroMatriz) {
        this.filtroMatriz = filtroMatriz;
    }

    public SedePrestadorDto getSedeSelected() {
        return sedeSelected;
    }

    public void setSedeSelected(SedePrestadorDto sedeSelected) {
        this.sedeSelected = sedeSelected;
    }

    public List<LegalizacionContratoDto> getContratos() {
        return contratos;
    }

    public List<MacroServicioDto> getGrupos() {
        return grupos;
    }

    public void setGrupos(List<MacroServicioDto> grupos) {
        this.grupos = grupos;
    }

    public StreamedContent getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(StreamedContent pdfFile) {
        this.pdfFile = pdfFile;
    }

    @PostConstruct
    private void init() {
        this.grupos = this.parametrosFacade.listarMacroServicios();
        this.departamentos = this.parametrosFacade.listarDepartamentos();
        this.filtroMatriz = new FiltroMatrizDto();
        this.regionales = this.parametrosFacade.listarRegionales();
        this.consultarContratos();
    }

    /**
     * Validacion que se hace para verificar si la tecnologia servicio
     * esta seleccionada ya que de ella dependen los campos: grupos,
     * codigoTecnologia y nombreTecnologia
     * @return
     */
    public boolean isServicioActivo(){
        return filtroMatriz.getTiposServicios() == null || this.filtroMatriz.getTiposServicios().stream().filter(ts -> ts.equals(TipoServicioEnum.SERVICIO)).count()==0;
    }

    /**
     * Consulta los contratos segun los filtros
     */
    public void consultarContratos(){
        this.contratos = this.matrizFacade.listarContratosByFiltros(filtroMatriz);
    }

    /**
     * Accion para limpiar el formulario
     */
    public void limpiarFormulario(){
        this.filtroMatriz = new FiltroMatrizDto();
        this.current = null;
    }
}
