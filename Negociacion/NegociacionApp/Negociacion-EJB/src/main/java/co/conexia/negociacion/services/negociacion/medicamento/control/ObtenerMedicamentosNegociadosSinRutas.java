package co.conexia.negociacion.services.negociacion.medicamento.control;

import com.conexia.contratacion.commons.constants.enums.TipoErrorTecnologiaEnum;
import com.conexia.contractual.model.contratacion.SedePrestador_;
import com.conexia.contractual.model.contratacion.negociacion.*;
import com.conexia.contractual.model.maestros.Medicamento_;
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

public class ObtenerMedicamentosNegociadosSinRutas extends AbstractMedicamentosNegociadosCriteria implements ObtenerMedicamentosNegociados {

    private NegociacionDto negociacion;

    public ObtenerMedicamentosNegociadosSinRutas() {
    }

    public ObtenerMedicamentosNegociadosSinRutas(EntityManager em, Log log) {
        this.em = em;
        this.log = log;
    }

    @Override
    public List<ErroresTecnologiasDto> obtenerMedicamentos(NegociacionDto negociacion) {
        try {
            return crearCriteriosParaObtenerLosMedicamentos(negociacion);
        } catch (PersistenceException e) {
            log.error("Se presentó un error de persistencia al obtener los paquetes sin valores negociados ", e);
            throw new TecnologiasSinRutasException();
        } catch (Exception e) {
            log.error("Se presentó un error de inesperado al obtener los paquetes sin valores negociados ", e);
            throw new TecnologiasSinRutasException();
        }
    }

    @Override
    protected List<ErroresTecnologiasDto> crearCondicionesParaObtenerLosMedicamentos(NegociacionDto negociacion) {
        this.negociacion = negociacion;
        switch (negociacion.getTipoModalidadNegociacion()) {
            case RIAS_CAPITA:
            case RIAS_CAPITA_GRUPO_ETAREO:
                return crearCondicionesParaObtenerLosMedicamentosSinRutas(negociacion);
            default:
                return Collections.emptyList();
        }
    }

    private List<ErroresTecnologiasDto> crearCondicionesParaObtenerLosMedicamentosSinRutas(NegociacionDto negociacion) {
        Join<SedeNegociacionMedicamento, NegociacionRiaRangoPoblacion> negociacionRiaRangoPoblacion = sedeNegociacionMedicamento.join(SedeNegociacionMedicamento_.negociacionRiaRangoPoblacional, JoinType.LEFT);

        criteriaQuery.multiselect(
                medicamento.get(Medicamento_.CUMS).alias(TupleColumnas.CODIGO_EMSSANAR),
                criteriaBuilder.concat(sedePrestador.get(SedePrestador_.CODIGO_HABILITACION), sedePrestador.get(SedePrestador_.CODIGO_SEDE)).alias(TupleColumnas.CODIGO_HABILITACION),
                medicamento.get(Medicamento_.DESCRIPCION).alias(TupleColumnas.DESCRIPCION_TECNOLOGIA)
        ).where(
                criteriaBuilder.equal(sedesNegociacion.get(SedesNegociacion_.negociacion).get(Negociacion_.ID), negociacion.getId()),
                criteriaBuilder.and(
                        criteriaBuilder.isNull(negociacionRiaRangoPoblacion.get(NegociacionRiaRangoPoblacion_.ID))
                )
        );
        List<Tuple> resultadoTuple = em.createQuery(criteriaQuery).getResultList();
        return convertir(resultadoTuple, TipoErrorTecnologiaEnum.SIN_RUTAS);
    }


    @Override
    protected Function<Tuple, ErroresTecnologiasDto> mapTupleToDto(TipoErrorTecnologiaEnum tipoErrorTecnologiaEnum) {
        return tuple -> new ErroresTecnologiasDto(
                tipoErrorTecnologiaEnum,
                tuple.get(TupleColumnas.CODIGO_EMSSANAR, String.class),
                tuple.get(TupleColumnas.CODIGO_HABILITACION, String.class),
                tuple.get(TupleColumnas.DESCRIPCION_TECNOLOGIA, String.class)
        );
    }

}
