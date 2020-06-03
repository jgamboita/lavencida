package com.conexia.contractual.model.contratacion;

import com.conexia.contratacion.commons.constants.enums.TipoDocumentoEnum;

import javax.persistence.*;
import java.io.Serializable;

/**
 *
 * @author Andrés Mise Olivera
 */
@Entity
@NamedQueries({
    @NamedQuery(name = "DocumentacionPrestador.findByPrestadorId", query = "select new com.conexia.contratacion.commons.dto.maestros.DocumentacionPrestadorDto("
            + "dp.id, dp.contentType, dp.nombreArchivo, dp.nombreArchivoServidor, dp.tipoDocumento) "
            + "from DocumentacionPrestador dp "
            + "join dp.prestador p "
            + "where p.id = :prestadorId"),
})
@Table(name = "documentacion_prestador", schema = "contratacion")
public class DocumentacionPrestador implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;
    
    @Column(name = "content_type", unique = true, nullable = false)
    private String contentType;

    @Column(name = "nombre_archivo", nullable = false, length = 256)
    private String nombreArchivo;

    @Column(name = "nombre_archivo_serv", nullable = false, length = 100)
    private String nombreArchivoServidor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "prestador_id", nullable = false)
    private Prestador prestador;

    @Column(name = "tipo_documento_id", nullable = false)
    private TipoDocumentoEnum tipoDocumento;

    public DocumentacionPrestador() {
    }

    public DocumentacionPrestador(Long id, String contentType, 
            String nombreArchivo, String nombreArchivoServidor, 
            Prestador prestador, TipoDocumentoEnum tipoDocumento) {
        this.id = id;
        this.contentType = contentType;
        this.nombreArchivo = nombreArchivo;
        this.nombreArchivoServidor = nombreArchivoServidor;
        this.prestador = prestador;
        this.tipoDocumento = tipoDocumento;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
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

    public Prestador getPrestador() {
        return prestador;
    }

    public void setPrestador(Prestador prestador) {
        this.prestador = prestador;
    }

    public TipoDocumentoEnum getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumentoEnum tipoDocumento) {
        this.tipoDocumento = tipoDocumento;
    }
    
}
