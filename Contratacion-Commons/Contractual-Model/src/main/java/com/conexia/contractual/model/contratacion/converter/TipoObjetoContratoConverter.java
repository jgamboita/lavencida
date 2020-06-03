package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.TipoObjetoContratoEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 *
 * @author Johan Lopez
 */
@Converter(autoApply = true)
public class TipoObjetoContratoConverter implements AttributeConverter<TipoObjetoContratoEnum, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(TipoObjetoContratoEnum tipoObjetoContratoEnum) {        
        return tipoObjetoContratoEnum.getId();
    }

    @Override
    public TipoObjetoContratoEnum convertToEntityAttribute(Integer integer) {
        TipoObjetoContratoEnum tipoObjetoContratoEnum = null;
        for (TipoObjetoContratoEnum tv : TipoObjetoContratoEnum.values()) {
            if(Objects.equals(tv.getId(), integer)) {
                tipoObjetoContratoEnum = tv;
            }
        }
        return tipoObjetoContratoEnum;
    }
    
}
