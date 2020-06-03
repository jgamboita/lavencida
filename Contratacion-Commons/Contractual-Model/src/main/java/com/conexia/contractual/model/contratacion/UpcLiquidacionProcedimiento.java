package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.maestros.Procedimientos;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entidad UPC Liquidación Procedimiento.
 *
 * @author mcastro
 * @date 09/10/2015
 */
@Entity
@Table(name = "upc_liquidacion_procedimiento", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name="UpcLiquidacionProcedimiento.listProcedimientosByServicioId",
			query=	"   SELECT new com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionProcedimientoDto("
                    + " ulp.id, "
                    + " round(ulp.porcentaje,3), "
                    + " round(ulp.valor,0), "
                    + "	uls.id, pro.id, pro.cups, pro.descripcion, pro.codigoCliente,"
                    + " round(ulp.porcentajeAsignado,3), "
                    + " round(uls.porcentaje,3))"
					+ " FROM UpcLiquidacionProcedimiento ulp "
                    + " JOIN ulp.upcLiquidacionServicio uls "
                    + " JOIN ulp.procedimientos pro "
					+ " WHERE uls.id = :liquidacionServicioId "
                    + " ORDER BY pro.cups "),
    @NamedQuery(name="UpcLiquidacionProcedimiento.getById",
			query=	"   SELECT new com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionProcedimientoDto("
                    + " ulp.id, ulp.porcentaje, ulp.valor, ulp.porcentajeAsignado) "
					+ " FROM UpcLiquidacionProcedimiento ulp "
					+ " WHERE ulp.id = :id "),
    @NamedQuery(name="UpcLiquidacionProcedimiento.countProcedimientosByServicioId",
			query=	"   SELECT count(ulp.id)"
					+ " FROM UpcLiquidacionProcedimiento ulp "
					+ " WHERE ulp.upcLiquidacionServicio.id = :liquidacionServicioId "),
    @NamedQuery(name="UpcLiquidacionProcedimiento.updateGroupByServicioId",
            query = "UPDATE UpcLiquidacionProcedimiento "
                    + " SET valor = :valor , "
                    + " porcentaje = :porcentaje "
                    + " WHERE upcLiquidacionServicio.id = :liquidacionServicioId "),
    @NamedQuery(name="UpcLiquidacionProcedimiento.updateGroupAsignadoByServicioId",
            query = "UPDATE UpcLiquidacionProcedimiento "
                    + " SET porcentajeAsignado = :porcentajeAsignado "
                    + " WHERE upcLiquidacionServicio.id = :liquidacionServicioId "),
    @NamedQuery(name="UpcLiquidacionProcedimiento.updateById",
            query = "UPDATE UpcLiquidacionProcedimiento "
                    + " SET valor = :valor , "
                    + " porcentaje = :porcentaje "
                    + " WHERE id = :procedimientoId "),
    @NamedQuery(name="UpcLiquidacionProcedimiento.updatePorcentajeAsignadoById",
            query = "UPDATE UpcLiquidacionProcedimiento "
                    + " SET porcentajeAsignado = :porcentajeAsignado "
                    + " WHERE id = :procedimientoId "),
    @NamedQuery(name="UpcLiquidacionProcedimiento.sumPercentProcedimientosByServicioId",
			query=	"   SELECT SUM(ulp.porcentaje)"
					+ " FROM UpcLiquidacionProcedimiento ulp "
					+ " WHERE ulp.upcLiquidacionServicio.id = :liquidacionServicioId ")
})
public class UpcLiquidacionProcedimiento implements Identifiable<Long>, Serializable {

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
    @JoinColumn(name = "upc_liquidacion_servicio_id")
    private UpcLiquidacionServicio upcLiquidacionServicio;
    
    @ManyToOne
    @JoinColumn(name = "procedimiento_servicio_id")
    private Procedimientos procedimientos;
    
    public UpcLiquidacionProcedimiento() {
    }

    public UpcLiquidacionProcedimiento(Long id) {
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

    public UpcLiquidacionServicio getUpcLiquidacionServicio() {
        return upcLiquidacionServicio;
    }

    public void setUpcLiquidacionServicio(UpcLiquidacionServicio upcLiquidacionServicio) {
        this.upcLiquidacionServicio = upcLiquidacionServicio;
    }

    public Procedimientos getProcedimientos() {
        return procedimientos;
    }

    public void setProcedimientos(Procedimientos procedimientos) {
        this.procedimientos = procedimientos;
    }

    public BigDecimal getPorcentajeAsignado() {
        return porcentajeAsignado;
    }

    public void setPorcentajeAsignado(BigDecimal porcentajeAsignado) {
        this.porcentajeAsignado = porcentajeAsignado;
    }
    
}
