package com.conexia.contractual.model.contratacion.portafolio;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contractual.model.contratacion.ServicioSalud;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the servicio_salud_modalidad database table.
 * 
 */
@Entity
@Table(name="servicio_salud_modalidad", schema = "contratacion")
public class ServicioSaludModalidad implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	private Integer id;

	@Column(name="modalidad_id")
	private NegociacionModalidadEnum modalidad;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="servicio_salud_id")
	private ServicioSalud servicioSalud;

	public ServicioSaludModalidad() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public ServicioSalud getServicioSalud() {
		return this.servicioSalud;
	}

	public void setServicioSalud(ServicioSalud servicioSalud) {
		this.servicioSalud = servicioSalud;
	}

    public NegociacionModalidadEnum getModalidad() {
        return modalidad;
    }

    public void setModalidad(NegociacionModalidadEnum modalidad) {
        this.modalidad = modalidad;
    }

}