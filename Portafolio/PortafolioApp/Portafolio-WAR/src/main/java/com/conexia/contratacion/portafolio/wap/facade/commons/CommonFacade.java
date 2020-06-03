package com.conexia.contratacion.portafolio.wap.facade.commons;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.DescriptivoDto;
import com.conexia.contratacion.commons.dto.maestros.ClasificacionPrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.GrupoInsumoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.conexia.contratacion.portafolio.definitions.commons.CommonViewServiceRemote;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.servicefactory.CnxService;

/**
 *
 * @author Andrés Mise Olivera
 */
public class CommonFacade implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
	@CnxService
	private CommonViewServiceRemote commonViewService;

	public List<ClasificacionPrestadorDto> listarClasificacionesPrestador() {
		return commonViewService.listarClasificacionesPrestador();
	}

	public List<GrupoInsumoDto> listarGruposInsumoEmpresarial() {
		return commonViewService.listarGruposInsumoEmpresarial();
	}

	public List<TipoIdentificacionDto> listarTiposIdentificacion() {
		return commonViewService.listarTiposIdentificacion();
	}

	public List<DepartamentoDto> listarDepartamentos()
			throws ConexiaBusinessException {
		return commonViewService.listarDepartamentos();
	}

	public List<MunicipioDto> listarMunicipios(DepartamentoDto departamento)
			throws ConexiaBusinessException {
		return commonViewService.listarMunicipios(departamento);
	}

	public List<DescriptivoDto> listarZonas() throws ConexiaBusinessException {
		return commonViewService.listarZonas();
	}

}
