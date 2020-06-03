package com.conexia.contratacion.portafolio.prestador.control;

import java.util.HashMap;

import com.conexia.contratacion.commons.constants.enums.TipoDocumentoEnum;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.exceptions.ConexiaBusinessException;

public class SedePrestadorControl {
	final String FROM_DOCUMENTOS_SEDE = "FROM DocumentacionSede d WHERE 1 = 1";

	final String FROM_OFERTA_PRESTADOR = " FROM OfertaSedePrestador osp JOIN osp.ofertaPrestador op JOIN osp.sedePrestador s JOIN s.zona z JOIN s.municipio m JOIN m.departamento d ";

	public void generarSelectDocumentos(StringBuilder queryString) {
		queryString
				.append("SELECT new com.conexia.contratacion.commons.dto.DocumentoAdjuntoDto(")
				.append("d.id, ").append("d.nombreArchivo, ")
				.append("d.nombreArchivoServidor) ")
				.append(FROM_DOCUMENTOS_SEDE);
	}

	public HashMap<String, Object> generarWhereDocumentos(
			StringBuilder queryString, TipoDocumentoEnum tipoAdjunto,
			Long sedeId) {
		HashMap<String, Object> parametros = new HashMap<String, Object>();

		if (tipoAdjunto != null) {
			queryString.append(" AND d.tipoArchivo = :tipoDocumento ");
			parametros.put("tipoDocumento", tipoAdjunto.name());
		} else {
			queryString.append(" AND d.tipoArchivo IS NULL ");
		}

		if (sedeId != null) {
			queryString.append(" AND d.sede.id = :sedeId ");
			parametros.put("sedeId", sedeId);
		}

		return parametros;
	}

	public void generarContarSedesOferta(StringBuilder queryString) {
		queryString
			.append("SELECT CAST(COUNT(s.id) as integer) ")
			.append(FROM_OFERTA_PRESTADOR);
	}

	public void generarSelectSedesOferta(StringBuilder queryString) {
		queryString
				.append("SELECT new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto(")
				.append("s.id, ")
				.append("s.nombreSede, ")
				.append("s.codigoPrestador, ")
				.append("s.codigoSede, ")
				.append("m.id, ")
				.append("m.descripcion, ")
				.append("d.id, ")
				.append("d.descripcion, ")
				.append("s.sedePrincipal, ")
				.append("z.id, ")
				.append("z.descripcion, ")
				.append("s.direccion, ")
				.append("s.barrio, ")
				.append("s.telefonoAdministrativo, ")
				.append("s.fax, ")
				.append("s.correo, ")
				.append("s.telefonoCitas, ")
				.append("s.enumStatus, ")
				.append("s.gerente)")
				.append(FROM_OFERTA_PRESTADOR);
	}
	
	public HashMap<String, Object> generarWhereSedesOferta(
			StringBuilder queryString, Long prestadorId, Integer modalidadId) throws ConexiaBusinessException {
		HashMap<String, Object> parametros = new HashMap<String, Object>();

		if(prestadorId == null){
			throw new ConexiaBusinessException(
					PreContractualMensajeErrorEnum.BUSINESS_ERROR,
					"El identificador del prestador esta nulo");
		}
		
		if(modalidadId == null){
			throw new ConexiaBusinessException(
					PreContractualMensajeErrorEnum.BUSINESS_ERROR,
					"El identificador de la modalidad esta nula");
		}
		
		queryString.append(" WHERE op.prestador.id = :prestadorId ")
				.append(" AND op.modalidad.id = :modalidadId ");
		
		parametros.put("prestadorId", prestadorId);
		parametros.put("modalidadId", modalidadId);
		
		
		return parametros;
	}
}
