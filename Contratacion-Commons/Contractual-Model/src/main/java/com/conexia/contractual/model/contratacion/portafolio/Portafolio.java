package com.conexia.contractual.model.contratacion.portafolio;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.contratacion.GrupoServicio;
import com.conexia.contractual.model.contratacion.ProcedimientoPaquete;
import com.conexia.contractual.model.contratacion.SedePrestador;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "portafolio", schema = "contratacion")
public class Portafolio implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "fecha_insert")
    private Timestamp fechaInsert;

    @Column(name = "fecha_update")
    private Timestamp fechaUpdate;

    @OneToOne(mappedBy = "portafolio")
    private SedePrestador sedePrestador;

    @Column(name = "eps_id")
    private Long eps;
    
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "portafolio_padre_id", nullable = true, insertable = false, updatable = false)
    private List<Portafolio> portafolioPadre;

    //bi-directional many-to-one association to GrupoServicio
    @OneToMany(mappedBy = "portafolio")
    private List<GrupoServicio> grupoServicios;

    @OneToMany(mappedBy = "portafolio")
    private Set<InsumoPortafolio> insumosPortafolios;
    
    //bi-directional many-to-one association to MedicamentoPortafolio
    @OneToMany(mappedBy = "portafolio")
    private List<MedicamentoPortafolio> medicamentoPortafolios;

    //bi-directional many-to-one association to PaquetePortafolio
    @OneToMany(mappedBy = "portafolio")
    private List<PaquetePortafolio> paquetePortafolios;
    
    @OneToMany(mappedBy = "portafolio")
    private Set<ProcedimientoPaquete> procedimientosPaquete;

    @OneToMany(mappedBy = "portafolio")
    private Set<TransportePortafolio> transportesPortafolio;
    
    public Portafolio() {
    }

    @Override
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Timestamp getFechaInsert() {
        return this.fechaInsert;
    }

    public void setFechaInsert(Timestamp fechaInsert) {
        this.fechaInsert = fechaInsert;
    }

    public Timestamp getFechaUpdate() {
        return this.fechaUpdate;
    }

    public void setFechaUpdate(Timestamp fechaUpdate) {
        this.fechaUpdate = fechaUpdate;
    }

    public SedePrestador getSedePrestador() {
        return this.sedePrestador;
    }

    public void setSedePrestador(SedePrestador sedePrestador) {
        this.sedePrestador = sedePrestador;
    }

    /**
     * @return the eps
     */
    public Long getEps() {
        return eps;
    }

    /**
     * @param eps the eps to set
     */
    public void setEps(Long eps) {
        this.eps = eps;
    }

    public List<Portafolio> getPortafolioPadre() {
        return portafolioPadre;
    }

    public void setPortafolioPadre(List<Portafolio> portafolioPadre) {
        this.portafolioPadre = portafolioPadre;
    }

    public List<GrupoServicio> getGrupoServicios() {
        return grupoServicios;
    }

    public void setGrupoServicios(List<GrupoServicio> grupoServicios) {
        this.grupoServicios = grupoServicios;
    }

    public Set<InsumoPortafolio> getInsumosPortafolios() {
        return insumosPortafolios;
    }

    public void setInsumosPortafolios(Set<InsumoPortafolio> insumosPortafolios) {
        this.insumosPortafolios = insumosPortafolios;
    }

    public List<MedicamentoPortafolio> getMedicamentoPortafolios() {
        return medicamentoPortafolios;
    }

    public void setMedicamentoPortafolios(
            List<MedicamentoPortafolio> medicamentoPortafolios) {
        this.medicamentoPortafolios = medicamentoPortafolios;
    }

    public List<PaquetePortafolio> getPaquetePortafolios() {
        return paquetePortafolios;
    }

    public void setPaquetePortafolios(List<PaquetePortafolio> paquetePortafolios) {
        this.paquetePortafolios = paquetePortafolios;
    }

    public Set<ProcedimientoPaquete> getProcedimientosPaquete() {
        return procedimientosPaquete;
    }

    public void setProcedimientosPaquete(Set<ProcedimientoPaquete> procedimientosPaquete) {
        this.procedimientosPaquete = procedimientosPaquete;
    }

    public Set<TransportePortafolio> getTransportesPortafolio() {
        return transportesPortafolio;
    }

    public void setTransportesPortafolio(Set<TransportePortafolio> transportesPortafolio) {
        this.transportesPortafolio = transportesPortafolio;
    }

}
