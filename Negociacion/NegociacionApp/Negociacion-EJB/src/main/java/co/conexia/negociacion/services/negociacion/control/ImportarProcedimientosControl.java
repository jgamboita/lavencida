package co.conexia.negociacion.services.negociacion.control;

import com.conexia.contratacion.commons.constants.enums.OpcionesImportacionTecnologiaEnum;
import com.conexia.contractual.model.contratacion.ServicioSalud;
import com.conexia.contractual.model.contratacion.ServicioSalud_;
import com.conexia.contractual.model.contratacion.Tarifario;
import com.conexia.contractual.model.contratacion.negociacion.*;
import com.conexia.contractual.model.maestros.Procedimientos;
import com.conexia.contractual.model.maestros.Procedimientos_;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionServicioDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.ArchivoTecnologiasNegociacionEventoDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ServiciosHabilitadosRespNoRepsDto;
import com.conexia.jdbcutils.database.BatchJdbcUtil;
import com.conexia.logfactory.Log;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Control para las acciones de importar procedimientos a la negociacion
 *
 * @author emedina
 */
public class ImportarProcedimientosControl {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;
    @Inject
    private Log log;
    @Inject
    private BatchJdbcUtil batchJdbUtil;

    public List<ServiciosHabilitadosRespNoRepsDto> consultaServicioRepsHabilitados(NegociacionDto negociacion, List<String> codigoServicio) {

        List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadosPorNegociacion = new ArrayList<>();
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("negociacion", negociacion.getId());
        parameters.put("codigoServicio", new ArrayList<>(codigoServicio));

        String sql = "SELECT sn.id                                                                             AS sede_prestador_id, " +
                "       ss.id                                                                             AS servicio_id, " +
                "       ss.codigo                                                                         AS servicio_codigo, " +
                "       CASE " +
                "           WHEN sr.id IS NOT NULL THEN " +
                "               CASE " +
                "                   WHEN sr.complejidad_alta = 'SI' THEN 3 " +
                "                   WHEN sr.complejidad_media = 'SI' THEN 2 " +
                "                   WHEN sr.complejidad_baja = 'SI' THEN 1 END " +
                "           WHEN snr.id IS NOT NULL THEN snr.nivel_complejidad END                        AS complejidad, " +
                "       CASE WHEN sr.id IS NOT NULL THEN sr.ind_habilitado ELSE snr.estado_servicio END   AS estado_servicio, " +
                "       sp.codigo_habilitacion                                                            AS codigo_habilitacion, " +
                "       sp.codigo_sede                                                                    AS codigo_sede, " +
                "       least(CASE " +
                "                 WHEN n.complejidad = 'ALTA' THEN 3 " +
                "                 WHEN n.complejidad = 'MEDIA' THEN 2 " +
                "                 ELSE 1 END, CASE " +
                "                                 WHEN sr.id IS NOT NULL THEN " +
                "                                     CASE " +
                "                                         WHEN sr.complejidad_alta = 'SI' THEN 3 " +
                "                                         WHEN sr.complejidad_media = 'SI' THEN 2 " +
                "                                         WHEN sr.complejidad_baja = 'SI' THEN 1 END " +
                "                                 WHEN snr.id IS NOT NULL THEN snr.nivel_complejidad END) AS complejidad_negociacion " +
                "FROM contratacion.servicio_salud ss " +
                "         JOIN contratacion.negociacion n ON n.id = :negociacion " +
                "         JOIN contratacion.sedes_negociacion sn ON n.id = sn.negociacion_id " +
                "         JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id " +
                "         LEFT JOIN maestros.servicios_reps sr ON sr.codigo_habilitacion = sp.codigo_habilitacion AND " +
                "                                                 sr.numero_sede = cast(sp.codigo_sede AS int) AND sr.ind_habilitado AND " +
                "                                                 sr.servicio_codigo = cast(ss.codigo AS int) " +
                "         LEFT JOIN maestros.servicios_no_reps snr " +
                "                   ON snr.sede_prestador_id = sp.id AND snr.servicio_id = ss.id AND snr.estado_servicio " +
                "WHERE 1 = CASE " +
                "              WHEN sr.id IS NOT NULL THEN 1 " +
                "              WHEN snr.id IS NOT NULL THEN 1 END " +
                "  AND ss.codigo IN (:codigoServicio)";
        Query consulta = em.createNativeQuery(sql);

        parameters.forEach(consulta::setParameter);
        List<Object[]> serviciosRepsHabilitados = consulta.getResultList();

        serviciosRepsHabilitados.stream().map((serviciosReps) -> {
            ServiciosHabilitadosRespNoRepsDto serviciosHabilitadosDto = new ServiciosHabilitadosRespNoRepsDto();
            serviciosHabilitadosDto.setSedeNegociacionId(new Long((Integer) serviciosReps[0]));
            serviciosHabilitadosDto.setServicioId(new Long((Integer) serviciosReps[1]));
            serviciosHabilitadosDto.setCodigoServicio((String) serviciosReps[2]);
            serviciosHabilitadosDto.setNivelComplejidad((Integer) serviciosReps[3]);
            serviciosHabilitadosDto.setEstadoServicio((Boolean)serviciosReps[4]);
            serviciosHabilitadosDto.setCodigohabilitacion((String) serviciosReps[5]);
            serviciosHabilitadosDto.setCodigoSede((String) serviciosReps[6]);
            serviciosHabilitadosDto.setNivelComplejidadMinimo((Integer) serviciosReps[7]);
            return serviciosHabilitadosDto;
        }).forEachOrdered(serviciosHabilitadosPorNegociacion::add);
        return serviciosHabilitadosPorNegociacion;
    }

    public List<ProcedimientoDto> consultarProcedimientosRias(List<String> codigoEmsannar) {
        return em.createNamedQuery("Procedimiento.getByCode", ProcedimientoDto.class)
                .setParameter("codigoEmssanar", codigoEmsannar.stream()
                        .collect(Collectors.toList()))
                .getResultList();
    }

    public List<ProcedimientoDto> consultarProcedimientoServicio(List<String> codigoEmssanar,List<String> codigoServicio) {
            return em.createNamedQuery("Procedimientos.consultaProcedimiientoServicio", ProcedimientoDto.class)
                    .setParameter("codigoEmssanar", new ArrayList<>(codigoEmssanar))
                    .setParameter("codigoServicio", new ArrayList<>(codigoServicio))
                    .getResultList();
    }

    public List<Tarifario> consultarTarifarioId(List<String> descripcionTarifario) {
        return em.createNamedQuery("Tarifario.buscarPorDescripcion", Tarifario.class)
                .setParameter(1, descripcionTarifario)
                .getResultList();
    }

    public BigDecimal calcularValorProcedimiento(String cups, Integer tarifarioId, Double porcentajeNegociacion) {
        return (BigDecimal) em.createNativeQuery("select coalesce(( SELECT contratacion.ajuste_porcentaje(contratacion.total_valores(:cups, :tarifarioId), CAST(:porcentajeNegociacion AS numeric)) AS valorCalculado),0)")
                .setParameter("cups", cups)
                .setParameter("tarifarioId", tarifarioId)
                .setParameter("porcentajeNegociacion", porcentajeNegociacion)
                .getSingleResult();
    }

    public List<SedeNegociacionProcedimiento> consultarProcedimientosPorNegociacion(List<Long> sedeNegociacionId, List<ArchivoTecnologiasNegociacionEventoDto> filas){
        CriteriaBuilder criteriaBuilder = this.em.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
        Root<SedeNegociacionProcedimiento> procedimiento = criteriaQuery.from(SedeNegociacionProcedimiento.class);
        Join<SedeNegociacionProcedimiento, Procedimientos> procedimientoServicio = procedimiento.join(SedeNegociacionProcedimiento_.procedimiento, JoinType.INNER);
        Join<SedeNegociacionProcedimiento, SedeNegociacionServicio> servicio = procedimiento.join(SedeNegociacionProcedimiento_.sedeNegociacionServicio, JoinType.INNER);
        Join<SedeNegociacionServicio, SedesNegociacion> sede = servicio.join(SedeNegociacionServicio_.sedeNegociacion, JoinType.INNER);
        Join<SedeNegociacionServicio, ServicioSalud> servicioSalud = servicio.join(SedeNegociacionServicio_.servicioSalud, JoinType.INNER);

        criteriaQuery.multiselect(
                procedimiento.get(SedeNegociacionProcedimiento_.ID).alias("negociacionProcedimientoId"),
                procedimientoServicio.get(Procedimientos_.ID).alias("procedimientoId"),
                procedimientoServicio.get(Procedimientos_.CODIGO_CLIENTE).alias("codigoEmsanar"),
                servicioSalud.get(ServicioSalud_.ID).alias("servicioSaludId"),
                servicioSalud.get(ServicioSalud_.CODIGO).alias("servicioSaludCodigo")
        ).where(
                getPredicateSedesNegociacion(sedeNegociacionId, sede),
                getPredicateProcedimientos(filas, criteriaBuilder, procedimientoServicio),
                getPredicateServicios(filas, criteriaBuilder, servicioSalud)
        );
        List<Tuple> resultadoTuple = em.createQuery(criteriaQuery).getResultList();

        return convertirProcedimiento(resultadoTuple);
    }

    private Predicate getPredicateServicios(List<ArchivoTecnologiasNegociacionEventoDto> filas, CriteriaBuilder criteriaBuilder, Join<SedeNegociacionServicio, ServicioSalud> servicioSalud) {
        return criteriaBuilder.and(
                servicioSalud.get(ServicioSalud_.ID)
                        .in(filas.stream().map(ArchivoTecnologiasNegociacionEventoDto::getServicioId)
                                .collect(Collectors.toSet()))
        );
    }

    private Predicate getPredicateProcedimientos(List<ArchivoTecnologiasNegociacionEventoDto> filas, CriteriaBuilder criteriaBuilder, Join<SedeNegociacionProcedimiento, Procedimientos> procedimientoServicio) {
        return criteriaBuilder.and(
                procedimientoServicio.get(Procedimientos_.ID)
                        .in(filas.stream()
                                .map(ArchivoTecnologiasNegociacionEventoDto::getProcedimientoServicioId)
                                .collect(Collectors.toSet()))
        );
    }

    private Predicate getPredicateSedesNegociacion(List<Long> sedeNegociacionId, Join<SedeNegociacionServicio, SedesNegociacion> sede) {
        return sede.get(SedesNegociacion_.ID).in(sedeNegociacionId);
    }

    private List<SedeNegociacionProcedimiento> convertirProcedimiento(List<Tuple> resultadoTuple) {
        return resultadoTuple.stream().map(tuple -> {
            SedeNegociacionProcedimiento negociacionProcedimiento = new SedeNegociacionProcedimiento();
            negociacionProcedimiento.setId(tuple.get("negociacionProcedimientoId", Long.class));

            Procedimientos procedimiento = new Procedimientos();
            procedimiento.setId(tuple.get("procedimientoId", Long.class));
            procedimiento.setCodigoCliente(tuple.get("codigoEmsanar", String.class));

            ServicioSalud servicioSalud = new ServicioSalud();
            servicioSalud.setId(tuple.get("servicioSaludId", Long.class));
            servicioSalud.setCodigo(tuple.get("servicioSaludCodigo", String.class));
            procedimiento.setServicioSalud(servicioSalud);

            negociacionProcedimiento.setProcedimiento(procedimiento);
            return negociacionProcedimiento;
        }).collect(Collectors.toList());
    }

    public void insertarSedeNegociacionServicioImportar(List<Long> sedeNegociacionIds, List<Long> servicios, NegociacionDto negociacion, Integer userId) {
        if (servicios.isEmpty()) {
            return;
        }
        StringBuilder query = new StringBuilder();
        List<Map<String, Object>> parametersMap = new ArrayList<>();
        query.append("insert into contratacion.sede_negociacion_servicio (sede_negociacion_id,servicio_id,tarifa_diferencial,user_id) ");
        query.append(" values(:sede_negociacion_id,:servicio_id,:tarifa_diferencial,:user_id) ");
        //se agrupan servicios archivos por codigoservicio
        servicios.forEach((id) -> sedeNegociacionIds.stream()
                .map((sedes) -> {
                    Map<String, Object> parameters = new HashMap<>();
                    parameters.put("sede_negociacion_id", sedes);
                    return parameters;
                })
                .peek((parameters) -> parameters.put("servicio_id", id))
                .peek((parameters) -> parameters.put("user_id", userId))
                .peek((parameters) -> parameters.put("tarifa_diferencial", false))
                .forEachOrdered(parametersMap::add));

        try {
            batchJdbUtil.executeIntoBatch(em, 1000, query.toString(), parametersMap);
        } catch (Exception e) {
            log.error("error : insertarSedeNegociacionServicioImportar", e);
        }
    }

    public void insertarProcedimientosImportEvento(List<Long> sedeNegociacionIds, List<ArchivoTecnologiasNegociacionEventoDto> procedimientos, Integer userId) {
        if (procedimientos.isEmpty()) {
            return;
        }
        List<Map<String, Object>> parametrosInsercionProcedimientos = procedimientos.stream()
                .map((pto) -> {
                    Map<String, Object> parameter = new HashMap<>();
                    parameter.put("sedeNegociacionIds", sedeNegociacionIds);
                    parameter.put("procedimiento_servidio_id", pto.getProcedimientoServicioId());
                    parameter.put("tarifa_diferencial", false);
                    parameter.put("valorNegociado", (new BigDecimal(pto.getValorNegociado())));
                    parameter.put("user_id", userId);
                    parameter.put("tarifario_negociado_id", pto.getTarifarioNegociacionId());
                    parameter.put("porcentaje_negociado", (new BigDecimal(pto.getPorcentajeNegociado())));
                    parameter.put("negociado", true);
                    return parameter;
                }).collect(Collectors.toList());
        try {
            batchJdbUtil.executeIntoBatch(em, 1000, obtenerInsercionSedeNegociacionProcedimiento(), parametrosInsercionProcedimientos);
        } catch (Exception e) {
            log.error("error : insertarProcedimientosImportEvento", e);
        }
    }

    private String obtenerInsercionSedeNegociacionProcedimiento() {
        return "INSERT INTO contratacion.sede_negociacion_procedimiento (sede_negociacion_servicio_id, procedimiento_id, " +
                "                                                         tarifa_diferencial, valor_negociado, user_id, " +
                "                                                         tarifario_negociado_id, porcentaje_negociado, negociado) " +
                "SELECT sns.id, " +
                "       ps.id, " +
                "       :tarifa_diferencial, " +
                "       :valorNegociado, " +
                "       :user_id, " +
                "       :tarifario_negociado_id, " +
                "       :porcentaje_negociado, " +
                "       :negociado " +
                "FROM contratacion.sede_negociacion_servicio sns " +
                "         INNER JOIN contratacion.servicio_salud ss ON sns.servicio_id = ss.id " +
                "         INNER JOIN maestros.procedimiento_servicio ps ON ss.id = ps.servicio_id " +
                "WHERE sns.sede_negociacion_id IN (:sedeNegociacionIds) " +
                "  AND ps.id = :procedimiento_servidio_id";
    }

    public void actualizarProcedimientoEventoImportar(List<Long> sedeNegociacionIds, List<ArchivoTecnologiasNegociacionEventoDto> procedimientos, NegociacionDto negociacion, Integer userId) {
        if (procedimientos.isEmpty()) {
            return;
        }
        List<Map<String, Object>> parametersMap = new ArrayList<>();
        StringBuilder query = new StringBuilder();
        query.append("update contratacion.sede_negociacion_procedimiento snp  set ")
                .append("valor_negociado = :valorNegociado, negociado = true, ")
                .append("tarifario_negociado_id = (select id from contratacion.tarifarios where descripcion = :tarifario),")
                .append("porcentaje_negociado = :porcentajeNegociado,")
                .append("  user_id = :userId ")
                .append("  from (")
                .append("  select distinct snp.id from contratacion.sede_negociacion_procedimiento snp ")
                .append("  join contratacion.sede_negociacion_servicio sns on snp.sede_negociacion_servicio_id = sns.id ")
                .append("  join contratacion.sedes_negociacion sn on sns.sede_negociacion_id = sn.id ")
                .append("  join contratacion.sede_prestador sp on sn.sede_prestador_id = sp.id ")
                .append("  join contratacion.servicio_salud ss on sns.servicio_id = ss.id ")
                .append("  join maestros.procedimiento_servicio ps on snp.procedimiento_id = ps.id ")
                .append("  where sn.id in (:sedeNegociacionIds) ")
                .append("  and ps.codigo_cliente = :codigoCliente  and ss.codigo = :servicioCodigo ");
        if (negociacion.getOpcionImportarSeleccionada().equals(OpcionesImportacionTecnologiaEnum.IMPORTAR_SEDE_A_SEDE)) {
            query.append(" and concat(coalesce(sp.codigo_habilitacion,sp.codigo_prestador),sp.codigo_sede) = :codigoHabSede");
        }
        query.append("  ) as procedimientos ").append("  where snp.id = procedimientos.id ");

        for (ArchivoTecnologiasNegociacionEventoDto pto : procedimientos) {
            Map<String, Object> parameter = new HashMap<>();
            parameter.put("valorNegociado", (new BigDecimal(pto.getValorNegociado())));
            parameter.put("tarifario", pto.getTarifarioNegociado());
            parameter.put("porcentajeNegociado", (new BigDecimal(pto.getPorcentajeNegociado())));
            parameter.put("userId", userId);
            parameter.put("sedeNegociacionIds", sedeNegociacionIds.stream().distinct().collect(Collectors.toList()));
            parameter.put("codigoCliente", pto.getCodigoEmssanar());
            parameter.put("servicioCodigo", pto.getCodigoServicio());
            if (negociacion.getOpcionImportarSeleccionada().equals(OpcionesImportacionTecnologiaEnum.IMPORTAR_SEDE_A_SEDE)) {
                parameter.put("codigoHabSede", pto.getCodigoHabilitacionSede());
            }
            parametersMap.add(parameter);
        }

        try {
            batchJdbUtil.executeIntoBatch(em, 1000, query.toString(), parametersMap);
        } catch (Exception e) {
           log.error("Error: actualizarProcedimientoEventoImportar", e);
        }
    }

    public List<SedeNegociacionServicioDto> consultarServiciosPorNegociacion(NegociacionDto negociacionDto){
        CriteriaBuilder criteriaBuilder = this.em.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
        Root<SedeNegociacionServicio> sedeNegociacionServicio = criteriaQuery.from(SedeNegociacionServicio.class);
        Join<SedeNegociacionServicio, SedesNegociacion> sedesNegociacion = sedeNegociacionServicio.join(SedeNegociacionServicio_.sedeNegociacion, JoinType.INNER);

        criteriaQuery.multiselect(
                sedeNegociacionServicio.get(SedeNegociacionServicio_.ID).alias("sedeNegociacionServico"),
                sedeNegociacionServicio.get(SedeNegociacionServicio_.servicioSalud).get(ServicioSalud_.ID).alias("servicioSaludId"),
                sedesNegociacion.get(SedesNegociacion_.ID).alias("sedeNegociacionId")
        ).where(
            criteriaBuilder.equal(sedesNegociacion.get(SedesNegociacion_.negociacion).get(Negociacion_.ID), negociacionDto.getId())
        );
        List<Tuple> resultadoTuple = em.createQuery(criteriaQuery).getResultList();

        return convertir(resultadoTuple);
    }

    private List<SedeNegociacionServicioDto> convertir(List<Tuple> resultadoTuple) {
        return resultadoTuple.stream().map(tuple -> {
            SedeNegociacionServicioDto dto = new SedeNegociacionServicioDto();
            dto.setId(tuple.get("sedeNegociacionServico", Long.class));
            dto.setSedeNegociacionId(tuple.get("sedeNegociacionId", Long.class));
            dto.setServicioSaludId(tuple.get("servicioSaludId", Long.class));
            return dto;
        }).collect(Collectors.toList());
    }

    public List<ProcedimientoDto> consultarProcedimiento(List<String> codigosEmssanar) {
        return em.createNamedQuery("AbstractTipoTecnologia.getByCodes", ProcedimientoDto.class)
                .setParameter("codigosEmssanar", new ArrayList<>(codigosEmssanar))
                .getResultList();
    }

    public void eliminarServiciosSinTecnologias(NegociacionDto negociacion) {
        List<SedeNegociacionServicio> serviciosSinTecnologias = obtenerServiciosSinTecnologias(negociacion);
        if (serviciosSinTecnologias.isEmpty()){
            return;
        }
        eliminarServiciosSinTecnologias(serviciosSinTecnologias);
    }

    private void eliminarServiciosSinTecnologias(List<SedeNegociacionServicio> serviciosSinTecnologias) {
        CriteriaBuilder criteriaBuilder = this.em.getCriteriaBuilder();
        CriteriaDelete<SedeNegociacionServicio> criteriaDelete = criteriaBuilder.createCriteriaDelete(SedeNegociacionServicio.class);
        Root<SedeNegociacionServicio> servicioRoot = criteriaDelete.from(SedeNegociacionServicio.class);
        criteriaDelete.where(servicioRoot.get(SedeNegociacionServicio_.ID).in(serviciosSinTecnologias.stream().map(SedeNegociacionServicio::getId).collect(Collectors.toList())));
        em.createQuery(criteriaDelete).executeUpdate();
    }

    private List<SedeNegociacionServicio> obtenerServiciosSinTecnologias(NegociacionDto negociacion) {
        CriteriaBuilder criteriaBuilder = this.em.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
        Root<SedesNegociacion> sedesNegociacion = criteriaQuery.from(SedesNegociacion.class);
        ListJoin<SedesNegociacion, SedeNegociacionServicio> servicios = sedesNegociacion.join(SedesNegociacion_.sedeNegociacionServicios, JoinType.INNER);
        ListJoin<SedeNegociacionServicio, SedeNegociacionProcedimiento> procedimientos = servicios.join(SedeNegociacionServicio_.sedeNegociacionProcedimientos, JoinType.LEFT);
        criteriaQuery.multiselect(
                servicios.get(SedeNegociacionServicio_.ID).alias("sedeNegociacionServicioId"),
                criteriaBuilder.count(procedimientos.get(SedeNegociacionProcedimiento_.ID)).alias("conteo")
        ).where(
                criteriaBuilder.equal(sedesNegociacion.get(SedesNegociacion_.negociacion).get(Negociacion_.ID), negociacion.getId()))
        .groupBy(
                servicios.get(SedeNegociacionServicio_.ID)
        );
        return em.createQuery(criteriaQuery).getResultList().stream()
                .filter(tuple -> Objects.equals(tuple.get("conteo", Long.class), 0L))
                .map(tuple -> {
                    SedeNegociacionServicio sedeNegociacionServicio = new SedeNegociacionServicio();
                    sedeNegociacionServicio.setId(tuple.get("sedeNegociacionServicioId", Long.class));
                    return sedeNegociacionServicio;
                })
                .collect(Collectors.toList());
    }
}
