package com.conexia.contratacion.portafolio.services.transactional.prestador.boundary;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.apache.commons.lang3.StringUtils;
import org.jboss.ejb3.annotation.TransactionTimeout;

import com.conexia.contratacion.commons.dto.maestros.TarifaPropuestaProcedimientoDto;
import com.conexia.contratacion.commons.dto.capita.ProcedimientoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.capita.ProcedimientoServicioPortafolioSedeDto;
import com.conexia.contratacion.commons.dto.capita.ResultadoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.capita.ServicioPortafolioSedeDto;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.ProcedimientoServicioPortafolioTransactionalRemote;
import com.conexia.contratacion.portafolio.services.transactional.prestador.controls.ProcedimientoServicioTransactionalControl;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;

/**
 *
 * @author <a href="dgarcia@conexia.com">David Garcia H</a>
 */
@Stateless
@Remote(ProcedimientoServicioPortafolioTransactionalRemote.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class ProcedimientoServicioPortafolioTransactionalBoundary implements
		ProcedimientoServicioPortafolioTransactionalRemote {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2409016559668518022L;

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;
	
	@Inject
	private Log logger;

	@Inject
	private ProcedimientoServicioTransactionalControl procedimientoServicioTransactional;

	@Override
	public void cambiarEstadoTodosProcedimiento(
			ServicioPortafolioSedeDto servicioPortafolioSede,
			Map<String, Object> filters) throws ConexiaBusinessException {

		StringBuilder queryString = new StringBuilder();
		Map<String, Object> parametros = procedimientoServicioTransactional
				.generarUpdateProcedimientoServicio(queryString,
						servicioPortafolioSede);

		procedimientoServicioTransactional.generarWhereProcedimientoServicio(
				queryString, servicioPortafolioSede, filters, parametros);

		Query query = em.createQuery(queryString.toString());

		for (Entry<String, Object> parametro : parametros.entrySet()) {
			query.setParameter(parametro.getKey(), parametro.getValue());
		}

		actualizarServicio(query.executeUpdate(), servicioPortafolioSede);
	}

	@Override
	public void cambiarEstadoProcedimiento(
			ProcedimientoServicioPortafolioSedeDto procedimientoModificado)
			throws ConexiaBusinessException {

		int registrosActualizados = em
				.createQuery(
						"UPDATE ProcedimientoServicioPortafolioSede s SET s.habilitado = :habilitado WHERE s.id = :procedimientoServicioId")
				.setParameter("procedimientoServicioId",
						procedimientoModificado.getId())
				.setParameter("habilitado",
						procedimientoModificado.getHabilitado())
				.executeUpdate();

		ServicioPortafolioSedeDto servicioPortafolio = procedimientoModificado
				.getServicioPortafolioSede();
		
		actualizarServicio(registrosActualizados, servicioPortafolio);
	}

	private void actualizarServicio(int registrosActualizados,
			ServicioPortafolioSedeDto servicioPortafolio) {
		/*
		 * Solo si modifico por lo menos un registro se actualiza el estado del
		 * servicio
		 */
		if (registrosActualizados > 0) {
			/*
			 * Se valida cuantos procedimientos tiene habilitado el servicio, por lo
			 * contrario lo deshabilitara
			 */
			int conteoProcedimientos = em
					.createQuery(
							"SELECT CAST(COUNT(p.id) as integer) "
									+ "FROM ProcedimientoServicioPortafolioSede p "
									+ "WHERE p.habilitado IS TRUE "
									+ "AND p.servicioPortafolioSede.id = :servicioPortafolioSedeId ",
							Integer.class)
					.setParameter("servicioPortafolioSedeId",
							servicioPortafolio.getId()).getSingleResult();

			/*
			 * Si el conteo es mayor a cero se habilitara el servicio
			 */
			em.createQuery("  UPDATE ServicioPortafolioSede s "
							+ "  SET s.habilitado = :habilitado "
							+ "WHERE s.id = :servicioPortafolioId")
					.setParameter("servicioPortafolioId", servicioPortafolio.getId())
					.setParameter("habilitado", conteoProcedimientos > 0)
					.executeUpdate();
		}
	}

	@Override
	public void cambiarCodigoInternoProcedimiento(
			ProcedimientoServicioPortafolioSedeDto procedimientoModificado)
			throws ConexiaBusinessException {

		em.createQuery(
				"UPDATE ProcedimientoServicioPortafolioSede s SET s.codigoInterno = :codigoInterno WHERE s.id = :procedimientoServicioId")
				.setParameter("procedimientoServicioId",
						procedimientoModificado.getId())
				.setParameter("codigoInterno",
						procedimientoModificado.getCodigoInterno())
				.executeUpdate();
	}

	@Override
	public void eliminarProcedimientosPortafolio(Long prestadorId) throws ConexiaBusinessException {
		em.createNamedQuery("ProcedimientoPortafolio.eliminarProcedimientoPortafolio")
			.setParameter("prestadorId", prestadorId)
			.executeUpdate();		
	}

	@Override
	public void agregarGrupoServicioByPrestador(Long prestadorId) throws ConexiaBusinessException {
		em.createNamedQuery("GrupoServicio.insertarGrupoServicioHabilitado")
		.setParameter("prestadorId", prestadorId)
		.executeUpdate();
	}
	
	@Override
	@TransactionTimeout(unit = TimeUnit.MINUTES, value = 30)
	public int insertarProcedimientosPortafolio(List<TarifaPropuestaProcedimientoDto> listProcedimientoPortafolio) throws ConexiaBusinessException {
		Query query = em.createNamedQuery("ProcedimientoPortafolio.insertarProcedimientoPortafolio");
		Integer contador = 0;
		listProcedimientoPortafolio.forEach(procedimientoPortafolio->{
			try{
				query.setParameter("prestadorId", procedimientoPortafolio.getPrestadorId())
				.setParameter("sedePrestadorId", procedimientoPortafolio.getSedeId())
				.setParameter("servicioId", procedimientoPortafolio.getServicioId())
				.setParameter("procedimientoId", procedimientoPortafolio.getProcedimientoId())
				.setParameter("tarifarioId", Objects.nonNull(procedimientoPortafolio.getTarifarioId()) ? procedimientoPortafolio.getTarifarioId() : 5)
				.setParameter("porcentajeNegociado", procedimientoPortafolio.getPorcentajePropuesto())
				.setParameter("valorNegociado", procedimientoPortafolio.getValorPropuesto())
				.executeUpdate();
			}catch(Exception e){
				logger.error("No se inserto el procedimiento portafolio: " 
					 + " prestadorId:"+ procedimientoPortafolio.getPrestadorId()
					 + " sedePrestadorId: "+ procedimientoPortafolio.getSedeId() 
					 + " servicioId: "+ procedimientoPortafolio.getServicioId() 
					 + " procedimientoId: "+ procedimientoPortafolio.getProcedimientoId()
					 + " tarifarioId: " +procedimientoPortafolio.getTarifarioId()
					 + " porcentajeNegociado: "+ procedimientoPortafolio.getPorcentajePropuesto()
					 + " valorNegociado:" + procedimientoPortafolio.getValorPropuesto(),e);				
			}
		});
		return contador;
	}

	@Override
	public void prepararPortafolioCapitaByPrestador(Long prestadorId) throws ConexiaBusinessException {
		logger.info("---> Elimina eliminarProcedimientoServicioPortafolioSede");
		em.createNamedQuery("ProcedimientoServicioPortafolioSede.eliminarProcedimientoServicioPortafolioSede")
			.setParameter("prestadorId", prestadorId).executeUpdate();
		logger.info("---> Elimina eliminarMedicamentoPortafolioSede");
		em.createNamedQuery("MedicamentoPortafolioSede.eliminarMedicamentoPortafolioSede")
			.setParameter("prestadorId", prestadorId).executeUpdate();
		logger.info("---> Elimina eliminarCategoriaMedicamentoPortafolioSede");
		em.createNamedQuery("CategoriaMedicamentoPortafolioSede.eliminarCategoriaMedicamentoPortafolioSede")
			.setParameter("prestadorId", prestadorId).executeUpdate();
		logger.info("---> Inserta insertarOfertaPrestador");
		em.createNamedQuery("OfertaPrestador.insertarOfertaPrestador")
			.setParameter("prestadorId", prestadorId).executeUpdate();
		logger.info("---> Inserta insertarPortafolioSede");
		em.createNamedQuery("PortafolioSede.insertarPortafolioSede")
			.setParameter("prestadorId", prestadorId).executeUpdate();
		logger.info("---> Inserta insertarOfertaSedePrestador");
		em.createNamedQuery("OfertaSedePrestador.insertarOfertaSedePrestador")
			.setParameter("prestadorId", prestadorId)
			.executeUpdate();		
		logger.info("---> Insertar Categorias de Medicamentos");
		em.createNamedQuery("CategoriaMedicamentoPortafolioSede.insertarCategoriasMedicamento")
			.setParameter("prestadorId", prestadorId)
			.setParameter("habilitado", true)
			.executeUpdate();	
		logger.info("---> Inserta insertarServicioPortafolioSede");
		em.createNamedQuery("ServicioPortafolioSede.insertarServicioPortafolioSede")
			.setParameter("prestadorId", prestadorId)
			.setParameter("habilitado", true)
			.executeUpdate();
		// Servicio que no estan en el Reps
		Query query = em.createNamedQuery("ServicioPortafolioSede.insertarServicioPortafolioSedeSinReps");
		query.setParameter("codigo", "1").setParameter("habilitado", true).executeUpdate();
		query.setParameter("codigo", "7").setParameter("habilitado", true).executeUpdate();
		query.setParameter("codigo", "8").setParameter("habilitado", true).executeUpdate();		
	}

	@Override
	@TransactionTimeout(unit = TimeUnit.MINUTES, value = 70)
	public void crearPortafolioCapitaByPrestador(Long prestadorId) {
		operacionesComunes(prestadorId, false);	
		logger.info("---> Insertar MedicamentoPortafolioSede");
		em.createNamedQuery("MedicamentoPortafolioSede.insertarMasivoMedicamentoPortafolioSede")
			.setParameter("prestadorId", prestadorId)
			.setParameter("habilitado", false)
			.executeUpdate();
	}
	
	@Override
	@TransactionTimeout(unit = TimeUnit.MINUTES, value = 70)
	public boolean crearPortafolioCapitaFromReferenteCapitaByPrestador(Long prestadorId) {
		// realiza el llamado a las opreciones comunes de creación de portafolio de Cápita
		operacionesComunes(prestadorId, true);		
		logger.info("---> Insertar MedicamentoPortafolioSede Basado en el referente Emssanar");
		em.createNamedQuery("MedicamentoPortafolioSede.insertarBaseMedicamentoPortafolioSede")
			.setParameter("prestadorId", prestadorId)
			.setParameter("habilitado", true)
			.executeUpdate();
		
		return true;
	}
	
	private void operacionesComunes(Long prestadorId, boolean habilitarPortafolio) {
		em.createNamedQuery("OfertaPrestador.insertarOfertaPrestador")
		.setParameter("prestadorId", prestadorId)
		.executeUpdate();
		logger.info("---> Inserta insertarPortafolioSede");
		em.createNamedQuery("PortafolioSede.insertarPortafolioSede")
			.setParameter("prestadorId", prestadorId)
			.executeUpdate();

		logger.info("---> Inserta insertarOfertaSedePrestador");
		
		// Servicio que no estan en el Reps
		Query query = em.createNamedQuery("ServicioPortafolioSede.insertarServicioPortafolioSedeSinReps");
		query.setParameter("codigo", "1").setParameter("habilitado", habilitarPortafolio).executeUpdate();
		query.setParameter("codigo", "7").setParameter("habilitado", habilitarPortafolio).executeUpdate();
		query.setParameter("codigo", "8").setParameter("habilitado", habilitarPortafolio).executeUpdate();


		em.createNamedQuery("OfertaSedePrestador.insertarOfertaSedePrestador")
			.setParameter("prestadorId", prestadorId)
			.executeUpdate();
		logger.info("---> Inserta insertarServicioPortafolioSede");		
		
		em.createNamedQuery("ServicioPortafolioSede.insertarServicioPortafolioSede")
			.setParameter("prestadorId", prestadorId)
			.setParameter("habilitado", habilitarPortafolio)
			.executeUpdate();
		logger.info("---> Inserta insertarProcedimientoServicioPortafolioSedeConReps");
		em.createNamedQuery("ProcedimientoServicioPortafolioSede.insertarProcedimientoServicioPortafolioSedeConReps")
			.setParameter("prestadorId", prestadorId)
			.setParameter("habilitado", habilitarPortafolio)
			.executeUpdate();
		
		
		logger.info("---> Insertar Categorias de Medicamentos");
		em.createNamedQuery("CategoriaMedicamentoPortafolioSede.insertarCategoriasMedicamento")
			.setParameter("prestadorId", prestadorId)
			.setParameter("habilitado", habilitarPortafolio)
			.executeUpdate();	
	}

	@Override
	@TransactionTimeout(unit = TimeUnit.MINUTES, value = 70)
	public int insertarProcedimientoServicioPortafolioSede(
			List<TarifaPropuestaProcedimientoDto> listProcedimientoPortafolioPropuesto)
			throws ConexiaBusinessException {
		Query query = em.createNamedQuery("ProcedimientoServicioPortafolioSede.insertarProcedimientoServicioPortafolioSede");
		Integer contador = 0;
		listProcedimientoPortafolioPropuesto.forEach(procedimientoPortafolio->{
			try{
				query.setParameter("prestadorId", procedimientoPortafolio.getPrestadorId())
				.setParameter("sedePrestadorId", procedimientoPortafolio.getSedeId())
				.setParameter("servicioId", procedimientoPortafolio.getServicioId())
				.setParameter("procedimientoId", procedimientoPortafolio.getProcedimientoId())
				.executeUpdate();
			}catch(Exception e){
				logger.error("No se inserto el procedimiento portafolio: " 
					 + " prestadorId:"+ procedimientoPortafolio.getPrestadorId()
					 + " sedePrestadorId: "+ procedimientoPortafolio.getSedeId() 
					 + " servicioId: "+ procedimientoPortafolio.getServicioId() 
					 + " procedimientoId: "+ procedimientoPortafolio.getProcedimientoId(),e);
			}
		});
		return contador;
	}

	@Override
	public ResultadoPortafolioReporteDto actualizarProcedimientoPortafolio(ProcedimientoPortafolioReporteDto procedimiento, Long portafolioId) {
		try {
			String codigoServicio = procedimiento.getServicio().split("-")[0].trim();
		
			if (!StringUtils.isNumeric(codigoServicio)) {
				return new ResultadoPortafolioReporteDto(procedimiento, "Codigo Servicio no numerico", false);
			}
			
			Query queryObtenerGrupoServicio = em.createNativeQuery("select gs.id, gs.tarifario_id, gs.porcentaje_negociacion from contratacion.grupo_servicio gs join contratacion.servicio_salud ss on gs.servicio_salud_id = ss.id "
					+ "where gs.portafolio_id = :portafolioId and ss.codigo = :codigo limit 1");
			queryObtenerGrupoServicio.setParameter("portafolioId", portafolioId);
			queryObtenerGrupoServicio.setParameter("codigo", codigoServicio);
			
			List grupoServicio = queryObtenerGrupoServicio.getResultList();
			if (grupoServicio == null || grupoServicio.isEmpty()) {
				//No hay grupo servicio, hay que crearlo
				Query insertarGrupoServicio = em.createNativeQuery("insert into contratacion.grupo_servicio (portafolio_id, servicio_salud_id, porcentaje_negociacion, " + 
						" intramural_ambulatorio, intramural_hospitalario, extramural_unidad_movil, extramural_domiciliario, " + 
						" extramural_otras, telemedicina_centro_ref, telemedicina_institucion_remisora, " + 
						" complejidad_baja, complejidad_media, complejidad_alta, tarifario_id, enum_status, estado, modalidad) " + 
						"select :portafolioId as portafolio_id, ss.id as servicio_salud_id, :cero as porcentaje_negociacion, " + 
						" case when sr.ambulatorio = :si then true else false end as intramural_ambulatorio, " + 
						" case when sr.hospitalario = :si then true else false end as intramural_hospitalario, " + 
						" case when sr.unidad_movil = :si then true else false end as extramural_unidad_movil, " + 
						" case when sr.domiciliario = :si then true else false end as extramural_domiciliario, " + 
						" case when sr.otras_estramural = :si then true else false end as extramural_otras, " + 
						" case when sr.centro_referencia = :si then true else false end as telemedicina_centro_ref, " + 
						" case when sr.institucion_remisora = :si then true else false end as telemedicina_institucion_remisora, " + 
						" case when sr.complejidad_baja = :si then true else false end as complejidad_baja, " + 
						" case when sr.complejidad_media = :si then true else false end as complejidad_media, " + 
						" case when sr.complejidad_alta = :si then true else false end as complejidad_alta, " + 
						" :uno as tarifario_id, :uno as enum_status, :estado as estado, :modalidad as modalidad " + 
						" from maestros.servicios_reps sr  " + 
						" join contratacion.sede_prestador sp on sp.codigo_habilitacion = sr.codigo_habilitacion " + 
						" join contratacion.servicio_salud ss on sr.servicio_codigo = cast (ss.codigo as integer) " + 
						" where sp.portafolio_id = :portafolioId and sr.servicio_codigo = :codigoServicio ");
				insertarGrupoServicio.setParameter("portafolioId", portafolioId);
				insertarGrupoServicio.setParameter("cero", 0L);
				insertarGrupoServicio.setParameter("si", "SI");
				insertarGrupoServicio.setParameter("uno", 1L);
				insertarGrupoServicio.setParameter("codigoServicio", Long.parseLong(codigoServicio));
				insertarGrupoServicio.setParameter("estado", "CARGADO_MINISTERIO");
				insertarGrupoServicio.setParameter("modalidad", "EVENTO");
				Integer insertado = insertarGrupoServicio.executeUpdate();
				logger.info("Se insertaron " + insertado + " grupo servicios");
				
				queryObtenerGrupoServicio.setParameter("portafolioId", portafolioId);
				queryObtenerGrupoServicio.setParameter("codigo", codigoServicio);
				
				grupoServicio = queryObtenerGrupoServicio.getResultList();
				if (grupoServicio == null || grupoServicio.isEmpty()) {
					return new ResultadoPortafolioReporteDto(procedimiento, "Grupo servicio no encontrado para codigoServicio: " + codigoServicio + " portafolio " + portafolioId, false);
				}
			}
			Integer grupoServicioId = ((Number)((Object[])grupoServicio.get(0))[0]).intValue();
			Integer grupoServicioTarifario = ((Number)((Object[])grupoServicio.get(0))[1]) != null ? ((Number)((Object[])grupoServicio.get(0))[1]).intValue() : null;
			Double grupoServicioPorcentaje = ((Number)((Object[])grupoServicio.get(0))[2]) != null ? ((Number)((Object[])grupoServicio.get(0))[2]).doubleValue() : null;
			
			if ("SI".equalsIgnoreCase(procedimiento.getEliminar())) {
				//hay que eliminar el procedimiento portafolio
				Query deleteProcedimientoPortafolio = em.createNativeQuery("delete from contratacion.procedimiento_portafolio pp " + 
						" using  maestros.procedimiento_servicio ps, maestros.procedimiento p" + 
						" where ps.id = pp.procedimiento_id and p.id = ps.procedimiento_id " + 
						" and p.codigo = :codigo and p.codigo_emssanar = :codigoEmssanar " + 
						" and pp.grupo_servicio_id = :grupoServicio ");
				deleteProcedimientoPortafolio.setParameter("codigo", procedimiento.getCodigo());
				deleteProcedimientoPortafolio.setParameter("codigoEmssanar", procedimiento.getCodigoEmssanar());
				deleteProcedimientoPortafolio.setParameter("grupoServicio", grupoServicioId);
				int result = deleteProcedimientoPortafolio.executeUpdate();
				logger.info("Se borraron " + result + " procedimiento portafolios");
				return new ResultadoPortafolioReporteDto(procedimiento, "Borrado " + result, true);
			} else {
				//hay que actualizar o insertar el procedimiento portafolio
				Query selectProcedimientoPortafolio = em.createNativeQuery("select pp.id from contratacion.procedimiento_portafolio pp " + 
						"join maestros.procedimiento_servicio ps on ps.id = pp.procedimiento_id " + 
						"join maestros.procedimiento p on p.id = ps.procedimiento_id " + 
						"where p.codigo = :codigo and p.codigo_emssanar = :codigoEmssanar " + 
						"and pp.grupo_servicio_id = :grupoServicio " + 
						"limit 1");
				selectProcedimientoPortafolio.setParameter("codigo", procedimiento.getCodigo());
				selectProcedimientoPortafolio.setParameter("codigoEmssanar", procedimiento.getCodigoEmssanar());
				selectProcedimientoPortafolio.setParameter("grupoServicio", grupoServicioId);
				List procedimientoPortafolioList = selectProcedimientoPortafolio.getResultList();
				
				Query comprobarTarifarioProcedimiento = em.createNativeQuery (  " SELECT tp.peso FROM contratacion.tarifarios_procedimientos tp " +
																				" JOIN  contratacion.tarifarios t on t.descripcion = :tarifa " +
																				" WHERE tp.cups = :codigo AND tp.tarifarios_codigo = t.codigo AND tp.peso > 0 LIMIT 1" );
				comprobarTarifarioProcedimiento.setParameter("codigo", procedimiento.getCodigo());
				comprobarTarifarioProcedimiento.setParameter("tarifa", procedimiento.getTarifario());
				List TarifarioProcedimientoList = comprobarTarifarioProcedimiento.getResultList();
				
				if ( ( TarifarioProcedimientoList == null || TarifarioProcedimientoList.isEmpty() ) && !( procedimiento.getTarifario().equals ( "TARIFA PROPIA" ) ) )
				{	
					return new ResultadoPortafolioReporteDto(procedimiento, "No se encontro relación con el tarifario", false);
				}
				
				if (procedimientoPortafolioList == null || procedimientoPortafolioList.isEmpty()) {
					StringBuilder queryInsert = new StringBuilder("insert into contratacion.procedimiento_portafolio (procedimiento_id, ");					
					if (StringUtils.isNotBlank(procedimiento.getCodigoIps())) {
						queryInsert.append("codigo_interno, ");
					}
					queryInsert.append("tarifario_id, ");
					if (StringUtils.isNotBlank(getPorcentajeNegociacion(procedimiento))) {
						queryInsert.append("porcentaje_negociacion, ");
					}
					
					queryInsert.append("valor, ");
					queryInsert.append("grupo_servicio_id, tarifa_diferencial, fecha_insert, modalidad, es_capita) ");
					
					queryInsert.append("select ps.id as procedimiento_id, ");
					if (StringUtils.isNotBlank(procedimiento.getCodigoIps())) {
						queryInsert.append(":codigoInterno as codigo_interno, ");
					}
					queryInsert.append("t.id, ");
					if (StringUtils.isNotBlank(getPorcentajeNegociacion(procedimiento))) {
						queryInsert.append(":porcentajeNegociacion as porcentaje_negociacion, ");
					}
					queryInsert.append("CASE WHEN ( t.id = 5 ) THEN :valor WHEN ( t.codigo = 'SOAT' ) THEN ( SELECT ROUND ( CAST ( ( ( t.valor * tp.peso ) * ( 1 + ( :porcentajeNegociacion / 100.0) ) ) AS NUMERIC ), -2 ) ");
					queryInsert.append("FROM contratacion.tarifarios_procedimientos tp WHERE tp.cups = p.codigo AND tp.tarifarios_codigo = t.codigo LIMIT 1) ");
					queryInsert.append("ELSE ( SELECT ROUND ( CAST ( ( tp.peso * ( 1 + ( :porcentajeNegociacion / 100.0 ) ) ) AS NUMERIC ), -2 ) FROM contratacion.tarifarios_procedimientos tp WHERE tp.cups = p.codigo AND tp.tarifarios_codigo = t.codigo LIMIT 1 ) " );
					queryInsert.append("END AS valor, ");
					queryInsert.append(":grupoServicio as grupo_servicio_id, :tarifaDiferencial as tarifa_diferencial, now() as fecha_insert, ");
					queryInsert.append(":modalidad as modalidad, :esCapita as es_capita ");
					queryInsert.append(" from maestros.procedimiento p ");
					queryInsert.append(" JOIN maestros.procedimiento_servicio ps ON ");
					queryInsert.append(" p.id = ps.procedimiento_id JOIN contratacion.tarifarios t on t.descripcion = :tarifa ");
					queryInsert.append(" JOIN contratacion.grupo_servicio gs ON gs.id = :grupoServicio ");
					queryInsert.append(" WHERE p.codigo = :codigo and p.codigo_emssanar = :codigoEmssanar ");
					queryInsert.append(" AND ps.reps_cups = :codigoServicio ");
					queryInsert.append(" AND ps.estado = 1 ");
					queryInsert.append(" AND ps.complejidad <= CASE WHEN gs.complejidad_alta THEN 3 WHEN gs.complejidad_media THEN 2 WHEN complejidad_baja THEN 1 ELSE 0 END");
					
					Query insertarProcedimientoPortafolio = em.createNativeQuery(queryInsert.toString());
										
					if (StringUtils.isNotBlank(getPorcentajeNegociacion(procedimiento))) {
						insertarProcedimientoPortafolio.setParameter("porcentajeNegociacion", Double.parseDouble(getPorcentajeNegociacion(procedimiento)));
					}
					if ( StringUtils.isNotBlank ( procedimiento.getValor() ) ) {
						insertarProcedimientoPortafolio.setParameter ( "valor", Double.parseDouble ( procedimiento.getValor() ) );
					}
					else
					{
						insertarProcedimientoPortafolio.setParameter ( "valor", Double.parseDouble ( "0" ) );
					}
					insertarProcedimientoPortafolio.setParameter("grupoServicio", grupoServicioId);
					Boolean tarifaDiferencial = true;
					if (grupoServicioTarifario != 5 && Double.compare(grupoServicioPorcentaje, Double.parseDouble(getPorcentajeNegociacion(procedimiento))) == 0) {
						tarifaDiferencial = false;
					}
					insertarProcedimientoPortafolio.setParameter("tarifaDiferencial", tarifaDiferencial);
					insertarProcedimientoPortafolio.setParameter("modalidad", "EVENTO");
					insertarProcedimientoPortafolio.setParameter("esCapita", false);
					insertarProcedimientoPortafolio.setParameter("tarifa", procedimiento.getTarifario());
					insertarProcedimientoPortafolio.setParameter("codigo", procedimiento.getCodigo());
					insertarProcedimientoPortafolio.setParameter("codigoEmssanar", procedimiento.getCodigoEmssanar());
					insertarProcedimientoPortafolio.setParameter("codigoServicio", Integer.parseInt(codigoServicio));
					if (StringUtils.isNotBlank(procedimiento.getCodigoIps())) {
						insertarProcedimientoPortafolio.setParameter("codigoInterno", procedimiento.getCodigoIps());
					}
					try {
						Integer registrosInsertados = insertarProcedimientoPortafolio.executeUpdate();
						if (registrosInsertados == 0) {
							return new ResultadoPortafolioReporteDto(procedimiento, String.format("No se puede importar el procedimiento debido a que presenta alguno de los siguientes inconvenientes: complejidad, habilitación o vigencia del procedimiento."
									, procedimiento.getCodigo(), procedimiento.getCodigoEmssanar(), grupoServicioId, codigoServicio), false);
						} else {
							return new ResultadoPortafolioReporteDto(procedimiento, "Valores insertados " + registrosInsertados, true);
						}
					} catch(Exception e) {
						return new ResultadoPortafolioReporteDto(procedimiento, String.format("Error insertando error: %s, "
								+ "codigo %s, codigo emssanar %s, grupoServicio %s", e.getMessage()
								, procedimiento.getCodigo(), procedimiento.getCodigoEmssanar(), grupoServicioId), false);
					}
				} else {
					//actualizamos el procedimiento portafolio
					StringBuilder cadena = new StringBuilder("update contratacion.procedimiento_portafolio pp set ");
					cadena.append(" porcentaje_negociacion = :porcentajeNegociacion, ");
					cadena.append(" valor = CASE WHEN ( t.id = 5 ) THEN :valor WHEN ( t.codigo = 'SOAT' ) THEN ( SELECT ROUND ( CAST ( ( ( t.valor * tp.peso ) * ( 1 + ( :porcentajeNegociacion / 100.0) ) ) AS NUMERIC ), -2 ) ");
					cadena.append(" FROM contratacion.tarifarios_procedimientos tp WHERE tp.cups = :codigo AND tp.tarifarios_codigo = t.codigo LIMIT 1) ");
					cadena.append(" ELSE (  SELECT ROUND ( CAST ( ( tp.peso * ( 1 + ( :porcentajeNegociacion / 100.0 ) ) ) AS NUMERIC ), -2 ) FROM contratacion.tarifarios_procedimientos tp ");
					cadena.append(" WHERE tp.cups = :codigo AND tp.tarifarios_codigo = t.codigo LIMIT 1 ) END, ");					
					cadena.append(" fecha_update = now(), ");
					cadena.append(" tarifario_id = t.id, ");
					cadena.append(" tarifa_diferencial = :tarifaDiferencial, ");
					if (StringUtils.isNotBlank(procedimiento.getCodigoIps())) {
						cadena.append(" codigo_interno = :codigoInterno, ");
					}
					cadena.append(" modalidad = :modalidad ");
					cadena.append(" FROM contratacion.tarifarios t WHERE t.descripcion = :tarifa "); 
					cadena.append(" and pp.id = :procedimientoPortafolioId ");
					Query actualizarProcedimientoPortafolio = em.createNativeQuery(cadena.toString());
					if (StringUtils.isNotBlank(procedimiento.getPorcentajeNegociacion())) {
						actualizarProcedimientoPortafolio.setParameter("porcentajeNegociacion", Double.parseDouble(procedimiento.getPorcentajeNegociacion()));
					}
					else
					{
						actualizarProcedimientoPortafolio.setParameter("porcentajeNegociacion", Double.parseDouble("0"));
					}
					if (StringUtils.isNotBlank(procedimiento.getValor())) {
						actualizarProcedimientoPortafolio.setParameter("valor", Double.parseDouble(procedimiento.getValor()));
					}
					else
					{
						actualizarProcedimientoPortafolio.setParameter("valor", Double.parseDouble("0"));
					}
					actualizarProcedimientoPortafolio.setParameter("modalidad", "EVENTO");
					actualizarProcedimientoPortafolio.setParameter("tarifa", procedimiento.getTarifario());
					actualizarProcedimientoPortafolio.setParameter("procedimientoPortafolioId", (Number)procedimientoPortafolioList.get(0));
					actualizarProcedimientoPortafolio.setParameter("codigo", procedimiento.getCodigo());					
					Boolean tarifaDiferencial = true;
					if (grupoServicioTarifario != 5 && grupoServicioPorcentaje == Double.parseDouble(getPorcentajeNegociacion(procedimiento))) {
						tarifaDiferencial = false;
					}
					actualizarProcedimientoPortafolio.setParameter("tarifaDiferencial", tarifaDiferencial);
					if (StringUtils.isNotBlank(procedimiento.getCodigoIps())) {
						actualizarProcedimientoPortafolio.setParameter("codigoInterno", procedimiento.getCodigoIps());
					}
					Integer registrosActualizados = actualizarProcedimientoPortafolio.executeUpdate();
					return new ResultadoPortafolioReporteDto(procedimiento, "Valores actualizados " + registrosActualizados, true);
				}
			}
		} catch (Exception e) {
			return new ResultadoPortafolioReporteDto(procedimiento, e.getMessage(), false);
		}
	}

	private String getPorcentajeNegociacion(ProcedimientoPortafolioReporteDto procedimiento) {
		if (StringUtils.isBlank(procedimiento.getPorcentajeNegociacion()) && "TARIFA_PROPIA".equalsIgnoreCase(procedimiento.getTarifario())) {
			return "0.0";
		}
		return procedimiento.getPorcentajeNegociacion();
	}
}
