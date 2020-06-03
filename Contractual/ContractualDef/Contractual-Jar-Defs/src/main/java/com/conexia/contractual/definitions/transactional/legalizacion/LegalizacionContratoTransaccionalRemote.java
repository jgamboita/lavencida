package com.conexia.contractual.definitions.transactional.legalizacion;

import java.util.Date;

import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 *
 * @author jalvarado
 */
public interface LegalizacionContratoTransaccionalRemote {

    LegalizacionContratoDto legalizacionContrato(final LegalizacionContratoDto legalizacionContratoDto) throws ConexiaBusinessException;

    void actualizarContenidoLegalizacionContrato(final LegalizacionContratoDto legalizacionContratoDto) throws ConexiaBusinessException;

    byte[] descargarMinuta(String nombreArchivo) throws ConexiaBusinessException;

    void subirMinuta(Long contratoId, String nombre, byte[] contenido);

    void actualizarNumeroContrato (final ContratoDto contrato);

    void asignarFirmaVoBo (final LegalizacionContratoDto legalizacion);

	void actualizarEstadoContrato(final LegalizacionContratoDto solicitud);

	void actualizarValoresUpc(LegalizacionContratoDto legalizacionContratoDto) throws ConexiaBusinessException;

    /**
     * Guarda el historial del cambio realizado sobre el contrato
     * @param userId usuario que realiza la modificacion
	 * @param contratoId id del contrato modificado
	 * @param evento evento realizado sobre el contrato (CREAR, MODIFICAR)
	 * @param negociacionId id de la negociacion modificado
	 */
    void guardarHistorialContrato(Integer userId, Long contratoId, String evento, Long negociacionId);

    void cambiarVigenciaTecnologiasContratadas(Long negociacionId, Integer userId) throws ConexiaBusinessException;

    void cambiarFechaContratoByNegociacionId(Long negociacionId, Date fechaInicio, Date fechaFin) throws ConexiaBusinessException;

    MinutaDetalleDto guardarClausulaParagrafoOtroSi(MinutaDetalleDto edicion);

    MinutaDetalleDto editarClausulaParagrafoOtroSi(MinutaDetalleDto edicion);

    NegociacionDto consultarFechaOtroSi(Long numeroNegociacion) throws ConexiaBusinessException;

    void eliminarClausulaParagrafoEditado(MinutaDetalleDto minutaDetalle);

    void actualizarObservacionOtroSi(LegalizacionContratoDto legalizacionContratoDto);
}
