package com.conexia.negociacion.definitions.capita;

import com.conexia.contratacion.commons.dto.negociacion.LiquidacionZonaDto;
import com.conexia.contratacion.commons.dto.negociacion.TecnologiaDistribucionDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcDistribucionDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionCategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionServicioDto;

import java.util.List;

import com.conexia.contratacion.commons.dto.negociacion.ZonaCapitaDto;

import java.math.BigDecimal;

/**
 * Interface remota para el boundary de referente cápita
 * 
 * @author mcastro
 *
 */
public interface ReferenteCapitaViewServiceRemote {

	List<ZonaCapitaDto> consultarTipoUpcByRegimenAndAnio(Integer regimenId, Integer anio);
    
    UpcDto consultarUpc(Integer regimenId, Integer anio, Long zonaCapitaId);
    
    LiquidacionZonaDto consultarLiquidacionZona(Long upcId);
    
    UpcDistribucionDto consultarDistribucion(Long liqZonaId, Long temaCapitaId);
    
    List<UpcLiquidacionCategoriaMedicamentoDto> consultarMedicamentos(final Long liquidacionZonaId);
    
    List<UpcLiquidacionServicioDto> consultarServicios(final Long liquidacionZonaId);
    
    Long contarProcedimientosPorServicioId(final Long liquidacionServicioId);
    
    List<UpcLiquidacionProcedimientoDto> listaProcedimientosPorServicioId(final Long liquidacionServicioId);
    
    UpcLiquidacionCategoriaMedicamentoDto getMedicamentoById(final Long id);
    
    UpcLiquidacionServicioDto getServicioById(final Long id);
    
    UpcLiquidacionProcedimientoDto getProcedimientoById(final Long id);
    
    BigDecimal sumarPorcProcedimientosPorServicioId(final Long liquidacionServicioId);
    
    BigDecimal sumaPorcentajes(final Long liquidacionZonaId);
    
    BigDecimal sumaValores(final Long liquidacionZonaId);

    BigDecimal sumaPorcentajeAsignado(Long id);

    TecnologiaDistribucionDto consultarDistribucionCategoriasMedicamento(Long zonaCapitaId);

}
