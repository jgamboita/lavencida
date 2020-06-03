package com.conexia.contractual.model.contratacion.portafolio;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteInsumoDto;
import com.conexia.contractual.model.maestros.Insumos;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "insumo_portafolio", schema = "contratacion")
@NamedQueries({
	@NamedQuery(name="InsumoPortafolio.findDtoByPortafolioId", query="SELECT new com.conexia.contratacion.commons.dto.InsumoPortafolioDto(ip.id, i.codigo, i.descripcion, '', ci.descripcion, gi.descripcion, ip.valor, ip.cantidad, ip.observacion) FROM InsumoPortafolio ip JOIN ip.insumo i JOIN i.categoriaInsumo ci JOIN i.grupoInsumo gi WHERE ip.portafolio.id = :portafolioId")
})
@NamedNativeQueries({
	@NamedNativeQuery(name = "InsumoPortafolio.findInsumoPaquetePrestador",
			query = "select DISTINCT ip.id as insumoPortafolioId, " +
					"i.id as insumoId, " +
					"i.codigo_emssanar," +
					"i.descripcion," +
					"gi.descripcion as grupoInsumo," +
					"ci.descripcion as categoriaInsumo,  "
				+ "coalesce(snpi.cantidad_minima, ip.cantidad_minima) as cantidad_minima " +
					",coalesce(snpi.cantidad_maxima,ip.cantidad_maxima) as cantidad_maxima, "
				+ "coalesce(snpi.observacion, ip.observacion) as observacion ,"
				+ "coalesce(snpi.ingreso_aplica,ip.ingreso_aplica) as ingreso_aplica " +
					",coalesce(snpi.ingreso_cantidad, ip.ingreso_cantidad) as ingreso_cantidad, "
				+ "coalesce(snpi.frecuencia_unidad, ip.frecuencia_unidad) as frecuencia_unidad " +
					",coalesce(snpi.frecuencia_cantidad, ip.frecuencia_cantidad)  as frecuencia_cantidad, "
				+ "case when (snpi.id is null) then false else true END en_negociacion "
				+ "from contratacion.insumo_portafolio ip "
				+ "join maestros.insumo i on ip.insumo_id = i.id "
				+ "join contratacion.paquete_portafolio pqp ON ip.portafolio_id = pqp.portafolio_id "
				+ "join contratacion.portafolio p on pqp.portafolio_id = p.id "
				+ "join contratacion.sede_prestador sp on sp.portafolio_id = p.portafolio_padre_id "
				+ "join contratacion.prestador pr on sp.prestador_id = pr.id "
				+ "join contratacion.grupo_insumo gi  on i.grupo_id = gi.id  "
				+ "join contratacion.categoria_insumo ci  on i.categoria_id=ci.id "
				+ "JOIN contratacion.sede_negociacion_paquete snp ON ip.portafolio_id = snp.paquete_id  "
				+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
				+ "LEFT JOIN contratacion.sede_negociacion_paquete_insumo snpi ON snpi.sede_negociacion_paquete_id = snp.id AND snpi.insumo_id = ip.insumo_id "
				+ "LEFT JOIN ( "
				+ "SELECT ip.insumo_id, ip.portafolio_id from contratacion.paquete_portafolio pp "
				+ "JOIN contratacion.portafolio p on p.id = pp.portafolio_id "
				+ "JOIN contratacion.insumo_portafolio ip ON ip.portafolio_id = p.id "
				+ "AND EXISTS (select  null from contratacion.portafolio por where por.id = p.portafolio_padre_id and eps_id is not null) "
				+ "where codigo = :codigoPaquete "
				+ ") insumoBasico ON  insumoBasico.insumo_id = ip.insumo_id "
				+ "WHERE p.id = :paqueteId AND pr.id = :prestadorId and sn.negociacion_id = :negociacionId AND i.estado_insumo_id = 1 "
				+ "AND EXISTS(SELECT NULL FROM contratacion.insumo_portafolio WHERE "
				+ "  insumo_id = insumoBasico.insumo_id and portafolio_id = insumoBasico.portafolio_id ) "
				+ " ORDER BY i.id DESC "
				, resultSetMapping = "InsumoPortafolio.insumosPaqueteNegociacionMapping"),
	})

@SqlResultSetMappings({
	@SqlResultSetMapping(name = "InsumoPortafolio.insumosPaqueteNegociacionMapping",
			classes =
				@ConstructorResult(targetClass = SedeNegociacionPaqueteInsumoDto.class,
				columns = {
				@ColumnResult(name = "insumoPortafolioId", type = Integer.class),
				@ColumnResult(name = "insumoId", type = Integer.class),
				@ColumnResult(name = "codigo_emssanar", type = String.class),
				@ColumnResult(name = "descripcion", type = String.class),
				@ColumnResult(name = "grupoInsumo", type = String.class),
				@ColumnResult(name = "categoriaInsumo", type = String.class),
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
	})
public class InsumoPortafolio implements Identifiable<Long>, Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "codigo_interno", length = 20)
	private String codigoInterno;

	@Column(name="valor")
	private Integer valor;

	@Column(name = "cantidad")
	private Integer cantidad;

	@Column(name = "etiqueta")
	private String etiqueta;

	@Column(name = "observacion", length = 255)
	private String observacion;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "insumo_id")
	private Insumos insumo;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "portafolio_id")
	private Portafolio portafolio;

	@Column(name = "cantidad_minima")
    private Integer cantidadMinima;

    @Column(name = "cantidad_maxima")
    private Integer cantidadMaxima;

    @Column(name = "costo_unitario")
    private BigDecimal costoUnitario;

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

	public InsumoPortafolio() {
	}

	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Insumos getInsumo() {
		return insumo;
	}

	public void setInsumo(Insumos insumo) {
		this.insumo = insumo;
	}

	public Integer getValor() {
		return this.valor;
	}

	public void setValor(Integer valor) {
		this.valor = valor;
	}

	public Integer getCantidad() {
		return this.cantidad;
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

	public Portafolio getPortafolio() {
		return this.portafolio;
	}

	public void setPortafolio(Portafolio portafolio) {
		this.portafolio = portafolio;
	}

	public String getCodigoInterno() {
		return codigoInterno;
	}

	public void setCodigoInterno(String codigoInterno) {
		this.codigoInterno = codigoInterno;
	}

	public String getObservacion() {
		return observacion;
	}

	public void setObservacion(String observacion) {
		this.observacion = observacion;
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