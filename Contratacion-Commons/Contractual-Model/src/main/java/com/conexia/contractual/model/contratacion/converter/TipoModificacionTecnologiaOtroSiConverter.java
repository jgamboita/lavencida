package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.TipoModificacionTecnologiaOtroSiEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 * Class TipoModificacionTecnologiaOtroSiConverter
 * @author aquintero
 */
@Converter(autoApply = true)
public class TipoModificacionTecnologiaOtroSiConverter implements AttributeConverter<TipoModificacionTecnologiaOtroSiEnum, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TipoModificacionTecnologiaOtroSiEnum tipoEnum) {
        if(tipoEnum == null)
            return null;
        return tipoEnum.getId();
    }

    @Override
    public TipoModificacionTecnologiaOtroSiEnum convertToEntityAttribute(Integer tipoEnumId) {
        for(TipoModificacionTecnologiaOtroSiEnum tipo: TipoModificacionTecnologiaOtroSiEnum.values()) {
            if(Objects.equals(tipo.getId(), tipoEnumId))
                return tipo;
        }
        return null;
    }

}
