package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.TemasCapitaEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 * 
 * @author icruz
 *
 */
@Converter(autoApply = true)
public class TemasCapitaConverter implements AttributeConverter<TemasCapitaEnum, Integer> {

	@Override
	public Integer convertToDatabaseColumn(TemasCapitaEnum temasCapitaEnum) {
        if(temasCapitaEnum == null)
            return null;
        return temasCapitaEnum.getId();
	}

	@Override
	public TemasCapitaEnum convertToEntityAttribute(Integer temasCapitaId) {
		for (TemasCapitaEnum temasCapita : TemasCapitaEnum.values()) {
            if(Objects.equals(temasCapita.getId(), temasCapitaId)) 
            	return temasCapita;
        }

		return null;
	}

}
