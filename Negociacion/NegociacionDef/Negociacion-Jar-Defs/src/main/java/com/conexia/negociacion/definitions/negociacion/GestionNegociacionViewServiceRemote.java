package com.conexia.negociacion.definitions.negociacion;

import java.util.List;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.FiltroAnexoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioDetallePaqueteDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioDetallePrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioMedicamentoDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioMedicamentoPgpDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioPaqueteDinamicoDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioPaqueteDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioProcedimientoPgpDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioTecnologiaPaqueteDto;
import com.conexia.contratacion.commons.dto.negociacion.PaquetePortafolioCausaRupturaDto;
import com.conexia.contratacion.commons.dto.negociacion.PaquetePortafolioExclusionDto;
import com.conexia.contratacion.commons.dto.negociacion.PaquetePortafolioObservacionDto;
import com.conexia.contratacion.commons.dto.negociacion.PaquetePortafolioRequerimientosDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 * Interface remota para el boundary de gestion de negociacion
 *
 * @author jjoya
 *
 */
public interface GestionNegociacionViewServiceRemote {

    /**
     * Metodo de definicion que consulta las negociaciones de un prestador por
     * su id
     *
     * @param prestadorId identificador del prestador
     * @return {@link - NegociacionDto}
     */
    List<NegociacionDto> consultarNegociacionesPrestador(Long prestadorId, NegociacionDto negociacion);

    /**
     * Consulta el detalle del anexoTarifario con los datos del prestador
     *
     * @param negociacionId
     * @return Detalle de {@link AnexoTarifarioDetallePrestadorDto}
     */
    AnexoTarifarioDetallePrestadorDto consultarAnexoTarifarioDetallePrestador(Long negociacionId) throws ConexiaBusinessException;

    /**
     * Consulta los procedimientos que pertenecen a una negociación finalizada
     *
     * @param negociacionId
     * @return Lista de {@link AnexoTarifarioProcedimientoDto}
     */
    List<AnexoTarifarioProcedimientoDto> consultarProcedimientosAnexoTarifario(Long negociacionId, NegociacionModalidadEnum modalidadNegociacion, boolean conRecuperacion, boolean fraccionarDescripcion, Boolean esOtroSi,Long negociacionPadreId) throws ConexiaBusinessException;

    List<AnexoTarifarioProcedimientoDto> consultarProcedimientosAnexoTarifarioOtroSiBase(NegociacionDto negociacionId, boolean conRecuperacion, boolean fraccionarDescripcion) throws ConexiaBusinessException;

    List<AnexoTarifarioProcedimientoDto> consultarProcedimientosAnexoTarifarioOtroSi(NegociacionModalidadEnum modalidadNegociacion, NegociacionDto negociacionDto) throws ConexiaBusinessException;

    List<AnexoTarifarioProcedimientoPgpDto> consultarProcedimientosPgpAnexoTarifario(Long negociacionId) throws ConexiaBusinessException;


    List<PaquetePortafolioExclusionDto> consultarPaquetePortafolioExclusion(String codigoPaquete, Long portafolioId) throws ConexiaBusinessException;

    List<AnexoTarifarioMedicamentoPgpDto> consultarMedicamentosPgpAnexoTarifario(Long negociacionId) throws ConexiaBusinessException;

    /**
     * Consulta los paquetes que pertenecen a una negociación finalizada
     *
     * @param negociacionId
     * @return Lista de {@link AnexoTarifarioPaqueteDto}
     */
    List<AnexoTarifarioPaqueteDto> consultarPaquetesAnexoTarifario(Long negociacionId);



	Boolean tienePaquetes(Long negociacionId);

	Boolean tieneMedicamentos(Long negociacionId, NegociacionModalidadEnum negociacionModalidadEnum);

	Boolean tieneProcedimientos(Long negociacionId, NegociacionModalidadEnum negociacionModalidadEnum);

	Boolean tieneProcedimientosRecuperacion(Long negociacionId);

	Boolean tieneMedicamentosRecuperacion(Long negociacionId);

	Boolean tienePoblacion(Long negociacionId);

    List<AnexoTarifarioMedicamentoDto> consultarMedicamentosAnexoTarifarioOtroSiBase(NegociacionDto negociacion)
            throws ConexiaBusinessException;

    List<AnexoTarifarioMedicamentoDto> consultarMedicamentosAnexoTarifario(FiltroAnexoTarifarioDto filtros,
                                                                           Boolean esOtroSi, Long negociacionPadreId)
            throws ConexiaBusinessException;

    List<AnexoTarifarioMedicamentoDto> consultarMedicamentosAnexoTarifarioOtroSi(FiltroAnexoTarifarioDto filtros,
                                                                                 NegociacionDto negociacionDto)
            throws ConexiaBusinessException;

    List<AnexoTarifarioPaqueteDto> consultarPaquetesAnexoTarifario(Long negociacionId, Boolean esOtroSi,
                                                                   Long negociacionPadreId)
            throws ConexiaBusinessException;

    List<AnexoTarifarioPaqueteDto> consultarPaquetesAnexoTarifarioOtroSi(NegociacionDto negociacionDto)
            throws ConexiaBusinessException;

    List<AnexoTarifarioPaqueteDto> consultarPaquetesAnexoTarifarioOtroSiBase(NegociacionDto negociacion)
            throws ConexiaBusinessException;

    List<AnexoTarifarioTecnologiaPaqueteDto> consultarTecnologiasPaquetesAnexoTarifario(Long negociacionId)
            throws ConexiaBusinessException;

    List<AnexoTarifarioTecnologiaPaqueteDto> consultarPortafolioTecnologiasPaquetesAnexoTarifario(String codigoPaquete,
                                                                                                  Long portafolioId)
            throws ConexiaBusinessException;

    List<AnexoTarifarioDetallePaqueteDto> consultarDetallePaquetesAnexoTarifario(Long negociacionId)
            throws ConexiaBusinessException;

    List<AnexoTarifarioPaqueteDinamicoDto> consultarPaquetesAnexoTarifarioDinamico(Long negociacionId)
            throws ConexiaBusinessException;

    List<AnexoTarifarioPaqueteDinamicoDto> consultarPortafolioPaquetesAnexoTarifarioDinamico(String codigoPaquete)
            throws ConexiaBusinessException;

    List<PaquetePortafolioObservacionDto> consultarPaqueteNegociacionObservacion(Long negociacionId)
            throws ConexiaBusinessException;

    List<PaquetePortafolioObservacionDto> consultarPaquetePortafolioObservacion(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException;

    List<PaquetePortafolioExclusionDto> consultarPaqueteNegociacionExclusion(Long negociacionId)
            throws ConexiaBusinessException;

    List<PaquetePortafolioRequerimientosDto> consultarPaqueteNegociacionRequerimientos(Long negociacionId)
            throws ConexiaBusinessException;

    List<PaquetePortafolioRequerimientosDto> consultarPaquetePortafolioRequerimientos(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException;

    List<PaquetePortafolioCausaRupturaDto> consultarPaqueteNegociacionCausaRuptura(Long negociacionId)
            throws ConexiaBusinessException;

    List<PaquetePortafolioCausaRupturaDto> consultarPaquetePortafolioCausaRuptura(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException;
}
