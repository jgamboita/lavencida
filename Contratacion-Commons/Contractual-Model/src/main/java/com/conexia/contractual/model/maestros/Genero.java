package com.conexia.contractual.model.maestros;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "genero", schema = "maestros")
public class Genero extends Descriptivo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String COD_AMBOS = "A";
	public static final String COD_FEMENINO = "F";
	public static final String COD_MASCULINO = "M";
}
