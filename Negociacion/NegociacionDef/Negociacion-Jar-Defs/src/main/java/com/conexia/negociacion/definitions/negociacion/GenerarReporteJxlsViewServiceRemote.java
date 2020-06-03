package com.conexia.negociacion.definitions.negociacion;

import com.conexia.contratacion.commons.constants.enums.ReportesNegociacionXlsENum;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioDto;

import java.io.IOException;


public interface GenerarReporteJxlsViewServiceRemote {

	String generarProcedimiento(ReportesNegociacionXlsENum xlsProcedimientosPgp, Long id,
                                AnexoTarifarioDto anexoTarifarioDto, String filename, Boolean esOtroSi, Long negociacionPadreId) throws IOException;

	String generarMedicamentos(ReportesNegociacionXlsENum xlsMedicamentosPgp, Long id,
                               AnexoTarifarioDto anexoTarifarioDto, String filename,Boolean esOtroSi,Long negociacionPadreId) throws IOException;

	String generarPaquete(ReportesNegociacionXlsENum xlsPaqueteEvento, Long id, AnexoTarifarioDto anexoTarifarioDto,
                          String filename,Boolean esOtroSi,Long negociacionPadreId) throws IOException;


}
