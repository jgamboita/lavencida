package com.conexia.contractual.wap.controller.parametrizacion;

import java.util.List;

import javax.enterprise.inject.Default;

import com.conexia.contractual.wap.facade.legalizacion.ParametrizacionMinutaFacade;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;

/**
 * Clase encargada de la parametrizacion de contratos de forma asincrona
 * @author icruz
 *
 */
@Default
public class ParametrizacionAsincronaRunnable implements Runnable{
	
	private ParametrizacionMinutaFacade parametrizacionMinutaFacade;
	private SolicitudContratacionParametrizableDto solicitudContratacionParametrizableDto;
	private Integer userId;
	private List<Long> listSedesNegociacion;
	
	public ParametrizacionAsincronaRunnable(SolicitudContratacionParametrizableDto solicitudContratacionParametrizableDto, 
			Integer userId, List<Long> listSedesNegociacion, ParametrizacionMinutaFacade parametrizacionMinutaFacade){
		this.solicitudContratacionParametrizableDto = solicitudContratacionParametrizableDto;
		this.userId = userId;
		this.listSedesNegociacion = listSedesNegociacion;
		this.parametrizacionMinutaFacade = parametrizacionMinutaFacade;
	}		
	
	@Override
	public void run() {
		for(Long sedeNegociacionId : listSedesNegociacion){
			this.parametrizacionMinutaFacade.crearSolicitudContratacion(this.solicitudContratacionParametrizableDto, 
				userId, sedeNegociacionId, 1);
		}		
	}

}
