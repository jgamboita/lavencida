package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.EstadoMaestroEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.Objects;

/**
 * @author Andrés Mise Olivera
 */
@Converter(autoApply = true)
public class EstadoMaestroConverter implements AttributeConverter<EstadoMaestroEnum, Integer> {

    @Override
    public Integer convertToDatabaseColumn(EstadoMaestroEnum estado) {
        return estado.getId();
    }

    @Override
    public EstadoMaestroEnum convertToEntityAttribute(Integer id) {
        return Arrays.stream(EstadoMaestroEnum.values())
                .filter(estadoMaestroEnum -> Objects.equals(estadoMaestroEnum.getId(), id))
                .findFirst()
                .orElse(null);
    }
}
