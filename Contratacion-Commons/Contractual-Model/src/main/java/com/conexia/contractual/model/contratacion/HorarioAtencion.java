package com.conexia.contractual.model.contratacion;

import com.conexia.contractual.common.persistence.Identifiable;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Entidad HorarioAtencion.
 * 
 * @author jjoya
 * @date 24/08/2014
 */

@Entity
@Table(name = "horario_atencion", schema = "contratacion")
public class HorarioAtencion implements Identifiable<Long>, Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;
	
	@Column(name = "lunes_inicial")
	private String lunesInicial;
	
	@Column(name = "lunes_final")
	private String lunesFinal;

	@Column(name = "martes_inicial")
	private String martesInicial;
	
	@Column(name = "martes_final")
	private String martesFinal;
	
	@Column(name = "miercoles_inicial")
	private String miercolesInicial;
	
	@Column(name = "miercoles_final")
	private String miercolesFinal;
	
	@Column(name = "jueves_inicial")
	private String juevesInicial;
	
	@Column(name = "jueves_final")
	private String juevesFinal;

	@Column(name = "viernes_inicial")
	private String viernesInicial;
	
	@Column(name = "viernes_final")
	private String viernesFinal;
	
	@Column(name = "sabado_inicial")
	private String sabadoInicial;
	
	@Column(name = "sabado_final")
	private String sabadoFinal;
	
	@Column(name = "domingo_inicial")
	private String domingoInicial;
	
	@Column(name = "domingo_final")
	private String domingoFinal;
	
	@Column(name = "festivo_inicial")
	private String festivoInicial;
	
	@Column(name = "festivo_final")
	private String festivoFinal;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getLunesInicial() {
		return lunesInicial;
	}

	public void setLunesInicial(String lunesInicial) {
		this.lunesInicial = lunesInicial;
	}

	public String getLunesFinal() {
		return lunesFinal;
	}



	public void setLunesFinal(String lunesFinal) {
		this.lunesFinal = lunesFinal;
	}

	public String getMartesInicial() {
		return martesInicial;
	}

	public void setMartesInicial(String martesInicial) {
		this.martesInicial = martesInicial;
	}

	public String getMartesFinal() {
		return martesFinal;
	}

	public void setMartesFinal(String martesFinal) {
		this.martesFinal = martesFinal;
	}

	public String getMiercolesInicial() {
		return miercolesInicial;
	}

	public void setMiercolesInicial(String miercolesInicial) {
		this.miercolesInicial = miercolesInicial;
	}

	public String getMiercolesFinal() {
		return miercolesFinal;
	}

	public void setMiercolesFinal(String miercolesFinal) {
		this.miercolesFinal = miercolesFinal;
	}

	public String getJuevesInicial() {
		return juevesInicial;
	}

	public void setJuevesInicial(String juevesInicial) {
		this.juevesInicial = juevesInicial;
	}

	public String getJuevesFinal() {
		return juevesFinal;
	}

	public void setJuevesFinal(String juevesFinal) {
		this.juevesFinal = juevesFinal;
	}

	public String getViernesInicial() {
		return viernesInicial;
	}

	public void setViernesInicial(String viernesInicial) {
		this.viernesInicial = viernesInicial;
	}

	public String getViernesFinal() {
		return viernesFinal;
	}

	public void setViernesFinal(String viernesFinal) {
		this.viernesFinal = viernesFinal;
	}

	public String getSabadoInicial() {
		return sabadoInicial;
	}

	public void setSabadoInicial(String sabadoInicial) {
		this.sabadoInicial = sabadoInicial;
	}

	public String getSabadoFinal() {
		return sabadoFinal;
	}

	public void setSabadoFinal(String sabadoFinal) {
		this.sabadoFinal = sabadoFinal;
	}

	public String getDomingoInicial() {
		return domingoInicial;
	}

	public void setDomingoInicial(String domingoInicial) {
		this.domingoInicial = domingoInicial;
	}

	public String getDomingoFinal() {
		return domingoFinal;
	}

	public void setDomingoFinal(String domingoFinal) {
		this.domingoFinal = domingoFinal;
	}

	public String getFestivoInicial() {
		return festivoInicial;
	}

	public void setFestivoInicial(String festivoInicial) {
		this.festivoInicial = festivoInicial;
	}

	public String getFestivoFinal() {
		return festivoFinal;
	}

	public void setFestivoFinal(String festivoFinal) {
		this.festivoFinal = festivoFinal;
	}
}
