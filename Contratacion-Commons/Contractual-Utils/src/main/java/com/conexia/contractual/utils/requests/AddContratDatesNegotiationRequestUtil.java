package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.contract.AddContratDatesNegotiationRequest;

import java.util.Date;

/**
 * Class AddContratDatesNegotiationRequestUtil
 * @author aquintero
 */
public class AddContratDatesNegotiationRequestUtil
{

    public static AddContratDatesNegotiationRequest resetRequest(Date startDate,
                                                                 Date endDate,
                                                                 Long negotiationId)
    {
        AddContratDatesNegotiationRequest request = new AddContratDatesNegotiationRequest();
        request.setStartDate(startDate);
        request.setEndDate(endDate);
        request.setNegotiationId(negotiationId);
        return request;
    }

}
