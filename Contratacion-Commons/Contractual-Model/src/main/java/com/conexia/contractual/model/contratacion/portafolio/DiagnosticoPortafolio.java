package com.conexia.contractual.model.contratacion.portafolio;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.maestros.Diagnostico;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "diagnostico_portafolio", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name="DiagnosticoPortafolio.findDtoByPortafolio", query="SELECT new com.conexia.contratacion.commons.dto.DiagnosticoPortafolioDto("
			+ "dp.id, d.id, d.codigo, d.descripcion, dp.portafolio.id, dp.principal"
			+ ") "
			+ "FROM DiagnosticoPortafolio dp "
			+ "JOIN dp.diagnostico d "
			+ "WHERE dp.portafolio.id = :portafolioId")
})
public class DiagnosticoPortafolio implements Identifiable<Long>, Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "diagnostico_id")
	private Diagnostico diagnostico;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "portafolio_id")
	private Portafolio portafolio;

	@Column(name = "principal")
	private Boolean principal;

	public DiagnosticoPortafolio() {
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the diagnostico
	 */
	public Diagnostico getDiagnostico() {
		return diagnostico;
	}

	/**
	 * @param diagnostico
	 *            the diagnostico to set
	 */
	public void setDiagnostico(Diagnostico diagnostico) {
		this.diagnostico = diagnostico;
	}

	/**
	 * @return the portafolio
	 */
	public Portafolio getPortafolio() {
		return portafolio;
	}

	/**
	 * @param portafolio
	 *            the portafolio to set
	 */
	public void setPortafolio(Portafolio portafolio) {
		this.portafolio = portafolio;
	}

	/**
	 * @return the esPrincipal
	 */
	public Boolean getPrincipal() {
		return principal;
	}

	/**
	 * @param esPrincipal
	 *            the esPrincipal to set
	 */
	public void setPrincipal(Boolean principal) {
		this.principal = principal;
	}

}