package com.conexia.contractual.model.maestros;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "programa", schema = "maestros")
public class Programa extends Descriptivo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7436285500534773301L;

	public static final String CODIGO_CANCER = "CA";
	
	@Column(name ="tipo_programa", nullable= true)
	private Integer tipoPrograma;
	
	@ManyToMany
	@JoinTable(name="programa_diagnostico", schema="maestros", joinColumns=
    @JoinColumn(name="programa_id"), inverseJoinColumns=
    @JoinColumn(name="diagnostico_id"))
	private List<Diagnostico> diagnosticos;
}
