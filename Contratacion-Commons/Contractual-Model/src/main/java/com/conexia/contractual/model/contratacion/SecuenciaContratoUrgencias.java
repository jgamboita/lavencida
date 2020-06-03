package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;


@Entity
@Table(name = "secuencia_contrato_urgencias", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name = "SecuenciaContratoUrgencias.findSecuencia",
		query = "SELECT  new com.conexia.contratacion.commons.dto.contractual.legalizacion.SecuenciaContratoDto(sc.id, sc.codigoRegional, sc.modalidad, sc.regimen,"
		+ "sc.anio, sc.secuencia)"
		+ "from SecuenciaContratoUrgencias sc "
		+ "WHERE sc.codigoRegional =:codigoRegional AND "
		+ "sc.modalidad =:modalidad AND "
		+ "sc.regimen =:regimen AND "
		+ "sc.anio =:anio "
		),
	@NamedQuery(name = "SecuenciaContratoUrgencias.updateSecuencia",
		query ="UPDATE SecuenciaContratoUrgencias "
		+ "SET secuencia =:secuencia "
		+ "WHERE codigoRegional =:codigoRegional AND "
		+ "modalidad =:modalidad AND "
		+ "regimen =:regimen AND "
		+ "anio =:anio ")
	})
public class SecuenciaContratoUrgencias implements Identifiable<Long>, Serializable {

	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@NotNull
	@Column(name = "codigo_regional")
	private String codigoRegional;
	
	@NotNull
	@Column(name = "modalidad")
	private String modalidad;
	
	@NotNull
	@Column(name = "regimen")
	private String regimen;
	
	@NotNull
	@Column(name = "anio")
	private String anio;
	
	@NotNull
	@Column(name = "secuencia")
	private Integer secuencia;
	
	@ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestador_id")
    private Prestador prestadorId;
	
	
	public String getCodigoRegional() {
		return codigoRegional;
	}

	public void setCodigoRegional(String codigoRegional) {
		this.codigoRegional = codigoRegional;
	}

	public String getModalidad() {
		return modalidad;
	}

	public void setModalidad(String modalidad) {
		this.modalidad = modalidad;
	}

	public String getRegimen() {
		return regimen;
	}

	public void setRegimen(String regimen) {
		this.regimen = regimen;
	}

	public String getAnio() {
		return anio;
	}

	public void setAnio(String anio) {
		this.anio = anio;
	}

	public Prestador getPrestadorId() {
		return prestadorId;
	}

	public void setPrestadorId(Prestador prestadorId) {
		this.prestadorId = prestadorId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getSecuencia() {
		return secuencia;
	}

	public void setSecuencia(Integer secuencia) {
		this.secuencia = secuencia;
	}
	

}
