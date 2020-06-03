package com.conexia.contractual.model.contratacion.referente;

import com.conexia.contractual.model.maestros.Departamento;
import com.conexia.contractual.model.maestros.Municipio;
import com.conexia.contractual.model.maestros.Regional;
import com.conexia.contractual.model.maestros.ZonaMunicipio;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity implementation class for Entity: ReferenteUbicacion
 * @dmora
 *
 */
@Entity
@Table(name = "referente_ubicacion", schema = "contratacion")
@NamedQueries({
@NamedQuery(name = "ReferenteUbicacion.buscarUbicacionReferente",
				query ="SELECT new com.conexia.contratacion.commons.dto.referente.ReferenteUbicacionDto(r.id, "
						+ "r.descripcion, d.id, d.descripcion, zm.id, "
						+ "zm.descripcion, m.id ,m.descripcion)"
						+ "FROM ReferenteUbicacion ru "
						+ "JOIN ru.regional r "
						+ "LEFT JOIN ru.departamento d "
						+ "LEFT JOIN ru.zonaMunicipio zm "
						+ "LEFT JOIN ru.municipio m "
						+ "WHERE ru.referente.id = :referenteId "),
@NamedQuery(name = "ReferenteUbicacion.eliminarReferenteUbicacion",
				query = "DELETE FROM ReferenteUbicacion rc WHERE rc.referente.id = :referenteId ")
})
public class ReferenteUbicacion implements Serializable {


	private static final long serialVersionUID = 1L;

	public ReferenteUbicacion() {
		super();
	}

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;

	@OneToOne
	@JoinColumn(name = "referente_id")
	private Referente referente;

	@OneToOne
	@JoinColumn(name = "regional_id")
	private Regional regional;

	@OneToOne
	@JoinColumn(name = "departamento_id")
	private Departamento departamento;

	@OneToOne
	@JoinColumn(name = "municipio_id")
	private Municipio municipio;

	@OneToOne
	@JoinColumn(name = "zona_municipio_id")
	private ZonaMunicipio zonaMunicipio;


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

	public Regional getRegional() {
		return regional;
	}

	public void setRegional(Regional regional) {
		this.regional = regional;
	}

	public Departamento getDepartamento() {
		return departamento;
	}

	public void setDepartamento(Departamento departamento) {
		this.departamento = departamento;
	}

	public Municipio getMunicipio() {
		return municipio;
	}

	public void setMunicipio(Municipio municipio) {
		this.municipio = municipio;
	}

	public ZonaMunicipio getZonaMunicipio() {
		return zonaMunicipio;
	}

	public void setZonaMunicipio(ZonaMunicipio zonaMunicipio) {
		this.zonaMunicipio = zonaMunicipio;
	}

}
