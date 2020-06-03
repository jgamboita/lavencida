package co.conexia.negociacion.services.negociacion.control;

import com.conexia.contratacion.commons.constants.enums.CodigoMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;
import org.hibernate.exception.DataException;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.transaction.Transactional;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class MedicamentoNegociacionControl implements CopiarTecnologias, DerogacionTecnologias {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private Log log;

    private static <T> T convertirResultado(Object listA, Class<T> clazz) {
        if (clazz.isInstance(listA))
            return clazz.cast(listA);
        throw new ClassCastException("No se puede convertir la clase");
    }

    @Override
    @Transactional(Transactional.TxType.REQUIRES_NEW)
    public void copiarTecnologias(NegociacionDto negociacionActual, NegociacionDto negociacionAnterior) throws CopiarTecnologiaException {
        try {
            String resultado = convertirResultado(
                    this.em.createNamedStoredProcedureQuery("copiarMedicamentosPorNegociacion")
                            .setParameter("negociacionAntualId", negociacionActual.getId())
                            .setParameter("negociacionAnteriorId", negociacionAnterior.getId())
                            .getOutputParameterValue("resultado"),
                    String.class
            );
            Optional.ofNullable(resultado).orElseThrow(RuntimeException::new);
        } catch (DataException e) {
            this.log.error("Se presento un error al copiar los procedimientos de una negociacion", e);
            throw new CopiarTecnologiaException(CodigoMensajeErrorEnum.ERROR_INSERT_DATABASE);
        } catch (PersistenceException e) {
            this.log.error("Se presento un error con el contexto de persistencia", e);
            throw new CopiarTecnologiaException(CodigoMensajeErrorEnum.ERROR_INSERT_DATABASE);
        } catch (Exception e){
            this.log.error("Se presento un error general al copiar los procedimientos", e);
            throw new CopiarTecnologiaException(CodigoMensajeErrorEnum.ERROR_INSERT_DATABASE);
        }
    }

    @Override
    public void revertirCopiadoTecnologias(NegociacionDto negociacionActual, NegociacionDto negociacionAnterior) {
        try {
            String resultado = convertirResultado(
                    this.em.createNamedStoredProcedureQuery("revertirCopiadoMedicamentosPorCopiado")
                            .setParameter("negociacionAntualId", negociacionActual.getId())
                            .setParameter("negociacionAnteriorId", negociacionAnterior.getId())
                            .getOutputParameterValue("resultado"),
                    String.class
            );
            Optional.ofNullable(resultado).orElseThrow(RuntimeException::new);
        } catch (DataException e) {
            this.log.error("Se presento un error al reverir el copiado de los procedimientos", e);
            throw new ConexiaSystemException(CodigoMensajeErrorEnum.SYSTEM_ERROR);
        } catch (PersistenceException e) {
            this.log.error("Se presento un error con el contexto de persistencia", e);
            throw new ConexiaSystemException(CodigoMensajeErrorEnum.SYSTEM_ERROR);
        } catch (Exception e){
            this.log.error("Se presento un error general al copiar los procedimientos", e);
            throw new ConexiaSystemException(CodigoMensajeErrorEnum.SYSTEM_ERROR);
        }
    }

    @Override
    public List<String> contieneDerogados(NegociacionDto negociacionDto) {
        return new ArrayList<>();
    }
}
