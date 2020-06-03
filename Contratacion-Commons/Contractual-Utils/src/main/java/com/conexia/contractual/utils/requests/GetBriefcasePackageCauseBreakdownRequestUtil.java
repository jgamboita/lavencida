package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.packagecausebreakdown.GetBriefcasePackageCauseBreakdownRequest;

/**
 * Class GetBriefcasePackageCauseBreakdownRequestUtil
 * @author aquintero
 */
public class GetBriefcasePackageCauseBreakdownRequestUtil
{

    public static GetBriefcasePackageCauseBreakdownRequest resetRequest(String packageCode, Long briefcaseId)
    {
        GetBriefcasePackageCauseBreakdownRequest request = new GetBriefcasePackageCauseBreakdownRequest();
        request.setBriefcaseId(briefcaseId);
        request.setPackageCode(packageCode);
        return request;
    }

}
