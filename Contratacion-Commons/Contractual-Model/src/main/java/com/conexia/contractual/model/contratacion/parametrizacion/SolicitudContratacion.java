package com.conexia.contractual.model.contratacion.parametrizacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoParametrizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.model.contratacion.contrato.Contrato;
import com.conexia.contractual.model.contratacion.negociacion.Negociacion;
import com.conexia.contractual.model.security.User;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 *
 * Entity que manipula los datos de la tabla solicitud_contratacion.
 *
 * @author jalvarado
 */
@Entity
@Table(schema = "contratacion", name = "solicitud_contratacion")
@NamedQueries({
    @NamedQuery(name = "SolicitudContratacion.filtrarPorNumeroNegociacion", query = "select sc "
            + "from SolicitudContratacion sc "
            + "where sc.id = :idSolicitudContratacion"),
    @NamedQuery(name = "SolicitudContratacion.findByNegociacionIdAndEstado",
            query = "select NEW com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto(sc.id) "
            + "from SolicitudContratacion sc "
            + "where sc.negociacion.id = :negociacionId "
            + "AND sc.estadoLegalizacion = :estadoLegalizacion"),
    @NamedQuery(name = "SolicitudContratacion.updateEstadoParametrizacion",
    		query = "UPDATE SolicitudContratacion sc SET sc.estadoParametrizacion = :estadoParametrizacion "
    		+ "WHERE sc.negociacion.id = :negociacionId "),
    @NamedQuery(name ="SolicitudContratacion.updateEstadoLegalizacion",
    		query = "UPDATE SolicitudContratacion sc "
    				+ "set sc.estadoLegalizacion = :estadoLegalizacion "
    				+ "WHERE sc.id = :idSolicitudContratacion"),
     @NamedQuery(name = "SolicitudContratacion.updateDeletedRegistro",
                    query = "update SolicitudContratacion sc set sc.deleted = :deleted where sc.negociacion.id =:negociacionId ")
})
@NamedNativeQueries({
	 @NamedNativeQuery(name = "SolicitudContratacion.bandejaSolicitudesContrato",
			query = "SELECT DISTINCT n.id as negociacionId, c.numero_contrato,sp.nombre_sede as razon_social,p.numero_documento, c.tipo_contrato, upper(r.descripcion) as regional, "
			+ "n.tipo_modalidad_negociacion, n.regimen, c.tipo_subsidiado,  "
			+ "c.fecha_inicio, c.fecha_fin, c.nivel_contrato, upper(m.descripcion) as minuta,n.poblacion, "
			+ "CONCAT(u.primer_nombre,' ',u.primer_apellido) as responsable_creacion, sol.estado_legalizacion_id as estado_legalizacion, "
			+ "lc.fecha_vo_bo as fecha_legalizacion,CASE WHEN c.fecha_fin >= now()  THEN 'VIGENTE' ELSE 'VENCIDO' END as estado_contrato, "
			+ "CAST(n.fecha_creacion AS date) as fecha_negociacion, n.estado_negociacion,sol.estado_parametrizacion_id, CAST(COUNT(sn.id) as integer) as numero_sedes "
			+ "FROM  contratacion.negociacion n "
			+ "JOIN security.user u on n.user_id = u.id "
			+ "JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id and sn.principal = true "
			+ "JOIN contratacion.sede_prestador sp on sn.sede_prestador_id = sp.id "
			+ "JOIN contratacion.prestador p on sp.prestador_id = p.id "
			+ "JOIN contratacion.solicitud_contratacion sol on sol.negociacion_id = sn.negociacion_id  "
			+ "LEFT JOIN contratacion.contrato c on c.solicitud_contratacion_id = sol.id "
			+ "LEFT JOIN contratacion.legalizacion_contrato lc on lc.contrato_id = c.id "
			+ "LEFT JOIN contratacion.minuta m on lc.minuta_id = m.id "
			+ "LEFT JOIN maestros.regional r on c.regional_id = r.id "
			+ "WHERE n.estado_negociacion = :estadoNegociacion "
			+ "GROUP BY n.id, c.numero_contrato,sp.nombre_sede,p.numero_documento, c.tipo_contrato, r.descripcion, "
			+ "n.tipo_modalidad_negociacion, n.regimen, c.tipo_subsidiado, "
			+ "c.fecha_inicio, c.fecha_fin, c.nivel_contrato, m.descripcion,n.poblacion, u.primer_nombre, u.primer_apellido, "
			+ "sol.estado_legalizacion_id,lc.fecha_vo_bo ,n.fecha_creacion ,n.estado_negociacion,sol.estado_parametrizacion_id "
			+ "ORDER BY n.id "
			)
})
@SqlResultSetMappings({
	@SqlResultSetMapping(name ="SolicitudContratacion.bandejaSolicitudesContratoMappging",
    		classes = @ConstructorResult(
    				targetClass = SolicitudContratacionParametrizableDto.class,
    				columns = {
    						@ColumnResult(name = "negociacion_id", type = Long.class),
    						@ColumnResult(name = "numero_contrato", type = String.class),
    						@ColumnResult(name = "razon_social", type = String.class),
    						@ColumnResult(name = "numero_documento", type = String.class),
    						@ColumnResult(name = "tipo_contrato", type = String.class),
    						@ColumnResult(name = "regional", type = String.class),
    						@ColumnResult(name = "tipo_modalidad_negociacion", type = String.class),
    						@ColumnResult(name = "regimen", type = String.class),
    						@ColumnResult(name = "tipo_subsidiado", type = String.class),
    						@ColumnResult(name = "fecha_inicio", type = Date.class),
    						@ColumnResult(name = "fecha_fin", type = Date.class),
    						@ColumnResult(name = "nivel_contrato", type = String.class),
    						@ColumnResult(name = "minuta", type = String.class),
    						@ColumnResult(name = "poblacion", type = Integer.class),
    						@ColumnResult(name = "responsable_creacion", type = String.class),
    						@ColumnResult(name = "estado_legalizacion", type = String.class),
    						@ColumnResult(name = "fecha_legalizacion", type = Date.class),
    						@ColumnResult(name = "estado_contrato", type = String.class),
    						@ColumnResult(name = "fecha_negociacion", type = Date.class),
    						@ColumnResult(name = "estado_negociacion", type = String.class),
    						@ColumnResult(name = "estado_parametrizacion_id", type = String.class),
    						@ColumnResult(name = "numero_sedes", type = Integer.class),
    						@ColumnResult(name = "solicitudId", type = Long.class),
    						@ColumnResult(name = "prestador_id", type = Long.class),
    						@ColumnResult(name = "codigo_identificacion", type = String.class),
    						@ColumnResult(name = "codigo_prestador", type = String.class),
    						@ColumnResult(name = "prefijo", type = String.class),
    						@ColumnResult(name = "tipo_prestador", type = String.class),
    						@ColumnResult(name = "naturaleza_juridica", type = Integer.class),
    						@ColumnResult(name = "clase_prestador", type = String.class),
    						@ColumnResult(name = "clasificacion_prestador", type = String.class),
    						@ColumnResult(name = "nivel_atencion", type = Integer.class),
    						@ColumnResult(name = "tipo_identificacion_representante_id", type = Integer.class),
    						@ColumnResult(name = "numero_documento_representante", type = String.class),
    						@ColumnResult(name = "representante_legal", type = String.class),
    						@ColumnResult(name = "correo_electronico", type = String.class),
    						@ColumnResult(name = "empresa_social_estado", type = Boolean.class),
    						@ColumnResult(name = "nuevo_contrato", type = Boolean.class),
    						@ColumnResult(name = "contrato_id", type = Long.class),
    						@ColumnResult(name = "nombre_archivo", type = String.class),
    						@ColumnResult(name = "nombre_original_archivo", type = String.class),
                            @ColumnResult(name = "negociacion_padre_id", type = Long.class),
                            @ColumnResult(name = "tipo_otro_si", type = String.class),
                            @ColumnResult(name = "numero_otro_si", type = Integer.class),
                            @ColumnResult(name = "tieneOtroSi", type = Integer.class)
    				})),
        @SqlResultSetMapping(name ="SolicitudContratacion.obtenerSolicitudMappging",
                classes = @ConstructorResult(
                        targetClass = SolicitudContratacionParametrizableDto.class,
                        columns = {
                                @ColumnResult(name = "solicitud_id", type = Long.class),
                                @ColumnResult(name = "negociacion_id", type = Long.class),
                                @ColumnResult(name = "prestador_id", type = Long.class),
                                @ColumnResult(name = "nombre", type = String.class),
                                @ColumnResult(name = "prestador_tipo_ident", type = String.class),
                                @ColumnResult(name = "numero_documento", type = String.class),
                                @ColumnResult(name = "codigo_prestador", type = String.class),
                                @ColumnResult(name = "prefijo", type = String.class),
                                @ColumnResult(name = "tipo_doc_prestador", type = String.class),
                                @ColumnResult(name = "naturaleza_juridica", type = String.class),
                                @ColumnResult(name = "clase_prestador", type = String.class),
                                @ColumnResult(name = "clasificacion_prestador", type = String.class),
                                @ColumnResult(name = "nivel_atencion", type = String.class),
                                @ColumnResult(name = "tipo_identificacion_rep_legal", type = String.class),
                                @ColumnResult(name = "numero_documento_representante", type = String.class),
                                @ColumnResult(name = "representante_legal", type = String.class),
                                @ColumnResult(name = "correo_electronico", type = String.class),
                                @ColumnResult(name = "empresa_social_estado", type = Boolean.class),
                                @ColumnResult(name = "tipo_modalidad_negociacion", type = String.class),
                                @ColumnResult(name = "nuevo_contrato", type = Boolean.class),
                                @ColumnResult(name = "estado_parametrizacion_id", type = String.class),
                                @ColumnResult(name = "estado_legalizacion_id", type = String.class),
                                @ColumnResult(name = "poblacion", type = Integer.class),
                                @ColumnResult(name = "regimen", type = String.class),
                                @ColumnResult(name = "es_rias", type = Boolean.class),
                                @ColumnResult(name = "negociacion_padre_id", type = Long.class),
                                @ColumnResult(name = "numero_otro_si", type = Integer.class),
                                @ColumnResult(name = "tipo_otro_si", type = Integer.class),
                                @ColumnResult(name = "negociacion_origen", type = Long.class),

                        }))
})
public class SolicitudContratacion implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negociacion_id", nullable = false, unique = true)
    private Negociacion negociacion;

    @Column(name = "fecha_creacion_solicitud", nullable = false, updatable = false)
    private Date fechaCreacionSolicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestador_id", nullable = false)
    private Prestador prestador;

    @Column(name = "tipo_modalidad_negociacion", nullable = false)
    @Enumerated(EnumType.STRING)
    private NegociacionModalidadEnum tipoModalidadNegociacion;

    @Column(name = "nuevo_contrato")
    private Boolean nuevoContrato;

    @Transient
    @Deprecated
    private User user;

    @Column(name = "user_id")
    private Integer userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_parametrizacion_id", nullable = true)
    private EstadoParametrizacionEnum estadoParametrizacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_legalizacion_id", nullable = true)
    private EstadoLegalizacionEnum estadoLegalizacion;

    @OneToMany(mappedBy = "solicitudContratacion", fetch = FetchType.EAGER)
    private List<Contrato> contratos;

    @Column(name = "regimen", nullable = false)
    @Enumerated(EnumType.STRING)
    private RegimenNegociacionEnum regimen;

    @Column(name = "deleted")
    private boolean deleted;

    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
    @Override
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

    public Date getFechaCreacionSolicitud() {
        return fechaCreacionSolicitud;
    }

    public void setFechaCreacionSolicitud(Date fechaCreacionSolicitud) {
        this.fechaCreacionSolicitud = fechaCreacionSolicitud;
    }

    public Prestador getPrestador() {
        return prestador;
    }

    public void setPrestador(Prestador prestador) {
        this.prestador = prestador;
    }

    public NegociacionModalidadEnum getTipoModalidadNegociacion() {
        return tipoModalidadNegociacion;
    }

    public void setTipoModalidadNegociacion(NegociacionModalidadEnum tipoModalidadNegociacion) {
        this.tipoModalidadNegociacion = tipoModalidadNegociacion;
    }

    public Boolean getNuevoContrato() {
        return nuevoContrato;
    }

    public void setNuevoContrato(Boolean nuevoContrato) {
        this.nuevoContrato = nuevoContrato;
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

    public EstadoParametrizacionEnum getEstadoParametrizacion() {
        return estadoParametrizacion;
    }

    public void setEstadoParametrizacion(EstadoParametrizacionEnum estadoParametrizacion) {
        this.estadoParametrizacion = estadoParametrizacion;
    }

    public EstadoLegalizacionEnum getEstadoLegalizacion() {
        return estadoLegalizacion;
    }

    public void setEstadoLegalizacion(EstadoLegalizacionEnum estadoLegalizacion) {
        this.estadoLegalizacion = estadoLegalizacion;
    }

    public List<Contrato> getContratos() {
        return this.contratos;
    }

    public void setContratos(final List<Contrato> contratos) {
        this.contratos = contratos;
    }

    public RegimenNegociacionEnum getRegimen() {
        return regimen;
    }

    public void setRegimen(RegimenNegociacionEnum regimen) {
        this.regimen = regimen;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
    //</editor-fold>
}
