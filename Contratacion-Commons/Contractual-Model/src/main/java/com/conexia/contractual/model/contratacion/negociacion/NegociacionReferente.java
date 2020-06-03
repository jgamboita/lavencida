/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import org.hibernate.annotations.Immutable;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author aquintero
 */
@Entity
@Immutable
@Table(schema = "contratacion", name = "negociacion_referente")
public class NegociacionReferente implements Identifiable<Long>, Serializable {
    
    public static final String FN_INSERTAR_NEGOCIACION_REFERENTE = "contratacion.fn_insertar_negociacion_referente";

    public static final String PARAM_NEGOCIACION_ID = "in_negociacion_id";
    public static final String PARAM_NEGOCIACION_REFERENTE_ID = "in_negociacion_referente_id";    
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique=true, nullable=false)
    private Long id;
    
    @OneToOne
    @JoinColumn(name = "negociacion_id")
    private Negociacion negociacion;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negociacion_referente_id")
    private Negociacion negociacionReferente;
    
    @Column(name = "fecha_creacion")
    private Date fechaCreacion;

    public NegociacionReferente() {
        
    }
    
    public NegociacionReferente(Negociacion negociacion, Negociacion negociacionReferente, Date fechaCreacion) 
    {
        this.negociacion = negociacion;
        this.negociacionReferente = negociacionReferente;
        this.fechaCreacion = fechaCreacion;
    }

    public NegociacionReferente(Long id, Negociacion negociacion, Negociacion negociacionReferente, Date fechaCreacion) 
    {
        this.id = id;
        this.negociacion = negociacion;
        this.negociacionReferente = negociacionReferente;
        this.fechaCreacion = fechaCreacion;
    }    
      
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Negociacion getNegociacion() {
        return negociacion;
    }

    public void setNegociacion(Negociacion negociacion) {
        this.negociacion = negociacion;
    }

    public Negociacion getNegociacionReferente() {
        return negociacionReferente;
    }

    public void setNegociacionReferente(Negociacion negociacionReferente) {
        this.negociacionReferente = negociacionReferente;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
    
}
