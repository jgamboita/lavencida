package co.conexia.negociacion.services.negociacion.medicamento.control;

import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

import javax.inject.Inject;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ObtenerMedicamentosNegociadosConErroresControl {

    @Inject
    private ObtenerMedicamentosNegociadosSinValores medicamentosNegociadosSinValores;

    @Inject
    private ObtenerMedicamentosNegociadosConTecnologiasInactivas medicamentosNegociadosConTecnologiasInactivas;

    @Inject
    private ObtenerMedicamentosNegociadosSinRutas medicamentosNegociadosSinRutas;

    public List<ErroresTecnologiasDto> obtenerMedicamentos(NegociacionDto negociacion) {
        return Stream.of(medicamentosNegociadosSinValores.obtenerMedicamentos(negociacion),
                medicamentosNegociadosConTecnologiasInactivas.obtenerMedicamentos(negociacion),
                medicamentosNegociadosSinRutas.obtenerMedicamentos(negociacion))
                .flatMap(Collection::parallelStream)
                .collect(Collectors.toList());
    }

}
