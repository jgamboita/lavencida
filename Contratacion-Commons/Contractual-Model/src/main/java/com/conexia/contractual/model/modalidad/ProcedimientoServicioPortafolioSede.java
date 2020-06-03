package com.conexia.contractual.model.modalidad;

import com.conexia.contractual.model.maestros.Procedimientos;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the mod_procedimiento_servicio_portafolio_sede
 * database table.
 * 
 */
@Entity
@Table(name = "mod_procedimiento_servicio_portafolio_sede", schema = "contratacion")
@NamedNativeQueries({
	@NamedNativeQuery(name="ProcedimientoServicioPortafolioSede.eliminarProcedimientoServicioPortafolioSede",
			query="DELETE FROM contratacion.mod_procedimiento_servicio_portafolio_sede "
					+ " WHERE servicio_portafolio_sede_id IN("
					+ "    SELECT ms.id "
					+ "    FROM contratacion.mod_servicio_portafolio_sede ms"
					+ "	   INNER JOIN contratacion.mod_portafolio_sede mps on ms.portafolio_sede_id = mps.id"
					+ "    WHERE mps.prestador_id =:prestadorId)"),
	@NamedNativeQuery(name="ProcedimientoServicioPortafolioSede.insertarProcedimientoServicioPortafolioSede",
	query="INSERT INTO contratacion.mod_procedimiento_servicio_portafolio_sede "
			+ " (servicio_portafolio_sede_id, procedimiento_servicio_id, habilitado)"
			+ " SELECT DISTINCT cmp.id, m.procedimiento_servicio_id, true "
			+ " FROM contratacion.upc_liquidacion_servicio us"
			+ " JOIN contratacion.upc_liquidacion_procedimiento m on m.upc_liquidacion_servicio_id = us.id"
			+ " JOIN contratacion.mod_servicio_portafolio_sede cmp on cmp.servicio_salud_id = us.servicio_salud_id and us.servicio_salud_id =:servicioId"
			+ " JOIN contratacion.mod_oferta_sede_prestador osp on osp.portafolio_id = cmp.portafolio_sede_id "
			+ "					AND osp.sede_prestador_id =:sedePrestadorId"
			+ " JOIN contratacion.mod_oferta_prestador op on osp.oferta_id = op.id and op.prestador_id =:prestadorId"
			+ " WHERE us.liquidacion_zona_id = (SELECT id FROM contratacion.liquidacion_zona limit 1 )"
			+ " AND m.procedimiento_servicio_id =:procedimientoId"
			+ " AND NOT EXISTS (SELECT  NULL FROM Contratacion.mod_procedimiento_servicio_portafolio_sede"
			+ "		 			WHERE servicio_portafolio_sede_id=cmp.id AND procedimiento_servicio_id = m.procedimiento_servicio_id "
			+ "					AND  habilitado = TRUE)"),
    @NamedNativeQuery(name="ProcedimientoServicioPortafolioSede.insertarProcedimientoServicioPortafolioSedeConReps",
            query="INSERT INTO contratacion.mod_procedimiento_servicio_portafolio_sede (procedimiento_servicio_id, servicio_portafolio_sede_id, habilitado) " 
            + " SELECT ps.id, msps.id,:habilitado"
            + " FROM contratacion.mod_servicio_portafolio_sede msps"
            + " JOIN contratacion.mod_portafolio_sede s on s.id = msps.portafolio_sede_id and s.prestador_id = :prestadorId"
            + " JOIN maestros.procedimiento_servicio ps on ps.servicio_id = msps.servicio_salud_id and ps.estado= 1"
            + " WHERE not exists (SELECT NULL FROM contratacion.mod_procedimiento_servicio_portafolio_sede"
            + " 				  WHERE procedimiento_servicio_id = ps.id AND servicio_portafolio_sede_id = msps.id)"
            + " GROUP BY ps.id, msps.id ")
})
public class ProcedimientoServicioPortafolioSede implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "codigo_interno")
	private String codigoInterno;

	@Column(name = "habilitado")
	private Boolean habilitado;

	@ManyToOne
	@JoinColumn(name = "procedimiento_servicio_id")
	private Procedimientos procedimientoServicio;

	// bi-directional many-to-one association to ServicioPortafolioSede
	@ManyToOne
	@JoinColumn(name = "servicio_portafolio_sede_id")
	private ServicioPortafolioSede servicioPortafolioSede;

	public ProcedimientoServicioPortafolioSede() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getCodigoInterno() {
		return this.codigoInterno;
	}

	public void setCodigoInterno(String codigoInterno) {
		this.codigoInterno = codigoInterno;
	}

	public Boolean getHabilitado() {
		return this.habilitado;
	}

	public void setHabilitado(Boolean habilitado) {
		this.habilitado = habilitado;
	}

	public Procedimientos getProcedimientoServicio() {
		return procedimientoServicio;
	}

	public void setProcedimientoServicio(
			Procedimientos procedimientoServicio) {
		this.procedimientoServicio = procedimientoServicio;
	}

	public ServicioPortafolioSede getServicioPortafolioSede() {
		return servicioPortafolioSede;
	}

	public void setServicioPortafolioSede(
			ServicioPortafolioSede servicioPortafolioSede) {
		this.servicioPortafolioSede = servicioPortafolioSede;
	}
}