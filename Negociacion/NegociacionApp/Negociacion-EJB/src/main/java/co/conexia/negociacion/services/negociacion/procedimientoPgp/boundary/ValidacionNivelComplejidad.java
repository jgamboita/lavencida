package co.conexia.negociacion.services.negociacion.procedimientoPgp.boundary;

import com.conexia.contratacion.commons.constants.enums.ErrorTecnologiasNegociacionEventoEnum;
import com.conexia.contratacion.commons.dto.maestros.AbstractProcedimiento;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.ArchivoTecnologiasNegociacionEventoDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ServiciosHabilitadosRespNoRepsDto;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasEventoDto;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ValidacionNivelComplejidad {

    private List<ArchivoTecnologiasNegociacionEventoDto> procedimientos;
    private List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadorPorNegociacion;
    private List<ProcedimientoDto> tecnologiasMaestras;
    private NegociacionDto negociacion;

    public static ValidacionNivelComplejidad crear(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos, List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadorPorNegociacion, List<ProcedimientoDto> tecnologiasMaestras, NegociacionDto negociacion) {
        return new ValidacionNivelComplejidad(procedimientos, serviciosHabilitadorPorNegociacion, tecnologiasMaestras, negociacion);
    }

    private ValidacionNivelComplejidad(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos, List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadorPorNegociacion, List<ProcedimientoDto> tecnologiasMaestras, NegociacionDto negociacion) {
        this.procedimientos = procedimientos;
        this.serviciosHabilitadorPorNegociacion = serviciosHabilitadorPorNegociacion;
        this.tecnologiasMaestras = tecnologiasMaestras;
        this.negociacion = negociacion;
    }

    public Collection<? extends ErroresImportTecnologiasEventoDto> validar() {
        return validarNivelComplejidad(procedimientos);
    }

    private Collection<? extends ErroresImportTecnologiasEventoDto> validarNivelComplejidad(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        obtenerNivelComplejidadTecnologia(procedimientos);
        List<ArchivoTecnologiasNegociacionEventoDto> nivelComplejidadInvalido = new ArrayList<>();
        procedimientos.stream()
                .filter(procedimiento -> contieneCodigoEmssanar(tecnologiasMaestras, procedimiento))
                .filter(procedimiento -> contieneIdServicioSalud(tecnologiasMaestras, procedimiento))
                .forEach(procedimiento -> {
                    List<ServiciosHabilitadosRespNoRepsDto> nivelesServicioPorSede = serviciosHabilitadorPorNegociacion.stream()
                            .filter(obtenerPredicadoPorTipoDeImportacion(procedimiento))
                            .collect(Collectors.toList());
                    if (isValidoNivelComplejidad(procedimiento, nivelesServicioPorSede)) {
                        nivelComplejidadInvalido.add(procedimiento);
                    }
                });
        procedimientos.removeAll(nivelComplejidadInvalido);
        List<ErroresImportTecnologiasEventoDto> nivelTecnologiaNegociacion = nivelComplejidadInvalido.stream()
                .filter(archivo -> archivo.getNivelComplejidadTecnologia() > negociacion.getComplejidad().getId())
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.NIVEL_COMPLEJIDAD_NEGOCIACION))
                .collect(Collectors.toList());

        List<ErroresImportTecnologiasEventoDto> nivelTecnologiaMaestro = nivelComplejidadInvalido.stream()
                .filter(archivo -> archivo.getNivelComplejidadTecnologia() <= negociacion.getComplejidad().getId())
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.NIVEL_COMPLEJIDAD_MAESTROS))
                .collect(Collectors.toList());

        return Stream.of(nivelTecnologiaNegociacion, nivelTecnologiaMaestro)
                .flatMap(Collection::parallelStream)
                .collect(Collectors.toList());
    }

    private void obtenerNivelComplejidadTecnologia(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos) {
        for (ArchivoTecnologiasNegociacionEventoDto procedimiento : procedimientos) {
            Optional<ProcedimientoDto> primeraCoincidencia = tecnologiasMaestras.stream()
                    .filter(isIgualTecnologiaArchivosVsTecnologiaMaestra(procedimiento))
                    .findFirst();
            primeraCoincidencia.ifPresent(procedimientoDto -> procedimiento.setNivelComplejidadTecnologia(procedimientoDto.getComplejidad()));
        }
    }

    private Predicate<ProcedimientoDto> isIgualTecnologiaArchivosVsTecnologiaMaestra(ArchivoTecnologiasNegociacionEventoDto procedimiento) {
        return procedimientoDto -> Objects.equals(procedimientoDto.getServicioSalud()
                .getId(), procedimiento.getServicioId()) && Objects.equals(procedimientoDto.getCodigoCliente(), procedimiento.getCodigoEmssanar());
    }

    private Predicate<ServiciosHabilitadosRespNoRepsDto> obtenerPredicadoPorTipoDeImportacion(ArchivoTecnologiasNegociacionEventoDto procedimiento) {
        switch (negociacion.getOpcionImportarSeleccionada()) {
            case IMPORTAR_SEDE_A_SEDE:
                return serviciohabilitado -> Objects.equals(serviciohabilitado.getServicioId(), procedimiento.getServicioId())
                        && Objects.equals(serviciohabilitado.getCodigoHabilitacionGuionCodigoSede(), procedimiento.getCodigoHabilitacionSede());
            case IMPORTAR_TODAS_LAS_SEDES:
                return serviciohabilitado -> Objects.equals(serviciohabilitado.getServicioId(), procedimiento.getServicioId());
            default:
                throw new IllegalArgumentException("Opcion para importa procedimientos invalida");
        }
    }

    private boolean contieneIdServicioSalud(List<ProcedimientoDto> tecnologiasMaestras, ArchivoTecnologiasNegociacionEventoDto procedimiento) {
        return tecnologiasMaestras.stream()
                .map(procedimientoDto -> procedimientoDto.getServicioSalud().getId())
                .collect(Collectors.toList())
                .contains(procedimiento.getServicioId());

    }

    private boolean contieneCodigoEmssanar(List<ProcedimientoDto> tecnologiasMaestras, ArchivoTecnologiasNegociacionEventoDto procedimiento) {
        return tecnologiasMaestras.stream()
                .map(AbstractProcedimiento::getCodigoCliente)
                .collect(Collectors.toList())
                .contains(procedimiento.getCodigoEmssanar());
    }

    private boolean isValidoNivelComplejidad(ArchivoTecnologiasNegociacionEventoDto procedimiento, List<ServiciosHabilitadosRespNoRepsDto> nivelesServicioPorSede) {
        return nivelesServicioPorSede.stream()
                .filter(serviciosHabilitadosRespNoRepsDto -> Objects.nonNull(procedimiento.getNivelComplejidadTecnologia()) && serviciosHabilitadosRespNoRepsDto.getNivelComplejidadMinimo() < procedimiento.getNivelComplejidadTecnologia())
                .count() == nivelesServicioPorSede.size();
    }

    private ErroresImportTecnologiasEventoDto convertirArchivosAErrorImportacion(ArchivoTecnologiasNegociacionEventoDto archivo, ErrorTecnologiasNegociacionEventoEnum camposObligatorios) {
        return new ErroresImportTecnologiasEventoDto(
                camposObligatorios.getCodigo(),
                camposObligatorios.getMensaje(),
                archivo.getLineaArchivo(),
                archivo.getCodigoHabilitacionSede(),
                archivo.getCodigoServicio(),
                archivo.getCodigoEmssanar(),
                archivo.getTarifarioNegociado(),
                archivo.getPorcentajeNegociado(),
                archivo.getValorNegociado());
    }
}
