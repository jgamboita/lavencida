package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.TipoReglaEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 *
 * @author clozano
 *
 */

@Converter(autoApply = true)
public class TipoReglaConverter implements AttributeConverter<TipoReglaEnum, Integer> {

	@Override
	public Integer convertToDatabaseColumn(TipoReglaEnum tipoRegla) {
		if (tipoRegla == null)
			return null;

			return tipoRegla.getId();
	}

	@Override
	public TipoReglaEnum convertToEntityAttribute(Integer tipoReglaId) {
		for (TipoReglaEnum tipoRegla : TipoReglaEnum.values()) {
            if(Objects.equals(tipoRegla.getId(), tipoReglaId))
            	return tipoRegla;
        }

		return null;
	}

}
