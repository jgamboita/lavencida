package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.hiringrequest.HowMuchHiringRequestToParameterizeRequest;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroConsultaSolicitudDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

/**
 * Class HowMuchHiringRequestToParameterizeRequestUtil
 * @author aquintero
 */
public class HowMuchHiringRequestToParameterizeRequestUtil
{

    public static HowMuchHiringRequestToParameterizeRequest resetRequest(FiltroConsultaSolicitudDto filtroConsultaSolicitudDto,
                                                                         NegociacionDto negDto)
    {
        HowMuchHiringRequestToParameterizeRequest request = new HowMuchHiringRequestToParameterizeRequest();
        request.setGetFilterRequestDto(filtroConsultaSolicitudDto);
        request.setNegotiationDto(negDto);
        return request;
    }

}
