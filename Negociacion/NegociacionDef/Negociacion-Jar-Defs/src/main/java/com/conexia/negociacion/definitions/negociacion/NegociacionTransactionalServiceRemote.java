package com.conexia.negociacion.definitions.negociacion;

import com.conexia.contratacion.commons.constants.enums.AreaCoberturaTipoEnum;
import com.conexia.contratacion.commons.constants.enums.TipoModificacionOtroSiEnum;
import com.conexia.contratacion.commons.constants.enums.TipoOtroSiEnum;
import com.conexia.contratacion.commons.dto.maestros.AfiliadoDto;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.exceptions.ConexiaBusinessException;
import java.util.Date;
import java.util.List;

/**
 * Interface remota transactional para el boundary de negociacion
 *
 * @author jjoya
 */
public interface NegociacionTransactionalServiceRemote {

    void actualizarPoblacion(Long negociacionId);

    Long crearNegociacion(NegociacionDto negociacion);

    void crearReglaNegociacion(ReglaNegociacionDto reglaNegociacion) throws ConexiaBusinessException;

    Integer eliminarReglaNegociacion(long reglaId) throws ConexiaBusinessException;

    Integer actualizarReglaNegociacion(ReglaNegociacionDto reglaNegociacion) throws ConexiaBusinessException;

    Integer eliminarNegociacion(Long negociacionId);

    void eliminarSedeNegociacion(Long negociacionId);

    NegociacionDto terminarBaseNegociacion(NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException;
 
    void terminarBaseNegociacionPGP(NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException;

    void actualizarAgrupadoresNegociacionPGP(Long negociacionId, Integer userId) throws ConexiaBusinessException;

    Long crearInvitacionNegociacion(InvitacionNegociacionDto invitacionNegociacion);

    Long actualizarInvitacionNegociacion(InvitacionNegociacionDto invitacionNegociacion);

    void finalizarNegociacion(NegociacionDto negociacion) throws ConexiaBusinessException;

    void asignarFechasOtroSiContrato(Date fechaInicioOtroSi, Date fechaFinOtroSi,Long negociacionId) throws ConexiaBusinessException;

    void guardarDatosCapita(NegociacionDto negociacion);

    Long guardarIncentivo(IncentivoModeloDto incentivo);

    void eliminarIncentivo(Long incentivoId);

    void eliminarModelo(Long modeloId);

    Long guardarModelo(IncentivoModeloDto modelo);

    void cambiarModalidadNegociacion(NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException;

    void actualizarValorTotal(NegociacionDto negociacion);

	void actualizarObservacionEvento(NegociacionDto negociacion);

	void actualizarFechaConcertacionMedicamentos(NegociacionDto negociacion);

	void actualizarFechaConcertacionProcedimientos(NegociacionDto negociacion);

	void actualizarFechaConcertacionPaquete(NegociacionDto negociacion);

    void establecerFechasConcertacion(NegociacionDto negociacion);

    void actualizarPoblacionNegociacion(NegociacionDto negociacion);

    void marcarNegociacionEnCreacion(final NegociacionDto negociacionDto, final Boolean enCreacion);

    void clonarNegociacion(ClonarNegociacionDto clonarNegociacion);

    void guardarHistorialNegociacion(Integer userId, Long negociacionId, String evento);

    boolean verificarPortafolioCapitaByPrestador(Long prestadorId);

    List<ProcedimientoDto> consultarProcedimientosSinValorPGP(Long negociacionId);

    List<ProcedimientoDto> consultaProcedimientosSinNegociaRiaCapitaGrupoEtario(NegociacionDto negociacion);

    void guardarDatosPgp(NegociacionDto negociacion) throws ConexiaBusinessException;

    void guardarDatosRiasCapita(NegociacionDto negociacion);

    void guardarActividades(List<NegociacionRiaActividadMetaDto> listaActividades);

    void validarNegociacionCapita(NegociacionDto negociacion, Integer userId);

    void validarNegociacionCapitaPorRias(NegociacionDto negociacion, Integer userId);

    void actualizarValorNegociadosRiasNegociacion(List<NegociacionRiaRangoPoblacionDto> negociacionRiaRango);

    void registrarDescargaAnexo(Long negociacionId, Integer userId, String nombreAnexo);

    void updateTecnologiaAgregadasOtroSi(NegociacionDto negociacion) throws ConexiaBusinessException;

    boolean importarPoblacionCapitaGrupoEtareo(List<AfiliadoDto> poblacion, NegociacionDto negociacion) throws Exception;

    void actualizarValoresDestoUpcNegociadaGrupoEtario(NegociacionRiaRangoPoblacionDto negociacionRiaRangoPoblacionDto);

    void copiaTecnologiasRiaCapitaSegunReferente(NegociacionDto negociacion, Integer userId);

    void eliminarTecnologiasNegociacionPGP(Long negociacionId, Integer userId) throws ConexiaBusinessException;

    void agregarMunicipiosNegociacion(Long negociacionId, Integer negociacionMunicipio) throws ConexiaBusinessException;

    void eliminarMunicipioNegociacionById(Long negociacionId, Integer municipioId) throws ConexiaBusinessException;

    void eliminarMunicipiosSinPoblacion(Long negociacionId) throws ConexiaBusinessException;

    void guardarFechaCorte(Date fechaCorte, Long negociacionId) throws ConexiaBusinessException;

    void addPoblacionFechaCorteMunicipioPGP(NegociacionDto negociacion, List<ReglaNegociacionDto> reglasNegociacion, List<SedesNegociacionDto> sedeSeleccionada) throws ConexiaBusinessException;

    void actualizarOpcionCobertura(AreaCoberturaTipoEnum opcionCobertura, Long negociacionId);

    Boolean habilitarContrato(NegociacionConsultaContratoDto selDTo);

    String eliminaContrato(NegociacionConsultaContratoDto EliminarContratoDto);

    void copiarTecnologiasPorNegociacion(NegociacionDto negociacionActual, NegociacionDto negociacionAnterior);
    
    void terminarCreacionNegociacionOtroSi(NegociacionDto negociacion, Integer userId,
                                                  TipoOtroSiEnum tipoOtroSi, TipoModificacionOtroSiEnum tipoModificacion,
                                                  Boolean cargarContenido,List<Long> sedePrestadorIdPadre)throws  ConexiaBusinessException;
    
    void actualizarFechasProrrogaTecnologias(Date fechaInicioProrroga,
            Date fechaFinProrroga,Long negociacionId) throws  ConexiaBusinessException;
    
    void cambiarFechaContratoByNegociacionId(Date fechaInicioOtroSi, 
            Date fechaFinOtroSi,Long negociacionId) throws  ConexiaBusinessException;;
    
    NegociacionDto
        consultarFechasVigenciaOtroSi(Long negociacionId) throws  ConexiaBusinessException;
    
    Date consultarFechaFinContratoPadre(
            NegociacionDto negociacion)
            throws  ConexiaBusinessException;
    
    Boolean validarNegociacionesOtrosSiSinLegalizar(
            NegociacionDto negociacionPadre)
            throws  ConexiaBusinessException;
    
    NegociacionDto
        crearNegociacionOtroSi(
                NegociacionDto negociacion,
                Integer userId, TipoOtroSiEnum tipoModificacionOtroSi)
                 throws  ConexiaBusinessException;
}
