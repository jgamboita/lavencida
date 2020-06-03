package com.conexia.contractual.services.transactional.legalizacion.control;

import com.conexia.contractual.utils.ContratacionWsUtil;
import com.conexia.contractual.utils.RestBaseUtil;
import com.conexia.contractual.utils.requests.GetLegalizationContractRequestUtil;
import com.conexia.contratacion.commons.api.contratacionws.request.legalizationcontract.GetLegalizationContractRequest;
import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contractual.model.contratacion.auditoria.HistorialCambios;
import com.conexia.contractual.model.contratacion.contrato.Contrato;
import com.conexia.contractual.model.maestros.ServicioReps;
import com.conexia.contractual.model.security.User;
import com.conexia.contractual.services.transactional.commons.control.CommonsDtoToEntityControl;
import com.conexia.contractual.services.transactional.parametrizacion.control.ParametrizacionContratoValidacionControl;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contratacion.commons.api.contratacionws.localizator.ApiLocalizatorPathEnum;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.SecuenciaContratoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionRiaRangoPoblacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ObjetoContractualDto;
import com.conexia.contratacion.commons.dto.negociacion.ServiciosHabilitadosRespNoRepsDto;
import com.conexia.contratacion.rest.client.execution.IApiRestExecution;
import com.conexia.contratacion.rest.client.execution.dto.Parametro;
import com.conexia.contratacion.rest.client.execution.dto.RequestHttp;
import com.conexia.contratacion.rest.client.execution.dto.ResponseHttp;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author jalvarado
 */
public class LegalizacionContratoControl extends RestBaseUtil {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    @Inject
    private GenerarNumeroContratoControl generarNumeroContratoControl;

    @Inject
    private ParametrizacionContratoValidacionControl parametrizacionContratoValidacionControl;

    @Inject
    private CommonsDtoToEntityControl commonsDtoToEntityControl;

    @Inject
    private Log log;

    @Inject
    private ContratacionWsUtil contratacionWsUtil;

    @Inject
    private IApiRestExecution iApiRestExecution;

    private static final String PARAM_NEGOTIATION_ID= "negotiationId";

    public Contrato almacenarContrato(final ContratoDto contratoDto, EstadoLegalizacionEnum estadoSolicitud, SecuenciaContratoDto secuencia) throws ConexiaBusinessException {
        Contrato contrato = new Contrato();
        try {
            final User user = this.commonsDtoToEntityControl.dtoToEntityUser(contratoDto.getUserId());
            if (contratoDto.getId() != null) {
                contrato.setId(contratoDto.getId());
            }
            contrato.setFechaCreacion(new Date());
            if (contratoDto.getSolicitudContratacionParametrizableDto().getEsOtroSi())
            {
                contrato.setFechaInicio(contratoDto.getFechaInicioOtroSi());
                contrato.setFechaFin(contratoDto.getFechaFinOtroSi());
                contrato.setFechaElaboracion(contratoDto.getFechaElaboracionContratoOtrosi());
            }else {
                contrato.setFechaInicio(contratoDto.getFechaInicioVigencia());
                contrato.setFechaFin(contratoDto.getFechaFinVigencia());
                contrato.setFechaElaboracion(contratoDto.getFechaElaboracionContrato());
            }
            contrato.setFechaInicioOtroSi(contratoDto.getFechaInicioOtroSi());
            contrato.setFechaFinOtroSi(contratoDto.getFechaFinOtroSi());
            contrato.setFechaElaboracionOtroSi(contratoDto.getFechaElaboracionContratoOtrosi());
            final NegociacionModalidadEnum negociacionModalidad = contratoDto.getSolicitudContratacionParametrizableDto()
                    .getModalidadNegociacionEnum();
            if (negociacionModalidad == null) {
                throw this.exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.CONTRATO_MODALIDAD_NEGOCIACION, "No existe la negociacion modalidad. ", contratoDto.getSolicitudContratacionParametrizableDto().getModalidadNegociacionEnum().getDescripcion());
            }
            contrato.setTipoModalidad(negociacionModalidad);
            contrato.setUserId(contratoDto.getUserId());
            contrato.setSolicitudContratacion(parametrizacionContratoValidacionControl.almacenarSolicitudContratacion(
                    contratoDto.getSolicitudContratacionParametrizableDto(), user));
            if (contrato.getSolicitudContratacion().getEstadoLegalizacion() != EstadoLegalizacionEnum.CONTRATO_SIN_VB &&
                    estadoSolicitud == EstadoLegalizacionEnum.CONTRATO_SIN_VB) {
                guardarHistorialContrato(contratoDto.getUserId(), contratoDto.getId(), "ESPERAR VoBo", contratoDto.getSolicitudContratacionParametrizableDto().getNumeroNegociacion());
            }else{
            	guardarHistorialContrato(contratoDto.getUserId(), contratoDto.getId(), estadoSolicitud.getDescripcion().toUpperCase(), contratoDto.getSolicitudContratacionParametrizableDto().getNumeroNegociacion());
            }
            contrato.getSolicitudContratacion().setEstadoLegalizacion(estadoSolicitud);
            em.merge(contrato.getSolicitudContratacion());
            if(contratoDto.getSolicitudContratacionParametrizableDto().getPoblacion() != null){
                contrato.setPoblacion(contratoDto.getSolicitudContratacionParametrizableDto().getPoblacion());
            }
            contrato.setRegional(this.commonsDtoToEntityControl.dtoToEntityRegional(contratoDto.getRegionalDto().getId()));
            contrato.setTipoSubsidiado(contratoDto.getTipoSubsidiado());
            contrato.setTipoContrato(contratoDto.getTipoContratoEnum());
            contrato.setNivelContrato(contratoDto.getNivelContrato());
            if(Objects.nonNull(contratoDto.getNombreArchivo())){
            	contrato.setNombreArchivo(contratoDto.getNombreArchivo());
            }
            if (Objects.isNull(contrato.getId())) {
            	contrato.setNumeroContrato(this.generarNumeroContratoControl.generarNumeroContrato(contratoDto, secuencia));
            	em.persist(contrato);
            } else {
            	if(!contratoDto.getRegionalDto().equals(contratoDto.getRegionalAntiguaDto())) {
                	contrato.setNumeroContrato(this.generarNumeroContratoControl.generarNumeroContrato(contratoDto, secuencia));
            		guardarHistorialRegionalContrato(contratoDto.getUserId(), contratoDto.getId(), contratoDto.getSolicitudContratacionParametrizableDto().getNumeroNegociacion()
            				, contratoDto.getRegionalAntiguaDto().getId(), contratoDto.getNumeroContrato(), contratoDto.getRegionalDto().getId(), contrato.getNumeroContrato());
            	}else {
                	contrato.setNumeroContrato(contratoDto.getNumeroContrato());
            	}
            	em.merge(contrato);
            }
            contratoDto.setNumeroContrato(contrato.getNumeroContrato());
            this.actualizarNumeroContrato(contratoDto);
        } catch (final Exception e) {
            throw this.exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.CONTRATO_CREACION,
                    "Error almacenando el contrato.", e.getMessage());
        }
        return contrato;
    }

    public List<ObjetoContractualDto> listarObjetoCapitaPorNegociacionId(Long negociacionId) {
        try {
            return obtenerServiciosDeProcedimientosYMedicamentos(negociacionId);
        } catch (Exception e) {
            log.error("Se presentó un error al obtener los servicios negociados ", e);
            return Collections.emptyList();
        }
    }

    public List<ObjetoContractualDto> listarObjetoEventoPorNegociacionId(Long negotiationId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                             ApiLocalizatorPathEnum.GET_OBJECT_EVENT_BY_NEGOTIATION_ID.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_OBJECT_EVENT_BY_NEGOTIATION_ID.getHttpMethod());
            request.setBody(negotiationId);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_NEGOTIATION_ID, negotiationId)};
            ResponseHttp<List<ObjetoContractualDto>> response = iApiRestExecution.executeRequest(request,dto)
                    .getAsResultList(ObjetoContractualDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_OBJECT_EVENT_BY_NEGOTIATION_ID);
        }
    }

    private List<ObjetoContractualDto> obtenerServiciosDeProcedimientosYMedicamentos(Long negociacionId) {
        List<ObjetoContractualDto> serviciosDeProcedimientos = listaProcedimientosObjeto(negociacionId);
        List<ObjetoContractualDto> serviciosDeMedicamentos = listaMedicamentosObjeto(negociacionId);
        if (Objects.isNull(serviciosDeProcedimientos) || Objects.isNull(serviciosDeMedicamentos)) { return null; }
            List<ObjetoContractualDto> servicios = Stream.of(serviciosDeProcedimientos, serviciosDeMedicamentos)
                .flatMap(Collection::parallelStream)
                .collect(Collectors.toList());
        obtenerModalidadObjectoEvento(negociacionId, servicios);
        obtenerMaximaComplejidad(negociacionId, servicios);
        return servicios;
    }

    private void obtenerMaximaComplejidad(Long negociacionId, List<ObjetoContractualDto> serviciosDeLosProcedimientos) {
        if (serviciosDeLosProcedimientos.isEmpty()) {
            return;
        }
        List<String> codigosServicio = serviciosDeLosProcedimientos.stream()
                .map(ObjetoContractualDto::getCodigo)
                .collect(Collectors.toList());

        List<Object[]> resultList = em.createNativeQuery("SELECT ss.codigo, " +
                "       CASE WHEN max(greatest(CASE WHEN sr.complejidad_alta = 'SI' THEN 3 END, " +
                "                             CASE WHEN sr.complejidad_media = 'SI' THEN 2 END, " +
                "                             CASE WHEN sr.complejidad_baja = 'SI' THEN 1 END, " +
                "                             snr.nivel_complejidad)) = 3 THEN 'ALTA' " +
                "           WHEN max(greatest(CASE WHEN sr.complejidad_alta = 'SI' THEN 3 END, " +
                "                             CASE WHEN sr.complejidad_media = 'SI' THEN 2 END, " +
                "                             CASE WHEN sr.complejidad_baja = 'SI' THEN 1 END, " +
                "                             snr.nivel_complejidad)) = 2 THEN 'MEDIA' " +
                "           WHEN max(greatest(CASE WHEN sr.complejidad_alta = 'SI' THEN 3 END, " +
                "                             CASE WHEN sr.complejidad_media = 'SI' THEN 2 END, " +
                "                             CASE WHEN sr.complejidad_baja = 'SI' THEN 1 END, " +
                "                             snr.nivel_complejidad)) = 1 THEN 'BAJA' " +
                "           END AS complejidad " +
                "FROM contratacion.negociacion n " +
                "         INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id " +
                "         INNER JOIN contratacion.sede_prestador sp ON sn.sede_prestador_id = sp.id " +
                "         INNER JOIN contratacion.servicio_salud ss ON ss.codigo in(:codigos) " +
                "         INNER JOIN contratacion.macroservicio M ON M.id = ss.macroservicio_id " +
                "         LEFT JOIN maestros.servicios_reps sr ON sr.codigo_habilitacion = sp.codigo_habilitacion AND " +
                "                                                 sr.numero_sede = CAST(sp.codigo_sede AS INT) AND " +
                "                                                 sr.servicio_codigo = CAST(ss.codigo AS INT) " +
                "         LEFT JOIN maestros.servicios_no_reps snr ON snr.sede_prestador_id = sp.id AND snr.servicio_id = ss.id " +
                "WHERE 1 = CASE WHEN sr.id IS NOT NULL THEN 1 WHEN snr.id IS NOT NULL THEN 1 END " +
                "  AND n.id = :negociacionId GROUP BY ss.codigo ")
                .setParameter("codigos", codigosServicio)
                .setParameter("negociacionId", negociacionId)
                .getResultList();

        resultList.forEach(o -> serviciosDeLosProcedimientos.stream().filter(objetoContractualDto -> Objects.equals(o[0], objetoContractualDto.getCodigo()))
        .forEach(objetoContractualDto -> objetoContractualDto.setComplejidad(String.valueOf(o[1]))));

    }

    private void obtenerModalidadObjectoEvento(Long negociacionId, List<ObjetoContractualDto> serviciosDeLosProcedimientos) {
        if (serviciosDeLosProcedimientos.isEmpty()) {
            return;
        }
        List<String> codigosServicio = serviciosDeLosProcedimientos.stream()
                .map(ObjetoContractualDto::getCodigo)
                .collect(Collectors.toList());

        List<ServicioReps> resultList = em.createQuery("select sr " +
                "from Negociacion n, ServicioSalud ss, ServicioReps sr " +
                "inner join n.sedesNegociacion sn inner join sn.sedePrestador sp " +
                "where ss.codigo in(:codigos) and n.id = :negociacionId and sr.servicioCodigo = cast(ss.codigo as int) " +
                "and sr.codigoHabilitacion = sp.codigoHabilitacion and sr.numeroSede = cast(sp.codigoSede as int) ", ServicioReps.class)
                .setParameter("codigos", codigosServicio)
                .setParameter("negociacionId", negociacionId)
                .getResultList();

        Map<Integer, List<ServicioReps>> collect = resultList.stream()
                .collect(Collectors.groupingBy(ServicioReps::getServicioCodigo));

        collect.forEach((codigoServicio, servicioReps) -> {
            if (servicioReps.stream().allMatch(servicioReps1 -> Objects.equals("SI", servicioReps1.getAmbulatorio()))) {
                serviciosDeLosProcedimientos.stream().filter(objetoContractualDto -> Objects.equals(objetoContractualDto.getCodigo(), String.valueOf(codigoServicio)))
                        .forEach(objetoContractualDto -> objetoContractualDto.setModalidad("INTRAMURAL AMBULATORIO"));
            }
            if (servicioReps.stream().allMatch(servicioReps1 -> Objects.equals("SI", servicioReps1.getHabilitado()))) {
                serviciosDeLosProcedimientos.stream().filter(objetoContractualDto -> Objects.equals(objetoContractualDto.getCodigo(), String.valueOf(codigoServicio)))
                        .forEach(objetoContractualDto -> objetoContractualDto.setModalidad("INTRAMURAL HOSPITALARIO"));
            }
            if (servicioReps.stream().allMatch(servicioReps1 -> Objects.equals("SI", servicioReps1.getUnidadMovil()))) {
                serviciosDeLosProcedimientos.stream().filter(objetoContractualDto -> Objects.equals(objetoContractualDto.getCodigo(), String.valueOf(codigoServicio)))
                        .forEach(objetoContractualDto -> objetoContractualDto.setModalidad("EXTRAMURAL UNIDAD MOVIL"));
            }
            if (servicioReps.stream().allMatch(servicioReps1 -> Objects.equals("SI", servicioReps1.getDomiciliario()))) {
                serviciosDeLosProcedimientos.stream().filter(objetoContractualDto -> Objects.equals(objetoContractualDto.getCodigo(), String.valueOf(codigoServicio)))
                        .forEach(objetoContractualDto -> objetoContractualDto.setModalidad("EXTRAMURAL DOMICILIARIO"));
            }
            if (servicioReps.stream().allMatch(servicioReps1 -> Objects.equals("SI", servicioReps1.getOtrasEstramural()))) {
                serviciosDeLosProcedimientos.stream().filter(objetoContractualDto -> Objects.equals(objetoContractualDto.getCodigo(), String.valueOf(codigoServicio)))
                        .forEach(objetoContractualDto -> objetoContractualDto.setModalidad("EXTRAMURAL OTRAS"));
            }
            if (servicioReps.stream().allMatch(servicioReps1 -> Objects.equals("SI", servicioReps1.getCentroReferencia()))) {
                serviciosDeLosProcedimientos.stream().filter(objetoContractualDto -> Objects.equals(objetoContractualDto.getCodigo(), String.valueOf(codigoServicio)))
                        .forEach(objetoContractualDto -> objetoContractualDto.setModalidad("TELEMEDICINA CENTRO REF"));
            }
            if (servicioReps.stream().allMatch(servicioReps1 -> Objects.equals("SI", servicioReps1.getInstitucionRemisora()))) {
                serviciosDeLosProcedimientos.stream().filter(objetoContractualDto -> Objects.equals(objetoContractualDto.getCodigo(), String.valueOf(codigoServicio)))
                        .forEach(objetoContractualDto -> objetoContractualDto.setModalidad("TELEMEDICINA INSTITUCION REMISORA"));
            }
        });
    }

    private List<ObjetoContractualDto> listaProcedimientosObjeto(Long negociacionId) {
        return em.createQuery("select new com.conexia.contratacion.commons.dto.negociacion.ObjetoContractualDto(m.nombre, ss.codigo, upper(ss.nombre), t.descripcion, case when sns.poblacion = 0 then 0 else n.poblacion end, sns.porcentajeNegociado, sns.valorNegociado) from Negociacion n " +
                "inner join n.sedesNegociacion sn inner join sn.sedeNegociacionServicios sns " +
                "inner join sns.servicioSalud ss inner join ss.macroServicio m left outer join sns.tarifarioNegociado t " +
                "where n.id = :negociacionId " +
                "group by m.nombre, ss.codigo, ss.nombre, t.descripcion, sns.poblacion, n.poblacion, sns.porcentajeNegociado, sns.valorNegociado", ObjetoContractualDto.class)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    private List<ObjetoContractualDto> listaMedicamentosObjeto(Long negociacionId) {
        return em.createQuery("select new com.conexia.contratacion.commons.dto.negociacion.ObjetoContractualDto(m.nombre, ss.codigo, upper(ss.nombre), case when n.poblacion = 0 then 0 else n.poblacion end) from Negociacion n, ServicioSalud ss " +
                "inner join n.sedesNegociacion sn inner join ss.macroServicio m " +
                "where n.id = :negociacionId and ss.codigo = :codigo and exists(" +
                "select sns.id from SedeNegociacionMedicamento sns where sns.sedeNegociacion.id = sn.id" +
                ") " +
                "group by m.nombre, ss.codigo, ss.nombre, n.poblacion", ObjetoContractualDto.class)
                .setParameter("negociacionId", negociacionId)
                .setParameter("codigo", "714")
                .getResultList();
    }

	private List<ObjetoContractualDto> listaPaquetesObjeto(Long negociacionId) {
        List<ObjetoContractualDto> serviciosDeLosPaquetes = obtenerServiciosDeLosPaquetes(negociacionId);
        List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitados = obtenerServiciosHabilitados(negociacionId);

        List<ObjetoContractualDto> serviciosIguales = serviciosDeLosPaquetes.stream()
                .filter(servicioPaquete -> serviciosHabilitados.stream()
                        .anyMatch(servicioHabilitado -> Objects.equals(servicioHabilitado.getCodigoServicio(), servicioPaquete.getCodigo())))
                .collect(Collectors.toList());

        obtenerModalidadObjectoEvento(negociacionId, serviciosIguales);
        obtenerMaximaComplejidad(negociacionId, serviciosIguales);
        return serviciosIguales;
    }

    private List<ObjetoContractualDto> obtenerServiciosDeLosPaquetes(Long negociacionId) {
        String sqlPaquete = "SELECT m.nombre                                                 AS grupo, " +
                "       ss.codigo                                                AS id, " +
                "       UPPER(ss.nombre)                                         AS nombre, " +
                "       NULL                                                     AS complejidad, " +
                "       NULL                                                     AS modalidad, " +
                "       'TARIFA PROPIA'                                          AS tarifario, " +
                "       CASE WHEN n.poblacion = 0 THEN NULL ELSE n.poblacion END AS poblacion, " +
                "       0                                                        AS porcentaje_negociado, " +
                "       0                                                        AS valor_negociado " +
                "FROM contratacion.negociacion n " +
                "         INNER JOIN contratacion.sedes_negociacion sn ON n.id = sn.negociacion_id " +
                "         INNER JOIN contratacion.sede_prestador sp ON sn.sede_prestador_id = sp.id " +
                "         INNER JOIN contratacion.sede_negociacion_paquete snp ON snp.sede_negociacion_id = sn.id " +
                "         INNER JOIN contratacion.paquete_portafolio pp ON snp.paquete_id = pp.portafolio_id " +
                "         INNER JOIN maestros.procedimiento_servicio ps ON pp.codigo = ps.codigo_cliente and ps.estado = 1" +
                "         INNER JOIN contratacion.servicio_salud ss ON ps.servicio_id = ss.id " +
                "         INNER JOIN contratacion.macroservicio m ON m.id = ss.macroservicio_id " +
                "WHERE sn.negociacion_id = :negociacionId " +
                "GROUP BY m.nombre, ss.codigo, ss.nombre, n.poblacion";
        return em.createNativeQuery(sqlPaquete, "Negociacion.ObjetoContractualEventoMapping")
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    private List<ServiciosHabilitadosRespNoRepsDto> obtenerServiciosHabilitados(Long negociacionId) {
        String sqlReps = "SELECT sn.id                                                                             AS sede_prestador_id, " +
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
                "         JOIN contratacion.negociacion n ON n.id = :negociacionId " +
                "         JOIN contratacion.sedes_negociacion sn ON n.id = sn.negociacion_id " +
                "         JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id " +
                "         LEFT JOIN maestros.servicios_reps sr ON sr.codigo_habilitacion = sp.codigo_habilitacion AND " +
                "                                                 sr.numero_sede = cast(sp.codigo_sede AS int) AND " +
                "                                                 sr.servicio_codigo = cast(ss.codigo AS int) " +
                "         LEFT JOIN maestros.servicios_no_reps snr ON snr.sede_prestador_id = sp.id AND snr.servicio_id = ss.id " +
                "WHERE 1 = CASE WHEN sr.id IS NOT NULL THEN 1 WHEN snr.id IS NOT NULL THEN 1 END";
        List<Object[]> resultados = em.createNativeQuery(sqlReps)
                .setParameter("negociacionId", negociacionId)
                .getResultList();

        return resultados.stream()
                .map((serviciosReps) -> {
                    ServiciosHabilitadosRespNoRepsDto serviciosHabilitadosDto = new ServiciosHabilitadosRespNoRepsDto();
                    serviciosHabilitadosDto.setSedeNegociacionId(new Long((Integer) serviciosReps[0]));
                    serviciosHabilitadosDto.setServicioId(new Long((Integer) serviciosReps[1]));
                    serviciosHabilitadosDto.setCodigoServicio((String) serviciosReps[2]);
                    serviciosHabilitadosDto.setNivelComplejidad((Integer) serviciosReps[3]);
                    serviciosHabilitadosDto.setEstadoServicio((Boolean) serviciosReps[4]);
                    serviciosHabilitadosDto.setCodigohabilitacion((String) serviciosReps[5]);
                    serviciosHabilitadosDto.setCodigoSede((String) serviciosReps[6]);
                    serviciosHabilitadosDto.setNivelComplejidadMinimo((Integer) serviciosReps[7]);
                    return serviciosHabilitadosDto;
                })
                .collect(Collectors.toList());
    }

  	public List<ObjetoContractualDto> listarObjetoPgpPorNegociacionId(Long negociacionId){
    	return em.createNativeQuery("SELECT upper(r.descripcion) nombre_ria, ss.codigo, ss.nombre"
    			+ " FROM contratacion.sedes_negociacion sn "
    			+ " JOIN contratacion.negociacion_ria rn on rn.negociacion_id = sn.negociacion_id"
    			+ " JOIN maestros.ria r on r.id = rn.ria_id"
    			+ " JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id"
    			+ " JOIN contratacion.servicio_salud ss on ss.id = sns.servicio_id"
    			+ " WHERE sn.negociacion_id = :negociacionId "
    			+ " ORDER BY r.descripcion, ss.codigo ", "Negociacion.ObjetoContractualPgpMapping")
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

	public List<ObjetoContractualDto> listarObjetoPgpSinRiaPorNegociacionId(Long negociacionId){

    	StringBuilder query = new StringBuilder();
    	query.append(" SELECT ")
    	.append(" distinct on (ss.codigo, ss.nombre)")
    	.append("  ss.codigo as codigo, ")
    	.append("  mss.nombre as grupo,  ")
    	.append("  ss.nombre as nombre, ")
    	.append("  CASE WHEN gs.complejidad_alta is true THEN 'ALTA'")
    	.append(" 		 WHEN gs.complejidad_baja is true THEN 'MEDIA'")
    	.append(" 		 ELSE 'BAJA' end complejidad, ")
    	.append("  CASE WHEN coalesce(gs.intramural_ambulatorio, false) IS TRUE THEN 'INTRAMURAL AMBULATORIO' ")
    	.append(" 		 WHEN coalesce(gs.intramural_hospitalario, false) IS TRUE THEN 'INTRAMURAL HOSPITALARIO'")
    	.append(" 		 WHEN coalesce(gs.extramural_unidad_movil, false) IS TRUE THEN 'EXTRAMURAL UNIDAD MOVIL'")
    	.append(" 	     WHEN coalesce(gs.extramural_domiciliario, false) IS TRUE THEN 'EXTRAMURAL DOMICILIARIO'")
    	.append(" 		 WHEN coalesce(gs.extramural_otras, false) IS TRUE THEN 'EXTRAMURAL OTRAS'")
    	.append(" 		 WHEN coalesce(gs.telemedicina_centro_ref, false) IS TRUE THEN 'TELEMEDICINA CENTRO REF'")
    	.append(" 		 WHEN coalesce(gs.telemedicina_institucion_remisora, false) IS TRUE THEN 'TELEMEDICINA INSTITUCION REMISORA'")
    	.append("  END AS modalidad,")
    	.append("  n.poblacion")
    	.append(" from contratacion.negociacion n")
    	.append(" join contratacion.prestador pres on pres.id = n.prestador_id ")
    	.append(" join contratacion.sedes_negociacion sn on sn.negociacion_id = n.id ")
    	.append(" join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id")
    	.append(" left join contratacion.sede_negociacion_capitulo snc on snc.sede_negociacion_id = sn.id")
    	.append(" left join contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_capitulo_id = snc.id")
    	.append(" inner join (")
    	.append(" 				select")
    	.append(" 				px.id as procedimientoId,")
    	.append("     		    ss.id as servicioSaludId")
    	.append(" 				from maestros.procedimiento px")
    	.append(" 				join maestros.procedimiento_servicio ps on ps.procedimiento_id = px.id")
    	.append(" 				join contratacion.servicio_salud ss on ss.id = ps.servicio_id")
    	.append(" 				join contratacion.grupo_servicio gs on gs.servicio_salud_id = ss.id")
    	.append(" 				join contratacion.sede_prestador sp on sp.portafolio_id = gs.portafolio_id")
    	.append(" 				join contratacion.prestador pe on pe.id = sp.prestador_id")
    	.append(" 				join maestros.servicios_reps sr on sr.nits_nit = pe.numero_documento")
    	.append(" 				join contratacion.negociacion n on n.id = :negociacionId")
    	.append(" 				where sp.prestador_id = n.prestador_id")
    	.append(" 				and sr.numero_sede = cast(sp.codigo_sede as integer)")
    	.append(" 				group by 1,2")
    	.append(" ) as habilitacion on habilitacion.procedimientoId = snp.pto_id")
    	.append(" LEFT join contratacion.servicio_salud ss on ss.id = habilitacion.servicioSaludId ")
    	.append(" left join contratacion.macroservicio mss on mss.id = ss.macroservicio_id")
    	.append(" LEFT join contratacion.grupo_servicio gs on gs.portafolio_id = sp.portafolio_id and gs.servicio_salud_id = ss.id")
    	.append(" where n.id = :negociacionId")
    	.append(" group by 1,2,3,4,5,6 ");

    	return em.createNativeQuery(
    			query.toString(), "Negociacion.ObjetoContratoPgpMapping")
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    public List<MunicipioDto> getChoosedTownByNegotiationId(Long negotiationId)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_CHOOSED_TOWN_BY_NEGOTIATION_ID.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_CHOOSED_TOWN_BY_NEGOTIATION_ID.getHttpMethod());
            request.setBody(negotiationId);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_NEGOTIATION_ID, negotiationId)};
            ResponseHttp<List<MunicipioDto>> response = iApiRestExecution.executeRequest(request, dto)
                    .getAsResultList(MunicipioDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_CHOOSED_TOWN_BY_NEGOTIATION_ID);
        }
    }

	public List<ObjetoContractualDto> listarObjetoRiaCapiataPorNegociacionId(Long negociacionId){
    	return em.createNativeQuery(""
    			+ "SELECT DISTINCT rp.id as rango_poblacion_id, CONCAT(upper(r.descripcion), '-', upper(rp.descripcion)) as ruta, a.descripcion as tema, ss.codigo, ss.nombre, "
    			+ "CASE msps.complejidad 	WHEN 1 THEN 'BAJA' WHEN 2 THEN 'MEDIA' WHEN 3 THEN 'ALTA' END AS complejidad, "
    			+ "CASE WHEN coalesce(msps.intramural_ambulatorio, false) IS TRUE THEN 'INTRAMURAL AMBULATORIO' "
    			+ "		WHEN coalesce(msps.intramural_hospitalario, false) IS TRUE THEN 'INTRAMURAL HOSPITALARIO' "
    			+ "		WHEN coalesce(msps.extramural_unidad_movil, false) IS TRUE THEN 'EXTRAMURAL UNIDAD MOVIL'"
    			+ "		WHEN coalesce(msps.extramural_domiciliario, false) IS TRUE THEN 'EXTRAMURAL DOMICILIARIO'"
    			+ "		WHEN coalesce(msps.extramural_otras, false) IS TRUE THEN 'EXTRAMURAL OTRAS'"
    			+ "		WHEN coalesce(msps.telemedicina_centro_ref, false) IS TRUE THEN 'TELEMEDICINA CENTRO REF'"
    			+ "		WHEN coalesce(msps.telemedicina_institucion_remisora, false) IS TRUE THEN 'TELEMEDICINA INSTITUCION REMISORA' "
    			+ "		END AS modalidad,"
    			+ "nrp.poblacion, snp.porcentaje_negociado "
    			+ "FROM contratacion.sede_negociacion_procedimiento snp "
    			+ "JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id "
    			+ "JOIN contratacion.negociacion_ria_rango_poblacion nrp ON snp.negociacion_ria_rango_poblacion_id = nrp.id "
    			+ "JOIN contratacion.negociacion_ria nr ON nrp.negociacion_ria_id = nr.id "
    			+ "JOIN contratacion.servicio_salud ss ON sns.servicio_id = ss.id "
    			+ "JOIN contratacion.sedes_negociacion sn ON sns.sede_negociacion_id = sn.id "
    			+ "JOIN contratacion.sede_prestador sp ON sn.sede_prestador_id = sp.id "
    			+ "JOIN contratacion.macroservicio msrv ON msrv.id = ss.macroservicio_id "
    			+ "JOIN contratacion.mod_servicio_portafolio_sede msps on msps.servicio_salud_id = sns.servicio_id "
    			+ "JOIN contratacion.mod_portafolio_sede mps on mps.id = msps.portafolio_sede_id and  mps.prestador_id = sp.prestador_id "
    			+ "JOIN maestros.ria r ON nr.ria_id = r.id "
    			+ "JOIN maestros.rango_poblacion rp ON nrp.rango_poblacion_id = rp.id "
    			+ "JOIN maestros.actividad a ON snp.actividad_id = a.id "
    			+ "JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id "
    			+ "WHERE nr.negociacion_id = :negociacionId "
    			+ "GROUP BY 1,2,3,4,5,6,7,8,9 "
    			+ "ORDER BY 1,3,4 " , "Negociacion.ObjetoContratoRiaCapitaMapping")
    			.setParameter("negociacionId", negociacionId)
    			.getResultList();
    }

    public List<ObjetoContractualDto> listarObjetoRiaCapitaResumidoPorNegociacionId(Long negociacionId){
        List<NegociacionRiaRangoPoblacionDto> listaNegociacionRiaRangoPoblacion = em.createNamedQuery("NegociacionRiaRangoPoblacion.findRiasByNegociacionId")
                .setParameter("negociacionId", negociacionId)
                .getResultList();
        List<ObjetoContractualDto> objetoContractual = new ArrayList<>();

        listaNegociacionRiaRangoPoblacion.stream()
                .sorted(Comparator.comparing(obj -> obj.getRangoPoblacion().getId()))
                .sorted(Comparator.comparing(obj -> obj.getNegociacionRia().getRia().getId()))
                .forEach(obj -> {
                    ObjetoContractualDto dto = new ObjetoContractualDto();
                    dto.setNombreRia(obj.getNegociacionRia().getRia().getDescripcion().concat(" - ").concat(obj.getRangoPoblacion().getDescripcion()));
                    dto.setPorcentajeNegociado(BigDecimal.valueOf(obj.getPesoPorcentualNegociado()));dto.setPoblacion(obj.getPoblacion());
                    dto.setRiaId(obj.getNegociacionRia().getRia().getId());
                    dto.setValorTotal(BigDecimal.valueOf(obj.getValorTotal()));
                    objetoContractual.add(dto);
                });
        return objetoContractual;
    }

    public void actualizarNumeroContrato(ContratoDto contrato){
    	em.createNamedQuery("Contrato.updateNumeroContrato")
    			.setParameter("numeroContrato", contrato.getNumeroContrato())
    			.setParameter("solicitudContratacion", contrato.getSolicitudContratacionParametrizableDto().getIdSolicitudContratacion())
    			.executeUpdate();

    }

    public void guardarHistorialContrato(Integer userId, Long contratoId, String evento, Long negociacionId) {
        HistorialCambios nuevoHistorial = new HistorialCambios();
        nuevoHistorial.setEvento(evento + " CONTRATO");
        nuevoHistorial.setObjeto("CONTRATO " + contratoId);
        nuevoHistorial.setUserId(userId);
        nuevoHistorial.setContratoId(contratoId);
        nuevoHistorial.setNegociacionId(negociacionId);
        em.persist(nuevoHistorial);
    }

    public void guardarHistorialRegionalContrato(Integer userId, Long contratoId, Long negociacionId, Integer regionalAntiguaId, String numeroContratoAntiguo, Integer regionalNuevaId, String numeroContratoNuevo) {
        HistorialCambios nuevoHistorial = new HistorialCambios();
        nuevoHistorial.setEvento("CAMBIO REGIONAL CONTRATO");
        nuevoHistorial.setObjeto("CONTRATO " + contratoId);
        nuevoHistorial.setUserId(userId);
        nuevoHistorial.setContratoId(contratoId);
        nuevoHistorial.setNegociacionId(negociacionId);
        nuevoHistorial.setRegionalAntiguaId(regionalAntiguaId);
        nuevoHistorial.setNumeroContratoAntiguo(numeroContratoAntiguo);
        nuevoHistorial.setRegionalNuevaId(regionalNuevaId);
        nuevoHistorial.setNumeroContratoNuevo(numeroContratoNuevo);
        em.persist(nuevoHistorial);
    }

    /* Llammado API */

    public LegalizacionContratoDto buscarLegalizacionContrato(SolicitudContratacionParametrizableDto solicitud)
            throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_LEGALIZATION_CONTRACT.getContext();
            RequestHttp<GetLegalizationContractRequest> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_LEGALIZATION_CONTRACT.getHttpMethod());
            request.setBody(GetLegalizationContractRequestUtil.resetRequest(solicitud));
            ResponseHttp<LegalizacionContratoDto> response = iApiRestExecution.executeRequest(request)
                                                                .getAsUniqueResult(LegalizacionContratoDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.CONSULTA_LEGALIZACION_CONTRATO);
        }
    }


    public NegociacionDto consultarFechaOtroSi(Long negotiationId) throws ConexiaBusinessException
    {
        try
        {
            String urlPath = contratacionWsUtil.getUrlContratacionWs() +
                    ApiLocalizatorPathEnum.GET_OTROSI_CURRENT_DATE.getContext();
            RequestHttp<Long> request = iApiRestExecution.createRequestTemplateCustom(
                    urlPath, ApiLocalizatorPathEnum.GET_OTROSI_CURRENT_DATE.getHttpMethod());
            request.setBody(negotiationId);
            Parametro [] dto = new Parametro[]{new Parametro(PARAM_NEGOTIATION_ID, negotiationId)};
            ResponseHttp<NegociacionDto> response = iApiRestExecution.executeRequest(request, dto)
                    .getAsUniqueResult(NegociacionDto.class);
            validarConsumo(response);
            return response.getResponseCustom();
        } catch (Exception ex) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GET_OTROSI_CURRENT_DATE);
        }
    }
}
