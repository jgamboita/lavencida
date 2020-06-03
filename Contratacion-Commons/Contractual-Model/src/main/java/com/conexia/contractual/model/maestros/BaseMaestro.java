package com.conexia.contractual.model.maestros;

import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.MappedSuperclass;
import java.io.Serializable;
import java.util.Date;

/**
 * Clase base utilizada en la informaci&oacute;n de auditor&iacute;a de los
 * registros de la aplicaci&oacute;n.
 * @author conexia.
 *
 */
@MappedSuperclass
@Inheritance(strategy= InheritanceType.TABLE_PER_CLASS)
public  class BaseMaestro implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -1170413685999774789L;
	/**
	 * Fecha de eliminaci&oacute;n l&oacute;gica de un registro.
	 */
	@Column(name="fecha_delete", nullable=true)
	private Date fechaDelete;
	/**
	 * Fecha de creaci&oacute;n de un registro.
	 */
	@Column(name="fecha_insert", nullable = false, columnDefinition="timestamp default getDate()")
	private Date fechaInsert;
	/**
	 * Fecha de actualizaci&oacute;n de un registro.
	 */
	@Column(name="fecha_update", nullable = false,  columnDefinition="timestamp default getDate()")
	private Date fechaUpdate;
	/**
	 * Versi&oacute;n del registro.
	 */
	@Column(name="version", nullable = false, columnDefinition="int4 default 1")
	private Integer version = new Integer(1);

	/**
	 * Devuelve la informaci&oacute;n de la fecha de eliminaci&oacute;n l&oacute;gica de un registro.
	 * @return fechaDelete Devuelve un dato de tipo Date.
	 */
	public final Date getFechaDelete() {
		return this.fechaDelete;
	}

	/**
	 * Establece la informaci&oacute;n de la fecha de eliminaci&oacute;n l&oacute;gica de un registro.
	 * @param fechaDelete Establece un dato de tipo Date.
	 */
	public final void setFechaDelete(final Date fechaDelete) {
		this.fechaDelete = fechaDelete;
	}

	/**
	 * Devuelve la informaci&oacute;n de la fecha de creaci&oacute;n de un registro.
	 * @return fechaInsert Establece un dato de tipo Date.
	 */
	public final Date getFechaInsert() {
		return fechaInsert;
	}

	/**
	 * Establece la informaci&oacute;n de la fecha de creaci&oacute;n de un registro.
	 * @param fechaInsert Establece un dato de tipo Date.
	 */
	public final void setFechaInsert(final Date fechaInsert) {
		this.fechaInsert = fechaInsert;
	}

	/**
	 * Devuelve la informaci&oacute;n de la fecha de actualizaci&oacute;n de un registro.
	 * @return fechaUpdate Establece un dato de tipo Date.
	 */
	public final Date getFechaUpdate() {
		return this.fechaUpdate;
	}

	/**
	 * Establece la informaci&oacute;n de la fecha de actualizaci&oacute;n de un registro.
	 * @param fechaUpdate Devuelve un dato de tipo Date.
	 */
	public final void setFechaUpdate(final Date fechaUpdate) {
		this.fechaUpdate = fechaUpdate;
	}

	/**
	 * Devuelve la informaci&oacute;n de la versi&oacute;n del registro.
	 * @return version Devuelve un dato de tipo Integer.
	 */
	public final Integer getVersion() {
		return this.version;
	}

	/**
	 * Establece la informaci&oacute;n de la versi&oacute;n del registro.
	 * @param version Establece un dato de tipo Integer.
	 */
	public final void setVersion(final Integer version) {
		this.version = version;
	}
	
	
	
}
