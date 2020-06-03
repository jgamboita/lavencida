package co.conexia.negociacion.services.common.boundary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import com.conexia.contratacion.commons.constants.CommonConstants;
import com.conexia.contratacion.commons.constants.enums.FiltroReferentePgpEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contractual.model.contratacion.Upc;
import com.conexia.contratacion.commons.dto.RegimenAfiliacionDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.DepartamentoDto;
import com.conexia.contratacion.commons.dto.maestros.GrupoEtnicoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.PrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.RangoPoblacionDto;
import com.conexia.contratacion.commons.dto.maestros.RegionalDto;
import com.conexia.contratacion.commons.dto.maestros.RiaDto;
import com.conexia.contratacion.commons.dto.maestros.TipoIdentificacionDto;
import com.conexia.contratacion.commons.dto.maestros.TipoTarifarioDto;
import com.conexia.contratacion.commons.dto.maestros.ZonaMunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionMunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ZonaCapitaDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.contratacion.commons.dto.referente.ReferentePrestadorDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteUbicacionDto;
import com.conexia.negociacion.definitions.common.CommonViewServiceRemote;

/**
 * Boundary para los servicios comunes de la aplicacion
 *
 * @author jjoya
 *
 */

@Stateless
@Remote(CommonViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class CommonViewBoundary implements Serializable, CommonViewServiceRemote {

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;

	@Override
	public List<TipoIdentificacionDto> listarTiposIdentificacion() {
		return em.createNamedQuery("TipoIdentificacion.findAll",
				TipoIdentificacionDto.class).getResultList();
	}

	@Override
	public List<TipoTarifarioDto> listarTiposTarifarios() {
		return em.createNamedQuery("TipoTarifario.findAll",
				TipoTarifarioDto.class).getResultList();
	}

	@Override
	public List<SedesNegociacionDto> listarSedesNegociacionById(Long negociacionId) {
		return em.createNamedQuery("SedesNegociacion.findDtoByNegociacionId", SedesNegociacionDto.class)
				.setParameter("negociacionId", negociacionId)
				.getResultList();
	}

	@Override
	public Integer contarPoblacionNegociacionById(Long negociacionId) {
		String conteo = em.createNamedQuery("Negociacion.contarPoblacionNegociacion")
							.setParameter("negociacionId", negociacionId)
							.getSingleResult().toString();

		return conteo != null ? new Integer(conteo) : 0;
	}

	@Override
	public TipoTarifarioDto consultarTarifaNoNormativa() {
		return em
				.createNamedQuery("TipoTarifario.findTarifarioNoNormativo",
						TipoTarifarioDto.class)
				.setParameter("codigoTarifa",
						CommonConstants.COD_TARIFARIO_NO_NORMATIVO)
				.getSingleResult();
	}

	@Override
	public PrestadorDto consultarPrestador(Long prestadorId) {
		return em
				.createNamedQuery("Prestador.findDtoByPrestadorId",
						PrestadorDto.class)
				.setParameter("prestadorId", prestadorId).getSingleResult();
	}

	@Override
	public List<ZonaCapitaDto> listarZonasCapita() {

		return em.createNamedQuery("ZonaCapita.findAll", ZonaCapitaDto.class)
				.getResultList();

	}

	@Override
	public List<RegimenAfiliacionDto> listarRegimenes() {

		return em.createNamedQuery("RegimenAfiliacion.findAll",
				RegimenAfiliacionDto.class).getResultList();

	}

	@Override
	public List<Integer> listarAniosCapita() {

		return em
				.createNamedQuery(Upc.OBTENER_ANIOS_DISPONIBLES, Integer.class)
				.getResultList();

	}

	@Override
	public List<RegionalDto> listarRegionales(){
    	return em.createNamedQuery("Regional.findRegionales", RegionalDto.class).getResultList();
    }

	@Override
	public List<ZonaMunicipioDto> listarZonas(Integer regionalId){
		 return em.createNamedQuery("ZonaMunicipio.findZonasPorRegional",
				 ZonaMunicipioDto.class).setParameter("regionalId", regionalId)
	                .getResultList();
	}

	@Override
	public List<MunicipioDto> listarZonasCobertura(){
		return em.createNamedQuery("ZonaMunicipio.findZonasCobertura",
				 MunicipioDto.class).getResultList();
	}

    @Override
    public List<DepartamentoDto> listarDepartamentosPorRegional(Integer regionalId) {
        return em.createNamedQuery("Departamento.findByRegionalId",
                DepartamentoDto.class).setParameter("regionalId", regionalId)
                .getResultList();
    }

    @Override
    public List<MunicipioDto> listarMunicipiosPorDepartamento(Integer departamentoId) {
    	return em.createNamedQuery("Municipio.listarByDepartamentoId",
    			MunicipioDto.class).setParameter("idDepartamento", departamentoId)
                .getResultList();
    }

	@Override
	public List<SedePrestadorDto> getSedesPrestadorByNegociacion(NegociacionDto negociacion) {
		return em
				.createNamedQuery("SedePrestador.findByNegociacionId",
						SedePrestadorDto.class)
				.setParameter("negociacionId", negociacion.getId())
				.getResultList();
	}

	@Override
	public TipoTarifarioDto consultarTarifaByDescripcion(String descripcionTarifa) {
		if (Objects.nonNull(descripcionTarifa)){
			return em.createNamedQuery("TipoTarifario.findTarifarioByDescripion",TipoTarifarioDto.class)
				.setParameter("descripcionTarifa", descripcionTarifa.toUpperCase())
				.getSingleResult();
		}
		return new TipoTarifarioDto();
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
	public List<DepartamentoDto> listarDepartamentos() {
		return em.createNamedQuery("Departamento.findAll", DepartamentoDto.class).getResultList();
	}

	@Override
	public List<MunicipioDto> listarMunicipiosPorDepartameto(final Integer idDepartamento) {
        return em.createNamedQuery("Municipio.listarByDepartamentoId", MunicipioDto.class)
                .setParameter("idDepartamento", idDepartamento)
                .getResultList();
    }

	@Override
    public List<MunicipioDto> listarMunicipiosPorDepartametoZona(Integer idDepartamento) {
        return em.createNamedQuery("Municipio.listarByDepartamentoZona", MunicipioDto.class)
                .setParameter("idDepartamento", idDepartamento)
                .getResultList();
    }

	@Override
    public List<RangoPoblacionDto> listarRangoPorRia(List<Integer> riasIds){
		List<RangoPoblacionDto> listRango = new ArrayList<>();
		listRango = em.createNamedQuery("RiaContendio.findRangoPorRuta", RangoPoblacionDto.class)
				.setParameter("riaId", riasIds)
				.getResultList();

		return listRango;
	}

	@Override
	public List<CapituloProcedimientoDto> listarCapitulos() {

		return em.createNamedQuery("CapituloProcedimiento.consultarCapitulos", CapituloProcedimientoDto.class)
                .getResultList();

    }

	@Override
	public List<CategoriaProcedimientoDto> listarCategoriasPorCapitulo(final List<CapituloProcedimientoDto>  capitulos) {
		List<CategoriaProcedimientoDto> listCategorias = new ArrayList<>();
		List<Long> capituloIds = new ArrayList<>();

		for(CapituloProcedimientoDto cap : capitulos){
			capituloIds.add(cap.getId());
		}

		listCategorias = em.createNamedQuery("CategoriaProcedimiento.consultarCategoriasPorCapitulo", CategoriaProcedimientoDto.class)
                .setParameter("capituloProcedimientoIds", capituloIds)
                .getResultList();

        return listCategorias;
    }

	@Override
	public List<PrestadorDto> getPrestadorByNombre(String nombrePrestador) {
		if(Objects.nonNull(nombrePrestador)){
			return em.createNamedQuery("Prestador.findByNombre",PrestadorDto.class)
				.setParameter("nombre", "%"+nombrePrestador.toLowerCase()+"%")
				.getResultList();
		}else{
			return new ArrayList<PrestadorDto>();
		}
	}

	@Override
	public List<ReferenteDto> listarReferentePgp() {
		return em.createNamedQuery("Referente.getListaPorModalidad")
				.setParameter("modalidadNegociacionId", NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.getId())
				.getResultList();
	}

	@Override
	public ReferenteDto buscarReferenteId (Long referenteId) {
			return em.createNamedQuery("Referente.findAllId",ReferenteDto.class)
				.setParameter("referenteId", referenteId)
				.getSingleResult();
	}

	@Override
	public List<String> modalidadesReferente (Long referenteId) {
		List<String> modalidades = new ArrayList<>();
		modalidades = em.createNamedQuery("ReferenteModalidad.modalidadesReferente",String.class)
			.setParameter("referenteId", referenteId)
			.getResultList();

		return modalidades;
	}

	@Override
	public ReferenteUbicacionDto buscarReferenteUbicacion (Long referenteId) {
		return em.createNamedQuery("ReferenteUbicacion.buscarUbicacionReferente",ReferenteUbicacionDto.class)
			.setParameter("referenteId", referenteId)
			.getSingleResult();
	}

	@Override
	public List<ReferentePrestadorDto> buscarSedesReferente (Long referenteId) {
		return em.createNamedQuery("ReferentePrestador.buscarReferentePrestador",ReferentePrestadorDto.class)
			.setParameter("referenteId", referenteId)
			.getResultList();
	}

	@Override
	public Integer poblacionTotalReferente(ReferenteDto referente, ReferenteUbicacionDto referenteUbicacion,List<ReferentePrestadorDto> referentePrestador){
		StringBuilder query = new StringBuilder();
		Map<String, Object> parameters = new HashMap<>();
		query.append("SELECT coalesce(ROUND(AVG(mp.poblacion)),0) as promedioPoblacion FROM  contratacion.contrato c "
				+ "JOIN maestros.regional r ON c.regional_id = r.id "
				+ "JOIN maestros.departamento d ON d.regional_id = r.id "
				+ "JOIN maestros.municipio m ON m.departamento_id = d.id "
				+ "JOIN maestros.municipio_poblacion mp ON mp.municipio_id = m.id "
				+ "JOIN maestros.mes ms ON mp.mes_id = ms.id "
				+ "LEFT JOIN maestros.zona_municipio zm ON m.zona_municipio_id = zm.id "
				+ "WHERE mp.regimen_id = :regimenId ");
		parameters.put("regimenId", referente.getRegimen().getId());
		if(referente.getFiltroReferente().equals(FiltroReferentePgpEnum.POR_UBICACION)){
			query.append("AND r.id = :regionalId ");
			parameters.put("regionalId", referenteUbicacion.getRegional().getId());
			if(Objects.nonNull(referenteUbicacion.getZonaMunicipio())){
				query.append("AND zm.id = :zonaId ");
				parameters.put("zonaId", referenteUbicacion.getZonaMunicipio().getId());
			}
			if(Objects.nonNull(referenteUbicacion.getDepartamento())){
				query.append("AND d.id = :departamentoId ");
				parameters.put("departamentoId", referenteUbicacion.getDepartamento().getId());
			}
			if(Objects.nonNull(referenteUbicacion.getMunicipio())){
				query.append("AND m.id = :municipioId ");
				parameters.put("municipioId", referenteUbicacion.getMunicipio().getId());
			}
		}
		else{
			query.append("AND m.id in (:municipioIds) ");
			List<Integer> listMunicipio = new ArrayList<>();
			for(ReferentePrestadorDto rp : referentePrestador){
				listMunicipio.add(rp.getSedePrestador().getMunicipio().getId());
			}
			parameters.put("municipioIds", listMunicipio);
		}
		query.append("AND (cast(CONCAT(ano,'-',mes_id,'-01') as date)) BETWEEN :fechaInicio AND :fechaFin ");
		parameters.put("fechaInicio", referente.getFechaInicio());
		parameters.put("fechaFin", referente.getFechaFin());

		Query queryFinal = this.em.createNativeQuery(query.toString());
		for (Entry<String, Object> llaveValor : parameters.entrySet()) {
			queryFinal.setParameter(llaveValor.getKey(), llaveValor.getValue());
		}

		return ((Number)queryFinal.getSingleResult()).intValue();
	}

	@Override
	public List<RiaDto> consultarTotalRiasByNegociacion(Long negociacionId) {
		// Consultar Tota ruta por rango poblacional
		List<RiaDto> listTotalesPorRango =
				em.createNativeQuery("SELECT r.id, (r.descripcion||' - '||rp.descripcion) as descripcion,  "
						+ "					round((coalesce(sum(valor.valor_negociado),0) + coalesce(sum(valor_med.valor_negociado),0))/n.poblacion) valor ,"
						+ "					round((coalesce(sum(valor.valor_negociado),0) + coalesce(sum(valor_med.valor_negociado),0))) valor_total"
						+ "			FROM contratacion.negociacion_ria nr"
						+ "			JOIN contratacion.negociacion n on n.id = nr.negociacion_id"
						+ "			JOIN maestros.ria r on r.id = nr.ria_id"
						+ "			JOIN maestros.ria_contenido rc on rc.ria_id = nr.ria_id"
						+ "			JOIN maestros.rango_poblacion rp on rp.id = rc.rango_poblacion_id"
						+ "			LEFT JOIN (SELECT snp.procedimiento_id , snp.valor_negociado"
						+ "						FROM contratacion.sedes_negociacion sn"
						+ "						JOIN contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id"
						+ "						JOIN contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id"
						+ "						JOIN contratacion.negociacion n on n.id = sn.negociacion_id"
						+ "						JOIN contratacion.referente_servicio rs on rs.referente_id = n.referente_id and rs.servicio_salud_id = sns.servicio_id"
						+ "						JOIN contratacion.referente_procedimiento_servicio rps on rps.referente_servicio_id = rs.id and snp.procedimiento_id = rps.procedimiento_servicio_id"
						+ "						WHERE sn.negociacion_id = :negociacionId"
						+ "					GROUP BY snp.procedimiento_id, snp.valor_negociado) valor on valor.procedimiento_id = rc.procedimiento_servicio_id"
						+ "			LEFT JOIN (SELECT snm.medicamento_id , snm.valor_negociado"
						+ "						FROM contratacion.sedes_negociacion sn"
						+ "						JOIN contratacion.sede_negociacion_medicamento snm on snm.sede_negociacion_id = sn.id"
						+ "						JOIN contratacion.negociacion n on n.id = sn.negociacion_id"
						+ "						JOIN contratacion.referente_medicamento rm on rm.referente_id = n.referente_id and rm.medicamento_id = snm.medicamento_id"
						+ "						WHERE sn.negociacion_id = :negociacionId"
						+ "					GROUP BY snm.medicamento_id , snm.valor_negociado) valor_med on valor_med.medicamento_id = rc.medicamento_id"
						+ "			WHERE nr.negociacion_id = :negociacionId  "
						+ "			GROUP BY r.id, r.descripcion,rp.id, rp.descripcion, n.poblacion"
						+ "			ORDER by rp.id","NegociacionRia.TotalPorRiaMapping")
				.setParameter("negociacionId", negociacionId)
				.getResultList();
		if(Objects.nonNull(listTotalesPorRango)){
			List<RiaDto> listTotalesPorRia = em.createNativeQuery(
					"SELECT r.id, (r.descripcion ||' - Total ')descripcion   , nr.valor, nr.valor_total "
					+ " FROM contratacion.negociacion_ria nr "
					+ " JOIN maestros.ria r on r.id = nr.ria_id "
					+ " WHERE nr.negociacion_id = :negociacionId "
					+ " GROUP BY r.id, r.descripcion, nr.valor, nr.valor_total"
					+ " ORDER BY r.id","NegociacionRia.TotalPorRiaMapping")
					.setParameter("negociacionId", negociacionId)
					.getResultList();
			listTotalesPorRango.addAll(listTotalesPorRia);
		}
		return listTotalesPorRango;
	}

	@Override
	public List<GrupoEtnicoDto> listarGruposEtnicos() {
		return em.createNamedQuery("GrupoEtnico.findAll", GrupoEtnicoDto.class).getResultList();
	}
}
