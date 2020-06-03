package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.model.maestros.Descriptivo;

import javax.persistence.*;

/**
 * Clasificacion prestador.
 * @author jalvarado
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "ClasificacionPrestador.findAll", query = "SELECT NEW com.conexia.contratacion.commons.dto.maestros.ClasificacionPrestadorDto("
            + "id, descripcion) "
            + "from ClasificacionPrestador")
})    
@Table(name = "clasificacion_prestador", schema = "contratacion")
public class ClasificacionPrestador extends Descriptivo {
    
    /**
     * Codigo.
     */
    @Column(name = "codigo", nullable = true)
    private String codigo;
    
    /**
     * Clase prestador.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clase_prestador_id", nullable = true)
    private ClasePrestador clasePrestador;

    public ClasificacionPrestador() {
    }

    public ClasificacionPrestador(Integer id) {
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

    /**
     * @return the clasePrestador
     */
    public ClasePrestador getClasePrestador() {
        return clasePrestador;
    }

    /**
     * @param clasePrestador the clasePrestador to set
     */
    public void setClasePrestador(ClasePrestador clasePrestador) {
        this.clasePrestador = clasePrestador;
    }
    
    
    
}
