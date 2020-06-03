package com.conexia.contractual.model.maestros;

import com.conexia.contratacion.commons.constants.enums.EstadoMaestroEnum;
import com.conexia.contractual.model.contratacion.converter.EstadoMaestroConverter;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;


/**
 * The persistent class for the sede_ips database table.
 */
@Entity
@Table(name = "sede_ips", schema = "maestros")
@NamedQueries({
        @NamedQuery(name = "SedeIps.findAll", query = "SELECT s FROM SedeIps s"),
        @NamedQuery(name = "SedeIps.buscarSedeIpsPorCodigoHabilitacion", query = "SELECT s  FROM SedeIps s  WHERE s.codigoHabilitacion = :codigoHabilitacion "),
})

public class SedeIps implements Serializable {

    @Id
    private Integer id;

    @Column(name = "aplica_eutanasia")
    private Boolean aplicaEutanasia;

    @Column(name = "aplica_ive")
    private Boolean aplicaIve;

    @Column(name = "cliente_pk")
    private Integer clientePk;

    @Column(name = "codigo_habilitacion")
    private String codigoHabilitacion;

    @Column(name = "codigo_min_salud")
    private String codigoMinSalud;

    @Column(name = "codigo_sede_ips")
    private String codigoSedeIps;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departamento_id", nullable = false)
    private Departamento departamentoId;

    private String direccion;

    private String director;

    private String email;

    @Column(name = "eps_id")
    private Integer epsId;

    @Column(name = "es_epsifarma")
    private Integer esEpsifarma;

    @Column(name = "es_especialista")
    private Integer esEspecialista;

    @Column(name = "es_red")
    private Boolean esRed;

    @Column(name = "es_sede_principal")
    private Boolean esSedePrincipal;

    @Column(name = "estado_ips_id")
    @Convert(converter = EstadoMaestroConverter.class)
    private EstadoMaestroEnum estadoIps;

    @Column(name = "estado_sede_ips_id")
    @Convert(converter = EstadoMaestroConverter.class)
    private EstadoMaestroEnum estadoSedeIps;

    @Column(name = "fecha_habilitacion")
    private Timestamp fechaHabilitacion;

    @Column(name = "fecha_inactivacion")
    private Timestamp fechaInactivacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ips_id")
    private Ips ips;

    @Column(name = "localidad_id")
    private Integer localidadId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id", nullable = false)
    private Municipio municipioId;

    @Column(name = "nivel_atencion_enum")
    private Integer nivelAtencionEnum;

    @Column(name = "nivel_de_atencion")
    private Integer nivelDeAtencion;

    private String nombre;

    @Column(name = "numero_sede")
    private String numeroSede;

    @Column(name = "pag_web")
    private String pagWeb;

    @Column(name = "regimen_juridico_id")
    private Integer regimenJuridicoId;

    @Column(name = "regimen_tributario_id")
    private Integer regimenTributarioId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regional_id", nullable = false)
    private Regional regionalId;

    @Column(name = "telefono_adm")
    private String telefonoAdm;

    @Column(name = "telefono_citas")
    private String telefonoCitas;

    @Column(name = "telefono_otros")
    private String telefonoOtros;

    private String telefono1;

    private String telefono2;

    @Column(name = "tipo_categoria")
    private String tipoCategoria;

    @Column(name = "tipo_ips_id")
    private Integer tipoIpsId;

    @Column(name = "tipo_servicio_id")
    private Integer tipoServicioId;

    @Column(name = "ubicacion_id")
    private Integer ubicacionId;

    private Integer version;

    public SedeIps() {
    }

    public Integer getId() {
        return this.id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Boolean getAplicaEutanasia() {
        return this.aplicaEutanasia;
    }

    public void setAplicaEutanasia(Boolean aplicaEutanasia) {
        this.aplicaEutanasia = aplicaEutanasia;
    }

    public Boolean getAplicaIve() {
        return this.aplicaIve;
    }

    public void setAplicaIve(Boolean aplicaIve) {
        this.aplicaIve = aplicaIve;
    }

    public Integer getClientePk() {
        return this.clientePk;
    }

    public void setClientePk(Integer clientePk) {
        this.clientePk = clientePk;
    }

    public String getCodigoHabilitacion() {
        return this.codigoHabilitacion;
    }

    public void setCodigoHabilitacion(String codigoHabilitacion) {
        this.codigoHabilitacion = codigoHabilitacion;
    }

    public String getCodigoMinSalud() {
        return this.codigoMinSalud;
    }

    public void setCodigoMinSalud(String codigoMinSalud) {
        this.codigoMinSalud = codigoMinSalud;
    }

    public String getCodigoSedeIps() {
        return this.codigoSedeIps;
    }

    public void setCodigoSedeIps(String codigoSedeIps) {
        this.codigoSedeIps = codigoSedeIps;
    }

    public Departamento getDepartamentoId() {
        return this.departamentoId;
    }

    public void setDepartamentoId(Departamento departamentoId) {
        this.departamentoId = departamentoId;
    }

    public String getDireccion() {
        return this.direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getDirector() {
        return this.director;
    }

    public void setDirector(String director) {
        this.director = director;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getEpsId() {
        return this.epsId;
    }

    public void setEpsId(Integer epsId) {
        this.epsId = epsId;
    }

    public Integer getEsEpsifarma() {
        return this.esEpsifarma;
    }

    public void setEsEpsifarma(Integer esEpsifarma) {
        this.esEpsifarma = esEpsifarma;
    }

    public Integer getEsEspecialista() {
        return this.esEspecialista;
    }

    public void setEsEspecialista(Integer esEspecialista) {
        this.esEspecialista = esEspecialista;
    }

    public Boolean getEsRed() {
        return this.esRed;
    }

    public void setEsRed(Boolean esRed) {
        this.esRed = esRed;
    }

    public Boolean getEsSedePrincipal() {
        return this.esSedePrincipal;
    }

    public void setEsSedePrincipal(Boolean esSedePrincipal) {
        this.esSedePrincipal = esSedePrincipal;
    }

    public EstadoMaestroEnum getEstadoIps() {
        return this.estadoIps;
    }

    public void setEstadoIps(EstadoMaestroEnum estadoIps) {
        this.estadoIps = estadoIps;
    }

    public EstadoMaestroEnum getEstadoSedeIps() {
        return this.estadoSedeIps;
    }

    public void setEstadoSedeIps(EstadoMaestroEnum estadoSedeIps) {
        this.estadoSedeIps = estadoSedeIps;
    }

    public Timestamp getFechaHabilitacion() {
        return this.fechaHabilitacion;
    }

    public void setFechaHabilitacion(Timestamp fechaHabilitacion) {
        this.fechaHabilitacion = fechaHabilitacion;
    }

    public Timestamp getFechaInactivacion() {
        return this.fechaInactivacion;
    }

    public void setFechaInactivacion(Timestamp fechaInactivacion) {
        this.fechaInactivacion = fechaInactivacion;
    }

    public Ips getIps() {
        return this.ips;
    }

    public void setIps(Ips ips) {
        this.ips = ips;
    }

    public Integer getLocalidadId() {
        return this.localidadId;
    }

    public void setLocalidadId(Integer localidadId) {
        this.localidadId = localidadId;
    }

    public Municipio getMunicipioId() {
        return this.municipioId;
    }

    public void setMunicipioId(Municipio municipioId) {
        this.municipioId = municipioId;
    }

    public Integer getNivelAtencionEnum() {
        return this.nivelAtencionEnum;
    }

    public void setNivelAtencionEnum(Integer nivelAtencionEnum) {
        this.nivelAtencionEnum = nivelAtencionEnum;
    }

    public Integer getNivelDeAtencion() {
        return this.nivelDeAtencion;
    }

    public void setNivelDeAtencion(Integer nivelDeAtencion) {
        this.nivelDeAtencion = nivelDeAtencion;
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumeroSede() {
        return this.numeroSede;
    }

    public void setNumeroSede(String numeroSede) {
        this.numeroSede = numeroSede;
    }

    public String getPagWeb() {
        return this.pagWeb;
    }

    public void setPagWeb(String pagWeb) {
        this.pagWeb = pagWeb;
    }

    public Integer getRegimenJuridicoId() {
        return this.regimenJuridicoId;
    }

    public void setRegimenJuridicoId(Integer regimenJuridicoId) {
        this.regimenJuridicoId = regimenJuridicoId;
    }

    public Integer getRegimenTributarioId() {
        return this.regimenTributarioId;
    }

    public void setRegimenTributarioId(Integer regimenTributarioId) {
        this.regimenTributarioId = regimenTributarioId;
    }

    public Regional getRegionalId() {
        return this.regionalId;
    }

    public void setRegionalId(Regional regionalId) {
        this.regionalId = regionalId;
    }

    public String getTelefonoAdm() {
        return this.telefonoAdm;
    }

    public void setTelefonoAdm(String telefonoAdm) {
        this.telefonoAdm = telefonoAdm;
    }

    public String getTelefonoCitas() {
        return this.telefonoCitas;
    }

    public void setTelefonoCitas(String telefonoCitas) {
        this.telefonoCitas = telefonoCitas;
    }

    public String getTelefonoOtros() {
        return this.telefonoOtros;
    }

    public void setTelefonoOtros(String telefonoOtros) {
        this.telefonoOtros = telefonoOtros;
    }

    public String getTelefono1() {
        return this.telefono1;
    }

    public void setTelefono1(String telefono1) {
        this.telefono1 = telefono1;
    }

    public String getTelefono2() {
        return this.telefono2;
    }

    public void setTelefono2(String telefono2) {
        this.telefono2 = telefono2;
    }

    public String getTipoCategoria() {
        return this.tipoCategoria;
    }

    public void setTipoCategoria(String tipoCategoria) {
        this.tipoCategoria = tipoCategoria;
    }

    public Integer getTipoIpsId() {
        return this.tipoIpsId;
    }

    public void setTipoIpsId(Integer tipoIpsId) {
        this.tipoIpsId = tipoIpsId;
    }

    public Integer getTipoServicioId() {
        return this.tipoServicioId;
    }

    public void setTipoServicioId(Integer tipoServicioId) {
        this.tipoServicioId = tipoServicioId;
    }

    public Integer getUbicacionId() {
        return this.ubicacionId;
    }

    public void setUbicacionId(Integer ubicacionId) {
        this.ubicacionId = ubicacionId;
    }

    public Integer getVersion() {
        return this.version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

}