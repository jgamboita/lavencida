package com.conexia.contratacion.portafolio.definitions.commons;

import java.util.List;

import com.conexia.contratacion.commons.dto.DescriptivoDto;
import com.conexia.contratacion.commons.dto.DocumentoAdjuntoDto;
import com.conexia.contratacion.commons.dto.maestros.ClasificacionPrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.GrupoInsumoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 *
 * @author Andrés Mise Olivera
 */
public interface CommonViewServiceRemote {

	List<ClasificacionPrestadorDto> listarClasificacionesPrestador();

	List<GrupoInsumoDto> listarGruposInsumoEmpresarial();

	List<TipoIdentificacionDto> listarTiposIdentificacion();

	/**
	 * Obtiene el flujo de bytes del archivo solicitado del repositorio de
	 * archivos
	 * 
	 * @param nombreArchivo
	 *            - El nombre del archivo en el repositorio de archivos
	 * @param tipoAdjuntoCodigo
	 *            - El codigo de los tipos de adjuto que se espera obtener
	 * @return Un flujo de bytes que representa el archivo obtenido desde el
	 *         repositorio
	 * @throws ConexiaBusinessException
	 */
	public DocumentoAdjuntoDto obtenerDocumentoAdjunto(
			DocumentoAdjuntoDto documentoAdjunto)
			throws ConexiaBusinessException;

	/**
	 * Permite registrar los documentos que han sido cargados a traves de la
	 * interfaz de usuario
	 * 
	 * @param adjuntos
	 *            - Una lista de unstancias de {@link DocumentoAdjuntoDto}
	 * @return Una lista de instancias de {@link DocumentoAdjuntoDto}
	 * @throws ConexiaBusinessException
	 */
	public DocumentoAdjuntoDto crearAdjunto(DocumentoAdjuntoDto adjunto)
			throws ConexiaBusinessException;

	/**
	 * Permite eliminar un documento alojado en disco
	 * 
	 * @param adjunto
	 *            - Una lista de unstancias de {@link DocumentoAdjuntoDto}
	 * @throws ConexiaBusinessException
	 */
	void eliminarAdjunto(DocumentoAdjuntoDto adjunto)
			throws ConexiaBusinessException;

	/**
	 * Obtiene una lista de todos los departamentos en base de datos
	 * 
	 * @return Una lista de instancias de {@link DepartamentoDto}
	 */
	List<DepartamentoDto> listarDepartamentos() throws ConexiaBusinessException;

	/**
	 * Obtiene una lista de municipios relacionados al departamento pasado como
	 * parametro
	 * 
	 * @param departamento - Una instancia de {@link DepartamentoDto}
	 * @return Una lista de instancias de {@link MunicipioDto}
	 */
	List<MunicipioDto> listarMunicipios(DepartamentoDto departamento) throws ConexiaBusinessException;

	
	/**
	 * Obtiene una lista de zonas soportadas en el sistema
	 * @return una lista de instancias de {@link DescriptivoDto}
	 * @throws ConexiaBusinessException
	 */
	List<DescriptivoDto> listarZonas() throws ConexiaBusinessException;

}
