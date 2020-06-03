package co.conexia.negociacion.wap.rest.negociacion;

public enum RestEnum {

    HEADER_AUTHORIZATION("Authorization"),
    PASSWORD_AUTHORIZATION("\"*claveRolAltaUsuario*\"");

    private String descripcion;

    /**
     * Constructor con parametros
     *
     * @param descripcion
     */
    private RestEnum(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getDescripcion() {
        return descripcion;
    }

}
