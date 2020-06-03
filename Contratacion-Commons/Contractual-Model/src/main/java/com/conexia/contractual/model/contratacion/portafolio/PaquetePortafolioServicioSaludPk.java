package com.conexia.contractual.model.contratacion.portafolio;

import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;

import javax.persistence.Embeddable;
import java.io.Serializable;

@Embeddable
public class PaquetePortafolioServicioSaludPk  implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private PaquetePortafolioDto paquetePortafolio;
	private ServicioSaludDto servicioSalud;


	public PaquetePortafolioDto getPaquetePortafolio() {
		return paquetePortafolio;
	}
	public void setPaquetePortafolio(PaquetePortafolioDto paquetePortafolio) {
		this.paquetePortafolio = paquetePortafolio;
	}
	public ServicioSaludDto getServicioSalud() {
		return servicioSalud;
	}
	public void setServicioSalud(ServicioSaludDto servicioSalud) {
		this.servicioSalud = servicioSalud;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((paquetePortafolio == null) ? 0 : paquetePortafolio.hashCode());
		result = prime * result + ((servicioSalud == null) ? 0 : servicioSalud.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PaquetePortafolioServicioSaludPk other = (PaquetePortafolioServicioSaludPk) obj;
		if (paquetePortafolio == null) {
			if (other.paquetePortafolio != null)
				return false;
		} else if (!paquetePortafolio.equals(other.paquetePortafolio))
			return false;
		if (servicioSalud == null) {
			if (other.servicioSalud != null)
				return false;
		} else if (!servicioSalud.equals(other.servicioSalud))
			return false;
		return true;
	}




}
