package com.conexia.contratacion.portafolio.wap.controller.commons;

import com.conexia.contratacion.commons.dto.maestros.ClasificacionPrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.GrupoInsumoDto;
import com.conexia.contratacion.portafolio.wap.facade.commons.CommonFacade;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.conexia.exceptions.ConexiaBusinessException;

import java.io.Serializable;
import java.util.List;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Named;

/**
 *
 * @author Andrés Mise Olivera
 */
public class CommonController implements Serializable {

    private static final long serialVersionUID = 1L;

    @Inject
    private CommonFacade commonFacade;

    @Produces
    @Named("listaClasificacionesPrestador")
    @SessionScoped
    public List<ClasificacionPrestadorDto> getClasificacionesPrestador() {
        return this.commonFacade.listarClasificacionesPrestador();
    }
    
    @Produces
    @Named("listaGruposInsumoEmpresarial")
    @SessionScoped
    public List<GrupoInsumoDto> getGruposInsumoEmpresarial() {
        return this.commonFacade.listarGruposInsumoEmpresarial();
    }
    
    @Produces
    @Named("listaTiposDocumento")
    @SessionScoped
    public List<TipoIdentificacionDto> getTiposDocumento() {
        return this.commonFacade.listarTiposIdentificacion();
    }

    @Produces
    @Named("listaDepartamentos")
    @SessionScoped
    public List<DepartamentoDto> getDepartamentos() throws ConexiaBusinessException {
        return this.commonFacade.listarDepartamentos();
    }
    
}
