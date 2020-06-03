package com.conexia.contractual.model.contratacion.referente;

import javax.persistence.*;
import java.io.Serializable;

/**
 * @dmora
 *
 */
@Entity
@Table(name = "referente_modalidad", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name = "ReferenteModalidad.borrarReferenteModalidad",
			query = "DELETE FROM ReferenteModalidad rm WHERE rm.referente.id = :referenteId "),
	@NamedQuery(name = "ReferenteModalidad.modalidadesReferente",
			query = "SELECT rm.modalidad FROM ReferenteModalidad rm WHERE rm.referente.id = :referenteId ")
})
public class ReferenteModalidad implements Serializable {


	private static final long serialVersionUID = 1L;

	public ReferenteModalidad() {
		super();
	}

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "referente_id")
	private Referente referente;


	@Column(name = "modalidad")
	private String modalidad;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Referente getReferente() {
		return referente;
	}

	public void setReferente(Referente referente) {
		this.referente = referente;
	}

	public String getModalidad() {
		return modalidad;
	}

	public void setModalidad(String modalidad) {
		this.modalidad = modalidad;
	}



}
