package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.packagerequirement.GetBriefcasePackageRequirementRequest;

/**
 * Class GetBriefcasePackageRequirementRequestUtil
 * @author aquintero
 */
public class GetBriefcasePackageRequirementRequestUtil
{

    public static GetBriefcasePackageRequirementRequest resetRequest(String packageCode, Long briefcaseId)
    {
        GetBriefcasePackageRequirementRequest request = new GetBriefcasePackageRequirementRequest();
        request.setBriefcaseId(briefcaseId);
        request.setPackageCode(packageCode);
        return request;
    }

}
