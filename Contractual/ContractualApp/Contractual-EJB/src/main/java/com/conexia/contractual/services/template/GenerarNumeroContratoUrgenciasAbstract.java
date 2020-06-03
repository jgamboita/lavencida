package com.conexia.contractual.services.template;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.ContratoUrgenciasDto;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.SecuenciaContratoDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.exceptions.ConexiaBusinessException;

/**
 * Template necesario para la implementar la generacion de un numero de contrato.
 * @author jalvarado
 */
public abstract class GenerarNumeroContratoUrgenciasAbstract {

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    @PersistenceContext(unitName ="contractualDS")
    private EntityManager em;

    public final String generarNumeroContrato(final ContratoUrgenciasDto contratoDto, SecuenciaContratoDto secuencia) throws ConexiaBusinessException {
        if (contratoDto == null) {
            throw new IllegalArgumentException("El contrato no puede ser nulo");
        }

        if (contratoDto.getPrestador() == null) {
            throw new IllegalArgumentException("El prestador no puede ser nulo.");
        }
        return
        		this.retornartipoContrato()
                + this.retornarGuion()
                + this.codigoRegional(contratoDto)
                + this.modalidadContratacion(contratoDto)
                + this.regimen(contratoDto)
                + this.anio(contratoDto)
                + this.consecutivo(contratoDto,secuencia);
    }

    private String retornarPrefijo(final PrestadorDto prestadorDto) throws ConexiaBusinessException {
        if (prestadorDto.getPrefijo() == null || prestadorDto.getPrefijo().length() < 3 || prestadorDto.getPrefijo().length() > 4) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.NUMERO_CONTRATO_PREFIJO);
        }
        return prestadorDto.getPrefijo();
    }

    private String codigoRegional(final ContratoUrgenciasDto contratoDto) throws ConexiaBusinessException {
        if (contratoDto.getRegionalDto().getCodigo() == null || contratoDto.getRegionalDto().getCodigo().length() == 0 || contratoDto.getRegionalDto().getCodigo().length() > 2) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.NUMERO_CONTRATO_REGIONAL);
        }
        return contratoDto.getRegionalDto().getCodigo().substring(contratoDto.getRegionalDto().getCodigo().length() - 1);
    }

    private String modalidadContratacion(final ContratoUrgenciasDto contratoDto) throws ConexiaBusinessException {
        if (contratoDto.getTipoModalidadNegociacion().getCodigoContratacion().length() == 0) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.NUMERO_CONTRATO_MODALIDAD);
        }
        if (contratoDto.getTipoModalidadNegociacion() == null) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.NUMERO_CONTRATO_MODALIDAD);
        }
        return contratoDto.getTipoModalidadNegociacion().getCodigoContratacion();
    }

    private String regimen(final ContratoUrgenciasDto contratoDto) throws ConexiaBusinessException {
        if (contratoDto.getRegimen() == null || contratoDto.getRegimen().getCodigoContratacion().length() == 0) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.NUMERO_CONTRATO_REGIMEN);
        }
        return contratoDto.getRegimen().getCodigoContratacion();
    }

    private String anio(final ContratoUrgenciasDto contratoDto) throws ConexiaBusinessException {
        if (contratoDto.getFechaInicioVigencia() == null) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.NUMERO_CONTRATO_ANIO);
        }
        return ((contratoDto.getFechaInicioVigencia().getYear() + 1900) + "").substring(2, 4);
    }

    private String retornarGuion() {
        return "-";
    }

    private String retornartipoContrato() {
        return "URG";
    }


	protected abstract String consecutivo(ContratoUrgenciasDto contrato, SecuenciaContratoDto secuencia) throws ConexiaBusinessException;

}
