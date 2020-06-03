package co.conexia.negociacion.services.negociacion.paquete.control;

import com.conexia.contratacion.commons.constants.enums.TipoErrorTecnologiaEnum;
import com.conexia.contractual.model.contratacion.SedePrestador;
import com.conexia.contractual.model.contratacion.negociacion.SedeNegociacionPaquete;
import com.conexia.contractual.model.contratacion.negociacion.SedeNegociacionPaquete_;
import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion;
import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion_;
import com.conexia.contractual.model.contratacion.portafolio.PaquetePortafolio;
import com.conexia.contractual.model.contratacion.portafolio.Portafolio;
import com.conexia.contractual.model.contratacion.portafolio.Portafolio_;
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

public abstract class AbstractPaquetesNegociadosCriteria {

    @PersistenceContext(unitName = "contractualDS")
    protected EntityManager em;
    @Inject
    protected Log log;

    protected CriteriaBuilder criteriaBuilder;
    protected CriteriaQuery<Tuple> criteriaQuery;
    protected Root<SedesNegociacion> sedesNegociacion;
    protected Join<SedesNegociacion, SedePrestador> sedePrestador;
    protected ListJoin<SedesNegociacion, SedeNegociacionPaquete> sedeNegociacionPaquete;
    protected Join<SedeNegociacionPaquete, Portafolio> portafolio;
    protected ListJoin<Portafolio, PaquetePortafolio> paquetePortafolio;


    protected List<ErroresTecnologiasDto> crearCriteriosParaObtenerLosPaquetes(NegociacionDto negociacion){
        criteriaBuilder = em.getCriteriaBuilder();
        criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
        sedesNegociacion = criteriaQuery.from(SedesNegociacion.class);
        sedePrestador = sedesNegociacion.join(SedesNegociacion_.sedePrestador, JoinType.INNER);
        sedeNegociacionPaquete = sedesNegociacion.join(SedesNegociacion_.sedeNegociacionPaquetes, JoinType.INNER);
        portafolio = sedeNegociacionPaquete.join(SedeNegociacionPaquete_.paquete, JoinType.INNER);
        paquetePortafolio = portafolio.join(Portafolio_.paquetePortafolios, JoinType.INNER);
        return crearCondicionesParaObtenerLosPaquetes(negociacion);
    }

    protected abstract List<ErroresTecnologiasDto> crearCondicionesParaObtenerLosPaquetes(NegociacionDto negociacion);

    protected List<ErroresTecnologiasDto> convertir(List<Tuple> resultadoTuple, TipoErrorTecnologiaEnum tipoErrorTecnologiaEnum) {
        return resultadoTuple.stream().map(mapTupleToDto(tipoErrorTecnologiaEnum)).collect(Collectors.toList());
    }

    protected Function<Tuple, ErroresTecnologiasDto> mapTupleToDto(TipoErrorTecnologiaEnum tipoErrorTecnologiaEnum) {
        return tuple -> new ErroresTecnologiasDto(
                tipoErrorTecnologiaEnum,
                tuple.get(TupleColumnas.CODIGO_EMSSANAR, String.class),
                tuple.get(TupleColumnas.CODIGO_HABILITACION, String.class),
                tuple.get(TupleColumnas.DESCRIPCION_TECNOLOGIA, String.class)
        );
    }

    protected static class TupleColumnas {
        public static final String CODIGO_EMSSANAR = "codigoEmssanar";
        public static final String CODIGO_HABILITACION = "codigoHabilitacion";
        public static final String DESCRIPCION_TECNOLOGIA = "descripcionTecnologia";
    }
}
