package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;

/**
 * Entity que manipula los datos de la tabla contratacion.tipo_necesidad.
 *
 * @author jalvarado
 */
@Entity
@Table(name = "grupo_necesidad", schema = "contratacion")
public class GrupoNecesidad implements Identifiable<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "nombre", length = 200)
    private String nombre;


    /**
     * Devuelve el valor de id.
     *
     * @return El valor de id.
     */
    public Long getId() {
        return this.id;
    }


    /**
     * Asigna un nuevo valor a id.
     *
     * @param id El valor a asignar a id.
     */
    public void setId(Long id) {
        this.id = id;
    }


    /**
     * Devuelve el valor de nombre.
     *
     * @return El valor de nombre.
     */
    public String getNombre() {
        return this.nombre;
    }


    /**
     * Asigna un nuevo valor a nombre.
     *
     * @param nombre El valor a asignar a nombre.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }


}
