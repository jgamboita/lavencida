package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity implementation class for Entity: SedeNegociacionPaqueteRequerimientoTecnico
 *
 */
@Entity
@Table(schema = "contratacion", name = "sede_negociacion_paquete_requerimiento_tecnico")
@NamedQueries({
	@NamedQuery(name = "SedeNegociacionPaqueteRequerimientoTecnico.updateRequerimientoSedeNegociacion",
			query = "UPDATE SedeNegociacionPaqueteRequerimientoTecnico SET requerimientoTecnico = :requerimientoTecnico WHERE id = :requerimientoPaqueteId "
			),
	@NamedQuery(name = "SedeNegociacionPaqueteRequerimientoTecnico.borrarRequerimientoPaqueteSede",
			query = "DELETE FROM SedeNegociacionPaqueteRequerimientoTecnico WHERE id = :requerimientoPaqueteId"),
	@NamedQuery(name = "SedeNegociacionPaqueteRequerimientoTecnico.deleteAllRequerimientoTecnicoNegociacionId",
			query = "DELETE FROM SedeNegociacionPaqueteRequerimientoTecnico snprt "
				+ "WHERE snprt.sedeNegociacionPaquete.id IN( "
				+ "SELECT snp.id FROM SedeNegociacionPaquete snp "
				+ "JOIN snp.sedeNegociacion sn "
				+ "WHERE sn.negociacion.id = :negociacionId)"),
	@NamedQuery(name ="SedeNegociacionPaqueteRequerimientoTecnico.findRequerimientoTecnicoSedeNegociacion",
	query = "SELECT new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteRequerimientoDto( "
		+ "snprt.id, snprt.sedeNegociacionPaquete.id, snprt.requerimientoTecnico ) FROM SedeNegociacionPaqueteRequerimientoTecnico snprt "
		+ "JOIN snprt.sedeNegociacionPaquete snp "
		+ "JOIN snp.sedeNegociacion sn "
		+ "WHERE sn.negociacion.id = :negociacionId and snp.paquete.id = :paqueteId ")
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "SedeNegociacionPaqueteRequerimientoTecnico.insertRequerimientoSedeNegociacion",
			query = "INSERT INTO contratacion.sede_negociacion_paquete_requerimiento_tecnico(sede_negociacion_paquete_id, requerimiento_tecnico) "
				+ "SELECT snp.id, ppr.requerimiento_tecnico from contratacion.sede_negociacion_paquete  snp "
				+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
				+ "JOIN contratacion.paquete_portafolio pp ON snp.paquete_id = pp.portafolio_id "
				+ "JOIN contratacion.paquete_portafolio_requerimiento_tecnico ppr ON ppr.paquete_portafolio_id = pp.id "
				+ "WHERE sn.id = :sedeNegociacionId "
	),
	@NamedNativeQuery(name = "SedeNegociacionPaqueteRequerimientoTecnico.insertRequerimientoNegociacion",
	query = "INSERT INTO contratacion.sede_negociacion_paquete_requerimiento_tecnico (sede_negociacion_paquete_id, requerimiento_tecnico) "
		+ "SELECT snp.id, :requerimientoTecnico FROM contratacion.sede_negociacion_paquete snp "
		+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
		+ "JOIN contratacion.paquete_portafolio pp ON snp.paquete_id = pp.portafolio_id "
		+ "WHERE sn.negociacion_id = :negociacionId AND snp.paquete_id = :paqueteId "
	)
	
})
public class SedeNegociacionPaqueteRequerimientoTecnico implements Identifiable<Long>,Serializable {

	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sede_negociacion_paquete_id")
	private SedeNegociacionPaquete sedeNegociacionPaquete;
	    
	@Column(name = "requerimiento_tecnico", length = 500)
	private String requerimientoTecnico;
	
	public SedeNegociacionPaqueteRequerimientoTecnico() {
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

	public String getRequerimientoTecnico() {
		return requerimientoTecnico;
	}

	public void setRequerimientoTecnico(String requerimientoTecnico) {
		this.requerimientoTecnico = requerimientoTecnico;
	}

	
   
	
}
