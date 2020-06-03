package com.conexia.contractual.model.contratacion.comite;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorComiteEnum;
import com.conexia.contratacion.commons.constants.enums.MotivoRechazoComiteEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contractual.model.contratacion.Prestador;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author etorres
 */
@Entity
@Table(schema = "contratacion", name = "prestador_comite")
@NamedQueries({
        @NamedQuery(name = "PrestadorComite.findByEstado",
                query = "select new com.conexia.contratacion.commons.dto.comite.PrestadorComiteDto("
                        + "pc.id, pc.justificacionEnvioComite, pc.prestador.id, pc.tipoTecnologiasPrestador  "
                        + ") "
                        + " from PrestadorComite pc "
                        + " where pc.estado = :estado "),
        @NamedQuery(name = "PrestadorComite.buscarPrestadorComiteByComiteId",
                query = "SELECT NEW com.conexia.contratacion.commons.dto.comite.TablaPrestadoresComiteDto("
                        + " pc.id, p.id, pc.fechaAsociacionComite, ti.codigo, p.numeroDocumento, p.nombre, "
                        + " pc.tipoTecnologiasPrestador, p.empresaSocialEstado, p.nivelAtencion, pc.estado, "
                        + " pc.conceptoComite, pc.justificacionEnvioComite, pc.tipoModalidad) "
                        + " FROM PrestadorComite pc "
                        + " JOIN pc.prestador p "
                        + " JOIN p.tipoIdentificacion ti "
                        + " WHERE pc.comite.id = :comiteId "),
        @NamedQuery(name = "PrestadorComite.updateEestadoAndConceptoById", query = "UPDATE PrestadorComite pc "
                + "SET pc.estado = :estado, pc.motivoRechazo = :motivoRechazo, pc.conceptoComite = :concepto WHERE pc.id = :prestadorComiteId"),

        @NamedQuery(name = "PrestadorComite.updatePrestadoresrSinComite", query = "UPDATE PrestadorComite pc "
                + "SET pc.comite.id =:comiteId, pc.fechaAsociacionComite =:fechaAsociarComite, pc.estado =:nuevoEstado "
                + "WHERE pc.estado IN (:estadosPrestadorComite) AND pc.comite is null and pc.id in (:ids)"),
        @NamedQuery(name = "PrestadorComite.updateEstadoById", query = "UPDATE PrestadorComite pc "
                + "SET pc.estado =:estado "
                + "WHERE pc.id =:prestadorComiteId"),
        @NamedQuery(name = "PrestadorComite.aplazarPrestadoresrComiteDisponible", query = "UPDATE PrestadorComite pc "
                + "SET pc.comite =:comiteDisponible, pc.fechaAsociacionComite =:fechaActual, pc.estado =:nuevoEstado "
                + "WHERE pc.id IN (:prestadoresComiteIds)"),
        @NamedQuery(
                name = "PrestadorComite.cambiarEstadoPorComiteIdYEstado",
                query = "UPDATE PrestadorComite pc SET pc.estado = :nuevoEstado WHERE pc.comite.id = :comiteId AND pc.estado = :estadoAnterior")
        , @NamedQuery(
        name = "PrestadorComite.obtenerEntidadPorComiteIdYEstado",
        query = "SELECT new com.conexia.contratacion.commons.dto.comite.PrestadorComiteDto(pc.justificacionEnvioComite, pc.prestador.id, pc.tipoTecnologiasPrestador) FROM PrestadorComite pc WHERE pc.comite.id = :comiteId AND pc.estado = :estado")
        , @NamedQuery(
        name = "PrestadorComite.contarPorComiteIdYEstado",
        query = "SELECT CAST(COUNT(pc.id) AS integer) FROM PrestadorComite pc WHERE pc.comite.id = :comiteId AND pc.estado = :estado")
        , @NamedQuery(name = "PrestadorComite.asignarPrestadoresComiteById", query = "UPDATE PrestadorComite pc "
        + "SET pc.comite.id =:comiteId, pc.fechaAsociacionComite =:fechaAsociarComite, pc.estado =:nuevoEstado "
        + "WHERE pc.id in (:ids)"),
})
public class PrestadorComite implements Serializable {

    private static final long serialVersionUID = -639675875115198604L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "justificacion_envio_comite")
    private String justificacionEnvioComite;

    @Column(name = "concepto_comite")
    private String conceptoComite;

    @Column(name = "fecha_asociacion_comite")
    private Date fechaAsociacionComite;

    @Column(name = "estado")
    @Enumerated(EnumType.STRING)
    private EstadoPrestadorComiteEnum estado;

    @Column(name = "motivo_rechazo_id")
    private MotivoRechazoComiteEnum motivoRechazo;

    @Column(name = "tipo_tecnologias_prestador")
    private String tipoTecnologiasPrestador;

    //bi-directional many-to-one association to Prestador
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestador_id")
    private Prestador prestador;

    //bi-directional many-to-one association to Prestador
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comite_id")
    private ComitePrecontratacion comite;

    //bi-directional many-to-one association to ResumenComparacionTarifa
    @OneToMany(mappedBy = "prestadorComite")
    private List<ResumenComparacionTarifas> resumenComparacionTarifas;

    /**
     * Tipo de modalidad de la negociacion.
     */
    @Column(name = "tipo_modalidad", nullable = false)
    @Enumerated(EnumType.STRING)
    private NegociacionModalidadEnum tipoModalidad;

    public PrestadorComite(Long id) {
        this.id = id;
    }

    public PrestadorComite() {
    }


    public PrestadorComite(String justificacionEnvioComite, Long prestadoId,
                           String tipoTecnologiasPrestador) {

        setJustificacionEnvioComite(justificacionEnvioComite);
        setPrestador(new Prestador(prestadoId));
        setTipoTecnologiasPrestador(tipoTecnologiasPrestador);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJustificacionEnvioComite() {
        return justificacionEnvioComite;
    }

    public void setJustificacionEnvioComite(String justificacionEnvioComite) {
        this.justificacionEnvioComite = justificacionEnvioComite;
    }

    public String getConceptoComite() {
        return conceptoComite;
    }

    public void setConceptoComite(String conceptoComite) {
        this.conceptoComite = conceptoComite;
    }

    public Date getFechaAsociacionComite() {
        return fechaAsociacionComite;
    }

    public void setFechaAsociacionComite(Date fechaAsociacionComite) {
        this.fechaAsociacionComite = fechaAsociacionComite;
    }

    public EstadoPrestadorComiteEnum getEstado() {
        return estado;
    }

    public void setEstado(EstadoPrestadorComiteEnum estado) {
        this.estado = estado;
    }

    public MotivoRechazoComiteEnum getMotivoRechazo() {
        return motivoRechazo;
    }

    public void setMotivoRechazo(MotivoRechazoComiteEnum motivoRechazo) {
        this.motivoRechazo = motivoRechazo;
    }

    public String getTipoTecnologiasPrestador() {
        return tipoTecnologiasPrestador;
    }

    public void setTipoTecnologiasPrestador(String tipoTecnologiasPrestador) {
        this.tipoTecnologiasPrestador = tipoTecnologiasPrestador;
    }

    public Prestador getPrestador() {
        return prestador;
    }

    public void setPrestador(Prestador prestador) {
        this.prestador = prestador;
    }

    public ComitePrecontratacion getComite() {
        return comite;
    }

    public void setComite(ComitePrecontratacion comite) {
        this.comite = comite;
    }

    public List<ResumenComparacionTarifas> getResumenComparacionTarifas() {
        return resumenComparacionTarifas;
    }

    public void setResumenComparacionTarifas(
            List<ResumenComparacionTarifas> resumenComparacionTarifas) {
        this.resumenComparacionTarifas = resumenComparacionTarifas;
    }

    public NegociacionModalidadEnum getTipoModalidad() {
        return tipoModalidad;
    }

    public void setTipoModalidad(NegociacionModalidadEnum tipoModalidad) {
        this.tipoModalidad = tipoModalidad;
    }

}
