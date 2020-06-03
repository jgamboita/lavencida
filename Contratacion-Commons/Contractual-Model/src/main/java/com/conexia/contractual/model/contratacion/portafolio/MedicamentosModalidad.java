package com.conexia.contractual.model.contratacion.portafolio;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contractual.model.maestros.Medicamento;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The persistent class for the medicamentos_modalidad database table.
 * 
 */
@Entity
@Table(name="medicamentos_modalidad", schema="contratacion")
@NamedQuery(name="MedicamentosModalidad.findAll", query="SELECT m FROM MedicamentosModalidad m")
public class MedicamentosModalidad implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private Integer id;

	@ManyToOne
    @JoinColumn(name="medicamento_id")
	private Medicamento medicamento;

	@Column(name="modalidad_id")
    private NegociacionModalidadEnum modalidad;

	public MedicamentosModalidad() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

    public Medicamento getMedicamento() {
        return medicamento;
    }

    public void setMedicamento(Medicamento medicamento) {
        this.medicamento = medicamento;
    }

    public NegociacionModalidadEnum getModalidad() {
        return modalidad;
    }

    public void setModalidad(NegociacionModalidadEnum modalidad) {
        this.modalidad = modalidad;
    }

}