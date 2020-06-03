package com.conexia.contractual.definitions.view.legalizacion;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.TipoResponsableEnum;
import com.conexia.contractual.model.contratacion.portafolio.PaquetePortafolio;
import com.conexia.contractual.model.maestros.Medicamento;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.*;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.AreaInfluenciaDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.AreaCoberturaDTO;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.IncentivoModeloDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ObjetoContractualDto;
import com.conexia.exceptions.ConexiaBusinessException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Remote de legalizacion contrato.
 * @author jalvarado
 */
public interface LegalizacionContratoViewRemote {

    /**
     * Funcion que permite listar los municipios del contrato.
     * @param contrato
     * @return
     */
    List<AreaInfluenciaDto> listarMunicipiosContrato(ContratoDto contrato);

    /**
     * Funcion que permite listar los responsables del contrato.
     * @param idRegional
     * @param tipoResponsableEnum
     * @return
     */
    List<ResponsableContratoDto> listarResponsablesContrato(final Integer idRegional,
            final TipoResponsableEnum tipoResponsableEnum);

    /**
     * Buscar legalizacion del contrato.
     * @param solicitud
     * @return
     * @throws com.conexia.exceptions.ConexiaBusinessException
     */
    LegalizacionContratoDto buscarLegalizacionContrato(final SolicitudContratacionParametrizableDto solicitud)
            throws ConexiaBusinessException;

    /**
     * Funcion que permite retornar el dto de la minuta a generar para una terminada legalizacion de contrato.
     * @param idLegalizacionContrato
     * @return
     * @throws ConexiaBusinessException
     */
    GeneracionMinutaDto generacionMinutaPorId(final Long idLegalizacionContrato, Integer regionalId, final Long negociacionId) throws ConexiaBusinessException;

    List<IncentivoModeloDto> listarIncentivosPorNegociacionId(final Long negociacionId);

    NegociacionDto consultarNegociacionById(final Long negociacionId);

    List<IncentivoModeloDto> listarModelosPorNegociacionId(Long negociacionId);

    List<ObjetoContractualDto> listarObjetoCapitaPorNegociacionId(Long negociacionId);

    List<ObjetoContractualDto>listarObjetoRiaCapitaResumidoPorNegociacionId(Long negociacionId);

    List<ObjetoContractualDto> listarObjetoEventoPorNegociacionId(Long negociacionId) throws ConexiaBusinessException;

    BigDecimal sumPorcentajeTotalTemaServiciosPyp (final Long negociacionId);

    BigDecimal sumPorcentajeTotalTemaServiciosRecuperacion (final Long negociacionId);

    BigDecimal sumPorcentajeTotalTemaMedicamentosPyp (final Long negociacionId);

    BigDecimal sumPorcentajeTotalTemaMedicamentosRecuperacion (final Long negociacionId);

    SecuenciaContratoDto secuenciaPrestador(ContratoDto contrato);

    /**
     * Busca legalización por Número Negociación, regimen y modalidad
     * @param numeroNegociacion
     * @param regimenNegociacionEnum
     * @param negociacionModalidadEnum
     * @return
     * @throws ConexiaBusinessException
     */
    LegalizacionContratoDto buscarLegalizacionByNroNegociacionAndRegimenAndModalidad(Long numeroNegociacion, RegimenNegociacionEnum regimenNegociacionEnum, NegociacionModalidadEnum negociacionModalidadEnum) throws ConexiaBusinessException;

    List<ProcedimientoDto> buscarProcedimientosNoCompletadosPorcontrato(LegalizacionContratoDto contrato);

    List<Medicamento> buscarMedicamentosNoCompletadosPorcontrato(LegalizacionContratoDto contrato);

    List<PaquetePortafolio> buscarPaquetesSinContenidoPorcontrato(LegalizacionContratoDto contrato);

    List<PaquetePortafolio> buscarPaquetesNoCompletadosPorcontrato(LegalizacionContratoDto contrato);

    List<PaquetePortafolio> buscarPaquetesSinCodEmsanarPorcontrato(LegalizacionContratoDto contrato);

    List<ProcedimientoDto> buscarProcedimientosSinValorContratoRia(LegalizacionContratoDto contrato);

    List<Medicamento> buscarMedicamentosSinValorContratoRia(LegalizacionContratoDto contrato);

    Double sumPorcentajePypRiaCapita(Long negociacionId);

    Double sumPorcentajeRecuperacionRiaCapita(Long negociacionId);

    BigDecimal consultaValorTotal(final Long negociacionId);

    Long conteoLegalizaciones(Long negociacionId);

    BigDecimal consultarValorTotalRia(final Long negociacionId);

    Boolean validarCambioFechaContrato(Long negociacionId);
    
  List<AreaCoberturaDTO> listarAreaCobertura(ContratoDto contrato);

  Integer getPoblacionContratoMunicip(String numeroContrato,
            Integer municipioId);

    List<LegalizacionContratoDto> consultarContratoPermanente(Long idPrestador, RegimenNegociacionEnum regimenNegociacionEnum, NegociacionModalidadEnum negociacionModalidadEnum,SolicitudContratacionParametrizableDto sol);

    List<MinutaDetalleDto> consultarClausulasYparagrafos(Long minutaId);

    List<MinutaDetalleDto> consultarClausulasParagrafosEditados(Long legalizacionContratoId);

    List<PaquetePortafolio> buscarPaquetesSinContenidoPorNegociacion(LegalizacionContratoDto contrato);

    List<PaquetePortafolio> buscarPaquetesNoCompletadosPorNegociacion(LegalizacionContratoDto contrato);
}