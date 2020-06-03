package com.conexia.contractual.model.contratacion.comite;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.EstadoComiteEnum;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * The persistent class for the comite database table.
 *
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "ComitePrecontratacion.findAll", query = "SELECT NEW com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto("
            + "c.id, c.fechaComite, c.fechaLimitePrestadores, c.cantidadPrestadores, c.limitePrestadores, c.estadoComite, "
            + "c.nombreArchivoActa)"
            + " FROM ComitePrecontratacion c ORDER BY CASE estadoComite WHEN 'FINALIZADO' THEN 2 ELSE 1 END ASC, c.fechaComite, c.fechaLimitePrestadores ASC"),
    @NamedQuery(name = "ComitePrecontratacion.findById", query = "select new com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto("
            + "c.id, c.fechaComite, c.fechaLimitePrestadores, c.cantidadPrestadores, c.limitePrestadores, c.estadoComite, "
            + "c.nombreArchivoActa) "
            + "from ComitePrecontratacion c "
            + "where c.id = :id "),
    @NamedQuery(
    	name = "ComitePrecontratacion.updateDatesAndEstadoById", 
    	query = "UPDATE ComitePrecontratacion c "
            + " SET c.fechaComite = :fechaComite, c.fechaLimitePrestadores = :fechaLimitePrestadores, c.estadoComite = :estadoComite "
            + "WHERE c.id =:comiteId"),
    @NamedQuery(name = "ComitePrecontratacion.findComiteDisponible", query = "SELECT NEW com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto("
            + "c.id, MIN(c.fechaComite), c.fechaLimitePrestadores, c.cantidadPrestadores, c.limitePrestadores, c.estadoComite, c.nombreArchivoActa) "
            + "FROM ComitePrecontratacion c "
            + "WHERE c.estadoComite NOT IN (:estadosComite) AND c.fechaLimitePrestadores >=:fechaActual GROUP BY c.id, c.fechaComite "
            + "ORDER BY c.fechaComite ASC "),
    @NamedQuery(name = "ComitePrecontratacion.updateEstadoById", query = "UPDATE ComitePrecontratacion c "
            + "SET c.estadoComite =:estado "
            + "WHERE c.id =:comiteId"),
    @NamedQuery(name = "ComitePrecontratacion.aumentarPrestadoresComite", query = "UPDATE ComitePrecontratacion c "
            + "SET c.cantidadPrestadores = (cantidadPrestadores+1) "
            + "WHERE c.id =:comiteId"),
    @NamedQuery(name = "ComitePrecontratacion.updateActaServidor", query = "UPDATE ComitePrecontratacion c "
            + "SET c.nombreArchivoActa = :nombreActaServidor WHERE c.id = :comiteId "),
    @NamedQuery(name = "ComitePrecontratacion.obtenerNombreActaServidorById", query = "SELECT c.nombreArchivoActa  "
            + "FROM ComitePrecontratacion c WHERE c.id = :comiteId "),
    @NamedQuery(
    	name = "ComitePrecontratacion.findNext", 
    	query = "SELECT new com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto(c.id, c.fechaComite) "
            + "FROM ComitePrecontratacion c "
            + "WHERE c.fechaComite > current_date "
            + "AND c.fechaLimitePrestadores >= current_date "
            + "AND c.estadoComite NOT IN ('FINALIZADO') "
            + "AND c.id <> :comiteId "
            + "ORDER BY c.fechaComite "),
    @NamedQuery(
    	name = "ComitePrecontratacion.existeDisponible", 
    	query = "SELECT new com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto(c.id, c.fechaComite) "
            + "FROM ComitePrecontratacion c "
            + "WHERE c.fechaComite > current_date "
            + "AND c.fechaLimitePrestadores >= current_date "
            + "AND c.estadoComite NOT IN ('FINALIZADO') "
            + "AND c.limitePrestadores > (SELECT COUNT(p.id) FROM PrestadorComite p WHERE p.comite.id=c.id) "
            + "ORDER BY c.fechaComite "),
    @NamedQuery(name = "ComitePrecontratacion.findByFechaComiteGreaterOrEqual", query = "select new com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto("
            + "c.id, c.fechaComite, c.fechaLimitePrestadores, c.cantidadPrestadores, c.limitePrestadores, c.estadoComite, "
            + "c.nombreArchivoActa) "
            + "from ComitePrecontratacion c "
            + "where c.fechaComite between current_date and :fecha "
            + "and c.estadoComite not in ('FINALIZADO', 'APLAZADO') "
            + "order by c.fechaComite "),
    @NamedQuery(
		name="ComitePrecontratacion.evaluarFechaDisponible",
		query="SELECT COUNT(c.id) = 0 FROM ComitePrecontratacion c WHERE c.fechaComite = :fechaComite "
	)
})
@Table(schema = "contratacion", name = "comite_precontratacion")
public class ComitePrecontratacion implements Identifiable<Long>, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 4105536873920371605L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "fecha_comite", nullable = false)
    private Date fechaComite;

    @Column(name = "fecha_limite_prestadores", nullable = false)
    private Date fechaLimitePrestadores;

    @Column(name = "cantidad_prestadores", nullable=false, columnDefinition="integer default 0")
    private Integer cantidadPrestadores;

    @Column(name = "limite_prestadores", nullable = false)
    private Integer limitePrestadores;

    @Column(name = "estado", nullable = false)
    @Enumerated(EnumType.STRING)
    private EstadoComiteEnum estadoComite;
    
    @Column(name = "nombre_archivo_acta")
    private String nombreArchivoActa;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(Long id) {
        this.id = id;
    }

    public ComitePrecontratacion(Long id) {
        this.id = id;
    }

    public ComitePrecontratacion() {

    }

    /**
     * @return the fechaComite
     */
    public Date getFechaComite() {
        return fechaComite;
    }

    /**
     * @param fechaComite the fechaComite to set
     */
    public void setFechaComite(Date fechaComite) {
        this.fechaComite = fechaComite;
    }

    /**
     * @return the fechaLimitePrestadores
     */
    public Date getFechaLimitePrestadores() {
        return fechaLimitePrestadores;
    }

    /**
     * @param fechaLimitePrestadores the fechaLimitePrestadores to set
     */
    public void setFechaLimitePrestadores(Date fechaLimitePrestadores) {
        this.fechaLimitePrestadores = fechaLimitePrestadores;
    }

    /**
     * @return the cantidadPrestadores
     */
    public Integer getCantidadPrestadores() {
        return cantidadPrestadores;
    }

    /**
     * @param cantidadPrestadores the cantidadPrestadores to set
     */
    public void setCantidadPrestadores(Integer cantidadPrestadores) {
        this.cantidadPrestadores = cantidadPrestadores;
    }

    /**
     * @return the limitePrestadores
     */
    public Integer getLimitePrestadores() {
        return limitePrestadores;
    }

    /**
     * @param limitePrestadores the limitePrestadores to set
     */
    public void setLimitePrestadores(Integer limitePrestadores) {
        this.limitePrestadores = limitePrestadores;
    }

    /**
     * @return the estadoComite
     */
    public EstadoComiteEnum getEstadoComite() {
        return estadoComite;
    }

    /**
     * @param estadoComite the estadoComite to set
     */
    public void setEstadoComite(EstadoComiteEnum estadoComite) {
        this.estadoComite = estadoComite;
    }

    public String getNombreArchivoActa() {
        return nombreArchivoActa;
    }

    public void setNombreArchivoActa(String nombreArchivoActa) {
        this.nombreArchivoActa = nombreArchivoActa;
    }
    
}
