package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "tarifarios_procedimientos", schema="contratacion")
@NamedQueries({
	@NamedQuery(name="TarifariosProcedimiento.findValidTarifariosByProcedimiento", query = "SELECT new com.conexia.contratacion.commons.dto.maestros.TipoTarifarioDto(tt.id, tt.codigo,tt.descripcion) "
			+ " FROM TarifariosProcedimiento tp "
			+ "	JOIN tp.tarifario tt "
			+ "	WHERE (tp.cups =:cupId OR tp.cups=:codigoEmssanar)"
			+ " ORDER BY tt.descripcion")
})
public class TarifariosProcedimiento implements Identifiable<Long>, Serializable {

	private static final long serialVersionUID = 120418536874703405L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	//@Column(name="tarifarios_codigo", nullable=false, updatable=false)
	//private String tarifariosCodigo;

	@Column(name="cups", nullable=false)
	private String cups;

	@Column(name="codigo_tarifario", nullable=false)
	private String codigoTarifario;

	@Column(name="grupo_id")
	private Integer grupoId;

	@Column(name="peso")
	private BigDecimal peso;

	@ManyToOne(optional=false)
	@JoinColumn(name="tarifarios_codigo", referencedColumnName="codigo", nullable=false, updatable=false)
	private TipoTarifario tarifario;

	/**
	 * Constructor por defecto
	 */
	public TarifariosProcedimiento() {}

	/**
	 * Constructor por id
	 * @param id
	 */
	public TarifariosProcedimiento(Long id) {
		this.id = id;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the cups
	 */
	public String getCups() {
		return cups;
	}

	/**
	 * @param cups the cups to set
	 */
	public void setCups(String cups) {
		this.cups = cups;
	}

	/**
	 * @return the codigoTarifario
	 */
	public String getCodigoTarifario() {
		return codigoTarifario;
	}

	/**
	 * @param codigoTarifario the codigoTarifario to set
	 */
	public void setCodigoTarifario(String codigoTarifario) {
		this.codigoTarifario = codigoTarifario;
	}

	/**
	 * @return the grupoId
	 */
	public Integer getGrupoId() {
		return grupoId;
	}

	/**
	 * @param grupoId the grupoId to set
	 */
	public void setGrupoId(Integer grupoId) {
		this.grupoId = grupoId;
	}

	/**
	 * @return the peso
	 */
	public BigDecimal getPeso() {
		return peso;
	}

	/**
	 * @param peso the peso to set
	 */
	public void setPeso(BigDecimal peso) {
		this.peso = peso;
	}

}
