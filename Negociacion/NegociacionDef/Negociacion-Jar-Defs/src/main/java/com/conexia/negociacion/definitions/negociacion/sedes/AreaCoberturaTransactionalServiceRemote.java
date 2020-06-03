package com.conexia.negociacion.definitions.negociacion.sedes;

import com.conexia.contratacion.commons.constants.enums.AreaCoberturaTipoEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;

import java.util.List;

import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;

/**
 * Interface remota para el boundary transaccional de area de cobertura
 * @author dprieto
 *
 */
public interface AreaCoberturaTransactionalServiceRemote {

	/**
	 * Metodo de creacion de sedes negociacion
	 * @param isCapitaOEventoPrimerNivel
	 * @param sedeId identificador de la sede
	 * @return lista de municipios referentes
	 */
	 Long crearSedesNegociacionCoberturaPorDefecto(SedesNegociacionDto sedesDto, Boolean isCapitaOEventoPrimerNivel,AreaCoberturaTipoEnum tipoArea,NegociacionDto negociacion);

	/**
	 * Replica la configuracion de una sede negociacion a las demas
	 * sedes involucradas en la negociacion
	 * @param sedesNegociacion una instancia de {@link SedesNegociacionDto}
	 * @param modalidad
	 */
	 void replicarAreaCoberturaBySedeAndNegociacionId(SedesNegociacionDto sedesNegociacion, NegociacionModalidadEnum modalidad);


    /**
     * Actualiza seleccion un municipio al area de una sede negociacion
     * @param municipio
     * @param sedesNegociacionId
     * @param seleccionado
     */
    void actualizarMunicipioCoberturaSedesNegociacion(MunicipioDto municipio, Long sedesNegociacionId, Boolean seleccionado );

	/**
	 * Elimina la sede negociacion por id de negociacion y
	 * @param sedeNegociacionId identificador de la sede negociacion
	 */
	void eliminarSedeNegociacionByNegociacionIdAndSedePrestador(Long negociacionId, Long sedePrestadorId);

	/**
     * Elimina la sede negociacion por id
     * @param sedeNegociacionId identificador de la sede negociacion
     */
    public void eliminarSedeNegociacionByNegociacionId(Long negociacionId);

	/**
	 * Actualiza la seleccion de municipios de una sedes negociacion dada
	 * @param sedeNegociacionId
	 * @param seleccionado
	 */
	void actualizarSeleccionBySedeNegociacionId(Long sedeNegociacionId, Boolean seleccionado);

	/**
	 * Actualiza los municipios seleccionados de una sedes negociacion dada
	 * @param municipiosId lista de los municipio seleccionados
	 * @param sedesNegociacionId
	 * @param seleccionado
	 */
	void actualizarSeleccionByMunicipiosAndSedesNegociacion(List<Integer> municipiosId, Long sedesNegociacionId, boolean seleccionado);
}
