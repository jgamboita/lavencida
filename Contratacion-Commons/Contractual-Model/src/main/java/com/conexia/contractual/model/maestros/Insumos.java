package com.conexia.contractual.model.maestros;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.maestros.InsumosDto;
import com.conexia.contractual.model.contratacion.CategoriaInsumo;
import com.conexia.contractual.model.contratacion.GrupoInsumo;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "insumo", schema = "maestros")
@SqlResultSetMappings({
        @SqlResultSetMapping(name = "Insumos.listarInsumoMapping",
                classes = @ConstructorResult(targetClass = InsumosDto.class,
                        columns = {
                                @ColumnResult(name = "id", type = Integer.class),
                                @ColumnResult(name = "codigo_emssanar", type = String.class),
                                @ColumnResult(name = "descripcion", type = String.class),
                                @ColumnResult(name = "grupoInsumo", type = String.class),
                                @ColumnResult(name = "categoriaInsumo", type = String.class)
                        })
        )
})

@NamedQueries({
        @NamedQuery(name = "Insumos.getByCode",
                query = "SELECT new com.conexia.contratacion.commons.dto.maestros.InsumosDto(i.id, i.codigoEmssanar, i.estado.id) " +
                        "from Insumos i inner join i.estado e " +
                        "where i.codigoEmssanar =:codigoEmssanar and e.id = 1")
})
public class Insumos implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "codigo", length = 20)
    private String codigo;

    @Column(name = "codigo_emssanar", length = 20)
    private String codigoEmssanar;

    @Column(name = "descripcion", length = 200)
    private String descripcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_insumo_id", nullable = false)
    private EstadoTecnologia estado;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private CategoriaInsumo categoriaInsumo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id")
    private GrupoInsumo grupoInsumo;

    @Column(name = "grupo_riesgo", length = 50)
    private String grupoRiesgo;

    @Column(name = "tipo_ppm_id")
    private Integer tipoPPMId;

    public Insumos() {

    }

    //<editor-fold desc="Getters && Setters">
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public EstadoTecnologia getEstado() {
        return estado;
    }

    public void setEstado(EstadoTecnologia estado) {
        this.estado = estado;
    }

    public CategoriaInsumo getCategoriaInsumo() {
        return categoriaInsumo;
    }

    public void setCategoriaInsumo(CategoriaInsumo categoriaInsumo) {
        this.categoriaInsumo = categoriaInsumo;
    }

    public GrupoInsumo getGrupoInsumo() {
        return grupoInsumo;
    }

    public void setGrupoInsumo(GrupoInsumo grupoInsumo) {
        this.grupoInsumo = grupoInsumo;
    }

    public String getGrupoRiesgo() {
        return grupoRiesgo;
    }

    public void setGrupoRiesgo(String grupoRiesgo) {
        this.grupoRiesgo = grupoRiesgo;
    }

    public Integer getTipoPPMId() {
        return this.tipoPPMId;
    }

    public void setTipoPPMId(final Integer tipoPPMId) {
        this.tipoPPMId = tipoPPMId;
    }
    //</editor-fold>
}
