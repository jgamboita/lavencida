package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.packagetarrifannex.GetBriefcaseTechnologiesPackageTarrifAnnexRequest;

public class GetBriefcaseTechnologiesPackageTarrifAnnexRequestUtil
{

    public static GetBriefcaseTechnologiesPackageTarrifAnnexRequest resetRequest(String packageCode, Long briefcaseId)
    {
        GetBriefcaseTechnologiesPackageTarrifAnnexRequest request = new GetBriefcaseTechnologiesPackageTarrifAnnexRequest();
        request.setBriefcaseId(briefcaseId);
        request.setPackageCode(packageCode);
        return request;
    }

}
