package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entidad Tema Cápita.
 *
 * @author mcastro
 * @date 09/10/2015
 */
@Entity
@Table(name = "tema_capita", schema = "contratacion")
public class TemaCapita implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "nombre", length = 50, nullable = false)
    private String nombre;
    
    @Column(name = "activo", nullable = false)
    private Boolean activo;
    
    @Column(name = "capita", nullable = false)
    private Boolean capita;
  
    public TemaCapita() {
    }

    public TemaCapita(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public Boolean getCapita() {
        return capita;
    }

    public void setCapita(Boolean capita) {
        this.capita = capita;
    }

}
