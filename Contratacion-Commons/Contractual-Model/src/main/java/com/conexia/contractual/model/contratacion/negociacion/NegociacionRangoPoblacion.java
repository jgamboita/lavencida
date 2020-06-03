package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.maestros.RangoPoblacion;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(schema = "contratacion", name = "negociacion_rango_poblacion")
@NamedQueries({
	@NamedQuery(name = "NegociacionRangoPoblacion.deleteNegociacionRangoPoblacion",
            query = "DELETE FROM NegociacionRangoPoblacion nrp WHERE nrp.negociacionId = :negociacionId "),
	@NamedQuery(name = "NegociacionRangoPoblacion.findRangoPByNegociacionId",
    query = "SELECT new com.conexia.contratacion.commons.dto.maestros.RangoPoblacionDto(r.id, r.descripcion, r.edadDesde, r.edadHasta) "
    		+ " FROM NegociacionRangoPoblacion nrp "
    		+ " JOIN nrp.rangoPoblacion r " 
    		+ " WHERE nrp.negociacionId = :negociacionId "),
	@NamedQuery(name = "NegociacionRangoPoblacion.updateDeletedRegistro",query = "update NegociacionRangoPoblacion nrp set nrp.deleted = :deleted where nrp.negociacionId =:negociacionId ")
})
public class NegociacionRangoPoblacion implements Identifiable<Integer>, Serializable {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rango_poblacion_id")
    private RangoPoblacion rangoPoblacion;

    @Column(name = "negociacion_id")
    private Long negociacionId;

    @Column(name = "deleted")
    private boolean deleted;

	//<editor-fold desc="Getters && Setters">
	@Override
	public Integer getId() {
		return id;
	}

	public Long getNegociacionId() {
		return negociacionId;
	}

	public void setNegociacionId(Long negociacionId) {
		this.negociacionId = negociacionId;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public RangoPoblacion getRangoPoblacion() {
		return rangoPoblacion;
	}

	public void setRangoPoblacion(RangoPoblacion rangoPoblacion) {
		this.rangoPoblacion = rangoPoblacion;
	}

   public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

	//</editor-fold>
}
