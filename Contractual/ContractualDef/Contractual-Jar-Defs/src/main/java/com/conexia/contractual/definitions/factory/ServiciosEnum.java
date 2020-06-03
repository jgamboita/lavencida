package com.conexia.contractual.definitions.factory;

import com.conexia.contractual.definitions.transactional.contratourgencias.ContratoUrgenciasVoBoTransaccionalRemote;
import com.conexia.contractual.definitions.transactional.legalizacion.LegalizacionContratoTransaccionalRemote;
import com.conexia.contractual.definitions.transactional.legalizacion.ParametrizacionMinutaTransaccionalRemote;
import com.conexia.contractual.definitions.transactional.parametrizacion.ParametrizacionContratoTransactionalRemote;
import com.conexia.contractual.definitions.transactional.prestador.PrestadorTransactionalRemote;
import com.conexia.contractual.definitions.view.contratourgencias.BandejaContratoUrgenciasViewServiceRemote;
import com.conexia.contractual.definitions.view.contratourgencias.ContratoUrgenciasViewServiceRemote;
import com.conexia.contractual.definitions.view.contratourgencias.GenerarContratoUrgenciasViewServiceRemote;
import com.conexia.contractual.definitions.view.legalizacion.ContratoUrgenciasVoBoViewServiceRemote;
import com.conexia.contractual.definitions.view.legalizacion.LegalizacionContratoViewRemote;
import com.conexia.contractual.definitions.view.legalizacion.ParametrizacionMinutaViewRemote;
import com.conexia.contractual.definitions.view.matriz.MatrizContratacionViewRemote;
import com.conexia.contractual.definitions.view.parametrizacion.ParametrizacionContratoViewRemote;
import com.conexia.contractual.definitions.view.parametros.ParametrosViewRemote;
import com.conexia.contractual.definitions.view.prestador.PrestadorViewServiceRemote;
import com.conexia.contractual.definitions.view.solicitud.ConsultaSolicitudesContratacionViewRemote;

/**
 * Created by pbastidas on 8/04/15.
 */
public enum ServiciosEnum {

    VIEW_PARAMETROS_SERVICE(ParametrosViewRemote.class, "ParametrosViewBoundary"),

    VIEW_CONSULTA_SOLICITUDES_CONTRATACION_SERVICE(ConsultaSolicitudesContratacionViewRemote.class,
            "ConsultaSolicitudesContratacionViewBoundary"),

    VIEW_PARAMETRIZACION_CONTRATO_SERVICE(ParametrizacionContratoViewRemote.class,
            "ParametrizacionContratoViewBoundary"),

    TRANSACCIONAL_PARAMETRIZACION_CONTRATO_SERVICE(ParametrizacionContratoTransactionalRemote.class,
            "ParametrizacionContratoTransactionalBoundary"),

    TRANSACCIONAL_PRESTADOR_SERVICE(PrestadorTransactionalRemote.class,
            "PrestadorTransactionalBoundary"),

    VIEW_PRESTADOR_SERVICE(PrestadorViewServiceRemote.class,
            "PrestadorViewBoundary"),

    VIEW_PARAMETRIZACION_MINUTA_SERVICE(ParametrizacionMinutaViewRemote.class,
            "ParametrizacionMinutaViewBoundary"),

    TRANSACCIONAL_PARAMETRIZACION_MINUTA_SERVICE(ParametrizacionMinutaTransaccionalRemote.class,
            "ParametrizacionMinutaTransaccionalBoundary"),

    TRANSACCIONAL_LEGALIZACION_CONTRATO_SERVICE(LegalizacionContratoTransaccionalRemote.class,
            "LegalizacionContratoTransaccionalBoundary"),

    VIEW_LEGALIZACION_CONTRATO_SERVICE(LegalizacionContratoViewRemote.class,
            "LegalizacionContratoViewBoundary"),

    VIEW_MATRIZ_CONTRATACION_SERVICE(MatrizContratacionViewRemote.class,
            "MatrizContratacionViewBoundary"),

    VIEW_BANDEJA_CONTRATO_URGENCIAS_SERVICE(BandejaContratoUrgenciasViewServiceRemote.class, "BandejaContratoUrgenciasViewBoundary"),

    VIEW_GENERAR_CONTRATO_URGENCIAS_SERVICE(GenerarContratoUrgenciasViewServiceRemote.class, "GenerarContratoUrgenciasViewBoundary"),

	VIEW_CONTRATO_URGENCIAS_SERVICE(ContratoUrgenciasViewServiceRemote.class, "ContratoUrgenciasViewBoundary"),

	VIEW_CONTRATO_URGENCIAS_VOBO_SERVICE(ContratoUrgenciasVoBoViewServiceRemote.class, "ContratoUrgenciasVoBoViewBoundary"),

	TRANSACCIONAL_CONTRATO_URGENCIAS_VOBO_SERVICE(ContratoUrgenciasVoBoTransaccionalRemote.class, "ContratoUrgenciasVoBoTransaccionalBoundary");



    public static final String MODULE_NAME = "contractual-ejb";
    // Estructura del enum
    private final String localBusinessName;
    private final Class remoteClazz;
    private final String beanName;

    ServiciosEnum(Class remoteClazz, String beanName) {
        this.localBusinessName = "";
        this.remoteClazz = remoteClazz;
        this.beanName = beanName;
    }

    public String getLocalBusinessName() {
        return localBusinessName;
    }

    public Class getRemoteClazz() {
        return remoteClazz;
    }

    public String getBeanName() {
        return beanName;
    }

    public String getAppName() {
        return "contractual";
    }
}
