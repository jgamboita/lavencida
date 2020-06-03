package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.TramiteEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 *
 * @author Andrés Mise Olivera
 */
@Converter(autoApply = true)
public class TramiteConverter implements AttributeConverter<TramiteEnum, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(TramiteEnum tramite) {        
        return tramite.getId();
    }

    @Override
    public TramiteEnum convertToEntityAttribute(Integer integer) {
        TramiteEnum tramite = null;
        for (TramiteEnum t : TramiteEnum.values()) {
            if(Objects.equals(t.getId(), integer)) {
                tramite = t;
            }
        }
        return tramite;
    }
    
}
