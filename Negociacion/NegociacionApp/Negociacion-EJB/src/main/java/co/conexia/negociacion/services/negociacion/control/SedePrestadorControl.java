package co.conexia.negociacion.services.negociacion.control;

import com.conexia.contratacion.commons.constants.enums.EstadoMaestroEnum;
import com.conexia.contractual.model.contratacion.SedePrestador;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class SedePrestadorControl {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private Log logger;

    public Collection<? extends SedePrestadorDto> obtenerSedesPorPrestador(PrestadorDto prestador) {
        try {
            return obtenerSedesPorPrestadorCriterial(prestador);
        } catch (PersistenceException e) {
            this.logger.error("Error con la persistencia. ", e);
            throw new ConexiaSystemException("Se presentó un error al intentar obtener las sedes por el prestador");
        }
    }

    private Collection<? extends SedePrestadorDto> obtenerSedesPorPrestadorCriterial(PrestadorDto prestador) {
        CriteriaBuilder criteriaBuilder = this.em.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
        Root<SedePrestador> sedePrestador = criteriaQuery.from(SedePrestador.class);

        criteriaQuery.multiselect(
                sedePrestador.get("id").alias("sedePrestadorId"),
                sedePrestador.get("codigoHabilitacion").alias("codigoHabilitacion"),
                sedePrestador.get("codigoSede").alias("codigoSede"),
                sedePrestador.get("nombreSede").alias("nombreSede"),
                sedePrestador.get("municipio").get("departamento").get("id").alias("departamentoId"),
                sedePrestador.get("municipio").get("departamento").get("descripcion").alias("departamentoDescripcion"),
                sedePrestador.get("municipio").get("id").alias("municipioId"),
                sedePrestador.get("municipio").get("descripcion").alias("municipioDescripcion"),
                sedePrestador.get("zona").get("id").alias("zonaId"),
                sedePrestador.get("direccion").alias("direccion")
        ).where(
                criteriaBuilder.and(
                        criteriaBuilder.equal(sedePrestador.get("prestador").get("id"), prestador.getId()),
                        criteriaBuilder.equal(sedePrestador.get("estado"), EstadoMaestroEnum.ACTIVO)
                )
        );
        List<Tuple> resultadoTuple = em.createQuery(criteriaQuery).getResultList();

        return convertir(resultadoTuple);
    }

    private List<SedePrestadorDto> convertir(List<Tuple> resultadoTuple) {
        return resultadoTuple.stream().map(tuple -> new SedePrestadorDto(
                tuple.get("sedePrestadorId", Long.class),
                tuple.get("codigoHabilitacion", String.class),
                tuple.get("codigoSede", String.class),
                tuple.get("nombreSede", String.class),
                tuple.get("departamentoId", Integer.class),
                tuple.get("departamentoDescripcion", String.class),
                tuple.get("municipioId", Integer.class),
                tuple.get("municipioDescripcion", String.class),
                tuple.get("zonaId", Integer.class),
                tuple.get("direccion", String.class)
        )).collect(Collectors.toList());
    }
}
