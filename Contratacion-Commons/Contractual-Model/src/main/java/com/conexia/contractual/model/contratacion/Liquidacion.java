package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.maestros.Municipio;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entidad UPC Distribución.
 *
 * @author mcastro
 * @date 09/10/2015
 */
@Entity
@Table(name = "liquidacion", schema = "contratacion")
public class Liquidacion implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "no_afiliados", nullable = false)
    private Long cantidadAfiliados;
    
    @Column(name = "valor_grupo_mes", nullable = false)
    private BigDecimal valorGrupoMes;
    
    @ManyToOne
    @JoinColumn(name = "liquidacion_zona_id")
    private LiquidacionZona liquidacionZona;
    
    @ManyToOne
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;
    
    @ManyToOne
    @JoinColumn(name = "upc_id")
    private Upc upc;
    
    public Liquidacion() {
    }

    public Liquidacion(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCantidadAfiliados() {
        return cantidadAfiliados;
    }

    public void setCantidadAfiliados(Long cantidadAfiliados) {
        this.cantidadAfiliados = cantidadAfiliados;
    }

    public BigDecimal getValorGrupoMes() {
        return valorGrupoMes;
    }

    public void setValorGrupoMes(BigDecimal valorGrupoMes) {
        this.valorGrupoMes = valorGrupoMes;
    }

    public LiquidacionZona getLiquidacionZona() {
        return liquidacionZona;
    }

    public void setLiquidacionZona(LiquidacionZona liquidacionZona) {
        this.liquidacionZona = liquidacionZona;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    public Upc getUpc() {
        return upc;
    }

    public void setUpc(Upc upc) {
        this.upc = upc;
    }
    
}
