package com.conexia.contractual.definitions.view.prestador;

import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;

/**
 * Interface que define los metodos relevantes al prestador.
 * @author jalvarado
 */
public interface PrestadorViewServiceRemote {

    /**
     * Funcion que permite validar si el codigo del prestador es correcto o ya esta registrado.
     * @param prestadorDto
     * @return true si el codigo es correcto.
     */
    boolean validarCodigoPrestador(final PrestadorDto prestadorDto);

    /**
     * Valida si el codigo de la sede ya existe.
     * @param sedePrestadorDto codigo del prestador y codigo de la sede.
     * @return true si existe false si no.
     */
    boolean validarCodigoEps(final SedePrestadorDto sedePrestadorDto);

    String sedePrincipal(Long negociacionId);
}
