package com.conexia.negociacion.definitions.common;

import java.util.List;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 * Interface para los servicios transaccionales del boundary CommonBoundary
 * @author jjoya
 *
 */
public interface CommonTransactionalServiceRemote {

    void actualizarEstadoPrestadoresByIds(EstadoPrestadorEnum estado, List<Long> prestadoresId);

    void calcularTotalRiasByNegociacion(Long negociacionId);

    void calcularTotalRiasByNegociacionPGP(Long negociacionId) throws ConexiaBusinessException;

    void calcularTotalByNegociacionPGP(Long negociacionId) throws ConexiaBusinessException;
}
