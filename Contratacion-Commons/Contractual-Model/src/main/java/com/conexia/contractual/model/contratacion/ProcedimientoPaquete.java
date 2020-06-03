package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteProcedimientoDto;
import com.conexia.contractual.model.contratacion.portafolio.Portafolio;
import com.conexia.contractual.model.maestros.Procedimientos;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "procedimiento_paquete", schema = "contratacion")
@NamedQueries({
        @NamedQuery(name = "ProcedimientoPaquete.findDtoByPaqueteId",
                query = "SELECT new com.conexia.contratacion.commons.dto.maestros.ProcedimientoPaqueteDto("
                        + "pp.id, pp.cantidad, pp.principal, pp.etiqueta, p.id, p.cups, p.codigoCliente, "
                        + "p.descripcion, p.complejidad, s.id, s.nombre, s.codigo, m.id, m.nombre, m.codigo) "
                        + "FROM ProcedimientoPaquete pp JOIN pp.procedimiento p JOIN p.servicioSalud s JOIN s.macroServicio m "
                        + "WHERE pp.portafolio.id = :paqueteId "
                        + "ORDER BY CASE pp.principal "
                        + "WHEN true THEN 0 WHEN false THEN 2 ELSE 1 END, "
                        + "CASE WHEN pp.cantidad IS NULL THEN 0 ELSE pp.cantidad END ASC"),
})
@SqlResultSetMappings({
        @SqlResultSetMapping(name = "ProcedimientoPaquete.procedimientoPaquetePrestadorMapping",
                classes = @ConstructorResult(targetClass = SedeNegociacionPaqueteProcedimientoDto.class,
                        columns = {
                                @ColumnResult(name = "portafolioId", type = Integer.class),
                                @ColumnResult(name = "procedimientoId", type = Integer.class),
                                @ColumnResult(name = "codigo_emssanar", type = String.class),
                                @ColumnResult(name = "descripcion", type = String.class),
                                @ColumnResult(name = "cantidad_minima", type = Integer.class),
                                @ColumnResult(name = "cantidad_maxima", type = Integer.class),
                                @ColumnResult(name = "observacion", type = String.class),
                                @ColumnResult(name = "ingreso_aplica", type = String.class),
                                @ColumnResult(name = "ingreso_cantidad", type = Double.class),
                                @ColumnResult(name = "frecuencia_unidad", type = String.class),
                                @ColumnResult(name = "frecuencia_cantidad", type = Double.class),
                                @ColumnResult(name = "en_negociacion", type = Boolean.class),
                                @ColumnResult(name = "principal", type = Boolean.class),
                                @ColumnResult(name = "estado", type = Integer.class)
                        })),
})
public class ProcedimientoPaquete implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "principal")
    private Boolean principal;

    @Column(name = "etiqueta")
    private String etiqueta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimiento_id")
    private Procedimientos procedimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paquete_id")
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

    public ProcedimientoPaquete() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public Boolean getPrincipal() {
        return principal;
    }

    public void setPrincipal(Boolean principal) {
        this.principal = principal;
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public void setEtiqueta(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public Procedimientos getProcedimiento() {
        return procedimiento;
    }

    public void setProcedimiento(Procedimientos procedimiento) {
        this.procedimiento = procedimiento;
    }

    public Portafolio getPortafolio() {
        return this.portafolio;
    }

    public void setPortafolio(Portafolio portafolio) {
        this.portafolio = portafolio;
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