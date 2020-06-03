package com.conexia.contractual.model.contratacion.legalizacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.TipoVariableEnum;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author jlopez
 */
@Entity
@Table(name = "variable", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name = "Variable.findAll", query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.VariableDto(v.id, v.codigo, v.descripcion, v.tipoVariable) "
            + " FROM Variable v "
            + " JOIN v.variableModalidad vm "
            + " WHERE vm.modalidad.id = :modalidad and vm.estadoVariable = 1 "
            + " ORDER BY v.descripcion"),
    @NamedQuery(name = "Variable.findById", query = "SELECT v FROM Variable v WHERE v.id = :id"),
    @NamedQuery(name = "Variable.findByCodigo", query = "SELECT v FROM Variable v WHERE v.codigo = :codigo"),
    @NamedQuery(name = "Variable.findByDescripcion", query = "SELECT v FROM Variable v WHERE v.descripcion = :descripcion"),
    @NamedQuery(name = "Variable.findByModalidad", query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.VariableDto(v.id, v.codigo, v.descripcion, v.tipoVariable) "
            + " FROM Variable v "
            + " JOIN v.variableModalidad vm "
            + " WHERE  v.tipoVariable = :tipoVariable and vm.modalidad.id =:modalidad and vm.estadoVariable = :estadoVariable "
            + " ORDER BY v.descripcion")
})
public class Variable implements Identifiable<Long>, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @NotNull
    @Column(name = "id")
    private Long id;
    
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "codigo")
    private String codigo;
    
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "descripcion")
    private String descripcion;
    
    @ManyToMany(mappedBy = "variables", fetch = FetchType.LAZY)
    private Set<Minuta> minutas = new HashSet<>(0);
    
    @Column(name = "estado", nullable = false)
    private Integer estadoVarible;
    
    @OneToMany(mappedBy = "variable")
    private List<VariableModalidad> variableModalidad;
    
    @Column(name = "tipo_variable_id", nullable = false)
    private TipoVariableEnum tipoVariable;

    public Variable() {
    }

    public Variable(Long id) {
        this.id = id;
    }

    public Variable(Long id, String codigo, String descripcion) {
        this.id = id;
        this.codigo = codigo;
        this.descripcion = descripcion;
    }
    
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Set<Minuta> getMinutas() {
        return minutas;
    }

    public void setMinutas(Set<Minuta> minutas) {
        this.minutas = minutas;
    }
     
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    public TipoVariableEnum getTipoVariable() {
        return tipoVariable;
    }

    public void setTipoVariable(TipoVariableEnum tipoVariable) {
        this.tipoVariable = tipoVariable;
    }

    public Integer getEstadoVarible() {
        return estadoVarible;
    }

    public void setEstadoVarible(Integer estadoVarible) {
        this.estadoVarible = estadoVarible;
    }

    public List<VariableModalidad> getVariableModalidad() {
        return variableModalidad;
    }

    public void setVariableModalidad(List<VariableModalidad> variableModalidad) {
        this.variableModalidad = variableModalidad;
    }
    
    
    
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Variable)) {
            return false;
        }
        Variable other = (Variable) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    

}
