package com.conexia.definitions.factory;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.conexia.contratacion.portafolio.definitions.commons.CommonViewServiceRemote;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.MedicamentoPortafolioTransactionalRemote;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.PrestadorTransactionalServiceRemote;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.ProcedimientoServicioPortafolioTransactionalRemote;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.SedePrestadorTransactionalRemote;
import com.conexia.contratacion.portafolio.definitions.transactional.prestador.ServicioHabilitacionPortafolioTransactionalRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.MedicamentoPortafolioViewRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.PrestadorViewServiceRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.ProcedimientoServicioPortafolioViewRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.SedePrestadorViewRemote;
import com.conexia.contratacion.portafolio.definitions.view.prestador.ServicioHabilitacionPortafolioViewRemote;
import com.conexia.definitions.view.BeanViewRemote;
import com.conexia.servicefactory.CnxService;
import com.conexia.servicefactory.ServicesProvider;
import com.conexia.servicefactory.dto.ServiceDescriptorDTO;

/**
 * Created by pbastidas on 8/04/15.
 */
public class ServicesFactory {

    // TODO: Services provider
    
    @Inject
    private ServicesProvider servicesProvider;
    

    private ServiceDescriptorDTO buildServicesDescriptorDTO(ServiciosEnum serviciosEnum) {
        return new ServiceDescriptorDTO(
            serviciosEnum.getLocalBusinessName(),
            serviciosEnum.getRemoteClazz(),
            serviciosEnum.getBeanName(),
            serviciosEnum.getAppName(),
            ServiciosEnum.MODULE_NAME
        );
    }

    // TODO: Generar producers para los servicios

    @Produces
    @CnxService
    public BeanViewRemote produceBean(){
        return servicesProvider.doLookup(BeanViewRemote.class, buildServicesDescriptorDTO(ServiciosEnum.VIEW_BEAN));
    }
    
    @Produces
    @CnxService
    public CommonViewServiceRemote getCommonViewServiceRemote() {
        return servicesProvider.doLookup(CommonViewServiceRemote.class, 
                buildServicesDescriptorDTO(ServiciosEnum.VIEW_COMMON_SERVICE));
    }
    
    @Produces
    @CnxService
    public PrestadorViewServiceRemote getPrestadorViewServiceRemote() {
        return servicesProvider.doLookup(PrestadorViewServiceRemote.class, 
                buildServicesDescriptorDTO(ServiciosEnum.VIEW_PRESTADOR_SERVICE));
    }
    
    @Produces
    @CnxService
    public SedePrestadorViewRemote getSedePrestadorViewRemote() {
        return servicesProvider.doLookup(SedePrestadorViewRemote.class, 
                buildServicesDescriptorDTO(ServiciosEnum.VIEW_SEDE_PRESTADOR_SERVICE));
    }
    
    @Produces
    @CnxService
    public ServicioHabilitacionPortafolioViewRemote getServicioHabilitacionPortafolioViewRemote() {
        return servicesProvider.doLookup(ServicioHabilitacionPortafolioViewRemote.class, 
                buildServicesDescriptorDTO(ServiciosEnum.VIEW_SERVICIO_HABILITACION_PORTAFOLIO_SERVICE));
    } 
    
    @Produces
    @CnxService
    public ProcedimientoServicioPortafolioViewRemote getProcedimientoServicioPortafolioViewRemote() {
        return servicesProvider.doLookup(ProcedimientoServicioPortafolioViewRemote.class, 
                buildServicesDescriptorDTO(ServiciosEnum.VIEW_PROCEDIMIENTO_SERVICIO_PORTAFOLIO_SERVICE));
    } 
    
    @Produces
    @CnxService
    public MedicamentoPortafolioViewRemote getMedicamentoPortafolioViewRemote() {
        return servicesProvider.doLookup(MedicamentoPortafolioViewRemote.class, 
                buildServicesDescriptorDTO(ServiciosEnum.VIEW_MEDICAMENTO_PORTAFOLIO_SERVICE));
    }
    
    @Produces
    @CnxService
    public PrestadorTransactionalServiceRemote getPrestadorTransactionalServiceRemote() {
        return servicesProvider.doLookup(PrestadorTransactionalServiceRemote.class, 
                buildServicesDescriptorDTO(ServiciosEnum.TRANSACCIONAL_PRESTADOR_SERVICE));
    }  
    
    @Produces
    @CnxService
    public SedePrestadorTransactionalRemote getSedePrestadorTransactionalRemote() {
        return servicesProvider.doLookup(SedePrestadorTransactionalRemote.class, 
                buildServicesDescriptorDTO(ServiciosEnum.TRANSACCIONAL_SEDE_PRESTADOR_SERVICE));
    } 
    
    @Produces
    @CnxService
    public ServicioHabilitacionPortafolioTransactionalRemote getServicioHabilitacionPortafolioTransactionalRemote() {
        return servicesProvider.doLookup(ServicioHabilitacionPortafolioTransactionalRemote.class, 
                buildServicesDescriptorDTO(ServiciosEnum.TRANSACCIONAL_SERVICIO_HABILITACION_PORTAFOLIO_SERVICE));
    } 
    
    @Produces
    @CnxService
    public ProcedimientoServicioPortafolioTransactionalRemote getProcedimientoServicioPortafolioTransactionalRemote() {
        return servicesProvider.doLookup(ProcedimientoServicioPortafolioTransactionalRemote.class, 
                buildServicesDescriptorDTO(ServiciosEnum.TRANSACCIONAL_PROCEDIMIENTO_SERVICIO_PORTAFOLIO_SERVICE));
    } 
       
    @Produces
    @CnxService
    public MedicamentoPortafolioTransactionalRemote getMedicamentoPortafolioTransactionalRemote() {
        return servicesProvider.doLookup(MedicamentoPortafolioTransactionalRemote.class, 
                buildServicesDescriptorDTO(ServiciosEnum.TRANSACCIONAL_MEDICAMENTO_PORTAFOLIO_SERVICE));
    } 
}

