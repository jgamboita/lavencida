package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity implementation class for Entity: SedeNegociacionPaqueteExclusion
 *
 */
@Entity
@Table(schema = "contratacion", name = "sede_negociacion_paquete_exclusion")
@NamedQueries({
	@NamedQuery(name = "SedeNegociacionPaqueteExclusion.updateExclusionSedeNegociacion",
			query = "UPDATE SedeNegociacionPaqueteExclusion SET exclusion = :exclusion Where id = :exclusionPaqueteId "
	),
	@NamedQuery(name = "SedeNegociacionPaqueteExclusion.deleteExclusionSedeNegociacion",
			query = "DELETE FROM SedeNegociacionPaqueteExclusion WHERE id = :exclusionPaqueteId"),
	@NamedQuery(name = "SedeNegociacionPaqueteExclusion.deleteAllExclusionNegociacionId",
			query = "DELETE FROM SedeNegociacionPaqueteExclusion snpe "
				+ "WHERE snpe.sedeNegociacionPaquete.id IN( "
				+ "SELECT snp.id FROM SedeNegociacionPaquete snp "
				+ "JOIN snp.sedeNegociacion sn "
				+ "WHERE sn.negociacion.id = :negociacionId)"),
	@NamedQuery(name ="SedeNegociacionPaqueteExclusion.findExclusionSedeNegociacion",
			query = "SELECT new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteExclusionDto( "
				+ "snpe.id, snpe.sedeNegociacionPaquete.id, snpe.exclusion ) FROM SedeNegociacionPaqueteExclusion snpe "
				+ "JOIN snpe.sedeNegociacionPaquete snp "
				+ "JOIN snp.sedeNegociacion sn "
				+ "WHERE sn.negociacion.id = :negociacionId and snp.paquete.id = :paqueteId ")
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "SedeNegociacionPaqueteExclusion.insertExclusionSedeNegociacion",
			query = "INSERT INTO contratacion.sede_negociacion_paquete_exclusion(sede_negociacion_paquete_id, exclusion) "
				+ "SELECT snp.id, ppe.exclusion from contratacion.sede_negociacion_paquete  snp "
				+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
				+ "JOIN contratacion.paquete_portafolio pp ON snp.paquete_id = pp.portafolio_id "
				+ "JOIN contratacion.paquete_portafolio_exclusion ppe ON ppe.paquete_portafolio_id = pp.id "
				+ "WHERE sn.id = :sedeNegociacionId "
	),
	@NamedNativeQuery(name = "SedeNegociacionPaqueteExclusion.insertExclusionNegociacion",
	query = "INSERT INTO contratacion.sede_negociacion_paquete_exclusion (sede_negociacion_paquete_id, exclusion) "
		+ "SELECT snp.id, :exclusion FROM contratacion.sede_negociacion_paquete snp "
		+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
		+ "JOIN contratacion.paquete_portafolio pp ON snp.paquete_id = pp.portafolio_id "
		+ "WHERE sn.negociacion_id = :negociacionId AND snp.paquete_id = :paqueteId "
	)
})
public class SedeNegociacionPaqueteExclusion implements Identifiable<Long>,Serializable {

	
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sede_negociacion_paquete_id")
	private SedeNegociacionPaquete sedeNegociacionPaquete;
	    
	@Column(name = "exclusion", length = 500)
	private String exclusion;

	public SedeNegociacionPaqueteExclusion() {
		super();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public SedeNegociacionPaquete getSedeNegociacionPaquete() {
		return sedeNegociacionPaquete;
	}

	public void setSedeNegociacionPaquete(SedeNegociacionPaquete sedeNegociacionPaquete) {
		this.sedeNegociacionPaquete = sedeNegociacionPaquete;
	}

	public String getExclusion() {
		return exclusion;
	}

	public void setExclusion(String exclusion) {
		this.exclusion = exclusion;
	}

	
   
	
	
}
