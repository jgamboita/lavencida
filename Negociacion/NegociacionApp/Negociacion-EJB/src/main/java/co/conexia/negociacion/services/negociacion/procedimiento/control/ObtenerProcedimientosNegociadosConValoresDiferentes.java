package co.conexia.negociacion.services.negociacion.procedimiento.control;

import co.conexia.negociacion.services.negociacion.ValoresNegociadosDiferentesException;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TipoErrorTecnologiaEnum;
import com.conexia.contractual.model.CriteriaBuilderFunctions;
import com.conexia.contractual.model.contratacion.SedePrestador_;
import com.conexia.contractual.model.contratacion.ServicioSalud;
import com.conexia.contractual.model.contratacion.ServicioSalud_;
import com.conexia.contractual.model.contratacion.negociacion.*;
import com.conexia.contractual.model.maestros.Procedimiento_;
import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.logfactory.Log;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.JoinType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ObtenerProcedimientosNegociadosConValoresDiferentes extends AbstractProcedimientosNegociadosCriteria{

    public static final int POSICION = 1;
    public static final int CANTIDAD_MINIMA_PERMITIDA = 1;
    public static final String DELIMITADOR = " - ";

    public ObtenerProcedimientosNegociadosConValoresDiferentes() {
    }

    public ObtenerProcedimientosNegociadosConValoresDiferentes(EntityManager em, Log log) {
        this.em = em;
        this.log = log;
    }

    public List<ErroresTecnologiasDto> obtenerProcedimientos(NegociacionDto negociacion) {
        try {
            return crearCriteriosParaObtenerLosProcedimientos(negociacion);
        } catch (PersistenceException e) {
            log.error("Se presentó un error de persistencia al obtener los paquetes sin valores negociados ", e);
            throw new ValoresNegociadosDiferentesException();
        } catch (Exception e) {
            log.error("Se presentó un error de inesperado al obtener los paquetes sin valores negociados ", e);
            throw new ValoresNegociadosDiferentesException();
        }
    }

    @Override
    protected List<ErroresTecnologiasDto> crearCondicionesParaObtenerLosProcedimientos(NegociacionDto negociacion) {
        if (Objects.equals(negociacion.getTipoModalidadNegociacion(), NegociacionModalidadEnum.EVENTO)) {
            return crearCriteriosParaObtenerLosMedicamentosEvento(negociacion);
        }
        return Collections.emptyList();
    }

    private List<ErroresTecnologiasDto> crearCriteriosParaObtenerLosMedicamentosEvento(NegociacionDto negociacion) {
        Join<SedeNegociacionServicio, ServicioSalud> servicioSalud = sedeNegociacionServicio.join(SedeNegociacionServicio_.servicioSalud, JoinType.INNER);
        criteriaQuery.multiselect(
                procedimiento.get(Procedimiento_.CODIGO_EMSSANAR).alias(TupleColumnas.CODIGO_EMSSANAR),
                criteriaBuilder.concat(sedePrestador.get(SedePrestador_.CODIGO_HABILITACION), sedePrestador.get(SedePrestador_.CODIGO_SEDE)).alias(TupleColumnas.CODIGO_HABILITACION),
                CriteriaBuilderFunctions.functionStringAgg(criteriaBuilder, servicioSalud.get(ServicioSalud_.CODIGO), DELIMITADOR).alias(TupleColumnas.CODIGO_SERVICIO),
                procedimiento.get(Procedimiento_.DESCRIPCION).alias(TupleColumnas.DESCRIPCION_TECNOLOGIA)
        ).where(
                criteriaBuilder.equal(sedesNegociacion.get(SedesNegociacion_.negociacion).get(Negociacion_.id), negociacion.getId())
        ).groupBy(
                sedesNegociacion.get(SedesNegociacion_.negociacion).get(Negociacion_.ID),
                procedimiento.get(Procedimiento_.ID),
                criteriaBuilder.concat(sedePrestador.get(SedePrestador_.CODIGO_HABILITACION), sedePrestador.get(SedePrestador_.CODIGO_SEDE))
        ).having(
                criteriaBuilder.gt(
                        CriteriaBuilderFunctions.functionArrayLength(criteriaBuilder,
                                CriteriaBuilderFunctions.functionArrayAgg(criteriaBuilder,
                                        sedeNegociacionProcedimiento.get(SedeNegociacionProcedimiento_.VALOR_NEGOCIADO)
                                ), POSICION
                        ), CANTIDAD_MINIMA_PERMITIDA)
        );
        List<Tuple> resultadoTuple = em.createQuery(criteriaQuery).getResultList();
        return convertir(resultadoTuple, TipoErrorTecnologiaEnum.PROCEDIMIENTOS_CON_VALORES_DIFERENTES);
    }
}
