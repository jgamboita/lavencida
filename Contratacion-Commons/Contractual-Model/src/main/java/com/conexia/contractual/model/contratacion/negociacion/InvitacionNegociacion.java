package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.EstadoInvitacionNegociacionEnum;
import com.conexia.contractual.model.security.User;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * The persistent class for the invitacion_negociacion database table.
 * @author etorres
 */
@Entity
@NamedQueries(
    {
    @NamedQuery(name="InvitacionNegociacion.findDtoByNegociacionId",
        query="SELECT NEW com.conexia.contratacion.commons.dto.negociacion.InvitacionNegociacionDto(i.id, i.estado, i.mensaje, i.fechaHoraCita, i.negociacion.id, i.userId, i.correo) FROM InvitacionNegociacion i WHERE i.negociacion.id =:negociacionId"),
    @NamedQuery(name = "InvitacionNegociacion.updateDeletedRegistro",query = "update InvitacionNegociacion ine set ine.deleted = :deleted where ine.negociacion.id =:negociacionId ")
    })
@Table(schema = "contratacion", name = "invitacion_negociacion")
public class InvitacionNegociacion implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Transient
    @Deprecated
    private User user;

    @Column(name = "user_id")
    private Integer userId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negociacion_id")
    private Negociacion negociacion;

    @Column(name = "fecha_creacion")
    private Date fechaCreacion;

    @Column(name = "fecha_hora_cita")
    private Date fechaHoraCita;

    @Column(name = "mensaje")
    private String mensaje;

    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private EstadoInvitacionNegociacionEnum estado;

    @Column(name = "correo")
    private String correo;

    @Column(name = "deleted")
    private boolean deleted;

    @Override
    public Long getId() {
        return this.id;
    }

    @Deprecated
    public User getUser() {
        return user;
    }

    @Deprecated
    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            this.setUserId(user.getId());
        }
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Negociacion getNegociacion() {
        return negociacion;
    }

    public void setNegociacion(Negociacion negociacion) {
        this.negociacion = negociacion;
    }

    public Date getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(Date fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public Date getFechaHoraCita() {
        return fechaHoraCita;
    }

    public void setFechaHoraCita(Date fechaHoraCita) {
        this.fechaHoraCita = fechaHoraCita;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public EstadoInvitacionNegociacionEnum getEstado() {
        return estado;
    }

    public void setEstado(EstadoInvitacionNegociacionEnum estado) {
        this.estado = estado;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
