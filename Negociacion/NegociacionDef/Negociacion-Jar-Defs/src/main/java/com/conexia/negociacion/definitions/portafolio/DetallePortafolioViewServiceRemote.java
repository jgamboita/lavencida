package com.conexia.negociacion.definitions.portafolio;

import java.io.IOException;
import java.util.List;

import com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.DiagnosticoPortafolioDto;
import com.conexia.contratacion.commons.dto.GrupoTransporteDto;
import com.conexia.contratacion.commons.dto.InsumoPortafolioDto;
import com.conexia.contratacion.commons.dto.MedicamentoPortafolioDto;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.ProcedimientoPortafolioDto;
import com.conexia.contratacion.commons.dto.ProcedimientoPropioPortafolioDto;
import com.conexia.contratacion.commons.dto.TransportePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.GrupoServicioDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoPaqueteDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 * Interface para el boundary del detalle del portafolio
 * 
 * @author jjoya
 *
 */
public interface DetallePortafolioViewServiceRemote {

	/**
	 * Devuelve el arreglo de bytes de los documentos de un prestador
	 * @param prestadorId identificador del prestador
	 * @return arreglo de byte
	 */
	byte[] obtenerByteArrayZipDocumentosPorPrestador(Long prestadorId)throws IOException, ConexiaBusinessException;

	/**
	 * Consulta las sedes del prestador y sus tecnologias
	 * @param prestadorId Identificador del prestador
	 * @return Lista de {@link - SedePrestadorDto}
	 */
	List<SedePrestadorDto> consultarTecnologiasSedesByPrestadorId(
			Long prestadorId);

	/**
	 * Consulta los servicios de habilitacion por sede
	 * @param sedeId Identificador de la sede
	 * @return Lista de {@link - GrupoServicioDto}
	 */
	List<GrupoServicioDto> consultarServiciosHabilitacionBySedeId(Long sedeId);
	
	/**
	 * Consulta las categorias de los medicamentos que pertenecen a los medicamentos
	 * @param idSede Identificador de la sede
	 * @return Lista de {@link - CategoriaMedicamentoDto}
	 */
	List<CategoriaMedicamentoDto> consultarCategoriasMedicamento(Long idSede);

	/**
	 * Consulta los medicamentos asociados a una sede y categoria
	 * @param sedeId Identificador de la sede
	 * @param categoriaId Identificador de la categria
	 * @return Lista de {@link - MedicamentosDto}
	 */
	List<MedicamentoPortafolioDto> consultarMedicamentosBySedeIdAndCategoriaId(
			Long sedeId, Long categoriaId);

	/**
	 * Consulta los procedimientos propios de la sede
	 * @param sedeId Identificador de la sede
	 * @return Lista {@link - ProcedimientoPropioPortafolioDto}
	 */
	List<ProcedimientoPropioPortafolioDto> consultarServiciosPropiosBySedeId(
			Long sedeId);

	/**
	 * Consulta los paquetes de la sede
	 * @param sedeId Identificador de la sede
	 * @return Lista {@link - PaquetePortafolioDto}
	 */
	List<PaquetePortafolioDto> consultarPaquetesBySedeId(Long sedeId);

	/**
	 * Consulta los grupos de los traslados por sede
	 * @param sedeId Identificador de la sede
	 * @return Lista de {@link GrupoTransporteDto}
	 */
	List<GrupoTransporteDto> consultarGrupoTrasaladosBySedeId(Long sedeId);

	/**
	 * Consulta los transportes pertenecientes a una sede por grupo
	 * @param sedeId identificador de la sede
	 * @param grupoId identificador del grupo tranporte
	 * @return lista de {@link - TransportePortafolioDto}
	 */
	List<TransportePortafolioDto> consultarTrasladosBySedeIdAndGrupoId(
			Long sedeId, Integer grupoId);

	/**
	 * Devuelve el arreglo de bytes de los documentos de una sede
	 * @param sedeId
	 * @return arreglo de byte
	 * @throws ConexiaBusinessException 
	 * @throws IOException 
	 */
	byte[] obtenerByteArrayZipDocumentosPorSede(Long sedeId) throws IOException, ConexiaBusinessException;
	
	/**
	 * Consulta los procedimientos asociados a un grupo servicio
	 * @param grupoServicioId Identificador del grupo servicio
	 * @return Lista de {@link - ProcedimientoPortafolioDto}
	 */
	List<ProcedimientoPortafolioDto> consultarProcedimientosByGrupoServicioId(
			Long grupoServicioId);

	/**
	 * Consulta los diagnosticos del portafolio
	 * @param portafolioId Identificador del portafolio
	 * @return Lista de {@link - DiagnosticoPortafolioDto}
	 */
	List<DiagnosticoPortafolioDto> consultarDiagnosticosPortafolio(
			Long portafolioId);

	/**
	 * Consulta los procedimientos asociados a un paquete
	 * @param paqueteId Identificador del paquete
	 * @return lista de {@link - ProcedimientoPaqueteDto}
	 */
	List<ProcedimientoPaqueteDto> consultarProcedimientosPaquete(Long paqueteId);

	/**
	 * Consulta los medicamentos asociados a un paquete
	 * @param paqueteId Identificador del paquete
	 * @return lista de {@link - MedicamentoPortafolioDto}
	 */
	List<MedicamentoPortafolioDto> consultarMedicamentosPaquete(Long paqueteId);

	/**
	 * Consulta los insumos asociados a un paquete
	 * @param paqueteId Identificador del paquete
	 * @return lista de {@link - InsumoPortafolioDto}
	 */
	List<InsumoPortafolioDto> consultarInsumosPaquete(Long paqueteId);

	/**
	 * Consulta los insumos asociados a un paquete
	 * @param paqueteId Identificador del paquete
	 * @return lista de {@link - TransportePortafolioDto}
	 */
	List<TransportePortafolioDto> consultarTrasladosPaquete(Long paqueteId);

}
