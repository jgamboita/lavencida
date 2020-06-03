package com.conexia.contractual.model.contratacion.comite;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "acta_comite_compromiso", schema = "contratacion")
@NamedQueries({
	@NamedQuery(
			name="CompromisoComite.obtenerCompromisosPorComiteId",
			query="SELECT new com.conexia.contratacion.commons.dto.comite.CompromisoComiteDto(c.id, c.tarea, c.responsable, c.fechaCompromiso) FROM CompromisoComite c JOIN c.actaComite a WHERE a.comite.id = :comiteId"
	)
})
public class CompromisoComite implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7988354598145765483L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "acta_comite_contratacion_id")
	private ActaComite actaComite;

	@Column(name="tarea")
	private String tarea;

	@Column(name="responsable")
	private String responsable;

	@Column(name="fecha_compromiso")
	private Date fechaCompromiso;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ActaComite getActaComite() {
		return actaComite;
	}

	public void setActaComite(ActaComite actaComite) {
		this.actaComite = actaComite;
	}

	public String getTarea() {
		return tarea;
	}

	public void setTarea(String tarea) {
		this.tarea = tarea;
	}

	public String getResponsable() {
		return responsable;
	}

	public void setResponsable(String responsable) {
		this.responsable = responsable;
	}

	public Date getFechaCompromiso() {
		return fechaCompromiso;
	}

	public void setFechaCompromiso(Date fechaCompromiso) {
		this.fechaCompromiso = fechaCompromiso;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((actaComite == null) ? 0 : actaComite.hashCode());
		result = prime * result
				+ ((fechaCompromiso == null) ? 0 : fechaCompromiso.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((responsable == null) ? 0 : responsable.hashCode());
		result = prime * result + ((tarea == null) ? 0 : tarea.hashCode());
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
		CompromisoComite other = (CompromisoComite) obj;
		if (actaComite == null) {
			if (other.actaComite != null)
				return false;
		} else if (!actaComite.equals(other.actaComite))
			return false;
		if (fechaCompromiso == null) {
			if (other.fechaCompromiso != null)
				return false;
		} else if (!fechaCompromiso.equals(other.fechaCompromiso))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (responsable == null) {
			if (other.responsable != null)
				return false;
		} else if (!responsable.equals(other.responsable))
			return false;
		if (tarea == null) {
			if (other.tarea != null)
				return false;
		} else if (!tarea.equals(other.tarea))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CompromisoComite [id=" + id + ", actaComite=" + actaComite
				+ ", tarea=" + tarea + ", responsable=" + responsable
				+ ", fechaCompromiso=" + fechaCompromiso + "]";
	}

}
