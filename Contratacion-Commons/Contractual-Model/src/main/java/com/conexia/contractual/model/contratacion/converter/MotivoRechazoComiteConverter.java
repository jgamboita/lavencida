package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.MotivoRechazoComiteEnum;

import javax.persistence.AttributeConverter;
import java.util.Objects;

/**
 *
 * @author Andrés Mise Olivera
 */
public class MotivoRechazoComiteConverter implements AttributeConverter<MotivoRechazoComiteEnum, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(MotivoRechazoComiteEnum motivo) {        
        return motivo.getId();
    }

    @Override
    public MotivoRechazoComiteEnum convertToEntityAttribute(Integer integer) {
        MotivoRechazoComiteEnum motivo = null;
        for (MotivoRechazoComiteEnum m : MotivoRechazoComiteEnum.values()) {
            if(Objects.equals(m.getId(), integer)) {
                motivo = m;
            }
        }
        return motivo;
    }
    
}
