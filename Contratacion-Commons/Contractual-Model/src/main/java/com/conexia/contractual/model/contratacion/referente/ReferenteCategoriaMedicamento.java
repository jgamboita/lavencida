package com.conexia.contractual.model.contratacion.referente;

import com.conexia.contractual.model.contratacion.CategoriaMedicamento;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entity implementation class for Entity: ReferenteCategoriaMedicamento
 *
 */
@Entity
@Table(name = "referente_categoria_medicamento", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name = "ReferenteCategoriaMedicamento.buscarReferenteCategoriasMedicamento",
			query = "SELECT  new com.conexia.contratacion.commons.dto.referente.ReferenteCategoriaMedicamentoDto "
				+ "(rcm.id,cm.id,cm.codigo, cm.nombre, SUM(rm.frecuencia), "
				+ "SUM(rm.costoMedioUsuario), SUM(rm.numeroAtenciones),SUM(rm.numeroUsuarios) ) "
				+ "FROM ReferenteMedicamento rm "
				+ "JOIN rm.referenteCategoriaMedicamento rcm "
				+ "JOIN rcm.categoriaMedicamento cm "
				+ "WHERE rcm.referente.id =:referenteId "
				+ "GROUP BY rcm.id,cm.id,cm.codigo,cm.nombre"),
	@NamedQuery(name = "ReferenteCategoriaMedicamento.eliminarReferenteCategoriaMedicamento",
			query = "DELETE FROM ReferenteCategoriaMedicamento rcm "
				+ "where rcm.id in (:Ids)"),
	@NamedQuery(name  = "ReferenteCategoriaMedicamento.eliminarCategoriaMedicamentoReferente",
			query = "DELETE FROM ReferenteCategoriaMedicamento rcm WHERE rcm.referente.id = :referenteId "
			)
})
public class ReferenteCategoriaMedicamento implements Serializable {


	private static final long serialVersionUID = 1L;

	public ReferenteCategoriaMedicamento() {
		super();
	}

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "id", unique=true, nullable=false)
	private Long id;

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
	@JoinColumn(name = "categoria_medicamento_id")
	private CategoriaMedicamento categoriaMedicamento;

	@ManyToOne
	@JoinColumn(name = "referente_id")
	private Referente referente;



	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public CategoriaMedicamento getCategoriaMedicamento() {
		return categoriaMedicamento;
	}

	public void setCategoriaMedicamento(CategoriaMedicamento categoriaMedicamento) {
		this.categoriaMedicamento = categoriaMedicamento;
	}

	public Referente getReferente() {
		return referente;
	}

	public void setReferente(Referente referente) {
		this.referente = referente;
	}

}
