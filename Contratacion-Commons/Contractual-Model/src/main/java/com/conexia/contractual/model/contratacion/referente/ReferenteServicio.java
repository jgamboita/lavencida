package com.conexia.contractual.model.contratacion.referente;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.contratacion.ServicioSalud;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 
 * @author icruz
 *
 */
@Entity
@Table(name = "referente_servicio", schema = "contratacion")
public class ReferenteServicio implements Identifiable<Long>, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4953565887539206035L;

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "referente_id")
    private Referente referente;
    
	@ManyToOne
    @JoinColumn(name = "servicio_salud_id")
    private ServicioSalud servicioSalud;
    
    @Column(name = "frecuencia")
    private Double frecuencia;
    
    @Column(name = "costo_medio_usuario")
    private BigDecimal costoMedioUsuario;
    
    @Column(name = "numero_atenciones")
    private Integer numeroAtenciones;
    
    @Column(name = "numero_usuarios")
    private Integer numeroUsuarios;
    
    @Column(name = "estado")
    private Integer estado;

	@Override
	public Long getId() {		
		return id;
	}
	
	public Referente getReferente() {
		return referente;
	}

	public void setReferente(Referente referente) {
		this.referente = referente;
	}

	public ServicioSalud getServicioSalud() {
		return servicioSalud;
	}

	public void setServicioSalud(ServicioSalud servicioSalud) {
		this.servicioSalud = servicioSalud;
	}
	
	public Double getFrecuencia() {
		return frecuencia;
	}

	public void setFrecuencia(Double frecuencia) {
		this.frecuencia = frecuencia;
	}

	public BigDecimal getCostoMedioUsuario() {
		return costoMedioUsuario;
	}

	public void setCostoMedioUsuario(BigDecimal costoMedioUsuario) {
		this.costoMedioUsuario = costoMedioUsuario;
	}

	public Integer getNumeroAtenciones() {
		return numeroAtenciones;
	}

	public void setNumeroAtenciones(Integer numeroAtenciones) {
		this.numeroAtenciones = numeroAtenciones;
	}

	public Integer getNumeroUsuarios() {
		return numeroUsuarios;
	}

	public void setNumeroUsuarios(Integer numeroUsuarios) {
		this.numeroUsuarios = numeroUsuarios;
	}

	public Integer getEstado() {
		return estado;
	}

	public void setEstado(Integer estado) {
		this.estado = estado;
	}

	public void setId(Long id) {
		this.id = id;
	}

    

}
