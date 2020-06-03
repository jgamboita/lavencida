package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;


/**
 * Entidad Capacidad Sede
 * 
 * @author mcastro
 * @date 22/05/2014 
 */

@Entity
@Table(name="capacidad_sede", schema = "contratacion")
public class CapacidadSede implements Identifiable<Long>, Serializable{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;

	@Column(name="num_camas_hospitalizacion")
	private Integer numCamasHospitalizacion;

	@Column(name="num_camas_observacion")
	private Integer numCamasObservacion;

	@Column(name="num_consultorios_consulta_ext")
	private Integer numConsultoriosConsultaExt;

	@Column(name="num_consultorios_urgencias")
	private Integer numConsultoriosUrgencias;

	@Column(name="num_salas")
	private Integer numSalas;

	@ManyToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="sede_prestador_id")
	private SedePrestador sedePrestador;

	public CapacidadSede() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getNumCamasHospitalizacion() {
		return this.numCamasHospitalizacion;
	}

	public void setNumCamasHospitalizacion(Integer numCamasHospitalizacion) {
		this.numCamasHospitalizacion = numCamasHospitalizacion;
	}

	public Integer getNumCamasObservacion() {
		return this.numCamasObservacion;
	}

	public void setNumCamasObservacion(Integer numCamasObservacion) {
		this.numCamasObservacion = numCamasObservacion;
	}

	public Integer getNumConsultoriosConsultaExt() {
		return this.numConsultoriosConsultaExt;
	}

	public void setNumConsultoriosConsultaExt(Integer numConsultoriosConsultaExt) {
		this.numConsultoriosConsultaExt = numConsultoriosConsultaExt;
	}

	public Integer getNumConsultoriosUrgencias() {
		return this.numConsultoriosUrgencias;
	}

	public void setNumConsultoriosUrgencias(Integer numConsultoriosUrgencias) {
		this.numConsultoriosUrgencias = numConsultoriosUrgencias;
	}

	public Integer getNumSalas() {
		return this.numSalas;
	}

	public void setNumSalas(Integer numSalas) {
		this.numSalas = numSalas;
	}

	public SedePrestador getSedePrestador() {
		return this.sedePrestador;
	}

	public void setSedePrestador(SedePrestador sedePrestador) {
		this.sedePrestador = sedePrestador;
	}

}