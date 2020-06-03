package co.conexia.negociacion.services.bandeja.comparacion.boundary;

import co.conexia.negociacion.services.bandeja.comparacion.control.BandejaComparacionControl;
import java.util.List;
import java.util.Map;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaComparacionDto;
import com.conexia.negociacion.definitions.bandeja.comparacion.BandejaComparacionViewServiceRemote;

@Stateless
@Remote(BandejaComparacionViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class BandejaComparacionViewBoundary implements BandejaComparacionViewServiceRemote{

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private BandejaComparacionControl bandejaControl;

    private StringBuilder query;

    /**
     * Método que realiza la busqueda de los prestadores en el sistema de
     * acuerdo a los filtros de busqueda asociados en el objeto
     * {@link FiltroBandejaComparacionDto}
     *
     * @param filtro filtro de busqueda ver: {@link FiltroBandejaComparacionDto}
     * @return
     */
    @Override
    public List<PrestadorDto> buscarPrestador(FiltroBandejaComparacionDto filtro) {
        query = new StringBuilder();
        bandejaControl.generarSelectBuscarPrestador(query, filtro.getModalidadNegociacion());
        Map<String, Object> params
                = bandejaControl.generarWhereBuscarPrestador(filtro, query);
        final TypedQuery<PrestadorDto> prestadores = em.createQuery(query.toString(), PrestadorDto.class);
        for (String key : params.keySet()) {
            prestadores.setParameter(key, params.get(key));
        }
        return prestadores.getResultList();
    }

}
