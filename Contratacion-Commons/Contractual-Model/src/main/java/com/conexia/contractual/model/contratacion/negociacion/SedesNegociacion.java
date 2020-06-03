package com.conexia.contractual.model.contratacion.negociacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.contractual.model.contratacion.SedePrestador;
import com.conexia.contractual.model.maestros.Municipio;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * The persistent class for the sede_negociacion database table.
 *
 */
@Entity
@Table(schema = "contratacion", name = "sedes_negociacion")
@NamedQueries({
    @NamedQuery(name = "SedesNegociacion.contarAreaInfluencia", query = "Select count(distinct m.id)  "
            + " from SedesNegociacion s join s.municipios m where s.id = :sedeNegociacionId"),
    @NamedQuery(name = "SedesNegociacion.findDtoByNegociacionId", query = "SELECT NEW com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto("
            + "sn.id, sn.sedePrestador.id, sn.negociacion.id) FROM SedesNegociacion sn WHERE sn.negociacion.id =:negociacionId"),
    @NamedQuery(name = "SedesNegociacion.findDtoByNegociacionIdAndSedePrestadorId", query = "SELECT NEW com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto("
            + "sn.id, sn.sedePrestador.id, sn.negociacion.id) FROM SedesNegociacion sn WHERE sn.negociacion.id =:negociacionId AND sn.sedePrestador.id =:sedePrestadorId"),
    @NamedQuery(name = "SedesNegociacion.findSedePrestadorDtoByNegociacionId", query = "SELECT DISTINCT NEW com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto("
            + "sn.id, sp.id, sn.negociacion.id, sp.nombreSede, mun.id, mun.descripcion, dep.descripcion, sp.direccion, sp.zona.id, sp.sedePrincipal, sp.codigoHabilitacion, sp.codigoSede ) "
            + "FROM SedesNegociacion sn JOIN sn.sedePrestador sp JOIN sp.municipio mun JOIN mun.departamento dep "
            + "WHERE sn.negociacion.id =:negociacionId"),
    @NamedQuery(name = "SedesNegociacion.findMunicipiosCoberturaById", query = " SELECT DISTINCT NEW com.conexia.contratacion.commons.dto.maestros.MunicipioDto"
            + "(m.id, m.descripcion, d.id ,d.descripcion) "
            + " FROM SedesNegociacion sd "
            + " JOIN sd.municipios m "
            + " JOIN m.departamento d "
            + " WHERE sd.id = :sedesNegociacionId ORDER BY d.descripcion, m.descripcion"),
    @NamedQuery(name = "SedesNegociacion.countBySedesNegociacionMunicio", query = "SELECT COUNT(sn.id) FROM SedesNegociacion sn "
            + "JOIN sn.negociacion n "
            + "JOIN sn.sedePrestador sp "
            + "JOIN sp.municipio m "
            + "WHERE n.id = :idNegociacion AND sn.id <> :idSedeNegociacion AND m.id = :idMunicipio"),
    @NamedQuery(name = "SedesNegociacion.deleteByNegociacionIdAndSedePrestador", query = "DELETE FROM SedesNegociacion sn WHERE sn.negociacion.id = :negociacionId AND sn.sedePrestador.id = :sedePrestadorId"),
    @NamedQuery(name = "SedesNegociacion.deleteByNegociacionId", query = "DELETE FROM SedesNegociacion sn WHERE sn.negociacion.id = :negociacionId"),
    @NamedQuery(
            name = "SedesNegociacion.deleteById",
            query = "delete from SedesNegociacion sn WHERE sn.id = :id "),
    @NamedQuery(name = "SedesNegociacion.findSedeBySedeNegociacionId", query = "SELECT sn from SedesNegociacion sn "
            + "where sn.id = :sedeNegociacionId"),
    @NamedQuery(name = "SedesNegociacion.findSedeWithMedicamentosById", query = "SELECT DISTINCT sn.id from SedesNegociacion sn "
            + "JOIN sn.sedeNegociacionMedicamentos snm "
            + "where sn.id = :sedeNegociacionId"),
    @NamedQuery(name = "SedesNegociacion.updateSedePrincipalBySedePestadorAndNegociacionId",
    query = "UPDATE SedesNegociacion sn SET sn.principal =:principal "
            + "WHERE sn.negociacion.id = :negociacionId AND sn.sedePrestador.id = :sedePrestadorId"),
    @NamedQuery(name = "SedesNegociacion.obtenerIdSedesNegociacion",
	query = "select distinct sn.id"
			+ " from SedesNegociacion sn "
			+ " join sn.negociacion neg "
			+ " where sn.negociacion.id = :negociacionId "),
    @NamedQuery(name = "SedesNegociacion.findSedePrestadorPrincipalDtoByNegociacionId",
    	query = "SELECT NEW com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto("
            + "sn.id, sp.id, sn.negociacion.id, sp.nombreSede, mun.id, mun.descripcion, dep.id, dep.descripcion, sp.direccion, sp.zona.id, sp.sedePrincipal, sp.codigoHabilitacion, sp.codigoSede) "
            + "FROM SedesNegociacion sn JOIN sn.sedePrestador sp JOIN sp.municipio mun JOIN mun.departamento dep "
            + "WHERE sn.negociacion.id =:negociacionId "
            + "AND sn.principal = true"),
    @NamedQuery(name = "SedesNegociacion.sedePrincipalNegociacion",
    		query = "SELECT DISTINCT sp.nombreSede FROM SedesNegociacion sn "
    		+ "JOIN sn.sedePrestador sp "
    		+ "WHERE sn.negociacion.id = :negociacionId AND sn.principal = :principal "),
        @NamedQuery(name = "SedesNegociacion.updateDeletedRegistro",
                query = "update SedesNegociacion sn set sn.deleted = :deleted where sn.negociacion.id =:negociacionId ")
})
@NamedNativeQueries({
    @NamedNativeQuery(name = "SedesNegociacion.countServiciosProcedByNegociacion",
            query = " select count(*) from contratacion.sede_negociacion_servicio sns\n"
            + "inner join contratacion.sedes_negociacion sn on sn.id=sns.sede_negociacion_id\n"
            + "where sn.negociacion_id=:idNegociacion and sns.negociado=true ")
})
@SqlResultSetMappings({
    @SqlResultSetMapping(
            name = "SedesNegociacion.sedeNegociacionMapping",
            classes = @ConstructorResult(
                    targetClass = SedesNegociacionDto.class,
                    columns = {
                    	@ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "sede_prestador_id", type = Long.class),
                        @ColumnResult(name = "negociacion_id", type = Long.class)
                    }
            ))
})
public class SedesNegociacion implements Identifiable<Long>, Serializable {

    public static final String BUSCAR_SEDES_POR_NEGOCIACION = "SedesNegociacion.buscarSedesPorNegociacion";


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negociacion_id")
    private Negociacion negociacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sede_prestador_id")
    private SedePrestador sedePrestador;

    @ManyToMany
    @JoinTable(name = "area_cobertura_sedes", schema = "contratacion", joinColumns = {
        @JoinColumn(name = "sede_negociacion_id")}, inverseJoinColumns = {
        @JoinColumn(name = "municipio_id")})
    private Set<Municipio> municipios;

    @OneToMany(mappedBy = "sedeNegociacion", fetch = FetchType.LAZY)
    private List<SedeNegociacionMedicamento> sedeNegociacionMedicamentos;

    @OneToMany(mappedBy = "sedeNegociacion", fetch = FetchType.LAZY)
    private List<SedeNegociacionPaquete> sedeNegociacionPaquetes;

    @OneToMany(mappedBy = "sedeNegociacion", fetch = FetchType.LAZY)
    private List<SedeNegociacionServicio> sedeNegociacionServicios;

    @OneToMany(mappedBy ="sedeNegociacion", fetch = FetchType.LAZY)
    private List<SedeNegociacionCategoriaMedicamento> sedeNegociacionCategoriaMedicamento;

    @Column(name = "principal")
    private Boolean principal;

    @Column(name = "deleted")
    private boolean deleted;
    
    public SedesNegociacion() {
    }

    public SedesNegociacion(Long id) {
        this.id = id;
    }

    //<editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public Long getId() {
        return this.id;
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

    public SedePrestador getSedePrestador() {
        return sedePrestador;
    }

    public void setSedePrestador(SedePrestador sedePrestador) {
        this.sedePrestador = sedePrestador;
    }

    public Set<Municipio> getMunicipios() {
        return municipios;
    }

    public void setMunicipios(Set<Municipio> municipios) {
        this.municipios = municipios;
    }

    public List<SedeNegociacionMedicamento> getSedeNegociacionMedicamentos() {
        return sedeNegociacionMedicamentos;
    }

    public void setSedeNegociacionMedicamentos(List<SedeNegociacionMedicamento> sedeNegociacionMedicamentos) {
        this.sedeNegociacionMedicamentos = sedeNegociacionMedicamentos;
    }

    public List<SedeNegociacionPaquete> getSedeNegociacionPaquetes() {
        return sedeNegociacionPaquetes;
    }

    public void setSedeNegociacionPaquetes(List<SedeNegociacionPaquete> sedeNegociacionPaquetes) {
        this.sedeNegociacionPaquetes = sedeNegociacionPaquetes;
    }

    public List<SedeNegociacionServicio> getSedeNegociacionServicios() {
        return sedeNegociacionServicios;
    }

    public void setSedeNegociacionServicios(List<SedeNegociacionServicio> sedeNegociacionServicios) {
        this.sedeNegociacionServicios = sedeNegociacionServicios;
    }

    public Boolean getPrincipal() {
        return principal;
    }

    public void setPrincipal(Boolean principal) {
        this.principal = principal;
    }

    public List<SedeNegociacionCategoriaMedicamento> getSedeNegociacionCategoriaMedicamento() {
		return sedeNegociacionCategoriaMedicamento;
	}

	public void setSedeNegociacionCategoriaMedicamento(List<SedeNegociacionCategoriaMedicamento> sedeNegociacionCategoriaMedicamento) {
		this.sedeNegociacionCategoriaMedicamento = sedeNegociacionCategoriaMedicamento;
	}

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
	//</editor-fold>
}
