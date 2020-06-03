package com.conexia.contractual.model.contratacion.converter;

import com.conexia.contratacion.commons.constants.enums.MacroCategoriaMedicamentoEnum;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.util.Objects;

/**
 *
 * @author Andrés Mise Olivera
 */
@Converter(autoApply = true)
public class MacroCategoriaMedicamentoConverter implements AttributeConverter<MacroCategoriaMedicamentoEnum, Integer> {
    
    @Override
    public Integer convertToDatabaseColumn(MacroCategoriaMedicamentoEnum categoria) {        
        return categoria.getId();
    }

    @Override
    public MacroCategoriaMedicamentoEnum convertToEntityAttribute(Integer integer) {
        MacroCategoriaMedicamentoEnum categoria = null;
        for (MacroCategoriaMedicamentoEnum m : MacroCategoriaMedicamentoEnum.values()) {
            if(Objects.equals(m.getId(), integer)) {
                categoria = m;
            }
        }
        return categoria;
    }
    
}
