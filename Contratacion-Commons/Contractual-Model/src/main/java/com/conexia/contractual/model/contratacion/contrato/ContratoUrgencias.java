package com.conexia.contractual.model.contratacion.contrato;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoUrgenciasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionConsultaContratoDto;
import com.conexia.contractual.model.maestros.Regional;
import com.conexia.contractual.model.security.User;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "contrato_urgencias", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name = "ContratoUrgencias.findAll", query = "SELECT c FROM ContratoUrgencias c")
    ,
    @NamedQuery(name = "ContratoUrgencias.findById", query = "SELECT c FROM Contrato c WHERE c.id = :id")
    ,
    @NamedQuery(name = "ContratoUrgencias.findByNumeroContrato", query = "SELECT c FROM Contrato c WHERE c.numeroContrato = :numeroContrato")
    ,
	@NamedQuery(name = "ContratoUrgencias.maxContrato", query = "SELECT MAX(c.numeroContrato) FROM ContratoUrgencias c "
            + "WHERE c.tipoModalidad = :tipoModalidad "
            + "AND c.regimen = :regimen "
            + "AND c.numeroContrato like :anio "
            + "AND c.regional.id = :regionalId ")
    ,
	@NamedQuery(name = "ContratoUrgencias.deleteContratoUrgenciasById", query = "DELETE  FROM ContratoUrgencias c WHERE c.id= :contratoUrgenciasId"
    ),})
@SqlResultSetMappings({
    @SqlResultSetMapping(
            name = "ContratoUrgencias.validacionContratosUrgenciasMapping",
            classes = @ConstructorResult(
                    targetClass = ContratoUrgenciasDto.class,
                    columns = {
                        @ColumnResult(name = "contrato_id", type = Long.class),
                        @ColumnResult(name = "numero_contrato", type = String.class),
                        @ColumnResult(name = "n_sedes", type = Integer.class),
                        @ColumnResult(name = "estado_legalizacion", type = String.class),
                        @ColumnResult(name = "regimen", type = String.class)
                    }
            ))
    ,
    @SqlResultSetMapping(
            name = "ContratoUrgencias.eliminarContratoUrgenciasMappging",
            classes = @ConstructorResult(
                    targetClass = NegociacionConsultaContratoDto.class,
                    columns = {
                        @ColumnResult(name = "contrato_id", type = Long.class),
                        @ColumnResult(name = "numero_contrato", type = String.class),
                        @ColumnResult(name = "regimen", type = String.class),
                        @ColumnResult(name = "numero_documento", type = String.class),
                        @ColumnResult(name = "tipo_contrato", type = String.class),
                        @ColumnResult(name = "nombre", type = String.class),
                        @ColumnResult(name = "descripcion", type = String.class),
                        @ColumnResult(name = "tipo_subsidiado", type = String.class),
                        @ColumnResult(name = "fecha_inicio", type = Date.class),
                        @ColumnResult(name = "name", type = String.class),
                        @ColumnResult(name = "fecha_creacion", type = Date.class),
                        @ColumnResult(name = "sedes_contratadas", type = Integer.class),
                        @ColumnResult(name = "estado_contrato", type = String.class)
                    }
            ))
    ,
    @SqlResultSetMapping(
            name = "ContratoUrgencias.ContratosPrestadorMapping",
            classes = @ConstructorResult(
                    targetClass = ContratoUrgenciasDto.class,
                    columns = {
                        @ColumnResult(name = "negociacion_id", type = Long.class),
                        @ColumnResult(name = "contrato_id", type = Long.class),
                        @ColumnResult(name = "numero_contrato", type = String.class),
                        @ColumnResult(name = "tipo_contrato", type = String.class),
                        @ColumnResult(name = "tipo_modalidad", type = String.class),
                        @ColumnResult(name = "regimen", type = String.class),
                        @ColumnResult(name = "tipo_subsidiado", type = String.class),
                        @ColumnResult(name = "fecha_inicio", type = Date.class),
                        @ColumnResult(name = "fecha_fin", type = Date.class),
                        @ColumnResult(name = "nivel_contrato", type = String.class),
                        @ColumnResult(name = "tipo_minuta", type = String.class),
                        @ColumnResult(name = "poblacion", type = Integer.class),
                        @ColumnResult(name = "regional_id", type = Integer.class),
                        @ColumnResult(name = "regional", type = String.class),
                        @ColumnResult(name = "user_id", type = Integer.class),
                        @ColumnResult(name = "primer_nombre", type = String.class),
                        @ColumnResult(name = "segundo_nombre", type = String.class),
                        @ColumnResult(name = "primer_apellido", type = String.class),
                        @ColumnResult(name = "segundo_apellido", type = String.class),
                        @ColumnResult(name = "estado_legalizacion", type = String.class),
                        @ColumnResult(name = "fecha_vo_bo", type = Date.class),
                        @ColumnResult(name = "sedes_contratadas", type = Integer.class),
                        @ColumnResult(name = "estado_contrato", type = String.class),
                        @ColumnResult(name = "solicitud_contratacion_id", type = Long.class),
                        @ColumnResult(name = "nombre_archivo", type = String.class),
                        @ColumnResult(name = "nombre_original_archivo", type = String.class)
                    }
            ))

})
public class ContratoUrgencias implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "numero_contrato_urgencias")
    private String numeroContrato;

    @Basic(optional = false)
    @NotNull
    @Column(name = "prestador_id")
    private Integer prestadorId;

    @Column(name = "regimen", nullable = false)
    @Enumerated(EnumType.STRING)
    private RegimenNegociacionEnum regimen;

    @Basic(optional = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regional_id")
    private Regional regional;

    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_inicio")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInicio;

    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_fin")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFin;

    @Column(name = "tipo_modalidad", nullable = false)
    @Enumerated(EnumType.STRING)
    private NegociacionModalidadEnum tipoModalidad;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User usuario;

    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @Column(name = "poblacion")
    private Integer poblacion;

    @Basic(optional = false)
    @Column(name = "tipo_subsidiado")
    @Enumerated(EnumType.STRING)
    private TipoSubsidiadoEnum tipoSubsidiado;

    @Basic(optional = false)
    @Column(name = "tipo_contrato")
    @Enumerated(EnumType.STRING)
    private TipoContratoEnum tipoContrato;

    @Basic(optional = false)
    @Column(name = "nivel_contrato")
    @Enumerated(EnumType.STRING)
    private NivelContratoEnum nivelContrato;

    @OneToMany(mappedBy="contratoUrgencias")
    private List<SedeContratoUrgencias> sedeContratoUrgencias;

    @Column(name = "deleted")
    private boolean deleted;

    public ContratoUrgencias() {
    }

    public ContratoUrgencias(Long id) {
        this.id = id;
    }

    public ContratoUrgencias(Long id, String numeroContrato, Date fechaInicio, Date fechaFin, NegociacionModalidadEnum tipoModalidad,
            Integer userId, Date fechaCreacion, int poblacion, int regionalId, TipoSubsidiadoEnum tipoSubsidiado,
            TipoContratoEnum tipoContrato, NivelContratoEnum nivelContrato) {
        this.id = id;
        this.numeroContrato = numeroContrato;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipoModalidad = tipoModalidad;
        //this.usuario.setId(id); = userId;
        this.fechaCreacion = fechaCreacion;
        this.poblacion = poblacion;
        this.tipoSubsidiado = tipoSubsidiado;
        this.tipoContrato = tipoContrato;
        this.nivelContrato = nivelContrato;
    }

    //<editor-fold desc="Getters && Setters">
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

	public String getNumeroContrato() {
		return numeroContrato;
	}

	public void setNumeroContrato(String numeroContrato) {
		this.numeroContrato = numeroContrato;
	}

	public Integer getPrestadorId() {
		return prestadorId;
	}

	public void setPrestadorId(Integer prestadorId) {
		this.prestadorId = prestadorId;
	}

	public RegimenNegociacionEnum getRegimen() {
		return regimen;
	}

	public void setRegimen(RegimenNegociacionEnum regimen) {
		this.regimen = regimen;
	}

	public Regional getRegional() {
		return regional;
	}

	public void setRegional(Regional regional) {
		this.regional = regional;
	}

	public Date getFechaInicio() {
		return fechaInicio;
	}

	public void setFechaInicio(Date fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public Date getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(Date fechaFin) {
		this.fechaFin = fechaFin;
	}

	public NegociacionModalidadEnum getTipoModalidad() {
		return tipoModalidad;
	}

	public void setTipoModalidad(NegociacionModalidadEnum tipoModalidad) {
		this.tipoModalidad = tipoModalidad;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public Integer getPoblacion() {
		return poblacion;
	}

	public void setPoblacion(Integer poblacion) {
		this.poblacion = poblacion;
	}

	public TipoSubsidiadoEnum getTipoSubsidiado() {
		return tipoSubsidiado;
	}

	public void setTipoSubsidiado(TipoSubsidiadoEnum tipoSubsidiado) {
		this.tipoSubsidiado = tipoSubsidiado;
	}

	public TipoContratoEnum getTipoContrato() {
		return tipoContrato;
	}

	public void setTipoContrato(TipoContratoEnum tipoContrato) {
		this.tipoContrato = tipoContrato;
	}

	public NivelContratoEnum getNivelContrato() {
		return nivelContrato;
	}

	public void setNivelContrato(NivelContratoEnum nivelContrato) {
		this.nivelContrato = nivelContrato;
	}

	public List<SedeContratoUrgencias> getSedeContratoUrgencias() {
		return sedeContratoUrgencias;
	}

	public void setSedeContratoUrgencias(List<SedeContratoUrgencias> sedeContratoUrgencias) {
		this.sedeContratoUrgencias = sedeContratoUrgencias;
	}

	public User getUsuario() {
		return usuario;
	}

	public void setUsuario(User usuario) {
		this.usuario = usuario;
	}
    //</editor-fold>

    //<editor-fold desc="HashCode && Equals && ToString">
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ContratoUrgencias)) {
            return false;
        }
        ContratoUrgencias other = (ContratoUrgencias) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.conexia.contractual.model.contratacion.ContratoUrgencias[ id=" + id + " ]";
    }
    //</editor-fold>

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
