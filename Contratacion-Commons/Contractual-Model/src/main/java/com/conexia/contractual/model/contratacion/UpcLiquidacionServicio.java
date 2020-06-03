package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Set;

/**
 * Entidad UPC Liquidación Servicio.
 *
 * @author mcastro
 * @date 09/10/2015
 */
@Entity
@Table(name = "upc_liquidacion_servicio", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name="UpcLiquidacionServicio.getServicioByLiqZonaId",
			query=	"   SELECT new com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionServicioDto("
                    + " uls.id, "
                    + " round(uls.porcentaje,3), "
                    + " round(uls.valor) ,"
                    + " ss.id, ss.codigo, ss.nombre, ms.id, ms.codigo, ms.nombre, "
                    + " round(uls.porcentajeAsignado,3)) "
					+ " FROM UpcLiquidacionServicio uls "
                    + " JOIN uls.liquidacionZona lz "
                    + " JOIN uls.servicioSalud ss "
                    + " JOIN ss.macroServicio ms "
					+ " WHERE lz.id = :liquidacionZonaId "),
    @NamedQuery(name="UpcLiquidacionServicio.getById",
			query=	"   SELECT new com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionServicioDto("
                    + " uls.id, uls.porcentaje, uls.valor, uls.porcentajeAsignado) "
					+ " FROM UpcLiquidacionServicio uls "
					+ " WHERE uls.id = :id "),
    @NamedQuery(name="UpcLiquidacionServicio.updateValorAndPercentById",
            query = "UPDATE UpcLiquidacionServicio "
                    + "SET valor = :valor , "
                    + "porcentaje = :porcentaje "
                    + "WHERE id = :liqServicioId "),
    @NamedQuery(name="UpcLiquidacionServicio.updatePorcentajeNegociadoServicioById",
            query = "UPDATE UpcLiquidacionServicio "
                    + "SET porcentajeAsignado = :porcentajeAsignado "
                    + "WHERE id = :id ")
})
public class UpcLiquidacionServicio implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "porcentaje", nullable = false)
    private BigDecimal porcentaje;
    
    @Column(name = "porcentaje_asignado", nullable = false)
    private BigDecimal porcentajeAsignado;
    
    @Column(name = "valor", nullable = false)
    private BigDecimal valor;
    
    @ManyToOne
    @JoinColumn(name = "liquidacion_zona_id")
    private LiquidacionZona liquidacionZona;
    
    @ManyToOne
    @JoinColumn(name = "servicio_salud_id")
    private ServicioSalud servicioSalud;
    
    @OneToMany(mappedBy = "upcLiquidacionServicio")
    private Set<UpcLiquidacionProcedimiento> liquidacionProcedimientos;
    
    public UpcLiquidacionServicio() {
    }

    public UpcLiquidacionServicio(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public LiquidacionZona getLiquidacionZona() {
        return liquidacionZona;
    }

    public void setLiquidacionZona(LiquidacionZona liquidacionZona) {
        this.liquidacionZona = liquidacionZona;
    }

    public ServicioSalud getServicioSalud() {
        return servicioSalud;
    }

    public void setServicioSalud(ServicioSalud servicioSalud) {
        this.servicioSalud = servicioSalud;
    }

    public Set<UpcLiquidacionProcedimiento> getLiquidacionProcedimientos() {
        return liquidacionProcedimientos;
    }

    public void setLiquidacionProcedimientos(Set<UpcLiquidacionProcedimiento> liquidacionProcedimientos) {
        this.liquidacionProcedimientos = liquidacionProcedimientos;
    }

    public BigDecimal getPorcentajeAsignado() {
        return porcentajeAsignado;
    }

    public void setPorcentajeAsignado(BigDecimal porcentajeAsignado) {
        this.porcentajeAsignado = porcentajeAsignado;
    }
    
}
