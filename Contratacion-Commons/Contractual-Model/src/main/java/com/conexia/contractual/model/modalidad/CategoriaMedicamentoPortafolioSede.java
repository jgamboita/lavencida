package com.conexia.contractual.model.modalidad;

import com.conexia.contratacion.commons.constants.CommonConstants;
import com.conexia.contratacion.commons.dto.capita.CategoriaMedicamentoPortafolioSedeDto;
import com.conexia.contractual.model.contratacion.CategoriaMedicamento;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

/**
 * The persistent class for the mod_categoria_medicamento_portafolio_sede
 * database table.
 * 
 */
@Entity
@Table(name = "mod_categoria_medicamento_portafolio_sede", schema = "contratacion")
@NamedNativeQueries({
	@NamedNativeQuery(name="CategoriaMedicamentoPortafolioSede.eliminarCategoriaMedicamentoPortafolioSede",
			query="DELETE FROM contratacion.mod_categoria_medicamento_portafolio_sede  "
					+ " WHERE id IN("
					+ "	SELECT mc.id FROM contratacion.mod_categoria_medicamento_portafolio_sede mc "
					+ "	INNER JOIN contratacion.mod_portafolio_sede mps on mc.portafolio_sede_id = mps.id"
					+ "	WHERE mps.prestador_id=:prestadorId)"),
	@NamedNativeQuery(name="CategoriaMedicamentoPortafolioSede.insertarCategoriasMedicamento",
			query="INSERT INTO contratacion.mod_categoria_medicamento_portafolio_sede("
					+ " portafolio_sede_id, categoria_medicamento_id, habilitado)"
					+ " SELECT DISTINCT osp.portafolio_id, cm.id as categoria_medicamento_id, :habilitado "
					+ "	FROM contratacion.mod_oferta_sede_prestador osp "
					+ "	CROSS JOIN contratacion.categoria_medicamento cm "
					+ " JOIN contratacion.sede_prestador s ON s.id = osp.sede_prestador_id and s.prestador_id = :prestadorId"
					+ " JOIN maestros.servicios_reps sr ON sr.servicio_codigo ="+ CommonConstants.COD_SERVICIO_MEDICAMENTOS
					+ "			AND  CAST(s.codigo_sede as numeric) = sr.numero_sede "
					+ "			AND s.codigo_habilitacion = sr.codigo_habilitacion "
					+ " LEFT JOIN contratacion.mod_categoria_medicamento_portafolio_sede cmp ON cmp.portafolio_sede_id = osp.portafolio_id"
					+ " WHERE cmp.id is null")
	
})
@SqlResultSetMapping(name = "CategoriaMedicamentoPortafolioSedeDtoTotalizado",
	classes = { 
		@ConstructorResult(
			targetClass = CategoriaMedicamentoPortafolioSedeDto.class,
			columns = {
				@ColumnResult(name="categoria_medicamento_portafolio_id"),
				@ColumnResult(name="categoria_medicamento_codigo"),
				@ColumnResult(name="categoria_medicamento_nombre"),
				@ColumnResult(name="categoria_medicamento_portafolio_estado"),
				@ColumnResult(name="medicamentos_seleccionados"),
				@ColumnResult(name="medicamentos_totales")
			}) 
	})

public class CategoriaMedicamentoPortafolioSede implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	@JoinColumn(name = "categoria_medicamento_id")
	private CategoriaMedicamento categoriaMedicamento;

	@ManyToOne
	@JoinColumn(name = "portafolio_sede_id")
	private PortafolioSede portafolioSede;

	@Column(name = "habilitado")
	private Boolean habilitado;
	
	//bi-directional many-to-one association to ModMedicamentoPortafolioSede
    @OneToMany(mappedBy="categoriaMedicamentoPortafolio")
    private List<MedicamentoPortafolioSede> medicamentoPortafolioSedes;

	public CategoriaMedicamentoPortafolioSede() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public CategoriaMedicamento getCategoriaMedicamento() {
		return categoriaMedicamento;
	}

	public void setCategoriaMedicamento(
			CategoriaMedicamento categoriaMedicamento) {
		this.categoriaMedicamento = categoriaMedicamento;
	}

	public PortafolioSede getPortafolioSede() {
		return portafolioSede;
	}

	public void setPortafolioSede(PortafolioSede portafolioSede) {
		this.portafolioSede = portafolioSede;
	}

	public Boolean getHabilitado() {
		return habilitado;
	}

	public void setHabilitado(Boolean habilitado) {
		this.habilitado = habilitado;
	}

	public List<MedicamentoPortafolioSede> getMedicamentoPortafolioSedes() {
        return medicamentoPortafolioSedes;
    }

    public void setMedicamentoPortafolioSedes(
            List<MedicamentoPortafolioSede> medicamentoPortafolioSedes) {
        this.medicamentoPortafolioSedes = medicamentoPortafolioSedes;
    }

}