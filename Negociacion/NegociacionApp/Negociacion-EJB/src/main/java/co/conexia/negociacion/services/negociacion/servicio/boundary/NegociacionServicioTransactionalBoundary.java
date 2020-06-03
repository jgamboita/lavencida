package co.conexia.negociacion.services.negociacion.servicio.boundary;

import co.conexia.negociacion.services.negociacion.control.EliminarTecnologiasAuditoriaControl;
import co.conexia.negociacion.services.negociacion.control.NegociacionControl;
import co.conexia.negociacion.services.negociacion.paquete.control.PaquetesNegociacionControl;
import co.conexia.negociacion.services.negociacion.servicio.control.ServicioNegociacionControl;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.model.contratacion.Tarifario;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.AfiliadoDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;
import com.conexia.contratacion.commons.dto.maestros.TipoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;
import com.conexia.negociacion.definitions.negociacion.servicio.NegociacionServicioTransactionalServiceRemote;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.*;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * Boundary para los servicios transactional de la negociacion servicio
 *
 * @author jjoya
 *
 */
@Stateless
@Remote(NegociacionServicioTransactionalServiceRemote.class)
public class NegociacionServicioTransactionalBoundary implements NegociacionServicioTransactionalServiceRemote {

	private static final String SERVICIO_MEDICAMENTO = "714";
	private static final int DECIMALES_APROXIMACION = 3;

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;

	@Inject
	private ServicioNegociacionControl servicioNegociacionControl;
        
        private final String NAME_NATIVE_QUERY_PORTAFOLIO_PROPUESTO = "aplicarTarifasPropuestasByNegociacionAndServicios";
        
    @Inject
    NegociacionControl negociacionControl;

	@Inject
    private PaquetesNegociacionControl paqueteNegociacionControl;

	@Inject
	private EliminarTecnologiasAuditoriaControl eliminarTecnologiasAuditoriaControl;

	@Inject
    protected Log logger;

	/**
	 * Elimina las sedeNegociacionServicio por negociacionId y servciosId
	 *
	 * @param negociacionId
	 * @param serviciosId
	 */
	@Override
	public void eliminarByNegociacionAndServicios(Long negociacionId,
			List<Long> serviciosId, Integer userId) {

		StringBuilder queryAuditoria = new StringBuilder();
		queryAuditoria.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarServicios())
		.append(" where sn.negociacion_id = :negociacionId and sns.servicio_id in (:serviciosId)");

		//Para registrar en auditoría los servicios a eliminar
		em.createNativeQuery(queryAuditoria.toString())
			.setParameter("negociacionId", negociacionId)
			.setParameter("serviciosId", serviciosId)
			.setParameter("userId", userId)
			.executeUpdate();

		em.createNamedQuery(
				"SedeNegociacionServicio.deleteByNegociacionIdAndServicios")
				.setParameter("negociacionId", negociacionId)
				.setParameter("serviciosId", serviciosId).executeUpdate();
	}

	@Override
	public void eliminarByNegociacionAndCapitulosAllProcedimientos(Long negociacionId,
			List<Long> capitulosId, Integer userId) throws ConexiaBusinessException {
		//Elimina los procedimientos
		servicioNegociacionControl.eliminarByNegociacionAndCapitulosAllProcedimientos(negociacionId, capitulosId, userId);

		StringBuilder queryAuditoriaCx = new StringBuilder();
		queryAuditoriaCx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarCapitulos())
		.append(" where sn.negociacion_id = :negociacionId and snc.capitulo_id in (:capitulosId)");

		//Registrar en auditoría los capítulos a eliminar
		em.createNativeQuery(queryAuditoriaCx.toString())
				.setParameter("userId", userId)
				.setParameter("negociacionId", negociacionId)
				.setParameter("capitulosId", capitulosId).executeUpdate();

		//Elimina el capítulo de la negociación
		em.createNamedQuery(
				"SedeNegociacionCapitulo.deleteByNegociacionIdAndCapitulo")
				.setParameter("negociacionId", negociacionId)
				.setParameter("capitulosId", capitulosId).executeUpdate();
	}

	/**
	 * Elimina las sedeNegociacionServicio por id
	 *
	 * @param serviciosId
	 *            Identificador del {@link - SedeNegociacionServicio}
	 */
	@Override
	public void eliminarById(List<Long> serviciosId, Integer userId) {

		StringBuilder queryAuditoria = new StringBuilder();
		queryAuditoria.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarServicios())
			.append(" where sns.id in (:serviciosId)");

		//Para registrar en auditoría la eliminación de servicios
		em.createNativeQuery(queryAuditoria.toString())
			.setParameter("serviciosId", serviciosId)
			.setParameter("userId", userId)
			.executeUpdate();

		em.createNamedQuery("SedeNegociacionServicio.deleteById")
				.setParameter("serviciosId", serviciosId).executeUpdate();
	}

	/**
	 * Elimina las sedeNegociacionProcedimiento por negociacion, servcio Salud y
	 * las ID de los procedimientos a eliminar
	 *
	 * @param negociacionId
	 *            La negociación actual.
	 * @param servicioId
	 *            El servicio al que pertenecen los procedimientos.
	 * @param procedimientosId
	 *            Lista con las IDs de los procedimientos a eliminar.
	 */
	@Override
	public void eliminarProcedimientosNegociacionByIdAndNegociacionAndServicio(
			Long negociacionId, Long servicioId, List<Long> procedimientosId, Integer userId) {

		StringBuilder auditoriaQuery = new StringBuilder();
		auditoriaQuery.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarProcedimientos())
		.append(" where sn.negociacion_id = :negociacionId and sns.servicio_id = :servicioId")
		.append(" and snp.procedimiento_id in (:procedimientosId)");

		//para insertar registro en auditoría de los procedimientos a eliminar
		em.createNativeQuery(auditoriaQuery.toString())
			.setParameter("negociacionId", negociacionId)
			.setParameter("servicioId", servicioId)
			.setParameter("procedimientosId", procedimientosId)
			.setParameter("userId", userId)
			.executeUpdate();

		em.createNamedQuery(
				"SedeNegociacionProcedimiento.deleteByIdAndNegociacionAndServicioSalud")
				.setParameter("negociacionId", negociacionId)
				.setParameter("servicioId", servicioId)
				.setParameter("procedimientosId", procedimientosId)
				.executeUpdate();
	}

	@Override
	public void eliminarProcedimientosNegociacionByIdAndNegociacionAndCapituloPGP(Long negociacionId, Long capituloId,
			List<Long> procedimientosId, Integer userId) {

		StringBuilder auditoriaQuery = new StringBuilder();
		auditoriaQuery.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarProcedimientosPgp())
		.append(" where sn.negociacion_id = :negociacionId AND snc.capitulo_id = :capituloId and snp.pto_id IN (:procedimientosId)");

		//para insertar en auditoría los procedimientos a eliminar
		em.createNativeQuery(auditoriaQuery.toString())
				.setParameter("negociacionId", negociacionId)
				.setParameter("capituloId", capituloId)
				.setParameter("procedimientosId", procedimientosId)
				.setParameter("userId", userId)
				.executeUpdate();

		em.createNamedQuery(
				"SedeNegociacionProcedimiento.deleteByIdAndNegociacionAndCapituloSalud")
				.setParameter("negociacionId", negociacionId)
				.setParameter("capituloId", capituloId)
				.setParameter("procedimientosId", procedimientosId)
				.executeUpdate();

	}

	@Override
	public void eliminarProcedimientosNegociacionByIdAndNegociacionAndServicio(
			Long negociacionId, Long servicioId, List<Long> procedimientosId, List<Long> sedeNegociacionServicioId, Integer userId) {
		eliminarProcedimientosNegociacionByIdAndNegociacionAndServicio(negociacionId, servicioId, procedimientosId, userId);
		em.createNamedQuery(
				"SedeNegociacionServicio.updateValorAndPorcentajeByIdsAndProcedimiento")
				.setParameter("ids", sedeNegociacionServicioId)
				.setParameter("servicioId", sedeNegociacionServicioId.get(0))
				.setParameter("negociado", true).executeUpdate();
	}

	@Override
	public void eliminarProcedimientosPorSedeNegociaconServicio(
			List<Long> servicios, Integer userId) {

		StringBuilder auditoriaQuery = new StringBuilder();
		auditoriaQuery.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarProcedimientos())
		.append(" where sns.id in (:sedeNegociacionServicioIds) ");

		//Para registrar en auditoría los procedimientos a eliminar
		em.createNativeQuery(auditoriaQuery.toString())
				.setParameter("sedeNegociacionServicioIds", servicios)
				.setParameter("userId", userId)
				.executeUpdate();

		em.createNamedQuery(
				"SedeNegociacionProcedimiento.deleteBySedeNegociacionServicioIds")
				.setParameter("sedeNegociacionServicioIds", servicios)
				.executeUpdate();
	}

	@Override
	public void actualizaTarifasServiciosPxNegociados(Long negociacionId, Integer userId){
		em.createNamedQuery("SedeNegociacionServicio.updateTarifaServiciosProcedimientosNegociado")
				.setParameter("negociacionId", negociacionId)
				.setParameter("userId", userId)
				.executeUpdate();
	}

	/**
	 * Función que guarda una lista de procedimientos en una SedeNegociación
	 * dada la negociación, el servicio al que pertenece y la lista de
	 * procedimientos, solo se actualiza el valor negociado o porcentaje de
	 * negociación, la tarifa negociada y el check de negociado.
	 *
	 * @param procedimientos
	 *            la lista de procedimientos a persistir.
	 * @param negociacionId
	 *            la negociación a la que pertenece esos procedimientos.
	 * @param servicioId
	 *            El servicio asociado a esos procedimientos.
	 */
	@Override
	public void guardarProcedimientosNegociados(List<ProcedimientoNegociacionDto> procedimientos, Long negociacionId,
			Long servicioId, TipoTarifarioDto tarifarioServicio, Integer userId, Long negociacionReferenteId,
                        TipoAsignacionTarifaProcedimientoEnum tipoAsignacionSeleccionado) {
		boolean actualizaTarifa = false;
		for (ProcedimientoNegociacionDto dto : procedimientos) {
			if (dto.isSeleccionado()) {
				// Se crea el objeto tarifario a ser asociado en la query...
				Tarifario tarifario = null;
				if (dto.getTarifarioNegociado() != null && dto.getTarifarioNegociado().getId() != null) {
					tarifario = new Tarifario();
					tarifario.setId(dto.getTarifarioNegociado().getId().intValue());
					// Valida si tiene tarifa diferencial
					if (tarifarioServicio != null && tarifarioServicio.getId() != null) {
						dto.setTarifaDiferencial(tarifarioServicio.getId() != dto.getTarifarioNegociado().getId());
					}
				}

				// Se persiste los cambios...
				// Si se replica
				if (dto.isReplicarOtros()) {
					servicioNegociacionControl.actualizarProcedimientoValoresNegociados(dto, negociacionId, tarifario, userId);
				}else{
					// Si es unico
					servicioNegociacionControl.actualizarProcedimientoValoresNegociados(dto, negociacionId, servicioId, tarifario, userId, negociacionReferenteId, tipoAsignacionSeleccionado);
				}
				if (!actualizaTarifa) {
					if (dto.getTarifaDiferencial().booleanValue() == true) {
						actualizaTarifa = true;
					}
				}
			}

		}
		// Se actualiza el servicio en cuanto a si se tiene tarifa diferencial o
		// no...
		servicioNegociacionControl.actualizarTarifaDiferencialServicioNegociacion(negociacionId, servicioId, actualizaTarifa, userId);
	}
        
        

	/**
	 * Guarda las tarifas de los servicios negociacion por el id de Tecnologia
	 *
	 * @param negociacionId
	 *            Identificador de la negociacion
	 * @param serviciosNegociacion
	 *            Lista de servicios negociacion
	 */
	@Override
	public String guardarServiciosNegociados(Long negociacionId,
			List<ServicioNegociacionDto> serviciosNegociacion,
			TipoAsignacionTarifaServicioEnum tipoAsignacion,
			boolean esTransporte, Integer poblacion, boolean aplicarValorNegociado, Integer userId, Long negociacionReferenteId) {

		StringBuilder mensaje = new StringBuilder();

		if (tipoAsignacion == TipoAsignacionTarifaServicioEnum.CONTRATO_ANTERIOR
				|| tipoAsignacion == TipoAsignacionTarifaServicioEnum.PORTAFOLIO_PROPUESTO) {
			this.aplicarTarifasMasivo(negociacionId, serviciosNegociacion,tipoAsignacion, userId, negociacionReferenteId);
		} else if (tipoAsignacion == TipoAsignacionTarifaServicioEnum.APLICAR_REFERENTE){
			this.guardarServiciosNegociados(negociacionId, serviciosNegociacion, poblacion, aplicarValorNegociado, userId);
		} else {
			for (ServicioNegociacionDto serv : serviciosNegociacion) {
				if(Objects.nonNull(serv.getServicioSalud()) && Objects.nonNull(serv.getServicioSalud().getCodigo())){
					esTransporte = ServicioHabilitacionEnum.isServicioTransporte(serv.getServicioSalud().getCodigo());
				}
				Integer diferencia = 0;
				Long totalProcedimientos = em
						.createNamedQuery("SedeNegociacionProcedimiento.countTotalProcedimientosByNegociacionAndServicio",Long.class)
						.setParameter("servicioId",serv.getServicioSalud().getId())
						.setParameter("negociacionId", negociacionId)
						.setParameter("tipoProcedimientoId", esTransporte ? TipoProcedimientoEnum.TRASLADO_ASISTENCIAL : TipoProcedimientoEnum.PROCEDIMIENTO)
						.getSingleResult();
				Integer procedimientosEjecutados = 0;
				if (serv.getTarifarioNegociado() != null && serv.getTarifarioNegociado().getId() != null) {
					// Se actualizan los procedimientos
					String named = (serv.getTarifarioNegociado()
							.getDescripcion().equals("TARIFA PROPIA")) ? "SedeNegociacionProcedimiento.updateTarifaAndPorcentajeAndValorByServicioAndNegociacion"
							: "SedeNegociacionProcedimiento.updateTarifaAndPorcentajeByServicioAndNegociacion";
					Query query = em
							.createNamedQuery(named)
							.setParameter("negociacionId", negociacionId)
							.setParameter("servicioId", serv.getServicioSalud().getId())
							.setParameter("tarifario", serv.getTarifarioNegociado().getId().intValue())
							.setParameter("porcentajeNegociado",serv.getPorcentajeNegociado())
							.setParameter("userId", userId)
							.setParameter("tipoProcedimientoId", esTransporte ?
									TipoProcedimientoEnum.TRASLADO_ASISTENCIAL.getId() : TipoProcedimientoEnum.PROCEDIMIENTO.getId());

					if (serv.getTarifarioNegociado().getDescripcion()
							.equals("TARIFA PROPIA")) {
						// Incremento propia : Flujo Alternativo 4: Seleccione
						// el Incremento a la tarifa convenida
						if (serv.getIncrementoPropia() != null) {
							procedimientosEjecutados = em
									.createNamedQuery(
											"SedeNegociacionProcedimiento.updateIncrementoTarifaPropiaByServicioAndNegociacion")
									.setParameter("negociacionId", negociacionId)
									.setParameter("servicioId", serv.getServicioSalud().getId())
									.setParameter("porcentajeNegociado", new BigDecimal(0))
									.setParameter("incrementoPorcentaje", new BigDecimal(serv.getIncrementoPropia()))
									.setParameter("tarifarioId", serv.getTarifarioNegociado().getId().intValue())
									.setParameter("userId", userId)
									.executeUpdate();
						} // Flujo Alternativo 6: Asignar valor tarifa propia
						else if (serv.getValorPropia() != null && serv.getValorPropia().doubleValue() >= 0) {
							procedimientosEjecutados =
									esTransporte ?
											query.setParameter("valorNegociado", new BigDecimal(serv.getValorPropia()).setScale(-2, BigDecimal.ROUND_HALF_UP ))
											.executeUpdate()
											: query.setParameter("valorNegociado", new BigDecimal(serv.getValorPropia()).setScale(-2, BigDecimal.ROUND_HALF_UP ))
											.executeUpdate();
						}
						// Flujo alternativo portafolio propuesto para traslados
						else {
							procedimientosEjecutados = em
									.createNamedQuery(
											"SedeNegociacionProcedimiento.updatePortafolioPropuestoByServicioAndNegociacion")
									.setParameter("negociacionId", negociacionId)
									.setParameter("servicioId", serv.getServicioSalud().getId())
									.setParameter("porcentajeNegociado", new BigDecimal(0))
									.setParameter("tarifarioId", serv.getTarifarioNegociado().getId().intValue())
									.setParameter("userId", userId)
									.executeUpdate();
						}
					} // Tarifario seleccionado, valor propia por defecto 0
					else {
						procedimientosEjecutados = query.executeUpdate();
					}

					diferencia = totalProcedimientos.intValue()
							- procedimientosEjecutados;

					// Mensaje de numero de procedimientos que no se pueden
					// actualizar
					if (diferencia > 0) {
						mensaje.append("Servicio "
								+ serv.getServicioSalud().getNombre());
						mensaje.append(" No se actualizaron " + diferencia
								+ " procedimientos, ");
						mensaje.append("ya que no pueden tener asignado el tarifario:"
								+ serv.getTarifarioNegociado().getDescripcion()
								+ "");
					}

					// Se actualiza el servicio
					em.createNamedQuery(
							"SedeNegociacionServicio.updateTarifaAndPorcentajeByServicioAndNegociacion")
							.setParameter("negociacionId", negociacionId)
							.setParameter("servicioId", serv.getServicioSalud().getId())
							.setParameter("tarifario", serv.getTarifarioNegociado().getId().intValue())
							.setParameter("porcentajeNegociado", serv.getPorcentajeNegociado())
							.setParameter("negociado", serv.isNegociado())
							.setParameter("tarifaDiferencial", diferencia > 0 ? true : false)
							.setParameter("userId", userId)
							.executeUpdate();

					// mensaje.append("\n");
				}
			}
		}
		return mensaje.toString();
	}


	@Override
	public String guardarCapitulosNegociadosPGP(Long negociacionId,
			List<CapitulosNegociacionDto> capitulosNegociacion,
			TipoAsignacionTarifaServicioEnum tipoAsignacion,
			boolean esTransporte, Integer poblacion, boolean aplicarValorNegociado, Integer userId) throws ConexiaBusinessException {

		StringBuilder mensaje = new StringBuilder();

		if (tipoAsignacion == TipoAsignacionTarifaServicioEnum.APLICAR_REFERENTE){
			this.guardarCapitulosNegociadosPGP(negociacionId, capitulosNegociacion, poblacion, aplicarValorNegociado, userId);
		}
		return mensaje.toString();
	}

	@Override
	public void guardarFranjaPGP(Long negociacionId, List<CapitulosNegociacionDto> capitulosNegociacion, BigDecimal franjaInicio,
			BigDecimal franjaFin, Integer userId) throws ConexiaBusinessException {
		this.guardarFranjaCapitulosNegociadosPGP(negociacionId, capitulosNegociacion, franjaInicio, franjaFin, userId);
	}

	/**
	 * Guarda las tarifas de los servicios negociacion por el id de Tecnologia
	 *
	 * @param negociacionId
	 *            Identificador de la negociacion
	 * @param serviciosNegociacion
	 *            Lista de servicios negociacion
	 */
	@Override
	public String guardarProcedimientosNegociacion(Long negociacionId,
			List<ProcedimientoNegociacionDto> procedimientosNegociacion,
			TipoAsignacionTarifaProcedimientoEnum tipoAsignacion,
			boolean esTransporte) {

		StringBuilder mensaje = new StringBuilder();

		if (tipoAsignacion == TipoAsignacionTarifaProcedimientoEnum.CONTRATO_ANTERIOR
				|| tipoAsignacion == TipoAsignacionTarifaProcedimientoEnum.PORTAFOLIO_PROPUESTO) {
			this.aplicarTarifasMasivoProcedimientos(negociacionId, procedimientosNegociacion,
					tipoAsignacion);
		}
		return mensaje.toString();
	}

	/**
	 * Aplica las tarifas masivas dependiendo por tecnologia si es: valor
	 * contrato anterior o valor propuesto
	 *
	 * @param negociacionId
	 * @param serviciosNegociacion
	 * @param tipoAsignacion
	 */
	private void aplicarTarifasMasivo(Long negociacionId,
			List<ServicioNegociacionDto> serviciosNegociacion,
			TipoAsignacionTarifaServicioEnum tipoAsignacion, Integer userId, Long negociacionReferenteId) 
        {
		String namedNativeQuery = tipoAsignacion == TipoAsignacionTarifaServicioEnum.PORTAFOLIO_PROPUESTO ? "aplicarTarifasPropuestasByNegociacionAndServicios"
				: "aplicarTarifasContratoByNegociacionAndServicios";
                if (NAME_NATIVE_QUERY_PORTAFOLIO_PROPUESTO.equals(namedNativeQuery)) 
                {                    
                    actualizarServiciosYProcedimientosPortafolioPropuesto(serviciosNegociacion, namedNativeQuery, negociacionId, userId);
                }else {
                    actualizarServiciosYProcedimientos(serviciosNegociacion, negociacionId, userId, negociacionReferenteId);
                }                
	}
        
        /**
         * Method to update the values of services and procedure from previous contract
         * by Negociation Referent ID
         * 
         * @param serviciosNegociacion      List of services id         
         * @param negociacionId             Negociation ID
         * @param userId                    User ID
         * @param negociacionReferenteId    Negociation Referent ID
         */
        private void actualizarServiciosYProcedimientos(List<ServicioNegociacionDto> serviciosNegociacion,                                                        
                                                        Long negociacionId,
                                                        Integer userId, 
                                                        Long negociacionReferenteId)
        {
            String serviciosId = serviciosNegociacion.stream()
				.map(sn -> String.valueOf(sn.getServicioSalud().getId()))
				.collect(Collectors.joining(","));
            ejecutarActualizacionServiciosYProcedimientos(negociacionId, userId, negociacionReferenteId, serviciosId);
                   
        }
        
        /**
         * Method to run the update the values of services and procedure from previous contract
         * by Negociation Referent ID
         * 
         * @param serviciosNegociacion      List of services id         
         * @param negociacionId             Negociation ID
         * @param userId                    User ID
         * @param negociacionReferenteId    Negociation Referent ID
         */
        private void ejecutarActualizacionServiciosYProcedimientos(Long negociacionId,
                                                                   Integer userId, 
                                                                   Long negociacionReferenteId,
                                                                   String serviciosId)
        {
            if (Objects.nonNull(negociacionReferenteId)) 
            {
                // Actualizacion de servicios y procedimientos
                StoredProcedureQuery spQuery = em.createStoredProcedureQuery("contratacion.fn_aplicar_tarifas_contrato_by_negociacion_servicios_proced")
                                            .registerStoredProcedureParameter("in_negociacion_id", Long.class, ParameterMode.IN)
                                            .registerStoredProcedureParameter("in_negociacion_referente_id", Long.class, ParameterMode.IN)                                                      
                                            .registerStoredProcedureParameter("in_list_servicio_id", String.class, ParameterMode.IN)                                                
                                            .registerStoredProcedureParameter("in_user_id", Integer.class, ParameterMode.IN)                                                   
                                            .setParameter("in_negociacion_id", negociacionId)
                                            .setParameter("in_negociacion_referente_id", negociacionReferenteId) 
                                            .setParameter("in_list_servicio_id", serviciosId)                                                 
                                            .setParameter("in_user_id", userId);
                spQuery.execute(); 
            }
        }
        
        /**
         * Method to update the values of services and procedure from previous contract         
         * 
         * @param serviciosNegociacion      List of services id
         * @param namedNativeQuery          Name to native query
         * @param negociacionId             Negociation ID
         * @param userId                    User ID
         * @param negociacionReferenteId    Negociation Referent ID
         */
        private void actualizarServiciosYProcedimientosPortafolioPropuesto(List<ServicioNegociacionDto> serviciosNegociacion,
                                                                           String namedNativeQuery,
                                                                           Long negociacionId,
                                                                           Integer userId)
        {
            List<Long> serviciosId = serviciosNegociacion.stream()
				.map(sn -> sn.getServicioSalud().getId())
				.collect(Collectors.toList());
                    // Actualizacion de servicios
                    em.createNamedQuery("SedeNegociacionServicio." + namedNativeQuery)
				.setParameter("negociacionId", negociacionId)
				.setParameter("listaServicios", serviciosId)
				.setParameter("userId", userId)
				.executeUpdate();

                    // Actualizacion de procedimientos
                    em.createNamedQuery("SedeNegociacionProcedimiento." + namedNativeQuery)
                                    .setParameter("negociacionId", negociacionId)
				.setParameter("listaServicios", serviciosId)
				.setParameter("userId", userId)
				.executeUpdate();
        }

	/**
	 * Aplica las tarifas masivas dependiendo por tecnologia si es: valor
	 * contrato anterior o valor propuesto
	 *
	 * @param negociacionId
	 * @param procedimientosNegociacion
	 * @param tipoAsignacion
	 */
	private void aplicarTarifasMasivoProcedimientos(Long negociacionId,
			List<ProcedimientoNegociacionDto> procedimientosNegociacion,
			TipoAsignacionTarifaProcedimientoEnum tipoAsignacion) {

		List<Long> procedimientosId = procedimientosNegociacion.stream()
				.map(sn -> sn.getProcedimientoDto().getId())
				.collect(Collectors.toList());

		String namedNativeQuery = tipoAsignacion == TipoAsignacionTarifaProcedimientoEnum.PORTAFOLIO_PROPUESTO ? "aplicarTarifasPropuestaNegociacionProcedimientos"
				: "aplicarTarifasContratoNegociacionProcedimientos";

		// Actualizacion de procedimientos
		em.createNamedQuery("SedeNegociacionProcedimiento." + namedNativeQuery)
				.setParameter("negociacionId", negociacionId)
				.setParameter("listaProcedimientos", procedimientosId).executeUpdate();
	}

	/**
	 * Aplica las tarifas masivas dependiendo por {@link -
	 * SedeNegociacionServicio} si es: valor contrato anterior o valor propuesto
	 *
	 * @param negociacionId
	 * @param serviciosNegociacion
	 * @param tipoAsignacion
	 */
	private void aplicarTarifasMasivoById(
			List<ServicioNegociacionDto> serviciosNegociacion,
			TipoAsignacionTarifaServicioEnum tipoAsignacion) {

		List<Long> serviciosId = serviciosNegociacion.stream()
				.map(sn -> sn.getSedeNegociacionServicioId())
				.collect(Collectors.toList());

		String namedNativeQuery = tipoAsignacion == TipoAsignacionTarifaServicioEnum.PORTAFOLIO_PROPUESTO ? "aplicarTarifasPropuestasById"
				: "aplicarTarifasContratoById";

		// Actualizacion de servicios
		em.createNamedQuery("SedeNegociacionServicio." + namedNativeQuery)
				.setParameter("listaServicios", serviciosId).executeUpdate();

		// Actualizacion de procedimientos
		em.createNamedQuery("SedeNegociacionProcedimiento." + namedNativeQuery)
				.setParameter("listaServicios", serviciosId).executeUpdate();
	}

	@Override
	public void asignarValorServicio(NegociacionDto negociacion,
			List<Long> ids, BigDecimal valor) {
		em.createNamedQuery("SedeNegociacionServicio.updateValorByIds")
				.setParameter("ids", ids).setParameter("valorNegociado", valor)
				.setParameter("negociado", true).executeUpdate();
		em.createNamedQuery(
				"SedeNegociacionProcedimiento.asignarValorProcedimientos")
				.setParameter("valorNegociacion", valor)
				.setParameter("zonaCapitaId",
						negociacion.getZonaCapita().getId().longValue())
				.setParameter("regimenId", negociacion.getRegimen().getId())
				.setParameter("ids", ids).executeUpdate();
	}

	@Override
	public void asignarValorServicio(NegociacionDto negociacion,
			List<Long> ids, BigDecimal valor, BigDecimal porcentajeNegociado, Integer userId) {
		em.createNamedQuery(
				"SedeNegociacionProcedimiento.asignarValorPorcentajeProcedimientos")
				.setParameter("valorNegociacion", valor)
				.setParameter("porcentajeNegociado", porcentajeNegociado)
				.setParameter("zonaCapitaId",negociacion.getZonaCapita().getId().longValue())
				.setParameter("regimenId", negociacion.getRegimen().getId())
				.setParameter("ids", ids)
				.setParameter("userId", userId)
				.executeUpdate();

		em.createNamedQuery("SedeNegociacionServicio.updateValorAndPorcentajeByIdsAndProcedimiento")
				.setParameter("ids", ids)
				.setParameter("servicioId",	ids.get(0))
				.setParameter("negociado", true)
				.setParameter("userId", userId)
				.executeUpdate();
	}

	@Override
	public void asignarValorServicio(NegociacionDto negociacion,
			ServicioNegociacionDto servicio, Integer userId) {
		BigDecimal valorNegociado = BigDecimal.ZERO;
		if (servicio.isNegociaPorcentaje()
				&& Objects.nonNull(servicio.getPorcentajeNegociado())) {
			valorNegociado = negociacion.getValorUpcMensual()
					.multiply((servicio.getPorcentajeNegociado()).divide(BigDecimal.valueOf(100),MathContext.DECIMAL64),MathContext.DECIMAL64)
					.setScale(0, BigDecimal.ROUND_HALF_UP);

		} else if (Objects.isNull(servicio.isNegociaPorcentaje())
				|| !servicio.isNegociaPorcentaje()) {
			valorNegociado = servicio.getValorNegociado();
			servicio.setPorcentajeNegociado(servicio.getValorNegociado()
					.multiply(BigDecimal.valueOf(100D), MathContext.DECIMAL64)
					.divide(negociacion.getValorUpcMensual(),MathContext.DECIMAL64)
					.setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP));
		}

		em.createNativeQuery(
				this.servicioNegociacionControl.generarUpdateProcedimientosCapita().toString())
				.setParameter("valorNegociacion", valorNegociado)
				.setParameter("zonaCapitaId", negociacion.getZonaCapita().getId().longValue())
				.setParameter("regimenId", negociacion.getRegimen().getId())
				.setParameter("ids", servicio.getSedesNegociacionServicioIds())
				.setParameter("upcTotalServicio", servicio.getPorcentajeNegociado())
				.setParameter("porcentajeAsignadoServicio", servicio.getPorcentajeAsignado())
				.setParameter("userId", userId)
				.executeUpdate();

		em.createNamedQuery(
				"SedeNegociacionServicio.updateValorAndPorcentajeByIdsAndProcedimiento")
				.setParameter("ids", servicio.getSedesNegociacionServicioIds())
				.setParameter("servicioId", servicio.getSedesNegociacionServicioIds().get(0))
				.setParameter("negociado", true)
				.setParameter("userId", userId)
				.executeUpdate();
	}

	@Override
	public void asignarValorServicioPorPorcentaje(List<Long> ids,
			BigDecimal porcentaje, Long zonaCapitaId, Integer userId) {
		em.createNamedQuery("SedeNegociacionServicio.updateValorByIdsAndPercent")
				.setParameter("ids", ids)
				.setParameter("percent", porcentaje)
				.setParameter("userId", userId)
				.executeUpdate();
		em.createNamedQuery("SedeNegociacionProcedimiento.updateValorByIdsAndPercent")
				.setParameter("ids", ids)
				.setParameter("percent", porcentaje)
				.setParameter("userId", userId)
				.executeUpdate();
	}

	@Override
	public void asignarValoresContratoAnteriorNegociadoServicio(List<ServicioNegociacionDto> serviciosNegociacion, NegociacionDto negociacion, Integer userId){
		serviciosNegociacion.stream()
		.filter(servicio -> Objects.nonNull(servicio.getPorcentajeContratoAnterior()) &&
							servicio.getPorcentajeContratoAnterior().compareTo(BigDecimal.ZERO) ==1)
		.forEach(servicio -> {
			BigDecimal valorNegociado = negociacion.getValorUpcMensual()
					.multiply((servicio.getPorcentajeContratoAnterior()).divide(BigDecimal.valueOf(100),MathContext.DECIMAL64),MathContext.DECIMAL64)
					.setScale(0, BigDecimal.ROUND_HALF_UP);

			em.createNativeQuery(
					this.servicioNegociacionControl.generarUpdateProcedimientosCapita().toString())
					.setParameter("valorNegociacion", valorNegociado)
					.setParameter("zonaCapitaId", negociacion.getZonaCapita().getId().longValue())
					.setParameter("regimenId", negociacion.getRegimen().getId())
					.setParameter("ids", servicio.getSedesNegociacionServicioIds())
					.setParameter("upcTotalServicio", servicio.getPorcentajeContratoAnterior())
					.setParameter("porcentajeAsignadoServicio", servicio.getPorcentajeAsignado())
					.setParameter("userId", userId)
					.executeUpdate();

			em.createNamedQuery("SedeNegociacionServicio.aplicarValorPortafolioByIds")
					.setParameter("ids", servicio.getSedesNegociacionServicioIds())
					.setParameter("userId", userId)
					.executeUpdate();
		});
	}

	@Override
	public void asignarValorReferente(List<Long> ids, NegociacionDto negociacion, Integer userId) {
		em.createNamedQuery("SedeNegociacionProcedimiento.aplicarValorReferenteByIds")
				.setParameter("ids", ids)
				.setParameter("zonaCapitaId",negociacion.getZonaCapita().getId().longValue())
				.setParameter("regimenId", negociacion.getRegimen().getId())
				.setParameter("valorUpcMensual", negociacion.getValorUpcMensual())
				.setParameter("userId", userId)
				.executeUpdate();
		em.createNamedQuery("SedeNegociacionServicio.aplicarValorReferenteByIds")
				.setParameter("ids", ids)
				.setParameter("zonaCapitaId", negociacion.getZonaCapita().getId().longValue())
				.setParameter("regimenId", negociacion.getRegimen().getId())
				.setParameter("userId", userId)
				.executeUpdate();
	}

	/**
	 * Asigna la poblacion a las sedes servicio por servicio
	 *
	 * @param sedesNegociacionServicioIds
	 *            Identificador de {@link - SedeNegociacionServicio}
	 * @param poblacion
	 *            poblacion a asignar
	 */
	@Override
	public void asignarPoblacionPorServicio(
			List<Long> sedesNegociacionServicioIds, Integer poblacion) {
		em.createNamedQuery("SedeNegociacionServicio.updatePoblacionByIds")
				.setParameter("poblacion", poblacion)
				.setParameter("ids", sedesNegociacionServicioIds)
				.executeUpdate();
	}

	@Override
	public void asignarPoblacionPorServicio(
			ServicioNegociacionDto servicioNegociacion,
			NegociacionDto negociacion) throws ConexiaBusinessException {

		em.createQuery(
				"UPDATE SedeNegociacionServicio s SET s.poblacion = :poblacion "
						+ " WHERE s.servicioSalud.id = :servicioId "
						+ "   AND s.sedeNegociacion.id IN ("
						+ "       SELECT n.id FROM SedesNegociacion n "
						+ "       WHERE n.negociacion.id = :negociacionId) ")
				.setParameter("servicioId",
						servicioNegociacion.getServicioSalud().getId())
				.setParameter("negociacionId", negociacion.getId())
				.setParameter("poblacion", servicioNegociacion.getPoblacion())
				.executeUpdate();
	}

	@Override
	public void agregarProcedimientosNegociacion(List<Long> procedimientosIds,
			NegociacionDto negociacion, String codigoServicio, List<ProcedimientoDto> procedimientos, Integer userId, Long negociacionReferenteId) {
		StringBuilder sql = new StringBuilder();                
                this.servicioNegociacionControl.generarInsertAgregarprocedimientos(sql,	negociacion);
		Query query = em.createNativeQuery(sql.toString())
				.setParameter("procedimientosIds", procedimientosIds)
				.setParameter("negociacionId", negociacion.getId())
				.setParameter("codigoServicio", codigoServicio)
				.setParameter("prestadorId", negociacion.getPrestador().getId())
				.setParameter("estadoLegalizacionDescripcion", EstadoLegalizacionEnum.LEGALIZADA.getDescripcion().toUpperCase())
				.setParameter("modalidadDescripcion", negociacion.getTipoModalidadNegociacion().getDescripcion().toUpperCase())
        		.setParameter("userId", userId);
		query.executeUpdate();
                procedimientos.stream().forEach(data -> 
                        this.servicioNegociacionControl.actualizarProcedimientoValoresNegociados(data.getId(), negociacion.getId(),
                                data.getServicioId().longValue(), userId, negociacionReferenteId));
	}

	@Override
	public void agregarProcedimientosNegociacionPGP(List<Long> procedimientosIds,
			NegociacionDto negociacion, Long capituloId, Integer userId) {
		StringBuilder sql = new StringBuilder();
		this.servicioNegociacionControl.generarInsertAgregarprocedimientos(sql,
				negociacion);

		Query query = em.createNativeQuery(sql.toString())
				.setParameter("procedimientosIds", procedimientosIds)
				.setParameter("negociacionId", negociacion.getId())
				.setParameter("capituloId", capituloId)
				.setParameter("prestadorId", negociacion.getPrestador().getId())
				.setParameter("estadoLegalizacionDescripcion", EstadoLegalizacionEnum.LEGALIZADA.getDescripcion().toUpperCase())
				.setParameter("modalidadDescripcion", negociacion.getTipoModalidadNegociacion().getDescripcion().toUpperCase())
        		.setParameter("userId", userId);
		query.executeUpdate();

	}

	@Override
	public void eliminarProcedimientosNegociacionByIdAndNegociacion(
			Long negociacionId, List<String> codigos, Integer userId) {

		StringBuilder queryAuditoriaPx = new StringBuilder();
		queryAuditoriaPx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarProcedimientos())
		.append(" join maestros.procedimiento_servicio ps on ps.id = snp.procedimiento_id")
		.append(" where sn.negociacion_id = :negociacionId and ps.codigo_cliente in (:codigos)");

		em.createNativeQuery(queryAuditoriaPx.toString())
				.setParameter("negociacionId", negociacionId)
				.setParameter("codigos", codigos)
				.setParameter("userId", userId)
				.executeUpdate();


		em.createNamedQuery(
				"SedeNegociacionProcedimiento.deleteByIdAndNegociacionServicios")
				.setParameter("negociacionId", negociacionId)
				.setParameter("codigos", codigos)
				.executeUpdate();
	}

	@Override
	public Integer eliminarProcedimientosNegociacionByIdAndNegociacionPGP(
			Long negociacionId, List<Long> codigos, CapitulosNegociacionDto capituloNegociacion, Integer userId) {

		StringBuilder queryAuditoriaPx = new StringBuilder();
		queryAuditoriaPx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarProcedimientosPgp())
		.append(" where sn.negociacion_id = :negociacionId")
		.append(" and snc.capitulo_id = :capituloId")
		.append(" and snp.pto_id in (:procedimientoIds)");


		//Para registrar en auditoría la eliminación de px
		em.createNativeQuery(queryAuditoriaPx.toString())
				.setParameter("userId", userId)
				.setParameter("negociacionId", negociacionId)
				.setParameter("procedimientoIds", codigos)
				.setParameter("capituloId", capituloNegociacion.getCapituloProcedimiento().getId())
				.executeUpdate();

		em.createNamedQuery(
				"SedeNegociacionProcedimiento.deleteByIdAndNegociacion")
				.setParameter("negociacionId", negociacionId)
				.setParameter("procedimientoIds", codigos)
				.setParameter("capituloId", capituloNegociacion.getCapituloProcedimiento().getId())
				.executeUpdate();

		//contar capitulos sin procedimientos
		String conteo = em.createNamedQuery("SedeNegociacionProcedimiento.contarProcedimientosByNegociacionCapitulo")
				.setParameter("negociacionId", negociacionId)
				.setParameter("capituloId", capituloNegociacion.getCapituloProcedimiento().getId())
				.getSingleResult().toString();

        Integer sinProcedimientos =  conteo != null ? new Integer(conteo) : 0;
		//eliminar capítulo sin procedimientos
        List<Long> capituloIds = new ArrayList<Long>();
        capituloIds.add(capituloNegociacion.getCapituloProcedimiento().getId());
        if(sinProcedimientos == 0) {

        	StringBuilder queryAuditoriaCx = new StringBuilder();
        	queryAuditoriaCx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarCapitulos())
        	.append(" where sn.negociacion_id = :negociacionId and snc.capitulo_id in (:capitulosId)");

        	//Para registrar en auditoría la eliminación de capítulos de la negociación
        	em.createNativeQuery(queryAuditoriaCx.toString())
		        	.setParameter("negociacionId", negociacionId)
					.setParameter("capitulosId", capituloIds)
					.setParameter("userId", userId)
					.executeUpdate();

        	//Elimina el capítulo de la negociación
    		em.createNamedQuery(
    				"SedeNegociacionCapitulo.deleteByNegociacionIdAndCapitulo")
    				.setParameter("negociacionId", negociacionId)
    				.setParameter("capitulosId", capituloIds).executeUpdate();
        }
		//actualizar valores
        servicioNegociacionControl.actualizarValorNegociadoCapituloNegociacion(negociacionId, capituloIds, userId);

        return sinProcedimientos;
	}

	public void eliminarTodasTecnologiasRiaCapita(Long negociacionId,List<String> codigos, Integer userId){

		StringBuilder auditoriaProcedimientos = new StringBuilder();
		auditoriaProcedimientos.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarProcedimientos())
		.append(" 	where snp.id in (")
		.append(" 	SELECT snp.id FROM contratacion.sede_negociacion_procedimiento snp ")
		.append(" 	JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id ")
		.append(" 	JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id ")
		.append(" 	JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id ")
		.append(" 	JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id ")
		.append(" 	JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id ")
		.append(" 	WHERE sn.negociacion_id = :negociacionId and ps.codigo_cliente in(:codigos)")
		.append(" )");


		//Para insertar registros en auditoria de los procedimientos a eliminar
		em.createNativeQuery(auditoriaProcedimientos.toString())
			.setParameter("negociacionId", negociacionId)
			.setParameter("codigos", codigos)
			.setParameter("userId", userId)
			.executeUpdate();


		StringBuilder auditoriaMedicamentos = new StringBuilder();
		auditoriaMedicamentos.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarMedicamentos())
		.append(" where snm.id in (")
		.append(" 	SELECT snm.id FROM contratacion.sede_negociacion_medicamento snm ")
		.append(" 	JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id ")
		.append(" 	JOIN maestros.medicamento m ON snm.medicamento_id = m.id ")
		.append(" 	JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id ")
		.append(" 	JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id ")
		.append(" 	WHERE sn.negociacion_id = :negociacionId and m.codigo in(:codigos) ")
		.append(" )");

		//Para insertar registros en auditoria de los medicamentos a eliminar
		em.createNativeQuery(auditoriaMedicamentos.toString())
			.setParameter("userId", userId)
			.setParameter("negociacionId", negociacionId)
			.setParameter("codigos", codigos)
			.executeUpdate();

		em.createNamedQuery("SedeNegociacionProcedimiento.borrarTodosProcedimientosRiaCapita")
			.setParameter("negociacionId", negociacionId)
			.setParameter("codigos", codigos)
			.executeUpdate();
		em.createNamedQuery("SedeNegociacionMedicamento.borrarTodosMedicamentosRiaCapita")
		.setParameter("negociacionId", negociacionId)
		.setParameter("codigos", codigos)
		.executeUpdate();
	}

	public void eliminarTecnologiasRiaCapitaActivdad(NegociacionRiaRangoPoblacionDto negociacionRangoPoblacion,List<String> codigos,
			List<Integer> actividadId, Long negociacionId,List<Integer> servicioCodigo,String eliminarTodo, Integer userId){

		this.eliminarProcedimientoRiaCapitaActividad(negociacionRangoPoblacion, codigos, actividadId, negociacionId, servicioCodigo, eliminarTodo, userId);

		StringBuilder queryAuditoriaMx = new StringBuilder();
		queryAuditoriaMx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarMedicamentos())
		.append(" where snm.id in (")
		.append(" 	SELECT snm.id FROM contratacion.sede_negociacion_medicamento snm ")
		.append(" 	JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id ")
		.append(" 	JOIN maestros.medicamento m ON snm.medicamento_id = m.id ")
		.append(" 	JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id ")
		.append(" 	JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id ")
		.append(" 	WHERE sn.negociacion_id = :negociacionId and m.codigo in(:codigos) ")
		.append(" 	and nr.ria_id = :riaId and nrp.rango_poblacion_id = :rangoPoblacionId and snm.actividad_id in (:actividadId)")
		.append(" )");

		//Para registrar en auditoria los medicamentos a eliminar
		em.createNativeQuery(queryAuditoriaMx.toString())
			.setParameter("userId", userId)
			.setParameter("negociacionId", negociacionId)
			.setParameter("codigos", codigos)
			.setParameter("riaId", negociacionRangoPoblacion.getNegociacionRia().getRia().getId())
			.setParameter("rangoPoblacionId", negociacionRangoPoblacion.getRangoPoblacion().getId())
			.setParameter("actividadId", actividadId)
			.executeUpdate();

		em.createNamedQuery("SedeNegociacionMedicamento.borrarPorActividadesMedicamentosRiaCapita")
			.setParameter("negociacionId", negociacionId)
			.setParameter("codigos", codigos)
			.setParameter("riaId", negociacionRangoPoblacion.getNegociacionRia().getRia().getId())
			.setParameter("rangoPoblacionId", negociacionRangoPoblacion.getRangoPoblacion().getId())
			.setParameter("actividadId", actividadId)
		.executeUpdate();

	}

	public void eliminarProcedimientoRiaCapitaActividad(NegociacionRiaRangoPoblacionDto negociacionRangoPoblacion,List<String> codigos,
			List<Integer> actividadId, Long negociacionId,List<Integer> servicioCodigo,String eliminarTodo, Integer userId){

		StringBuilder queryAuditoria = new StringBuilder();
		queryAuditoria.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarProcedimientos())
		.append(" WHERE snp.id IN ( ")
		.append(" SELECT snp.id FROM contratacion.sede_negociacion_procedimiento snp ")
		.append(" JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id ")
		.append(" JOIN contratacion.servicio_salud ss ON sns.servicio_id = ss.id ")
		.append(" JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id ")
		.append(" JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id ")
		.append(" JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id ")
		.append(" JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id  AND snp.negociacion_ria_rango_poblacion_id = nrp.id ")
		.append(" WHERE sn.negociacion_id = :negociacionId and ps.codigo_cliente in(:codigos) ")
		.append(" and nr.ria_id = :riaId and nrp.rango_poblacion_id = :rangoPoblacionId ");

		StringBuilder query = new StringBuilder();
		Map<String,Object> parameters = new HashMap<>();
		query.append("DELETE FROM contratacion.sede_negociacion_procedimiento WHERE id IN ( "
			+ "SELECT snp.id FROM contratacion.sede_negociacion_procedimiento snp "
			+ "JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id "
			+ "JOIN contratacion.servicio_salud ss ON sns.servicio_id = ss.id "
			+ "JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id "
			+ "JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id "
			+ "JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id "
			+ "JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id  AND snp.negociacion_ria_rango_poblacion_id = nrp.id "
			+ "WHERE sn.negociacion_id = :negociacionId and ps.codigo_cliente in(:codigos) "
			+ "and nr.ria_id = :riaId and nrp.rango_poblacion_id = :rangoPoblacionId ");
		parameters.put("negociacionId", negociacionId);
		parameters.put("codigos", codigos);
		parameters.put("riaId", negociacionRangoPoblacion.getNegociacionRia().getRia().getId());
		parameters.put("rangoPoblacionId", negociacionRangoPoblacion.getRangoPoblacion().getId());

		if(eliminarTodo.equals("RI")){

			queryAuditoria.append("and snp.actividad_id in (:actividadId)  and ps.reps_cups in (:servicioCodigo) ) ");

			query.append("and snp.actividad_id in (:actividadId)  and ps.reps_cups in (:servicioCodigo) ) ");
			parameters.put("actividadId", actividadId);
			parameters.put("servicioCodigo", servicioCodigo);
		}
		else{

			queryAuditoria.append(" ) ");

			query.append(" ) ");
		}

		//Para registrar en auditoria los procedimientos a eliminar
		Query queryFinalAuditoria = this.em.createNativeQuery(queryAuditoria.toString());
		for (Entry<String, Object> llaveValor : parameters.entrySet()) {
			queryFinalAuditoria.setParameter(llaveValor.getKey(), llaveValor.getValue());
		}
		queryFinalAuditoria.setParameter("userId", userId);
		queryFinalAuditoria.executeUpdate();

		Query queryFinal = this.em.createNativeQuery(query.toString());
		for (Entry<String, Object> llaveValor : parameters.entrySet()) {
			queryFinal.setParameter(llaveValor.getKey(), llaveValor.getValue());
		}
		queryFinal.executeUpdate();

	}

	public void agregarGrupoServicioPrestador(Long prestadorId){
		em.createNamedQuery("GrupoServicio.insertarGrupoServicioHabilitado")
		.setParameter("prestadorId", prestadorId)
		.executeUpdate();
	}

	@Override
	public void almacenarProcedimientosArchivoImportado(List<ProcedimientoNegociacionDto> listProcedimientoNegociacion,Integer userId,
			NegociacionModalidadEnum negociacionModalidad) throws ConexiaSystemException{
		if (Objects.isNull(listProcedimientoNegociacion.isEmpty())) {
            throw new IllegalArgumentException("La lista de ofertas sede servicios no puede ser nula");
        }
        if (Objects.isNull(userId)) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        try {
        	Long max = new Long(100);
        	Long min  = new Long(-100);
        	// Verificar si es Ria
        	List<String> actividad = listProcedimientoNegociacion
        		.stream().filter(p -> (Objects.nonNull(p.getActividad()) && Objects.nonNull(p.getActividad().getDescripcion())))
        		.map(p -> p.getActividad().getDescripcion()).collect(Collectors.toList());

        	if(Objects.nonNull(actividad) && !actividad.isEmpty()){
        		// Agrega procedimientos de Capita por Rias
        		listProcedimientoNegociacion.stream().forEach(procedimientosNegociacion ->{
        			try{
	        			List<Integer> complejidades = paqueteNegociacionControl.generarComplejidadesByNegociacionComplejidad(procedimientosNegociacion.getComplejidadNegociacion());
	        			int result = em.createNamedQuery("SedeNegociacionProcedimiento.updateProcedimientosCapitaRiasNegociacion")
	    			    .setParameter("riasDescripcion", procedimientosNegociacion.getRangoPoblacion().getNegociacionRia().getRia().getDescripcion().toUpperCase())
	    				.setParameter("actividadDescripcion", procedimientosNegociacion.getActividad().getDescripcion().toUpperCase())
	    				.setParameter("rangoPoblacionDescripcion", procedimientosNegociacion.getRangoPoblacion().getRangoPoblacion().getDescripcion().toUpperCase())
	    				.setParameter("servicioCodigo", procedimientosNegociacion.getProcedimientoDto().getServicioSalud().getCodigo())
	    				.setParameter("codigoCliente", procedimientosNegociacion.getProcedimientoDto().getCodigoCliente())
	    				.setParameter("negociacionId", procedimientosNegociacion.getNegociacionId())
	    				.setParameter("complejidades", complejidades)
	    				.setParameter("pesoPorcentual", procedimientosNegociacion.getPorcentajePropuestoPortafolio())
	    				.setParameter("userId", userId)
	        			.executeUpdate();

	        			if(result == 0){
		        			em.createNamedQuery("SedeNegociacionProcedimiento.insertProcedimientosCapitaRiasNegociacion")
			        			    .setParameter("riasDescripcion", procedimientosNegociacion.getRangoPoblacion().getNegociacionRia().getRia().getDescripcion().toUpperCase())
			        				.setParameter("actividadDescripcion", procedimientosNegociacion.getActividad().getDescripcion().toUpperCase())
			        				.setParameter("rangoPoblacionDescripcion", procedimientosNegociacion.getRangoPoblacion().getRangoPoblacion().getDescripcion().toUpperCase())
			        				.setParameter("servicioCodigo", procedimientosNegociacion.getProcedimientoDto().getServicioSalud().getCodigo())
			        				.setParameter("codigoCliente", procedimientosNegociacion.getProcedimientoDto().getCodigoCliente())
			        				.setParameter("negociacionId", procedimientosNegociacion.getNegociacionId())
			        				.setParameter("complejidades", complejidades)
			        				.setParameter("pesoPorcentual", procedimientosNegociacion.getPorcentajePropuestoPortafolio())
			        				.setParameter("userId", userId)
			        			.executeUpdate();
	        			}
        			}catch (Exception e) {
        				logger.error("Error al procesar el registro:"+
        						procedimientosNegociacion.getRangoPoblacion().getNegociacionRia().getRia().getDescripcion() +"-"+
        						procedimientosNegociacion.getActividad().getDescripcion() +"-"+
        						procedimientosNegociacion.getRangoPoblacion().getRangoPoblacion().getDescripcion()+"-"+
        						procedimientosNegociacion.getProcedimientoDto().getServicioSalud().getCodigo()+"-"+
        						procedimientosNegociacion.getProcedimientoDto().getCodigoCliente()+"-"+
        						procedimientosNegociacion.getNegociacionId()+"-"+
        						procedimientosNegociacion.getPorcentajePropuestoPortafolio(), e);
        			}
	        	});

        	}else{
	        	listProcedimientoNegociacion.stream().forEach(procedimientosNegociacion ->{
	        		if(procedimientosNegociacion.getPorcentajePropuestoPortafolio().longValue() >= min && procedimientosNegociacion.getPorcentajePropuestoPortafolio().longValue() <= max ){
	        			Query updt = em.createNativeQuery(servicioNegociacionControl.generarUpdateProcedimientosArchivo(negociacionModalidad))
	        				.setParameter("tarifario", procedimientosNegociacion.getTarifarioPropuestoPortafolio().toUpperCase())
	        				.setParameter("porcentajePropuesto", (procedimientosNegociacion.getPorcentajePropuestoPortafolio() != null ? procedimientosNegociacion.getPorcentajePropuestoPortafolio().longValue() : BigDecimal.ZERO.longValue()))
	        				.setParameter("negociacionId", procedimientosNegociacion.getNegociacionId())
	        				.setParameter("codigoCliente", procedimientosNegociacion.getProcedimientoDto().getCodigoCliente())
	        				.setParameter("servicioCodigo", procedimientosNegociacion.getProcedimientoDto().getServicioSalud().getCodigo())
	        				.setParameter("userId", userId);

	        			switch(negociacionModalidad) {
		        			case EVENTO:
		        				updt.setParameter("valorNegociado", (procedimientosNegociacion.getValorNegociado() != null ?  procedimientosNegociacion.getValorNegociado().longValue() : BigDecimal.ZERO.longValue()));
		        				break;
		        			default:
		        				updt.setParameter("valorPropuesto", (procedimientosNegociacion.getValorPropuestoPortafolio() != null ?  procedimientosNegociacion.getValorPropuestoPortafolio().longValue() : BigDecimal.ZERO.longValue()));
		        				break;
		        		}

	        			int result = updt.executeUpdate();

	        			if(result == 0){
		        			List<Integer> complejidades = paqueteNegociacionControl.generarComplejidadesByNegociacionComplejidad(procedimientosNegociacion.getComplejidadNegociacion());
		        			Query inst = em.createNativeQuery(servicioNegociacionControl.generarInsertProcedimientosNegociacion(negociacionModalidad))
		        				.setParameter("tarifario", procedimientosNegociacion.getTarifarioPropuestoPortafolio().toUpperCase())
		        				.setParameter("porcentajePropuesto", (procedimientosNegociacion.getPorcentajePropuestoPortafolio() != null ? procedimientosNegociacion.getPorcentajePropuestoPortafolio().longValue() : BigDecimal.ZERO.longValue()))
		        				.setParameter("negociacionId", procedimientosNegociacion.getNegociacionId())
		        				.setParameter("servicioCodigo", procedimientosNegociacion.getProcedimientoDto().getServicioSalud().getCodigo())
		        				.setParameter("codigoEmssanar", procedimientosNegociacion.getProcedimientoDto().getCodigoCliente())
		        				.setParameter("complejidades", complejidades)
		        				.setParameter("userId", userId);

		        			switch(negociacionModalidad) {
			        			case EVENTO:
			        				inst.setParameter("valorNegociado", (procedimientosNegociacion.getValorNegociado() != null ?  procedimientosNegociacion.getValorNegociado().longValue() : BigDecimal.ZERO.longValue()));
			        				break;
			        			default:
			        				inst.setParameter("valorPropuesto", (procedimientosNegociacion.getValorPropuestoPortafolio() != null ?  procedimientosNegociacion.getValorPropuestoPortafolio().longValue() : BigDecimal.ZERO.longValue()));
			        				break;
			        		}

		        			inst.executeUpdate();

	        			}
	        		}
	        	});
        	}
		} catch (Exception e) {
			throw new ConexiaSystemException("No fue posible realizar la importación del archivo. "+ e.fillInStackTrace());
		}

	}

	@Override
	public void almacenarServiciosArchivoImportado(List<String> listServicios,Long negociacionId, Integer userId) throws ConexiaSystemException{
		if (Objects.isNull(listServicios.isEmpty())) {
            throw new IllegalArgumentException("La lista de ofertas sede servicios no puede ser nula");
        }
        if (Objects.isNull(userId)) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        try {
        	//Agrega servicios a la negociacion
        	listServicios.stream().forEach(serviciosNegociacion ->{
        		em.createNamedQuery("SedeNegociacionServicio.insertServicioNegociacion")
    			.setParameter("NegociacionId", negociacionId)
    			.setParameter("servicioCodigo", Integer.parseInt(serviciosNegociacion))
    			.setParameter("userId", userId)
    			.executeUpdate();
        	});
		} catch (Exception e) {
			throw new ConexiaSystemException("No fue posible realizar la importación del archivo. "+ e.fillInStackTrace());
		}
	}

	@Override
	public void almacenarServiciosArchivoImportadoAddEmpresariales (Long negociacionId, Integer userId, List<String> codigosServicios) throws ConexiaBusinessException {
		em.createNamedQuery("SedeNegociacionServicio.insertServiciosEmpresarialesNegociacion")
				.setParameter("userId", userId)
				.setParameter("negociacionId", negociacionId)
				.setParameter("servicioSaludCodigos", codigosServicios)
				.executeUpdate();

	}

	/**
	 * Actualizac el valor de los procedimientos con el valor de Referente
	 * @param procedimientos
	 * @param negociacionId
	 * @param servicioId
	 */
	@Override
	public void guardarProcedimientosNegociados(List<ProcedimientoNegociacionDto> procedimientos, Long negociacionId,Long servicioId, Integer userId) {
		procedimientos.stream()
			.filter(dto -> dto.isSeleccionado())
			.forEach(dto -> {
					servicioNegociacionControl.actualizarProcedimientoValoresNegociados(dto, negociacionId, servicioId);
			});

		// Actualiza el costo medio usuario
		servicioNegociacionControl.actualizarValorNegociadoServicioNegociacion(negociacionId, servicioId, userId);
	}

	/**
	 * Actualiza el valor de los procedimientos con el valor del referente para negociaciones PGP
	 */
	@Override
	public void guardarProcedimientosNegociadosPGP(List<ProcedimientoNegociacionDto> procedimientos, Long negociacionId,Long capituloId, Integer userId) {
		procedimientos.stream()
			.filter(ProcedimientoNegociacionDto::isSeleccionado)
			.forEach(dto -> servicioNegociacionControl.actualizarProcedimientoValoresNegociadosPGP(dto, negociacionId, capituloId));

		List<Long> capituloIds = new ArrayList<Long>();
		capituloIds.add(capituloId);

		// Actualiza el costo medio usuario
		servicioNegociacionControl.actualizarValorNegociadoCapituloNegociacion(negociacionId, capituloIds, userId);
	}

	@Override
	public void guardarProcedimientosFranjaPGP(Long negociacionId, List<Long> procedimientoIds, Long capituloId, BigDecimal franjaInicio,
			BigDecimal franjaFin, Integer userId) throws ConexiaBusinessException {

		servicioNegociacionControl.actualizarProcedimientoFranjaPGP(negociacionId, procedimientoIds, capituloId, franjaInicio, franjaFin);

		List<Long> capituloIds = new ArrayList<Long>();
		capituloIds.add(capituloId);
		servicioNegociacionControl.actualizarFranjaRiesgoCapituloNegociacion(negociacionId, capituloIds, userId);
	}

	@Override
	public void aplicarValorNegociadoByPoblacion(Long negociacionId, Integer userId) throws ConexiaBusinessException {
		servicioNegociacionControl.actualizarProcedimientosNegociadosValorPGP(negociacionId);
		servicioNegociacionControl.actualizarValorNegociadoCapituloByProcedimientosNegociacion(negociacionId, userId);
	}

	/**
	 * Realiza la actualizacion de servicios y procedimiento con el valor del referente
	 * @param negociacionId
	 * @param List<ServicioNegociacionDto>
	 */
	@Override
	public void guardarServiciosNegociados(Long negociacionId, List<ServicioNegociacionDto> serviciosNegociacion, Integer poblacion,
			boolean aplicarValorNegociado, Integer userId){
		serviciosNegociacion.stream()
			.forEach(servicio -> {
				//servicioNegociacionControl.actualizarProcedimientoValoresNegociados(negociacionId, servicio.getServicioSalud().getId(), poblacion, aplicarValorNegociado, userId);
				servicioNegociacionControl.actualizarValorNegociadoServicioNegociacion(negociacionId, servicio, aplicarValorNegociado, userId);
			});
	}

	/**
	 * Realiza la actualizacin de capitulos y procedimiento con el valor del referente
	 * @param negociacionId
	 * @param capitulosNegociacion
	 * @param poblacion
	 * @param aplicarValorNegociado
	 * @param userId
	 */
	public void guardarCapitulosNegociadosPGP(Long negociacionId, List<CapitulosNegociacionDto> capitulosNegociacion, Integer poblacion,
			boolean aplicarValorNegociado, Integer userId) throws ConexiaBusinessException{
		List<Long> capituloIds = new ArrayList<Long>();
		capitulosNegociacion.stream()
			.forEach(capitulo -> {
				capituloIds.add(capitulo.getCapituloProcedimiento().getId());
			});
		servicioNegociacionControl.actualizarProcedimientoValoresNegociados(negociacionId, capituloIds, poblacion, aplicarValorNegociado, userId);
		servicioNegociacionControl.actualizarValorNegociadoCapituloNegociacion(negociacionId, capituloIds, userId);
	}

	/**
	 * Realiza la actualización de la franja de riesgo de los capítulos y sus respectivos procedimientos
	 * @param negociacionId
	 * @param capitulosNegociacion
	 * @param franjaInicio
	 * @param franjaFin
	 * @param userId
	 * @throws ConexiaBusinessException
	 */
	public void guardarFranjaCapitulosNegociadosPGP(Long negociacionId, List<CapitulosNegociacionDto> capitulosNegociacion,
			BigDecimal franjaInicio, BigDecimal franjaFin, Integer userId) throws ConexiaBusinessException {
		List<Long> capituloIds = new ArrayList<Long>();
		capitulosNegociacion.stream()
			.forEach(capitulo -> {
				capituloIds.add(capitulo.getCapituloProcedimiento().getId());
			});
		servicioNegociacionControl.actualizarProcedimientoFranjaRiesgo(negociacionId, capituloIds, franjaInicio, franjaFin, userId);
		servicioNegociacionControl.actualizarFranjaRiesgoCapituloNegociacion(negociacionId, capituloIds, userId);
	}

	public void actualizarProcedimientosRiaCapita(List<ProcedimientoNegociacionDto> procedimientos,NegociacionRiaRangoPoblacionDto negociacionRangoPoblacion ,
			Long negociacionId, Integer userId){
		procedimientos.stream()
		.forEach(dto -> {
			if (dto.getServicio().equals(SERVICIO_MEDICAMENTO)) {
				servicioNegociacionControl.actualizarMedicamentosRiaCapita(dto, negociacionRangoPoblacion, negociacionId, userId);
			} else {
				servicioNegociacionControl.actualizarProcedimientosRiaCapita(dto, negociacionRangoPoblacion, negociacionId);
			}
		});
	}

	public void actualizarValorNegociadoProcedimientoRC(ProcedimientoNegociacionDto procedimiento,NegociacionRiaRangoPoblacionDto negociacionRangoPoblacion ,
			Long negociacionId, Integer userId){
		if (Integer.toString(procedimiento.getServicio()).equals(SERVICIO_MEDICAMENTO)) {
			servicioNegociacionControl.actualizarMedicamentosRiaCapita(procedimiento, negociacionRangoPoblacion, negociacionId, userId);
		} else {
			servicioNegociacionControl.actualizarProcedimientosRiaCapita(procedimiento, negociacionRangoPoblacion, negociacionId);
		}
	}
	/**
	 *
	 * @param negociacionId
	 * @param servicioId
	 */
	@Override
	public void actualizarValorNegociadoServicioNegociacion(Long negociacionId,Long servicioId, Integer userId){
		servicioNegociacionControl.actualizarValorNegociadoServicioNegociacion(negociacionId, servicioId, userId);
	}

	@Override
	public void actualizarValorNegociadoCapituloNegociacion(Long negociacionId,Long capituloId, Integer userId) throws ConexiaBusinessException{
		servicioNegociacionControl.actualizarValorNegociadoCapituloNegociacion(negociacionId, capituloId, userId);
	}

	@Override
	public void distribuirRias(BigDecimal valorServNegociado, Double porcentajeServNegociado,
			NegociacionDto negociacion, Integer negociacionRiaRangoPoblacionId, Integer userId) {
		servicioNegociacionControl.distribuirRias(valorServNegociado, porcentajeServNegociado, negociacion, negociacionRiaRangoPoblacionId, userId);
	}

	@Override
	public void actualizarNegociadoServicios(Long negociacionId) {
		em.createNamedQuery("SedeNegociacionServicio.generaraUpdateNegociadoServicios")
					.setParameter("negociacionId", negociacionId)
					.executeUpdate();

	}

	@Override
	public void actualizarRiasRangoPoblacionById(NegociacionRiaRangoPoblacionDto negociacionRiaRangoPoblacionDto) {
		servicioNegociacionControl.actualizarRiaRangoPoblacionById(negociacionRiaRangoPoblacionDto);
	}

	@Override
	public void eliminarTecnologiasRutas(Long negociacionId,List<NegociacionRiaRangoPoblacionDto> riasSeleccionadas, Integer userId){
		List<Integer> riasNegociacionIds = new ArrayList<>();
		riasNegociacionIds.addAll(riasSeleccionadas.stream().map(r -> r.getId()).collect(Collectors.toList()));

		StringBuilder auditoriaQuery = new StringBuilder();
		auditoriaQuery.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarProcedimientos())
		.append(" where snp.id in (")
		.append(" 	SELECT snp.id FROM contratacion.sede_negociacion_procedimiento snp ")
		.append(" 	JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id ")
		.append(" 	JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id ")
		.append(" 	JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id ")
		.append(" 	JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id ")
		.append(" 	JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id and snp.negociacion_ria_rango_poblacion_id = nrp.id ")
		.append(" 	WHERE sn.negociacion_id = :negociacionId AND snp.negociacion_ria_rango_poblacion_id in(:negociacionRiasIds) ")
		.append(" )");

		//Para insertar registros en auditoría de los procedimientos a eliminar
		em.createNativeQuery(auditoriaQuery.toString())
			.setParameter("negociacionId", negociacionId)
			.setParameter("negociacionRiasIds", riasNegociacionIds)
			.setParameter("userId", userId)
			.executeUpdate();

		StringBuilder auditoriaMedicamentos = new StringBuilder();
		auditoriaMedicamentos.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarMedicamentos())
		.append(" where snm.id in (")
		.append(" 	SELECT snm.id FROM contratacion.sede_negociacion_medicamento snm ")
		.append(" 	JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id ")
		.append(" 	JOIN maestros.medicamento m ON snm.medicamento_id = m.id ")
		.append(" 	JOIN contratacion.negociacion_ria nr ON sn.negociacion_id = nr.negociacion_id ")
		.append(" 	JOIN contratacion.negociacion_ria_rango_poblacion nrp ON nrp.negociacion_ria_id = nr.id and snm.negociacion_ria_rango_poblacion_id = nrp.id ")
		.append(" 	WHERE sn.negociacion_id = :negociacionId AND snm.negociacion_ria_rango_poblacion_id in(:negociacionRiasIds)")
		.append(" )");

		//Para registrar en auditoria los medicamentos a eliminar
		em.createNativeQuery(auditoriaMedicamentos.toString())
			.setParameter("userId", userId)
			.setParameter("negociacionId", negociacionId)
			.setParameter("negociacionRiasIds", riasNegociacionIds)
			.executeUpdate();

		em.createNamedQuery("SedeNegociacionProcedimiento.borrarTodosProcedimientosRutasSeleccionadasNegociacion")
			.setParameter("negociacionId", negociacionId)
			.setParameter("negociacionRiasIds", riasNegociacionIds)
			.executeUpdate();

		em.createNamedQuery("SedeNegociacionMedicamento.borrarTodosMedicamentosRutasSeleccionadasNegociacion")
			.setParameter("negociacionId", negociacionId)
			.setParameter("negociacionRiasIds", riasNegociacionIds)
			.executeUpdate();
	}
	public AfiliadoDto findAfiliadoByTipoYNumeroIdentificacion(String tipoIdentificacion, String numeroIdentificacion, Date fechaCorteDB) {
		try{
			AfiliadoDto afiliado = (AfiliadoDto) em.createNativeQuery(this.servicioNegociacionControl.generarFindAfiliadoByTipoYNumeroIdentificacion(),
									"Afiliado.afiliadoByTipoNoIdentificacionMapping")
					.setParameter("tipoIdentificacion", tipoIdentificacion)
					.setParameter("numeroIdentificacion", numeroIdentificacion)
					.setParameter("fechaCorte", fechaCorteDB)
					.getSingleResult();

			return afiliado;
		} catch (NonUniqueResultException nore){
			return null;
		} catch (NoResultException nre){
			return null;
		}
	}

	public List<SedePrestadorDto> findSedeIpsByNegociacionId(Long negociacionId) {
		try{
			return em.createNamedQuery("SedePrestador.findSedesByNegociacionId", SedePrestadorDto.class)
					.setParameter("negociacionId", negociacionId)
					.getResultList();
		} catch (NoResultException nre){
			return null;
		}
	}

	public void insertarAfiliadosPorSedeNegociacion(Long afiliadoId, Long sedeNegociacionId) {
		try{

			StringBuilder query = new StringBuilder();
			query.append("  INSERT INTO contratacion.afiliado_x_sede_negociacion ")
			.append("  (afiliado_id, sede_negociacion_id) ")
			.append("  VALUES( :afiliadoId, :sedeNegociacionId)")
			.append("  on conflict (afiliado_id, sede_negociacion_id)")
			.append(" do nothing ");

			em.createNativeQuery(query.toString())
			.setParameter("afiliadoId", afiliadoId)
			.setParameter("sedeNegociacionId", sedeNegociacionId)
			.executeUpdate();
		} catch(Exception ex){
			//Llave duplicada, ya existe el afil. en la negociación.
		}

	}

	public List<AfiliadoDto> findAfiliadosPorSedeNegociacion(Long idSedeNegociacion, Long negociacionId){
		try{
			StringBuilder query = new StringBuilder("SELECT a.id afiliadoId,  "
							+ " ti.id tipoDocId, ti.descripcion tipoDocDescripcion,"
							+ " a.numero_identificacion numero_identificacion, a.primer_nombre primer_nombre, "
							+ " a.segundo_nombre segundo_nombre, a.primer_apellido primer_apellido, a.segundo_apellido,"
							+ " CONCAT(sp.codigo_habilitacion,'-',sp.codigo_sede) AS codigo_sede,"
							+ " a.codigo_unico_afiliado, a.fecha_nacimiento, m.descripcion AS municipio_residencia,"
							+ " a.fecha_afiliacion_eps, sp.nombre_sede "
						+ " FROM contratacion.afiliado_x_sede_negociacion asn "
						+ " JOIN maestros.afiliado a ON asn.afiliado_id = a.id "
						+ " JOIN maestros.municipio m ON m.id = a.municipio_residencia_id "
						+ " JOIN maestros.tipo_identificacion ti on ti.id = a.tipo_identificacion_id "
						+ " JOIN contratacion.sedes_negociacion sn ON sn.id = asn.sede_negociacion_id "
						+ " JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id "
						+ " WHERE sn.id = :idSedeNegociacion ");

			return em.createNativeQuery(query.toString(), "AfiliadoPorSedeNegociacionDto")
			.setParameter("idSedeNegociacion", idSedeNegociacion)
			.getResultList();
		} catch (NoResultException nre){
			return null;
		}
	}

	public void eliminarAfiliadoNegociacionPgp(Long afiliadoId, Long sedeNegociacionId) {
		em.createNativeQuery("DELETE FROM contratacion.afiliado_x_sede_negociacion "
				+ " WHERE afiliado_id = :afiliadoId "
				+ " AND sede_negociacion_id = :sedeNegociacionId ")
		.setParameter("afiliadoId", afiliadoId)
		.setParameter("sedeNegociacionId", sedeNegociacionId)
		.executeUpdate();
	}

	@Override
	public void actualizarValorNegociadoTecnologiasNegociadasRuta(NegociacionRiaRangoPoblacionDto negociacionRiaRangoPoblacionDto, Long negociacionId){
		if(Objects.nonNull(negociacionRiaRangoPoblacionDto)) {
			em.createNamedQuery("SedeNegociacionProcedimiento.actualizarValorNegociadoProcedimientosNegociadosRuta")
				.setParameter("upcNegociada", negociacionRiaRangoPoblacionDto.getUpcNegociada())
				.setParameter("negociacionId", negociacionId)
				.setParameter("negociacionRiaId", negociacionRiaRangoPoblacionDto.getId())
				.executeUpdate();

			em.createNamedQuery("SedeNegociacionMedicamento.actualizarValorNegociadoMedicamentosNegociadosRuta")
			.setParameter("upcNegociada", negociacionRiaRangoPoblacionDto.getUpcNegociada())
			.setParameter("negociacionId", negociacionId)
			.setParameter("negociacionRiaId", negociacionRiaRangoPoblacionDto.getId())
			.executeUpdate();
		}
	}

	@Override
	public void agregarServiciosNegociacionMaestros(List<ServicioSaludDto> servicios, NegociacionDto negociacion, Integer userId,
                                                        Long negociacionReferenteId) {
		this.servicioNegociacionControl.agregarServiciosNegociacionMaestros(negociacion, servicios, userId);
                this.servicioNegociacionControl.agregarProcedimientosAServiciosAgregadosMaestros(negociacion, servicios, userId);
                String serviciosId = servicios.stream()
				.map(sn -> String.valueOf(sn.getId()))
				.collect(Collectors.joining(","));
                ejecutarActualizacionServiciosYProcedimientos(negociacion.getId(), userId, negociacionReferenteId, serviciosId);
	}

}
