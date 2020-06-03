package com.conexia.contractual.model.contratacion.portafolio;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.ModalidadEnum;
import com.conexia.contratacion.commons.dto.capita.ProcedimientoPortafolioReporteDto;
import com.conexia.contractual.model.contratacion.GrupoServicio;
import com.conexia.contractual.model.contratacion.TipoTarifario;
import com.conexia.contractual.model.maestros.Procedimientos;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entidad procedimiento portafolio.
 * 
 * @author mcastro
 * @date 23/05/2014
 */

@Entity
@Table(name = "procedimiento_portafolio", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name = "ProcedimientoPortafolio.findTarifariosByGrupoServicioId",
	  		  query = "SELECT NEW com.conexia.contratacion.commons.dto.ProcedimientoPortafolioDto("
	  		  		+ "pp.id, mp.codigo, mp.codigoEmssanar, pp.codigoInterno, mp.id, mp.descripcion, p.complejidad, p.esPos, "
	  		  		+ "pp.modalidad, pp.tarifaDiferencial, t.id, t.descripcion, pp.porcentajeNegociacion, pp.valor) "
	  		  		+ "FROM ProcedimientoPortafolio pp "
	  		  		+ "JOIN pp.procedimiento p "
	  		  		+ "JOIN p.procedimiento mp "
	  		  		+ "LEFT JOIN pp.tarifario t "
	  		  		+ "WHERE pp.grupoServicio.id = :grupoServicioId ORDER BY pp.tarifario desc")
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "ProcedimientoPortafolio.eliminarProcedimientoPortafolio" ,
			query = "DELETE from contratacion.procedimiento_portafolio"
					+ " WHERE id in (SELECT por.id from contratacion.procedimiento_portafolio por "
					+ " JOIN contratacion.grupo_servicio grupo on grupo.id = por.grupo_servicio_id "
					+ " JOIN contratacion.sede_prestador sp on grupo.portafolio_id = sp.portafolio_id"
					+ " 	AND sp.prestador_id =:prestadorId)"),
	@NamedNativeQuery(name = "ProcedimientoPortafolio.insertarProcedimientoPortafolio" ,
	query = "INSERT INTO contratacion.procedimiento_portafolio "
			+ "	(procedimiento_id,tarifario_id,porcentaje_negociacion,valor,grupo_servicio_id,tarifa_diferencial,fecha_insert,es_capita) "
			+ " SELECT ps.id, :tarifarioId, :porcentajeNegociado ,round( :valorNegociado), gs.id, false, current_timestamp, false "
			+ " FROM contratacion.grupo_servicio gs "
			+ " JOIN maestros.procedimiento_servicio ps on gs.servicio_salud_id = ps.servicio_id "
			+ " JOIN contratacion.sede_prestador sp on sp.portafolio_id = gs.portafolio_id "
			+ " WHERE ps.servicio_id = :servicioId "
			+ " AND   ps.id = :procedimientoId "
			+ " AND   sp.prestador_id = :prestadorId "
			+ " AND   sp.id = :sedePrestadorId "
			+ " AND NOT EXISTS (SELECT NULL from contratacion.procedimiento_portafolio proc"
			+ "					WHERE proc.grupo_servicio_id = gs.id"
			+ "					AND procedimiento_id = ps.id)")
})
@SqlResultSetMappings({
    @SqlResultSetMapping(
		name = "ProcedimientoPortafolio.procedimientoPortafolioReporteDto",
		classes = @ConstructorResult(
			targetClass = ProcedimientoPortafolioReporteDto.class,
			columns = {
				@ColumnResult(name = "servicio", type = String.class),
				@ColumnResult(name = "codigo", type = String.class),
				@ColumnResult(name = "codigo_emssanar", type = String.class),
				@ColumnResult(name = "codigo_ips", type = String.class),
				@ColumnResult(name = "descripcion", type = String.class),
				@ColumnResult(name = "modalidad", type = String.class),
				@ColumnResult(name = "tarifario", type = String.class),
				@ColumnResult(name = "porcentaje_negociacion", type = String.class),
				@ColumnResult(name = "valor", type = String.class),
				@ColumnResult(name = "tarifarios_procedimiento", type = String.class),
				@ColumnResult(name = "eliminar", type = String.class),
			}
		)
	)
})

public class ProcedimientoPortafolio implements Identifiable<Long>, Serializable{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "codigo_interno", length = 50)
	private String codigoInterno;

	@Enumerated(EnumType.STRING)
	@Column(name = "modalidad")
	private ModalidadEnum modalidad;

	@Column(name = "porcentaje_negociacion", precision = 10, scale = 2)
	private BigDecimal porcentajeNegociacion;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "procedimiento_id")
	private Procedimientos procedimiento;

	@Column(name = "tarifa_diferencial")
	private Boolean tarifaDiferencial;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "tarifario_id", nullable = true)
	private TipoTarifario tarifario;

	@Column(name = "valor")
	private Integer valor;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "grupo_servicio_id")
	private GrupoServicio grupoServicio;

	public ProcedimientoPortafolio() {
	}
		public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCodigoInterno() {
		return this.codigoInterno;
	}

	public void setCodigoInterno(String codigoInterno) {
		this.codigoInterno = codigoInterno;
	}

	public ModalidadEnum getModalidad() {
		return modalidad;
	}
	public void setModalidad(ModalidadEnum modalidad) {
		this.modalidad = modalidad;
	}
	public BigDecimal getPorcentajeNegociacion() {
		return this.porcentajeNegociacion;
	}

	public void setPorcentajeNegociacion(BigDecimal porcentajeNegociacion) {
		this.porcentajeNegociacion = porcentajeNegociacion;
	}

	public Procedimientos getProcedimiento() {
		return procedimiento;
	}

	public void setProcedimiento(Procedimientos procedimiento) {
		this.procedimiento = procedimiento;
	}

	public Boolean getTarifaDiferencial() {
		return tarifaDiferencial;
	}

	public void setTarifaDiferencial(Boolean tarifaDiferencial) {
		this.tarifaDiferencial = tarifaDiferencial;
	}

	public TipoTarifario getTarifario() {
		return tarifario;
	}

	public void setTarifario(TipoTarifario tarifario) {
		this.tarifario = tarifario;
	}

	public Integer getValor() {
		return this.valor;
	}

	public void setValor(Integer valor) {
		this.valor = valor;
	}

	public GrupoServicio getGrupoServicio() {
		return this.grupoServicio;
	}

	public void setGrupoServicio(GrupoServicio grupoServicio) {
		this.grupoServicio = grupoServicio;
	}
}