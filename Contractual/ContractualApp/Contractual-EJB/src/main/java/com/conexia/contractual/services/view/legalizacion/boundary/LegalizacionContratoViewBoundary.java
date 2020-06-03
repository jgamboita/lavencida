package com.conexia.contractual.services.view.legalizacion.boundary;

import com.conexia.contractual.definitions.view.legalizacion.LegalizacionContratoViewRemote;
import com.conexia.contractual.model.contratacion.legalizacion.MinutaDetalle;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.model.contratacion.portafolio.PaquetePortafolio;
import com.conexia.contractual.model.maestros.Medicamento;
import com.conexia.contractual.services.transactional.legalizacion.control.LegalizacionContratoControl;
import com.conexia.contractual.utils.exceptions.ConexiaExceptionUtils;
import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.contractual.legalizacion.*;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.AreaInfluenciaDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.AreaCoberturaDTO;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.IncentivoModeloDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ObjetoContractualDto;
import com.conexia.contratacion.commons.dto.negociacion.SedesNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

@Stateless
@Remote(LegalizacionContratoViewRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class LegalizacionContratoViewBoundary implements LegalizacionContratoViewRemote {

    @PersistenceContext(unitName = "contractualDS")
    EntityManager em;

    @Inject
    private LegalizacionContratoControl legalizacionContratoControl;

    @Inject
    private ConexiaExceptionUtils exceptionUtils;

    private static final int DECIMALES_APROXIMACION = 3;

    @Override
    public List<AreaInfluenciaDto> listarMunicipiosContrato(ContratoDto contrato) {
        return em.createNamedQuery("AreaCoberturaContrato.findByContratoId",
                AreaInfluenciaDto.class)
                .setParameter("contratoId", contrato.getId())
                .getResultList();
    }

    @Override
    public List<ResponsableContratoDto> listarResponsablesContrato(Integer idRegional, TipoResponsableEnum tipoResponsableEnum) {
        return em.createNamedQuery("ResponsableContrato.findResponsable",
                ResponsableContratoDto.class)
                .setParameter("idRegional", idRegional)
                .setParameter("tipoResponsable", tipoResponsableEnum)
                .getResultList();
    }

	@Override
	public LegalizacionContratoDto buscarLegalizacionContrato(SolicitudContratacionParametrizableDto solicitud)
            throws ConexiaBusinessException
    {
		return  legalizacionContratoControl.buscarLegalizacionContrato(solicitud);
	}

    @Override
    public GeneracionMinutaDto generacionMinutaPorId(final Long idLegalizacionContrato, Integer regionalId, final Long negociacionId) throws ConexiaBusinessException {
        try {
        	GeneracionMinutaDto generarMinutaDto = em.createNamedQuery("LegalizacionContrato.generacionMinutaPorId",
                    GeneracionMinutaDto.class)
                    .setParameter("idLegalizacionContrato", idLegalizacionContrato)
                    .setParameter("regionalId", regionalId)
                    .getSingleResult();
        	// Consulta informaci´on de la sede Principal
        	if(Objects.nonNull(negociacionId)){
        		List<SedesNegociacionDto> listSedeNegociacionDto =
        				em.createNamedQuery("SedesNegociacion.findSedePrestadorPrincipalDtoByNegociacionId",
            			SedesNegociacionDto.class)
            			.setParameter("negociacionId", negociacionId)
            			.setFirstResult(0)
            			.getResultList();
        		if(Objects.nonNull(listSedeNegociacionDto) && !listSedeNegociacionDto.isEmpty()){
        			generarMinutaDto.setNombreSede(listSedeNegociacionDto.get(0).getSedePrestador().getNombreSede());
        			generarMinutaDto.setCodigoPrestador((listSedeNegociacionDto.get(0).getSedePrestador().getCodigoHabilitacion() != null ?
        				listSedeNegociacionDto.get(0).getSedePrestador().getCodigoHabilitacion() : listSedeNegociacionDto.get(0).getSedePrestador().getCodigoPrestador())
        				+ listSedeNegociacionDto.get(0).getSedePrestador().getCodigoSede());
        		}else{
        			throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GENERACION_MINUTA);
        		}
        	}
            return generarMinutaDto;
        } catch (final NoResultException e) {
            throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.GENERACION_MINUTA);
        }
    }

    @Override
    public List<IncentivoModeloDto> listarIncentivosPorNegociacionId(final Long negociacionId){
        return em.createNamedQuery("Incentivo.findByNegociacionId",
                IncentivoModeloDto.class)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    @Override
	public NegociacionDto consultarNegociacionById(final Long negociacionId){
		NegociacionDto negociacion = em.createNamedQuery(
				"Negociacion.findDtoById", NegociacionDto.class).
				setParameter("negociacionId", negociacionId)
				.getSingleResult();

		return negociacion;
	}

    public BigDecimal sumPorcentajeTotalTemaServiciosPyp(Long negociacionId){
		return ((BigDecimal) em.createNamedQuery("Negociacion.sumPorcentajeTotalTemaServicios")
				.setParameter("negociacionId", negociacionId)
				.setParameter("temaCapita", TemasCapitaEnum.PYP.getId()).getSingleResult());
	}

    public BigDecimal sumPorcentajeTotalTemaServiciosRecuperacion(Long negociacionId){
		return ((BigDecimal) em.createNamedQuery("Negociacion.sumPorcentajeTotalTemaServicios")
				.setParameter("negociacionId", negociacionId)
				.setParameter("temaCapita", TemasCapitaEnum.RECUPERACION.getId()).getSingleResult());
	}

    public Double sumPorcentajePypRiaCapita(Long negociacionId){
    	return ((Double) em.createNamedQuery("NegociacionRiaRangoPoblacion.sumPorcentajePyp")
    			.setParameter("negociacionId", negociacionId).getSingleResult());
    }

    public Double sumPorcentajeRecuperacionRiaCapita(Long negociacionId){
    	return  ((Double) em.createNamedQuery("NegociacionRiaRangoPoblacion.sumPorcentajeRecuperacion")
    			.setParameter("negociacionId", negociacionId).getSingleResult());
    }

    public BigDecimal sumPorcentajeTotalTemaMedicamentosPyp(Long negociacionId){
    	List<Integer> idsPyp = new ArrayList<>();
    	return ((BigDecimal) em.createNamedQuery("Negociacion.sumPorcentajeTotalTemaMedicamentos")
				.setParameter("negociacionId", negociacionId)
				.setParameter("macroCategoriaMedicamento", idsPyp).getSingleResult());

    }

    public BigDecimal sumPorcentajeTotalTemaMedicamentosRecuperacion(Long negociacionId){
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

    public SecuenciaContratoDto secuenciaPrestador(ContratoDto contrato){
    	SecuenciaContratoDto secuenciaPrestador = em.createNamedQuery("secuenciaContrato.findSecuenciaPrestador", SecuenciaContratoDto.class)
    			.setParameter("codigoRegional", contrato.getRegionalDto().getCodigo())
    			.setParameter("modalidad", contrato.getSolicitudContratacionParametrizableDto().getRegimenNegociacion())
    			.setParameter("regimen", contrato.getSolicitudContratacionParametrizableDto().getRegimenNegociacion())
    			.setParameter("anio", contrato.getFechaInicioVigencia())
    			.setParameter("prestadorId", contrato.getSolicitudContratacionParametrizableDto().getPrestadorDto().getId())
    			.getSingleResult();

    	return secuenciaPrestador;
    }

    public String consultaObservaciones(final Long negociacionId){
    	String negociacion = em.createNamedQuery(
				"Negociacion.findObservaciones", String.class).
				setParameter("negociacionId", negociacionId)
				.getSingleResult();

		return negociacion;
    }

    public String consultaTipoUps(final Long negociacionId){
    	String negociacion = em.createNamedQuery(
				"Negociacion.findTipoUpc", String.class).
				setParameter("negociacionId", negociacionId)
				.getSingleResult();

		return negociacion;
    }

    public BigDecimal consultaValorTotal(final Long negociacionId){
    	BigDecimal negociacion = em.createNamedQuery(
    			"Negociacion.findValorTotal", BigDecimal.class)
    			.setParameter("negociacionId", negociacionId)
    			.getSingleResult();
    	return negociacion;
    }

    @Override
    public Long conteoLegalizaciones(Long negociacionId) {
    	return em.createNamedQuery("Negociacion.contarLegalizacionesNegociacionById", Long.class)
    			.setParameter("negociacionId", negociacionId)
    			.getSingleResult();
    }

    public BigDecimal consultarValorTotalRia(final Long negociacionId){
    	BigDecimal valorTotal = em.createNamedQuery("NegociacionRia.consultarvalorTotal", BigDecimal.class)
    			.setParameter("negociacionId", negociacionId)
    			.getSingleResult();


    	return valorTotal;

    }

    public BigDecimal consultaValorUpcMensual(final Long negociacionId){
    	BigDecimal negociacion = em.createNamedQuery(
    			"Negociacion.findValorUpcMensual", BigDecimal.class)
    			.setParameter("negociacionId", negociacionId)
    			.getSingleResult();
    	return negociacion;
    }

    @Override
    public List<IncentivoModeloDto> listarModelosPorNegociacionId(Long negociacionId) {
        return em.createNamedQuery("Modelo.findByNegociacionId",
                IncentivoModeloDto.class)
                .setParameter("negociacionId", negociacionId)
                .getResultList();
    }

    @Override
    public List<ObjetoContractualDto> listarObjetoRiaCapitaResumidoPorNegociacionId(Long negociacionId){
        return legalizacionContratoControl.listarObjetoRiaCapitaResumidoPorNegociacionId(negociacionId);
    }

	@Override
	public List<ObjetoContractualDto> listarObjetoCapitaPorNegociacionId(Long negociacionId){
		return legalizacionContratoControl.listarObjetoCapitaPorNegociacionId(negociacionId);
	}

    @Override
    public List<ObjetoContractualDto> listarObjetoEventoPorNegociacionId(Long negociacionId) throws ConexiaBusinessException {
    	return legalizacionContratoControl.listarObjetoEventoPorNegociacionId(negociacionId);
    }

	@Override
	public LegalizacionContratoDto buscarLegalizacionByNroNegociacionAndRegimenAndModalidad(Long numeroNegociacion, RegimenNegociacionEnum regimenNegociacionEnum, NegociacionModalidadEnum negociacionModalidadEnum)throws ConexiaBusinessException {
		SolicitudContratacionParametrizableDto solicitud = new SolicitudContratacionParametrizableDto();
		solicitud.setNumeroNegociacion(numeroNegociacion);
		solicitud.setRegimenNegociacion(regimenNegociacionEnum);
		solicitud.setModalidadNegociacionEnum(negociacionModalidadEnum);

		LegalizacionContratoDto legalizacion = em
				.createNamedQuery("LegalizacionContrato.findLegalizacionContratoByNegociacion",LegalizacionContratoDto.class)
				.setParameter("negociacionId", numeroNegociacion)
				.setParameter("regimen", regimenNegociacionEnum)
				.setParameter("tipoModalidad", negociacionModalidadEnum).getSingleResult();

		return buscarLegalizacionDto(legalizacion, solicitud);
	}

	private LegalizacionContratoDto buscarLegalizacionDto(LegalizacionContratoDto legalizacion, SolicitudContratacionParametrizableDto solicitud) throws ConexiaBusinessException {
		try {
			List<DescuentoDto> descuentos = em.createNamedQuery("DescuentoLegalizacionContrato.findByIdLegalizacion", DescuentoDto.class)
					.setParameter("idLegalizacion", legalizacion.getId())
                    .getResultList();

			List<ObjetoContractualDto> objetoEvento = listarObjetoEventoPorNegociacionId(solicitud.getNumeroNegociacion());

			String observaciones = consultaObservaciones(solicitud.getNumeroNegociacion());

			if (NegociacionModalidadEnum.CAPITA.equals(solicitud.getModalidadNegociacionEnum()) ||
					NegociacionModalidadEnum.RIAS_CAPITA.equals(solicitud.getModalidadNegociacionEnum())||
                    NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(solicitud.getModalidadNegociacionEnum())) {
				BigDecimal porcentajeRecuperacionNegociacion = BigDecimal.ZERO;
				BigDecimal porcentajePypNegociacion = BigDecimal.ZERO;
				Integer valPyp = 0;
				Integer valRec = 0;
				// Asigna el objeto capita a la legalización

				legalizacion.setObjetoCapita(listarObjetoCapitaPorNegociacionId(solicitud.getNumeroNegociacion()));
				legalizacion.setObjetoRiaCapitaResumido(this.listarObjetoRiaCapitaResumidoPorNegociacionId(solicitud.getNumeroNegociacion()));
				List<IncentivoModeloDto> incentivos = em
						.createNamedQuery("Incentivo.findByNegociacionId", IncentivoModeloDto.class)
						.setParameter("negociacionId", solicitud.getNumeroNegociacion()).getResultList();

				List<IncentivoModeloDto> modelos = em
						.createNamedQuery("Modelo.findByNegociacionId", IncentivoModeloDto.class)
						.setParameter("negociacionId", solicitud.getNumeroNegociacion()).getResultList();

				String tipoUpc;
				if(NegociacionModalidadEnum.RIAS_CAPITA.equals(solicitud.getModalidadNegociacionEnum())
					|| NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(solicitud.getModalidadNegociacionEnum())
					){
					tipoUpc = "CAPITA POR RIAS";
				}
				else{
					tipoUpc = NegociacionModalidadEnum.CAPITA.equals(solicitud.getModalidadNegociacionEnum()) ?
					this.consultaTipoUps(solicitud.getNumeroNegociacion()) : "";
				}

				BigDecimal valorTotal = this.consultaValorTotal(solicitud.getNumeroNegociacion());

				BigDecimal valorMensualUpc = this.consultaValorUpcMensual(solicitud.getNumeroNegociacion());

				BigDecimal porcentajePypMedicamentos = BigDecimal.valueOf(0);
				// this.sumPorcentajeTotalTemaMedicamentosPyp(solicitud.getNumeroNegociacion());

				BigDecimal porcentajeRecuperacionMedicamentos = this
						.sumPorcentajeTotalTemaMedicamentosRecuperacion(solicitud.getNumeroNegociacion());

				BigDecimal porcentajePypServicios = this
						.sumPorcentajeTotalTemaServiciosPyp(solicitud.getNumeroNegociacion());

				BigDecimal porcentajeRecuperacionServicios = this
						.sumPorcentajeTotalTemaServiciosRecuperacion(solicitud.getNumeroNegociacion());

				//VALORES NEGOCIACION CAPITA POR RIAS
				//Valores Recuperacion
				if(NegociacionModalidadEnum.RIAS_CAPITA.equals(solicitud.getModalidadNegociacionEnum())
					|| NegociacionModalidadEnum.RIAS_CAPITA_GRUPO_ETAREO.equals(solicitud.getModalidadNegociacionEnum())
					){
					BigDecimal porcentajeRiasPyp  =new BigDecimal
							(Objects.isNull(this.sumPorcentajePypRiaCapita(solicitud.getNumeroNegociacion())) ? 0 :this.sumPorcentajePypRiaCapita(solicitud.getNumeroNegociacion()));
					porcentajePypNegociacion = porcentajePypNegociacion.add(porcentajeRiasPyp);
					BigDecimal porcentajeRiasRecuperacion = new BigDecimal
							(Objects.isNull(this.sumPorcentajeRecuperacionRiaCapita(solicitud.getNumeroNegociacion())) ? 0 :this.sumPorcentajeRecuperacionRiaCapita(solicitud.getNumeroNegociacion()));
					porcentajeRecuperacionNegociacion = porcentajeRecuperacionNegociacion.add(porcentajeRiasRecuperacion);
					valRec = 1;
				}
				else if (NegociacionModalidadEnum.CAPITA.equals(solicitud.getModalidadNegociacionEnum())){
					if ((porcentajePypServicios != null) && (porcentajePypMedicamentos != null)) {
						porcentajePypNegociacion = porcentajePypNegociacion.add(porcentajePypServicios);
						porcentajePypNegociacion = porcentajePypNegociacion.add(porcentajePypMedicamentos);
						valPyp = 1;
					}
					if ((porcentajeRecuperacionServicios != null) && (porcentajeRecuperacionMedicamentos != null)) {
						porcentajeRecuperacionNegociacion = porcentajeRecuperacionNegociacion
								.add(porcentajeRecuperacionServicios);
						porcentajeRecuperacionNegociacion = porcentajeRecuperacionNegociacion
								.add(porcentajeRecuperacionMedicamentos);
						valRec = 1;
					}
					if (valPyp == 0) {
						if (porcentajePypServicios != null) {
							porcentajePypNegociacion = porcentajePypNegociacion.add(porcentajePypServicios);
						}
						if (porcentajePypMedicamentos != null) {
							porcentajePypNegociacion = porcentajePypNegociacion.add(porcentajePypMedicamentos);
						}
					}
					if (valRec == 0) {
						if (porcentajeRecuperacionServicios != null) {
							porcentajeRecuperacionNegociacion = porcentajeRecuperacionNegociacion
									.add(porcentajeRecuperacionServicios);
						}
						if (porcentajeRecuperacionMedicamentos != null) {
							porcentajeRecuperacionNegociacion = porcentajeRecuperacionNegociacion
									.add(porcentajeRecuperacionMedicamentos);
						}
					}
				}

				porcentajeRecuperacionNegociacion = Objects.nonNull(porcentajeRecuperacionNegociacion)
						? porcentajeRecuperacionNegociacion.setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP)
						: null;
				porcentajePypNegociacion = Objects.nonNull(porcentajePypNegociacion)
						? porcentajePypNegociacion.setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP) : null;

				legalizacion.setIncentivoModelos(incentivos);
				legalizacion.setModelos(modelos);
				legalizacion.setTipoUpcNegociacion(tipoUpc);
				legalizacion.setValorAnualUpc(valorTotal);
				legalizacion.setPorcentajeUpcRecuperacion(porcentajeRecuperacionNegociacion);
				legalizacion.setPorcentajeUpcPyp(porcentajePypNegociacion);
				legalizacion.setValorMensualUpc(valorMensualUpc);

			}
			List<MunicipioDto> municipiosCobertura = em
					.createNamedQuery("AreaCoberturaSedes.findMunicipiosSeleccionadosCoberturaByNegociacionId",
							MunicipioDto.class)
					.setParameter("negociacionId", solicitud.getNumeroNegociacion()).getResultList();
            String municipioLegalizacion = null;
			List<String> townList = em
                    .createNamedQuery("LegalizacionContrato.municipioLegalizacion", String.class)
                    .setParameter("solicitudId", solicitud.getIdSolicitudContratacion()).getResultList();
			if (!townList.isEmpty())
            {
                municipioLegalizacion = townList.get(0);
            }


			legalizacion.setObservacionNegociacion(observaciones);
			legalizacion.setDescuentos(descuentos);
			legalizacion.setObjetoEvento(objetoEvento);
			legalizacion.setMunicipios(municipiosCobertura);
			legalizacion.setMunicipoLegalizacionMinuta(municipioLegalizacion);
			
            if(Objects.equals(solicitud.getEsRia(), Boolean.TRUE)){
                legalizacion.setObjetoPgp(legalizacionContratoControl.listarObjetoPgpPorNegociacionId(solicitud.getNumeroNegociacion()));
            }else{
                legalizacion.setObjetoPgp(legalizacionContratoControl.listarObjetoPgpSinRiaPorNegociacionId(solicitud.getNumeroNegociacion()));
            }
			
			return legalizacion;

		} catch (final PersistenceException e) {
			throw exceptionUtils.createBusinessException(CodigoMensajeErrorEnum.CONSULTA_LEGALIZACION_CONTRATO);
		}
	}

	@Override
	public List<ProcedimientoDto> buscarProcedimientosNoCompletadosPorcontrato(LegalizacionContratoDto contrato) {
		List<ProcedimientoDto> result = new ArrayList<>();

		result = em.createNativeQuery("select distinct ss.codigo as servicio_codigo, ss.nombre as servicio_nombre, ps.codigo_cliente, ps.cups, ps.descripcion " +
				"from contratacion.contrato c " +
				"join contratacion.solicitud_contratacion sc on sc.id = c.solicitud_contratacion_id " +
				"join contratacion.sedes_negociacion sn on sn.negociacion_id = sc.negociacion_id " +
				"join contratacion.sede_negociacion_servicio sns on sns.sede_negociacion_id = sn.id " +
				"join contratacion.servicio_salud ss on ss.id = sns.servicio_id " +
				"join contratacion.sede_negociacion_procedimiento snp on snp.sede_negociacion_servicio_id = sns.id " +
				"join maestros.procedimiento_servicio ps on ps.id = snp.procedimiento_id " +
				"where (snp.valor_negociado is null or snp.valor_negociado= 1 or snp.valor_negociado = 0) " +
				"and c.numero_contrato = :numeroContrato", "Procedimientos.procedimientoSinValorMapping")
				.setParameter("numeroContrato", contrato.getContratoDto().getNumeroContrato())
				.getResultList();
		return result;
	}

	@Override
	public List<Medicamento> buscarMedicamentosNoCompletadosPorcontrato(LegalizacionContratoDto contrato) {
		List<Medicamento> result = new ArrayList<>();

		result = em.createNativeQuery("select med.* " +
				"from contratacion.contrato c " +
				"join contratacion.solicitud_contratacion sc on sc.id = c.solicitud_contratacion_id " +
				"join contratacion.sedes_negociacion sn on sn.negociacion_id = sc.negociacion_id " +
				"join contratacion.sede_negociacion_medicamento snm on snm.sede_negociacion_id = sn.id " +
				"join maestros.medicamento med on med.id = snm.medicamento_id " +
				"join contratacion.prestador pres on pres.id = sc.prestador_id " +
				"where (snm.valor_negociado is null or snm.valor_negociado= 1 or snm.valor_negociado = 0) " +
				"and c.tipo_modalidad = '"+NegociacionModalidadEnum.EVENTO.name()+"'"+
				"and c.numero_contrato = :numeroContrato", Medicamento.class)
				.setParameter("numeroContrato", contrato.getContratoDto().getNumeroContrato())
				.getResultList();
		return result;
	}

	@Override
	public List<PaquetePortafolio> buscarPaquetesNoCompletadosPorcontrato(LegalizacionContratoDto contrato) {
		List<PaquetePortafolio> result = new ArrayList<>();

		result = em.createNativeQuery("select distinct pp.* " +
				"from contratacion.contrato c " +
				"join contratacion.solicitud_contratacion sc on sc.id = c.solicitud_contratacion_id " +
				"join contratacion.sedes_negociacion sn on sn.negociacion_id = sc.negociacion_id " +
				"join contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id " +
				"join contratacion.paquete_portafolio pp on snp.paquete_id = pp.portafolio_id " +
				"join contratacion.prestador pres on pres.id = sc.prestador_id " +
				"where (snp.valor_negociado is null or snp.valor_negociado = 1 or snp.valor_negociado = 0) " +
				"and c.numero_contrato = :numeroContrato", PaquetePortafolio.class)
				.setParameter("numeroContrato", contrato.getContratoDto().getNumeroContrato())
				.getResultList();
		return result;
	}

	@Override
	public List<PaquetePortafolio> buscarPaquetesSinCodEmsanarPorcontrato(LegalizacionContratoDto contrato) {
		List<PaquetePortafolio> result = new ArrayList<>();

		result = em.createNativeQuery("select distinct pp.* " +
				"from contratacion.contrato c " +
				"join contratacion.solicitud_contratacion sc on sc.id = c.solicitud_contratacion_id " +
				"join contratacion.sedes_negociacion sn on sn.negociacion_id = sc.negociacion_id " +
				"join contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id " +
				"join contratacion.paquete_portafolio pp on snp.paquete_id = pp.portafolio_id " +
				"join contratacion.prestador pres on pres.id = sc.prestador_id " +
				"where (pp.codigo not ilike '00Q%'  or pp.codigo is null) " +
				"and c.numero_contrato = :numeroContrato", PaquetePortafolio.class)
				.setParameter("numeroContrato", contrato.getContratoDto().getNumeroContrato())
				.getResultList();
		return result;
	}

	@Override
	public List<PaquetePortafolio> buscarPaquetesSinContenidoPorcontrato(LegalizacionContratoDto contrato) {
		List<PaquetePortafolio> result = new ArrayList<>();

		result = em.createNativeQuery("select distinct pp.* " +
				"from contratacion.contrato c " +
				"join contratacion.solicitud_contratacion sc on sc.id = c.solicitud_contratacion_id " +
				"join contratacion.sedes_negociacion sn on sn.negociacion_id = sc.negociacion_id " +
				"join contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id " +
				"join contratacion.paquete_portafolio pp on snp.paquete_id = pp.portafolio_id " +
				"join contratacion.prestador pres on pres.id = sc.prestador_id " +
				"where not exists (select null "
				 + "	from contratacion.sede_negociacion_paquete_procedimiento "
				+  "	where sede_negociacion_paquete_id = snp.id) " +
				"and not exists (select null " +
				"		from contratacion.sede_negociacion_paquete_medicamento " +
				"		where sede_negociacion_paquete_id = snp.id) " +
				"and not exists (select null " +
				"		from contratacion.sede_negociacion_paquete_insumo " +
				"		where sede_negociacion_paquete_id = snp.id) " +
				"and c.numero_contrato = :numeroContrato", PaquetePortafolio.class)
				.setParameter("numeroContrato", contrato.getContratoDto().getNumeroContrato())
				.getResultList();
		return result;
	}

	public List<ProcedimientoDto> buscarProcedimientosSinValorContratoRia(LegalizacionContratoDto contrato){
		List<ProcedimientoDto> result  = new ArrayList<>();
		result = em.createNativeQuery("SELECT DISTINCT CONCAT(r.descripcion,'-',rp.descripcion) AS ruta, a.descripcion as actividad, "
				+ "ss.codigo as codigo_servicio, ss.nombre as nombre_servicio, "
				+ "ps.codigo_cliente, ps.cups, ps.descripcion "
				+ "FROM contratacion.negociacion_ria_rango_poblacion nrp "
				+ "JOIN contratacion.sede_negociacion_procedimiento snp ON snp.negociacion_ria_rango_poblacion_id = nrp.id "
				+ "JOIN contratacion.sede_negociacion_servicio sns ON snp.sede_negociacion_servicio_id = sns.id "
				+ "JOIN contratacion.negociacion_ria nr ON nrp.negociacion_ria_id = nr.id "
				+ "JOIN contratacion.negociacion n ON nr.negociacion_id = n.id "
				+ "JOIN contratacion.servicio_salud ss ON sns.servicio_id = ss.id "
				+ "JOIN contratacion.solicitud_contratacion sc ON sc.negociacion_id = n.id "
				+ "JOIN contratacion.contrato c ON c.solicitud_contratacion_id = sc.id "
				+ "JOIN maestros.procedimiento_servicio ps ON snp.procedimiento_id = ps.id "
				+ "JOIN maestros.ria r ON nr.ria_id = r.id "
				+ "JOIN maestros.rango_poblacion rp ON nrp.rango_poblacion_id = rp.id "
				+ "JOIN maestros.ria_contenido rc ON snp.procedimiento_id = rc.procedimiento_servicio_id and rc.rango_poblacion_id = rp.id and rc.ria_id = r.id "
				+ "JOIN maestros.actividad a ON rc.actividad_id = a.id "
				+ "WHERE c.numero_contrato = :numeroContrato and n.referente_id = n.referente_id and nr.negociado = true "
				+ "AND (snp.valor_negociado is null or snp.valor_negociado = 0) "
				+ "ORDER BY 1,2,3,4,5 ",  "Procedimientos.procedimientosSinValorRiaCapita")
				.setParameter("numeroContrato", contrato.getContratoDto().getNumeroContrato())
				.getResultList();

		return result;
	}

	public List<Medicamento> buscarMedicamentosSinValorContratoRia(LegalizacionContratoDto contrato){
		List<Medicamento> result = new ArrayList<>();

		result = em.createNativeQuery("SELECT DISTINCT CONCAT(r.descripcion,'-',rp.descripcion) AS ruta, a.descripcion as actividad,m.codigo, m.descripcion "
				+ "FROM contratacion.negociacion_ria_rango_poblacion nrp "
				+ "JOIN contratacion.sede_negociacion_medicamento snm ON snm.negociacion_ria_rango_poblacion_id = nrp.id "
				+ "JOIN contratacion.negociacion_ria nr ON nrp.negociacion_ria_id = nr.id "
				+ "JOIN contratacion.negociacion n ON nr.negociacion_id = n.id "
				+ "JOIN contratacion.solicitud_contratacion sc ON sc.negociacion_id = n.id "
				+ "JOIN contratacion.contrato c ON c.solicitud_contratacion_id = sc.id "
				+ "JOIN maestros.medicamento m ON snm.medicamento_id = m.id "
				+ "JOIN maestros.ria r ON nr.ria_id = r.id "
				+ "JOIN maestros.rango_poblacion rp ON nrp.rango_poblacion_id = rp.id "
				+ "JOIN maestros.ria_contenido rc ON snm.medicamento_id = rc.medicamento_id and rc.rango_poblacion_id = rp.id and rc.ria_id = r.id "
				+ "JOIN maestros.actividad a ON rc.actividad_id = a.id "
				+ "WHERE c.numero_contrato = :numeroContrato and n.referente_id = n.referente_id  and nr.negociado = true "
				+ "AND (snm.valor_negociado is null or snm.valor_negociado = 0) "
                + "AND (r.id <> 3 or m.id in (select m2.id from maestros.medicamento m2 where m2.codigo ilike 'MED-%')) "
				+ "ORDER BY 1,2,3,4 ", "Medicamento.medicamentosSinNegociarRiaMappgin")
				.setParameter("numeroContrato", contrato.getContratoDto().getNumeroContrato())
				.getResultList();

		return result;
	}

	@Override
	public Boolean validarCambioFechaContrato(Long negociacionId) {

		Boolean response = false;

		StringBuilder query = new StringBuilder();
		query.append(" select evento from auditoria.historial_cambios where id = (")
		.append(" 	select max(hc.id)")
		.append(" 	from auditoria.historial_cambios hc")
		.append(" 	where hc.negociacion_id = :negociacionId")
		.append(" )");

		try {
			String evento = em.createNativeQuery(query.toString())
								.setParameter("negociacionId", negociacionId)
								.getSingleResult().toString();
			if(evento.equals("CAMBIO FECHA CONTRATO")) {
				response = true;
			}
		} catch (NoResultException e) {
			// TODO: handle exception
		}

		return response;
	}
        
    @Override
    public List<AreaCoberturaDTO> listarAreaCobertura(ContratoDto contrato) {
        List<AreaCoberturaDTO> areaCoberturaDTO = em.
                createNamedQuery("AreaCoberturaSedes.findAreaCoberturaByNumContrato", AreaCoberturaDTO.class)
                .setParameter("numeroNegociacion",contrato.getSolicitudContratacionParametrizableDto().getNumeroNegociacion())
                .getResultList();
        return areaCoberturaDTO;
    }
    
    @Override
    public Integer getPoblacionContratoMunicip(String numeroContrato, Integer municipioId) {
        String poblacion = em.createNamedQuery("AreaCoberturaSedes.getPoblacionContratoMunicip")
                .setParameter("numeroContrato", numeroContrato)
                .setParameter("municipioId", municipioId)
                .getSingleResult().toString();
        System.out.println("poblacion "+poblacion);

         return poblacion != null ? new Integer(poblacion) : 0;
    }

    @Override
    public  List<LegalizacionContratoDto> consultarContratoPermanente(Long idPrestador, RegimenNegociacionEnum regimenNegociacionEnum, NegociacionModalidadEnum negociacionModalidadEnum, SolicitudContratacionParametrizableDto sol) {
   
        List<LegalizacionContratoDto> listLegalizacion = em
                .createNamedQuery("LegalizacionContrato.findLegalizacionContratoByPrestador", LegalizacionContratoDto.class)
                .setParameter("prestadorId", idPrestador)
                .setParameter("regimen", regimenNegociacionEnum)
                .setParameter("tipoModalidad", negociacionModalidadEnum)
                .getResultList();
        return listLegalizacion;

    }

    @Override
    public List<MinutaDetalleDto> consultarClausulasYparagrafos(Long minutaId) {

        List<Object[]> results = this.em.createNamedQuery(MinutaDetalle.CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_MINUTA_ID).setParameter(1, minutaId).getResultList();

        List<MinutaDetalleDto> clausulas = new ArrayList<MinutaDetalleDto>();
        for (Object[] record: results) {
            MinutaDetalleDto clausula = (MinutaDetalleDto) record[0];

            MinutaDetalleDto paragrafo = (MinutaDetalleDto) record[1];

            MinutaDetalleDto c2 = clausulas.stream().filter(c -> c.getOrdinal() == clausula.getOrdinal()).findFirst().orElse(null);
            if (c2 != null && paragrafo.getOrdinal() != null) {
                c2.getParagrafos().add(paragrafo);
            } else {
                if (paragrafo.getOrdinal() != null) {
                    clausula.getParagrafos().add(paragrafo);
                }
                clausulas.add(clausula);
            }

        }
        return clausulas;
    }


    @Override
    public List<MinutaDetalleDto> consultarClausulasParagrafosEditados(Long legalizacionContratoId) {
        List<MinutaDetalleDto> clausulasParagrafosEditados = this.em
                .createNamedQuery(MinutaDetalle.CONSULTAR_CLAUSULAS_Y_PARAGRAFOS_POR_LEGALIZACION_CONTRATO_ID)
                .setParameter(1, legalizacionContratoId).getResultList();

        return clausulasParagrafosEditados;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PaquetePortafolio> buscarPaquetesSinContenidoPorNegociacion(LegalizacionContratoDto contrato) {
        return em.createNativeQuery("select distinct pp.*  from contratacion.sedes_negociacion sn "
                + "join contratacion.sede_negociacion_paquete snp on snp.sede_negociacion_id = sn.id "
                + "join contratacion.paquete_portafolio pp on snp.paquete_id = pp.portafolio_id "
                + "where sn.negociacion_id = :negociacionId "
                + "and not exists (select null from contratacion.sede_negociacion_paquete_procedimiento where sede_negociacion_paquete_id = snp.id) "
                + "and not exists (select null from contratacion.sede_negociacion_paquete_medicamento where sede_negociacion_paquete_id = snp.id) "
                + "and not exists (select null from contratacion.sede_negociacion_paquete_insumo where sede_negociacion_paquete_id = snp.id) ", PaquetePortafolio.class)
                .setParameter("negociacionId", contrato.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion())
                .getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<PaquetePortafolio> buscarPaquetesNoCompletadosPorNegociacion(LegalizacionContratoDto contrato) {
        List<PaquetePortafolio> listPaquetes = em.createNativeQuery("select distinct pp.* from contratacion.sede_negociacion_paquete snp "
                + "JOIN contratacion.sedes_negociacion sn ON snp.sede_negociacion_id = sn.id "
                + "JOIN contratacion.paquete_portafolio pp ON snp.paquete_id = pp.portafolio_id "
                + "WHERE  sn.negociacion_id = :negociacionId "
                + "and (snp.valor_negociado is null or snp.valor_negociado = 1 or snp.valor_negociado = 0) ", PaquetePortafolio.class)
                .setParameter("negociacionId", contrato.getContratoDto().getSolicitudContratacionParametrizableDto().getNumeroNegociacion())
                .getResultList();
        return listPaquetes;
    }


}
