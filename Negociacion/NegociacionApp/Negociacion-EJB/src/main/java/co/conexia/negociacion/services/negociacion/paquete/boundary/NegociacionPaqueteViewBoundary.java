package co.conexia.negociacion.services.negociacion.paquete.boundary;

import co.conexia.negociacion.services.negociacion.paquete.control.PaquetesNegociacionControl;
import co.conexia.negociacion.services.negociacion.paquete.control.ObtenerPaquetesNegociadosConErroresControl;
import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.filtro.FiltroSedeNegociacionPaquete;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.PaqueteNegociacionDto;
import com.conexia.contratacion.commons.dto.util.PaquetePortafolioServicioSaludDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.negociacion.paquete.NegociacionPaqueteViewServiceRemote;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Boundary de la negociacion para los paquetes a negociar de consulta
 *
 * @author jtorres
 */
@Stateless
@Remote(NegociacionPaqueteViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NegociacionPaqueteViewBoundary implements NegociacionPaqueteViewServiceRemote {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private PaquetesNegociacionControl control;

    @Inject
    private ObtenerPaquetesNegociadosConErroresControl obtenerPaquetesNegociadosConErroresControl;

    @Override
    public List<PaqueteNegociacionDto> consultarPaquetesNegociacionNoSedesByNegociacionId(Long negociacionId) {
        List<PaqueteNegociacionDto> paquetesNegociacion = em.createNamedQuery(
                "SedeNegociacionPaquete.findPaquetesNegociacionNoSedesByNegociacionId", PaqueteNegociacionDto.class).
                setParameter("negociacionId", negociacionId)
                .getResultList();
        return paquetesNegociacion;
    }

    @Override
    public List<PaquetePortafolioServicioSaludDto> consultaPaqueteServicioVsSedes(NegociacionDto negociacion, List<PaquetePortafolioDto> paquetes, List<SedePrestadorDto> sedes) {
        List<String> codigoPaquetes = paquetes.stream().map(pq -> pq.getCodigoPortafolio())
                .collect(Collectors.toList());
        List<Long> listadoSedes = sedes.stream().map(sc -> sc.getId()).collect(Collectors.toList());
        List<PaquetePortafolioServicioSaludDto> paquetesServiciosNoHab;
        paquetesServiciosNoHab = em.createNamedQuery("PaquetePortafolioServicioSalud.findPaquetesVsServicioSedes")
                .setParameter("codigoPaquete", codigoPaquetes)
                .setParameter("listaSedes", listadoSedes)
                .setParameter("nivelNegociacion", negociacion.getComplejidad().getNivel())
                .getResultList();
        return paquetesServiciosNoHab;
    }

    @Override
    public List<PaquetePortafolioDto> consultarPaquetesAgregar(FiltroSedeNegociacionPaquete filtroSedeNegociacionPaquete, NegociacionDto negociacion) throws ConexiaBusinessException {
        return this.control.consultarPaquetes(filtroSedeNegociacionPaquete, negociacion);
    }

    @Override
    public boolean consultarInactivosContenidoPaquetes(PaquetePortafolioDto paquete) {
        return (boolean) em.createNativeQuery("SELECT EXISTS(SELECT paquete.id, ps.id ps_id " +
                "              FROM contratacion.portafolio paquete " +
                "                       INNER JOIN contratacion.portafolio paquete_padre ON paquete_padre.id = paquete.portafolio_padre_id " +
                "                       LEFT JOIN contratacion.sede_prestador sp ON sp.portafolio_id = paquete.portafolio_padre_id " +
                "                       LEFT JOIN contratacion.procedimiento_paquete pp_1 ON pp_1.paquete_id = paquete.id " +
                "                       LEFT JOIN maestros.procedimiento_servicio ps ON pp_1.procedimiento_id = ps.id " +
                "                       LEFT JOIN maestros.procedimiento p ON p.id = ps.procedimiento_id AND p.estado_procedimiento_id = 2 " +
                "              WHERE paquete.id = :paqueteId " +
                "                AND sp.id ISNULL " +
                "                AND p.id NOTNULL " +
                "              UNION ALL " +
                "              SELECT paquete.id, mp.id " +
                "              FROM contratacion.portafolio paquete " +
                "                       INNER JOIN contratacion.portafolio paquete_padre ON paquete_padre.id = paquete.portafolio_padre_id " +
                "                       LEFT JOIN contratacion.sede_prestador sp ON sp.portafolio_id = paquete.portafolio_padre_id " +
                "                       LEFT JOIN contratacion.medicamento_portafolio mp ON paquete.id = mp.portafolio_id " +
                "                       LEFT JOIN maestros.medicamento m ON mp.medicamento_id = m.id " +
                "              WHERE 1 = CASE WHEN m.estado_medicamento_id = 2 THEN 1 WHEN m.estado_cum = 2 THEN 1 END " +
                "                AND paquete.id = :paqueteId " +
                "                AND sp.id ISNULL " +
                "                AND m.id NOTNULL " +
                "              UNION ALL " +
                "              SELECT paquete.id, i.id " +
                "              FROM contratacion.portafolio paquete " +
                "                       INNER JOIN contratacion.portafolio paquete_padre ON paquete_padre.id = paquete.portafolio_padre_id " +
                "                       LEFT JOIN contratacion.sede_prestador sp ON sp.portafolio_id = paquete.portafolio_padre_id " +
                "                       LEFT JOIN contratacion.insumo_portafolio ip ON paquete.id = ip.portafolio_id " +
                "                       LEFT JOIN maestros.insumo i ON ip.insumo_id = i.id AND i.estado_insumo_id = 2 " +
                "              WHERE paquete.id = :paqueteId " +
                "                AND sp.id ISNULL " +
                "                AND i.id NOTNULL " +
                "              UNION ALL " +
                "              SELECT pp.portafolio_id, p.id " +
                "              FROM contratacion.paquete_portafolio pp " +
                "                       JOIN maestros.procedimiento p ON pp.origen_id = p.id AND p.estado_procedimiento_id = 2 " +
                "              WHERE portafolio_id = :paqueteId) estado;")
                .setParameter("paqueteId", paquete.getPortafolio().getId())
                .getSingleResult();
    }

    @Override
    public boolean validarPaqueteSinServicio(PaquetePortafolioDto paquete) {
        return (boolean) em.createNativeQuery(" SELECT count(0)>0 FROM maestros.procedimiento_servicio ps WHERE ps.estado = 1 AND ps.codigo_cliente = :codigoPaquete ")
                .setParameter("codigoPaquete", paquete.getCodigoPortafolio())
                .getSingleResult();
    }

    @Override
    public boolean existenPaqueteInsumoSinParametros(Long negociacionId) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*)>0 ")
                .append("FROM contratacion.sede_negociacion_paquete_insumo snpi ")
                .append("JOIN contratacion.sede_negociacion_paquete snp  ON snpi.sede_negociacion_paquete_id=snp.id ")
                .append("JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id ")
                .append("WHERE sn.negociacion_id = :negociacionId ")
                .append("AND ( (cantidad_maxima IS NULL AND cantidad_maxima IS NULL) OR  (COALESCE(cantidad_minima,0)>COALESCE(cantidad_maxima,0))  )");
        Object objC = this.em.createNativeQuery(sb.toString()).setParameter("negociacionId", negociacionId).getSingleResult();
        return Objects.nonNull(objC) && (Boolean) objC;
    }

    @Override
    public boolean existePaqueteMedicamentoSinParametros(Long negociacionId) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT COUNT(*)>0 ")
                .append("FROM contratacion.sedes_negociacion sn ")
                .append("JOIN contratacion.sede_negociacion_paquete snpq  on snpq.sede_negociacion_id = sn.id ")
                .append("JOIN contratacion.sede_negociacion_paquete_medicamento  snpm on snpm.sede_negociacion_paquete_id = snpq.id ")
                .append("JOIN maestros.medicamento m on snpm.medicamento_id = m.id ")
                .append("WHERE sn.negociacion_id = :negociacionId ")
                .append("AND ((snpm.cantidad_minima IS NULL OR snpm.cantidad_maxima IS NULL) OR  (COALESCE(snpm.cantidad_minima,0)>COALESCE(snpm.cantidad_maxima,0))  )");
        Object objC = this.em.createNativeQuery(sb.toString()).setParameter("negociacionId", negociacionId).getSingleResult();
        return Objects.nonNull(objC) && (Boolean) objC;
    }

    @Override
    public boolean existePaqueteProcedimientosSinParametros(Long negociacionId) {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT  count(*)>0 ")
                .append("FROM  contratacion.sedes_negociacion sn ")
                .append("JOIN  contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id ")
                .append("JOIN  contratacion.sede_negociacion_paquete_procedimiento snpp on snpp.sede_negociacion_paquete_id = snp.id ")
                .append("JOIN  maestros.procedimiento_servicio ps on snpp.procedimiento_id = ps.id ")
                .append("JOIN  maestros.procedimiento p_n on ps.procedimiento_id = p_n.id ")
                .append("where sn.negociacion_id = :negociacionId ")
                .append("AND p_n.tipo_procedimiento_id = 1 ")
                .append("AND ((snpp.cantidad_minima IS NULL OR snpp.cantidad_maxima IS NULL) OR  (COALESCE(snpp.cantidad_minima,0)>COALESCE(snpp.cantidad_maxima,0))  )");
        Object objC = this.em.createNativeQuery(sb.toString()).setParameter("negociacionId", negociacionId).getSingleResult();
        return Objects.nonNull(objC) && (Boolean) objC;
    }

    @Override
    public List<ErroresTecnologiasDto> obtenerPaquetesNegociadosConErrores(NegociacionDto negociacion){
        if (Objects.isNull(negociacion)) {
            throw new IllegalArgumentException("Para obtener los paquetes negociados con errores, el identificador de la negociación no puede estar vacío ");
        }
        return obtenerPaquetesNegociadosConErroresControl.obtenerPaquetes(negociacion);
    }
}
