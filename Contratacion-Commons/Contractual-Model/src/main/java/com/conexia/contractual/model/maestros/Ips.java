package com.conexia.contractual.model.maestros;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "ips", schema = "maestros")
public class Ips extends BaseMaestro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    @Column(name = "direccion", nullable = false, length = 100)
    private String direccion;
    @Column(name = "telefono", nullable = false, length = 50)
    private String telefono;
    @Column(name = "fax", nullable = false, length = 30)
    private String fax;
    @Column(name = "contacto", nullable = false, length = 50)
    private String contacto;
    @Column(name = "email", nullable = false, length = 50)
    private String email;
    @Column(name = "cod_minsalud", nullable = false, length = 20)
    private String codigoMinsalud;
    @Column(name = "digito_cheq", nullable = false, length = 20)
    private String digitoVerificacion;
    @Column(name = "razon_social", nullable = false, length = 100)
    private String razonSocial;
    @Column(name = "nombre_representante", nullable = false)
    private String nombreRepresentante;
    @Column(name = "codigo_ips", length = 20)
    private String codigoIPS;
    @Column(name = "numero_identificacion", nullable = false, length = 10)
    private String numeroIdentificacion;
    @Column(name = "cliente_pk")
    private Integer clientePk;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_identificacion_id", nullable = false)
    private TipoIdentificacion tipoIdentificacion;
    @OneToMany(mappedBy = "ips", fetch = FetchType.LAZY)
    private Set<SedeIps> sedes = new HashSet<>();

    //<editor-fold desc="Getters && Setters">
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCodigoMinsalud() {
        return codigoMinsalud;
    }

    public void setCodigoMinsalud(String codigoMinsalud) {
        this.codigoMinsalud = codigoMinsalud;
    }

    public String getDigitoVerificacion() {
        return digitoVerificacion;
    }

    public void setDigitoVerificacion(String digitoVerificacion) {
        this.digitoVerificacion = digitoVerificacion;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getNombreRepresentante() {
        return nombreRepresentante;
    }

    public void setNombreRepresentante(String nombreRepresentante) {
        this.nombreRepresentante = nombreRepresentante;
    }

    public String getCodigoIPS() {
        return codigoIPS;
    }

    public void setCodigoIPS(String codigoIPS) {
        this.codigoIPS = codigoIPS;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public Integer getClientePk() {
        return clientePk;
    }

    public void setClientePk(Integer clientePk) {
        this.clientePk = clientePk;
    }

    public TipoIdentificacion getTipoIdentificacion() {
        return tipoIdentificacion;
    }

    public void setTipoIdentificacion(TipoIdentificacion tipoIdentificacion) {
        this.tipoIdentificacion = tipoIdentificacion;
    }
    //</editor-fold>
}