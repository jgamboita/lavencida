package co.conexia.negociacion.services.negociacion.servicio.boundary;

import co.conexia.negociacion.services.negociacion.control.NegociacionControl;
import co.conexia.negociacion.services.negociacion.paquete.control.PaquetesNegociacionControl;
import co.conexia.negociacion.services.negociacion.servicio.control.ServicioNegociacionControl;
import com.conexia.contratacion.commons.constants.enums.RiasEnum;
import com.conexia.contratacion.commons.constants.enums.TipoNegociacionEnum;
import com.conexia.contractual.utils.exceptions.constants.PreContractualMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ProcedimientoContratoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroNegociacionTecnologiaDto;
import com.conexia.contratacion.commons.dto.maestros.*;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.negociacion.servicio.NegociacionServicioViewServiceRemote;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

@Stateless
@Remote(NegociacionServicioViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NegociacionServicioViewBoundary implements NegociacionServicioViewServiceRemote {

    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;

    /**
     * El control para este boundary *
     */
    @Inject
    private ServicioNegociacionControl serviciosControl;

    @Inject
    private NegociacionControl negociacionControl;

    @Inject
    private PaquetesNegociacionControl paqueteNegociacionControl;


    /**
     * *
     */
    private final String COMPARACION_CONTRATO_ANTERIOR = "SELECT DISTINCT "
            + "ps.servicio_id AS servicioId, ps.procedimiento_id AS procedimientoId, "
            + "cp.prestador_id AS prestadorId, ps.cups AS cups, "
            + "ps.codigo_cliente AS codigoCliente, ps.descripcion AS descripcionProcedimiento, "
            + "ps.complejidad AS complejidad, cp.tarifario_id AS tarifarioId, t.descripcion AS tarifarioDescripcion, "
            + "CASE WHEN ps.es_pos THEN 'POS' ELSE 'NO POS' END AS categoriaPos, "
            + "cp.porcentaje_negociacion AS porcentajeNegociacion, cp.valor_contratado AS valorContratado "
            + "FROM contratacion.contrato_procedimiento cp  "
            + "INNER JOIN contratacion.tarifarios t ON t.id = cp.tarifario_id "
            + "INNER JOIN maestros.procedimiento_servicio ps ON ps.id = cp.procedimiento_id  "
            + "WHERE cp.servicio_id = :servicioId AND cp.prestador_id = :prestadorId "
            + "ORDER BY cups ASC";

    private final String COMPARACION_PORTAFOLIO_PROPUESTO = "SELECT DISTINCT "
            + "ps.servicio_id AS servicioId, ps.procedimiento_id AS procedimientoId, "
            + "n.prestador_id AS prestadorId, ps.cups AS cups, "
            + "ps.codigo_cliente AS codigoCliente, px.descripcion AS descripcionProcedimiento, "
            + "ps.complejidad AS complejidad, snp.tarifario_propuesto_id AS tarifarioId, t.descripcion AS tarifarioDescripcion, "
            + "CASE WHEN ps.es_pos THEN 'POS' ELSE 'NO POS'  END AS categoriaPos, "
            + "snp.porcentaje_propuesto AS porcentajePropuesto, snp.valor_propuesto AS valorPropuesto "
            + "FROM contratacion.negociacion n "
            + "INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id "
            + "INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
            + "INNER JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id "
            + "LEFT JOIN contratacion.tarifarios t ON t.id = snp.tarifario_propuesto_id "
            + "INNER JOIN maestros.procedimiento_servicio ps   ON ps.id = snp.procedimiento_id "
            + "INNER JOIN maestros.procedimiento px ON px.id = ps.procedimiento_id "
            + "WHERE n.id = :negociacionId AND ps.servicio_id = :servicioId "
            + "ORDER BY cups ASC";

    /**
     * consulta los servicios que estan en la negociacion
     *
     * @param negociacionId Identificador de la negociacion
     * @return lista de {@link - ServicioNegociacionDto}
     */
    public List<ServicioNegociacionDto> consultarServiciosNegociacionNoSedesByNegociacionId(
            NegociacionDto negociacion) {
        List<ServicioNegociacionDto> listBase = em.createNamedQuery(
                "SedeNegociacionServicio.findServiciosNegociacionNoSedesByNegociacionId", ServicioNegociacionDto.class)
                .setParameter("negociacionId", negociacion.getId())
                .getResultList();
        // Evita la repetición de servicios en la negociación
        Map<Long, ServicioNegociacionDto> mapServicioNegociacion = new HashMap<Long, ServicioNegociacionDto>();
        listBase.stream()
                .forEach(servicioNegociacion -> {
                    if (!mapServicioNegociacion.containsKey(servicioNegociacion.getServicioSalud().getId())) {
                        mapServicioNegociacion.put(servicioNegociacion.getServicioSalud().getId(), servicioNegociacion);
                    }
                });
        return Objects.nonNull(mapServicioNegociacion) ?
                (List<ServicioNegociacionDto>) mapServicioNegociacion.values().stream()
                        .sorted((s1, s2) -> Long.compare(s1.getServicioSalud().getId(), s2.getServicioSalud().getId()))
                        .collect(Collectors.toList()) : null;
    }


    public List<CapitulosNegociacionDto> consultarCapitulosNegociacionNoSedesByNegociacionId(
            NegociacionDto negociacion) throws ConexiaBusinessException {
        List<CapitulosNegociacionDto> listBase = em.createNamedQuery(
                "SedeNegociacionCapitulo.findCapitulosNegociacionNoSedesByNegociacionId", CapitulosNegociacionDto.class)
                .setParameter("negociacionId", negociacion.getId())
                .getResultList();
        // Evita la repetición de servicios en la negociación
        Map<Long, CapitulosNegociacionDto> mapCapituloNegociacion = new HashMap<Long, CapitulosNegociacionDto>();
        listBase.stream()
                .forEach(capituloNegociacion -> {
                    if (!mapCapituloNegociacion.containsKey(capituloNegociacion.getCapituloProcedimiento().getId())) {
                        mapCapituloNegociacion.put(capituloNegociacion.getCapituloProcedimiento().getId(), capituloNegociacion);
                    }
                });
        return Objects.nonNull(mapCapituloNegociacion) ?
                (List<CapitulosNegociacionDto>) mapCapituloNegociacion.values().stream()
                        .sorted((c1, c2) -> Long.compare(c1.getCapituloProcedimiento().getId(), c2.getCapituloProcedimiento().getId()))
                        .collect(Collectors.toList()) : null;
    }

    @Override
    public List<ServicioNegociacionDto> consultarServiciosNegociacionCapita(
            NegociacionDto negociacion, Integer anio) throws ConexiaBusinessException {

        if (negociacion.getValorUpcMensual() == null) {
            throw new ConexiaBusinessException(PreContractualMensajeErrorEnum.SIN_VALOR_UPC);
        }

        return em.createNativeQuery(
                this.serviciosControl.generarConsultaCapita(negociacion, negociacion.getTipoNegociacion()).toString(),
                TipoNegociacionEnum.SEDE_A_SEDE.equals(negociacion.getTipoNegociacion())
                        ? "SedeNegociacionServicio.consultaNegociacioCapitaSSMapping"
                        : "SedeNegociacionServicio.consultaNegociacioCapitaMapping")
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("zonaCapitaId", negociacion.getZonaCapita().getId().longValue())
                .setParameter("regimenId", negociacion.getRegimen().getId())
                .setParameter("valorUpcMensual", negociacion.getValorUpcMensual())
                .setParameter("ano", anio)
                .getResultList();
    }

    /**
     * Consulta los datos para los procedimientos de 1 servicio en negociación
     * sin importar las sedes asociadas.
     *
     * @param negociacionId identificador de la negociacion
     * @param servicioId    El servicio a consultar los procedimientos.
     *                      procedimientos.
     * @return {@link - List<ProcedimientoNegociacionDto>}
     */
    @Override
    public List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionNoSedesByNegociacionAndServicio(NegociacionDto negociacion, Long negociacionId, Long servicioId, TipoTarifarioDto tarifaNoNormativa, FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia) {
        List<Long> negociacionServiciosIds = em.createNamedQuery("SedeNegociacionServicio.findIdsByNegociacion", Long.class)
                .setParameter("negociacionId", negociacionId)
                .getResultList();

        String QUERY = "SELECT "
                + " snp.valor_contrato as valor_contrato,"
                + " (select descripcion from contratacion.tarifarios where id = snp.tarifario_contrato_id) as tarifario_contrato, "
                + " snp.porcentaje_contrato as porcentaje_contrato, "
                + " min(snp.valor_propuesto) as valor_propuesto, "
                + " min(propuesto.tarifario_propuesto)  as tarifario_propuesto, "
                + " min(propuesto.porcentaje_propuesto) as porcentaje_propuesto,  "
                + " snp.valor_negociado as valor_negociado,  "
                + " snp.negociado as negociado, "
                + " snp.porcentaje_negociado as porcentaje_negociado, "
                + " 1=1 as tarifa_diferencial,"
                + " tarifario.id as tarifario_negociado_id,"
                + " tarifario.descripcion as tarifario_negociado_descripcion, "
                + " ps.id as procedimiento_id, "
                + " ps.cups as cups, "
                + " procedimiento.codigo_emssanar as codigo_cliente, procedimiento.descripcion as descripcion_emssanar, procedimiento.tipo_procedimiento_id,"
                + " ps.complejidad as complejidad, ps.es_pos as es_pos, ";
        if (filtroNegociacionTecnologia.getPgp()) {
            QUERY = QUERY
                    + " round(rps.frecuencia, 6) frecuencia_referente, rps.costo_medio_usuario costo_medio_usuario_referente, snp.costo_medio_usuario "
                    + " FROM contratacion.sede_negociacion_procedimiento snp "
                    + " inner join maestros.procedimiento_servicio ps on snp.procedimiento_id=ps.id AND ps.estado = 1 ";
        } else {
            QUERY = QUERY
                    + " snp.frecuencia_referente, snp.costo_medio_usuario_referente, "
                    + "snp.costo_medio_usuario, procedimiento.estado_procedimiento_id "
                    + " FROM contratacion.sede_negociacion_procedimiento snp "
                    + " inner join maestros.procedimiento_servicio ps on snp.procedimiento_id=ps.id";
        }
        QUERY = QUERY
                + " inner join maestros.procedimiento procedimiento on ps.procedimiento_id=procedimiento.id "
                + " inner join contratacion.sede_negociacion_servicio sns on snp.sede_negociacion_servicio_id=sns.id "
                + " inner join contratacion.sedes_negociacion sn on sns.sede_negociacion_id=sn.id "
                + " inner join contratacion.sede_prestador spre on sn.sede_prestador_id = spre.id"
                + " left outer join contratacion.tarifarios tarifario on snp.tarifario_negociado_id=tarifario.id  "
                + " left join (select ps_propuesto.cups,snp_propuesto.porcentaje_propuesto, snp_propuesto.valor_propuesto,"
                + "				tarifario_propuesto.descripcion as tarifario_propuesto"
                + "			   from contratacion.sede_negociacion_procedimiento snp_propuesto"
                + "			   inner join contratacion.tarifarios tarifario_propuesto on snp_propuesto.tarifario_propuesto_id=tarifario_propuesto.id"
                + "			   inner join maestros.procedimiento_servicio ps_propuesto on snp_propuesto.procedimiento_id=ps_propuesto.id"
                + "			   where snp_propuesto.sede_negociacion_servicio_id in ( :negociacionServiciosIds)"
                + "			   group by ps_propuesto.cups,snp_propuesto.porcentaje_propuesto,tarifario_propuesto.descripcion, snp_propuesto.valor_propuesto"
                + "			  ) as propuesto on propuesto.cups=ps.cups and propuesto.valor_propuesto=snp.valor_propuesto ";
        if (filtroNegociacionTecnologia.getPgp()) {
            if (negociacion.getEsRia() == Boolean.TRUE) {
                QUERY = QUERY
                        + "  	JOIN contratacion.negociacion neg on neg.id = sn.negociacion_id "
                        + "  	JOIN contratacion.referente_servicio rs on neg.referente_id = rs.referente_id "
                        + "  	JOIN contratacion.referente_procedimiento_servicio rps on rps.referente_servicio_id = rs.id "
                        + "				AND  rps.procedimiento_servicio_id = ps.id "
                        + " 	JOIN contratacion.negociacion_ria nr on nr.negociacion_id = neg.id"
                        + " 	JOIN maestros.ria_contenido rc on rc.ria_id = nr.ria_id "
                        + "				AND rc.procedimiento_servicio_id = ps.id ";
            } else {
                QUERY = QUERY + "JOIN contratacion.negociacion neg on neg.id = sn.negociacion_id "
                        + "  	JOIN contratacion.referente_servicio rs on neg.referente_id = rs.referente_id "
                        + "  	JOIN contratacion.referente_procedimiento_servicio rps on rps.referente_servicio_id = rs.id "
                        + "				AND  rps.procedimiento_servicio_id = ps.id "
                        + "     JOIN contratacion.grupo_servicio gs ON spre.portafolio_id = gs.portafolio_id "
                        + "				AND gs.servicio_salud_id = rs.servicio_salud_id "
                        + "     JOIN contratacion.procedimiento_portafolio prop ON prop.grupo_servicio_id = gs.id "
                        + "				AND prop.procedimiento_id = ps.id ";
            }
        }
        QUERY = QUERY
                + "	where sn.negociacion_id= :negociacionId"
                + "	and sns.servicio_id= :servicioId"
                + " group by "
                + "	ps.id , ps.cups , procedimiento.codigo_emssanar ,  procedimiento.descripcion , procedimiento.tipo_procedimiento_id,"
                + " ps.complejidad , ps.es_pos , tarifario.id , tarifario.descripcion ,"
                + " snp.tarifario_contrato_id,snp.valor_contrato,snp.porcentaje_contrato , snp.porcentaje_negociado ,"
                + " snp.negociado , snp.valor_negociado , snp.tarifario_negociado_id, ";
        if (filtroNegociacionTecnologia.getPgp()) {
            QUERY = QUERY
                    + " rps.frecuencia , rps.costo_medio_usuario , snp.costo_medio_usuario ";
        } else {
            QUERY = QUERY
                    + "	snp.frecuencia_referente, snp.costo_medio_usuario_referente, snp.costo_medio_usuario, procedimiento.estado_procedimiento_id ";
        }
        QUERY = QUERY
                + " order by snp.tarifario_negociado_id nulls first, snp.porcentaje_negociado nulls first, snp.valor_negociado nulls first";

        @SuppressWarnings("unchecked")
        List<ProcedimientoNegociacionDto> dtos =
                em.createNativeQuery(QUERY, "SedeNegociacionProcedimiento.ProcedimientoNegociacionMapping")
                        .setParameter("negociacionId", negociacionId)
                        .setParameter("servicioId", servicioId)
                        .setParameter("negociacionServiciosIds", negociacionServiciosIds)
                        .getResultList();
        dtos = serviciosControl.asignarTarifariosPermitidosProcedimientos(dtos, tarifaNoNormativa);
        return dtos;
    }

    @Override
    public List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionNoSedesByNegociacionAndCapitulo(NegociacionDto negociacion,
                                                                                                               Long negociacionId, Long capituloId, TipoTarifarioDto tarifaNoNormativa, FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia) {
        List<Long> negociacionCapitulosIds = em
                .createNamedQuery("SedeNegociacionCapitulo.findIdsByNegociacion", Long.class)
                .setParameter("negociacionId", negociacionId).getResultList();

        StringBuilder SELECT = new StringBuilder();
        SELECT.append(" select")
                .append(" px.id as procedimiento_id,")
                .append(" px.nivel_complejidad as complejidad,")
                .append(" px.tipo_procedimiento_id,")
                .append(" ps.es_pos as es_pos,")
                .append(" px.codigo as cups,")
                .append(" px.codigo_emssanar as codigo_cliente,")
                .append(" substring(px.descripcion,0,50) as descripcion_emssanar,")
                .append(" round(snp.frecuencia_referente,10) as frecuencia_referente, ")
                .append(" round(snp.costo_medio_usuario_referente,3) as costo_medio_usuario_referente, ")
                .append(" round(snp.frecuencia,10) as frecuencia_negociado,")
                .append(" round(snp.costo_medio_usuario,3) as costo_medio_usuario_negociado,")
                .append(" snp.valor_negociado as valor_negociado, ")
                .append(" snp.negociado as negociado,")
                .append(" snp.valor_referente as valorReferente,")
                .append(" snp.franja_inicio as franjaInicio,")
                .append(" snp.franja_fin as franjaFin")
                .append(" from contratacion.sede_negociacion_procedimiento snp")
                .append(" join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id")
                .append(" join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id")
                .append(" join contratacion.negociacion n on n.id = sn.negociacion_id")
                .append(" join maestros.procedimiento px on px.id = snp.pto_id")
                .append(" join maestros.procedimiento_servicio ps on ps.procedimiento_id = px.id")
                .append(" where snc.id in (:negociacionCapitulosIds) and")
                .append(" sn.negociacion_id = :negociacionId and snc.capitulo_id = :capituloId")
                .append(" group by 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16")
                .append(" order by px.codigo");


        @SuppressWarnings("unchecked")
        List<ProcedimientoNegociacionDto> dtos =
                em.createNativeQuery(SELECT.toString(), "SedeNegociacionProcedimiento.ProcedimientoNegociacionPGPMapping")
                        .setParameter("negociacionId", negociacionId)
                        .setParameter("capituloId", capituloId)
                        .setParameter("negociacionCapitulosIds", negociacionCapitulosIds)
                        .getResultList();
        dtos = serviciosControl.asignarTarifariosPermitidosProcedimientos(dtos, tarifaNoNormativa);
        return dtos;
    }

    public List<ProcedimientoNegociacionDto> asignarTarifariosSoportados(List<ProcedimientoNegociacionDto> procedimientos) {
        return serviciosControl.asignarTarifariosPermitidosProcedimientos(procedimientos);
    }

    public List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionCapita(List<Long> ids, NegociacionDto negociacion) {
        return this.serviciosControl.consultaProcedimientosNegociacionCapita(ids, negociacion);
    }
    /**
     * Consulta los procedimientos y valores para el contrato anterior por un
     * prestador y un servicio
     *
     * @param prestadorId
     * @param servicioId
     * @return Lista de ProcedimientoContratoDto
     */
    public List<ProcedimientoContratoDto> consultarProcedimientosContratoAnterior(Long prestadorId, Long servicioId) {
        List<ProcedimientoContratoDto> contratoProcedimientos = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object[]> resultado = em.createNativeQuery(COMPARACION_CONTRATO_ANTERIOR)
                .setParameter("prestadorId", prestadorId)
                .setParameter("servicioId", servicioId)
                .getResultList();

        // Mapeea los datos para crear el Dto de la consulta Nativa
        for (Object[] r : resultado) {
            ProcedimientoContratoDto contrato = new ProcedimientoContratoDto(
                    r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8], r[9], r[10], r[11]);
            contratoProcedimientos.add(contrato);
        }

        return contratoProcedimientos;
    }

    /**
     * Método que liquida el valor de un procedimiento basado en un porcentaje y
     * un tarifario
     *
     * @param cups
     * @param tarifarioId
     * @param porcentajeNegociacion
     * @return
     */
    public BigDecimal calcularValorProcedimiento(String cups, Integer tarifarioId, Double porcentajeNegociacion) {
        return (BigDecimal) em.createNativeQuery("SELECT contratacion.ajuste_porcentaje(contratacion.total_valores(:cups, :tarifarioId), CAST(:porcentajeNegociacion AS numeric)) AS valorCalculado")
                .setParameter("cups", cups)
                .setParameter("tarifarioId", tarifarioId)
                .setParameter("porcentajeNegociacion", porcentajeNegociacion)
                .getSingleResult();
    }

    /**
     * Consulta los procedimientos y valores para las tarifas propuestas del
     * servicio y prestador seleccionados
     *
     * @param negociacionId
     * @param servicioId
     * @return Lista de {@link - List<TarifaPropuestaProcedimientoDto>}
     */
    public List<TarifaPropuestaProcedimientoDto> consultarTarifasPropuestasProcedimiento(
            Long negociacionId, Long servicioId) {
        List<TarifaPropuestaProcedimientoDto> tarifasPropuestas = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Object[]> resultado = em
                .createNativeQuery(COMPARACION_PORTAFOLIO_PROPUESTO)
                .setParameter("negociacionId", negociacionId)
                .setParameter("servicioId", servicioId).getResultList();
        // Mapeea los datos para crear el Dto de la consulta Nativa
        for (Object[] r : resultado) {
            TarifaPropuestaProcedimientoDto tarifa = new TarifaPropuestaProcedimientoDto(
                    r[0], r[1], r[2], r[3], r[4], r[5], r[6], r[7], r[8], r[9],
                    r[10], r[11]);
            tarifasPropuestas.add(tarifa);
        }
        return tarifasPropuestas;
    }

    public List<ServicioSaludDto> findProcedimientosNoNegociados(NegociacionDto negociacion, boolean esTransporte) {
        StringBuilder query = new StringBuilder();
        this.serviciosControl.crearQueryBaseValidacionNoNegociados(query);
        this.serviciosControl.agregarValidacionQueryCamposNegociadosPorModalidad(negociacion.getTipoModalidadNegociacion(), query);
        query.append(" GROUP BY ss.codigo ORDER BY 1");
        return this.serviciosControl.consultarProcedimientosNoNegociados(negociacion.getId(), (esTransporte ? 3L : 1L), query);
    }

    @Override
    public List<ProcedimientoDto> consultarProcedimientosAgregar(ProcedimientoDto procedimiento, NegociacionDto negociacion) throws ConexiaBusinessException {
        this.serviciosControl.consultaEstadoProcedimiento(procedimiento);
        this.serviciosControl.consultaEstadoServicioProcedimiento(procedimiento);
        this.serviciosControl.validaNivelComplejidad(procedimiento, negociacion);
        this.serviciosControl.validarProcedimientosAgregar(procedimiento, negociacion);

        StringBuilder query = new StringBuilder();
        this.serviciosControl.generarQueryBaseConsultaProcedimientosAgregarNegociacion(query, negociacion);
        String resultClass = "procedimientosAgregarMapping";
        if (!procedimiento.getDescripcion().isEmpty() && !procedimiento.getCodigoCliente().isEmpty()) {
            query = new StringBuilder(query.toString().replace("OR", "AND"));
        }

        return em.createNativeQuery(query.toString(), resultClass)
                .setParameter("descripcion", (procedimiento.getDescripcion().isEmpty() ? "" : procedimiento.getDescripcion()))
                .setParameter("codigoServicio", procedimiento.getServicioHabilitacion())
                .setParameter("codigoProcedimiento", procedimiento.getCodigoCliente().isEmpty() ? "" : "%" + procedimiento.getCodigoCliente() + "%")
                .setParameter("negociacionId", negociacion.getId())
                .getResultList();
    }


    @Override
    public List<ProcedimientoDto> consultarProcedimientosAgregarPGP(
            ProcedimientoDto procedimiento, NegociacionDto negociacion, Long capituloId) throws ConexiaBusinessException {

        this.serviciosControl.validarProcedimientosAgregarPGP(procedimiento, negociacion, capituloId);

        StringBuilder query = new StringBuilder();
        this.serviciosControl
                .generarQueryBaseConsultaProcedimientosAgregarNegociacion(
                        query, negociacion);

        String resultClass = "procedimientosAgregarMapping";

        if (!procedimiento.getDescripcion().isEmpty()
                && !procedimiento.getCodigoCliente().isEmpty()) {
            query = new StringBuilder(query.toString().replace("OR", "AND"));
        }

        return em
                .createNativeQuery(query.toString(), resultClass)
                .setParameter(
                        "descripcion",
                        (procedimiento.getDescripcion().isEmpty() ? "" : ("%"
                                + procedimiento.getDescripcion() + "%")))
                .setParameter("codigoServicio",
                        procedimiento.getServicioHabilitacion())
                .setParameter(
                        "complejidad",
                        negociacionControl
                                .generarComplejidadesByNegociacionComplejidad(negociacion
                                        .getComplejidad()))
                .setParameter(
                        "codigoProcedimiento",
                        procedimiento.getCodigoCliente().isEmpty() ? "" : "%"
                                + procedimiento.getCodigoCliente() + "%")
                .setParameter("negociacionId", negociacion.getId())
                .getResultList();
    }

    /**
     * @param filtroNegociacionTecnologia
     * @return
     */
    @Override
    public List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionNoSedesByNegociacionAndServicio(NegociacionDto negociacion, FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia, TipoTarifarioDto tarifaNoNormativa) {
        String SELECT = "SELECT "
                + " snp.valor_contrato as valor_contrato,"
                + " (select descripcion from contratacion.tarifarios where id = snp.tarifario_contrato_id) as tarifario_contrato, "
                + " snp.porcentaje_contrato as porcentaje_contrato, "
                + " min(snp.valor_propuesto) as valor_propuesto, "
                + " min(propuesto.tarifario_propuesto)  as tarifario_propuesto, "
                + " min(propuesto.porcentaje_propuesto) as porcentaje_propuesto,  "
                + " snp.valor_negociado as valor_negociado,  "
                + " snp.negociado as negociado, "
                + " snp.porcentaje_negociado as porcentaje_negociado, "
                + " 1=1 as tarifa_diferencial,"
                + " tarifario.id as tarifario_negociado_id,"
                + " tarifario.descripcion as tarifario_negociado_descripcion, "
                + " ps.id as procedimiento_id, "
                + " ps.cups as cups, "
                + " procedimiento.codigo_emssanar as codigo_cliente, procedimiento.descripcion as descripcion_emssanar, procedimiento.tipo_procedimiento_id, "
                + " ps.complejidad as complejidad, ps.es_pos as es_pos,";
        if (filtroNegociacionTecnologia.getPgp()) {
            SELECT = SELECT + " round(rps.frecuencia,6) frecuencia_referente, rps.costo_medio_usuario costo_medio_usuario_referente, snp.costo_medio_usuario "
                            + " FROM contratacion.sede_negociacion_procedimiento snp "
                            + " inner join maestros.procedimiento_servicio ps on snp.procedimiento_id = ps.id AND ps.estado = 1";
        } else {
            SELECT = SELECT + " snp.frecuencia_referente, snp.costo_medio_usuario_referente, "
                            + " snp.costo_medio_usuario, procedimiento.estado_procedimiento_id "
                            + " FROM contratacion.sede_negociacion_procedimiento snp "
                            + " inner join maestros.procedimiento_servicio ps on snp.procedimiento_id = ps.id";
        }
        SELECT = SELECT + " inner join maestros.procedimiento procedimiento on ps.procedimiento_id=procedimiento.id "
                + " inner join contratacion.sede_negociacion_servicio sns on snp.sede_negociacion_servicio_id=sns.id "
                + " inner join contratacion.sedes_negociacion sn on sns.sede_negociacion_id=sn.id "
                + " inner join contratacion.sede_prestador spre on sn.sede_prestador_id = spre.id"
                + " left outer join contratacion.tarifarios tarifario on snp.tarifario_negociado_id=tarifario.id  "
                + " left join (select ps_propuesto.cups,snp_propuesto.porcentaje_propuesto, snp_propuesto.valor_propuesto,"
                + "				tarifario_propuesto.descripcion as tarifario_propuesto"
                + "			   from contratacion.sede_negociacion_procedimiento snp_propuesto"
                + "			   inner join contratacion.tarifarios tarifario_propuesto on snp_propuesto.tarifario_propuesto_id=tarifario_propuesto.id "
                + "			   inner join maestros.procedimiento_servicio ps_propuesto on snp_propuesto.procedimiento_id=ps_propuesto.id "
                + "			   where snp_propuesto.sede_negociacion_servicio_id in ( :negociacionServiciosIds) "
                + "			   group by ps_propuesto.cups,snp_propuesto.porcentaje_propuesto,tarifario_propuesto.descripcion, snp_propuesto.valor_propuesto "
                + "			  ) as propuesto on propuesto.cups=ps.cups and propuesto.valor_propuesto=snp.valor_propuesto ";
        if (filtroNegociacionTecnologia.getPgp()) {
            if (negociacion.getEsRia() == Boolean.TRUE) {
                SELECT = SELECT + " JOIN contratacion.negociacion neg on neg.id = sn.negociacion_id "
                        + "  	JOIN contratacion.referente_servicio rs on neg.referente_id = rs.referente_id "
                        + "  	JOIN contratacion.referente_procedimiento_servicio rps on rps.referente_servicio_id = rs.id AND  rps.procedimiento_servicio_id = ps.id "
                        + " 	JOIN contratacion.negociacion_ria nr on nr.negociacion_id = neg.id"
                        + " 	JOIN maestros.ria_contenido rc on rc.ria_id = nr.ria_id AND rc.procedimiento_servicio_id = ps.id ";
            } else {
                SELECT = SELECT + " JOIN contratacion.negociacion neg on neg.id = sn.negociacion_id "
                        + "  	JOIN contratacion.referente_servicio rs on neg.referente_id = rs.referente_id "
                        + "  	JOIN contratacion.referente_procedimiento_servicio rps on rps.referente_servicio_id = rs.id AND  rps.procedimiento_servicio_id = ps.id ";
            }

        }
        SELECT = SELECT + "	where sn.negociacion_id= :negociacionId and sns.servicio_id= :servicioId ";

        String GROUP_BY = "  GROUP BY "
                + "		ps.id , ps.cups , procedimiento.codigo_emssanar , procedimiento.descripcion ,procedimiento.tipo_procedimiento_id, "
                + "     ps.complejidad , ps.es_pos , tarifario.id ,	tarifario.descripcion ,"
                + "     snp.tarifario_contrato_id,snp.porcentaje_contrato,snp.valor_contrato , snp.porcentaje_negociado , "
                + "		snp.negociado , snp.valor_negociado , snp.tarifario_negociado_id, ";
        if (filtroNegociacionTecnologia.getPgp()) {
            GROUP_BY = GROUP_BY + " rps.frecuencia , rps.costo_medio_usuario , snp.costo_medio_usuario ";
        } else {
            GROUP_BY = GROUP_BY + "	snp.frecuencia_referente, snp.costo_medio_usuario_referente, snp.costo_medio_usuario, procedimiento.estado_procedimiento_id ";
        }
        GROUP_BY = GROUP_BY + "  ORDER BY snp.tarifario_negociado_id nulls first, snp.porcentaje_negociado nulls first, snp.valor_negociado nulls first";

        // Genera la conulta Final
        SELECT = SELECT + negociacionControl.generarConsultaFiltros(filtroNegociacionTecnologia) + GROUP_BY;

        List<Long> negociacionServiciosIds = em.createNamedQuery("SedeNegociacionServicio.findIdsByNegociacion", Long.class)
                .setParameter("negociacionId", filtroNegociacionTecnologia.getNegociacionId())
                .getResultList();

        @SuppressWarnings("unchecked")
        List<ProcedimientoNegociacionDto> dtos = em.createNativeQuery(SELECT, "SedeNegociacionProcedimiento.ProcedimientoNegociacionMapping")
                .setParameter("negociacionId", filtroNegociacionTecnologia.getNegociacionId())
                .setParameter("servicioId", filtroNegociacionTecnologia.getServicioNegociacionId())
                .setParameter("negociacionServiciosIds", negociacionServiciosIds)
                .setFirstResult(filtroNegociacionTecnologia.getPagina())
                .setMaxResults(filtroNegociacionTecnologia.getCantidadRegistros())
                .getResultList();
        dtos = serviciosControl.asignarTarifariosPermitidosProcedimientos(dtos, tarifaNoNormativa);
        return dtos;
    }

    @Override
    public List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionNoSedesByNegociacionAndCapitulo(NegociacionDto negociacion, FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia, TipoTarifarioDto tarifaNoNormativa) {

        StringBuilder SELECT = new StringBuilder();
        SELECT.append(" select")
                .append(" px.id as procedimiento_id,")
                .append(" px.nivel_complejidad as complejidad,")
                .append(" px.tipo_procedimiento_id,")
                .append(" ps.es_pos as es_pos,")
                .append(" px.codigo as cups,")
                .append(" px.codigo_emssanar as codigo_cliente,")
                .append(" substring(px.descripcion,0,50) as descripcion_emssanar,")
                .append(" round(snp.frecuencia_referente,10) as frecuencia_referente, ")
                .append(" round(snp.costo_medio_usuario_referente,3) as costo_medio_usuario_referente, ")
                .append(" round(snp.frecuencia,10) as frecuencia_negociado,")
                .append(" round(snp.costo_medio_usuario,3) as costo_medio_usuario_negociado,")
                .append(" snp.valor_negociado as valor_negociado, ")
                .append(" snp.negociado as negociado,")
                .append(" snp.valor_referente as valorReferente,")
                .append(" snp.franja_inicio as franjaInicio,")
                .append(" snp.franja_fin as franjaFin")
                .append(" from contratacion.sede_negociacion_procedimiento snp")
                .append(" join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id")
                .append(" join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id")
                .append(" join contratacion.negociacion n on n.id = sn.negociacion_id")
                .append(" join maestros.procedimiento px on px.id = snp.pto_id")
                .append(" join maestros.procedimiento_servicio ps on ps.procedimiento_id = px.id")
                .append(" where snc.id in (:negociacionCapitulosIds) and")
                .append(" sn.negociacion_id = :negociacionId and snc.capitulo_id = :capituloId");


        StringBuilder GROUP_BY = new StringBuilder();
        GROUP_BY.append(" group by 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16")
                .append(" order by px.codigo");

        // Genera la conulta Final
        SELECT.append(negociacionControl.generarConsultaFiltrosPGP(filtroNegociacionTecnologia)).append(GROUP_BY);

        List<Long> negociacionCapitulosIds = em
                .createNamedQuery("SedeNegociacionCapitulo.findIdsByNegociacion", Long.class)
                .setParameter("negociacionId", filtroNegociacionTecnologia.getNegociacionId()).getResultList();

        @SuppressWarnings("unchecked")
        List<ProcedimientoNegociacionDto> dtos = em.createNativeQuery(SELECT.toString(), "SedeNegociacionProcedimiento.ProcedimientoNegociacionPGPMapping")
                .setParameter("negociacionId", filtroNegociacionTecnologia.getNegociacionId())
                .setParameter("capituloId", filtroNegociacionTecnologia.getCapituloNegociacionId())
                .setParameter("negociacionCapitulosIds", negociacionCapitulosIds)
                .setFirstResult(filtroNegociacionTecnologia.getPagina())
                .setMaxResults(filtroNegociacionTecnologia.getCantidadRegistros()).getResultList();
        dtos = serviciosControl.asignarTarifariosPermitidosProcedimientos(dtos, tarifaNoNormativa);

        return dtos;
    }

    /**
     * @param filtroNegociacionTecnologia
     * @return
     */
    @Override
    public Integer contarProcedimientosNegociacionNoSedesByNegociacionAndServicio(FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia,
                                                                                  NegociacionDto negociacion) {
        String CONTEO = "SELECT count(0) " +
                "FROM (SELECT min(snp.valor_contrato)       AS valor_contrato, " +
                "             'tarifario 1'                 AS tarifario_1, " +
                "             0.0                           AS porcentaje_1, " +
                "             min(snp.valor_propuesto)      AS valor_propuesto, " +
                "             'tarifario 2'                 AS tarifario_2, " +
                "             0.0                           AS porcentaje_2, " +
                "             snp.valor_negociado           AS valor_negociado, " +
                "             snp.negociado                 AS negociado, " +
                "             snp.porcentaje_negociado      AS porcentaje_negociado, " +
                "             TRUE, " +
                "             tar.id                        AS tarifari_id, " +
                "             tar.descripcion               AS tarifario_descripcion, " +
                "             ps.id                         AS procedimiento_servicio_id, " +
                "             ps.cups                       AS cups, " +
                "             procedimiento.codigo_emssanar AS codigo_emssanar, " +
                "             procedimiento.descripcion     AS descripcion_procedimiento, " +
                "             ps.complejidad                AS complejidad, " +
                "             ps.es_pos                     AS es_pos, " +
                "             snp.frecuencia_referente, " +
                "             snp.costo_medio_usuario_referente ";
        if (filtroNegociacionTecnologia.getPgp()) {
            if (negociacion.getEsRia() == Boolean.TRUE) {
                CONTEO = CONTEO + "FROM contratacion.sede_negociacion_procedimiento snp " +
                        " JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id AND ps.estado = 1 " +
                        " JOIN maestros.procedimiento procedimiento ON ps.procedimiento_id = procedimiento.id " +
                        " JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id " +
                        " JOIN contratacion.sedes_negociacion sedesneg ON sns.sede_negociacion_id = sedesneg.id " +

                        " JOIN contratacion.sede_prestador spre ON sedesneg.sede_prestador_id = spre.id" +
                        " JOIN contratacion.negociacion neg on neg.id = sedesneg.negociacion_id " +
                        " JOIN contratacion.referente_servicio rs on neg.referente_id = rs.referente_id " +
                        " JOIN contratacion.referente_procedimiento_servicio rps on rps.referente_servicio_id = rs.id AND  rps.procedimiento_servicio_id = ps.id " +
                        " JOIN contratacion.negociacion_ria nr on nr.negociacion_id = neg.id" +
                        " JOIN maestros.ria_contenido rc on rc.ria_id = nr.ria_id AND rc.procedimiento_servicio_id = ps.id ";
            } else {
                CONTEO = CONTEO + "FROM contratacion.sede_negociacion_procedimiento snp " +
                        " JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id AND ps.estado = 1 " +
                        " JOIN maestros.procedimiento procedimiento ON ps.procedimiento_id = procedimiento.id " +
                        " JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id " +
                        " JOIN contratacion.sedes_negociacion sedesneg ON sns.sede_negociacion_id = sedesneg.id " +

                        " JOIN contratacion.negociacion neg on neg.id = sedesneg.negociacion_id " +
                        " JOIN contratacion.referente_servicio rs on neg.referente_id = rs.referente_id " +
                        " JOIN contratacion.referente_procedimiento_servicio rps on rps.referente_servicio_id = rs.id AND rps.procedimiento_servicio_id = ps.id ";
            }
        }
        CONTEO = CONTEO + "FROM contratacion.sede_negociacion_procedimiento snp " +
                " JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id " +
                " JOIN maestros.procedimiento procedimiento ON ps.procedimiento_id = procedimiento.id " +
                " JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id " +
                " JOIN contratacion.sedes_negociacion sedesneg ON sns.sede_negociacion_id = sedesneg.id " +

                " LEFT OUTER JOIN contratacion.tarifarios tar on snp.tarifario_negociado_id = tar.id " +
                " WHERE sedesneg.negociacion_id= :negociacionId " +
                "   and sns.servicio_id= :servicioId ";
        String GROUP_BY = "GROUP BY ps.id, ps.cups, procedimiento.codigo_emssanar, procedimiento.descripcion, ps.complejidad, ps.es_pos, tar.id, " +
                "         tar.descripcion, snp.porcentaje_negociado, snp.negociado, snp.valor_negociado, snp.tarifario_negociado_id,  " +
                "         snp.frecuencia_referente, snp.costo_medio_usuario_referente) AS conteo";
        CONTEO = CONTEO + negociacionControl.generarConsultaFiltros(filtroNegociacionTecnologia) + GROUP_BY;

        String conteo = em.createNativeQuery(CONTEO)
                .setParameter("negociacionId", filtroNegociacionTecnologia.getNegociacionId())
                .setParameter("servicioId", filtroNegociacionTecnologia.getServicioNegociacionId())
                .getSingleResult().toString();

        return conteo != null ? new Integer(conteo) : 0;
    }

    @Override
    public Integer contarProcedimientosNegociacionNoSedesByNegociacionAndCapitulo(FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia,
                                                                                  NegociacionDto negociacion) {
        StringBuilder CONTEO = new StringBuilder();

        CONTEO.append(" select COUNT(0) from (select")
                .append(" px.id as procedimiento_id,")
                .append(" px.nivel_complejidad as complejidad,")
                .append(" px.tipo_procedimiento_id,")
                .append(" ps.es_pos as es_pos,")
                .append(" px.codigo as cups,")
                .append(" px.codigo_emssanar as codigo_cliente,")
                .append(" substring(px.descripcion,0,50) as descripcion_emssanar,")
                .append(" round(snp.frecuencia_referente,6) as frecuencia_referente, ")
                .append(" snp.costo_medio_usuario_referente as costo_medio_usuario_referente, ")
                .append(" round(snp.frecuencia,10) as frecuencia_negociado,")
                .append(" snp.costo_medio_usuario as costo_medio_usuario_negociado,")
                .append(" snp.valor_negociado as valor_negociado, ")
                .append(" snp.negociado as negociado,")
                .append(" snp.valor_referente as valorReferente,")
                .append(" snp.franja_inicio as franjaInicio,")
                .append(" snp.franja_fin as franjaFin")
                .append(" from contratacion.sede_negociacion_procedimiento snp")
                .append(" join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id")
                .append(" join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id")
                .append(" join contratacion.negociacion n on n.id = sn.negociacion_id")
                .append(" join maestros.procedimiento px on px.id = snp.pto_id")
                .append(" join maestros.procedimiento_servicio ps on ps.procedimiento_id = px.id")
                .append(" where")
                .append(" sn.negociacion_id = :negociacionId ")
                .append(" and snc.capitulo_id = :capituloId ");

        StringBuilder GROUP_BY = new StringBuilder();
        GROUP_BY.append(" group by 1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16) as conteo ");


        String conteo = em.createNativeQuery(CONTEO.toString() + negociacionControl.generarConsultaFiltrosPGP(filtroNegociacionTecnologia) + GROUP_BY.toString())
                .setParameter("negociacionId", filtroNegociacionTecnologia.getNegociacionId())
                .setParameter("capituloId", filtroNegociacionTecnologia.getCapituloNegociacionId())
                .getSingleResult().toString();
        return conteo != null ? new Integer(conteo) : 0;
    }

    @Override
    public Integer contarProcedimientosByNegociacionId(Long negociacionId) {
        String conteo = em.createNamedQuery("SedeNegociacionProcedimiento.contarProcedimientosByNegociacionId")
                .setParameter("negociacionId", negociacionId)
                .getSingleResult().toString();

        return conteo != null ? new Integer(conteo) : 0;
    }

    @Override
    public TipoTarifarioDto consultarTarifarioByDescripcion(String tarifario) {
        TipoTarifarioDto tipoTarifario = new TipoTarifarioDto();
        try {
            tipoTarifario = em.createNamedQuery("TipoTarifario.findTarifarioByDescripion", TipoTarifarioDto.class)
                    .setParameter("descripcionTarifa", tarifario)
                    .getSingleResult();
        } catch (NoResultException e) {
        }

        return tipoTarifario;
    }


    @Override
    public List<String> serviciosRepsNegociacion(List<ProcedimientoNegociacionDto> listProcedimientoNegociacion,
                                                 Long negociacionId) {
        Set<String> servicios = new HashSet<>();
        for (ProcedimientoNegociacionDto pro : listProcedimientoNegociacion) {
            servicios.add(pro.getProcedimientoDto().getServicioSalud().getCodigo());
        }
        List<String> serviciosArchivo = new ArrayList<String>(servicios);
        try {
            List<ServicioSaludDto> serviciosAgregados = em.createNamedQuery("SedeNegociacionServicio.selectServiciosNegociacion", ServicioSaludDto.class)
                    .setParameter("negociacionId", negociacionId)
                    .setParameter("servicioCodigo", servicios)
                    .getResultList();

            // Actualiza valores negociados en los serviciso


            List<String> serviciosFinales = new ArrayList<>();
            for (ServicioSaludDto servicio : serviciosAgregados) {
                serviciosFinales.add(servicio.getCodigo());
            }
            serviciosArchivo.removeAll(serviciosFinales);
            Collections.sort(serviciosArchivo);
            return serviciosArchivo;

        } catch (ArrayIndexOutOfBoundsException e) {
            Collections.sort(serviciosArchivo);
            return serviciosArchivo;
        }
    }

    @Override
    public List<SedesNegociacionDto> consultarSedeNegociacionServiciosByNegociacionId(Long negociacionId) {
        String query = "SELECT DISTINCT sn.id, sn.sede_prestador_id, sn.negociacion_id "
                + " FROM contratacion.sedes_negociacion sn"
                + " JOIN contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id "
                + " JOIN contratacion.prestador p on p.id = sp.prestador_id"
                + " JOIN maestros.servicios_reps sr on sr.nits_nit = p.numero_documento"
                + " WHERE sn.negociacion_id =:negociacionId "
                + " AND sr.numero_sede = cast (sp.codigo_sede as integer)";
        return em.createNativeQuery(query, "SedesNegociacion.sedeNegociacionMapping")
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    @Override
    public List<CapitulosNegociacionDto> consultarCapitulosNegociacionSedesPgpByNegociacionId(
            NegociacionDto negociacion) {
        List<CapitulosNegociacionDto> listBase = new ArrayList<>();
        if (negociacion.getEsRia() == Boolean.FALSE) {

            listBase = em.createNamedQuery("SedeNegociacionCapitulo.findCapitulosNegociacionSedesPgpSinRuta",
                    CapitulosNegociacionDto.class).setParameter("negociacionId", negociacion.getId()).getResultList();
        } else {
           listBase = em.createNamedQuery("SedeNegociacionCapitulo.findCapitulosNegociacionNoSedesByNegociacionId", CapitulosNegociacionDto.class)
                    .setParameter("negociacionId", negociacion.getId())
                    .getResultList();
        }
        // Evita la repetición de capítulos en la negociación
        Map<Long, CapitulosNegociacionDto> mapCapituloNegociacion = new HashMap<Long, CapitulosNegociacionDto>();
        listBase.stream().forEach(capituloNegociacion -> {
            if (!mapCapituloNegociacion.containsKey(capituloNegociacion.getCapituloProcedimiento().getId())) {
                mapCapituloNegociacion.put(capituloNegociacion.getCapituloProcedimiento().getId(), capituloNegociacion);
            }
        });
        return Objects.nonNull(mapCapituloNegociacion) ? (List<CapitulosNegociacionDto>) mapCapituloNegociacion.values()
                .stream().sorted((c1, c2) -> Long.compare(c1.getCapituloProcedimiento().getId(), c2.getCapituloProcedimiento().getId()))
                .collect(Collectors.toList()) : null;
    }

    public List<ProcedimientoNegociacionDto> consultarDetalleProcedimientoRia(NegociacionRiaRangoPoblacionDto rangoPoblacion, NegociacionDto negociacion) {
        return em.createNamedQuery("SedeNegociacionProcedimiento.consultarDetalleProcedimientoRia", ProcedimientoNegociacionDto.class)
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("rangoPoblacionId", rangoPoblacion.getRangoPoblacion().getId())
                .setParameter("riaId", rangoPoblacion.getNegociacionRia().getRia().getId())
                .setParameter("referenteId", negociacion.getReferenteDto().getId())
                .getResultList();

    }

    @Override
    public List<ProcedimientoNegociacionDto> consultarDetalleMedicamentoRia(NegociacionRiaRangoPoblacionDto rangoPoblacion, NegociacionDto negociacion) {
        if (rangoPoblacion.getNegociacionRia().getRia().getId().equals(RiasEnum.RECUPERACION.getId())) {
            List<ProcedimientoNegociacionDto> result = em.createNamedQuery("SedeNegociacionProcedimiento.consultarDetalleMedicamentoRia", ProcedimientoNegociacionDto.class)
                    .setParameter("negociacionId", negociacion.getId())
                    .setParameter("rangoPoblacionId", rangoPoblacion.getRangoPoblacion().getId())
                    .setParameter("riaId", rangoPoblacion.getNegociacionRia().getRia().getId())
                    .setParameter("referenteId", negociacion.getReferenteDto().getId())
                    .getResultList();
            for (ProcedimientoNegociacionDto procedimientoNegociacionDto : result) {
                procedimientoNegociacionDto.setValorNegociado(new BigDecimal(new DecimalFormat("#.####").format(procedimientoNegociacionDto.getValorNegociado()).replaceAll(",", ".")));
            }
            return result;
        } else {
            return em.createNamedQuery("SedeNegociacionProcedimiento.consultarDetalleMedicamentoRia", ProcedimientoNegociacionDto.class)
                    .setParameter("negociacionId", negociacion.getId())
                    .setParameter("rangoPoblacionId", rangoPoblacion.getRangoPoblacion().getId())
                    .setParameter("riaId", rangoPoblacion.getNegociacionRia().getRia().getId())
                    .setParameter("referenteId", negociacion.getReferenteDto().getId())
                    .getResultList();
        }
    }


    @Override
    public Integer contarProcedimientosByNegociacionCapitulo(Long negociacionId, Long capituloId) {
        String conteo = em.createNamedQuery("SedeNegociacionProcedimiento.contarProcedimientosByNegociacionCapitulo")
                .setParameter("negociacionId", negociacionId)
                .setParameter("capituloId", capituloId)
                .getSingleResult().toString();

        return conteo != null ? new Integer(conteo) : 0;
    }

    @Override
    public List<AfiliadoDto> consultarAfiliadosByNegociacionId(Long negociacionId) throws ConexiaBusinessException {
        return em.createNamedQuery("Negociacion.consultarAfiliadosByNegociacionId", AfiliadoDto.class)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    @Override
    public Integer countProcedByNegociacion(NegociacionDto negociacion) {
        String procedimientos = em.createNamedQuery("SedesNegociacion.countServiciosProcedByNegociacion")
                .setParameter("idNegociacion", negociacion.getId())
                .getSingleResult().toString();
        return procedimientos != null ? new Integer(procedimientos) : 0;
    }

    @Override
    public Integer countMedicamentoByNegociacion(NegociacionDto negociacion) {
        String medicamentos = em.createNamedQuery("SedeNegociacionMedicamento.countMedicamentoByNegociacion")
                .setParameter("negociacionId", negociacion.getId())
                .getSingleResult().toString();
        return medicamentos != null ? new Integer(medicamentos) : 0;
    }

    @Override
    public ValidarTarifarioDTO cargarReporteTarifarioByNegociacion (NegociacionDto negociacion) {
        ValidarTarifarioDTO validarTarifarioDto = new ValidarTarifarioDTO();
        List<Long> negociacionServiciosIds = em
                .createNamedQuery("SedeNegociacionServicio.findIdsByNegociacion", Long.class)
                .setParameter("negociacionId", negociacion.getId()).getResultList();

        String selectProcedimiento = " select proced.id,\n"
                + "proced.codigo,\n"
                + "proced.descripcion,\n"
                + "proced.nivel_autorizacion,\n"
                + "proced.nivel_complejidad,\n"
                + "proced.estado_procedimiento_id,\n"
                + "proced.genero_id,\n"
                + "proced.tipo_ppm_id,\n"
                + "proced.tipo_pago_requerido_enum,\n"
                + "proced.cliente_pk,\n"
                + "proced.es_quirurgico,\n"
                + "proced.es_recobrable,\n"
                + "proced.es_internacion,\n"
                + "proced.codigo_emssanar,\n"
                + "proced.codigo_habilitacion,\n"
                + "proced.categoria_transporte_id,\n"
                + "proced.tipo_procedimiento_id,\n"
                + "proced.procedimiento_padre_id,\n"
                + "proced.codigo_cups_seccion,\n"
                + "proced.descripcion_cups_seccion,\n"
                + "proced.codigo_cups_capitulo,\n"
                + "proced.descripcion_cups_capitulo,\n"
                + "proced.tipo_transporte_comercial_id,\n"
                + "proced.tipo_pago_requerido_contributivo,\n"
                + "proced.cod_rips_categoria_agrupada_id,\n"
                + "proced.categoria_procedimiento_id,\n"
                + "proced.parametrizacion_ambulatoria,\n"
                + "proced.parametrizacion_hospitalaria,\n"
                + "proced.valor_ambito_ambulatorio,\n"
                + "proced.valor_ambito_hospitalario,\n"
                + "coalesce(snp.valor_negociado,0) as valor_negociado \n"
                + "\n";
        String cuerpo = " FROM contratacion.sede_negociacion_procedimiento snp  inner join maestros.procedimiento_servicio ps on snp.procedimiento_id=ps.id AND ps.estado = 1 inner join maestros.procedimiento proced on ps.procedimiento_id=proced.id  inner join contratacion.sede_negociacion_servicio sns on snp.sede_negociacion_servicio_id=sns.id  inner join contratacion.servicio_salud ss on ss.id=sns.servicio_id  inner join contratacion.sedes_negociacion sn on sns.sede_negociacion_id=sn.id  inner join contratacion.sede_prestador spre on sn.sede_prestador_id = spre.id left outer join contratacion.tarifarios tarifario on snp.tarifario_negociado_id=tarifario.id   left join (select ps_propuesto.cups,snp_propuesto.porcentaje_propuesto, snp_propuesto.valor_propuesto,				tarifario_propuesto.descripcion as tarifario_propuesto			   from contratacion.sede_negociacion_procedimiento snp_propuesto			   inner join contratacion.tarifarios tarifario_propuesto on snp_propuesto.tarifario_propuesto_id=tarifario_propuesto.id 			   inner join maestros.procedimiento_servicio ps_propuesto on snp_propuesto.procedimiento_id=ps_propuesto.id 			   where snp_propuesto.sede_negociacion_servicio_id in ( :negociacionServiciosIds) 			   group by ps_propuesto.cups,snp_propuesto.porcentaje_propuesto,tarifario_propuesto.descripcion, snp_propuesto.valor_propuesto 			  ) as propuesto on propuesto.cups=ps.cups and propuesto.valor_propuesto=snp.valor_propuesto 	where sn.negociacion_id= :negociacionId "//nd sns.servicio_id= :servicioId\n"
                + " ";

        List<ProcedimientoDto> dtos = em.createNativeQuery(selectProcedimiento.concat(cuerpo), "Procedimiento.ProcedimientoOfNegociacionMapping")
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("negociacionServiciosIds", negociacionServiciosIds.size() == 0 ? 0 : negociacionServiciosIds)
                .getResultList();

        String selecReporteTarifario = " select ss.id as id_servicio,"
                + "proced.parametrizacion_ambulatoria,\n"
                + "proced.parametrizacion_hospitalaria,\n"
                + "proced.valor_ambito_ambulatorio,\n"
                + "proced.valor_ambito_hospitalario,\n"
                + "proced.parametrizacion_domiciliaria,\n"
                + "proced.valor_ambito_domiciliario,\n"
                + "  ps.id,\n"
                + "ps.descripcion,\n"
                + "ps.cups,\n"
                + "ps.nivel_cups,\n"
                + "ps.servicio_id,\n"
                + "ps.reps_cups,\n"
                + " ps.complejidad,\n"
                + " ps.especialidad_id,\n"
                + "ps.codigo_cliente,\n"
                + " ps.es_pos,\n"
                + " ps.procedimiento_id,\n"
                + " ps.estado, coalesce(snp.valor_negociado,0) as valor_negociado, "
                + " proced.codigo_cups_seccion as codigo_cups_capitulo,"
                + " (select descripcion from maestros.capitulo_procedimiento where codigo=proced.codigo_cups_seccion) as descripcion_cups_capitulo,"
                + " ss.codigo as codigo_servicio,"
                + " ss.nombre as nombre_servicio ";

        List<ProcedimientoServicioDTO> dtosServicio
                = em.createNativeQuery(selecReporteTarifario.concat(cuerpo), "ProcedimientoServicio.ProcedimientoServiceMapping")
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("negociacionServiciosIds", negociacionServiciosIds.size() == 0 ? 0 : negociacionServiciosIds)
                .getResultList();


        String selectMedicamento = " select\n"
                + "	m.id,\n"
                + "	m.codigo,\n"
                + "	m.codigo_emssanar,\n"
                + "m.descripcion,\n"
                + "m.nombre_alterno,\n"
                + "m.descripcion_atc,\n"
                + "m.descripcion_invima,\n"
                + "	m.valor_referente,\n"
                + "	coalesce(snm.valor_negociado,0) as valor_negociado,"
                + "m.regulado,m.valor_maximo_regulado,m.valor_referente_minimo\n"
                + "from\n"
                + "	contratacion.sede_negociacion_medicamento snm\n"
                + "join maestros.medicamento m on\n"
                + "	snm.medicamento_id = m.id\n"
                + "left join contratacion.categoria_medicamento cm on\n"
                + "	m.categoria_id = cm.id\n"
                + "join contratacion.sedes_negociacion sn on\n"
                + "	snm.sede_negociacion_id = sn.id\n"
                + "where\n"
                + "	sn.negociacion_id = :negociacionId\n"
                + "	and snm.negociado = true ";

        List<SedeNegociacionMedicamentoDTO> dtosMedicamento
                = em.createNativeQuery(selectMedicamento, "SedeNegociacionMedicamento.medicamentosTarifarioMapping")
                .setParameter("negociacionId", negociacion.getId())
                .getResultList();

        validarTarifarioDto.setListSedeNegociacionMedicamentoDto(dtosMedicamento);
        validarTarifarioDto.setListProcedimientoDTO(dtos);
        validarTarifarioDto.setListProcedimientoServicioDTO(dtosServicio);

        return validarTarifarioDto;
    }

    @Override
    public Collection<? extends ServicioSaludDto> consultarServiciosMaestros(ServicioSaludDto servicio, NegociacionDto negociacion) throws ConexiaBusinessException {
        return this.serviciosControl.consultarServicioMaestros(servicio, negociacion);
    }

    public Boolean consultarExistenciaServicioMaestros(ServicioSaludDto servicio)
            throws ConexiaBusinessException
    {
        return this.serviciosControl.consultarExistenciaServicioMaestros(servicio);
    }

}
