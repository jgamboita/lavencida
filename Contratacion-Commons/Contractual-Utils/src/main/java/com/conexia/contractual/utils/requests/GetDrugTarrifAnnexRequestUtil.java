package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.drugtarrifannex.GetDrugTarrifAnnexRequest;
import com.conexia.contratacion.commons.dto.FiltroAnexoTarifarioDto;

/**
 * Class GetDrugTarrifAnnexRequestUtil
 * @author aquintero
 */
public class GetDrugTarrifAnnexRequestUtil
{

    public static GetDrugTarrifAnnexRequest resetRequest(FiltroAnexoTarifarioDto filter,
                                                    Boolean isOtroSi,
                                                    Long parentNegotiationId)
    {
        GetDrugTarrifAnnexRequest request =  new GetDrugTarrifAnnexRequest();
        request.setFilter(filter);
        request.setOtroSi(isOtroSi);
        request.setParentNegotiationId(parentNegotiationId);
        return request;
    }

}
