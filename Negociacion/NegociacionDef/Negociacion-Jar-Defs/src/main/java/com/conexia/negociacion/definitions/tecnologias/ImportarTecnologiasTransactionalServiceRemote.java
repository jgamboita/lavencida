package com.conexia.negociacion.definitions.tecnologias;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TecnologiaEnum;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasRIasCapitaDto;
import com.conexia.exceptions.ConexiaBusinessException;

import java.io.Serializable;
import java.util.List;

public interface ImportarTecnologiasTransactionalServiceRemote extends Serializable {

    List<ErroresImportTecnologiasRIasCapitaDto> importarTecnologia(String nombreArchivo, NegociacionDto negociacion, Integer userId, NegociacionModalidadEnum modalidad, TecnologiaEnum Tecnologia) throws ConexiaBusinessException;
}
