package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;


/**
 * Entidad para la documentacion del servicio.
 * 
 * @author jpicon
 * @date 20/08/2014 
 */

@Entity
@Table(name="documentacion_servicio", schema = "contratacion")
public class DocumentacionServicio implements Identifiable<Long>, Serializable {

    private static final long serialVersionUID = -4687650936572524909L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "nombre_archivo", nullable = false, length = 256)
    private String nombreArchivo;

    @Column(name = "nombre_archivo_serv", nullable = false, length = 100)
    private String nombreArchivoServidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_servicio_id", nullable = false)
    private GrupoServicio grupoServicio;
	
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getNombreArchivoServidor() {
        return nombreArchivoServidor;
    }

    public void setNombreArchivoServidor(String nombreArchivoServidor) {
        this.nombreArchivoServidor = nombreArchivoServidor;
    }

    public GrupoServicio getGrupoServicio() {
        return grupoServicio;
    }

    public void setGrupoServicio(GrupoServicio grupoServicio) {
        this.grupoServicio = grupoServicio;
    }

}