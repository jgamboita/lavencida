package com.conexia.contractual.services.transactional.legalizacion.control;

import com.conexia.contractual.model.contratacion.legalizacion.ContenidoMinuta;
import com.conexia.contractual.model.contratacion.legalizacion.Minuta;
import com.conexia.contractual.model.contratacion.legalizacion.MinutaDetalle;
import com.conexia.contractual.model.contratacion.legalizacion.Variable;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContenidoMinutaDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.VariableDto;
import java.util.Date;

/**
 * Control que permite realizar las respectivas validaciones de la creacion de la solicitud de autorizacion.
 * @author jalvarado
 */
public class ParametrizacionMinutaDtoToEntityControl {
    
    public Minuta minutaDtoToEntity(final MinutaDto minuta) {
        Minuta m = new Minuta();
        m.setId(minuta.getId());
        m.setDescripcion(minuta.getDescripcion());
        m.setEstado(minuta.getEstado());
        m.setModalidad(minuta.getModalidad());
        m.setNombre(minuta.getNombre());
        m.setTramite(minuta.getTramite());
        for (VariableDto dto : minuta.getVariables()) {
            Variable v = new Variable(dto.getId().longValue(), dto.getCodigo(), dto.getDescripcion());
            v.setTipoVariable(dto.getTipoVariable());
            v.getMinutas().add(m);
            m.getVariables().add(v);            
        }
        return m;
    }
    
    public MinutaDto minutaToDto(final Minuta minuta) {
        MinutaDto m = new MinutaDto();
        m.setId(minuta.getId());
        m.setDescripcion(minuta.getDescripcion());
        m.setEstado(minuta.getEstado());
        m.setModalidad(minuta.getModalidad());
        m.setNombre(minuta.getNombre());
        m.setTramite(minuta.getTramite());
        return m;
    }
    
    /**
     * Convierte un dto a entity para guardar.
     * 
     * @param minutaDetalleDto
     * @return entity minuta detalle.
     */
    public MinutaDetalle minutaDetalleDtoToEntity(final MinutaDetalleDto minutaDetalleDto) {
        MinutaDetalle minutaDet = new MinutaDetalle();
        minutaDet.setOrdinal(minutaDetalleDto.getOrdinal());
        minutaDet.setPadreId(minutaDetalleDto.getPadreId());
        minutaDet.setTitulo(minutaDetalleDto.getTitulo());
        minutaDet.setUserId(minutaDetalleDto.getUserId());
        ContenidoMinuta contenidoMinuta = new ContenidoMinuta();
        contenidoMinuta.setId(minutaDetalleDto.getContenidoMinuta().getId());
        contenidoMinuta.setIcono(minutaDetalleDto.getContenidoMinuta().getIcono());
        Minuta minuta = new Minuta();
        minuta.setId(minutaDetalleDto.getMinutaId().getId());
        minutaDet.setContenidoMinutaId(contenidoMinuta);
        minutaDet.setMinutaId(minuta);
        minutaDet.setDescripcion(minutaDetalleDto.getDescripcion() == null 
                ? " " : minutaDetalleDto.getDescripcion());
        minutaDet.setFechaRegistro(new Date());
        return minutaDet;
    }
    
    /**
     * Retorna el dto del entity almacenado.
     * 
     * @param min minuta detalle guardada.
     * @return minuta dto.
     */
    public MinutaDetalleDto minutaDetalleEntityToDto(MinutaDetalle min) {
        MinutaDetalleDto dto = new MinutaDetalleDto();
        dto.setId(min.getId());
        ContenidoMinutaDto contenidoMinutaDto = new ContenidoMinutaDto();
        contenidoMinutaDto.setIcono(min.getContenidoMinutaId().getIcono());
        contenidoMinutaDto.setIconoAbierto(min.getContenidoMinutaId().getIcono());
        dto.setContenidoMinuta(contenidoMinutaDto);
        dto.setOrdinal(min.getOrdinal());
        MinutaDto minuta = new MinutaDto();
        minuta.setId(min.getMinutaId().getId());
        dto.setMinutaId(minuta);
        dto.setTitulo(min.getTitulo());
        dto.setPadreId(min.getPadreId());        
        return dto;
    }
    
}
