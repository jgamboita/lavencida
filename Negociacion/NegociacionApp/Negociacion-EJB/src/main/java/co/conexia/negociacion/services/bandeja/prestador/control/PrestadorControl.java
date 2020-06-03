package co.conexia.negociacion.services.bandeja.prestador.control;

import co.conexia.negociacion.services.bandeja.prestador.filtros.FiltroPrestadorManager;
import com.conexia.contratacion.commons.constants.enums.EstadoMaestroEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.model.contratacion.SedePrestador;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaPrestadorDto;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.logfactory.Log;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.Tuple;
import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

public class PrestadorControl {

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;

    @Inject
    private Log logger;

    @Inject
    private FiltroPrestadorManager filtroPrestadorManager;

    public Long contarPrestadores(FiltroBandejaPrestadorDto filtroBandejaPrestador) {
        try {
            return contarPrestadoresActivos(filtroBandejaPrestador);
        } catch (PersistenceException e) {
            this.logger.error("Error con la persistencia. ", e);
            throw new ConexiaSystemException("Se presentó un error al intentar contar los prestadores");
        }
    }

    public List<PrestadorDto> obtenerPrestadores(FiltroBandejaPrestadorDto filtroBandejaPrestador) {
        try {
            return obtenerPrestadoresActivosConTecnologias(filtroBandejaPrestador);
        } catch (PersistenceException e) {
            this.logger.error("Error con la persistencia. ", e);
            throw new ConexiaSystemException("Se presentó un error al intentar obtener los prestadores");
        }
    }

    private List<PrestadorDto> obtenerPrestadoresActivosConTecnologias(FiltroBandejaPrestadorDto filtroBandejaPrestador) {
        List<PrestadorDto> prestadoresActivos = obtenerPrestadoresActivos(filtroBandejaPrestador);
        if(prestadoresActivos.isEmpty()){
            return new ArrayList<>();
        }
        List<PrestadorDto> prestadores = obtenerCantidadesTecnologiasPorPrestador(prestadoresActivos);
        return prestadoresActivos.stream()
                .filter(prestadorDto -> prestadores.stream().anyMatch(prestadorDto1 -> Objects.equals(prestadorDto1.getId(), prestadorDto.getId())))
                .peek(prestadorDto -> {
                    Optional<PrestadorDto> primerPrestador = prestadores.stream()
                            .filter(prestadorDto1 -> Objects.equals(prestadorDto1.getId(), prestadorDto.getId()))
                            .findFirst();
                    primerPrestador.ifPresent(prestadorDto1 -> prestadorDto.setTiposTecnologias(prestadorDto1.getTiposTecnologias()));
                }).collect(Collectors.toList());
    }

    private List<PrestadorDto> obtenerCantidadesTecnologiasPorPrestador(List<PrestadorDto> prestadoresActivos) {
        return em.createQuery("select new com.conexia.contratacion.commons.dto.maestros.PrestadorDto(p.id,  " +
                    "(select count(sns.id) from SedeNegociacionServicio sns inner join sns.sedeNegociacion sn inner join sn.sedePrestador sp where sp.prestador.id = p.id and sp.estado = ?1), " +
                    "(select count(sns.id) from SedeNegociacionMedicamento sns inner join sns.sedeNegociacion sn inner join sn.sedePrestador sp where sp.prestador.id = p.id and sp.estado = ?1),  " +
                    "(select count(sns.id) from SedeNegociacionPaquete sns inner join sns.sedeNegociacion sn inner join sn.sedePrestador sp where sp.prestador.id = p.id and sp.estado = ?1)) " +
                    "from Prestador p where p.id in (?2)", PrestadorDto.class)
                    .setParameter(1, EstadoMaestroEnum.ACTIVO)
                    .setParameter(2, prestadoresActivos.stream().map(PrestadorDto::getId).collect(Collectors.toList()))
                    .getResultList();
    }

    private Long contarPrestadoresActivos(FiltroBandejaPrestadorDto filtro) {
        CriteriaBuilder criteriaBuilder = this.em.getCriteriaBuilder();
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Root<Prestador> prestador = criteriaQuery.from(Prestador.class);
        Join<Prestador, SedePrestador> sedePrestador = prestador.join("sedePrestador", JoinType.INNER);

        List<Predicate> predicados = this.filtroPrestadorManager.inicializar(criteriaQuery, criteriaBuilder, prestador, sedePrestador)
                .setFiltroPrestador(filtro).obtenerFiltros();

        criteriaQuery.select(criteriaBuilder.countDistinct(prestador))
                .where(predicados.stream().filter(Objects::nonNull).toArray(Predicate[]::new));
        return em.createQuery(criteriaQuery).getSingleResult();
    }

    private List<PrestadorDto> obtenerPrestadoresActivos(FiltroBandejaPrestadorDto filtro) {
        CriteriaBuilder criteriaBuilder = this.em.getCriteriaBuilder();
        CriteriaQuery<Tuple> criteriaQuery = criteriaBuilder.createQuery(Tuple.class);
        Root<Prestador> prestador = criteriaQuery.from(Prestador.class);
        Join<Prestador, SedePrestador> sedePrestador = prestador.join("sedePrestador", JoinType.INNER);

        List<Predicate> predicados = this.filtroPrestadorManager.inicializar(criteriaQuery, criteriaBuilder, prestador, sedePrestador)
                .setFiltroPrestador(filtro).obtenerFiltros();

        criteriaQuery.multiselect(
                prestador.get("id").alias("prestadorId"),
                prestador.get("nombre").alias("nombre"),
                prestador.get("numeroDocumento").alias("numeroDocumento"),
                prestador.get("prefijo").alias("prefijo"),
                prestador.get("tipoIdentificacion").get("id").alias("tipoIdentificacionId"),
                prestador.get("fechaInicioVigencia").alias("fechaInicioVigencia"),
                prestador.get("mesesVigencia").alias("mesesVigencia"),
                prestador.get("estadoPrestador").alias("estadoPrestador"),
                criteriaBuilder.count(sedePrestador.get("id")).alias("totalSedes")
        ).where(
                predicados.stream().filter(Objects::nonNull).toArray(Predicate[]::new)
        ).groupBy(
                prestador.get("id"),
                prestador.get("nombre"),
                prestador.get("numeroDocumento"),
                prestador.get("prefijo"),
                prestador.get("tipoIdentificacion").get("id"),
                prestador.get("fechaInicioVigencia"),
                prestador.get("mesesVigencia"),
                prestador.get("estadoPrestador")
        );
        List<Tuple> resultadoTuple = em.createQuery(criteriaQuery)
                .setMaxResults(filtro.getCantidadRegistros())
                .setFirstResult(filtro.getPagina())
                .getResultList();
        return convertir(resultadoTuple);
    }

    private List<PrestadorDto> convertir(List<Tuple> resultadoTuple) {
        return resultadoTuple.stream().map(tuple -> new PrestadorDto(
                tuple.get("prestadorId", Long.class),
                tuple.get("nombre", String.class),
                tuple.get("numeroDocumento", String.class),
                tuple.get("prefijo", String.class),
                tuple.get("totalSedes", Long.class),
                tuple.get("fechaInicioVigencia", Date.class),
                tuple.get("mesesVigencia", Integer.class),
                0L,
                0L,
                0L,
                0L,
                tuple.get("estadoPrestador", EstadoPrestadorEnum.class)
        )).collect(Collectors.toList());
    }
}
