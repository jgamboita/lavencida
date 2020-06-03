package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.model.maestros.Descriptivo;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Entity que manipula los datos de la tabla clase prestador.
 * @author jalvarado
 */
@Entity
@Table(name = "clase_prestador", schema = "contratacion")
public class ClasePrestador extends Descriptivo {
    
    /**
     * Codigo del prestador.
     */
    @Column(name = "codigo")
    private String codigo;

    public ClasePrestador() {
    }

    public ClasePrestador(Integer id) {
        super(id);
    }
    
    /**
     * @return the codigo
     */
    public String getCodigo() {
        return codigo;
    }

    /**
     * @param codigo the codigo to set
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }
    
    
    
}
