package co.conexia.negociacion.services.bandeja.comite.boundary;

import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import co.conexia.negociacion.services.bandeja.comite.control.BandejaComiteControl;
import co.conexia.negociacion.services.common.control.NegociacionFileRepositoryControl;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorComiteEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contractual.model.contratacion.comite.ComitePrecontratacion;
import com.conexia.contractual.model.contratacion.comite.PrestadorComite;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.bandeja.comite.BandejaComiteTransactionalServiceRemote;

@Stateless
@Remote(BandejaComiteTransactionalServiceRemote.class)
public class BandejaComiteTransactionalBoundary implements BandejaComiteTransactionalServiceRemote {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private BandejaComiteControl bandejaControl;

    @Inject
    private NegociacionFileRepositoryControl fileControl;

    @Override
    public int guardarNuevoComite(ComitePrecontratacionDto nuevo, 
            List<Long> prestadores, Integer usuarioId) {
        
        //Crea un nuevo comite
        ComitePrecontratacion comite = this.bandejaControl.crearComite(nuevo);
        
        //Crea la trazibilidad
        this.bandejaControl.crearTrazabilidadComite(comite, usuarioId);
        
        //Se buscar asociar prestadores_comite que esten disponibles para el nuevo comite.
        if (comite != null && comite.getId() != null 
                && prestadores != null && !prestadores.isEmpty()) {
            int rows = bandejaControl.actualizarPrestadoresPendientesComite(
                    comite, prestadores);
            if (rows > 0) {
                this.bandejaControl.actualizarPrestadoresPendientesComite(comite);
                //Se debe actualizar los prestadores asociados al comite.
                comite.setCantidadPrestadores(rows);
                em.merge(comite);
            }
            return rows;
        }
        return 0;
    }

    /**
     * Actualiza los datos de un nuevo comité.
     *
     * @param comite
     */
    @Override
    public void actualizarComite(ComitePrecontratacionDto comite) {
        em.createNamedQuery("ComitePrecontratacion.updateDatesAndEstadoById")
                .setParameter("fechaComite", comite.getFechaComite())
                .setParameter("fechaLimitePrestadores", comite.getFechaLimitePrestadores())
                .setParameter("estadoComite", comite.getEstadoComite())
                .setParameter("comiteId", comite.getId())
                .executeUpdate();
    }

    /**
     * Realiza la carga de un acta asociada a un comite
     *
     * @param contenido
     * @param nombreArchivo
     * @return nombre del archivo en el servidor
     */
    @Override
    public String cargarActaComite(byte[] contenido, String nombreArchivo) {
        String nombreArchivoServidor = fileControl.nuevoArchivo(contenido,
                fileControl.getRutaRepositorioFromBD());
        return nombreArchivoServidor;
    }

    /**
     * Actualiza el nombre del acta guardad en el servidor a el comite
     * seleccionado al que se le carga el acta.
     *
     * @param comiteId
     * @param nombreActaServidor
     */
    @Override
    public void actualizarNombreActaComite(Long comiteId, String nombreActaServidor) {
        em.createNamedQuery("ComitePrecontratacion.updateActaServidor")
                .setParameter("comiteId", comiteId)
                .setParameter("nombreActaServidor", nombreActaServidor)
                .executeUpdate();
    }

    /**
     * Obtiene el contenido de un acta cargada en el servidor
     *
     * @param comiteId
     * @return contenido del acta en un arreglo de bytes
     */
    @Override
    public byte[] obtenerContenidoActa(Long comiteId) {
        String nombreActaServidor = (String) em.createNamedQuery("ComitePrecontratacion.obtenerNombreActaServidorById")
                .setParameter("comiteId", comiteId)
                .getSingleResult();

        byte[] contenidoActa = null;

        try {
            contenidoActa = fileControl.obteneArchivo(nombreActaServidor,
                    fileControl.getRutaRepositorioFromBD());
        } catch (ConexiaBusinessException ex) {
            Logger.getLogger(BandejaComiteTransactionalBoundary.class.getName()).log(Level.SEVERE, null, ex);
        }

        return contenidoActa;
    }

    /**
     * crea los prestadores comite y luego los asigna a un nuevo comite
     * @param comite Nuevo comite a ser creado
     * @param prestadores Lista de prestadores a ser asignados a un comite
     * @param usuarioId Usuario de la aplicacion
     */
    @Override
    public Long guardarComiteWithPrestadores(ComitePrecontratacionDto comite,
            List<PrestadorDto> prestadores, Integer usuarioId) {
        // Se asignan los prestadores
        List<PrestadorComite> prestadoresComite = prestadores.stream()
                .parallel()
                .map(new Function<PrestadorDto, PrestadorComite>() {
                    @Override
                    public PrestadorComite apply(PrestadorDto t) {
                        PrestadorComite pc = new PrestadorComite("", t.getId(), "");
                        pc.setEstado(EstadoPrestadorComiteEnum.APROBADO);
                        pc.setTipoModalidad(NegociacionModalidadEnum.CAPITA);
                        pc.setTipoTecnologiasPrestador(t.getTiposTecnologias());
                        return pc;
                    }
                }).collect(Collectors.toList());
        
        for(PrestadorComite p: prestadoresComite){
            em.persist(p);
        }
        
        List<Long> prestadoresId = prestadoresComite.stream().map(p -> p.getId())
                .collect(Collectors.toList());
        
        //Crea un nuevo comite
        ComitePrecontratacion nuevoComite = this.bandejaControl
                .crearComite(comite);
        
        //Crea la trazibilidad
        this.bandejaControl.crearTrazabilidadComite(nuevoComite, usuarioId);
        
        //Asigna los prestadores al comite
        this.bandejaControl.asignarPrestadoresAComite(nuevoComite.getId(),
                prestadoresId, EstadoPrestadorComiteEnum.APROBADO);
        
        return nuevoComite.getId();
        
    }

}
