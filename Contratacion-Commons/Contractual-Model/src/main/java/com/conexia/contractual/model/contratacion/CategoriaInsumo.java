package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;


/**
 * Entidad categoria insumo.
 * 
 * @author mcastro
 * @date 22/05/2014 
 */

@Entity
@Table(name="categoria_insumo", schema = "contratacion")
public class CategoriaInsumo implements Identifiable<Long>, Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;

	@Column(name = "descripcion", nullable = true, length=500)
	private String descripcion;

	public CategoriaInsumo() {
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