package com.conexia.contractual.model.maestros;

import javax.persistence.*;

@Entity
@NamedQueries({
        @NamedQuery(name = "RangoPoblacion.findAll",
                query = "SELECT new com.conexia.contratacion.commons.dto.maestros.RangoPoblacionDto(rp.id, rp.descripcion, rp.edadDesde, rp.edadHasta) "
                        + " FROM RangoPoblacion rp where rp.isDefault is null order by rp.id"),
        @NamedQuery(name = "RangoPoblacion.buscarPorDescripciones",
                query = "SELECT new com.conexia.contratacion.commons.dto.maestros.RangoPoblacionDto(rp.id, rp.descripcion, rp.edadDesde, rp.edadHasta) "
                        + " FROM RangoPoblacion rp where rp.descripcion in (:descripcionRango)"),
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "RangoPoblacion.findIdRangoPoblacion",
			query = "SELECT COALESCE((SELECT DISTINCT rp.id FROM maestros.rango_poblacion rp WHERE rp.descripcion = :descripcionRango),0)")
})
@Table(name = "rango_poblacion", schema = "maestros")
public class RangoPoblacion extends Descriptivo {

	/**
	 *
	 */
	private static final long serialVersionUID = 5717880616544154482L;

	@Column(name = "edad_desde", nullable = false)
	private Integer edadDesde;

	@Column(name = "edad_hasta", nullable = false)
	private Integer edadHasta;

	@Column(name = "is_default", nullable = false)
	private Boolean isDefault;

	public RangoPoblacion() {}

	public RangoPoblacion(Integer id){
		setId(id);
	}

	public Integer getEdadDesde() {
		return edadDesde;
	}

	public void setEdadDesde(Integer edadDesde) {
		this.edadDesde = edadDesde;
	}

	public Integer getEdadHasta() {
		return edadHasta;
	}

	public void setEdadHasta(Integer edadHasta) {
		this.edadHasta = edadHasta;
	}


	/**
	 * Devuelve el valor de isDefault.
	 *
	 * @return El valor de isDefault.
	 */
	public Boolean getIsDefault() {

		return isDefault;
	}


	/**
	 * Asigna un nuevo valor a isDefault.
	 *
	 * @param isDefault El valor a asignar a isDefault.
	 */
	public void setIsDefault(Boolean isDefault) {

		this.isDefault = isDefault;
	}
}
