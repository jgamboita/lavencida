package co.conexia.negociacion.wap.facade.detalle;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.contractual.legalizacion.ProcedimientoContratoDto;
import com.conexia.negociacion.definitions.negociacion.servicio.NegociacionServicioViewServiceRemote;
import com.conexia.servicefactory.CnxService;

public class NegociacionProcedimientoContratoFacade  implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 4374132447104009539L;
    
    @Inject
    @CnxService
    private NegociacionServicioViewServiceRemote negociacionServicioService;
    
    /**
     * Consulta los procedimientos y valores para el contrato anterior por un prestador
     * y un servicio.
     * @param prestadorId
     * @param servicioId 
     * @return Lista de {@link - List<ProcedimientoContratoDto>}
     */
    public List<ProcedimientoContratoDto> consultarProcedimientosContratoAnterior(Long prestadorId, Long servicioId) {
        return negociacionServicioService.consultarProcedimientosContratoAnterior(prestadorId, servicioId);
    }

}
