package com.conexia.contractual.model.contratacion.contrato;

import com.conexia.contractual.model.contratacion.SedePrestador;

import javax.persistence.*;
import java.io.Serializable;


@Entity
@Table(schema = "contratacion", name = "sede_contrato_urgencias")
@NamedNativeQueries({
	@NamedNativeQuery(name = "SedeContratoUrgencias.deleteSedesContratoUrgenciasByContratoUrgenciasId",
		    query = "DELETE FROM contratacion.sede_contrato_urgencias WHERE contrato_urgencias_id = :contratoUrgenciasId AND sede_prestador_id not in(:sedesContratoUrgencias) "),
	@NamedNativeQuery(name = "SedeContratoUrgencias.deleteSedesContratoUrgenciasById",
    		query = "DELETE FROM contratacion.sede_contrato_urgencias WHERE contrato_urgencias_id = :contratoUrgenciasId "),
	@NamedNativeQuery(name = "SedeContratoUrgencias.insertSedeContratoUrgenciasByContratoUrgenciasId",
    		query = "WITH sede_old AS (UPDATE contratacion.sede_contrato_urgencias SET sede_prestador_id=:sedePrestadorId WHERE contrato_urgencias_id = :contratoUrgenciasId AND sede_prestador_id=:sedePrestadorId "
    		+ "		RETURNING sede_prestador_id,contrato_urgencias_id), "
    		+ "	  sede_new AS (SELECT :sedePrestadorId, :contratoUrgenciasId WHERE NOT EXISTS (SELECT sede_prestador_id,contrato_urgencias_id FROM sede_old)) "
    		+ "	INSERT INTO contratacion.sede_contrato_urgencias(sede_prestador_id, contrato_urgencias_id) SELECT * FROM sede_new "),
})
@SqlResultSetMappings({

})
public class SedeContratoUrgencias implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @JoinColumn(name = "contrato_urgencias_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ContratoUrgencias contratoUrgencias;

    @JoinColumn(name = "sede_prestador_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private com.conexia.contractual.model.contratacion.SedePrestador SedePrestador;

//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sedeContrato", fetch = FetchType.LAZY)
//    private List<InsumoContrato> insumosContrato;
//
//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sedeContrato", fetch = FetchType.LAZY)
//    private List<MedicamentoContrato> medicamentosContrato;
//
//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sedeContrato", fetch = FetchType.LAZY)
//    private List<PaqueteContrato> paquetesContrato;
//
//    @OneToMany(cascade = CascadeType.ALL, mappedBy = "sedeContrato", fetch = FetchType.LAZY)
//    private List<ProcedimientoContrato> procedimientosContrato;
//
//    @OneToMany(mappedBy="sedeContrato")
//    private List<AreaCoberturaContrato> areaCoberturaContratos;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }





    public ContratoUrgencias getContratoUrgencias() {
		return contratoUrgencias;
	}

	public void setContratoUrgencias(ContratoUrgencias contratoUrgencias) {
		this.contratoUrgencias = contratoUrgencias;
	}

	/**
     * @return the SedePrestador
     */
    public SedePrestador getSedePrestador() {
        return SedePrestador;
    }

    /**
     * @param SedePrestador the SedePrestador to set
     */
    public void setSedePrestador(SedePrestador SedePrestador) {
        this.SedePrestador = SedePrestador;
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
        if (!(object instanceof SedeContratoUrgencias)) {
            return false;
        }
        SedeContratoUrgencias other = (SedeContratoUrgencias) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.conexia.contractual.model.contratacion.contrato.SedeContratoUrgencias[ id=" + id + " ]";
    }

}
