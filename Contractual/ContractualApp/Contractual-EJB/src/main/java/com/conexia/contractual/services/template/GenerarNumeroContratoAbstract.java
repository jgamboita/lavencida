package com.conexia.contractual.services.template;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.SecuenciaContratoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 * Template necesario para la implementar la generacion de un numero de contrato.
 * @author jalvarado
 */
public abstract class GenerarNumeroContratoAbstract {
    
    @Inject
    private ConexiaExceptionUtils exceptionUtils;
    
    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;
    
    public final String generarNumeroContrato(final ContratoDto contratoDto, SecuenciaContratoDto secuencia) throws ConexiaBusinessException {
        if (contratoDto == null) {
            throw new IllegalArgumentException("El contrato no puede ser nulo");
        }
        if (contratoDto.getSolicitudContratacionParametrizableDto() == null) {
            throw new IllegalArgumentException("La solicitud de contratacion no puede ser nula.");
        }
        if (contratoDto.getSolicitudContratacionParametrizableDto().getPrestadorDto() == null) {
            throw new IllegalArgumentException("El prestador no puede ser nulo.");
        }
        return this.retornarPrefijo(contratoDto.getSolicitudContratacionParametrizableDto().getPrestadorDto()) 
                + this.retornarGuion()
                + this.codigoRegional(contratoDto)
                + this.modalidadContratacion(contratoDto.getSolicitudContratacionParametrizableDto())
                + this.regimen(contratoDto.getSolicitudContratacionParametrizableDto())
                + this.anio(contratoDto)
                + this.consecutivo(contratoDto,secuencia);
    }
    
    private String retornarPrefijo(final PrestadorDto prestadorDto) throws ConexiaBusinessException {
        if (prestadorDto.getPrefijo() == null || prestadorDto.getPrefijo().length() < 3 || prestadorDto.getPrefijo().length() > 4) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.NUMERO_CONTRATO_PREFIJO);
        } 
        return prestadorDto.getPrefijo();
    }
    
    private String codigoRegional(final ContratoDto contratoDto) throws ConexiaBusinessException {
        if (contratoDto.getRegionalDto().getCodigo() == null || contratoDto.getRegionalDto().getCodigo().length() == 0 || contratoDto.getRegionalDto().getCodigo().length() > 2) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.NUMERO_CONTRATO_REGIONAL);
        }
        return contratoDto.getRegionalDto().getCodigo().substring(contratoDto.getRegionalDto().getCodigo().length() - 1);
    }
    
    private String modalidadContratacion(final SolicitudContratacionParametrizableDto solicitudDto) throws ConexiaBusinessException
    {
        if (solicitudDto.getModalidadNegociacionEnum() == null) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.NUMERO_CONTRATO_MODALIDAD);
        }
        return solicitudDto.getModalidadNegociacionEnum().getCodigoContratacion();
    }
    
    private String regimen(final SolicitudContratacionParametrizableDto solicitud) throws ConexiaBusinessException {
        if (solicitud.getRegimenNegociacion() == null || solicitud.getRegimenNegociacion().getCodigoContratacion().length() == 0) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.NUMERO_CONTRATO_REGIMEN);
        }
        return solicitud.getRegimenNegociacion().getCodigoContratacion();
    }
    
    private String anio(final ContratoDto contratoDto) throws ConexiaBusinessException {
        if (contratoDto.getFechaInicioVigencia() == null) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.NUMERO_CONTRATO_ANIO);
        }
        return ((contratoDto.getFechaInicioVigencia().getYear() + 1900) + "").substring(2, 4);
    }
    
    private String retornarGuion() {
        return "-";
    }
    
    protected abstract String consecutivo(final ContratoDto contratoDto, SecuenciaContratoDto secuencia) throws ConexiaBusinessException;
    
}
