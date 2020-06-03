package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.TipoModificacionTecnologiaOtroSiEnum;
import com.conexia.contractual.model.contratacion.Tarifario;
import com.conexia.contractual.model.contratacion.converter.TipoModificacionTecnologiaOtroSiConverter;
import com.conexia.contractual.model.maestros.Actividad;
import com.conexia.contractual.model.maestros.Procedimientos;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Procedimientos negociados para la sede.
 *
 * @author jalvarado
 * @param <E> Procedimiento o Transporte por el cual se va a realizar la busqueda.
 */
@MappedSuperclass
public abstract class SedeNegociacionProcedimientos implements Identifiable<Long>, Serializable  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Boolean negociado;

    @Column(name = "porcentaje_contrato")
    private BigDecimal porcentajeContrato;

    @Column(name = "porcentaje_negociado")
    private BigDecimal porcentajeNegociado;

    @Column(name = "porcentaje_propuesto")
    private BigDecimal porcentajePropuesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimiento_id")
    private Procedimientos procedimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pto_id")
    private Procedimientos pto;

    @Column(name = "tarifa_diferencial")
    private Boolean tarifaDiferencial = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifario_contrato_id")
    private Tarifario tarifarioContrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifario_negociado_id")
    private Tarifario tarifarioNegociado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifario_propuesto_id")
    private Tarifario tarifarioPropuesto;

    @Column(name = "valor_contrato")
    private BigDecimal valorContrato;

    @Column(name = "valor_negociado")
    private BigDecimal valorNegociado;

    @Column(name = "valor_propuesto")
    private BigDecimal valorPropuesto;

    //bi-directional many-to-one association to SedeNegociacionServicio
    @ManyToOne
    @JoinColumn(name = "sede_negociacion_servicio_id")
    private SedeNegociacionServicio sedeNegociacionServicio;

  //bi-directional many-to-one association to sedeNegociacionCapitulo
    @ManyToOne
    @JoinColumn(name = "sede_negociacion_capitulo_id")
    private SedeNegociacionCapitulo sedeNegociacionCapitulo;

    @Column(name = "requiere_autorizacion")
    private Boolean requiereAutorizacion;

    @Column(name = "frecuencia_referente")
    private Double frecuenciaReferente;

    @Column(name = "frecuencia")
    private Double frecuenciaUsuario;

    @Column(name = "costo_medio_usuario_referente")
    private BigDecimal costoMedioUsuarioReferente;

    @Column(name = "costo_medio_usuario")
    private BigDecimal costoMedioUsuario;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "requiere_autorizacion_ambulatorio")
    private String requiereAutorizacionAmbulatorio;

    @Column(name = "requiere_autorizacion_hospitalario")
    private String requiereAutorizacionHospitalario;

    @Column(name = "user_parametrizador_id")
    private Integer userParametrizadorId;

    @Column(name = "fecha_parametrizacion")
    @Temporal(TemporalType.TIMESTAMP)
	private Date fechaParametrizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actividad_id")
    private Actividad actividad;

    @Column(name= "franja_inicio")
    private BigDecimal franjaInicio;

    @Column(name="franja_fin")
    private BigDecimal franjaFin;

    @Column(name = "tipo_modificacion_otro_si_id")
    @Convert(converter = TipoModificacionTecnologiaOtroSiConverter.class)
    private TipoModificacionTecnologiaOtroSiEnum tipoModificacionOtroSi;

    @Column(name = "fecha_inicio_otro_si")
    private Date fechaInicioOtroSi;

    @Column(name = "fecha_fin_otro_si")
    private Date fechaFinOtroSi;

    @Column(name = "item_visible")
    private Boolean itemVisible;

    @Column(name = "tipo_adicion_otro_si")
    private Integer tipoAdicionOtroSi;


    public SedeNegociacionProcedimientos() {
    }

    //<editor-fold desc="Getters && Setters">

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getNegociado() {
        return this.negociado;
    }

    public void setNegociado(Boolean negociado) {
        this.negociado = negociado;
    }

    public BigDecimal getPorcentajeContrato() {
        return this.porcentajeContrato;
    }

    public void setPorcentajeContrato(BigDecimal porcentajeContrato) {
        this.porcentajeContrato = porcentajeContrato;
    }

    public BigDecimal getPorcentajeNegociado() {
        return this.porcentajeNegociado;
    }

    public void setPorcentajeNegociado(BigDecimal porcentajeNegociado) {
        this.porcentajeNegociado = porcentajeNegociado;
    }

    public BigDecimal getPorcentajePropuesto() {
        return this.porcentajePropuesto;
    }

    public void setPorcentajePropuesto(BigDecimal porcentajePropuesto) {
        this.porcentajePropuesto = porcentajePropuesto;
    }

    public Procedimientos getProcedimiento() {
        return procedimiento;
    }

    public void setProcedimiento(Procedimientos procedimiento) {
        this.procedimiento = procedimiento;
    }

    public Boolean getTarifaDiferencial() {
        return this.tarifaDiferencial;
    }

    public void setTarifaDiferencial(Boolean tarifaDiferencial) {
        this.tarifaDiferencial = tarifaDiferencial;
    }

    public Tarifario getTarifarioContrato() {
        return tarifarioContrato;
    }

    public void setTarifarioContrato(Tarifario tarifarioContrato) {
        this.tarifarioContrato = tarifarioContrato;
    }

    public Tarifario getTarifarioNegociado() {
        return tarifarioNegociado;
    }

    public void setTarifarioNegociado(Tarifario tarifarioNegociado) {
        this.tarifarioNegociado = tarifarioNegociado;
    }

    public Tarifario getTarifarioPropuesto() {
        return tarifarioPropuesto;
    }

    public void setTarifarioPropuesto(Tarifario tarifarioPropuesto) {
        this.tarifarioPropuesto = tarifarioPropuesto;
    }

    public BigDecimal getValorContrato() {
        return this.valorContrato;
    }

    public void setValorContrato(BigDecimal valorContrato) {
        this.valorContrato = valorContrato;
    }

    public BigDecimal getValorNegociado() {
        return this.valorNegociado;
    }

    public void setValorNegociado(BigDecimal valorNegociado) {
        this.valorNegociado = valorNegociado;
    }

    public BigDecimal getValorPropuesto() {
        return this.valorPropuesto;
    }

    public void setValorPropuesto(BigDecimal valorPropuesto) {
        this.valorPropuesto = valorPropuesto;
    }

    public SedeNegociacionServicio getSedeNegociacionServicio() {
        return this.sedeNegociacionServicio;
    }

    public void setSedeNegociacionServicio(SedeNegociacionServicio sedeNegociacionServicio) {
        this.sedeNegociacionServicio = sedeNegociacionServicio;
    }

    /**
     * @return the requiereAutorizacion
     */
    public Boolean getRequiereAutorizacion() {
        return requiereAutorizacion;
    }

    /**
     * @param requiereAutorizacion the requiereAutorizacion to set
     */
    public void setRequiereAutorizacion(Boolean requiereAutorizacion) {
        this.requiereAutorizacion = requiereAutorizacion;
    }

	public Double getFrecuenciaReferente() {
		return frecuenciaReferente;
	}

	public void setFrecuenciaReferente(Double frecuenciaReferente) {
		this.frecuenciaReferente = frecuenciaReferente;
	}

	public BigDecimal getCostoMedioUsuarioReferente() {
		return costoMedioUsuarioReferente;
	}

	public void setCostoMedioUsuarioReferente(BigDecimal costoMedioUsuarioReferente) {
		this.costoMedioUsuarioReferente = costoMedioUsuarioReferente;
	}

	public BigDecimal getCostoMedioUsuario() {
		return costoMedioUsuario;
	}

	public void setCostoMedioUsuario(BigDecimal costoMedioUsuario) {
		this.costoMedioUsuario = costoMedioUsuario;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Actividad getActividad() {
		return actividad;
	}

	public void setActividad(Actividad actividad) {
		this.actividad = actividad;
	}

	public String getRequiereAutorizacionAmbulatorio() {
		return requiereAutorizacionAmbulatorio;
	}

	public void setRequiereAutorizacionAmbulatorio(String requiereAutorizacionAmbulatorio) {
		this.requiereAutorizacionAmbulatorio = requiereAutorizacionAmbulatorio;
	}

	public String getRequiereAutorizacionHospitalario() {
		return requiereAutorizacionHospitalario;
	}

	public void setRequiereAutorizacionHospitalario(String requiereAutorizacionHospitalario) {
		this.requiereAutorizacionHospitalario = requiereAutorizacionHospitalario;
	}

	public Integer getUserParametrizadorId() {
		return userParametrizadorId;
	}

	public void setUserParametrizadorId(Integer userParametrizadorId) {
		this.userParametrizadorId = userParametrizadorId;
	}

	public Date getFechaParametrizacion() {
		return fechaParametrizacion;
	}

	public void setFechaParametrizacion(Date fechaParametrizacion) {
		this.fechaParametrizacion = fechaParametrizacion;
	}

	public SedeNegociacionCapitulo getSedeNegociacionCapitulo() {
		return sedeNegociacionCapitulo;
	}

	public void setSedeNegociacionCapitulo(SedeNegociacionCapitulo sedeNegociacionCapitulo) {
		this.sedeNegociacionCapitulo = sedeNegociacionCapitulo;
	}

	public Procedimientos getPto() {
		return pto;
	}

	public void setPto(Procedimientos pto) {
		this.pto = pto;
	}

	public Double getFrecuenciaUsuario() {
		return frecuenciaUsuario;
	}

	public void setFrecuenciaUsuario(Double frecuenciaUsuario) {
		this.frecuenciaUsuario = frecuenciaUsuario;
	}

	public BigDecimal getFranjaInicio() {
		return franjaInicio;
	}

	public void setFranjaInicio(BigDecimal franjaInicio) {
		this.franjaInicio = franjaInicio;
	}

	public BigDecimal getFranjaFin() {
		return franjaFin;
	}

	public void setFranjaFin(BigDecimal franjaFin) {
		this.franjaFin = franjaFin;
	}

    public TipoModificacionTecnologiaOtroSiEnum getTipoModificacionOtroSi() {
        return tipoModificacionOtroSi;
    }

    public void setTipoModificacionOtroSi(TipoModificacionTecnologiaOtroSiEnum tipoModificacionOtroSi) {
        this.tipoModificacionOtroSi = tipoModificacionOtroSi;
    }

    public Date getFechaInicioOtroSi() {
        return fechaInicioOtroSi;
    }

    public void setFechaInicioOtroSi(Date fechaInicioOtroSi) {
        this.fechaInicioOtroSi = fechaInicioOtroSi;
    }

    public Date getFechaFinOtroSi() {
        return fechaFinOtroSi;
    }

    public void setFechaFinOtroSi(Date fechaFinOtroSi) {
        this.fechaFinOtroSi = fechaFinOtroSi;
    }

    public Boolean getItemVisible() {
        return itemVisible;
    }

    public void setItemVisible(Boolean itemVisible) {
        this.itemVisible = itemVisible;
    }

    public Integer getTipoAdicionOtroSi() {
        return tipoAdicionOtroSi;
    }

    public void setTipoAdicionOtroSi(Integer tipoAdicionOtroSi) {
        this.tipoAdicionOtroSi = tipoAdicionOtroSi;
    }

    //</editor-fold>
}
