package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.technologyextensiondate.UpdateTechnologyExtensionDateRequest;

import java.util.Date;

/**
 * Class UpdateTechnologyExtensionDateRequestUtil
 * @author aquintero
 */
public class UpdateTechnologyExtensionDateRequestUtil
{

    public static UpdateTechnologyExtensionDateRequest resetRequest(Long negotiationId,
                                                                    Date startDate,
                                                                    Date endDate)
    {
        UpdateTechnologyExtensionDateRequest request = new UpdateTechnologyExtensionDateRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setNegotiationId(negotiationId);
        return request;
    }

}
