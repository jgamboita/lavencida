package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteMedicamentoDto;
import com.conexia.contractual.model.maestros.Medicamento;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * The persistent class for the sede_negociacion_paquete_medicamento database
 * table.
 *
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "SedeNegociacionPaqueteMedicamento.findMedicamentosBySedeNegociacionPaquete",
            query = "select new com.conexia.contratacion.commons.dto.maestros.MedicamentosDto("
            + "m.atc, m.cums, m.principioActivo, m.concentracion, m.formaFarmaceutica, m.titularRegistroSanitario, m.viaAdministracion, snpm.cantidad  "
            + ") "
            + "from SedeNegociacionPaqueteMedicamento snpm "
            + "join snpm.medicamento m "
            + "where snpm.sedeNegociacionPaquete.id = :sedeNegociacionPaqueteId "),
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "SedeNegociacionPaqueteMedicamento.findMedicamentoPaqueteDetalleNegociacion",
			query = "SELECT m.id as medicamentoId,snpm.id as sedePaqueteMedicamentoId,m.atc,m.codigo, "
			+ "m.principio_activo,m.concentracion,m.forma_farmaceutica,m.titular_registro, "
			+ "m.via_administracion,snpm.cantidad_minima,snpm.cantidad_maxima,snpm.costo_unitario, "
			+ "snpm.observacion,snpm.ingreso_aplica,snpm.ingreso_cantidad, "
			+ "snpm.frecuencia_unidad,snpm.frecuencia_cantidad, m.estado_medicamento_id "
			+ " FROM contratacion.sedes_negociacion sn"
			+ " JOIN contratacion.sede_negociacion_paquete snpq  on snpq.sede_negociacion_id = sn.id"
			+ " JOIN contratacion.sede_negociacion_paquete_medicamento  snpm on snpm.sede_negociacion_paquete_id = snpq.id"
			+ " JOIN maestros.medicamento m on snpm.medicamento_id = m.id "
			+ " LEFT JOIN contratacion.medicamento_portafolio mp on mp.medicamento_id = m.id and mp.portafolio_id = snpq.paquete_id "
			+ " LEFT JOIN ( "
			+ "		SELECT mp.medicamento_id, mp.portafolio_id from contratacion.paquete_portafolio pp "
			+ "		JOIN contratacion.portafolio p on p.id = pp.portafolio_id "
			+ "		JOIN contratacion.medicamento_portafolio mp ON mp.portafolio_id = p.id "
			+ "		AND EXISTS (select  null from contratacion.portafolio por where por.id = p.portafolio_padre_id and eps_id is not null) "
			+ "		where codigo = :codigoPaquete "
			+ "		) medicamentoBasico ON  medicamentoBasico.medicamento_id = mp.medicamento_id "
			+ " WHERE sn.negociacion_id = :negociacionId "
			+ " AND snpq.paquete_id= :paqueteId "
			+ " AND NOT EXISTS(SELECT NULL FROM contratacion.medicamento_portafolio "
			+ "					WHERE medicamento_id = medicamentoBasico.medicamento_id and portafolio_id = medicamentoBasico.portafolio_id ) "
			+ " GROUP BY 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17" , resultSetMapping = "SedeNegociacionPaqueteMedicamento.medicamentosPaqueteNegociacionMapping"
			),
	@NamedNativeQuery(name = "SedeNegociacionPaqueteMedicamento.insertMedicamentosSedeNegociacinoPaquete",
			query = "INSERT INTO contratacion.sede_negociacion_paquete_medicamento(medicamento_id, sede_negociacion_paquete_id, cantidad_minima, "
			+ "cantidad_maxima, costo_unitario, costo_total, observacion, ingreso_aplica, "
			+ "ingreso_cantidad, frecuencia_unidad, frecuencia_cantidad) "
			+ "SELECT m.id,snp.id,mp.cantidad_minima,  mp.cantidad_maxima, "
			+ "COALESCE(mp.costo_unitario, m.valor_referente) as costo_unitario, "
			+ "COALESCE(mp.costo_total, m.valor_referente * mp.cantidad) as costo_total, "
			+ "mp.observacion,mp.ingreso_aplica,mp.ingreso_cantidad,mp.frecuencia_unidad,mp.frecuencia_cantidad "
			+ "FROM contratacion.sede_negociacion_paquete  snp "
			+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
			+ "JOIN contratacion.medicamento_portafolio mp  ON snp.paquete_id = mp.portafolio_id "
			+ "JOIN maestros.medicamento m ON mp.medicamento_id = m.id AND m.estado_medicamento_id = 1 "
			+ "JOIN contratacion.paquete_portafolio pqp ON mp.portafolio_id = pqp.portafolio_id  "
			+ "JOIN contratacion.portafolio p ON pqp.portafolio_id = p.id  "
			+ "WHERE sn.id = :sedeNegociacionId"
			),
	@NamedNativeQuery(name = "SedeNegociacionPaqueteMedicamento.deleteMedicamentoSedeNegociacion",
			query = "DELETE from contratacion.sede_negociacion_paquete_medicamento WHERE id IN ( "
			+ "SELECT snpm.id FROM contratacion.sede_negociacion_paquete_medicamento snpm "
			+ "JOIN contratacion.sede_negociacion_paquete snp ON snpm.sede_negociacion_paquete_id = snp.id "
			+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
			+ "WHERE sn.negociacion_id = :negociacionId AND snp.paquete_id = :paqueteId AND snpm.medicamento_id = :medicamentoId) "
			),
	@NamedNativeQuery(name = "SedeNegociacionPaqueteMedicamento.deleteAllMedicamentosSedeNegociacion", query = "DELETE from contratacion.sede_negociacion_paquete_medicamento WHERE id IN ( "
				+ "SELECT snpm.id FROM contratacion.sede_negociacion_paquete_medicamento snpm "
				+ "JOIN contratacion.sede_negociacion_paquete snp ON snpm.sede_negociacion_paquete_id = snp.id "
				+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
				+ "WHERE sn.negociacion_id = :negociacionId AND snp.paquete_id = :paqueteId) "),
	@NamedNativeQuery(name = "SedeNegociacionPaqueteMedicamento.insertMedicamentoSedeNegociacion",
			query = "INSERT INTO  contratacion.sede_negociacion_paquete_medicamento(medicamento_id,sede_negociacion_paquete_id)  "
			+ "SELECT DISTINCT m.id, snp.id "
			+ "FROM contratacion.sede_negociacion_paquete snp  "
			+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id  "
			+ "CROSS JOIN maestros.medicamento m "
			+ "LEFT JOIN contratacion.sede_negociacion_paquete_medicamento snpm ON  snpm.sede_negociacion_paquete_id = snp.id and snpm.medicamento_id = m.id "
			+ "WHERE sn.negociacion_id = :negociacionId AND snp.paquete_id = :paqueteId AND m.id = :medicamentoId "
			+ "and snpm.id is null")

})

@SqlResultSetMappings({
	@SqlResultSetMapping(name = "SedeNegociacionPaqueteMedicamento.medicamentosPaqueteNegociacionMapping",
			classes = @ConstructorResult(targetClass = SedeNegociacionPaqueteMedicamentoDto.class,
			columns = {
					@ColumnResult(name = "medicamentoId", type = Integer.class),
					@ColumnResult(name = "sedePaqueteMedicamentoId", type = Integer.class),
					@ColumnResult(name = "atc", type = String.class),
					@ColumnResult(name = "codigo", type = String.class),
					@ColumnResult(name = "principio_activo", type = String.class),
					@ColumnResult(name = "concentracion", type = String.class),
					@ColumnResult(name = "forma_farmaceutica", type = String.class),
					@ColumnResult(name = "titular_registro", type = String.class),
					@ColumnResult(name = "via_administracion", type = String.class),
					@ColumnResult(name = "cantidad_minima", type = Integer.class),
					@ColumnResult(name = "cantidad_maxima", type = Integer.class),
					@ColumnResult(name = "costo_unitario", type = BigDecimal.class),
					@ColumnResult(name = "observacion", type = String.class),
					@ColumnResult(name = "ingreso_aplica", type = String.class),
					@ColumnResult(name = "ingreso_cantidad", type = Double.class),
					@ColumnResult(name = "frecuencia_unidad", type = String.class),
					@ColumnResult(name = "frecuencia_cantidad", type = Double.class),
                                        @ColumnResult(name = "estado_medicamento_id", type = Integer.class)
			})),
})
@Table(schema = "contratacion", name = "sede_negociacion_paquete_medicamento")
public class SedeNegociacionPaqueteMedicamento implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicamento_id")
    private Medicamento medicamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_negociacion_paquete_id")
    private SedeNegociacionPaquete sedeNegociacionPaquete;

    @Column(name="cantidad")
    private Integer cantidad;

    @Column(name = "cantidad_minima")
    private Integer cantidadMinima;

    @Column(name = "cantidad_maxima")
    private Integer cantidadMaxima;

    @Column(name = "costo_unitario")
    private BigDecimal costoUnitario;

    @Column(name = "observacion")
    private String observacion;

    @Column(name = "ingreso_aplica")
    private String ingresoAplica;

    @Column(name = "ingreso_cantidad")
    private String ingresoCantidad;

    @Column(name = "frecuencia_unidad")
    private String frecuenciaUnidad;

    @Column(name = "frecuencia_cantidad")
    private String frecuenciaCantidad;


    public SedeNegociacionPaqueteMedicamento() {
    }

    @Override
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SedeNegociacionPaquete getSedeNegociacionPaquete() {
        return sedeNegociacionPaquete;
    }

    public void setSedeNegociacionPaquete(
            SedeNegociacionPaquete sedeNegociacionPaquete) {
        this.sedeNegociacionPaquete = sedeNegociacionPaquete;
    }

	public Medicamento getMedicamento() {
		return medicamento;
	}

	public void setMedicamento(Medicamento medicamento) {
		this.medicamento = medicamento;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public Integer getCantidadMinima() {
		return cantidadMinima;
	}

	public void setCantidadMinima(Integer cantidadMinima) {
		this.cantidadMinima = cantidadMinima;
	}

	public Integer getCantidadMaxima() {
		return cantidadMaxima;
	}

	public void setCantidadMaxima(Integer cantidadMaxima) {
		this.cantidadMaxima = cantidadMaxima;
	}

	public BigDecimal getCostoUnitario() {
		return costoUnitario;
	}

	public void setCostoUnitario(BigDecimal costoUnitario) {
		this.costoUnitario = costoUnitario;
	}

	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		this.observacion = observacion;
	}

	public String getIngresoAplica() {
		return ingresoAplica;
	}

	public void setIngresoAplica(String ingresoAplica) {
		this.ingresoAplica = ingresoAplica;
	}

	public String getIngresoCantidad() {
		return ingresoCantidad;
	}

	public void setIngresoCantidad(String ingresoCantidad) {
		this.ingresoCantidad = ingresoCantidad;
	}

	public String getFrecuenciaUnidad() {
		return frecuenciaUnidad;
	}

	public void setFrecuenciaUnidad(String frecuenciaUnidad) {
		this.frecuenciaUnidad = frecuenciaUnidad;
	}

	public String getFrecuenciaCantidad() {
		return frecuenciaCantidad;
	}

	public void setFrecuenciaCantidad(String frecuenciaCantidad) {
		this.frecuenciaCantidad = frecuenciaCantidad;
	}

}
