package com.conexia.contratacion.portafolio.services.transactional.prestador.controls;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;

public class SedePrestadorTransactionalControl implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5761460356096082947L;

	
	public Map<String, Object> generarUpdateSedePrestador(StringBuilder queryString,
			SedePrestadorDto sedePrestadorAnterior, SedePrestadorDto sedePrestadorNueva) {

		
		Map<String, Object> parametros = new HashMap<String, Object>();
		queryString.append("UPDATE SedePrestador s ");
		
		if ((sedePrestadorNueva.getNombreSede() == null)
				|| !(sedePrestadorNueva.getNombreSede()
						.equals(sedePrestadorAnterior.getNombreSede()))) {

			queryString.append(" SET s.nombreSede = :nombreSede");
			parametros.put("nombreSede", sedePrestadorNueva.getNombreSede());
		}
		
		
		if ((sedePrestadorNueva.getSedePrincipal() == null)
				|| !(sedePrestadorNueva.getSedePrincipal()
						.equals(sedePrestadorAnterior.getSedePrincipal()))) {

			if(parametros.size() > 0){
				queryString.append(",");
			} else {
				queryString.append(" SET");
			}
			
			queryString.append(" s.sedePrincipal = :sedePrincipal");
			parametros.put("sedePrincipal", sedePrestadorNueva.getSedePrincipal());
		}
		
		if ((sedePrestadorNueva.getCodigoPrestador() == null)
				|| !(sedePrestadorNueva.getCodigoPrestador()
						.equals(sedePrestadorAnterior.getCodigoPrestador()))) {

			if(parametros.size() > 0){
				queryString.append(",");
			} else {
				queryString.append(" SET");
			}
			
			queryString.append(" s.codigoPrestador = :codigoPrestador");
			parametros.put("codigoPrestador", sedePrestadorNueva.getCodigoPrestador());
		}
		
		if ((sedePrestadorNueva.getCodigoSede() == null)
				|| !(sedePrestadorNueva.getCodigoSede()
						.equals(sedePrestadorAnterior.getCodigoSede()))) {

			if(parametros.size() > 0){
				queryString.append(",");
			} else {
				queryString.append(" SET");
			}
			
			queryString.append(" s.codigoSede = :codigoSede");
			parametros.put("codigoSede", sedePrestadorNueva.getCodigoSede());
		}
		
		if ((sedePrestadorNueva.getMunicipio() == null)
				|| !(sedePrestadorNueva.getMunicipio()
						.equals(sedePrestadorAnterior.getMunicipio()))) {

			if(parametros.size() > 0){
				queryString.append(",");
			} else {
				queryString.append(" SET");
			}
			
			queryString.append(" s.municipio.id = :municipioId");
			parametros.put("municipioId", sedePrestadorNueva.getMunicipio());
		}
		
		if ((sedePrestadorNueva.getZonaId() == null)
				|| !(sedePrestadorNueva.getZonaId()
						.equals(sedePrestadorAnterior.getZonaId()))) {

			if(parametros.size() > 0){
				queryString.append(",");
			} else {
				queryString.append(" SET");
			}
			
			queryString.append(" s.zona.id = :zonaId");
			parametros.put("zonaId", sedePrestadorNueva.getZonaId());
		}
				
		if ((sedePrestadorNueva.getBarrio() == null)
				|| !(sedePrestadorNueva.getBarrio()
						.equals(sedePrestadorAnterior.getBarrio()))) {

			if(parametros.size() > 0){
				queryString.append(",");
			} else {
				queryString.append(" SET");
			}
			
			queryString.append(" s.barrio = :barrio");
			parametros.put("barrio", sedePrestadorNueva.getBarrio());
		}
		
		if ((sedePrestadorNueva.getDireccion() == null)
				|| !(sedePrestadorNueva.getDireccion()
						.equals(sedePrestadorAnterior.getDireccion()))) {

			if(parametros.size() > 0){
				queryString.append(",");
			} else {
				queryString.append(" SET");
			}
			
			queryString.append(" s.direccion = :direccion");
			parametros.put("direccion", sedePrestadorNueva.getDireccion());
		}
		
		
		if ((sedePrestadorNueva.getTelefonoAdministrativo() == null)
				|| !(sedePrestadorNueva.getTelefonoAdministrativo()
						.equals(sedePrestadorAnterior.getTelefonoAdministrativo()))) {

			if(parametros.size() > 0){
				queryString.append(",");
			} else {
				queryString.append(" SET");
			}
			
			queryString.append(" s.telefonoAdministrativo = :telefonoAdministrativo");
			parametros.put("telefonoAdministrativo", sedePrestadorNueva.getTelefonoAdministrativo());
		}
		
		if ((sedePrestadorNueva.getFax() == null)
				|| !(sedePrestadorNueva.getFax()
						.equals(sedePrestadorAnterior.getFax()))) {

			if(parametros.size() > 0){
				queryString.append(",");
			} else {
				queryString.append(" SET");
			}
			
			queryString.append(" s.fax = :fax");
			parametros.put("fax", sedePrestadorNueva.getFax());
		}
		
		if ((sedePrestadorNueva.getTelefonoCitas() == null)
				|| !(sedePrestadorNueva.getTelefonoCitas()
						.equals(sedePrestadorAnterior.getTelefonoCitas()))) {

			if(parametros.size() > 0){
				queryString.append(",");
			} else {
				queryString.append(" SET");
			}
			
			queryString.append(" s.telefonoCitas = :telefonoCitas");
			parametros.put("telefonoCitas", sedePrestadorNueva.getTelefonoCitas());
		}
		
		if ((sedePrestadorNueva.getCorreo() == null)
				|| !(sedePrestadorNueva.getCorreo()
						.equals(sedePrestadorAnterior.getCorreo()))) {

			if(parametros.size() > 0){
				queryString.append(",");
			} else {
				queryString.append(" SET");
			}
			
			queryString.append(" s.correo = :correo");
			parametros.put("correo", sedePrestadorNueva.getCorreo());
		}
		
		if ((sedePrestadorNueva.getGerente() == null)
				|| !(sedePrestadorNueva.getGerente()
						.equals(sedePrestadorAnterior.getGerente()))) {

			if(parametros.size() > 0){
				queryString.append(",");
			} else {
				queryString.append(" SET");
			}
			
			queryString.append(" s.gerente = :gerente");
			parametros.put("gerente", sedePrestadorNueva.getGerente());
		}
		
		return parametros;
	}
	
	public void generarWhereSedePrestador(StringBuilder queryString,
			SedePrestadorDto sedePrestadorAnterior, Map<String, Object> parametros){
		
		queryString.append(" WHERE s.id = :sedePrestadorId");
		parametros.put("sedePrestadorId", sedePrestadorAnterior.getId());
	}
}
