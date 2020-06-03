package com.conexia.contractual.definitions.factory;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

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
import com.conexia.servicefactory.CnxService;
import com.conexia.servicefactory.ServicesProvider;
import com.conexia.servicefactory.dto.ServiceDescriptorDTO;

/**
 * Created by pbastidas on 8/04/15.
 */
public class ServicesFactory {

    /**
     * Provider de servicios.
     */
    @Inject
    private ServicesProvider servicesProvider;

    private ServiceDescriptorDTO buildServicesDescriptorDTO(
        ServiciosEnum auditoriaServiciosEnum) {
        return new ServiceDescriptorDTO(
                        auditoriaServiciosEnum.getLocalBusinessName(),
                        auditoriaServiciosEnum.getRemoteClazz(),
                        auditoriaServiciosEnum.getBeanName(),
                        auditoriaServiciosEnum.getAppName(),
            ServiciosEnum.MODULE_NAME);
    }



    @Produces
    @CnxService
    public ParametrosViewRemote getParametrosViewService() {
        return servicesProvider
            .doLookup(ParametrosViewRemote.class,
                buildServicesDescriptorDTO(ServiciosEnum.VIEW_PARAMETROS_SERVICE));
    }

    @Produces
    @CnxService
    public ConsultaSolicitudesContratacionViewRemote getConsultaSolicitudesContratacionViewService() {
        return servicesProvider.doLookup(ConsultaSolicitudesContratacionViewRemote.class, buildServicesDescriptorDTO(ServiciosEnum.VIEW_CONSULTA_SOLICITUDES_CONTRATACION_SERVICE));
    }

    @Produces
    @CnxService
    public ParametrizacionContratoViewRemote getParametrizacionContratoViewService() {
        return servicesProvider.doLookup(ParametrizacionContratoViewRemote.class, buildServicesDescriptorDTO(ServiciosEnum.VIEW_PARAMETRIZACION_CONTRATO_SERVICE));
    }

    @Produces
    @CnxService
    public ParametrizacionContratoTransactionalRemote getParametrizacionContratoTransactionalService() {
        return servicesProvider.doLookup(ParametrizacionContratoTransactionalRemote.class, buildServicesDescriptorDTO(ServiciosEnum.TRANSACCIONAL_PARAMETRIZACION_CONTRATO_SERVICE));
    }

    @Produces
    @CnxService
    public PrestadorTransactionalRemote getPrestadorTransactionalService() {
        return servicesProvider.doLookup(PrestadorTransactionalRemote.class, buildServicesDescriptorDTO(ServiciosEnum.TRANSACCIONAL_PRESTADOR_SERVICE));
    }

    @Produces
    @CnxService
    public PrestadorViewServiceRemote getPrestadorViewService() {
        return servicesProvider.doLookup(PrestadorViewServiceRemote.class, buildServicesDescriptorDTO(ServiciosEnum.VIEW_PRESTADOR_SERVICE));
    }

    @Produces
    @CnxService
    public ParametrizacionMinutaViewRemote getParametrizacionMinutaViewService() {
        return servicesProvider.doLookup(ParametrizacionMinutaViewRemote.class, buildServicesDescriptorDTO(ServiciosEnum.VIEW_PARAMETRIZACION_MINUTA_SERVICE));
    }

    @Produces
    @CnxService
    public ParametrizacionMinutaTransaccionalRemote getParametrizacionMinutaTransactionalService() {
        return servicesProvider.doLookup(ParametrizacionMinutaTransaccionalRemote.class,
                buildServicesDescriptorDTO(ServiciosEnum.TRANSACCIONAL_PARAMETRIZACION_MINUTA_SERVICE));
    }

    @Produces
    @CnxService
    public LegalizacionContratoTransaccionalRemote getLegalizacionContratoTransactionalService() {
        return servicesProvider.doLookup(LegalizacionContratoTransaccionalRemote.class,
                buildServicesDescriptorDTO(ServiciosEnum.TRANSACCIONAL_LEGALIZACION_CONTRATO_SERVICE));
    }

    @Produces
    @CnxService
    public LegalizacionContratoViewRemote getLegalizacionContratoViewService() {
        return servicesProvider.doLookup(LegalizacionContratoViewRemote.class,
                buildServicesDescriptorDTO(ServiciosEnum.VIEW_LEGALIZACION_CONTRATO_SERVICE));
    }

    @Produces
    @CnxService
    public MatrizContratacionViewRemote getMatrizContratacionViewRemote() {
        return servicesProvider.doLookup(MatrizContratacionViewRemote.class,
                buildServicesDescriptorDTO(ServiciosEnum.VIEW_MATRIZ_CONTRATACION_SERVICE));
    }


    @Produces
    @CnxService
    public BandejaContratoUrgenciasViewServiceRemote getBandejaContratoUrgenciasViewServiceRemote() {
        return servicesProvider
            .doLookup(
            		BandejaContratoUrgenciasViewServiceRemote.class,
                buildServicesDescriptorDTO(ServiciosEnum.VIEW_BANDEJA_CONTRATO_URGENCIAS_SERVICE));
    }


    @Produces
    @CnxService
    public GenerarContratoUrgenciasViewServiceRemote getGenerarContratoUrgenciasViewServiceRemote() {
        return servicesProvider
            .doLookup(
            		GenerarContratoUrgenciasViewServiceRemote.class,
                buildServicesDescriptorDTO(ServiciosEnum.VIEW_GENERAR_CONTRATO_URGENCIAS_SERVICE));
    }

    @Produces
    @CnxService
    public ContratoUrgenciasViewServiceRemote getContratoUrgenciasViewServiceRemote() {
        return servicesProvider
            .doLookup(
            		ContratoUrgenciasViewServiceRemote.class,
                buildServicesDescriptorDTO(ServiciosEnum.VIEW_CONTRATO_URGENCIAS_SERVICE));
    }

    @Produces
    @CnxService
    public ContratoUrgenciasVoBoViewServiceRemote getContratoUrgenciasVoBoViewServiceRemote() {
        return servicesProvider
            .doLookup(
            		ContratoUrgenciasVoBoViewServiceRemote.class,
                buildServicesDescriptorDTO(ServiciosEnum.VIEW_CONTRATO_URGENCIAS_VOBO_SERVICE));
    }

    @Produces
    @CnxService
    public ContratoUrgenciasVoBoTransaccionalRemote getContratoUrgenciasVoBoTransaccionalRemote() {
        return servicesProvider
            .doLookup(
            		ContratoUrgenciasVoBoTransaccionalRemote.class,
                buildServicesDescriptorDTO(ServiciosEnum.TRANSACCIONAL_CONTRATO_URGENCIAS_VOBO_SERVICE));
    }



}
