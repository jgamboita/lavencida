package com.conexia.contratacion.portafolio.definitions.view.prestador;

import java.io.Serializable;
import java.util.List;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.TarifaPropuestaProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contratacion.commons.dto.capita.OfertaPrestadorDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 *
 * @author Andrés Mise Olivera
 */
public interface PrestadorViewServiceRemote extends Serializable {
    
    PrestadorDto buscarPrestador(PrestadorDto prestador);
    
    /**
     * Busca un prestador por id
     * @param prestadorId
     * @return el prestador
     */
    PrestadorDto buscarPrestador(Long prestadorId);
    
    List<PrestadorDto> buscarPrestadores(List<EstadoPrestadorEnum> estados);
    byte[] descargarArchivo(String nombreArchivo) throws ConexiaBusinessException;
    
    
	/**
	 * @author <a href="dgarcia@conexia.com">David Garcia H</a>
	 * Obtiene la informacion del prestador al usuario identificado con el id
	 * pasado como parametro
	 * 
	 * @param usuarioId
	 *            - El identificador unico de usuario relacionado con el
	 *            prestador
	 * 
	 * @return Una instancia de {@link PrestadorDto} con la informacion obtenida
	 *         del modelo o vacia si no existe el prestador
	 *
	 * @throws ConexiaBusinessException
	 */
    public PrestadorDto obtenerPorUsuarioId(Integer usuarioId) throws ConexiaBusinessException;

	OfertaPrestadorDto obtenerOfertaPresentarPorPrestadorIdYModalidadId(
			Long prestadorId, Integer modalidadId) throws ConexiaBusinessException;
    
	/**
	 * Obtiene el listado de las mejores tarifas por prestador y Modalidad
	 * @param prestadorId
	 * @param modalidadNegociacionEnum
	 * @return
	 */
	public List<TarifaPropuestaProcedimientoDto> obtenerMejoresTarifasServicios(Long prestadorId, NegociacionModalidadEnum modalidadNegociacionEnum);
	
	/**
	 * Obtiene el listado de los servicios y procedimientos negociados
	 * @param prestadorId
	 * @param modalidadNegociacionEnum
	 * @return
	 */
	public List<TarifaPropuestaProcedimientoDto> obtenerSedesYServiciosNegociados(Long prestadorId, NegociacionModalidadEnum modalidadNegociacionEnum);
	
	/**
	 * Obtiene el listado de prestadores a cargar portafolio por modalidad
	 * @param modalidadNegociacionEnum
	 * @return
	 */
	public List<Long> obtenerListadoPrestadores(NegociacionModalidadEnum modalidadNegociacionEnum);
	
	/**
	 * Obtiene las mejores tarifas de los Medicamentos negociados
	 * @param prestadorId
	 * @param modalidadNegociacionEnum
	 * @return
	 */
	public List<MedicamentoNegociacionDto> obtenerMejoresTarifasMedicamentos(Long prestadorId, NegociacionModalidadEnum modalidadNegociacionEnum);
		
	/**
	 * Obtiene los medicamentos que fueron negociados por el prestador 
	 * @param prestadorId
	 * @param modalidadNegociacionEnum
	 * @return
	 */
	public List<MedicamentoNegociacionDto> obtenerSedesYMedicamentosNegociados(Long prestadorId, NegociacionModalidadEnum modalidadNegociacionEnum);
}
