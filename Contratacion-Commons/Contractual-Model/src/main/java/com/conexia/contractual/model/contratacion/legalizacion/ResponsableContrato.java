/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conexia.contractual.model.contratacion.legalizacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.TipoResponsableEnum;
import com.conexia.contractual.model.maestros.Regional;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 *
 * @author jalvarado
 */
@Entity
@Table(name = "responsable_contrato", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name = "ResponsableContrato.findResponsable", query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.ResponsableContratoDto("
            + "r.id, r.nombre, r.municipioRecepcion, r.tipoDocumento, r.numeroDocumento, r.lugarExpedicionDocumento) "
            + "FROM ResponsableContrato r "
            + "where r.regional.id = :idRegional and "
            + "r.tipoResponsable = :tipoResponsable "
            + "order by r.nombre")
})
public class ResponsableContrato implements Identifiable<Integer>, Serializable {
    private static long serialVersionUID = 1L;

    /**
     * @return the serialVersionUID
     */
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    /**
     * @param aSerialVersionUID the serialVersionUID to set
     */
    public static void setSerialVersionUID(long aSerialVersionUID) {
        serialVersionUID = aSerialVersionUID;
    }
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Integer id;
    
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(name = "nombre")
    private String nombre;
    
    @Basic(optional = false)
    @Column(name = "tipo_responsable")
    @Enumerated(EnumType.STRING)
    private TipoResponsableEnum tipoResponsable;
    
    @JoinColumn(name = "regional_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Regional regional;
    
    @Column(name = "municipio_recepcion")
    private String municipioRecepcion;

    @Column(name = "tipo_documento")
    private String tipoDocumento;

    @Column(name = "numero_documento")
    private String numeroDocumento;

    @Column(name = "lugar_expedicion_documento")
    private String lugarExpedicionDocumento;

    public ResponsableContrato() {
    }

    public ResponsableContrato(Integer id) {
        this.id = id;
    }

    public ResponsableContrato(Integer id, String nombre, TipoResponsableEnum tipoResponsable, Regional regional, String municipioRecepcion, String tipoDocumento, String numeroDocumento, String lugarExpedicionDocumento) {
        this.id = id;
        this.nombre = nombre;
        this.tipoResponsable = tipoResponsable;
        this.regional = regional;
        this.municipioRecepcion = municipioRecepcion;
        this.tipoDocumento = tipoDocumento;
        this.numeroDocumento = numeroDocumento;
        this.lugarExpedicionDocumento = lugarExpedicionDocumento;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoResponsableEnum getTipoResponsable() {
        return tipoResponsable;
    }

    public void setTipoResponsable(TipoResponsableEnum tipoResponsable) {
        this.tipoResponsable = tipoResponsable;
    }

    public Regional getRegional() {
        return regional;
    }

    public void setRegional(Regional regional) {
        this.regional = regional;
    }

    /**
     * @return the municipioRecepcion
     */
    public String getMunicipioRecepcion() {
        return municipioRecepcion;
    }

    /**
     * @param municipioRecepcion the municipioRecepcion to set
     */
    public void setMunicipioRecepcion(String municipioRecepcion) {
        this.municipioRecepcion = municipioRecepcion;
    }

    public String getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(String tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getLugarExpedicionDocumento() {
        return lugarExpedicionDocumento;
    }

    public void setLugarExpedicionDocumento(String lugarExpedicionDocumento) {
        this.lugarExpedicionDocumento = lugarExpedicionDocumento;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (getId() != null ? getId().hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof ResponsableContrato)) {
            return false;
        }
        ResponsableContrato other = (ResponsableContrato) object;
        if ((this.getId() == null && other.getId() != null) || (this.getId() != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.conexia.contractual.model.contratacion.legalizacion.ResponsableContrato[ id=" + getId() + " ]";
    }

}
