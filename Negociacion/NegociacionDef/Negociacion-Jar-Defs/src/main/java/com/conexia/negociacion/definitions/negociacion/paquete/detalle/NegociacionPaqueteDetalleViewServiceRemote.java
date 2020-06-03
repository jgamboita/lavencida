package com.conexia.negociacion.definitions.negociacion.paquete.detalle;

import com.conexia.contratacion.commons.dto.MedicamentoPortafolioDto;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.*;
import com.conexia.contratacion.commons.dto.maestros.*;

import java.util.List;

/**
 * Interface remota para el boundary view de paquete detalle
 *
 * @author jjoya
 */
public interface NegociacionPaqueteDetalleViewServiceRemote {

    List<SedeNegociacionPaqueteMedicamentoDto> listarMedicamentosPorPaqueteIdAndNegociacionId(final String codigoPaquete, final Long negociacionId, final Long paqueteId);

    PaquetePortafolioDto consultarInformacionBasicaByPaqueteId(Long paqueteId);

    List<ServicioSaludDto> consultaServiciosOrigenPaqueteId(Long portafolioId, Long negociacionId);

    List<SedeNegociacionPaqueteProcedimientoDto> listarProcedimientosPorPaqueteIdAndNegociacionId(final SedePrestadorDto codigoPaquete, final Long negociacionId, final Long paqueteId);

    List<SedeNegociacionPaqueteInsumoDto> listarInsumosPorPaqueteIdAndNegociacionId(String codigoPaquete, Long negociacionId, Long paqueteId);

    List<SedeNegociacionPaqueteProcedimientoDto> listarProcedimientosPaquetePrestador(final String codigoPaquete, SedePrestadorDto sedePrestadorSeleccionada, final Long paqueteId, final Long prestadorId, final Long negociacionId);

    List<SedeNegociacionPaqueteMedicamentoDto> listarMedicamentosPaquetePrestador(final String codigoPaquete, final Long paqueteId, final Long prestadorId, final Long negociacionId);

    List<MedicamentoPortafolioDto> listarMedicamentosPaqueteByPrestador(final String codigoPaquete, final Long paqueteId, final Long prestadorId, final Long negociacionId);

    List<SedeNegociacionPaqueteInsumoDto> listarInsumosPaquetePrestador(final String codigoPaquete, final Long paqueteId, final Long prestadorId, final Long negociacionId);

    List<SedeNegociacionPaqueteInsumoDto> listarInsumosPaqueteByPrestador(final String codigoPaquete, final Long paqueteId, final Long prestadorId, final Long negociacionId);

    List<SedeNegociacionPaqueteObservacionDto> observacionPaquetePrestador(Long negociacionId, Long paqueteId);

    List<SedeNegociacionPaqueteExclusionDto> exclusionPaquetePrestador(Long negociacionId, Long paqueteId);

    List<SedeNegociacionPaqueteCausaRupturaDto> causaRupturatoPaquetePrestador(Long negociacionId, Long paqueteId);

    List<SedeNegociacionPaqueteRequerimientoDto> requerimientoPaquetePrestador(Long negociacionId, Long paqueteId);

    List<MedicamentosDto> listarMedicamentos(MedicamentosDto medicamento, List<String> codigosNoPermitidos);

    List<InsumosDto> listarInsumos(InsumosDto insumo, List<String> codigosNoPermitidos);

    List<ProcedimientoDto> listarProcedimientos(ProcedimientoDto procedimiento, List<String> codigosNoPermitidos);

    List<CategoriaMedicamentoDto> listarCategoriasMedicamento();
}
