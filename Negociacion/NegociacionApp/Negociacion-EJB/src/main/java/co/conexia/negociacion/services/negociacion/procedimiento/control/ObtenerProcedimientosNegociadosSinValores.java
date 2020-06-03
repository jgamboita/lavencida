package co.conexia.negociacion.services.negociacion.procedimiento.control;

import co.conexia.negociacion.services.negociacion.ValoresNegociadosInvalidosException;
import com.conexia.contratacion.commons.constants.enums.TipoErrorTecnologiaEnum;
import com.conexia.contractual.model.contratacion.SedePrestador_;
import com.conexia.contractual.model.contratacion.ServicioSalud_;
import com.conexia.contractual.model.contratacion.negociacion.Negociacion_;
import com.conexia.contractual.model.contratacion.negociacion.SedeNegociacionProcedimiento_;
import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion_;
import com.conexia.contractual.model.maestros.Procedimiento_;
import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.logfactory.Log;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import java.util.List;

public class ObtenerProcedimientosNegociadosSinValores extends AbstractProcedimientosNegociadosCriteria {

    public ObtenerProcedimientosNegociadosSinValores() {
    }

    public ObtenerProcedimientosNegociadosSinValores(EntityManager em, Log log) {
        this.em = em;
        this.log = log;
    }

    public List<ErroresTecnologiasDto> obtenerProcedimientos(NegociacionDto negociacion) {
        try {
            return crearCriteriosParaObtenerLosProcedimientos(negociacion);
        } catch (PersistenceException e) {
            log.error("Se presentó un error de persistencia al obtener los paquetes sin valores negociados ", e);
            throw new ValoresNegociadosInvalidosException();
        } catch (Exception e) {
            log.error("Se presentó un error de inesperado al obtener los paquetes sin valores negociados ", e);
            throw new ValoresNegociadosInvalidosException();
        }
    }

    @Override
    protected List<ErroresTecnologiasDto> crearCondicionesParaObtenerLosProcedimientos(NegociacionDto negociacion) {
        criteriaQuery.multiselect(
                procedimiento.get(Procedimiento_.CODIGO_EMSSANAR).alias(TupleColumnas.CODIGO_EMSSANAR),
                criteriaBuilder.concat(sedePrestador.get(SedePrestador_.CODIGO_HABILITACION), sedePrestador.get(SedePrestador_.CODIGO_SEDE)).alias(TupleColumnas.CODIGO_HABILITACION),
                servicioSalud.get(ServicioSalud_.CODIGO).alias(TupleColumnas.CODIGO_SERVICIO),
                procedimiento.get(Procedimiento_.DESCRIPCION).alias(TupleColumnas.DESCRIPCION_TECNOLOGIA)
        ).where(
                criteriaBuilder.equal(sedesNegociacion.get(SedesNegociacion_.negociacion).get(Negociacion_.id), negociacion.getId()),
                criteriaBuilder.and(
                        criteriaBuilder.or(
                                sedeNegociacionProcedimiento.get(SedeNegociacionProcedimiento_.VALOR_NEGOCIADO).isNull(),
                                criteriaBuilder.equal(sedeNegociacionProcedimiento.get(SedeNegociacionProcedimiento_.VALOR_NEGOCIADO), 0)
                        )
                )
        );
        List<Tuple> resultadoTuple = em.createQuery(criteriaQuery).getResultList();
        return convertir(resultadoTuple, TipoErrorTecnologiaEnum.SIN_VALORES_NEGOCIADOS);
    }
}
