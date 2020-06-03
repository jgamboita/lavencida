package com.conexia.contractual.services.view.parametros.boundary;

import java.util.List;
import java.util.Objects;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import com.conexia.contractual.definitions.view.parametros.ParametrosViewRemote;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.RiasEnum;
import com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.GrupoTransporteDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MacroServicioDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.RangoPoblacionDto;
import com.conexia.contratacion.commons.dto.maestros.RegionalDto;
import com.conexia.contratacion.commons.dto.maestros.RiaDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;

/**
 * EJB de tablas de parametrizacion.
 *
 * @author jalvarado
 */
@Stateless
@Remote(ParametrosViewRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class ParametrosViewBoundary implements ParametrosViewRemote {

    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;

    @Override
    public List<CategoriaMedicamentoDto> listarCategoriasMedicamento() {
        return em.createNamedQuery("CategoriaMedicamento.findAllDto", CategoriaMedicamentoDto.class).getResultList();
    }

    @Override
    public List<GrupoTransporteDto> listarGruposTransporte() {
        return em.createNamedQuery("GrupoTransporte.findAllDto", GrupoTransporteDto.class).getResultList();
    }

    @Override
    public List<MacroServicioDto> listarMacroServicios() {
        return em.createNamedQuery("MacroServicio.findAllDto", MacroServicioDto.class).getResultList();
    }

    @Override
    public List<TipoIdentificacionDto> listarTiposIdentificacion() {
        return em.createNamedQuery("TipoIdentificacion.findAll", TipoIdentificacionDto.class).getResultList();
    }

    @Override
    public List<TipoIdentificacionDto> listarTiposIdentificacionIPS() {
        return em.createNamedQuery("TipoIdentificacion.findAllIPS", TipoIdentificacionDto.class).getResultList();
    }

    @Override
    public List<DepartamentoDto> listarDepartamentos() {
        return em.createNamedQuery("Departamento.findAll", DepartamentoDto.class).getResultList();
    }

    @Override
    public List<DepartamentoDto> listarDepartamentosPorRegional(Integer regionalId) {
        return em.createNamedQuery("Departamento.findByRegionalId", 
                DepartamentoDto.class).setParameter("regionalId", regionalId)
                .getResultList();
    }
    
    @Override
    public List<MunicipioDto> listarMunicipiosPorDepartameto(final Integer idDepartamento) {
        return em.createNamedQuery("Municipio.listarByDepartamentoId", MunicipioDto.class)
                .setParameter("idDepartamento", idDepartamento)
                .getResultList();
    }

    @Override
    public List<RegionalDto> listarRegionales() {
        return em.createNamedQuery("Regional.findRegionales", RegionalDto.class)
                .getResultList();
    }

    @Override
    public RegionalDto buscarRegionalPorId(final Integer regionalId) {
        return em.createNamedQuery("Regional.findRegionalById", RegionalDto.class)
                .setParameter("id", regionalId)
                .getSingleResult();         
    }

	@Override
	public Boolean tienePaquetes(Long negociacionId) {
		StringBuilder select = new StringBuilder();
		select.append("select count(distinct ppp.codigo) from contratacion.negociacion n "
				+ "	inner join contratacion.sedes_negociacion sn on n.id = sn.negociacion_id "
				+ "	inner join contratacion.sede_prestador sp on sp.id = sn.sede_prestador_id "
				+ "	inner join contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id "
				+ "	inner join contratacion.paquete_portafolio ppp on snp.paquete_id = ppp.portafolio_id "
				+ "	inner join contratacion.macroservicio m on m.id = ppp.macroservicio_id "
				+ "	where n.id = :negociacionId");

		return ((Number) em.createNativeQuery(select.toString())
		        .setParameter("negociacionId", negociacionId)
				.getSingleResult()).intValue() > 0;
	}

    @Override
    public Boolean tieneMedicamentos(Long negociacionId, Integer modalidadId) {
        StringBuilder select = new StringBuilder();

        if (NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.getId().equals(modalidadId)) {
            select.append("SELECT count(distinct snm.id) " + "        FROM contratacion.sedes_negociacion sn "
                    + "          INNER JOIN contratacion.negociacion n ON n.id = sn.negociacion_id "
                    + "          INNER JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id "
                    + "          INNER JOIN contratacion.sede_negociacion_medicamento snm ON snm.sede_negociacion_id = sn.id "
                    + "          INNER JOIN maestros.medicamento m ON m.id = snm.medicamento_id "
                    + "          where n.id = :negociacionId");
        } else {
            select.append("SELECT count(distinct snm.id) " + "        FROM contratacion.sedes_negociacion sn "
                    + "          INNER JOIN contratacion.negociacion n ON n.id = sn.negociacion_id "
                    + "          INNER JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id "
                    + "          INNER JOIN contratacion.sede_negociacion_medicamento snm ON snm.sede_negociacion_id = sn.id "
                    + "          INNER JOIN maestros.medicamento m ON m.id = snm.medicamento_id "
                    + "          INNER JOIN contratacion.categoria_medicamento cm ON cm.id = m.categoria_id "
                    + "          where n.id = :negociacionId");
        }

        return ((Number) em.createNativeQuery(select.toString()).setParameter("negociacionId", negociacionId)
                .getSingleResult()).intValue() > 0;
    }
	
    @Override
    public Boolean tieneProcedimientos(Long negociacionId, Integer modalidadId) {
        StringBuilder select = new StringBuilder();
        if (NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.getId().equals(modalidadId)) {
            select.append("SELECT count(distinct p.id) " + "                 FROM contratacion.sedes_negociacion sn "
                    + "                 INNER JOIN contratacion.negociacion n ON n.id = sn.negociacion_id "
                    + "                 INNER JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id "
                    + "                 INNER JOIN contratacion.sede_negociacion_capitulo snc ON snc.sede_negociacion_id = sn.id "
                    + "                 INNER JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_capitulo_id = snc.id "
                    + "                 INNER JOIN maestros.procedimiento p ON p.id = snp.pto_id "
                    + "                 INNER JOIN maestros.categoria_procedimiento cp ON cp.id = p.categoria_procedimiento_id "
                    + "                 INNER JOIN maestros.capitulo_procedimiento cap ON cap.id = cp.capitulo_procedimiento_id "
                    + "                 where n.id = :negociacionId");
        } else {
            select.append("SELECT count(distinct p.id) " + "                 FROM contratacion.sedes_negociacion sn "
                    + "                 INNER JOIN contratacion.negociacion n ON n.id = sn.negociacion_id "
                    + "                 INNER JOIN contratacion.sede_prestador sp ON sp.id = sn.sede_prestador_id "
                    + "                 INNER JOIN contratacion.sede_negociacion_servicio sns ON sns.sede_negociacion_id = sn.id "
                    + "                 INNER JOIN contratacion.sede_negociacion_procedimiento snp ON snp.sede_negociacion_servicio_id = sns.id "
                    + "                 INNER JOIN contratacion.servicio_salud ss ON ss.id = sns.servicio_id "
                    + "                 INNER JOIN maestros.procedimiento_servicio ps ON ps.id = snp.procedimiento_id "
                    + "                 INNER JOIN maestros.procedimiento p ON p.id = ps.procedimiento_id "
                    + "                 where n.id = :negociacionId");
        }

        return ((Number) em.createNativeQuery(select.toString()).setParameter("negociacionId", negociacionId)
                .getSingleResult()).intValue() > 0;
    }

	@Override
	public Boolean tieneProcedimientosRecuperacion(Long negociacionId) {
		StringBuilder select = new StringBuilder();
        select.append("SELECT count(0) "
        		+ " FROM contratacion.negociacion_ria nr "
        		+ " JOIN maestros.ria r on r.id = nr.ria_id "
        		+ " JOIN contratacion.negociacion_ria_rango_poblacion nrr on nrr.negociacion_ria_id = nr.id"
        		+ " JOIN contratacion.sedes_negociacion sn on sn.negociacion_id = nr.negociacion_id "
        		+ " JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id "
        		+ " JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id "
        		+ "			AND snp.negociacion_ria_rango_poblacion_id = nrr.id "
        		+ "	WHERE nr.negociado = true and nr.negociacion_id = :negociacionId "
        		+ " AND r.codigo = '"+RiasEnum.RECUPERACION.getCodigo()+"'");        
        return ((Number)em.createNativeQuery(select.toString())
        		.setParameter("negociacionId", negociacionId).getSingleResult()).intValue() > 0;
	}
	
	@Override
	public List<RangoPoblacionDto> listarRangoPoblacion() {
		return em.createNamedQuery("RangoPoblacion.findAll", RangoPoblacionDto.class).getResultList();
	}

	@Override
	public List<RiaDto> listarRias() {
		return em.createNamedQuery("Ria.findAll", RiaDto.class).getResultList();
	}
	
	@Override
	public List<SedesNegociacionDto> listarSedesPorNegociacion(Long negociacionId) {
		return em
				.createNamedQuery("SedesNegociacion.findSedePrestadorPrincipalDtoByNegociacionId",
						SedesNegociacionDto.class)
				.setParameter("negociacionId", negociacionId)
				.getResultList();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public List<RiaDto> consultarTotalRiasByNegociacion(Long negociacionId) {
		return em.createNativeQuery(
				"SELECT r.id, r.descripcion, nr.valor, nr.valor_total "
				+ " FROM contratacion.negociacion_ria nr "
				+ " JOIN maestros.ria r on r.id = nr.ria_id "
				+ " WHERE nr.negociacion_id = :negociacionId "
				+ " GROUP BY r.id, r.descripcion, nr.valor, nr.valor_total"
				+ " ORDER BY r.id","NegociacionRia.TotalPorRiaMapping")
				.setParameter("negociacionId", negociacionId)
				.getResultList();
	}

	@Override
	public Double consultarTotalRiasCapitaByNegociacion(Long negociacionId) {
		Number resultado =(Number)em.createNativeQuery("SELECT n.valor_total "
				+ " FROM contratacion.negociacion n "
				+ " WHERE n.id = :negociacionId ")
				.setParameter("negociacionId", negociacionId).getSingleResult();		
		return Objects.nonNull(resultado) ?  resultado.doubleValue() : 0D;
	}
	
	
	public PrestadorDto consultarPrestador(Long prestadorId) {
		return em
				.createNamedQuery("Prestador.findDtoByPrestadorId",
						PrestadorDto.class)
				.setParameter("prestadorId", prestadorId).getSingleResult();
	}
	
    @Override
    public Boolean tienePoblacion(Long negociacionId) {
        StringBuilder select = new StringBuilder();
        select.append("SELECT count(0) " 
                + " FROM contratacion.afiliado_x_sede_negociacion asn "
                + " INNER JOIN contratacion.sedes_negociacion sn ON sn.id = asn.sede_negociacion_id "
                + " WHERE sn.negociacion_id = :negociacionId ");
        return ((Number) em.createNativeQuery(select.toString()).setParameter("negociacionId", negociacionId)
                .getSingleResult()).intValue() > 0;
    }
}
