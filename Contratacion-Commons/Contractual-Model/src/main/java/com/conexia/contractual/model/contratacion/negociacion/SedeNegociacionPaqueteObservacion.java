package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity implementation class for Entity: SedeNegociacionPaqueteObservacion
 *
 */
@Entity
@Table(schema = "contratacion", name = "sede_negociacion_paquete_observacion")
@NamedQueries({
	@NamedQuery(name = "SedeNegociacionPaqueteObservacion.updateObservacionSedeNegociacion",
			query = "UPDATE SedeNegociacionPaqueteObservacion SET observacion = :observacion Where id = :observacionPaqueteId "
	),
	@NamedQuery(name = "SedeNegociacionPaqueteObservacion.deleteObservacionSedeNegociacion",
			query = "DELETE FROM SedeNegociacionPaqueteObservacion WHERE id = :observacionPaqueteId"),
	@NamedQuery(name = "SedeNegociacionPaqueteObservacion.deleteAllObservacionNegociacionId",
			query = "DELETE FROM SedeNegociacionPaqueteObservacion snpo "
				+ "WHERE snpo.sedeNegociacionPaquete.id IN( "
				+ "SELECT snp.id FROM SedeNegociacionPaquete snp "
				+ "JOIN snp.sedeNegociacion sn "
				+ "WHERE sn.negociacion.id = :negociacionId)"),
	@NamedQuery(name = "SedeNegociacionPaqueteObservacion.findObservacionSedeNegociacion",
			query = "SELECT new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteObservacionDto( "
				+ "snpo.id, snpo.sedeNegociacionPaquete.id, snpo.observacion ) FROM SedeNegociacionPaqueteObservacion snpo "
				+ "JOIN snpo.sedeNegociacionPaquete snp "
				+ "JOIN snp.sedeNegociacion sn "
				+ "WHERE sn.negociacion.id = :negociacionId and snp.paquete.id = :paqueteId ")
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "SedeNegociacionPaqueteObservacion.insertObservacionesSedeNegociacion",
			query = "INSERT INTO contratacion.sede_negociacion_paquete_observacion(sede_negociacion_paquete_id, observacion) "
			+ "SELECT snp.id, ppo.observacion from contratacion.sede_negociacion_paquete  snp "
			+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
			+ "JOIN contratacion.paquete_portafolio pp ON snp.paquete_id = pp.portafolio_id "
			+ "JOIN contratacion.paquete_portafolio_observacion ppo ON ppo.paquete_portafolio_id = pp.id "
			+ "WHERE sn.id = :sedeNegociacionId"
	),
	@NamedNativeQuery(name = "SedeNegociacionPaqueteObservacion.insertObservacionNegociacion",
			query = "INSERT INTO contratacion.sede_negociacion_paquete_observacion (sede_negociacion_paquete_id, observacion) "
				+ "SELECT snp.id, :observacion FROM contratacion.sede_negociacion_paquete snp "
				+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
				+ "JOIN contratacion.paquete_portafolio pp ON snp.paquete_id = pp.portafolio_id "
				+ "WHERE sn.negociacion_id = :negociacionId AND snp.paquete_id = :paqueteId "
	)
})
public class SedeNegociacionPaqueteObservacion implements Identifiable<Long>,Serializable{

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sede_negociacion_paquete_id")
	private SedeNegociacionPaquete sedeNegociacionPaquete;
	    
	@Column(name = "observacion", length = 500)
	private String observacion;
	
	public SedeNegociacionPaqueteObservacion() {
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

	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}
	
	
   
}
