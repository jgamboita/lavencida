package com.conexia.contratacion.portafolio.definitions.transactional.prestador;

import java.io.Serializable;
import java.util.Map;

import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;
import com.conexia.exceptions.ConexiaBusinessException;

public interface ServicioHabilitacionPortafolioTransactionalRemote extends
		Serializable {

	public void cambiarEstadoServicioHabilitacion(
			ServicioPortafolioSedeDto servicioModificado)
			throws ConexiaBusinessException;

	public void cambiarEstadoTodosServicioHabilitacionPortafolio(
			PortafolioSedeDto portafolio, Map<String, Object> filters)
			throws ConexiaBusinessException;
}
