package com.conexia.negociacion.definitions.negociacion.procedimientoPgp;

import com.conexia.contratacion.commons.constants.enums.ErrorTecnologiasNegociacionPgpEnum;
import com.conexia.contratacion.commons.dto.negociacion.ArchivoTecnologiasNegociacionEventoDto;
import com.conexia.contratacion.commons.dto.negociacion.ArchivoTecnologiasNegociacionPgpDto;
import com.conexia.contratacion.commons.dto.negociacion.ArchivoTecnologiasNegociacionRiasCapitaDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasEventoDto;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasRIasCapitaDto;
import com.conexia.exceptions.ConexiaBusinessException;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Interface para importar procedimientos en negociación PGP
 *
 * @author clozano
 */
public interface NegociacionProcedimientoPgpTransactionalServiceRemote extends Serializable {

    List<ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum>> validarProcedimientoNegociacionPgp(List<ArchivoTecnologiasNegociacionPgpDto> procedimientos, NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException;

    List<ErroresImportTecnologiasEventoDto> validarProcedimientoNegociacionEvento(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos, NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException;

    List<ErroresImportTecnologiasRIasCapitaDto> validarProcedimientoNegociacionRiasCapita(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, NegociacionDto negociacion, Integer userId);

}
