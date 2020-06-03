package co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeindividual;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.negociacion.ProcedimientoNegociacionDto;
import com.conexia.negociacion.definitions.negociacion.servicio.NegociacionServicioViewServiceRemote;
import com.conexia.servicefactory.CnxService;

public class NegociacionServicioProcedimientoSIFacade implements Serializable{

	@Inject
	@CnxService
	private NegociacionServicioViewServiceRemote negociacionServicioService;

    public List<ProcedimientoNegociacionDto> asignarTarifariosSoportados(List<ProcedimientoNegociacionDto> procedimientos) {
    	return negociacionServicioService.asignarTarifariosSoportados(procedimientos);
    }
}
