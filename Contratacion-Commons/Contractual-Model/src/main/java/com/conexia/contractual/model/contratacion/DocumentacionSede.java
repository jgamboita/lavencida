package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entidad para la documentacion de la sede
 * 
 * @author jjoya
 *
 */
@Entity
@Table(name = "documentacion_sede", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name = "DocumentacionSede.findDtoByPrestadorId",
	query = "SELECT NEW com.conexia.contratacion.commons.dto.maestros.DocumentoSedeDto(d.id, d.nombreArchivo, d.nombreArchivoServidor, s.id, s.codigoPrestador, s.codigoSede, d.tipoArchivo) "
		+ "FROM DocumentacionSede d "
		+ "JOIN d.sede s "
		+ "WHERE s.prestador.id = :prestadorId ORDER BY s.id"),
	@NamedQuery(name = "DocumentacionSede.findDtoBySedePrestadorId",
		query = "SELECT new com.conexia.contratacion.commons.dto.maestros.DocumentoSedeDto(d.id, d.nombreArchivo, d.nombreArchivoServidor, s.id, s.codigoPrestador, s.codigoSede, d.tipoArchivo) "
				+ "FROM DocumentacionSede d "
				+ "JOIN d.sede s "
				+ "WHERE s.id = :sedeId ")	
})
public class DocumentacionSede implements Serializable, Identifiable<Long> {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "nombre_archivo", nullable = false, length = 256)
	private String nombreArchivo;

	@Column(name = "nombre_archivo_serv", nullable = false, length = 100)
	private String nombreArchivoServidor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sede_id", nullable = false)
	private SedePrestador sede;

	@Column(name = "tipo_archivo")
	private String tipoArchivo;

	public DocumentacionSede() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getNombreArchivo() {
		return this.nombreArchivo;
	}

	public void setNombreArchivo(String nombreArchivo) {
		this.nombreArchivo = nombreArchivo;
	}

	public SedePrestador getSede() {
		return sede;
	}

	public void setSede(SedePrestador sede) {
		this.sede = sede;
	}

	public String getNombreArchivoServidor() {
		return nombreArchivoServidor;
	}

	public void setNombreArchivoServidor(String nombreArchivoServidor) {
		this.nombreArchivoServidor = nombreArchivoServidor;
	}

	public String getTipoArchivo() {
		return tipoArchivo;
	}

	public void setTipoArchivo(String tipoArchivo) {
		this.tipoArchivo = tipoArchivo;
	}

}