package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.GeneroEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;


/**
 *
 * @author clozano
 *
 */

@Converter(autoApply = true)
public class GeneroConverter implements AttributeConverter<GeneroEnum, Integer> {

	@Override
	public Integer convertToDatabaseColumn(GeneroEnum generoEnum) {
		if(generoEnum == null)
			return null;
		return generoEnum.getId();
	}

	@Override
	public GeneroEnum convertToEntityAttribute(Integer generoId) {
		for (GeneroEnum genero : GeneroEnum.values()) {
            if(Objects.equals(genero.getId(), generoId))
            	return genero;
        }

		return null;
	}

}
