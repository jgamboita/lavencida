package com.conexia.contractual.model.contratacion.contrato;

import com.conexia.contractual.model.maestros.Municipio;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author Andrés Mise Olivera
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "AreaCoberturaContrato.findAreaInfluenciaDtoBySedeContratoId",
            query = "select new com.conexia.contratacion.commons.dto.contractual.parametrizacion.AreaInfluenciaDto("
            + "m.descripcion, d.descripcion, r.descripcion)"
            + "from AreaCoberturaContrato acc "
            + "join acc.municipio m "
            + "join m.departamento d "
            + "join d.regional r "
            + "where acc.sedeContrato.id = :sedeContratoId")
    ,
    @NamedQuery(name = "AreaCoberturaContrato.findByContratoId",
            query = "select new com.conexia.contratacion.commons.dto.contractual.parametrizacion.AreaInfluenciaDto("
            + "m.descripcion, d.descripcion, r.descripcion)"
            + "from AreaCoberturaContrato acc "
            + "join acc.municipio m "
            + "join m.departamento d "
            + "join d.regional r "
            + "join acc.sedeContrato sc  "
            + "where sc.contrato.id = :contratoId")
})
@NamedNativeQueries({
    @NamedNativeQuery(name = "AreaCoberturaContrato.insertAreaCoberturaContrato",
            query = "INSERT INTO contratacion.area_cobertura_contrato "
            + " (municipio_id, sede_contrato_id)"
            + " SELECT m.id , :sedeContratoId "
            + "	FROM maestros.municipio m "
            + " WHERE id in (:municipioIds) "
            + " AND NOT EXISTS (SELECT NULL FROM contratacion.area_cobertura_contrato "
            + "					WHERE municipio_id = m.id"
            + "					AND sede_contrato_id = :sedeContratoId)")
})
@Table(schema = "contratacion", name = "area_cobertura_contrato")
public class AreaCoberturaContrato implements Serializable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "municipio_id", referencedColumnName = "id")
    private Municipio municipio;

    @ManyToOne
    @JoinColumn(name = "sede_contrato_id", referencedColumnName = "id")
    private SedeContrato sedeContrato;

    @Column(name = "deleted")
    private boolean deleted;

    public AreaCoberturaContrato() {
    }

    public AreaCoberturaContrato(Municipio municipio, SedeContrato sedeContrato) {
        this.municipio = municipio;
        this.sedeContrato = sedeContrato;
    }

    //<editor-fold desc="Getters && Setters">
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    public SedeContrato getSedeContrato() {
        return sedeContrato;
    }

    public void setSedeContrato(SedeContrato sedeContrato) {
        this.sedeContrato = sedeContrato;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

}
