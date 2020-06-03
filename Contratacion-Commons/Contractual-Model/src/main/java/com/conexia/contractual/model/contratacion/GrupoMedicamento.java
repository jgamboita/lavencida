package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The persistent class for the grupo_medicamento database table.
 * 
 */
@Entity
@Table(name="grupo_medicamento", schema = "contratacion")
public class GrupoMedicamento implements Identifiable<Long>, Serializable {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;

	@Column(name = "descripcion", length=100)
	private String descripcion;

	public GrupoMedicamento() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

}