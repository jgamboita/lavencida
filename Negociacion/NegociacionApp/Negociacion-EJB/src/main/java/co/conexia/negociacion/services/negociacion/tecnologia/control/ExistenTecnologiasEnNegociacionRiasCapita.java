package co.conexia.negociacion.services.negociacion.tecnologia.control;

import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.logfactory.Log;

import javax.inject.Inject;
import javax.persistence.EntityManager;

public class ExistenTecnologiasEnNegociacionRiasCapita {

    private ExistenProcedimientosCriteria existenProcedimientosCriteria;

    private ExistenMedicamentosCriteria existenMedicamentosCriteria;

    @Inject
    public ExistenTecnologiasEnNegociacionRiasCapita(ExistenProcedimientosCriteria existenProcedimientosCriteria,
                                                     ExistenMedicamentosCriteria existenMedicamentosCriteria) {
        this.existenProcedimientosCriteria = existenProcedimientosCriteria;
        this.existenMedicamentosCriteria = existenMedicamentosCriteria;
    }

    public boolean existenTecnologias(NegociacionDto negociacion) {
        Long cantidadProcedimientos = existenProcedimientosCriteria.obtenerCantidadTecnologias(negociacion);
        Long cantidadMedicamentos = existenMedicamentosCriteria.obtenerCantidadTecnologias(negociacion);
        return cantidadProcedimientos > 0 | cantidadMedicamentos > 0;
    }

}
