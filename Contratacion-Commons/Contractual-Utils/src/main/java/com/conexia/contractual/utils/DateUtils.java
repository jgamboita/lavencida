package com.conexia.contractual.utils;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * Utilidades de fecha.
 *
 * @author jlopez
 */
public class DateUtils implements Serializable {    /**
	 * 
	 */
	private static final long serialVersionUID = 6149488850348222239L;
	
	private static final String FECHA_ANIO = " Año ";
	private static final String FECHA_ANIOS = " Años ";
	private static final String FECHA_MES = " Mes ";
	private static final String FECHA_MESES = " Meses ";
	private static final String FECHA_DIA = " Día.";
	private static final String FECHA_DIAS = " Días.";

	public Date asDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

    public Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public LocalDate asLocalDate(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    public LocalDateTime asLocalDateTime(Date date) {
        return Instant.ofEpochMilli(date.getTime()).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Calcula la diferencia entre dos fechas en meses dias y años.
     *
     * @param fechaInicio fecha de inicio.
     * @param fechaFin fecha fin.
     * @return diferencia en meses años y dias.
     */
    public String calcularFechaLetras(final Date fechaInicio, final Date fechaFin) {		
		return calculaAñosMesesDias(fechaInicio, fechaFin);			
    }
    
    public String calcularUrgenciasFechaLetras(final Date fechaInicio, final Date fechaFin) {		
		return calculaUrgenciasAñosMesesDias(fechaInicio, fechaFin);			
    }
    
     /**
     * Calcula la diferencia entre dos fechas y la devuelve en meses.
     *
     * @param fechaInicio fecha de inicio.
     * @param fechaFin fecha fin.
     * @return diferencia en meses.
     */
    public String calcularMesesContratoLetras(final Date fechaInicio, final Date fechaFin){
                return calculaMesesContrato(fechaInicio, fechaFin);
    }
    
    /**
     * Calcula la diferencia entre dos fechas en meses dias y años.
     *
     * @param fechaInicio fecha de inicio.
     * @param fechaFin fecha fin.
     * @return diferencia en meses años y dias.
     */
    public String calculaAñosMesesDias(final Date fechaInicio, final Date fechaFin) {
		LocalDateTime fromDateTime = LocalDateTime.ofInstant(fechaInicio.toInstant(), ZoneId.systemDefault());
		LocalDateTime toDateTime = LocalDateTime.ofInstant(fechaFin.toInstant(), ZoneId.systemDefault());
		LocalDateTime tempDateTime = LocalDateTime.from(fromDateTime);
		long years = tempDateTime.until(toDateTime, ChronoUnit.YEARS);
		tempDateTime = tempDateTime.plusYears(years);
		long months = tempDateTime.until(toDateTime, ChronoUnit.MONTHS);
		tempDateTime = tempDateTime.plusMonths(months);
		long days = tempDateTime.until(toDateTime, ChronoUnit.DAYS);		
		return ((years == 1)? years + FECHA_ANIO:(years > 1)?years + FECHA_ANIOS:"")+  
				((months == 1)?months + FECHA_MES:(months > 1)?months + FECHA_MESES:"") + 
				((days == 1) ? days + FECHA_DIA: (days > 1)?days +FECHA_DIAS:"");
    }
    
    
    
    
    public String calculaUrgenciasAñosMesesDias(final Date fechaInicio, final Date fechaFin) {
		LocalDateTime fromDateTime = LocalDateTime.ofInstant(fechaInicio.toInstant(), ZoneId.systemDefault());
		LocalDateTime toDateTime = LocalDateTime.ofInstant(fechaFin.toInstant(), ZoneId.systemDefault());
		LocalDateTime tempDateTime = LocalDateTime.from(fromDateTime);
		long years = tempDateTime.until(toDateTime, ChronoUnit.YEARS);
		tempDateTime = tempDateTime.plusYears(years);
		long months = tempDateTime.until(toDateTime, ChronoUnit.MONTHS);
		tempDateTime = tempDateTime.plusMonths(months);
		long days = tempDateTime.until(toDateTime, ChronoUnit.DAYS);	
		if(days > 0) {
		 	months = months +1;
		}
		
		if(months == 0 && days > 0 && days < 30) {
			months = 1;
		}
		
		
		return ((years == 1)? years + FECHA_ANIO:(years > 1)?years + FECHA_ANIOS:"")+  
				((months == 1)?months + FECHA_MES:(months > 1)?months + FECHA_MESES:"");
    }
    
    
    /**
     * Calcula los meses de la vigencia de un contrato partiendo de
     * las dos fechas ingresadas en el form (inicioVigencia y finVigencia).
     *
     * @param fechaInicio
     * @param fechaFin
     * @return cantidad en meses para vigencia de un contrato entre dos fechas ingresadas en el form.
     */
    
    public String calculaMesesContrato(final Date fechaInicio, final Date fechaFin) {
        LocalDateTime fromDateTime = LocalDateTime.ofInstant(fechaInicio.toInstant(), ZoneId.systemDefault());
        LocalDateTime toDateTime = LocalDateTime.ofInstant(fechaFin.toInstant(), ZoneId.systemDefault());
        LocalDateTime tempDateTime = LocalDateTime.from(fromDateTime);
        long years = tempDateTime.until(toDateTime, ChronoUnit.YEARS);
        tempDateTime = tempDateTime.plusYears(years);
        long months = tempDateTime.until(toDateTime, ChronoUnit.MONTHS);
        tempDateTime = tempDateTime.plusMonths(months);
        long days = tempDateTime.until(toDateTime, ChronoUnit.DAYS);

        long diffInMonths = (years * 12) + months;
        //Redondeo de mes
        if (days > 1) {
            diffInMonths++;
        }

        return ((diffInMonths == 1) ? diffInMonths + FECHA_MES : (diffInMonths > 1) ? diffInMonths + FECHA_MESES : "");
    }
    

    /**
     * Calcula los meses entre dos fechas.
     *
     * @param fechaInicio
     * @param fechaFin
     * @return meses entre dos fechas.
     */
    public long calculaMeses(final Date fechaInicio, final Date fechaFin) {
    	LocalDateTime fromDateTime = LocalDateTime.ofInstant(fechaInicio.toInstant(), ZoneId.systemDefault());
		LocalDateTime toDateTime = LocalDateTime.ofInstant(fechaFin.toInstant(), ZoneId.systemDefault());
		LocalDateTime tempDateTime = LocalDateTime.from(fromDateTime);
		long years = tempDateTime.until(toDateTime, ChronoUnit.YEARS);
		tempDateTime = tempDateTime.plusYears(years);
		long months = tempDateTime.until(toDateTime, ChronoUnit.MONTHS);
		tempDateTime = tempDateTime.plusMonths(months);
		if(years >= 1){
			long mesesAño = 12;
			if(years == 1)
			{
				months = months + mesesAño;
				return months;
			}
			else{
				long mesesAños = years * 12;
				months = months + mesesAños;
				return months;
			}
		}else{
			return months;
		}
        
    }
    /**
     * calcula los meses y agrega un porcentaje segun los días
     * @param fechaInicio
     * @param fechaFin
     * @return
     */
    public double calcularMesesAndDias(final Date fechaInicio, final Date fechaFin) {
    	LocalDateTime fromDateTime = LocalDateTime.ofInstant(fechaInicio.toInstant(), ZoneId.systemDefault());
		LocalDateTime toDateTime = LocalDateTime.ofInstant(fechaFin.toInstant(), ZoneId.systemDefault());
		LocalDateTime tempDateTime = LocalDateTime.from(fromDateTime);
		long years = tempDateTime.until(toDateTime, ChronoUnit.YEARS);
		tempDateTime = tempDateTime.plusYears(years);
		double months = tempDateTime.until(toDateTime, ChronoUnit.MONTHS);
		tempDateTime = tempDateTime.plusMonths((long)months);
		double day = tempDateTime.until(toDateTime, ChronoUnit.DAYS);
		if(day >= 0){
			// regla de calculo de dias capita (dias*100%)/30
			months = months + redondearDecimales(day/30,3);
		}
		if(years >= 1){
			long mesesAño = 12;
			if(years == 1)
			{
				months = months + mesesAño;
				return months;
			}
			else{
				long mesesAños = years * 12;
				months = months + mesesAños;
				return months;
			}
		}else{
			return months;
		}
        
    }
    
    /**
     * Limita la cantidad de decimales a los enviados como parámetro  
     * @param decimal
     * @param numeroDecimales
     * @return
     */
    private double redondearDecimales(double decimal,int numeroDecimales){
    	decimal = decimal*(java.lang.Math.pow(10, numeroDecimales));
    	decimal = Math.round(decimal);
    	decimal = decimal/java.lang.Math.pow(10, numeroDecimales);
    	return decimal;  
    }
    
    
	public static Date getFechaActual() {
        LocalDate date = LocalDate.now();
        return  Date.from(date.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }

}
