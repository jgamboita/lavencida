package co.conexia.negociacion.services.negociacion.boundary;


import co.conexia.negociacion.services.negociacion.control.*;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.model.contratacion.negociacion.Negociacion;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacion;
import com.conexia.contratacion.commons.dto.PaquetePortafolioDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.*;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.contratacion.commons.dto.referente.ReferentePrestadorDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.negociacion.definitions.negociacion.NegociacionViewServiceRemote;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


/**
 * Boundary de la negociacion para los servicios de consulta
 *
 * @author jjoya
 */
@Stateless
@Remote(NegociacionViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class NegociacionViewBoundary implements NegociacionViewServiceRemote {

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;

	@Inject
	private NegociacionControl negociacionControl;

	@Inject
	private SedePrestadorControl sedePrestadorControl;

	@Inject
	private NegociacionGenerarReportesAnexosControl capitaGrEtareoControl;

    @Inject
    private EliminarNegociacionControl eliminarNegociacionControl;
	
	@Inject
	private TecnologiaNegociacionControl tecnologiaNegociacionControl;

	@Inject
	private Log log;

    @Override
    public TecnologiasNegociacionDto consultarTecnologiasNoNegociadas(Long prestadorId, ComplejidadNegociacionEnum complejidad, NegociacionModalidadEnum modalidad) {
        TecnologiasNegociacionDto tecnologias = new TecnologiasNegociacionDto();
        if (modalidad == NegociacionModalidadEnum.EVENTO) {
            tecnologias.setServiciosSalud(this.negociacionControl.consultarServiciosNoNegociacidosByPrestadorEvento(prestadorId,this.negociacionControl.generarComplejidadesByNegociacionComplejidad(complejidad)));
            tecnologias.setMedicamentos(this.negociacionControl.consultarMedicamentosNoNegociacidosByPrestadorEvento(prestadorId));
            tecnologias.setPaquete(this.negociacionControl.consultarPaquetesNoNegociacidosByPrestadorEvento(prestadorId));
            tecnologias.setTraslados(this.negociacionControl.consultarTrasladosNoNegociadosByPrestadorEvento(prestadorId));
        } else if (modalidad == NegociacionModalidadEnum.CAPITA) {
            tecnologias.setServiciosSalud(this.negociacionControl.consultarServiciosNoNegociacidosByPrestadorAndModalidad(prestadorId, modalidad));
            tecnologias.setMedicamentos(this.negociacionControl.consultarMedicamentosNoNegociacidosByPrestadorAndModalidad(prestadorId, modalidad));
            tecnologias.setPaquete(new ArrayList<>());
            tecnologias.setTraslados(new ArrayList<>());
        }
        return tecnologias;
    }

    @Override
    public List<SedePrestadorDto> consultarSedesPrestadorPuedenNegociar(PrestadorDto prestador) {
        if (Objects.isNull(prestador.getId()) && Objects.isNull(prestador.getNumeroDocumento())) {
            throw new IllegalArgumentException("El id del prestador no puede ser nulo, al igual que el numero de documento");
        }
        return new ArrayList<>(this.sedePrestadorControl.obtenerSedesPorPrestador(prestador));
    }

    @Override
    public List<ReferentePrestadorDto> consultarSedesVigentesPrestador(String prestadorNumeroDocumento) {
        return this.negociacionControl.consultarSedesVigentesDelPrestador(prestadorNumeroDocumento);
    }

    @Override
    public NegociacionDto consultarNegociacionByIdPGP(Long negociacionId) {

        try {
            NegociacionDto negociacion = em.createNamedQuery(
                    "Negociacion.findDtoByIdPGP", NegociacionDto.class).
                    setParameter("negociacionId", negociacionId)
                    .getSingleResult();

            // Consulta las sedes Negociacion de la negociacion
            negociacion.setSedesNegociacion(this.negociacionControl
                    .consultarSedesNegociacion(negociacionId));
            return negociacion;
        } catch (NonUniqueResultException e) {
            return null;
        }

    }

    @Override
    public NegociacionDto consultarNegociacionById(Long negociacionId) {
        try {
            NegociacionDto negociacion = em.createNamedQuery("Negociacion.findDtoById", NegociacionDto.class)
                    .setParameter("negociacionId", negociacionId)
                    .getSingleResult();
            negociacion.setSedesNegociacion(this.negociacionControl.consultarSedesNegociacion(negociacionId));
            return negociacion;
        } catch (Exception ex) {
            return null;
        }
    }
    
    @Override
    public Long obtenerNegociacionReferenteId(Long negociacionId) {
        try {
            NegociacionDto negociacion = em.createNamedQuery("Negociacion.findDtoById", NegociacionDto.class).
                    setParameter("negociacionId", negociacionId)
                    .getSingleResult();
            negociacion.setSedesNegociacion(this.negociacionControl.consultarSedesNegociacion(negociacionId));            
            Long negociacionReferenteId = Objects.nonNull(negociacion) && Objects.nonNull(negociacion.getNegociacionReferenteDto()) ? 
                negociacion.getNegociacionReferenteDto().getNegociacionReferenteId() : null;            
            return negociacionReferenteId;
        } catch (Exception ex) {
            return null;
        }
    }
    
    /**
     * Consulta las sedes de una negociacion por id
     *
     * @param negociacionId identificador de la negociacion
     * @return {@link - List<SedesNegociacionDto>}
     */
    public List<SedesNegociacionDto> consultarSedeNegociacionByNegociacionId(Long negociacionId) {
        List<SedesNegociacionDto> sedes = em.createNamedQuery(
                "SedesNegociacion.findSedePrestadorDtoByNegociacionId", SedesNegociacionDto.class).
                setParameter("negociacionId", negociacionId)
                .getResultList();
        return sedes;
    }

    /**
     * Consulta una invitacion a negociar asociada a una negociacion ya creada
     *
     * @param negociacionId
     * @return {@link - InvitacionNegociacionDto}
     */
    public InvitacionNegociacionDto consultarInvitacionNegociacionByNegociacionId(Long negociacionId) {
        InvitacionNegociacionDto invitacionNegociacion = null;

        try {
            invitacionNegociacion = em
                    .createNamedQuery("InvitacionNegociacion.findDtoByNegociacionId", InvitacionNegociacionDto.class)
                    .setParameter("negociacionId", negociacionId)
                    .getSingleResult();
        } catch (NoResultException e) {
            invitacionNegociacion = new InvitacionNegociacionDto();
        }

        return invitacionNegociacion;
    }

    @Override
    public List<IncentivoModeloDto> consultarIncentivosByNegociacionId(Long negociacionId) {
        return em
                .createNamedQuery("Incentivo.findByNegociacionId",
                        IncentivoModeloDto.class)
                .setParameter("negociacionId", negociacionId).getResultList();
    }

    @Override
    public List<IncentivoModeloDto> consultarModelosByNegociacionId(Long negociacionId) {
        return em
                .createNamedQuery("Modelo.findByNegociacionId",
                        IncentivoModeloDto.class)
                .setParameter("negociacionId", negociacionId).getResultList();
    }

    @Override
    public Long countTotalTecnologiasNegociacion(Long negociacionId) {
        try {
            return ((BigDecimal) em.createNamedQuery("Negociacion.countTotalTecnologiasNegociacion")
                    .setParameter("negociacionId", negociacionId)
                    .getSingleResult()).longValue();
        } catch (Exception e) {
            this.log.error("Se presento un error al contar las tecnologias", e);
            throw e;
        }
    }

    @Override
    public Long countTotalTecnologiasNegociacionPGP(Long negociacionId) {
        String total = em.createNamedQuery("Negociacion.countTotalTecnologiasNegociacionPGP")
                .setParameter("negociacionId", negociacionId).getSingleResult().toString();
        return total != null ? new Long(total) : 0;
    }

    @Override
    public Long countTecnologiaNoNegociadas(Long negociacionId) {
        try {
            return ((BigDecimal) em
                    .createNamedQuery(
                            "Negociacion.countTotalTecnologiasNegociacionNoNegociadasCapita")
                    .setParameter("negociacionId", negociacionId).getSingleResult()).longValue();
        } catch (Exception e) {
            this.log.error("Se presento un error al contar las tecnologias no negociadas ", e);
            throw e;
        }
    }

    @Override
    public BigDecimal sumValorTotal(Long negociacionId) {
        return ((BigDecimal) em.createNamedQuery("Negociacion.sumValorTotal")
                .setParameter("negociacionId", negociacionId).getSingleResult());
    }

    @Override
    public BigDecimal sumValorTotalPGP(Long negociacionId) {
        return ((BigDecimal) em.createNamedQuery("Negociacion.sumValorTotalPGP")
                .setParameter("negociacionId", negociacionId).getSingleResult());
    }

    @Override
    public List<SolicitudContratacionParametrizableDto> findSolicitudesContratacionByNegociacionIdAndEstado(Long negociacionId, EstadoLegalizacionEnum estado) {
        return em.createNamedQuery("SolicitudContratacion.findByNegociacionIdAndEstado", SolicitudContratacionParametrizableDto.class)
                .setParameter("negociacionId", negociacionId)
                .setParameter("estadoLegalizacion", estado).getResultList();
    }

    @Override
    public Long countMunicipiosAreaCoberturaPorSedeNegociacion(Long sedeNegociacionId) {
        try {
            return (Long) em.createNamedQuery("AreaCoberturaSedes.countMunicipiosSeleccionadosCoberturaBySedeNegociacionId")
                    .setParameter("sedesNegociacionId", sedeNegociacionId)
                    .getSingleResult();
        } catch (Exception e) {
	        this.log.error("Se presento un error al contar los municipios por negociacion sede", e);
            throw e;
        }
    }

    /**
     * Consulta el porcentaje total de pyp / recuperacion de los servicios
     *
     * @param negociacionId
     */
    public BigDecimal sumPorcentajeTotalTemaServiciosPyp(Long negociacionId) {
        return ((BigDecimal) em.createNamedQuery("Negociacion.sumPorcentajeTotalTemaServicios")
                .setParameter("negociacionId", negociacionId)
                .setParameter("temaCapita", TemasCapitaEnum.PYP.getId()).getSingleResult());
    }

    @Override
    public BigDecimal sumPorcentajeTotalTemaServiciosRecuperacion(Long negociacionId) {
        return ((BigDecimal) em.createNamedQuery("Negociacion.sumPorcentajeTotalTemaServicios")
                .setParameter("negociacionId", negociacionId)
                .setParameter("temaCapita", TemasCapitaEnum.RECUPERACION.getId()).getSingleResult());
    }

    public BigDecimal sumPorcentajeTotalTemaMedicamentosRecuperacion(Long negociacionId) {
        List<Integer> idsRec = new ArrayList<>();
        idsRec.add(MacroCategoriaMedicamentoEnum.HOSPITALARIO.getId());
        idsRec.add(MacroCategoriaMedicamentoEnum.AMBULATORIO.getId());
        idsRec.add(MacroCategoriaMedicamentoEnum.URGENCIAS.getId());
        idsRec.add(MacroCategoriaMedicamentoEnum.P_Y_P.getId());
        idsRec.add(MacroCategoriaMedicamentoEnum.OXIGENO.getId());
        idsRec.add(MacroCategoriaMedicamentoEnum.POS_HOSPITALARIO.getId());
        return ((BigDecimal) em.createNamedQuery("Negociacion.sumPorcentajeTotalTemaMedicamentos")
                .setParameter("negociacionId", negociacionId)
                .setParameter("macroCategoriaMedicamento", idsRec).getSingleResult());
    }

    @Override
    public PrestadorDto consultarNegociacionAClonar(Long negociacionBaseId, NegociacionDto nuevaNegociacion) throws ConexiaBusinessException {
        this.negociacionControl.validarCondicionesNegociacionBase(negociacionBaseId, nuevaNegociacion);
        return this.negociacionControl.consultarPrestadorByNegociacion(negociacionBaseId);
    }

    @Override
    public NegociacionDto consultarRias(NegociacionDto negociacion) {
        // consulta Los rias asociados a la negociacion
        List<NegociacionRiaDto> listaNegociacionRiaDto = em.createNamedQuery("NegociacionRia.findRiasByNegociacionId")
                .setParameter("negociacionId", negociacion.getId()).getResultList();
        if (Objects.nonNull(listaNegociacionRiaDto) && !listaNegociacionRiaDto.isEmpty()) {
            negociacion.setListaNegociacionRiaDto(listaNegociacionRiaDto);
        } else {
            negociacion.setListaNegociacionRiaDto(new ArrayList<>());
        }
        // consulta Los rias asociados a la negociacion
        List<RangoPoblacionDto> listRangoPoblacionDto = em.createNamedQuery("NegociacionRangoPoblacion.findRangoPByNegociacionId")
                .setParameter("negociacionId", negociacion.getId()).getResultList();
        if (Objects.nonNull(listRangoPoblacionDto) && !listRangoPoblacionDto.isEmpty()) {
            negociacion.setNegociacionRangoPoblacionDto(new NegociacionRangoPoblacionDto(negociacion.getId(), listRangoPoblacionDto));
        } else {
            negociacion.setNegociacionRangoPoblacionDto(new NegociacionRangoPoblacionDto());
        }
        return negociacion;
    }

    @Override
    public NegociacionDto consultarCapitaPorRias(NegociacionDto negociacion) {
        // consulta Los rias asociados a la negociacion
        Negociacion negociacionEntidad = em.find(Negociacion.class, negociacion.getId());
        negociacion.setPoblacion(negociacionEntidad.getPoblacion());
        List<NegociacionRiaRangoPoblacionDto> listaNegociacionRiaRangoPoblacion
                = em.createNamedQuery("NegociacionRiaRangoPoblacion.findRiasByNegociacionId")
                        .setParameter("negociacionId", negociacion.getId())
                        .getResultList();

        if (NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(negociacion.getTipoModalidadNegociacion())) {

            for (NegociacionRiaRangoPoblacionDto negociacionRiaRangoPoblacionDto : listaNegociacionRiaRangoPoblacion) {
                String queryString = "SELECT gr.id, gr.descripcion, gr.genero, " +
                        "ROUND(upc.valor_mensual_upc, 2) upc, ROUND(upc.valor_mensual_upc_x_poblacion, 2) upc_mensual, " +
                        "upc.poblacion_grupo_etario poblacion " +
                        "FROM contratacion.grupo_etario gr " +
                        "LEFT JOIN ( " +
                        "	SELECT ge.id, ge.descripcion, upc.valor_mensual_upc, " +
                        "   valor_mensual_upc_x_poblacion, upc.poblacion_grupo_etario, upc.id_ria_rango_poblacion " +
                        "   FROM  contratacion.determinate_upc_gp_etario(:negociacionId , :municipioId, :regimenId , :fechaCorte) upc" +
                        "   RIGHT JOIN contratacion.grupo_etario ge ON ge.id = upc.id_grupo_etario " +
                        "   WHERE id_ria_rango_poblacion = :riaRangoPoblacionId " +
                        ") upc ON upc.id = gr.id " +
                        "ORDER BY gr.id ";
                List<GrupoEtarioDto> gruposEtariosRia = em.createNativeQuery(queryString, "GrupoEtarioMapping")
                        .setParameter("negociacionId", negociacion.getId())
                        .setParameter("municipioId", negociacion.getMunicipioDto().getId())
                        .setParameter("regimenId", negociacion.getRegimen().getId())
                        .setParameter("fechaCorte", negociacion.getFechaCorte())
                        .setParameter("riaRangoPoblacionId", negociacionRiaRangoPoblacionDto.getId())
                        .getResultList();
                negociacionRiaRangoPoblacionDto.setGruposEtarios(gruposEtariosRia);
            }
        }

        List<NegociacionRiaDto> listNegociacionRiaDto = listaNegociacionRiaRangoPoblacion.stream()
                .filter(obj -> obj.getNegociacionRia().getNegociado())
                .map(NegociacionRiaRangoPoblacionDto::getNegociacionRia)
                .distinct().collect(Collectors.toList());

        if (!listNegociacionRiaDto.isEmpty()) {
            for (NegociacionRiaDto negociacionRia : listNegociacionRiaDto) {
                negociacionRia.setListaNegociacionRiaRangoPobl(listaNegociacionRiaRangoPoblacion.stream()
                        .filter(obj -> obj.getNegociacionRia().equals(negociacionRia))
                        .collect(Collectors.toList()));
                negociacionRia.getListaNegociacionRiaRangoPobl().forEach(
                        obj -> obj.setNegociacionRia(negociacionRia)
                );
            }
            negociacion.setListaNegociacionRiaDto(listNegociacionRiaDto);
        } else {
            negociacion.setListaNegociacionRiaDto(new ArrayList<>());
        }
        return negociacion;
    }

    @Override
    public List<NegociacionRiaDto> consultarRiasDisponibles(NegociacionDto negociacion) {
        return (List<NegociacionRiaDto>) em.createNamedQuery("NegociacionRia.findRiasByNegociacionId")
                .setParameter("negociacionId", negociacion.getId()).getResultList();
    }

    @Override
    public List<NegociacionRiaActividadMetaDto> consultarActividades(NegociacionRiaRangoPoblacionDto riaRangoPoblacion) {
        return (List<NegociacionRiaActividadMetaDto>) em.createNamedQuery("NegociacionRiaActividadMeta.findByRiaRangoPoblacion")
                .setParameter("idNegociacionRiaRangoPoblacion", riaRangoPoblacion.getId())
                .getResultList();
    }

    @Override
    public List<ReferenteDto> getListaReferenteCapitaPorRias() {
        return (List<ReferenteDto>) em.createNamedQuery("Referente.getListaPorModalidad")
                .setParameter("modalidadNegociacionId", NegociacionModalidadEnum.RIAS_CAPITA.getId())
                .setParameter("esRia", true)
                .getResultList();
    }

    @Override
    public List<ReferenteDto> getListaReferentePGP(NegociacionDto negociacion) {
        List<RegimenNegociacionEnum> regimenNegociacion = new ArrayList<>();
		regimenNegociacion.add(RegimenNegociacionEnum.AMBOS);
		regimenNegociacion.add(RegimenNegociacionEnum.SUBSIDIADO);
		regimenNegociacion.add(RegimenNegociacionEnum.CONTRIBUTIVO);


        return (List<ReferenteDto>) em.createNamedQuery("Referente.getListaPorModalidadYRegimen")
                .setParameter("modalidadNegociacionId", NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO.getId())
                .setParameter("esRia", negociacion.getEsRia())
                .setParameter("regimenNegociacion", regimenNegociacion)
                .setParameter("estadoReferente", EstadoReferentePgpEnum.FINALIZADO)
                .getResultList();
	}

	@Override
	public String generarArchivoCapitaGrupoEtareo(ReportesAnexosNegociacionEnum xls, NegociacionDto negociacion) throws IOException {
		return capitaGrEtareoControl.generarDescargaXls(xls, negociacion);
	}

	@Override
	public Boolean validarPoblacionXMunicipioPgp(Long negociacionId, Integer municipioId) {
		boolean response = false;
		try {
			Long result = em.createNamedQuery("NegociacionMunicipio.validarPoblacionXMunicipioPgp", Long.class)
					.setParameter("negociacionId", negociacionId)
					.setParameter("municipioId", municipioId)
					.getSingleResult();

			response = Objects.nonNull(result) && result > 0;
		} catch (NoResultException e) {
		    this.log.error("No se encontraron municipios para PGP", e);
		}
		return response;
	}
    

    @Override
    public Boolean validarLegalizacionPreliminar(Long negociacionId) {
        StringBuilder select = new StringBuilder();

        select.append("select count(estado_legalizacion_id)")
                .append("from contratacion.solicitud_contratacion ")
                .append("where negociacion_id = :negociacionId ")
                .append("and estado_legalizacion_id = '" + EstadoLegalizacionEnum.LEGALIZACION_PRELIMINAR + "'");

        return ((Number) em.createNativeQuery(select.toString())
                .setParameter("negociacionId", negociacionId)
                .getSingleResult()).intValue() > 0;
    }

    @Override
    public List<NegociacionMunicipioDto> obtenerMunicipiosNegociacion(long negociacionId) {
        return em.createNamedQuery("NegociacionMunicipio.municipiosPorNegociacionPGPSinConteo", NegociacionMunicipioDto.class)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    @Override
    public List<ReglaNegociacionDto> obtenerReglasNegociacion(long negociacionId) {
        return (List<ReglaNegociacionDto>) em.createNamedQuery("ReglaNegociacion.listarReglasByNegociacionId")
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    @Override
    public List<ProcedimientoDto> consultarProcedimientosSinFranja(Long negociacionId) {
        return em.createNamedQuery("SedeNegociacionProcedimiento.consultarProcedimientosSinFranjaPGP", ProcedimientoDto.class)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    @Override
    public List<MedicamentosDto> consultarMedicamentosSinFranja(Long negociacionId) {
        return em.createNamedQuery("SedeNegociacionMedicamento.consultarMedicamentosSinFranjaPGP", MedicamentosDto.class)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    @Override
    public Long consultarReferenteNegociacion(Long negociacionId) throws NoResultException {
        String query = "";
        try {
            query = em.createNamedQuery("Negociacion.consultarReferenteNegociacion")
                    .setParameter("negociacionId", negociacionId)
                    .getSingleResult().toString();
        } catch (NoResultException | NullPointerException e) {
            this.log.error("No se encontro referente para la negociacion ", e);
        }
        return query != null && !query.isEmpty() ? new Long(query) : 0;
    }

    @Override
    public List<SedePrestadorDto> getSedesSinPoblacionPGP(Long negociacionId) {
        List<SedePrestadorDto> sedes = new ArrayList<>();
        try {
            sedes = em.createNamedQuery("Negociacion.getSedesSinPoblacionPGP", SedePrestadorDto.class)
                    .setParameter("negociacionId", negociacionId)
                    .getResultList();
        } catch (NoResultException e) {
            this.log.error("No se encontro sedes sin poblacion", e);
        }
        return sedes.size() > 0 ? sedes : null;
    }

    @Override
    public List<MunicipioDto> obtenerMunicipiosAreaCobertura(Long negociacionId) {
        List<MunicipioDto> municipios = new ArrayList<>();
        try {
            municipios = em.createNativeQuery(this.negociacionControl.generarConsultarMunicipiosCobertura(null),
                    "Negociacion.municipiosAreaCoberturaMapping")
                    .setParameter("negociacionId", negociacionId)
                    .getResultList();
        } catch (NoResultException e) {
            this.log.error("No se encontraron municipios", e);
        }
        return municipios.size() > 0 ? municipios : null;
    }

    @Override
    public List<MunicipioDto> obtenerMunicipiosAreaCobertura(Long negociacionId, List<Long> sedeNegociacionId) {
        List<MunicipioDto> municipios = new ArrayList<>();
        try {
            Query query = em.createNativeQuery(this.negociacionControl.generarConsultarMunicipiosCobertura(sedeNegociacionId),"Negociacion.municipiosAreaCoberturaMapping")
                    .setParameter("negociacionId", negociacionId);

            if (Objects.nonNull(sedeNegociacionId) && sedeNegociacionId.size() > 0) {
                query.setParameter("sedeNegociacionId", sedeNegociacionId);
            }
            municipios = query.getResultList();
        } catch (NoResultException e) {
            this.log.error("No se encontraron municipios", e);
        }
        return municipios.size() > 0 ? municipios : null;
    }

    @Override
    public List<AnexoTarifarioPoblacionDto> obtenerPoblacionPgpAnexo(Long negociacionId) {
        return this.negociacionControl.consultarPoblacionNegociacionPgp(negociacionId);
    }

    @Override
    public List<MedicamentosDto> consultarMedicamentosSinValorPgp(Long negociacionId) {
        return em.createNamedQuery("SedeNegociacionMedicamento.consultarMedicamentosSinValorPgp")
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    @Override
    public List<NegociacionConsultaContratoDto> consultarContratos(FiltroBandejaConsultaContratoDto dto) {
        return this.eliminarNegociacionControl.consultarContratos(dto);
    }

    @Override
    public List<NegociacionDto> consultarNegociaciones(Long idNegociacionReferente) {
        if (Objects.isNull(idNegociacionReferente)){
            throw new IllegalArgumentException("La identificacion de la negociacion esta vacia.");
        }
        return this.negociacionControl.consultarNegociaciones(idNegociacionReferente);
    }

    @Override
    public List<NegociacionDto> consultarNegociaciones(String numeroContrato) {
        if (Objects.isNull(numeroContrato)){
            throw new IllegalArgumentException("El numero de contrato no puede ser vacio.");
        }
        return this.negociacionControl.consultarNegociaciones(numeroContrato);
    }

    @Override
    public boolean existeLegalizaciones(NegociacionDto negociacion) {
        if (Objects.isNull(negociacion.getId())) {
            throw new IllegalArgumentException("Para verificar si existen legalizaciones, la identificación de la negociación no debe estar vacío");
        }
        return negociacionControl.existeLegalizaciones(negociacion);
    }
}
