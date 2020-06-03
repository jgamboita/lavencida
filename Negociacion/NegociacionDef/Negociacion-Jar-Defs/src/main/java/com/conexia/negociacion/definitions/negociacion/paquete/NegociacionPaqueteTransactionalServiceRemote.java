package com.conexia.negociacion.definitions.negociacion.paquete;

import java.util.List;

import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.PaqueteNegociacionDto;
import com.conexia.contratacion.commons.dto.util.PaquetePortafolioServicioSaludDto;


/**
 * Interface remota transactional para el boundary de negociacionPaquete
 * @author jtorres
 *
 */
public interface NegociacionPaqueteTransactionalServiceRemote {

	/**
	 * Elimina varias SedesNegociacionPaquete dada la id de la negociación y el paquete en cuestión.
	 * @param negociacionId
	 * @param paqueteId
	 * @return un entero que indica el resultado de la operación
	 */
	public Integer eliminarByNegociacionAndPaquete(final Long negociacionId, final Long paqueteId, Integer userId);

	/**
	 * Función que guarda una lista de paquetes en una SedeNegociación dada la negociación y la lista
	 * de paquetes.
	 * @param paquetes la lista de medicamentos a persistir.
	 * @param negociacionId la negociación a la que pertenece esos medicamentos.
	 */
    public void guardarPaquetesNegociados(List<PaqueteNegociacionDto> paquetes, Long negociacionId, Integer userId);

    /**
     * Almacena el valor negociado del Paquete
     * @param paquete
     * @param negociacionId
     * @param userId
     */
    public void guardarPaqueteNegociado(PaqueteNegociacionDto paquete, Long negociacionId, Integer userId);

	public void agregarPaquetesNegociacion(List<PaquetePortafolioServicioSaludDto> sedesPrestador, NegociacionDto negociacion, Integer userId);
}
