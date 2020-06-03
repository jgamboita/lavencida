package co.conexia.negociacion.wap.facade.portafolio;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

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
import com.conexia.negociacion.definitions.portafolio.DetallePortafolioViewServiceRemote;
import com.conexia.servicefactory.CnxService;

public class DetallePortafolioFacade implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5591776289220320862L;
	
	@Inject
	@CnxService
	private DetallePortafolioViewServiceRemote detallePortafolioViewServiceRemote;

	/**
	 * Devuelve el arreglo de bytes de los documentos de un prestador
	 * @param prestadorId identificador del prestador
	 * @return arreglo de byte
	 * @throws ConexiaBusinessException 
	 * @throws IOException 
	 */
	public byte[] obtenerByteArrayZipDocumentosPorPrestador(Long prestadorId) throws IOException, ConexiaBusinessException {
		return detallePortafolioViewServiceRemote.obtenerByteArrayZipDocumentosPorPrestador(prestadorId);
	}

	/**
	 * Consulta las sedes del prestador y sus tecnologias
	 * @param prestadorId Identificador del prestador
	 * @return Lista de {@link - SedePrestadorDto}
	 */
	public List<SedePrestadorDto> consultarTecnologiasSedesByPrestadorId(Long prestadorId) {
		return this.detallePortafolioViewServiceRemote
				.consultarTecnologiasSedesByPrestadorId(prestadorId);
	}

	/**
	 * Consulta los servicios de habilitacion por sede
	 * @param sedeId Identificador de la sede
	 * @return Lista de {@link - GrupoServicioDto}
	 */
	public List<GrupoServicioDto> consultarServiciosHabilitacionBySedeId(Long sedeId) {
		return this.detallePortafolioViewServiceRemote.consultarServiciosHabilitacionBySedeId(sedeId);
	}
	
	/**
	 * Consulta las categorias de los medicamentos que pertenecen a los medicamentos
	 * @param idSede Identificador de la sede
	 * @return Lista de {@link - CategoriaMedicamentoDto}
	 */
	public List<CategoriaMedicamentoDto> consultarCategoriasMedicamento(Long idSede){
		return this.detallePortafolioViewServiceRemote.consultarCategoriasMedicamento(idSede);
	}

	/**
	 * Consulta los medicamentos asociados a una sede y categoria
	 * @param sedeId Identificador de la sede
	 * @param categoriaId Identificador de la categria
	 * @return Lista de {@link - MedicamentosDto}
	 */
	public List<MedicamentoPortafolioDto> consultarMedicamentosBySedeIdAndCategoriaId(
			Long sedeId, Long categoriaId) {
		return this.detallePortafolioViewServiceRemote.consultarMedicamentosBySedeIdAndCategoriaId(sedeId,categoriaId);
	}

	/**
	 * Consulta los procedimientos propios de la sede
	 * @param sedeId Identificador de la sede
	 * @return Lista {@link - ProcedimientoPropioPortafolioDto}
	 */
	public List<ProcedimientoPropioPortafolioDto> consultarServiciosPropiosBySedeId(
			Long sedeId) {
		return this.detallePortafolioViewServiceRemote
				.consultarServiciosPropiosBySedeId(sedeId);
	}

	/**
	 * Consulta los paquetes de la sede
	 * @param sedeId Identificador de la sede
	 * @return Lista {@link - PaquetePortafolioDto}
	 */
	public List<PaquetePortafolioDto> consultarPaquetesBySedeId(Long sedeId) {
		return this.detallePortafolioViewServiceRemote.consultarPaquetesBySedeId(sedeId);
	}

	/**
	 * Consulta los grupos de los traslados por sede
	 * @param sedeId Identificador de la sede
	 * @return Lista de {@link GrupoTransporteDto}
	 */
	public List<GrupoTransporteDto> consultarGrupoTrasaladosBySedeId(Long sedeId) {
		return this.detallePortafolioViewServiceRemote.consultarGrupoTrasaladosBySedeId(sedeId);
	}

	/**
	 * Consulta los transportes pertenecientes a una sede por grupo
	 * @param sedeId identificador de la sede
	 * @param grupoId identificador del grupo tranporte
	 * @return lista de {@link - TransportePortafolioDto}
	 */
	public List<TransportePortafolioDto> consultarTrasladosBySedeIdAndGrupoId(
			Long sedeId, Integer grupoId) {
		return this.detallePortafolioViewServiceRemote.consultarTrasladosBySedeIdAndGrupoId(sedeId, grupoId);
	}

	/**
	 * Devuelve el arreglo de bytes de los documentos de una sede
	 * @param sedeId
	 * @return arreglo de byte
	 * @throws ConexiaBusinessException 
	 * @throws IOException 
	 */
	public byte[] obtenerByteArrayZipDocumentosPorSede(Long sedeId) throws IOException, ConexiaBusinessException {
		return this.detallePortafolioViewServiceRemote.obtenerByteArrayZipDocumentosPorSede(sedeId);
	}

	/**
	 * Consulta los procedimientos asociados a un grupo servicio
	 * @param grupoServicioId Identificador del grupo servicio
	 * @return Lista de {@link - ProcedimientoPortafolioDto}
	 */
	public List<ProcedimientoPortafolioDto> consultarProcedimientoByGrupoServicioId(
			Long grupoServicioId) {
		return this.detallePortafolioViewServiceRemote.consultarProcedimientosByGrupoServicioId(grupoServicioId);
	}

	/**
	 * Consulta los diagnosticos del portafolio
	 * @param portafolioId Identificador del portafolio
	 * @return Lista de {@link - DiagnosticoPortafolioDto}
	 */
	public List<DiagnosticoPortafolioDto> consultarDiagnosticosPortafolio(
			Long portafolioId) {
		return this.detallePortafolioViewServiceRemote.consultarDiagnosticosPortafolio(portafolioId);
	}

	/**
	 * Consulta los procedimientos asociados a un paquete
	 * @param paqueteId Identificador del paquete
	 * @return lista de {@link - ProcedimientoPaqueteDto}
	 */
	public List<ProcedimientoPaqueteDto> consultarProcedimientosPaquete(Long paqueteId) {
		return this.detallePortafolioViewServiceRemote.consultarProcedimientosPaquete(paqueteId);
	}

	/**
	 * Consulta los medicamentos asociados a un paquete
	 * @param paqueteId Identificador del paquete
	 * @return lista de {@link - MedicamentoPortafolioDto}
	 */
	public List<MedicamentoPortafolioDto> consultarMedicamentosPaquete(Long paqueteId) {
		return this.detallePortafolioViewServiceRemote.consultarMedicamentosPaquete(paqueteId);
	}

	/**
	 * Consulta los insumos asociados a un paquete
	 * @param paqueteId Identificador del paquete
	 * @return lista de {@link - InsumoPortafolioDto}
	 */
	public List<InsumoPortafolioDto> consultarInsumosPaquete(Long paqueteId) {
		return this.detallePortafolioViewServiceRemote.consultarInsumosPaquete(paqueteId);
	}

	/**
	 * Consulta los insumos asociados a un paquete
	 * @param paqueteId Identificador del paquete
	 * @return lista de {@link - TransportePortafolioDto}
	 */
	public List<TransportePortafolioDto> consultarTrasladosPaquete(Long paqueteId) {
		return detallePortafolioViewServiceRemote.consultarTrasladosPaquete(paqueteId);
	}


}
