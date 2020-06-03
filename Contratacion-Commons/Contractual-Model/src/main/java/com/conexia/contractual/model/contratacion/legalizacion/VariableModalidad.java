/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conexia.contractual.model.contratacion.legalizacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.contratacion.negociacion.ModalidadNegociacion;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 *
 * @author dmora
 */

@Entity
@Table(name = "variable_modalidad", schema ="contratacion")
@NamedQueries({
   @NamedQuery(name ="VariableModalidad.actualizarEstado", query = "UPDATE VariableModalidad vm "
        +"set vm.estadoVariable = :estadoVariable "
        +"WHERE vm.variable.id = :variable and vm.modalidad.id = :modalidad")
})
public class VariableModalidad implements Identifiable<Long>, Serializable{
  
    private static final long serialVersionUID = 1L;
    
    @Id
    @NotNull
    @Column(name = "id")
    private Integer id;
    
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variable_id")
    private Variable variable;
 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modalidad_negociacion_id")
    private ModalidadNegociacion modalidad;
    
    @NotNull
    @Column(name = "estado")
    private Integer estadoVariable;

     public Variable getVariable() {
        return variable;
    }

    public void setVariable(Variable variable) {
        this.variable = variable;
    }

    public ModalidadNegociacion getModalidad() {
        return modalidad;
    }

    public void setModalidad(ModalidadNegociacion modalidad) {
        this.modalidad = modalidad;
    }

    public Integer getEstadoVariable() {
        return estadoVariable;
    }

    public void setEstadoVariable(Integer estado) {
        this.estadoVariable = estado;
    }
    
    
    
    @Override
    public Long getId() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
