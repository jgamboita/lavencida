package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.ModalidadEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 *
 * @author Andrés Mise Olivera
 */
@Converter(autoApply = true)
public class ModalidadConverter implements AttributeConverter<ModalidadEnum, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(ModalidadEnum modalidad) {        
        return modalidad.getId();
    }

    @Override
    public ModalidadEnum convertToEntityAttribute(Integer integer) {
        ModalidadEnum modalidad = null;
        for (ModalidadEnum m : ModalidadEnum.values()) {
            if(Objects.equals(m.getId(), integer)) {
                modalidad = m;
            }
        }
        return modalidad;
    }
    
}
