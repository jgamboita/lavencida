package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.packagetarrifannex.GetPackageTarrifAnnexRequest;

/**
 * Class GetPackageTarrifAnnexRequestUtil
 * @author aquintero
 */
public class GetPackageTarrifAnnexRequestUtil
{

    public static GetPackageTarrifAnnexRequest resetRequest(Long negotiationId, Boolean isOtroSi,
                                                            Long negotiationParentId)
    {
        GetPackageTarrifAnnexRequest request = new GetPackageTarrifAnnexRequest();
        request.setNegociacionId(negotiationId);
        request.setEsOtroSi(isOtroSi);
        request.setNegociacionPadreId(negotiationParentId);
        return request;
    }

}
