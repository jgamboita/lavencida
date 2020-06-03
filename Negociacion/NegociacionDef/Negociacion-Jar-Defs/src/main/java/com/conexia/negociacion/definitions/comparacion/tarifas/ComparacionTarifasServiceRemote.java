package com.conexia.negociacion.definitions.comparacion.tarifas;

import java.util.ArrayList;
import java.util.List;

import com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.comparacion.ReporteComparacionMedicamentosDto;
import com.conexia.contratacion.commons.dto.comparacion.ReporteComparacionPaquetesDto;
import com.conexia.contratacion.commons.dto.comparacion.ReporteComparacionTarifasDto;
import com.conexia.contratacion.commons.dto.comparacion.ReporteComparacionTrasladosDto;
import com.conexia.contratacion.commons.dto.comparacion.ReportePaquetesPrestadorDto;
import com.conexia.contratacion.commons.dto.comparacion.TablaComparacionTarifaDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.MacroServicioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.ServicioSaludDto;

/**
 * Interface remota para el boundary de comparacion de tarifas
 * @author etorres
 */
public interface ComparacionTarifasServiceRemote {
    
    /**
     * Busca las sedes de un prestador y una tecnologia especificada
     * @param idPrestador
     * @param tecnologia
     * @return Lista de SedesPrestador
     */
    public List<SedePrestadorDto> findSedePrestadorByPrestadorIdAndTecnologia(Long idPrestador, String tecnologia);    
    
    /**
    * Realiza la busqueda de los Macroservicios asociados a una lista
    * de sedes y un prestador.
    * @param prestadorId
    * @param sedesId
    * @return Lista de MacroServicios
    * @throws Exception 
    */
   public List<MacroServicioDto> findMacroServiciosByPrestadorIdAndSedesId(Long prestadorId, List<Long> sedesId) throws Exception;
   
   /**
    * Consulta filtro servicios para la comparacion de tarifas
    * @param prestadorId, List<filtroSedes>, List<filtroMacroServicios>
    * @param sedesId
    * @param macroServiciosId
    * @return Lista de {@link - ServicioSaludDto}
    * @throws Exception 
    */
   public List<ServicioSaludDto> findServiciosSaludByPrestadorIdSedesIdAndMacroServId(Long prestadorId,  
                                    List<Long> sedesId, List<Long> macroServiciosId) throws Exception;
   
   
    /**
    * Consulta filtro categoriaMedicamentos para la comparacion de tarifas
    * @param prestadorId, List<filtroSedes>
    * @param sedesId
    * @return Lista de {@link - CategoriaMedicamentoDto}
    * @throws Exception 
    */
    public List<CategoriaMedicamentoDto> findCategoriasByPrestadorIdAndSedesId(
                                    Long prestadorId, List<Long> sedesId) throws Exception;
      
    /**
     * Compara los servicios del prestador baso o del Rias base con los prestadores
     * @param prestadorId
     * @param sedesId
     * @param macroServiciosId
     * @param serviciosSaludId
     * @param marcadosTodosServicios
     * @param marcadosTodosMacroServicios
     * @param comparaConNegociacion
     * @param riasId
     * @param rangoPoblacionId
     * @param esRias
     * @return
     */
    public List<TablaComparacionTarifaDto> coberturaSedePrestador(
            Long prestadorId, List<Long> sedesId, List<Long> macroServiciosId, List<Long> serviciosSaludId,
            Boolean marcadosTodosServicios, Boolean marcadosTodosMacroServicios, Boolean comparaConNegociacion, 
            List<Integer> riasId, List<Integer> rangoPoblacionId, boolean esRias);
      
    /**
    * compara los paquetes cubiertos por las sedes de los prestadores respecto al prestador de referencia
    * @param prestadorId
    * @param sedesId
    * @return
    */
   public List<TablaComparacionTarifaDto> coberturaSedePrestadorPaquetes(Long prestadorId, List<Long> sedesId);
      
    /**
    *  Consulta los procedimientos del prestador de referencia y sus campos respectivos para el reporte de excel
    * @param prestadorId
    * @param sedesId
    * @param macroServiciosId
    * @param serviciosSaludId
    * @param marcadosTodosMacroServicios
    * @param marcadosTodosServicios
    * @return
    */
    public List<ReporteComparacionTarifasDto> reporteComparacionTarifasExcelReferente(
                   Long prestadorId, List<Long> sedesId, List<Long> macroServiciosId, List<Long> serviciosSaludId,
                    Boolean marcadosTodosMacroServicios, Boolean marcadosTodosServicios);
      
    /**
     * Consulta los procedimientos de la sede comparada y sus valores respectivos para el reporte de excel
     * @param prestadorId
     * @param sedesId
     * @param macroServiciosId
     * @param serviciosSaludId
     * @param sedePrestadorId
     * @param marcadosTodosServicios
     * @param marcadosTodosMacroServicios
     * @param compararConNegociacion
     * @param riasId
     * @param rangoPoblacionId
     * @param esRia
     * @return
     */
    public List<ReporteComparacionTarifasDto> reporteComparacionTarifasExcelSedeComparacion(
                   Long prestadorId, List<Long> sedesId, List<Long> macroServiciosId, List<Long> serviciosSaludId, Integer sedePrestadorId,
                   Boolean marcadosTodosServicios, Boolean marcadosTodosMacroServicios, Boolean compararConNegociacion,
                   List<Integer> riasId, List<Integer> rangoPoblacionId, boolean esRia);
     
    /**
    * 
    * @param prestadorId
    * @param sedesId
    * @param categoriasMedId
    * @param marcadasTodasCategorias
    * @return 
    */
   public List<TablaComparacionTarifaDto> coberturaMedicamentosSedePrestador(
                   Long prestadorId, List<Long> sedesId, List<Long> categoriasMedId, Boolean marcadasTodasCategorias, 
                   Boolean compararConNegociacion, String tipoReporte);
   
      
    /**
    * 
    * @param prestadorId
    * @param sedesId
    * @param categoriasMedicamentoId
    * @param marcadasTodasCategorias
    * @return 
    */
    public List<ReporteComparacionMedicamentosDto> comparacionMedicamentosReferente(
                   Long prestadorId, List<Long> sedesId, List<Long> categoriasMedicamentoId, 
                    Boolean marcadasTodasCategorias,String tipoReporte);
    
    
      
    /**
    * Consulta los medicamentos de la sede comparada y sus valores respectivos para el reporte de excel
    * @param prestadorId
    * @param sedesId
    * @param categoriasMedicamentoId
    * @param sedePrestadorId
    * @param marcadasTodasCategorias
    * @return
    */
   public List<ReporteComparacionMedicamentosDto> reporteComparacionMedicamentosSedeComparacion(
                   Long prestadorId, List<Long> sedesId, List<Long> categoriasMedicamentoId, 
                   Integer sedePrestadorId, Boolean marcadasTodasCategorias, Boolean compararConNegociacion, 
                   String reporteSeleccionado);
   
   /**
    * Consulta los paquetes de la sede comparada y sus valores respectivos para el reporte de excel
    * 
    * @param prestadorId
    * @param sedesId
    * @return 
    */
   public List<ReporteComparacionPaquetesDto> reporteComparacionPaquetesExcelReferente(
                    Long prestadorId, List<Long> sedesId);
   
   
   /**
    * Consultas para el reporte de comparacion de paquetes comparados
    * @param prestadorId
    * @param sedesId
    * @param sedePrestadorId
    * @return 
    */
   public List<ReporteComparacionPaquetesDto> reporteComparacionPaquetesExcelComparados(
                    Long prestadorId, List<Long> sedesId, Integer sedePrestadorId, Boolean compararConNegociaciones);
   
   
   /**
    * Consulta con el detalle de los paquetes en la comparacion de tarifas
    * 
    * @param prestadorRefId
    * @param sedesId
    * @param prestadorCompId
    * @param portafolioFull
    * @return 
    */
   public List<ReportePaquetesPrestadorDto> reporteComparacionPaquetesExcelDetalle(
                    Long prestadorRefId, List<Long> sedesId, List<String> prestadorCompId, Boolean portafolioFull);
   
   /**
    * Obtiene los datos de un prestador por Id para mostrar en la comparacion 
    * de tarifas
    * @param prestadorId
    * @return PrestadorDto
    */
   public PrestadorDto findPrestadorById(Long prestadorId);
      
   /**
    * Realiza la generacion del reporte de comparacion de tarifas para procedimientos
    * @param dtosReporte
    * @param titulosSedes
    * @param nitPrestador
    * @param esRia
    * @return
    */
   public byte[] generateExcel(List<ReporteComparacionTarifasDto> dtosReporte, 
           ArrayList<String> titulosSedes, String nitPrestador, boolean esRia);
   
   /**
    * Realiza la generacion del reporte de comparacion de tarifas para medicamentos
    * @param dtosReporte
    * @param titulosSedes
    * @param nitPrestador
 * @param tipoReporte 
    * @return 
    */
   public byte[] generateExcelMedicamentos(List<ReporteComparacionMedicamentosDto> dtosReporte, 
           ArrayList<String> titulosSedes, String nitPrestador, String tipoReporte);
   
   
   /**
    * Realiza la generacion del reporte de comparacion de tarifas para paquetes
    * @param dtosReporte
    * @param dtosPaquetes
    * @param titulosSedes
    * @param nitPrestador
    * @return datos del reporte
    */
   public byte[]  generateExcelPaquetes(List<ReporteComparacionPaquetesDto> dtosReporte, 
            List<ReportePaquetesPrestadorDto> dtosPaquetes, ArrayList<String> titulosSedes, String nitPrestador);
   
   
   /**
    * Realiza la generacion del reporte de comparacion de tarifas para traslados
    * @param dtosReporte
    * @param titulosSedes
    * @param nitPrestador
    * @return datos del reporte
    */
   public byte[]  generateExcelTraslados(List<ReporteComparacionTrasladosDto> dtosReporte, 
            ArrayList<String> titulosSedes, String nitPrestador);
   
   /**
    * Consulta los traslados del prestador de referencia y sus campos respectivos para el reporte de excel
    * 
    * @param prestadorId
    * @param sedesId
    * @return Lista prestadores que dan cobertura a los traslados por sede.
    */
   public List<TablaComparacionTarifaDto> coberturaSedeTrasladosPrestador(Long prestadorId, List<Long> sedesId);
   
   /**
    * Consulta los traslados de la sede comparada y sus valores respectivos para el reporte de excel
    * 
    * @param prestadorId
    * @param sedesId
    * @return 
    */
   public List<ReporteComparacionTrasladosDto> reporteComparacionTrasladosExcelReferente(Long prestadorId, List<Long> sedesId);
   
   /**
    * Consultas para el reporte de comparacion de paquetes comparados
    * @param prestadorId
    * @param sedesId
    * @param sedePrestadorId
    * @return 
    */
   public List<ReporteComparacionTrasladosDto> reporteComparacionTrasladosExcelComparados(Long prestadorId, List<Long> sedesId, Integer sedePrestadorId);
   
   /**
    * Consulta las rutas de referente para el excel
    * @param riasId
    * @param rangosPoblacionId
    * @return
    */
   public List<ReporteComparacionTarifasDto> reporteComparacionTarifasExcelReferenteRia(List<Integer> riasId, List<Integer> rangosPoblacionId);

   
   public List<CapituloProcedimientoDto> findCapituloProcedureBySede(Long pretadorId, List<Long> sedesId) throws Exception;

   public List<CategoriaProcedimientoDto> findCategoriaByPrestadorIdSedesIdAndCapituloId(Long id, List<Long> sedesId,
		List<Long> capitulosId) throws Exception;

   public List<TablaComparacionTarifaDto> coberturaSedePrestadorPGP(Long id, List<Long> sedesId, List<Long> capitulosId,
		List<Long> categoriasId);


}
