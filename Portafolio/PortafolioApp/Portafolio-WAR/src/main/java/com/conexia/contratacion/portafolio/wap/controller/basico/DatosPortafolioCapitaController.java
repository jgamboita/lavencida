/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.conexia.contratacion.portafolio.wap.controller.basico;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import com.conexia.contratacion.commons.dto.capita.MedicamentoCapitaDto;
import com.conexia.contratacion.commons.dto.capita.ProcedimientoCapitaDto;
import com.ocpsoft.pretty.faces.annotation.URLMapping;

/**
 *
 * @author dmora
 */

@Named
@ViewScoped
@URLMapping(id = "datosPortafolio", pattern = "/gestion/datosPortafolio", viewId = "/basico/datosPortafolio.page")
public class DatosPortafolioCapitaController implements Serializable {
    
    private static final long serialVersionUID = -5821142040501439547L;

    private List<ProcedimientoCapitaDto> lsProcedimientoCapita;
    private List<MedicamentoCapitaDto> lsMedicamentoCapita;

	/**
	 * Constructor por defecto
	 */
	public DatosPortafolioCapitaController(){}

	/**
	 * Metodo postConstruct
	 */
	@PostConstruct
	public void postConstruct(){
            preCargaProcedimientos();
            preCargaMedicamentos();

	}
    
    
    private void preCargaProcedimientos(){
        
        lsProcedimientoCapita = new ArrayList<ProcedimientoCapitaDto>();
        ProcedimientoCapitaDto proced = new ProcedimientoCapitaDto();
        proced.setGrupoHabilitacion("CONSULTA EXTERNA");
        proced.setServicioHabilitacion("361-CARDIOLOGIA PEDIATRICA");
        proced.setPorcentaje(13);
        proced.setValor(57500);
        proced.setEstado("CARGADA DESDE MINISTERIO");
        lsProcedimientoCapita.add(proced);
       
        ProcedimientoCapitaDto proced2 = new ProcedimientoCapitaDto();
        proced2.setGrupoHabilitacion("APOYO DIAGNOSTICO Y COMPLEMENTACION TERAPEUTICA");
        proced2.setServicioHabilitacion("701 - DIAGNOSTICO CARDIOVASCULAR");
        proced2.setPorcentaje(8);
        proced2.setValor(92520);
        proced2.setEstado("CARGADA DESDE MINISTERIO");
        lsProcedimientoCapita.add(proced2);
    }
    
    private void preCargaMedicamentos(){
        
        lsMedicamentoCapita = new ArrayList<MedicamentoCapitaDto>();
        MedicamentoCapitaDto medicina = new  MedicamentoCapitaDto();
        medicina.setGrupoTerapeutico("");
        medicina.setInclusion("Si");
        medicina.setPorcentaje(0);
        lsMedicamentoCapita.add(medicina);
        
        MedicamentoCapitaDto medicina2 = new  MedicamentoCapitaDto();
        medicina2.setGrupoTerapeutico("");
        medicina2.setInclusion("Si");
        medicina2.setPorcentaje(0);
        lsMedicamentoCapita.add(medicina2);
        
        MedicamentoCapitaDto medicina3 = new  MedicamentoCapitaDto();
        medicina3.setGrupoTerapeutico("");
        medicina3.setInclusion("Si");
        medicina3.setPorcentaje(0);
        lsMedicamentoCapita.add(medicina3);
    }

    public List<ProcedimientoCapitaDto> getLsProcedimientoCapita() {
        return lsProcedimientoCapita;
    }

    public void setLsProcedimientoCapita(List<ProcedimientoCapitaDto> lsProcedimientoCapita) {
        this.lsProcedimientoCapita = lsProcedimientoCapita;
    }

    public List<MedicamentoCapitaDto> getLsMedicamentoCapita() {
        return lsMedicamentoCapita;
    }

    public void setLsMedicamentoCapita(List<MedicamentoCapitaDto> lsMedicamentoCapita) {
        this.lsMedicamentoCapita = lsMedicamentoCapita;
    }

}
