package co.conexia.negociacion.wap.facade.bandeja.rias;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.RangoPoblacionDto;
import com.conexia.contratacion.commons.dto.maestros.RegionalDto;
import com.conexia.contratacion.commons.dto.maestros.RiaDto;
import com.conexia.negociacion.definitions.bandeja.rias.BandejaConsultaRiasViewServiceRemote;
import com.conexia.servicefactory.CnxService;

public class BandejaConsultaRiasFacade implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@Inject
	@CnxService
	private BandejaConsultaRiasViewServiceRemote riasRemote;

	public List<RiaDto> buscarRias (List<RiaDto> rias, DepartamentoDto departamentoId, MunicipioDto municipioId,RegionalDto regional ){
		return riasRemote.buscarRias(rias, departamentoId, municipioId, regional);
	}
}
