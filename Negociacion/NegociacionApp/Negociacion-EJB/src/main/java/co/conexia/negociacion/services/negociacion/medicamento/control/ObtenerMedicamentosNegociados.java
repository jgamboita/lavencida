package co.conexia.negociacion.services.negociacion.medicamento.control;

import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

import java.util.List;

public interface ObtenerMedicamentosNegociados {
    List<ErroresTecnologiasDto> obtenerMedicamentos(NegociacionDto negociacion);
}
