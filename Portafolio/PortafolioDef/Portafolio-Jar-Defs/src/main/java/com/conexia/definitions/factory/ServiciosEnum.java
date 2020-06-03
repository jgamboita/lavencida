package com.conexia.definitions.factory;

import com.conexia.contratacion.portafolio.definitions.commons.CommonViewServiceRemote;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.MedicamentoPortafolioTransactionalRemote;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.PrestadorTransactionalServiceRemote;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.ProcedimientoServicioPortafolioTransactionalRemote;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.SedePrestadorTransactionalRemote;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.ServicioHabilitacionPortafolioTransactionalRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.MedicamentoPortafolioViewRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.PrestadorViewServiceRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.ProcedimientoServicioPortafolioViewRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.SedePrestadorViewRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.ServicioHabilitacionPortafolioViewRemote;
import com.conexia.definitions.view.BeanViewRemote;

/**
 * Created by pbastidas on 8/04/15.
 */
public enum ServiciosEnum {

	// TODO: crear enums con los valores correctos
	VIEW_BEAN(BeanViewRemote.class, "BeanViewBoundary"), 
	VIEW_COMMON_SERVICE(
			CommonViewServiceRemote.class, "CommonViewBoundary"), 
	VIEW_PRESTADOR_SERVICE(
			PrestadorViewServiceRemote.class, "PrestadorViewBoundary"),
	VIEW_SEDE_PRESTADOR_SERVICE(
			SedePrestadorViewRemote.class, "SedePrestadorViewBoundary"), 
	VIEW_SERVICIO_HABILITACION_PORTAFOLIO_SERVICE(
			ServicioHabilitacionPortafolioViewRemote.class,
			"ServicioHabilitacionPortafolioViewBoundary"),
	VIEW_PROCEDIMIENTO_SERVICIO_PORTAFOLIO_SERVICE(
			ProcedimientoServicioPortafolioViewRemote.class,
			"ProcedimientoServicioPortafolioViewBoundary"),
	VIEW_MEDICAMENTO_PORTAFOLIO_SERVICE(
			MedicamentoPortafolioViewRemote.class,
			"MedicamentoPortafolioViewBoundary"),
			
	TRANSACCIONAL_SEDE_PRESTADOR_SERVICE(
			SedePrestadorTransactionalRemote.class,
			"SedePrestadorTransactionalBoundary"), 
	TRANSACCIONAL_PRESTADOR_SERVICE(
			PrestadorTransactionalServiceRemote.class,
			"PrestadorTransactionalBoundary"), 
	TRANSACCIONAL_SERVICIO_HABILITACION_PORTAFOLIO_SERVICE(
			ServicioHabilitacionPortafolioTransactionalRemote.class,
			"ServicioHabilitacionPortafolioTransactionalBoundary"),
	TRANSACCIONAL_PROCEDIMIENTO_SERVICIO_PORTAFOLIO_SERVICE(
			ProcedimientoServicioPortafolioTransactionalRemote.class,
			"ProcedimientoServicioPortafolioTransactionalBoundary"),
	TRANSACCIONAL_MEDICAMENTO_PORTAFOLIO_SERVICE(
			MedicamentoPortafolioTransactionalRemote.class,
			"MedicamentoPortafolioTransactionalBoundary"),;

	public static final String MODULE_NAME = "portafolio-ejb";
	// Estructura del enum
	private final String localBusinessName;
	private final Class remoteClazz;
	private final String beanName;

	ServiciosEnum(Class remoteClazz, String beanName) {
		this.localBusinessName = "";
		this.remoteClazz = remoteClazz;
		this.beanName = beanName;
	}

	public String getLocalBusinessName() {
		return localBusinessName;
	}

	public Class getRemoteClazz() {
		return remoteClazz;
	}

	public String getBeanName() {
		return beanName;
	}

	public String getAppName() {
		return "portafolio";
	}
}
