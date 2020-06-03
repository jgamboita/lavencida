package com.conexia.contractual.model.contratacion.contrato;

import com.conexia.contractual.model.maestros.Medicamento;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author jlopez
 */
@Entity
@Table(schema = "contratacion", name = "paquete_medicamento_contrato")
@NamedQueries({
    @NamedQuery(name = "PaqueteMedicamentoContrato.findAll", query = "SELECT p FROM PaqueteMedicamentoContrato p")})
@NamedNativeQueries({
    @NamedNativeQuery(name = "PaqueteMedicamentoContrato.insertPaqueteMedicamentoContrato",
            query = "INSERT INTO contratacion.paquete_medicamento_contrato (paquete_contrato_id, medicamento_id, cantidad)\n" +
            		" SELECT pc.id, snpm.medicamento_id, coalesce(snpm.cantidad, 0) \n" +
            		" from contratacion.sede_negociacion_paquete snp \n" +
            		" join contratacion.paquete_portafolio paqpor on paqpor.portafolio_id = snp.paquete_id\n" +
            		" join maestros.procedimiento p on p.codigo_emssanar = paqpor.codigo \n" +
            		" JOIN contratacion.paquete_contrato pc on pc.paquete_id = p.id AND pc.sede_contrato_id = :sedeContratoId AND pc.estado = :estado\n" +
            		" join contratacion.sede_negociacion_paquete_medicamento snpm on snpm.sede_negociacion_paquete_id = snp.id\n" +
            		" WHERE NOT EXISTS (SELECT NULL \n" +
            		"					FROM contratacion.paquete_medicamento_contrato pmc\n" +
            		"					WHERE pmc.paquete_contrato_id = pc.id AND pmc.medicamento_id = snpm.medicamento_id) and snp.sede_negociacion_id = :sedeNegociacionId")
})
public class PaqueteMedicamentoContrato implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id")
    private Medicamento medicamento;

    @Column(name = "cantidad")
    private Integer cantidad;

    @JoinColumn(name = "paquete_contrato_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PaqueteContrato paqueteContrato;

    public PaqueteMedicamentoContrato() {
    }

    public PaqueteMedicamentoContrato(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Medicamento getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(Medicamento medicamento) {
        this.medicamento = medicamento;
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
        if (!(object instanceof PaqueteMedicamentoContrato)) {
            return false;
        }
        PaqueteMedicamentoContrato other = (PaqueteMedicamentoContrato) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.conexia.contractual.model.contratacion.contrato.PaqueteMedicamentoContrato[ id=" + id + " ]";
    }

}
