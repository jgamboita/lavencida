package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.model.contratacion.legalizacion.Variable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * The persistent class for the modalidad_negociacion database table.
 *
 */
@Entity
@Table(schema = "contratacion", name = "modalidad_negociacion")
public class ModalidadNegociacion implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String codigo;

    private String descripcion;

    @ManyToMany
    @JoinTable(name = "variable_modalidad", schema = "contratacion", joinColumns = {
        @JoinColumn(name = "variable_id")}, inverseJoinColumns = {
        @JoinColumn(name = "modalidad_negociacion_id")})
    private Set<Variable> variables = new HashSet<>(0);

    public ModalidadNegociacion() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCodigo() {
        return this.codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Set<Variable> getVariables() {
        return variables;
    }

    public void setVariables(Set<Variable> variables) {
        this.variables = variables;
    }

}
