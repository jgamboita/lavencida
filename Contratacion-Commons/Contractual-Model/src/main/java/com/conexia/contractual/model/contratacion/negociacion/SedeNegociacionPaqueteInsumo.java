package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteInsumoDto;
import com.conexia.contractual.model.maestros.Insumos;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@NamedQueries({
    @NamedQuery(name = "SedeNegociacionPaqueteInsumo.findInsumosByNegociacionAndPaquete",
            query = "SELECT new com.conexia.contratacion.commons.dto.maestros.InsumosDto("
            + "i.codigo, i.descripcion, gi.descripcion, ci.descripcion, snpi.cantidad , snpi.observacion "
            + ") "
            + "FROM SedeNegociacionPaqueteInsumo snpi "
            + "JOIN snpi.insumo i "
            + "JOIN i.grupoInsumo gi "
            + "JOIN i.categoriaInsumo ci "
            + "JOIN snpi.sedeNegociacionPaquete snp "
            + "WHERE snp.sedeNegociacion.negociacion.id = :negociacionId "
            + "AND snp.id = :sedeNegociacionPaqueteId "),

})
@NamedNativeQueries({
		@NamedNativeQuery(name = "SedeNegociacionPaqueteInsumo.findInsumoPaqueteDetalleNegociacion",
				query = "SELECT DISTINCT i.id as insumoId, snpi.sede_negociacion_paquete_id as sedePaqueteInsumoId, i.codigo_emssanar, i.descripcion, "
					+ "gi.descripcion as grupoInsumo, ci.descripcion as categoriaInsumo, snpi.cantidad_minima, snpi.cantidad_maxima,snpi.costo_unitario, snpi.observacion, "
					+ "snpi.ingreso_aplica, snpi.ingreso_cantidad, snpi.frecuencia_unidad, snpi.frecuencia_cantidad "
					+ "FROM contratacion.sede_negociacion_paquete_insumo snpi "
					+ "JOIN maestros.insumo i  ON snpi.insumo_id = i.id "
					+ "JOIN contratacion.grupo_insumo gi  ON i.grupo_id = gi.id "
					+ "JOIN contratacion.categoria_insumo ci  ON i.categoria_id=ci.id "
					+ "JOIN contratacion.sede_negociacion_paquete snp  ON snpi.sede_negociacion_paquete_id=snp.id "
					+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
					+ "LEFT JOIN contratacion.insumo_portafolio ip ON ip.insumo_id = i.id "
					+ "LEFT JOIN ( "
					+ "SELECT ip.insumo_id, ip.portafolio_id from contratacion.paquete_portafolio pp "
					+ "JOIN contratacion.portafolio p on p.id = pp.portafolio_id "
					+ "JOIN contratacion.insumo_portafolio ip ON ip.portafolio_id = p.id "
					+ "AND EXISTS (select  null from contratacion.portafolio por where por.id = p.portafolio_padre_id and eps_id is not null) "
					+ "where codigo = :codigoPaquete "
					+ ") insumoBasico ON  insumoBasico.insumo_id = ip.insumo_id "
					+ "WHERE  sn.negociacion_id = :negociacionId "
					+ "AND snp.paquete_id= :paqueteId AND i.estado_insumo_id = 1 "
					+ "AND NOT EXISTS(SELECT NULL FROM contratacion.insumo_portafolio WHERE "
					+ "  insumo_id = insumoBasico.insumo_id and portafolio_id = insumoBasico.portafolio_id ) ", resultSetMapping = "SedeNegociacionPaqueteInsumo.insumosPaqueteNegociacionMapping"
					),
		@NamedNativeQuery(name = "SedeNegociacionPaqueteInsumo.deleteInsumoSedeNegociacion",
				query = "DELETE from contratacion.sede_negociacion_paquete_insumo WHERE id IN ( "
					+ "SELECT snpi.id FROM contratacion.sede_negociacion_paquete_insumo snpi "
					+ "JOIN contratacion.sede_negociacion_paquete snp ON snpi.sede_negociacion_paquete_id = snp.id "
					+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
					+ "WHERE sn.negociacion_id = :negociacionId AND snp.paquete_id = :paqueteId AND snpi.insumo_id = :insumoId) "
					),
		@NamedNativeQuery(name = "SedeNegociacionPaqueteInsumo.deleteAllInsumosSedeNegociacion", query = "DELETE from contratacion.sede_negociacion_paquete_insumo WHERE id IN ( "
				+ "SELECT snpi.id FROM contratacion.sede_negociacion_paquete_insumo snpi "
				+ "JOIN contratacion.sede_negociacion_paquete snp ON snpi.sede_negociacion_paquete_id = snp.id "
				+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
				+ "WHERE sn.negociacion_id = :negociacionId AND snp.paquete_id = :paqueteId) "),
		@NamedNativeQuery(name = "SedeNegociacionPaqueteInsumo.insertInsumoSedeNegociacion",
				query = "INSERT INTO  contratacion.sede_negociacion_paquete_insumo(insumo_id,sede_negociacion_paquete_id) "
					+ "SELECT DISTINCT i.id, snp.id "
					+ "FROM contratacion.sede_negociacion_paquete snp "
					+ "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
					+ "CROSS JOIN maestros.insumo i "
					+ "LEFT JOIN contratacion.sede_negociacion_paquete_insumo snpi ON snpi.sede_negociacion_paquete_id = snp.id and snpi.insumo_id = i.id "
					+ "WHERE sn.negociacion_id  = :negociacionId AND snp.paquete_id = :paqueteId AND i.id in (:insumoId) "
					+ "and snpi.id is null "),
                @NamedNativeQuery(name = "SedeNegociacionPaqueteInsumo.insertInsumoSedeNegociacionPaquete",
				query = "INSERT INTO  contratacion.sede_negociacion_paquete_insumo(insumo_id,sede_negociacion_paquete_id, cantidad_minima, cantidad_maxima, cantidad," 
                                        + "observacion, ingreso_aplica, ingreso_cantidad, frecuencia_unidad,frecuencia_cantidad) "
					+ "SELECT DISTINCT i.id, snp.id , :cantidadMinima, :cantidadMaxima, :cantidad, :observacion, :ingresoAplica, "
                                        + ":ingresoCantidad, :frecuenciaUnidad, :frecuenciaCantidad "
                                        + "FROM contratacion.sede_negociacion_paquete snp "
                                        + "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
                                        + "CROSS JOIN maestros.insumo i "
                                        + "LEFT JOIN contratacion.sede_negociacion_paquete_insumo snpi ON snpi.sede_negociacion_paquete_id = snp.id and snpi.insumo_id = i.id "
                                        + "WHERE sn.negociacion_id  = :negociacionId AND snp.paquete_id = :paqueteId AND i.id in (:insumoId) " 
                                        + "and snpi.id is null ")
	})


@SqlResultSetMappings({
		@SqlResultSetMapping(name = "SedeNegociacionPaqueteInsumo.insumosPaqueteNegociacionMapping",
				classes =
					@ConstructorResult(targetClass = SedeNegociacionPaqueteInsumoDto.class,
					columns = {
					@ColumnResult(name = "insumoId", type = Integer.class),
					@ColumnResult(name = "sedePaqueteInsumoId", type = Integer.class),
					@ColumnResult(name = "codigo_emssanar", type = String.class),
					@ColumnResult(name = "descripcion", type = String.class),
					@ColumnResult(name = "grupoInsumo", type = String.class),
					@ColumnResult(name = "categoriaInsumo", type = String.class),
					@ColumnResult(name = "cantidad_minima", type = Integer.class),
					@ColumnResult(name = "cantidad_maxima", type = Integer.class),
					@ColumnResult(name = "costo_unitario", type = BigDecimal.class),
					@ColumnResult(name = "observacion", type = String.class),
					@ColumnResult(name = "ingreso_aplica", type = String.class),
					@ColumnResult(name = "ingreso_cantidad", type = Double.class),
					@ColumnResult(name = "frecuencia_unidad", type = String.class),
					@ColumnResult(name = "frecuencia_cantidad", type = Double.class)
					})),
		})
@Table(schema = "contratacion", name = "sede_negociacion_paquete_insumo")
public class SedeNegociacionPaqueteInsumo implements Identifiable<Long>,
        Serializable {

    /**
     *
     */
    private static final long serialVersionUID = -2832599685860043832L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "insumo_id")
    private Insumos insumo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_negociacion_paquete_id")
    private SedeNegociacionPaquete sedeNegociacionPaquete;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "observacion")
    private String observacion;


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

    /**
     * Constructor por defecto
     */
    public SedeNegociacionPaqueteInsumo() {
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the insumo
     */
    public Insumos getInsumo() {
        return insumo;
    }

    /**
     * @param insumo the insumo to set
     */
    public void setInsumo(Insumos insumo) {
        this.insumo = insumo;
    }

    /**
     * @return the sedeNegociacionPaquete
     */
    public SedeNegociacionPaquete getSedeNegociacionPaquete() {
        return sedeNegociacionPaquete;
    }

    /**
     * @param sedeNegociacionPaquete the sedeNegociacionPaquete to set
     */
    public void setSedeNegociacionPaquete(
            SedeNegociacionPaquete sedeNegociacionPaquete) {
        this.sedeNegociacionPaquete = sedeNegociacionPaquete;
    }

    /**
     * @return the cantidad
     */
    public Integer getCantidad() {
        return cantidad;
    }

    /**
     * @param cantidad the cantidad to set
     */
    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
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

}
