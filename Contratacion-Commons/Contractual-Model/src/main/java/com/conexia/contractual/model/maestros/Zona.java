package com.conexia.contractual.model.maestros;

import javax.persistence.Entity;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(schema = "maestros", name = "zona")
@NamedQuery(
		name = "Zona.listarTodas", 
		query = "SELECT new com.conexia.contratacion.commons.dto.DescriptivoDto(z.id, z.descripcion) FROM Zona z ")
public class Zona extends Descriptivo {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4855188548126023453L;

}
