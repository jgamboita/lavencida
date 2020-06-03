package com.conexia.contractual.model.contratacion.portafolio;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;
import com.conexia.contractual.model.contratacion.MacroServicio;
import com.conexia.contractual.model.contratacion.negociacion.SedeNegociacionPaquete;
import com.conexia.contractual.model.maestros.Procedimiento;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "paquete_portafolio", schema = "contratacion")
@NamedQueries({
        @NamedQuery(name = "PaquetePortafolio.findDtoPaquetesPuedenNegociarByPrestadorId", query = "select DISTINCT NEW com.conexia.contratacion.commons.dto.PaquetePortafolioDto(pp.id, pp.codigoSedePrestador, pp.descripcion) FROM Prestador p JOIN p.sedePrestador sp JOIN sp.portafolio por join por.portafolioPadre paqu JOIN paqu.paquetePortafolios pp "
                + "WHERE p.id = :prestadorId AND sp.enumStatus = 1 "),
        @NamedQuery(name = "PaquetePortafolio.findDtoPaquetesByPortafolioId", query = "select DISTINCT NEW com.conexia.contratacion.commons.dto.PaquetePortafolioDto(pp.id, pp.codigoPortafolio, pp.codigoSedePrestador, pp.descripcion, pp.valor, ms.nombre, ms.codigo, pp.tipoPaquete) "
                + "FROM PaquetePortafolio pp JOIN pp.macroservicio ms where pp.portafolio.id = :paqueteId"),
        @NamedQuery(name = "PaquetePortafolio.findByPortafolioId", query = "select pp from PaquetePortafolio pp "
                + "where pp.portafolio.id = :portafolioId"),
        @NamedQuery(
                name = "PaquetePortafolio.buscarTecnologiaOrigenPorPaquete",
                query = "select new com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto(p.id, p.codigo, p.codigoEmssanar, p.descripcion, p.estadoProcedimiento.id, p.nivelComplejidad) from PaquetePortafolio pp inner join pp.origen p where pp.portafolio.id = ?1"
        )
})
@NamedNativeQueries({
        @NamedNativeQuery(name = "PaquetePortafolio.eliminarPaquetePortafolio",
                query = "update contratacion.paquete_portafolio " +
                        " set deleted = true " +
                        " where id in (select paqpor.id " +
                        "			from contratacion.paquete_portafolio paqpor " +
                        "			where paqpor.portafolio_id = :paqueteId AND exists (SELECT DISTINCT pp.codigo   " +
                        "																FROM contratacion.paquete_portafolio pp    " +
                        "																JOIN contratacion.portafolio p on p.id = pp.portafolio_id  " +
                        "																where EXISTS (SELECT  NULL    " +
                        "																				FROM contratacion.portafolio por    " +
                        "																				WHERE por.id = p.portafolio_padre_id and eps_id is not null) and pp.codigo = paqpor.codigo))"),
        @NamedNativeQuery(name = "PaquetePortafolio.findPaquetesByNegociacion",
                query = "SELECT DISTINCT pp.portafolio_id as id, pp.codigo, pp.codigo_sede_prestador "
                        + " FROM contratacion.sede_negociacion_paquete snp "
                        + " JOIN contratacion.sedes_negociacion sn on snp.sede_negociacion_id = sn.id "
                        + " JOIN contratacion.paquete_portafolio pp on pp.portafolio_id = snp.paquete_id "
                        + " WHERE sn.negociacion_id = :negociacionId "),
        @NamedNativeQuery(name = "PaquetePortafolio.findServiciosOrigenPaquetePortafolioId",
                query = "SELECT DISTINCT ss.id, ss.codigo, ss.nombre FROM contratacion.paquete_portafolio_servicio_salud ppss "
                        + "JOIN contratacion.paquete_portafolio pp ON ppss.paquete_portafolio_id = pp.id "
                        + "JOIN contratacion.servicio_salud ss ON ppss.servicio_salud_id = ss.id and ss.enabled is true "
                        + "JOIN contratacion.grupo_servicio gp on gp.servicio_salud_id = ss.id "
                        + "JOIN contratacion.sede_prestador sp on sp.portafolio_id  = gp.portafolio_id "
                        + "JOIN contratacion.sedes_negociacion sn on sn.sede_prestador_id  = sp.id "
                        + "where pp.portafolio_id = :portafolioId AND sn.negociacion_id = :negociacionId "
                        + "ORDER BY 1", resultSetMapping = "PaquetePortafolio.serviciosOrigenMapping"),

})
@SqlResultSetMappings({
        @SqlResultSetMapping(name = "paquetesAgregarMapping",
                classes = @ConstructorResult(targetClass = PaquetePortafolioDto.class, columns = {
                        @ColumnResult(name = "id", type = Long.class), @ColumnResult(name = "codigo", type = String.class),
                        @ColumnResult(name = "codigo_ips", type = String.class),
                        @ColumnResult(name = "descripcion", type = String.class),
                        @ColumnResult(name = "complejidad", type = Integer.class),
                        @ColumnResult(name = "basico", type = Boolean.class),
                        @ColumnResult(name = "sede", type = String.class),
                        @ColumnResult(name = "fechaInsert", type = Date.class),
                        @ColumnResult(name = "estado", type =Boolean.class)
                })),
        @SqlResultSetMapping(name = "PaquetePortafolio.paquetesNegociacionMapping",
                classes = @ConstructorResult(targetClass = PaquetePortafolioDto.class, columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "codigo", type = String.class),
                        @ColumnResult(name = "codigo_sede_prestador", type = String.class)})),
        @SqlResultSetMapping(name = "PaquetePortafolio.serviciosOrigenMapping",
                classes = @ConstructorResult(targetClass = ServicioSaludDto.class, columns = {
                        @ColumnResult(name = "id", type = Long.class),
                        @ColumnResult(name = "codigo", type = String.class),
                        @ColumnResult(name = "nombre", type = String.class),
                })),
})
public class PaquetePortafolio implements Identifiable<Long>, Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portafolio_id", nullable = false)
    private Portafolio portafolio;

    @Column(name = "codigo")
    private String codigoPortafolio;

    @Column(name = "codigo_sede_prestador")
    private String codigoSedePrestador;

    @Column(name = "descripcion")
    private String descripcion;

    @Column(name = "valor")
    private Long valor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "macroservicio_id", nullable = false)
    private MacroServicio macroservicio;

    @Column(name = "estado")
    private String estado;

    @Column(name = "tipo_paquete")
    private String tipoPaquete;

    @Column(name = "es_eliminar_tecnologias")
    private Boolean esEliminarTecnologias;

    @Column(name = "es_modificar_cantidades")
    private Boolean esModificarCantidades;

    @Column(name = "es_adicionar_tecnologias")
    private Boolean esAdicionarTecnologias;

    @Column(name = "es_modificar_valor")
    private Boolean esModificarValor;

    @Column(name = "es_modificar_diagnosticos")
    private Boolean esModificarDiagnosticos;

    @OneToMany(mappedBy = "paquete", fetch = FetchType.LAZY)
    private Set<SedeNegociacionPaquete> sedesNegociacionPaquete;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origen_id")
    private Procedimiento origen;

    //<editor-fold desc="Getters && Setters">
    @Override
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Portafolio getPortafolio() {
        return portafolio;
    }

    public void setPortafolio(Portafolio portafolio) {
        this.portafolio = portafolio;
    }

    public String getCodigoPortafolio() {
        return codigoPortafolio;
    }

    public void setCodigoPortafolio(String codigoPortafolio) {
        this.codigoPortafolio = codigoPortafolio;
    }

    public String getCodigoSedePrestador() {
        return codigoSedePrestador;
    }

    public void setCodigoSedePrestador(String codigoSedePrestador) {
        this.codigoSedePrestador = codigoSedePrestador;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getValor() {
        return valor;
    }

    public void setValor(Long valor) {
        this.valor = valor;
    }

    public Boolean getEsEliminarTecnologias() {
        return esEliminarTecnologias;
    }

    public void setEsEliminarTecnologias(Boolean esEliminarTecnologias) {
        this.esEliminarTecnologias = esEliminarTecnologias;
    }

    public Boolean getEsModificarCantidades() {
        return esModificarCantidades;
    }

    public void setEsModificarCantidades(Boolean esModificarCantidades) {
        this.esModificarCantidades = esModificarCantidades;
    }

    public Boolean getEsAdicionarTecnologias() {
        return esAdicionarTecnologias;
    }

    public void setEsAdicionarTecnologias(Boolean esAdicionarTecnologias) {
        this.esAdicionarTecnologias = esAdicionarTecnologias;
    }

    public Boolean getEsModificarValor() {
        return esModificarValor;
    }

    public void setEsModificarValor(Boolean esModificarValor) {
        this.esModificarValor = esModificarValor;
    }

    public Boolean getEsModificarDiagnosticos() {
        return esModificarDiagnosticos;
    }

    public void setEsModificarDiagnosticos(Boolean esModificarDiagnosticos) {
        this.esModificarDiagnosticos = esModificarDiagnosticos;
    }

    public MacroServicio getMacroservicio() {
        return macroservicio;
    }

    public String getTipoPaquete() {
        return tipoPaquete;
    }

    public void setTipoPaquete(String tipoPaquete) {
        this.tipoPaquete = tipoPaquete;
    }

    public void setMacroservicio(MacroServicio macroservicio) {
        this.macroservicio = macroservicio;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public Set<SedeNegociacionPaquete> getSedesNegociacionPaquete() {
        return sedesNegociacionPaquete;
    }

    public void setSedesNegociacionPaquete(Set<SedeNegociacionPaquete> sedesNegociacionPaquete) {
        this.sedesNegociacionPaquete = sedesNegociacionPaquete;
    }

    public Procedimiento getOrigen() {
        return origen;
    }

    public void setOrigen(Procedimiento origen) {
        this.origen = origen;
    }
    //</editor-fold>
}
