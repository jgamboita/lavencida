package com.conexia.contractual.model.maestros;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@Entity
@Table(name = "servicios_reps", schema = "maestros")
public class ServicioReps {
    @Id
    @Column(name = "id", nullable = false)
    private int id;
    @Basic
    @Column(name = "departamento_nombre", length = 150)
    private String departamentoNombre;
    @Basic
    @Column(name = "municipio_nombre", length = 150)
    private String municipioNombre;
    @Basic
    @Column(name = "habi_codigo_habilitacion", length = 50)
    private String habiCodigoHabilitacion;
    @Basic
    @Column(name = "codigo_habilitacion", length = 50)
    private String codigoHabilitacion;
    @Basic
    @Column(name = "numero_sede")
    private Integer numeroSede;
    @Basic
    @Column(name = "sede_nombre", length = 200)
    private String sedeNombre;
    @Basic
    @Column(name = "direccion", length = 150)
    private String direccion;
    @Basic
    @Column(name = "telefono", length = 200)
    private String telefono;
    @Basic
    @Column(name = "email", length = 200)
    private String email;
    @Basic
    @Column(name = "nits_nit", length = 20)
    private String nitsNit;
    @Basic
    @Column(name = "dv")
    private Integer dv;
    @Basic
    @Column(name = "clase_persona", length = 50)
    private String clasePersona;
    @Basic
    @Column(name = "codigo_naturaleza_juridica")
    private Integer codigoNaturalezaJuridica;
    @Basic
    @Column(name = "nombre_naturaleza_juridica", length = 100)
    private String nombreNaturalezaJuridica;
    @Basic
    @Column(name = "clpr_codigo")
    private Integer clprCodigo;
    @Basic
    @Column(name = "clpr_nombre", length = 200)
    private String clprNombre;
    @Basic
    @Column(name = "ese", length = 50)
    private String ese;
    @Basic
    @Column(name = "nivel", length = 10)
    private String nivel;
    @Basic
    @Column(name = "caracter", length = 50)
    private String caracter;
    @Basic
    @Column(name = "habilitado", length = 50)
    private String habilitado;
    @Basic
    @Column(name = "grse_codigo")
    private Integer grseCodigo;
    @Basic
    @Column(name = "grse_nombre", length = 200)
    private String grseNombre;
    @Basic
    @Column(name = "servicio_codigo")
    private Integer servicioCodigo;
    @Basic
    @Column(name = "servicio_nombre", length = 200)
    private String servicioNombre;
    @Basic
    @Column(name = "ambulatorio", length = 50)
    private String ambulatorio;
    @Basic
    @Column(name = "hospitalario", length = 50)
    private String hospitalario;
    @Basic
    @Column(name = "unidad_movil", length = 50)
    private String unidadMovil;
    @Basic
    @Column(name = "domiciliario", length = 50)
    private String domiciliario;
    @Basic
    @Column(name = "otras_estramural", length = 50)
    private String otrasEstramural;
    @Basic
    @Column(name = "centro_referencia", length = 50)
    private String centroReferencia;
    @Basic
    @Column(name = "institucion_remisora", length = 50)
    private String institucionRemisora;
    @Basic
    @Column(name = "complejidad_baja", length = 50)
    private String complejidadBaja;
    @Basic
    @Column(name = "complejidad_media", length = 50)
    private String complejidadMedia;
    @Basic
    @Column(name = "complejidad_alta", length = 50)
    private String complejidadAlta;
    @Basic
    @Column(name = "fecha_apertura", length = 50)
    private String fechaApertura;
    @Basic
    @Column(name = "fecha_cierre", length = 50)
    private String fechaCierre;
    @Basic
    @Column(name = "numero_distintivo", length = 50)
    private String numeroDistintivo;
    @Basic
    @Column(name = "numero_sede_principal")
    private Integer numeroSedePrincipal;
    @Basic
    @Column(name = "fecha_corte_reps", length = 50)
    private String fechaCorteReps;
    @Basic
    @Column(name = "fecha_insert", nullable = false)
    private Date fechaInsert;
    @Basic
    @Column(name = "ind_habilitado")
    private Boolean indHabilitado;

    //<editor-fold desc="Getters && Setters">
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDepartamentoNombre() {
        return departamentoNombre;
    }

    public void setDepartamentoNombre(String departamentoNombre) {
        this.departamentoNombre = departamentoNombre;
    }

    public String getMunicipioNombre() {
        return municipioNombre;
    }

    public void setMunicipioNombre(String municipioNombre) {
        this.municipioNombre = municipioNombre;
    }

    public String getHabiCodigoHabilitacion() {
        return habiCodigoHabilitacion;
    }

    public void setHabiCodigoHabilitacion(String habiCodigoHabilitacion) {
        this.habiCodigoHabilitacion = habiCodigoHabilitacion;
    }

    public String getCodigoHabilitacion() {
        return codigoHabilitacion;
    }

    public void setCodigoHabilitacion(String codigoHabilitacion) {
        this.codigoHabilitacion = codigoHabilitacion;
    }

    public Integer getNumeroSede() {
        return numeroSede;
    }

    public void setNumeroSede(Integer numeroSede) {
        this.numeroSede = numeroSede;
    }

    public String getSedeNombre() {
        return sedeNombre;
    }

    public void setSedeNombre(String sedeNombre) {
        this.sedeNombre = sedeNombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNitsNit() {
        return nitsNit;
    }

    public void setNitsNit(String nitsNit) {
        this.nitsNit = nitsNit;
    }

    public Integer getDv() {
        return dv;
    }

    public void setDv(Integer dv) {
        this.dv = dv;
    }

    public String getClasePersona() {
        return clasePersona;
    }

    public void setClasePersona(String clasePersona) {
        this.clasePersona = clasePersona;
    }

    public Integer getCodigoNaturalezaJuridica() {
        return codigoNaturalezaJuridica;
    }

    public void setCodigoNaturalezaJuridica(Integer codigoNaturalezaJuridica) {
        this.codigoNaturalezaJuridica = codigoNaturalezaJuridica;
    }

    public String getNombreNaturalezaJuridica() {
        return nombreNaturalezaJuridica;
    }

    public void setNombreNaturalezaJuridica(String nombreNaturalezaJuridica) {
        this.nombreNaturalezaJuridica = nombreNaturalezaJuridica;
    }

    public Integer getClprCodigo() {
        return clprCodigo;
    }

    public void setClprCodigo(Integer clprCodigo) {
        this.clprCodigo = clprCodigo;
    }

    public String getClprNombre() {
        return clprNombre;
    }

    public void setClprNombre(String clprNombre) {
        this.clprNombre = clprNombre;
    }

    public String getEse() {
        return ese;
    }

    public void setEse(String ese) {
        this.ese = ese;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getCaracter() {
        return caracter;
    }

    public void setCaracter(String caracter) {
        this.caracter = caracter;
    }

    public String getHabilitado() {
        return habilitado;
    }

    public void setHabilitado(String habilitado) {
        this.habilitado = habilitado;
    }

    public Integer getGrseCodigo() {
        return grseCodigo;
    }

    public void setGrseCodigo(Integer grseCodigo) {
        this.grseCodigo = grseCodigo;
    }

    public String getGrseNombre() {
        return grseNombre;
    }

    public void setGrseNombre(String grseNombre) {
        this.grseNombre = grseNombre;
    }

    public Integer getServicioCodigo() {
        return servicioCodigo;
    }

    public void setServicioCodigo(Integer servicioCodigo) {
        this.servicioCodigo = servicioCodigo;
    }

    public String getServicioNombre() {
        return servicioNombre;
    }

    public void setServicioNombre(String servicioNombre) {
        this.servicioNombre = servicioNombre;
    }

    public String getAmbulatorio() {
        return ambulatorio;
    }

    public void setAmbulatorio(String ambulatorio) {
        this.ambulatorio = ambulatorio;
    }

    public String getHospitalario() {
        return hospitalario;
    }

    public void setHospitalario(String hospitalario) {
        this.hospitalario = hospitalario;
    }

    public String getUnidadMovil() {
        return unidadMovil;
    }

    public void setUnidadMovil(String unidadMovil) {
        this.unidadMovil = unidadMovil;
    }

    public String getDomiciliario() {
        return domiciliario;
    }

    public void setDomiciliario(String domiciliario) {
        this.domiciliario = domiciliario;
    }

    public String getOtrasEstramural() {
        return otrasEstramural;
    }

    public void setOtrasEstramural(String otrasEstramural) {
        this.otrasEstramural = otrasEstramural;
    }

    public String getCentroReferencia() {
        return centroReferencia;
    }

    public void setCentroReferencia(String centroReferencia) {
        this.centroReferencia = centroReferencia;
    }

    public String getInstitucionRemisora() {
        return institucionRemisora;
    }

    public void setInstitucionRemisora(String institucionRemisora) {
        this.institucionRemisora = institucionRemisora;
    }

    public String getComplejidadBaja() {
        return complejidadBaja;
    }

    public void setComplejidadBaja(String complejidadBaja) {
        this.complejidadBaja = complejidadBaja;
    }

    public String getComplejidadMedia() {
        return complejidadMedia;
    }

    public void setComplejidadMedia(String complejidadMedia) {
        this.complejidadMedia = complejidadMedia;
    }

    public String getComplejidadAlta() {
        return complejidadAlta;
    }

    public void setComplejidadAlta(String complejidadAlta) {
        this.complejidadAlta = complejidadAlta;
    }

    public String getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(String fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public String getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(String fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public String getNumeroDistintivo() {
        return numeroDistintivo;
    }

    public void setNumeroDistintivo(String numeroDistintivo) {
        this.numeroDistintivo = numeroDistintivo;
    }

    public Integer getNumeroSedePrincipal() {
        return numeroSedePrincipal;
    }

    public void setNumeroSedePrincipal(Integer numeroSedePrincipal) {
        this.numeroSedePrincipal = numeroSedePrincipal;
    }

    public String getFechaCorteReps() {
        return fechaCorteReps;
    }

    public void setFechaCorteReps(String fechaCorteReps) {
        this.fechaCorteReps = fechaCorteReps;
    }

    public Date getFechaInsert() {
        return fechaInsert;
    }

    public void setFechaInsert(Date fechaInsert) {
        this.fechaInsert = fechaInsert;
    }

    public Boolean getIndHabilitado() {
        return indHabilitado;
    }

    public void setIndHabilitado(Boolean indHabilitado) {
        this.indHabilitado = indHabilitado;
    }
    //</editor-fold>

    //<editor-fold desc="Equals && HashCode">
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServicioReps that = (ServicioReps) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    //</editor-fold>
}

