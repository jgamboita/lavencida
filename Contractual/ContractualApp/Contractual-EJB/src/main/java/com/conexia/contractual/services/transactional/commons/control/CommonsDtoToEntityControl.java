package com.conexia.contractual.services.transactional.commons.control;

import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.model.maestros.Municipio;
import com.conexia.contractual.model.maestros.Regional;
import com.conexia.contractual.model.maestros.TipoIdentificacion;
import com.conexia.contractual.model.security.User;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;

/**
 *
 * @author jalvarado
 */
public class CommonsDtoToEntityControl {
    
    /**
     * Retorna un objeto del entity de prestador.
     * @param dto - Dto de solicitud de contratacion.
     * @return prestador.
     */
    public Prestador dtoToEntityPrestador(final SolicitudContratacionParametrizableDto dto) {
        final Prestador pres = new Prestador();
        pres.setId(dto.getPrestadorDto().getId());
        return pres;
    }
    
    /**
     * Retorna un objeto del entity de prestador.
     * @param idUsuario - id del usuario.
     * @return User.
     */
    public User dtoToEntityUser(final Integer idUsuario) {
        final User user = new User(idUsuario);
        return user;
    }
    
    /**
     * Funcion que retorna una regional a partir de un id la regional.
     * @param idRegional
     * @return Regional
     */
    public Regional dtoToEntityRegional(final Integer idRegional) {
        final Regional regional = new Regional();
        regional.setId(idRegional);
        return regional;
    }
    
    /**
     * Funcion que retorna un entity de tipo de identificacion a partir de su dto.
     * @param tipoIdentificacionDto - Dto de tipo de identificacion.
     * @return Entity de tipo de identificacion.
     */
    public TipoIdentificacion dtoToEntityTipoIdentificacion(final TipoIdentificacionDto tipoIdentificacionDto) {
        final TipoIdentificacion tipoIdentificacion = new TipoIdentificacion();
        tipoIdentificacion.setId(tipoIdentificacionDto.getId());
        tipoIdentificacion.setCodigo(tipoIdentificacionDto.getCodigo());
        tipoIdentificacion.setDescripcion(tipoIdentificacionDto.getDescripcion());
        return tipoIdentificacion;
    }
    
    public Municipio dtoToEntityMunicipio(final MunicipioDto municipioDto) {
        final Municipio municipio = new Municipio();
        municipio.setId(municipioDto.getId());
        return municipio;
    }
    
}
