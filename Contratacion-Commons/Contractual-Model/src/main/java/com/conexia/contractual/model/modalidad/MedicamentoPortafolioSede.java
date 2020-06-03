package com.conexia.contractual.model.modalidad;

import com.conexia.contratacion.commons.constants.CommonConstants;
import com.conexia.contractual.model.maestros.Medicamento;

import javax.persistence.*;
import java.io.Serializable;

/**
 * The persistent class for the mod_medicamento_portafolio_sede database table.
 * 
 */
@Entity
@Table(name = "mod_medicamento_portafolio_sede", schema = "contratacion")
@NamedNativeQueries({
	@NamedNativeQuery(name="MedicamentoPortafolioSede.eliminarMedicamentoPortafolioSede",
			query="DELETE FROM contratacion.mod_medicamento_portafolio_sede "
					+ " WHERE categoria_medicamento_portafolio_id IN(SELECT mc.id "
					+ "		FROM contratacion.mod_categoria_medicamento_portafolio_sede mc"
					+ "		INNER JOIN contratacion.mod_portafolio_sede mps on mc.portafolio_sede_id = mps.id"
					+ "  	WHERE mps.prestador_id =:prestadorId)"),
	@NamedNativeQuery(name = "MedicamentoPortafolioSede.insertarMedicamentoPortafolioSede" ,
	query = "INSERT INTO contratacion.mod_medicamento_portafolio_sede (categoria_medicamento_portafolio_id, medicamento_id,habilitado)"
			+ " SELECT cmp.id, m.id, true"
			+ "	FROM  maestros.medicamento m "
			+ " JOIN contratacion.categoria_medicamento c on c.id = m.categoria_id "
			+ " JOIN contratacion.mod_categoria_medicamento_portafolio_sede cmp on cmp.categoria_medicamento_id = c.id "
			+ " JOIN contratacion.mod_oferta_sede_prestador osp on osp.portafolio_id = portafolio_sede_id"
			+ " JOIN contratacion.mod_oferta_prestador op on osp.oferta_id = op.id"
			+ " JOIN contratacion.prestador p  on p.id = op.prestador_id  and p.id =:prestadorId"
			+ " JOIN contratacion.mod_servicio_portafolio_sede ms on ms.portafolio_sede_id = osp.portafolio_id "
			+ " JOIN contratacion.servicio_salud ss on ss.id = ms.servicio_salud_id "
			+ " WHERE m.id =:medicamentoId"
			+ " AND ss.codigo = '"+CommonConstants.COD_SERVICIO_MEDICAMENTOS+"' "
			+ " AND osp.sede_prestador_id =:sedePrestadorId"
			+ " AND m.estado_medicamento_id = 1 "
			+ " AND not exists (select  null from contratacion.mod_medicamento_portafolio_sede mps"
			+ "					WHERE mps.categoria_medicamento_portafolio_id = cmp.id "
			+ "					AND mps.medicamento_id = m.id)"),
	@NamedNativeQuery(name = "MedicamentoPortafolioSede.insertarMasivoMedicamentoPortafolioSede" ,
	query = "INSERT INTO contratacion.mod_medicamento_portafolio_sede (categoria_medicamento_portafolio_id, medicamento_id,habilitado)"
			+ " SELECT distinct cmp.id, m.id, :habilitado "
			+ " FROM  maestros.medicamento m "
			+ " JOIN contratacion.categoria_medicamento c on c.id = m.categoria_id "
			+ " JOIN contratacion.mod_categoria_medicamento_portafolio_sede cmp on cmp.categoria_medicamento_id = c.id "
			+ " JOIN contratacion.mod_oferta_sede_prestador osp on osp.portafolio_id = portafolio_sede_id "
			+ " JOIN contratacion.mod_oferta_prestador op on osp.oferta_id = op.id "
			+ " JOIN contratacion.prestador p  on p.id = op.prestador_id  and p.id =:prestadorId "
			+ " JOIN contratacion.mod_servicio_portafolio_sede ms on ms.portafolio_sede_id = osp.portafolio_id "
			+ " JOIN contratacion.servicio_salud ss on ss.id = ms.servicio_salud_id "
			+ " WHERE ss.codigo = '"+ CommonConstants.COD_SERVICIO_MEDICAMENTOS+"' "
			+ " AND m.estado_medicamento_id = 1 "
			+ " AND not exists (SELECT  null from contratacion.mod_medicamento_portafolio_sede mps "
			+ " 				WHERE mps.categoria_medicamento_portafolio_id = cmp.id "
			+ " 				AND mps.medicamento_id = m.id) "),
	@NamedNativeQuery(name="MedicamentoPortafolioSede.insertarBaseMedicamentoPortafolioSede" ,
		query="INSERT INTO contratacion.mod_medicamento_portafolio_sede (categoria_medicamento_portafolio_id, medicamento_id,habilitado)"
				+ " SELECT DISTINCT cmp.id, m.id, :habilitado "
				+ " FROM contratacion.base_portafolio_medicamento mp "
				+ " JOIN maestros.medicamento m on m.codigo = mp.codigo  and mp.atc = m.atc "
				+ " JOIN contratacion.categoria_medicamento c on c.id = m.categoria_id "
				+ " JOIN contratacion.mod_categoria_medicamento_portafolio_sede cmp on cmp.categoria_medicamento_id = c.id "
				+ " JOIN contratacion.mod_oferta_sede_prestador osp on osp.portafolio_id = portafolio_sede_id "
				+ " JOIN contratacion.mod_oferta_prestador op on osp.oferta_id = op.id "
				+ " JOIN contratacion.prestador p  on p.id = op.prestador_id  and p.id =:prestadorId "
				+ " LEFT JOIN contratacion.mod_medicamento_portafolio_sede mps on mps.categoria_medicamento_portafolio_id = cmp.id "
				+ " WHERE mps.id is null and mp.is_total= true ")	
})
public class MedicamentoPortafolioSede implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2873479222796464923L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer cantidad;

	@Column(name = "codigo_interno")
	private String codigoInterno;

	@ManyToOne
	@JoinColumn(name = "medicamento_id")
	private Medicamento medicamento;

	@ManyToOne
	@JoinColumn(name = "categoria_medicamento_portafolio_id")
	private CategoriaMedicamentoPortafolioSede categoriaMedicamentoPortafolio;

	@Column(name = "habilitado")
	private Boolean habilitado;

	public MedicamentoPortafolioSede() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCantidad() {
		return this.cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public String getCodigoInterno() {
		return this.codigoInterno;
	}

	public void setCodigoInterno(String codigoInterno) {
		this.codigoInterno = codigoInterno;
	}

	public Medicamento getMedicamento() {
		return medicamento;
	}

	public void setMedicamento(Medicamento medicamento) {
		this.medicamento = medicamento;
	}

	public CategoriaMedicamentoPortafolioSede getCategoriaMedicamentoPortafolio() {
		return categoriaMedicamentoPortafolio;
	}

	public void setCategoriaMedicamentoPortafolio(
			CategoriaMedicamentoPortafolioSede categoriaMedicamentoPortafolio) {
		this.categoriaMedicamentoPortafolio = categoriaMedicamentoPortafolio;
	}

	public Boolean getHabilitado() {
		return habilitado;
	}

	public void setHabilitado(Boolean habilitado) {
		this.habilitado = habilitado;
	}

}