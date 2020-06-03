package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.packageexclusion.GetBriefcasePackageExclusionRequest;

/**
 * Class GetBriefcasePackageExclusionRequestUtil
 * @author aquintero
 */
public class GetBriefcasePackageExclusionRequestUtil
{

    public static GetBriefcasePackageExclusionRequest resetRequest(String packageCode, Long briefcaseId)
    {
        GetBriefcasePackageExclusionRequest request =  new GetBriefcasePackageExclusionRequest();
        request.setBriefcaseId(briefcaseId);
        request.setPackageCode(packageCode);
        return request;
    }

}
