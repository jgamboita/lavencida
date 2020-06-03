package co.conexia.negociacion.wap.facade.negociacion;

import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.negociacion.definitions.negociacion.medicamento.NegociacionMedicamentoViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.paquete.NegociacionPaqueteViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.procedimiento.NegociacionProcedimientoViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.tecnologia.NegociacionTecnologiasViewServiceRemote;
import com.conexia.servicefactory.CnxService;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

public class TecnologiasFacade implements Serializable {

    @Inject
    @CnxService
    private NegociacionPaqueteViewServiceRemote negociacionPaqueteViewService;

    @Inject
    @CnxService
    private NegociacionMedicamentoViewServiceRemote negociacionMedicamentoViewServiceRemote;

    @Inject
    @CnxService
    private NegociacionProcedimientoViewServiceRemote negociacionProcedimientoViewServiceRemote;

    @Inject
    @CnxService
    private NegociacionTecnologiasViewServiceRemote negociacionTecnologiasViewServiceRemote;

    public List<ErroresTecnologiasDto> obtenerPaquetesNegociadosConErrores(NegociacionDto negociacion){
        return negociacionPaqueteViewService.obtenerPaquetesNegociadosConErrores(negociacion);
    }

    public List<ErroresTecnologiasDto> obtenerMedicamentosNegociadosConErrores(NegociacionDto negociacion) {
        return negociacionMedicamentoViewServiceRemote.obtenerMedicamentosNegociadosConErrores(negociacion);
    }

    public List<ErroresTecnologiasDto> obtenerProcedimientosNegociadosConErrores(NegociacionDto negociacion) {
        return negociacionProcedimientoViewServiceRemote.obtenerProcedimientosNegociadosConErrores(negociacion);
    }

    public boolean existenTecnologias(NegociacionDto negociacion) {
        return negociacionTecnologiasViewServiceRemote.existenTecnologias(negociacion);
    }
}
