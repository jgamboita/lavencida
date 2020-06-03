package com.conexia.contractual.model.contratacion.referente;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.maestros.Procedimientos;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author icruz
 *
 */
@Entity
@Table(name = "referente_procedimiento_servicio", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name = "ReferenteProcedimientoServicio.exportarProcedimientosPGP",
			query = "SELECT new com.conexia.contratacion.commons.dto.negociacion.AnexoReferenteProcedimientosDto(cap.id, "
				+ "cap.descripcion, cp.codigo, cp.descripcion, "
				+ "ps.codigoCliente, ps.descripcion, rps.frecuencia, rps.costoMedioUsuario, "
				+ "rps.numeroAtenciones, rps.numeroUsuarios, rps.pgp) FROM ReferenteProcedimientoServicio rps "
				+ "JOIN rps.referenteCapitulo rc "
				+ "JOIN rps.procedimientoServicio ps "
				+ "JOIN rc.capituloProcedimiento cap "
				+ "JOIN rc.categoriaProcedimiento cp "
				+ "WHERE rc.referente.id = :referenteId ")

})
public class ReferenteProcedimientoServicio implements Identifiable<Long>, Serializable {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;


	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;

	@ManyToOne
    @JoinColumn(name = "referente_servicio_id")
    private ReferenteServicio referenteServicio;

	@ManyToOne
    @JoinColumn(name = "procedimiento_servicio_id")
    private Procedimientos procedimientoServicio;

	@Column(name = "frecuencia")
    private BigDecimal frecuencia;

    @Column(name = "costo_medio_usuario")
    private BigDecimal costoMedioUsuario;

    @Column(name = "numero_atenciones")
    private Integer numeroAtenciones;

    @Column(name = "numero_usuarios")
    private Integer numeroUsuarios;

    @Column(name = "estado")
    private Integer estado;

    @ManyToOne
	@JoinColumn(name = "referente_capitulo_id")
	private ReferenteCapitulo referenteCapitulo;

    @Column(name="pgp")
    private BigDecimal pgp;


	@Override
	public Long getId() {
		return id;
	}

	public ReferenteServicio getReferenteServicio() {
		return referenteServicio;
	}


	public void setReferenteServicio(ReferenteServicio referenteServicio) {
		this.referenteServicio = referenteServicio;
	}


	public Procedimientos getProcedimientoServicio() {
		return procedimientoServicio;
	}


	public void setProcedimientoServicio(Procedimientos procedimientoServicio) {
		this.procedimientoServicio = procedimientoServicio;
	}

	public BigDecimal getFrecuencia() {
		return frecuencia;
	}

	public void setFrecuencia(BigDecimal frecuencia) {
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

	public ReferenteCapitulo getReferenteCapitulo() {
		return referenteCapitulo;
	}

	public void setReferenteCapitulo(ReferenteCapitulo referenteCapitulo) {
		this.referenteCapitulo = referenteCapitulo;
	}

}
