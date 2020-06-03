package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.legalizationcontract.GetLegalizationContractRequest;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;

/**
 * Class GetLegalizationContractRequestUtil
 * @author aquintero
 */
public class GetLegalizationContractRequestUtil
{

    public static GetLegalizationContractRequest resetRequest(SolicitudContratacionParametrizableDto solicitud)
    {
        GetLegalizationContractRequest request = new GetLegalizationContractRequest();
        request.setContratacionParametrizableDto(solicitud);
        return request;
    }

}
