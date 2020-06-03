package com.conexia.contractual.services.transactional.parametrizacion.control;

import com.conexia.contratacion.commons.constants.enums.EstadoLegalizacionEnum;
import com.conexia.contratacion.commons.constants.enums.EstadoParametrizacionEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.model.contratacion.negociacion.Negociacion;
import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacion;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacionSede;
import com.conexia.contractual.model.security.User;
import com.conexia.contractual.services.transactional.commons.control.CommonsDtoToEntityControl;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import java.util.Date;
import javax.inject.Inject;

/**
 * Control transaccional para la parametrizacion de contratos.
 *
 * @author jalvarado
 */
public class ParametrizacionContratoDtoToEntityControl {

    @Inject
    private CommonsDtoToEntityControl commonsDtoToEntityControl;

    /**
     * Retorna el entity de solicitud de contratacion.
     * @param dto - Dto de solicitud de contratacion.
     * @param user - Usuario que origin la solicitud de contratacion.
     * @return SolicitudContratacion
     */
    public SolicitudContratacion dtoToEntitySolicitudContratacion(final SolicitudContratacionParametrizableDto dto,
            final User user) {
        final SolicitudContratacion sc = new SolicitudContratacion();
        sc.setNegociacion(this.dtoToEntityNegociacion(dto));
        sc.setFechaCreacionSolicitud(new Date());
        sc.setPrestador(commonsDtoToEntityControl.dtoToEntityPrestador(dto));
        sc.setTipoModalidadNegociacion(dto.getModalidadNegociacionEnum());
        sc.setNuevoContrato(Boolean.TRUE);
        sc.setUser(user);
        sc.setEstadoParametrizacion(EstadoParametrizacionEnum.PEND_PARAMETRIZACION);
        sc.setEstadoLegalizacion(EstadoLegalizacionEnum.PEND_LEGALIZACION);
        return sc;
    }

    public SolicitudContratacionSede dtoToEntitySolicitudContratacionSede(final SolicitudContratacion sc,
            final Long sedeNegociacionId, final User user) {
        final SolicitudContratacionSede scs = new SolicitudContratacionSede();
        scs.setSolicitudContratacion(sc);
        scs.setEstadoParametrizacionEnum(EstadoParametrizacionEnum.PEND_PARAMETRIZACION);
        scs.setFechaEstado(new Date());
        scs.setSedeNegociacion(this.dtoToEntitySedeNegociacion(sedeNegociacionId));
        scs.setUser(user);
        return scs;
    }

    /**
     * Retorna un objeto del entity de negociacion.
     * @param dto - Dto de solicitud de contratacion.
     * @return negociacion.
     */
    private Negociacion dtoToEntityNegociacion(final SolicitudContratacionParametrizableDto dto) {
        final Negociacion neg = new Negociacion();
        neg.setId(dto.getNumeroNegociacion());
        return neg;
    }



    private SedesNegociacion dtoToEntitySedeNegociacion(final Long sedeNegociacionId) {
        final SedesNegociacion sn = new SedesNegociacion();
        sn.setId(sedeNegociacionId);
        return sn;
    }

}
