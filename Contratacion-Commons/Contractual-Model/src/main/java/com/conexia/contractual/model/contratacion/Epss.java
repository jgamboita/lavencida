package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.maestros.Regional;
import com.conexia.contractual.model.maestros.TipoIdentificacion;

import javax.persistence.*;


/**
 * Entity EPS.
 *
 * @author jalvarado
 */
@Entity
@Table(name = "epss", schema = "contratacion")
public class Epss implements Identifiable<Integer> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Integer id;

    @Column(name = "razon_social", nullable = false, length = 100)
    private String razonSocial;

    @ManyToOne
    @JoinColumn(name = "tipo_identificacion_id", nullable = false)
    private TipoIdentificacion tipoIdentificacion;

    @Column(name = "numero_identificacion", nullable = false)
    private String numeroIdentificacion;

    @Column(name = "nit", length = 50)
    private String nit;
    
    @Column(name = "digito_verificacion", length = 3)
    private String digitoVerificacion;
    
    @Column(name = "representante_legal", length = 120)
    private String representanteLegal;
    
    @Column(name = "documento_representante_legal", length = 20)
    private String documentoRepresentanteLegal;
    
    @Column(name = "tipo_documento_representante_legal", length = 4)
    private String tipoDocumentoRepresentanteLegal;
    
    @Column(name = "ciudad", length = 30)
    private String ciudad;
    
    @Column(name = "sitio_web", length = 50)
    private String sitioWeb;
    
    @Column(name = "direccion", length = 50)
    private String direccion;
    
    @Column(name = "email", length = 50)
    private String email;
    
    @Column(name = "telefono", length = 50)
    private String telefono;
    
    @Column(name = "fax", length = 50)
    private String fax;
    
    @Column(name = "cargo_representante", length = 100)
    private String cargoRepresentante;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regional_id")
    private Regional regional;
    
    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
    /**
     * Devuelve el valor de id.
     *
     * @return El valor de id.
     */
    public Integer getId() {
        return this.id;
    }
    
    
    /**
     * Asigna un nuevo valor a id.
     *
     * @param id El valor a asignar a id.
     */
    public void setId(Integer id) {
        this.id = id;
    }
    
    
    /**
     * Devuelve el valor de razonSocial.
     *
     * @return El valor de razonSocial.
     */
    public String getRazonSocial() {
        return this.razonSocial;
    }
    
    
    /**
     * Asigna un nuevo valor a razonSocial.
     *
     * @param razonSocial El valor a asignar a razonSocial.
     */
    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }
    
    
    /**
     * Devuelve el valor de tipoIdentificacion.
     *
     * @return El valor de tipoIdentificacion.
     */
    public TipoIdentificacion getTipoIdentificacion() {
        return this.tipoIdentificacion;
    }
    
    
    /**
     * Asigna un nuevo valor a tipoIdentificacion.
     *
     * @param tipoIdentificacion El valor a asignar a tipoIdentificacion.
     */
    public void setTipoIdentificacion(TipoIdentificacion tipoIdentificacion) {
        this.tipoIdentificacion = tipoIdentificacion;
    }
    
    
    /**
     * Devuelve el valor de numeroIdentificacion.
     *
     * @return El valor de numeroIdentificacion.
     */
    public String getNumeroIdentificacion() {
        return this.numeroIdentificacion;
    }
    
    
    /**
     * Asigna un nuevo valor a numeroIdentificacion.
     *
     * @param numeroIdentificacion El valor a asignar a numeroIdentificacion.
     */
    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }
    
    /**
     * @return the nit
     */
    public String getNit() {
        return nit;
    }
    
    /**
     * @param nit the nit to set
     */
    public void setNit(String nit) {
        this.nit = nit;
    }
    
    /**
     * @return the digitoVerificacion
     */
    public String getDigitoVerificacion() {
        return digitoVerificacion;
    }
    
    /**
     * @param digitoVerificacion the digitoVerificacion to set
     */
    public void setDigitoVerificacion(String digitoVerificacion) {
        this.digitoVerificacion = digitoVerificacion;
    }
    
    /**
     * @return the representanteLegal
     */
    public String getRepresentanteLegal() {
        return representanteLegal;
    }
    
    /**
     * @param representanteLegal the representanteLegal to set
     */
    public void setRepresentanteLegal(String representanteLegal) {
        this.representanteLegal = representanteLegal;
    }
    
    /**
     * @return the documentoRepresentanteLegal
     */
    public String getDocumentoRepresentanteLegal() {
        return documentoRepresentanteLegal;
    }
    
    /**
     * @param documentoRepresentanteLegal the documentoRepresentanteLegal to set
     */
    public void setDocumentoRepresentanteLegal(String documentoRepresentanteLegal) {
        this.documentoRepresentanteLegal = documentoRepresentanteLegal;
    }
    
    /**
     * @return the tipoDocumentoRepresentanteLegal
     */
    public String getTipoDocumentoRepresentanteLegal() {
        return tipoDocumentoRepresentanteLegal;
    }
    
    /**
     * @param tipoDocumentoRepresentanteLegal the tipoDocumentoRepresentanteLegal to set
     */
    public void setTipoDocumentoRepresentanteLegal(String tipoDocumentoRepresentanteLegal) {
        this.tipoDocumentoRepresentanteLegal = tipoDocumentoRepresentanteLegal;
    }
    
    /**
     * @return the ciudad
     */
    public String getCiudad() {
        return ciudad;
    }
    
    /**
     * @param ciudad the ciudad to set
     */
    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }
    
    /**
     * @return the sitioWeb
     */
    public String getSitioWeb() {
        return sitioWeb;
    }
    
    /**
     * @param sitioWeb the sitioWeb to set
     */
    public void setSitioWeb(String sitioWeb) {
        this.sitioWeb = sitioWeb;
    }
    
    /**
     * @return the direccion
     */
    public String getDireccion() {
        return direccion;
    }
    
    /**
     * @param direccion the direccion to set
     */
    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }
    
    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }
    
    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /**
     * @return the telefono
     */
    public String getTelefono() {
        return telefono;
    }
    
    /**
     * @param telefono the telefono to set
     */
    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }
    
    /**
     * @return the fax
     */
    public String getFax() {
        return fax;
    }
    
    /**
     * @param fax the fax to set
     */
    public void setFax(String fax) {
        this.fax = fax;
    }
    
    /**
     * @return the cargoRepresentante
     */
    public String getCargoRepresentante() {
        return cargoRepresentante;
    }

    /**
     * @param cargoRepresentante the cargoRepresentante to set
     */
    public void setCargoRepresentante(String cargoRepresentante) {
        this.cargoRepresentante = cargoRepresentante;
    }


	public Regional getRegional() {
		return regional;
	}


	public void setRegional(Regional regional) {
		this.regional = regional;
	}
    
    
    
//</editor-fold>

}
