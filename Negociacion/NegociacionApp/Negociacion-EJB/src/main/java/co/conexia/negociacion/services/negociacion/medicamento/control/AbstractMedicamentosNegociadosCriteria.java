package co.conexia.negociacion.services.negociacion.medicamento.control;

import com.conexia.contratacion.commons.constants.enums.TipoErrorTecnologiaEnum;
import com.conexia.contractual.model.contratacion.SedePrestador;
import com.conexia.contractual.model.contratacion.negociacion.SedeNegociacionMedicamento;
import com.conexia.contractual.model.contratacion.negociacion.SedeNegociacionMedicamento_;
import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion;
import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion_;
import com.conexia.contractual.model.maestros.Medicamento;
import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.logfactory.Log;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class AbstractMedicamentosNegociadosCriteria {

    @PersistenceContext(unitName = "contractualDS")
    protected EntityManager em;
    @Inject
    protected Log log;

    protected CriteriaBuilder criteriaBuilder;
    protected CriteriaQuery<Tuple> criteriaQuery;
    protected Root<SedesNegociacion> sedesNegociacion;
    protected Join<SedesNegociacion, SedePrestador> sedePrestador;
    protected ListJoin<SedesNegociacion, SedeNegociacionMedicamento> sedeNegociacionMedicamento;
    protected Join<SedeNegociacionMedicamento, Medicamento> medicamento;

    protected List<ErroresTecnologiasDto> crearCriteriosParaObtenerLosMedicamentos(NegociacionDto negociacion) {
        criteriaBuilder = em.getCriteriaBuilder();
        criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
        sedesNegociacion = criteriaQuery.from(SedesNegociacion.class);
        sedePrestador = sedesNegociacion.join(SedesNegociacion_.sedePrestador, JoinType.INNER);
        sedeNegociacionMedicamento = sedesNegociacion.join(SedesNegociacion_.sedeNegociacionMedicamentos, JoinType.INNER);
        medicamento = sedeNegociacionMedicamento.join(SedeNegociacionMedicamento_.medicamento, JoinType.INNER);
        return crearCondicionesParaObtenerLosMedicamentos(negociacion);
    }

    protected List<ErroresTecnologiasDto> convertir(List<Tuple> resultadoTuple, TipoErrorTecnologiaEnum tipoErrorTecnologiaEnum) {
        return resultadoTuple.stream().map(mapTupleToDto(tipoErrorTecnologiaEnum)).collect(Collectors.toList());
    }

    protected abstract List<ErroresTecnologiasDto> crearCondicionesParaObtenerLosMedicamentos(NegociacionDto negociacion);

    protected abstract Function<Tuple, ErroresTecnologiasDto> mapTupleToDto(TipoErrorTecnologiaEnum tipoErrorTecnologiaEnum);

    protected static class TupleColumnas {
        public static final String CODIGO_EMSSANAR = "codigoEmssanar";
        public static final String CODIGO_HABILITACION = "codigoHabilitacion";
        public static final String DESCRIPCION_TECNOLOGIA = "descripcionTecnologia";
        public static final String DESCRIPCION_RIA = "descrpcionRia";
        public static final String DESCRIPCION_ACTIVIDAD = "descripcionActividad";
    }
}
