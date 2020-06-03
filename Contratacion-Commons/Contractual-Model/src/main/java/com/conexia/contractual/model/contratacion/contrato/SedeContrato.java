package com.conexia.contractual.model.contratacion.contrato;

import com.conexia.contractual.model.contratacion.SedePrestador;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * Entidad para la clase Sede COntrato.
 *
 * @author jlopez
 */
@Entity
@Table(schema = "contratacion", name = "sede_contrato")
@NamedQueries({
    @NamedQuery(name = "SedeContrato.findAll", query = "SELECT s FROM SedeContrato s")
    ,
    @NamedQuery(name = "SedeContrato.findSedeDtoByContratoId",
            query = "select new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto("
            + "sc.id, sp.codigoSede, sp.nombreSede, sp.sedePrincipal, "
            + "m.id, m.descripcion, d.descripcion, sp.direccion) "
            + "from SedeContrato sc "
            + "join sc.SedePrestador sp "
            + "join sp.municipio m "
            + "join m.departamento d "
            + "where sc.contrato.id = :contratoId"
    )
    ,
    @NamedQuery(name = "SedeContrato.findSedeContratoByPrestadorAndContrato",
            query = "SELECT sc "
            + " FROM SedeContrato sc "
            + " JOIN sc.SedePrestador sp "
            + " JOIN sc.contrato c "
            + " WHERE c = :contratoId "
            + " AND   sp = :sedePrestadorId "
            + " ORDER BY sc.id ASC ")
})
public class SedeContrato implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @JoinColumn(name = "contrato_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Contrato contrato;

    @JoinColumn(name = "sede_prestador_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private com.conexia.contractual.model.contratacion.SedePrestador SedePrestador;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sedeContrato", fetch = FetchType.LAZY)
    private List<InsumoContrato> insumosContrato;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sedeContrato", fetch = FetchType.LAZY)
    private List<MedicamentoContrato> medicamentosContrato;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sedeContrato", fetch = FetchType.LAZY)
    private List<PaqueteContrato> paquetesContrato;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sedeContrato", fetch = FetchType.LAZY)
    private List<ProcedimientoContrato> procedimientosContrato;

    @OneToMany(mappedBy = "sedeContrato")
    private List<AreaCoberturaContrato> areaCoberturaContratos;

    @Column(name = "deleted")
    private boolean deleted;

    public SedeContrato() {
    }

    public SedeContrato(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public List<InsumoContrato> getInsumosContrato() {
        return insumosContrato;
    }
    
    public void setInsumosContrato(List<InsumoContrato> insumosContrato) {
        this.insumosContrato = insumosContrato;
    }
    
    public List<MedicamentoContrato> getMedicamentosContrato() {
        return medicamentosContrato;
    }
    
    public void setMedicamentosContrato(List<MedicamentoContrato> medicamentosContrato) {
        this.medicamentosContrato = medicamentosContrato;
    }
    
    public List<PaqueteContrato> getPaquetesContrato() {
        return paquetesContrato;
    }
    
    public void setPaquetesContrato(List<PaqueteContrato> paquetesContrato) {
        this.paquetesContrato = paquetesContrato;
    }
    
    public List<ProcedimientoContrato> getProcedimientosContrato() {
        return procedimientosContrato;
    }
    
    public void setProcedimientosContrato(List<ProcedimientoContrato> procedimientosContrato) {
        this.procedimientosContrato = procedimientosContrato;
    }
    
    public Contrato getContrato() {
        return contrato;
    }
    
    public void setContrato(Contrato contrato) {
        this.contrato = contrato;
    }
    
    public SedePrestador getSedePrestador() {
        return SedePrestador;
    }
    
    public void setSedePrestador(SedePrestador SedePrestador) {
        this.SedePrestador = SedePrestador;
    }
    
    public List<AreaCoberturaContrato> getAreaCoberturaContratos() {
        return areaCoberturaContratos;
    }

    public void setAreaCoberturaContratos(List<AreaCoberturaContrato> areaCoberturaContratos) {
        this.areaCoberturaContratos = areaCoberturaContratos;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    //</editor-fold>

    //<editor-fold desc="HashCode && Equals && To String">
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof SedeContrato)) {
            return false;
        }
        SedeContrato other = (SedeContrato) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.conexia.contractual.model.contratacion.contrato.SedeContrato[ id=" + id + " ]";
    }
    //</editor-fold>
}
