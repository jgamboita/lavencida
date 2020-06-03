package com.conexia.contractual.model.maestros;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 
 * @author icruz
 *
 */
@Entity
@Table(name = "tipo_referente", schema = "contratacion")
public class TipoReferente extends Descriptivo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2697763879640026797L;

    @Column(name = "codigo", nullable = true, length = 50)
    private String codigo;
    
    @Column(name = "modalidad_negociacion_id", nullable = true, length = 50)
    private Integer modalidadNegociacionId;
    
    @Column(name = "es_ria")
    private Boolean esRia;


	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Integer getModalidadNegociacionId() {
		return modalidadNegociacionId;
	}

	public void setModalidadNegociacionId(Integer modalidadNegociacionId) {
		this.modalidadNegociacionId = modalidadNegociacionId;
	}

	public Boolean getEsRia() {
		return esRia;
	}

	public void setEsRia(Boolean esRia) {
		this.esRia = esRia;
	}
}
