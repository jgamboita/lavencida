package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.hiringrequest.HowMuchHiringRequestToVoBoRequest;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroConsultaSolicitudDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

/**
 * Class HowMuchHiringRequestToVoBoRequestUtil
 * @author aquinteri
 */
public class HowMuchHiringRequestToVoBoRequestUtil
{

    public static HowMuchHiringRequestToVoBoRequest resetRequest(FiltroConsultaSolicitudDto filtroDto,
                                                                 NegociacionDto negDto)
    {
        HowMuchHiringRequestToVoBoRequest request = new HowMuchHiringRequestToVoBoRequest();
        request.setGetFilterRequestDto(filtroDto);
        request.setNegotiationDto(negDto);
        return request;
    }

}
