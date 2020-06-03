package com.conexia.contractual.model.contratacion.auditoria;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "historial_cambios", schema = "auditoria")
public class HistorialCambios implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	/** Identificador. **/
	private Integer id;

	@Column(name = "user_id")
	private Integer userId;

	@Column(name = "evento")
	private String evento;

	@Column(name = "objeto")
	private String objeto;

	@Column(name = "negociacion_id")
	private Long negociacionId;

	@Column(name = "contrato_id")
	private Long contratoId;

	@Column(name = "minuta_id")
	private Long minutaId;

	@Column(name = "regional_antigua_id")
	private Integer regionalAntiguaId;

	@Column(name = "numero_contrato_antiguo")
	private String numeroContratoAntiguo;

	@Column(name = "regional_nueva_id")
	private Integer regionalNuevaId;

	@Column(name = "numero_contrato_nuevo")
	private String numeroContratoNuevo;

	public HistorialCambios() {}

	/**
	 * @return the id
	 */
	public Integer getId() {

		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Integer id) {

		this.id = id;
	}

	/**
	 * @return the user
	 */
	public Integer getUserId() {

		return userId;
	}

	/**
	 * @param user the user to set
	 */
	public void setUserId(Integer userId) {

		this.userId = userId;
	}

	/**
	 * @return the evento
	 */
	public String getEvento() {

		return evento;
	}

	/**
	 * @param evento the evento to set
	 */
	public void setEvento(String evento) {

		this.evento = evento;
	}

	/**
	 * @return the objeto
	 */
	public String getObjeto() {

		return objeto;
	}

	/**
	 * @param objeto the objeto to set
	 */
	public void setObjeto(String objeto) {

		this.objeto = objeto;
	}



	/**
	 * @return el negociacionId
	 */
	public Long getNegociacionId() {
		return negociacionId;
	}

	/**
	 * @param negociacionId el negociacionId a establecer
	 */
	public void setNegociacionId(Long negociacionId) {
		this.negociacionId = negociacionId;
	}

	/**
	 * @return el contratoId
	 */
	public Long getContratoId() {
		return contratoId;
	}

	/**
	 * @param contratoId el contratoId a establecer
	 */
	public void setContratoId(Long contratoId) {
		this.contratoId = contratoId;
	}

	/**
	 * @return el minutaId
	 */
	public Long getMinutaId() {
		return minutaId;
	}

	/**
	 * @param minutaId el minutaId a establecer
	 */
	public void setMinutaId(Long minutaId) {
		this.minutaId = minutaId;
	}

	public Integer getRegionalAntiguaId() {
		return regionalAntiguaId;
	}

	public void setRegionalAntiguaId(Integer regionalAntiguaId) {
		this.regionalAntiguaId = regionalAntiguaId;
	}

	public String getNumeroContratoAntiguo() {
		return numeroContratoAntiguo;
	}

	public void setNumeroContratoAntiguo(String numeroContratoAntiguo) {
		this.numeroContratoAntiguo = numeroContratoAntiguo;
	}

	public Integer getRegionalNuevaId() {
		return regionalNuevaId;
	}

	public void setRegionalNuevaId(Integer regionalNuevaId) {
		this.regionalNuevaId = regionalNuevaId;
	}

	public String getNumeroContratoNuevo() {
		return numeroContratoNuevo;
	}

	public void setNumeroContratoNuevo(String numeroContratoNuevo) {
		this.numeroContratoNuevo = numeroContratoNuevo;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {

		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		HistorialCambios other = (HistorialCambios) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		return "HistorialCambios [id=" + id + ", user=" + userId + ", evento=" + evento
				+ ", objeto=" + objeto + "]";
	}
}
