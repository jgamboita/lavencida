package co.conexia.negociacion.wap.facade.comparacion.tarifas;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

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
import com.conexia.negociacion.definitions.comparacion.tarifas.ComparacionTarifasServiceRemote;
import com.conexia.servicefactory.CnxService;

/**
 * Facade expuesto para comparacion de tarifas
 *
 * @author etorres
 */
public class ComparacionTarifasFacade implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8955295168555070887L;
	
	@Inject
    @CnxService
    private ComparacionTarifasServiceRemote comparacionTarifas;

    public PrestadorDto findPrestadorById(Long prestadorId) {
        return comparacionTarifas.findPrestadorById(prestadorId);
    }

    public List<SedePrestadorDto> findSedePrestadorByPrestadorIdAndTecnologia(Long idPrestador, String tecnologia) {
        return comparacionTarifas.findSedePrestadorByPrestadorIdAndTecnologia(idPrestador, tecnologia);
    }

    public List<MacroServicioDto> findMacroServiciosByPrestadorIdAndSedesId(Long prestadorId, List<Long> sedesId) throws Exception {
        return comparacionTarifas.findMacroServiciosByPrestadorIdAndSedesId(prestadorId, sedesId);
    }
    
    public List<CategoriaMedicamentoDto> findCategoriasByPrestadorIdAndSedesId(Long prestadorId, List<Long> sedesId) throws Exception{
        return comparacionTarifas.findCategoriasByPrestadorIdAndSedesId(prestadorId, sedesId);
    }
    
    public List<ServicioSaludDto> findServiciosSaludByPrestadorIdSedesIdAndMacroServId(Long prestadorId,  
                                    List<Long> sedesId, List<Long> macroServiciosId) throws Exception{
        return comparacionTarifas.findServiciosSaludByPrestadorIdSedesIdAndMacroServId(prestadorId, sedesId, macroServiciosId);
    }
    
    public List<TablaComparacionTarifaDto> coberturaSedePrestador(
            Long prestadorId, List<Long> sedesId, List<Long> macroServiciosId, List<Long> serviciosSaludId,
            Boolean marcadosTodosServicios, Boolean marcadosTodosMacroServicios, Boolean compararConNegociacion,
            List<Integer> riasId, List<Integer> rangoPoblacionId, boolean esRias){
        return comparacionTarifas.coberturaSedePrestador(prestadorId, sedesId, macroServiciosId, 
                serviciosSaludId, marcadosTodosServicios, marcadosTodosMacroServicios, compararConNegociacion,
                riasId, rangoPoblacionId, esRias);
    }
    
    public List<TablaComparacionTarifaDto> coberturaSedePrestadorPaquetes(
                   Long prestadorId, List<Long> sedesId){
        return comparacionTarifas.coberturaSedePrestadorPaquetes(prestadorId, sedesId);
    }
    
    public List<TablaComparacionTarifaDto> coberturaMedicamentosSedePrestador(
                   Long prestadorId, List<Long> sedesId, List<Long> categoriasMedId, Boolean marcadasTodasCategorias, Boolean compararConNegociacion, String tipoReporte){
        return comparacionTarifas.coberturaMedicamentosSedePrestador(prestadorId, sedesId, 
                categoriasMedId, marcadasTodasCategorias, compararConNegociacion, tipoReporte);
    }
    
    
    /**
     * Realiza las consultas para generar el reporte de comparacion de tarifas para procedimientos
     * y devuelve un arreglo de bytes con los datos del reporte
     * 
     * @param tablaComparacion
     * @param prestadorId
     * @param sedesId
     * @param macroserviciosId
     * @param serviciosId
     * @param marcadosTodosMacroServicios
     * @param marcadosTodosServicios
     * @param prestadorNit
     * @return Arreglo de bytes con los datos del reporte
     * @throws IOException 
     */
    public byte[] generarExcelComparacionProcedimientos(List<TablaComparacionTarifaDto> tablaComparacion,
            Long prestadorId, List<Long> sedesId, List<Long> macroserviciosId, List<Long> serviciosId,
            Boolean marcadosTodosMacroServicios, Boolean marcadosTodosServicios, String prestadorNit , Boolean compararConNegociacion,
            List<Integer> riasId, List<Integer> rangoPoblacionId, boolean esRia) throws IOException{
        
        // Valores del prestador de referencia
        List<ReporteComparacionTarifasDto> dtosReporte = 
        		esRia ? 
        			comparacionTarifas.reporteComparacionTarifasExcelReferenteRia(riasId, rangoPoblacionId) : 
        			comparacionTarifas.reporteComparacionTarifasExcelReferente(prestadorId, sedesId, macroserviciosId, serviciosId, marcadosTodosMacroServicios, marcadosTodosServicios);
        
        //Valores de las sedes a comparar		
        ArrayList<String> titulosSedes = new ArrayList<String>();
        /** Conjunto de datos para almacenar los transportes ofertados por el prestador para los codigos de transporte especial. **/
        Set<ReporteComparacionTarifasDto> dtosReporteTrasladosSede = new HashSet<ReporteComparacionTarifasDto>();
        
        if(!tablaComparacion.isEmpty()){
            for(TablaComparacionTarifaDto sede : tablaComparacion){
                //TablaComparacionTarifaDto sede = filtro.getTablaTarifasComparacion().get(i);
                //Titulos de las sedes (codigo habilitacion + nombre sede
                titulosSedes.add( (sede.getCodigoHabilitacionSede()==null?"":sede.getCodigoHabilitacionSede()) + " " + sede.getNombreSede() );
                //Valores de los procedimientos en cada sede
                List<ReporteComparacionTarifasDto> dtosReporteValoresSede = comparacionTarifas
                        .reporteComparacionTarifasExcelSedeComparacion(
                                prestadorId, sedesId, macroserviciosId, serviciosId, sede.getSedeId(),
                                marcadosTodosMacroServicios, marcadosTodosServicios, compararConNegociacion,
                                riasId, rangoPoblacionId, esRia);
                
                for (ReporteComparacionTarifasDto registro : dtosReporte){
                    List<ReporteComparacionTarifasDto> coincidencias =  dtosReporteValoresSede.stream().filter((p)-> p.getCodigoEmssanar().equals((registro.getCodigoEmssanar()))).collect(Collectors.toList());
                    if (coincidencias.size()>0){
                        ReporteComparacionTarifasDto dto = coincidencias.get(0);
                        registro.getValoresSedes().add(dto.getValorSedeComp()!=null?dto.getValorSedeComp().toString():"");
                    }else{
                        registro.getValoresSedes().add("");
                    }
                    
                }
            }
        }
        
        return comparacionTarifas.generateExcel(dtosReporte, titulosSedes, prestadorNit, esRia);
    }
    
    

    
    /**
     * Realiza las consultas para generar el reporte de comparacion de tarifas para medicamentos
     * y devuelve un arreglo de bytes con los datos del reporte
     * 
     * @param tablaComparacion
     * @param prestadorId
     * @param sedesId
     * @param categoriaMedicamentosId
     * @param marcadosTodosGruposMedicamentos
     * @param prestadorNit
     * @return ArreglomarcadosTodosGruposMedicamentosde bytes con los datos del reporte
     * @throws IOException 
     */
    public byte[] generarExcelComparacionMedicamentos(List<TablaComparacionTarifaDto> tablaComparacion,
            Long prestadorId, List<Long> sedesId, List<Long> categoriaMedicamentosId, 
            Boolean marcadosTodosGruposMedicamentos, String prestadorNit , Boolean compararConNegociacion, String tipoReporte) throws IOException{
        
        // Valores del prestador de referencia
        List<ReporteComparacionMedicamentosDto> dtosReporte = comparacionTarifas.comparacionMedicamentosReferente(
                prestadorId, sedesId, categoriaMedicamentosId, marcadosTodosGruposMedicamentos,tipoReporte);
        
        //Valores de las sedes a comparar		
        ArrayList<String> titulosSedes = new ArrayList<>();
        
        if(!tablaComparacion.isEmpty()){
            for(TablaComparacionTarifaDto sede : tablaComparacion){
                //TablaComparacionTarifaDto sede = filtro.getTablaTarifasComparacion().get(i);
                //Titulos de las sedes (codigo habilitacion + nombre sede
                titulosSedes.add( (sede.getCodigoHabilitacionSede()==null?"":sede.getCodigoHabilitacionSede()) + " " + sede.getNombreSede() );
                //Valores de los procedimientos en cada sede
                List<ReporteComparacionMedicamentosDto> dtosMedicamentosSedeComparada = comparacionTarifas
                        .reporteComparacionMedicamentosSedeComparacion(prestadorId, sedesId, 
                                categoriaMedicamentosId, sede.getSedeId(), marcadosTodosGruposMedicamentos, compararConNegociacion,tipoReporte);

                //Se adiciona el valor del medicamento de la sede consultada al registro de medicamentos de referencia 
                for(int j = 0;j<dtosReporte.size();j++){
                    ReporteComparacionMedicamentosDto registro = dtosReporte.get(j);
                    String valorMedicamentoSedeComparacion = "";
                    String valorModalidadSedeComparacion = "";
                    if(dtosMedicamentosSedeComparada != null && dtosMedicamentosSedeComparada.size() > 0){
                        try {
                            if(dtosMedicamentosSedeComparada.get(j) != null){
                            	
                            	if (tipoReporte != null && tipoReporte.equals("gPgp")) {
                            		 valorModalidadSedeComparacion = dtosMedicamentosSedeComparada
                                      .get(j).getModalidadSedeComp()!=null 
                                      ? dtosMedicamentosSedeComparada.get(j).getModalidadSedeComp().toString()
                                      : "";
                            	 }else {
                               valorMedicamentoSedeComparacion = dtosMedicamentosSedeComparada
                                       .get(j).getValorSedeComp()!=null 
                                       ? dtosMedicamentosSedeComparada.get(j).getValorSedeComp().toString()
                                       : "";
                            	 }
                            }
                        } catch(IndexOutOfBoundsException e){

                        }
                    }
                	if (tipoReporte != null && tipoReporte.equals("gPgp")) {
                	registro.getModalidadSedes().add(valorModalidadSedeComparacion);	
                	}else {
                    registro.getValoresSedes().add(valorMedicamentoSedeComparacion);
                	}
                }
            }
        }
        return comparacionTarifas.generateExcelMedicamentos(dtosReporte, titulosSedes, prestadorNit, tipoReporte);
    }
    
    /**
     * Realiza las consultas para generar el reporte de comparacion de tarifas para medicamentos
     * y devuelve un arreglo de bytes con los datos del reporte
     * 
     * @param tablaComparacion
     * @param prestadorId
     * @param sedesId
     * @param prestadorNit
     * @return
     * @throws IOException 
     */
    public byte[] generarExcelComparacionPaquetes(List<TablaComparacionTarifaDto> tablaComparacion,
            Long prestadorId, List<Long> sedesId, String prestadorNit, Boolean compararConNegociacion) throws IOException{
        
        //Valores del prestador de referencia
        List<ReporteComparacionPaquetesDto> dtosReporte = comparacionTarifas
                .reporteComparacionPaquetesExcelReferente(prestadorId, sedesId);
        
        //Valores de las sedes a comparar		
        ArrayList<String> titulosSedes = new ArrayList<>();
        
        if(!tablaComparacion.isEmpty()){
            for(TablaComparacionTarifaDto sede : tablaComparacion){
                //Titulos de las sedes (codigo habilitacion + nombre sede)
                titulosSedes.add( sede.getCodigoHabilitacionSede() + " " + sede.getNombreSede() );
                //Valores de los paquetes de una sede comparada
                List<ReporteComparacionPaquetesDto> dtosPaquetesSedeComparada = 
                        comparacionTarifas.reporteComparacionPaquetesExcelComparados(
                                prestadorId, sedesId, sede.getSedeId(), compararConNegociacion);

                //Se adiciona el valor del paquete de la sede consultada al registro de paquetes de referencia
                for(int j = 0,k = 0;j<dtosReporte.size();j++){
                    ReporteComparacionPaquetesDto registro = dtosReporte.get(j);
                    String valorPaqueteSedeComparacion = "";
                    boolean valorComparacionDefinido = false;
                    if(dtosPaquetesSedeComparada != null && dtosPaquetesSedeComparada.size() > 0){
                        try {
                            while(!valorComparacionDefinido){
                                if(dtosPaquetesSedeComparada.get(k) != null ){
                                    valorPaqueteSedeComparacion = dtosPaquetesSedeComparada.get(k).getValorPrestComp()!=null?dtosPaquetesSedeComparada.get(k).getValorPrestComp().toString():"";
                                    k++;
                                    valorComparacionDefinido = true;
                                } else {
                                    k++;
                                }
                            }
                        } catch(IndexOutOfBoundsException ioobe){

                        }
                        registro.getValoresSedes().add(valorPaqueteSedeComparacion);
                    }
                }
            }
        }
        
        List<String> prestadoresComparacion = new ArrayList<String>();
        prestadoresComparacion.add(prestadorId.toString());
        if(!tablaComparacion.isEmpty()){
            for(TablaComparacionTarifaDto seleccionado:tablaComparacion){
                prestadoresComparacion.add(seleccionado.getPrestadorId().toString());
            }
        }

        //se le pasa la lista de los prestadores seleccionados en el filtro, más el de referencia
        List<ReportePaquetesPrestadorDto> dtoDetallePaquetes = comparacionTarifas
                .reporteComparacionPaquetesExcelDetalle(prestadorId, sedesId, prestadoresComparacion, tablaComparacion.isEmpty());
                
        List<ReportePaquetesPrestadorDto> dtoDetallePaquetesOrdenados = new ArrayList<>();
        //se debe ordenar el resultado dejando primero los registros del prestador de referencia 
        if(dtoDetallePaquetes != null && dtoDetallePaquetes.size() > 0){
            for(ReportePaquetesPrestadorDto paquete:dtoDetallePaquetes){
                if(prestadorNit.equals(paquete.getNumeroDocumento())){
                        dtoDetallePaquetesOrdenados.add(paquete);
                }
            }
            for(ReportePaquetesPrestadorDto paquete:dtoDetallePaquetes){
                if(!prestadorNit.equals(paquete.getNumeroDocumento())){
                        dtoDetallePaquetesOrdenados.add(paquete);
                }
            }
        }
    
        return comparacionTarifas.generateExcelPaquetes(dtosReporte, dtoDetallePaquetes, titulosSedes, prestadorNit);
    }
    
    
    /**
     * Realiza las consultas para generar el reporte de comparacion de tarifas para medicamentos
     * y devuelve un arreglo de bytes con los datos del reporte
     * 
     * @param tablaComparacion
     * @param prestadorId
     * @param sedesId
     * @param prestadorNit
     * @return
     * @throws IOException 
     */
    public byte[] generarExcelComparacionTraslados(List<TablaComparacionTarifaDto> tablaComparacion,
            Long prestadorId, List<Long> sedesId, String prestadorNit ) throws IOException{
        
        // Valores del prestador de referencia
        List<ReporteComparacionTrasladosDto> dtosReporte = comparacionTarifas
                .reporteComparacionTrasladosExcelReferente(prestadorId, sedesId);
        
        //Valores de las sedes a comparar		
        ArrayList<String> titulosSedes = new ArrayList<String>();
        
        // Conjunto de datos para almacenar los transportes ofertados por el prestador para los codigos de transporte especial.
        Set<ReporteComparacionTrasladosDto> dtosReporteTrasladosSede = new HashSet<ReporteComparacionTrasladosDto>();
        
        if(!tablaComparacion.isEmpty()){
            for(TablaComparacionTarifaDto sede : tablaComparacion){
                //Titulos de las sedes (codigo habilitacion + nombre sede
                titulosSedes.add( (sede.getCodigoHabilitacionSede()==null?"":sede.getCodigoHabilitacionSede()) + " " + sede.getNombreSede() );
                
                //Valores de los procedimientos en cada sede
                List<ReporteComparacionTrasladosDto> dtosReporteValoresSede = comparacionTarifas
                        .reporteComparacionTrasladosExcelComparados(prestadorId, sedesId, sede.getSedeId());

                for(int j = 0;j<dtosReporte.size();j++){
                        ReporteComparacionTrasladosDto registro = dtosReporte.get(j);
                        Integer valorSede = dtosReporteValoresSede.get(j).getValorSedeComp();
                        registro.getValoresSedes().add(valorSede!=null?valorSede.toString():"");
                }
            }
        }
    
        return comparacionTarifas.generateExcelTraslados(dtosReporte, titulosSedes, prestadorNit);
    }
    
    /**
    * Consulta los traslados del prestador de referencia y sus campos respectivos para el reporte de excel
    * 
    * @param prestadorId
    * @param sedesId
    * @return Lista prestadores que dan cobertura a los traslados por sede.
    */
    public List<TablaComparacionTarifaDto> coberturaSedeTrasladosPrestador(Long prestadorId, List<Long> sedesId){
        return comparacionTarifas.coberturaSedeTrasladosPrestador(prestadorId, sedesId);
    }

	public List<CapituloProcedimientoDto> findCapituloProcedureBySede(Long pretadorId, List<Long> sedesId) throws Exception  {
        return comparacionTarifas.findCapituloProcedureBySede(pretadorId, sedesId);
	}

	public List<CategoriaProcedimientoDto> findCategoriaByPrestadorIdSedesIdAndCapituloId(Long id, List<Long> sedesId,
			List<Long> capitulosId) throws Exception {
		return comparacionTarifas.findCategoriaByPrestadorIdSedesIdAndCapituloId(id, sedesId, capitulosId);
	}

	public List<TablaComparacionTarifaDto> coberturaSedePrestadorPGP(Long id, List<Long> sedesId,
			List<Long> capitulosId, List<Long> categoriasId) {
		return comparacionTarifas.coberturaSedePrestadorPGP( id, sedesId,
				 capitulosId,  categoriasId);
	}
    
}
