package com.conexia.negociacion.definitions.bandeja.rias;

import java.util.List;

import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.RangoPoblacionDto;
import com.conexia.contratacion.commons.dto.maestros.RegionalDto;
import com.conexia.contratacion.commons.dto.maestros.RiaDto;

public interface BandejaConsultaRiasViewServiceRemote {

	List<RiaDto> buscarRias(List<RiaDto> rias,DepartamentoDto departamento, MunicipioDto municipio, RegionalDto regional);
}
