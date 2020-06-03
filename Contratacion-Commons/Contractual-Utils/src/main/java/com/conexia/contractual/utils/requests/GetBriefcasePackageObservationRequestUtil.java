package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.packageobservation.GetBriefcasePackageObservationRequest;

/**
 * Class GetBriefcasePackageObservationRequestUtil
 * @author aquintero
 */
public class GetBriefcasePackageObservationRequestUtil
{

    public static GetBriefcasePackageObservationRequest resetRequest(String packageCode, Long briefcaseId)
    {
        GetBriefcasePackageObservationRequest request =  new GetBriefcasePackageObservationRequest();
        request.setBriefcaseId(briefcaseId);
        request.setPackageCode(packageCode);
        return request;
    }

}
