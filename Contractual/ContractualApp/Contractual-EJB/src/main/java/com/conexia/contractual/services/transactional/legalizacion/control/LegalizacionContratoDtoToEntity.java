package com.conexia.contractual.services.transactional.legalizacion.control;

import com.conexia.contractual.model.contratacion.legalizacion.ResponsableContrato;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ResponsableContratoDto;

/**
 * Clase que permite convertir dtos to entities.
 * @author jalvarado
 */
public class LegalizacionContratoDtoToEntity {
    
    public ResponsableContrato responsableContratoDtoToEntity(final ResponsableContratoDto responsableContratoDto) {
        return new ResponsableContrato(responsableContratoDto.getId());
    }
            
    
    
}
