package com.conexia.contractual.wap.facade.legalizacion;

import com.conexia.contractual.definitions.transactional.legalizacion.ParametrizacionMinutaTransaccionalRemote;
import com.conexia.contractual.definitions.transactional.parametrizacion.ParametrizacionContratoTransactionalRemote;
import com.conexia.contractual.definitions.view.legalizacion.ParametrizacionMinutaViewRemote;
import com.conexia.contratacion.commons.constants.enums.EstadoEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TipoVariableEnum;
import com.conexia.contratacion.commons.constants.enums.TramiteEnum;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContenidoMinutaDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.VariableDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.negociacion.ReglaNegociacionPgpDTO;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.servicefactory.CnxService;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;

/**
 * Facade de las tablas de parametrizacion de minuta.
 *
 * @author jalvarado
 */
public class ParametrizacionMinutaFacade implements Serializable {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = -5789833792263400766L;

    /**
     * Service view de consulta de parametrizacion de minuta.
     */
    @Inject
    @CnxService
    private ParametrizacionMinutaViewRemote parametrizacionMinutaViewService;

    /**
     * Service transaccional de parametrizacion de minuta.
     */
    @Inject
    @CnxService
    private ParametrizacionMinutaTransaccionalRemote parametrizacionMinutaTransaccionalService;

    /**
     * Service transaccional de parametrizacion de contratos.
     */
    @Inject
    @CnxService
    private ParametrizacionContratoTransactionalRemote parametrizacionContratoTransactionalService;

    public int actualizarMinuta(MinutaDto minuta) throws ConexiaBusinessException {
        return this.parametrizacionMinutaTransaccionalService.actualizarMinuta(minuta);
    }

    public Integer actualizarMinutaDetalle(MinutaDetalleDto minuta) {
        return this.parametrizacionMinutaTransaccionalService.actualizarMinutaDetalle(minuta);
    }

    public Integer actualizarMinutaDetalleTitulo(MinutaDetalleDto minuta) {
        return this.parametrizacionMinutaTransaccionalService.actualizarMinutaDetalleTitulo(minuta);
    }

    public MinutaDto duplicarMinuta(MinutaDto minuta) {
        return this.parametrizacionMinutaTransaccionalService.duplicarMinuta(minuta);
    }

    public String generarVistaPrevia(Long minutaId) {
        return this.parametrizacionMinutaViewService.generarVistaPrevia(minutaId);
    }

    public String generarVistaPreviaActaOtroSi(Long legalizacionContratoId, Long minutaOtroSiId) {
        return this.parametrizacionMinutaViewService.generarVistaPreviaActaOtroSi(legalizacionContratoId, minutaOtroSiId);
    }

    /**
     * Retorna todas las variables
     * @param modalidadNegociacion
     * @return lista de variables
     */
    public List<VariableDto> listarVariables(Integer modalidadNegociacion) {
        return this.parametrizacionMinutaViewService.listarVariables(modalidadNegociacion);
    }

    /**
     * Retorna las variables que se le pueden aplicara una Minuta
     * @param estado
     * @param tipoVariableId
     * @param modalidadNegociacion
     * @return lista de variables
     */
    public List<VariableDto> listarVariablesPorModalidad(TipoVariableEnum tipoVariableId, Integer modalidadNegociacion,Integer estado) {
        return this.parametrizacionMinutaViewService.listarVariablesPorModalidad(tipoVariableId,modalidadNegociacion,estado);
    }

    public void asociarVariables (Integer estado,Long variableId, Integer modalidadNegociacionId) throws ConexiaBusinessException{
        this.parametrizacionMinutaTransaccionalService.asociarVariable(estado,variableId, modalidadNegociacionId);
    }

    public MinutaDto mergeMinuta(MinutaDto minuta) {
        return parametrizacionMinutaTransaccionalService.mergeMinuta(minuta);
    }


    /**
     * Obtiene el detalle de la minuta.
     *
     * @param minutaId
     * @return lista de detalles de la minuta.
     */
    public List<MinutaDetalleDto> obtieneDetalleMinuta(final Long minutaId) {
        return this.parametrizacionMinutaViewService.obtieneDetalleMinuta(minutaId);
    }

    public MinutaDto obtenerMinuta(Long minutaId) {
        return this.parametrizacionMinutaViewService.obtenerMinuta(minutaId);
    }

    public MinutaDetalleDto obtenerMinutaDetallePorMinutaYOrdinal(
            Long minutaId, Integer ordinal) {
        return this.parametrizacionMinutaViewService.obtenerMinutaDetallePorMinutaYOrdinal(minutaId, ordinal);
    }

    public MinutaDetalleDto obtenerMinutaDetallePorPadreYOrdinal(
            Long padreId, Integer ordinal) {
        return this.parametrizacionMinutaViewService.obtenerMinutaDetallePorPadreYOrdinal(padreId, ordinal);
    }

    /**
     * Listar las minutas existentes en la BD.
     * @return List<MinutaDto>
     */
    public List<MinutaDto> listarMinutas() {
        return this.parametrizacionMinutaViewService.listarMinutas();
    }

    public List<MinutaDto> listarMinutas(EstadoEnum estado) {
        return this.parametrizacionMinutaViewService.listarMinutas(estado);
    }

    public List<MinutaDto> listarMinutas(EstadoEnum estado, NegociacionModalidadEnum modalidad) {
        return this.parametrizacionMinutaViewService.listarMinutas(estado, modalidad);
    }

    public List<MinutaDto> listarMinutas(EstadoEnum estado, NegociacionModalidadEnum modalidad, TramiteEnum tramiteEnum)
    {
        return this.parametrizacionMinutaViewService.listarMinutas(estado, modalidad, tramiteEnum);
    }

    /**
     * Retorna los contenidos de la minuta mayores al nivel que se le envia.
     *
     * @param nivel nivel.
     * @return lista de contenidos.
     */
    public List<ContenidoMinutaDto> obtieneContenidosMinutaPorNivel(final int nivel) {
        return this.parametrizacionMinutaViewService.obtieneContenidosMinutaPorNivel(nivel);
    }

    /**
     * Crea una minuta detalle.
     * @param minutaDto
     * @return minutadto crada.
     * @throws com.conexia.exceptions.ConexiaBusinessException
     */
    public MinutaDto guardarMinuta(MinutaDto minutaDto) throws ConexiaBusinessException {
        return this.parametrizacionMinutaTransaccionalService.guardarMinuta(minutaDto);
    }

    /**
     * Crea una minuta detalle.
     * @param minutaDetalleDto
     * @return minutadto crada.
     */
    public MinutaDetalleDto guardaMinutaDetalle(MinutaDetalleDto minutaDetalleDto) {
        MinutaDetalleDto minutaCreada = this.parametrizacionMinutaTransaccionalService.guardaMinutaDetalle(minutaDetalleDto);
        return minutaCreada;
    }

     /**
     * Id del minuta detalle.
     * @param minutaDetalleDto - Detalle de la minuta que se va actualizar.
     * @throws com.conexia.exceptions.ConexiaBusinessException
     */
    public void actualizarOrdinalMinutaDetalle(final MinutaDetalleDto minutaDetalleDto) throws ConexiaBusinessException {
        this.parametrizacionMinutaTransaccionalService.actualizarOrdinalMinutaDetalle(minutaDetalleDto);
    }

    /**
     * Elimina el detalle de la minuta.
     * @param minutaDetalle
     * @throws ConexiaBusinessException
     */
    public void eliminarMinutaDetalle(final MinutaDetalleDto minutaDetalle) throws ConexiaBusinessException{
        this.parametrizacionMinutaTransaccionalService.eliminarOrdinalMinutaDetalle(minutaDetalle);
    }

    public Long validaExistenciaNombreMinutaDetalle(final MinutaDetalleDto minutaDetalleDto) {
        return this.parametrizacionMinutaViewService.validaExistenciaNombreMinutaDetalle(minutaDetalleDto);
    }

    /**
     * Realiza la miggracion de Contratatos a Autorizaciones
     * @param dto
     * @param idUsuario
     * @param sedeNegociacionId
     * @param sedesNegociadas
     * @return
     */
    public boolean crearSolicitudContratacion(final SolicitudContratacionParametrizableDto dto,
            final Integer idUsuario, final Long sedeNegociacionId, final Integer sedesNegociadas) {
        return this.parametrizacionContratoTransactionalService.crearSolicitudContratacion(dto, idUsuario, sedeNegociacionId, sedesNegociadas);
    }

    /**
     * Guarda el historial del cambio realizado sobre la minuta
     * @param userId usuario que realiza la modificacion
     * @param minutaId id de la minuta modificada
     * @param evento evento realizado sobre la minuta (CREAR, MODIFICAR)
     */
    public void guardarHistorialMinuta(Integer userId, Long minutaId, String evento) throws ConexiaBusinessException {
        this.parametrizacionMinutaTransaccionalService.guardarHistorialMinuta(userId, minutaId, evento);;
    }
    
    public List<ReglaNegociacionPgpDTO> listarReglaNegociacionPGP(ContratoDto contrato) {
        return this.parametrizacionMinutaViewService.listarReglaNegociacionPGP(contrato);
    }

}
