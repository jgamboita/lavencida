package co.conexia.negociacion.services.bandeja.prestador.filtros;

import co.conexia.negociacion.filtros.FiltroTableLazyProvider;
import com.conexia.contratacion.commons.constants.enums.EstadoMaestroEnum;
import com.conexia.contratacion.commons.constants.enums.TipoTecnologiaEnum;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.model.contratacion.Prestador_;
import com.conexia.contractual.model.contratacion.SedePrestador;
import com.conexia.contractual.model.contratacion.SedePrestador_;
import com.conexia.contractual.model.contratacion.negociacion.*;
import com.conexia.contractual.model.maestros.TipoIdentificacion;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaPrestadorDto;

import javax.persistence.criteria.*;
import java.util.*;
import java.util.stream.Collectors;

public class FiltroPrestadorTableLazyProvider extends FiltroTableLazyProvider<FiltroBandejaPrestadorDto, Prestador> {

    public static final String PORCENTAJE = "%";
    private FiltroBandejaPrestadorDto filtroBandejaPrestador;

    FiltroPrestadorTableLazyProvider(CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder, Root<Prestador> root) {
        super(criteriaQuery, criteriaBuilder, root);
    }

    FiltroPrestadorTableLazyProvider(CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder, Root<Prestador> root, Join<?, ?>... joins) {
        super(criteriaQuery, criteriaBuilder, root, joins);
    }

    @Override
    public void setFiltro(FiltroBandejaPrestadorDto filtroBandejaPrestador) {
        this.filtroBandejaPrestador = filtroBandejaPrestador;
    }

    FiltroBandejaPrestadorDto getFiltroBandejaPrestador() {
        return filtroBandejaPrestador;
    }

    Predicate obtenerFiltroPorTipoDocumento() {
        TipoIdentificacion tipoIdentificacion = new TipoIdentificacion();
        tipoIdentificacion.setId(this.filtroBandejaPrestador.getTipoIdentificacionSeleccionado().getId());
        return this.criteriaBuilder.equal(this.root.get("tipoIdentificacion"), tipoIdentificacion);
    }

    Predicate obtenerFiltroPorEstadoActivo() {
        return this.criteriaBuilder.equal(this.root.get("estado"), EstadoMaestroEnum.ACTIVO);
    }

    Predicate obtenerFiltroPorNumeroDocumento() {
        return this.criteriaBuilder.like(this.root.get("numeroDocumento"), this.filtroBandejaPrestador.getNumeroIdentificacion());
    }

    Predicate obtenerFiltroPorNumeroDocumentoInterno() {
        return this.criteriaBuilder.like(this.root.get("numeroDocumento"), PORCENTAJE.concat(((String) this.filtroBandejaPrestador.getFiltros().get("numeroDocumento"))).concat(PORCENTAJE));
    }

    Predicate obtenerFiltroPorNombrePrestador() {
        return this.criteriaBuilder.like(this.root.get("nombre"), PORCENTAJE.concat(this.filtroBandejaPrestador.getNombrePrestador().toUpperCase()).concat(PORCENTAJE));
    }

    Predicate obtenerFiltroPorNombrePrestadorInterno() {
        return this.criteriaBuilder.like(this.root.get("nombre"), PORCENTAJE.concat(((String) this.filtroBandejaPrestador.getFiltros().get("nombre")).toUpperCase()).concat(PORCENTAJE));
    }

    Predicate obtenerFiltroPorPrefijo() {
        return this.criteriaBuilder.like(this.root.get("prefijo"), this.filtroBandejaPrestador.getPrefijo());
    }

    Predicate obtenerFiltroPorPrefijoInterno() {
        return this.criteriaBuilder.like(this.root.get("prefijo"), PORCENTAJE.concat(((String) this.filtroBandejaPrestador.getFiltros().get("prefijo"))).concat(PORCENTAJE));
    }

    Predicate obtenerFiltroPorNumeroNegociacion() {
        Subquery<Long> subquery = this.criteriaQuery.subquery(Long.class);
        Root<Negociacion> negociacionEntity = subquery.from(Negociacion.class);
        subquery.select(negociacionEntity.get("id")).where(this.criteriaBuilder.and(
                this.criteriaBuilder.equal(negociacionEntity.get("prestador"), this.root.get("id")),
                this.criteriaBuilder.equal(negociacionEntity.get("id"), this.filtroBandejaPrestador.getNumeroNegociacion()))
        );
        return this.criteriaBuilder.exists(subquery);
    }

    Predicate obtenerFiltroPorIdPrestador() {
        return this.criteriaBuilder.equal(this.root.get("id"), this.filtroBandejaPrestador.getFiltros().get("id"));
    }

    Predicate obtenerFiltroPorModalidad() {
        Subquery<Long> subquery = this.criteriaQuery.subquery(Long.class);
        Root<Negociacion> negociacionEntity = subquery.from(Negociacion.class);
        subquery.select(negociacionEntity.get("id")).where(this.criteriaBuilder.and(
                this.criteriaBuilder.equal(negociacionEntity.get("prestador"), this.root.get("id")),
                this.criteriaBuilder.equal(negociacionEntity.get("tipoModalidadNegociacion"), this.filtroBandejaPrestador.getTipoModalidad()))
        );
        return this.criteriaBuilder.exists(subquery);
    }

    Predicate obtenerFiltroPorEstadoDeSede() {
        return this.criteriaBuilder.equal(this.joins[0].get("estado"), EstadoMaestroEnum.ACTIVO);
    }

    Predicate obtenerFiltroPorSedesContadas() {
        Subquery<Long> subquery = this.criteriaQuery.subquery(Long.class);
        Root<SedePrestador> sedePrestador = subquery.from(SedePrestador.class);
        subquery.select(
                this.criteriaBuilder.count(sedePrestador.get(SedePrestador_.ID))
        ).where(this.criteriaBuilder.and(
                this.criteriaBuilder.equal(sedePrestador.get(SedePrestador_.prestador).get(Prestador_.ID), this.root.get("id")),
                this.criteriaBuilder.equal(sedePrestador.get(SedePrestador_.ESTADO), EstadoMaestroEnum.ACTIVO))
        ).groupBy(
                sedePrestador.get(SedePrestador_.prestador).get(Prestador_.ID)
        ).having(
                this.criteriaBuilder.equal(this.criteriaBuilder.count(sedePrestador.get(SedePrestador_.ID)), this.filtroBandejaPrestador.getFiltros().get("sedes"))
        );
        return this.criteriaBuilder.exists(subquery);
    }

    public List<Predicate> obtenerFiltroPorTecnologias() {
        List<Predicate> predicates = new ArrayList<>();
        filtroBandejaPrestador.getTiposTecnologiasSeleccionados()
                .forEach(tiposTecnologiasSeleccionado -> obtenerTipoTecnologia(predicates, tiposTecnologiasSeleccionado));
        return predicates;
    }

    public Collection<? extends Predicate> obtenerFiltroPorTecnologiasInternas() {
        List<Predicate> predicates = new ArrayList<>();
        Object[] arreglo = (Object[]) filtroBandejaPrestador.getFiltros().get("tiposTecnologias");
        for (Object tiposTecnologiasSeleccionado : arreglo) {
            obtenerTipoTecnologia(predicates, (TipoTecnologiaEnum) tiposTecnologiasSeleccionado);
        }
        return predicates;
    }

    private void obtenerTipoTecnologia(List<Predicate> predicates, TipoTecnologiaEnum tiposTecnologiasSeleccionado) {
        switch (tiposTecnologiasSeleccionado) {
            case PROCEDIMIENTOS:
                predicates.add(existeProcedimientosNegociados());
                break;
            case MEDICAMENTOS:
                predicates.add(existeMedicamentosNegociados());
                break;
            case PAQUETES:
                predicates.add(existePaquetesNegociados());
                break;
        }
    }

    private Predicate existePaquetesNegociados() {
        Subquery<Long> subqueryPaquete = this.criteriaQuery.subquery(Long.class);
        Root<SedeNegociacionPaquete> sedeNegociacionPaquete = subqueryPaquete.from(SedeNegociacionPaquete.class);
        Join<SedeNegociacionPaquete, SedesNegociacion> sedesNegociacion = sedeNegociacionPaquete.join(SedeNegociacionPaquete_.sedeNegociacion, JoinType.INNER);
        subqueryPaquete.select(
                sedeNegociacionPaquete.get(SedeNegociacionServicio_.ID)
        ).where(
                this.criteriaBuilder.equal(sedesNegociacion.get(SedesNegociacion_.sedePrestador).get(SedePrestador_.ID), this.joins[0].get(SedePrestador_.ID))
        );
        return criteriaBuilder.exists(subqueryPaquete);
    }

    private Predicate existeMedicamentosNegociados() {
        Subquery<Long> subqueryMedicamento = this.criteriaQuery.subquery(Long.class);
        Root<SedeNegociacionMedicamento> sedeNegociacionMedicamento = subqueryMedicamento.from(SedeNegociacionMedicamento.class);
        Join<SedeNegociacionMedicamento, SedesNegociacion> sedesNegociacion = sedeNegociacionMedicamento.join(SedeNegociacionMedicamento_.sedeNegociacion, JoinType.INNER);
        subqueryMedicamento.select(
                sedeNegociacionMedicamento.get(SedeNegociacionServicio_.ID)
        ).where(
                this.criteriaBuilder.equal(sedesNegociacion.get(SedesNegociacion_.sedePrestador).get(SedePrestador_.ID), this.joins[0].get(SedePrestador_.ID))
        );
        return criteriaBuilder.exists(subqueryMedicamento);
    }

    private Predicate existeProcedimientosNegociados() {
        Subquery<Long> subqueryProcedimiento = this.criteriaQuery.subquery(Long.class);
        Root<SedeNegociacionServicio> sedeNegociacionServicio = subqueryProcedimiento.from(SedeNegociacionServicio.class);
        Join<SedeNegociacionServicio, SedesNegociacion> sedesNegociacionProcedimiento = sedeNegociacionServicio.join(SedeNegociacionServicio_.SEDE_NEGOCIACION, JoinType.INNER);
        subqueryProcedimiento.select(
                sedeNegociacionServicio.get(SedeNegociacionServicio_.ID)
        ).where(
                this.criteriaBuilder.equal(sedesNegociacionProcedimiento.get(SedesNegociacion_.sedePrestador).get(SedePrestador_.ID), this.joins[0].get(SedePrestador_.ID))
        );
        return criteriaBuilder.exists(subqueryProcedimiento);
    }
}
