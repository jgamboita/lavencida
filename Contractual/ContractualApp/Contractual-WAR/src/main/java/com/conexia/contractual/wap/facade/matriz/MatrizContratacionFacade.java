package com.conexia.contractual.wap.facade.matriz;

import java.io.Serializable;

import javax.inject.Inject;

import com.conexia.contractual.definitions.view.matriz.MatrizContratacionViewRemote;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.DescuentoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.AreaInfluenciaDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.matriz.FiltroMatrizDto;
import com.conexia.servicefactory.CnxService;

import java.util.List;

public class MatrizContratacionFacade implements Serializable{

    private static final long serialVersionUID = 5151599474610642619L;
    
    @Inject
    @CnxService
    private MatrizContratacionViewRemote matrizContratacionView;
    
    public List<AreaInfluenciaDto> listarAreaPorSedeContrato(Long sedeContratoId) {
        return this.matrizContratacionView.listarAreaPorSedeContrato(sedeContratoId);
    }
    
    public List<DescuentoDto> listarDescuentoPorContrato(
            LegalizacionContratoDto legalizacionContrato) {
        return this.matrizContratacionView.listarDescuentoPorContrato(legalizacionContrato);
    }
    
    public List<SedePrestadorDto> listarSedesPorContrato(
            LegalizacionContratoDto legalizacionContrato) {
        return this.matrizContratacionView.listarSedesPorContrato(legalizacionContrato);
    }

    /**
     * Consulta los contratos para la matriz de contratacion con base en los filtros enviados
     * @param filtroDto Dto con los filtros de la matriz
     * @return lista de {@link - LegalizacionContratoDto}
     */
    public List<LegalizacionContratoDto> listarContratosByFiltros(
            FiltroMatrizDto filtroMatriz) {
        return this.matrizContratacionView.listarContratosByFiltros(filtroMatriz);
    }

}
