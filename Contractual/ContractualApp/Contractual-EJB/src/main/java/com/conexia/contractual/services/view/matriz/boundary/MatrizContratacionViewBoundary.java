package com.conexia.contractual.services.view.matriz.boundary;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.conexia.contractual.definitions.view.matriz.MatrizContratacionViewRemote;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NivelContratoEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.TipoServicioEnum;
import com.conexia.contractual.services.view.matriz.control.MatrizContratacionControl;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.DescuentoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.AreaInfluenciaDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MacroServicioDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.matriz.FiltroMatrizDto;

@Stateless
@Remote(MatrizContratacionViewRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class MatrizContratacionViewBoundary implements MatrizContratacionViewRemote{
    
    @Inject
    MatrizContratacionControl control;
    
    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;
    
    /**
     * Consulta los contratos para la matriz de contratacion con base en los filtros enviados
     * @param filtroDto Dto con los filtros de la matriz
     * @return lista de {@link - LegalizacionContratoDto}
     */
    public List<LegalizacionContratoDto> listarContratosByFiltros(
            FiltroMatrizDto filtroDto) {
        
        StringBuilder sql = this.control.generarSqlBase();

        StringBuilder sqlWhere = new StringBuilder();
        final Map<String, Object> params = control
                .completarWhereConsultaContratos(sql, sqlWhere, filtroDto);
        
        sqlWhere.append( " order by c.fechaFin ");

        TypedQuery<LegalizacionContratoDto> query = em.createQuery(
                sql.toString()+sqlWhere.toString(), LegalizacionContratoDto.class);

        params.keySet().stream().filter((param) -> (params.get(param) != null))
                .forEach((param) -> {
                    query.setParameter(param, params.get(param));
                });
        return query.getResultList();
    }
    
    @Override
    public List<AreaInfluenciaDto> listarAreaPorSedeContrato(Long sedeContratoId) {
        return em.createNamedQuery("AreaCoberturaContrato.findAreaInfluenciaDtoBySedeContratoId",
                AreaInfluenciaDto.class)
                .setParameter("sedeContratoId", sedeContratoId.intValue())
                .getResultList();
    }
    
    @Override
    public List<DescuentoDto> listarDescuentoPorContrato(
            LegalizacionContratoDto legalizacionContrato) {
        return em.createNamedQuery("DescuentoLegalizacionContrato.findDtoByLegalizacionContratoId",
                DescuentoDto.class)
                .setParameter("legalizacionContratoId", legalizacionContrato.getId())
                .getResultList();
    }
    
    @Override
    public List<SedePrestadorDto> listarSedesPorContrato(
            LegalizacionContratoDto legalizacionContrato) {
        return em.createNamedQuery("SedeContrato.findSedeDtoByContratoId",
                SedePrestadorDto.class)
                .setParameter("contratoId", legalizacionContrato.getContratoDto().getId())
                .getResultList();
    }

}
