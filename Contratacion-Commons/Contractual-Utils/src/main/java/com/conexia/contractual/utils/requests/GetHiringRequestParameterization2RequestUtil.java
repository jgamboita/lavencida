package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.hiringrequest.GetHiringRequestParameterization2Request;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;

/**
 * Class GetHiringRequestParameterization2RequestUtil
 * @author aquintero
 */
public class GetHiringRequestParameterization2RequestUtil
{

    public static GetHiringRequestParameterization2Request resetRequest(Long negotiationId,
                                                                        RegimenNegociacionEnum regimeNegotiationEnum,
                                                                        NegociacionModalidadEnum modalityNegotiationEnum)
    {
        GetHiringRequestParameterization2Request request = new GetHiringRequestParameterization2Request();
        request.setNegotiationId(negotiationId);
        request.setRegimeNegotiationEnum(regimeNegotiationEnum);
        request.setModalityNegotiationEnum(modalityNegotiationEnum);
        return request;
    }

}
