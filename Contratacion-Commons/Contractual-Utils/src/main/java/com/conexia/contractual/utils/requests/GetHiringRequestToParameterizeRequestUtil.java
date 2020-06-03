package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.hiringrequest.GetHiringRequestToParameterizeRequest;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroConsultaSolicitudDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

/**
 * Class GetHiringRequestToParameterizeRequestUtil
 * @author aquintero
 */
public class GetHiringRequestToParameterizeRequestUtil
{

    public static GetHiringRequestToParameterizeRequest resetRequest(FiltroConsultaSolicitudDto filtroDto,
                                                                     NegociacionDto negDto)
    {
        GetHiringRequestToParameterizeRequest request = new GetHiringRequestToParameterizeRequest();
        request.setGetFilterRequestDto(filtroDto);
        request.setNegotiationDto(negDto);
        return request;
    }

}
