package com.conexia.contractual.model.maestros;

import javax.persistence.*;

/**
 * Entity de tipo de identificacion.
 *
 * @author jalvarado
 */
@Entity
@NamedQueries({
    @NamedQuery(name="TipoIdentificacion.findAll",
			query=	"   SELECT new com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto(ti.id, ti.descripcion) "
					+ " FROM TipoIdentificacion ti order by ti.descripcion"),
    @NamedQuery(name="TipoIdentificacion.findAllIPS",
			query=	"   SELECT new com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto(ti.id, ti.descripcion) "
					+ " FROM TipoIdentificacion ti"
                                        + " where ti.aplicaIps=1"
                                        + " order by ti.descripcion")
})
@Table(name = "tipo_identificacion", schema = "maestros")
public class TipoIdentificacion extends Descriptivo {

    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 9031344663837514397L;

    @Column(name = "codigo", nullable = true, length = 50)
    private String codigo;

    @Column(name = "es_alfanumerico", nullable = false)
    private Short esAlfanumerico;

    @Column(name = "min_length", nullable = false)
    private Integer minLength;

    @Column(name = "max_length", nullable = false)
    private Integer maxLength;

    @Column(name = "aplica_ips")
    private Short aplicaIps;

    @Column(name = "aplica_afiliado")
    private Short aplicaAfiliado;

    @Column(name = "aplica_profesional")
    private Short aplicaProfesional;

    @Column(name = "aplica_usuario")
    private Short aplicaUsuario;

    public TipoIdentificacion() {
    }

    public TipoIdentificacion(Integer id) {
        super(id);
    }

    /**
     * Devuelve el valor de codigo.
     *
     * @return El valor de codigo.
     */
    public String getCodigo() {
        return this.codigo;
    }


    /**
     * Asigna un nuevo valor a codigo.
     *
     * @param codigo El valor a asignar a codigo.
     */
    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }


    /**
     * Devuelve el valor de esAlfanumerico.
     *
     * @return El valor de esAlfanumerico.
     */
    public Short getEsAlfanumerico() {
        return this.esAlfanumerico;
    }


    /**
     * Asigna un nuevo valor a esAlfanumerico.
     *
     * @param esAlfanumerico El valor a asignar a esAlfanumerico.
     */
    public void setEsAlfanumerico(Short esAlfanumerico) {
        this.esAlfanumerico = esAlfanumerico;
    }


    /**
     * Devuelve el valor de minLength.
     *
     * @return El valor de minLength.
     */
    public Integer getMinLength() {
        return this.minLength;
    }


    /**
     * Asigna un nuevo valor a minLength.
     *
     * @param minLength El valor a asignar a minLength.
     */
    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }


    /**
     * Devuelve el valor de maxLength.
     *
     * @return El valor de maxLength.
     */
    public Integer getMaxLength() {
        return this.maxLength;
    }


    /**
     * Asigna un nuevo valor a maxLength.
     *
     * @param maxLength El valor a asignar a maxLength.
     */
    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }


    /**
     * Devuelve el valor de aplicaIps.
     *
     * @return El valor de aplicaIps.
     */
    public Short getAplicaIps() {
        return this.aplicaIps;
    }


    /**
     * Asigna un nuevo valor a aplicaIps.
     *
     * @param aplicaIps El valor a asignar a aplicaIps.
     */
    public void setAplicaIps(Short aplicaIps) {
        this.aplicaIps = aplicaIps;
    }


    /**
     * Devuelve el valor de aplicaAfiliado.
     *
     * @return El valor de aplicaAfiliado.
     */
    public Short getAplicaAfiliado() {
        return this.aplicaAfiliado;
    }


    /**
     * Asigna un nuevo valor a aplicaAfiliado.
     *
     * @param aplicaAfiliado El valor a asignar a aplicaAfiliado.
     */
    public void setAplicaAfiliado(Short aplicaAfiliado) {
        this.aplicaAfiliado = aplicaAfiliado;
    }


    /**
     * Devuelve el valor de aplicaProfesional.
     *
     * @return El valor de aplicaProfesional.
     */
    public Short getAplicaProfesional() {
        return this.aplicaProfesional;
    }


    /**
     * Asigna un nuevo valor a aplicaProfesional.
     *
     * @param aplicaProfesional El valor a asignar a aplicaProfesional.
     */
    public void setAplicaProfesional(Short aplicaProfesional) {
        this.aplicaProfesional = aplicaProfesional;
    }


    /**
     * Devuelve el valor de aplicaUsuario.
     *
     * @return El valor de aplicaUsuario.
     */
    public Short getAplicaUsuario() {
        return this.aplicaUsuario;
    }


    /**
     * Asigna un nuevo valor a aplicaUsuario.
     *
     * @param aplicaUsuario El valor a asignar a aplicaUsuario.
     */
    public void setAplicaUsuario(Short aplicaUsuario) {
        this.aplicaUsuario = aplicaUsuario;
    }


}
