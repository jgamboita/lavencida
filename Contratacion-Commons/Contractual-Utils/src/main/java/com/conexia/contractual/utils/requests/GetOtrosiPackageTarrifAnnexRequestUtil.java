package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.packagetarrifannex.GetOtrosiPackageTarrifAnnexRequest;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

/**
 * Class GetOtrosiPackageTarrifAnnexRequestUtil
 * @author aquintero
 */
public class GetOtrosiPackageTarrifAnnexRequestUtil
{

    public static GetOtrosiPackageTarrifAnnexRequest resetRequest(NegociacionDto negotiationDto)
    {
        GetOtrosiPackageTarrifAnnexRequest request = new GetOtrosiPackageTarrifAnnexRequest();
        request.setNegotiationDto(negotiationDto);
        return request;
    }

}
