package co.conexia.negociacion.services.negociacion.medicamento.control;

import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;

public class ObtenerMedicamentosNegociadosSinValores implements ObtenerMedicamentosNegociados {

    private ObtenerMedicamentosNegociadosSinValoresEvento obtenerMedicamentosNegociadosSinValoresEvento;

    private ObtenerMedicamentosNegociadosSinValoresRiasCapita obtenerMedicamentosNegociadosSinValoresRiasCapita;

    @Inject
    public ObtenerMedicamentosNegociadosSinValores(ObtenerMedicamentosNegociadosSinValoresEvento obtenerMedicamentosNegociadosSinValoresEvento,
                                                   ObtenerMedicamentosNegociadosSinValoresRiasCapita obtenerMedicamentosNegociadosSinValoresRiasCapita) {
        this.obtenerMedicamentosNegociadosSinValoresEvento = obtenerMedicamentosNegociadosSinValoresEvento;
        this.obtenerMedicamentosNegociadosSinValoresRiasCapita = obtenerMedicamentosNegociadosSinValoresRiasCapita;
    }

    public List<ErroresTecnologiasDto> obtenerMedicamentos(NegociacionDto negociacion) {
        switch (negociacion.getTipoModalidadNegociacion()) {
            case EVENTO:
                return obtenerMedicamentosNegociadosSinValoresEvento.obtenerMedicamentos(negociacion);
            case RIAS_CAPITA:
                return obtenerMedicamentosNegociadosSinValoresRiasCapita.obtenerMedicamentos(negociacion);
            default:
                return Collections.emptyList();
        }
    }

}
