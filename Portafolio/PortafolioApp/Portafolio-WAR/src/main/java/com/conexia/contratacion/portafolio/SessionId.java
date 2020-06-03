package com.conexia.contratacion.portafolio;

import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.capita.CategoriaMedicamentoPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.OfertaPrestadorDto;
import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;

public interface SessionId {
	static final String PRESTADOR_ID = PrestadorDto.class.getSimpleName();
    static final String SEDE_PRESTADOR_ID = SedePrestadorDto.class.getSimpleName();
	static final String OFERTA_PRESTADOR_ID = OfertaPrestadorDto.class.getSimpleName();
	static final String PORTAFOLIO_SEDE_ID = PortafolioSedeDto.class.getSimpleName();
	static final String SERVICIO_PORTAFOLIO_SEDE_ID = ServicioPortafolioSedeDto.class.getSimpleName();
	static final String CATEGORIA_MEDICAMENTO_ID = CategoriaMedicamentoPortafolioSedeDto.class.getSimpleName();
}
