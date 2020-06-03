package com.conexia.contractual.wap.facade.parametros;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.conexia.contractual.definitions.view.parametros.ParametrosViewRemote;
import com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.GrupoTransporteDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MacroServicioDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.RangoPoblacionDto;
import com.conexia.contratacion.commons.dto.maestros.RegionalDto;
import com.conexia.contratacion.commons.dto.maestros.RiaDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.seguridad.dto.RolBO;
import com.conexia.seguridad.dto.UserApp;
import com.conexia.servicefactory.CnxService;

/**
 *
 * @author jalvarado
 */
public class ParametrosFacade implements Serializable {

    /**
	 *
	 */
	private static final long serialVersionUID = 7536768176244172337L;
	/**
     * Service view de parametros.
     */
    @Inject
    @CnxService
    private ParametrosViewRemote parametrosViewService;

    public List<CategoriaMedicamentoDto> listarCategoriasMedicamento() {
        return this.parametrosViewService.listarCategoriasMedicamento();
    }

    /**
     * Funcion que permite listar los grupos de transporte.
     *
     * @return List<GrupoTransporteDto>
     */
    public List<GrupoTransporteDto> listarGruposTransporte() {
        return this.parametrosViewService.listarGruposTransporte();
    }

    /**
     * Funcion que permite listar los macro servicios.
     *
     * @return List<MacroServicioDto>
     */
    public List<MacroServicioDto> listarMacroServicios() {
        return this.parametrosViewService.listarMacroServicios();
    }

    /**
     * Funcion que permite listar los tipos de identificacion.
     *
     * @return List<TipoIdentificacionDto>
     */
    public List<TipoIdentificacionDto> listarTiposIdentificacion() {
        return this.parametrosViewService.listarTiposIdentificacion();
    }

    /**
     * Funcion que permite listar los tipos de identificacion.
     *
     * @return List<TipoIdentificacionDto>
     */
    public List<TipoIdentificacionDto> listarTiposIdentificacionIPS() {
        return this.parametrosViewService.listarTiposIdentificacionIPS();
    }

    /**
     * Retorna una lista de departamentos dto.
     *
     * @return lista de departamentos.
     */
    public List<DepartamentoDto> listarDepartamentos() {
        return this.parametrosViewService.listarDepartamentos();
    }

    public List<DepartamentoDto> listarDepartamentosPorRegional(Integer regionalId) {
        return this.parametrosViewService.listarDepartamentosPorRegional(regionalId);
    }

    /**
     * Retorna una lista de municipios dto.
     *
     * @param idDepartamento del departamento.
     * @return lista de municipios.
     */
    public List<MunicipioDto> listarMunicipiosPorDepartameto(final Integer idDepartamento) {
        return this.parametrosViewService.listarMunicipiosPorDepartameto(idDepartamento);
    }

    public List<RegionalDto> listarRegionales() {
        return this.parametrosViewService.listarRegionales();
    }

    public List<RegionalDto> listarRegionalesWithOutNacional() {
        List<RegionalDto> regionales = this.parametrosViewService.listarRegionales();
        regionales.remove(1);
        return regionales;
     }


    public List<RegionalDto> listarRegionalesUsuario(final UserApp userApp) {
        if (userApp.getRegionales().contains(RegionalDto.NACIONAL) || userApp.getRegionales().contains(RegionalDto.CENTRAL)) {
            return this.parametrosViewService.listarRegionales();
        }
        if (userApp.getRoles().stream().map(RolBO::getId).collect(Collectors.toList()).contains(94)){
            List<RegionalDto> collect = userApp.getRegionales().stream().map(ru -> this.parametrosViewService.buscarRegionalPorId(ru)).collect(Collectors.toList());
            collect.add(this.parametrosViewService.buscarRegionalPorId(1));
            return collect;
        }
        return userApp.getRegionales().stream().map(ru -> this.parametrosViewService.buscarRegionalPorId(ru)).collect(Collectors.toList());

    }
    
    
	public PrestadorDto consultarPrestador(Long prestadorId) {
		return this.parametrosViewService.consultarPrestador(prestadorId);
	}

	public Boolean tienePaquetes(Long negociacion) {
		return this.parametrosViewService.tienePaquetes(negociacion);
	}

	public Boolean tieneProcedimientos(Long negociacion, Integer modalidadId) {
		return this.parametrosViewService.tieneProcedimientos(negociacion, modalidadId);
	}

	public Boolean tieneMedicamentos(Long negociacion, Integer modalidadId) {
		return this.parametrosViewService.tieneMedicamentos(negociacion, modalidadId);
	}

	public Boolean tieneProcedimientosRecuperacion(Long negociacion) {
		return this.parametrosViewService.tieneProcedimientosRecuperacion(negociacion);
	}

	public List<RangoPoblacionDto> listarRangoPoblacion(){
		return this.parametrosViewService.listarRangoPoblacion();
	}

	public List<RiaDto> listarRias(){
		return this.parametrosViewService.listarRias();
	}

	public List<SedesNegociacionDto> listarSedesPorNegociacion(Long negociacionId){
		return this.parametrosViewService.listarSedesPorNegociacion(negociacionId);
	}

	public List<RiaDto> consultarTotalRiasByNegociacion(Long negociacionId){
		return this.parametrosViewService.consultarTotalRiasByNegociacion(negociacionId);
	}

	public Double consultarTotalRiasCapitaByNegociacion(Long negociacionId){
		return this.parametrosViewService.consultarTotalRiasCapitaByNegociacion(negociacionId);
	}

    public Boolean tienePoblacion(Long negociacion) {
        return this.parametrosViewService.tienePoblacion(negociacion);
    }
}
