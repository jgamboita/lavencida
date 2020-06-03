package com.conexia.contractual.model.contratacion.portafolio;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contractual.model.maestros.Procedimientos;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The persistent class for the procedimientos_modalidad database table.
 * 
 */
@Entity
@Table(name="procedimientos_modalidad")
@NamedQuery(name="ProcedimientosModalidad.findAll", query="SELECT p FROM ProcedimientosModalidad p")
public class ProcedimientosModalidad implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private Integer id;

	@Column(name="modalidad_id")
    private NegociacionModalidadEnum modalidad;

	@OneToOne
    @JoinColumn(name="procedimiento_servicio_id")
	private Procedimientos procedimientoServicio;

	public ProcedimientosModalidad() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

    public NegociacionModalidadEnum getModalidad() {
        return modalidad;
    }

    public void setModalidad(NegociacionModalidadEnum modalidad) {
        this.modalidad = modalidad;
    }

    public Procedimientos getProcedimientoServicio() {
        return procedimientoServicio;
    }

    public void setProcedimientoServicio(
            Procedimientos procedimientoServicio) {
        this.procedimientoServicio = procedimientoServicio;
    }
}