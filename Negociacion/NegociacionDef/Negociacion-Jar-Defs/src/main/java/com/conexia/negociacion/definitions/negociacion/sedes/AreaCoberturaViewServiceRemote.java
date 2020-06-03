package com.conexia.negociacion.definitions.negociacion.sedes;

import java.util.List;

import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 * Interface remota para el boundary de area de cobertura
 *
 * @author dprieto
 *
 */
public interface AreaCoberturaViewServiceRemote {

    /**
     * Metodo de definicion que consulta los municipios referentes de cobertura
     * para una sedes negociacion dada
     *
     * @param sedeNegociacionId identificador de la sede
     * @return lista de municipios referentes
     */
    List<MunicipioDto> consultarMunicipiosSeleccionadosCoberturaSedesNegociacion(Long sedeNegociacionId, Boolean seleccionado);

    /**
     * Metodo de definicion que consulta los municipios seleccionados de
     * cobertura para una sedes negociacion dada
     *
     * @param sedeNegociacionId identificador de la sede
     * @return lista de municipios referentes
     */
    List<MunicipioDto> consultarMunicipiosCoberturaSedesNegociacion(Long sedeNegociacionId);

    Integer consultarPoblacionNegociacion(Long negociacionId);

    /**
     * Valida si existen sedes con el mismo departamento en la sede negociación.
     *
     * @param sedeNegociacionComparar DTO de la sede a comparar debe incluir los
     * municipios asociados al área de cobertura.
     * @return <b>True</b> si solo sí encuentra otra sede negociación con alguno
     * de los departamentos asociados a la sede comparada. <b>False</b> de lo
     * contrario.
     */
    boolean consultarSedesNegociacionDepartamento(SedesNegociacionDto sedeNegociacionComparar);

    /**
     *
     * @param negociacionId
     * @param sedePrestadorId
     * @return
     * @throws com.conexia.exceptions.ConexiaBusinessException
     */
    SedesNegociacionDto consultarSedeNegociacionByNegociacionIdAndSedePrestadorId(
            Long negociacionId, Long sedePrestadorId) throws ConexiaBusinessException;

    /**
     * Consulta los municipios con area de cobertura por negociacion
     * @param negociacionId Identificador de la negociacion
     * @return Lista de municipios
     */
    List<MunicipioDto> consultarMunicipiosSeleccionadosByNegociacion(
            Long negociacionId);

}
