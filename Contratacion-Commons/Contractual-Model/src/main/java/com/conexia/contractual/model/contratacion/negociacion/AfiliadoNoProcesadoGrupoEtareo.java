package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "afiliado_no_procesado_grupo_etareo", schema = "contratacion")
@NamedQueries({
        @NamedQuery(name = "AfiliadoNoProcesado.findAllToDto", query = "SELECT NEW com.conexia.contratacion.commons.dto.negociacion.AfiliadoNoProcesadoGrupoEtareoDto( "
                + "af.id, af.codigoUnicoAfiliado, af.codigoTipoIdentificacion, af.numeroIdentificacion, af.mensaje, af.negociacion.id) FROM "
                + "AfiliadoNoProcesadoGrupoEtareo af "
                + "WHERE af.negociacion.id = :id_negociacion "),
        @NamedQuery(name = "AfiliadoNoProcesado.deleteByNegociacionId", query = "DELETE FROM AfiliadoNoProcesadoGrupoEtareo anp "
                + "WHERE anp.negociacion.id = :negociacionId "),
        @NamedQuery(name = "AfiliadoNoProcesado.updateDeletedRegistro",
                query = "update AfiliadoNoProcesadoGrupoEtareo anpge set anpge.deleted = :deleted where anpge.negociacion.id =:negociacionId ")

})
public class AfiliadoNoProcesadoGrupoEtareo implements Identifiable<Long>, Serializable {

    @Id
    private Long id;

    @Column(name = "codigo_unico_afiliado")
    private String codigoUnicoAfiliado;

    @Column(name = "codigo_tipo_identificacion")
    private String codigoTipoIdentificacion;

    @Column(name = "numero_identificacion")
    private String numeroIdentificacion;

    @Column(name = "deleted")
    private boolean deleted;

    private String mensaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negociacion_id")
    private Negociacion negociacion;

	//<editor-fold desc="Getters && Setters">
	public void setId(Long id) {
        this.id = id;
    }

    @Override
    public Long getId() {
        return this.id;
    }

    public String getCodigoUnicoAfiliado() {
        return codigoUnicoAfiliado;
    }

    public void setCodigoUnicoAfiliado(String codigoUnicoAfiliado) {
        this.codigoUnicoAfiliado = codigoUnicoAfiliado;
    }

    public String getCodigoTipoIdentificacion() {
        return codigoTipoIdentificacion;
    }

    public void setCodigoTipoIdentificacion(String codigoTipoIdentificacion) {
        this.codigoTipoIdentificacion = codigoTipoIdentificacion;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    public Negociacion getNegociacion() {
        return negociacion;
    }

    public void setNegociacion(Negociacion negociacion) {
        this.negociacion = negociacion;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
	//</editor-fold>
}
