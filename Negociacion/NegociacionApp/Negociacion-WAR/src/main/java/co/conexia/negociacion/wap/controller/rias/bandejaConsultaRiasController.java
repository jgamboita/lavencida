package co.conexia.negociacion.wap.controller.rias;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.omnifaces.util.Ajax;

import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.RangoPoblacionDto;
import com.conexia.contratacion.commons.dto.maestros.RegionalDto;
import com.conexia.contratacion.commons.dto.maestros.RiaDto;
import com.conexia.logfactory.Log;
import com.conexia.webutils.FacesUtils;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

import co.conexia.negociacion.wap.facade.bandeja.rias.BandejaConsultaRiasFacade;
import co.conexia.negociacion.wap.facade.common.CommonFacade;

/**
 * Controller creado para la bandeja de consultas de rias
 *
 * @author dmora
 *
 */

@Named
@ViewScoped
@URLMapping(id = "bandejaConsultaRias", pattern = "/bandejaConsultaRias", viewId = "bandeja/rias/bandejaConsultaRias.page")
public class bandejaConsultaRiasController implements Serializable {

	private static final long serialVersionUID = 1L;

	private List<DepartamentoDto> departamentos;

	private List<MunicipioDto> municipios;

	private List<RangoPoblacionDto> rangoPoblacional;

    private List<RiaDto> rias;

    private List<RangoPoblacionDto> rangoPoblacionalSeleccionados;

    private List<RiaDto> riasSeleccionados;

    private DepartamentoDto departamento;

    private MunicipioDto municipioDto;

    private RegionalDto regionalDto;

    private List<RiaDto> rutaSalud;

    private List<RegionalDto> regionales;


    private boolean conRangoPoblacion;

    private Integer opcionFiltroBandeja;

	/**
     * Facade de parametros.
     */
    @Inject
    private CommonFacade parametrosFacada;

    @Inject
    private BandejaConsultaRiasFacade riasFacade;

    @Inject
    private FacesUtils facesUtils;

    @Inject
    private Log logger;



	@PostConstruct
	public void onload() {
		rias = parametrosFacada.listarRias();
		departamentos = parametrosFacada.listarDepartamentos();
		regionales = parametrosFacada.listarRegionales();
	}

	public void buscarMunicipios(){
		this.municipios = parametrosFacada.listarMunicipiosPorDepartameto(departamento.getId());
	}

	public void  buscarRutaSalud(){
		try {
			rutaSalud = riasFacade.buscarRias(riasSeleccionados, departamento, municipioDto, regionalDto);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}

	}

	public void gestionarCupsBandejaRias(Integer opcionFiltro){
		List<RiaDto> listRutaGestionada = new ArrayList<RiaDto>();
		//Visualizar todos
		if(opcionFiltroBandeja == 1){
			listRutaGestionada = rutaSalud;
			Ajax.update("rutaSaludTab");
		}
		//Visualizar cups contratados
		else if (opcionFiltroBandeja == 2 ){
			for(RiaDto ruta : rutaSalud){
				if(ruta.getNegociacion().getId() != 0){
					listRutaGestionada.add(ruta);
				}
			}
			rutaSalud = listRutaGestionada;
			Ajax.update("rutaSaludTab");
		}
		//Visualizar cups no contratados
		else if (opcionFiltroBandeja == 3){
			for(RiaDto ruta : rutaSalud){
				if(ruta.getNegociacion().getId() == 0){
					listRutaGestionada.add(ruta);
				}
			}
			rutaSalud = listRutaGestionada;
			Ajax.update("rutaSaludTab");
		}
	}

	public void limpiar(){
		facesUtils.urlRedirect("/bandeja/rias/bandejaConsultaRias.page");
	}

	public List<DepartamentoDto> getDepartamentos() {
		return departamentos;
	}

	public void setDepartamentos(List<DepartamentoDto> departamentos) {
		this.departamentos = departamentos;
	}

	public List<MunicipioDto> getMunicipios() {
		return municipios;
	}

	public void setMunicipios(List<MunicipioDto> municipios) {
		this.municipios = municipios;
	}

	public List<RangoPoblacionDto> getRangoPoblacional() {
		return rangoPoblacional;
	}

	public void setRangoPoblacional(List<RangoPoblacionDto> rangoPoblacional) {
		this.rangoPoblacional = rangoPoblacional;
	}

	public List<RiaDto> getRias() {
		return rias;
	}

	public void setRias(List<RiaDto> rias) {
		this.rias = rias;
	}

	public DepartamentoDto getDepartamento() {
		return departamento;
	}

	public void setDepartamento(DepartamentoDto departamento) {
		this.departamento = departamento;
	}

	public MunicipioDto getMunicipioDto() {
		return municipioDto;
	}

	public void setMunicipioDto(MunicipioDto municipioDto) {
		this.municipioDto = municipioDto;
	}

	public List<RangoPoblacionDto> getRangoPoblacionalSeleccionados() {
		return rangoPoblacionalSeleccionados;
	}

	public void setRangoPoblacionalSeleccionados(List<RangoPoblacionDto> rangoPoblacionalSeleccionados) {
		this.rangoPoblacionalSeleccionados = rangoPoblacionalSeleccionados;
	}

	public List<RiaDto> getRiasSeleccionados() {
		return riasSeleccionados;
	}

	public void setRiasSeleccionados(List<RiaDto> riasSeleccionados) {
		this.riasSeleccionados = riasSeleccionados;
	}

	public List<RiaDto> getRutaSalud() {
		return rutaSalud;
	}

	public void setRutaSalud(List<RiaDto> rutaSalud) {
		this.rutaSalud = rutaSalud;
	}

	public RegionalDto getRegionalDto() {
		return regionalDto;
	}

	public void setRegionalDto(RegionalDto regionalDto) {
		this.regionalDto = regionalDto;
	}

	public List<RegionalDto> getRegionales() {
		return regionales;
	}

	public void setRegionales(List<RegionalDto> regionales) {
		this.regionales = regionales;
	}

	public boolean isConRangoPoblacion() {
		return conRangoPoblacion;
	}

	public void setConRangoPoblacion(boolean conRangoPoblacion) {
		this.conRangoPoblacion = conRangoPoblacion;
	}

	public Integer getOpcionFiltroBandeja() {
		return opcionFiltroBandeja;
	}

	public void setOpcionFiltroBandeja(Integer opcionFiltroBandeja) {
		this.opcionFiltroBandeja = opcionFiltroBandeja;
	}
}
