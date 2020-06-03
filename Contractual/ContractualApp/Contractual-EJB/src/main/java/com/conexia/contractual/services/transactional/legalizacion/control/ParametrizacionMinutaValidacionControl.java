package com.conexia.contractual.services.transactional.legalizacion.control;

import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.MinutaDetalleDto;
import com.conexia.exceptions.ConexiaBusinessException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.util.List;
import javax.inject.Inject;

/**
 * Control que permite realizar las respectivas validaciones de la creacion de
 * la solicitud de autorizacion.
 *
 * @author jalvarado
 */
public class ParametrizacionMinutaValidacionControl {

    /**
     * Contexto de Persistencia.
     */
    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    public String generarTextoHijos(List<MinutaDetalleDto> detalles, final Long padreId) {
        StringBuilder texto = new StringBuilder();
        for (MinutaDetalleDto d : detalles) {
            if (padreId.equals(d.getPadreId())) {
                texto.append(d.getDescripcion());
            }
        }
        return texto.toString();
    }

    public void eliminarOrdinalMinutaDetalle(final MinutaDetalleDto minutaDetalleDto) throws ConexiaBusinessException {
        this.eliminarMinutaDetalle(minutaDetalleDto);
        this.ordenarMinutaDetalle(minutaDetalleDto);
    }

    private void eliminarMinutaDetalle(final MinutaDetalleDto minutaDetalle) throws ConexiaBusinessException {
        try {
            em.createNamedQuery("MinutaDetalle.borrarMinutaDetalle")
                    .setParameter("padreId", minutaDetalle.getId())
                    .setParameter("detalleId", minutaDetalle.getId())
                    .executeUpdate();
        } catch (final Exception e) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.ACTUALIZAR_ORDINAL_MINUTA_DETALLE);
        }

    }

    /**
     * Actualiza el orden de la minuta.
     *
     * @param minutaDetalleDto minuta seleccionada.
     * @throws ConexiaBusinessException
     */
    public void actualizarOrdinalMinutaDetalle(final MinutaDetalleDto minutaDetalleDto) throws ConexiaBusinessException {
        this.actualizarOrdinalPadreMinutaDetalle(minutaDetalleDto);
        this.ordenarMinutaDetalle(minutaDetalleDto);
    }

    /**
     * Funcion que permite actualizar el ordinal del minuta detalle.
     *
     * @param minutaDetalleDto
     * @throws com.conexia.exceptions.ConexiaBusinessException
     */
    private void ordenarMinutaDetalle(final MinutaDetalleDto minutaDetalleDto) throws ConexiaBusinessException {
        try {
            int index = 1;
            if (minutaDetalleDto.getMinutaDetalleAnteriorDto() != null) {
                List<MinutaDetalleDto> minutasDetalleAnterior = this.listarMinutaDetalleAnterior(minutaDetalleDto.getMinutaDetalleAnteriorDto());

                for (MinutaDetalleDto minDetalleDto : minutasDetalleAnterior) {
                    if (!(index == minDetalleDto.getOrdinal())) {
                        minDetalleDto.setOrdinal(index);
                        this.actualizarOrdinalPadreMinutaDetalle(minDetalleDto);
                    }
                    index = index + 1;
                }
            }
            index = 1;
            int ordinalNuevo = minutaDetalleDto.getOrdinal();
            List<MinutaDetalleDto> minutasDetalleNueva = this.listarMinutaDetalleAnterior(minutaDetalleDto);
            for (MinutaDetalleDto minDetalleDto : minutasDetalleNueva) {
                if (!(index == minDetalleDto.getOrdinal())) {
                    minDetalleDto.setOrdinal(index);
                    this.actualizarOrdinalPadreMinutaDetalle(minDetalleDto);
                } else {
                    if (index == minDetalleDto.getOrdinal() && index == ordinalNuevo) {
                        ordinalNuevo++;
                        minDetalleDto.setOrdinal(ordinalNuevo);
                        this.actualizarOrdinalPadreMinutaDetalle(minDetalleDto);
                    }
                }
                index = index + 1;
            }
        } catch (PersistenceException e) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.ACTUALIZAR_ORDINAL_MINUTA_DETALLE);
        }

    }

    private void actualizarOrdinalPadreMinutaDetalle(final MinutaDetalleDto minutaDetalleDto) {
        if (minutaDetalleDto.getPadreId() != null) {
            em.createNamedQuery("MinutaDetalle.updateOrdinalPadreIdById")
                    .setParameter("ordinal", minutaDetalleDto.getOrdinal())
                    .setParameter("padreId", minutaDetalleDto.getPadreId())
                    .setParameter("id", minutaDetalleDto.getId())
                    .executeUpdate();
        } else {
            em.createNamedQuery("MinutaDetalle.updateOrdinalById")
                    .setParameter("ordinal", minutaDetalleDto.getOrdinal())
                    .setParameter("id", minutaDetalleDto.getId())
                    .executeUpdate();
        }

    }

    private List<MinutaDetalleDto> listarMinutaDetalleAnterior(final MinutaDetalleDto minutaDetalleDtoAnt) {
        if (minutaDetalleDtoAnt.getPadreId() != null) {
            return em.createNamedQuery("MinutaDetalle.findByPadreAndDiffId", MinutaDetalleDto.class)
                .setParameter("id", minutaDetalleDtoAnt.getId())
                .setParameter("padreId", minutaDetalleDtoAnt.getPadreId())
                .setParameter("minutaId", minutaDetalleDtoAnt.getMinutaId().getId())
                .getResultList();
        }
        return em.createNamedQuery("MinutaDetalle.findByDiffId", MinutaDetalleDto.class)
                .setParameter("id", minutaDetalleDtoAnt.getId())
                .setParameter("minutaId", minutaDetalleDtoAnt.getMinutaId().getId())
                .getResultList();
    }

}
