package co.conexia.negociacion.services.bandeja.comparacion.control;

import co.conexia.negociacion.services.bandeja.comparacion.boundary.BandejaComparacionViewBoundary;

import java.util.HashMap;
import java.util.Map;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TipoTecnologiaEnum;
import com.conexia.contratacion.commons.dto.negociacion.FiltroBandejaComparacionDto;

/**
 * Control para las acciones adicionales que necesite el boundary
 * {@link BandejaComparacionViewBoundary}
 *
 * @author mcastro
 *
 */
public class BandejaComparacionControl {

    public void generarSelectBuscarPrestador(StringBuilder query, NegociacionModalidadEnum modalidad) {
        query.append("SELECT DISTINCT NEW com.conexia.contratacion.commons.dto.maestros.PrestadorDto(")
                .append("   p.id ,")
                .append("   p.nombre ,")
                .append("   p.numeroDocumento ,")
                .append("   p.prefijo ,")
                .append("   (SELECT COUNT(sp.id) FROM SedePrestador sp WHERE sp.prestador.id = p.id  AND sp.enumStatus = 1),")//contar sedes
                .append("   p.fechaInicioVigencia ,")
                .append("   p.mesesVigencia ,");
        this.generarSelecTecnologias(query, modalidad);
        query.append(" p.estadoPrestador ) ").append("   FROM Prestador p ");
    }

    /**
     * Genera los select dependiendo si es para el portafolio evento 
     * o por portafolio mod
     * @param query
     * @param modalidad
     */
    private void generarSelecTecnologias(StringBuilder query, NegociacionModalidadEnum modalidad) {
        if(modalidad == NegociacionModalidadEnum.EVENTO){
            query.append("   (SELECT COUNT(gs.id) FROM GrupoServicio gs JOIN gs.portafolio por JOIN por.sedePrestador sp ")
            .append("   WHERE sp.prestador.id = p.id AND sp.enumStatus = 1), ") //Contar procedimientos
            .append("   (SELECT COUNT(mp.id) FROM MedicamentoPortafolio mp JOIN mp.portafolio por JOIN por.sedePrestador sp ")
            .append("   WHERE sp.prestador.id = p.id AND sp.enumStatus = 1), ") //Contar medicamentos
            .append("   (SELECT COUNT(pp.id) FROM PaquetePortafolio pp JOIN pp.portafolio por JOIN por.portafolioPadre porPadre ")
            .append("   JOIN porPadre.sedePrestador sp WHERE sp.prestador.id = p.id AND sp.enumStatus = 1), ") //Contar paquetes
            .append("   (SELECT COUNT(tp.id) FROM TransportePortafolio tp JOIN tp.portafolio por JOIN por.sedePrestador sp JOIN tp.transporte t JOIN t.procedimiento px  ")
            .append("   WHERE sp.prestador.id = p.id AND sp.enumStatus = 1 AND px.tipoProcedimiento.id = 3 ), "); //Contar traslados
        }else if(modalidad == NegociacionModalidadEnum.CAPITA){
            query.append(" (SELECT COUNT(sps.id) FROM OfertaSedePrestador mosp JOIN mosp.portafolio por JOIN por.servicioPortafolioSedes sps JOIN mosp.ofertaPrestador op ")
            .append("   WHERE por.prestador.id = p.id AND op.ofertaPresentar = true AND mop.modalidad.id = "+modalidad.getId()+"),") //Contar procedimientos
            .append(" (SELECT COUNT(cmps.id) FROM OfertaSedePrestador mosp JOIN mosp.portafolio por JOIN por.categoriasMedicamentosPortafolioSede cmps JOIN cmps.medicamentoPortafolioSedes mps JOIN mosp.ofertaPrestador op ")
            .append("   WHERE por.prestador.id = p.id AND op.ofertaPresentar = true AND mop.modalidad.id = "+modalidad.getId()+"),") //Contar medicamentos
            .append("   0L,") //Contar paquetes
            .append("   0L,") //Contar traslados
            .append("   mop.fechaInicioVigencia,") //fecha nuevo esquema
            .append("   mop.mesesVigencia,"); //meses nuevo esquema
        }
    }

    public Map<String, Object> generarWhereBuscarPrestador(FiltroBandejaComparacionDto filtro, StringBuilder query) {
        if(filtro.getModalidadNegociacion() == NegociacionModalidadEnum.EVENTO){
            query.append(" JOIN p.sedePrestador sp ");
        }else if(filtro.getModalidadNegociacion() == NegociacionModalidadEnum.CAPITA){
            query.append(" JOIN p.modOfertaPrestadores mop JOIN mop.ofertaSedesPrestador osp ");
        }
        Map<String, Object> params = new HashMap<>();
        query.append(" WHERE p.estadoPrestador = '" + EstadoPrestadorEnum.REVISION_TARIFAS.toString() + "'");
        if (filtro.getNumeroIdentificacion() != null && !filtro.getNumeroIdentificacion().equals("")) {
            query.append(" AND p.numeroDocumento LIKE :numeroDocumento");
            params.put("numeroDocumento", "%" + filtro.getNumeroIdentificacion().trim() + "%");
        }
        if (filtro.getPrefijo() != null && !filtro.getPrefijo().equals("")) {
            query.append(" AND p.prefijo LIKE :prefijo");
            params.put("prefijo", "%" +filtro.getPrefijo().trim()+ "%");
        }
        if (filtro.getNombrePrestador() != null && !filtro.getNombrePrestador().equals("")) {
            query.append(" AND p.nombre like :nombrePrestador");
            params.put("nombrePrestador", "%" + filtro.getNombrePrestador().trim().toUpperCase() + "%");
        }
        if (filtro.getTipoIdentificacionSeleccionado() != null && filtro.getTipoIdentificacionSeleccionado().getId() != null) {
            query.append(" AND p.tipoIdentificacion.id = :tipoDocumento ");
            params.put("tipoDocumento", filtro.getTipoIdentificacionSeleccionado().getId());
        }
        if(filtro.getModalidadNegociacion() == NegociacionModalidadEnum.CAPITA){
            query.append(" AND mop.ofertaPresentar = true AND mop.modalidad.id = "+filtro.getModalidadNegociacion().getId());
        }

        // Filtro de tecnologias por modalidad
         if (filtro.getTiposTecnologiasSeleccionados() != null && filtro.getTiposTecnologiasSeleccionados().size() > 0) {
            if (filtro.getModalidadNegociacion() == NegociacionModalidadEnum.EVENTO) {
                for (Object tecnologiaObj : filtro.getTiposTecnologiasSeleccionados()) {
                    TipoTecnologiaEnum tecnologia = TipoTecnologiaEnum.valueOf((String) tecnologiaObj);
                    if (tecnologia == TipoTecnologiaEnum.PROCEDIMIENTOS) {
                        query.append(" AND (SELECT COUNT(*) FROM SedeNegociacionServicio s JOIN s.sedeNegociacion sn JOIN sn.sedePrestador sp WHERE sp.prestador.id = p.id) > 0 ");
                    } else if (tecnologia == TipoTecnologiaEnum.MEDICAMENTOS) {
                        query.append(" AND (SELECT COUNT(*) FROM SedeNegociacionMedicamento mp JOIN mp.sedeNegociacion sn JOIN sn.sedePrestador sp WHERE sp.prestador.id = p.id) > 0 ");
                    } else if (tecnologia == TipoTecnologiaEnum.PAQUETES) {
                        query.append(" AND (SELECT COUNT(*) FROM SedeNegociacionPaquete pp JOIN pp.sedeNegociacion sn JOIN sn.sedePrestador sp WHERE sp.prestador.id = p.id) > 0 ");
                    }
                }
            } else if (filtro.getModalidadNegociacion() == NegociacionModalidadEnum.CAPITA) {
                for (Object tecnologiaObj : filtro.getTiposTecnologiasSeleccionados()) {
                    TipoTecnologiaEnum tecnologia = TipoTecnologiaEnum.valueOf((String) tecnologiaObj);
                    if (tecnologia == TipoTecnologiaEnum.PROCEDIMIENTOS) {
                        query.append(" AND (SELECT COUNT(*) FROM SedeNegociacionServicio gs JOIN gs.sedeNegociacion por JOIN por.sedePrestador sp WHERE sp.prestador.id = p.id) > 0 ");
                    } else if (tecnologia == TipoTecnologiaEnum.MEDICAMENTOS) {
                        query.append(" AND (SELECT COUNT(*) FROM SedeNegociacionMedicamento mp JOIN mp.sedeNegociacion por JOIN por.sedePrestador sp WHERE sp.prestador.id = p.id) > 0 ");
                    }
                }
            }
        }
        
        
        return params;
    }

    
}
