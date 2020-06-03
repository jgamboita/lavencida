package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionRiaRangoPoblacionDto;
import com.conexia.contractual.model.contratacion.GrupoEtario;
import com.conexia.contractual.model.maestros.RangoPoblacion;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(schema = "contratacion", name = "negociacion_ria_rango_poblacion")
@NamedQueries({
	@NamedQuery(name = "NegociacionRiaRangoPoblacion.updateValoresNegociadosId",
			query = "UPDATE NegociacionRiaRangoPoblacion set "
				+ "porcentajeNegociado = :porcentajeNegociado ,"
				+ "valorNegociado = :valorNegociado "
				+ "WHERE id = :negociacionRiaRangoPoblacionId"),
	@NamedQuery(name ="NegociacionRiaRangoPoblacion.sumPorcentajePyp",
			query = "SELECT SUM(nrp.porcentajeNegociado) FROM NegociacionRiaRangoPoblacion nrp "
				+ "JOIN nrp.negociacionRia nr "
				+ "WHERE nr.negociacion.id = :negociacionId AND (nr.ria.id NOT IN (3)) AND nr.negociado = TRUE "),
	@NamedQuery(name ="NegociacionRiaRangoPoblacion.sumPorcentajeRecuperacion",
	query = "SELECT SUM(nrp.porcentajeNegociado) FROM NegociacionRiaRangoPoblacion nrp "
				+ "JOIN nrp.negociacionRia nr "
				+ "WHERE nr.negociacion.id = :negociacionId AND nr.ria.id = 3 AND nr.negociado = TRUE "),
	@NamedQuery(name = "NegociacionRiaRangoPoblacion.updateValoresDestoUpcNegociada",
			query = "UPDATE NegociacionRiaRangoPoblacion set "
				+ "porcentaje_descuento = :porcentajeDescuento, "
				+ "upc_negociada = :upcNegociada "
				+ "WHERE id = :negociacionRiaRangoPoblacionId"),
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "NegociacionRiaRangoPoblacion.borrarPorNoNegociacionIds",
			query = "DELETE FROM contratacion.negociacion_ria_rango_poblacion " +
					" USING contratacion.negociacion_ria nr " +
					" WHERE nr.id = negociacion_ria_id " +
					" AND nr.negociacion_id = :negociacionId " +
					" AND nr.id NOT IN (:ids)"),
	@NamedNativeQuery(name = "NegociacionRiaRangoPoblacion.borrarPorNegociacionId",
			query = "DELETE FROM contratacion.negociacion_ria_rango_poblacion " +
					" USING contratacion.negociacion_ria nr " +
					" WHERE nr.id = negociacion_ria_id " +
					" AND nr.negociacion_id = :negociacionId "),
	@NamedNativeQuery(name = "NegociacionRiaRangoPoblacion.findRiasByNegociacionId",
			query = "SELECT nrrp.id, nrrp.poblacion, nr.id negociacion_ria_id, r.id ria_id, r.codigo, r.descripcion, nr.negociado, "
					+ "		rp.id rango_poblacion_id, rp.descripcion rango_poblacion_descripcion, rp.edad_desde, rp.edad_hasta, "
					+ "		(COALESCE(snp.peso_porcentual_referente,0.0) + COALESCE(snm.peso_porcentual_referente,0.0)) peso_porcentual_referente,"
					+ "		(COALESCE(snp.valor_referente,0) + COALESCE(snm.valor_referente,0)) valor_referente"
					+ "		,nrrp.upc,nrrp.porcentaje_descuento,nrrp.upc_negociada"
					+ "		,(COALESCE(snp.porcentaje_negociado,0.0)+  COALESCE(snm.porcentaje_negociado,0.0)) peso_porcentual_negociado,"
					+ "		(COALESCE(snp.valor_negociado,0) + COALESCE(snm.valor_negociado,0))  valor_negociado,"
					+ "		(nrrp.poblacion * (COALESCE(snp.valor_negociado,0)+  COALESCE(snm.valor_negociado,0)) ) as valor_total"
					+ " FROM contratacion.negociacion_ria_rango_poblacion nrrp "
					+ " JOIN contratacion.negociacion_ria nr ON nrrp.negociacion_ria_id = nr.id"
					+ " JOIN maestros.ria r ON r.id = nr.ria_id "
					+ " JOIN maestros.rango_poblacion rp ON rp.id = nrrp.rango_poblacion_id"
					+ " LEFT JOIN (SELECT snp.negociacion_ria_rango_poblacion_id,"
					+ " 			sum (snp.peso_porcentual_referente)peso_porcentual_referente, sum(snp.valor_referente)valor_referente,"
					+ "				sum(snp.porcentaje_negociado )porcentaje_negociado, sum(snp.valor_negociado)valor_negociado"
					+ "			   FROM (SELECT snp.procedimiento_id, snp.negociacion_ria_rango_poblacion_id,snp.actividad_id,"
					+ "						snp.peso_porcentual_referente, snp.valor_referente,"
					+ "						snp.valor_negociado, snp.porcentaje_negociado"
					+ "					 FROM contratacion.sede_negociacion_procedimiento snp"
					+ "					 JOIN contratacion.sede_negociacion_servicio sns on sns.id = snp.sede_negociacion_servicio_id"
					+ "					 JOIN contratacion.sedes_negociacion sn on sn.id = sns.sede_negociacion_id "
					+ "					 JOIN contratacion.referente_procedimiento_servicio_ria_capita rps ON rps.procedimiento_servicio_id  = snp.procedimiento_id "
					+ "					 WHERE sn.negociacion_id = :negociacionId "
					+ " 		    	 GROUP BY snp.procedimiento_id, snp.negociacion_ria_rango_poblacion_id,snp.actividad_id,"
					+ "						 snp.peso_porcentual_referente, snp.valor_referente,"
					+ "						 snp.valor_negociado, snp.porcentaje_negociado) snp "
					+ "				GROUP BY snp.negociacion_ria_rango_poblacion_id) snp ON nrrp.id = snp.negociacion_ria_rango_poblacion_id"
					+ " LEFT JOIN (SELECT snm.negociacion_ria_rango_poblacion_id,"
					+ "					sum(snm.peso_porcentual_referente)peso_porcentual_referente, sum(snm.valor_referente)valor_referente,"
					+ "					sum(snm.porcentaje_negociado )porcentaje_negociado, sum(snm.valor_negociado)valor_negociado"
					+ "				FROM (SELECT snm.medicamento_id, snm.negociacion_ria_rango_poblacion_id,snm.actividad_id,"
					+ "							 snm.peso_porcentual_referente, snm.valor_referente,"
					+ "							 snm.valor_negociado, snm.porcentaje_negociado"
					+ "					  FROM contratacion.sede_negociacion_medicamento snm"
					+ "					  JOIN contratacion.sedes_negociacion sn on sn.id = snm.sede_negociacion_id "
					+ "					  JOIN contratacion.referente_medicamento_ria_capita  rmc ON rmc.medicamento_id = snm.medicamento_id "
					+ "					  WHERE sn.negociacion_id = :negociacionId "
					+ "					  GROUP BY snm.medicamento_id, snm.negociacion_ria_rango_poblacion_id,snm.actividad_id,"
					+ "								snm.peso_porcentual_referente, snm.valor_referente,"
					+ "								snm.valor_negociado, snm.porcentaje_negociado)snm"
					+ "				group by snm.negociacion_ria_rango_poblacion_id	) snm ON nrrp.id = snm.negociacion_ria_rango_poblacion_id"
					+ "	 WHERE nr.negociacion_id = :negociacionId"
					+ "	AND nr.negociado = true"
					+ " GROUP BY nrrp.id, nrrp.poblacion, nr.id, r.id, r.codigo, r.descripcion, rp.edad_desde, rp.edad_hasta, rp.id, rp.descripcion,"
					+ " 		snp.peso_porcentual_referente,snm.peso_porcentual_referente, snp.porcentaje_negociado, snm.porcentaje_negociado,"
					+ "			snp.valor_referente, snm.valor_referente, snp.valor_negociado, snm.valor_negociado"
					+ "	ORDER BY rp.id ",
			resultSetMapping = "NegociacionRiaRangoPoblacion.datosPorcentuales"),
	@NamedNativeQuery(name = "NegociacionRiaRangoPoblacion.actualizarRiaRangoPoblacionById",
		query = "UPDATE contratacion.negociacion_ria_rango_poblacion "
				+ " SET poblacion = :poblacion"
				+ " WHERE id = :id"),
	@NamedNativeQuery(name="NegociacionRiaRangoPoblacion.deletePoblacionAsociadaGrupoEtareo", query = "DELETE FROM "
			+ "contratacion.poblacion_ria_grupo_etareo prge USING contratacion.negociacion_ria_rango_poblacion nrrp "
			+ "JOIN contratacion.negociacion_ria nr ON nr.id = nrrp.negociacion_ria_id "
			+ "WHERE prge.negociacion_ria_rango_poblacion_id  = nrrp.id  AND nr.negociacion_id = :negociacionId "),
})
@SqlResultSetMappings({
	@SqlResultSetMapping(name = "NegociacionRiaRangoPoblacion.datosPorcentuales",
			classes = @ConstructorResult(
					targetClass = NegociacionRiaRangoPoblacionDto.class,
					columns = {
						@ColumnResult(name = "id", type = Integer.class),
						@ColumnResult(name = "poblacion", type = Integer.class),
						@ColumnResult(name = "negociacion_ria_id", type = Integer.class),
						@ColumnResult(name = "ria_id", type = Integer.class),
						@ColumnResult(name = "codigo", type = String.class),
						@ColumnResult(name = "descripcion", type = String.class),
						@ColumnResult(name = "negociado", type = Boolean.class),
						@ColumnResult(name = "rango_poblacion_id", type = Integer.class),
						@ColumnResult(name = "rango_poblacion_descripcion", type = String.class),
						@ColumnResult(name = "edad_desde", type = Integer.class),
						@ColumnResult(name = "edad_hasta", type = Integer.class),
						@ColumnResult(name = "peso_porcentual_referente", type = Double.class),
						@ColumnResult(name = "valor_referente", type = Double.class),
						@ColumnResult(name = "upc", type = Double.class),
						@ColumnResult(name = "porcentaje_descuento", type = Double.class),
						@ColumnResult(name = "upc_negociada", type = Double.class),
						@ColumnResult(name = "peso_porcentual_negociado", type = Double.class),
						@ColumnResult(name = "valor_negociado", type = Double.class),
						@ColumnResult(name = "valor_total", type = Double.class),
					}
			)
	)
})
public class NegociacionRiaRangoPoblacion implements Identifiable<Integer>, Serializable {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "negociacion_ria_id")
	private NegociacionRia negociacionRia;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "rango_poblacion_id")
	private RangoPoblacion rangoPoblacion;

	@Column(name = "poblacion")
	private Integer poblacion;

	@Column(name = "porcentaje_negociado")
	private Double porcentajeNegociado;

	@Column(name = "valor_negociado")
	private Double valorNegociado;

	@Column(name = "upc")
	private Double upc;

	@Column(name = "porcentaje_descuento")
	private Double porcentajeDescuento;

	@Column(name="upc_negociada")
	private Double upcNegociada;

	@OneToMany(fetch = FetchType.LAZY)
	@JoinColumn(name="grupo_etario_id")
	private List<GrupoEtario> gruposEtarios;

	@OneToMany(mappedBy = "negociacionRiaRangoPoblacional",fetch = FetchType.LAZY)
	private List<SedeNegociacionMedicamento> sedeNegociacionMedicamentos;

    public NegociacionRiaRangoPoblacion() {}

    public NegociacionRiaRangoPoblacion(Integer id) {
        this.id = id;
    }

    //<editor-fold desc="Getters && Setters">
    public Integer getId() {

		return id;
	}

	public void setId(Integer id) {

		this.id = id;
	}

	public NegociacionRia getNegociacionRia() {

		return negociacionRia;
	}

	public void setNegociacionRia(NegociacionRia negociacionRia) {

		this.negociacionRia = negociacionRia;
	}

	public RangoPoblacion getRangoPoblacion() {

		return rangoPoblacion;
	}

	public void setRangoPoblacion(RangoPoblacion rangoPoblacion) {

		this.rangoPoblacion = rangoPoblacion;
	}

	public Integer getPoblacion() {

		return poblacion;
	}

	public void setPoblacion(Integer poblacion) {

		this.poblacion = poblacion;
	}

	public Double getPorcentajeNegociado() {
		return porcentajeNegociado;
	}

	public void setPorcentajeNegociado(Double porcentajeNegociado) {
		this.porcentajeNegociado = porcentajeNegociado;
	}

	public Double getValorNegociado() {
		return valorNegociado;
	}

	public void setValorNegociado(Double valorNegociado) {
		this.valorNegociado = valorNegociado;
	}

	public Double getUpc() {
		return upc;
	}

	public void setUpc(Double upc) {
		this.upc = upc;
	}

	public Double getPorcentajeDescuento() {
		return porcentajeDescuento;
	}

	public void setPorcentajeDescuento(Double porcentajeDescuento) {
		this.porcentajeDescuento = porcentajeDescuento;
	}

	public Double getUpcNegociada() {
		return upcNegociada;
	}

	public void setUpcNegociada(Double upcNegociada) {
		this.upcNegociada = upcNegociada;
	}

	public List<GrupoEtario> getGruposEtarios() {
		return gruposEtarios;
	}

	public void setGruposEtarios(List<GrupoEtario> gruposEtarios) {
		this.gruposEtarios = gruposEtarios;
	}

    public List<SedeNegociacionMedicamento> getSedeNegociacionMedicamentos() {
        return sedeNegociacionMedicamentos;
    }

    public void setSedeNegociacionMedicamentos(List<SedeNegociacionMedicamento> sedeNegociacionMedicamentos) {
        this.sedeNegociacionMedicamentos = sedeNegociacionMedicamentos;
    }

    //</editor-fold>
}
