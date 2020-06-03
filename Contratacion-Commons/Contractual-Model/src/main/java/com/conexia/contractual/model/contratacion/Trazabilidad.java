package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.model.security.User;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;


/**
 * Entidad que se encarga de llevar el historico de cambios para cualquier
 * entidad del sistema
 * 
 * @author David Garcia <dgarcia@conexia.com>
 * 
 */
@Entity
@Table(name = "trazabilidad", schema = "contratacion")
@NamedQuery(
	name="Trazabilidad.obtenerTrazaComitePorPK",
	query="SELECT new com.conexia.contratacion.commons.dto.TrazabilidadDto(t.fechaModificacion, t.descripcion, t.valorAnterior, t.valorNuevo, u.nombreUsuario) From Trazabilidad t JOIN t.usuario u WHERE t.llavePrimaria = :comiteId AND t.nombreTabla = 'contratacion.comite_precontratacion' ORDER BY t.fechaModificacion DESC"
)
public class Trazabilidad implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5982352982097073659L;

	/**
	 * Identificador unico de trazabilidad
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/**
	 * Nombre completo de la tabla, debe concatenar con ".", <nombre
	 * esquema>.<nombre de la tabla>
	 */
	@Column(name = "nombre_tabla")
	private String nombreTabla;

	/**
	 * Identificador unico del registro al que se va a realizar la traza
	 */
	@Column(name = "p_key")
	private Long llavePrimaria;

	/**
	 * Descripcion del cambio realizado, como nombre de campos afectados y/o
	 * accion CRUD ejecutada
	 */
	@Column(name = "descripcion")
	private String descripcion;

	/**
	 * Valor de los campos del registros relacionados en la descripcion antes de
	 * la modificacion
	 */
	@Column(name = "valor_anterior")
	private String valorAnterior;

	/**
	 * Valor de los campos del registros relacionados en la descripcion
	 * posteriores a la modificacion
	 */
	@Column(name = "valor_nuevo")
	private String valorNuevo;

	/**
	 * Usuario que realiza la modificacion
	 */
	@ManyToOne(targetEntity = User.class)
	@JoinColumn(name = "usuario_id")
	private User usuario;

	/**
	 * Fecha de ejecucion de la modificacion
	 */
	@Column(name = "fecha_modificacion")
	private Date fechaModificacion;

	/* Getters & Setters */

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombreTabla() {
		return nombreTabla;
	}

	public void setNombreTabla(String nombreTabla) {
		this.nombreTabla = nombreTabla;
	}

	public Long getLlavePrimaria() {
		return llavePrimaria;
	}

	public void setLlavePrimaria(Long llavePrimaria) {
		this.llavePrimaria = llavePrimaria;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getValorAnterior() {
		return valorAnterior;
	}

	public void setValorAnterior(String valorAnterior) {
		this.valorAnterior = valorAnterior;
	}

	public String getValorNuevo() {
		return valorNuevo;
	}

	public void setValorNuevo(String valorNuevo) {
		this.valorNuevo = valorNuevo;
	}

	public User getUsuario() {
		return usuario;
	}

	public void setUsuario(User usuario) {
		this.usuario = usuario;
	}

	public Date getFechaModificacion() {
		return fechaModificacion;
	}

	public void setFechaModificacion(Date fechaModificacion) {
		this.fechaModificacion = fechaModificacion;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((descripcion == null) ? 0 : descripcion.hashCode());
		result = prime
				* result
				+ ((fechaModificacion == null) ? 0 : fechaModificacion
						.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((llavePrimaria == null) ? 0 : llavePrimaria.hashCode());
		result = prime * result
				+ ((nombreTabla == null) ? 0 : nombreTabla.hashCode());
		result = prime * result + ((usuario == null) ? 0 : usuario.hashCode());
		result = prime * result
				+ ((valorAnterior == null) ? 0 : valorAnterior.hashCode());
		result = prime * result
				+ ((valorNuevo == null) ? 0 : valorNuevo.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Trazabilidad other = (Trazabilidad) obj;
		if (descripcion == null) {
			if (other.descripcion != null)
				return false;
		} else if (!descripcion.equals(other.descripcion))
			return false;
		if (fechaModificacion == null) {
			if (other.fechaModificacion != null)
				return false;
		} else if (!fechaModificacion.equals(other.fechaModificacion))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (llavePrimaria == null) {
			if (other.llavePrimaria != null)
				return false;
		} else if (!llavePrimaria.equals(other.llavePrimaria))
			return false;
		if (nombreTabla == null) {
			if (other.nombreTabla != null)
				return false;
		} else if (!nombreTabla.equals(other.nombreTabla))
			return false;
		if (usuario == null) {
			if (other.usuario != null)
				return false;
		} else if (!usuario.equals(other.usuario))
			return false;
		if (valorAnterior == null) {
			if (other.valorAnterior != null)
				return false;
		} else if (!valorAnterior.equals(other.valorAnterior))
			return false;
		if (valorNuevo == null) {
			if (other.valorNuevo != null)
				return false;
		} else if (!valorNuevo.equals(other.valorNuevo))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Trazabilidad [id=" + id + ", nombreTabla=" + nombreTabla
				+ ", llavePrimaria=" + llavePrimaria + ", descripcion="
				+ descripcion + ", valorAnterior=" + valorAnterior
				+ ", valorNuevo=" + valorNuevo + ", usuario=" + usuario
				+ ", fechaModificacion=" + fechaModificacion + "]";
	}

}
