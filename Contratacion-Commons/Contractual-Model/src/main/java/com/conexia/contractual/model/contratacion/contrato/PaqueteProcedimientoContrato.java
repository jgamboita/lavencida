package com.conexia.contractual.model.contratacion.contrato;

import com.conexia.contractual.model.maestros.Procedimientos;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author jlopez
 */
@Entity
@Table(schema = "contratacion", name = "paquete_procedimiento_contrato")
@NamedQueries({
    @NamedQuery(name = "PaqueteProcedimientoContrato.findAll", query = "SELECT p FROM PaqueteProcedimientoContrato p")})
@NamedNativeQueries({
    @NamedNativeQuery(name = "PaqueteProcedimientoContrato.insertPaqueteProcedimientoContrato",
            query = "INSERT INTO contratacion.paquete_procedimiento_contrato (paquete_contrato_id, procedimiento_id, cantidad, principal)\n" +
            		" SELECT pc.id, snpp.procedimiento_id, coalesce(snpp.cantidad, 0), snpp.principal \n" +
            		" from contratacion.sede_negociacion_paquete snp \n" +
            		" join contratacion.paquete_portafolio paqpor on paqpor.portafolio_id = snp.paquete_id\n" +
            		" join maestros.procedimiento p on p.codigo_emssanar = paqpor.codigo   \n" +
            		" JOIN contratacion.paquete_contrato pc on pc.paquete_id = p.id AND pc.sede_contrato_id = :sedeContratoId AND pc.estado = :estado\n" +
            		" join contratacion.sede_negociacion_paquete_procedimiento snpp on snpp.sede_negociacion_paquete_id = snp.id\n" +
            		" WHERE NOT EXISTS (SELECT NULL \n" +
            		" 					FROM contratacion.paquete_procedimiento_contrato ppc\n" +
            		" 					WHERE ppc.paquete_contrato_id = pc.id AND ppc.procedimiento_id = snpp.procedimiento_id) and snp.sede_negociacion_id = :sedeNegociacionId")
})
public class PaqueteProcedimientoContrato implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @JoinColumn(name = "procedimiento_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Procedimientos procedimiento;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "principal")
    private Boolean principal;

    @JoinColumn(name = "paquete_contrato_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PaqueteContrato paqueteContrato;

    public PaqueteProcedimientoContrato() {
    }

    public PaqueteProcedimientoContrato(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Procedimientos getProcedimiento() {
        return procedimiento;
    }

    public void setProcedimiento(Procedimientos procedimiento) {
        this.procedimiento = procedimiento;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Boolean getPrincipal() {
        return principal;
    }

    public void setPrincipal(Boolean principal) {
        this.principal = principal;
    }

    public PaqueteContrato getPaqueteContrato() {
        return paqueteContrato;
    }

    public void setPaqueteContrato(PaqueteContrato paqueteContrato) {
        this.paqueteContrato = paqueteContrato;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaqueteProcedimientoContrato that = (PaqueteProcedimientoContrato) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "com.conexia.contractual.model.contratacion.contrato.PaqueteProcedimientoContrato[ id=" + id + " ]";
    }

}
