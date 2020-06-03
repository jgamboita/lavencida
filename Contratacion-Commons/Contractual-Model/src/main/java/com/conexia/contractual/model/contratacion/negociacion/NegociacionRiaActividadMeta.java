package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.maestros.Actividad;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(schema = "contratacion", name = "negociacion_ria_actividad_meta")
@NamedQueries({
	@NamedQuery(name = "NegociacionRiaActividadMeta.findByRiaRangoPoblacion",
			query = "SELECT new com.conexia.contratacion.commons.dto.negociacion.NegociacionRiaActividadMetaDto(nram.id, nram.actividad.id, nram.actividad.descripcion, nram.meta) "
					+ "FROM NegociacionRiaActividadMeta nram "
					+ "WHERE nram.negociacionRiaRangoPoblacion.id = :idNegociacionRiaRangoPoblacion")
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "NegociacionRiaActividadMeta.borrarPorNoNegociacionIds",
			query = "DELETE FROM contratacion.negociacion_ria_actividad_meta " + 
					"	USING contratacion.negociacion_ria_rango_poblacion nrrp, " + 
					"	contratacion.negociacion_ria nr " + 
					"	WHERE nrrp.id = negociacion_ria_rango_poblacion_id " + 
					"	AND nr.id = nrrp.negociacion_ria_id " + 
					"	AND nr.negociacion_id = :negociacionId " + 
					"	AND nr.id NOT IN (:ids)"),
	@NamedNativeQuery(name = "NegociacionRiaActividadMeta.borrarPorNegociacionId",
			query = "DELETE FROM contratacion.negociacion_ria_actividad_meta " + 
					"	USING contratacion.negociacion_ria_rango_poblacion nrrp, " + 
					"	contratacion.negociacion_ria nr " + 
					"	WHERE nrrp.id = negociacion_ria_rango_poblacion_id " + 
					"	AND nr.id = nrrp.negociacion_ria_id " + 
					"	AND nr.negociacion_id = :negociacionId ")
	
})
public class NegociacionRiaActividadMeta implements Identifiable<Integer>, Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "negociacion_ria_rango_poblacion_id")
	private NegociacionRiaRangoPoblacion negociacionRiaRangoPoblacion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "actividad_id")
	private Actividad actividad;
	
	@Column(name = "meta")
	private Integer meta;

	
	/**
	 * Devuelve el valor de id.
	 *
	 * @return El valor de id.
	 */
	public Integer getId() {
	
		return id;
	}

	
	/**
	 * Asigna un nuevo valor a id.
	 *
	 * @param id El valor a asignar a id.
	 */
	public void setId(Integer id) {
	
		this.id = id;
	}

	
	/**
	 * Devuelve el valor de negociacionRiaRangoPoblacion.
	 *
	 * @return El valor de negociacionRiaRangoPoblacion.
	 */
	public NegociacionRiaRangoPoblacion getNegociacionRiaRangoPoblacion() {
	
		return negociacionRiaRangoPoblacion;
	}

	/**
	 * Asigna un nuevo valor a negociacionRiaRangoPoblacion.
	 *
	 * @param negociacionRiaRangoPoblacion El valor a asignar a negociacionRiaRangoPoblacion.
	 */
	public void
			setNegociacionRiaRangoPoblacion(NegociacionRiaRangoPoblacion negociacionRiaRangoPoblacion) {
	
		this.negociacionRiaRangoPoblacion = negociacionRiaRangoPoblacion;
	}

	
	
	/**
	 * Devuelve el valor de actividad.
	 *
	 * @return El valor de actividad.
	 */
	public Actividad getActividad() {
	
		return actividad;
	}


	
	/**
	 * Asigna un nuevo valor a actividad.
	 *
	 * @param actividad El valor a asignar a actividad.
	 */
	public void setActividad(Actividad actividad) {
	
		this.actividad = actividad;
	}


	/**
	 * Devuelve el valor de meta.
	 *
	 * @return El valor de meta.
	 */
	public Integer getMeta() {
	
		return meta;
	}

	
	/**
	 * Asigna un nuevo valor a meta.
	 *
	 * @param meta El valor a asignar a meta.
	 */
	public void setMeta(Integer meta) {
	
		this.meta = meta;
	}
}
