package com.conexia.contractual.definitions.view.matriz;

import com.conexia.contratacion.commons.dto.contractual.legalizacion.DescuentoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.AreaInfluenciaDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.matriz.FiltroMatrizDto;

import java.util.List;

/**
 * Interaface para los boundaries view de MatrizContratacion
 * 
 * @author jjoya
 *
 */
public interface MatrizContratacionViewRemote {
    
    List<AreaInfluenciaDto> listarAreaPorSedeContrato(Long sedeContratoId);
    
    List<DescuentoDto> listarDescuentoPorContrato(
            LegalizacionContratoDto legalizacionContrato);
    
    List<SedePrestadorDto> listarSedesPorContrato(
            LegalizacionContratoDto legalizacionContrato);
    
    List<LegalizacionContratoDto> listarContratosByFiltros(
            FiltroMatrizDto filtroDto);

}
