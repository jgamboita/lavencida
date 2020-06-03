package com.conexia.contractual.model.contratacion.portafolio;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.model.contratacion.ServicioSalud;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entidad procedimiento propio.
 * 
 * @author jpicon
 * @date 05/09/2014
 */
@Entity
@Table(name="procedimiento_propio", schema = "contratacion")
public class ProcedimientoPropio implements Serializable, Identifiable<Long> {

	private static final long serialVersionUID = 3209010850932538275L;

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;

	@Column(name = "codigo", length=10)
	private String codigo;
	
	@Column(name = "codigo_normativo", length=10)
	private String codigoNormativo;
	
	@Column(name = "descripcion", length=500)
	private String descripcion;
	
	@OneToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="prestador_id")
	private Prestador prestador;
	
	@OneToOne(fetch= FetchType.LAZY)
	@JoinColumn(name="servicio_salud_id")
	private ServicioSalud servicioSalud;

	public ProcedimientoPropio() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodigo() {
		return this.codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getCodigoNormativo() {
		return codigoNormativo;
	}

	public void setCodigoNormativo(String codigoNormativo) {
		this.codigoNormativo = codigoNormativo;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}
	
	public Prestador getPrestador() {
		return prestador;
	}

	public void setPrestador(Prestador prestador) {
		this.prestador = prestador;
	}

	public ServicioSalud getServicioSalud() {
		return servicioSalud;
	}

	public void setServicioSalud(ServicioSalud servicioSalud) {
		this.servicioSalud = servicioSalud;
	}

}