package co.conexia.negociacion.wap.controller.notatecnica;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import com.conexia.contratacion.commons.dto.filtro.FiltroBandejaReferenteDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionCategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionServicioDto;
import com.conexia.webutils.i18n.CnxI18n;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import co.conexia.negociacion.wap.facade.common.CommonFacade;

/**
 * Controlador que maneja la nota técnica
 *
 * @author mcastro
 *
 */
@Named
@ViewScoped
@URLMapping(id = "referentePgp", pattern = "/referentePgp", viewId = "/configuracion/referenteCapita/referentePgp.page")
public class ReferentePgpController implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -5821142040501439547L;

    @Inject
    @CnxI18n
    transient ResourceBundle resourceBundle;

    @Inject 
    private CommonFacade commonFacade;
    
    private FiltroBandejaReferenteDto filtro;

    private List<DepartamentoDto> lsDepartamento;
    private List<MunicipioDto> lsMunicipio;
    private List<UpcLiquidacionServicioDto> lsServicios;
    private List<UpcLiquidacionProcedimientoDto> lsProcedimientos;
    private List<UpcLiquidacionCategoriaMedicamentoDto> lsMedicamentos;
    private List<PrestadorDto> listPrestador;
    private BigDecimal sumPorcentaje;    
    private BigDecimal sumValor;
    
    
    /**
     * Constructor por defecto
     */
    public ReferentePgpController() {
    }

    /**
     * Metodo postConstruct
     */
    @PostConstruct
    public void postConstruct() {
    	filtro = new FiltroBandejaReferenteDto();
    	lsDepartamento = new ArrayList<DepartamentoDto>();
    	lsMunicipio = new ArrayList<MunicipioDto>();
    	listPrestador = new ArrayList<PrestadorDto>();
    }

    public void buscarDepartamentos(){
    	if(Objects.nonNull(filtro) && 
    			Objects.nonNull(filtro.getRegionalDto()) && 
    			Objects.nonNull(filtro.getRegionalDto().getId()) ){
    		lsDepartamento = commonFacade.listarDepartamentosPorRegional(filtro.getRegionalDto().getId());
    	}else{
    		lsDepartamento = new ArrayList<DepartamentoDto>();
    	}
    	lsMunicipio = new ArrayList<MunicipioDto>();
    }
    
    public void buscarMunicipios(){
    	if(Objects.nonNull(filtro) && 
    			Objects.nonNull(filtro.getDepartamentoDto()) && 
    			Objects.nonNull(filtro.getDepartamentoDto().getId()) ){
    		lsMunicipio = commonFacade.listarMunicipiosPorDepartamento(filtro.getDepartamentoDto().getId());
    	}else{
    		lsMunicipio = new ArrayList<MunicipioDto>();
    	}    	
    }
    
    public List<PrestadorDto> autoCompletePrestador(String nombre){
    	listPrestador = new ArrayList<PrestadorDto>();
    	if(Objects.nonNull(nombre)){
    		listPrestador = commonFacade.getPrestadorByNombre(nombre);
    	}
    	return listPrestador;	   	
    }

    public List<UpcLiquidacionServicioDto> getLsServicios() {
        return lsServicios;
    }

    public void setLsServicios(List<UpcLiquidacionServicioDto> lsServicios) {
        this.lsServicios = lsServicios;
    }

    public List<UpcLiquidacionProcedimientoDto> getLsProcedimientos() {
        return lsProcedimientos;
    }

    public void setLsProcedimientos(List<UpcLiquidacionProcedimientoDto> lsProcedimientos) {
        this.lsProcedimientos = lsProcedimientos;
    }

    public FiltroBandejaReferenteDto getFiltro() {
        return filtro;
    }

    public void setFiltro(FiltroBandejaReferenteDto filtro) {
        this.filtro = filtro;
    }

    public List<UpcLiquidacionCategoriaMedicamentoDto> getLsMedicamentos() {
        return lsMedicamentos;
    }

    public void setLsMedicamentos(List<UpcLiquidacionCategoriaMedicamentoDto> lsMedicamentos) {
        this.lsMedicamentos = lsMedicamentos;
    }

    public BigDecimal getSumPorcentaje() {
        return sumPorcentaje;
    }

    public void setSumPorcentaje(BigDecimal sumPorcentaje) {
        this.sumPorcentaje = sumPorcentaje;
    }

    public BigDecimal getSumValor() {
        return sumValor;
    }

    public void setSumValor(BigDecimal sumValor) {
        this.sumValor = sumValor;
    }

	public List<DepartamentoDto> getLsDepartamento() {
		return lsDepartamento;
	}

	public void setLsDepartamento(List<DepartamentoDto> lsDepartamento) {
		this.lsDepartamento = lsDepartamento;
	}

	public List<MunicipioDto> getLsMunicipio() {
		return lsMunicipio;
	}

	public void setLsMunicipio(List<MunicipioDto> lsMunicipio) {
		this.lsMunicipio = lsMunicipio;
	}

	public List<PrestadorDto> getListPrestador() {
		return listPrestador;
	}

	public void setListPrestador(List<PrestadorDto> listPrestador) {
		this.listPrestador = listPrestador;
	}

    
}
