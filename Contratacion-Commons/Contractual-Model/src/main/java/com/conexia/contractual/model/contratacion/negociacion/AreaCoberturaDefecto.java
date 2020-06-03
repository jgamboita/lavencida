package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.maestros.Municipio;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The persistent class for the area_cobertura_defecto database table.
 *
 */
@Entity
@Table(schema = "contratacion", name="area_cobertura_defecto")
@NamedQueries({
	@NamedQuery(name = "AreaCoberturaDefecto.findMunicipiosCoberturaBySedeId", query = " SELECT mc "
			+ " FROM AreaCoberturaDefecto ac, SedePrestador sp "
			+ " JOIN ac.municipioReferente mr "
			+ " JOIN ac.municipioCobertura mc "
			+ " JOIN mc.departamento dpto "
			+ " WHERE sp.id = :sedeId "
			+ " AND sp.municipio.id = mr.id "),
	@NamedQuery(name = "AreaCoberturaDefecto.findMunicipiosPresenciaEmssanar",
			query = "SELECT mc FROM AreaCoberturaDefecto ac "
			+ " JOIN ac.municipioCobertura mc "
			+ " WHERE ac.municipioReferente.departamento.id in (:departamentoIds) ")
})
public class AreaCoberturaDefecto implements Identifiable<Long>, Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_cobertura_id")
	private Municipio municipioCobertura;

	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_referente_id")
	private Municipio municipioReferente;

	public AreaCoberturaDefecto() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Municipio getMunicipioCobertura() {
		return municipioCobertura;
	}

	public void setMunicipioCobertura(Municipio municipioCobertura) {
		this.municipioCobertura = municipioCobertura;
	}

	public Municipio getMunicipioReferente() {
		return municipioReferente;
	}

	public void setMunicipioReferente(Municipio municipioReferente) {
		this.municipioReferente = municipioReferente;
	}
}
