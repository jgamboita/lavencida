package com.conexia.negociacion.definitions.negociacion.procedimiento;

import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

import java.util.List;

public interface NegociacionProcedimientoViewServiceRemote {
    List<ErroresTecnologiasDto> obtenerProcedimientosNegociadosConErrores(NegociacionDto negociacion);
}
