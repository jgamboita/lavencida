package com.conexia.contratacion.portafolio.wap.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.conexia.contratacion.commons.constants.enums.EstadoServicioPortafolioEnum;

/**
 * Utilidad que permite convertir un valor booleano a su correspundiene en
 * Espaniol, al igual que las palabras "SI" y "NO" a su valor booleano
 * correspondiente
 * 
 * @author <a href = "dgarcia@conexia.com">David Garcia Hernandez</a>
 */
@FacesConverter("com.conexia.referencia.wap.converter.EstadoPortafolioConverter")
public class EstadoPortafolioConverter implements Converter {

	/**
	 * Reversa la conversion a partir de los textos si y no
	 */
	@Override
	public Object getAsObject(FacesContext context, UIComponent component,
			String value) {
		
		if(value == null){
			return null;
		}
		
		EstadoServicioPortafolioEnum estadoPortafolio = EstadoServicioPortafolioEnum.valueOf(value);
		
		return estadoPortafolio
					.equals(EstadoServicioPortafolioEnum.CARGADO_MINISTERIO) 
					? 1 : 2;
	}

	/**
	 * Convierte un valor booleano a su correspondencia en Espaniol
	 */
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		if(!(value instanceof Number)){
			throw new IllegalArgumentException("El valor debe ser numerico.");
		}
				
		
		return (value == null) ? "" 
				 : (value.equals(1) 
						 ? EstadoServicioPortafolioEnum.CARGADO_MINISTERIO.getDescription()
						 : value.equals(2) 
						 	? EstadoServicioPortafolioEnum.PENDIENTE_APROBACION.getDescription() 
						 	: "");
	}

}