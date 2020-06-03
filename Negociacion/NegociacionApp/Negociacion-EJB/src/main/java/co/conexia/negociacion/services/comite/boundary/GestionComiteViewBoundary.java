package co.conexia.negociacion.services.comite.boundary;

import java.util.Date;
import java.util.List;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import co.conexia.negociacion.services.comite.control.GestionComiteControl;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorComiteEnum;
import com.conexia.contratacion.commons.dto.comite.AsistenteComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.PrestadorComiteDto;
import com.conexia.contratacion.commons.dto.comite.TablaPrestadoresComiteDto;
import com.conexia.contratacion.commons.dto.comparacion.ResumenComparacionDto;
import com.conexia.negociacion.definitions.comite.GestionComiteViewServiceRemote;

/**
 * Boundary que contiene las consultas utilizadas en la gestion de comites
 *
 * @author etorres
 */
@Stateless
@Remote(GestionComiteViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class GestionComiteViewBoundary implements GestionComiteViewServiceRemote {

    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;

    @Inject
    private GestionComiteControl control;

    /**
     * Metodo que devuelve la lista de los prestadores a evaluar en un comite
     * determinado
     *
     * @param comiteId
     * @return Lista de Prestadores
     */
    @Override
    public List<TablaPrestadoresComiteDto> buscarPrestadoresComiteByComiteId(Long comiteId) {
        return em.createNamedQuery("PrestadorComite.buscarPrestadorComiteByComiteId", TablaPrestadoresComiteDto.class)
                .setParameter("comiteId", comiteId)
                .getResultList();
    }

    /**
     * Busca asistentes a un comite de precontratacion dado el comite.
     *
     * @param comiteId la id del comite.
     * @return Lista con los asistentes al comite.
     */
    @Override
    public List<AsistenteComitePrecontratacionDto> buscarAsistentesComite(Long comiteId) {
        return em
                .createNamedQuery("AsistenteComitePrecontratacion.findByComite",
                        AsistenteComitePrecontratacionDto.class)
                .setParameter("comiteId", comiteId).getResultList();
    }

    /**
     * Busca un comite válido disponible en el sistema
     *
     * @return Un comite con la id asociada o <b>null</b> en el caso que no
     * encuentre el comite.
     */
    @Override
    public ComitePrecontratacionDto buscarComiteDisponible() {
        return this.control.buscarComiteDisponible();
    }

    /**
     * Consulta de el resumen de comparacion por el id del prestador comite
     *
     * @param idPrestadorComite Identificador del prestador comite
     * @return lista de {@link - ResumenComparacionDto}
     */
    @Override
    public List<ResumenComparacionDto> consultarResumenComparacionByPrestadorComiteId(
            Long idPrestadorComite) {
        return em
                .createNamedQuery(
                        "ResumenComparacionTarifas.findDtoByPrestadorComiteId",
                        ResumenComparacionDto.class)
                .setParameter("prestadorComiteId", idPrestadorComite)
                .getResultList();
    }

    /**
     * @see GestionComiteViewServiceRemote#validarExisteActaComite(Long)
     */
    @Override
    public boolean validarExisteActaComite(Long comiteId) {
        return em.createNamedQuery(
                "ActaComite.validarExisteActaPorComiteId",
                Boolean.class)
                .setParameter("comiteId", comiteId)
                .getSingleResult();
    }

    @Override
    public List<PrestadorComiteDto> buscarPrestadoresComite(
            EstadoPrestadorComiteEnum estado, Integer maxResult) {
        
    	return em.createNamedQuery(
                "PrestadorComite.findByEstado",
                PrestadorComiteDto.class)
                .setParameter("estado", estado)
                .setMaxResults(maxResult)
                .getResultList();
    }
    
    /**
     * @see GestionComiteViewServiceRemote#evaluarFechaDisponible(Date)
     */
    public boolean evaluarFechaDisponible(Date fechaAplazamiento){
    	return em.createNamedQuery(
                "ComitePrecontratacion.evaluarFechaDisponible", Boolean.class)
                .setParameter("fechaComite", fechaAplazamiento)
                .getSingleResult();
    }
}
