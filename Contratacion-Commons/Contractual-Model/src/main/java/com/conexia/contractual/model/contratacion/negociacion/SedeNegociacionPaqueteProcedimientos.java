package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.maestros.Procedimientos;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * The persistent class for the sede_negociacion_servicio database table.
 *
 * @param <E>
 */
@MappedSuperclass
public abstract class SedeNegociacionPaqueteProcedimientos implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimiento_id")
    private Procedimientos procedimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_negociacion_paquete_id")
    private SedeNegociacionPaquete sedeNegociacionPaquete;

    @Column(name = "cantidad")
    private Integer cantidad;

    @Column(name = "principal")
    private Boolean principal;
    
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

    public SedeNegociacionPaqueteProcedimientos() {
    }

    @Override
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Procedimientos getProcedimiento() {
        return procedimiento;
    }

    public void setProcedimiento(Procedimientos procedimiento) {
        this.procedimiento = procedimiento;
    }

    public SedeNegociacionPaquete getSedeNegociacionPaquete() {
        return sedeNegociacionPaquete;
    }

    public void setSedeNegociacionPaquete(SedeNegociacionPaquete sedeNegociacionPaquete) {
        this.sedeNegociacionPaquete = sedeNegociacionPaquete;
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
