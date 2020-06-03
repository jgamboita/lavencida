package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.hiringrequest.GetHiringRequestToVoBoRequest;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroConsultaSolicitudDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

/**
 * Class GetHiringRequestToVoBoRequestUtil
 * @author aquintero
 */
public class GetHiringRequestToVoBoRequestUtil
{

    public static GetHiringRequestToVoBoRequest resetRequest(FiltroConsultaSolicitudDto filtroDto,
                                                             NegociacionDto negDto)
    {
        GetHiringRequestToVoBoRequest request = new GetHiringRequestToVoBoRequest();
        request.setGetFilterRequestDto(filtroDto);
        request.setNegotiationDto(negDto);
        return request;
    }

}
