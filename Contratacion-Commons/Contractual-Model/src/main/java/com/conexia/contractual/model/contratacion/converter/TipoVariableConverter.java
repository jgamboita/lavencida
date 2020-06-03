package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.TipoVariableEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 *
 * @author Andrés Mise Olivera
 */
@Converter(autoApply = true)
public class TipoVariableConverter implements AttributeConverter<TipoVariableEnum, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(TipoVariableEnum tipoVariableEnum) {        
        return tipoVariableEnum.getId();
    }

    @Override
    public TipoVariableEnum convertToEntityAttribute(Integer integer) {
        TipoVariableEnum tipoVariableEnum = null;
        for (TipoVariableEnum tv : TipoVariableEnum.values()) {
            if(Objects.equals(tv.getId(), integer)) {
                tipoVariableEnum = tv;
            }
        }
        return tipoVariableEnum;
    }
    
}
