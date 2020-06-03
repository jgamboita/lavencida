package com.conexia.contractual.model.contratacion.negociacion;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity implementation class for Entity: SedeNegociacionPaqueteCausaRuptura
 *
 */
@Entity
@Table(schema = "contratacion", name = "sede_negociacion_paquete_causa_ruptura")
@NamedQueries({
	@NamedQuery(name = "SedeNegociacionPaqueteCausaRuptura.updateCausaRupturaSedeNegociacion",
			query = "UPDATE SedeNegociacionPaqueteCausaRuptura SET causaRuptura = :causaRuptura WHERE id = :causaPaqueteId "
			),
	@NamedQuery(name = "SedeNegociacionPaqueteCausaRuptura.deleteCausaRupturaSedeNegociacion",
			query = "DELETE FROM SedeNegociacionPaqueteCausaRuptura WHERE id = :causaPaqueteId"),
	@NamedQuery(name = "SedeNegociacionPaqueteCausaRuptura.deleteAllCausaRupturaNegociacionId",
			query = "DELETE FROM SedeNegociacionPaqueteCausaRuptura snpcr "
				+ "WHERE snpcr.sedeNegociacionPaquete.id IN( "
				+ "SELECT snp.id FROM SedeNegociacionPaquete snp "
				+ "JOIN snp.sedeNegociacion sn "
				+ "WHERE sn.negociacion.id = :negociacionId)"),
	@NamedQuery(name ="SedeNegociacionPaqueteCausaRuptura.findCausaRupturaSedeNegociacion",
			query = "SELECT new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteCausaRupturaDto( "
				+ "snpcr.id, snpcr.sedeNegociacionPaquete.id, snpcr.causaRuptura ) FROM SedeNegociacionPaqueteCausaRuptura snpcr "
				+ "JOIN snpcr.sedeNegociacionPaquete snp "
				+ "JOIN snp.sedeNegociacion sn "
				+ "WHERE sn.negociacion.id = :negociacionId and snp.paquete.id = :paqueteId ")
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "SedeNegociacionPaqueteCausaRuptura.insertCausaRupturaSedeNegociacion",
			query = "INSERT INTO contratacion.sede_negociacion_paquete_causa_ruptura(sede_negociacion_paquete_id, causa_ruptura) "
				+ "SELECT snp.id, ppcr.causa_ruptura from contratacion.sede_negociacion_paquete  snp "
				+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
				+ "JOIN contratacion.paquete_portafolio pp ON snp.paquete_id = pp.portafolio_id "
				+ "JOIN contratacion.paquete_portafolio_causa_ruptura ppcr ON ppcr.paquete_portafolio_id = pp.id "
				+ "WHERE sn.id = :sedeNegociacionId "
	),
	@NamedNativeQuery(name = "SedeNegociacionPaqueteCausaRuptura.insertCausaRupturaNegociacion",
	query = "INSERT INTO contratacion.sede_negociacion_paquete_causa_ruptura (sede_negociacion_paquete_id, causa_ruptura) "
		+ "SELECT snp.id, :causaRuptura FROM contratacion.sede_negociacion_paquete snp "
		+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
		+ "JOIN contratacion.paquete_portafolio pp ON snp.paquete_id = pp.portafolio_id "
		+ "WHERE sn.negociacion_id = :negociacionId AND snp.paquete_id = :paqueteId "
	)
})
public class SedeNegociacionPaqueteCausaRuptura implements Serializable {

	
	private static final long serialVersionUID = 1L;

	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sede_negociacion_paquete_id")
	private SedeNegociacionPaquete sedeNegociacionPaquete; 
	
	@Column(name = "causa_ruptura", length = 500)
	private String causaRuptura;
	
	
	public SedeNegociacionPaqueteCausaRuptura() {
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


	public String getCausaRuptura() {
		return causaRuptura;
	}


	public void setCausaRuptura(String causaRuptura) {
		this.causaRuptura = causaRuptura;
	}
   
	
}
