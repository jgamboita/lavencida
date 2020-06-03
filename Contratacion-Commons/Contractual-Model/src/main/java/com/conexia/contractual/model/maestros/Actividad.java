package com.conexia.contractual.model.maestros;

import javax.persistence.*;

/**
 *
 * @author dmora
 *
 */

@Entity
@Table(name = "actividad", schema = "maestros")
@NamedQueries(
        @NamedQuery(name = "Actividad.buscarPorDescripciones",
        query = "select new com.conexia.contratacion.commons.dto.maestros.ActividadDto(a.id, a.descripcion) from Actividad a where a.descripcion in (:descripcionActividad)")
)
@NamedNativeQueries({
	@NamedNativeQuery(name = "Actividad.findIdActividad",
			query = "SELECT COALESCE((SELECT DISTINCT a.id FROM maestros.actividad a WHERE a.descripcion = :descripcionActividad ),0)")
})
public class Actividad extends Descriptivo {

	public Actividad() {
		super();
	}

	public Actividad(int id) {
		super(id);
	}

}
