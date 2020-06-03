package co.conexia.negociacion.wap.facade.comite;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.ResourceBundle;

import javax.inject.Inject;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contratacion.commons.dto.comite.AsistenteComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.TablaPrestadoresComiteDto;
import com.conexia.contratacion.commons.dto.comparacion.ResumenComparacionDto;
import com.conexia.negociacion.definitions.comite.GestionComiteTransactionalServiceRemote;
import com.conexia.negociacion.definitions.comite.GestionComiteViewServiceRemote;
import com.conexia.servicefactory.CnxService;
import com.conexia.webutils.i18n.CnxI18n;

public class GestionComiteFacade implements Serializable {

	/**
     *
     */
	private static final long serialVersionUID = -8676937280733951064L;

	@Inject
	@CnxService
	private GestionComiteViewServiceRemote gestionComiteViewService;
	@Inject
	@CnxService
	private GestionComiteTransactionalServiceRemote gestionComiteTransactional;

	@Inject
	@CnxI18n
	transient ResourceBundle resourceBundle;

	/**
	 * Metodo que devuelve la lista de los prestadores a evaluar en un comite
	 * determinado
	 *
	 * @param comiteId
	 * @return Lista de Prestadores
	 */
	public List<TablaPrestadoresComiteDto> buscarPrestadoresComiteByComiteId(
			Long comiteId) {
		return gestionComiteViewService
				.buscarPrestadoresComiteByComiteId(comiteId);
	}

	/**
	 * Busca asistentes a un comite de precontratacion dado el comite.
	 *
	 * @param comiteId
	 *            la id del comite.
	 * @return Lista con los asistentes al comite.
	 */
	public List<AsistenteComitePrecontratacionDto> buscarAsistentesComite(
			Long comiteId) {
		return gestionComiteViewService.buscarAsistentesComite(comiteId);
	}

	/**
	 * Actualiza un asistente dado su comite al que pertenece
	 *
	 * @param asistente
	 *            Asistente con los datos actualizados.
	 * @param asistenteId
	 *            La id del asistente a editar.
	 */
	public void actualizarAsistenteComite(
			AsistenteComitePrecontratacionDto asistente, Long asistenteId) {
		gestionComiteTransactional.actualizarAsistenteComite(asistente,
				asistenteId);
	}

	public Long crearPrestadorComite(String justificacion, Long comiteId,
			Long prestadorId, String tipoTecnologias) {
		return gestionComiteTransactional.crearPrestadorComite(justificacion,
				comiteId, prestadorId, tipoTecnologias);
	}

	/**
	 * Guarda el concepto del prestador comite en la base de datos
	 *
	 * @param prestadorComite
	 */
	public void guardarConceptoPrestadorComite(
			TablaPrestadoresComiteDto prestadorComite) {
		this.gestionComiteTransactional
				.guardarConceptoPrestadorComite(prestadorComite);
	}

	/**
	 * Busca un comite válido disponible en el sistema
	 *
	 * @return Un comite con la id asociada o <b>null</b> en el caso que no
	 *         encuentre el comite.
	 */
	public ComitePrecontratacionDto buscarComiteDisponible() {
		return gestionComiteViewService.buscarComiteDisponible();
	}

	public void crearResumenComparacionTarifas(ResumenComparacionDto dto,
			Long prestadorComiteId) {
		gestionComiteTransactional.crearResumenComparacionTarifas(dto,
				prestadorComiteId);
	}

	public Integer actualizarEstadoPrestador(final EstadoPrestadorEnum estado,
			final Long prestadorId) {
		return this.gestionComiteTransactional.actualizarEstadoPrestador(
				estado, prestadorId);
	}
	
	public Integer actualizarEstadoPrestador(final EstadoPrestadorEnum estado, final Long prestadorId, final Integer usuarioId) {
		return this.gestionComiteTransactional.actualizarEstadoPrestador(estado, prestadorId, usuarioId);
	}


	/**
	 * Realiza todo el proceso de aplazar un comite.
	 *
	 * @param comiteId
	 *            el comite a actualizar.
	 */
	public void aplazarComite(ComitePrecontratacionDto comiteActual,
			ComitePrecontratacionDto comiteNuevo, Integer usuarioId) {

		gestionComiteTransactional.aplazarComite(comiteActual, comiteNuevo, usuarioId);
	}

	/**
	 * Realiza todo el proceso de finalizar un comite.
	 *
	 * @param comiteId
	 *            el comite a actualizar.
	 */
	public void finalizarComite(Long comiteId, Integer usuarioId) {
		this.gestionComiteTransactional.finalizarComite(comiteId, usuarioId);
	}

	/**
	 * Consulta de el resumen de comparacion por el id del prestador comite
	 *
	 * @param idPrestadorComite
	 *            Identificador del prestador comite
	 * @return lista de {@link - ResumenComparacionDto}
	 */
	public List<ResumenComparacionDto> consultarResumenComparacionByPrestadorComiteId(
			Long idPrestadorComite) {
		return this.gestionComiteViewService
				.consultarResumenComparacionByPrestadorComiteId(idPrestadorComite);
	}

	/**
	 * Encargado de realizar el flujo completo de envio de un prestador a comité
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
	public void enviarPrestadorAComite(
			final ComitePrecontratacionDto comiteDto,
			final List<ResumenComparacionDto> reportesDescargados,
			final String justificacionComite,
			final String tecnologiasPrestador, final Long prestadorId) {
		this.gestionComiteTransactional.enviarPrestadorAComite(comiteDto,
				reportesDescargados, justificacionComite, tecnologiasPrestador,
				prestadorId);
	}

	/**
	 * Valida si ya existe un acta para el comite con identificador igual al
	 * pasado como parametro.
	 * 
	 * @param comiteId
	 *            - Identificador unico de comite
	 * @return true si ya se a creado una entrada de acta para el comite, false
	 *         de no existir ninguna entrada
	 */
	public boolean validarExisteActaComite(Long comiteId) {
		return gestionComiteViewService.validarExisteActaComite(comiteId);
	}

	public boolean evaluarFechaDisponible(Date fechaAplazamiento) {
		return gestionComiteViewService.evaluarFechaDisponible(fechaAplazamiento);
	}

}
