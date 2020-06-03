package co.conexia.negociacion.services.negociacion.paquete.control;

import co.conexia.negociacion.services.negociacion.ValoresNegociadosInvalidosException;
import com.conexia.contratacion.commons.constants.enums.TipoErrorTecnologiaEnum;
import com.conexia.contractual.model.contratacion.SedePrestador_;
import com.conexia.contractual.model.contratacion.negociacion.Negociacion_;
import com.conexia.contractual.model.contratacion.negociacion.SedeNegociacionPaquete_;
import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion_;
import com.conexia.contractual.model.contratacion.portafolio.PaquetePortafolio_;
import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.logfactory.Log;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import java.util.List;

public class ObtenerPaquetesNegociadosSinValores extends AbstractPaquetesNegociadosCriteria {

    public ObtenerPaquetesNegociadosSinValores() {
    }

    public ObtenerPaquetesNegociadosSinValores(EntityManager em, Log log) {
        this.em = em;
        this.log = log;
    }

    public List<ErroresTecnologiasDto> obtenerPaquetes(NegociacionDto negociacion) {
        try {
            return crearCriteriosParaObtenerLosPaquetes(negociacion);
        } catch (PersistenceException e) {
            log.error("Se presentó un error de persistencia al obtener los paquetes sin valores negociados ", e);
            throw new ValoresNegociadosInvalidosException();
        } catch (Exception e) {
            log.error("Se presentó un error de inesperado al obtener los paquetes sin valores negociados ", e);
            throw new ValoresNegociadosInvalidosException();
        }
    }

    @Override
    protected List<ErroresTecnologiasDto> crearCondicionesParaObtenerLosPaquetes(NegociacionDto negociacion) {
        criteriaQuery.multiselect(
                paquetePortafolio.get(PaquetePortafolio_.codigoPortafolio).alias(TupleColumnas.CODIGO_EMSSANAR),
                criteriaBuilder.concat(sedePrestador.get(SedePrestador_.CODIGO_HABILITACION), sedePrestador.get(SedePrestador_.CODIGO_SEDE)).alias(TupleColumnas.CODIGO_HABILITACION),
                paquetePortafolio.get(PaquetePortafolio_.DESCRIPCION).alias(TupleColumnas.DESCRIPCION_TECNOLOGIA)
        ).where(
                criteriaBuilder.equal(sedesNegociacion.get(SedesNegociacion_.negociacion).get(Negociacion_.id), negociacion.getId()),
                criteriaBuilder.and(
                        criteriaBuilder.or(
                                sedeNegociacionPaquete.get(SedeNegociacionPaquete_.VALOR_NEGOCIADO).isNull(),
                                criteriaBuilder.equal(sedeNegociacionPaquete.get(SedeNegociacionPaquete_.VALOR_NEGOCIADO), 0)
                        )
                )
        );
        List<Tuple> resultadoTuple = em.createQuery(criteriaQuery).getResultList();
        return convertir(resultadoTuple, TipoErrorTecnologiaEnum.SIN_VALORES_NEGOCIADOS);
    }
}
