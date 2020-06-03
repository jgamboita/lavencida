package co.conexia.negociacion.services.negociacion.medicamento.control;

import com.conexia.contratacion.commons.constants.enums.TipoErrorTecnologiaEnum;
import com.conexia.contractual.model.contratacion.negociacion.*;
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

public abstract class AbstractTecnologiaSinValorCriteria {

    @PersistenceContext(unitName = "contractualDS")
    protected EntityManager em;
    @Inject
    protected Log log;

    protected CriteriaBuilder criteriaBuilder;
    protected CriteriaQuery<Tuple> criteriaQuery;
    protected Root<Negociacion> negociacionRoot;

    protected List<ErroresTecnologiasDto> crearCriteriosParaObtenerLosMedicamentos(NegociacionDto negociacion) {
        criteriaBuilder = em.getCriteriaBuilder();
        criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
        negociacionRoot = criteriaQuery.from(Negociacion.class);
        return crearCondicionesParaObtenerLosMedicamentos(negociacion);
    }

    protected List<ErroresTecnologiasDto> convertir(List<Tuple> resultadoTuple, TipoErrorTecnologiaEnum tipoErrorTecnologiaEnum) {
        return resultadoTuple.stream()
                .map(mapTupleToDto(tipoErrorTecnologiaEnum))
                .collect(Collectors.toList());
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
