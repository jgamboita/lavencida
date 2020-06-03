package com.conexia.contractual.model.maestros;

import com.conexia.contratacion.commons.dto.maestros.ProcedimientoServicioDTO;
import com.conexia.contractual.model.contratacion.ServicioSalud;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "procedimiento_servicio", schema = "maestros")
@SqlResultSetMappings({
	@SqlResultSetMapping(name = "ProcedimientoServicio.ProcedimientoServiceMapping",
			classes = @ConstructorResult(targetClass = ProcedimientoServicioDTO.class,
			columns = {
                        @ColumnResult(name = "   parametrizacion_ambulatoria     ", type = String.class),
                        @ColumnResult(name = "     parametrizacion_hospitalaria   ", type = String.class),
                        @ColumnResult(name = " valor_ambito_ambulatorio       ", type = Double.class),
                        @ColumnResult(name = "   valor_ambito_hospitalario     ", type = Double.class),
                        @ColumnResult(name = "     parametrizacion_domiciliaria   ", type = String.class),
                        @ColumnResult(name = "   valor_ambito_domiciliario     ", type = Double.class),
			@ColumnResult(name = "   id     ", type = Long.class),
                        @ColumnResult(name = "     descripcion   ", type = String.class),
                        @ColumnResult(name = " cups       ", type = String.class),
                        @ColumnResult(name = "   nivel_cups     ", type = String.class),
                        @ColumnResult(name = "   servicio_id     ", type = Integer.class),
                        @ColumnResult(name = "   reps_cups     ", type = Integer.class),
                        @ColumnResult(name = "   complejidad     ", type = Integer.class),
                        @ColumnResult(name = "   especialidad_id     ", type = Integer.class),
                        @ColumnResult(name = "   codigo_cliente     ", type = String.class),
                        @ColumnResult(name = "   es_pos     ", type = Boolean.class),
                        @ColumnResult(name = "   procedimiento_id     ", type = Long.class),
                        @ColumnResult(name = "    estado    ", type = Integer.class),
                        @ColumnResult(name = "    valor_negociado    ", type = BigDecimal.class),
                        @ColumnResult(name = "     codigo_cups_capitulo   ", type = String.class),
                        @ColumnResult(name = " descripcion_cups_capitulo       ", type = String.class),
                        @ColumnResult(name = "   codigo_servicio     ", type = String.class),
                        @ColumnResult(name = "   nombre_servicio     ", type = String.class)
			})),
    @SqlResultSetMapping(name = "ProcedimientoServicio.NivelProcedimientoServiceMapping",
			classes = @ConstructorResult(targetClass = ProcedimientoServicioDTO.class,
			columns = {
                        @ColumnResult(name = "   complejidad     ", type = Integer.class),
                        @ColumnResult(name = "   estado     ", type = Integer.class),
                        @ColumnResult(name = "   id     ", type = Long.class)
			})),
})

public class ProcedimientoServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descripcion", nullable = false, length = 500)
    private String descripcion;

    @Column(name = "cups", nullable = false, length = 10)
    private String cups;

    @Column(name = "nivel_cups", nullable = false, length = 2)
    private String nivel_cups;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private ServicioSalud servicioSalud;

    @Column(name = "reps_cups")
    private Integer repsCups;

    @Column(name = "complejidad")
    private Integer complejidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "especialidad_id")
    private ServicioSalud especialidad;

    @Column(name = "codigo_cliente", nullable = false, length = 10)
    private String codigoCliente;

    @Column(name = "es_pos", nullable = false)
    private Boolean esPos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "procedimiento_id")
    private Procedimiento procedimiento;

    @Column(name = "estado")
    private Integer estado;

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

    public String getNivel_cups() {
        return nivel_cups;
    }

    public void setNivel_cups(String nivel_cups) {
        this.nivel_cups = nivel_cups;
    }

    public ServicioSalud getServicioSalud() {
        return servicioSalud;
    }

    public void setServicioSalud(ServicioSalud servicioSalud) {
        this.servicioSalud = servicioSalud;
    }

    public Integer getRepsCups() {
        return repsCups;
    }

    public void setRepsCups(Integer repsCups) {
        this.repsCups = repsCups;
    }

    public Integer getComplejidad() {
        return complejidad;
    }

    public void setComplejidad(Integer complejidad) {
        this.complejidad = complejidad;
    }

    public ServicioSalud getEspecialidad() {
        return especialidad;
    }

    public void setEspecialidad(ServicioSalud especialidad) {
        this.especialidad = especialidad;
    }

    public String getCodigoCliente() {
        return codigoCliente;
    }

    public void setCodigoCliente(String codigoCliente) {
        this.codigoCliente = codigoCliente;
    }

    public Boolean getEsPos() {
        return esPos;
    }

    public void setEsPos(Boolean esPos) {
        this.esPos = esPos;
    }

    public Procedimiento getProcedimiento() {
        return procedimiento;
    }

    public void setProcedimiento(Procedimiento procedimiento) {
        this.procedimiento = procedimiento;
    }

    public Integer getEstado() {
        return estado;
    }

    public void setEstado(Integer estado) {
        this.estado = estado;
    }

}
