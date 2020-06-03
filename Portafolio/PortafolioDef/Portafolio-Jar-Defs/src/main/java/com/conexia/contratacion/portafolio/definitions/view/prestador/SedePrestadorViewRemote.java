package com.conexia.contratacion.portafolio.definitions.view.prestador;

import java.io.Serializable;
import java.util.List;

import com.conexia.contratacion.commons.constants.enums.TipoDocumentoEnum;
import com.conexia.contratacion.commons.dto.DocumentoAdjuntoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.HorarioAtencionDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.capita.PortafolioSedeDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 *
 * @author <a href="dgarcia@conexia.com">David Garcia H</a>
 */
public interface SedePrestadorViewRemote extends Serializable {

	/**
	 * Obtiene las sedes del prestador con identificador igual al pasado como
	 * parametro
	 * 
	 * @param prestadorId
	 *            - El identificador unico de prestador
	 * @param tamanioPagina
	 * @param offset
	 * @return Una lista con instancias de {@link SedePrestadorDto} o vacia
	 */
	List<SedePrestadorDto> listarSedesPorPrestadorUsuarioId(Long prestadorId,
			int offset, int tamanioPagina) throws ConexiaBusinessException;

	/**
	 * Permite obtener los documentos adjuntados por para la sede con id unico
	 * igual al pasado como parametro
	 * 
	 * @param sedeId
	 *            - Identificador unico de sede
	 * @param tipoDocumento
	 *            - El tipo de documento que se desea recuperar para la sede
	 * 
	 * @return Una lista de instancias de {@link DocumentoAdjuntoDto}
	 * @throws ConexiaBusinessException
	 */
	public List<DocumentoAdjuntoDto> obtenerDocumentosSedeId(Long sedeId,
			TipoDocumentoEnum tipoDocumento) throws ConexiaBusinessException;

	/**
	 * Permite obtener el horario dela sede identificada con el argumento pasado
	 * como parametro
	 * 
	 * @param sedeId
	 *            - Identificador unico de sede
	 * @return Una instancia de {@link HorarioAtencionDto}
	 * 
	 * @throws ConexiaBusinessException
	 */
	public HorarioAtencionDto obtenerHorarioSedeId(Long sedeId)
			throws ConexiaBusinessException;

	/**
	 * Obtiene el numero total de sedes exsitentes para el prestador
	 * 
	 * @param prestadorId
	 *            - El identificador unico del prestador
	 * @return Un entero con la cantidad de sedes disponibles
	 * @throws ConexiaBusinessException
	 */
	public Integer contarSedesPorPrestadorId(Long prestadorId)
			throws ConexiaBusinessException;

	public PortafolioSedeDto obtenerPortafolioPorOfertaIdYSedeId(
			Integer ofertaPrestadorId, Long sedePrestadorId)
			throws ConexiaBusinessException;
}
