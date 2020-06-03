package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Entidad grupo insumo.
 *
 * @author mcastro
 * @date 23/05/2014
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "GrupoInsumo.findEmpresarial", query = "select new com.conexia.contratacion.commons.dto.maestros.GrupoInsumoDto("
            + "id, descripcion) "
            + "from GrupoInsumo "
            + "where descripcion like 'EMPRESARIAL%'"),
    @NamedQuery(name = "GrupoInsumo.findByPrestadorId", query = "select new com.conexia.contratacion.commons.dto.maestros.GrupoInsumoDto("
            + "gi.id, gi.descripcion) "
            + "from GrupoInsumo gi "
            + "join gi.prestadores p "
            + "where p.id = :prestadorId"),
})
@Table(name = "grupo_insumo", schema = "contratacion")
public class GrupoInsumo implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "descripcion", length = 300)
    private String descripcion;

    @ManyToMany(mappedBy = "grupos", fetch = FetchType.LAZY)
    private Set<Prestador> prestadores = new HashSet<>(0);

    public GrupoInsumo() {
    }

    public GrupoInsumo(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Set<Prestador> getPrestadores() {
        return prestadores;
    }

    public void setPrestadores(Set<Prestador> prestadores) {
        this.prestadores = prestadores;
    }
    
}
