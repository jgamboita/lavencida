package co.conexia.negociacion.services.negociacion.tecnologia.control;

import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

import javax.inject.Inject;

public class ExistenTecnologiasEnNegociacionControl {

    @Inject
    private ExistenTecnologiasEnNegociacionEvento existenTecnologiasEnNegociacionEvento;

    @Inject
    private ExistenTecnologiasEnNegociacionRiasCapita existenTecnologiasEnNegociacionRiasCapita;

    public boolean existenTecnologias(NegociacionDto negociacion) {
        switch (negociacion.getTipoModalidadNegociacion()) {
            case EVENTO:
                return existenTecnologiasEnNegociacionEvento.existenTecnologias(negociacion);
            case RIAS_CAPITA:
                return existenTecnologiasEnNegociacionRiasCapita.existenTecnologias(negociacion);
            default:
                return false;
        }
    }
}
