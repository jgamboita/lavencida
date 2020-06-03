package com.conexia.contratacion.portafolio.services.view.prestador.boundary;

import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contractual.utils.DateUtils;
import com.conexia.contratacion.commons.dto.maestros.DocumentacionPrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.GrupoInsumoDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.TarifaPropuestaProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contratacion.commons.dto.capita.OfertaPrestadorDto;
import com.conexia.contratacion.portafolio.definitions.view.prestador.PrestadorViewServiceRemote;
import com.conexia.contratacion.portafolio.services.commons.controls.FileRepositoryControl;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;

/**
 *
 * @author Andrés Mise Olivera
 */
@Stateless
@Remote(PrestadorViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class PrestadorViewBoundary implements PrestadorViewServiceRemote {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5466928585524463477L;

	@PersistenceContext(unitName ="contractualDS")
    private EntityManager em;

    @Inject
    private DateUtils dateUtil;

    @Inject
    private FileRepositoryControl repositoryControl;
    @Inject
	private Log log;
    
    private enum CODIGO_SERVICIO_ENUM{
    	EMPRESARIAL_INSUMOS("1"),
    	EMPRESARIAL_MODELO("7"),
    	EMPRESARIAL_ACTIVIDADES("8");    	
    	private String codigo;    	
    	private CODIGO_SERVICIO_ENUM(String codigo){
    		this.codigo = codigo;
    	}
    	public String getCodigo(){
    		return this.codigo;
    	}
    	
    }

    @Override
    public PrestadorDto buscarPrestador(PrestadorDto prestador) {
        PrestadorDto p = prestador;
        try {
            p = em.createNamedQuery("Prestador.findByTipoAndNumeroDocumento", PrestadorDto.class)
                    .setParameter("tipoIdentificacion", prestador.getTipoIdentificacion().getId())
                    .setParameter("numeroDocumento", prestador.getNumeroDocumento())
                    .getSingleResult();
            p.setFechaFinVigencia(
                    this.dateUtil.asDate(
                            this.dateUtil.asLocalDate(
                                    p.getFechaInicioVigencia()).plusMonths(
                                    p.getMesesVigencia().longValue()
                            )));
        } catch (NoResultException nre) {
            p.setUsuarioConexia(false);
        }
        return p;
    }

    @Override
    public PrestadorDto buscarPrestador(Long prestadorId) {
        try {
            PrestadorDto prestador = em.createNamedQuery("Prestador.findBasicDataById", PrestadorDto.class)
                    .setParameter("prestadorId", prestadorId)
                    .getSingleResult();
            prestador.setGrupos(em.createNamedQuery("GrupoInsumo.findByPrestadorId", GrupoInsumoDto.class)
                    .setParameter("prestadorId", prestadorId)
                    .getResultList());
            prestador.setDocumentos(em.createNamedQuery("DocumentacionPrestador.findByPrestadorId", DocumentacionPrestadorDto.class)
                    .setParameter("prestadorId", prestadorId)
                    .getResultList());
            prestador.setFechaFinVigencia(
                    this.dateUtil.asDate(
                            this.dateUtil.asLocalDate(
                                    prestador.getFechaInicioVigencia()).plusMonths(
                                    prestador.getMesesVigencia().longValue()
                            )));
            return prestador;
        } catch (NoResultException nre) {
            return null;
        }
    }

    @Override
    public List<PrestadorDto> buscarPrestadores(List<EstadoPrestadorEnum> estados) {
        return em.createNamedQuery("Prestador.findByState", PrestadorDto.class)
                .setParameter("estados", estados)
                .getResultList();
    }

    @Override
    public byte[] descargarArchivo(String nombreArchivo) throws ConexiaBusinessException {
        return repositoryControl.obteneArchivo(nombreArchivo,
                repositoryControl.getRutaRepositorioFromBD());
    }

    /**
     * @see PrestadorViewServiceRemote#obtenerPorUsuarioId(Integer)
     */
	public PrestadorDto obtenerPorUsuarioId(Integer usuarioId)
			throws ConexiaBusinessException {
		
		PrestadorDto prestador = null;
		try {
			log.info("I: usuario " + usuarioId);
			prestador = em.createNamedQuery("Prestador.obtenerPorUsuarioId", PrestadorDto.class)
							.setParameter("usuarioId", usuarioId)
							.getSingleResult();
		} catch (NoResultException e) {
			log.error("E: usuario ", e);
			prestador = new PrestadorDto();
		}
		
		return prestador;
	}
	
	@Override
	public OfertaPrestadorDto obtenerOfertaPresentarPorPrestadorIdYModalidadId(
			Long prestadorId, Integer modalidadId)
			throws ConexiaBusinessException {
		
		List<OfertaPrestadorDto> ofertas = em.createNamedQuery("OfertaPrestador.obtenerAPresentarPorPrestadorIdYModalidadId",
				OfertaPrestadorDto.class).setParameter("prestadorId", prestadorId).setParameter("modalidadId", modalidadId).setMaxResults(1).getResultList();
		
		return ofertas.isEmpty() ? null : ofertas.get(0);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TarifaPropuestaProcedimientoDto> obtenerMejoresTarifasServicios(Long prestadorId,
			NegociacionModalidadEnum modalidadNegociacionEnum) {
		return em.createNativeQuery("SELECT sc.prestador_id,sns.servicio_id, snp.procedimiento_id, snp.tarifario_negociado_id, snp.porcentaje_negociado, "
					+ "		agrupacion.valor_negociado"
					+ " FROM contratacion.solicitud_contratacion sc "
					+ " JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = sc.negociacion_id "
					+ " JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id "
					+ " JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id"
					+ " JOIN (select sc.prestador_id,sns.servicio_id, snp.procedimiento_id, min(snp.valor_negociado) as valor_negociado "
					+ " FROM contratacion.solicitud_contratacion sc "
					+ " JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = sc.negociacion_id "
					+ " JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id "
					+ " JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id "
					+ " WHERE sc.estado_legalizacion_id='LEGALIZADA' "
					+ " AND sc.tipo_modalidad_negociacion =:modalidadNegociacion"
					+ " GROUP BY sc.prestador_id,sns.servicio_id, snp.procedimiento_id)as agrupacion "
					+ "		ON sns.servicio_id = agrupacion.servicio_id AND snp.procedimiento_id = agrupacion.procedimiento_id"
					+ " 	AND agrupacion.prestador_id = sc.prestador_id AND snp.valor_negociado = agrupacion.valor_negociado"
					+ " WHERE sc.estado_legalizacion_id='LEGALIZADA' "
					+ " AND sc.tipo_modalidad_negociacion =:modalidadNegociacion "
					+ " AND sc.prestador_id =:prestadorId "
					+ " GROUP BY sc.prestador_id,sns.servicio_id, snp.procedimiento_id, snp.tarifario_negociado_id, "
					+ "		snp.porcentaje_negociado,agrupacion.valor_negociado "
					+ " ORDER BY sns.servicio_id, snp.procedimiento_id", "Prestador.mejoresTarifasServicioMapping")
				.setParameter("modalidadNegociacion", modalidadNegociacionEnum.getDescripcion().toUpperCase())
				.setParameter("prestadorId", prestadorId)
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<TarifaPropuestaProcedimientoDto> obtenerSedesYServiciosNegociados(Long prestadorId,
			NegociacionModalidadEnum modalidadNegociacionEnum) {
		return em.createNativeQuery("SELECT sc.prestador_id, sn.sede_prestador_id, sns.servicio_id, snp.procedimiento_id"
				+ " FROM contratacion.solicitud_contratacion sc"
				+ " JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = sc.negociacion_id"
				+ " JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id"
				+ " JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id"
				+ " JOIN maestros.procedimiento_servicio ps on ps.id = snp.procedimiento_id and ps.estado = 1 "
				+ " JOIN contratacion.prestador pres on pres.id = sc.prestador_id"
				+ " JOIN maestros.servicios_reps reps on reps.nits_nit = pres.numero_documento "
				+ " JOIN contratacion.servicio_salud ss on ss.codigo = ''||reps.servicio_codigo and ss.id = sns.servicio_id"
				+ " WHERE sc.estado_legalizacion_id='LEGALIZADA'"
				+ " AND sc.tipo_modalidad_negociacion =:modalidadNegociacion  AND sc.prestador_id =:prestadorId"				
				+ " UNION "
				+ " SELECT sc.prestador_id, sn.sede_prestador_id, sns.servicio_id, snp.procedimiento_id"
				+ " FROM contratacion.solicitud_contratacion sc"
				+ " JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = sc.negociacion_id"
				+ " JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id"
				+ " JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id"
				+ " JOIN maestros.procedimiento_servicio ps on ps.id = snp.procedimiento_id and ps.estado = 1 "
				+ " JOIN contratacion.servicio_salud ss on ss.id = sns.servicio_id "
				+ "		and ss.codigo in ('"+CODIGO_SERVICIO_ENUM.EMPRESARIAL_ACTIVIDADES.getCodigo() 
												 +"','"+CODIGO_SERVICIO_ENUM.EMPRESARIAL_INSUMOS.getCodigo()
												 +"','"+CODIGO_SERVICIO_ENUM.EMPRESARIAL_MODELO.getCodigo()+"')"
				+ " WHERE sc.estado_legalizacion_id='LEGALIZADA'"
				+ " AND sc.tipo_modalidad_negociacion =:modalidadNegociacion  AND sc.prestador_id =:prestadorId"
				+ " GROUP BY sc.prestador_id, sn.sede_prestador_id, sns.servicio_id, snp.procedimiento_id "
				+ " ORDER BY prestador_id, sede_prestador_id, servicio_id, procedimiento_id ", "Prestador.serviciosPrestadosMapping")
			.setParameter("modalidadNegociacion", modalidadNegociacionEnum.getDescripcion().toUpperCase())
			.setParameter("prestadorId", prestadorId)
			.getResultList();	
	}

	
	@Override
	public List<Long> obtenerListadoPrestadores(NegociacionModalidadEnum modalidadNegociacionEnum){
		return em.createNamedQuery("Prestador.findPrestadorByModalidad", Long.class)
			.setParameter("modalidadNegociacion", modalidadNegociacionEnum)
			.setParameter("estadoLegalizacion", EstadoLegalizacionEnum.LEGALIZADA)			
			.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<MedicamentoNegociacionDto> obtenerMejoresTarifasMedicamentos(Long prestadorId,
			NegociacionModalidadEnum modalidadNegociacionEnum) {
		return em.createNativeQuery("SELECT sc.prestador_id,snm.medicamento_id, min(snm.valor_negociado) as valor_negociado "
				+ " FROM contratacion.solicitud_contratacion sc "
				+ " JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = sc.negociacion_id "
				+ " JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_negociacion_id = sn.id "
				+ " WHERE sc.estado_legalizacion_id='LEGALIZADA' "
				+ " AND sc.tipo_modalidad_negociacion =:modalidadNegociacion "
				+ " AND sc.prestador_id =:prestadorId "
				+ " GROUP BY sc.prestador_id,snm.medicamento_id"
				+ " ORDER BY sc.prestador_id,snm.medicamento_id", "Prestador.mejoresTarifasMedicamentoMapping")
				.setParameter("modalidadNegociacion", modalidadNegociacionEnum.getDescripcion().toUpperCase())
				.setParameter("prestadorId", prestadorId)
				.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<MedicamentoNegociacionDto> obtenerSedesYMedicamentosNegociados(Long prestadorId,
			NegociacionModalidadEnum modalidadNegociacionEnum) {
		return em.createNativeQuery("SELECT sc.prestador_id, sn.sede_prestador_id, snm.medicamento_id "
				+ "	 FROM contratacion.solicitud_contratacion sc "
				+ "	 JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = sc.negociacion_id "
				+ "	 JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_negociacion_id = sn.id"
				+ "	 WHERE sc.estado_legalizacion_id='LEGALIZADA' "
				+ "  AND sc.tipo_modalidad_negociacion =:modalidadNegociacion"
				+ "	 AND sc.prestador_id =:prestadorId "
				+ "	 GROUP BY sc.prestador_id, sn.sede_prestador_id, snm.medicamento_id"
				+ "	 ORDER BY sc.prestador_id, sn.sede_prestador_id, snm.medicamento_id", "Prestador.medicamentosNegociadosMapping")
			.setParameter("modalidadNegociacion", modalidadNegociacionEnum.getDescripcion().toUpperCase())
			.setParameter("prestadorId", prestadorId)
			.getResultList();		
	}
}
