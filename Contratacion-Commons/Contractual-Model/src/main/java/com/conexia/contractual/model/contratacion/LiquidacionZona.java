package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entidad Liquidación Zona.
 *
 * @author mcastro
 * @date 09/10/2015
 */
@Entity
@Table(name = "liquidacion_zona", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name = "LiquidacionZona.getLiquidacionZonaByUpc",
            query = "SELECT new com.conexia.contratacion.commons.dto.negociacion.LiquidacionZonaDto(lz.id, lz.valorPromedio, lz.totalAfiliados) "
                    + " FROM LiquidacionZona lz WHERE lz.upc.id =:upcId"),
    @NamedQuery(name = "LiquidacionZona.updateValorById",
            query = "UPDATE LiquidacionZona "
                    + "SET valorPromedio = :valorPromedio "
                    + "WHERE id = :liquidacionZonaId ")
})
public class LiquidacionZona implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "valor_promedio", nullable = false)
    private BigDecimal valorPromedio;
    
    @Column(name = "total_afiliados", nullable = false)
    private Integer totalAfiliados;
  
    @ManyToOne
    @JoinColumn(name = "upc_id")
    private Upc upc;
 

    public LiquidacionZona() {
    }

    public LiquidacionZona(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getValorPromedio() {
        return valorPromedio;
    }

    public void setValorPromedio(BigDecimal valorPromedio) {
        this.valorPromedio = valorPromedio;
    }

    public Integer getTotalAfiliados() {
        return totalAfiliados;
    }

    public void setTotalAfiliados(Integer totalAfiliados) {
        this.totalAfiliados = totalAfiliados;
    }

    public Upc getUpc() {
        return upc;
    }

    public void setUpc(Upc upc) {
        this.upc = upc;
    }

}
