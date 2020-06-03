package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contratacion.commons.dto.comparacion.*;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contractual.model.contratacion.converter.EstadoMaestroConverter;
import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion;
import com.conexia.contractual.model.contratacion.portafolio.Portafolio;
import com.conexia.contractual.model.maestros.Municipio;
import com.conexia.contractual.model.maestros.Regional;
import com.conexia.contractual.model.maestros.Zona;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "sede_prestador", schema = "contratacion")
@NamedQueries({
        @NamedQuery(name = "SedePrestador.obtenerSedes", query = "select distinct new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto(s.id,s.nombreSede,"
                + "s.codigoSede, s.codigoPrestador, m.id, m.descripcion, d.descripcion, sn.principal,sn.id, sc.id) "
                + " from SedesNegociacion sn "
                + " join sn.sedePrestador s "
                + " join s.municipio m "
                + " join m.departamento d "
                + " join sn.negociacion neg "
                + " join neg.solicitudesContratacion sc "
                + " where sn.negociacion.id = :negociacionId and "
                + " sc.id = :idSolicitudContratacion "),
        @NamedQuery(name = "SedePrestador.findByIdAndNegociacionId", query = "select new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto(s.id,s.nombreSede,"
                + "s.codigoSede, s.codigoPrestador, m.id, m.descripcion, d.descripcion, s.sedePrincipal,sn.id) "
                + " from SedesNegociacion sn join sn.sedePrestador s "
                + " join s.municipio m join m.departamento d "
                + " where s.id = :id AND sn.negociacion.id = :negociacionId"),
        @NamedQuery(name = "SedePrestador.contarSedes", query = "Select count(distinct s.id) from SedesNegociacion sn "
                + "join sn.sedePrestador s where sn.negociacion.id = :negociacionId"),
        @NamedQuery(name = "SedePrestador.findDtoSedesPuedenNegociarServiciosByPrestadorId", query = "select NEW com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto(sp.id, sp.codigoSede, sp.nombreSede, d.descripcion, m.descripcion, sp.zona.id, sp.direccion) "
                + "FROM Prestador p JOIN p.sedePrestador sp JOIN sp.portafolio por JOIN por.grupoServicios gs join gs.procedimientoPortafolios pp join pp.procedimiento ps join sp.municipio m join m.departamento d left join sp.sedesNegociacion sn "
                + "WHERE p.id = :prestadorId AND sp.enumStatus = 1 AND ps.id NOT IN (SELECT snp.procedimiento.id FROM SedesNegociacion sn JOIN sn.negociacion n join sn.sedeNegociacionServicios sns join sns.sedeNegociacionProcedimientos snp WHERE sp.id=sn.sedePrestador.id )"),
        @NamedQuery(name = "SedePrestador.findDtoSedesPuedenNegociarTrasladosByPrestadorId", query = "select NEW com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto(sp.id, sp.codigoSede,sp.nombreSede, d.descripcion, m.descripcion, sp.zona.id, sp.direccion) "
                + "FROM Prestador p JOIN p.sedePrestador sp JOIN sp.portafolio por JOIN por.transportesPortafolio tp join tp.transporte t join sp.municipio m join m.departamento d left join sp.sedesNegociacion sn "
                + "WHERE p.id = :prestadorId AND sp.enumStatus = 1 AND t.id NOT IN (SELECT snp.procedimiento.id FROM SedesNegociacion sn JOIN sn.negociacion n join sn.sedeNegociacionServicios sns join sns.sedeNegociacionProcedimientos snp WHERE sp.id=sn.sedePrestador.id )"),
        @NamedQuery(name = "SedePrestador.findDtoSedesPuedenNegociarMedicamentosByPrestadorId", query = "select NEW com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto(sp.id, sp.codigoSede ,sp.nombreSede, d.descripcion, mu.descripcion, sp.zona.id, sp.direccion) "
                + "FROM Prestador p JOIN p.sedePrestador sp JOIN sp.portafolio por JOIN por.medicamentoPortafolios mp join mp.medicamento m join sp.municipio mu join mu.departamento d "
                + "WHERE p.id = :prestadorId AND sp.enumStatus = 1 AND m.id NOT IN (SELECT snm.medicamento.id FROM SedesNegociacion sn join sn.negociacion n join sn.sedeNegociacionMedicamentos snm WHERE sp.id=sn.sedePrestador.id )"),
        @NamedQuery(name = "SedePrestador.obtenerSedeById", query = "select new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto(s.id,s.nombreSede,"
                + "s.codigoSede, s.codigoPrestador, s.sedePrincipal, sn.id, n.id) "
                + " from SedesNegociacion sn join sn.negociacion n join sn.sedePrestador s  "
                + " where sn.id = :sedeNegociacionId"),
        @NamedQuery(name = "SedePrestador.contarSedesById", query = "select count(distinct s.id) "
                + " from SedePrestador s " + " where s.codigoPrestador = :codigoPrestador "
                + " and s.codigoSede = :codigoSede"),
        @NamedQuery(name = "SedePrestador.updateCodigoSede", query = "update SedePrestador s set "
                + " s.codigoSede = :codigoSede "
                + " where s.id = :idSede"),
        @NamedQuery(name = "SedePrestador.findTecnologiasSedePrestadorByPrestadorId", query = "SELECT NEW com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto"
                + "(sp.id, sp.codigoSede,sp.codigoPrestador,sp.nombreSede, mun.id, mun.descripcion, dept.id, "
                + "dept.descripcion, sp.sedePrincipal, "
                + "(SELECT COUNT (gs.id) FROM GrupoServicio gs, SedePrestador sp2 WHERE sp2.portafolio.id = gs.portafolio.id AND sp2.id = sp.id),"
                + "(SELECT COUNT(mp.id) FROM MedicamentoPortafolio mp JOIN  mp.portafolio p where p.sedePrestador.id = sp.id),"
                + "(SELECT COUNT(tp.id) FROM Transporte pp join pp.procedimientoServicio ps join ps.transportePortafolios tp join tp.portafolio p join p.sedePrestador sp2 LEFT JOIN pp.categoriaTransporte c LEFT JOIN c.grupoTransporte g WHERE sp2.id = sp.id AND pp.categoriaTransporte IS NOT NULL),"
                + "(SELECT count(ppp.id) FROM ProcedimientoPropioPortafolio ppp JOIN ppp.portafolio p WHERE p.sedePrestador.id = sp.id),"
                + "(SELECT COUNT(ppp.id) FROM PaquetePortafolio ppp JOIN ppp.portafolio pp JOIN pp.portafolioPadre paq WHERE paq.id = (SELECT sp2.portafolio.id FROM SedePrestador sp2 WHERE sp2.id = sp.id)))"
                + " FROM SedePrestador sp JOIN sp.prestador pr LEFT JOIN sp.municipio mun LEFT JOIN mun.departamento dept "
                + "LEFT JOIN sp.regional reg LEFT JOIN sp.portafolio port "
                + "WHERE pr.id = :idPrestador and sp.enumStatus = :enumStatus ORDER BY sp.codigoPrestador, sp.sedePrincipal DESC"),
        @NamedQuery(name = "SedePrestador.findDtoServiciosPuedenNegociarByPrestadorIdAndModalidad", query = "select DISTINCT NEW com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto(sp.id, sp.codigoSede,sp.nombreSede, d.descripcion, m.descripcion, sp.zona.id, sp.direccion) "
                + "FROM OfertaSedePrestador mosp "
                + "JOIN mosp.sedePrestador sp "
                + "JOIN sp.municipio m "
                + "JOIN m.departamento d "
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
                + "WHERE mosp.sedePrestador.id = sn.sedePrestador.id )"),
        @NamedQuery(name = "SedePrestador.findDtoMedicamentoPuedenNegociarByPrestadorIdAndModalidad", query = "select DISTINCT NEW com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto(sp.id, sp.codigoSede ,sp.nombreSede, d.descripcion, mu.descripcion, sp.zona.id, sp.direccion) "
                + "FROM OfertaSedePrestador mosp "
                + "JOIN mosp.sedePrestador sp "
                + "JOIN sp.municipio mu "
                + "JOIN mu.departamento d "
                + "JOIN mosp.portafolio por "
                + "JOIN por.categoriasMedicamentosPortafolioSede cmp "
                + "JOIN cmp.medicamentoPortafolioSedes mps "
                + "JOIN mosp.ofertaPrestador op "
                + "JOIN mps.medicamento m "
                + "JOIN m.categoriaMedicamento cm "
                + "WHERE por.prestador.id = :prestadorId "
                + "AND op.ofertaPresentar = true  "
                + "AND mps.habilitado = true  "
                + "AND op.modalidad.id = :modalidad  "
                + "AND m.id NOT IN " + "( "
                + "SELECT snm.medicamento.id FROM SedesNegociacion sn "
                + "join sn.negociacion n "
                + "join sn.sedeNegociacionMedicamentos snm  "
                + "WHERE  mosp.sedePrestador.id=sn.sedePrestador.id)"),
        @NamedQuery(name = "SedePrestador.findByNegociacionId", query = "select new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto(s.id,s.nombreSede,"
                + "s.codigoSede, s.codigoPrestador, m.id, m.descripcion, d.descripcion, sn.id, s.portafolio.id, s.sedePrincipal) "
                + " from SedesNegociacion sn join sn.sedePrestador s "
                + " join s.municipio m join m.departamento d "
                + " where sn.negociacion.id = :negociacionId"),
        @NamedQuery(name = "SedePrestador.findByPortafolioPadre", query = "select new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto(s.id, pp.id)"
                + " from SedePrestador s join s.portafolio pp join pp.portafolioPadre ppadre"
                + " where ppadre.id IN(:portafoliosIds) "
                + " AND s.id IN(:sedesId)"),
        @NamedQuery(name = "SedePrestador.findSedesByNegociacionId", query = "select new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto(s.id,s.nombreSede,s.codigoSede, "
                + "s.codigoPrestador, m.id, m.descripcion, d.descripcion, sn.id, s.portafolio.id, s.sedePrincipal)  "
                + "from SedesNegociacion sn "
                + "join sn.sedePrestador s  "
                + "join s.municipio m "
                + "join m.departamento d  "
                + "where "
                + "sn.negociacion.id = :negociacionId")
})
@SqlResultSetMappings({
        @SqlResultSetMapping(name = "TablaComparacionTarifaMapping", classes = @ConstructorResult(targetClass = TablaComparacionTarifaDto.class, columns = {
                @ColumnResult(name = "prestadorId", type = Integer.class),
                @ColumnResult(name = "sedeId", type = Integer.class),
                @ColumnResult(name = "numeroDocumento", type = String.class),
                @ColumnResult(name = "nombrePrestador", type = String.class),
                @ColumnResult(name = "nombreSede", type = String.class),
                @ColumnResult(name = "codigoHabilitacionSede", type = String.class),
                @ColumnResult(name = "departamento", type = String.class),
                @ColumnResult(name = "municipio", type = String.class),
                @ColumnResult(name = "porcentajeCubrimiento", type = Integer.class)})),
        @SqlResultSetMapping(name = "ReporteComparacionTarifasMapping", classes = @ConstructorResult(targetClass = ReporteComparacionTarifasDto.class, columns = {
                @ColumnResult(name = "prestadorId", type = Integer.class),
                @ColumnResult(name = "portafolioId", type = Integer.class),
                @ColumnResult(name = "sedePrestadorId", type = Integer.class),
                @ColumnResult(name = "nombreSede", type = String.class),
                @ColumnResult(name = "codigoServicio", type = String.class),
                @ColumnResult(name = "nombreServicio", type = String.class),
                @ColumnResult(name = "codigoEmssanar", type = String.class),
                @ColumnResult(name = "nombreEmssanar", type = String.class),
                @ColumnResult(name = "desSeccionCUPS", type = String.class),
                @ColumnResult(name = "desCapituloCUPS", type = String.class),
                @ColumnResult(name = "nivelTecnologia", type = String.class),
                @ColumnResult(name = "categoriaPos", type = String.class),
                @ColumnResult(name = "tarifaPropuesta", type = Integer.class),
                @ColumnResult(name = "tarifaAnterior", type = Integer.class),
                @ColumnResult(name = "soatVigente", type = Integer.class)})),
        @SqlResultSetMapping(name = "ReporteComparacionTarifasTrasladosMapping", classes = @ConstructorResult(targetClass = ReporteComparacionTrasladosDto.class, columns = {
                @ColumnResult(name = "prestadorId", type = Integer.class),
                @ColumnResult(name = "portafolioId", type = Integer.class),
                @ColumnResult(name = "sedePrestadorId", type = Integer.class),
                @ColumnResult(name = "nombreSede", type = String.class),
                @ColumnResult(name = "grupoTransporte", type = String.class),
                @ColumnResult(name = "nombreServicio", type = String.class),
                @ColumnResult(name = "codigoEmssanar", type = String.class),
                @ColumnResult(name = "nombreEmssanar", type = String.class),
                @ColumnResult(name = "desSeccionCUPS", type = String.class),
                @ColumnResult(name = "desCapituloCUPS", type = String.class),
                @ColumnResult(name = "nivelTecnologia", type = String.class),
                @ColumnResult(name = "categoriaPos", type = String.class),
                @ColumnResult(name = "tarifaPropuesta", type = Integer.class),
                @ColumnResult(name = "tarifaAnterior", type = Integer.class),
                @ColumnResult(name = "soatVigente", type = Integer.class)})),
        @SqlResultSetMapping(name = "ReporteComparacionTarifasSedeMapping", classes = @ConstructorResult(targetClass = ReporteComparacionTarifasDto.class, columns = {
                @ColumnResult(name = "sedePrestadorId", type = Integer.class),
                @ColumnResult(name = "codigoServicio", type = String.class),
                @ColumnResult(name = "codigoEmssanar", type = String.class),
                @ColumnResult(name = "valorSedeComp", type = Integer.class)})),
        @SqlResultSetMapping(name = "ReporteComparacionTrasladosSedeMapping", classes = @ConstructorResult(targetClass = ReporteComparacionTrasladosDto.class, columns = {
                @ColumnResult(name = "sedePrestadorId", type = Integer.class),
                @ColumnResult(name = "codigoServicio", type = String.class),
                @ColumnResult(name = "codigoEmssanar", type = String.class),
                @ColumnResult(name = "valorSedeComp", type = Integer.class)})),
        @SqlResultSetMapping(name = "ReporteComparacionTarifasMedicamentosMapping", classes = @ConstructorResult(targetClass = ReporteComparacionMedicamentosDto.class, columns = {
                @ColumnResult(name = "prestadorId", type = Integer.class),
                @ColumnResult(name = "numeroDocumento", type = String.class),
                @ColumnResult(name = "codGrupo", type = String.class),
                @ColumnResult(name = "descGrupo", type = String.class),
                @ColumnResult(name = "cum", type = String.class), @ColumnResult(name = "atc", type = String.class),
                @ColumnResult(name = "descAtc", type = String.class),
                @ColumnResult(name = "nombreProducto", type = String.class),
                @ColumnResult(name = "principioActivo", type = String.class),
                @ColumnResult(name = "formaFarmaceutica", type = String.class),
                @ColumnResult(name = "concentracion", type = String.class),
                @ColumnResult(name = "presentacion", type = String.class),
                @ColumnResult(name = "titularRegistro", type = String.class),
                @ColumnResult(name = "categoriaPos", type = String.class),
                @ColumnResult(name = "regulado", type = Boolean.class),
                @ColumnResult(name = "tarifaPropuesta", type = Double.class),
                @ColumnResult(name = "tarifaAnterior", type = Double.class),
                @ColumnResult(name = "tarifaReferente", type = Double.class),
                @ColumnResult(name = "medicamentoId", type = Integer.class)})),
        @SqlResultSetMapping(name = "ReporteComparacionTarifasMedicamentosSedeMapping", classes = @ConstructorResult(targetClass = ReporteComparacionMedicamentosDto.class, columns = {
                @ColumnResult(name = "prestadorIdRef", type = Integer.class),
                @ColumnResult(name = "medicamentoId", type = Integer.class),
                @ColumnResult(name = "valorSedeComp", type = Double.class),
                @ColumnResult(name = "codGrupo", type = String.class)})),
        @SqlResultSetMapping(name = "ReporteComparacionTarifasPaquetesMapping", classes = @ConstructorResult(targetClass = ReporteComparacionPaquetesDto.class, columns = {
                @ColumnResult(name = "sedeId", type = Integer.class),
                @ColumnResult(name = "nombre", type = String.class),
                @ColumnResult(name = "grupoHabilitacion", type = String.class),
                @ColumnResult(name = "codigoPaqueteEmssanar", type = String.class),
                @ColumnResult(name = "descPaqueteIps", type = String.class),
                @ColumnResult(name = "codigoPaqueteIps", type = String.class),
                @ColumnResult(name = "descPaqueteEmssanar", type = String.class),
                @ColumnResult(name = "categoriaPos", type = String.class),
                @ColumnResult(name = "tarifaPropuesta", type = Integer.class),
                @ColumnResult(name = "tarifaAnterior", type = Integer.class),
                @ColumnResult(name = "tarifaEmssanar", type = Integer.class)})),
        @SqlResultSetMapping(name = "ReporteComparacionTarifasPaquetesExcelMapping", classes = @ConstructorResult(targetClass = ReporteComparacionPaquetesDto.class, columns = {
                @ColumnResult(name = "sedeId", type = Integer.class),
                @ColumnResult(name = "nombreSede", type = String.class),
                @ColumnResult(name = "grupoHabilitacion", type = String.class),
                @ColumnResult(name = "codigoPaqueteRef", type = String.class),
                @ColumnResult(name = "codigoPaqueteComp", type = String.class),
                @ColumnResult(name = "valorPrestComp", type = Integer.class)})),
        @SqlResultSetMapping(name = "ReporteComparacionTarifasPaquetesExcelDetalleMapping", classes = @ConstructorResult(targetClass = ReportePaquetesPrestadorDto.class, columns = {
                @ColumnResult(name = "numeroDocumento", type = String.class),
                @ColumnResult(name = "nombrePrestador", type = String.class),
                @ColumnResult(name = "grupoHabilitacion", type = String.class),
                @ColumnResult(name = "codigoPaqueteEmssanar", type = String.class),
                @ColumnResult(name = "descPaqueteEmssanar", type = String.class),
                @ColumnResult(name = "codigoPaqueteIps", type = String.class),
                @ColumnResult(name = "descPaqueteIps", type = String.class),
                @ColumnResult(name = "tipoTecnologia", type = String.class),
                @ColumnResult(name = "codigoServicioSalud", type = String.class),
                @ColumnResult(name = "descServicioSalud", type = String.class),
                @ColumnResult(name = "codigoEmssanar", type = String.class),
                @ColumnResult(name = "descEmssanar", type = String.class),
                @ColumnResult(name = "cantidad", type = Integer.class),
                @ColumnResult(name = "observaciones", type = String.class)})),
        @SqlResultSetMapping(name = "SedesPuedenNegociarServicios", classes = @ConstructorResult(targetClass = SedePrestadorDto.class, columns = {
                @ColumnResult(name = "sede_id", type = Long.class),
                @ColumnResult(name = "codigo", type = String.class),
                @ColumnResult(name = "nombre", type = String.class),
                @ColumnResult(name = "departamento", type = String.class),
                @ColumnResult(name = "municipio", type = String.class),
                @ColumnResult(name = "zona_id", type = Integer.class),
                @ColumnResult(name = "direccion", type = String.class)})),
        @SqlResultSetMapping(name = "ReporteComparacionNegociacionMapping", classes = @ConstructorResult(targetClass = ReporteComparacionNegociacionDto.class, columns = {
                @ColumnResult(name = "prestadorId", type = Integer.class),
                @ColumnResult(name = "portafolioId", type = Integer.class),
                @ColumnResult(name = "sedePrestadorId", type = Integer.class),
                @ColumnResult(name = "nombreSede", type = String.class),
                @ColumnResult(name = "capitulo", type = String.class),
                @ColumnResult(name = "categoria", type = String.class),
                @ColumnResult(name = "codigoEmssanar", type = String.class),
                @ColumnResult(name = "nombreEmssanar", type = String.class),
                @ColumnResult(name = "desSeccionCUPS", type = String.class),
                @ColumnResult(name = "desCapituloCUPS", type = String.class),
                @ColumnResult(name = "nivelTecnologia", type = String.class),
                @ColumnResult(name = "categoriaPos", type = String.class),
                @ColumnResult(name = "modalidad", type = String.class)
        })),
        @SqlResultSetMapping(name = "ReporteComparacionNegociacionSedeMapping", classes = @ConstructorResult(targetClass = ReporteComparacionNegociacionDto.class, columns = {
                @ColumnResult(name = "sedePrestadorId", type = Integer.class),
                @ColumnResult(name = "capitulo", type = String.class),
                @ColumnResult(name = "categoria", type = String.class),
                @ColumnResult(name = "codigoEmssanar", type = String.class),
                @ColumnResult(name = "modalidadSedeComp", type = String.class)
        })),
        @SqlResultSetMapping(name = "ReporteComparacionNegociacionMedicamentosMapping", classes = @ConstructorResult(targetClass = ReporteComparacionMedicamentosDto.class, columns = {
                @ColumnResult(name = "prestadorId", type = Integer.class),
                @ColumnResult(name = "numeroDocumento", type = String.class),
                @ColumnResult(name = "codGrupo", type = String.class),
                @ColumnResult(name = "descGrupo", type = String.class),
                @ColumnResult(name = "cum", type = String.class), @ColumnResult(name = "atc", type = String.class),
                @ColumnResult(name = "descAtc", type = String.class),
                @ColumnResult(name = "nombreProducto", type = String.class),
                @ColumnResult(name = "principioActivo", type = String.class),
                @ColumnResult(name = "formaFarmaceutica", type = String.class),
                @ColumnResult(name = "concentracion", type = String.class),
                @ColumnResult(name = "presentacion", type = String.class),
                @ColumnResult(name = "titularRegistro", type = String.class),
                @ColumnResult(name = "categoriaPos", type = String.class),
                @ColumnResult(name = "regulado", type = Boolean.class),
                @ColumnResult(name = "modalidad", type = String.class),
                @ColumnResult(name = "medicamentoId", type = Integer.class)})),
        @SqlResultSetMapping(name = "ReporteComparacionNegociacionMedicamentosSedeMapping", classes = @ConstructorResult(targetClass = ReporteComparacionMedicamentosDto.class, columns = {
                @ColumnResult(name = "prestadorIdRef", type = Integer.class),
                @ColumnResult(name = "medicamentoId", type = Integer.class),
                @ColumnResult(name = "modalidad", type = String.class),
                @ColumnResult(name = "codGrupo", type = String.class)
        })),

})
public class SedePrestador implements Identifiable<Long>, Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true, nullable = false)
    private Long id;

    @Column(name = "codigo_prestador", length = 50)
    private String codigoPrestador;

    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "prestador_id")
    private Prestador prestador;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portafolio_id")
    private Portafolio portafolio;

    @Column(name = "nombre_sede", length = 100)
    private String nombreSede;

    @Column(name = "sede_principal")
    private Boolean sedePrincipal;

    @Column(name = "codigo_sede", length = 60)
    private String codigoSede;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "municipio_id")
    private Municipio municipio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_id")
    private Zona zona;

    @Column(name = "barrio", length = 50)
    private String barrio;

    @Column(name = "direccion", length = 100)
    private String direccion;

    @Column(name = "fax", length = 10)
    private String fax;

    @Column(name = "telefono_citas", length = 50)
    private String telefonoCitas;

    @Column(name = "correo", length = 150)
    private String correo;

    @Column(name = "gerente", length = 150)
    private String gerente;

    @Column(name = "telefono_administrativo", length = 50)
    private String telefonoAdministrativo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "regional_id")
    private Regional regional;

    @OneToMany(mappedBy = "sedePrestador", fetch = FetchType.LAZY)
    private Set<CapacidadSede> capacidadSede;

    @Column(name = "estado")
    @Convert(converter = EstadoMaestroConverter.class)
    private EstadoMaestroEnum estado;

    @Column(name = "enum_status")
    private Integer enumStatus;

    @Column(name = "numero_identificacion")
    private String numeroIdentificacion;

    @OneToOne(fetch = FetchType.LAZY, cascade = {CascadeType.ALL})
    @JoinColumn(name = "horario_atencion_id")
    private HorarioAtencion horarioAtencion;

    @OneToMany(mappedBy = "sedePrestador", fetch = FetchType.LAZY)
    private List<SedesNegociacion> sedesNegociacion;

    @Column(name = "codigo_habilitacion")
    private String codigoHabilitacion;

    public SedePrestador() {
    }

    public SedePrestador(Long id) {
        this.id = id;
    }

    // <editor-fold defaultstate="collapsed" desc="Getters & Setters">
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCodigoPrestador() {
        return this.codigoPrestador;
    }

    public void setCodigoPrestador(String codigoPrestador) {
        this.codigoPrestador = codigoPrestador;
    }

    public String getDireccion() {
        return this.direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNombreSede() {
        return this.nombreSede;
    }

    public void setNombreSede(String nombreSede) {
        this.nombreSede = nombreSede;
    }

    public Boolean getSedePrincipal() {
        return this.sedePrincipal;
    }

    public void setSedePrincipal(Boolean sedePrincipal) {
        this.sedePrincipal = sedePrincipal;
    }

    public String getTelefonoAdministrativo() {
        return this.telefonoAdministrativo;
    }

    public void setTelefonoAdministrativo(String telefonoAdministrativo) {
        this.telefonoAdministrativo = telefonoAdministrativo;
    }

    public String getTelefonoCitas() {
        return this.telefonoCitas;
    }

    public void setTelefonoCitas(String telefonoCitas) {
        this.telefonoCitas = telefonoCitas;
    }

    public Set<CapacidadSede> getCapacidadSede() {
        return capacidadSede;
    }

    public void setCapacidadSede(Set<CapacidadSede> capacidadSede) {
        this.capacidadSede = capacidadSede;
    }

    public Portafolio getPortafolio() {
        return this.portafolio;
    }

    public void setPortafolio(Portafolio portafolio) {
        this.portafolio = portafolio;
    }

    public Prestador getPrestador() {
        return prestador;
    }

    public void setPrestador(Prestador prestador) {
        this.prestador = prestador;
    }

    public Municipio getMunicipio() {
        return municipio;
    }

    public void setMunicipio(Municipio municipio) {
        this.municipio = municipio;
    }

    public Regional getRegional() {
        return regional;
    }

    public void setRegional(Regional regional) {
        this.regional = regional;
    }

    public String getCodigoSede() {
        return codigoSede;
    }

    public void setCodigoSede(String codigoSede) {
        this.codigoSede = codigoSede;
    }

    public Zona getZona() {
        return zona;
    }

    public void setZona(Zona zona) {
        this.zona = zona;
    }

    public EstadoMaestroEnum getEstado() {
        return estado;
    }

    public void setEstado(EstadoMaestroEnum estado) {
        this.estado = estado;
    }

    public Integer getEnumStatus() {
        return enumStatus;
    }

    public void setEnumStatus(Integer enumStatus) {
        this.enumStatus = enumStatus;
    }

    /**
     * @return the numeroIdentificacion
     */
    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    /**
     * @param numeroIdentificacion the numeroIdentificacion to set
     */
    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public HorarioAtencion getHorarioAtencion() {
        return horarioAtencion;
    }

    public void setHorarioAtencion(HorarioAtencion horarioAtencion) {
        this.horarioAtencion = horarioAtencion;
    }

    public String getBarrio() {
        return barrio;
    }

    public void setBarrio(String barrio) {
        this.barrio = barrio;
    }

    public String getFax() {
        return fax;
    }

    public void setFax(String fax) {
        this.fax = fax;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public String getGerente() {
        return gerente;
    }

    public void setGerente(String gerente) {
        this.gerente = gerente;
    }

    public List<SedesNegociacion> getSedesNegociacion() {
        return sedesNegociacion;
    }

    public void setSedesNegociacion(List<SedesNegociacion> sedesNegociacion) {
        this.sedesNegociacion = sedesNegociacion;
    }

    public String getCodigoHabilitacion() {
        return codigoHabilitacion;
    }

    public void setCodigoHabilitacion(String codigoHabilitacion) {
        this.codigoHabilitacion = codigoHabilitacion;
    }

    // </editor-fold>

}
