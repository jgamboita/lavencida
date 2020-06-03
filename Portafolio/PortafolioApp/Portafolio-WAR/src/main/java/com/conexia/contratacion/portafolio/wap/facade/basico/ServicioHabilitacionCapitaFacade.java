package com.conexia.contratacion.portafolio.wap.facade.basico;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.ServicioHabilitacionPortafolioTransactionalRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.ServicioHabilitacionPortafolioViewRemote;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.servicefactory.CnxService;

public class ServicioHabilitacionCapitaFacade implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5973423822118557490L;

	@Inject
	@CnxService
	private ServicioHabilitacionPortafolioViewRemote servicioHabilitacionPortafolioView;

	@Inject
	@CnxService
	private ServicioHabilitacionPortafolioTransactionalRemote servicioHabilitacionPortafolioTransactional;

	public int contarServiciosHabilitacionPortafolio(
			PortafolioSedeDto portafolio, Map<String, Object> filters)
			throws ConexiaBusinessException {
		return servicioHabilitacionPortafolioView
				.contarServiciosHabilitacionPortafolio(portafolio, filters);
	}

	public List<ServicioPortafolioSedeDto> listarServiciosHabilitacionPortafolio(
			PortafolioSedeDto portafolio, int first, int pageSize,
			Map<String, Object> filters) throws ConexiaBusinessException {

		return servicioHabilitacionPortafolioView
				.listarServiciosHabilitacionPortafolio(portafolio, first,
						pageSize, filters);
	}

	public void cambiarEstadoServicioHabilitacion(
			ServicioPortafolioSedeDto servicioModificado)
			throws ConexiaBusinessException {

		servicioHabilitacionPortafolioTransactional
				.cambiarEstadoServicioHabilitacion(servicioModificado);
	}

	public void cambiarEstadoTodosServicioHabilitacionPortafolio(
			PortafolioSedeDto portafolio, Map<String, Object> filters)
			throws ConexiaBusinessException {

		servicioHabilitacionPortafolioTransactional
				.cambiarEstadoTodosServicioHabilitacionPortafolio(portafolio,
						filters);
	}

	public Map<String, Boolean> obtenerModalidadServicio(
			ServicioPortafolioSedeDto servicioPortafolioSede)
			throws ConexiaBusinessException {
		return servicioHabilitacionPortafolioView
				.obtenerModalidadServicio(servicioPortafolioSede);
	}

	public String calcularComplejidadServicio(
			ServicioPortafolioSedeDto servicioPortafolioSede)
			throws ConexiaBusinessException {

		return servicioHabilitacionPortafolioView
				.calcularComplejidadServicio(servicioPortafolioSede);
	}

}
