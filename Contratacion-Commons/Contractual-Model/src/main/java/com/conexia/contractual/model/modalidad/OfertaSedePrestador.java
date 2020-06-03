package com.conexia.contractual.model.modalidad;

import com.conexia.contractual.model.contratacion.SedePrestador;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the mod_oferta_sede_prestador database table.
 * 
 */
@Entity
@Table(name = "mod_oferta_sede_prestador", schema="contratacion")
@NamedQueries({
	@NamedQuery(
		name="OfertaSedePrestador.validarFinalizarOferta",  
		query="SELECT ( COUNT(o.sedePrestador.id) > 0 ) FROM OfertaSedePrestador o JOIN o.portafolio p LEFT JOIN p.servicioPortafolioSedes s LEFT JOIN p.categoriasMedicamentosPortafolioSede c WHERE (s.habilitado IS TRUE OR c.habilitado IS TRUE) AND o.ofertaPrestador.id = :ofertaId")
})
@NamedNativeQueries({
	@NamedNativeQuery(name="OfertaSedePrestador.eliminarOfertaSedePrestador",
			query="DELETE FROM contratacion.mod_oferta_sede_prestador "
					+ " WHERE id IN(SELECT mosp.id "
					+ "				FROM contratacion.mod_oferta_sede_prestador mosp"
					+ "				INNER JOIN contratacion.mod_portafolio_sede mps on mps.id = mosp.portafolio_id"
					+ "				WHERE mps.prestador_id=:prestadorId)"),
	@NamedNativeQuery(name="OfertaSedePrestador.insertarOfertaSedePrestador",
		query="INSERT INTO contratacion.mod_oferta_sede_prestador (oferta_id, sede_prestador_id, portafolio_id)"
				+ "	SELECT distinct mo.id, s.id, s.portafolio_id "
				+ "	FROM contratacion.sede_prestador s"
				+ "	JOIN contratacion.mod_oferta_prestador mo ON mo.prestador_id =  s.prestador_id"
				+ " WHERE s.prestador_id =:prestadorId"
				+ " AND s.portafolio_id IS NOT NULL"
				+ " AND NOT EXISTS (SELECT NULL FROM contratacion.mod_oferta_sede_prestador"
				+ "  			 WHERE oferta_id = mo.id AND sede_prestador_id =  s.id AND  portafolio_id = s.portafolio_id)")
})
public class OfertaSedePrestador implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "portafolio_id")
	private PortafolioSede portafolio;

	@ManyToOne
	@JoinColumn(name = "sede_prestador_id")
	private SedePrestador sedePrestador;

	// bi-directional many-to-one association to OfertaPrestador
	@ManyToOne
	@JoinColumn(name = "oferta_id")
	private OfertaPrestador ofertaPrestador;

	public OfertaSedePrestador() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public PortafolioSede getPortafolio() {
		return portafolio;
	}

	public void setPortafolio(PortafolioSede portafolio) {
		this.portafolio = portafolio;
	}

	public SedePrestador getSedePrestador() {
		return sedePrestador;
	}

	public void setSedePrestador(SedePrestador sedePrestador) {
		this.sedePrestador = sedePrestador;
	}

	public OfertaPrestador getOfertaPrestador() {
		return ofertaPrestador;
	}

	public void setOfertaPrestador(OfertaPrestador ofertaPrestador) {
		this.ofertaPrestador = ofertaPrestador;
	}

}