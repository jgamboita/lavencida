package com.conexia.negociacion.definitions.negociacion.medicamentoPgp;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import com.conexia.contratacion.commons.constants.enums.ErrorTecnologiasNegociacionEventoEnum;
import com.conexia.contratacion.commons.constants.enums.ErrorTecnologiasNegociacionPgpEnum;
import com.conexia.contratacion.commons.constants.enums.ErrorTecnologiasNegociacionRiasCapitaEnum;
import com.conexia.contratacion.commons.dto.negociacion.ArchivoTecnologiasNegociacionEventoDto;
import com.conexia.contratacion.commons.dto.negociacion.ArchivoTecnologiasNegociacionPgpDto;
import com.conexia.contratacion.commons.dto.negociacion.ArchivoTecnologiasNegociacionRiasCapitaDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 * Interface para importar medicamentos en negociaciones PGP
 * @author clozano
 *
 */
public interface NegociacionMedicamentoPgpTransactionalServiceRemote extends Serializable {

	List<ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum>>
		validarMedicamentoNegociacionPgp(List<ArchivoTecnologiasNegociacionPgpDto> medicamentos, NegociacionDto negociacion, Integer userId)
				throws ConexiaBusinessException;

	void clearMaps();

	public List<ConcurrentHashMap<ArchivoTecnologiasNegociacionEventoDto, ErrorTecnologiasNegociacionEventoEnum>>
		validarMedicamentoNegociacionEvento(List<ArchivoTecnologiasNegociacionEventoDto> medicamentos, NegociacionDto negociacion, Integer userId, Boolean importRegulados)
				throws ConexiaBusinessException;

	List<ArchivoTecnologiasNegociacionEventoDto>
		retornarMedicamentosReguladosImport(List<ArchivoTecnologiasNegociacionEventoDto> medicamentosImport)
				throws ConexiaBusinessException;
}
