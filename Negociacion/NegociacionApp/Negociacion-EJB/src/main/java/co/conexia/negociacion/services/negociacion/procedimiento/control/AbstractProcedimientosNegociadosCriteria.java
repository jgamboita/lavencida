package co.conexia.negociacion.services.negociacion.procedimiento.control;

import com.conexia.contratacion.commons.constants.enums.TipoErrorTecnologiaEnum;
import com.conexia.contractual.model.contratacion.SedePrestador;
import com.conexia.contractual.model.contratacion.ServicioSalud;
import com.conexia.contractual.model.contratacion.negociacion.*;
import com.conexia.contractual.model.maestros.Procedimiento;
import com.conexia.contractual.model.maestros.Procedimientos;
import com.conexia.contractual.model.maestros.Procedimientos_;
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

public abstract class AbstractProcedimientosNegociadosCriteria {

    @PersistenceContext(unitName = "contractualDS")
    protected EntityManager em;
    @Inject
    protected Log log;

    protected CriteriaBuilder criteriaBuilder;
    protected CriteriaQuery<Tuple> criteriaQuery;
    protected Root<SedesNegociacion> sedesNegociacion;
    protected Join<SedesNegociacion, SedePrestador> sedePrestador;
    protected ListJoin<SedesNegociacion, SedeNegociacionServicio> sedeNegociacionServicio;
    protected ListJoin<SedeNegociacionServicio, SedeNegociacionProcedimiento> sedeNegociacionProcedimiento;
    protected Join<SedeNegociacionProcedimiento, Procedimientos> procedimientoServicio;
    protected Join<Procedimientos, Procedimiento> procedimiento;
    protected Join<Procedimientos, ServicioSalud> servicioSalud;

    protected List<ErroresTecnologiasDto> crearCriteriosParaObtenerLosProcedimientos(NegociacionDto negociacion){
        criteriaBuilder = em.getCriteriaBuilder();
        criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
        sedesNegociacion = criteriaQuery.from(SedesNegociacion.class);
        sedePrestador = sedesNegociacion.join(SedesNegociacion_.sedePrestador, JoinType.INNER);
        sedeNegociacionServicio = sedesNegociacion.join(SedesNegociacion_.sedeNegociacionServicios, JoinType.INNER);
        sedeNegociacionProcedimiento = sedeNegociacionServicio.join(SedeNegociacionServicio_.sedeNegociacionProcedimientos, JoinType.INNER);
        procedimientoServicio = sedeNegociacionProcedimiento.join(SedeNegociacionProcedimiento_.procedimiento, JoinType.INNER);
        procedimiento = procedimientoServicio.join(Procedimientos_.procedimiento, JoinType.INNER);
        servicioSalud = procedimientoServicio.join(Procedimientos_.servicioSalud, JoinType.INNER);
        return crearCondicionesParaObtenerLosProcedimientos(negociacion);
    }

    protected abstract List<ErroresTecnologiasDto> crearCondicionesParaObtenerLosProcedimientos(NegociacionDto negociacion);

    protected List<ErroresTecnologiasDto> convertir(List<Tuple> resultadoTuple, TipoErrorTecnologiaEnum tipoErrorTecnologiaEnum) {
        return resultadoTuple.stream().map(mapTupleToDto(tipoErrorTecnologiaEnum)).collect(Collectors.toList());
    }

    protected Function<Tuple, ErroresTecnologiasDto> mapTupleToDto(TipoErrorTecnologiaEnum tipoErrorTecnologiaEnum) {
        return tuple -> new ErroresTecnologiasDto(
                tipoErrorTecnologiaEnum,
                tuple.get(TupleColumnas.CODIGO_EMSSANAR, String.class),
                tuple.get(TupleColumnas.CODIGO_HABILITACION, String.class),
                tuple.get(TupleColumnas.CODIGO_SERVICIO, String.class),
                tuple.get(TupleColumnas.DESCRIPCION_TECNOLOGIA, String.class)
        );
    }

    protected static class TupleColumnas {
        public static final String CODIGO_EMSSANAR = "codigoEmssanar";
        public static final String CODIGO_HABILITACION = "codigoHabilitacion";
        public static final String DESCRIPCION_TECNOLOGIA = "descripcionTecnologia";
        public static final String CODIGO_SERVICIO = "codigoServicio";
    }
}
