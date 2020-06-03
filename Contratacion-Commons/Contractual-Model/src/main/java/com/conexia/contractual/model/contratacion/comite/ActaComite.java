package com.conexia.contractual.model.contratacion.comite;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "acta_comite_contratacion", schema = "contratacion")
@NamedQueries({
	@NamedQuery(
		name = "ActaComite.validarExisteActaPorComiteId", 
		query = "SELECT COUNT(a.id) > 0 FROM ActaComite a WHERE a.comite.id = :comiteId"
	),
	@NamedQuery(
		name="ActaComite.obtenerActaPorComiteId",
		query="SELECT new com.conexia.contratacion.commons.dto.comite.ActaComiteDto(a.id, a.lugar, a.fechaActa, a.cordinador, a.cargo, a.objetivos, a.desarrolloReunion, a.lecturaActaAnterior) FROM ActaComite a WHERE a.comite.id = :comiteId"
	)
})
public class ActaComite implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5082764686652401193L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "comite_contratacion_id")
	private ComitePrecontratacion comite;

	@Column(name = "lugar")
	private String lugar;

	@Column(name = "fecha_acta")
	private Date fechaActa;

	@Column(name = "coordinador")
	private String cordinador;

	@Column(name = "cargo")
	private String cargo;

	@Column(name = "objetivos")
	private String objetivos;

	@Column(name = "desarrollo_reunion")
	private String desarrolloReunion;

	@Column(name = "lectura_acta_anterior")
	private String lecturaActaAnterior;

	@Column(name = "nombre_archivo")
	private String nombreArchivo;

	@Column(name = "nombre_archivo_servidor")
	private String nombreArchivoServidor;

	@Column(name = "mime_type")
	private String mimeType;

	@OneToMany(mappedBy = "actaComite", fetch = FetchType.LAZY)
	private List<CompromisoComite> compromisos = new ArrayList<CompromisoComite>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public ComitePrecontratacion getComite() {
		return comite;
	}

	public void setComite(ComitePrecontratacion comite) {
		this.comite = comite;
	}

	public String getLugar() {
		return lugar;
	}

	public void setLugar(String lugar) {
		this.lugar = lugar;
	}

	public Date getFechaActa() {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fechaActa);
		return fechaActa;
	}

	public void setFechaActa(Date fechaActa) {
		this.fechaActa = fechaActa;
	}

	public String getCordinador() {
		return cordinador;
	}

	public void setCordinador(String coordinador) {
		this.cordinador = coordinador;
	}

	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	public String getObjetivos() {
		return objetivos;
	}

	public void setObjetivos(String objetivos) {
		this.objetivos = objetivos;
	}

	public String getDesarrolloReunion() {
		return desarrolloReunion;
	}

	public void setDesarrolloReunion(String desarrolloReunion) {
		this.desarrolloReunion = desarrolloReunion;
	}

	public String getLecturaActaAnterior() {
		return lecturaActaAnterior;
	}

	public void setLecturaActaAnterior(String lecturaActaAnterior) {
		this.lecturaActaAnterior = lecturaActaAnterior;
	}

	public String getNombreArchivo() {
		return nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	public String getNombreArchivoServidor() {
		return nombreArchivoServidor;
	}

	public void setNombreArchivoServidor(String nombreArchivoServidor) {
		this.nombreArchivoServidor = nombreArchivoServidor;
	}

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public List<CompromisoComite> getCompromisos() {
		return compromisos;
	}

	public void setCompromisos(List<CompromisoComite> compromisos) {
		this.compromisos = compromisos;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((cargo == null) ? 0 : cargo.hashCode());
		result = prime * result + ((comite == null) ? 0 : comite.hashCode());
		result = prime * result
				+ ((compromisos == null) ? 0 : compromisos.hashCode());
		result = prime * result
				+ ((cordinador == null) ? 0 : cordinador.hashCode());
		result = prime
				* result
				+ ((desarrolloReunion == null) ? 0 : desarrolloReunion
						.hashCode());
		result = prime * result
				+ ((fechaActa == null) ? 0 : fechaActa.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime
				* result
				+ ((lecturaActaAnterior == null) ? 0 : lecturaActaAnterior
						.hashCode());
		result = prime * result + ((lugar == null) ? 0 : lugar.hashCode());
		result = prime * result
				+ ((mimeType == null) ? 0 : mimeType.hashCode());
		result = prime * result
				+ ((nombreArchivo == null) ? 0 : nombreArchivo.hashCode());
		result = prime
				* result
				+ ((nombreArchivoServidor == null) ? 0 : nombreArchivoServidor
						.hashCode());
		result = prime * result
				+ ((objetivos == null) ? 0 : objetivos.hashCode());
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
		ActaComite other = (ActaComite) obj;
		if (cargo == null) {
			if (other.cargo != null)
				return false;
		} else if (!cargo.equals(other.cargo))
			return false;
		if (comite == null) {
			if (other.comite != null)
				return false;
		} else if (!comite.equals(other.comite))
			return false;
		if (compromisos == null) {
			if (other.compromisos != null)
				return false;
		} else if (!compromisos.equals(other.compromisos))
			return false;
		if (cordinador == null) {
			if (other.cordinador != null)
				return false;
		} else if (!cordinador.equals(other.cordinador))
			return false;
		if (desarrolloReunion == null) {
			if (other.desarrolloReunion != null)
				return false;
		} else if (!desarrolloReunion.equals(other.desarrolloReunion))
			return false;
		if (fechaActa == null) {
			if (other.fechaActa != null)
				return false;
		} else if (!fechaActa.equals(other.fechaActa))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (lecturaActaAnterior == null) {
			if (other.lecturaActaAnterior != null)
				return false;
		} else if (!lecturaActaAnterior.equals(other.lecturaActaAnterior))
			return false;
		if (lugar == null) {
			if (other.lugar != null)
				return false;
		} else if (!lugar.equals(other.lugar))
			return false;
		if (mimeType == null) {
			if (other.mimeType != null)
				return false;
		} else if (!mimeType.equals(other.mimeType))
			return false;
		if (nombreArchivo == null) {
			if (other.nombreArchivo != null)
				return false;
		} else if (!nombreArchivo.equals(other.nombreArchivo))
			return false;
		if (nombreArchivoServidor == null) {
			if (other.nombreArchivoServidor != null)
				return false;
		} else if (!nombreArchivoServidor.equals(other.nombreArchivoServidor))
			return false;
		if (objetivos == null) {
			if (other.objetivos != null)
				return false;
		} else if (!objetivos.equals(other.objetivos))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ActaComite [id=" + id + ", comite=" + comite + ", lugar="
				+ lugar + ", fechaActa=" + fechaActa + ", cordinador="
				+ cordinador + ", cargo=" + cargo + ", objetivos=" + objetivos
				+ ", desarrolloReunion=" + desarrolloReunion
				+ ", lecturaActaAnterior=" + lecturaActaAnterior
				+ ", nombreArchivo=" + nombreArchivo
				+ ", nombreArchivoServidor=" + nombreArchivoServidor
				+ ", mimeType=" + mimeType + ", compromisos=" + compromisos
				+ "]";
	}

}
