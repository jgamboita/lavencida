package com.conexia.contractual.services.view.solicitud.boundary;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import com.conexia.contractual.definitions.view.solicitud.ConsultaSolicitudesContratacionViewRemote;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacion;
import com.conexia.contractual.services.view.solicitud.control.ConsultaSolicitudesContratacionControl;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroConsultaSolicitudDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 * EJB para visualizar las solicitudes de contratacion a parametrizar.
 *
 * @author jalvarado
 */
@Stateless
@Remote(ConsultaSolicitudesContratacionViewRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ConsultaSolicitudesContratacionViewBoundary implements ConsultaSolicitudesContratacionViewRemote {

    /**
     * Contexto de Persistencia.
     */
    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;

    /**
     * Control de consulta de solicitudes de contratacion.
     */
    @Inject
    private ConsultaSolicitudesContratacionControl control;


    @Override
    public List<SolicitudContratacionParametrizableDto> listarSolicitudesPorParametrizar(FiltroConsultaSolicitudDto filtroDto,
                                                                                         NegociacionDto dtoNeg)
            throws ConexiaBusinessException
    {
        return control.listarSolicitudesPorParametrizar(filtroDto,dtoNeg);

    	/*StringBuilder query  = new StringBuilder();
    	query.append("SELECT DISTINCT n.id as negociacion_id, c.numero_contrato,sp.nombre_sede as razon_social,p.numero_documento, c.tipo_contrato, upper(r.descripcion) as regional, "
    			+ "n.tipo_modalidad_negociacion, n.regimen, c.tipo_subsidiado,  "
    			+ "c.fecha_inicio, c.fecha_fin, c.nivel_contrato, upper(m.nombre) as minuta,n.poblacion,  "
    			+ "CONCAT(u.primer_nombre,' ',u.primer_apellido) as responsable_creacion, sc.estado_legalizacion_id as estado_legalizacion, "
    			+ "lc.fecha_vo_bo as fecha_legalizacion,CASE WHEN c.fecha_fin >= now()  THEN 'VIGENTE' ELSE 'VENCIDO' END as estado_contrato, "
    			+ "CAST(n.fecha_creacion AS date) as fecha_negociacion, n.estado_negociacion,sc.estado_parametrizacion_id, CAST(COUNT(sn.id) as integer) as numero_sedes, "
    			+ "sc.id as solicitudId, p.id as prestador_id, tp.codigo as codigo_identificacion, p.codigo_prestador, "
    			+ "p.prefijo, tip.descripcion as tipo_prestador, p.naturaleza_juridica, cl.descripcion as clase_prestador, "
    			+ "cla.descripcion as clasificacion_prestador, p.nivel_atencion, p.tipo_identificacion_representante_id,p.numero_documento_representante, "
    			+ "p.representante_legal,p.correo_electronico,p.empresa_social_estado, n.nuevo_contrato,  c.id as contrato_id, c.nombre_archivo, c.nombre_original_archivo "
    			+ "FROM  contratacion.negociacion n "
    			+ "JOIN security.user u on n.user_id = u.id "
    			+ "JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = n.id and sn.principal = true "
    			+ "JOIN contratacion.sede_prestador sp on sn.sede_prestador_id = sp.id "
    			+ "JOIN contratacion.prestador p on sp.prestador_id = p.id "
    			+ "JOIN contratacion.tipo_prestador tip on p.tipo_prestador_id = tip.id "
    			+ "JOIN contratacion.clase_prestador cl on p.clase_prestador = cl.id "
    			+ "JOIN contratacion.clasificacion_prestador cla on p.clasificacion_id = cla.id  "
    			+ "JOIN maestros.tipo_identificacion tp on p.tipo_identificacion_id = tp.id "
    			+ "JOIN maestros.tipo_identificacion tp2 on p.tipo_identificacion_representante_id = tp2.id "
    			+ "JOIN contratacion.solicitud_contratacion sc on sc.negociacion_id = sn.negociacion_id  "
    			+ "LEFT JOIN contratacion.contrato c on c.solicitud_contratacion_id = sc.id "
    			+ "LEFT JOIN contratacion.legalizacion_contrato lc on lc.contrato_id = c.id "
    			+ "LEFT JOIN contratacion.minuta m on lc.minuta_id = m.id "
    			+ "LEFT JOIN maestros.regional r on c.regional_id = r.id "
    			+ "WHERE n.estado_negociacion = :estadoNegociacion "
                        + "and n.deleted is false " +
				"  and (c.deleted is false or c.deleted is null)");//
        final Map<String, Object> params = control.completarWhereConsultaSolicitudes(query, filtroDto);
    	query.append("  GROUP BY n.id, c.numero_contrato,sp.nombre_sede,p.numero_documento, c.tipo_contrato, r.descripcion, "
    			+ "n.tipo_modalidad_negociacion, n.regimen, c.tipo_subsidiado, "
    			+ "c.fecha_inicio, c.fecha_fin, c.nivel_contrato, m.nombre,n.poblacion, u.primer_nombre, u.primer_apellido, "
    			+ "sc.estado_legalizacion_id,lc.fecha_vo_bo ,n.fecha_creacion ,n.estado_negociacion,sc.estado_parametrizacion_id, "
    			+ "sc.id, p.id, tp.codigo, p.codigo_prestador, "
    			+ "p.prefijo, tip.descripcion, p.naturaleza_juridica, cl.descripcion , "
    			+ "cla.descripcion , p.nivel_atencion, p.tipo_identificacion_representante_id,p.numero_documento_representante,p.representante_legal, "
    			+ "p.correo_electronico,p.empresa_social_estado, n.nuevo_contrato,  c.id, c.nombre_archivo, c.nombre_original_archivo "
    			+ "ORDER BY c.fecha_inicio DESC ");
        Query queryFinal = em.createNativeQuery(query.toString(), "SolicitudContratacion.bandejaSolicitudesContratoMappging");
        queryFinal.setParameter("estadoNegociacion", EstadoNegociacionEnum.FINALIZADA.toString());
        params.keySet().stream().filter((param) -> (params.get(param) != null)).forEach((param) -> {
        	queryFinal.setParameter(param, params.get(param));
        });
        return queryFinal.setMaxResults(filtroDto.getCantidadRegistros())
                .setFirstResult(filtroDto.getPagina())
                .getResultList();*/

    }

    public List<SolicitudContratacionParametrizableDto> listarContratos(FiltroConsultaSolicitudDto filtroDto) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT n.id                                                             AS negociacion_id, " +
                "       c.numero_contrato                                                AS numero_contrato, " +
                "       sp.nombre_sede                                                   AS razon_social, " +
                "       p.numero_documento                                               AS numero_documento, " +
                "       c.tipo_contrato                                                  AS tipo_contrato, " +
                "       upper(r.descripcion)                                             AS regional, " +
                "       n.tipo_modalidad_negociacion                                     AS tipo_modalidad_negociacion, " +
                "       n.regimen                                                        AS regimen, " +
                "       c.tipo_subsidiado                                                AS tipo_subsidiado, " +
                "       c.fecha_inicio                                                   AS fecha_inicio, " +
                "       c.fecha_fin                                                      AS fecha_fin, " +
                "       c.nivel_contrato                                                 AS nivel_contrato, " +
                "       upper(m.nombre)                                                  AS minuta, " +
                "       n.poblacion                                                      AS poblacion, " +
                "       CONCAT(u.primer_nombre, ' ', u.primer_apellido)                  AS responsable_creacion, " +
                "       sc.estado_legalizacion_id                                        AS estado_legalizacion, " +
                "       lc.fecha_vo_bo                                                   AS fecha_legalizacion, " +
                "       CASE WHEN c.fecha_fin >= now() THEN 'VIGENTE' ELSE 'VENCIDO' END AS estado_contrato, " +
                "       CAST(n.fecha_creacion AS date)                                   AS fecha_negociacion, " +
                "       n.estado_negociacion                                             AS estado_negociacion, " +
                "       sc.estado_parametrizacion_id                                     AS estado_parametrizacion_id, " +
                "       CAST(COUNT(sn.id) AS integer)                                    AS numero_sedes, " +
                "       sc.id                                                            AS solicitudId, " +
                "       p.id                                                             AS prestador_id, " +
                "       tp.codigo                                                        AS codigo_identificacion, " +
                "       p.codigo_prestador                                               AS codigo_prestador, " +
                "       p.prefijo                                                        AS prefijo, " +
                "       tip.descripcion                                                  AS tipo_prestador, " +
                "       p.naturaleza_juridica                                            AS naturaleza_juridica, " +
                "       cl.descripcion                                                   AS clase_prestador, " +
                "       cla.descripcion                                                  AS clasificacion_prestador, " +
                "       p.nivel_atencion                                                 AS nivel_atencion, " +
                "       p.tipo_identificacion_representante_id                           AS tipo_identificacion_representante_id, " +
                "       p.numero_documento_representante                                 AS numero_documento_representante, " +
                "       p.representante_legal                                            AS representante_legal, " +
                "       p.correo_electronico                                             AS correo_electronico, " +
                "       p.empresa_social_estado                                          AS empresa_social_estado, " +
                "       n.nuevo_contrato                                                 AS nuevo_contrato, " +
                "       c.id                                                             AS contrato_id, " +
                "       c.nombre_archivo                                                 AS nombre_archivo, " +
                "       c.nombre_original_archivo                                        AS nombre_original_archivo " +
                "FROM contratacion.negociacion n " +
                "         JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id AND sn.principal = TRUE " +
                "         JOIN contratacion.sede_prestador sp ON sn.sede_prestador_id = sp.id " +
                "         JOIN contratacion.prestador p ON sp.prestador_id = p.id " +
                "         JOIN contratacion.tipo_prestador tip ON p.tipo_prestador_id = tip.id " +
                "         JOIN contratacion.clase_prestador cl ON p.clase_prestador = cl.id " +
                "         JOIN contratacion.clasificacion_prestador cla ON p.clasificacion_id = cla.id " +
                "         JOIN maestros.tipo_identificacion tp ON p.tipo_identificacion_id = tp.id " +
                "         JOIN maestros.tipo_identificacion tp2 ON p.tipo_identificacion_representante_id = tp2.id " +
                "         JOIN security.user u ON n.user_id = u.id " +
                "         JOIN contratacion.solicitud_contratacion sc ON sc.negociacion_id = sn.negociacion_id " +
                "         LEFT JOIN contratacion.contrato c ON c.solicitud_contratacion_id = sc.id " +
                "         LEFT JOIN contratacion.legalizacion_contrato lc ON lc.contrato_id = c.id " +
                "         LEFT JOIN contratacion.minuta m ON lc.minuta_id = m.id " +
                "         LEFT JOIN maestros.regional r ON c.regional_id = r.id " +
                "WHERE c.deleted = FALSE ");//
        final Map<String, Object> params = control.completarWhereConsultaSolicitudes(query, filtroDto);
        query.append(" GROUP BY n.id, c.numero_contrato, sp.nombre_sede, p.numero_documento, c.tipo_contrato, r.descripcion, " +
                "         n.tipo_modalidad_negociacion, n.regimen, c.tipo_subsidiado, c.fecha_inicio, c.fecha_fin, c.nivel_contrato, " +
                "         m.nombre, n.poblacion, u.primer_nombre, u.primer_apellido, sc.estado_legalizacion_id, " +
                "         lc.fecha_vo_bo, c.fecha_fin, n.fecha_creacion, n.estado_negociacion, sc.estado_parametrizacion_id, " +
                "         sc.id, p.id, tp.codigo, p.codigo_prestador, p.prefijo, tip.descripcion, p.naturaleza_juridica, cl.descripcion, " +
                "         cla.descripcion, p.nivel_atencion, p.tipo_identificacion_representante_id, p.numero_documento_representante, " +
                "         p.representante_legal, p.correo_electronico, p.empresa_social_estado, n.nuevo_contrato, c.id, c.nombre_archivo, " +
                "         c.nombre_original_archivo " +
                "ORDER BY c.fecha_inicio DESC");
        Query queryFinal = em.createNativeQuery(query.toString(), "SolicitudContratacion.bandejaSolicitudesContratoMappging");
        params.keySet().stream().filter((param) -> (params.get(param) != null)).forEach((param) -> queryFinal.setParameter(param, params.get(param)));
        return queryFinal.setMaxResults(filtroDto.getCantidadRegistros())
                .setFirstResult(filtroDto.getPagina())
                .getResultList();
    }
    public Long contarContratos(FiltroConsultaSolicitudDto filtroConsultaSolicitudDto) {
            final StringBuilder ql = new StringBuilder("SELECT count(sc.id) " +
                    "FROM contratacion.negociacion n " +
                    "         INNER JOIN contratacion.sedes_negociacion sn ON sn.negociacion_id = n.id AND sn.principal = TRUE " +
                    "         INNER JOIN contratacion.sede_prestador sp ON sn.sede_prestador_id = sp.id " +
                    "         INNER JOIN contratacion.prestador p ON sp.prestador_id = p.id " +
                    "         INNER JOIN contratacion.tipo_prestador tip ON p.tipo_prestador_id = tip.id " +
                    "         INNER JOIN contratacion.clase_prestador cl ON p.clase_prestador = cl.id " +
                    "         INNER JOIN contratacion.clasificacion_prestador cla ON p.clasificacion_id = cla.id " +
                    "         INNER JOIN maestros.tipo_identificacion tp ON p.tipo_identificacion_id = tp.id " +
                    "         INNER JOIN maestros.tipo_identificacion tp2 ON p.tipo_identificacion_representante_id = tp2.id " +
                    "         INNER JOIN security.user u ON n.user_id = u.id " +
                    "         INNER JOIN contratacion.solicitud_contratacion sc ON n.id = sc.negociacion_id " +
                    "         LEFT OUTER JOIN contratacion.contrato c ON c.solicitud_contratacion_id = sc.id " +
                    "         LEFT OUTER JOIN contratacion.legalizacion_contrato lc ON lc.contrato_id = c.id " +
                    "         LEFT OUTER JOIN contratacion.minuta m ON lc.minuta_id = m.id " +
                    "         LEFT OUTER JOIN maestros.regional r ON c.regional_id = r.id " +
                    "WHERE c.deleted = FALSE ");
            final Map<String, Object> params = control.completarWhereConsultaSolicitudes(ql, filtroConsultaSolicitudDto);
            Query query = em.createNativeQuery(ql.toString());
            params.keySet().stream().filter((param) -> (params.get(param) != null)).forEach((param) -> query.setParameter(param, params.get(param)));
        return ((BigInteger) query.getSingleResult()).longValue();
    }

    @Override
    public Integer contarSolicitudesPorParametrizar(FiltroConsultaSolicitudDto filtroConsultaSolicitudDto,  NegociacionDto dtoNeg) throws ConexiaBusinessException {

        /* ANDRES - howMuchHiringRequestToParametrize */
      return control.contarSolicitudesPorParametrizar(filtroConsultaSolicitudDto, dtoNeg);

    	/* final StringBuilder ql = new StringBuilder("select cast(count(distinct sc.id) as Integer) "
                 + "from contratacion.solicitud_contratacion sc "
                 + "LEFT JOIN contratacion.contrato c on c.solicitud_contratacion_id  = sc.id "
                 + "LEFT JOIN maestros.regional r on c.regional_id = r.id "
                 + "LEFT JOIN contratacion.legalizacion_contrato lc on lc.contrato_id = c.id "
                 + "LEFT JOIN contratacion.minuta m on lc.minuta_id = m.id "
                 + "JOIN contratacion.negociacion n on sc.negociacion_id  = n.id "
                 + "JOIN security.user u on n.user_id = u.id "
                 + "JOIN contratacion.prestador p on n.prestador_id = p.id "
                 + "JOIN maestros.tipo_identificacion tp on p.tipo_identificacion_id = tp.id "
                 + "JOIN contratacion.sede_prestador sp on sp.prestador_id = p.id "
                 + "where n.estado_negociacion = :estadoNegociacion ");
         final Map<String, Object> params = control.completarWhereConsultaSolicitudes(ql, filtroConsultaSolicitudDto);
        Query query = em.createNativeQuery(ql.toString());
        query.setParameter("estadoNegociacion", EstadoNegociacionEnum.FINALIZADA.toString());
        params.keySet().stream().filter((param) -> (params.get(param) != null)).forEach((param) -> {
            query.setParameter(param, params.get(param));
        });
        Integer total = (Integer) query.getSingleResult();
        return total;*/
    }

    @Override
    public SolicitudContratacionParametrizableDto obtenerSolicitudParametrizacion(Long idSolicitudContratacion) throws ConexiaBusinessException {


        /* ANDRES -   */

        SolicitudContratacionParametrizableDto solicitud = control.obtenerSolicitudParametrizacion(idSolicitudContratacion);
                /*
        final StringBuilder ql = new StringBuilder("select new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto("
                + "sc.id, "
                + "neg.id, "
                + "p.id, p.nombre, "
                + "p.tipoIdentificacion.codigo, "
                + "p.numeroDocumento, "
				+ "sp.codigoPrestador, "
                + "p.prefijo, "
                + "p.tipoPrestador.descripcion, "
                + "p.naturalezaJuridica, "
                + "p.clasePrestador.descripcion,"
                + "p.clasificacionPrestador.descripcion,"
                + "p.nivelAtencion, "
                + "p.tipoIdentificacionRepLegal.id, "
                + "p.numeroDocumentoRepresentanteLegal, "
                + "p.representanteLegal, "
                + "p.correoElectronico, "
                + "p.empresaSocialEstado,"
                + "neg.tipoModalidadNegociacion,"
                + "neg.nuevoContrato, sc.estadoParametrizacion, "
                + "sc.estadoLegalizacion, "
                + "neg.poblacion,"
                + "sc.regimen,"
                + "neg.esRias "
                + ") "
                + "from SolicitudContratacion sc "
                + "JOIN sc.negociacion neg "
				+ "join neg.sedesNegociacion sn "
				+ "join sn.sedePrestador sp "
                + "JOIN neg.prestador p "
                + "where sn.principal = true " +
                "and sc.id = :idSolicitudContratacion ");

        TypedQuery<SolicitudContratacionParametrizableDto> query = em.createQuery(ql.toString(), SolicitudContratacionParametrizableDto.class);
        query.setParameter("idSolicitudContratacion", idSolicitudContratacion);
        SolicitudContratacionParametrizableDto solicitud = query.getSingleResult();*/

        SolicitudContratacion sc = em.createNamedQuery("SolicitudContratacion.filtrarPorNumeroNegociacion",
                SolicitudContratacion.class).setParameter("idSolicitudContratacion", idSolicitudContratacion)
                .getSingleResult();
        if (sc.getContratos().size() > 0) {
            solicitud.setNumeroContrato(sc.getContratos().get(0).getNumeroContrato());
        }
        return solicitud;
    }

	@Override
	public SolicitudContratacionParametrizableDto obtenerSolicitudParametrizacion(Long negociacionId,
			RegimenNegociacionEnum regimenNegociacionEnum, NegociacionModalidadEnum negociacionModalidadEnum) throws ConexiaBusinessException {
        return control.getHiringRequestParameterization2(negociacionId, regimenNegociacionEnum, negociacionModalidadEnum);
	}

    @Override
    public Boolean countOtroSiByNegociaicion(Long negociacionId) throws ConexiaBusinessException {
        return control.countOtroSiByNegociaicion(negociacionId);
    }

    public Integer contarContratosVistoBueno(FiltroConsultaSolicitudDto filtroConsultaSolicitudDto, NegociacionDto negociacion) throws ConexiaBusinessException {
          return control.howMuchHiringRequestToVoBo(filtroConsultaSolicitudDto, negociacion);
    }

    @Override
    public List<SolicitudContratacionParametrizableDto> listarContratosParaVistoBueno(
            FiltroConsultaSolicitudDto filtroConsultaSolicitudDto, NegociacionDto negociacion) throws ConexiaBusinessException {
        return control.getHiringRequestToVoBo(filtroConsultaSolicitudDto, negociacion);
    }


}
