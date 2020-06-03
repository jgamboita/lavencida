package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.negotiation.FinishNegotiationRequest;
import com.conexia.contratacion.commons.constants.enums.EstadoNegociacionEnum;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

/**
 * Class FinishNegotiationRequestUtil
 * @author aquintero
 */
public class FinishNegotiationRequestUtil
{

    public static FinishNegotiationRequest resetRequest(NegociacionDto negotiationDto,
                                                        EstadoNegociacionEnum statusNegotiationEnum)
    {
        FinishNegotiationRequest request = new FinishNegotiationRequest();
        request.setNegotiationDto(negotiationDto);
        request.setStatusNegotiationEnum(statusNegotiationEnum);
        return request;
    }

}
