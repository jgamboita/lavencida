package com.conexia.contractual.definitions.transactional.prestador;

import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;

/**
 * Interface que define los metodos del Boundary transaccional del prestador.
 * @author jalvarado
 */
public interface PrestadorTransactionalRemote {

    /**
     * Funcion que permite actualizar el prestador.
     * @param prestadorDto - Dto del prestador a persistir.
     * @return total de registros afectados.
     */
    Integer actualizarPrestador(final PrestadorDto prestadorDto);

    /**
     * Funcion que permite actualizar el codigo de la sede.
     * @param sedePrestadorDto codigo e id de la sede.
     * @return resultado del execute
     */
    Integer actualizarCodigoSedePrestador(final SedePrestadorDto sedePrestadorDto);

}
