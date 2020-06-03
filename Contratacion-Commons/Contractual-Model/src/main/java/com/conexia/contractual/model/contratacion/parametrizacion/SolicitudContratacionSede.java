package com.conexia.contractual.model.contratacion.parametrizacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.EstadoParametrizacionEnum;
import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion;
import com.conexia.contractual.model.security.User;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * Entity que manipula los datos de la tabla solicitud_contratacion.
 * 
 * @author jalvarado
 */
@Entity
@Table(schema = "contratacion", name = "solicitud_contratacion_sede")
@NamedQueries({
    @NamedQuery(name = "SolicitudContratacionSede.contarSedesAsociadas", query = "select count(scs.id) "
            + "from SolicitudContratacionSede scs "
            + "where scs.solicitudContratacion.id = :solicitudContratacionId"),
    @NamedQuery(name = "SolicitudContratacionSede.filtrarPorSolicitudContratacionSede", query = "select scs "
            + "from SolicitudContratacionSede scs "
            + "where scs.solicitudContratacion.id = :solicitudContratacionId and "
            + "scs.sedeNegociacion.id = :sedeNegociacionId"),
    @NamedQuery(name = "SolicitudContratacionSede.contarSolicitudContratacionSede", query = "select count(scs.id) "
            + "from SolicitudContratacionSede scs "
            + "where scs.solicitudContratacion.id = :idSolicitudContratacion and "
            + "scs.sedeNegociacion.id = :sedeNegociacionId")
})
public class SolicitudContratacionSede implements Identifiable<Long>, Serializable {

    /**
     * Id de la solicitud de contratacion.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_contratacion_id")
    private SolicitudContratacion solicitudContratacion;
    
    /**
     * Estado de parametrizacion.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "estado_parametrizacion_id", nullable = false)
    private EstadoParametrizacionEnum estadoParametrizacionEnum;
    
    /**
     * Sede prestador.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_negociacion_id")
    private SedesNegociacion sedeNegociacion;
    
    /**
     * Usuario que parametrizar la sede de la solicitud de contratacion.
     */
    @Column(name = "user_id", nullable = false, updatable = false)
    private Integer userId;
    
    
    @Transient
    @Deprecated
    private User user;
    
    /**
     * Fecha en la que se registra la asociacion de la sede a la solicitud de contratacion.
     */
    @Column(name = "fecha_estado", nullable = false, updatable = false)
    private Date fechaEstado;
    
    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
    
    @Override
    public Long getId() {
        return this.id;
    }
    
    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return the estadoParametrizacionEnum
     */
    public EstadoParametrizacionEnum getEstadoParametrizacionEnum() {
        return estadoParametrizacionEnum;
    }

    /**
     * @param estadoParametrizacionEnum the estadoParametrizacionEnum to set
     */
    public void setEstadoParametrizacionEnum(EstadoParametrizacionEnum estadoParametrizacionEnum) {
        this.estadoParametrizacionEnum = estadoParametrizacionEnum;
    }

    
    /**
     * @return the user
     */
    @Deprecated
    public User getUser() {
        return user;
    }

    /**
     * @param user the user to set
     */
    @Deprecated
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.setUserId(user.getId());
        }
    }

    /**
     * @return the userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }
    
    /**
     * @return the fechaEstado
     */
    public Date getFechaEstado() {
        return fechaEstado;
    }

    /**
     * @param fechaEstado the fechaEstado to set
     */
    public void setFechaEstado(Date fechaEstado) {
        this.fechaEstado = fechaEstado;
    }
    
    /**
     * @return the solicitudContratacion
     */
    public SolicitudContratacion getSolicitudContratacion() {
        return solicitudContratacion;
    }

    /**
     * @param solicitudContratacion the solicitudContratacion to set
     */
    public void setSolicitudContratacion(SolicitudContratacion solicitudContratacion) {
        this.solicitudContratacion = solicitudContratacion;
    }
    
    /**
     * @return the sedeNegociacion
     */
    public SedesNegociacion getSedeNegociacion() {
        return sedeNegociacion;
    }

    /**
     * @param sedeNegociacion the sedeNegociacion to set
     */
    public void setSedeNegociacion(SedesNegociacion sedeNegociacion) {
        this.sedeNegociacion = sedeNegociacion;
    }
    
    //</editor-fold>

}
