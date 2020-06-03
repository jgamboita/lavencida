package com.conexia.contractual.services.transactional.legalizacion.control;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroContratoUrgenciasDto;

public class ContratoUrgenciasVoBoControl {

	 public Map<String, Object> completarWhereConsultaContratosUrgenciasVoBo(StringBuilder ql, FiltroContratoUrgenciasDto filtro) {

	        //<editor-fold defaultstate="collapsed" desc="Parametrizacion">
	        Map<String, Object> params = new HashMap<>();


	        if(!"".equals(filtro.getNumeroContrato()) && filtro.getNumeroContrato() != null){
	        	params.put("numeroContrato",filtro.getNumeroContrato().toUpperCase().trim());
	        }

	        if (filtro.getNegociacionModalidad() != null) {
	            params.put("modalidadContrato", filtro.getNegociacionModalidad());
	        }

	        if (filtro.getTipoIdentificacionDto() != null) {
	            params.put("tipoIdentificacion", filtro.getTipoIdentificacionDto().getId());
	        }

	        if (!"".equals(filtro.getNumeroIdentificacion()) && filtro.getNumeroIdentificacion() != null) {
	            params.put("numeroIdentificacion", filtro.getNumeroIdentificacion().trim());
	        }

	        if (!"".equals(filtro.getRazonSocial()) && filtro.getRazonSocial() != null) {
	            params.put("razonSocialLike", "%" + filtro.getRazonSocial().toUpperCase().trim() + "%");
	        }

	        this.agregarFiltrosTabla(ql, filtro, params);
	        return this.setParams(ql, params, this.setearAtributosConsultaContratosUrgenciasVoBo());
	    }

	    private Map<String, String> setearAtributosConsultaContratosUrgenciasVoBo() {
	        Map<String, String> mapeoAttrs = new HashMap<>();
	        mapeoAttrs.put("numeroContrato","cu.numero_contrato_urgencias");
	        mapeoAttrs.put("modalidadContrato", "cu.tipo_modalidad");
	        mapeoAttrs.put("tipoIdentificacion", "p.tipo_identificacion_id");
	        mapeoAttrs.put("numeroIdentificacion", "p.numero_documento");
	        mapeoAttrs.put("razonSocialLike", "p.nombre");

	        return mapeoAttrs;
	    }

	    private Map<String, Object> setParams(StringBuilder ql, Map<String, Object> params,
	            Map<String, String> attrs) {
	        // TODO Método generado automáticamente. Completar lo antes posible.
	        if (params != null) {
	            for (String key : params.keySet()) {
	                if (attrs.containsKey(key)) {
	                    Object objectValue = params.get(key);
	                    if (objectValue != null) {
	                        if (key.contains("Like")) {
	                            ql.append(" AND ")
	                                    .append(attrs.get(key))
	                                    .append(" like :")
	                                    .append(key);
	                        } else {
	                            ql.append(" AND ")
	                                    .append(attrs.get(key))
	                                    .append(" = :")
	                                    .append(key);
	                        }
	                    }
	                }
	            }
	        }
	        return params;
	    }

	    private void agregarFiltrosTabla(StringBuilder query, FiltroContratoUrgenciasDto filtro, Map<String, Object> params) {
	    	for (Entry<String, Object> entry : filtro.getFiltros().entrySet()) {
	    		switch (entry.getKey()) {
					case "contratoUrgencias.numeroContrato":
						query.append(" AND cu.numero_contrato_urgencias LIKE :numeroContratoLike ");
						params.put("numeroContratoLike", "%" + entry.getValue().toString().toUpperCase().trim() + "%");
						break;
					case "contratoUrgencias.regionalDto.descripcion":
						query.append(" AND UPPER(rl.descripcion) LIKE :regionalLike ");
						params.put("regionalLike", "%" + entry.getValue().toString().toUpperCase().trim() + "%");
						break;
					case "contratoUrgencias.usuarioDto.nombreCompleto":
						query.append(" AND UPPER(u.name) LIKE :usuarioCreadorLike ");
						params.put("usuarioCreadorLike", "%" + entry.getValue().toString().toUpperCase().trim() + "%");
						break;
					case "contratoUrgencias.tipoContratoEnum":
						query.append(" AND cu.tipo_contrato LIKE :tipoContratoLike ");
						params.put("tipoContratoLike", "%" + entry.getValue().toString().toUpperCase().trim() + "%");
						break;
					case "estadoLegalizacion.descripcion":
						query.append(" AND '"+EstadoLegalizacionEnum.CONTRATO_SIN_VB.getDescripcion().toUpperCase()+"' LIKE :estadoLegalizacionLike ");
						params.put("estadoLegalizacionLike", "%" + entry.getValue().toString().toUpperCase().trim() + "%");
						break;
					case "contratoUrgencias.estadoContrato":
						query.append(" AND (CASE WHEN cu.fecha_fin>CAST(NOW() AS date) THEN 'VIGENTE' END) LIKE :estadoContratoLike ");
						params.put("estadoContratoLike", "%" + entry.getValue().toString().toUpperCase().trim() + "%");
						break;
					case "contratoUrgencias.tipoSubsidiado":
						query.append(" AND cu.tipo_subsidiado LIKE :tipoSubsidioLike ");
						params.put("tipoSubsidioLike", "%" + entry.getValue().toString().toUpperCase().trim() + "%");
						break;
					case "contratoUrgencias.fechaInicioVigencia":
						query.append(" AND TO_CHAR(cu.fecha_inicio,'DD/MM/YYYY') LIKE :fechaInicioLike ");
						params.put("fechaInicioLike", "%" + entry.getValue().toString().toUpperCase().trim() + "%");
						break;
					case "contratoUrgencias.fechaFinVigencia":
						query.append(" AND TO_CHAR(cu.fecha_fin,'DD/MM/YYYY') LIKE :fechaFinLike ");
						params.put("fechaFinLike", "%" + entry.getValue().toString().toUpperCase().trim() + "%");
						break;
					case "contratoUrgencias.nroSedes":
						query.append(" AND CAST((SELECT COUNT(id) FROM contratacion.sede_contrato_urgencias WHERE contrato_urgencias_id=cu.id) AS text)= :nSedes ");
						params.put("nSedes",entry.getValue().toString());
						break;
					case "contratoUrgencias.tipoModalidadNegociacion":
						query.append(" AND cu.tipo_modalidad LIKE :tipoModalidadLike ");
						params.put("tipoModalidadLike", "%" + entry.getValue().toString().toUpperCase().trim() + "%");
						break;
					case "contratoUrgencias.regimen":
						query.append(" AND cu.regimen LIKE :regimenLike ");
						params.put("regimenLike", "%" + entry.getValue().toString().toUpperCase().trim() + "%");
						break;
					case "contratoUrgencias.prestador.numeroDocumento":
						query.append(" AND p.numero_documento LIKE :numeroDocumentoLike ");
						params.put("numeroDocumentoLike", "%" + entry.getValue().toString().toUpperCase().trim() + "%");
						break;
					case "contratoUrgencias.prestador.nombre":
						query.append(" AND p.nombre LIKE :prestadorLike ");
						params.put("prestadorLike", "%" + entry.getValue().toString().toUpperCase().trim() + "%");
						break;
					default:
						break;
	    		}
	    	}
	    }
}
