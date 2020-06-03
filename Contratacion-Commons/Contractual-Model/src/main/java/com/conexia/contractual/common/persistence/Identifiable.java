package com.conexia.contractual.common.persistence;

import java.io.Serializable;

/**
 * Representa un objeto que puede ser identificado en forma individual. Toda entidad persistente con
 * Hibernate debe implementarlo.
 *
 * @param <Id> Tipo de la propiedad que identifica a la instancia.
 * @author jalvarado
 */
public interface Identifiable<Id extends Serializable> {

    /**
     * Obtiene el identificador de la instancia.
     *
     * @return Identificador de la instancia.
     */
    Id getId();
}