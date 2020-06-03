package com.conexia.contractual.services.view.matriz.control;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import com.conexia.contratacion.commons.constants.enums.TipoServicioEnum;
import com.conexia.contratacion.commons.dto.matriz.FiltroMatrizDto;

/**
 * Control de MatrizContratacion
 * 
 * @author jjoya
 *
 */
public class MatrizContratacionControl {

    /**
     * Genera el sql base para la consulta de los contratos
     */
    public StringBuilder generarSqlBase() {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT DISTINCT new com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoDto(");
        sql.append("c.id, lc.id, r.descripcion, c.numeroContrato, p.numeroDocumento, p.nombre, p.naturalezaJuridica,  ");
        sql.append("c.fechaInicio, c.fechaFin,c.fechaCreacion, s.tipoModalidadNegociacion, ");
        sql.append("lc.valorFiscal, c.poblacion, c.nivelContrato, c.nombreArchivo, lc.objetoContrato, lc.fechaFirmaContrato, lc.valorPoliza, ");
        sql.append("rfc.nombre, lc.diasPlazo, ");
        sql.append("s.regimen, s.tipoModalidadNegociacion, mlc.descripcion, lc.direccion, rfc.municipioRecepcion, lc.contenido, ");
        sql.append("m.id, m.nombre, m.descripcion, n.id) ");
        sql.append("FROM Contrato c ");
        sql.append("JOIN c.legalizacionContratos lc ");
        sql.append("JOIN lc.municipio mlc ");
        sql.append("JOIN lc.responsableFirmaContrato rfc ");
        sql.append("JOIN c.regional r ");
        sql.append("JOIN c.solicitudContratacion s ");
        sql.append("JOIN s.negociacion n ");
        sql.append("JOIN s.prestador p ");
        sql.append("JOIN c.sedeContratos sc ");
        sql.append("JOIN lc.minuta m ");
        return sql;
    }

    /**
     * Funcion que permite retornar los parametros de la consulta.
     *
     * @param sql - Consulta a completar el filtro de consulta de contratos.
     * @param sqlWhere 
     * @param filtro - Filtro a agregar a la consulta.
     * @return params - Parametros de la consulta
     */
    public Map<String, Object> completarWhereConsultaContratos(
            StringBuilder sql, StringBuilder sqlWhere, FiltroMatrizDto filtro) {
        Map<String, Object> params = new HashMap<>();
        
        this.generarWhereZona(sql,sqlWhere,filtro,params);
        this.generarWhereFecha(sql,sqlWhere,filtro,params);
        this.generarWhereLegalizacion(sql,sqlWhere,filtro,params);
        this.generarWherePrestador(sql,sqlWhere,filtro,params);
        this.generarWhereTecnologias(sql,sqlWhere,filtro,params);
        
        sql.append("WHERE 1=1 ");
        
        return params;
        
    }

    /**
     * Genera el where cuando aplica a las tecnologias (Medicamentos, Insumos, Procedimientos)
     * @param sql
     * @param sqlWhere
     * @param filtro
     * @param params
     */
    private void generarWhereTecnologias(StringBuilder sql,
            StringBuilder sqlWhere, FiltroMatrizDto filtro,
            Map<String, Object> params) {
        if (filtro.getGruposServicio() != null && filtro.getGruposServicio().size() > 0){
            sql.append("JOIN sc.procedimientosContrato pc JOIN pc.tarifarioContrato tc JOIN pc.procedimiento px JOIN px.servicioSalud ss JOIN ss.macroServicio ms ");
            sqlWhere.append(" AND ms.id IN (:gruposServicio) ");
            
            params.put("gruposServicio", filtro.getGruposServicio().stream().parallel()
                    .map(gs -> gs.getId()).collect(Collectors.toList()));
        }
        
        if(filtro.getTiposServicios() != null && filtro.getTiposServicios().size() > 0){
            
            for(TipoServicioEnum tipo : filtro.getTiposServicios()){
                this.agregarJoinTecnologias(sql,tipo);
            }
        }
        
        if (filtro.getTipoTecnologia() != null
                && (filtro.getCodigoTecnologiaEspecifica() != null || filtro
                        .getNombreTecnologiaEspecifica() != null)) {
            
            this.agregarJoinTecnologias(sql, filtro.getTipoTecnologia());
            this.agregarSelectByTecnologia(sql, filtro.getTipoTecnologia());
            this.agregarWhereByTecnologia(sqlWhere, filtro, params);

        }
        
    }

    /**
     * Genera los joins cuando aplica a las tecnologias (Medicamentos, Insumos, Procedimientos)
     * @param sql
     * @param sqlWhere
     * @param filtro
     * @param params
     */
    private void agregarJoinTecnologias(StringBuilder sql, TipoServicioEnum tipo) {
        if (tipo == TipoServicioEnum.SERVICIO) {
            if(sql.indexOf("sc.procedimientosContrato") == -1){
                sql.append("JOIN sc.procedimientosContrato pc JOIN pc.tarifarioContrato tc JOIN pc.procedimiento px JOIN px.procedimiento proc ");
            }
        }else if(tipo == TipoServicioEnum.MEDICAMENTO){
            if(sql.indexOf("sc.medicamentosContrato") == -1){
                sql.append("JOIN sc.medicamentosContrato mc JOIN mc.medicamento med ");
            }
        }else if(tipo == TipoServicioEnum.INSUMO){
            if(sql.indexOf("sc.insumosContrato") == -1){
                sql.append("JOIN sc.insumosContrato ic JOIN ic.insumo ins ");
            }
        }else if (tipo == TipoServicioEnum.TRANSPORTE) {
            if(sql.indexOf("sc.procedimientosContrato") == -1){
                sql.append("JOIN sc.procedimientosContrato pc JOIN pc.tarifarioContrato tc JOIN pc.procedimiento px JOIN px.procedimiento proc ");
            }
        }
        
    }
    
    /**
     * agrega select al constructor expression segun la tecnologia 
     * @param sql
     * @param tipo
     */
    private void agregarSelectByTecnologia(StringBuilder sql, TipoServicioEnum tipo){
        if (tipo == TipoServicioEnum.SERVICIO || tipo == TipoServicioEnum.TRANSPORTE) {
            sql.insert(sql.indexOf(")"), ", tc.descripcion,pc.porcentajeContrato, pc.valorContrato ");
        }else if(tipo == TipoServicioEnum.MEDICAMENTO){
            sql.insert(sql.indexOf(")"), ", '',mc.valorContrato, mc.valorContrato ");
        }else if(tipo == TipoServicioEnum.INSUMO){
            sql.insert(sql.indexOf(")"), ", '',ic.valorContrato, ic.valorContrato ");
        }
    }
    
    /**
     * Agrega el where dependiendo de la tecnologia seleccionada
     * para servicios y traslados se diferencia por 
     * tipoProcedimientoId 1 es para procedimiento 3 para traslados
     * @param sqlWhere
     * @param filtro
     * @param params
     */
    private void agregarWhereByTecnologia(StringBuilder sqlWhere,
            FiltroMatrizDto filtro, Map<String, Object> params) {
        
        if(filtro.getCodigoTecnologiaEspecifica() != null && filtro.getCodigoTecnologiaEspecifica().length() > 0){
            
            if (filtro.getTipoTecnologia() == TipoServicioEnum.SERVICIO) {
                sqlWhere.append(" AND px.cups = :codigoTecnologia AND proc.tipoProcedimiento.id = 1 ");
            }else if(filtro.getTipoTecnologia() == TipoServicioEnum.MEDICAMENTO){
                sqlWhere.append(" AND med.cums = :codigoTecnologia  ");
            }else if(filtro.getTipoTecnologia() == TipoServicioEnum.INSUMO){
                sqlWhere.append(" AND ins.codigo = :codigoTecnologia  ");
            }else if(filtro.getTipoTecnologia() == TipoServicioEnum.TRANSPORTE){
                sqlWhere.append(" AND px.cups = :codigoTecnologia AND proc.tipoProcedimiento.id = 3 ");
            }
            params.put("codigoTecnologia", filtro.getCodigoTecnologiaEspecifica());
                
        }
        
        if(filtro.getNombreTecnologiaEspecifica() != null && filtro.getNombreTecnologiaEspecifica().length() > 0){
            if (filtro.getTipoTecnologia() == TipoServicioEnum.SERVICIO || filtro.getTipoTecnologia() == TipoServicioEnum.TRANSPORTE) {
                sqlWhere.append(" AND px.descripcion LIKE :nombreTecnologia  ");    
            }else if(filtro.getTipoTecnologia() == TipoServicioEnum.MEDICAMENTO){
                sqlWhere.append(" AND med.descripcion LIKE :nombreTecnologia  ");
            }else if(filtro.getTipoTecnologia() == TipoServicioEnum.INSUMO){
                sqlWhere.append(" AND ins.descripcion LIKE :nombreTecnologia  ");
            }
            
            params.put("nombreTecnologia", "%"+filtro.getNombreTecnologiaEspecifica()+"%");
        }
        
    }

    private void generarWherePrestador(StringBuilder sql,
            StringBuilder sqlWhere, FiltroMatrizDto filtro,
            Map<String, Object> params) {
        
        if(filtro.getPrestador() != null){
            
            if(filtro.getPrestador().getNumeroDocumento() != null && filtro.getPrestador().getNumeroDocumento().length() > 0){
                sqlWhere.append(" AND p.numeroDocumento = :numeroDocPrestador  ");
                params.put("numeroDocPrestador", filtro.getPrestador().getNumeroDocumento());
            }
            
            if(filtro.getPrestador().getNombre() != null && filtro.getPrestador().getNombre().length() > 0){
                sqlWhere.append(" AND p.nombre like :nombrePrestador  ");
                params.put("nombrePrestador", "%"+filtro.getPrestador().getNombre()+"%");
            }
            
        }
        
    }

    private void generarWhereLegalizacion(StringBuilder sql,
            StringBuilder sqlWhere, FiltroMatrizDto filtro,
            Map<String, Object> params) {
        
        if(filtro.getNivel() != null){
            sqlWhere.append(" AND c.nivelContrato = :nivelContrato ");
            params.put("nivelContrato", filtro.getNivel());
        }
        
        if(filtro.getRegimen() != null){
            sqlWhere.append(" AND s.regimen = :regimen ");
            params.put("regimen", filtro.getRegimen());
        }
        
        if(filtro.getModalidades() != null && filtro.getModalidades().size() > 0){
            sqlWhere.append(" AND c.tipoModalidad IN (:modalidades)  ");
            params.put("modalidades", filtro.getModalidades());
        }
        
        if(filtro.getRegional() != null && filtro.getRegional().getId() != null){
            sqlWhere.append(" AND r.id = :regionalId ");
            params.put("regionalId", filtro.getRegional().getId());
        }
        
        if(filtro.getNumeroContrato() != null && filtro.getNumeroContrato().length() > 0){
            sqlWhere.append(" AND c.numeroContrato = :numeroContrato ");
            params.put("numeroContrato", filtro.getNumeroContrato());
        }
        
        if(filtro.getValorFiscalMin() != null && filtro.getValorFiscalMax() != null){
            sqlWhere.append(" AND (lc.valorFiscal >=  :valorFiscalMin AND lc.valorFiscal <= :valorFiscalMax)  ");
            params.put("valorFiscalMin", filtro.getValorFiscalMin());
            params.put("valorFiscalMax", filtro.getValorFiscalMax());
        }
    }

    private void generarWhereFecha(StringBuilder sql, StringBuilder sqlWhere,
            FiltroMatrizDto filtro, Map<String, Object> params) {
        if (filtro.getFechaInicioVigencia() != null && filtro.getFechaFinVigencia() != null) {
            sqlWhere.append(" AND  (c.fechaInicio >= :fechaInicio AND c.fechaFin <= :fechaFin) ");
            params.put("fechaInicio", filtro.getFechaInicioVigencia());
            params.put("fechaFin", filtro.getFechaFinVigencia());
        }
    }

    private void generarWhereZona(StringBuilder sql, StringBuilder sqlWhere,
            FiltroMatrizDto filtro, Map<String, Object> params) {
        boolean isMunicipio = false;

        if (filtro.getDepartamento() != null && filtro.getDepartamento().getId() != null) {
            isMunicipio = true;
            
            sql.append("JOIN sc.areaCoberturaContratos acc ");
            sql.append("JOIN acc.municipio mu JOIN mu.departamento d ");
            
            sqlWhere.append(" AND d.id = :departamentoId ");
            params.put("departamentoId", filtro.getDepartamento().getId());
        }
        
        if (filtro.getMunicipio() != null && filtro.getMunicipio().getId() != null) {
            
            if(!isMunicipio){
                sql.append("JOIN sc.areaCoberturaContratos acc ");
                sql.append("JOIN acc.municipio mu JOIN mu.departamento d ");
            }
            
            sqlWhere.append(" AND mu.id = :municipioId ");
            params.put("municipioId", filtro.getMunicipio().getId());
        }
    }

}
