package co.conexia.negociacion.services.negociacion.procedimientoPgp.boundary;

import com.conexia.contratacion.commons.constants.enums.ErrorTecnologiasNegociacionEventoEnum;
import com.conexia.contratacion.commons.dto.negociacion.ArchivoTecnologiasNegociacionEventoDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ServiciosHabilitadosRespNoRepsDto;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasEventoDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class ValidacionServiciosReps {

    private List<ArchivoTecnologiasNegociacionEventoDto> procedimientos;
    private List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadorPorNegociacion;
    private NegociacionDto negociacion;

    public static ValidacionServiciosReps crear(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos, List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadorPorNegociacion, NegociacionDto negociacion) {
        return new ValidacionServiciosReps(procedimientos, serviciosHabilitadorPorNegociacion, negociacion);
    }

    private ValidacionServiciosReps(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos, List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadorPorNegociacion, NegociacionDto negociacion) {
        this.procedimientos = procedimientos;
        this.serviciosHabilitadorPorNegociacion = serviciosHabilitadorPorNegociacion;
        this.negociacion = negociacion;
    }

    public List<ErroresImportTecnologiasEventoDto> validar() {
        switch (negociacion.getOpcionImportarSeleccionada()) {
            case IMPORTAR_SEDE_A_SEDE:
                return validar(procedimientos, nonContieneCodigoHabilitacionMasServicio());
            case IMPORTAR_TODAS_LAS_SEDES:
                return validar(procedimientos, nonContieneCodigoServicio());
            default:
                throw new IllegalArgumentException("Opcion para importa procedimientos invalida");
        }
    }

    private Predicate<ArchivoTecnologiasNegociacionEventoDto> nonContieneCodigoHabilitacionMasServicio() {
        return archivo -> serviciosHabilitadorPorNegociacion.stream()
                .noneMatch(servicios -> Objects.equals(servicios.getCodigoHabilitacionMasCodigoSede(), archivo.getCodigoHabilitacionSedeSoloDigitos())
                        && Objects.equals(servicios.getCodigoServicio(), archivo.getCodigoServicio()));
    }

    private Predicate<ArchivoTecnologiasNegociacionEventoDto> nonContieneCodigoServicio() {
        return archivo -> !serviciosHabilitadorPorNegociacion.stream()
                .map(ServiciosHabilitadosRespNoRepsDto::getCodigoServicio)
                .collect(Collectors.toList())
                .contains(archivo.getCodigoServicio());
    }

    private List<ErroresImportTecnologiasEventoDto> validar(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos,
                                                            Predicate<ArchivoTecnologiasNegociacionEventoDto> predicate) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        List<ArchivoTecnologiasNegociacionEventoDto> serviciosNoHabilitadosParaNingunaSede = procedimientos.stream()
                .filter(predicate)
                .collect(Collectors.toList());
        procedimientos.removeAll(serviciosNoHabilitadosParaNingunaSede);
        return serviciosNoHabilitadosParaNingunaSede.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.SERVICIOS_NO_HABILITADOS))
                .collect(Collectors.toList());
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
