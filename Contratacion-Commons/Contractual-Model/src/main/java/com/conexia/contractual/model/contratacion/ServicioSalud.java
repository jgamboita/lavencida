package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.TemasCapitaEnum;
import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;
import com.conexia.contractual.model.contratacion.converter.TemasCapitaConverter;
import com.conexia.contractual.model.contratacion.portafolio.ServicioSaludModalidad;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * Entidad servicio salud.
 *
 * @author mcastro
 * @date 23/05/2014
 */
@Entity
@Table(name = "servicio_salud", schema = "contratacion")
@NamedQueries({
    @NamedQuery(name = "ServicioSalud.findDtoServiciosPuedenNegociarByPrestadorId", query = "select DISTINCT NEW com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto(ss.id, ss.nombre ,ms.id, ms.nombre) "
            + "FROM Prestador p JOIN p.sedePrestador sp JOIN sp.portafolio por JOIN por.grupoServicios gs join gs.servicioSalud ss join ss.macroServicio ms join gs.procedimientoPortafolios pp join pp.procedimiento ps "
            + "WHERE p.id = :prestadorId AND sp.enumStatus = 1 AND ps.complejidad IN(:complejidades) "),
    @NamedQuery(name = "ServicioSalud.findDtoServiciosPuedenNegociarByPrestadorIdAndModalidad", query = "select NEW com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto(ss.id, ss.nombre ,ms.id, ms.nombre) "
            + "FROM OfertaSedePrestador mosp "
            + "JOIN mosp.portafolio por "
            + "JOIN por.servicioPortafolioSedes sps "
            + "JOIN sps.procedimientoServicioPortafolioSedes ps "
            + "JOIN mosp.ofertaPrestador op "
            + "JOIN sps.servicioSalud ss "
            + "JOIN ss.macroServicio ms "
            + "WHERE por.prestador.id = :prestadorId "
            + "AND op.ofertaPresentar = true "
            + "AND sps.habilitado = true "
            + "AND op.modalidad.id = :modalidad "
            + "AND ps.procedimientoServicio.id NOT IN ("
            + "SELECT snp.procedimiento.id FROM SedesNegociacion sn "
            + "JOIN sn.negociacion n "
            + "join sn.sedeNegociacionServicios sns "
            + "join sns.sedeNegociacionProcedimientos snp "
            + "WHERE mosp.sedePrestador.id = sn.sedePrestador.id "
            + "AND n.tipoModalidadNegociacion = :modalidadEnum ) "
            + "GROUP BY ss.id, ss.nombre ,ms.id, ms.nombre")
})

@SqlResultSetMappings({
	@SqlResultSetMapping(name = "ServicioSalud.serviciosAgregarMapping",
	classes = @ConstructorResult(targetClass = ServicioSaludDto.class, columns = {
		@ColumnResult(name = "id", type = Long.class),
		@ColumnResult(name = "codigo", type = String.class),
		@ColumnResult(name = "nombre", type = String.class),
		@ColumnResult(name = "sede", type = String.class),
		@ColumnResult(name = "sedeNegociacion", type = Long.class),
        @ColumnResult(name = "nivel_servicio", type = Integer.class)
	}))
})
public class ServicioSalud implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "codigo", length = 20)
    private String codigo;

    @Column(name = "nombre", length = 500)
    private String nombre;

    @OneToMany(mappedBy = "servicioSalud")
    private Set<GrupoServicio> grupoServicios;

    @OneToMany(mappedBy = "servicioSalud")
    private Set<UpcLiquidacionServicio> liquidacionServicios;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "macroservicio_id")
    private MacroServicio macroServicio;

    @Column(name = "tema_capita_id", nullable = true)
    @Convert(converter = TemasCapitaConverter.class)
    private TemasCapitaEnum temaCapita;

    @Column(name = "enabled")
    private Boolean estado;

    @OneToMany(mappedBy = "servicioSalud")
    private List<ServicioSaludModalidad> modalidad;

    public ServicioSalud() {
    }

    public ServicioSalud(Long id) {
        this.id = id;
    }

    //<editor-fold desc="Getters && Setters">
    @Override
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigo() {
        return this.codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Set<GrupoServicio> getGrupoServicios() {
        return this.grupoServicios;
    }

    public void setGrupoServicios(Set<GrupoServicio> grupoServicios) {
        this.grupoServicios = grupoServicios;
    }

    public Set<UpcLiquidacionServicio> getLiquidacionServicios() {
        return liquidacionServicios;
    }

    public void setLiquidacionServicios(Set<UpcLiquidacionServicio> liquidacionServicios) {
        this.liquidacionServicios = liquidacionServicios;
    }

    public MacroServicio getMacroServicio() {
        return this.macroServicio;
    }

    public void setMacroServicio(MacroServicio macroServicio) {
        this.macroServicio = macroServicio;
    }

    public TemasCapitaEnum getTemaCapita() {
        return temaCapita;
    }

    public void setTemaCapita(TemasCapitaEnum temaCapita) {
        this.temaCapita = temaCapita;
    }

    public List<ServicioSaludModalidad> getModalidad() {
        return modalidad;
    }

    public void setModalidad(List<ServicioSaludModalidad> modalidad) {
        this.modalidad = modalidad;
    }

	public Boolean getEstado() {
		return estado;
	}

	public void setEstado(Boolean estado) {
		this.estado = estado;
	}
    //</editor-fold>

}
