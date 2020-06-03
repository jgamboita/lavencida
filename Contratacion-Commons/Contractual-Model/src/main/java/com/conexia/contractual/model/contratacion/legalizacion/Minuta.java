package com.conexia.contractual.model.contratacion.legalizacion;

import com.conexia.contratacion.commons.constants.enums.EstadoEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TramiteEnum;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author jlopez
 */
@Entity
@Table(name = "minuta", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name = "Minuta.findAllDto", query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDto(m.id, m.descripcion, m.estado, m.modalidad, m.nombre, m.tramite) FROM Minuta m order by m.descripcion"),
    @NamedQuery(name = "Minuta.findByEstado", query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDto(m.id, m.descripcion, m.estado, m.modalidad, m.nombre, m.tramite) FROM Minuta m WHERE m.estado = :estado order by m.descripcion"),
    @NamedQuery(name = "Minuta.findByEstadoAndModalidad", query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDto(m.id, m.descripcion, m.estado, m.modalidad, m.nombre, m.tramite) FROM Minuta m WHERE m.estado = :estado and m.modalidad = :modalidad order by m.descripcion"),
    @NamedQuery(name = "Minuta.findByEstadoAndModalidadAndTramite", query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDto(m.id, m.descripcion, m.estado, m.modalidad, m.nombre, m.tramite) FROM Minuta m WHERE m.estado = :estado and m.modalidad = :modalidad and m.tramite = :tramite order by m.descripcion"),
    @NamedQuery(name = "Minuta.findById", query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDto(m.id, m.descripcion, m.estado, m.modalidad, m.nombre, m.tramite) FROM Minuta m where m.id = :minutaId"),
    @NamedQuery(name = "Minuta.updateById", query = "update Minuta set descripcion = :descripcion, estado = :estado, modalidad = :modalidad, nombre = :nombre, tramite = :tramite where id = :minutaId")
})
public class Minuta implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "descripcion", nullable = false)
    private String descripcion;

    @Column(name = "estado_id", nullable = false)
    private EstadoEnum estado;
    
    @Column(name = "modalidad_id", nullable = false)
    private NegociacionModalidadEnum modalidad;

    @Column(name = "nombre", nullable = false, unique = true)
    private String nombre;

    @Column(name = "tramite_id", nullable = false)
    private TramiteEnum tramite;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.REFRESH)
    @JoinTable(
            name = "minuta_variable", schema = "contratacion",
            joinColumns = {
                @JoinColumn(name = "minuta_id", nullable = false)
            },
            inverseJoinColumns = {
                @JoinColumn(name = "variable_id", nullable = false)
            }
    )
    private Set<Variable> variables = new HashSet<>(0);

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "minutaId", fetch = FetchType.LAZY)
    private Set<MinutaDetalle> minutaDetalleSet;

    public Minuta() {
    }
    
    public Minuta(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(final Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public void setDescripcion(final String descripcion) {
        this.descripcion = descripcion;
    }

    public EstadoEnum getEstado() {
        return estado;
    }

    public void setEstado(EstadoEnum estado) {
        this.estado = estado;
    }

    public NegociacionModalidadEnum getModalidad() {
        return modalidad;
    }

    public void setModalidad(NegociacionModalidadEnum modalidad) {
        this.modalidad = modalidad;
    }

    @XmlTransient
    public Set<MinutaDetalle> getMinutaDetalleSet() {
        return minutaDetalleSet;
    }

    public void setMinutaDetalleSet(Set<MinutaDetalle> minutaDetalleSet) {
        this.minutaDetalleSet = minutaDetalleSet;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TramiteEnum getTramite() {
        return tramite;
    }

    public void setTramite(TramiteEnum tramite) {
        this.tramite = tramite;
    }

    public Set<Variable> getVariables() {
        return variables;
    }

    public void setVariables(Set<Variable> variables) {
        this.variables = variables;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (this.getId() != null ? this.getId().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Minuta)) {
            return false;
        }
        Minuta other = (Minuta) object;
        if ((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.getId().equals(other.getId()))) {
            return false;
        }
        return true;
    }
}
