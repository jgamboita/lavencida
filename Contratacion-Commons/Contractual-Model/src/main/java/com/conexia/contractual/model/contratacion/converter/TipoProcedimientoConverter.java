package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.TipoProcedimientoEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Arrays;
import java.util.Objects;

/**
 *
 * @author Andrés Mise Olivera
 */
@Converter(autoApply = true)
public class TipoProcedimientoConverter implements AttributeConverter<TipoProcedimientoEnum, Long> {
    
    @Override
    public Long convertToDatabaseColumn(TipoProcedimientoEnum tramite) {
        return tramite.getId();
    }

    @Override
    public TipoProcedimientoEnum convertToEntityAttribute(Long id) {
        return Arrays.stream(TipoProcedimientoEnum.values())
                .filter(estadoMaestroEnum -> Objects.equals(estadoMaestroEnum.getId(), id))
                .findFirst()
                .orElse(null);
    }
}
