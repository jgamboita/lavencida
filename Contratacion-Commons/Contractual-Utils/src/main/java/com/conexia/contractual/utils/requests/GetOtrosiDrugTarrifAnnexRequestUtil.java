package com.conexia.contractual.utils.requests;

import com.conexia.contratacion.commons.api.contratacionws.request.drugtarrifannex.GetOtrosiDrugTarrifAnnexRequest;
import com.conexia.contratacion.commons.dto.FiltroAnexoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;

/**
 * Class GetOtrosiDrugTarrifAnnexRequestUtil
 * @author aquintero
 */
public class GetOtrosiDrugTarrifAnnexRequestUtil
{

    public static GetOtrosiDrugTarrifAnnexRequest resetRequest(FiltroAnexoTarifarioDto filter,
                                                               NegociacionDto negotiationDto)
    {
        GetOtrosiDrugTarrifAnnexRequest request = new GetOtrosiDrugTarrifAnnexRequest();
        request.setFilter(filter);
        request.setNegotiationDto(negotiationDto);
        return request;
    }

}
