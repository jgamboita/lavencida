package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.TipoCondicionEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 *
 * @author Andrés Mise Olivera
 */
@Converter(autoApply = true)
public class TipoCondicionConverter implements AttributeConverter<TipoCondicionEnum, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(TipoCondicionEnum tipoValorEnum) {        
        return tipoValorEnum.getId();
    }

    @Override
    public TipoCondicionEnum convertToEntityAttribute(Integer integer) {
        TipoCondicionEnum tipoCondicionEnum = null;
        for (TipoCondicionEnum tv : TipoCondicionEnum.values()) {
            if(Objects.equals(tv.getId(), integer)) {
                tipoCondicionEnum = tv;
            }
        }
        return tipoCondicionEnum;
    }
    
}
