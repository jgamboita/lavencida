/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.TipoOtroSiEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 *
 * @author aquintero
 */
@Converter(autoApply = true)
public class TipoOtroSiConverter implements AttributeConverter<TipoOtroSiEnum, Integer> {

    @Override
    public Integer convertToDatabaseColumn(TipoOtroSiEnum tipoEnum) {
        if(tipoEnum == null)
            return null;
        return tipoEnum.getId();
    }

    @Override
    public TipoOtroSiEnum convertToEntityAttribute(Integer tipoEnumId) {
        for(TipoOtroSiEnum tipo: TipoOtroSiEnum.values()) {
            if(Objects.equals(tipo.getId(), tipoEnumId))
                return tipo;
        }
        return null;
    }
    
}
