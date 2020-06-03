package com.conexia.contractual.definitions.view.legalizacion;

import com.conexia.contratacion.commons.constants.enums.EstadoEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TipoVariableEnum;
import com.conexia.contratacion.commons.constants.enums.TramiteEnum;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContenidoMinutaDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.VariableDto;
import com.conexia.contratacion.commons.dto.negociacion.ReglaNegociacionPgpDTO;
import java.util.List;

/**
 * Interface para la clase de parametrizacion de minuta.
 *
 * @author jLopez
 */
public interface ParametrizacionMinutaViewRemote {
    
    /**
     * Retorna todas las variables
     * @param modalidadNegociacion modalidad para buscar las variables
     * @return lista de variables
     */
    List<VariableDto> listarVariables(Integer modalidadNegociacion);
    
    /**
     * Retorna las variables que se le pueden aplicara una Minuta
     * @param tipoVariableId
     * @param modalidadNegociacion
     * @param estado
     * @return lista de variables
     */
    List<VariableDto> listarVariablesPorModalidad(TipoVariableEnum tipoVariableId,Integer modalidadNegociacion,Integer estado );
    
    /**
     * 
     * @param minutaId
     * @return 
     */
    String generarVistaPrevia(Long minutaId);

    /**
     * Genera el contenido para el acta de otro si
     * @param legalizacionContratoId
     * @param minutaOtroSiId
     * @return
     */
    String generarVistaPreviaActaOtroSi(Long legalizacionContratoId, Long minutaOtroSiId);
    
    /**
     * Retorna el detalle de la minuta.
     * 
     * @param idMinuta
     * @return detalleMinuta.
     */
    List<MinutaDetalleDto> obtieneDetalleMinuta(final Long idMinuta);
    
    /**
     * Retorna los contenidos de la minuta mayores al nivel que se le envia.
     * 
     * @param nivel nivel.
     * @return lista de contenidos.
     */
    List<ContenidoMinutaDto> obtieneContenidosMinutaPorNivel(final int nivel);
    
    /**
     * 
     * @param minutaId
     * @return 
     */
    MinutaDto obtenerMinuta(Long minutaId);
    
    /**
     * 
     * @param padreId
     * @param ordinal
     * @return 
     */
    MinutaDetalleDto obtenerMinutaDetallePorMinutaYOrdinal(
            Long padreId, Integer ordinal);
    
    /**
     * 
     * @param padreId
     * @param ordinal
     * @return 
     */
    MinutaDetalleDto obtenerMinutaDetallePorPadreYOrdinal(
            Long padreId, Integer ordinal);
            
    /**
     * Listar las minutas existentes en la BD.
     * @return List<MinutaDto>
     */
    List<MinutaDto> listarMinutas();

    List<MinutaDto> listarMinutas(EstadoEnum estado);
    List<MinutaDto> listarMinutas(EstadoEnum estado, NegociacionModalidadEnum modalidad);
    List<MinutaDto> listarMinutas(EstadoEnum estado,
                                  NegociacionModalidadEnum modalidad,
                                  TramiteEnum tramite);
    
    /**
     * Valida la exitencia del nombre de la minuta.
     * @param minuta
     * @return 
     */
    Long validaExistenciaNombreMinutaDetalle(final MinutaDetalleDto minuta);
    
    
    List<ReglaNegociacionPgpDTO> listarReglaNegociacionPGP(ContratoDto contrato);
}
