package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.EstadoEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 *
 * @author Andrés Mise Olivera
 */
@Converter(autoApply = true)
public class EstadoConverter implements AttributeConverter<EstadoEnum, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(EstadoEnum estado) {        
        return estado.getId();
    }

    @Override
    public EstadoEnum convertToEntityAttribute(Integer integer) {
        EstadoEnum estado = null;
        for (EstadoEnum e : EstadoEnum.values()) {
            if(Objects.equals(e.getId(), integer)) {
                estado = e;
            }
        }
        return estado;
    }
    
}
