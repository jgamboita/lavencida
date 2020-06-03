package co.conexia.negociacion.wap.facade.negociacion;

import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contratacion.commons.constants.enums.TipoModificacionOtroSiEnum;
import com.conexia.contratacion.commons.constants.enums.TipoOtroSiEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SolicitudContratacionParametrizableDto;
import com.conexia.contratacion.commons.dto.maestros.*;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.contratacion.commons.dto.referente.ReferentePrestadorDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.negociacion.NegociacionTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.NegociacionViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.sedes.AreaCoberturaTransactionalServiceRemote;
import com.conexia.notificaciones.remote.NotificacionesRemote;
import com.conexia.servicefactory.CnxService;

import javax.inject.Inject;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class NegociacionFacade implements Serializable {

    @Inject
    @CnxService
    private NegociacionTransactionalServiceRemote negociacionTransactionalService;

    @Inject
    @CnxService
    private NegociacionViewServiceRemote negociacionViewService;

    @Inject
    @CnxService
    private AreaCoberturaTransactionalServiceRemote areaCoberturaTransactionalService;

    @Inject
    @CnxService
    private NotificacionesRemote notificacionesRemote;

    public void actualizarPoblacion(Long negociacionId) {
        this.negociacionTransactionalService.actualizarPoblacion(negociacionId);
    }

    public Long crearNegociacion(NegociacionDto negociacion) {
        return this.negociacionTransactionalService.crearNegociacion(negociacion);
    }

    public TecnologiasNegociacionDto consultarTecnologiasNoNegociadas(Long prestadorId, ComplejidadNegociacionEnum complejidadNegociacionEnum, NegociacionModalidadEnum modalidad) {
        return this.negociacionViewService.consultarTecnologiasNoNegociadas(
                prestadorId, complejidadNegociacionEnum,modalidad);
	}

    public List<SedePrestadorDto> consultarSedesPuedenNegociar(PrestadorDto prestador) {
        return this.negociacionViewService.consultarSedesPrestadorPuedenNegociar(prestador);
    }

    public List<ReferentePrestadorDto> consultarSedesVigentesPrestador(String prestadorNumeroDocumento) {
    	return this.negociacionViewService.consultarSedesVigentesPrestador(prestadorNumeroDocumento);
    }

    public NegociacionDto consultarNegociacionById(Long negociacionId) {
        return this.negociacionViewService.consultarNegociacionById(negociacionId);
    }
    
    public Long obtenerNegociacionReferenteId(Long negociacionId) 
    {
        return this.negociacionViewService.obtenerNegociacionReferenteId(negociacionId);
    }

   public NegociacionDto consultarNegociacionByIdPGP(Long negociacionId) {
       return this.negociacionViewService.consultarNegociacionByIdPGP(negociacionId);
   }

    public boolean validarAreaCoberturarSedesNegociacion(Long sedeNegociacionId){
    	return this.negociacionViewService.countMunicipiosAreaCoberturaPorSedeNegociacion(sedeNegociacionId)>0;
    }

    public void eliminarMunicipiosSinPoblacion(Long negociacionId) throws ConexiaBusinessException {
    	negociacionTransactionalService.eliminarMunicipiosSinPoblacion(negociacionId);
    }

    public List<SedesNegociacionDto> consultarSedeNegociacionByNegociacionId(Long negociacionId) {
        return this.negociacionViewService.consultarSedeNegociacionByNegociacionId(negociacionId);
    }

    public Long crearSedesNegociacionCoberturaPorDefecto(SedesNegociacionDto sedeNegociacion, Boolean isCapitaOEventoPrimerNivel, AreaCoberturaTipoEnum tipoArea,NegociacionDto negociacion) {
        return this.areaCoberturaTransactionalService.crearSedesNegociacionCoberturaPorDefecto(sedeNegociacion, isCapitaOEventoPrimerNivel,tipoArea,negociacion);
    }

    public Integer eliminarNegociacion(Long negociacionId) {
        return this.negociacionTransactionalService.eliminarNegociacion(negociacionId);
    }

    public void eliminarSedeNegociacion(Long sedeNegociacionId) {
        this.negociacionTransactionalService.eliminarSedeNegociacion(sedeNegociacionId);
    }

    public void eliminarSedeNegociacionByNegociacionIdAndSedePrestador(Long negociacionId, Long sedePrestadorId) {
        this.areaCoberturaTransactionalService.eliminarSedeNegociacionByNegociacionIdAndSedePrestador(negociacionId, sedePrestadorId);

    }

    public void eliminarSedeNegociacionByNegociacionId(Long negociacionId) {
        this.areaCoberturaTransactionalService.eliminarSedeNegociacionByNegociacionId(negociacionId);

    }

    public NegociacionDto terminarBaseNegociacion(NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException {
        return this.negociacionTransactionalService.terminarBaseNegociacion(negociacion, userId);
    }

    public void terminarBaseNegociacionPGP(NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException {
        this.negociacionTransactionalService.terminarBaseNegociacionPGP(negociacion, userId);
    }

    public void actualizarAgrupadoresNegociacionPGP(Long negociacionId, Integer userId) throws ConexiaBusinessException {
    	this.negociacionTransactionalService.actualizarAgrupadoresNegociacionPGP(negociacionId, userId);
    }

    public Long crearInvitacionNegociacion(InvitacionNegociacionDto invitacionNegociacion) {
        return this.negociacionTransactionalService.crearInvitacionNegociacion(invitacionNegociacion);
    }

    public InvitacionNegociacionDto consultarInvitacionNegociacionByNegociacionId(Long negociacionId) {
        return this.negociacionViewService.consultarInvitacionNegociacionByNegociacionId(negociacionId);
    }

    public Long actualizarInvitacionNegociacion(InvitacionNegociacionDto invitacionNegociacion) {
        return this.negociacionTransactionalService.actualizarInvitacionNegociacion(invitacionNegociacion);
    }

    public void guardarDatosCapita(NegociacionDto negociacion) {
        this.negociacionTransactionalService.guardarDatosCapita(negociacion);
    }

    public void actualizarObservacionesEvento(NegociacionDto negociacion){
    	this.negociacionTransactionalService.actualizarObservacionEvento(negociacion);
    }

    public void actualizarFechaConcertacionMedicamentos(NegociacionDto negociacion) {
    	this.negociacionTransactionalService.actualizarFechaConcertacionMedicamentos(negociacion);
    }

    public void actualizarFechaConcertacionProcedimientos(NegociacionDto negociacion) {
        this.negociacionTransactionalService.actualizarFechaConcertacionProcedimientos(negociacion);
    }

    public void actualizarFechaConcertacionPaquete(NegociacionDto negociacion) {
        this.negociacionTransactionalService.actualizarFechaConcertacionPaquete(negociacion);
    }

    public void establecerFechasConcertacion(NegociacionDto negociacion) {
        this.negociacionTransactionalService.establecerFechasConcertacion(negociacion);
    }

    public void actualizarPoblacionNegociacion (NegociacionDto negociacion){
    	this.negociacionTransactionalService.actualizarPoblacionNegociacion(negociacion);
    }

    public Long guardarIncentivoNegociacion(IncentivoModeloDto incentivo) {
        return this.negociacionTransactionalService.guardarIncentivo(incentivo);
    }

    public void eliminarIncentivoNegociacion(Long incentivoId ){
        this.negociacionTransactionalService.eliminarIncentivo(incentivoId);
    }

    public List<IncentivoModeloDto> consultarIncentivosByNegociacionId(Long negociacionId) {
        return this.negociacionViewService.consultarIncentivosByNegociacionId(negociacionId);

    }

    public List<IncentivoModeloDto> consultarModelosByNegociacionId(Long negociacionId) {
        return this.negociacionViewService.consultarModelosByNegociacionId(negociacionId);
    }

    public void eliminarModeloNegociacion(Long modeloId) {
        this.negociacionTransactionalService.eliminarModelo(modeloId);
    }

    public Long guardarModeloNegociacion(IncentivoModeloDto modelo) {
        return this.negociacionTransactionalService.guardarModelo(modelo);
    }

    public Long countTotalTecnologiasNegociacion(Long negociacionId) {
        return this.negociacionViewService.countTotalTecnologiasNegociacion(negociacionId);
    }

    public Long countTotalTecnologiasNegociacionPGP(Long negociacionId) {
        return this.negociacionViewService.countTotalTecnologiasNegociacionPGP(negociacionId);
    }

    public Long countTecnologiaNoNegociadas(Long negociacionId) {
        return this.negociacionViewService.countTecnologiaNoNegociadas(negociacionId);
    }

    public void cambiarModalidadNegociacion(NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException {
        this.negociacionTransactionalService.cambiarModalidadNegociacion(negociacion, userId);
    }

    public List<SolicitudContratacionParametrizableDto> findSolicitudesFinalizadasContratacionByNegociacionId(Long negociacionId, EstadoLegalizacionEnum estado) {
        return this.negociacionViewService.findSolicitudesContratacionByNegociacionIdAndEstado(negociacionId, estado);
    }

    public void actualizarValorTotal(NegociacionDto negociacion){
    	this.negociacionTransactionalService.actualizarValorTotal(negociacion);
    }
    public BigDecimal sumPorcentajeTotalTemaServiciosPyp (final Long negociacionId){
    	return this.negociacionViewService.sumPorcentajeTotalTemaServiciosPyp(negociacionId);
    }

    public BigDecimal sumPorcentajeTotalTemaServiciosRecuperacion (final Long negociacionId){
    	return this.negociacionViewService.sumPorcentajeTotalTemaServiciosRecuperacion(negociacionId);
    }

    public BigDecimal sumPorcentajeTotalTemaMedicamentosRecuperacion (final Long negociacionId){
    	return this.negociacionViewService.sumPorcentajeTotalTemaMedicamentosRecuperacion(negociacionId);
    }

    public void marcarNegociacionEnCreacion(final NegociacionDto negociacionDto, final Boolean enCreacion){
    	this.negociacionTransactionalService.marcarNegociacionEnCreacion(negociacionDto, enCreacion);
    }

	public PrestadorDto consultarNegociacionAClonar(Long negociacionBaseId, NegociacionDto nuevaNegociacion) throws ConexiaBusinessException {
		return negociacionViewService.consultarNegociacionAClonar(negociacionBaseId, nuevaNegociacion);
	}

	public void clonarNegociacion(ClonarNegociacionDto clonarNegociacion) {
		this.negociacionTransactionalService.clonarNegociacion(clonarNegociacion);
	}

    public void guardarHistorialFinalizarNegociacion(Integer userId, Long negociacionId) {
        this.negociacionTransactionalService.guardarHistorialNegociacion(userId, negociacionId, "FINALIZAR");
    }

    public void guardarHistorialEliminarNegociacion(int userId, Long negociacionId) {
        this.negociacionTransactionalService.guardarHistorialNegociacion(userId, negociacionId, "ELIMINAR");
    }

    public boolean verificarPortafolioCapitaByPrestador(Long prestadorId){
    	return this.negociacionTransactionalService.verificarPortafolioCapitaByPrestador(prestadorId);
    }

    public List<ProcedimientoDto> consultarProcedimientosSinValorPGP (Long negociacionId){
    	return this.negociacionTransactionalService.consultarProcedimientosSinValorPGP(negociacionId);
    }

    public List<MedicamentosDto> consultarMedicamentosSinValorPgp(Long negociacionId) {
    	return this.negociacionViewService.consultarMedicamentosSinValorPgp(negociacionId);
    }

	public List<ProcedimientoDto> consultarProcedimientosSinNegociaRiaCapitaGrupoEtario(NegociacionDto negociacion){
		return this.negociacionTransactionalService.consultaProcedimientosSinNegociaRiaCapitaGrupoEtario(negociacion);
	}

	public NegociacionDto consultarRias(NegociacionDto negociacion){
		return this.negociacionViewService.consultarRias(negociacion);
	}

	public void guardarDatosPgp(NegociacionDto negociacion) throws ConexiaBusinessException {
        this.negociacionTransactionalService.guardarDatosPgp(negociacion);
    }

	public void guardarDatosRiasCapita(NegociacionDto negociacion) {
		this.negociacionTransactionalService.guardarDatosRiasCapita(negociacion);
	}

	public NegociacionDto consultarCapitaPorRias(NegociacionDto negociacion) {
		return this.negociacionViewService.consultarCapitaPorRias(negociacion);
	}

	public List<NegociacionRiaDto> consultarRiasDisponibles(NegociacionDto negociacion) {
		return this.negociacionViewService.consultarRiasDisponibles(negociacion);
	}

	public List<NegociacionRiaActividadMetaDto> consultarActividades(NegociacionRiaRangoPoblacionDto riaRangoPoblacion) {
		return this.negociacionViewService.consultarActividades(riaRangoPoblacion);
	}

	public void guardarActividades(List<NegociacionRiaActividadMetaDto> listaActividades) {
		this.negociacionTransactionalService.guardarActividades(listaActividades);
	}

	public List<ReferenteDto> getListaReferenteCapitaPorRias() {
		return this.negociacionViewService.getListaReferenteCapitaPorRias();
	}

	public List<ReferenteDto> getListaReferentePGP(NegociacionDto negociacion) {
		return this.negociacionViewService.getListaReferentePGP(negociacion);
	}

	public void validarNegociacionCapita(NegociacionDto negociacion, Integer userId){
		this.negociacionTransactionalService.validarNegociacionCapita(negociacion, userId);
	}

	public void validarNegociacionCapitaPorRias(NegociacionDto negociacion, Integer userId){
		this.negociacionTransactionalService.validarNegociacionCapitaPorRias(negociacion, userId);
	}

	public void actualizarValorNegociadosRiasNegociacion(List<NegociacionRiaRangoPoblacionDto> negociacionRiaRango){
		this.negociacionTransactionalService.actualizarValorNegociadosRiasNegociacion(negociacionRiaRango);
	}

	public BigDecimal sumValorTotal(Long negociacionId){
		return this.negociacionViewService.sumValorTotal(negociacionId);
	}

	public BigDecimal sumValorTotalPGP(Long negociacionId){
		return this.negociacionViewService.sumValorTotalPGP(negociacionId);
	}

	public Boolean validarPoblacionXMunicipioPgp(Long negociacionId, Integer municipioId) throws ConexiaBusinessException {
		return this.negociacionViewService.validarPoblacionXMunicipioPgp(negociacionId, municipioId);
	}

	public Boolean validarLegalizacionPreliminar(Long negociacionId) {
    	return this.negociacionViewService.validarLegalizacionPreliminar(negociacionId);
    }

	public List<NegociacionMunicipioDto> obtenerMunicipiosNegociacion(long negociacionId){
		return this.negociacionViewService.obtenerMunicipiosNegociacion(negociacionId);
	}

	public void addMunicipiosNegociacion(Long negociacionId, Integer municipioId) throws ConexiaBusinessException {
		negociacionTransactionalService.agregarMunicipiosNegociacion(negociacionId, municipioId);
	}

	public void eliminarMunicipioNegociacionById(Long negociacionId, Integer municipioId) throws ConexiaBusinessException {
		negociacionTransactionalService.eliminarMunicipioNegociacionById(negociacionId, municipioId);
	}

	public void registrarDescargaAnexo(Long negociacionId, Integer userId, String nombreAnexo){
		negociacionTransactionalService.registrarDescargaAnexo(negociacionId, userId, nombreAnexo);
	}

    public void updateTecnologiaAgregadasOtroSi(NegociacionDto negociacion) throws ConexiaBusinessException
    {
        this.negociacionTransactionalService.updateTecnologiaAgregadasOtroSi(negociacion);
    }

	public boolean importarPoblacionCapitaGrupoEtareo(List<AfiliadoDto> poblacion, NegociacionDto negociacion) throws Exception {
		return negociacionTransactionalService.importarPoblacionCapitaGrupoEtareo(poblacion, negociacion);
	}

	public String generarArchivoRiasGrupoEtareo(ReportesAnexosNegociacionEnum xls, NegociacionDto negociacion) throws IOException {
		return negociacionViewService.generarArchivoCapitaGrupoEtareo(xls, negociacion);
	}

	public void actualizarValoresDestoUpcNegociadaGrupoEtario(NegociacionRiaRangoPoblacionDto negociacionRiaRangoPoblacionDto) throws IOException {
		negociacionTransactionalService.actualizarValoresDestoUpcNegociadaGrupoEtario(negociacionRiaRangoPoblacionDto);
	}

	public void copiaTecnologiasRiaCapitaSegunReferente(NegociacionDto negociacion, Integer userId) throws IOException {
		negociacionTransactionalService.copiaTecnologiasRiaCapitaSegunReferente(negociacion, userId);
	}

	public void crearReglaNegociacion(ReglaNegociacionDto reglaNegociacion) throws ConexiaBusinessException{
		this.negociacionTransactionalService.crearReglaNegociacion(reglaNegociacion);
	}

	public List<ReglaNegociacionDto> getListReglasNegociacion(long negociacionId) throws ConexiaBusinessException {
		return this.negociacionViewService.obtenerReglasNegociacion(negociacionId);
	}

	public Integer eliminarReglaNegociacion(long reglaId) throws ConexiaBusinessException {
		return this.negociacionTransactionalService.eliminarReglaNegociacion(reglaId);
	}

	public Integer actualizarReglaNegociacion(ReglaNegociacionDto reglaNegociacion) throws ConexiaBusinessException {
		return this.negociacionTransactionalService.actualizarReglaNegociacion(reglaNegociacion);
	}

	public void eliminarTecnologiasNegociacionPGP(Long negociacionId, Integer userId) throws ConexiaBusinessException {
		this.negociacionTransactionalService.eliminarTecnologiasNegociacionPGP(negociacionId, userId);
	}

	public List<ProcedimientoDto> consultarProcedimientosSinFranja(Long negociacionId) throws ConexiaBusinessException {
		return this.negociacionViewService.consultarProcedimientosSinFranja(negociacionId);
	}

	public List<MedicamentosDto> consultarMedicamentosSinFranja(Long negociacionId) throws ConexiaBusinessException {
		return this.negociacionViewService.consultarMedicamentosSinFranja(negociacionId);
	}

	public Long consultarReferenteNegociacion(Long negociacionId) throws ConexiaBusinessException {
		return this.negociacionViewService.consultarReferenteNegociacion(negociacionId);
	}

	public List<SedePrestadorDto> getSedesSinPoblacionPGP(Long negociacionId) throws ConexiaBusinessException {
		return this.negociacionViewService.getSedesSinPoblacionPGP(negociacionId);
	}

	public void guardarFechaCorte(Date fechaCorte, Long negociacionId) throws ConexiaBusinessException {
		this.negociacionTransactionalService.guardarFechaCorte(fechaCorte, negociacionId);
	}

	public void addPoblacionFechaCorteMunicipioPGP(NegociacionDto negociacion, List<ReglaNegociacionDto> reglasNegociacion, List<SedesNegociacionDto> sedeSeleccionada) throws ConexiaBusinessException {
		this.negociacionTransactionalService.addPoblacionFechaCorteMunicipioPGP(negociacion, reglasNegociacion, sedeSeleccionada);
	}

	public void actualizarOpcionCobertura(AreaCoberturaTipoEnum opcionCobertura, Long negociacionId){
		this.negociacionTransactionalService.actualizarOpcionCobertura(opcionCobertura, negociacionId);
	}

	public List<AnexoTarifarioPoblacionDto> obtenerPoblacionPgpAnexo(Long negociacionId) throws ConexiaBusinessException {
		return this.negociacionViewService.obtenerPoblacionPgpAnexo(negociacionId);
	}

	public List<MunicipioDto> obtenerMunicipiosAreaCobertura(Long negociacionId) throws ConexiaBusinessException {
		return this.negociacionViewService.obtenerMunicipiosAreaCobertura(negociacionId);
	}

	public List<MunicipioDto> obtenerMunicipiosAreaCobertura(Long negociacionId, List<Long> sedeNegociacionIds) throws ConexiaBusinessException {
		return this.negociacionViewService.obtenerMunicipiosAreaCobertura(negociacionId, sedeNegociacionIds);
	}

    public void copiarTecnologiasNegociacion(NegociacionDto negociacion, NegociacionDto negociacionAnterior) {
        this.negociacionTransactionalService.copiarTecnologiasPorNegociacion(negociacion, negociacionAnterior);
    }

    public List<NegociacionDto> consultarNegociaciones(Long idNegociacionReferente) {
        return this.negociacionViewService.consultarNegociaciones(idNegociacionReferente);
    }

    public List<NegociacionDto> consultarNegociaciones(String numeroContrato) {
        return this.negociacionViewService.consultarNegociaciones(numeroContrato);
    }

    public boolean existeLegalizaciones(NegociacionDto negociacion) {
        return negociacionViewService.existeLegalizaciones(negociacion);
    }

    public NegociacionDto
        crearNegociacionOtroSi(
                NegociacionDto negociacion,
                Integer userId, TipoOtroSiEnum tipoModificacionOtroSi)
                throws ConexiaBusinessException
    {
        return this.negociacionTransactionalService.crearNegociacionOtroSi(
                negociacion, userId, tipoModificacionOtroSi);
    }

    public void terminarCreacionNegociacionOtroSi(NegociacionDto negociacion, Integer userId,
                                                  TipoOtroSiEnum tipoOtroSi, TipoModificacionOtroSiEnum tipoModificacion,
                                                  Boolean cargarContenido,List<Long> sedePrestadorIdPadre)
            throws ConexiaBusinessException
    {
        this.negociacionTransactionalService.terminarCreacionNegociacionOtroSi(
                negociacion, userId, tipoOtroSi, tipoModificacion, 
                cargarContenido, sedePrestadorIdPadre);
    }

    public void actualizarFechasProrrogaTecnologias(Date fechaInicioProrroga,
            Date fechaFinProrroga,Long negociacionId) throws ConexiaBusinessException
    {
        this.negociacionTransactionalService.actualizarFechasProrrogaTecnologias(
                fechaInicioProrroga, fechaFinProrroga, negociacionId);
    }

    public void finalizarNegociacion(
            NegociacionDto negociacion)
            throws ConexiaBusinessException{
        this.negociacionTransactionalService.finalizarNegociacion(negociacion);
    }

    public void asignarFechasOtroSiContrato(Date fechaInicioOtroSi, Date fechaFinOtroSi,Long negociacionId)
            throws ConexiaBusinessException
    {
        this.negociacionTransactionalService.asignarFechasOtroSiContrato(fechaInicioOtroSi, fechaFinOtroSi, negociacionId);
    }

    public void cambiarFechaContratoByNegociacionId(Date fechaInicioOtroSi, 
            Date fechaFinOtroSi, Long negociacionId) throws ConexiaBusinessException{
        this.negociacionTransactionalService.cambiarFechaContratoByNegociacionId(
                fechaInicioOtroSi, fechaFinOtroSi, negociacionId);
    }

    public NegociacionDto
        consultarFechasVigenciaOtroSi(Long negociacionId) throws ConexiaBusinessException
    {
        return this.negociacionTransactionalService.
                consultarFechasVigenciaOtroSi(negociacionId);
    }

    public Date consultarFechaFinContratoPadre(NegociacionDto negociacion)
            throws ConexiaBusinessException
    {
        return this.negociacionTransactionalService.
                consultarFechaFinContratoPadre(negociacion);
    }

    /**
     * Para determinar si la negociación padre tiene negociaciones otros si pendientes de legalizar
     * @param negociacionPadre
     * @return
     * @throws ConexiaBusinessException
     */
    public Boolean validarNegociacionesOtrosSiSinLegalizar(
            NegociacionDto negociacionPadre)
            throws ConexiaBusinessException
    {
        return this.negociacionTransactionalService.
                validarNegociacionesOtrosSiSinLegalizar(negociacionPadre);
    }
}

