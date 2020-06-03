package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.maestros.Transporte;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;


/**
 * Entidad Categoria Transporte.
 *
 * @author jpicon
 * @date 24/06/2014
 */

@Entity
@Table(name = "categoria_transporte", schema = "contratacion")
public class CategoriaTransporte implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_transporte_id")
    private GrupoTransporte grupoTransporte;

    @OneToMany(mappedBy = "categoriaTransporte",fetch = FetchType.LAZY)
    private List<Transporte> transportes;

    public CategoriaTransporte() {
    }

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

    public GrupoTransporte getGrupoTransporte() {
        return grupoTransporte;
    }

    public void setGrupoTransporte(GrupoTransporte grupoTransporte) {
        this.grupoTransporte = grupoTransporte;
    }

    public List<Transporte> getTransportes() {
        return transportes;
    }

    public void setTransportes(List<Transporte> transportes) {
        this.transportes = transportes;
    }
}