package com.conexia.contractual.wap.facade.legalizacion;

import com.conexia.contractual.definitions.transactional.legalizacion.LegalizacionContratoTransaccionalRemote;
import com.conexia.contractual.definitions.view.legalizacion.LegalizacionContratoViewRemote;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.TipoResponsableEnum;
import com.conexia.contractual.model.contratacion.portafolio.PaquetePortafolio;
import com.conexia.contractual.model.maestros.Medicamento;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.*;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.AreaInfluenciaDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.AreaCoberturaDTO;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.IncentivoModeloDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.servicefactory.CnxService;

import javax.inject.Inject;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * Facade de las tablas de legalizacion de contractual.
 *
 * @author jLopez
 */
public class LegalizacionFacade implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Inject
    @CnxService
    private LegalizacionContratoTransaccionalRemote legalizacionContratoTransaccionalRemote;

    @Inject
    @CnxService
    private LegalizacionContratoViewRemote legalizacionContratoViewRemote;

    public LegalizacionContratoDto almacenarLegalizacionContrato(LegalizacionContratoDto legalizacionContratoDto) throws ConexiaBusinessException {
        return this.legalizacionContratoTransaccionalRemote.legalizacionContrato(legalizacionContratoDto);
    }

    public List<AreaInfluenciaDto> listarMunicipiosContrato(final ContratoDto contrato) {
        return this.legalizacionContratoViewRemote.listarMunicipiosContrato(contrato);
    }

    public List<ResponsableContratoDto> listarResponsablesFirma(final Integer idRegional) {
        return this.legalizacionContratoViewRemote.listarResponsablesContrato(idRegional, TipoResponsableEnum.FIRMA_CONTRATO);
    }

    public List<ResponsableContratoDto> listarResponsablesVoBo(final Integer idRegional) {
        return this.legalizacionContratoViewRemote.listarResponsablesContrato(idRegional, TipoResponsableEnum.VO_BO);
    }

    public List<IncentivoModeloDto> listarIncentivosPorNegociacionId(final Long negociacionId) {
        return this.legalizacionContratoViewRemote.listarIncentivosPorNegociacionId(negociacionId);
    }

    public LegalizacionContratoDto buscarLegalizacionContrato(SolicitudContratacionParametrizableDto solicitud) throws ConexiaBusinessException {
        return this.legalizacionContratoViewRemote.buscarLegalizacionContrato(solicitud);
    }

    public GeneracionMinutaDto generacionMinutaPorId(final Long idLegalizacionContrato, final Integer regionalId, final Long negociacionId) throws ConexiaBusinessException {
        return this.legalizacionContratoViewRemote.generacionMinutaPorId(idLegalizacionContrato, regionalId, negociacionId);
    }

    public void actualizarContenidoLegalizacionContrato(LegalizacionContratoDto legalizacionContratoDto)
            throws ConexiaBusinessException {
        this.legalizacionContratoTransaccionalRemote.actualizarContenidoLegalizacionContrato(legalizacionContratoDto);
    }

    public void actualizarValoresUpc(LegalizacionContratoDto legalizacionContratoDto)
            throws ConexiaBusinessException {
        this.legalizacionContratoTransaccionalRemote.actualizarValoresUpc(legalizacionContratoDto);
    }

    public byte[] descargarMinuta(String nombreArchivo) throws ConexiaBusinessException {
        return this.legalizacionContratoTransaccionalRemote.descargarMinuta(nombreArchivo);
    }

    public void subirMinuta(Long contratoId, String nombre, byte[] contenido) {
        this.legalizacionContratoTransaccionalRemote.subirMinuta(contratoId, nombre, contenido);
    }

    public NegociacionDto consultarNegociacionById(final Long negociacionId){
        return this.legalizacionContratoViewRemote.consultarNegociacionById(negociacionId);
    }

    public List<IncentivoModeloDto> listarModelosPorNegociacionId(
            Long negociacionId) {
        return this.legalizacionContratoViewRemote.listarModelosPorNegociacionId(negociacionId);
    }


    public BigDecimal sumPorcentajeTotalTemaServiciosPyp (final Long negociacionId){
    	return this.legalizacionContratoViewRemote.sumPorcentajeTotalTemaServiciosPyp(negociacionId);
    }

    public BigDecimal sumPorcentajeTotalTemaServiciosRecuperacion (final Long negociacionId){
    	return this.legalizacionContratoViewRemote.sumPorcentajeTotalTemaServiciosRecuperacion(negociacionId);
    }

    public BigDecimal sumPorcentajeTotalTemaMedicamentosPyp (final Long negociacionId){
    	return this.legalizacionContratoViewRemote.sumPorcentajeTotalTemaMedicamentosPyp(negociacionId);
    }

    public BigDecimal sumPorcentajeTotalTemaMedicamentosRecuperacion (final Long negociacionId){
    	return this.legalizacionContratoViewRemote.sumPorcentajeTotalTemaMedicamentosRecuperacion(negociacionId);
    }

    public Double sumPorcentajePypRiaCapita (final Long negociacionId){
    	return this.legalizacionContratoViewRemote.sumPorcentajePypRiaCapita(negociacionId);
    }

    public Double sumPorcentajeRecuperacionRiaCapita (final Long negociacionId){
    	return this.legalizacionContratoViewRemote.sumPorcentajeRecuperacionRiaCapita(negociacionId);
    }
    public void actualizarNumeroContrato (final ContratoDto contrato){
    	this.legalizacionContratoTransaccionalRemote.actualizarNumeroContrato(contrato);
    }

    public void actualizarEstadoContrato(final LegalizacionContratoDto solicitud){
    	this.legalizacionContratoTransaccionalRemote.actualizarEstadoContrato(solicitud);
    }

    public void asignarFirmaVoBo (final LegalizacionContratoDto legalizacion){
    	this.legalizacionContratoTransaccionalRemote.asignarFirmaVoBo(legalizacion);
    }

    public Long conteoLegalizaciones(Long negociacionId) {
    	return this.legalizacionContratoViewRemote.conteoLegalizaciones(negociacionId);
    }

    public BigDecimal consultarValorTotalNegociacion(Long negociacionId){
    	return this.legalizacionContratoViewRemote.consultaValorTotal(negociacionId);
    }

    public BigDecimal consultarValorRia(Long negociacionId){
    	return this.legalizacionContratoViewRemote.consultarValorTotalRia(negociacionId);
    }

    public SecuenciaContratoDto secuenciaPrestador(final ContratoDto contrato){
    	return this.legalizacionContratoViewRemote.secuenciaPrestador(contrato);
    }

    public LegalizacionContratoDto buscarLegalizacionByNroNegociacionAndRegimenAndModalidad(Long numeroNegociacion, RegimenNegociacionEnum regimenNegociacionEnum, NegociacionModalidadEnum negociacionModalidadEnum) throws ConexiaBusinessException {
        return this.legalizacionContratoViewRemote.buscarLegalizacionByNroNegociacionAndRegimenAndModalidad(numeroNegociacion, regimenNegociacionEnum, negociacionModalidadEnum);
    }

    public List<ProcedimientoDto> buscarProcedimientosNoCompletadosPorcontrato(LegalizacionContratoDto contrato) {
        return this.legalizacionContratoViewRemote.buscarProcedimientosNoCompletadosPorcontrato(contrato);
    }

    public List<Medicamento> buscarMedicamentosNoCompletadosPorcontrato(LegalizacionContratoDto contrato) {
        return this.legalizacionContratoViewRemote.buscarMedicamentosNoCompletadosPorcontrato(contrato);
    }

    public List<PaquetePortafolio> buscarPaquetesSinContenidoPorcontrato(LegalizacionContratoDto contrato) {
        return this.legalizacionContratoViewRemote.buscarPaquetesSinContenidoPorcontrato(contrato);
    }

    public List<PaquetePortafolio> buscarPaquetesNoCompletadosPorcontrato(LegalizacionContratoDto contrato) {
        return this.legalizacionContratoViewRemote.buscarPaquetesNoCompletadosPorcontrato(contrato);
    }

    public List<PaquetePortafolio> buscarPaquetesSinCodEmsanarPorcontrato(LegalizacionContratoDto contrato) {
        return this.legalizacionContratoViewRemote.buscarPaquetesSinCodEmsanarPorcontrato(contrato);
    }

    public List<ProcedimientoDto> buscarProcedimientosSinValorContratoRia(LegalizacionContratoDto contrato){
    	return this.legalizacionContratoViewRemote.buscarProcedimientosSinValorContratoRia(contrato);
    }

    public List<Medicamento> buscarMedicamentosSinValorContratoRia(LegalizacionContratoDto contrato){
    	return this.legalizacionContratoViewRemote.buscarMedicamentosSinValorContratoRia(contrato);
    }

    /**
     * Guarda el historial del cambio realizado sobre el contrato
     * @param userId usuario que realiza la modificacion
     * @param contratoId id del contrato modificado
     * @param evento evento realizado sobre el contrato (CREAR, MODIFICAR)
     */
    public void guardarHistorialContrato(Integer userId, Long contratoId, String evento, Long negociacionId) {
        this.legalizacionContratoTransaccionalRemote.guardarHistorialContrato(userId, contratoId, evento, negociacionId);
    }

    /**
     * Para cambiar la vigencia de las tecnologías contratadas por un cambio en las fechas del contrato
     * @param negociacionId
     * @throws ConexiaBusinessException
     */
    public void cambiarVigenciaTecnologiasContratadas(Long negociacionId, Integer userId)  throws ConexiaBusinessException {
    	this.legalizacionContratoTransaccionalRemote.cambiarVigenciaTecnologiasContratadas(negociacionId, userId);
    }

    public Boolean validarCambioFechaContrato(Long negociacionId) {
    	return this.legalizacionContratoViewRemote.validarCambioFechaContrato(negociacionId);
    }

    /**
     * Para cambiar la fecha de los contratos asociados a una negociación
     * @param negociacionId
     * @param fechaInicio
     * @param fechaFin
     * @throws ConexiaBusinessException
     */
    public void cambiarFechaContratoByNegociacionId(Long negociacionId, Date fechaInicio, Date fechaFin) throws ConexiaBusinessException {
    	this.legalizacionContratoTransaccionalRemote.cambiarFechaContratoByNegociacionId(negociacionId, fechaInicio, fechaFin);
    }
    
    public List<AreaCoberturaDTO> listarAreaCobertura(final ContratoDto contrato) {
        return this.legalizacionContratoViewRemote.listarAreaCobertura(contrato);
    }
    
    public Integer getPoblacionContratoMunicipPgp(String numeroContrato,
            Integer municipioId) {
        return this.legalizacionContratoViewRemote.
                getPoblacionContratoMunicip(numeroContrato, municipioId);
    }

    public List<LegalizacionContratoDto> buscarContratoPermanente(Long idPrestador, RegimenNegociacionEnum regimenNegociacionEnum, NegociacionModalidadEnum negociacionModalidadEnum, SolicitudContratacionParametrizableDto sol) {
        return this.legalizacionContratoViewRemote.consultarContratoPermanente(idPrestador, regimenNegociacionEnum, negociacionModalidadEnum,sol);
    }

    /**
     * Consulta las cláusulas y parágrafos de una minuta
     * @param minutaId
     * @return
     */
    public List<MinutaDetalleDto> consultarClausulasyParagrafos(Long minutaId) {
        return this.legalizacionContratoViewRemote.consultarClausulasYparagrafos(minutaId);
    }

    public List<MinutaDetalleDto> consultarClausulasParagrafosEditados(Long legalizacionContratoId) {
        return this.legalizacionContratoViewRemote.consultarClausulasParagrafosEditados(legalizacionContratoId);
    }

    public List<PaquetePortafolio> buscarPaquetesSinContenidoPorNegociacion(LegalizacionContratoDto contrato) {
        return this.legalizacionContratoViewRemote.buscarPaquetesSinContenidoPorNegociacion(contrato);
    }

    public List<PaquetePortafolio> buscarPaquetesNoCompletadosPorNegociacion(LegalizacionContratoDto contrato) {
        return this.legalizacionContratoViewRemote.buscarPaquetesNoCompletadosPorNegociacion(contrato);
    }

    public MinutaDetalleDto guardarClausulaParagrafoOtroSi(MinutaDetalleDto edicion) {
        return this.legalizacionContratoTransaccionalRemote.guardarClausulaParagrafoOtroSi(edicion);
    }

    public MinutaDetalleDto editarClausulaParagrafoOtroSi(MinutaDetalleDto edicion) {
        return this.legalizacionContratoTransaccionalRemote.editarClausulaParagrafoOtroSi(edicion);
    }

    public NegociacionDto consultarFechaOtroSi(Long negociacionId) throws ConexiaBusinessException {
        return this.legalizacionContratoTransaccionalRemote.consultarFechaOtroSi(negociacionId);
    }

    public void eliminarClausulaParagrafoEditado(MinutaDetalleDto minutaDetalle) {
        this.legalizacionContratoTransaccionalRemote.eliminarClausulaParagrafoEditado(minutaDetalle);
    }

    public void actualizarObservacionOtroSi(LegalizacionContratoDto legalizacionContratoDto) {
        this.legalizacionContratoTransaccionalRemote.actualizarObservacionOtroSi(legalizacionContratoDto);
    }

}
