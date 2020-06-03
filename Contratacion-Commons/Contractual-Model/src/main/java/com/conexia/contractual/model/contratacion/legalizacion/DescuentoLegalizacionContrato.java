package com.conexia.contractual.model.contratacion.legalizacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.TipoCondicionEnum;
import com.conexia.contratacion.commons.constants.enums.TipoDescuentoEnum;
import com.conexia.contratacion.commons.constants.enums.TipoValorEnum;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 *
 * @author jalvarado
 */
@Entity
@Table(name = "descuento_legalizacion_contrato", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name = "DescuentoLegalizacionContrato.findAll", query = "SELECT d FROM DescuentoLegalizacionContrato d"),
    @NamedQuery(name = "DescuentoLegalizacionContrato.findDtoByLegalizacionContratoId",
            query = "select new com.conexia.contratacion.commons.dto.contractual.legalizacion.DescuentoDto("
            + "d.id, d.valorCondicion, d.valorDescuento, d.detalle, d.tipoDescuento, "
            + "d.tipoCondicion, d.tipoValor) "
            + "from DescuentoLegalizacionContrato d "
            + "where d.legalizacionContrato.id = :legalizacionContratoId "),
    @NamedQuery(name = "DescuentoLegalizacionContrato.findByIdLegalizacion",
            query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.DescuentoDto(d.id, d.valorCondicion, d.valorDescuento,"
            + "d.detalle, d.tipoDescuento, d.tipoCondicion, d.tipoValor) "
            + "FROM DescuentoLegalizacionContrato d join d.legalizacionContrato l WHERE l.id = :idLegalizacion")})
public class DescuentoLegalizacionContrato implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "tipo_descuento_id")
    private TipoDescuentoEnum tipoDescuento;

    @Column(name = "valor_condicion")
    private Double valorCondicion;

    @NotNull
    @Column(name = "tipo_condicion_id")
    private TipoCondicionEnum tipoCondicion;

    @Column(name = "valor_descuento")
    private Double valorDescuento;

    @NotNull
    @Column(name = "tipo_valor_id")
    private TipoValorEnum tipoValor;

    @Size(max = 2147483647)
    @Column(name = "detalle")
    private String detalle;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "legalizacion_contrato_id")
    private LegalizacionContrato legalizacionContrato;

    public DescuentoLegalizacionContrato() {
    }

    public DescuentoLegalizacionContrato(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getValorCondicion() {
        return valorCondicion;
    }

    public void setValorCondicion(Double valorCondicion) {
        this.valorCondicion = valorCondicion;
    }

    public Double getValorDescuento() {
        return valorDescuento;
    }

    public void setValorDescuento(Double valorDescuento) {
        this.valorDescuento = valorDescuento;
    }

    public String getDetalle() {
        return detalle;
    }

    public void setDetalle(String detalle) {
        this.detalle = detalle;
    }

    /**
     * @return the tipoDescuento
     */
    public TipoDescuentoEnum getTipoDescuento() {
        return tipoDescuento;
    }

    /**
     * @param tipoDescuento the tipoDescuento to set
     */
    public void setTipoDescuento(TipoDescuentoEnum tipoDescuento) {
        this.tipoDescuento = tipoDescuento;
    }

    /**
     * @return the tipoCondicion
     */
    public TipoCondicionEnum getTipoCondicion() {
        return tipoCondicion;
    }

    /**
     * @param tipoCondicion the tipoCondicion to set
     */
    public void setTipoCondicion(TipoCondicionEnum tipoCondicion) {
        this.tipoCondicion = tipoCondicion;
    }

    /**
     * @return the tipoValor
     */
    public TipoValorEnum getTipoValor() {
        return tipoValor;
    }

    /**
     * @param tipoValor the tipoValor to set
     */
    public void setTipoValor(TipoValorEnum tipoValor) {
        this.tipoValor = tipoValor;
    }

    /**
     * @return the legalizacionContrato
     */
    public LegalizacionContrato getLegalizacionContrato() {
        return legalizacionContrato;
    }

    /**
     * @param legalizacionContrato the legalizacionContrato to set
     */
    public void setLegalizacionContrato(LegalizacionContrato legalizacionContrato) {
        this.legalizacionContrato = legalizacionContrato;
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
        if (!(object instanceof DescuentoLegalizacionContrato)) {
            return false;
        }
        DescuentoLegalizacionContrato other = (DescuentoLegalizacionContrato) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.conexia.contractual.model.contratacion.legalizacion.DescuentoLegalizacionContrato[ id=" + id + " ]";
    }

}
