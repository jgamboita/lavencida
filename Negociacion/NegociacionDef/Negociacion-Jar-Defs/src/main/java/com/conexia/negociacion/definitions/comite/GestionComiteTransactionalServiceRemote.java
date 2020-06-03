package com.conexia.negociacion.definitions.comite;

import java.util.List;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contratacion.commons.dto.comite.AsistenteComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.PrestadorComiteDto;
import com.conexia.contratacion.commons.dto.comite.TablaPrestadoresComiteDto;
import com.conexia.contratacion.commons.dto.comparacion.ResumenComparacionDto;

/**
 * @author jtorres Interfaz remota para los servicios del boundary transaccional
 *         de gestionar un comite.
 */
public interface GestionComiteTransactionalServiceRemote {

	/**
	 * Actualiza un asistente dado su comite al que pertenece
	 *
	 * @param asistente
	 *            Asistente con los datos actualizados.
	 * @param asistenteId
	 *            La id del asistente a editar.
	 */
	void actualizarAsistenteComite(AsistenteComitePrecontratacionDto asistente,
			Long asistenteId);

	/**
	 * Asigna prestadores a un comité especifico
	 * 
	 * @param prestadores
	 *            a asignar
	 * @param comite
	 *            que se les asignara
	 */
	void asignarComite(List<PrestadorComiteDto> prestadores,
			ComitePrecontratacionDto comite);

	/**
	 * Guarda el concepto del prestador comite en la base de datos
	 *
	 * @param prestadorComite
	 */
	void guardarConceptoPrestadorComite(
			TablaPrestadoresComiteDto prestadorComite);

	Long crearPrestadorComite(String justificacion, Long comiteId,
			Long prestadorId, String tipoTecnologias);

	void crearResumenComparacionTarifas(ResumenComparacionDto dto,
			Long prestadorComiteId);

	Integer actualizarEstadoPrestador(final EstadoPrestadorEnum estado,
			final Long prestadorId);

	/**
	 * Firma de método que se encarga de realizar el flujo completo de envio de
	 * un prestador a comité
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
	void enviarPrestadorAComite(final ComitePrecontratacionDto comiteDto,
			final List<ResumenComparacionDto> reportesDescargados,
			final String justificacionComite,
			final String tecnologiasPrestador, final Long prestadorId);

	/**
	 * Realiza todo el proceso de aplazar un comite.
	 *
	 * @param comiteActual
	 * @param comiteNuevo
	 * @param usuarioId
	 */
	void aplazarComite(ComitePrecontratacionDto comiteActual,
			ComitePrecontratacionDto comiteNuevo, Integer usuarioId);

	/**
	 * Finaliza un comite
	 *
	 * @param comiteId
	 */
	void finalizarComite(Long comiteId, Integer usuarioId);

	/**
	 * Aumenta la cantidad de prestadores de un comité
	 *
	 * @param comiteId
	 *            identificador del comité
	 */
	void aumentarPrestadoresComite(final Long comiteId);

	/**
	 * Registra el cambio de estado del prestador
	 * @param estado
	 * @param prestadorId
	 * @param usuarioId
	 * @return
	 */
	Integer actualizarEstadoPrestador(final EstadoPrestadorEnum estado,	final Long prestadorId, final Integer usuarioId);
}
