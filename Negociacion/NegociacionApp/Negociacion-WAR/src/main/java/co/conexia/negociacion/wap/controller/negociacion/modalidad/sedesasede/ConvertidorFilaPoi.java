package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import com.conexia.contratacion.commons.dto.negociacion.AnexosIngresadosDto;
import com.conexia.contratacion.commons.dto.negociacion.TecnologiasIngresadasDto;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.util.Arrays;
import java.util.Objects;

public class ConvertidorFilaPoi {
    private enum ColumnasExcelTenologiasPaqueteEnum {
        CODIGO_TECNOLOGIA(0), CANTIDAD_MINIMA(1), CONTIDAD_MAXIMA(2), OBSERVACION(3), INGRESO_PROGRAMA(4),
        INGRESO_CANTIDAD(5), FRECUENCIA_UNIDAD(6), FRECUENCIA_CANTIDAD(7);

        private int index;

        ColumnasExcelTenologiasPaqueteEnum(int index) {
            this.index = index;
        }

        public static ColumnasExcelTenologiasPaqueteEnum getPorIndex(int columnIndex) {
            return Arrays.stream(ColumnasExcelTenologiasPaqueteEnum.values())
                    .filter(enumT -> Objects.equals(enumT.getIndex(), columnIndex))
                    .findFirst().orElseThrow(NullPointerException::new);
        }

        public int getIndex() {
            return index;
        }
    }

    private enum ColumnasExcelAnexosPaqueteEnum {
        TIPO_ANEXO(0), DETALLE(1);

        private int index;

        ColumnasExcelAnexosPaqueteEnum(int index) {
            this.index = index;
        }

        public static ColumnasExcelAnexosPaqueteEnum getPorIndex(int columnIndex) {
            return Arrays.stream(ColumnasExcelAnexosPaqueteEnum.values())
                    .filter(enumT -> Objects.equals(enumT.getIndex(), columnIndex))
                    .findFirst().orElseThrow(NullPointerException::new);
        }

        public int getIndex() {
            return index;
        }
    }

    public TecnologiasIngresadasDto convertirFilaATecnologiaDto(Row fila) {
        TecnologiasIngresadasDto te = new TecnologiasIngresadasDto();
        fila.forEach(cell -> {
            ColumnasExcelTenologiasPaqueteEnum columna = ColumnasExcelTenologiasPaqueteEnum.getPorIndex(cell.getColumnIndex());
            switch (columna) {
                case CODIGO_TECNOLOGIA:
                    te.setCodigoTecnologia(obtenerTipoDato(cell));
                    break;
                case CANTIDAD_MINIMA:
                    te.setCantidadMinima(obtenerTipoDato(cell));
                    break;
                case CONTIDAD_MAXIMA:
                    te.setCantidadMaxima(obtenerTipoDato(cell));
                    break;
                case OBSERVACION:
                    te.setObservacion(obtenerTipoDato(cell));
                    break;
                case INGRESO_PROGRAMA:
                    te.setIngresoPrograma(obtenerTipoDato(cell));
                    break;
                case INGRESO_CANTIDAD:
                    te.setIngresoCantidad(obtenerTipoDato(cell));
                    break;
                case FRECUENCIA_UNIDAD:
                    te.setFrecuenciaUnidad(obtenerTipoDato(cell));
                    break;
                case FRECUENCIA_CANTIDAD:
                    te.setFrecuenciaCantidad(obtenerTipoDato(cell));
                    break;
                default:
                    break;
            }
        });
        return te;
    }

    public AnexosIngresadosDto convertirFilaAAnexoDto(Row fila) {
        AnexosIngresadosDto te = new AnexosIngresadosDto();
        fila.forEach(cell -> {
            ColumnasExcelAnexosPaqueteEnum columna = ColumnasExcelAnexosPaqueteEnum.getPorIndex(cell.getColumnIndex());
            switch (columna) {
                case TIPO_ANEXO:
                    te.setTipoAnexo(obtenerTipoDato(cell));
                    break;
                case DETALLE:
                    te.setDetalle(obtenerTipoDato(cell));
                    break;
                default:
                    break;
            }
        });
        return te;
    }

    private String obtenerTipoDato(Cell cell) {
        switch (cell.getCellType()) {
            case NUMERIC:
                return String.valueOf(Double.valueOf(cell.getNumericCellValue()).intValue());
            case STRING:
                return cell.getStringCellValue();
            default:
                return String.valueOf(cell);
        }
    }
}
