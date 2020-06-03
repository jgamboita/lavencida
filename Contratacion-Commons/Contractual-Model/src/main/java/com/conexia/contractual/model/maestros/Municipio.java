package com.conexia.contractual.model.maestros;

import javax.persistence.*;

/**
 * Entity municipio.
 *
 * @author jalvarado
 */
@Entity
@Table(name = "municipio", schema = "maestros")
@NamedQueries({
	@NamedQuery(
			name = "Municipio.consultarPorSedeNegociacionId",
			query = "SELECT new com.conexia.contratacion.commons.dto.maestros.MunicipioDto(m.id, m.descripcion, d.id, d.descripcion) FROM Municipio m JOIN m.departamento d WHERE m.departamento.id = (SELECT d.id FROM SedesNegociacion sn JOIN sn.sedePrestador s JOIN s.municipio m JOIN m.departamento d WHERE sn.id = :sedeNegociacionId)"
		),
	@NamedQuery(
            name = "Municipio.consultarEntityPorSedeNegociacionId",
            query = "SELECT m FROM Municipio m JOIN m.departamento d WHERE m.departamento.id = (SELECT d.id FROM SedePrestador s JOIN s.municipio m JOIN m.departamento d WHERE s.id = :sedePrestadorId)"
        ),
	@NamedQuery(
			name ="Municipio.consultarMunicipioSedePrestador",
			query = "SELECT m FROM Municipio m WHERE m.id = (SELECT sp.municipio.id FROM SedePrestador sp WHERE sp.id = :sedePrestadorId) "
			),
	@NamedQuery(name = "Municipio.findByDepartamentoId",
	query = "SELECT m FROM Municipio m "
			+ " WHERE m.departamento.id IN (:departamentoId) "
			+ " ORDER BY m.descripcion"),
    @NamedQuery(name = "Municipio.listarByDepartamentoId",
            query = "select new com.conexia.contratacion.commons.dto.maestros.MunicipioDto(m.id, m.descripcion) "
                    + "from Municipio m join m.departamento d where d.id = :idDepartamento "
					+ " ORDER BY m.descripcion ASC "),
    @NamedQuery(name = "Municipio.findMunicipioIdFromSedePresador",
            query = "select sp.municipio.id from SedePrestador sp where sp.id = :sedePrestador"),
    @NamedQuery(name = "Municipio.listarByDepartamentoZona",
    query = "select new com.conexia.contratacion.commons.dto.maestros.MunicipioDto(m.id, m.descripcion) "
    		+ " from Municipio m "
    		+ " join m.zona z  "
    		+ " join m.departamento d "
    		+ " where d.id = :idDepartamento"
    		+ " Order by m.descripcion ")
})
public class Municipio extends Descriptivo {

    /**
     *
     */
    private static final long serialVersionUID = 2782731117974154924L;

    /**
     * Código del municipio.
     */
    @Column(name = "codigo", nullable = false, length = 10)
    private String codigo;


    @ManyToOne
    @JoinColumn(name = "departamento_id")
    private Departamento departamento;

    /**
     * Codigo de la zona del municipio.
     */
    @Column(name = "codigo_zona", length = 50)
    private String codigoZona;

    @ManyToOne
    @JoinColumn(name = "zona_municipio_id")
    private ZonaMunicipio zona;

    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
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
     * Devuelve el valor de departamento.
     *
     * @return El valor de departamento.
     */
    public Departamento getDepartamento() {
        return this.departamento;
    }


    /**
     * Asigna un nuevo valor a departamento.
     *
     * @param departamento El valor a asignar a departamento.
     */
    public void setDepartamento(Departamento departamento) {
        this.departamento = departamento;
    }


    /**
     * Devuelve el valor de codigoZona.
     *
     * @return El valor de codigoZona.
     */
    public String getCodigoZona() {
        return this.codigoZona;
    }


    /**
     * Asigna un nuevo valor a codigoZona.
     *
     * @param codigoZona El valor a asignar a codigoZona.
     */
    public void setCodigoZona(String codigoZona) {
        this.codigoZona = codigoZona;
    }


	public ZonaMunicipio getZona() {
		return zona;
	}


	public void setZona(ZonaMunicipio zona) {
		this.zona = zona;
	}

//</editor-fold>


}
