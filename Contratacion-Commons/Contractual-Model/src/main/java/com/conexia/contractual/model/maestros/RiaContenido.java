package com.conexia.contractual.model.maestros;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Date;

/**
 * Entity implementation class for Entity: RiaContenido
 *
 */
@Entity
@NamedQueries({
	@NamedQuery(name = "RiaContenido.findRangoPorRuta",
			query = "SELECT DISTINCT new com.conexia.contratacion.commons.dto.maestros.RangoPoblacionDto(rp.id, rp.descripcion) from  RiaContenido rc "
				+ "JOIN rc.rangoPoblacion rp "
				+ "JOIN rc.ria r "
				+ "WHERE r.id  in (:riaId) AND rp.isDefault = false "
				+ "ORDER BY rp.descripcion "
			),
	@NamedQuery(name = "RiaContenido.listRangoPoblacionPorRuta",
			query = "SELECT DISTINCT rc.rangoPoblacion.id  "
					+ " FROM RiaContenido rc "
					+ " WHERE rc.ria.id = :riaId"),
	@NamedQuery(name = "RiaContenido.listActividadesPorRutaYRangoPobl",
			query = "SELECT DISTINCT rc.actividad.id "
					+ " FROM RiaContenido rc "
					+ " WHERE rc.ria.id = :riaId "
					+ "	AND rc.rangoPoblacion.id = :rangoPoblacionId ")
	
})
@Table(name = "ria_contenido", schema = "maestros")
public class RiaContenido implements Serializable {

	
	private static final long serialVersionUID = 1L;

	public RiaContenido() {
		super();
	}
	@Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;
	
	@ManyToOne
	@JoinColumn(name = "ria_id")
	private Ria ria;
	
	@ManyToOne
	@JoinColumn(name = "procedimiento_servicio_id")
    private Procedimientos procedimientoServicio;
	
	@ManyToOne
	@JoinColumn(name = "rango_poblacion_id")
	private RangoPoblacion rangoPoblacion;
	
	@Column(name = "fecha_insert", nullable = false)
	private Date fecha_insert;
	
	@ManyToOne
	@JoinColumn(name = "medicamento_id")
    private Medicamento medicamento;
	
	@ManyToOne
	@JoinColumn(name = "actividad_id")
	private Actividad actividad;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Procedimientos getProcedimientoServicio() {
		return procedimientoServicio;
	}

	public void setProcedimientoServicio(Procedimientos procedimientoServicio) {
		this.procedimientoServicio = procedimientoServicio;
	}

	public RangoPoblacion getRangoPoblacion() {
		return rangoPoblacion;
	}

	public void setRangoPoblacion(RangoPoblacion rangoPoblacion) {
		this.rangoPoblacion = rangoPoblacion;
	}

	public Date getFecha_insert() {
		return fecha_insert;
	}

	public void setFecha_insert(Date fecha_insert) {
		this.fecha_insert = fecha_insert;
	}

	public Medicamento getMedicamento() {
		return medicamento;
	}

	public void setMedicamento(Medicamento medicamento) {
		this.medicamento = medicamento;
	}

	public Actividad getActividad() {
		return actividad;
	}

	public void setActividad(Actividad actividad) {
		this.actividad = actividad;
	}
	
}
