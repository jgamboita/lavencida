package co.conexia.negociacion.services.negociacion.paquete.control;

import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObtenerPaquetesNegociadosConErroresControl {

    @Inject
    private ObtenerPaquetesNegociadosSinValores paquetesNegociadosSinValores;

    @Inject
    private ObtenerPaquetesNegociadosSinContenido paquetesNegociadosSinContenido;

    @Inject
    private ObtenerPaquetesNegociadosConTecnologiasSinCantidades paquetesNegociadosConTecnologiasSinCantidades;

    @Inject
    private ObtenerPaquetesNegociadosConTecnologiasInactivas paquetesNegociadosConTecnologiasInactivas;

    public List<ErroresTecnologiasDto> obtenerPaquetes(NegociacionDto negociacion) {
        return Stream.of(paquetesNegociadosSinValores.obtenerPaquetes(negociacion),
                paquetesNegociadosSinContenido.obtenerPaquetes(negociacion),
                paquetesNegociadosConTecnologiasSinCantidades.obtenerPaquetes(negociacion),
                paquetesNegociadosConTecnologiasInactivas.obtenerPaquetes(negociacion))
                .flatMap(Collection::parallelStream)
                .collect(Collectors.toList());
    }
}
