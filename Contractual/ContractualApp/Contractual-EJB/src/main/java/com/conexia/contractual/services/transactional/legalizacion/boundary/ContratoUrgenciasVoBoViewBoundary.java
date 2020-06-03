package com.conexia.contractual.services.transactional.legalizacion.boundary;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Query;

import com.conexia.contractual.definitions.view.legalizacion.ContratoUrgenciasVoBoViewServiceRemote;
import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.TypeUserContractUrgencyEnum;
import com.conexia.contractual.services.transactional.legalizacion.control.ContratoUrgenciasVoBoControl;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.LegalizacionContratoUrgenciasDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroContratoUrgenciasDto;

@Stateless
@Remote(ContratoUrgenciasVoBoViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ContratoUrgenciasVoBoViewBoundary implements ContratoUrgenciasVoBoViewServiceRemote {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private ContratoUrgenciasVoBoControl contratoUrgenciasVoBoControl;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    private StringBuilder query;


    @Override
    public List<LegalizacionContratoUrgenciasDto> consultarContratosUrgenciasParaVistoBueno(FiltroContratoUrgenciasDto filtros, String typeUserCode) {

        query = new StringBuilder("SELECT DISTINCT cu.id contrato_id, lcu.id legalizacion_id, cu.numero_contrato_urgencias numero_contrato ");
        query.append(" 	,cu.tipo_contrato tipo_contrato, rl.descripcion regional, cu.tipo_modalidad, cu.regimen ");
        query.append(" 	,cu.tipo_subsidiado, cu.fecha_inicio, cu.fecha_fin, cu.user_id, u.primer_nombre, u.segundo_nombre, u.primer_apellido, u.segundo_apellido, lcu.estado_legalizacion ");
        query.append(" 	,CAST((CASE WHEN cu.fecha_fin>CAST(NOW() AS date)THEN 'VIGENTE' ELSE 'NO VIGENTE' END) AS varchar) estado_contrato ");
        query.append(" 	,CAST(COUNT(scu.id) over(partition by scu.contrato_urgencias_id order by scu.contrato_urgencias_id) AS integer) n_sedes, p.numero_documento nit, p.nombre prestador ");
        query.append(" FROM contratacion.contrato_urgencias cu ");
        query.append(" JOIN contratacion.legalizacion_contrato_urgencias lcu ON cu.id=lcu.contrato_urgencias_id ");
        query.append(" JOIN contratacion.prestador p ON cu.prestador_id=p.id ");
        query.append(" LEFT JOIN maestros.regional rl ON cu.regional_id=rl.id ");
        query.append(" LEFT JOIN security.user u ON u.id=cu.user_id");
        query.append(" LEFT JOIN contratacion.sede_contrato_urgencias scu ON cu.id=scu.contrato_urgencias_id ");
        query.append(" WHERE lcu.estado_legalizacion= :estadoLegalizacion ");
        query.append("and cu.deleted = false ");///

        /* **Restriccion segun el tipo de usuario**** **/
        if (!typeUserCode.equals(TypeUserContractUrgencyEnum.GERENCIA_SALUD.getCode())) {
            if (typeUserCode.equals(TypeUserContractUrgencyEnum.CUENTAS_MEDICAS.getCode())) {
                query.append(" AND p.aplica_red_ips=0 ");
            } else if (typeUserCode.equals(TypeUserContractUrgencyEnum.GERENCIA_REGIONAL.getCode())) {
                query.append(" AND (p.aplica_red_ips=0 OR p.aplica_red_ips=1)");
            } else {
                //Se asigan codigo que no existe ya q el tipo de usuario no tendira permisos para ver los contratos de urgencias para VoBo
                query.append(" AND p.aplica_red_ips=10 ");
            }
        }
        /* ****************************************** */

        final Map<String, Object> params = contratoUrgenciasVoBoControl.completarWhereConsultaContratosUrgenciasVoBo(query, filtros);

        query.append(" ORDER BY cu.fecha_inicio DESC, cu.numero_contrato_urgencias DESC, cu.id ASC ");
        Query q = em.createNativeQuery(query.toString(), "LegalizacionContratoUrgencias.contratosUrgenciasParaVoBoMapping");
        q.setParameter("estadoLegalizacion", EstadoLegalizacionEnum.CONTRATO_SIN_VB.toString());
        params.keySet().stream().filter((param) -> (params.get(param) != null)).forEach((param) -> {
            q.setParameter(param, params.get(param));
        });

        return q.setMaxResults(filtros.getCantidadRegistros())
                .setFirstResult(filtros.getPagina())
                .getResultList();

    }


    @SuppressWarnings("finally")
    @Override
    public LegalizacionContratoUrgenciasDto consultarContratoUrgenciasByIdContrato(Long IdContratoUrgencias) {
        LegalizacionContratoUrgenciasDto legalizacionContratoUrgencias = new LegalizacionContratoUrgenciasDto();
        try {
            legalizacionContratoUrgencias = em
                    .createNamedQuery("LegalizacionContratoUrgencias.findLegalizacionContratoUrgenciasByContratoUrgencias", LegalizacionContratoUrgenciasDto.class)
                    .setParameter("idContratoUrgencias", IdContratoUrgencias)
                    .getSingleResult();
        } catch (final PersistenceException e) {
            Logger.getLogger(ContratoUrgenciasVoBoViewBoundary.class.getName()).log(Level.SEVERE, null, e);
        } finally {
            return legalizacionContratoUrgencias;
        }
    }

    public int contarContratosUrgenciasParaVistoBueno(FiltroContratoUrgenciasDto filtros, String typeUserCode) {

        query = new StringBuilder("SELECT count(DISTINCT cu.id) ");
        query.append(" FROM contratacion.contrato_urgencias cu ");
        query.append(" JOIN contratacion.legalizacion_contrato_urgencias lcu ON cu.id=lcu.contrato_urgencias_id ");
        query.append(" JOIN contratacion.prestador p ON cu.prestador_id=p.id ");
        query.append(" LEFT JOIN maestros.regional rl ON cu.regional_id=rl.id ");
        query.append(" LEFT JOIN security.user u ON u.id=cu.user_id");
        query.append(" LEFT JOIN contratacion.sede_contrato_urgencias scu ON cu.id=scu.contrato_urgencias_id ");
        query.append(" WHERE lcu.estado_legalizacion= :estadoLegalizacion ");

        /***Restriccion segun el tipo de usuario******/
        if (!typeUserCode.equals(TypeUserContractUrgencyEnum.GERENCIA_SALUD.getCode())) {
            if (typeUserCode.equals(TypeUserContractUrgencyEnum.CUENTAS_MEDICAS.getCode())) {
                query.append(" AND p.aplica_red_ips=0 ");
            } else if (typeUserCode.equals(TypeUserContractUrgencyEnum.GERENCIA_REGIONAL.getCode())) {
                query.append(" AND (p.aplica_red_ips=0 OR p.aplica_red_ips=1) ");
            } else {
                //Se asigan codigo que no existe ya q el tipo de usuario no tendira permisos para ver los contratos de urgencias para VoBo
                query.append(" AND p.aplica_red_ips=10 ");
            }
        }
        /********************************************/

        final Map<String, Object> params = contratoUrgenciasVoBoControl.completarWhereConsultaContratosUrgenciasVoBo(query, filtros);

        // query.append( " ORDER BY cu.id ");
        Query q = em.createNativeQuery(query.toString());
        q.setParameter("estadoLegalizacion", EstadoLegalizacionEnum.CONTRATO_SIN_VB.toString());
        params.keySet().stream().filter((param) -> (params.get(param) != null)).forEach((param) -> {
            q.setParameter(param, params.get(param));
        });
        return ((BigInteger) q.getSingleResult()).intValue();
    }

}
