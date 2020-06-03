package co.conexia.negociacion.services.negociacion.control;

import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

public interface CopiarTecnologias {

    void copiarTecnologias(NegociacionDto negociacionActual, NegociacionDto negociacionAnterior) throws CopiarTecnologiaException;

    void revertirCopiadoTecnologias(NegociacionDto negociacionActual, NegociacionDto negociacionAnterior);
}
