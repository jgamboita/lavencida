package com.conexia.contractual.model.maestros;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contractual.model.contratacion.ServicioSalud;
import com.conexia.contractual.model.contratacion.UpcLiquidacionProcedimiento;
import com.conexia.contractual.model.contratacion.negociacion.SedeNegociacionPaqueteTraslado;
import com.conexia.contractual.model.contratacion.negociacion.SedeNegociacionTransporte;
import com.conexia.contractual.model.contratacion.portafolio.TransportePortafolio;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "procedimiento_servicio", schema = "maestros")
@NamedQueries({
    @NamedQuery(name = "Procedimientos.consultaProcedimiientoServicio" ,
    		query = "SELECT new com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto(p.id,p.cups,p.codigoCliente, p.estado, p.complejidad,ss.codigo,ss.id) "
    				+ "FROM Procedimientos p INNER JOIN  p.servicioSalud ss  "
    				+ "WHERE p.codigoCliente in(:codigoEmssanar) AND ss.codigo in(:codigoServicio) and p.estado = 1 "),
})
@SqlResultSetMappings({
	@SqlResultSetMapping(name = "procedimientosAgregarMapping",
	classes = @ConstructorResult(targetClass = ProcedimientoDto.class, columns = {
		@ColumnResult(name = "id", type = Long.class),
		@ColumnResult(name = "codigoCliente", type = String.class),
		@ColumnResult(name = "cups", type = String.class),
		@ColumnResult(name = "descripcion", type = String.class),
		@ColumnResult(name = "complejidad", type = Integer.class)
	})),
	@SqlResultSetMapping(name = "Procedimientos.listarProcedimientoMapping",
	classes = @ConstructorResult(targetClass = ProcedimientoDto.class, columns = {
		@ColumnResult(name = "id", type = Integer.class),
		@ColumnResult(name = "codigo", type = String.class),
		@ColumnResult(name = "codigo_emssanar", type = String.class),
		@ColumnResult(name = "descripcion", type = String.class),
		@ColumnResult(name = "nivel_complejidad", type = Integer.class)
	})),
	@SqlResultSetMapping(name = "Procedimientos.procedimientoSinValorMapping",
	classes = @ConstructorResult(targetClass = ProcedimientoDto.class, columns = {
		@ColumnResult(name = "servicio_codigo", type = String.class),
		@ColumnResult(name = "servicio_nombre", type = String.class),
		@ColumnResult(name = "codigo_cliente", type = String.class),
		@ColumnResult(name = "cups", type = String.class),
		@ColumnResult(name = "descripcion", type = String.class)
	})),
	@SqlResultSetMapping(name = "Procedimientos.procedimientoSinValorPGPMapping",
	classes = @ConstructorResult(targetClass = ProcedimientoDto.class, columns = {
		@ColumnResult(name = "sedeNegociacionId", type = Long.class),
		@ColumnResult(name = "capituloCodigo", type = String.class),
		@ColumnResult(name = "capitulo", type = String.class),
		@ColumnResult(name = "codigo_cliente", type = String.class),
		@ColumnResult(name = "descripcion", type = String.class),
		@ColumnResult(name = "codigo", type = String.class)
	})),
	@SqlResultSetMapping(name = "Procedimientos.procedimientosSinValorRiaCapita",
	classes = @ConstructorResult(targetClass = ProcedimientoDto.class, columns = {
		@ColumnResult(name = "ruta", type = String.class),
		@ColumnResult(name = "actividad", type = String.class),
		@ColumnResult(name = "codigo_servicio", type = String.class),
		@ColumnResult(name = "nombre_servicio", type = String.class),
		@ColumnResult(name = "codigo_cliente", type = String.class),
		@ColumnResult(name = "cups", type = String.class),
		@ColumnResult(name = "descripcion", type = String.class)

	}))
})
public class Procedimientos implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "cups", length = 10)
    private String cups;

    @Column(name = "codigo_cliente", length = 10)
    private String codigoCliente;

    @Column(name = "nivel_cups", length = 2)
    private String nivelCups;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private ServicioSalud servicioSalud;

    @Column(name = "complejidad")
    private Integer complejidad;

    @Column(name = "es_pos")
    private Boolean esPos;

    @Column(name = "reps_cups")
    private Integer repsCups;

    @OneToOne(fetch = FetchType.LAZY, targetEntity = Procedimiento.class)
    @JoinColumn(name = "procedimiento_id")
    private AbstractTipoTecnologia procedimiento;

    @OneToMany(mappedBy = "procedimientos")
    private Set<UpcLiquidacionProcedimiento> liquidacionProcedimientos;

    @Column(name = "estado")
    private Integer estado;

    @OneToMany(mappedBy = "transporte", fetch = FetchType.LAZY)
    private List<TransportePortafolio> transportePortafolios;

    @OneToMany(mappedBy = "procedimiento", fetch = FetchType.LAZY)
    private List<SedeNegociacionPaqueteTraslado> sedeNegociacionPaqueteTraslados;

    @OneToMany(mappedBy = "procedimiento", fetch = FetchType.LAZY)
    private List<SedeNegociacionTransporte> sedeNegociacionTransportes;

    public Procedimientos() {
    }

    public Procedimientos(Long id) {
        this.id = id;
    }

    //<editor-fold desc="Getters && Setters">
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCups() {
        return cups;
    }

    public void setCups(String cups) {
        this.cups = cups;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public String getNivelCups() {
        return nivelCups;
    }

    public void setNivelCups(String nivelCups) {
        this.nivelCups = nivelCups;
    }

    public ServicioSalud getServicioSalud() {
        return servicioSalud;
    }

    public void setServicioSalud(ServicioSalud servicioSalud) {
        this.servicioSalud = servicioSalud;
    }

    public Integer getComplejidad() {
        return complejidad;
    }

    public void setComplejidad(Integer complejidad) {
        this.complejidad = complejidad;
    }

    public AbstractTipoTecnologia getProcedimiento() {
        return procedimiento;
    }

    public void setProcedimiento(AbstractTipoTecnologia procedimiento) {
        this.procedimiento = procedimiento;
    }

    public Set<UpcLiquidacionProcedimiento> getLiquidacionProcedimientos() {
        return liquidacionProcedimientos;
    }

    public void setLiquidacionProcedimientos(Set<UpcLiquidacionProcedimiento> liquidacionProcedimientos) {
        this.liquidacionProcedimientos = liquidacionProcedimientos;
    }

	public Integer getRepsCups() {
		return repsCups;
	}

	public Integer setRepsCups(Integer repsCups) {
		return this.repsCups = repsCups;
	}

	public Integer getEstado() {
		return estado;
	}

	public void setEstado(Integer estado) {
		this.estado = estado;
	}

    public List<TransportePortafolio> getTransportePortafolios() {
        return transportePortafolios;
    }

    public void setTransportePortafolios(List<TransportePortafolio> transportePortafolios) {
        this.transportePortafolios = transportePortafolios;
    }

    public List<SedeNegociacionPaqueteTraslado> getSedeNegociacionPaqueteTraslados() {
        return sedeNegociacionPaqueteTraslados;
    }

    public void setSedeNegociacionPaqueteTraslados(List<SedeNegociacionPaqueteTraslado> sedeNegociacionPaqueteTraslados) {
        this.sedeNegociacionPaqueteTraslados = sedeNegociacionPaqueteTraslados;
    }

    public List<SedeNegociacionTransporte> getSedeNegociacionTransportes() {
        return sedeNegociacionTransportes;
    }

    public void setSedeNegociacionTransportes(List<SedeNegociacionTransporte> sedeNegociacionTransportes) {
        this.sedeNegociacionTransportes = sedeNegociacionTransportes;
    }
    //</editor-fold>
}
