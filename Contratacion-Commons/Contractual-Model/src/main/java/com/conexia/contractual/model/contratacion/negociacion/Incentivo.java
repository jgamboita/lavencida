package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.TipoIncentivoEnum;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entidad tabla incentivo
 *
 * @author mcastro
 *
 */
@Entity
@Table(schema = "contratacion", name = "incentivo")

@NamedQueries({
    @NamedQuery(name = "Incentivo.findByNegociacionId",
            query = "SELECT NEW com.conexia.contratacion.commons.dto.negociacion.IncentivoModeloDto(ic.id,ic.tipoIncentivo, ic.descripcion, ic.meta) "
            + "FROM Incentivo ic WHERE ic.negociacion.id = :negociacionId")
    ,
    @NamedQuery(name = "Incentivo.deleteIncentivo",
            query = "DELETE FROM Incentivo ic WHERE ic.id = :id "),
    @NamedQuery(name = "Incentivo.updateDeletedRegistro", query = "update Incentivo i set i.deleted = :deleted where i.negociacion.id =:negociacionId ")
})

public class Incentivo implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negociacion_id")
    private Negociacion negociacion;

    @Column(name = "tipo_incentivo")
    @Enumerated(EnumType.STRING)
    private TipoIncentivoEnum tipoIncentivo;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "meta")
    private String meta;

    @Column(name = "deleted")
    private boolean deleted;

    public Incentivo() {
    }

    public Incentivo(Long id) {
        this.id = id;
    }

    //<editor-fold desc="Getters && Setters">
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Negociacion getNegociacion() {
        return negociacion;
    }

    public void setNegociacion(Negociacion negociacion) {
        this.negociacion = negociacion;
    }

    public TipoIncentivoEnum getTipoIncentivo() {
        return tipoIncentivo;
    }

    public void setTipoIncentivo(TipoIncentivoEnum tipoIncentivo) {
        this.tipoIncentivo = tipoIncentivo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getMeta() {
        return meta;
    }

    public void setMeta(String meta) {
        this.meta = meta;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
