package com.conexia.contractual.definitions.view.parametros;

import java.util.List;

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

/**
 * Service remote de parametros.
 *
 * @author jalvarado
 */
public interface ParametrosViewRemote {
    
    /**
     * Lista de categorias de medicamentos
     * @return Lista de categorias de medicamentos
     */
    List<CategoriaMedicamentoDto> listarCategoriasMedicamento();

    /**
     * Listar los grupos de transporte.
     * 
     * @return  List<GrupoTransporteDto>
     */
    List<GrupoTransporteDto> listarGruposTransporte();
    
    /**
     * Funcion que permite listar los macro servicios de los procedimientos.
     * @return  List<MacroServicioDto>
     */
    List<MacroServicioDto> listarMacroServicios();
    
    /**
     * FUncion que permite listar los tipos de identificacion.
     * @return List<TipoIdentificacionDto>
     */
    List<TipoIdentificacionDto> listarTiposIdentificacion();
    
    /**
     * FUncion que permite listar los tipos de identificacion de las IPS.
     * @return List<TipoIdentificacionDto>
     */
    List<TipoIdentificacionDto> listarTiposIdentificacionIPS();

    /**
     * FUncion que permite listar los departamentos.
     * @return List<DepartamentoDto>
     */
    List<DepartamentoDto> listarDepartamentos();
    
    List<DepartamentoDto> listarDepartamentosPorRegional(Integer regionalId);
    
    /**
     * Lista los municipios de un departamento.
     * @param idDepartamento id del departamento.
     * @return lista de municipios.
     */
    List<MunicipioDto> listarMunicipiosPorDepartameto(final Integer idDepartamento);
    
    /**
     * Listar todas las regionales.
     * @return  RegionalDto
     */
    List<RegionalDto> listarRegionales();
    
    /**
     * Buscar regional por id.
     * @param regionalId - Id de la regional.
     * @return RegionalDto
     */
    RegionalDto buscarRegionalPorId(final Integer regionalId);

	Boolean tienePaquetes(final Long negociacion);

	Boolean tieneProcedimientos(final Long negociacion, Integer modalidadId);

	Boolean tieneMedicamentos(final Long negociacion, Integer modalidadId);
	
	Boolean tieneProcedimientosRecuperacion(final Long negociacion);
	
	/**
	 * Lista el listado del rango poblacion vigente
	 * @return
	 */
	List<RangoPoblacionDto> listarRangoPoblacion();
	
	/**
	 * Lista las Rutas disponibles en la plataforma
	 * @return
	 */
	List<RiaDto>listarRias();

	List<SedesNegociacionDto> listarSedesPorNegociacion(Long negociacionId);

	/**
	 * Consulta el total de Los Rias negociados
	 * @param negociacionId
	 * @return
	 */
	List<RiaDto> consultarTotalRiasByNegociacion(Long negociacionId);
	
	/**
	 * Consulta el total de la negociación de cápita por rias
	 * @param negociacionId
	 * @return
	 */
	Double consultarTotalRiasCapitaByNegociacion(Long negociacionId);

    Boolean tienePoblacion(Long negociacion);

	PrestadorDto consultarPrestador(Long prestadorId);

}
