package com.conexia.contractual.model.contratacion.legalizacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.TipoObjetoContratoEnum;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.model.contratacion.contrato.Contrato;
import com.conexia.contractual.model.maestros.Municipio;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static javax.persistence.CascadeType.ALL;

/**
 *
 * @author jalvarado
 */
@Entity
@Table(name = "legalizacion_contrato", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name = "LegalizacionContrato.findLegalizacionContratoBySolicitudContratacion",
            query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoDto("
                    + "c.id, c.numeroContrato, c.regional.id, c.regional.codigo, c.regional.descripcion, c.tipoSubsidiado, c.tipoContrato,"
                    + " case when sc.negociacion.observacion='TECNOLOGÍA CON PRÓRROGA POR OTRO SI' then (( select cont.fechaInicio from Contrato cont where "
                    + " cont.solicitudContratacion.id=(select scon.id from SolicitudContratacion scon "
                    + " where scon.negociacion.id=:negociacionOrigenIde)) ) else (c.fechaInicio) end  as fechaInicio, "
                    + " case when sc.negociacion.observacion='TECNOLOGÍA CON PRÓRROGA POR OTRO SI' then (( select cont.fechaFin from Contrato cont where "
                    + " cont.solicitudContratacion.id=(select scon.id from SolicitudContratacion scon "
                    + " where scon.negociacion.id=:negociacionOrigenIde)) ) else (c.fechaFin) end  as fechaFin, "
                    + " c.nivelContrato, c.nombreArchivo, "
                    + " l.id, l.minuta.id, l.minuta.nombre, l.minuta.descripcion, l.objetoContrato, l.valorFiscal, l.valorPoliza, l.diasPlazo, "
                    + " l.municipioResponsable.departamento.id, l.municipioResponsable.id, l.fechaFirmaContrato, l.responsableFirmaContrato.id, "
                    + " l.municipio.departamento.id, l.municipio.departamento.descripcion, l.municipio.id, l.municipio.descripcion, l.direccion, "
                    + " l.responsableFirmaContrato.nombre,l.responsableFirmaContrato.municipioRecepcion, l.contenido, sc.estadoLegalizacion, c.fechaElaboracion, "
                    + " case when sc.negociacion.observacion='TECNOLOGÍA CON PRÓRROGA POR OTRO SI' then (( select cont.fechaInicio from Contrato cont where "
                    + " cont.solicitudContratacion.id=(select scon.id from SolicitudContratacion scon "
                    + " where scon.negociacion.id=:negociacionId)) ) when sc.negociacion.observacion='TECNOLOGÍA CON PRÓRROGA POR OTRO SI + TECNOLOGÍA MODIFICADA/CORREGIDA POR OTRO SI' "
                    + " then(( select cont.fechaInicioOtroSi from Contrato cont where "
                    + " cont.solicitudContratacion.id=(select scon.id from SolicitudContratacion scon "
                    + " where scon.negociacion.id=:negociacionPadreId)))"
                    + " else (c.fechaInicioOtroSi) end as fechaInicioOtroSi,"
                    + " case when sc.negociacion.observacion='TECNOLOGÍA CON PRÓRROGA POR OTRO SI' then (( select cont.fechaFin from Contrato cont where "
                    + " cont.solicitudContratacion.id=(select scon.id from SolicitudContratacion scon "
                    + " where scon.negociacion.id=:negociacionId)) ) when sc.negociacion.observacion='TECNOLOGÍA CON PRÓRROGA POR OTRO SI + TECNOLOGÍA MODIFICADA/CORREGIDA POR OTRO SI' "
                    + " then(( select cont.fechaFinOtroSi from Contrato cont where "
                    + "		 cont.solicitudContratacion.id=(select scon.id from SolicitudContratacion scon "
                    + " where scon.negociacion.id=:negociacionPadreId))) "
                    + " "
                    + " when sc.negociacion.negociacionOrigenId is not null then ("
                    + " case when sc.negociacion.observacion is null then (( select cont.fechaFinOtroSi from Contrato cont where "
                    + " cont.solicitudContratacion.id=(select scon.id from SolicitudContratacion scon "
                    + " where scon.negociacion.id=:negociacionOrigenIde))) end ) else (c.fechaFinOtroSi) end "
                    + ""
                    + ""
                    + ""
                    + " as fechaFinOtroSi,"
                    + " m.id, m.nombre, m.descripcion, l.observacionOtroSi, c.fechaElaboracionOtroSi, "
                    + " l.responsableFirmaContrato.tipoDocumento, l.responsableFirmaContrato.numeroDocumento, l.responsableFirmaContrato.lugarExpedicionDocumento) "
                    + " FROM LegalizacionContrato l "
                    + " join l.contrato c "
                    + " left join l.minutaOtroSi m "
                    + " join c.solicitudContratacion sc "
                    + " where sc.id = :solicitudContratacionId "
                    + " and c.solicitudContratacion.regimen = :regimen "
                    + " and c.tipoModalidad = :tipoModalidad"),
    @NamedQuery(name = "LegalizacionContrato.generacionMinutaPorId",
            query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.GeneracionMinutaDto("
            + "eps.razonSocial, p.nombre, c.nivelContrato, p.sitioWeb,"
            + "eps.nit, eps.digitoVerificacion, eps.representanteLegal, eps.tipoDocumentoRepresentanteLegal,"
            + "eps.documentoRepresentanteLegal,eps.ciudad, eps.sitioWeb, eps.direccion, eps.email, eps.telefono,eps.fax,"
            + "p.tipoIdentificacion.codigo, p.numeroDocumento, p.digitoVerificacion, cast(null as string) , p.representanteLegal,"
            + "tirl.codigo, p.numeroDocumentoRepresentanteLegal, p.correoElectronico, "
            + "p.telFijoContactoAdministrativo,'',c.poblacion, eps.cargoRepresentante, eps.regional.id, l.fechaVoBo) "
            + "FROM LegalizacionContrato l,Epss eps  "
            + "join l.contrato c "
            + "join l.prestador p "
            + "left join p.tipoIdentificacionRepLegal tirl "
            + "where l.id = :idLegalizacionContrato and eps.regional.id = :regionalId")
    ,
    @NamedQuery(name = "LegalizacionContrato.municipioLegalizacion",
            query = "SELECT m.descripcion from LegalizacionContrato lc "
            + "JOIN lc.municipio m JOIN lc.contrato c JOIN c.solicitudContratacion sc WHERE sc.id = :solicitudId ")
    ,
    @NamedQuery(name = "LegalizacionContrato.findById", query = "Select l from LegalizacionContrato l where l.id = :idLegalizacion")
    ,
    @NamedQuery(name = "LegalizacionContrato.findFirmaVistoBueno", query = "Select l.fechaVoBo, l.responsableVoBo from LegalizacionContrato l "
            + "WHERE l.id = :idLegalizacion ")
    ,
    @NamedQuery(name = "LegalizacionContrato.UpdateContenido", query = "Update LegalizacionContrato l "
            + "set l.contenido = :contenido where l.id = :idLegalizacion")
    ,
    @NamedQuery(name = "LegalizacionContrato.UpdateObservacionOtroSi", query = "Update LegalizacionContrato l "
            + "set l.observacionOtroSi = :observacion where l.id = :idLegalizacion")
    ,
    @NamedQuery(name = "LegalizacionContrato.updateFirmaVoBo", query = "UPDATE LegalizacionContrato l "
            + "SET l.fechaVoBo =:fechaVoBo, l.responsableVoBo.id = :responsableVoBo "
            + "WHERE l.id = :idLegalizacion ")
    ,
    @NamedQuery(name = "LegalizacionContrato.updateValor", query = "UPDATE LegalizacionContrato l "
            + " SET l.porcentajeTotalUpc =:porcentajeTotalUpc, l.porcentajeTotalPyp  =:porcentajeTotalPyp, l.valorTotalUpc =:valorTotalUpc, "
            + " l.valorTotalPyp =:valorTotalPyp, l.porcentajteTotalRecuperacion =:porcentajteTotalRecuperacion, " + " l.valorTotalRecuperacion =:valorTotalRecuperacion "
            + "WHERE l.id = :idLegalizacion")
    ,
    @NamedQuery(name = "LegalizacionContrato.findLegalizacionContratoByNegociacion",
            query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoDto("
                    + "c.id, c.numeroContrato, c.regional.id, c.regional.codigo, c.regional.descripcion, c.tipoSubsidiado, c.tipoContrato, c.fechaInicio, c.fechaFin, c.nivelContrato, c.nombreArchivo,"
                    + "l.id, l.minuta.id, l.minuta.nombre, l.minuta.descripcion, l.objetoContrato, l.valorFiscal, l.valorPoliza, l.diasPlazo,"
                    + "l.municipioResponsable.departamento.id, l.municipioResponsable.id, l.fechaFirmaContrato, l.responsableFirmaContrato.id,"
                    + "l.municipio.departamento.id, l.municipio.departamento.descripcion, l.municipio.id, l.municipio.descripcion, l.direccion,"
                    + "l.responsableFirmaContrato.nombre,l.responsableFirmaContrato.municipioRecepcion, l.contenido, sc.estadoLegalizacion, c.fechaElaboracion, c.fechaInicioOtroSi, c.fechaFinOtroSi, c.fechaElaboracionOtroSi,"
                    + "l.responsableFirmaContrato.tipoDocumento, l.responsableFirmaContrato.numeroDocumento, l.responsableFirmaContrato.lugarExpedicionDocumento) "
                    + "FROM LegalizacionContrato l "
                    + "join l.contrato c "
                    + "join c.solicitudContratacion sc "
                    + "where sc.negociacion.id = :negociacionId "
                    + "and c.solicitudContratacion.regimen = :regimen "
                    + "and c.tipoModalidad = :tipoModalidad")
    ,
    
      @NamedQuery(name = "LegalizacionContrato.findLegalizacionContratoByPrestador",
              query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoDto("
                      + "c.id, c.numeroContrato, c.regional.id, c.regional.codigo, c.regional.descripcion, c.tipoSubsidiado, c.tipoContrato, c.fechaInicio, c.fechaFin, c.nivelContrato, c.nombreArchivo,"
                      + "l.id, l.minuta.id, l.minuta.nombre, l.minuta.descripcion, l.objetoContrato, l.valorFiscal, l.valorPoliza, l.diasPlazo,"
                      + "l.municipioResponsable.departamento.id, l.municipioResponsable.id, l.fechaFirmaContrato, l.responsableFirmaContrato.id,"
                      + "l.municipio.departamento.id, l.municipio.departamento.descripcion, l.municipio.id, l.municipio.descripcion, l.direccion,"
                      + "l.responsableFirmaContrato.nombre,l.responsableFirmaContrato.municipioRecepcion, l.contenido, sc.estadoLegalizacion, c.fechaElaboracion, c.fechaInicioOtroSi, c.fechaFinOtroSi, c.fechaElaboracionOtroSi, "
                      + "l.responsableFirmaContrato.tipoDocumento, l.responsableFirmaContrato.numeroDocumento, l.responsableFirmaContrato.lugarExpedicionDocumento) "
                      + "FROM LegalizacionContrato l "
                      + "join l.contrato c "
                      + "join c.solicitudContratacion sc "
                      + "where sc.prestador.id = :prestadorId "
                      + "and c.solicitudContratacion.regimen = :regimen "
                      + "and c.tipoModalidad = :tipoModalidad")
})

public class LegalizacionContrato implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;

    @Size(max = 500)
    @Column(name = "direccion")
    private String direccion;

    @Basic(optional = false)
    @NotNull
    @Column(name = "valor_fiscal")
    private double valorFiscal;

    @Column(name = "valor_poliza")
    private Double valorPoliza;

    @Column(name = "dias_plazo")
    private Integer diasPlazo;

    @Basic(optional = false)
    @JoinColumn(name = "municipio_responsable_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Municipio municipioResponsable;

    @Basic(optional = false)
    @NotNull
    @Column(name = "fecha_firma_contrato")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaFirmaContrato;

    @Basic(optional = false)
    @Column(name = "fecha_vo_bo")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaVoBo;

    @Basic(optional = false)
    @NotNull
    @Column(name = "usuario_id")
    private Integer usuarioId;

    @JoinColumn(name = "contrato_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Contrato contrato;

    @JoinColumn(name = "minuta_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Minuta minuta;

    @JoinColumn(name = "minuta_otro_si_id", referencedColumnName = "id")
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private Minuta minutaOtroSi;

    @NotNull
    @Column(name = "objeto_contrato_id")
    private TipoObjetoContratoEnum objetoContrato;

    @JoinColumn(name = "prestador_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Prestador prestador;

    @JoinColumn(name = "responsable_firma_contrato_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ResponsableContrato responsableFirmaContrato;

    @JoinColumn(name = "responsable_vo_bo", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ResponsableContrato responsableVoBo;

    @OneToMany(cascade = ALL, mappedBy = "legalizacionContrato")
    private List<DescuentoLegalizacionContrato> descuestos;

    @Column(name = "porc_total_upc")
    private BigDecimal porcentajeTotalUpc;

    @Column(name = "porc_total_pyp")
    private BigDecimal porcentajeTotalPyp;

    @Column(name = "valor_total_upc")
    private Double valorTotalUpc;

    @Column(name = "valor_total_pyp")
    private Double valorTotalPyp;

    @Column(name = "porc_total_recuperacion")
    private BigDecimal porcentajteTotalRecuperacion;

    @Column(name = "valor_total_recuperacion")
    private Double valorTotalRecuperacion;

    @Column(name = "contenido")
    private String contenido;

    @Column(name = "deleted")
    private boolean deleted;

    @Column(name = "observacion_otro_si")
    private String observacionOtroSi;

    public LegalizacionContrato() {
    }

    public LegalizacionContrato(Long id) {
        this.id = id;
    }

    public LegalizacionContrato(Long id, int municipio, double valorFiscal, int municipioResponsable, Date fechaFirmaContrato, Integer usuarioId) {
        this.id = id;
        this.valorFiscal = valorFiscal;
        this.fechaFirmaContrato = fechaFirmaContrato;
        this.usuarioId = usuarioId;
    }

    //<editor-fold desc="Getters && Setters">
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public double getValorFiscal() {
        return valorFiscal;
    }

    public void setValorFiscal(double valorFiscal) {
        this.valorFiscal = valorFiscal;
    }

    public Double getValorPoliza() {
        return valorPoliza;
    }

    public void setValorPoliza(Double valorPoliza) {
        this.valorPoliza = valorPoliza;
    }

    public Integer getDiasPlazo() {
        return diasPlazo;
    }

    public void setDiasPlazo(Integer diasPlazo) {
        this.diasPlazo = diasPlazo;
    }

    public Municipio getMunicipioResponsableId() {
        return municipioResponsable;
    }

    public void setMunicipioResponsableId(Municipio municipioResponsable) {
        this.municipioResponsable = municipioResponsable;
    }

    public Date getFechaFirmaContrato() {
        return fechaFirmaContrato;
    }

    public void setFechaFirmaContrato(Date fechaFirmaContrato) {
        this.fechaFirmaContrato = fechaFirmaContrato;
    }

    public Date getFechaVoBo() {
        return fechaVoBo;
    }

    public void setFechaVoBo(Date fechaVoBo) {
        this.fechaVoBo = fechaVoBo;
    }

    public Integer getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Integer usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public void setContrato(Contrato contrato) {
        this.contrato = contrato;
    }

    public Minuta getMinuta() {
        return minuta;
    }

    public void setMinuta(Minuta minuta) {
        this.minuta = minuta;
    }

    public TipoObjetoContratoEnum getObjetoContrato() {
        return objetoContrato;
    }

    public void setObjetoContrato(TipoObjetoContratoEnum objetoContrato) {
        this.objetoContrato = objetoContrato;
    }

    public Prestador getPrestador() {
        return prestador;
    }

    public void setPrestador(Prestador prestador) {
        this.prestador = prestador;
    }

    public ResponsableContrato getResponsableFirmaContrato() {
        return responsableFirmaContrato;
    }

    public void setResponsableFirmaContrato(ResponsableContrato responsableFirmaContrato) {
        this.responsableFirmaContrato = responsableFirmaContrato;
    }

    public ResponsableContrato getResponsableVoBo() {
        return responsableVoBo;
    }

    public void setResponsableVoBo(ResponsableContrato responsableVoBo) {
        this.responsableVoBo = responsableVoBo;
    }

    /**
     * @return the descuestos
     */
    public List<DescuentoLegalizacionContrato> getDescuestos() {
        return descuestos;
    }

    /**
     * @param descuestos the descuestos to set
     */
    public void setDescuestos(List<DescuentoLegalizacionContrato> descuestos) {
        this.descuestos = descuestos;
    }

    /**
     * @return the contenido
     */
    public String getContenido() {
        return contenido;
    }

    /**
     * @param contenido the contenido to set
     */
    public void setContenido(String contenido) {
        this.contenido = contenido;
    }

    public BigDecimal getPorcentajeTotalUpc() {
        return porcentajeTotalUpc;
    }

    public void setPorcentajeTotalUpc(BigDecimal porcentajeTotalUpc) {
        this.porcentajeTotalUpc = porcentajeTotalUpc;
    }

    public BigDecimal getPorcentajeTotalPyp() {
        return porcentajeTotalPyp;
    }

    public void setPorcentajeTotalPyp(BigDecimal porcentajeTotalPyp) {
        this.porcentajeTotalPyp = porcentajeTotalPyp;
    }

    public Double getValorTotalUpc() {
        return valorTotalUpc;
    }

    public void setValorTotalUpc(Double valorTotalUpc) {
        this.valorTotalUpc = valorTotalUpc;
    }

    public Double getValorTotalPyp() {
        return valorTotalPyp;
    }

    public void setValorTotalPyp(Double valorTotalPyp) {
        this.valorTotalPyp = valorTotalPyp;
    }

    public BigDecimal getPorcentajteTotalRecuperacion() {
        return porcentajteTotalRecuperacion;
    }

    public void setPorcentajteTotalRecuperacion(BigDecimal porcentajteTotalRecuperacion) {
        this.porcentajteTotalRecuperacion = porcentajteTotalRecuperacion;
    }

    public Double getValorTotalRecuperacion() {
        return valorTotalRecuperacion;
    }

    public void setValorTotalRecuperacion(Double valorTotalRecuperacion) {
        this.valorTotalRecuperacion = valorTotalRecuperacion;
    }

    public Municipio getMunicipioResponsable() {
        return municipioResponsable;
    }

    public void setMunicipioResponsable(Municipio municipioResponsable) {
        this.municipioResponsable = municipioResponsable;
    }

    public Minuta getMinutaOtroSi() {
        return minutaOtroSi;
    }

    public void setMinutaOtroSi(Minuta minutaOtroSi) {
        this.minutaOtroSi = minutaOtroSi;
    }

    public String getObservacionOtroSi() {
        return observacionOtroSi;
    }

    public void setObservacionOtroSi(String observacionOtroSi) {
        this.observacionOtroSi = observacionOtroSi;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

	@Override
    public boolean equals(Object object) {
        if (!(object instanceof LegalizacionContrato)) {
            return false;
        }
        LegalizacionContrato other = (LegalizacionContrato) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.conexia.contractual.model.contratacion.legalizacion.LegalizacionContrato[ id=" + id + " ]";
    }
    //</editor-fold>

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

}
