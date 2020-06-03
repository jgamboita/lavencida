package co.conexia.negociacion.services.negociacion.control;

import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.logfactory.Log;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TecnologiaNegociacionControl {
    @Inject
    private ProcedimientoNegociacionControl procedimientoNegociacionControl;

    @Inject
    private MedicamentoNegociacionControl medicamentoNegociacionControl;

    @Inject
    private PaqueteNegociacionControl paqueteNegociacionControl;

    @Inject
    private Log log;

    public void copiarTecnologiasPorNegocacion(NegociacionDto negociacionActual, NegociacionDto negociacionAnterior) {
        try {
            this.procedimientoNegociacionControl.copiarTecnologias(negociacionActual, negociacionAnterior);
            this.medicamentoNegociacionControl.copiarTecnologias(negociacionActual, negociacionAnterior);
            this.paqueteNegociacionControl.copiarTecnologias(negociacionActual, negociacionAnterior);
        } catch (CopiarTecnologiaException e) {
            this.log.info("Se revierte la inserción de las tecnologias");
            this.procedimientoNegociacionControl.revertirCopiadoTecnologias(negociacionActual, negociacionAnterior);
            this.medicamentoNegociacionControl.revertirCopiadoTecnologias(negociacionActual, negociacionAnterior);
            this.paqueteNegociacionControl.revertirCopiadoTecnologias(negociacionActual, negociacionAnterior);
        }
    }

}
