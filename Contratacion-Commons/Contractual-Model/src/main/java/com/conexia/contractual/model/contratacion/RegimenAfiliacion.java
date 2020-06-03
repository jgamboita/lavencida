/**
 *
 */
package com.conexia.contractual.model.contratacion;

import javax.persistence.*;
import java.io.Serializable;


/**
 * Clase encargada de el tipo de fase.
 *
 */
@Entity
@Table(name = "regimen_afiliacion", schema = "maestros")
@NamedQueries({
    @NamedQuery(name="RegimenAfiliacion.findAll",
			query=	"   SELECT new com.conexia.contratacion.commons.dto.RegimenAfiliacionDto(ra.id, ra.descripcion) "
					+ " FROM RegimenAfiliacion ra "
					+ "	ORDER BY ra.descripcion")
})
public class RegimenAfiliacion implements Serializable {

	/**
	 * Uuid serializable.
	 */
	private static final long serialVersionUID = 5482130963801854988L;

	/** Identificador. **/
	private Integer id;

	/** Código. **/
	private String codigo;

	/** Descripcion. **/
	private String descripcion;





	/**
	 * Constructor de clase.
	 */
	public RegimenAfiliacion() {

	}

	/**
	 * Constructor de clase.
	 *
	 * @param id
	 *            identificador.
	 * @param codigo
	 *            código.
	 * @param descripcion
	 *            descripción.
	 */
	public RegimenAfiliacion(Integer id, String codigo, String descripcion) {
		this.id = id;
		this.codigo = codigo;
		this.descripcion = descripcion;
	}

	/**
	 * Constructor de clase.
	 *
	 * @param id
	 *            identificador.
	 * @param descripcion
	 *            descripcion.
	 */
	public RegimenAfiliacion(Integer id, String descripcion) {
		this (id, null, descripcion);
	}

	/**
	 * Getter del id.
	 * @return el id.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Integer getId() {
		return id;
	}

	/**
	 * Setter del id a establecer.
	 *
	 * @param id el id a establecer.
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * Getter de codigo.
	 * @return el codigo.
	 */
	@Column(name = "codigo", length = 50)
	public String getCodigo() {
		return codigo;
	}

	/**
	 * Setter de código a establecer.
	 *
	 * @param codigo el código a establecer.
	 */
	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	/**
	 * Getter de descripción.
	 * @return la descripción.
	 */
	@Column(name = "descripcion", nullable = false, length = 500)
	public String getDescripcion() {
		return descripcion;
	}

	/**
	 * Setter de descripción a establecer.
	 *
	 * @param descripcion el descripción a establecer.
	 */
	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	/**
	 * @see Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((codigo == null) ? 0 : codigo.hashCode());
		result = prime * result
				+ ((descripcion == null) ? 0 : descripcion.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * @see Object#equals(Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RegimenAfiliacion other = (RegimenAfiliacion) obj;
		if (codigo == null) {
			if (other.codigo != null)
				return false;
		} else if (!codigo.equals(other.codigo))
			return false;
		if (descripcion == null) {
			if (other.descripcion != null)
				return false;
		} else if (!descripcion.equals(other.descripcion))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return "CodigoDescripcion [id=" + id + ", codigo=" + codigo
				+ ", descripcion=" + descripcion + "]";
	}



}
