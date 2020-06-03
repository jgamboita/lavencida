package co.conexia.negociacion.services.comite.control;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

import com.conexia.contratacion.commons.constants.enums.EstadoComiteEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorComiteEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.model.contratacion.comite.ComitePrecontratacion;
import com.conexia.contractual.model.contratacion.comite.PrestadorComite;
import com.conexia.contratacion.commons.dto.comite.AsistenteComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.ComitePrecontratacionDto;
import com.conexia.contratacion.commons.dto.comite.TablaPrestadoresComiteDto;

/**
 * Boundary que contiene las consultas utilizadas en la gestion de comites
 *
 * @author etorres
 */
public class GestionComiteControl {

    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;

    /**
     * Actualiza un asistente dado su comite al que pertenece
     *
     * @param asistente Asistente con los datos actualizados.
     * @param asistenteId La id del asistente a editar.
     */
    public void actualizarAsistenteComite(AsistenteComitePrecontratacionDto asistente, Long asistenteId) {
        em.createNamedQuery("AsistenteComitePrecontratacion.updateById")
                .setParameter("cargo", asistente.getCargo())
                .setParameter("nombres", asistente.getNombre())
                .setParameter("siAsiste", asistente.getSiAsiste())
                .setParameter("siTieneVoz", asistente.getSiTieneVoz())
                .setParameter("siTieneVoto", asistente.getSiTieneVoto())
                .setParameter("asistenteId", asistenteId)
                .executeUpdate();
    }

    /**
     *
     * @param estado
     * @param prestadorId
     * @return
     */
    public Integer actualizarEstadoPrestador(final EstadoPrestadorEnum estado, final Long prestadorId) {
        return em.createNamedQuery("Prestador.actualizarEstadoPrestador")
                .setParameter("estadoPrestador", estado)
                .setParameter("prestadorId", prestadorId)
                .executeUpdate();
    }

    /**
     * Actualiza el estado de un comite dado el comite y el estado.
     *
     * @param comiteId
     * @param estado
     */
    public void actualizarEstadoComite(final Long comiteId, final EstadoComiteEnum estado) {
        em.createNamedQuery("ComitePrecontratacion.updateEstadoById")
                .setParameter("comiteId", comiteId)
                .setParameter("estado", estado)
                .executeUpdate();
    }

    /**
     * Actualiza el estado de un prestador asociado a un comite dado el
     * prestador_comite y el estado.
     *
     * @param comiteId
     * @param estado
     */
    public void actualizarEstadoPrestadorComite(final Long prestadorComiteId, final EstadoPrestadorComiteEnum estado) {
        em.createNamedQuery("PrestadorComite.updateEstadoById")
                .setParameter("prestadorComiteId", prestadorComiteId)
                .setParameter("estado", estado)
                .executeUpdate();
    }

    /**
     * Metodo que devuelve la lista de los prestadores a evaluar en un comite
     * determinado
     *
     * @param comiteId
     * @return Lista de Prestadores
     */
    public List<TablaPrestadoresComiteDto> buscarPrestadoresComiteByComiteId(Long comiteId) {
        return em.createNamedQuery("PrestadorComite.buscarPrestadorComiteByComiteId", TablaPrestadoresComiteDto.class)
                .setParameter("comiteId", comiteId)
                .getResultList();
    }

    /**
     * Busca un comite válido disponible en el sistema
     *
     * @return Un comite con la id asociada o <b>null</b> en el caso que no
     * encuentre el comite.
     */
    public ComitePrecontratacionDto buscarComiteDisponible() {
        try {
            return em.createNamedQuery(
                    "ComitePrecontratacion.existeDisponible",
                    ComitePrecontratacionDto.class)
                    .setMaxResults(1)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Valida y actualiza los estados de los prestadores y los actualiza de
     * acuerdo a kas reglas del comite.
     *
     * @param prestadoresComite Lista de prestadores asociados a un comite.
     * @return Lista de prstadores con los estados actualizados.
     */
    public List<TablaPrestadoresComiteDto> manejarEstadosPrestadoresComiteAplazado(List<TablaPrestadoresComiteDto> prestadoresComite) {
        if (prestadoresComite != null) {
            for (TablaPrestadoresComiteDto dto : prestadoresComite) {
                if (dto.getEstado().equals(EstadoPrestadorComiteEnum.PROGRAMADO)) {
                    dto.setEstado(EstadoPrestadorComiteEnum.APLAZADO);
                    this.actualizarEstadoPrestadorComite(dto.getIdPrestadorComite(), EstadoPrestadorComiteEnum.APLAZADO);
                }
            }
        }
        return prestadoresComite;
    }

    /**
     * Aplaza los prestadores de un comite dado el comite disponible a donde
     * moverlos.
     *
     * @param prestadoresComite
     * @param comiteDisponibleId La Id del comite nuevo.
     * @param limitePrestadores El limite de prestadores a asignar para el
     * comite disponible.
     */
    public void aplazarPrestadoresComite(List<TablaPrestadoresComiteDto> prestadoresComite, Long comiteDisponibleId, int limitePrestadores) {
        List<Long> prestadoresComiteIds = new ArrayList<Long>();
        int count = 0;
        //Se crea el comite disponible para asociarlo a los prestadores.
        ComitePrecontratacion comiteDisponible = new ComitePrecontratacion(comiteDisponibleId);

        for (int i = 0; i < prestadoresComite.size(); i++) {
            TablaPrestadoresComiteDto dto = prestadoresComite.get(i);
            if (count <= limitePrestadores) {
                prestadoresComiteIds.add(dto.getIdPrestadorComite());
                PrestadorComite prestadorComite = new PrestadorComite();
                prestadorComite.setComite(comiteDisponible);
                prestadorComite.setConceptoComite(dto.getConceptoComite());
                prestadorComite.setEstado(EstadoPrestadorComiteEnum.PROGRAMADO);
                prestadorComite.setFechaAsociacionComite(new Date());
                prestadorComite.setJustificacionEnvioComite(dto.getJustificacionEnvioComite());
                Prestador p = new Prestador(dto.getIdPrestador());
                prestadorComite.setPrestador(p);
                prestadorComite.setTipoTecnologiasPrestador(dto.getTipoTecnologias());
                em.persist(prestadorComite);
                count++;
            } else {
                break;
            }
        }
    }

    /**
     * Libera o activa el portafolio de un prestador seleccionado. Lo deja sin
     * estado y sin fecha de vijencia y mes de vigencia del portafolio.
     *
     * @param prestadorId
     */
    public void liberarPortafolioPrestadorNegado(Long prestadorId) {
        em.createNamedQuery("Prestador.activarPortafolioPrestador")
                .setParameter("estadoSinEstadoPrestador", null)
                .setParameter("sinFechaVigencia", null)
                .setParameter("sinMesVigencia", null)
                .setParameter("prestadorId", prestadorId)
                .executeUpdate();
    }
}
