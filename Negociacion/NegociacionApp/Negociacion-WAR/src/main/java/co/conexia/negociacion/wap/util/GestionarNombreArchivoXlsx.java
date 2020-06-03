/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package co.conexia.negociacion.wap.util;

import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Objects;

/**
 * Class to generate the file name with extenxion .xlxs
 * @author aquintero
 */
public class GestionarNombreArchivoXlsx implements Serializable
{
    
    
    /**
     * Method to get file name
     * @param servicio      Service
     * @param negociacion   Negotiation
     * @return String       
     */
    public String getFileName(String servicio, NegociacionDto negociacion)
    {
        SimpleDateFormat formateaFecha = new SimpleDateFormat("yyyyMMdd");
        String filename = "";
        String fecha;
        String numeroContrato = negociacion.getNumeroContratoAnexo();
        String numeroDocumento;
        if (Objects.nonNull(negociacion.getEsOtroSi()) ? negociacion.getEsOtroSi() : false) {
            numeroDocumento = Objects.nonNull(numeroContrato) ? numeroContrato : negociacion.getNegociacionPadre().getId().toString();
        } else {
            numeroDocumento = Objects.nonNull(numeroContrato) ? numeroContrato : negociacion.getId().toString();
        }

        switch (servicio) {
            case "medicamentos":
                fecha = Objects.nonNull(negociacion.getFechaConcertacionMx()) ? "_" + formateaFecha.format(negociacion.getFechaConcertacionMx()) : "";
                filename = numeroDocumento + fecha + "_TAD_MED.xlsx";
                if (Objects.nonNull(negociacion.getEsOtroSi()) && negociacion.getEsOtroSi()) {
                    filename = numeroDocumento + fecha + "_TAD_MED" + "_OTROSI_" + concatZeroToNumbersLessThan10(negociacion.getNumeroOtroSi()) + ".xlsx";
                }
                break;
            case "procedimientosSinDetalles":
            case "procedimientos":
                fecha = Objects.nonNull(negociacion.getFechaConcertacionPx()) ? "_" + formateaFecha.format(negociacion.getFechaConcertacionPx()) : "";
                filename = numeroDocumento + fecha + "_TAD_PRO.xlsx";
                if (Objects.nonNull(negociacion.getEsOtroSi()) && negociacion.getEsOtroSi()) {
                    filename = numeroDocumento + fecha + "_TAD_PRO" + "_OTROSI_" + concatZeroToNumbersLessThan10(negociacion.getNumeroOtroSi()) + ".xlsx";
                }
                break;
            default:
                fecha = Objects.nonNull(negociacion.getFechaConcertacionPq()) ? "_" + formateaFecha.format(negociacion.getFechaConcertacionPq()) : "";
                filename = numeroDocumento + fecha + "_TAD_PAQ.xls";
                break;
        }
        return filename;
    }
    
    /**
     * Method to concat "0" to numbers less than 10
     * 
     * @param number    Number
     * @return String   String concat
     */
    private static String concatZeroToNumbersLessThan10(Integer number)
    {
        if (Objects.isNull(number)) { return "0"; }
        return number < 10 ? "0" + String.valueOf(number) : String.valueOf(number);
    }
}

