package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "tarifarios", schema="contratacion")
@NamedQueries({
	@NamedQuery(name="TipoTarifario.findAll",
			query=	"   SELECT new com.conexia.contratacion.commons.dto.maestros.TipoTarifarioDto(tt.id, tt.codigo,tt.descripcion) "
					+ " FROM TipoTarifario tt "
					+ "	ORDER BY tt.descripcion"),
	@NamedQuery(name="TipoTarifario.findTarifarioNoNormativo",
	query=	"   SELECT new com.conexia.contratacion.commons.dto.maestros.TipoTarifarioDto(tt.id, tt.codigo,tt.descripcion) "
			+ " FROM TipoTarifario tt "
			+ "	WHERE tt.codigo =:codigoTarifa"
			+ "	ORDER BY tt.descripcion"),
	@NamedQuery(name="TipoTarifario.findTarifarioByDescripion",
	query=	"   SELECT new com.conexia.contratacion.commons.dto.maestros.TipoTarifarioDto(tt.id, tt.codigo,tt.descripcion) "
			+ " FROM TipoTarifario tt "
			+ "	WHERE tt.descripcion =:descripcionTarifa")
})

public class TipoTarifario implements Identifiable<Long>, Serializable {

	private static final long serialVersionUID = -2651649032565124425L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", nullable = false)
	private Long id;

	@Column(name="codigo", nullable=false)
	private String codigo;

	@Column(name="descripcion", nullable=false)
	private String descripcion;



	public TipoTarifario() {}

	public TipoTarifario(Long id) {
		this.id = id;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}


}
