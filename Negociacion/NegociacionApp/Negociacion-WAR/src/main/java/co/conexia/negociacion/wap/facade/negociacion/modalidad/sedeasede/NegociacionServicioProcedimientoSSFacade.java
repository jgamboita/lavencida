

package co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import javax.inject.Inject;

import com.conexia.contratacion.commons.constants.CommonConstants;
import com.conexia.contratacion.commons.constants.enums.TipoAsignacionTarifaProcedimientoEnum;
import com.conexia.contratacion.commons.constants.enums.TipoProcedimientoEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.FiltroNegociacionTecnologiaDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.TipoTarifarioDto;
import com.conexia.contratacion.commons.dto.negociacion.CapitulosNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionRiaRangoPoblacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ProcedimientoNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.common.CommonViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.servicio.NegociacionServicioTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.servicio.NegociacionServicioViewServiceRemote;
import com.conexia.servicefactory.CnxService;

public class NegociacionServicioProcedimientoSSFacade implements Serializable{

	@Inject
	@CnxService
	private NegociacionServicioViewServiceRemote negociacionServicioService;

	@Inject
	@CnxService
	private NegociacionServicioTransactionalServiceRemote negociacionServicioTransacc;

	@Inject
	@CnxService
	private CommonViewServiceRemote commonService;

	/**
	 * Consulta los datos para los procedimientos de 1 servicio en negociación sin importar las sedes asociadas.
	 * @param negociacionId identificador de la negociacion
	 * @return {@link - List<ProcedimientoNegociacionDto>}
	 */
	public List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionNoSedesByNegociacionAndServicio(NegociacionDto negociacion,Long negociacionId, Long servicioId, FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia){
		TipoTarifarioDto tarifaNoNormativa = commonService.consultarTarifaNoNormativa();
		return negociacionServicioService.consultarProcedimientosNegociacionNoSedesByNegociacionAndServicio(negociacion,negociacionId, servicioId, tarifaNoNormativa, filtroNegociacionTecnologia);
	}

	/**
	 * Consulta los datos para los procedimientos de 1 capítulo en negociación sin importar las sedes asociadas
	 * @param negociacion
	 * @param negociacionId
	 * @param capituloId
	 * @param filtroNegociacionTecnologia
	 * @return
	 */
	public List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionNoSedesByNegociacionAndCapitulo(NegociacionDto negociacion,Long negociacionId, Long capituloId, FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia){
		TipoTarifarioDto tarifaNoNormativa = commonService.consultarTarifaNoNormativa();
		return negociacionServicioService.consultarProcedimientosNegociacionNoSedesByNegociacionAndCapitulo(negociacion,negociacionId, capituloId, tarifaNoNormativa, filtroNegociacionTecnologia);
	}

	/**
	 * Elimina las sedeNegociacionProcedimiento por negociacion, servcio Salud y las
	 * ID de los procedimientos a eliminar
	 * @param negociacionId La negociación actual.
	 * @param servicioId El servicio al que pertenecen los procedimientos.
	 * @param procedimientosId Lista con las IDs de los procedimientos a eliminar.
	 */
	public void eliminarProcedimientosNegociacionByIdAndNegociacionAndServicio(Long negociacionId, Long servicioId, List<Long> procedimientosId, Integer userId){
		negociacionServicioTransacc.eliminarProcedimientosNegociacionByIdAndNegociacionAndServicio(negociacionId, servicioId, procedimientosId, userId);
	}

	public void eliminarProcedimientosNegociacionByIdAndNegociacionAndCapitulo(Long negociacionId, Long capituloId, List<Long> procedimientosId, Integer userId){
		negociacionServicioTransacc.eliminarProcedimientosNegociacionByIdAndNegociacionAndCapituloPGP(negociacionId, capituloId, procedimientosId, userId);
	}

	/**
	 * Elimina las sedeNegociacionProcedimiento por negociacion, servcio Salud y las
	 * ID de los procedimientos a eliminar
	 * @param negociacionId La negociación actual.
	 * @param servicioId El servicio al que pertenecen los procedimientos.
	 * @param procedimientosId Lista con las IDs de los procedimientos a eliminar.
	 */
	public void eliminarProcedimientosNegociacionByIdAndNegociacionAndServicio(Long negociacionId, Long servicioId, List<Long> procedimientosId, List<Long> sedeNegociacionServicioId, Integer userId){
		negociacionServicioTransacc.eliminarProcedimientosNegociacionByIdAndNegociacionAndServicio(negociacionId, servicioId, procedimientosId, sedeNegociacionServicioId, userId);
	}

	/**
	 * Función que guarda una lista de procedimientos en una SedeNegociación dada la negociación, el servicio al que pertenece y la lista
	 * de procedimientos.
	 * @param procedimientos la lista de procedimientos a persistir.
	 * @param negociacionId la negociación a la que pertenece esos procedimientos.
	 * @param servicioId El servicio asociado a esos procedimientos.
	 */
    public void guardarProcedimientosNegociados(List<ProcedimientoNegociacionDto> procedimientos, Long negociacionId, Long servicioId, TipoTarifarioDto tarifario, Integer userId,
                                                Long negociacionReferenteId, TipoAsignacionTarifaProcedimientoEnum tipoAsignacionSeleccionado) {
    	negociacionServicioTransacc.guardarProcedimientosNegociados(procedimientos, negociacionId, servicioId, tarifario, userId, negociacionReferenteId, tipoAsignacionSeleccionado);
    }

    /**
     * Función que guarda una lista de procedimientos en una SedeNegociación dada la negociación, el servicio al que pertenece y la lista
     * @param procedimientos
     * @param negociacionId
     * @param servicioId
     */
    public void guardarProcedimientosNegociados(List<ProcedimientoNegociacionDto> procedimientos, Long negociacionId, Long servicioId, Integer userId) {
    	negociacionServicioTransacc.guardarProcedimientosNegociados(procedimientos, negociacionId, servicioId, userId);
    }

    /**
     * FUnción que guarda una lista de procedimientos en una sedeNegociacion dada la negociación, el capítulo al que pertenece
     * @param procedimientos
     * @param negociacionId
     * @param capituloId
     * @param userId
     * @throws ConexiaBusinessException
     */
    public void guardarProcedimientosNegociadosPGP(List<ProcedimientoNegociacionDto> procedimientos, Long negociacionId, Long capituloId, Integer userId) throws ConexiaBusinessException {
    	negociacionServicioTransacc.guardarProcedimientosNegociadosPGP(procedimientos, negociacionId, capituloId, userId);
    }

    /**
     * Permite guardar el rango franja riesgo en los procedimientos seleccionados
     * @param negociacionId
     * @param procedimientoIds
     * @param capituloId
     * @param franjaInicio
     * @param franjaFin
     * @throws ConexiaBusinessException
     */
    public void guardarProcedimientosFranjaPGP(Long negociacionId, List<Long>procedimientoIds, Long capituloId, BigDecimal franjaInicio, BigDecimal franjaFin, Integer userId) throws ConexiaBusinessException {
    	negociacionServicioTransacc.guardarProcedimientosFranjaPGP(negociacionId, procedimientoIds, capituloId, franjaInicio, franjaFin, userId);
    }

    /**
	 * Método que liquida el valor de un procedimiento basado en un porcentaje y un tarifario
	 *
	 * @param cups
	 * @param tarifarioId
	 * @param porcentajeNegociacion
	 * @return
	 */
	public BigDecimal calcularValorProcedimiento(String cups, Integer tarifarioId, Double porcentajeNegociacion) {
		return this.negociacionServicioService.calcularValorProcedimiento(cups, tarifarioId, porcentajeNegociacion);
	}

    /**
     * Asigan el valor de Contrato anterior a los procedimientos
     * @param dto
     */
    public boolean asignarValoresContratoAnterioProcedimiento(ProcedimientoNegociacionDto dto){
    	if(Objects.nonNull(dto.getTarifarioContratoAnterior()) && !dto.getTarifarioContratoAnterior().isEmpty()){
	    	TipoTarifarioDto tarifario = commonService.consultarTarifaByDescripcion(dto.getTarifarioContratoAnterior());
	    	if(dto.getTarifarioSoportados().contains(tarifario)){
				dto.setValorNegociado(null);
				dto.setTarifarioNegociado(tarifario);
				dto.setPorcentajeNegociado(Objects.nonNull(dto.getPorcentajeContratoAnterior()) ? dto.getPorcentajeContratoAnterior() : BigDecimal.ZERO);
				dto.setNegociado(true);
				if(tarifario.getCodigo().equals(CommonConstants.COD_TARIFARIO_NO_NORMATIVO)){
					if(Objects.nonNull(dto.getValorContratoAnterior())){
						if(dto.getProcedimientoDto().getTipoProcedimiento()==TipoProcedimientoEnum.PROCEDIMIENTO.getId().intValue()){
							dto.setValorNegociado(dto.getValorContratoAnterior().setScale(-1, BigDecimal.ROUND_HALF_UP));
						}else{
							dto.setValorNegociado(dto.getValorContratoAnterior().setScale(-2, BigDecimal.ROUND_HALF_UP));
						}
					}else{
						dto.setValorNegociado(dto.getValorContratoAnterior());
					}
				}else{
					dto.setValorNegociado(this.calcularValorProcedimiento(dto.getProcedimientoDto().getCups(),
							tarifario.getId().intValue(),
							Objects.nonNull(dto.getPorcentajeContratoAnterior()) ? dto.getPorcentajeContratoAnterior().doubleValue() : 0));
					if(Objects.isNull(dto.getValorNegociado())){
						dto.setValorNegociado(this.calcularValorProcedimiento(dto.getProcedimientoDto().getCodigoCliente(),
								tarifario.getId().intValue(),
								Objects.nonNull(dto.getPorcentajeContratoAnterior()) ? dto.getPorcentajeContratoAnterior().doubleValue() : 0));
					}
				}
				return true;
			}
    	}
    	return false;
    }

    /**
	 * Asigna el tarifario dependiendo de la opcion y valida si se puede
	 * asignar a ese tarifario o no.
	 * @param dto
	 * @return <b>True</b> Si es posible asignar el tarifario, <b>False</b> de lo contrario.
	 * @throws ConexiaBusinessException
	 */
	public boolean asignarTarifarioMasivo(ProcedimientoNegociacionDto dto, TipoTarifarioDto tarifarioAsignar, Double porcentajeValor) throws ConexiaBusinessException {
		//Se valida que efectivamente se pueda asignar la nueva tarifa
		if(Objects.nonNull(dto.getTarifarioSoportados()) && Objects.nonNull(tarifarioAsignar)) {
			if(dto.getTarifarioSoportados().contains(tarifarioAsignar)){
				dto.setValorNegociado(null);
				dto.setTarifarioNegociado(tarifarioAsignar != null ? tarifarioAsignar: dto.getTarifarioNegociado());
				dto.setPorcentajeNegociado(BigDecimal
						.valueOf(porcentajeValor));
				dto.setNegociado(true);
				dto.setValorNegociado(this.calcularValorProcedimiento(dto.getProcedimientoDto().getCups(), tarifarioAsignar.getId().intValue(), porcentajeValor));
				if(Objects.isNull(dto.getValorNegociado())){
					dto.setValorNegociado(this.calcularValorProcedimiento(dto.getProcedimientoDto().getCodigoCliente(), tarifarioAsignar.getId().intValue(), porcentajeValor));
				}
				return true;
			}
		}

		return false;
	}

	/**
	 * Valida si el tarifario a asignar genera una tarifa diferencial en el procedimiento actual.
	 * Si este es el caso se actualiza el Dto del procedimiento para que marque que tiene tarifa diferencial
	 * @param dto El Dto del procedimiento actual a validar.
	 * @param tarifarioServicioPadre Tarifa del servicio Salud al que pertecene este procedimiento.
	 * @param tarifarioAsignar El tarifario que se quiere asignar.
	 */
	public void asignarVerificarTarifaDiferencial(ProcedimientoNegociacionDto dto, TipoTarifarioDto tarifarioServicioPadre, TipoTarifarioDto tarifarioAsignar){
		if(tarifarioServicioPadre!=null){
			if(tarifarioServicioPadre.getId()!=null){
				if(!tarifarioServicioPadre.equals(tarifarioAsignar)){
					dto.setTarifaDiferencial(true);
				}else{
					dto.setTarifaDiferencial(false);
				}
			}
		}

	}

	public List<ProcedimientoDto> consultarProcedimientosAgregar(ProcedimientoDto procedimiento, NegociacionDto negociacion) throws ConexiaBusinessException {
		return this.negociacionServicioService.consultarProcedimientosAgregar(procedimiento, negociacion);
	}

	public List<ProcedimientoDto> consultarProcedimientosAgregarPGP(ProcedimientoDto procedimiento, NegociacionDto negociacion, Long capituloId) throws ConexiaBusinessException {
		return this.negociacionServicioService.consultarProcedimientosAgregarPGP(procedimiento, negociacion, capituloId);
	}

	public void agregarProcedimientosNegociacion(List<Long> procedimientosIds, NegociacionDto negociacion, String codigoServicio, 
                List<ProcedimientoDto> procedimientos, Integer userId, Long negociacionReferenteId) {
		this.negociacionServicioTransacc.agregarProcedimientosNegociacion(
				procedimientosIds, negociacion, codigoServicio, procedimientos, userId, negociacionReferenteId);
	}

	/**
	 * Permite agregar procedimientos a la negociación PGP en curso
	 * @param procedimientosIds
	 * @param negociacion
	 * @param capituloId
	 * @param userId
	 */
	public void agregarProcedimientosNegociacion(List<Long> procedimientosIds, NegociacionDto negociacion, Long capituloId, Integer userId) {
		this.negociacionServicioTransacc.agregarProcedimientosNegociacionPGP(
				procedimientosIds, negociacion, capituloId, userId);
	}

	/**
	 * Cuenta la cantidad de procedimientos de un capítulo en negociación PGP
	 * @param negociacionId
	 * @param capituloId
	 * @return
	 */
	public Integer contarProcedimientosByNegociacionCapitulo(Long negociacionId, Long capituloId) {
		return this.negociacionServicioService.contarProcedimientosByNegociacionCapitulo(negociacionId, capituloId);
	}

	public void eliminarProcedimientosNegociacionByIdAndNegociacion(Long negociacionId, List<String> procedimientosId, Integer userId) {
		negociacionServicioTransacc.eliminarProcedimientosNegociacionByIdAndNegociacion(negociacionId, procedimientosId, userId);
	}

	public Integer eliminarProcedimientosNegociacionByIdAndNegociacionPGP(Long negociacionId, List<Long> procedimientosId, CapitulosNegociacionDto capituloNegociacion, Integer userId) {
		return this.negociacionServicioTransacc.eliminarProcedimientosNegociacionByIdAndNegociacionPGP(negociacionId, procedimientosId, capituloNegociacion, userId);
	}

	public void eliminarTodasTecnologiasRiaCapita(Long negociacionId,List<String> codigos, Integer userId){
		negociacionServicioTransacc.eliminarTodasTecnologiasRiaCapita(negociacionId, codigos, userId);
	}

	public void eliminarTecnologiasRiaCapitaActivdad(NegociacionRiaRangoPoblacionDto negociacionRangoPoblacion,List<String> codigos,List<Integer> actividadId, Long negociacionId,List<Integer> servicioCodigo,String eliminarTodo, Integer userId){
		negociacionServicioTransacc.eliminarTecnologiasRiaCapitaActivdad(negociacionRangoPoblacion, codigos, actividadId,
				negociacionId,servicioCodigo,eliminarTodo,userId);
	}

	/**
	 * Guarda las tarifas de los procedimientos de la ne negociacion
	 *
	 * @param negociacionId Identificador de la negociacion
	 * @param procedimientosNegociacion Lista de procedimientos
	 */
	public String guardar(Long negociacionId, List<ProcedimientoNegociacionDto> procedimientosNegociacion, TipoAsignacionTarifaProcedimientoEnum tipoAsignacion, boolean esTransporte ) {
		return this.negociacionServicioTransacc
				.guardarProcedimientosNegociacion(negociacionId, procedimientosNegociacion, tipoAsignacion, esTransporte);

	}

	/**
	 * Consulta los datos para los procedimientos de 1 servicio en negociación sin importar las sedes asociadas.
	 * @param filtroNegociacionTecnologia
	 * @return {@link - List<ProcedimientoNegociacionDto>}
	 */
	public List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionNoSedesByNegociacionAndServicio(NegociacionDto negociacion, FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia){
		TipoTarifarioDto tarifaNoNormativa = commonService.consultarTarifaNoNormativa();
		return negociacionServicioService.consultarProcedimientosNegociacionNoSedesByNegociacionAndServicio(negociacion,filtroNegociacionTecnologia, tarifaNoNormativa);
	}

	public List<ProcedimientoNegociacionDto> consultarProcedimientosNegociacionNoSedesByNegociacionAndCapitulo(NegociacionDto negociacion, FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia){
		TipoTarifarioDto tarifaNoNormativa = commonService.consultarTarifaNoNormativa();
		return negociacionServicioService.consultarProcedimientosNegociacionNoSedesByNegociacionAndCapitulo(negociacion,filtroNegociacionTecnologia, tarifaNoNormativa);
	}

	/**
	 * Cuenta la cantidad de registros eistentes para la consulta
	 * @param filtroNegociacionTecnologia
	 * @return
	 */
	public Integer contarProcedimientosNegociacionNoSedesByNegociacionAndServicio(FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia, NegociacionDto negociacion){
		return negociacionServicioService.contarProcedimientosNegociacionNoSedesByNegociacionAndServicio(filtroNegociacionTecnologia, negociacion);
	}

	/**
	 * CUenta la cantidad de procedimientos existentes en el capítulo de la negociación PGP indicado
	 * @param filtroNegociacionTecnologia
	 * @param negociacion
	 * @return
	 */
	public Integer contarProcedimientosNegociacionNoSedesByNegociacionAndCapitulo(FiltroNegociacionTecnologiaDto filtroNegociacionTecnologia, NegociacionDto negociacion){
		return negociacionServicioService.contarProcedimientosNegociacionNoSedesByNegociacionAndCapitulo(filtroNegociacionTecnologia, negociacion);
	}

	/**
	 * Permite contar la cantidad de procedimientos de una negociación PGP
	 * @param negociacionId
	 * @return
	 */
	public Integer contarProcedimientosByNegociacionId(Long negociacionId) {
		return negociacionServicioService.contarProcedimientosByNegociacionId(negociacionId);
	}

	public TipoTarifarioDto consultarTarifarioByDescripcion(String tarifario) {
		return negociacionServicioService.consultarTarifarioByDescripcion(tarifario);
	}

	public void actualizaProcedimientosRiaCapita(List<ProcedimientoNegociacionDto> procedimientos,NegociacionRiaRangoPoblacionDto negociacionRangoPoblacion, Long negociacionId, Integer userId){
		negociacionServicioTransacc.actualizarProcedimientosRiaCapita(procedimientos, negociacionRangoPoblacion, negociacionId, userId);

	}

	public void actualizarValorNegociadoProcedimientoRC(ProcedimientoNegociacionDto procedimiento,NegociacionRiaRangoPoblacionDto negociacionRangoPoblacion ,Long negociacionId, Integer userId){
		negociacionServicioTransacc.actualizarValorNegociadoProcedimientoRC(procedimiento, negociacionRangoPoblacion, negociacionId, userId);
	}

	/**
	 *
	 * @param negociacionId
	 * @param servicioId
	 */
	public void actualizarValorNegociadoServicioNegociacion(Long negociacionId, Long servicioId, Integer userId){
		negociacionServicioTransacc.actualizarValorNegociadoServicioNegociacion(negociacionId, servicioId, userId);
	}

	/**
	 *
	 * @param negociacionId
	 * @param servicioId
	 */
	public void eliminarTecnologiasRutas(Long negociacionId,List<NegociacionRiaRangoPoblacionDto> riasSeleccionadas, Integer userId){
		negociacionServicioTransacc.eliminarTecnologiasRutas(negociacionId, riasSeleccionadas, userId);
	}

	public void actualizarValorNegociadoCapituloNegociacion(Long negociacionId, Long capituloId, Integer userId) throws ConexiaBusinessException{
		negociacionServicioTransacc.actualizarValorNegociadoCapituloNegociacion(negociacionId, capituloId, userId);
	}

	public void aplicarValorNegociadoByPoblacionId(Long negociacionId, Integer userId) throws ConexiaBusinessException {
		negociacionServicioTransacc.aplicarValorNegociadoByPoblacion(negociacionId, userId);
	}

    public void actualizarValorNegociadoTecnologiasNegociadasRuta(NegociacionRiaRangoPoblacionDto negociacionRiaRangoPoblacionDto, Long negociacionId) {
    	negociacionServicioTransacc.actualizarValorNegociadoTecnologiasNegociadasRuta( negociacionRiaRangoPoblacionDto, negociacionId);
    }
}

