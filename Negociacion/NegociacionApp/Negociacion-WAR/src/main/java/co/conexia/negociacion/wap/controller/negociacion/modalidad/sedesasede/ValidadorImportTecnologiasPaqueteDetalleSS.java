package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionPaqueteDetalleSSFacade;
import com.conexia.contratacion.commons.constants.CommonMaps;
import com.conexia.contratacion.commons.constants.enums.ArchivosNegociacionEnum;
import com.conexia.contractual.model.maestros.EstadoTecnologia;
import com.conexia.contratacion.commons.dto.maestros.InsumosDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexosIngresadosDto;
import com.conexia.contratacion.commons.dto.negociacion.ErroresImportTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.TecnologiasIngresadasDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ValidadorImportTecnologiasPaqueteDetalleSS implements Serializable {

    @Inject
    private NegociacionPaqueteDetalleSSFacade detallePaquetefacade;

    private List<ErroresImportTecnologiasDto> listErrors;
    private List<ErroresImportTecnologiasDto> listAnexosErrors;

    public List<ErroresImportTecnologiasDto> validateStructure(List<TecnologiasIngresadasDto> tecnologiasIngresadas) {
        listErrors = new ArrayList<>();

        //Validacion Estructura
        for (TecnologiasIngresadasDto tec : tecnologiasIngresadas) {
            if (Objects.isNull(tec.getCantidadMinima()) || tec.getCantidadMinima().isEmpty() || Objects.isNull(tec.getCantidadMaxima()) || tec.getCantidadMaxima().isEmpty()) {
                ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Cantidades mínima-máxima", "Las cantidades mínima  y  máxima son requeridas");
                listErrors.add(error);
            } else if (!isNumeric(tec.getCantidadMaxima())) {
                listErrors.add(new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Cantidad Máxima", "El campo debe ser numerico"));
            } else if (!isNumeric(tec.getCantidadMinima())) {
                listErrors.add(new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Cantidad Minima", "El campo debe ser numerico"));
            }
            if (tec.getIngresoCantidad() != null && !tec.getIngresoCantidad().isEmpty() && !isDouble(tec.getIngresoCantidad())) {
                listErrors.add(new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Cantidad Ingreso", "El campo debe ser numerico"));
            }
            if (tec.getFrecuenciaCantidad() != null && !tec.getFrecuenciaCantidad().isEmpty() && !isDouble(tec.getFrecuenciaCantidad())) {
                listErrors.add(new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Cantidad Frecuencia", "El campo debe ser numerico"));
            }

            if (tec.getIngresoPrograma() != null && !tec.getIngresoPrograma().isEmpty()) {
                String valueKeyOpcionesIngreso = CommonMaps.getKeyOpcionesIngreso(tec.getIngresoPrograma().toUpperCase());
                if (valueKeyOpcionesIngreso == null) {
                    listErrors.add(new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Ingreso al Programa", "Ingrese correctamente Opciones de Ingreso"));
                } else {
                    tec.setIngresoPrograma(valueKeyOpcionesIngreso);
                }
            } else {
                tec.setIngresoPrograma("NA");
            }

            if (tec.getFrecuenciaUnidad() != null && !tec.getFrecuenciaUnidad().isEmpty()) {
                String valueKeyOpcionesFrecuencia = CommonMaps.getKeyOpcionesFrecuencia(tec.getFrecuenciaUnidad().toUpperCase());
                if (valueKeyOpcionesFrecuencia == null) {
                    listErrors.add(new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Ingreso Frecuencia Unidad",
                            "Ingrese correctamente Opciones de Frecuencia Unidad"));
                } else {
                    tec.setFrecuenciaUnidad(valueKeyOpcionesFrecuencia);
                }
            } else {
                tec.setFrecuenciaUnidad("NA");
            }

        }
        return listErrors;
    }

    public Object[] validateCoherence(List<TecnologiasIngresadasDto> tecnologiasIngresadas, Long negociacionId, Long paqueteId, Long prestadorId) {
        listErrors = new ArrayList<>();
        Object[] objectos = new Object[2];

        // Validate if technology code exist in maestras
        tecnologiasIngresadas = detallePaquetefacade.searchTechnologies(tecnologiasIngresadas, negociacionId, paqueteId, prestadorId);

        tecnologiasIngresadas.forEach(tec -> {
            if (tec.getTipoTecnologia() == null) {
                ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Código tecnología", "La tecnología no existe en lazos con el código tecnología única emssanar ingresado");
                listErrors.add(error);
            } else {
                if (Integer.parseInt(tec.getCantidadMinima()) > Integer.parseInt(tec.getCantidadMaxima())) {
                    ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Cantidad mínima", "La cantidad mínima debe ser menor o igual a la máxima.");
                    listErrors.add(error);
                }
                if (Integer.parseInt(tec.getCantidadMinima()) < 1 || Integer.parseInt(tec.getCantidadMinima()) > 10000) {
                    ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Cantidad mínima", "La cantidad debe ser un número mayor a 0 y menor a 10.000");
                    listErrors.add(error);
                }
                if (Integer.parseInt(tec.getCantidadMaxima()) < 1 || Integer.parseInt(tec.getCantidadMaxima()) > 10000) {
                    ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Cantidad máxima", "La cantidad debe ser un número mayor a 0 y menor a 10.000");
                    listErrors.add(error);
                }
                if (tec.getIngresoCantidad() != null && !tec.getIngresoCantidad().isEmpty()) {
                    if (Double.parseDouble(tec.getIngresoCantidad()) < 0.01 || Double.parseDouble(tec.getIngresoCantidad()) > 10000) {
                        ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Cantidad ingreso", "La cantidad debe ser un número mayor a 0.01 y menor a 10.000");
                        listErrors.add(error);
                    }
                }
                if (tec.getFrecuenciaCantidad() != null && !tec.getFrecuenciaCantidad().isEmpty()) {
                    if (Double.parseDouble(tec.getFrecuenciaCantidad()) < 0.01 || Double.parseDouble(tec.getFrecuenciaCantidad()) > 10000) {
                        ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Cantidad frecuencia", "La cantidad debe ser un número mayor a 0.01 y menor a 10.000");
                        listErrors.add(error);
                    }
                }
                if (Objects.isNull(tec.getCodigoTecnologia()) || tec.getCodigoTecnologia().equals("")) {
                    ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto("", "Código tecnología", "El código de la tecnología no puede ir vacio");
                    listErrors.add(error);
                }
                if (tec.getTipoTecnologia() == 1) {
                    if (tec.getListMedicamentoDto().size() == 1) {
                        Optional<MedicamentosDto> primerMedicamento = tec.getListMedicamentoDto().stream().findFirst();
                        if (primerMedicamento.isPresent() && Objects.equals(primerMedicamento.get().getEstadoCums(), EstadoTecnologia.ACTIVO)) {
                            MedicamentosDto medicamentosDto = primerMedicamento.get();
                            tec.setIdTecnologia(medicamentosDto.getId());
                            validateMedicamento(medicamentosDto);
                        } else {
                            ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Código tecnología", "El código de tecnología única está derogada/inactiva.");
                            listErrors.add(error);
                        }
                    } else {
                        ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Código tecnología", "El código de la tecnología se encuentra duplicado en maestras consulte administrador");
                        listErrors.add(error);
                    }
                } else if (tec.getTipoTecnologia() == 2) {
                    if (tec.getListProcedimientoDto().size() == 1) {
                        Optional<ProcedimientoDto> primerProcedimiento = tec.getListProcedimientoDto().stream().findFirst();
                        if (primerProcedimiento.isPresent() && Objects.equals(primerProcedimiento.get().getEstadoProcedimientoId(), EstadoTecnologia.ACTIVO)) {
                            ProcedimientoDto procedimientoDto = primerProcedimiento.get();
                            tec.setIdTecnologia(procedimientoDto.getId());
                            validateProcedimiento(procedimientoDto);
                        } else {
                            ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Código tecnología", "El código de tecnología única está derogada/inactiva.");
                            listErrors.add(error);
                        }
                    } else {
                        ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Código tecnología", "El código de la tecnología se encuentra duplicado en maestras consulte administrador");
                        listErrors.add(error);
                    }

                } else if (tec.getTipoTecnologia() == 3) {
                    if (tec.getListInsumoDto().size() == 1) {
                        Optional<InsumosDto> primerInsumo = tec.getListInsumoDto().stream().findFirst();
                        if (primerInsumo.isPresent() && Objects.equals(primerInsumo.get().getEstadoInsumoId(), EstadoTecnologia.ACTIVO)) {
                            InsumosDto insumosDto = primerInsumo.get();
                            tec.setIdTecnologia(insumosDto.getId());
                            validateInsumo(insumosDto);
                        } else {
                            ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Código tecnología", "El código de tecnología única está derogada/inactiva.");
                            listErrors.add(error);
                        }
                    } else {
                        ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(tec.getCodigoTecnologia(), "Código tecnología", "El código de la tecnología se encuentra duplicado en maestras consulte administrador");
                        listErrors.add(error);
                    }
                }
            }
        });
        objectos[0] = listErrors;
        objectos[1] = tecnologiasIngresadas;
        return objectos;
    }
    
    public void validateProcedimiento(ProcedimientoDto px) {

        if (px != null) {
            // ValidateStatus
            if (px.getEstadoProcedimientoId() != 1) {
                ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(px.getCodigoCliente(),
                        "Código tecnología", "El procedimiento no esta áctivo");
                listErrors.add(error);
            }
            // Validate Format
            if (!px.getCodigoCliente().substring(0, 3).equals("00P")
                    && !px.getCodigoCliente().substring(0, 2).equals("01")) {

                ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(px.getCodigoCliente(),
                        "Código tecnología",
                        "El procedimiento debe tener estructura 00P para códigos propios y  01  para codificación normativa");
                listErrors.add(error);
            }
        }

    }

    public void validateMedicamento(MedicamentosDto med) {
        if (Objects.nonNull(med)) {
            if (med.getEstadoCums() != 1) {
                ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(med.getCums(), "Código tecnología", "El código de tecnología única está derogada/inactiva");
                listErrors.add(error);
            } else if (!med.getCums().matches("(?:\\d)+-(?:\\d)+")) {
                ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(med.getCums(), "Código tecnología", "El medicamento no cumple con la estructura codigo de consecutivo terminado en 00-99");
                listErrors.add(error);
            }
        }
    }

    public void validateInsumo(InsumosDto in) {

        if (in != null) {
            // ValidateStatus
            if (in.getEstadoInsumoId() != 1) {
                ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(in.getCodigo(), "Código tecnología",
                        "El Insumo no esta áctivo");
                listErrors.add(error);
            }
            // Validate Format
            if (!in.getCodigo().substring(0, 3).equals("00E")) {

                ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(in.getCodigo(), "Código tecnología",
                        "El insumo debe tener estructura 00E");
                listErrors.add(error);
            }
        }

    }

    private static boolean isNumeric(String cadena) {
        try {
            Integer.parseInt(cadena);
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }
    }

    private static boolean isDouble(String cadena) {
        try {
            Double.parseDouble(cadena.replace(",", "."));
            return true;
        } catch (NumberFormatException nfe) {
            return false;
        }

    }

    public List<ErroresImportTecnologiasDto> validateAnexosStructures(List<AnexosIngresadosDto> anexosIngresados) {
        listAnexosErrors = new ArrayList<>();
        anexosIngresados.forEach(an -> {
            if (!(an.getTipoAnexo().toUpperCase().equals("OBSERVACION")
                    || an.getTipoAnexo().toUpperCase().equals("EXCLUSION")
                    || an.getTipoAnexo().toUpperCase().equals("CAUSA")
                    || an.getTipoAnexo().toUpperCase().equals("REQUERIMIENTO"))) {
                ErroresImportTecnologiasDto errorAnexo = new ErroresImportTecnologiasDto(an.getTipoAnexo(), an.getDetalle(), "Ingrese correctamente tipo de anexo: Observacion, Exclusion, Causa ó Requerimiento ");
                listAnexosErrors.add(errorAnexo);
            }

            if (an.getDetalle() != null && an.getDetalle().length() > 500) {
                ErroresImportTecnologiasDto errorAnexo = new ErroresImportTecnologiasDto(an.getTipoAnexo(), an.getDetalle(),
                        "El detalle supera la longitud máxima");
                listAnexosErrors.add(errorAnexo);
            }
            if (Objects.isNull(an.getDetalle()) || an.getDetalle().equals("")) {
                ErroresImportTecnologiasDto errorAnexo = new ErroresImportTecnologiasDto(an.getTipoAnexo(), an.getDetalle(),
                        "El detalle no puede ir vacio");
                listAnexosErrors.add(errorAnexo);
            }
        });
        return listAnexosErrors;
    }

    public List<ErroresImportTecnologiasDto> validateFormat(Sheet hoja, ArchivosNegociacionEnum typeImport) {
        List<ErroresImportTecnologiasDto> listErrorFormat = new ArrayList<>();
        for (Row cells : hoja) {
            int cantCeldas = 0;
            if (cells.getRowNum() == 0) {
                Iterator<Cell> cellIterator = cells.cellIterator();
                Cell celda;
                while (cellIterator.hasNext()) {
                    celda = cellIterator.next();
                    cantCeldas = cantCeldas + 1;
                }
                if (cantCeldas != typeImport.getNumColumnas()) {
                    ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto("El archivo no cumple con el formato, verifique que el archivo corresponda a cargue de " + typeImport.getDescripcion());
                    listErrorFormat.add(error);
                }
            }
        }
        return listErrorFormat;
    }

    public Boolean validarCamposFila(Row fila, ArchivosNegociacionEnum typeImport) {
        Boolean response = true;
        List<Boolean> nulos = IntStream.range(0, typeImport.getNumColumnas())
                .filter(i -> Objects.isNull(fila.getCell(i)))
                .mapToObj(i -> true)
                .collect(Collectors.toList());

        if (nulos.size() == typeImport.getNumColumnas()) {
            response = false;
        }

        return response;
    }
}
