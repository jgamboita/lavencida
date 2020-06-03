package com.conexia.contractual.services.transactional.contratourgencias.boundary;

import com.conexia.contractual.definitions.view.contratourgencias.BandejaContratoUrgenciasViewServiceRemote;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.services.transactional.contratourgencias.control.BandejaContratoUrgenciasControl;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaContratoUrgenciaDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaPrestadorDto;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Stateless
@Remote(BandejaContratoUrgenciasViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class BandejaContratoUrgenciasViewBoundary implements BandejaContratoUrgenciasViewServiceRemote {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private BandejaContratoUrgenciasControl bandejaControl;

    private StringBuilder query;

    /**
     * Método que realiza la busqueda de los prestadores en el sistema de
     * acuerdo a los filtros de busqueda asociados en el objeto
     * {@link FiltroBandejaPrestadorDto}
     *
     * @param filtro filtro de busqueda ver: {@link FiltroBandejaPrestadorDto}
     * @return Lista de prestadores
     */
    @Override
    public List<PrestadorDto> buscarPrestador(FiltroBandejaContratoUrgenciaDto filtro, String typeUserCode) {
        query = new StringBuilder();
        bandejaControl.generarSelectBuscarPrestador(query, filtro, typeUserCode);
        Map<String, Object> params = bandejaControl.generarWhereBuscarPrestador(query, filtro);
        Query q = em.createNativeQuery(this.query.toString(), Prestador.MAPPING_PRESTADOR);
        params.keySet().forEach(key -> q.setParameter(key, params.get(key)));
        return  q.setMaxResults(filtro.getCantidadRegistros())
                .setFirstResult(filtro.getPagina())
                .getResultList();
    }

    @Override
    public int contarPrestadoresNegociacion(FiltroBandejaContratoUrgenciaDto filtro, String typeUserCode) {

        query = new StringBuilder();
        bandejaControl.generarSelectContarPrestador(query, filtro, typeUserCode);
        final Map<String, Object> params = bandejaControl.generarWhereBuscarPrestador(query, filtro);
        query.append(" ) tabla ");
        Query q = em.createNativeQuery(this.query.toString());
        for (Map.Entry<String, Object> p : params.entrySet()) {
            q.setParameter(p.getKey(), p.getValue());
        }
        return ((Number) q.getSingleResult()).intValue();
    }


    public Map<String, Integer> getUserClassification(Integer userId) {
        Map<String, Integer> mapClasif = new HashMap<>();

        Integer isUserC = isUserContratacion(userId);
        mapClasif.put("isUserContratacion", isUserC);
        Integer isUserM = isUserCuentasMedicas(userId);
        mapClasif.put("isUserCuentasMedicas", isUserM);

        Integer regional = getRegional(userId);
        mapClasif.put("regional", regional);

        return mapClasif;

    }


    public Integer isUserContratacion(Integer userId) {
        Integer resp = 0;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT r.role  from security.user u ")
                .append("inner join security.user_role ur on ur.user_id = u.id ")
                .append("inner join security.role r on r.id = ur.role_id where r.role ilike 'ROLE_LEGALIZACION' ")
                .append("and u.id = :userId");

        Query query = em.createNativeQuery(sb.toString());
        query.setParameter("userId", userId);
        List<Object[]> listObj = (List<Object[]>) query.getResultList();

        if (!listObj.isEmpty()) {
            resp = 1;
        }
        return resp;
    }


    public Integer isUserCuentasMedicas(Integer userId) {
        Integer resp = 0;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT r.role  from security.user u ")
                .append("inner join security.user_role ur on ur.user_id = u.id ")
                .append("inner join security.role r on r.id = ur.role_id where r.descripcion ilike 'Rol Cuentas médicas%' ")
                .append("and u.id = :userId");

        Query query = em.createNativeQuery(sb.toString());
        query.setParameter("userId", userId);
        List<Object[]> listObj = (List<Object[]>) query.getResultList();

        if (!listObj.isEmpty()) {
            resp = 1;
        }
        return resp;

    }


    public Integer getRegional(Integer userId) {
        Integer resp = 0;

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT case when ue.regional_id is null and ue.municipio_id is not null then 0 else ue.regional_id end regionalId ")
                .append("from security.user u ")
                .append("inner join security.usuario_entidad ue on ue.usuario_id = u.id ")
                .append("where u.id = :userId ");

        Query query = em.createNativeQuery(sb.toString());
        query.setParameter("userId", userId);
        Integer regional = (Integer) query.getSingleResult();

        return regional;

    }

}
