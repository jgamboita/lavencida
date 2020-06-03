package com.conexia.contractual.model.contratacion.portafolio;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.MedicamentoPortafolioDto;
import com.conexia.contratacion.commons.dto.capita.MedicamentoPortafolioReporteDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteMedicamentoDto;
import com.conexia.contractual.model.maestros.Medicamento;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "medicamento_portafolio", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name="MedicamentoPortafolio.findDtoBySedeIdAndCategoriaId",
			query="SELECT NEW com.conexia.contratacion.commons.dto.MedicamentoPortafolioDto"
			+ "(mp.id, m.atc, m.cums, m.principioActivo, m.unidadMedida, m.concentracion, m.formaFarmaceutica, mp.codigoInterno, mp.valor) "
			+ "FROM MedicamentoPortafolio mp JOIN mp.medicamento m JOIN mp.portafolio p JOIN p.sedePrestador sp "
			+ "JOIN m.categoriaMedicamento cm WHERE cm.id = :categoriaId AND sp.id = :sedeId"),
	@NamedQuery(name="MedicamentoPortafolio.findDtoByPortafolioId",
			query="SELECT new com.conexia.contratacion.commons.dto.MedicamentoPortafolioDto("
					+ "mp.id, mp.codigoInterno, mp.cantidad, mp.valor, mp.etiqueta, mp.portafolio.id, "
					+ "m.id, m.cums, m.atc, m.unidadMedida, m.concentracion, m.formaFarmaceutica, m.principioActivo, "
					+ "m.viaAdministracion, m.titularRegistroSanitario) "
					+ "FROM MedicamentoPortafolio mp JOIN mp.medicamento m WHERE mp.portafolio.id = :portafolioId")
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "MedicamentoPortafolio.eliminarMedicamentoPortafolio" ,
			query = "DELETE from contratacion.medicamento_portafolio "
					+ " WHERE id in (SELECT por.id from contratacion.medicamento_portafolio por "
					+ " 			JOIN contratacion.sede_prestador sp on por.portafolio_id = sp.portafolio_id "
					+ "				AND sp.prestador_id =:prestadorId)"),
	@NamedNativeQuery(name = "MedicamentoPortafolio.insertarMedicamentoPortafolio" ,
	query = "INSERT INTO contratacion.medicamento_portafolio (portafolio_id, medicamento_id,valor,es_capita)"
			+ "  SELECT sp.portafolio_id, med.id, round(:valorNegociado,2), false"
			+ "  FROM maestros.medicamento med "
			+ "  JOIN contratacion.sede_prestador sp on sp.id =:sedePrestadorId and sp.prestador_id =:prestadorId "
			+ "  WHERE med.id =:medicamentoId "
			+ "  AND med.estado_medicamento_id = 1 "
			+ "  AND NOT EXISTS (SELECT NULL from contratacion.medicamento_portafolio proc "
			+ "					  WHERE proc.portafolio_id = sp.portafolio_id "
			+ "					  AND   proc.medicamento_id = med.id) "),
	@NamedNativeQuery(name = "MedicamentoPortafolio.findMedicamentosPaquetePrestador",
			query = "SELECT DISTINCT mp.id as medicamentoPortafolioId ,m.id as medicamentoId,m.atc,m.codigo,m.principio_activo,  "
			+ "m.concentracion,m.forma_farmaceutica,m.titular_registro,  "
			+ "m.via_administracion,coalesce(snpm.cantidad_minima, mp.cantidad_minima) as cantidad_minima ,"
			+ "coalesce(snpm.cantidad_maxima, mp.cantidad_maxima) as cantidad_maxima, "
			+ "coalesce(snpm.observacion,mp.observacion) as observacion, "
			+ "coalesce(snpm.ingreso_aplica,mp.ingreso_aplica) as ingreso_aplica, coalesce(snpm.ingreso_cantidad, mp.ingreso_cantidad) as ingreso_cantidad "
			+ ",coalesce(snpm.frecuencia_unidad,mp.frecuencia_unidad) as frecuencia_unidad,coalesce(snpm.frecuencia_cantidad, mp.frecuencia_cantidad)  as frecuencia_cantidad, "
			+ "case when (snpm.id is null) then false else true END en_negociacion "
			+ "FROM contratacion.medicamento_portafolio mp "
			+ "JOIN maestros.medicamento m ON mp.medicamento_id = m.id "
			+ "JOIN contratacion.paquete_portafolio pqp ON mp.portafolio_id = pqp.portafolio_id "
			+ "JOIN contratacion.portafolio p ON pqp.portafolio_id = p.id "
			+ "JOIN contratacion.sede_prestador sp ON sp.portafolio_id = p.portafolio_padre_id "
			+ "JOIN contratacion.prestador pr ON sp.prestador_id = pr.id "
			+ "JOIN contratacion.sede_negociacion_paquete snp ON mp.portafolio_id = snp.paquete_id "
			+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
			+ "LEFT JOIN contratacion.sede_negociacion_paquete_medicamento snpm ON snpm.sede_negociacion_paquete_id = snp.id AND snpm.medicamento_id = mp.medicamento_id "
			+ "LEFT JOIN ( "
			+ "SELECT mp.medicamento_id, mp.portafolio_id from contratacion.paquete_portafolio pp "
			+ "JOIN contratacion.portafolio p on p.id = pp.portafolio_id "
			+ "JOIN contratacion.medicamento_portafolio mp ON mp.portafolio_id = p.id "
			+ "AND EXISTS (SELECT  NULL FROM contratacion.portafolio por where por.id = p.portafolio_padre_id and eps_id is not null) "
			+ "where codigo = :codigoPaquete "
			+ ") medicamentoBasico ON  medicamentoBasico.medicamento_id = mp.medicamento_id "
			+ "WHERE p.id = :paqueteId AND pr.id = :prestadorId and sn.negociacion_id = :negociacionId AND m.estado_medicamento_id = 1 "
			+ "AND EXISTS(SELECT NULL FROM contratacion.medicamento_portafolio WHERE "
			+ " 	medicamento_id = medicamentoBasico.medicamento_id and portafolio_id = medicamentoBasico.portafolio_id )"
			+ " ORDER BY m.id DESC"
			, resultSetMapping = "MedicamentoPortafolio.medicamentosPaquetePrestadorMapping"
			),
        @NamedNativeQuery(name = "MedicamentoPaquete.findMedicamentosPaquetePrestador",
			query = "SELECT DISTINCT mp.id as medicamentoPortafolioId ,m.id as medicamentoId,m.atc,m.codigo,m.principio_activo,  "
			+ "m.concentracion,m.forma_farmaceutica,m.titular_registro,  "
			+ "m.via_administracion,coalesce(snpm.cantidad_minima, mp.cantidad_minima) as cantidad_minima ,"
			+ "coalesce(snpm.cantidad_maxima, mp.cantidad_maxima) as cantidad_maxima, "
			+ "coalesce(snpm.observacion,mp.observacion) as observacion, "
			+ "coalesce(snpm.ingreso_aplica,mp.ingreso_aplica) as ingreso_aplica, coalesce(snpm.ingreso_cantidad, mp.ingreso_cantidad) as ingreso_cantidad "
			+ ",coalesce(snpm.frecuencia_unidad,mp.frecuencia_unidad) as frecuencia_unidad,coalesce(snpm.frecuencia_cantidad, mp.frecuencia_cantidad)  as frecuencia_cantidad, "
			+ "case when (snpm.id is null) then false else true END en_negociacion, m.estado_medicamento_id "
			+ "FROM contratacion.medicamento_portafolio mp "
			+ "JOIN maestros.medicamento m ON mp.medicamento_id = m.id "
			+ "JOIN contratacion.paquete_portafolio pqp ON mp.portafolio_id = pqp.portafolio_id "
			+ "JOIN contratacion.portafolio p ON pqp.portafolio_id = p.id "
			+ "JOIN contratacion.sede_prestador sp ON sp.portafolio_id = p.portafolio_padre_id "
			+ "JOIN contratacion.prestador pr ON sp.prestador_id = pr.id "
			+ "JOIN contratacion.sede_negociacion_paquete snp ON mp.portafolio_id = snp.paquete_id "
			+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
			+ "LEFT JOIN contratacion.sede_negociacion_paquete_medicamento snpm ON snpm.sede_negociacion_paquete_id = snp.id AND snpm.medicamento_id = mp.medicamento_id "
			+ "LEFT JOIN ( "
			+ "SELECT mp.medicamento_id, mp.portafolio_id from contratacion.paquete_portafolio pp "
			+ "JOIN contratacion.portafolio p on p.id = pp.portafolio_id "
			+ "JOIN contratacion.medicamento_portafolio mp ON mp.portafolio_id = p.id "
			+ "AND EXISTS (SELECT  NULL FROM contratacion.portafolio por where por.id = p.portafolio_padre_id and eps_id is not null) "
			+ "where codigo = :codigoPaquete "
			+ ") medicamentoBasico ON  medicamentoBasico.medicamento_id = mp.medicamento_id "
			+ "WHERE p.id = :paqueteId AND pr.id = :prestadorId and sn.negociacion_id = :negociacionId "
			+ "AND EXISTS(SELECT NULL FROM contratacion.medicamento_portafolio WHERE "
			+ " 	medicamento_id = medicamentoBasico.medicamento_id and portafolio_id = medicamentoBasico.portafolio_id )"
			+ " ORDER BY m.id DESC"
			, resultSetMapping = "MedicamentoPaquete.medicamentosPaquetePrestadorMapping"
			)
})
@SqlResultSetMappings({
	@SqlResultSetMapping(
		name = "MedicamentoPortafolio.medicamentoPortafolioReporteDto",
		classes = @ConstructorResult(
			targetClass = MedicamentoPortafolioReporteDto.class,
			columns = {
				@ColumnResult(name = "codigo", type = String.class),
				@ColumnResult(name = "principio_activo", type = String.class),
				@ColumnResult(name = "concentracion", type = String.class),
				@ColumnResult(name = "titular_registro", type = String.class),
				@ColumnResult(name = "registro_sanitario", type = String.class),
				@ColumnResult(name = "descripcion_comercial", type = String.class),
				@ColumnResult(name = "atc", type = String.class),
				@ColumnResult(name = "descripcion_atc", type = String.class),
				@ColumnResult(name = "forma_farmaceutica", type = String.class),
				@ColumnResult(name = "capitado", type = String.class),
				@ColumnResult(name = "codigo_ips", type = String.class),
				@ColumnResult(name = "valor", type = String.class)
			}
		)
	),
	@SqlResultSetMapping(name = "MedicamentoPortafolio.medicamentosPaquetePrestadorMapping",
	classes = @ConstructorResult(
			targetClass = MedicamentoPortafolioDto.class,
	columns = {
				@ColumnResult(name = "medicamentoPortafolioId", type = Integer.class),
				@ColumnResult(name = "medicamentoId", type = Integer.class),
				@ColumnResult(name = "atc", type = String.class),
				@ColumnResult(name = "codigo", type = String.class),
				@ColumnResult(name = "principio_activo", type = String.class),
				@ColumnResult(name = "concentracion", type = String.class),
				@ColumnResult(name = "forma_farmaceutica", type = String.class),
				@ColumnResult(name = "titular_registro", type = String.class),
				@ColumnResult(name = "via_administracion", type = String.class),
				@ColumnResult(name = "cantidad_minima", type = Integer.class),
				@ColumnResult(name = "cantidad_maxima", type = Integer.class),
				@ColumnResult(name = "observacion", type = String.class),
				@ColumnResult(name = "ingreso_aplica", type = String.class),
				@ColumnResult(name = "ingreso_cantidad", type = Double.class),
				@ColumnResult(name = "frecuencia_unidad", type = String.class),
				@ColumnResult(name = "frecuencia_cantidad", type = Double.class),
				@ColumnResult(name = "en_negociacion", type = Boolean.class)
			}
		)
	),
        @SqlResultSetMapping(name = "MedicamentoPaquete.medicamentosPaquetePrestadorMapping",
	classes = @ConstructorResult(
			targetClass = SedeNegociacionPaqueteMedicamentoDto.class,
	columns = {
				@ColumnResult(name = "medicamentoPortafolioId", type = Integer.class),
				@ColumnResult(name = "medicamentoId", type = Integer.class),
				@ColumnResult(name = "atc", type = String.class),
				@ColumnResult(name = "codigo", type = String.class),
				@ColumnResult(name = "principio_activo", type = String.class),
				@ColumnResult(name = "concentracion", type = String.class),
				@ColumnResult(name = "forma_farmaceutica", type = String.class),
				@ColumnResult(name = "titular_registro", type = String.class),
				@ColumnResult(name = "via_administracion", type = String.class),
				@ColumnResult(name = "cantidad_minima", type = Integer.class),
				@ColumnResult(name = "cantidad_maxima", type = Integer.class),
				@ColumnResult(name = "observacion", type = String.class),
				@ColumnResult(name = "ingreso_aplica", type = String.class),
				@ColumnResult(name = "ingreso_cantidad", type = Double.class),
				@ColumnResult(name = "frecuencia_unidad", type = String.class),
				@ColumnResult(name = "frecuencia_cantidad", type = Double.class),
				@ColumnResult(name = "en_negociacion", type = Boolean.class),
                                @ColumnResult(name = "estado_medicamento_id", type = Integer.class)
			}
		)
	),

})
public class MedicamentoPortafolio implements Identifiable<Long>, Serializable{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "es_capita")
	private Boolean capita;

	@Column(name = "codigo_interno", length = 20)
	private String codigoInterno;

	@Column(name = "valor")
	private Double valor;


	@Column(name = "cantidad")
	private Integer cantidad;

	@Column(name = "etiqueta")
	private String etiqueta;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "medicamento_id")
	private Medicamento medicamento;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "portafolio_id")
	private Portafolio portafolio;

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

    @Column(name = "is_default")
    private Boolean isDefault;

	public MedicamentoPortafolio() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean getCapita() {
		return capita;
	}

	public void setCapita(Boolean capita) {
		this.capita = capita;
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

	public Portafolio getPortafolio() {
		return this.portafolio;
	}

	public void setPortafolio(Portafolio portafolio) {
		this.portafolio = portafolio;
	}

	public Double getValor() {
		return valor;
	}

	public void setValor(Double valor) {
		this.valor = valor;
	}

	public Integer getCantidad() {
		return cantidad;
	}

	public void setCantidad(Integer cantidad) {
		this.cantidad = cantidad;
	}

	public String getEtiqueta() {
		return etiqueta;
	}

	public void setEtiqueta(String etiqueta) {
		this.etiqueta = etiqueta;
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

	public Boolean getIsDefault() {
		return isDefault;
	}

	public void setIsDefault(Boolean isDefault) {
		this.isDefault = isDefault;
	}


}