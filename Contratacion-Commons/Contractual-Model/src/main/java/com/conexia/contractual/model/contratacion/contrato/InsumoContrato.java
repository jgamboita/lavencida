package com.conexia.contractual.model.contratacion.contrato;

import com.conexia.contractual.model.maestros.Insumos;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Entidad para la tabla Insumo COntrato.
 * 
 * @author jlopez
 */
@Entity
@Table(schema = "contratacion", name = "insumo_contrato")
@NamedQueries({
    @NamedQuery(name = "InsumoContrato.findAll", query = "SELECT i FROM InsumoContrato i")})
public class InsumoContrato implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;
    
    @JoinColumn(name = "insumo_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Insumos insumo;
    
    @Column(name = "valor_contrato")
    private BigDecimal valorContrato;
    
    @Column(name = "requiere_autorizacion")
    private Boolean requiereAutorizacion;
    
    @Column(name = "estado")
    private int estado;
    
    @JoinColumn(name = "sede_contrato_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private SedeContrato sedeContrato;

    public InsumoContrato() {
    }

    public InsumoContrato(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Insumos getInsumo() {
        return insumo;
    }

    public void setInsumo(Insumos insumo) {
        this.insumo = insumo;
    }

    public BigDecimal getValorContrato() {
        return valorContrato;
    }

    public void setValorContrato(BigDecimal valorContrato) {
        this.valorContrato = valorContrato;
    }

    public Boolean getRequiereAutorizacion() {
        return requiereAutorizacion;
    }

    public void setRequiereAutorizacion(Boolean requiereAutorizacion) {
        this.requiereAutorizacion = requiereAutorizacion;
    }

    public int getEstado() {
        return estado;
    }

    public void setEstado(int estado) {
        this.estado = estado;
    }

    public SedeContrato getSedeContrato() {
        return sedeContrato;
    }

    public void setSedeContrato(SedeContrato sedeContrato) {
        this.sedeContrato = sedeContrato;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof InsumoContrato)) {
            return false;
        }
        InsumoContrato other = (InsumoContrato) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.conexia.contractual.model.contratacion.contrato.InsumoContrato[ id=" + id + " ]";
    }
    
}
