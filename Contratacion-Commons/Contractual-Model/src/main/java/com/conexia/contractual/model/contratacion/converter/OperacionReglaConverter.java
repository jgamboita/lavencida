package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.OperacionReglaEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 *
 * @author clozano
 *
 */

@Converter(autoApply = true)
public class OperacionReglaConverter implements AttributeConverter<OperacionReglaEnum, Integer> {

	@Override
	public Integer convertToDatabaseColumn(OperacionReglaEnum operacionReglaEnum) {
		if (operacionReglaEnum == null)
		return null;

		return operacionReglaEnum.getId();
	}

	@Override
	public OperacionReglaEnum convertToEntityAttribute(Integer operacionReglaId) {
		for (OperacionReglaEnum operacionRegla : OperacionReglaEnum.values()) {
            if(Objects.equals(operacionRegla.getId(), operacionReglaId))
            	return operacionRegla;
        }

		return null;
	}

}
