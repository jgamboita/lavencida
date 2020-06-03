package com.conexia.contractual.model.maestros;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Superclass del entity generico.
 *
 * @author jalvarado
 */
@MappedSuperclass
public abstract class Descriptivo implements Serializable, Identifiable<Integer> {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -7691148421646817541L;

    /**
     * Tamaño del campo descripcion.
     */
    private static final int LONGITUD_DESCRIPCION = 200;

    /**
     * id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    /**
     * Descripcion.
     */
    @Column(name = "descripcion", nullable = false, length = Descriptivo.LONGITUD_DESCRIPCION)
    private String descripcion;

    public Descriptivo() {
    }

    public Descriptivo(Integer id) {
        this.id = id;
    }
    
    /**
     * Devuelve el valor de id.
     *
     * @return El valor de id.
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * Asigna un nuevo valor a id.
     *
     * @param id El valor a asignar a id.
     */
    public void setId(final Integer id) {
        this.id = id;
    }

    /**
     * Devuelve el valor de descripcion.
     *
     * @return El valor de descripcion.
     */
    public String getDescripcion() {
        return this.descripcion;
    }

    /**
     * Asigna un nuevo valor a descripcion.
     *
     * @param descripcion El valor a asignar a descripcion.
     */
    public void setDescripcion(final String descripcion) {
        this.descripcion = descripcion;
    }

}
