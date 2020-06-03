package co.conexia.negociacion.services.comite.boundary;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import co.conexia.negociacion.services.comite.control.GestionComiteControl;

import com.conexia.contratacion.commons.constants.enums.EstadoComiteEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorComiteEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.model.contratacion.Trazabilidad;
import com.conexia.contractual.model.contratacion.comite.ComitePrecontratacion;
import com.conexia.contractual.model.contratacion.comite.PrestadorComite;
import com.conexia.contractual.model.contratacion.comite.ResumenComparacionTarifas;
import com.conexia.contractual.model.security.User;
import com.conexia.contratacion.commons.dto.comite.AsistenteComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.PrestadorComiteDto;
import com.conexia.contratacion.commons.dto.comite.TablaPrestadoresComiteDto;
import com.conexia.contratacion.commons.dto.comparacion.ResumenComparacionDto;
import com.conexia.negociacion.definitions.comite.GestionComiteTransactionalServiceRemote;

/**
 * Boundary que contiene las consultas utilizadas en la gestion de comites
 *
 * @author etorres
 */
@Stateless
@Remote(GestionComiteTransactionalServiceRemote.class)
public class GestionComiteTransactionalBoundary implements
		GestionComiteTransactionalServiceRemote {

	@PersistenceContext(unitName = "contractualDS")
	EntityManager em;

	@Inject
	GestionComiteControl gestionComiteControl;

	/**
	 * Actualiza un asistente dado su comite al que pertenece
	 *
	 * @param asistente
	 *            Asistente con los datos actualizados.
	 * @param asistenteId
	 *            La id del asistente a editar.
	 */
	@Override
	public void actualizarAsistenteComite(
			AsistenteComitePrecontratacionDto asistente, Long asistenteId) {
		em.createNamedQuery("AsistenteComitePrecontratacion.updateById")
				.setParameter("cargo", asistente.getCargo())
				.setParameter("nombres", asistente.getNombre())
				.setParameter("siAsiste", asistente.getSiAsiste())
				.setParameter("siTieneVoz", asistente.getSiTieneVoz())
				.setParameter("siTieneVoto", asistente.getSiTieneVoto())
				.setParameter("asistenteId", asistenteId).executeUpdate();
	}

	@Override
	public void asignarComite(List<PrestadorComiteDto> prestadores,
			ComitePrecontratacionDto comite) {
		int disponible = (Objects.isNull(comite)) ? prestadores.size() : comite
				.getLimitePrestadores() - comite.getCantidadPrestadores();
		List<PrestadorComiteDto> iterar = prestadores.subList(0, disponible);
		for (PrestadorComiteDto pc : iterar) {
			this.crearPrestadorComite(pc.getJustificacionEnvioComite(),
					comite.getId(), pc.getPrestador().getId(),
					pc.getTipoTecnologiasPrestador());
			if (comite != null) {
				this.aumentarPrestadoresComite(comite.getId());
			}
		}
		if (disponible < prestadores.size()) {
			this.asignarComite(
					prestadores.subList(disponible, prestadores.size()),
					this.gestionComiteControl.buscarComiteDisponible());
		}
	}

	/**
	 * Guarda el concepto del prestador comite en la base de datos
	 *
	 * @param prestadorComite
	 */
	@Override
	public void guardarConceptoPrestadorComite(
			TablaPrestadoresComiteDto prestadorComite) {
		em.createNamedQuery("PrestadorComite.updateEestadoAndConceptoById")
				.setParameter("estado", prestadorComite.getEstado())
				.setParameter("concepto", prestadorComite.getConceptoComite())
				.setParameter("motivoRechazo",
						prestadorComite.getMotivoRechazo())
				.setParameter("prestadorComiteId",
						prestadorComite.getIdPrestadorComite()).executeUpdate();
	}

	@Override
	public Long crearPrestadorComite(String justificacion, Long comiteId,
			Long prestadorId, String tipoTecnologias) {
		PrestadorComite prestadorComite = new PrestadorComite();
		prestadorComite.setJustificacionEnvioComite(justificacion);
		prestadorComite.setFechaAsociacionComite(Calendar.getInstance()
				.getTime());
		prestadorComite.setEstado(EstadoPrestadorComiteEnum.PENDIENTE_COMITE);
		if (comiteId != null) {
			prestadorComite.setEstado(EstadoPrestadorComiteEnum.PROGRAMADO);
			prestadorComite.setComite(new ComitePrecontratacion(comiteId));
		}

        prestadorComite.setTipoModalidad(NegociacionModalidadEnum.EVENTO);
		prestadorComite.setPrestador(new Prestador(prestadorId));
		prestadorComite.setTipoTecnologiasPrestador(tipoTecnologias);
		em.persist(prestadorComite);

		return prestadorComite.getId();

	}

	@Override
	public void crearResumenComparacionTarifas(ResumenComparacionDto dto,
			Long prestadorComiteId) {
		ResumenComparacionTarifas rct = new ResumenComparacionTarifas();
		rct.setSedesPrestadorSeleccionadas(dto.getSedesPrestador() != null ? dto
				.getSedesPrestador() : null);
		rct.setGruposHabilitacionSeleccionados(dto.getGruposHabilitacion() != null ? dto
				.getGruposHabilitacion() : null);
		rct.setServiciosSaludSeleccionados(dto.getServiciosSalud() != null ? dto
				.getServiciosSalud() : null);
		rct.setTablaComparacionSeleccionados(dto.getSedesComparadas() != null ? dto
				.getSedesComparadas() : null);
		rct.setTipoResumen(dto.getTecnologia() != null ? dto.getTecnologia()
				: null);
		rct.setPrestadorComite(new PrestadorComite(prestadorComiteId));

		em.persist(rct);
	}

	@Override
	public Integer actualizarEstadoPrestador(final EstadoPrestadorEnum estado,
			final Long prestadorId) {
		return em.createNamedQuery("Prestador.actualizarEstadoPrestador")
				.setParameter("estadoPrestador", estado)
				.setParameter("prestadorId", prestadorId).executeUpdate();
	}

	/**
	 * Realiza todo el proceso de aplazar un comite.
	 *
	 * @param comiteId
	 *            el comite a actualizar.
	 */
	@Override
	public void aplazarComite(ComitePrecontratacionDto comiteActual,
			ComitePrecontratacionDto comiteNuevo, Integer usuarioId) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

		// Inicio de registro de trazabilidad

		Trazabilidad traza = new Trazabilidad();
		traza.setDescripcion("Aplazamiento de comite.");
		traza.setValorAnterior("[Estado= "
				+ comiteActual.getEstadoComite().name() + ", Fecha comite="
				+ dateFormat.format(comiteActual.getFechaComite()) + "]");
		traza.setValorNuevo("[Estado= " + comiteNuevo.getEstadoComite().name()
				+ ", Fecha comite="
				+ dateFormat.format(comiteNuevo.getFechaComite()) + "]");
		traza.setLlavePrimaria(comiteActual.getId());
		traza.setNombreTabla("contratacion.comite_precontratacion");
		traza.setFechaModificacion(new Date());
		traza.setUsuario(new User(usuarioId));
		em.persist(traza);

		// Fin de registro de trazabilidad

		// Inicio actualizacion de comite actual

		em.createNamedQuery("ComitePrecontratacion.updateDatesAndEstadoById")
				.setParameter("comiteId", comiteActual.getId())
				.setParameter("estadoComite", EstadoComiteEnum.REPROGRAMADO)
				.setParameter("fechaComite", comiteNuevo.getFechaComite())
				.setParameter("fechaLimitePrestadores",
						comiteActual.getFechaLimitePrestadores())
				.executeUpdate();

		// Fin actualizacion de comite actual

		// Inicio actualizacion prestadores comite

		/*
		 * Se modifican unicamente los registros que sufrieron algun cambio
		 * durante el comite y fueron aplazados manualmente, en este punto no
		 * deberia llegar ninguna solicitud negada o aprobada
		 */
		em.createNamedQuery("PrestadorComite.cambiarEstadoPorComiteIdYEstado")
				.setParameter("comiteId", comiteActual.getId())
				.setParameter("nuevoEstado",
						EstadoPrestadorComiteEnum.PROGRAMADO)
				.setParameter("estadoAnterior",
						EstadoPrestadorComiteEnum.APLAZADO).executeUpdate();

		// Fin actualizacion prestadores comite
	}

	/**
	 * Finaliza un comite
	 *
	 * @param comiteId
	 */
	@Override
	public void finalizarComite(Long comiteId, Integer usuarioId) {

		/*
		 * Actualiza el estado de los portafolios de prestadores cuyas
		 * solicitudes finalizaron con concepto favorable, dejando el estado de
		 * prestador en estado de negociacion
		 */
		int portafoliosActualizados = em
				.createNamedQuery(
						"Prestador.updateEstadoPorEstadoSolicitudYComiteId")
				.setParameter("estadoPrestador",
						EstadoPrestadorEnum.EN_NEGOCIACION)
				.setParameter("comiteId", comiteId)
				.setParameter("estadoSolicitud",
						EstadoPrestadorComiteEnum.APROBADO).executeUpdate();

		/*
		 * Se libera el portafolio de los prestadores para los cuales las
		 * solicitudes finalizaron con estado desfavorable, dejando el estado en
		 * null y las fecha y los meses de vijencia en null.
		 */
		int portafoliosActivados = em
				.createNamedQuery(
						"Prestador.activarPortafolioPrestadorPorEstadoSolicitudYComiteId")
				.setParameter("estadoPrestador", null)
				.setParameter("fechaVigencia", null)
				.setParameter("mesVigencia", null)
				.setParameter("comiteId", comiteId)
				.setParameter("estadoSolicitud",
						EstadoPrestadorComiteEnum.NEGADO).executeUpdate();

		/*
		 * Actualiza las solicitutes que finalizaron con estado programado,
		 * marcandolas en estado aplazado
		 */
		int solicitudesCambiadasProgramadas = em
				.createNamedQuery(
						"PrestadorComite.cambiarEstadoPorComiteIdYEstado")
				.setParameter("comiteId", comiteId)
				.setParameter("nuevoEstado", EstadoPrestadorComiteEnum.APLAZADO)
				.setParameter("estadoAnterior",
						EstadoPrestadorComiteEnum.PROGRAMADO).executeUpdate();

		/*
		 * Obtiene las solicitudes que fueron aplazadas para el comite actual
		 */
		List<PrestadorComite> prestadoresComite = em
				.createNamedQuery(
						"PrestadorComite.obtenerEntidadPorComiteIdYEstado",
						PrestadorComite.class)
				.setParameter("comiteId", comiteId)
				.setParameter("estado", EstadoPrestadorComiteEnum.APLAZADO)
				.getResultList();
		
		// Inicio de registro de trazabilidad

		Trazabilidad trazaComiteAnterior = new Trazabilidad();
		trazaComiteAnterior.setLlavePrimaria(comiteId);
		trazaComiteAnterior.setNombreTabla("contratacion.comite_precontratacion");
		trazaComiteAnterior.setDescripcion("Finalizaci\u00f3n de comite.");
		
		trazaComiteAnterior.setValorAnterior("[Estado = "
				+ em.getReference(ComitePrecontratacion.class, comiteId)
						.getEstadoComite()
				+ "]");
		
		trazaComiteAnterior.setValorNuevo("[Estado = " + EstadoComiteEnum.FINALIZADO
						+ ", Prestadores en negociacion = "
						+ portafoliosActualizados
						+ ", Portafolios habilitados = " + portafoliosActivados
						+ ", Solicitudes aplazadas = "
						+ prestadoresComite.size() + "]");
		
		trazaComiteAnterior.setFechaModificacion(new Date());
		trazaComiteAnterior.setUsuario(new User(usuarioId));
		em.persist(trazaComiteAnterior);

		// Fin de registro de trazabilidad

		/*
		 * Marca el comite como finalizado
		 */
		em.createNamedQuery("ComitePrecontratacion.updateEstadoById")
				.setParameter("comiteId", comiteId)
				.setParameter("estado", EstadoComiteEnum.FINALIZADO)
				.executeUpdate();
		
		// Si no hay solicitudes por aplazar corta la ejecucion
		if(prestadoresComite.isEmpty()){
			return;			
		}
		
		/*
		 * Se obtine el comite proximo a ejecutarse, que tenga fecha de
		 * recepcion de solicitudes valida
		 */
		ComitePrecontratacion comite = null;
		try {
			ComitePrecontratacionDto proximoComite = em
					.createNamedQuery("ComitePrecontratacion.findNext",
							ComitePrecontratacionDto.class)
					.setParameter("comiteId", comiteId).setMaxResults(1)
					.getSingleResult();

			comite = new ComitePrecontratacion(proximoComite.getId());
			comite.setLimitePrestadores(proximoComite.getLimitePrestadores());
		} catch (NoResultException e) {}

		/*
		 * Se crean y se asocian las solicitude al comite hallado, de no haber
		 * encontrado comite se generan como pendientes de solicitud
		 */
		for (PrestadorComite prestadorComite : prestadoresComite) {
		    prestadorComite.setTipoModalidad(NegociacionModalidadEnum.EVENTO);
			if (comite != null) {
				prestadorComite.setComite(comite);
				prestadorComite.setEstado(EstadoPrestadorComiteEnum.PROGRAMADO);
			} else {
				prestadorComite
						.setEstado(EstadoPrestadorComiteEnum.PENDIENTE_COMITE);
			}

			prestadorComite.setFechaAsociacionComite(new Date());
			em.persist(prestadorComite);
		}

		if (comite != null) {
			Integer cantidadPrestadores = em
					.createNamedQuery(
							"PrestadorComite.contarPorComiteIdYEstado",
							Integer.class)
					.setParameter("comiteId", comite.getId())
					.setParameter("estado",	EstadoPrestadorComiteEnum.PROGRAMADO)
					.getSingleResult();

			Trazabilidad trazaComiteNuevo = new Trazabilidad();
			trazaComiteNuevo
					.setDescripcion("Asociacion solicitudes aplazadas de comite anterior.");
			trazaComiteNuevo.setValorAnterior("[Comite anterior = " + comiteId
					+ ", Solicitudes = " + comite.getCantidadPrestadores()
					+ ", Solicitudes permitidas = "
					+ comite.getLimitePrestadores() + "]");

			trazaComiteNuevo.setValorNuevo("[Comite nuevo = "
					+ comite.getId() + ", Solicitudes = "
					+ cantidadPrestadores
					+ ", Solicitudes permitidas = "
					+ comite.getLimitePrestadores() + "]");
			trazaComiteNuevo.setLlavePrimaria(comite.getId());
			trazaComiteNuevo
					.setNombreTabla("contratacion.comite_precontratacion");
			trazaComiteNuevo.setFechaModificacion(new Date());
			trazaComiteNuevo.setUsuario(new User(usuarioId));
			em.persist(trazaComiteNuevo);

			em.getReference(ComitePrecontratacion.class, comite.getId())
				.setCantidadPrestadores(cantidadPrestadores);
		}
	}

	/**
	 * Método que se encarga de realizar el flujo completo de envio de un
	 * prestador a comité
	 *
	 * @param comiteDto
	 *            comite disponible para envío del prestador, si vuelve nulo no
	 *            se asigna a ningún comité el prestador
	 * @param reportesDescargados
	 *            lista de reportes descargados
	 * @param justificacionComite
	 *            String con la justificación del envío a comité
	 * @param tecnologiasPrestador
	 *            String con las tecnologías que maneja el prestador
	 * @param prestadorId
	 *            Long que contiene el id del prestador
	 */
	@Override
	public void enviarPrestadorAComite(
			final ComitePrecontratacionDto comiteDto,
			final List<ResumenComparacionDto> reportesDescargados,
			final String justificacionComite,
			final String tecnologiasPrestador, final Long prestadorId) {

		// Se crea el prestadorComite
		Long prestadorComiteId = this.crearPrestadorComite(
				justificacionComite,
				(comiteDto != null && comiteDto.getId() != null) ? comiteDto
						.getId() : null, prestadorId, tecnologiasPrestador);

		// Persistimos uno a uno el resumen de comparación
		for (ResumenComparacionDto dto : reportesDescargados) {
			this.crearResumenComparacionTarifas(dto, prestadorComiteId);
		}

		if ((comiteDto!=null)&&(comiteDto.getId() != null)) {
			this.aumentarPrestadoresComite(comiteDto.getId());
		}

		// Se actualiza el estado del prestador
		this.actualizarEstadoPrestador(EstadoPrestadorEnum.PENDIENTE_COMITE,
				prestadorId);
	}

	/**
	 * Método encargado de aumentar la cantidad de prestadores de un comité
	 *
	 * @param comiteId
	 */
	@Override
	public void aumentarPrestadoresComite(final Long comiteId) {
		em.createNamedQuery("ComitePrecontratacion.aumentarPrestadoresComite")
				.setParameter("comiteId", comiteId).executeUpdate();
	}
	
	@Override
	public Integer actualizarEstadoPrestador(final EstadoPrestadorEnum estado,final Long prestadorId, final Integer usuarioId) {
		return em.createNamedQuery("Prestador.actualizarEstadoPrestadorUsuario")
				.setParameter("estadoPrestador", estado)
				.setParameter("prestadorId", prestadorId)				
				.setParameter("usuarioCambioEstadoId", usuarioId)
				.setParameter("fechaCambioEstado", new Date())
				.executeUpdate();
	}

}
