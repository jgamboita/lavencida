package com.conexia.contractual.model.maestros;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "diagnostico", schema="maestros")
public class Diagnostico implements Identifiable<Long>, Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;
	
	@Column(name = "codigo", nullable = false, length=6)
	private String codigo;
	
	@Column(name = "descripcion", nullable = false, length=500)
	private String descripcion;
	
	@Column(name="cliente_pk", nullable = true)
	private Integer clientePk;
	
    @ManyToOne
    @JoinColumn(name = "genero_id", nullable = false)
    private Genero genero;
	
	@Column(name = "edad_ini", nullable = false)
    private Integer edadIni;

    @Column(name = "edad_fin", nullable = false)
    private Integer edadFin;
    
    @ManyToMany
    @JoinTable(name="programa_diagnostico", schema="maestros", joinColumns=
    @JoinColumn(name="diagnostico_id"), inverseJoinColumns=
    @JoinColumn(name="programa_id"))
	private List<Programa> programas;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}


	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Integer getClientePk() {
		return clientePk;
	}

	public void setClientePk(Integer clientePk) {
		this.clientePk = clientePk;
	}

	public Integer getEdadIni() {
		return edadIni;
	}

	public void setEdadIni(Integer edadIni) {
		this.edadIni = edadIni;
	}

	public Integer getEdadFin() {
		return edadFin;
	}

	public void setEdadFin(Integer edadFin) {
		this.edadFin = edadFin;
	}

	/**
	 * Método que obtiene el valor del atributo <code>genero</code>
	 * @return the genero
	 * @author Camilo A. Alfonso V. <calfonso@conexia.com>
	 * @version 12/05/2014
	 */
	public Genero getGenero() {
		return genero;
	}

	/**
	 * Método que asigna el valor al atributo <code>genero</code>
	 * @param genero the genero to set
	 * @author Camilo A. Alfonso V. <calfonso@conexia.com>
	 * @version 12/05/2014
	 */
	public void setGenero(Genero genero) {
		this.genero = genero;
	}

	/**
	 * Método que obtiene el valor del atributo <code>programas</code>
	 * @return the programas
	 * @author Camilo A. Alfonso V. <calfonso@conexia.com>
	 * @version 12/05/2014
	 */
	public List<Programa> getProgramas() {
		return programas;
	}

	/**
	 * Método que asigna el valor al atributo <code>programas</code>
	 * @param programasDx the programas to set
	 * @author Camilo A. Alfonso V. <calfonso@conexia.com>
	 * @version 12/05/2014
	 */
	public void setProgramasDx(List<Programa> programas) {
		this.programas = programas;
	}
}
