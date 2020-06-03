package com.conexia.contractual.model.maestros;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entidad encargada de controlar los capitulos de un procedimiento, nueva nomenclatura para pgp
 * @author dmora
 *
 */
@Entity
@Table(name = "capitulo_procedimiento", schema = "maestros")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({
	@NamedQuery(
			name = "CapituloProcedimiento.consultarCapitulos",
			query = "SELECT new com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto(ca.id, ca.codigo, ca.descripcion) "
					+ "FROM CapituloProcedimiento ca "
					+ "ORDER BY ca.id"
		),
	@NamedQuery(
			name= "CapituloProcedimiento.getByCodes",
			query = "SELECT new com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto(ca.id, trim(ca.codigo), ca.descripcion) "
					+ "FROM CapituloProcedimiento ca "
					+ " WHERE ca.codigo IN(:codigosCapitulos)"
					),
	@NamedQuery(
			name ="CapituloProcedimiento.capitulosReferente",
			query = "SELECT DISTINCT new com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto(cp.id, cp.codigo, cp.descripcion) "
					+ "FROM ReferenteCapitulo rc "
					+ "JOIN rc.capituloProcedimiento cp "
					+ "WHERE rc.referente.id = :referenteId "
					+ "ORDER BY cp.codigo ")
     })
public class CapituloProcedimiento implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

	@Column(name = "codigo", nullable = false, length = 3)
    private String codigo;

	@Column(name = "descripcion", nullable = false, length = 150)
    private String descripcion;




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
