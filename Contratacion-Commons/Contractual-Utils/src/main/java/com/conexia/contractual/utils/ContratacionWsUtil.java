package com.conexia.contractual.utils;

import javax.inject.Inject;
import java.util.List;

/**
 * Class ContratacionWsUtil
 * @author aquintero
 */
public class ContratacionWsUtil
{

    public static final String PROPERTY_KEY_URL_WS_CONTRATACION = "url_contratacion_ws";
    public static final String PROPERTY_APPLICATION_CONTRATACION = "contratacion";

    @Inject
    private PropertiesUtil propertiesUtil;

    public String getUrlContratacionWs()
    {
        List<String> listResulted = propertiesUtil.getValueProperty(PROPERTY_KEY_URL_WS_CONTRATACION,
                                                PROPERTY_APPLICATION_CONTRATACION);
        return listResulted.get(0);
    }


}
