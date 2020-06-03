package com.conexia.contratacion.portafolio.definitions.view.prestador;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 *
 * @author <a href="dgarcia@conexia.com">David Garcia H</a>
 */
public interface ServicioHabilitacionPortafolioViewRemote extends Serializable {

	int contarServiciosHabilitacionPortafolio(PortafolioSedeDto portafolio,
			Map<String, Object> filters)
			throws ConexiaBusinessException;

	List<ServicioPortafolioSedeDto> listarServiciosHabilitacionPortafolio(
			PortafolioSedeDto portafolio, Integer first, Integer pageSize,
			Map<String, Object> filters) throws ConexiaBusinessException;

	Map<String, Boolean> obtenerModalidadServicio(
			ServicioPortafolioSedeDto servicioPortafolioSede)
			throws ConexiaBusinessException;

	String calcularComplejidadServicio(
			ServicioPortafolioSedeDto servicioPortafolioSede)
			throws ConexiaBusinessException;

}
