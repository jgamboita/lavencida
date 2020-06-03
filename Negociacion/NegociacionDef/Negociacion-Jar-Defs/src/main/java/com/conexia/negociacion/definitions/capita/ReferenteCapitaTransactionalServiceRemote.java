package com.conexia.negociacion.definitions.capita;

import java.math.BigDecimal;

/**
 * Interface remota transactional para el boundary de referente cápita
 * @author mcastro
 *
 */
public interface ReferenteCapitaTransactionalServiceRemote {
    
    Integer actualizarValorPromedioLiquidacionZona(final BigDecimal valorPromedio, final Long liquidacionZonaId);
    
    Integer actualizarValorDistribucion(final BigDecimal valor, final BigDecimal porcentaje, final Long upcDistribucionId);
    
    Integer actualizarValorCategoriaMedicamento(final BigDecimal valor, final BigDecimal porcentaje, final Long catMedicamentoId);
    
    Integer actualizarValorLiquidacionServicio(final BigDecimal valor, final BigDecimal porcentaje, final Long liqServicioId);
    
    Integer actualizarValorProcedimientosPorServicioId(final BigDecimal valor, final BigDecimal porcentaje, final Long liquidacionServicioId);
    
    Integer actualizarValorProcedimientoPorId(final BigDecimal valor, final BigDecimal porcentaje, final Long procedimientoId);

    Integer actualizarValorPrcAsignadoCategoriaMedicamento(final BigDecimal porcentajeAsignado, final Long id);
    
    Integer actualizarProcedimientoPorId(BigDecimal porcentajeAsignado, Long procedimientoId);

    Integer actualizarPorcentajeNegociadoServicio(BigDecimal porcentaje, Long liqServicioId);
    
    Integer actualizarPorAsigPorServicioId(final BigDecimal porcentajeAsignado, final Long liquidacionServicioId);
	
}
