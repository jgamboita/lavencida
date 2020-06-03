package com.conexia.contractual.model.contratacion.contrato;

import com.conexia.contractual.model.maestros.Insumos;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author jlopez
 */
@Entity
@Table(schema = "contratacion", name = "paquete_insumo_contrato")
@NamedQueries({
    @NamedQuery(name = "PaqueteInsumoContrato.findAll", query = "SELECT p FROM PaqueteInsumoContrato p")})
@NamedNativeQueries({
    @NamedNativeQuery(name = "PaqueteInsumoContrato.insertPaqueteInsumoContrato",
            query = "INSERT INTO contratacion.paquete_insumo_contrato (paquete_contrato_id, insumo_id, cantidad)\n" +
            		" SELECT pc.id, snpi.insumo_id, coalesce(snpi.cantidad, 0)  \n" +
            		" from contratacion.sede_negociacion_paquete snp \n" +
            		" join contratacion.paquete_portafolio paqpor on paqpor.portafolio_id = snp.paquete_id\n" +
            		" join maestros.procedimiento p on p.codigo_emssanar = paqpor.codigo   \n" +
            		" JOIN contratacion.paquete_contrato pc on pc.paquete_id = p.id AND pc.sede_contrato_id = :sedeContratoId AND pc.estado = :estado\n" +
            		" join contratacion.sede_negociacion_paquete_insumo snpi on snpi.sede_negociacion_paquete_id = snp.id\n" +
            		" WHERE NOT EXISTS (SELECT NULL \n" +
            		" 					FROM contratacion.paquete_insumo_contrato pic\n" +
            		" 					WHERE pic.paquete_contrato_id = pc.id AND pic.insumo_id = snpi.insumo_id) and snp.sede_negociacion_id = :sedeNegociacionId")
})
public class PaqueteInsumoContrato implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")

    private Integer id;

    @ManyToOne
    @JoinColumn(name = "insumo_id")
    private Insumos insumo;

    @Column(name = "cantidad")
    private Integer cantidad;

    @JoinColumn(name = "paquete_contrato_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PaqueteContrato paqueteContrato;

    public PaqueteInsumoContrato() {
    }

    public PaqueteInsumoContrato(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Insumos getInsumo() {
        return insumo;
    }

    public void setInsumo(Insumos insumo) {
        this.insumo = insumo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public PaqueteContrato getPaqueteContrato() {
        return paqueteContrato;
    }

    public void setPaqueteContrato(PaqueteContrato paqueteContrato) {
        this.paqueteContrato = paqueteContrato;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof PaqueteInsumoContrato)) {
            return false;
        }
        PaqueteInsumoContrato other = (PaqueteInsumoContrato) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.conexia.contractual.model.contratacion.contrato.PaqueteInsumoContrato[ id=" + id + " ]";
    }

}
