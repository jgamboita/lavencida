package co.conexia.negociacion.services.bandeja.prestador.filtros;

import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaPrestadorDto;

import javax.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FiltroPrestadorManager {

    private FiltroPrestadorTableLazyProvider filtroProvider;
    private List<Predicate> condiciones = new ArrayList<>();

    public FiltroPrestadorManager inicializar(CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder, Root<Prestador> root, Join<?,?>... join) {
        this.filtroProvider = new FiltroPrestadorTableLazyProvider(criteriaQuery, criteriaBuilder, root, join);
        return this;
    }

    public FiltroPrestadorManager setFiltroPrestador(FiltroBandejaPrestadorDto filtroBandejaPrestadorDto) {
        this.filtroProvider.setFiltro(filtroBandejaPrestadorDto);
        return this;
    }

    public List<Predicate> obtenerFiltros() {
        condiciones.clear();
        filtroEstadoPrestador(condiciones);
        filtroEstadoSedePrestador(condiciones);
        filtroTipoModalidad(condiciones);
        filtroTipoIdentificacion(condiciones);
        filtroNumeroDocumento(condiciones);
        filtroNombrePrestador(condiciones);
        filtroPrefijo(condiciones);
        filtroNumeroNegociacion(condiciones);
        filtroContarTecnologias(condiciones);
        filtroIdPrestador(condiciones);
        filtroNombrePrestadorInterno(condiciones);
        filtroNumeroDocumentoInterno(condiciones);
        filtroPrefijoInterno(condiciones);
        filtroContarSedeInterno(condiciones);
        filtroContarTecnologiasInternas(condiciones);
        return condiciones;
    }

    private void filtroEstadoPrestador(List<Predicate> condiciones) {
        condiciones.add(this.filtroProvider.obtenerFiltroPorEstadoActivo());
    }

    private void filtroEstadoSedePrestador(List<Predicate> condiciones) {
        condiciones.add(this.filtroProvider.obtenerFiltroPorEstadoDeSede());
    }

    private void filtroNumeroNegociacion(List<Predicate> condiciones) {
        if (Objects.nonNull(this.filtroProvider.getFiltroBandejaPrestador().getNumeroNegociacion())) {
            condiciones.add(this.filtroProvider.obtenerFiltroPorNumeroNegociacion());
        }
    }

    private void filtroPrefijo(List<Predicate> condiciones) {
        if (Objects.nonNull(this.filtroProvider.getFiltroBandejaPrestador().getPrefijo())
                && !this.filtroProvider.getFiltroBandejaPrestador().getPrefijo().isEmpty()) {
            condiciones.add(this.filtroProvider.obtenerFiltroPorPrefijo());
        }
    }

    private void filtroPrefijoInterno(List<Predicate> condiciones) {
        if (this.filtroProvider.getFiltroBandejaPrestador().getFiltros().containsKey("prefijo")) {
            condiciones.add(this.filtroProvider.obtenerFiltroPorPrefijoInterno());
        }
    }

    private void filtroNombrePrestador(List<Predicate> condiciones) {
        if (Objects.nonNull(this.filtroProvider.getFiltroBandejaPrestador().getNombrePrestador())
                && !this.filtroProvider.getFiltroBandejaPrestador().getNombrePrestador().isEmpty()) {
            condiciones.add(this.filtroProvider.obtenerFiltroPorNombrePrestador());
        }
    }

    private void filtroNombrePrestadorInterno(List<Predicate> condiciones) {
        if (this.filtroProvider.getFiltroBandejaPrestador().getFiltros().containsKey("nombre")) {
            condiciones.add(this.filtroProvider.obtenerFiltroPorNombrePrestadorInterno());
        }
    }

    private void filtroNumeroDocumento(List<Predicate> condiciones) {
        if (Objects.nonNull(this.filtroProvider.getFiltroBandejaPrestador().getNumeroIdentificacion())
                && !this.filtroProvider.getFiltroBandejaPrestador().getNumeroIdentificacion().isEmpty()) {
            condiciones.add(this.filtroProvider.obtenerFiltroPorNumeroDocumento());
        }
    }

    private void filtroNumeroDocumentoInterno(List<Predicate> condiciones) {
        if (this.filtroProvider.getFiltroBandejaPrestador().getFiltros().containsKey("numeroDocumento")) {
            condiciones.add(this.filtroProvider.obtenerFiltroPorNumeroDocumentoInterno());
        }
    }

    private void filtroTipoIdentificacion(List<Predicate> condiciones) {
        if (Objects.nonNull(this.filtroProvider.getFiltroBandejaPrestador().getTipoIdentificacionSeleccionado())) {
            condiciones.add(this.filtroProvider.obtenerFiltroPorTipoDocumento());
        }
    }

    private void filtroTipoModalidad(List<Predicate> condiciones) {
        if (Objects.nonNull(this.filtroProvider.getFiltroBandejaPrestador().getTipoModalidad())) {
            condiciones.add(this.filtroProvider.obtenerFiltroPorModalidad());
        }
    }

    private void filtroIdPrestador(List<Predicate> condiciones) {
        if (this.filtroProvider.getFiltroBandejaPrestador().getFiltros().containsKey("id")) {
            condiciones.add(this.filtroProvider.obtenerFiltroPorIdPrestador());
        }
    }

    private void filtroContarSedeInterno(List<Predicate> condiciones) {
        if (this.filtroProvider.getFiltroBandejaPrestador().getFiltros().containsKey("sedes")) {
            condiciones.add(this.filtroProvider.obtenerFiltroPorSedesContadas());
        }
    }

    private void filtroContarTecnologias(List<Predicate> condiciones) {
        if (Objects.nonNull(this.filtroProvider.getFiltroBandejaPrestador().getTiposTecnologiasSeleccionados())
                && !this.filtroProvider.getFiltroBandejaPrestador().getTiposTecnologiasSeleccionados().isEmpty()) {
            condiciones.addAll(this.filtroProvider.obtenerFiltroPorTecnologias());
        }
    }

    private void filtroContarTecnologiasInternas(List<Predicate> condiciones) {
        if (this.filtroProvider.getFiltroBandejaPrestador().getFiltros().containsKey("tiposTecnologias")) {
            condiciones.addAll(this.filtroProvider.obtenerFiltroPorTecnologiasInternas());
        }
    }
}
