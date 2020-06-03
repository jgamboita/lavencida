package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;


/**
 * The persistent class for the tarifarios database table.
 *
 */
@Entity
@Table(schema = "contratacion", name="tarifarios")
@NamedQueries({
        @NamedQuery(name = "Tarifario.buscarPorDescripcion", query = "select t from Tarifario t where t.descripcion in (?1)")
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "Tarifario.buscarIdTarifarioDescripcion",
			query = "SELECT COALESCE((SELECT DISTINCT t.id "
					+ "FROM contratacion.tarifarios t WHERE t.descripcion = :descripcionTarifario), 0) ")
})
public class Tarifario implements Identifiable<Integer>, Serializable {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	private Integer id;

	private String codigo;

	private String descripcion;

	private Integer valor;

	private Boolean vigente;

	public Tarifario() {
	}

    public Tarifario(Integer id) {
        this.id = id;
    }

    public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCodigo() {
		return this.codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getDescripcion() {
		return this.descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public Integer getValor() {
		return this.valor;
	}

	public void setValor(Integer valor) {
		this.valor = valor;
	}

	public Boolean getVigente() {
		return this.vigente;
	}

	public void setVigente(Boolean vigente) {
		this.vigente = vigente;
	}
}