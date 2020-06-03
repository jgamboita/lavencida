package co.conexia.negociacion.services.negociacion.procedimiento.control;

import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObtenerProcedimientosNegociadosConErroresControl {

    @Inject
    private ObtenerProcedimientosNegociadosSinValores paquetesNegociadosSinValores;

    @Inject
    private ObtenerProcedimientosNegociadosConTecnologiasInactivas paquetesNegociadosConTecnologiasInactivas;

    @Inject
    private ObtenerProcedimientosNegociadosConValoresDiferentes procedimientosNegociadosConValoresDiferentes;

    public List<ErroresTecnologiasDto> obtenerProcedimientos(NegociacionDto negociacion) {
        return Stream.of(paquetesNegociadosSinValores.obtenerProcedimientos(negociacion),
                paquetesNegociadosConTecnologiasInactivas.obtenerProcedimientos(negociacion),
                procedimientosNegociadosConValoresDiferentes.obtenerProcedimientos(negociacion))
                .flatMap(Collection::parallelStream)
                .collect(Collectors.toList());
    }
}
