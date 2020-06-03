package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.TipoDocumentoEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 *
 * @author Andrés Mise Olivera
 */
@Converter(autoApply = true)
public class TipoDocumentoConverter implements AttributeConverter<TipoDocumentoEnum, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(TipoDocumentoEnum tipo) {        
        return tipo.getId();
    }

    @Override
    public TipoDocumentoEnum convertToEntityAttribute(Integer integer) {
        TipoDocumentoEnum tipo = null;
        for (TipoDocumentoEnum t : TipoDocumentoEnum.values()) {
            if(Objects.equals(t.getId(), integer)) {
                tipo = t;
            }
        }
        return tipo;
    }
    
}
