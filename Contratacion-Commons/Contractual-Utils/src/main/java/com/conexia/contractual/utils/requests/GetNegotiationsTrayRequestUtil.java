package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.negotiation.GetNegotiationsTrayRequest;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import java.util.Objects;

/**
 * Class GetNegotiationsTrayRequestUtil
 * @author aquintero
 */
public class GetNegotiationsTrayRequestUtil
{

    public static GetNegotiationsTrayRequest resetRequest(
            Long prestadorId, NegociacionDto negociacion)
    {
        GetNegotiationsTrayRequest request = new GetNegotiationsTrayRequest();
        request.setLenderId(prestadorId);
        request.setOriginNegotiationId(Objects.nonNull(negociacion.getNegociacionOrigen()) ?
                negociacion.getNegociacionOrigen().getId() : null);
        return request;
    }
}
