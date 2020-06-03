package co.conexia.negociacion.services.negociacion.medicamento.control;

import co.conexia.negociacion.services.negociacion.TecnologiasInactivasException;
import com.conexia.contratacion.commons.constants.enums.TipoErrorTecnologiaEnum;
import com.conexia.contractual.model.contratacion.SedePrestador_;
import com.conexia.contractual.model.contratacion.negociacion.*;
import com.conexia.contractual.model.maestros.*;
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
import java.util.function.Function;

public class ObtenerMedicamentosNegociadosConTecnologiasInactivas extends AbstractMedicamentosNegociadosCriteria implements ObtenerMedicamentosNegociados {

    private NegociacionDto negociacion;

    public ObtenerMedicamentosNegociadosConTecnologiasInactivas() {
    }

    public ObtenerMedicamentosNegociadosConTecnologiasInactivas(EntityManager em, Log log) {
        this.em = em;
        this.log = log;
    }

    public List<ErroresTecnologiasDto> obtenerMedicamentos(NegociacionDto negociacion) {
        try {
            return crearCriteriosParaObtenerLosMedicamentos(negociacion);
        } catch (PersistenceException e) {
            log.error("Se presentó un error de persistencia al obtener los paquetes sin valores negociados ", e);
            throw new TecnologiasInactivasException();
        } catch (Exception e) {
            log.error("Se presentó un error de inesperado al obtener los paquetes sin valores negociados ", e);
            throw new TecnologiasInactivasException();
        }
    }

    @Override
    protected List<ErroresTecnologiasDto> crearCondicionesParaObtenerLosMedicamentos(NegociacionDto negociacion) {
        this.negociacion = negociacion;
        switch (negociacion.getTipoModalidadNegociacion()) {
            case EVENTO:
                return crearCriteriosParaObtenerLosMedicamentosEvento(negociacion);
            case RIAS_CAPITA:
                return crearCriteriosParaObtenerLosMedicamentosRiasCapita(negociacion);
            default:
                return Collections.emptyList();
        }
    }

    private List<ErroresTecnologiasDto> crearCriteriosParaObtenerLosMedicamentosRiasCapita(NegociacionDto negociacion) {
        Join<SedeNegociacionMedicamento, Actividad> actividad = sedeNegociacionMedicamento.join(SedeNegociacionMedicamento_.actividad, JoinType.INNER);
        Join<SedeNegociacionMedicamento, NegociacionRiaRangoPoblacion> negociacionRiaRangoPoblacion = sedeNegociacionMedicamento.join(SedeNegociacionMedicamento_.negociacionRiaRangoPoblacional, JoinType.INNER);
        Join<NegociacionRiaRangoPoblacion, NegociacionRia> negociacionRia = negociacionRiaRangoPoblacion.join(NegociacionRiaRangoPoblacion_.negociacionRia, JoinType.INNER);
        Join<NegociacionRia, Ria> ria = negociacionRia.join(NegociacionRia_.ria, JoinType.INNER);

        criteriaQuery.multiselect(
                medicamento.get(Medicamento_.CUMS).alias(TupleColumnas.CODIGO_EMSSANAR),
                criteriaBuilder.concat(sedePrestador.get(SedePrestador_.CODIGO_HABILITACION), sedePrestador.get(SedePrestador_.CODIGO_SEDE)).alias(TupleColumnas.CODIGO_HABILITACION),
                medicamento.get(Medicamento_.DESCRIPCION).alias(TupleColumnas.DESCRIPCION_TECNOLOGIA),
                ria.get(Ria_.DESCRIPCION).alias(TupleColumnas.DESCRIPCION_RIA),
                actividad.get(Actividad_.DESCRIPCION).alias(TupleColumnas.DESCRIPCION_ACTIVIDAD)
        ).where(
                criteriaBuilder.equal(sedesNegociacion.get(SedesNegociacion_.negociacion).get(Negociacion_.id), negociacion.getId()),
                criteriaBuilder.and(
                        criteriaBuilder.notEqual(medicamento.get(Medicamento_.ESTADO_MEDICAMENTO), 1)
                )
        );
        List<Tuple> resultadoTuple = em.createQuery(criteriaQuery).getResultList();
        return convertir(resultadoTuple, TipoErrorTecnologiaEnum.MEDICAMENTOS_INACTIVAS);
    }

    private List<ErroresTecnologiasDto> crearCriteriosParaObtenerLosMedicamentosEvento(NegociacionDto negociacion) {
        criteriaQuery.multiselect(
                medicamento.get(Medicamento_.CUMS).alias(TupleColumnas.CODIGO_EMSSANAR),
                medicamento.get(Medicamento_.DESCRIPCION).alias(TupleColumnas.DESCRIPCION_TECNOLOGIA)
        ).where(
                criteriaBuilder.equal(sedesNegociacion.get(SedesNegociacion_.negociacion).get(Negociacion_.id), negociacion.getId()),
                criteriaBuilder.and(
                        criteriaBuilder.notEqual(medicamento.get(Medicamento_.ESTADO_MEDICAMENTO), 1)
                )
        ).groupBy(
                medicamento.get(Medicamento_.CUMS),
                medicamento.get(Medicamento_.DESCRIPCION)
        );
        List<Tuple> resultadoTuple = em.createQuery(criteriaQuery).getResultList();
        return convertir(resultadoTuple, TipoErrorTecnologiaEnum.MEDICAMENTOS_INACTIVAS);
    }

    protected Function<Tuple, ErroresTecnologiasDto> mapTupleToDto(TipoErrorTecnologiaEnum tipoErrorTecnologiaEnum) {
        switch (negociacion.getTipoModalidadNegociacion()) {
            case EVENTO:
                return tuple -> new ErroresTecnologiasDto(
                        tipoErrorTecnologiaEnum,
                        tuple.get(TupleColumnas.CODIGO_EMSSANAR, String.class),
                        "",
                        tuple.get(TupleColumnas.DESCRIPCION_TECNOLOGIA, String.class)
                );
            case RIAS_CAPITA:
                return tuple -> new ErroresTecnologiasDto(
                        tipoErrorTecnologiaEnum,
                        tuple.get(TupleColumnas.CODIGO_EMSSANAR, String.class),
                        tuple.get(TupleColumnas.CODIGO_HABILITACION, String.class),
                        tuple.get(TupleColumnas.DESCRIPCION_TECNOLOGIA, String.class),
                        tuple.get(TupleColumnas.DESCRIPCION_RIA, String.class),
                        tuple.get(TupleColumnas.DESCRIPCION_ACTIVIDAD, String.class)
                );
            default:
                throw new IllegalArgumentException("La opción no esta soportada para obtener medicamentos");
        }
    }
}
