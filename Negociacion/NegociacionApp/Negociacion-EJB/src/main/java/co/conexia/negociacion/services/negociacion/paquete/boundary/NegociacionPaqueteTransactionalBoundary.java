package co.conexia.negociacion.services.negociacion.paquete.boundary;

import co.conexia.negociacion.services.negociacion.control.EliminarTecnologiasAuditoriaControl;
import co.conexia.negociacion.services.negociacion.paquete.control.PaquetesNegociacionControl;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.PaqueteNegociacionDto;
import com.conexia.contratacion.commons.dto.util.PaquetePortafolioServicioSaludDto;
import co.conexia.negociacion.services.negociacion.portafolio.control.PortafolioControl;
import com.conexia.negociacion.definitions.negociacion.paquete.NegociacionPaqueteTransactionalServiceRemote;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;

/**
 * Boundary para los servicios de Negociar Paquetes.
 * @author jtorres
 *
 */
@Stateless
@Remote(NegociacionPaqueteTransactionalServiceRemote.class)
public class NegociacionPaqueteTransactionalBoundary implements NegociacionPaqueteTransactionalServiceRemote{

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private PaquetesNegociacionControl paqueteControl;

    @Inject
    private PortafolioControl portafolioControl;

    @Inject
    private EliminarTecnologiasAuditoriaControl eliminarTecnologiasAuditoriaControl;


    /**
     * Elimina varias SedesNegociacionPaquete dada la id de la negociación y el paquete en cuestión.
     * @param negociacionId
     * @param paqueteId
     * @return un entero que indica el resultado de la operación
     */
    public Integer eliminarByNegociacionAndPaquete(final Long negociacionId, final Long paqueteId, Integer userId){
        StringBuilder queryAuditoria = new StringBuilder();
        queryAuditoria.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarPaquetes())
                .append(" where sn.negociacion_id = :negociacionId and snp.paquete_id = :paqueteId");

        //Para registrar en auditoría los paquetes a eliminar
        em.createNativeQuery(queryAuditoria.toString())
                .setParameter("userId", userId)
                .setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId)
                .executeUpdate();

        Integer cantidadEliminados =  em.createNamedQuery("SedeNegociacionPaquete.deleteByNegociacionAndPaquete")
                .setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteId)
                .executeUpdate();

        em.createNamedQuery("PaquetePortafolio.eliminarPaquetePortafolio")
                .setParameter("paqueteId", paqueteId)
                .executeUpdate();

        return cantidadEliminados;
    }

    /**
     * Función que guarda una lista de paquetes en una SedeNegociación dada la negociación y la lista
     * de paquetes.
     * @param paquetes la lista de paquetes a persistir.
     * @param negociacionId la negociación a la que pertenece esos paquetes.
     */
    public void guardarPaquetesNegociados(List<PaqueteNegociacionDto> paquetes, Long negociacionId, Integer userId){
        //Se recorren los paquetes a persistir...
        for (PaqueteNegociacionDto paqueteNegociacion : paquetes) {
            em.createNamedQuery("SedeNegociacionPaquete.updateByNegociacionAndPaquetes")
                    .setParameter("valorNegociado", paqueteNegociacion.getValorNegociado())
                    .setParameter("negociado", paqueteNegociacion.getNegociado())
                    .setParameter("negociacionId", negociacionId)
                    .setParameter("paqueteId", paqueteNegociacion.getPaquetePortafolioDto().getPortafolio().getId())
                    .setParameter("userId", userId)
                    .executeUpdate();
        }
    }

    /**
     * Se almacena el valor del paquete negociado
     * @param paqueteNegociacion
     * @param negociacionId
     * @param userId
     */
    public void guardarPaqueteNegociado(PaqueteNegociacionDto paqueteNegociacion, Long negociacionId, Integer userId){
        em.createNamedQuery("SedeNegociacionPaquete.updateByNegociacionAndPaquetes")
                .setParameter("valorNegociado", paqueteNegociacion.getValorNegociado())
                .setParameter("negociado", paqueteNegociacion.getNegociado())
                .setParameter("negociacionId", negociacionId)
                .setParameter("paqueteId", paqueteNegociacion.getPaquetePortafolioDto().getPortafolio().getId())
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void agregarPaquetesNegociacion(List<PaquetePortafolioServicioSaludDto> sedesPrestador, NegociacionDto negociacion, Integer userId) {

        for (PaquetePortafolioServicioSaludDto dato : sedesPrestador) {
            List paquetesBase = new ArrayList<>();
            paquetesBase.add(dato.getPaquetePortafolio().getId());
            List sedesIds = new ArrayList<>();
            sedesIds.add(dato.getSedePrestador().getId());
            List paquetes = new ArrayList();
            paquetes.add(dato.getPaquetePortafolio());

            this.portafolioControl.crearPortafolioASedePrestadorSinPortafolio(dato.getSedePrestador().getId());
            this.paqueteControl.registrarPaquetesBaseComoPaquetePropio(paquetesBase, sedesIds, negociacion, userId);
            this.paqueteControl.agregarAnexos(negociacion, paquetes);

        }

    }
}
