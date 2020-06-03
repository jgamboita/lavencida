package com.conexia.contractual.model.contratacion.converter;


import com.conexia.contratacion.commons.constants.enums.TipoValorEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 *
 * @author Andrés Mise Olivera
 */
@Converter(autoApply = true)
public class TipoValorConverter implements AttributeConverter<TipoValorEnum, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(TipoValorEnum tipoValorEnum) {        
        return tipoValorEnum.getId();
    }

    @Override
    public TipoValorEnum convertToEntityAttribute(Integer integer) {
        TipoValorEnum tipoValorEnum = null;
        for (TipoValorEnum tv : TipoValorEnum.values()) {
            if(Objects.equals(tv.getId(), integer)) {
                tipoValorEnum = tv;
            }
        }
        return tipoValorEnum;
    }
    
}
