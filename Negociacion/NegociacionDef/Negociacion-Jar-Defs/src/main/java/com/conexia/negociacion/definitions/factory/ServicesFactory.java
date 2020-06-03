package com.conexia.negociacion.definitions.factory;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import com.conexia.negociacion.definitions.bandeja.comite.BandejaComiteTransactionalServiceRemote;
import com.conexia.negociacion.definitions.bandeja.comite.BandejaComiteViewServiceRemote;
import com.conexia.negociacion.definitions.bandeja.comparacion.BandejaComparacionViewServiceRemote;
import com.conexia.negociacion.definitions.bandeja.prestador.BandejaPrestadorViewServiceRemote;
import com.conexia.negociacion.definitions.bandeja.referentePgp.BandejaReferentePgpTransactionalServiceRemote;
import com.conexia.negociacion.definitions.bandeja.referentePgp.BandejaReferentePgpViewServiceRemote;
import com.conexia.negociacion.definitions.bandeja.rias.BandejaConsultaRiasViewServiceRemote;
import com.conexia.negociacion.definitions.capita.ReferenteCapitaTransactionalServiceRemote;
import com.conexia.negociacion.definitions.capita.ReferenteCapitaViewServiceRemote;
import com.conexia.negociacion.definitions.comite.ActaComiteTransactionalServiceRemote;
import com.conexia.negociacion.definitions.comite.ActaComiteViewServiceRemote;
import com.conexia.negociacion.definitions.comite.GestionComiteTransactionalServiceRemote;
import com.conexia.negociacion.definitions.comite.GestionComiteViewServiceRemote;
import com.conexia.negociacion.definitions.common.CommonTransactionalServiceRemote;
import com.conexia.negociacion.definitions.common.CommonViewServiceRemote;
import com.conexia.negociacion.definitions.comparacion.negociacion.ComparacionNegociacionServiceRemote;
import com.conexia.negociacion.definitions.comparacion.tarifas.ComparacionTarifasServiceRemote;
import com.conexia.negociacion.definitions.negociacion.GenerarReporteJxlsViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.GestionNegociacionViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.NegociacionTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.NegociacionViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.ReporteNegociacionJxlsViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.medicamento.NegociacionMedicamentoTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.medicamento.NegociacionMedicamentoViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.medicamentoPgp.NegociacionMedicamentoPgpTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.paquete.NegociacionPaqueteTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.paquete.NegociacionPaqueteViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.paquete.detalle.NegociacionPaqueteDetalleTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.paquete.detalle.NegociacionPaqueteDetalleViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.procedimiento.NegociacionProcedimientoViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.procedimientoPgp.NegociacionProcedimientoPgpTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.sedes.AreaCoberturaTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.sedes.AreaCoberturaViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.servicio.NegociacionServicioTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.servicio.NegociacionServicioViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.tecnologia.NegociacionTecnologiasViewServiceRemote;
import com.conexia.negociacion.definitions.portafolio.DetallePortafolioViewServiceRemote;
import com.conexia.negociacion.definitions.referentePgp.GestionReferentePgpTransactionalServiceRemote;
import com.conexia.negociacion.definitions.referentePgp.GestionReferentePgpViewServiceRemote;
import com.conexia.negociacion.definitions.tecnologias.ImportarTecnologiasTransactionalServiceRemote;
import com.conexia.servicefactory.CnxService;
import com.conexia.servicefactory.ServicesProvider;
import com.conexia.servicefactory.dto.ServiceDescriptorDTO;
import com.conexia.negociacion.definitions.negociacion.NegociacionReferenteTransactionalServiceRemote;

/**
 * Created by pbastidas on 8/04/15.
 */
public class ServicesFactory {

    @Inject
    private ServicesProvider servicesProvider;

    private ServiceDescriptorDTO buildServicesDescriptorDTO(ServiciosEnum auditoriaServiciosEnum) {
        return new ServiceDescriptorDTO(
                auditoriaServiciosEnum.getLocalBusinessName(),
                auditoriaServiciosEnum.getRemoteClazz(),
                auditoriaServiciosEnum.getBeanName(),
                auditoriaServiciosEnum.getAppName(),
                ServiciosEnum.MODULE_NAME
        );
    }

    @Produces
    @CnxService
    public BandejaPrestadorViewServiceRemote getBandejaPrestadorViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        BandejaPrestadorViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_BANDEJA_PRESTADOR_SERVICE));
    }

    @Produces
    @CnxService
    public GestionNegociacionViewServiceRemote getGestionNegociacionViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        GestionNegociacionViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_GESTION_NEGOCIACION_SERVICE));
    }

    @Produces
    @CnxService
    public AreaCoberturaViewServiceRemote getAreaCoberturaViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        AreaCoberturaViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_AREA_COBERTURA));
    }

    @Produces
    @CnxService
    public NegociacionViewServiceRemote getNegociacionViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        NegociacionViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_NEGOCIACION_SERVICE));
    }

    @Produces
    @CnxService
    public ReporteNegociacionJxlsViewServiceRemote getReporteNegociacionJxlsViewServiceRemote() {
        return servicesProvider.
                doLookup(ReporteNegociacionJxlsViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_REPORTE_NEGOCIACION_SERVICE));
    }

    @Produces
    @CnxService
    public GenerarReporteJxlsViewServiceRemote getGenerarReporteJxlsViewServiceRemote() {
        return servicesProvider.
                doLookup(GenerarReporteJxlsViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_GENERAR_REPORTE_NEGOCIACION_SERVICE));
    }


    @Produces
    @CnxService
    public NegociacionTransactionalServiceRemote getNegociacionTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        NegociacionTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_NEGOCIACION_SERVICE));
    }

    @Produces
    @CnxService
    public AreaCoberturaTransactionalServiceRemote getAreaCoberturaTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        AreaCoberturaTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_AREA_COBERTURA));
    }

    @Produces
    @CnxService
    public NegociacionMedicamentoViewServiceRemote getNegociacionMedicamentoViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        NegociacionMedicamentoViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_NEGOCIACION_MEDICAMENTO_SERVICE));
    }

    @Produces
    @CnxService
    public CommonViewServiceRemote getCommonViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        CommonViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_COMMON));
    }

    @Produces
    @CnxService
    public CommonTransactionalServiceRemote getCommonTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        CommonTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_COMMON));
    }

    @Produces
    @CnxService
    public NegociacionMedicamentoTransactionalServiceRemote getNegociacionMedicamentoTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        NegociacionMedicamentoTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_MEDICAMENTO_SERVICE));
    }

    @Produces
    @CnxService
    public NegociacionPaqueteDetalleViewServiceRemote getNegociacionPaqueteDetalleViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        NegociacionPaqueteDetalleViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_NEGOCIACION_PAQUETE_DETALLE_SERVICE));
    }

    @Produces
    @CnxService
    public NegociacionPaqueteDetalleTransactionalServiceRemote getNegociacionPaqueteDetalleTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        NegociacionPaqueteDetalleTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_NEGOCIACION_PAQUETE_DETALLE_SERVICE));
    }

    @Produces
    @CnxService
    public NegociacionPaqueteViewServiceRemote getNegociacionPaqueteViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        NegociacionPaqueteViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_NEGOCIACION_PAQUETE_SERVICE));
    }

    @Produces
    @CnxService
    public NegociacionPaqueteTransactionalServiceRemote getNegociacionPaqueteTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        NegociacionPaqueteTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_PAQUETE_SERVICE));
    }

    @Produces
    @CnxService
    public NegociacionServicioViewServiceRemote getNegociacionServicioViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        NegociacionServicioViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_NEGOCIACION_SERVICIO_SERVICE));
    }

    @Produces
    @CnxService
    public NegociacionProcedimientoViewServiceRemote getNegociacionProcedimientoViewServiceRemote() {
        return servicesProvider.doLookup(NegociacionProcedimientoViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_NEGOCIACION_PROCEDIMIENTO_SERVICE));
    }

    @Produces
    @CnxService
    public NegociacionTecnologiasViewServiceRemote getNegociacionTecnologiaViewServiceRemote() {
        return servicesProvider.doLookup(NegociacionTecnologiasViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_NEGOCIACION_TECNOLOGIA_SERVICE));
    }

    @Produces
    @CnxService
    public NegociacionServicioTransactionalServiceRemote getNegociacionServicioTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        NegociacionServicioTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_NEGOCIACION_SERVICIO_SERVICE));
    }

    @Produces
    @CnxService
    public ComparacionTarifasServiceRemote getComparacionTarifasServiceRemote() {
        return servicesProvider.doLookup(ComparacionTarifasServiceRemote.class,
                buildServicesDescriptorDTO(ServiciosEnum.VIEW_COMPARACION_TARIFAS_SERVICE));
    }

    @Produces
    @CnxService
    public ComparacionNegociacionServiceRemote getComparacionNegociacionServiceRemote() {
        return servicesProvider.doLookup(ComparacionNegociacionServiceRemote.class,
                buildServicesDescriptorDTO(ServiciosEnum.VIEW_COMPARACION_NEGOCIACION_SERVICE));
    }

    @Produces
    @CnxService
    public BandejaComparacionViewServiceRemote getBandejaComparacionViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        BandejaComparacionViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_BANDEJA_COMPARACION_SERVICE));
    }

    @Produces
    @CnxService
    public DetallePortafolioViewServiceRemote getDetallePortafolioViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        DetallePortafolioViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_DETALLE_PORTAFOLIO_SERVICE));
    }

    @Produces
    @CnxService
    public BandejaComiteViewServiceRemote getBandejaComiteViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        BandejaComiteViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_BANDEJA_COMITE_SERVICE));
    }

    @Produces
    @CnxService
    public BandejaComiteTransactionalServiceRemote getBandejaComiteTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        BandejaComiteTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_BANDEJA_COMITE_SERVICE));
    }

    @Produces
    @CnxService
    public GestionComiteViewServiceRemote getGestionComiteViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        GestionComiteViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_GESTION_COMITE_SERVICE));
    }

    @Produces
    @CnxService
    public GestionComiteTransactionalServiceRemote getGestionComiteTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        GestionComiteTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_GESTION_COMITE_SERVICE));
    }

    @Produces
    @CnxService
    public ActaComiteViewServiceRemote getActaComiteViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        ActaComiteViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_ACTA_COMITE_SERVICE));
    }

    @Produces
    @CnxService
    public ActaComiteTransactionalServiceRemote getActaComiteTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        ActaComiteTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_ACTA_COMITE_SERVICE));
    }

    @Produces
    @CnxService
    public ReferenteCapitaViewServiceRemote getReferenteCapitaViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        ReferenteCapitaViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_REFERENTE_CAPITA_SERVICE));
    }

    @Produces
    @CnxService
    public ReferenteCapitaTransactionalServiceRemote getReferenteCapitaTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        ReferenteCapitaTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_REFERENTE_CAPITA_SERVICE));
    }

    @Produces
    @CnxService
    public BandejaConsultaRiasViewServiceRemote getBandejaConsultaRiasViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        BandejaConsultaRiasViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_BANDEJA_RIAS));
    }

    @Produces
    @CnxService
    public BandejaReferentePgpViewServiceRemote getBandejaReferentePgpViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        BandejaReferentePgpViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_BANDEJA_REFERETE_PGP));
    }

    @Produces
    @CnxService
    public GestionReferentePgpViewServiceRemote getGestionReferentePgpViewServiceRemote() {
        return servicesProvider
                .doLookup(
                        GestionReferentePgpViewServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.VIEW_GESTION_REFERENTE_PGP));
    }

    @Produces
    @CnxService
    public BandejaReferentePgpTransactionalServiceRemote getBandejaReferentePgpTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        BandejaReferentePgpTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_BANDEJA_REFERENTE_PGP));
    }

    @Produces
    @CnxService
    public GestionReferentePgpTransactionalServiceRemote getGestionReferentePgpTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        GestionReferentePgpTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_GESTION_REFERENTE_PGP));
    }

    @Produces
    @CnxService
    public NegociacionProcedimientoPgpTransactionalServiceRemote getNegociacionProcedimientoPgpTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        NegociacionProcedimientoPgpTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_IMPORTAR_PROCEDIMIENTO_PGP));
    }

    @Produces
    @CnxService
    public NegociacionMedicamentoPgpTransactionalServiceRemote getNegociacionMedicamentoPgpTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        NegociacionMedicamentoPgpTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_IMPORTAR_MEDICAMENTO_PGP));
    }

    @Produces
    @CnxService
    public ImportarTecnologiasTransactionalServiceRemote getImportarTecnologiasTransactionalServiceRemote() {
        return servicesProvider
                .doLookup(
                        ImportarTecnologiasTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_IMPORTAR_TECNOLOGIA));
    }
    
    @Produces
    @CnxService
    public NegociacionReferenteTransactionalServiceRemote getNegociacionReferenteViewServiceRemote() {
        return servicesProvider
                .doLookup(NegociacionReferenteTransactionalServiceRemote.class,
                        buildServicesDescriptorDTO(ServiciosEnum.TRANSACTIONAL_NEGOCIACION_REFERENTE));
    }

}
