package com.conexia.contractual.model.maestros;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entity implementation class for Entity: ZonaMunicipio
 *
 */
@Entity
@Table(name = "zona_municipio", schema = "maestros")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({
	@NamedQuery(name = "ZonaMunicipio.findZonasPorRegional",
			query = "SELECT DISTINCT NEW com.conexia.contratacion.commons.dto.maestros.ZonaMunicipioDto(zm.id, zm.descripcion, d.id) "
					+ "FROM Municipio m "
					+ "JOIN m.zona zm "
					+ "JOIN m.departamento d "
					+ "JOIN d.regional r "
					+ "WHERE r.id = :regionalId "),
	@NamedQuery(name = "ZonaMunicipio.findZonasCobertura",
			query = "SELECT DISTINCT NEW com.conexia.contratacion.commons.dto.maestros.MunicipioDto(zm.id, zm.descripcion, d.id, d.codigo, d.descripcion) "
					+ "FROM Municipio m "
					+ "JOIN m.zona zm "
					+ "JOIN m.departamento d "
					+ "GROUP BY zm.id, zm.descripcion, d.id, d.codigo, d.descripcion "
					+ "ORDER BY d.descripcion ")
})


public class ZonaMunicipio implements Serializable {


	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Integer id;

	@Column(name = "descripcion", nullable = false, length = 25)
    private String descripcion;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
}
