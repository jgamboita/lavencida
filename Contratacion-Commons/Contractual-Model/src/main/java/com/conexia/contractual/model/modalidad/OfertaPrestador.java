package com.conexia.contractual.model.modalidad;

import com.conexia.contractual.model.contratacion.Prestador;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * The persistent class for the mod_oferta_prestador database table.
 * 
 */
@Entity
@Table(name = "mod_oferta_prestador", schema="contratacion")
@NamedQueries({
	@NamedQuery(
		name = "OfertaPrestador.obtenerAPresentarPorPrestadorIdYModalidadId",
		query = "SELECT new com.conexia.contratacion.commons.dto.capita.OfertaPrestadorDto("
				+ " o.id, o.mesesVigencia, o.fechaInicioVigencia) "
				+ " FROM OfertaPrestador o "
				+ "WHERE o.prestador.id = :prestadorId "
				+ "  AND o.ofertaPresentar IS TRUE "
				+ "  AND o.modalidad.id = :modalidadId"),
	@NamedQuery(
		name = "OfertaPrestador.cierreAperturaOfera",	
		query = "UPDATE OfertaPrestador o SET o.fechaFinVigencia = :fechaFinVigencia, o.fechaInicioVigencia = :fechaInicioVigencia, o.mesesVigencia = :mesesVigencia WHERE o.id = :ofertaId")
})
@NamedNativeQueries({
	@NamedNativeQuery(name="OfertaPrestador.eliminarOfertaPrestador",
			query="DELETE FROM contratacion.mod_oferta_prestador "
					+ "	WHERE id IN(SELECT mop.id "
					+ "				FROM contratacion.mod_oferta_prestador mop"
					+ "				WHERE mop.prestador_id=:prestadorId)"),
	@NamedNativeQuery(name="OfertaPrestador.insertarOfertaPrestador",
		query="INSERT INTO contratacion.mod_oferta_prestador(prestador_id, modalidad_id, oferta_a_presentar)"
				+ " SELECT id, 1, true FROM contratacion.prestador p"
				+ " WHERE p.id =:prestadorId"
				+ "	AND NOT exists (select null from contratacion.mod_oferta_prestador"
				+ "					where prestador_id = p.id and modalidad_id = 1)")
})
public class OfertaPrestador implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Temporal(TemporalType.DATE)
	@Column(name = "fecha_fin_vigencia")
	private Date fechaFinVigencia;

	@Temporal(TemporalType.DATE)
	@Column(name = "fecha_inicio_vigencia")
	private Date fechaInicioVigencia;

	@Column(name = "meses_vigencia")
	private Integer mesesVigencia;

	@ManyToOne
	@JoinColumn(name = "modalidad_id")
	private Modalidad modalidad;

	@Column(name = "oferta_a_presentar")
	private Boolean ofertaPresentar;

	@ManyToOne
	@JoinColumn(name = "prestador_id")
	private Prestador prestador;

	// bi-directional many-to-one association to OfertaSedePrestador
	@OneToMany(mappedBy = "ofertaPrestador")
	private List<OfertaSedePrestador> ofertaSedesPrestador;

	public OfertaPrestador() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getFechaFinVigencia() {
		return this.fechaFinVigencia;
	}

	public void setFechaFinVigencia(Date fechaFinVigencia) {
		this.fechaFinVigencia = fechaFinVigencia;
	}

	public Date getFechaInicioVigencia() {
		return this.fechaInicioVigencia;
	}

	public void setFechaInicioVigencia(Date fechaInicioVigencia) {
		this.fechaInicioVigencia = fechaInicioVigencia;
	}

	public Integer getMesesVigencia() {
		return this.mesesVigencia;
	}

	public void setMesesVigencia(Integer mesesVigencia) {
		this.mesesVigencia = mesesVigencia;
	}

	public Modalidad getModalidad() {
		return modalidad;
	}

	public void setModalidad(Modalidad modalidad) {
		this.modalidad = modalidad;
	}

	public Boolean getOfertaPresentar() {
		return ofertaPresentar;
	}

	public void setOfertaPresentar(Boolean ofertaPresentar) {
		this.ofertaPresentar = ofertaPresentar;
	}

	public Prestador getPrestador() {
		return prestador;
	}

	public void setPrestador(Prestador prestador) {
		this.prestador = prestador;
	}

	public List<OfertaSedePrestador> getOfertaSedesPrestador() {
		return ofertaSedesPrestador;
	}

	public void setOfertaSedesPrestador(
			List<OfertaSedePrestador> ofertaSedesPrestador) {
		this.ofertaSedesPrestador = ofertaSedesPrestador;
	}

}