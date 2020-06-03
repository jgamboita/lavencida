package co.conexia.negociacion.services.portafolio.boundary;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Normalizer;
import java.text.Normalizer.Form;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import co.conexia.negociacion.services.common.control.NegociacionFileRepositoryControl;

import com.conexia.contractual.utils.GeneradorZip;
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
import com.conexia.contratacion.commons.dto.maestros.DocumentoSedeDto;
import com.conexia.contratacion.commons.dto.maestros.GrupoServicioDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoPaqueteDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.portafolio.DetallePortafolioViewServiceRemote;

/**
 * Boundary para las consultas del detalle portafolio
 * @author jjoya
 *
 */
@Stateless
@Remote(DetallePortafolioViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class DetallePortafolioViewBoundary implements DetallePortafolioViewServiceRemote {
	
	@Inject
	private NegociacionFileRepositoryControl fileRepository;

	@PersistenceContext(unitName = "contractualDS")
	EntityManager em;
	
	/**
	 * Devuelve el arreglo de bytes de los documentos de un prestador
	 * @param prestadorId identificador del prestador
	 * @return arreglo de byte
	 * @throws IOException 
	 * @throws ConexiaBusinessException 
	 */
	public byte[] obtenerByteArrayZipDocumentosPorPrestador(Long prestadorId) throws IOException, ConexiaBusinessException{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		File log = null;		
		BufferedWriter writer = null;
		log = File.createTempFile("documentos_prestador", ".log");
		writer = new BufferedWriter(new FileWriter(log));
		
		GeneradorZip generadorZip = new GeneradorZip(outputStream);
		for(DocumentoSedeDto documento : this.obtenerDocumentosPorPrestador(prestadorId)){
			String codigoSede = documento.getSede().getCodigoPrestador() + "_" + documento.getSede().getCodigoSede();
			String nombreArchivo = Normalizer.normalize(documento.getNombreArchivo(), Form.NFD)
		            .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
			
			byte[] fileBuffer = fileRepository.obteneArchivo(documento.getNombreArchivoServidor(), fileRepository.getRutaRepositorioFromBD());

			generadorZip.crearArchivo(codigoSede + "/" + nombreArchivo,
						new ByteArrayInputStream(fileBuffer));
		}
		
		if (log != null) {
			if(writer != null){
				writer.close();
			}	
			generadorZip.crearArchivo("documentos_prestador.log", log);			
		}
		
		generadorZip.finalizar();		
		return outputStream.toByteArray();
	}
	
	/**
	 * Devuelve el arreglo de bytes de los documentos de una sede
	 * @param sedeId identificador de la sede
	 * @return arreglo de bytes
	 * @throws IOException 
	 * @throws ConexiaBusinessException 
	 */
	public byte[] obtenerByteArrayZipDocumentosPorSede(Long sedeId) throws IOException, ConexiaBusinessException{
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		
		File log = null;		
		BufferedWriter writer = null;
		log = File.createTempFile("documentos_sedes_prestador", ".log");
		writer = new BufferedWriter(new FileWriter(log));
		
		GeneradorZip generadorZip = new GeneradorZip(outputStream);
		List<DocumentoSedeDto> documentoSedeDtos = this.obtenerDocumentosPorSedePrestadorId(sedeId);
		if(documentoSedeDtos.isEmpty()){
		    return null;
        } else{
            for(DocumentoSedeDto documento : documentoSedeDtos){
                String codigoSede = documento.getSede().getCodigoPrestador() + "_" + documento.getSede().getCodigoSede();
                String nombreArchivo = Normalizer.normalize(documento.getNombreArchivo(), Form.NFD)
                        .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");

                byte[] fileBuffer = fileRepository.obteneArchivo(documento.getNombreArchivoServidor(), fileRepository.getRutaRepositorioFromBD());

                generadorZip.crearArchivo(codigoSede + "/" + nombreArchivo,
                            new ByteArrayInputStream(fileBuffer));
            }

            if (log != null) {
                if(writer != null){
                    writer.close();
                }
                generadorZip.crearArchivo("documentos_sedes_prestador.log", log);
            }

            generadorZip.finalizar();

        }
		return outputStream.toByteArray();
	}

	/**
	 * Obtiene los documentos de las sedes asociadas a un prestador
	 * @param prestadorId Identificador del prestador
	 * @return Lista de {@link - DocumentoSedeDto}
	 */
	public List<DocumentoSedeDto> obtenerDocumentosPorPrestador(Long prestadorId) {
		return em
				.createNamedQuery("DocumentacionSede.findDtoByPrestadorId",
						DocumentoSedeDto.class)
				.setParameter("prestadorId", prestadorId).getResultList();
	}
	
	/**
	 * Obtiene los documentos de las sedes asociadas a un prestador
	 * @param sedeId Identificador de la sede
	 * @returnLista de {@link - DocumentoSedeDto}
	 */
	public List<DocumentoSedeDto> obtenerDocumentosPorSedePrestadorId(Long sedeId) {
		return em
				.createNamedQuery("DocumentacionSede.findDtoBySedePrestadorId",
						DocumentoSedeDto.class)
				.setParameter("sedeId", sedeId).getResultList();
	}
	
	/**
	 * Consulta las sedes del prestador y sus tecnologias
	 * @param prestadorId Identificador del prestador
	 * @return Lista de {@link - SedePrestadorDto}
	 */
	public List<SedePrestadorDto> consultarTecnologiasSedesByPrestadorId(Long prestadorId){
		return em
				.createNamedQuery(
						"SedePrestador.findTecnologiasSedePrestadorByPrestadorId",
						SedePrestadorDto.class)
				.setParameter("idPrestador", prestadorId)
				.setParameter("enumStatus", 1).getResultList();
	}
	
	/**
	 * Consulta los servicios de habilitacion por sede
	 * @param sedeId Identificador de la sede
	 * @return Lista de {@link - GrupoServicioDto}
	 */
	public List<GrupoServicioDto> consultarServiciosHabilitacionBySedeId(Long sedeId){
		return em
				.createNamedQuery("GrupoServicio.findDtoBySedeId",
						GrupoServicioDto.class)
				.setParameter("sedePrestadorId", sedeId).getResultList();
	}
	
	/**
	 * Consulta las categorias de los medicamentos que pertenecen a los medicamentos
	 * @param idSede Identificador de la sede
	 * @return Lista de {@link - CategoriaMedicamentoDto}
	 */
	public List<CategoriaMedicamentoDto> consultarCategoriasMedicamento(Long idSede){
		return em
				.createNamedQuery("CategoriaMedicamento.findBySedePrestador",
						CategoriaMedicamentoDto.class)
				.setParameter("idSede", idSede).getResultList();
	}
	
	/**
	 * Consulta los medicamentos asociados a una sede y categoria
	 * @param sedeId Identificador de la sede
	 * @param categoriaId Identificador de la categria
	 * @return Lista de {@link - MedicamentosDto}
	 */
	public List<MedicamentoPortafolioDto> consultarMedicamentosBySedeIdAndCategoriaId(
			Long sedeId, Long categoriaId){
		return em
				.createNamedQuery(
						"MedicamentoPortafolio.findDtoBySedeIdAndCategoriaId",
						MedicamentoPortafolioDto.class)
				.setParameter("categoriaId", categoriaId)
				.setParameter("sedeId", sedeId).getResultList();
	}
	
	/**
	 * Consulta los procedimientos propios de la sede
	 * @param sedeId Identificador de la sede
	 * @return Lista {@link - ProcedimientoPropioPortafolioDto}
	 */
	public List<ProcedimientoPropioPortafolioDto> consultarServiciosPropiosBySedeId(
			Long sedeId){
		return em
				.createNamedQuery(
						"ProcedimientoPropioPortafolio.findProcedimientosPropiosBySedeId",
						ProcedimientoPropioPortafolioDto.class)
				.setParameter("sedeId", sedeId).getResultList();
	}
	
	/**
	 * Consulta los paquetes de la sede
	 * @param sedeId Identificador de la sede
	 * @return Lista {@link - PaquetePortafolioDto}
	 */
	public List<PaquetePortafolioDto> consultarPaquetesBySedeId(Long sedeId){
		return em.createNamedQuery(
				"VPaquetePortafolioServicios.findPaquetePortafolioBySedeId",
				PaquetePortafolioDto.class).setParameter("idSede",
				sedeId == null ? 0 : sedeId).getResultList();
	}
	
	/**
	 * Consulta los grupos de los traslados por sede
	 * @param sedeId Identificador de la sede
	 * @return Lista de {@link GrupoTransporteDto}
	 */
	@Override
	public List<GrupoTransporteDto> consultarGrupoTrasaladosBySedeId(Long sedeId){
		return em
				.createNamedQuery("GrupoTransporte.findBySedeId",
						GrupoTransporteDto.class)
				.setParameter("idSede", sedeId).getResultList();
        
	}
	
	/**
	 * Consulta los transportes pertenecientes a una sede por grupo
	 * @param sedeId identificador de la sede
	 * @param grupoId identificador del grupo tranporte
	 * @return lista de {@link - TransportePortafolioDto}
	 */
	public List<TransportePortafolioDto> consultarTrasladosBySedeIdAndGrupoId(
			Long sedeId, Integer grupoId){
		
		return em.createNamedQuery("TransportePortafolio.findDtoBySedeIdAndGrupoId", TransportePortafolioDto.class)
				.setParameter("sedeId", sedeId).setParameter("grupoId", grupoId)
				.getResultList();
	}
	
	/**
	 * Consulta los procedimientos asociados a un grupo servicio
	 * @param grupoServicioId Identificador del grupo servicio
	 * @return Lista de {@link - ProcedimientoPortafolioDto}
	 */
	public List<ProcedimientoPortafolioDto> consultarProcedimientosByGrupoServicioId(
			Long grupoServicioId) {
		return em
				.createNamedQuery(
						"ProcedimientoPortafolio.findTarifariosByGrupoServicioId",
						ProcedimientoPortafolioDto.class)
				.setParameter("grupoServicioId", grupoServicioId)
				.getResultList();
	}
	

	/**
	 * Consulta los diagnosticos del portafolio
	 * @param portafolioId Identificador del portafolio
	 * @return Lista de {@link - DiagnosticoPortafolioDto}
	 */
	public List<DiagnosticoPortafolioDto> consultarDiagnosticosPortafolio(
			Long portafolioId){
		return em.createNamedQuery("DiagnosticoPortafolio.findDtoByPortafolio", DiagnosticoPortafolioDto.class)
				.setParameter("portafolioId", portafolioId).getResultList();
	}
	
	/**
	 * Consulta los procedimientos asociados a un paquete
	 * @param paqueteId Identificador del paquete
	 * @return lista de {@link - ProcedimientoPaqueteDto}
	 */
	public List<ProcedimientoPaqueteDto> consultarProcedimientosPaquete(Long paqueteId){
		return em
				.createNamedQuery("ProcedimientoPaquete.findDtoByPaqueteId",
						ProcedimientoPaqueteDto.class)
				.setParameter("paqueteId", paqueteId).getResultList();
	}
	
	/**
	 * Consulta los medicamentos asociados a un paquete
	 * @param paqueteId Identificador del paquete
	 * @return lista de {@link - MedicamentoPortafolioDto}
	 */
	public List<MedicamentoPortafolioDto> consultarMedicamentosPaquete(Long paqueteId){
		return em
				.createNamedQuery("MedicamentoPortafolio.findDtoByPortafolioId",
						MedicamentoPortafolioDto.class)
				.setParameter("portafolioId", paqueteId).getResultList();
	}
	
	/**
	 * Consulta los insumos asociados a un paquete
	 * @param paqueteId Identificador del paquete
	 * @return lista de {@link - InsumoPortafolioDto}
	 */
	public List<InsumoPortafolioDto> consultarInsumosPaquete(Long paqueteId){
		return em
				.createNamedQuery("InsumoPortafolio.findDtoByPortafolioId",
						InsumoPortafolioDto.class)
				.setParameter("portafolioId", paqueteId).getResultList();
	}
	
	/**
	 * Consulta los traslados asociados a un paquete
	 * @param paqueteId Identificador del paquete
	 * @return lista de {@link - TransportePortafolioDto}
	 */
	public List<TransportePortafolioDto> consultarTrasladosPaquete(Long paqueteId){
		return em
				.createNamedQuery("TransportePortafolio.findDtoByPortafolioId",
						TransportePortafolioDto.class)
				.setParameter("portafolioId", paqueteId).getResultList();
	}
}
