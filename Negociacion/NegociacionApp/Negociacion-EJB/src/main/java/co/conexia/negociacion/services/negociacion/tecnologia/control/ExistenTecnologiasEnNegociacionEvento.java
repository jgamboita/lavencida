package co.conexia.negociacion.services.negociacion.tecnologia.control;

import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

import javax.inject.Inject;

public class ExistenTecnologiasEnNegociacionEvento {

    private ExistenProcedimientosCriteria existenProcedimientosCriteria;

    private ExistenPaquetesCriteria existenPaquetesCriteria;

    private ExistenMedicamentosCriteria existenMedicamentosCriteria;

    @Inject
    public ExistenTecnologiasEnNegociacionEvento(ExistenProcedimientosCriteria existenProcedimientosCriteria,
                                                 ExistenPaquetesCriteria existenPaquetesCriteria,
                                                 ExistenMedicamentosCriteria existenMedicamentosCriteria) {
        this.existenProcedimientosCriteria = existenProcedimientosCriteria;
        this.existenPaquetesCriteria = existenPaquetesCriteria;
        this.existenMedicamentosCriteria = existenMedicamentosCriteria;
    }

    public boolean existenTecnologias(NegociacionDto negociacion) {
        return crearCondiciones(negociacion);
    }

    protected boolean crearCondiciones(NegociacionDto negociacion) {
        Long cantidadProcedimientos = existenProcedimientosCriteria.obtenerCantidadTecnologias(negociacion);
        Long cantidadMedicamentos = existenMedicamentosCriteria.obtenerCantidadTecnologias(negociacion);
        Long cantidadPaquetes = existenPaquetesCriteria.obtenerCantidadTecnologias(negociacion);
        return cantidadProcedimientos > 0 | cantidadMedicamentos > 0 | cantidadPaquetes > 0;
    }
}
