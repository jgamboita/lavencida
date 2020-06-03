package co.conexia.negociacion.wap.facade.detalle;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.maestros.TarifaPropuestaProcedimientoDto;
import com.conexia.negociacion.definitions.negociacion.servicio.NegociacionServicioViewServiceRemote;
import com.conexia.servicefactory.CnxService;

public class NegociacionProcedimientoTarifaPropuestaFacade  implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 538996541913316991L;
    
    @Inject
    @CnxService
    private NegociacionServicioViewServiceRemote negociacionServicioService;

    
    public List<TarifaPropuestaProcedimientoDto> consultarTarifasPropuestasProcedimiento(Long negociacionId, Long servicioId) {
        return negociacionServicioService.consultarTarifasPropuestasProcedimiento(negociacionId, servicioId);
    }
}
