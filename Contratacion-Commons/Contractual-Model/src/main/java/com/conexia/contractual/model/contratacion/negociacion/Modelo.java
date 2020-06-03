package com.conexia.contractual.model.contratacion.negociacion;


import com.conexia.contratacion.commons.constants.enums.ModeloEnum;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The persistent class for the modelo database table.
 *
 */
@Entity
@Table(schema = "contratacion", name = "modelo")
@NamedQueries({
        @NamedQuery(name = "Modelo.findByNegociacionId", query = "SELECT NEW com.conexia.contratacion.commons.dto.negociacion.IncentivoModeloDto(ic.id,ic.modelo, ic.descripcion, ic.meta) "
                + "FROM Modelo ic WHERE ic.negociacion.id = :negociacionId"),
        @NamedQuery(name = "Modelo.deleteModelo", query = "DELETE FROM Modelo ic WHERE ic.id = :id "),
        @NamedQuery(name = "Modelo.updateDeletedRegistro",query = "update Modelo m set m.deleted = :deleted where m.negociacion.id =:negociacionId ")
})
public class Modelo implements Serializable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String descripcion;

    private String meta;

    @Column(name = "modelo")
    @Enumerated(EnumType.STRING)
    private ModeloEnum modelo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negociacion_id")
    private Negociacion negociacion;

    @Column(name = "deleted")
    private boolean deleted;

    public Modelo() {
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

    public String getMeta() {
        return this.meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public ModeloEnum getModelo() {
        return modelo;
    }

    public void setModelo(ModeloEnum modelo) {
        this.modelo = modelo;
    }

    public Negociacion getNegociacion() {
        return negociacion;
    }

    public void setNegociacion(Negociacion negociacion) {
        this.negociacion = negociacion;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

}
