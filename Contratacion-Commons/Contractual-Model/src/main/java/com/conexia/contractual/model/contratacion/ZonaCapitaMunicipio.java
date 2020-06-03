package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contractual.model.maestros.Municipio;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entidad Zona Cápita Municipio.
 *
 * @author mcastro
 * @date 09/10/2015
 */
@Entity
@Table(name = "zona_capita_municipio", schema = "contratacion")
public class ZonaCapitaMunicipio implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "zona_capita_id")
    private ZonaCapita zonaCapita;
    
    @ManyToOne
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;
    

    public ZonaCapitaMunicipio() {
    }

    public ZonaCapitaMunicipio(Long id) {
        this.id = id;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ZonaCapita getZonaCapita() {
        return zonaCapita;
    }

    public void setZonaCapita(ZonaCapita zonaCapita) {
        this.zonaCapita = zonaCapita;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }
    
}
