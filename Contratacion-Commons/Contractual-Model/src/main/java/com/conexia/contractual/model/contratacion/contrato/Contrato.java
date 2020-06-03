package com.conexia.contractual.model.contratacion.contrato;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.NivelContratoEnum;
import com.conexia.contratacion.commons.constants.enums.TipoContratoEnum;
import com.conexia.contratacion.commons.constants.enums.TipoSubsidiadoEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.TecnologiasContratadasDto;
import com.conexia.contractual.model.contratacion.legalizacion.LegalizacionContrato;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacion;
import com.conexia.contractual.model.maestros.Regional;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 *
 * @author jalvarado
 */
@Entity
@Table(name = "contrato", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name = "Contrato.findAll", query = "SELECT c FROM Contrato c")
    ,
    @NamedQuery(name = "Contrato.findById", query = "SELECT c FROM Contrato c WHERE c.id = :id")
    ,
    @NamedQuery(name = "Contrato.findByNumeroContrato", query = "SELECT c FROM Contrato c WHERE c.numeroContrato = :numeroContrato")
    ,
    @NamedQuery(name = "Contrato.updateNombreArchivo", query = "update Contrato set nombreArchivo = :nombreArchivo, nombreOriginalArchivo = :nombreOriginalArchivo WHERE id = :id")
    ,
    @NamedQuery(name = "Contrato.updateFechasOtroSi",
            query = "UPDATE Contrato c SET c.fechaInicioOtroSi = :fechaInicioOtroSi, "
                    + "c.fechaFinOtroSi = :fechaFinOtroSi "
                    + "WHERE c.id IN (SELECT c.id "
                    + "FROM Contrato c "
                    + "JOIN c.solicitudContratacion sol "
                    + "WHERE sol.negociacion.id = :negociacionId) ")
    ,
	@NamedQuery(name = "Contrato.maxContrato", query = "SELECT MAX(c.numeroContrato) FROM Contrato c "
            + "JOIN c.solicitudContratacion sc "
            + "JOIN sc.negociacion n "
            + "WHERE n.prestador.id = :prestadorId "
            + "AND sc.tipoModalidadNegociacion = :tipoModalidad "
            + "AND sc.regimen = :regimen "
            + "AND c.numeroContrato like :anio "
            + "AND c.regional.id = :regionalId "),})
@NamedNativeQueries({
        @NamedNativeQuery(name = "Contrato.updateNumeroContrato",
                query = " update contratacion.contrato set numero_contrato = :numeroContrato where solicitud_contratacion_id = :solicitudContratacion  "),
    @NamedNativeQuery(name = "Contrato.actualizarFechaContratoByNegociacionId",
            query = " update contratacion.contrato set fecha_inicio = :fechaInicio, fecha_fin = :fechaFin where id in (  "
            + "	select ct.id  "
            + "	from contratacion.contrato ct  "
            + "	join contratacion.solicitud_contratacion sct on sct.id = ct.solicitud_contratacion_id  "
            + "	where sct.negociacion_id = :negociacionId  "
            + ") "),
        @NamedNativeQuery(name = "Contrato.findFechaInicioContratoPadreValidateDateProrroga",
                query = " SELECT   " +
                        "	case	  " +
                        "		when (select count(*) from contratacion.solicitud_contratacion sc where sc.negociacion_id = neg.id)>1   " +
                        "			then 		  " +
                        "			(  " +
                        "				SELECT DISTINCT CAST(c.fecha_fin + CAST('1 days' AS INTERVAL) AS DATE)  " +
                        "	            FROM contratacion.solicitud_contratacion sc   " +
                        "	               INNER JOIN contratacion.contrato c ON c.solicitud_contratacion_id = sc.id and   " +
                        "	                                                     sc.negociacion_id = neg.id and   " +
                        "	                                                     sc.regimen = 'SUBSIDIADO'  " +
                        "			)  " +
                        "		else   " +
                        "		    (      " +
                        "				SELECT DISTINCT CAST(c.fecha_fin + CAST('1 days' AS INTERVAL) AS DATE)  " +
                        "	            FROM contratacion.solicitud_contratacion sc   " +
                        "	               INNER JOIN contratacion.contrato c ON c.solicitud_contratacion_id = sc.id and   " +
                        "	                                                     sc.negociacion_id = neg.id		  " +
                        "		    )  " +
                        "	end  " +
                        " FROM contratacion.negociacion neg  " +
                        " WHERE neg.id =:negociacionPadreId "),
        @NamedNativeQuery(name = "Contrato.findFechaInicioContratoPadre",
                query = " SELECT DISTINCT CAST(coalesce (c.fecha_inicio) AS DATE) fecha_inicio "
                        + "from contratacion.contrato  c "
                        + "JOIN contratacion.solicitud_contratacion sol on c.solicitud_contratacion_id = sol.id "
                        + "WHERE sol.negociacion_id = :negociacionPadreId "),

        @NamedNativeQuery(name = "Contrato.findFechaInicioContratoPadreValidateDateProrrogaSub",
                query = "SELECT CAST((coalesce (c.fecha_fin_otro_si, c.fecha_fin) + CAST('1 days' AS INTERVAL)) AS DATE) fecha_fin   " +
                        "						from contratacion.contrato  c   " +
                        "						JOIN contratacion.solicitud_contratacion sol on c.solicitud_contratacion_id = sol.id   " +
                        "						WHERE sol.negociacion_id = :negociacionPadreId and sol.regimen='SUBSIDIADO' "),
})
@SqlResultSetMappings({
        @SqlResultSetMapping( name = "Contrato.consultaTecnologiasContratadasEvento",
                classes = @ConstructorResult(
                        targetClass = TecnologiasContratadasDto.class,
                        columns = {
                                @ColumnResult(name = "negociacion_id", type = Long.class),
                                @ColumnResult(name = "numero_contrato", type = String.class),
                                @ColumnResult(name = "numero_otro_si", type = Integer.class),
                                @ColumnResult(name = "tipo_identificacion_id", type = Integer.class),
                                @ColumnResult(name = "nombre", type = String.class),
                                @ColumnResult(name = "numero_documento", type = String.class),
                                @ColumnResult(name = "tipo_contrato", type = String.class),
                                @ColumnResult(name = "tipo_modalidad_negociacion", type = String.class),
                                @ColumnResult(name = "regimen", type = String.class),
                                @ColumnResult(name = "tipo_subsidiado", type = String.class),
                                @ColumnResult(name = "fecha_inicio", type = Date.class),
                                @ColumnResult(name = "fecha_fin", type = Date.class),
                                @ColumnResult(name = "nivel_contrato", type = String.class),
                                @ColumnResult(name = "poblacion", type = Integer.class),
                                @ColumnResult(name = "minuta", type = String.class),
                                @ColumnResult(name = "regional", type = String.class),
                                @ColumnResult(name = "responsable", type = String.class),
                                @ColumnResult(name = "fecha_creacion", type = Date.class),
                                @ColumnResult(name = "estado_legalizacion", type = String.class),
                                @ColumnResult(name = "fecha_legalizacion", type = Date.class),
                                @ColumnResult(name = "estado_contrato", type = String.class),
                                @ColumnResult(name = "codigo_servicio", type = String.class),
                                @ColumnResult(name = "nombre_servicio", type = String.class),
                                @ColumnResult(name = "codigo_tecnologia", type = String.class),
                                @ColumnResult(name = "descripcion_tecnologia", type = String.class),
                                @ColumnResult(name = "tarifario", type = String.class),
                                @ColumnResult(name = "porcentaje_negociado", type = BigDecimal.class),
                                @ColumnResult(name = "valor_negociado", type = BigDecimal.class)
                        }
                )),
})
public class Contrato implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "numero_contrato")
    private String numeroContrato;

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

    @Basic(optional = false)
    @NotNull
    @Column(name = "user_id")
    private Integer userId;

    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_creacion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaCreacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_contratacion_id")
    private SolicitudContratacion solicitudContratacion;

    @Column(name = "poblacion")
    private Integer poblacion;

    @Basic(optional = false)
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regional_id")
    private Regional regional;

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

    @Column(name = "nombre_archivo")
    private String nombreArchivo;

    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_elaboracion")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaElaboracion;

    @Column(name = "nombre_original_archivo")
    private String nombreOriginalArchivo;

    @Column(name = "fecha_inicio_otro_si")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaInicioOtroSi;

    @Column(name = "fecha_fin_otro_si")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFinOtroSi;

    @OneToMany(mappedBy = "contrato", fetch = FetchType.LAZY)
    private List<LegalizacionContrato> legalizacionContratos;

    @OneToMany(mappedBy="contrato")
    private List<SedeContrato> sedeContratos;

    @Column(name = "deleted")
    private boolean deleted;

    @Column(name = "fecha_elaboracion_otrosi")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaElaboracionOtroSi;

    public Contrato() {
    }

    public Contrato(Long id) {
        this.id = id;
    }

    public Contrato(Long id, String numeroContrato, Date fechaInicio, Date fechaFin, NegociacionModalidadEnum tipoModalidad, Integer userId, Date fechaCreacion, int poblacion, int regionalId, TipoSubsidiadoEnum tipoSubsidiado, TipoContratoEnum tipoContrato, NivelContratoEnum nivelContrato) {
        this.id = id;
        this.numeroContrato = numeroContrato;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.tipoModalidad = tipoModalidad;
        this.userId = userId;
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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    /**
     * @return the solicitudContratacion
     */
    public SolicitudContratacion getSolicitudContratacion() {
        return solicitudContratacion;
    }

    /**
     * @param solicitudContratacion the solicitudContratacion to set
     */
    public void setSolicitudContratacion(SolicitudContratacion solicitudContratacion) {
        this.solicitudContratacion = solicitudContratacion;
    }

    public int getPoblacion() {
        return poblacion;
    }

    public void setPoblacion(Integer poblacion) {
        this.poblacion = poblacion;
    }

    public Regional getRegional() {
        return regional;
    }

    public void setRegional(Regional regional) {
        this.regional = regional;
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

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public List<LegalizacionContrato> getLegalizacionContratos() {
        return this.legalizacionContratos;
    }

    public void setLegalizacionContratos(final List<LegalizacionContrato> legalizacionContratos) {
        this.legalizacionContratos = legalizacionContratos;
    }

    public List<SedeContrato> getSedeContratos() {
        return sedeContratos;
    }

    public void setSedeContratos(List<SedeContrato> sedeContratos) {
        this.sedeContratos = sedeContratos;
    }

    public Date getFechaElaboracion() {
        return fechaElaboracion;
    }

    public void setFechaElaboracion(Date fechaElaboracion) {
        this.fechaElaboracion = fechaElaboracion;
    }

    public String getNombreOriginalArchivo() {
        return nombreOriginalArchivo;
    }

    public void setNombreOriginalArchivo(String nombreOriginalArchivo) {
        this.nombreOriginalArchivo = nombreOriginalArchivo;
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

    public Date getFechaElaboracionOtroSi() {
        return fechaElaboracionOtroSi;
    }

    public void setFechaElaboracionOtroSi(Date fechaElaboracionOtroSi) {
        this.fechaElaboracionOtroSi = fechaElaboracionOtroSi;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Contrato)) {
            return false;
        }
        Contrato other = (Contrato) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.conexia.contractual.model.contratacion.Contrato[ id=" + id + " ]";
    }
    //</editor-fold>

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
