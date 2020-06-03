package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.contract.AddOtrosiContratDatesRequest;
import java.util.Date;

/**
 * Class AddOtrosiContratDatesRequestUtil
 * @author aquintero
 */
public class AddOtrosiContratDatesRequestUtil
{

    public static AddOtrosiContratDatesRequest resetRequest(Date startDateOtrosi,
                                                            Date endDateOtroSi,
                                                            Long negotiationId)
    {
        AddOtrosiContratDatesRequest request = new AddOtrosiContratDatesRequest();
        request.setStartDateOtrosi(startDateOtrosi);
        request.setEndDateOtroSi(endDateOtroSi);
        request.setNegotiationId(negotiationId);
        return request;
    }

}
