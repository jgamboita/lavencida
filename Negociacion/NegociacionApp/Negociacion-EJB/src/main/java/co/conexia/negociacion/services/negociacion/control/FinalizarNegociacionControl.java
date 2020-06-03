package co.conexia.negociacion.services.negociacion.control;

import com.conexia.contratacion.commons.constants.enums.ComplejidadNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoNegociacionEnum;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.logfactory.Log;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.util.Date;
import java.util.Objects;

public class FinalizarNegociacionControl {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;
    @Inject
    private ConexiaExceptionUtils conexiaExceptionsUtils;
    @Inject
    private Log log;

    public void finalizar(NegociacionDto negociacion) {
        try {
            actualizarPoblacionDistribucionServicio(negociacion);
            finalizarNegociacion(negociacion);
        } catch (PersistenceException e) {
            log.error("Se presentó un error de persistencia al finalizar la negociación ", e);
            throw conexiaExceptionsUtils.createSystemErrorException();
        } catch (Exception e) {
            log.error("Se presentó un error inesperado al finalizar la negociación ", e);
            throw conexiaExceptionsUtils.createSystemErrorException();
        }
    }

    private void finalizarNegociacion(NegociacionDto negociacion) {
        em.createNamedQuery("Negociacion.finalizarNegociacionById")
                .setParameter("idNegociacion", negociacion.getId())
                .setParameter("estadoNegociacion", EstadoNegociacionEnum.FINALIZADA)
                .setParameter("porcentajeTotalUpc", negociacion.getPorcentajeTotalUpc())
                .setParameter("valorUpcAnual", negociacion.getValorUpcAnual())
                .setParameter("valorTotal", negociacion.getValorTotal())
                .setParameter("fechaConcertacionMx", Objects.nonNull(negociacion.getFechaConcertacionMx()) ? negociacion.getFechaConcertacionMx() : new Date())
                .setParameter("fechaConcertacionPx", Objects.nonNull(negociacion.getFechaConcertacionPx()) ? negociacion.getFechaConcertacionPx() : new Date())
                .setParameter("fechaConcertacionPq", Objects.nonNull(negociacion.getFechaConcertacionPq()) ? negociacion.getFechaConcertacionPq() : new Date())
                .executeUpdate();
    }

    private void actualizarPoblacionDistribucionServicio(NegociacionDto negociacion) {
        if (Objects.equals(ComplejidadNegociacionEnum.BAJA, negociacion.getComplejidad()) && Objects.equals(Boolean.TRUE, negociacion.getPoblacionServicio())) {
            em.createNamedQuery("Negociacion.updatePoblacionServiciosById")
                    .setParameter("negociacionId", negociacion.getId())
                    .executeUpdate();
        }
    }
}
