package com.conexia.contractual.model.maestros;

import javax.persistence.*;


@Entity
@Table(name = "grupo_etnico", schema = "maestros")
@NamedQueries({
    @NamedQuery(name = "GrupoEtnico.findAll",
    		query="SELECT new com.conexia.contratacion.commons.dto.maestros.GrupoEtnicoDto(g.id, g.codigo, g.descripcion) "
    				+ " FROM GrupoEtnico g ")
}) 
public class GrupoEtnico extends Descriptivo {

	private static final long serialVersionUID = 1L;
	
	@Column(name = "codigo")
	private String codigo;

	
	/**
	 * Devuelve el valor de codigo.
	 *
	 * @return El valor de codigo.
	 */
	public String getCodigo() {
	
		return codigo;
	}

	
	/**
	 * Asigna un nuevo valor a codigo.
	 *
	 * @param codigo El valor a asignar a codigo.
	 */
	public void setCodigo(String codigo) {
	
		this.codigo = codigo;
	}
	
	
}

