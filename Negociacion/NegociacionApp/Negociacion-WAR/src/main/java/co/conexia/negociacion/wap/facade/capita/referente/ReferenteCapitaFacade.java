package co.conexia.negociacion.wap.facade.capita.referente;

import com.conexia.contratacion.commons.dto.negociacion.LiquidacionZonaDto;
import com.conexia.contratacion.commons.dto.negociacion.TecnologiaDistribucionDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcDistribucionDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionCategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.UpcLiquidacionServicioDto;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.negociacion.ZonaCapitaDto;
import com.conexia.negociacion.definitions.capita.ReferenteCapitaTransactionalServiceRemote;
import com.conexia.negociacion.definitions.capita.ReferenteCapitaViewServiceRemote;
import com.conexia.servicefactory.CnxService;

import java.math.BigDecimal;

public class ReferenteCapitaFacade implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4523046871374286183L;
	
	@Inject 
	@CnxService
	private ReferenteCapitaViewServiceRemote viewService;
    
    @Inject 
	@CnxService
	private ReferenteCapitaTransactionalServiceRemote transactionalService;
	
	public List<ZonaCapitaDto> consultarTipoUpcByRegimenAndAnio(Integer regimenId, Integer anio) {
		return this.viewService.consultarTipoUpcByRegimenAndAnio(regimenId, anio);
	}
    
    public UpcDto consultarUpc(Integer regimenId, Integer anio, Long zonaCapitaId) {
		return this.viewService.consultarUpc(regimenId, anio, zonaCapitaId);
	}
    
    public LiquidacionZonaDto consultarLiquidacionZona(final Long upcId){
        return this.viewService.consultarLiquidacionZona(upcId);
    }
    
    public UpcDistribucionDto consultarDistribucion(final Long liqZonaId, final Long temaCapitaId){
        return this.viewService.consultarDistribucion(liqZonaId, temaCapitaId);
    }
    
    public UpcLiquidacionCategoriaMedicamentoDto getMedicamentoById(final Long id){
        return this.viewService.getMedicamentoById(id);
    }
    
    public UpcLiquidacionServicioDto getServicioById(final Long id){
        return this.viewService.getServicioById(id);
    }
    
    public UpcLiquidacionProcedimientoDto getProcedimientoById(final Long id){
        return this.viewService.getProcedimientoById(id);
    }
    
    public void actualizarValorPromedioLiquidacionZona(final BigDecimal valorPromedio, final Long liquidacionZonaId){
        this.transactionalService.actualizarValorPromedioLiquidacionZona(valorPromedio, liquidacionZonaId);
    }
    
    public void actualizarValorCategoriaMedicamento(final BigDecimal valor, final BigDecimal porcentaje, final Long catMedicamentoId){
        this.transactionalService.actualizarValorCategoriaMedicamento(valor, porcentaje, catMedicamentoId);
    }
    
    public void actualizarValorLiquidacionServicio(final BigDecimal valor, final BigDecimal porcentaje, final Long liqServicioId){
        this.transactionalService.actualizarValorLiquidacionServicio(valor, porcentaje, liqServicioId);
    }
    
    public void actualizarValorDistribucion(final BigDecimal valorPromedio, final BigDecimal porcentaje, final Long upcDistribucionId){
        this.transactionalService.actualizarValorDistribucion(valorPromedio, porcentaje, upcDistribucionId);
    }
    
    public List<UpcLiquidacionCategoriaMedicamentoDto> consultarMedicamentos(final Long liquidacionZonaId){
        return this.viewService.consultarMedicamentos(liquidacionZonaId);
    }
    
    public List<UpcLiquidacionServicioDto> consultarServicios(final Long liquidacionZonaId){
        return this.viewService.consultarServicios(liquidacionZonaId);
    }
	
    public Long contarProcedimientosPorServicioId(final Long liquidacionServicioId){
        return this.viewService.contarProcedimientosPorServicioId(liquidacionServicioId);
    }
    
    public void actualizarValorProcedimientosPorServicioId(final BigDecimal valor, final BigDecimal porcentaje, final Long liquidacionServicioId){
        this.transactionalService.actualizarValorProcedimientosPorServicioId(valor, porcentaje, liquidacionServicioId);
    }
    
    public void actualizarPorAsigPorServicioId(final BigDecimal porcentajeAsignado, final Long liquidacionServicioId){
        this.transactionalService.actualizarPorAsigPorServicioId(porcentajeAsignado, liquidacionServicioId);
    }
    
    public List<UpcLiquidacionProcedimientoDto> listaProcedimientosPorServicioId(final Long liquidacionServicioId){
        return this.viewService.listaProcedimientosPorServicioId(liquidacionServicioId);
    }
    
    public void actualizarValorProcedimientoPorId(final BigDecimal valor, final BigDecimal porcentaje, final Long procedimientoId){
        this.transactionalService.actualizarValorProcedimientoPorId(valor, porcentaje, procedimientoId);
    }
    
    public BigDecimal sumarPorcProcedimientosPorServicioId(final Long liquidacionServicioId){
        return this.viewService.sumarPorcProcedimientosPorServicioId(liquidacionServicioId);
    }
    
    public BigDecimal sumaPorcentajes(final Long liquidacionZonaId){
        return this.viewService.sumaPorcentajes(liquidacionZonaId);
    }
    
    public BigDecimal sumaValores(final Long liquidacionZonaId){
        return this.viewService.sumaValores(liquidacionZonaId);
    }

    public void actualizarValorPrcAsignadoCategoriaMedicamento(BigDecimal porcentajeAsignado, Long id) {
        this.transactionalService.actualizarValorPrcAsignadoCategoriaMedicamento(porcentajeAsignado, id);
    }

    public BigDecimal sumaPorcentajeAsignado(Long id) {
        return this.viewService.sumaPorcentajeAsignado(id);
    }

    public void actualizarProcedimientoPorId(BigDecimal porcentajeAsignado, Long procedimientoId) {
        this.transactionalService.actualizarProcedimientoPorId(porcentajeAsignado, procedimientoId);
    }

    public void actualizarValorPorcentajeAsignadoServicio(BigDecimal porcentaje, Long liqServicioId) {
        this.transactionalService.actualizarPorcentajeNegociadoServicio(porcentaje, liqServicioId);
    }
    
    public TecnologiaDistribucionDto consultarDistribucionCategoriasMedicamento(Long zonaCapitaId){
        return this.viewService.consultarDistribucionCategoriasMedicamento(zonaCapitaId);
    }
}
