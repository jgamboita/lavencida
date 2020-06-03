package com.conexia.negociacion.definitions.negociacion.medicamento;

import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.exceptions.ConexiaBusinessException;

import java.util.List;

/**
 * Interface remota para el boundary de negociacion para medicamentos
 * negociados.
 *
 * @author jtorres
 */
public interface NegociacionMedicamentoViewServiceRemote {

    List<MedicamentoNegociacionDto> consultaMedicamentosNegociacion(NegociacionDto negociacion);

    List<MedicamentoNegociacionDto> consultarMedicamentosByGrupoAndNegociacionId(Long negociacionId, Long grupoId) throws ConexiaBusinessException;

    List<MedicamentoNegociacionDto> consultaMedicamentosNegociacionCapita(Long negociacionId);

    List<GrupoTerapeuticoNegociacionDto> consultaGruposNegociacionPGP(NegociacionDto negociacion) throws ConexiaBusinessException;

    List<SedesNegociacionDto> consultarSedeNegociacionMedicamentosByNegociacionId(Long negociacionId);

    List<MedicamentosDto> consultarMedicamentosAgregar(MedicamentosDto medicamento, NegociacionDto negociacion) throws ConexiaBusinessException;

    List<SedeNegociacionCategoriaMedicamentoDto> consultarCategoriasNegociacionCapita(Long sedeNegociacionId, Integer regimenId);

    List<MedicamentoNegociacionDto> consultarCategoriasSedesMedicamentosCapita(NegociacionDto negociacion, Long zonaCapitaId);

    boolean tieneServicioFarmaceuticoHabilitado(Long negociacionId);

    Integer contarMedicamentosByNegociacionId(Long negociacionId);

    List<ArchivoTecnologiasNegociacionEventoDto> consultarValorReferenteMedicamentos(List<ArchivoTecnologiasNegociacionEventoDto> medicamentosImportar) throws ConexiaBusinessException;

    List<MedicamentoNegociacionDto> listarMedicamentosReguladosNegociado(Long negociacionId);

    List<ErroresTecnologiasDto> obtenerMedicamentosNegociadosConErrores(NegociacionDto negociacion);
}
