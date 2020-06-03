package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.TipoDescuentoEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 *
 * @author Andrés Mise Olivera
 */
@Converter(autoApply = true)
public class TipoDescuentoConverter implements AttributeConverter<TipoDescuentoEnum, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(TipoDescuentoEnum tipoDescuentoEnum) {        
        return tipoDescuentoEnum.getId();
    }

    @Override
    public TipoDescuentoEnum convertToEntityAttribute(Integer integer) {
        TipoDescuentoEnum tipoDescuentoEnum = null;
        for (TipoDescuentoEnum tv : TipoDescuentoEnum.values()) {
            if(Objects.equals(tv.getId(), integer)) {
                tipoDescuentoEnum = tv;
            }
        }
        return tipoDescuentoEnum;
    }
    
}
