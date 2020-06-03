package co.conexia.negociacion.services.bandeja.comite.control;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import co.conexia.negociacion.services.bandeja.comite.boundary.BandejaComiteViewBoundary;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorComiteEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contractual.model.contratacion.Trazabilidad;
import com.conexia.contractual.model.contratacion.comite.ComitePrecontratacion;
import com.conexia.contractual.model.contratacion.comite.PrestadorComite;
import com.conexia.contractual.model.security.User;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;

/**
 * Control para las acciones adicionales que necesite el boundary
 * {@link BandejaComiteViewBoundary}
 *
 * @author jtorres
 *
 */
public class BandejaComiteControl {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    /**
     * Realiza la busqueda de prestadores que no tengan asociado un comite.
     *
     * @param comite
     * @param prestadores prestadores a los que se les va asignar el comite
     * @return Lista de prestadores encontrados, <b>null</b> en el caso que no
     * se encuentre.
     */
    public int actualizarPrestadoresPendientesComite(ComitePrecontratacion comite, 
            List<Long> prestadores) {
        
        List<EstadoPrestadorComiteEnum> estadosPrestadorComite = new ArrayList<>();
        estadosPrestadorComite.add(EstadoPrestadorComiteEnum.PENDIENTE_COMITE);
        estadosPrestadorComite.add(EstadoPrestadorComiteEnum.APLAZADO);
        return em.createNamedQuery("PrestadorComite.updatePrestadoresrSinComite")
                .setParameter("comiteId", comite.getId())
                .setParameter("nuevoEstado", EstadoPrestadorComiteEnum.PROGRAMADO)
                .setParameter("estadosPrestadorComite", estadosPrestadorComite)
                .setParameter("fechaAsociarComite", new Date())
                .setParameter("ids", prestadores)
                .executeUpdate();
    }
    
    /**
     * Actualiza el estado prestador que tienen un comite
     * @param comite
     * @return 
     */
    public int actualizarPrestadoresPendientesComite(ComitePrecontratacion comite) {        
        return em.createNamedQuery("Prestador.updateEstadoPorEstadoSolicitudYComiteId")
                .setParameter("estadoPrestador", EstadoPrestadorEnum.PROPUESTO_COMITE)
                .setParameter("comiteId", comite.getId())
                .setParameter("estadoSolicitud", EstadoPrestadorComiteEnum.PROGRAMADO)
                .executeUpdate();
    }
    
    /**
     * Crea un nuevo comite
     * @param nuevo {@link - ComitePrecontratacionDto}
     */
    public ComitePrecontratacion crearComite(ComitePrecontratacionDto nuevo){
        ComitePrecontratacion comite = new ComitePrecontratacion();
        comite.setFechaComite(nuevo.getFechaComite());
        comite.setFechaLimitePrestadores(nuevo.getFechaLimitePrestadores());
        comite.setCantidadPrestadores(nuevo.getCantidadPrestadores());
        comite.setLimitePrestadores(nuevo.getLimitePrestadores());
        comite.setEstadoComite(nuevo.getEstadoComite());
        em.persist(comite);
        return comite;
    }

    /**
     * Crea una trazabilidad al comite
     * @param comite Comite 
     * @param usuarioId usuario que realiza la accion
     * @return
     */
    public Trazabilidad crearTrazabilidadComite(ComitePrecontratacion comite,
            Integer usuarioId) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        // Inicio de registro de trazabilidad
        
        Trazabilidad traza = new Trazabilidad();
        traza.setDescripcion("Nuevo comité.");
        traza.setValorNuevo("[Estado= "
                + comite.getEstadoComite().name() + ", Fecha comite="
                + dateFormat.format(comite.getFechaComite()) + "]");
        traza.setLlavePrimaria(comite.getId());
        traza.setNombreTabla("contratacion.comite_precontratacion");
        traza.setFechaModificacion(new Date());
        traza.setUsuario(new User(usuarioId));
        em.persist(traza);
        
        return traza;
    }
    
    /**
     * Asigna prestadoresComite a un comite
     * @param comiteId Identificador del comite
     * @param prestadoresId Identificadores de {@link PrestadorComite}
     * @param estadoPrestador Estado del prestador en el comite
     * @return numero de registros actualizados
     */
    public int asignarPrestadoresAComite(Long comiteId, List<Long> prestadoresId,
            EstadoPrestadorComiteEnum estadoPrestador) {
        return em
                .createNamedQuery(
                        "PrestadorComite.asignarPrestadoresComiteById")
                .setParameter("comiteId", comiteId)
                .setParameter("nuevoEstado", estadoPrestador)
                .setParameter("fechaAsociarComite", new Date())
                .setParameter("ids", prestadoresId).executeUpdate();
    }
}
