package com.conexia.contractual.services.transactional.contratourgencias.control;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.conexia.contratacion.commons.constants.enums.TypeUserContractUrgencyEnum;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaContratoUrgenciaDto;


public class BandejaContratoUrgenciasControl {

    public void generarSelectBuscarPrestador(StringBuilder query, FiltroBandejaContratoUrgenciaDto filtro, String typeUserCode) {
        query.append("SELECT  ")
                .append("    p.id as prestadorId, ")
                .append("    sp.nombre_sede as nombre, ")
                .append("    p.tipo_identificacion_id tipoIdentificacionId, ")
                .append("    p.numero_documento numeroDocumento, ")
                .append("    p.prefijo, ")
                .append("    CASE WHEN p.aplica_red_ips = 0 THEN 'No Red'  WHEN p.aplica_red_ips = 1 THEN 'Red'  END as prestadorRed,  ")
                .append("    sp.direccion as direccionPrestador, ")
                .append("    p.codigo_prestador as codigoPrestador, ")
                .append("    p.naturaleza_juridica as naturalezaJuridicaId, ")
                .append("    p.clase_prestador as clasePrestadorId, COALESCE(p.nivel_atencion, p.nivel_atencion) as nivelContratoId, p.nivel_atencion as nivelAtencionId, ")
                .append("    p.clasificacion_id as clasificacionId, ")
                .append("    tp.descripcion as tipoPrestador, p.correo_electronico as correo, ")
                .append("    p.representante_legal as representanteLegal, ")
                .append("    p.numero_documento_representante as numeroDocumentoRepresentanteLegal, ")
                .append("    p.tipo_identificacion_representante_id as tipoIdentificacionRepLegalId, ")
                .append("    dep.id as departamentoId, dep.descripcion as departamento, ")
                .append("    sp.municipio_id as municipioId, muni.descripcion as municipio, p.empresa_social_estado as ese  ")
                .append("FROM contratacion.prestador p  ")
                .append("    INNER JOIN contratacion.sede_prestador sp  on p.id = sp.prestador_id and sp.sede_principal ")
                .append("    INNER JOIN maestros.municipio muni on muni.id = sp.municipio_id ")
                .append("    INNER JOIN maestros.departamento dep on dep.id = muni.departamento_id ")
                .append("    INNER JOIN contratacion.tipo_prestador tp on tp.id = p.tipo_prestador_id ");
        if (filtro.getNumeroContrato() != null && filtro.getNumeroContrato() != "" && !filtro.getNumeroContrato().isEmpty()) {
            query.append("   inner join contratacion.contrato_urgencias cu on cu.prestador_id = p.id  ");
            query.append("and cu.deleted = false ");
        }

        query.append("WHERE sp.enum_status = 1 AND p.aplica_red_ips is not null ");
        if (typeUserCode.equals(TypeUserContractUrgencyEnum.CUENTAS_MEDICAS.getCode())) {
            query.append("AND p.aplica_red_ips = 0 ");
        }

    }

    public void generarSelectContarPrestador(StringBuilder query, FiltroBandejaContratoUrgenciaDto filtro, String typeUserCode) {

        query.append("select count(0) from (");
        generarSelectBuscarPrestador(query, filtro, typeUserCode);
    }

    public Map<String, Object> generarWhereBuscarPrestador(StringBuilder query, FiltroBandejaContratoUrgenciaDto filtro) {
        Map<String, Object> params = new HashMap<>();

        if (filtro.getNumeroIdentificacion() != null && !filtro.getNumeroIdentificacion().trim().isEmpty()) {
            query.append(" and p.numero_documento like :numeroDocumento ");
            params.put("numeroDocumento", "%" + filtro.getNumeroIdentificacion().trim() + "%");
        }

        if (filtro.getNumeroContrato() != null && filtro.getNumeroContrato() != "" && !filtro.getNumeroContrato().isEmpty()) {
            query.append("and cu.numero_contrato_urgencias like :numeroContrato");
            params.put("numeroContrato", "%" + filtro.getNumeroContrato().trim().toUpperCase() + "%");
        }

        if (filtro.getPrefijo() != null && !filtro.getPrefijo().trim().isEmpty()) {
            query.append(" and p.prefijo like :prefijo ");
            params.put("prefijo", "%" + filtro.getPrefijo().trim().toUpperCase() + "%");
        }
        if (filtro.getNombrePrestador() != null && !filtro.getNombrePrestador().trim().isEmpty()) {
            query.append(" and p.nombre like :nombrePrestador ");
            params.put("nombrePrestador", "%" + filtro.getNombrePrestador().trim().toUpperCase() + "%");
        }
        if (filtro.getTipoIdentificacionSeleccionado() != null
                && filtro.getTipoIdentificacionSeleccionado().getId() != null) {
            query.append(" and p.tipo_identificacion_id = :tipoDocumento ");
            params.put("tipoDocumento", filtro.getTipoIdentificacionSeleccionado().getId());
        }
        if (filtro.getDepartamentoDto() != null
                && filtro.getDepartamentoDto().getId() != null) {
            query.append(" and dep.id = :departamentoId ");
            params.put("departamentoId", filtro.getDepartamentoDto().getId());
        }

        if (filtro.getMunicipioDto() != null
                && filtro.getMunicipioDto().getId() != null) {
            query.append(" and muni.id = :municipioId ");
            params.put("municipioId", filtro.getMunicipioDto().getId());
        }

        if (filtro.getPrestadorEsRed() != null) {
            if (filtro.getPrestadorEsRed().equals("RED")) {
                query.append(" and p.aplica_red_ips = 1 ");
            } else {
                query.append(" and p.aplica_red_ips = 0 ");
            }
        }


        // Filtro de tecnologias por modalidad
        this.agregarFiltrosDeTabla(filtro, query, params);

        return params;
    }

    private void agregarFiltrosDeTabla(FiltroBandejaContratoUrgenciaDto filtro, StringBuilder query, Map<String, Object> params) {
        for (Entry<String, Object> entry : filtro.getFiltros().entrySet()) {
            switch (entry.getKey()) {
                case "id":
                    query.append(" and to_char(p.id, 'FM99999999999') like :idPrestador ");
                    params.put("idPrestador", "%" + entry.getValue() + "%");
                    break;
                case "nombre":
                    query.append(" and p.nombre like :nombrePrestador ");
                    params.put("nombrePrestador", "%" + entry.getValue().toString().trim().toUpperCase() + "%");
                    break;
                case "numeroDocumento":
                    query.append(" and p.numero_documento like :documentoPrestador ");
                    params.put("documentoPrestador", "%" + entry.getValue() + "%");
                    break;
                case "prefijo":
                    query.append(" and p.prefijo like :prefijoPrestador ");
                    params.put("prefijoPrestador", "%" + entry.getValue() + "%");
                    break;
                case "municipioDto.departamento":
                    query.append(" and dep.descripcion   like :departamento ");
                    params.put("departamento", "%" + entry.getValue().toString().trim().toUpperCase() + "%");
                    break;
                case "municipioDto.descripcion":
                    query.append(" and muni.descripcion like :municipio ");
                    params.put("municipio", "%" + entry.getValue().toString().trim().toUpperCase() + "%");
                    break;
                case "prestadorRed":
                    if (entry.getValue().toString().trim().toUpperCase().contains("RE")) {
                        query.append(" and p.aplica_red_ips = 1 ");
                    } else if (entry.getValue().toString().trim().toUpperCase().contains("NO R")) {
                        query.append(" and p.aplica_red_ips = 0 ");
                    }
                    break;
            }
        }
    }
}
