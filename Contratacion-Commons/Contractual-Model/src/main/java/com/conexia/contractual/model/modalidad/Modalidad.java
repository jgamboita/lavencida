package com.conexia.contractual.model.modalidad;

import com.conexia.contractual.model.maestros.Descriptivo;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * The persistent class for the modalidad_negociacion database table.
 * 
 */
@Entity
@Table(name = "modalidad_negociacion", schema = "contratacion")
@NamedQuery(name = "Modalidad.findAll", query = "SELECT new com.conexia.contratacion.commons.dto.DescriptivoDto(m.id, m.descripcion) FROM Modalidad m")
public class Modalidad extends Descriptivo {
	private static final long serialVersionUID = 1L;
	
	public Modalidad() {}

	public Modalidad(Integer id) {
		super(id);
	}
}