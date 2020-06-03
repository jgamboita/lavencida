package com.conexia.contratacion.portafolio.wap.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Utilidad que permite convertir un valor booleano a su correspundiene en
 * Espaniol, al igual que las palabras "SI" y "NO" a su valor booleano
 * correspondiente
 * 
 * @author <a href = "dgarcia@conexia.com">David Garcia Hernandez</a>
 */
@FacesConverter("com.conexia.referencia.wap.converter.BooleanConverter")
public class BooleanConverter implements Converter {

	/**
	 * Reversa la conversion a partir de los textos si y no
	 */
	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {
		return value != null && value.matches("(?i)si") ? Boolean.TRUE : Boolean.FALSE;
	}

	/**
	 * Convierte un valor booleano a su correspondencia en Espaniol
	 */
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if(!(value instanceof Boolean)){
			throw new IllegalArgumentException("El valor debe ser booleano.");
		}
		return (value != null && (value instanceof Boolean)) ? 
				String.valueOf(((Boolean) value).equals(Boolean.TRUE) ? "Si": "No") :  "";
	}

}