package com.conexia.contractual.services.view.parametrizacion.boundary;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import com.conexia.contractual.definitions.view.parametrizacion.ParametrizacionContratoViewRemote;
import com.conexia.contratacion.commons.constants.enums.TipoProcedimientoEnum;
import com.conexia.contratacion.commons.dto.DescriptivoPaginacionDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.AreaInfluenciaDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroMedicamentoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroPaqueteDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroServicioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroTransporteDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.NegociacionServicioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionMedicamentoDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionServicioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.InsumosDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.TransporteDto;

/**
 * Boundary que implementa la logica de la parametrizacion de una solicitud de
 * contratacion.
 *
 * @author jalvarado
 */
@Stateless
@Remote(ParametrizacionContratoViewRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ParametrizacionContratoViewBoundary implements ParametrizacionContratoViewRemote {

    /**
     * Contexto de Persistencia.
     */
    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;

    //<editor-fold defaultstate="collapsed" desc="Consultar sedes negociadas">
    @Override
    public List<SedePrestadorDto> listarSedesPorParametrizar(final DescriptivoPaginacionDto filtroSedesDto,
            final Long idSolicitudContratacion) {
        return em.createNamedQuery("SedePrestador.obtenerSedes", SedePrestadorDto.class)
                .setParameter("negociacionId", new Long(filtroSedesDto.getId()))
                .setParameter("idSolicitudContratacion", idSolicitudContratacion)
                .setMaxResults(filtroSedesDto.getCantidadRegistros())
                .setFirstResult(filtroSedesDto.getPagina())
                .getResultList();
    }

    public List<SedePrestadorDto> listarSedesParametrizar(final Long negociacionId,
            final Long idSolicitudContratacion) {
        return em.createNamedQuery("SedePrestador.obtenerSedes", SedePrestadorDto.class)
                .setParameter("negociacionId", negociacionId)
                .setParameter("idSolicitudContratacion", idSolicitudContratacion)
                .getResultList();
    }

    @Override
    public int contarSedesPorParametrizar(final DescriptivoPaginacionDto filtroSedesDto) {
        Query query = em.createNamedQuery("SedePrestador.contarSedes");
        final Long conteo = (Long) query.setParameter("negociacionId", new Long(filtroSedesDto.getId()))
                .getSingleResult();
        return conteo.intValue();
    }

    @Override
    public SedePrestadorDto obtenerSedePorParametrizar(Long idSede) {
        return em.createNamedQuery("SedePrestador.obtenerSedeById", SedePrestadorDto.class)
                .setParameter("sedeNegociacionId", idSede)
                .getSingleResult();
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Parametrizacion de medicamentos">
    @Override
    public int contarMedicamentosPorParametrizar(
            SedeNegociacionMedicamentoDto sedeNegociacionMedicamentoDto) {
        return this.em.createNamedQuery("Medicamento.countMedicamentoBySedeNegociacionAndCategoria", Long.class)
                .setParameter("categoriaMedicamentoId", sedeNegociacionMedicamentoDto.getCategoriaMedicamentoId())
                .setParameter("negociacionId", sedeNegociacionMedicamentoDto.getNegociacionId())
                .getSingleResult().intValue();
    }

    @Override
    public List<SedeNegociacionMedicamentoDto> listarMedicamentosPorParametrizar(
            FiltroMedicamentoDto filtroMedicamento) {
        TypedQuery<SedeNegociacionMedicamentoDto> query = em.createQuery(""
                + "select DISTINCT new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionMedicamentoDto("
                + "cm.codigo, cm.nombre, count(m.id), "
                + "cm.id, snm.sedeNegociacion.negociacion.id, snm.sedeNegociacion.sedePrestador.id,"
                + "count(case when snm.requiereAutorizacionAmbulatorio = 'SI' THEN 'SI' END) as tecnologias_parametrizadas_ambulatorio_SI, "
                + "count(case when snm.requiereAutorizacionAmbulatorio = 'NO' THEN 'NO' END) as tecnologias_parametrizadas_ambulatorio_NO, "
                + "count(case when snm.requiereAutorizacionAmbulatorio = 'ESPECIAL' THEN 'ESPECIAL' END) as tecnologias_parametrizadas_ambulatorio_ESPECIAL, "
                + "count(case when snm.requiereAutorizacionHospitalario = 'SI' THEN 'SI' END) as tecnologias_parametrizadas_hospitalario_SI,  "
                + "count(case when snm.requiereAutorizacionHospitalario = 'NO' THEN 'NO' END) as tecnologias_parametrizadas_hospitalario_NO, "
                + "count(case when snm.requiereAutorizacionHospitalario = 'ESPECIAL' THEN 'ESPECIAL' END) as tecnologias_parametrizadas_hospitalario_ESPECIAL, "
                + "case when string_agg(cast(m.tipoPPMId as string),',') like '%2%' then 'true' else 'false' END) "
                + "from SedeNegociacionMedicamento snm "
                + "join snm.medicamento m "
                + "join m.categoriaMedicamento cm "
                + "where snm.sedeNegociacion.id = :sedeNegociacionId "
                + (!filtroMedicamento.getCategoriasMedicamento().isEmpty() ? "and cm.id in :categoriaMedicamentoId " : "")
                + "group by cm, snm.sedeNegociacion, snm.sedeNegociacion.negociacion, snm.sedeNegociacion.sedePrestador.id "
                + "order by cm.codigo", SedeNegociacionMedicamentoDto.class);
        if (!filtroMedicamento.getCategoriasMedicamento().isEmpty()) {
            query.setParameter("categoriaMedicamentoId", filtroMedicamento.getCategoriasMedicamento().stream().map(
                    categoriasMedicamento -> categoriasMedicamento.getId()).collect(Collectors.toList()));
        }
        query.setParameter("sedeNegociacionId", filtroMedicamento.getSedeNegociacionId());
        return query.getResultList();
    }

    @Override
    public int contarMedicamentosPorParamterizar(FiltroMedicamentoDto filtroMedicamento) {
        return this.em.createNamedQuery("SedeNegociacionMedicamento.countSedeNegociacionMedicamento", Long.class)
                .setParameter("sedeNegociacionId", filtroMedicamento.getSedeNegociacionId())
                .getSingleResult().intValue();
    }

    @Override
    public int validarMedicamentosPorParamterizar(FiltroMedicamentoDto filtroMedicamento) {
        return this.em.createNamedQuery("SedeNegociacionMedicamento.validarMedicamentosPorParametrizar", Long.class)
                .setParameter("sedeNegociacionId", filtroMedicamento.getSedeNegociacionId())
                .getSingleResult().intValue();
    }

    @Override
    public List<MedicamentosDto> listarMedicamentosPorParametrizar(
            SedeNegociacionMedicamentoDto sedeNegociacionMedicamentoDto) {
        Map<String, Object> filtros = sedeNegociacionMedicamentoDto.getFiltros();
        return this.em.createNamedQuery("Medicamento.findMedicamentoBySedeNegociacionAndCategoria", MedicamentosDto.class)
                .setParameter("categoriaMedicamentoId", sedeNegociacionMedicamentoDto.getCategoriaMedicamentoId())
                .setParameter("negociacionId", sedeNegociacionMedicamentoDto.getNegociacionId())
                .getResultList();
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Parametrizacion de paquetes">
    @Override
    public int contarPaquetesPorParametrizar(FiltroPaqueteDto filtroPaqueteDto) {
        return this.em.createNamedQuery("SedeNegociacionPaquete.countBySedesNegociacion",
                Long.class).setParameter("sedeNegociacionId",
                        filtroPaqueteDto.getSedeNegociacionId())
                .getSingleResult().intValue();
    }

    @Override
    public List<SedeNegociacionPaqueteDto> listarPaquetesPorParametrizar(
            FiltroPaqueteDto filtroPaqueteDto) {
        TypedQuery<SedeNegociacionPaqueteDto> query = this.em.createQuery(
                "select new com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionPaqueteDto "
                + "(snp.id, pp.codigoPortafolio, pp.codigoSedePrestador, pp.descripcion,"
                + "pp.tipoPaquete, ms.nombre, "
                + "count(distinct m) + count(distinct p) + count(distinct i),  "
                + "count (case "
                + "when (t.estadoProcedimiento.id != 1) then 'PROCEDIMIENTO' "
                + "when (m.estadoMedicamento != 1) then 'MEDICAMENTO' "
                + "when (t.estadoProcedimiento.id != 1) then 'TRASLADO' "
                + "end), "
                + "snp.sedeNegociacion.id, sn.negociacion.id , "
                + "snp.requiereAutorizacionAmbulatorio, snp.requiereAutorizacionHospitalario , count(m), "
                + "count(case when p.tipoProcedimiento.id = 1 then p.id end), "
                + "count(case when t.tipoProcedimiento.id = 3 then t.id end), "
                + "count(i) "
                + ") "
                + "from SedeNegociacionPaquete snp "
                + "join snp.paquete paq "
                + "join paq.paquetePortafolios pp "
                + "join pp.macroservicio ms "
                + "left join snp.sedeNegociacion sn "
                + "left join snp.sedeNegociacionPaqueteProcedimiento snpp "
                + "left join snpp.procedimiento procPaq "
                + "left join procPaq.procedimiento p "
                + "left join snp.sedeNegociacionPaqueteTraslados snpt "
                + "left join snpt.procedimiento procs "
                + "left join procs.procedimiento t "
                + "left join snp.sedeNegociacionPaqueteMedicamentos snpm "
                + "left join snpm.medicamento m "
                + "left join snp.sedeNegociacionPaqueteInsumos snpi "
                + "left join snpi.insumo i "
                + "where snp.sedeNegociacion.id = :sedeNegociacionId "
                + (!filtroPaqueteDto.getMacroServicios().isEmpty() ? "and ms.id in :macroservicios " : "")
                + "group by snp, pp, ms, sn ",
                SedeNegociacionPaqueteDto.class);
        query.setParameter("sedeNegociacionId", filtroPaqueteDto.getSedeNegociacionId());
        if (!filtroPaqueteDto.getMacroServicios().isEmpty()) {
            query.setParameter("macroservicios", filtroPaqueteDto.getMacroServicios().stream().map(
                    macroservicio -> macroservicio.getId()).collect(Collectors.toList()));
        }
        return query.getResultList();
    }

    @Override
    public List<InsumosDto> listarInsumosPorPaquete(
            SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto) {
        return this.em.createNamedQuery(
                "SedeNegociacionPaqueteInsumo.findInsumosByNegociacionAndPaquete",
                InsumosDto.class)
        		.setParameter("negociacionId", sedeNegociacionPaqueteDto.getNegociacionId())
                .setParameter("sedeNegociacionPaqueteId", sedeNegociacionPaqueteDto.getSedeNegociacionPaqueteId())
                .getResultList();
    }

    @Override
    public List<MedicamentosDto> listarMedicamentosPorPaquete(
            SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto) {
        return this.em.createNamedQuery("SedeNegociacionPaqueteMedicamento.findMedicamentosBySedeNegociacionPaquete",
                MedicamentosDto.class).setParameter("sedeNegociacionPaqueteId", sedeNegociacionPaqueteDto.getSedeNegociacionPaqueteId()).getResultList();
    }

    @Override
    public List<ProcedimientoDto> listarProcedimientosPorPaquete(SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto) {
        return this.em.createNamedQuery("SedeNegociacionPaqueteProcedimiento.findProcedimientosBySedeNegociacionPaquete", ProcedimientoDto.class)
                .setParameter("sedeNegociacionPaqueteId", sedeNegociacionPaqueteDto.getSedeNegociacionPaqueteId())
                .setParameter("tipoProcedimiento", TipoProcedimientoEnum.PROCEDIMIENTO)
                .getResultList();
    }

    @Override
    public List<TransporteDto> listarTrasladosPorPaquete(
            SedeNegociacionPaqueteDto sedeNegociacionPaqueteDto) {
        return this.em.createNamedQuery("SedeNegociacionPaqueteTraslado.findTrasladosBySedeNegociacionPaquete",
                TransporteDto.class).setParameter("sedeNegociacionPaqueteId", sedeNegociacionPaqueteDto.getSedeNegociacionPaqueteId()).getResultList();
    }

    @Override
    public int validarTrasladosPorParametrizar(FiltroTransporteDto filtroTransporteDto) {
        return em.createNamedQuery("SedeNegociacionTransporte.validarTrasladosPorParametrizar", Long.class)
                .setParameter("sedeNegociacionId", filtroTransporteDto.getSedeNegociacionId())
                .getSingleResult().intValue();
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Consulta area de influencia">
    @Override
    public List<AreaInfluenciaDto> listarAreasInfluencia(DescriptivoPaginacionDto filtroSedesDto) {
        final StringBuilder ql = new StringBuilder("Select new com.conexia.contratacion.commons.dto.contractual.parametrizacion.AreaInfluenciaDto("
                + "m.descripcion,d.descripcion,r.descripcion) "
                + " from AreaCoberturaSedes s join s.municipio m join m.departamento d join d.regional r"
                + " where s.sedesNegociacion.id  = " + filtroSedesDto.getId());
        if (filtroSedesDto.getFiltros().size() > 0) {
            Iterator it = filtroSedesDto.getFiltros().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (pair.getKey().toString().equals("regional")) {
                    ql.append(" AND LOWER(r.descripcion) LIKE '%");
                    ql.append(pair.getValue().toString().toLowerCase()).append("%'");
                }
                if (pair.getKey().toString().equals("departamento")) {
                    ql.append(" AND LOWER(d.descripcion) LIKE '%");
                    ql.append(pair.getValue().toString().toLowerCase()).append("%'");
                }

                if (pair.getKey().toString().equals("municipio")) {
                    ql.append(" AND LOWER(m.descripcion) LIKE '%");
                    ql.append(pair.getValue().toString().toLowerCase()).append("%'");
                }

                if (pair.getKey().toString().equals("seleccionado")) {
                	ql.append(" AND s.seleccionado = ");
                	ql.append(pair.getValue().toString().toLowerCase());
                }
            }
        }
        TypedQuery<AreaInfluenciaDto> query = em.createQuery(ql.toString(), AreaInfluenciaDto.class);
        return query.setMaxResults(filtroSedesDto.getCantidadRegistros())
                .setFirstResult(filtroSedesDto.getPagina())
                .getResultList();
    }

    @Override
    public int contarAreasInfluencia(DescriptivoPaginacionDto filtroSedesDto) {
        final StringBuilder sb = new StringBuilder("Select count(distinct m.id)  ");
        sb.append(" from AreaCoberturaSedes s join s.municipio m join m.departamento d join d.regional r "
                + " where s.sedesNegociacion.id = :sedeNegociacionId ");
        if (filtroSedesDto.getFiltros().size() > 0) {
            Iterator it = filtroSedesDto.getFiltros().entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                if (pair.getKey().toString().equals("regional")) {
                    sb.append(" AND LOWER(r.descripcion) LIKE '%");
                    sb.append(pair.getValue().toString().toLowerCase()).append("%'");
                }
                if (pair.getKey().toString().equals("departamento")) {
                    sb.append(" AND LOWER(d.descripcion) LIKE '%");
                    sb.append(pair.getValue().toString().toLowerCase()).append("%'");
                }

                if (pair.getKey().toString().equals("municipio")) {
                    sb.append(" AND LOWER(m.descripcion) LIKE '%");
                    sb.append(pair.getValue().toString().toLowerCase()).append("%'");
                }
                if (pair.getKey().toString().equals("seleccionado")) {
                	sb.append(" AND s.seleccionado = ");
                	sb.append(pair.getValue().toString().toLowerCase());
                }
            }
        }
        TypedQuery<Long> query = em.createQuery(sb.toString(), Long.class);
        final int areas = query.setParameter("sedeNegociacionId", new Long(filtroSedesDto.getId()))
                .getSingleResult().intValue();
        return areas;
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Parametrizacion servicios">
    @Override
    public List<SedeNegociacionServicioDto> listarServiciosPorParametrizar(FiltroServicioDto filtroServicioDto) {
        return em.createNamedQuery("SedeNegociacionProcedimiento.listarProcedimientosPorParametrizar", SedeNegociacionServicioDto.class)
                .setParameter("macroServicios", filtroServicioDto.getMacroServiciosDto().stream().map(ms -> ms.getId()).collect(Collectors.toList()))
                .setParameter("sedeNegociacionId", filtroServicioDto.getSedeNegociacionId())
                .getResultList();
    }

    @Override
    public int contarServiciosPorParametrizar(FiltroServicioDto filtroServicioDto) {
        return em.createNamedQuery("SedeNegociacionProcedimiento.contarProcedimientosPorParametrizar", Long.class)
                .setParameter("sedeNegociacionId", filtroServicioDto.getSedeNegociacionId())
                .setParameter("tipoProcedimiento", TipoProcedimientoEnum.PROCEDIMIENTO)
                .getSingleResult().intValue();
    }

    @Override
    public int validarServiciosPorParametrizar(FiltroServicioDto filtroServicioDto) {
        return em.createNamedQuery("SedeNegociacionProcedimiento.validarProcedimientosPorParametrizar", Long.class)
                .setParameter("sedeNegociacionId", filtroServicioDto.getSedeNegociacionId())
                .setParameter("tipoProcedimiento", TipoProcedimientoEnum.PROCEDIMIENTO)
                .getSingleResult().intValue();
    }

    @Override
    public List<NegociacionServicioDto> listarDetalleServiciosPorParametrizar(SedeNegociacionServicioDto sedeNegociacionServicioDto) {
        Map<String, Object> filtro = sedeNegociacionServicioDto.getFiltros();
        return em.createNamedQuery("SedeNegociacionProcedimiento.listarDetalleServiciosPorParametrizar", NegociacionServicioDto.class)
                .setParameter("negociacionId", sedeNegociacionServicioDto.getNegociacionId())
                .setParameter("sedePrestadorId", sedeNegociacionServicioDto.getSedePrestadorId())
                .setParameter("categoriaProcedimientoId", sedeNegociacionServicioDto.getServicioSaludId())
                .setParameter("cups", obtenerValor(filtro, "cups"))
                .setParameter("codigo", obtenerValor(filtro, "codigo"))
                .setParameter("descripcion", obtenerValor(filtro, "descripcion"))
                .setMaxResults(sedeNegociacionServicioDto.getCantidadRegistros())
                .setFirstResult(sedeNegociacionServicioDto.getPagina())
                .getResultList();
    }

    public List<NegociacionServicioDto> listarDetalleServiciosParametrizar(SedeNegociacionServicioDto sedeNegociacionServicioDto) {
        Map<String, Object> filtro = sedeNegociacionServicioDto.getFiltros();
        return em.createNamedQuery("SedeNegociacionProcedimiento.listarDetalleServiciosPorParametrizar", NegociacionServicioDto.class)
                .setParameter("negociacionId", sedeNegociacionServicioDto.getNegociacionId())
                .setParameter("sedePrestadorId", sedeNegociacionServicioDto.getSedePrestadorId())
                .setParameter("categoriaProcedimientoId", sedeNegociacionServicioDto.getServicioSaludId())
                .getResultList();
    }

    @Override
    public int contarDetalleServiciosPorParametrizar(SedeNegociacionServicioDto sedeNegociacionServicioDto) {
        Map<String, Object> filtro = sedeNegociacionServicioDto.getFiltros();
        return em.createNamedQuery("SedeNegociacionProcedimiento.contarDetalleServiciosPorParametrizar", Long.class)
                .setParameter("categoriaProcedimientoId", sedeNegociacionServicioDto.getServicioSaludId())
                .setParameter("negociacionId", sedeNegociacionServicioDto.getNegociacionId())
                .setParameter("sedePrestadorId", sedeNegociacionServicioDto.getSedePrestadorId())
                .setParameter("cups", obtenerValor(filtro, "cups"))
                .setParameter("codigo", obtenerValor(filtro, "codigo"))
                .setParameter("descripcion", obtenerValor(filtro, "descripcion"))
                .getSingleResult().intValue();
    }

    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Solicitud Contratacion Sede">
    @Override
    public int contarSolicitudContratacionSede(final Long sedeNegociacionId, final Long idSolicitudContratacion) {
        return em.createNamedQuery("SolicitudContratacionSede.contarSolicitudContratacionSede", Long.class)
                .setParameter("sedeNegociacionId", sedeNegociacionId)
                .setParameter("idSolicitudContratacion", idSolicitudContratacion)
                .getSingleResult().intValue();
    }
//</editor-fold>

    private Object obtenerValor(Map<String, Object> filtro, String clave) {
        return "%" + (filtro.get(clave) == null ? ""
                : filtro.get(clave).toString().toUpperCase()) + "%";
    }
    @Override
    public List<Long> obtenerIdSedesNegociacion(final Long negociacionId){
    	return em.createNamedQuery("SedesNegociacion.obtenerIdSedesNegociacion", Long.class)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }
}
