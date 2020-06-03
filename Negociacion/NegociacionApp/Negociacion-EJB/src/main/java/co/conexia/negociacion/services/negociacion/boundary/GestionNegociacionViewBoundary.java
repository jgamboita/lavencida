package co.conexia.negociacion.services.negociacion.boundary;

import java.util.Collections;
import java.util.List;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import co.conexia.negociacion.services.negociacion.control.NegociacionAnexoTarifarioControlRest;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.FiltroAnexoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioDetallePaqueteDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioDetallePrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioMedicamentoDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioMedicamentoPgpDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioPaqueteDinamicoDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioPaqueteDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioProcedimientoPgpDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoTarifarioTecnologiaPaqueteDto;
import com.conexia.contratacion.commons.dto.negociacion.PaquetePortafolioCausaRupturaDto;
import com.conexia.contratacion.commons.dto.negociacion.PaquetePortafolioExclusionDto;
import com.conexia.contratacion.commons.dto.negociacion.PaquetePortafolioObservacionDto;
import com.conexia.contratacion.commons.dto.negociacion.PaquetePortafolioRequerimientosDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.negociacion.definitions.negociacion.GestionNegociacionViewServiceRemote;
import co.conexia.negociacion.services.negociacion.control.GestionNegociacionControl;
import co.conexia.negociacion.services.negociacion.control.GestionNegociacionControlRest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Boundary para la gestion de las negociaciones asociadas a un prestador
 *
 * @author jjoya
 */
@Stateless
@Remote(GestionNegociacionViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class GestionNegociacionViewBoundary implements GestionNegociacionViewServiceRemote {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private GestionNegociacionControlRest gestionNegociacionControlRest;

    @Inject
    private NegociacionAnexoTarifarioControlRest negociacionAnexoTarifarioControlRest;

    @Inject
    private Log log;

    public List<NegociacionDto> consultarNegociacionesPrestador(Long prestadorId, NegociacionDto negociacion) {
        try {
            return gestionNegociacionControlRest.consultarNegociacionesPrestador(prestadorId, negociacion);
        } catch (Exception e) {
            log.error("Se presento un error consultado el servicio consultarNegociacionesPrestador", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<AnexoTarifarioProcedimientoDto> consultarProcedimientosAnexoTarifario(Long negociacionId, NegociacionModalidadEnum modalidadNegociacion, boolean conRecuperacion, boolean fraccionarDescripcion, Boolean esOtroSi,Long negociacionPadreId) throws ConexiaBusinessException {
        try {
            return negociacionAnexoTarifarioControlRest.consultarProcedimientosAnexoTarifario(negociacionId, modalidadNegociacion, conRecuperacion, fraccionarDescripcion, esOtroSi, negociacionPadreId);
        } catch (Exception e) {
            log.error("Se presento un error consultado el servicio consultarProcedimientosAnexoTarifario", e);
            throw e;
        }
    }

    @Override
    public List<AnexoTarifarioProcedimientoPgpDto> consultarProcedimientosPgpAnexoTarifario(Long negociacionId) throws ConexiaBusinessException {
        try {
            return negociacionAnexoTarifarioControlRest.consultarProcedimientosPgpAnexoTarifario(negociacionId);
        } catch (Exception e) {
            log.error("Se presento un error consultado el servicio consultarProcedimientosPgpAnexoTarifario", e);
            throw e;
        }
    }

    @Override
    public List<AnexoTarifarioMedicamentoPgpDto> consultarMedicamentosPgpAnexoTarifario(Long negociacionId)
            throws ConexiaBusinessException
    {
        return gestionNegociacionControlRest.getPGPDrugTarrifAnnexBase(negociacionId);
    }

    @Override
    public List<AnexoTarifarioPaqueteDto> consultarPaquetesAnexoTarifario(Long negociacionId) {
        try {
            String sql = "SELECT CONCAT(sp.codigo_habilitacion, '-', sp.codigo_sede)                        AS codHabilitacionSede, " +
                    "       sp.nombre_sede                                                             AS sedePrestador, " +
                    "       m.nombre                                                                   AS descGrupoHab, " +
                    "       ppp.codigo                                                                 AS codPaqueteEmssanar, " +
                    "       ppp.descripcion                                                            AS descPaqueteEmssanar, " +
                    "       ppp.codigo_sede_prestador                                                  AS codPaqueteIps, " +
                    "       c.fecha_fin                                                                AS fechaFinal, " +
                    "       c.fecha_inicio                                                             AS fechaInicial, " +
                    "       px.descripcion                                                             AS descPaqueteIps, " +
                    "       CASE WHEN ppp.tipo_paquete = 'POS' THEN 'SI' ELSE 'NO' END                 AS categoriaPos, " +
                    "       snp.valor_negociado                                                        AS tarifaNegociada, " +
                    "       ss.codigo                                                                  AS codServicioHabReps, " +
                    "       ss.nombre                                                                  AS descServicioHabReps, " +
                    "       (SELECT ppo.observacion " +
                    "        FROM contratacion.paquete_portafolio_observacion ppo " +
                    "        WHERE ppo.paquete_portafolio_id = ppp.id " +
                    "        LIMIT 1)                                                                  AS observacionPaquete, " +
                    "       CASE WHEN px.estado_procedimiento_id = 1 THEN 'Activo' ELSE 'Inactivo' END AS estado " +
                    "FROM contratacion.sedes_negociacion sn " +
                    "         INNER JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id " +
                    "         INNER JOIN contratacion.sede_negociacion_paquete snp ON snp.sede_negociacion_id = sn.id " +
                    "         INNER JOIN contratacion.paquete_portafolio ppp ON snp.paquete_id = ppp.portafolio_id " +
                    "         INNER JOIN maestros.procedimiento px ON px.codigo_emssanar = ppp.codigo " +
                    "         INNER JOIN maestros.procedimiento_servicio ps ON px.id = ps.procedimiento_id AND ps.estado = 1 " +
                    "         INNER JOIN contratacion.servicio_salud ss ON ps.servicio_id = ss.id " +
                    "         INNER JOIN contratacion.macroservicio m ON m.id = ppp.macroservicio_id " +
                    "         LEFT JOIN contratacion.solicitud_contratacion sol ON sol.negociacion_id = sn.negociacion_id " +
                    "         LEFT JOIN contratacion.contrato c ON c.solicitud_contratacion_id = sol.id " +
                    "         LEFT JOIN maestros.servicios_reps sr ON sr.codigo_habilitacion = sp.codigo_habilitacion AND " +
                    "                                                 sr.numero_sede = cast(sp.codigo_sede AS int) AND " +
                    "                                                 sr.servicio_codigo = cast(ss.codigo AS int) " +
                    "         LEFT JOIN maestros.servicios_no_reps snr ON snr.sede_prestador_id = sp.id AND snr.servicio_id = ss.id " +
                    "WHERE 1 = CASE WHEN sr.id IS NOT NULL THEN 1 WHEN snr.id IS NOT NULL THEN 1 END " +
                    "  AND sn.negociacion_id = :negociacionId " +
                    "GROUP BY 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15";
            return (List<AnexoTarifarioPaqueteDto>) em
                    .createNativeQuery(sql, "Negociacion.TablaPaquetesAnexoTarifarioMapping")
                    .setParameter("negociacionId", negociacionId)
                    .getResultList();
        } catch (PersistenceException e) {
            log.error("Se presento un error de persistencia consultado los servicios para el anexo del paquete", e);
            return Collections.emptyList();
        } catch (Exception e) {
            log.error("Se presento un error consultado los servicios para el anexo del paquete", e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<AnexoTarifarioDetallePaqueteDto> consultarDetallePaquetesAnexoTarifario(Long negociacionId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionControlRest.getPackageTarrifAnnexDetails(negociacionId);
    }

    @Override
    public AnexoTarifarioDetallePrestadorDto consultarAnexoTarifarioDetallePrestador(Long negociacionId)
            throws ConexiaBusinessException
    {
        return gestionNegociacionControlRest.getTarrifAnnexDetailsTray(negociacionId);
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<AnexoTarifarioProcedimientoDto> consultarProcedimientosAnexoTarifarioOtroSiBase(NegociacionDto negociacion, boolean conRecuperacion, boolean fraccionarDescripcion) throws ConexiaBusinessException {
        try {
            return negociacionAnexoTarifarioControlRest.consultarProcedimientosAnexoTarifarioOtroSiBase(negociacion, conRecuperacion, fraccionarDescripcion);
        } catch (ConexiaBusinessException e) {
            log.error("Se presento un error consultado el servicio consultarProcedimientosAnexoTarifarioOtroSiBase", e);
            throw e;
        }
    }

    public List<AnexoTarifarioProcedimientoDto> consultarProcedimientosAnexoTarifarioOtroSi( NegociacionModalidadEnum modalidadNegociacion, NegociacionDto negociacionDto) throws ConexiaBusinessException {
        try {
            return negociacionAnexoTarifarioControlRest.consultarProcedimientosAnexoTarifarioOtroSi(modalidadNegociacion, negociacionDto);
        } catch (Exception e) {
            log.error("Se presento un error consultado el servicio consultarProcedimientosAnexoTarifarioOtroSi", e);
            throw e;
        }
    }

    @Override
    public Boolean tienePaquetes(Long negociacionId) {
        String select = "select count(distinct ppp.codigo) from contratacion.negociacion n " +
                "	inner join contratacion.sedes_negociacion sn on n.id = sn.negociacion_id " +
                "	inner join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id " +
                "	inner join contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id " +
                "	inner join contratacion.paquete_portafolio ppp on snp.paquete_id = ppp.portafolio_id " +
                "	inner join contratacion.macroservicio m on m.id = ppp.macroservicio_id " +
                "	where n.id = :negociacionId";
        return ((Number) em.createNativeQuery(select)
                .setParameter("negociacionId", negociacionId)
                .getSingleResult()).intValue() > 0;
    }

    @Override
    public Boolean tieneMedicamentos(Long negociacionId, 
            NegociacionModalidadEnum negociacionModalidadEnum) {
        try {
            return gestionNegociacionControlRest.tieneMedicamentos(negociacionId,
                    negociacionModalidadEnum);
        }catch (Exception ex) {
            Logger.getLogger(GestionNegociacionViewBoundary.class.getName()).log(Level.SEVERE, "Error en la respuesta del api", ex);
        } return false;
    }

    @Override
    public Boolean tieneProcedimientos(Long negociacionId, 
            NegociacionModalidadEnum negociacionModalidadEnum) {
        try {
            return gestionNegociacionControlRest.tieneProcedimientos(negociacionId,
                    negociacionModalidadEnum );
        }  catch (Exception ex) {
            Logger.getLogger(GestionNegociacionViewBoundary.class.getName()).log(Level.SEVERE, "Error en la respuesta del api", ex);
        } 
        return false;
    }

    @Override
    public Boolean tieneProcedimientosRecuperacion(Long negociacionId) {
        StringBuilder select = new StringBuilder();
        select.append("SELECT count(0) "
                + " FROM contratacion.negociacion_ria nr "
                + " JOIN maestros.ria r on r.id = nr.ria_id "
                + " JOIN contratacion.negociacion_ria_rango_poblacion nrr on nrr.negociacion_ria_id = nr.id"
                + " JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = nr.negociacion_id "
                + " JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id"
                + " JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id "
                + "			AND snp.negociacion_ria_rango_poblacion_id = nrr.id "
                + "	WHERE nr.negociado = true and nr.negociacion_id = :negociacionId "
                + " AND r.codigo = '" + RiasEnum.RECUPERACION.getCodigo() + "'");
        return ((Number) em.createNativeQuery(select.toString())
                .setParameter("negociacionId", negociacionId)
                .getSingleResult()).intValue() > 0;
    }

    @Override
    public Boolean tieneMedicamentosRecuperacion(Long negociacionId) {
        StringBuilder select = new StringBuilder();
        select.append("SELECT count(0) "
                + " FROM contratacion.negociacion_ria nr "
                + " JOIN maestros.ria r on r.id = nr.ria_id "
                + " JOIN contratacion.negociacion_ria_rango_poblacion nrr on nrr.negociacion_ria_id = nr.id"
                + " JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = nr.negociacion_id "
                + " JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_negociacion_id = sn.id "
                + "			AND snm.negociacion_ria_rango_poblacion_id = nrr.id "
                + "	WHERE nr.negociado = true and nr.negociacion_id = :negociacionId "
                + " AND r.codigo = '" + RiasEnum.RECUPERACION.getCodigo() + "'");
        return ((Number) em.createNativeQuery(select.toString())
                .setParameter("negociacionId", negociacionId)
                .getSingleResult()).intValue() > 0;
    }

    @Override
    public Boolean tienePoblacion(Long negociacionId) {
        StringBuilder select = new StringBuilder();
        select.append("SELECT count(0) "
                + " FROM contratacion.afiliado_x_sede_negociacion asn "
                + " INNER JOIN contratacion.sedes_negociacion sn ON sn.id = asn.sede_negociacion_id "
                + "	WHERE sn.negociacion_id = :negociacionId ");
        return ((Number) em.createNativeQuery(select.toString())
                .setParameter("negociacionId", negociacionId)
                .getSingleResult()).intValue() > 0;
    }

    @Override
    public List<AnexoTarifarioMedicamentoDto> consultarMedicamentosAnexoTarifarioOtroSiBase(NegociacionDto negociacion)
            throws ConexiaBusinessException
    {
        return gestionNegociacionControlRest.getOtrosiDrugTarrifAnnexBase(negociacion);
    }

    @Override
    public List<AnexoTarifarioMedicamentoDto> consultarMedicamentosAnexoTarifario(FiltroAnexoTarifarioDto filtros,
                                                                                  Boolean esOtroSi,Long negociacionPadreId)
            throws ConexiaBusinessException
    {
        return gestionNegociacionControlRest.getDrugTarrifAnnex(filtros, esOtroSi, negociacionPadreId);
    }

    @Override
    public List<AnexoTarifarioMedicamentoDto> consultarMedicamentosAnexoTarifarioOtroSi(FiltroAnexoTarifarioDto filtros,
                                                                                        NegociacionDto negociacionDto) throws ConexiaBusinessException {
        return gestionNegociacionControlRest.getOtrosiDrugTarrifAnnex(filtros, negociacionDto);
    }

    @Override
    public List<AnexoTarifarioPaqueteDto> consultarPaquetesAnexoTarifario(Long negociacionId,Boolean esOtroSi,Long negociacionPadreId)
            throws ConexiaBusinessException
    {
        return gestionNegociacionControlRest.getPackageTarrifAnnex(negociacionId, esOtroSi, negociacionPadreId);
    }

    @Override
    public List<AnexoTarifarioPaqueteDto> consultarPaquetesAnexoTarifarioOtroSi(NegociacionDto negociacionDto)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionControlRest.getOtrosiPackageTarrifAnnex(negociacionDto);
    }

    @Override
    public List<AnexoTarifarioPaqueteDto> consultarPaquetesAnexoTarifarioOtroSiBase(NegociacionDto negociacion)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionControlRest.getOtrosiPackageTarrifAnnexBase(negociacion.getId());
    }

    @Override
    public List<AnexoTarifarioTecnologiaPaqueteDto> consultarTecnologiasPaquetesAnexoTarifario(Long negociacionId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionControlRest.getTechnologiesPackageTarrifAnnex(negociacionId);

    }

    @Override
    public List<AnexoTarifarioPaqueteDinamicoDto> consultarPaquetesAnexoTarifarioDinamico(Long negociacionId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionControlRest.getPackageTarrifAnnexDynamic(negociacionId);
    }

    @Override
    public List<PaquetePortafolioObservacionDto> consultarPaqueteNegociacionObservacion(Long negociacionId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionControlRest.getPackageNegotiationObservation(negociacionId);
    }

    @Override
    public List<PaquetePortafolioObservacionDto> consultarPaquetePortafolioObservacion(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionControlRest.getBriefcasePackageObservation(codigoPaquete, portafolioId);
    }

    @Override
    public List<PaquetePortafolioExclusionDto> consultarPaqueteNegociacionExclusion(Long negociacionId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionControlRest.getPackageNegotiationExclusion(negociacionId);
    }
    @Override
    public List<PaquetePortafolioExclusionDto> consultarPaquetePortafolioExclusion(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionControlRest.getBriefcasePackageExclusion(codigoPaquete, portafolioId);
    }

    @Override
    public List<PaquetePortafolioRequerimientosDto> consultarPaqueteNegociacionRequerimientos(Long negociacionId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionControlRest.getPackageNegotiationRequirement(negociacionId);
    }

    @Override
    public List<PaquetePortafolioRequerimientosDto> consultarPaquetePortafolioRequerimientos(String codigoPaquete,
                                                                                             Long portafolioId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionControlRest.getBriefcasePackageRequirement(codigoPaquete, portafolioId);
    }

    @Override
    public List<PaquetePortafolioCausaRupturaDto> consultarPaqueteNegociacionCausaRuptura(Long negociacionId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionControlRest.getPackageNegotiationCauseBreakdown(negociacionId);
    }

    @Override
    public List<PaquetePortafolioCausaRupturaDto> consultarPaquetePortafolioCausaRuptura(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException
    {
            return this.gestionNegociacionControlRest.getBriefcasePackageCauseBreakdown(codigoPaquete, portafolioId);
    }


    @Override
    public List<AnexoTarifarioTecnologiaPaqueteDto>
    consultarPortafolioTecnologiasPaquetesAnexoTarifario(String codigoPaquete, Long portafolioId)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionControlRest.getBriefcaseTechnologiesPackageTarrifAnnex(codigoPaquete, portafolioId);
    }

    @Override
    public List<AnexoTarifarioPaqueteDinamicoDto> consultarPortafolioPaquetesAnexoTarifarioDinamico(String codigoPaquete)
            throws ConexiaBusinessException
    {
        return this.gestionNegociacionControlRest.getBriefcasePackageTarrifAnnexDynamic(codigoPaquete);
    }
}