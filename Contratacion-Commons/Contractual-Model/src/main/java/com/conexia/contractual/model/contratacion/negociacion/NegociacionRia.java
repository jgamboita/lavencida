package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.maestros.RiaDto;
import com.conexia.contractual.model.maestros.Ria;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(schema = "contratacion", name = "negociacion_ria")
@NamedQueries({
        @NamedQuery(name = "NegociacionRia.deleteNegociacionRia",
                query = "DELETE FROM NegociacionRia nr WHERE nr.negociacion.id = :negociacionId "),
        @NamedQuery(name = "NegociacionRia.findRiasByNegociacionId",
                query = "SELECT new com.conexia.contratacion.commons.dto.negociacion.NegociacionRiaDto(nr.id, r.id, r.codigo, r.descripcion, nr.negociado) "
                        + " FROM NegociacionRia nr "
                        + " JOIN nr.ria r ON nr.negociacion.id = :negociacionId "),
        @NamedQuery(name = "NegociacionRia.borrarNegociacionRiaSinIds",
                query = "DELETE FROM NegociacionRia nr "
                        + " WHERE nr.id not in (:ids) "
                        + " AND nr.negociacion.id = :negociacionId"),
        @NamedQuery(name = "NegociacionRia.consultarvalorTotal",
                query = "SELECT nr.valorTotal FROM NegociacionRia nr "
                        + "WHERE nr.negociacion.id = :negociacionId "),
        @NamedQuery(name = "NegociacionRia.borrarNegociacionRiaPorNegociacionId",
                query = "DELETE FROM NegociacionRia nr "
                        + " WHERE nr.negociacion.id = :negociacionId"),
        @NamedQuery(name = "NegociacionRia.deshabilitarPorNegociacionIdSinIds",
                query = "UPDATE FROM NegociacionRia nr "
                        + " SET nr.negociado = false "
                        + " WHERE nr.negociacion.id = :negociacionId "
                        + " AND nr.id not in (:ids) "),
        @NamedQuery(name = "NegociacionRia.deshabilitarPorNegociacionId",
                query = "UPDATE FROM NegociacionRia nr "
                        + " SET nr.negociado = false "
                        + " WHERE nr.negociacion.id = :negociacionId "),
        @NamedQuery(name = "NegociacionRia.findNegociacionRiaByNegociacionId",
                query = "SELECT new com.conexia.contratacion.commons.dto.negociacion.NegociacionRiaDto(nr.id, r.id, r.codigo, r.descripcion) "
                        + " FROM NegociacionRia nr "
                        + " JOIN nr.ria r "
                        + " WHERE nr.negociacion.id = :negociacionId "),
        @NamedQuery(name = "NegociacionRia.updateDeletedRegistro", query = "update NegociacionRia nr set nr.deleted = :deleted where nr.negociacion.id =:negociacionId "),
        @NamedQuery(name = "NegociacionRia.buscarPorRutaYNegociacion",
                query = "select new com.conexia.contratacion.commons.dto.negociacion.NegociacionRiaDto(nr.id, r.id, r.codigo, r.descripcion, nr.negociado) " +
                        "from NegociacionRia nr inner join nr.ria r where nr.negociado = true and nr.negociacion.id = :negociacionId and r.id in (:rutas)")
})
@NamedNativeQueries({
        @NamedNativeQuery(name = "NegociacionRia.consultarRutaNegociacion",
                query = "SELECT COALESCE((SELECT nr.id FROM contratacion.negociacion_ria nr "
                        + "JOIN maestros.ria r on nr.ria_id = r.id "
                        + "WHERE nr.negociacion_id = :negociacionId AND r.id = :rutaId  AND nr.negociado = true ),0) ")
})
@SqlResultSetMappings({
        @SqlResultSetMapping(
                name = "NegociacionRia.TotalPorRiaMapping",
                classes = @ConstructorResult(
                        targetClass = RiaDto.class,
                        columns = {
                                @ColumnResult(name = "id", type = Integer.class),
                                @ColumnResult(name = "descripcion", type = String.class),
                                @ColumnResult(name = "valor", type = BigDecimal.class),
                                @ColumnResult(name = "valor_total", type = BigDecimal.class)
                        }
                ))
})
public class NegociacionRia implements Identifiable<Integer>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ria_id")
    private Ria ria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negociacion_id")
    private Negociacion negociacion;

    @OneToMany(mappedBy = "negociacionRia", fetch = FetchType.LAZY)
    private List<NegociacionRiaRangoPoblacion> listaNegociacionRiaRangoPoblacion;

    @Column(name = "valor")
    private BigDecimal valor;

    @Column(name = "valor_total")
    private BigDecimal valorTotal;

    @Column(name = "negociado")
    private boolean negociado;

    @Column(name = "deleted")
    private boolean deleted;

    //<editor-fold desc="Getters && Setters">
    @Override
    public Integer getId() {
        return id;
    }

    public Negociacion getNegociacion() {
        return negociacion;
    }

    public void setNegociacion(Negociacion negociacion) {
        this.negociacion = negociacion;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Ria getRia() {
        return ria;
    }

    public void setRia(Ria ria) {
        this.ria = ria;
    }

    public List<NegociacionRiaRangoPoblacion> getListaNegociacionRiaRangoPoblacion() {
        return listaNegociacionRiaRangoPoblacion;
    }

    public void setListaNegociacionRiaRangoPoblacion(List<NegociacionRiaRangoPoblacion> listaNegociacionRiaRangoPoblacion) {
        this.listaNegociacionRiaRangoPoblacion = listaNegociacionRiaRangoPoblacion;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public boolean isNegociado() {
        return negociado;
    }

    public void setNegociado(boolean negociado) {
        this.negociado = negociado;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    //</editor-fold>
}
