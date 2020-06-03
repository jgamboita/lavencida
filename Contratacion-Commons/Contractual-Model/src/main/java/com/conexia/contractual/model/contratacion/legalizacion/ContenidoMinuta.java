package com.conexia.contractual.model.contratacion.legalizacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Set;

/**
 * Entidad para la clase contenido minuta.
 * 
 * @author jlopez
 */
@Entity
@Table(name = "contenido_minuta", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name = "ContenidoMinuta.findAllByOrdinal", query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.ContenidoMinutaDto"
            + "(c.id, c.descripcion, c.nivel, c.unico, c.icono) FROM ContenidoMinuta c where nivel = :ordinal"),
    @NamedQuery(name = "ContenidoMinuta.findById", query = "SELECT c FROM ContenidoMinuta c WHERE c.id = :id")
})
public class ContenidoMinuta implements Identifiable<Long>, Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "descripcion")
    private String descripcion;
    
    @NotNull
    @Column(name = "nivel")
    private int nivel;
    
    @NotNull
    @Column(name = "unico")
    private boolean unico;
    
    @NotNull
    @Column(name = "tiene_hijos")
    private boolean tieneHijos;
    
    @Column(name = "icono")
    private String icono;
    
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "contenidoMinutaId", fetch = FetchType.LAZY)
    private Set<MinutaDetalle> minutaDetalleSet;

    public ContenidoMinuta() {
    }

    public ContenidoMinuta(Long id) {
        this.id = id;
    }

    public ContenidoMinuta(Long id, String descripcion, int nivel, boolean unico) {
        this.id = id;
        this.descripcion = descripcion;
        this.nivel = nivel;
        this.unico = unico;
    }
    
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getNivel() {
        return nivel;
    }

    public void setNivel(int nivel) {
        this.nivel = nivel;
    }

    /**
     * @return the unico
     */
    public boolean isUnico() {
        return unico;
    }

    /**
     * @return the tieneHijos
     */
    public boolean isTieneHijos() {
        return tieneHijos;
    }

    /**
     * @param tieneHijos the tieneHijos to set
     */
    public void setTieneHijos(boolean tieneHijos) {
        this.tieneHijos = tieneHijos;
    }
    
    public void setUnico(boolean unico) {
        this.unico = unico;
    }
    
    

    public String getIcono() {
        return icono;
    }

    public void setIcono(String icono) {
        this.icono = icono;
    }
    
    @XmlTransient
    public Set<MinutaDetalle> getMinutaDetalleSet() {
        return minutaDetalleSet;
    }

    public void setMinutaDetalleSet(Set<MinutaDetalle> minutaDetalleSet) {
        this.minutaDetalleSet = minutaDetalleSet;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ContenidoMinuta)) {
            return false;
        }
        ContenidoMinuta other = (ContenidoMinuta) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }


}
