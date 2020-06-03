package com.conexia.contractual.model.contratacion.legalizacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.Date;

/**
 *  Entidad de la tabla minuta detalle.
 * 
 * @author jlopez
 */
@Entity
@Table(name = "minuta_detalle", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name = "MinutaDetalle.findByMinutaId", query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto"
            + "(dm.id, dm.ordinal, dm.padreId, dm.titulo, dm.descripcion, m.id, m.descripcion, m.nombre, "
            + "cm.id, cm.descripcion, cm.nivel, cm.unico, cm.tieneHijos, cm.icono) "
            + "FROM MinutaDetalle dm join dm.minutaId m join dm.contenidoMinutaId cm  where m.id = :idMinuta and dm.legalizacionContrato.id is null "
            + "order by cm.nivel, dm.ordinal"),
    @NamedQuery(name = "MinutaDetalle.findByPadreId", query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto"
            + "(dm.id, dm.ordinal, dm.padreId, dm.titulo, dm.descripcion, m.id, m.descripcion, m.nombre, "
            + "cm.id, cm.descripcion, cm.nivel, cm.unico, cm.tieneHijos, cm.icono) "
            + "FROM MinutaDetalle dm join dm.minutaId m join dm.contenidoMinutaId cm  "
            + "where dm.padreId = :padreId "),
    @NamedQuery(name = "MinutaDetalle.findByPadreAndOrdinal", query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto"
            + "(dm.id, dm.ordinal, dm.padreId, dm.titulo, dm.descripcion, m.id, m.descripcion, m.nombre, "
            + "cm.id, cm.descripcion, cm.nivel, cm.unico, cm.tieneHijos, cm.icono) "
            + "FROM MinutaDetalle dm join dm.minutaId m join dm.contenidoMinutaId cm "
            + "where dm.padreId = :padreId "
            + "and ordinal = :ordinal "
            + "order by cm.nivel, dm.ordinal"),
    @NamedQuery(name = "MinutaDetalle.findByMinutaAndOrdinal", query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto"
            + "(dm.id, dm.ordinal, dm.padreId, dm.titulo, dm.descripcion, m.id, m.descripcion, m.nombre, "
            + "cm.id, cm.descripcion, cm.nivel, cm.unico, cm.tieneHijos, cm.icono) "
            + "FROM MinutaDetalle dm join dm.minutaId m join dm.contenidoMinutaId cm "
            + "where m.id = :minutaId "
            + "and ordinal = :ordinal "
            + "and dm.padreId is null and dm.legalizacionContrato.id is null "
            + "order by cm.nivel, dm.ordinal"),
    @NamedQuery(name = "MinutaDetalle.updateById", query = "update MinutaDetalle set descripcion = :descripcion, ordinal = :ordinal where id = :id"),
    @NamedQuery(name = "MinutaDetalle.updateOrdinalPadreIdById", query = "update MinutaDetalle set ordinal = :ordinal, padre_id = :padreId where id = :id"),
    @NamedQuery(name = "MinutaDetalle.updateOrdinalById", query = "update MinutaDetalle set ordinal = :ordinal where id = :id"),
    @NamedQuery(name = "MinutaDetalle.updateTituloById", query = "update MinutaDetalle set titulo = :titulo where id = :id"),
    @NamedQuery(name = "MinutaDetalle.findByPadreAndDiffId", query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto"
            + "(dm.id, dm.ordinal, dm.padreId) "
            + "FROM MinutaDetalle dm "
            + "join dm.contenidoMinutaId cm "
            + "where dm.padreId = :padreId and dm.id <> :id "
            + "and dm.minutaId.id = :minutaId and dm.legalizacionContrato.id is null "
            + "order by cm.nivel, dm.ordinal"),
    @NamedQuery(name = "MinutaDetalle.findByDiffId", query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto"
            + "(dm.id, dm.ordinal, dm.padreId) "
            + "FROM MinutaDetalle dm "
            + "join dm.contenidoMinutaId cm "
            + "where dm.id <> :id and dm.padreId is null "
            + "and dm.minutaId.id = :minutaId and dm.legalizacionContrato.id is null "
            + "order by cm.nivel, dm.ordinal"),
    @NamedQuery(name = MinutaDetalle.CONSULTAR_DETALLE_MINUTA_POR_MINUTA_ID, query = "SELECT new com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto"
            + "(dm.id, dm.descripcion, dm.padreId, dm.ordinal, cm.nivel) "
            + "FROM MinutaDetalle dm join dm.minutaId m join dm.contenidoMinutaId cm "
            + "where m.id = :minutaId and dm.legalizacionContrato.id is null "
            + "order by cm.nivel, dm.ordinal"),
    @NamedQuery(name = "MinutaDetalle.borrarMinutaDetalle", query = "DELETE FROM MinutaDetalle md WHERE md.padreId = :padreId OR md.id = :detalleId"),

})

@NamedNativeQueries ( {
        @NamedNativeQuery(name= MinutaDetalle.CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_MINUTA_ID,
                query ="SELECT md.titulo as tituloClausula, md.ordinal as ordinalClausula, md.descripcion as contenidoClausula, md.id as origenId,\n" +
                        " md_paragrafo.titulo as tituloParagrafo, md_paragrafo.ordinal as ordinalParagrafo, md_paragrafo.descripcion as contenidoParagrafo, md_paragrafo.id as origenIdParagrafo\n" +
                        " FROM contratacion.minuta_detalle md \n" +
                        " INNER JOIN contratacion.contenido_minuta  cm on (cm.id = md.contenido_minuta_id)\n" +
                        " LEFT  JOIN contratacion.minuta_detalle md_paragrafo on (md.id = md_paragrafo.padre_id and md_paragrafo.contenido_minuta_id = 3)\n" +
                        " WHERE md.minuta_id = ? AND cm.id = 2 AND md.legalizacion_contrato_id IS NULL \n" +
                        " ORDER BY md.ordinal, md_paragrafo.ordinal", resultSetMapping = MinutaDetalle.RESULT_SET_MAPPING_CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_MINUTA_ID),

        @NamedNativeQuery(name=MinutaDetalle.CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_LEGALIZACION_CONTRATO_ID,
                query="SELECT md.id, COALESCE(cmPadre.descripcion, cm.descripcion) AS descripcionClausula, \n" +
                        " COALESCE(mdPadre.titulo, md.titulo) AS tituloClausula, \n" +
                        " CASE \n" +
                        "	WHEN  cm.descripcion = 'Clausula' THEN null\n" +
                        "	ELSE cm.descripcion\n" +
                        " END AS descripcionParagrafo, \n" +
                        " CASE \n" +
                        "	WHEN cm.descripcion = 'Clausula' THEN null\n" +
                        "	ELSE md.titulo\n" +
                        " END AS tituloParagrafo, \n" +
                        " COALESCE(mdPadre.ordinal, mdOrigen.ordinal) AS ordinalPadre, md.descripcion as contenido, md.minuta_detalle_origen_id as padreId, mdOrigen.descripcion as contenidoOriginalNegociacionPadre\n" +
                        " FROM contratacion.minuta_detalle md\n" +
                        " INNER JOIN contratacion.legalizacion_contrato lc on (lc.id = md.legalizacion_contrato_id)\n" +
                        " INNER JOIN contratacion.minuta_detalle mdOrigen on (mdOrigen.id = md.minuta_detalle_origen_id)\n" +
                        " INNER JOIN contratacion.contenido_minuta cm on (cm.id = mdOrigen.contenido_minuta_id)\n" +
                        " LEFT  JOIN contratacion.minuta_detalle mdPadre on (mdPadre.id = mdOrigen.padre_id)\n" +
                        " LEFT  JOIN contratacion.contenido_minuta cmPadre on (cmPadre.id = mdPadre.contenido_minuta_id)\n" +
                        " WHERE lc.id = ?\n" +
                        " ORDER BY ordinalPadre, descripcionParagrafo DESC, mdOrigen.ordinal", resultSetMapping = MinutaDetalle.RESULT_SET_MAPPING_CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_LEGALIZACION_CONTRATO_ID),

        @NamedNativeQuery(name=MinutaDetalle.CONSULTAR_DETALLE_ACTA_POR_LEGALIZACION_CONTRATO_ID,
                query=
                        "SELECT md.id, md.descripcion, md.padre_id AS padreId, md.ordinal, cm.nivel\n" +
                        "FROM contratacion.minuta_detalle md\n" +
                        "JOIN contratacion.contenido_minuta cm ON cm.id = md.contenido_minuta_id\n" +
                        "WHERE minuta_id = :minutaId\n" +
                        "UNION\n" +
                        "SELECT md.minuta_detalle_origen_id id, md.descripcion, mdp.padre_id AS padreId, mdp.ordinal, cm.nivel\n" +
                        "FROM contratacion.minuta_detalle md\n" +
                        "JOIN contratacion.minuta_detalle mdp ON md.minuta_detalle_origen_id = mdp.id\n" +
                        "JOIN contratacion.contenido_minuta cm ON cm.id = mdp.contenido_minuta_id\n" +
                        "WHERE md.legalizacion_contrato_id = :legalizacionContratoId\n" +
                        "UNION\n" +
                        "SELECT mdpp.id, mdpp.descripcion, mdpp.padre_id AS padreId, mdpp.ordinal, cm.nivel\n" +
                        "FROM contratacion.minuta_detalle md\n" +
                        "JOIN contratacion.minuta_detalle mdp ON md.minuta_detalle_origen_id = mdp.id\n" +
                        "JOIN contratacion.minuta_detalle mdpp ON mdp.padre_id = mdpp.id\n" +
                        "JOIN contratacion.contenido_minuta cm ON cm.id = mdpp.contenido_minuta_id\n" +
                        "WHERE md.legalizacion_contrato_id = :legalizacionContratoId AND mdpp.id NOT IN (\n" +
                        "    SELECT minuta_detalle_origen_id\n" +
                        "    FROM contratacion.minuta_detalle\n" +
                        "    WHERE legalizacion_contrato_id = :legalizacionContratoId\n" +
                        ")\n" +
                        "ORDER BY nivel, ordinal;\n"
                , resultSetMapping = MinutaDetalle.RESULT_SET_MAPPING_CONSULTAR_DETALLE_ACTA_POR_LEGALIZACION_CONTRATO_ID),
})
@SqlResultSetMappings({
        @SqlResultSetMapping(name = MinutaDetalle.RESULT_SET_MAPPING_CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_MINUTA_ID,
                classes = {
                        @ConstructorResult (
                                targetClass = MinutaDetalleDto.class, columns = {
                                @ColumnResult(name="tituloClausula"),
                                @ColumnResult(name="ordinalClausula"),
                                @ColumnResult(name="contenidoClausula"),
                                @ColumnResult(name="origenId")
                        }
                        ),

                        @ConstructorResult (
                                targetClass = MinutaDetalleDto.class, columns = {
                                @ColumnResult(name="tituloParagrafo"),
                                @ColumnResult(name="ordinalParagrafo"),
                                @ColumnResult(name="contenidoParagrafo"),
                                @ColumnResult(name="origenIdParagrafo")
                        }
                        )
                }),
        @SqlResultSetMapping(name = MinutaDetalle.RESULT_SET_MAPPING_CONSULTAR_DETALLE_ACTA_POR_LEGALIZACION_CONTRATO_ID,
                classes = {
                        @ConstructorResult (
                                targetClass = MinutaDetalleDto.class, columns = {
                                @ColumnResult(name="id", type = Long.class),
                                @ColumnResult(name="descripcion", type = String.class),
                                @ColumnResult(name="padreId", type = Long.class),
                                @ColumnResult(name="ordinal", type = Integer.class),
                                @ColumnResult(name="nivel", type = Integer.class)
                        }
                        )
                }),
        @SqlResultSetMapping(name = MinutaDetalle.RESULT_SET_MAPPING_CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_LEGALIZACION_CONTRATO_ID,
                classes = {
                        @ConstructorResult (
                                targetClass = MinutaDetalleDto.class, columns = {
                                @ColumnResult(name="id", type=Integer.class),
                                @ColumnResult(name="descripcionClausula", type=String.class),
                                @ColumnResult(name="tituloClausula", type=String.class),
                                @ColumnResult(name="descripcionParagrafo", type=String.class),
                                @ColumnResult(name="tituloParagrafo", type=String.class),
                                @ColumnResult(name="ordinalPadre", type=Integer.class),
                                @ColumnResult(name="contenido", type=String.class),
                                @ColumnResult(name="padreId", type=Long.class),
                                @ColumnResult(name="contenidoOriginalNegociacionPadre", type=String.class)
                        }
                        )
                })
})
public class MinutaDetalle implements Identifiable<Long>, Serializable {
    
    private static final long serialVersionUID = 1L;

    public static final String CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_MINUTA_ID = "ContenidoMinuta.CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_MINUTA_ID";
    public static final String RESULT_SET_MAPPING_CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_MINUTA_ID = "RESULT_SET_MAPPING.ContenidoMinuta.CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_MINUTA_ID";
    public static final String CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_LEGALIZACION_CONTRATO_ID = "ContenidoMinuta.CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_LEGALIZACION_CONTRATO_ID";
    public static final String RESULT_SET_MAPPING_CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_LEGALIZACION_CONTRATO_ID = "RESULT_SET_MAPPING.ContenidoMinuta.CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_LEGALIZACION_CONTRATO_ID";
    public static final String CONSULTAR_DETALLE_MINUTA_POR_MINUTA_ID = "MinutaDetalle.findDescripcionByMinuta";
    public static final String CONSULTAR_DETALLE_ACTA_POR_LEGALIZACION_CONTRATO_ID = "MinutaDetalle.CONSULTAR_DETALLE_ACTA_POR_LEGALIZACION_CONTRATO_ID";
    public static final String RESULT_SET_MAPPING_CONSULTAR_DETALLE_ACTA_POR_LEGALIZACION_CONTRATO_ID = "RESULT_SET_MAPPING.ContenidoMinuta.RESULT_SET_MAPPING_CONSULTAR_DETALLE_ACTA_POR_LEGALIZACION_CONTRATO_ID";

    public static final String NPAR_MINUTA_ID = "minutaId";
    public static final String NPAR_LEGALIZACION_CONTRATO_ID = "legalizacionContratoId";

    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotNull
    @Column(name = "ordinal")
    private int ordinal;
    
    @Column(name = "padre_id")
    private Long padreId;
    
    @NotNull
    @Size(min = 5, max = 50)
    @Column(name = "titulo")
    private String titulo;
    
    @NotNull
    @Size(min = 1, max = 2147483647)
    @Column(name = "descripcion")
    private String descripcion;
    
    @NotNull
    @Column(name = "user_id")
    private long userId;
    
    @NotNull
    @Column(name = "fecha_registro")
    @Temporal(TemporalType.TIMESTAMP)
    private Date fechaRegistro;
    
    @JoinColumn(name = "contenido_minuta_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private ContenidoMinuta contenidoMinutaId;
    
    @JoinColumn(name = "minuta_id", referencedColumnName = "id")
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private Minuta minutaId;

    @JoinColumn(name = "minuta_detalle_origen_id", referencedColumnName = "id")
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private MinutaDetalle minutaDetalleOrigen;

    @JoinColumn(name = "legalizacion_contrato_id", referencedColumnName = "id")
    @ManyToOne(optional = true, fetch = FetchType.LAZY)
    private LegalizacionContrato legalizacionContrato;

    public MinutaDetalle() {
    }

    public MinutaDetalle(Long id) {
        this.id = id;
    }

    public MinutaDetalle(Long id, int ordinal, String titulo, String descripcion, long userId, Date fechaRegistro) {
        this.id = id;
        this.ordinal = ordinal;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.userId = userId;
        this.fechaRegistro = fechaRegistro;
    }

    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public Long getPadreId() {
        return padreId;
    }

    public void setPadreId(Long padreId) {
        this.padreId = padreId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public Date getFechaRegistro() {
        return fechaRegistro;
    }

    public void setFechaRegistro(Date fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public ContenidoMinuta getContenidoMinutaId() {
        return contenidoMinutaId;
    }

    public void setContenidoMinutaId(ContenidoMinuta contenidoMinutaId) {
        this.contenidoMinutaId = contenidoMinutaId;
    }

    public Minuta getMinutaId() {
        return minutaId;
    }

    public void setMinutaId(Minuta minutaId) {
        this.minutaId = minutaId;
    }

    public MinutaDetalle getMinutaDetalleOrigen() {
        return minutaDetalleOrigen;
    }

    public void setMinutaDetalleOrigen(MinutaDetalle minutaDetalleOrigen) {
        this.minutaDetalleOrigen = minutaDetalleOrigen;
    }

    public LegalizacionContrato getLegalizacionContrato() {
        return legalizacionContrato;
    }

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
        if (!(object instanceof MinutaDetalle)) {
            return false;
        }
        MinutaDetalle other = (MinutaDetalle) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

}
