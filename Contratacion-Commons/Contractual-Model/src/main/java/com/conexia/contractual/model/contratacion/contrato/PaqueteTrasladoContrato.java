package com.conexia.contractual.model.contratacion.contrato;

import com.conexia.contractual.model.maestros.Procedimientos;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 *
 * @author jlopez
 */
@Entity
@Table(schema = "contratacion", name = "paquete_traslado_contrato")
@NamedNativeQueries({
    @NamedNativeQuery(name = "PaqueteTrasladoContrato.insertPaqueteTrasladoContrato",
            query = "INSERT INTO contratacion.paquete_traslado_contrato "
            		+ " (paquete_contrato_id, traslado_id, cantidad) "
            		+ " SELECT pc.id, :trasladoId, :cantidad "
            		+ " FROM maestros.procedimiento p "
            		+ " JOIN contratacion.paquete_contrato pc on pc.paquete_id = p.id AND pc.sede_contrato_id = :sedeContratoId "
            		+ " WHERE p.codigo_emssanar = :codigoEmssanar "
            		+ " AND NOT EXISTS (SELECT NULL FROM contratacion.paquete_traslado_contrato "
            		+ " 				WHERE paquete_contrato_id = pc.id "
            		+ "					AND traslado_id = :trasladoId) ")
})
public class PaqueteTrasladoContrato implements Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "traslado_id")
    private Procedimientos traslado;
    
    @Column(name = "cantidad")
    private Integer cantidad;
    
    @JoinColumn(name = "paquete_contrato_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private PaqueteContrato paqueteContrato;

    public PaqueteTrasladoContrato() {
    }

    public PaqueteTrasladoContrato(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Procedimientos getTraslado() {
        return traslado;
    }

    public void setTraslado(Procedimientos traslado) {
        this.traslado = traslado;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public PaqueteContrato getPaqueteContrato() {
        return paqueteContrato;
    }

    public void setPaqueteContrato(PaqueteContrato paqueteContrato) {
        this.paqueteContrato = paqueteContrato;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PaqueteTrasladoContrato that = (PaqueteTrasladoContrato) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
