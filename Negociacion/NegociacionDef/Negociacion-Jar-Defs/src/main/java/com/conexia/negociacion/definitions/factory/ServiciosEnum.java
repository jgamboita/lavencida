package com.conexia.negociacion.definitions.factory;

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
import com.conexia.negociacion.definitions.negociacion.NegociacionReferenteTransactionalServiceRemote;
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

/**
 * Created by pbastidas on 8/04/15.
 */
public enum ServiciosEnum {

    VIEW_BANDEJA_PRESTADOR_SERVICE(BandejaPrestadorViewServiceRemote.class, "BandejaPrestadorViewBoundary"),
    VIEW_GESTION_NEGOCIACION_SERVICE(GestionNegociacionViewServiceRemote.class, "GestionNegociacionViewBoundary"),
    VIEW_NEGOCIACION_SERVICE(NegociacionViewServiceRemote.class, "NegociacionViewBoundary"),
    VIEW_REPORTE_NEGOCIACION_SERVICE(ReporteNegociacionJxlsViewServiceRemote.class, "ReporteNegociacionJxlsBoundary"),
    VIEW_GENERAR_REPORTE_NEGOCIACION_SERVICE(GenerarReporteJxlsViewServiceRemote.class, "GenerarReporteJxlsBoundary"),
    VIEW_AREA_COBERTURA(AreaCoberturaViewServiceRemote.class, "AreaCoberturaViewBoundary"),
    VIEW_COMMON(CommonViewServiceRemote.class, "CommonViewBoundary"),
    VIEW_NEGOCIACION_PAQUETE_SERVICE(NegociacionPaqueteViewServiceRemote.class, "NegociacionPaqueteViewBoundary"),
    VIEW_NEGOCIACION_PAQUETE_DETALLE_SERVICE(NegociacionPaqueteDetalleViewServiceRemote.class, "NegociacionPaqueteDetalleViewBoundary"),
    VIEW_NEGOCIACION_MEDICAMENTO_SERVICE(NegociacionMedicamentoViewServiceRemote.class, "NegociacionMedicamentoViewBoundary"),
    VIEW_NEGOCIACION_SERVICIO_SERVICE(NegociacionServicioViewServiceRemote.class, "NegociacionServicioViewBoundary"),
    VIEW_NEGOCIACION_PROCEDIMIENTO_SERVICE(NegociacionProcedimientoViewServiceRemote.class, "NegociacionProcedimientoViewBoundary"),
    VIEW_NEGOCIACION_TECNOLOGIA_SERVICE(NegociacionTecnologiasViewServiceRemote.class, "NegociacionTecnologiasViewBoundary"),
    VIEW_COMPARACION_TARIFAS_SERVICE(ComparacionTarifasServiceRemote.class, "ComparacionTarifasViewBoundary"),
    VIEW_COMPARACION_NEGOCIACION_SERVICE(ComparacionNegociacionServiceRemote.class, "ComparacionNegociacionViewBoundary"),
    VIEW_BANDEJA_COMPARACION_SERVICE(BandejaComparacionViewServiceRemote.class, "BandejaComparacionViewBoundary"),
    VIEW_DETALLE_PORTAFOLIO_SERVICE(DetallePortafolioViewServiceRemote.class, "DetallePortafolioViewBoundary"),
    VIEW_BANDEJA_COMITE_SERVICE(BandejaComiteViewServiceRemote.class, "BandejaComiteViewBoundary"),
    VIEW_GESTION_COMITE_SERVICE(GestionComiteViewServiceRemote.class, "GestionComiteViewBoundary"),
    VIEW_ACTA_COMITE_SERVICE(ActaComiteViewServiceRemote.class, "ActaComiteViewBoundary"),
    VIEW_REFERENTE_CAPITA_SERVICE(ReferenteCapitaViewServiceRemote.class, "ReferenteCapitaViewBoundary"),
    VIEW_BANDEJA_RIAS(BandejaConsultaRiasViewServiceRemote.class, "BandejaConsultaRiasViewBoundary"),
    VIEW_BANDEJA_REFERETE_PGP(BandejaReferentePgpViewServiceRemote.class, "BandejaReferentePgpViewBoundary"),
    VIEW_GESTION_REFERENTE_PGP(GestionReferentePgpViewServiceRemote.class, "GestionReferentePgpViewBoundary"),
    TRANSACTIONAL_NEGOCIACION_SERVICE(NegociacionTransactionalServiceRemote.class, "NegociacionTransactionalBoundary"),
    TRANSACTIONAL_AREA_COBERTURA(AreaCoberturaTransactionalServiceRemote.class, "AreaCoberturaTransactionalBoundary"),
    TRANSACTIONAL_MEDICAMENTO_SERVICE(NegociacionMedicamentoTransactionalServiceRemote.class, "NegociacionMedicamentoTransactionalBoundary"),
    TRANSACTIONAL_PAQUETE_SERVICE(NegociacionPaqueteTransactionalServiceRemote.class, "NegociacionPaqueteTransactionalBoundary"),
    TRANSACTIONAL_NEGOCIACION_PAQUETE_DETALLE_SERVICE(NegociacionPaqueteDetalleTransactionalServiceRemote.class, "NegociacionPaqueteDetalleTransactionalBoundary"),
    TRANSACTIONAL_NEGOCIACION_SERVICIO_SERVICE(NegociacionServicioTransactionalServiceRemote.class, "NegociacionServicioTransactionalBoundary"),
    TRANSACTIONAL_BANDEJA_COMITE_SERVICE(BandejaComiteTransactionalServiceRemote.class, "BandejaComiteTransactionalBoundary"),
    TRANSACTIONAL_GESTION_COMITE_SERVICE(GestionComiteTransactionalServiceRemote.class, "GestionComiteTransactionalBoundary"),
    TRANSACTIONAL_ACTA_COMITE_SERVICE(ActaComiteTransactionalServiceRemote.class, "ActaComiteTransactionalBoundary"),
    TRANSACTIONAL_COMMON(CommonTransactionalServiceRemote.class, "CommonTransactionalBoundary"),
    TRANSACTIONAL_REFERENTE_CAPITA_SERVICE(ReferenteCapitaTransactionalServiceRemote.class, "ReferenteCapitaTransactionalBoundary"),
    TRANSACTIONAL_BANDEJA_REFERENTE_PGP(BandejaReferentePgpTransactionalServiceRemote.class, "BandejaReferentePgpTransactionalBoundary"),
    TRANSACTIONAL_GESTION_REFERENTE_PGP(GestionReferentePgpTransactionalServiceRemote.class, "GestionReferentePgpTransactionalBoundary"),
    TRANSACTIONAL_IMPORTAR_PROCEDIMIENTO_PGP(NegociacionProcedimientoPgpTransactionalServiceRemote.class, "NegociacionProcedimientoImportarTransactionalBoundary"),
    TRANSACTIONAL_IMPORTAR_MEDICAMENTO_PGP(NegociacionMedicamentoPgpTransactionalServiceRemote.class, "NegociacionMedicamentoImportarTransactionalBoundary"),
    TRANSACTIONAL_IMPORTAR_TECNOLOGIA(ImportarTecnologiasTransactionalServiceRemote.class,"ImportarTecnologiaTransactionalBoundary"),
    TRANSACTIONAL_NEGOCIACION_REFERENTE(NegociacionReferenteTransactionalServiceRemote.class, "NegociacionReferenteTransactionalBoundary")
    ;

    public static final String MODULE_NAME = "negociacion-ejb";
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
        return "negociacion";
    }
}
