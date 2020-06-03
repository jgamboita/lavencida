package com.conexia.negociacion.definitions.negociacion;

import java.io.IOException;
import java.util.List;

import com.conexia.contratacion.commons.constants.enums.ReportesAnexosNegociacionEnum;
import com.conexia.contratacion.commons.dto.negociacion.ParametrosReporteJxlsDto;

public interface ReporteNegociacionJxlsViewServiceRemote {

	String generarAnexoJxls(ReportesAnexosNegociacionEnum xlsAnexoPoblacionPgp, List<ParametrosReporteJxlsDto> datasource,
			String string) throws IOException;

	

}
