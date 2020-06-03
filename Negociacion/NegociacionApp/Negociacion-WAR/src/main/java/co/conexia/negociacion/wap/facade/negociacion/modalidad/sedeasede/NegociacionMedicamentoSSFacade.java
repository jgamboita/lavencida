package co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede;

import com.conexia.contratacion.commons.constants.enums.ErrorTecnologiasNegociacionEventoEnum;
import com.conexia.contratacion.commons.constants.enums.ErrorTecnologiasNegociacionPgpEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TecnologiaEnum;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasRIasCapitaDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.negociacion.definitions.negociacion.medicamento.NegociacionMedicamentoTransactionalServiceRemote;
import com.conexia.negociacion.definitions.negociacion.medicamento.NegociacionMedicamentoViewServiceRemote;
import com.conexia.negociacion.definitions.negociacion.medicamentoPgp.NegociacionMedicamentoPgpTransactionalServiceRemote;
import com.conexia.negociacion.definitions.tecnologias.ImportarTecnologiasTransactionalServiceRemote;
import com.conexia.servicefactory.CnxService;
import javax.inject.Inject;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class NegociacionMedicamentoSSFacade implements Serializable {

    @Inject
    @CnxService
    private NegociacionMedicamentoViewServiceRemote serviceRemote;

    @Inject
    @CnxService
    private NegociacionMedicamentoTransactionalServiceRemote transactionalServiceRemote;

    @Inject
    @CnxService
    private NegociacionMedicamentoPgpTransactionalServiceRemote negociacionImportarTransactionalService;

    @Inject
    @CnxService
    private ImportarTecnologiasTransactionalServiceRemote importarTecnologiasTransactionalServiceRemote;

    public void asignarTarifas(List<Long> medicamentos, Long negociacionId, Integer userId) {
        this.transactionalServiceRemote.asignarTarifas(medicamentos, negociacionId, userId);
    }

    public int asignarTarifas(List<Long> medicamentos, Long negociacionId, String propiedad, Integer userId) {
        return this.transactionalServiceRemote.asignarTarifas(medicamentos, negociacionId, propiedad, userId);
    }

    public void asignarValorContratoAnterior(Long negociacionId, List<MedicamentoNegociacionDto> medicamento, Integer userId) {
        this.transactionalServiceRemote.asignarValorContratoAnterior(negociacionId, medicamento, userId);
    }
    
    public void asignarValorContratoAnteriorByNegociacionReferente(Long negociacionId,  Long negociacionReferenteId, 
                                                                   List<MedicamentoNegociacionDto> medicamento, 
                                                                   Integer userId) 
    {
        this.transactionalServiceRemote.asignarValorContratoAnteriorByNegociacionReferente(negociacionId, negociacionReferenteId, medicamento, userId);
    }

    public void asignarValorCostoMedio(Long negociacionId, List<MedicamentoNegociacionDto> medicamento, Integer poblacion, boolean aplicarReferente, Integer userId) {
        this.transactionalServiceRemote.asignarValorCostoMedio(negociacionId, medicamento, poblacion, aplicarReferente, userId);
    }

    public void guardarValorReferenteMedicamentosPGP(Long negociacionId, Long grupoTerapeuticoId, List<MedicamentoNegociacionDto> medicamentos, Integer userId) throws ConexiaBusinessException {
        this.transactionalServiceRemote.guardarValorReferenteMedicamentosPGP(negociacionId, grupoTerapeuticoId, medicamentos, userId);
    }

    public void guardarMedicamentosFranjaPGP(Long negociacionId, List<Long> medicamentoIds, Long grupoId, BigDecimal franjaInicio, BigDecimal franjaFin, Integer userId) throws ConexiaBusinessException {
        this.transactionalServiceRemote.guardarMedicamentosFranjaPGP(negociacionId, medicamentoIds, grupoId, franjaInicio,
                franjaFin, userId);
    }

    public void aplicarValorNegociadoByPoblacion(Long negociacionId, Integer userId) throws ConexiaBusinessException {
        this.transactionalServiceRemote.aplicarValorNegociadoByPoblacion(negociacionId, userId);
    }

    public List<MedicamentoNegociacionDto> consultarMedicamentosNegociacionNoSedesByNegociacionId(NegociacionDto negociacion) {
        List<MedicamentoNegociacionDto> listMedicamentoNegociacionDto = new ArrayList<>();
        listMedicamentoNegociacionDto = serviceRemote.consultaMedicamentosNegociacion(negociacion);
        listMedicamentoNegociacionDto.stream().filter(dto -> Objects.nonNull(dto.getValorReferente())).forEach(dto -> {
            if (Objects.nonNull(negociacion.getPoblacion())) {
                dto.setValorReferente(dto.getValorReferente().multiply(BigDecimal.valueOf(negociacion.getPoblacion())));
            }
        });
        return listMedicamentoNegociacionDto;
    }

    public List<MedicamentoNegociacionDto> consultarMedicamentosNegociacionPGPNoSedesByNegociacionAndGrupoId(Long negociacionId, Long grupoId) throws ConexiaBusinessException {
        return serviceRemote.consultarMedicamentosByGrupoAndNegociacionId(negociacionId, grupoId);
    }

    public List<GrupoTerapeuticoNegociacionDto> consultarGruposNegociacionNoSedesByNegociacionId(NegociacionDto negociacion) throws ConexiaBusinessException {
        return serviceRemote.consultaGruposNegociacionPGP(negociacion);
    }

    public Integer eliminarByNegociacionAndMedicamento(final Long negociacionId, final Long medicamentoId, Integer userId) {
        return transactionalServiceRemote.eliminarByNegociacionAndMedicamento(negociacionId, medicamentoId, userId);
    }

    public Integer eliminarByNegociacionAndMedicamento(Long negociacionId, List<Long> medicamentoIds, Long grupoId, Integer userId) {
        return transactionalServiceRemote.eliminarByNegociacionAndMedicamento(negociacionId, medicamentoIds, grupoId, userId);
    }

    public void guardarMedicamentosNegociados(List<MedicamentoNegociacionDto> medicamentos, Long negociacionId, Integer userId) {
        transactionalServiceRemote.guardarMedicamentosNegociados(medicamentos, negociacionId, userId);
    }

    public void guardarMedicamentoNegociado(MedicamentoNegociacionDto medicamento, Long negociacionId, Integer userId) {
        this.transactionalServiceRemote.guardarMedicamentoNegociado(medicamento, negociacionId, userId);
    }

    public void guardarMedicamentoNegociadoPgp(MedicamentoNegociacionDto medicamento, Long negociacionId, Integer userId) {
        this.transactionalServiceRemote.guardarMedicamentoNegociadoPgp(medicamento, negociacionId, userId);
    }

    public List<MedicamentosDto> consultarMedicamentosAgregar(MedicamentosDto medicamento, NegociacionDto negociacion) throws ConexiaBusinessException {
        return this.serviceRemote.consultarMedicamentosAgregar(medicamento, negociacion);
    }

    public void agregarMedicamentosNegociacion(List<Long> medicamentoIds, Long negociacionId, Integer userId, Long negociacionReferenteId) 
            throws ConexiaBusinessException {
        this.transactionalServiceRemote.agregarMedicamentosNegociacion(medicamentoIds, negociacionId, userId, negociacionReferenteId);
    }

    public List<SedesNegociacionDto> consultarSedeNegociacionMedicamentosByNegociacionId(Long negociacionId) {
        return this.serviceRemote.consultarSedeNegociacionMedicamentosByNegociacionId(negociacionId);
    }

    public void almacenarMedicamentosArchivoImportado(List<MedicamentoNegociacionDto> medicamento, Integer userId, NegociacionModalidadEnum negociacionModalidad) throws ConexiaSystemException {
        transactionalServiceRemote.almacenarMedicamentosArchivoImportado(medicamento, userId, negociacionModalidad);
    }

    public boolean tieneServicioFarmaceuticoHabilitado(Long negociacionId) {
        return serviceRemote.tieneServicioFarmaceuticoHabilitado(negociacionId);
    }

    public void guardarGrupoTerapeuticoPGP(Long negociacionId, List<GrupoTerapeuticoNegociacionDto> gruposNegociacion, Integer poblacion, Integer userId) throws ConexiaBusinessException {
        transactionalServiceRemote
                .guardarGrupoTerapeuticoPGP(negociacionId, gruposNegociacion, poblacion, userId);

    }

    public void guardarFranjaPGP(Long negociacionId, List<GrupoTerapeuticoNegociacionDto> gruposNegociacion, BigDecimal franjaInicio, BigDecimal franjaFin, Integer userId) throws ConexiaBusinessException {
        transactionalServiceRemote.guardarFranjaGruposPGP(negociacionId, gruposNegociacion, franjaInicio, franjaFin, userId);
    }

    public void eliminarByNegociacionAndGruposAllMedicamentos(Long negociacionId, List<Long> gruposId, Integer userId) throws ConexiaBusinessException {
        transactionalServiceRemote.eliminarByNegociacionAndGruposAllMedicamentos(negociacionId, gruposId, userId);
    }

    public Integer contarMedicamentosByNegociacionId(Long negociacionId) {
        return this.serviceRemote.contarMedicamentosByNegociacionId(negociacionId);
    }

    public List<ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum>> validarMedicamentoNegociacionPgp(List<ArchivoTecnologiasNegociacionPgpDto> medicamentos, NegociacionDto negociacion, Integer userId)             throws ConexiaBusinessException {
        return this.negociacionImportarTransactionalService.validarMedicamentoNegociacionPgp(medicamentos, negociacion, userId);
    }

    public List<ConcurrentHashMap<ArchivoTecnologiasNegociacionEventoDto, ErrorTecnologiasNegociacionEventoEnum>> validarMedicamentoNegociacionEvento(List<ArchivoTecnologiasNegociacionEventoDto> medicamentos, NegociacionDto negociacion, Integer userId, Boolean importRegulados) throws ConexiaBusinessException {
        return this.negociacionImportarTransactionalService.validarMedicamentoNegociacionEvento(medicamentos, negociacion, userId, importRegulados);

    }

    public List<ArchivoTecnologiasNegociacionEventoDto> retornarMedicamentosReguladosImport(List<ArchivoTecnologiasNegociacionEventoDto> medicamentosImport) throws ConexiaBusinessException {
        return this.negociacionImportarTransactionalService.retornarMedicamentosReguladosImport(medicamentosImport);
    }

    public List<ErroresImportTecnologiasRIasCapitaDto> importarTecnologiasNegociacionRiasCapita(String nombreArchivo, NegociacionDto negociacion, Integer userId, TecnologiaEnum tecnologia) throws ConexiaBusinessException {
        return importarTecnologiasTransactionalServiceRemote.importarTecnologia(nombreArchivo, negociacion, userId, NegociacionModalidadEnum.RIAS_CAPITA, tecnologia);
    }

    public List<ArchivoTecnologiasNegociacionEventoDto> consultarValorReferenteMedicamentos(List<ArchivoTecnologiasNegociacionEventoDto> medicamentosImportar) throws ConexiaBusinessException {
        return (List<ArchivoTecnologiasNegociacionEventoDto>) this.serviceRemote.consultarValorReferenteMedicamentos(medicamentosImportar);
    }

    public List<MedicamentoNegociacionDto> listarMedicamentosReguladosNegociado(Long negociacionId) {
        return this.serviceRemote.listarMedicamentosReguladosNegociado(negociacionId);
    }
}