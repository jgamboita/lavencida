package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 *
 * @author Andrés Mise Olivera
 */
@Converter(autoApply = true)
public class NegociacionModalidadConverter implements AttributeConverter<NegociacionModalidadEnum, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(NegociacionModalidadEnum modalidad) {
        return modalidad.getId();
    }

    @Override
    public NegociacionModalidadEnum convertToEntityAttribute(Integer integer) {
        NegociacionModalidadEnum modalidad = null;
        for (NegociacionModalidadEnum m : NegociacionModalidadEnum.values()) {
            if(Objects.equals(m.getId(), integer)) {
                modalidad = m;
            }
        }
        return modalidad;
    }
    
}
