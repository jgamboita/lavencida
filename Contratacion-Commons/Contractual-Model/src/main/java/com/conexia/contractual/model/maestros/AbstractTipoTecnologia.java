package com.conexia.contractual.model.maestros;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.TipoProcedimientoEnum;
import com.conexia.contractual.model.contratacion.converter.TipoProcedimientoConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "procedimiento", schema = "maestros")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "tipo_procedimiento_id", discriminatorType = DiscriminatorType.INTEGER)
@NamedQueries({
        @NamedQuery(name = "AbstractTipoTecnologia.getByCodes",
                query = "select new com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto(p.id, p.codigoEmssanar, p.estadoProcedimiento.id, p.nivelComplejidad, p.tipoProcedimiento) " +
                        "FROM AbstractTipoTecnologia p " +
                        "where p.codigoEmssanar IN (:codigosEmssanar)"),
})
public abstract class AbstractTipoTecnologia implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "codigo", nullable = false, length = 20)
    private String codigo;

    @Column(name = "codigo_emssanar", nullable = false, length = 20)
    private String codigoEmssanar;

    @Column(name = "descripcion", nullable = false, length = 1500)
    private String descripcion;

    @Column(name = "edad_ini", nullable = false)
    private Integer edadIni;

    @Column(name = "edad_fin", nullable = false)
    private Integer edadFin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_procedimiento_id", nullable = false)
    private EstadoTecnologia estadoProcedimiento;

    @Column(name = "nivel_autorizacion", nullable = false)
    private Integer nivelAutorizacion;

    @Column(name = "nivel_complejidad", nullable = false)
    private Integer nivelComplejidad;

    @Column(name = "tipo_pago_requerido_enum", nullable = false)
    private Long tipoPagoRequeridoId;

    @Column(name = "cliente_pk")
    private Integer clientePk;

    @Column(name = "es_recobrable", nullable = false)
    private Boolean recobrable;

    @Column(name = "es_quirurgico", nullable = false)
    private Boolean quirurgico;

    @Column(name = "es_internacion")
    private Boolean internacion;

    @Column(name = "sin_auditoria_nacional")
    private Boolean sinAuditoriaNacional;

    @ManyToOne
    @JoinColumn(name = "procedimiento_padre_id")
    private Procedimiento procedimientoPadre;

    @Column(name = "tipo_procedimiento_id", nullable = false, insertable = false, updatable = false)
    @Convert(converter = TipoProcedimientoConverter.class)
    private TipoProcedimientoEnum tipoProcedimiento;

    @Column(name = "fecha_delete", nullable = true)
    private Date fechaDelete;

    @Column(name = "fecha_insert", nullable = false, columnDefinition = "timestamp default getDate()")
    private Date fechaInsert;

    @Column(name = "fecha_update", nullable = false, columnDefinition = "timestamp default getDate()")
    private Date fechaUpdate;

    @Column(name = "version", nullable = false, columnDefinition = "int4 default 1")
    private Integer version = new Integer(1);

    @Column(name = "es_excluyente", nullable = false)
    private Boolean esExcluyente = Boolean.FALSE;

    @Column(name = "tipo_ppm_id")
    private Integer tipoPPMId;

    //<editor-fold desc="Getters && Setters">
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getCodigoEmssanar() {
        return codigoEmssanar;
    }

    public void setCodigoEmssanar(String codigoEmssanar) {
        this.codigoEmssanar = codigoEmssanar;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getEdadIni() {
        return edadIni;
    }

    public void setEdadIni(Integer edadIni) {
        this.edadIni = edadIni;
    }

    public Integer getEdadFin() {
        return edadFin;
    }

    public void setEdadFin(Integer edadFin) {
        this.edadFin = edadFin;
    }

    public EstadoTecnologia getEstadoProcedimiento() {
        return estadoProcedimiento;
    }

    public void setEstadoProcedimiento(EstadoTecnologia estadoProcedimiento) {
        this.estadoProcedimiento = estadoProcedimiento;
    }

    public Integer getNivelAutorizacion() {
        return nivelAutorizacion;
    }

    public void setNivelAutorizacion(Integer nivelAutorizacion) {
        this.nivelAutorizacion = nivelAutorizacion;
    }

    public Integer getNivelComplejidad() {
        return nivelComplejidad;
    }

    public void setNivelComplejidad(Integer nivelComplejidad) {
        this.nivelComplejidad = nivelComplejidad;
    }

    public Long getTipoPagoRequeridoId() {
        return tipoPagoRequeridoId;
    }

    public void setTipoPagoRequeridoId(Long tipoPagoRequeridoId) {
        this.tipoPagoRequeridoId = tipoPagoRequeridoId;
    }

    public Integer getClientePk() {
        return clientePk;
    }

    public void setClientePk(Integer clientePk) {
        this.clientePk = clientePk;
    }

    public Boolean getRecobrable() {
        return recobrable;
    }

    public void setRecobrable(Boolean recobrable) {
        this.recobrable = recobrable;
    }

    public Boolean getQuirurgico() {
        return quirurgico;
    }

    public void setQuirurgico(Boolean quirurgico) {
        this.quirurgico = quirurgico;
    }

    public Boolean getInternacion() {
        return internacion;
    }

    public void setInternacion(Boolean internacion) {
        this.internacion = internacion;
    }

    public Boolean getSinAuditoriaNacional() {
        return sinAuditoriaNacional;
    }

    public void setSinAuditoriaNacional(Boolean sinAuditoriaNacional) {
        this.sinAuditoriaNacional = sinAuditoriaNacional;
    }

    public Procedimiento getProcedimientoPadre() {
        return procedimientoPadre;
    }

    public void setProcedimientoPadre(Procedimiento procedimientoPadre) {
        this.procedimientoPadre = procedimientoPadre;
    }

    public TipoProcedimientoEnum getTipoProcedimiento() {
        return tipoProcedimiento;
    }

    public void setTipoProcedimiento(TipoProcedimientoEnum tipoProcedimiento) {
        this.tipoProcedimiento = tipoProcedimiento;
    }

    public Date getFechaDelete() {
        return fechaDelete;
    }

    public void setFechaDelete(Date fechaDelete) {
        this.fechaDelete = fechaDelete;
    }

    public Date getFechaInsert() {
        return fechaInsert;
    }

    public void setFechaInsert(Date fechaInsert) {
        this.fechaInsert = fechaInsert;
    }

    public Date getFechaUpdate() {
        return fechaUpdate;
    }

    public void setFechaUpdate(Date fechaUpdate) {
        this.fechaUpdate = fechaUpdate;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Boolean getEsExcluyente() {
        return esExcluyente;
    }

    public void setEsExcluyente(Boolean esExcluyente) {
        this.esExcluyente = esExcluyente;
    }

    public Integer getTipoPPMId() {
        return tipoPPMId;
    }

    public void setTipoPPMId(Integer tipoPPMId) {
        this.tipoPPMId = tipoPPMId;
    }
    //</editor-fold>
}
