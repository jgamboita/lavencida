package com.conexia.contractual.model.maestros;

import javax.persistence.*;
import java.io.Serializable;


/**
 * Entidad encargada de controlar las categorias de un procedimiento, nueva nomenclatura para pgp
 * @author dmora
 *
 */
@Entity
@Table(name = "categoria_procedimiento", schema = "maestros")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@NamedQueries({
	@NamedQuery(
			name = "CategoriaProcedimiento.consultarCategoriasPorCapitulo",
			query = "SELECT new com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto(ca.id, ca.codigo, ca.descripcion) "
					+ "FROM CategoriaProcedimiento ca "
					+ "JOIN ca.capituloProcedimiento cap "
					+ "WHERE cap.id in (:capituloProcedimientoIds) "
					+ "ORDER BY ca.codigo "
		),
	@NamedQuery(
			name = "CategoriaProcedimiento.getByCodes",
			query = "SELECT new com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto(ca.id, trim(ca.codigo), ca.descripcion) "
					+ "FROM CategoriaProcedimiento ca "
					+ "WHERE ca.codigo IN (:categoriasProcedimiento) "
		),
	@NamedQuery(
	name = "CategoriaProcedimiento.relationCapituloCategoria",
	query = "SELECT new com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto(ca.id, trim(ca.codigo), ca.descripcion) "
			+ "FROM CategoriaProcedimiento ca "
			+ "JOIN ca.capituloProcedimiento cap "
			+ "WHERE cap.id = :capituloCode "
			+ "AND ca.id   = :categoriaCode"
	   ),
	@NamedQuery(
	name = "CategoriaProcedimiento.categoriasReferente",
	query = "SELECT DISTINCT new com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto(cat.id, cat.codigo, cat.descripcion) "
			+ "FROM ReferenteCapitulo rc "
			+ "JOIN rc.categoriaProcedimiento cat "
			+ "WHERE rc.referente.id = :referenteId "
			+ "ORDER BY cat.codigo "
			)
})
public class CategoriaProcedimiento implements Serializable{

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	@Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

	@Column(name = "codigo", nullable = false, length = 6)
    private String codigo;

	@Column(name = "descripcion", nullable = false, length = 1000)
    private String descripcion;

	@Column(name = "norma", nullable = false, length = 200)
    private String norma;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "capitulo_procedimiento_id")
	private CapituloProcedimiento capituloProcedimiento;




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

	public String getNorma() {
		return norma;
	}

	public void setNorma(String norma) {
		this.norma = norma;
	}

	public CapituloProcedimiento getCapituloProcedimiento() {
		return capituloProcedimiento;
	}

	public void setCapituloProcedimiento(CapituloProcedimiento capituloProcedimiento) {
		this.capituloProcedimiento = capituloProcedimiento;
	}

}
