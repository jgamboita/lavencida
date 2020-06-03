package co.conexia.negociacion.services.negociacion.medicamentoPgp.boundary;

import co.conexia.negociacion.services.negociacion.control.NegociacionControl;
import com.conexia.contratacion.commons.constants.enums.ErrorTecnologiasNegociacionEventoEnum;
import com.conexia.contratacion.commons.constants.enums.ErrorTecnologiasNegociacionPgpEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionMedicamentoDto;
import com.conexia.contratacion.commons.dto.maestros.CategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.negociacion.medicamentoPgp.NegociacionMedicamentoPgpTransactionalServiceRemote;
import co.conexia.negociacion.services.negociacion.medicamento.control.NegociacionMedicamentoControl;
import org.jboss.ejb3.annotation.TransactionTimeout;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Boundary para importar medicamentos a una negociación PGP
 *
 * @author clozano
 */
@Stateless
@Remote(NegociacionMedicamentoPgpTransactionalServiceRemote.class)
public class NegociacionMedicamentoImportarTransactionalBoundary implements NegociacionMedicamentoPgpTransactionalServiceRemote {

    @Inject
    private NegociacionMedicamentoControl control;
    @Inject
    private NegociacionControl negociacionControl;

    private List<ConcurrentHashMap<List<Long>, MedicamentoNegociacionDto>> registrosCorrectosUpdate;
    private List<ConcurrentHashMap<List<Long>, MedicamentoNegociacionDto>> registrosCorrectosInsert;
    private List<String> medicamentosCorrectos;
    private List<Long> allSedeGrupoIds;
    private List<Long> newGrupoIds;

    @Override
    @TransactionTimeout(value = 30, unit = TimeUnit.MINUTES)
    public List<ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum>> validarMedicamentoNegociacionPgp(List<ArchivoTecnologiasNegociacionPgpDto> medicamentos, NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException {

        List<ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum>> errores
                = new ArrayList<ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum>>();
        List<ConcurrentHashMap<List<Long>, MedicamentosDto>> medicamentosAEliminar = new ArrayList<>();
        registrosCorrectosInsert = new ArrayList<ConcurrentHashMap<List<Long>, MedicamentoNegociacionDto>>();
        registrosCorrectosUpdate = new ArrayList<ConcurrentHashMap<List<Long>, MedicamentoNegociacionDto>>();
        allSedeGrupoIds = new ArrayList<Long>();
        newGrupoIds = new ArrayList<Long>();
        medicamentosCorrectos = new ArrayList<String>();

        List<Long> medicamentoIdsHabilitacion = control.consultarIdsMedicamentosHabilitacionPGP(negociacion);
        List<MedicamentoNegociacionDto> medicamentosNegociacion = control.consultarMedicamentosNegociacionPGP(negociacion.getId());

        for (ArchivoTecnologiasNegociacionPgpDto medicamentoArchivo : medicamentos) {

            ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum> error = new ConcurrentHashMap<>();

            if (!medicamentosCorrectos.contains(medicamentoArchivo.getCodigoTecnologiaUnicaEmssanar())) {
                MedicamentosDto medicamentoPGP = control.consultarMedicamentoByCodigoCUM(medicamentoArchivo.getCodigoTecnologiaUnicaEmssanar());

                //Verificar si el procedimiento existe
                if (Objects.isNull(medicamentoPGP)) {
                    error.put(medicamentoArchivo, ErrorTecnologiasNegociacionPgpEnum.TECNOLOGIA_NO_EXISTE);
                } else {
                    String si = "SI";
                    String no = "NO";
                    if (!si.equals(medicamentoArchivo.getEliminarTecnologia()) && !no.equals(medicamentoArchivo.getEliminarTecnologia())) {
                        error.put(medicamentoArchivo, ErrorTecnologiasNegociacionPgpEnum.VALOR_NO_PERMITIDO_ELIMINAR);
                    } else {
                        if (!medicamentoArchivo.getCodigoTecnologiaUnicaEmssanar().equals(medicamentoPGP.getCums())) {
                            error.put(medicamentoArchivo, ErrorTecnologiasNegociacionPgpEnum.CODIGO_UNICO_TECNOLOGIA_NO_CORRESPONDE);
                        } else {
                            if (1 != medicamentoPGP.getEstadoCums()) {
                                error.put(medicamentoArchivo, ErrorTecnologiasNegociacionPgpEnum.TECNOLOGIA_INACTIVA);
                            } else {
                                Boolean enHabilitacion = false;
                                for (Long id : medicamentoIdsHabilitacion) {
                                    if (id.equals(medicamentoPGP.getId())) {
                                        enHabilitacion = true;
                                    }
                                }

                                if (!enHabilitacion) {
                                    error.put(medicamentoArchivo, ErrorTecnologiasNegociacionPgpEnum.MEDICAMENTO_NO_HABILITADO);
                                } else {

                                    Boolean enNegociacion = false;
                                    if (Objects.nonNull(medicamentosNegociacion)) {
                                        for (MedicamentoNegociacionDto mtoNegociacion : medicamentosNegociacion) {
                                            if (mtoNegociacion.getMedicamentoDto().getId().equals(medicamentoPGP.getId())) {
                                                enNegociacion = true;
                                            }
                                        }
                                    }

                                    //consultar grupo terapéutico
                                    Long grupoId = control.consultarGrupoByMedicamentoCUM(medicamentoArchivo.getCodigoTecnologiaUnicaEmssanar());
                                    if (Objects.isNull(grupoId) || grupoId == 0) {
                                        error.put(medicamentoArchivo, ErrorTecnologiasNegociacionPgpEnum.GRUPO_NO_ENCONTRADO);
                                    } else {
                                        CategoriaMedicamentoDto grupoDto = new CategoriaMedicamentoDto();
                                        grupoDto.setId(grupoId);
                                        medicamentoPGP.setCategoriaMedicamento(grupoDto);

                                        List<Long> sedeGrupoIds = control.consultarSedeGrupoIdsByNegociacionAndGrupo(negociacion.getId(), grupoId);

                                        try {
                                            negociacionControl.validarNumericos(error, medicamentoArchivo);
                                        } catch (NumberFormatException | ParseException e) {
                                            error.put(medicamentoArchivo, ErrorTecnologiasNegociacionPgpEnum.VALOR_NUMERICO_NO_VALIDO);
                                        }

                                        if (enNegociacion) { //Si está en negociación se valida que si exista una sedeNegociacionGrupoTeraputico para el medicamento
                                            if (si.equals(medicamentoArchivo.getEliminarTecnologia())) {
                                                ConcurrentHashMap<List<Long>, MedicamentosDto> mtoEliminar
                                                        = new ConcurrentHashMap<List<Long>, MedicamentosDto>();
                                                mtoEliminar.put(sedeGrupoIds, medicamentoPGP);
                                                medicamentosAEliminar.add(mtoEliminar);
                                            } else {
                                                if (Objects.isNull(sedeGrupoIds)) {
                                                    error.put(medicamentoArchivo, ErrorTecnologiasNegociacionPgpEnum.SEDE_NEGOCIACION_NO_ENCONTRADA);
                                                } else {
                                                    if (error.size() == 0) {
                                                        addRegistroValido(2, sedeGrupoIds, medicamentoArchivo, medicamentoPGP, negociacion);
                                                    }
                                                }
                                            }

                                        } else {
                                            //Si el medicamento no está en negociación se debe crear una sedeNegociacionGrupoTerapeutico para
                                            //ser relacionada en la sedeNegociacionMedicamento donde se insertará en el medicamento importado
                                            //sólo en el caso de que el grupo no exista en la negociación

                                            if (si.equals(medicamentoArchivo.getEliminarTecnologia())) {
                                                error.put(medicamentoArchivo, ErrorTecnologiasNegociacionPgpEnum.ELIMINAR_INVALIDO_NO_NEGOCIACION);
                                            } else {
                                                Boolean existeSedeCapitulo = false;
                                                for (MedicamentoNegociacionDto mtoNegociacion : medicamentosNegociacion) {
                                                    if (mtoNegociacion.getMedicamentoDto().getCategoriaMedicamento().getId().equals(grupoId)) {
                                                        existeSedeCapitulo = true;
                                                    }
                                                }

                                                if (!existeSedeCapitulo) {

                                                    if (!newGrupoIds.contains(grupoId)) {//para que no se creen duplicados en base a los grupos encontrados

                                                        newGrupoIds.add(grupoId);
                                                        List<Long> sedeNegociacionIds = new ArrayList<Long>();
                                                        negociacion.getSedesNegociacion().stream().forEach(
                                                                sn -> {
                                                                    sedeNegociacionIds.add(sn.getId());
                                                                }
                                                        );

                                                        List<Long> newSedeGrupoIds = new ArrayList<Long>();
                                                        //Se crean las nuevas sedeNegociacionGrupoTerapeutico y se recuperan los id
                                                        newSedeGrupoIds = control.crearSedesNegociacionGrupoTerapeutico(sedeNegociacionIds, grupoId, userId);
                                                        if (error.size() == 0) {
                                                            addRegistroValido(1, newSedeGrupoIds, medicamentoArchivo, medicamentoPGP, negociacion);
                                                        }
                                                    } else {
                                                        if (error.size() == 0) {
                                                            addRegistroValido(1, sedeGrupoIds, medicamentoArchivo, medicamentoPGP, negociacion);
                                                        }
                                                    }

                                                } else {
                                                    //insertar procedimiento con sedeNegociacionCapituloIds encontrados
                                                    if (error.size() == 0) {
                                                        addRegistroValido(1, sedeGrupoIds, medicamentoArchivo, medicamentoPGP, negociacion);
                                                    }

                                                }
                                            }

                                        }
                                    }

                                }

                            }
                        }
                    }
                }

            } else {
                error.put(medicamentoArchivo, ErrorTecnologiasNegociacionPgpEnum.TECNOLOGIA_REPETIDA);
            }

            //Se valida que el list de errores realmente contenga un registro de procedimientos que no pasó las validaciones
            if (Objects.nonNull(error) && error.size() > 0) {
                errores.add(error);
            }

        }

        /**
         * Sección para insertar o actualizar los procedimientos importados
         */
        if (registrosCorrectosInsert.size() > 0) {
            insertarMedicamentosEnNegociacion(registrosCorrectosInsert, negociacion);
            for (ConcurrentHashMap<List<Long>, MedicamentoNegociacionDto> mto : registrosCorrectosInsert) {
                for (ConcurrentHashMap.Entry<List<Long>, MedicamentoNegociacionDto> registro : mto.entrySet()) {
                    for (long sncId : registro.getKey()) {
                        allSedeGrupoIds.add(sncId);
                    }
                }
            }
        }

        if (registrosCorrectosUpdate.size() > 0) {
            actualizarMedicamentosEnNegociacion(registrosCorrectosUpdate, negociacion);
            for (ConcurrentHashMap<List<Long>, MedicamentoNegociacionDto> mto : registrosCorrectosUpdate) {
                for (ConcurrentHashMap.Entry<List<Long>, MedicamentoNegociacionDto> registro : mto.entrySet()) {
                    for (Long sncId : registro.getKey()) {
                        allSedeGrupoIds.add(sncId);
                    }
                }
            }
        }

        //---- Sección para eliminar los procedimientos
        if (medicamentosAEliminar.size() > 0) {
            List<MedicamentosDto> mtosEliminar = new ArrayList<MedicamentosDto>();
            for (ConcurrentHashMap<List<Long>, MedicamentosDto> mto : medicamentosAEliminar) {
                for (ConcurrentHashMap.Entry<List<Long>, MedicamentosDto> registro : mto.entrySet()) {
                    for (Long sncId : registro.getKey()) {
                        allSedeGrupoIds.add(sncId);
                    }
                    mtosEliminar.add(registro.getValue());
                }
            }
            eliminarMedicamentosEnNegociacion(mtosEliminar, negociacion.getId(), userId);
        }

        //----- Sección para actualizar los valores de las sedeNegociacionGrupoTerapeutico que aún esten en negociación
        if (registrosCorrectosInsert.size() > 0 || registrosCorrectosUpdate.size() > 0) {
            actualizarGruposEnNegociacion(negociacion.getId(), userId);
        }

        return errores;

    }

    @TransactionTimeout(value = 30, unit = TimeUnit.MINUTES)
    public List<ConcurrentHashMap<ArchivoTecnologiasNegociacionEventoDto, ErrorTecnologiasNegociacionEventoEnum>> validarMedicamentoNegociacionEvento(List<ArchivoTecnologiasNegociacionEventoDto> medicamentos, NegociacionDto negociacion, Integer userId, Boolean importRegulados) throws ConexiaBusinessException {

        List<ConcurrentHashMap<ArchivoTecnologiasNegociacionEventoDto, ErrorTecnologiasNegociacionEventoEnum>> errores = new ArrayList<>();
        List<ConcurrentHashMap<ArchivoTecnologiasNegociacionEventoDto, ErrorTecnologiasNegociacionEventoEnum>> erroresFinal = new ArrayList<>();
        List<ConcurrentHashMap<ArchivoTecnologiasNegociacionEventoDto, ErrorTecnologiasNegociacionEventoEnum>> noProcesados = new ArrayList<>();

        List<ArchivoTecnologiasNegociacionEventoDto> registrosCorrectosInsert = new ArrayList<>();
        List<ArchivoTecnologiasNegociacionEventoDto> registrosCorrectosInsertRegulados = new ArrayList<>();
        List<ArchivoTecnologiasNegociacionEventoDto> registrosCorrectosUpdate = new ArrayList<>();
        List<ArchivoTecnologiasNegociacionEventoDto> registrosCorrectosUpdateRegulados = new ArrayList<>();

        ConcurrentHashMap<ArchivoTecnologiasNegociacionEventoDto, ErrorTecnologiasNegociacionEventoEnum> error = new ConcurrentHashMap<>();
        List<Long> sedesHabilitadas = new ArrayList<>();
        List<SedesNegociacionDto> sedesNegociacion = this.negociacionControl.consultarSedesNegociacion(negociacion.getId());
        //verifica si la sede tiene el servicio farmaceutica habilitada en reps
        String codigoServicio = "714";
        List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadosRespNoRepsDtos = this.negociacionControl.consultarSedeHabilitadaMxId(negociacion, Collections.singletonList(codigoServicio));
        List<MedicamentosDto> medicinesByCodes = control.getMedicinesByCodes(medicamentos.stream().map(ArchivoTecnologiasNegociacionEventoDto::getCodigoMedicamento).distinct().collect(Collectors.toList()));

        if (sedesNegociacion.stream().noneMatch(sedesNegociacionDto -> serviciosHabilitadosRespNoRepsDtos.stream().anyMatch(servicios -> Objects.equals(servicios.getSedeNegociacionId(), sedesNegociacionDto.getId())))) {
            medicamentos.forEach(medicamentoArchivo -> {
                ConcurrentHashMap<ArchivoTecnologiasNegociacionEventoDto, ErrorTecnologiasNegociacionEventoEnum> errorInactivo = new ConcurrentHashMap<>();
                errorInactivo.put(medicamentoArchivo, ErrorTecnologiasNegociacionEventoEnum.SEDE_SIN_SERVICIO_MEDICAMENTO);
                errores.add(errorInactivo);
            });
        }

        List<SedeNegociacionMedicamentoDto> sedeNegociacionMedicamentoDtos = this.control.consultarSedeMxId(sedesNegociacion.stream().map(SedesNegociacionDto::getId).collect(Collectors.toList()),
                medicamentos.stream().map(ArchivoTecnologiasNegociacionEventoDto::getCodigoMedicamento).collect(Collectors.toList()));

        for (SedesNegociacionDto sede : sedesNegociacion) {
            if (serviciosHabilitadosRespNoRepsDtos.stream().anyMatch(servicios -> Objects.equals(servicios.getSedeNegociacionId(), sede.getId()))) {
                for (ArchivoTecnologiasNegociacionEventoDto medicamentoArchivo : medicamentos) {
                    if ((medicamentoArchivo.getCodigoMedicamento().isEmpty() || Objects.isNull(medicamentoArchivo.getCodigoMedicamento())
                            || (medicamentoArchivo.getValorNegociado().isEmpty() || Objects.isNull(medicamentoArchivo.getValorNegociado())))) {
                        error.put(medicamentoArchivo, ErrorTecnologiasNegociacionEventoEnum.CAMPOS_OBLIGATORIOS_MX);
                        errores.add(error);
                        error = new ConcurrentHashMap<>();
                    }
                    // Verificar si el medicamento existe
                    if (medicinesByCodes.stream().noneMatch(medicamentosDto -> Objects.equals(medicamentosDto.getCums(), medicamentoArchivo.getCodigoMedicamento()))) {
                        error.put(medicamentoArchivo, ErrorTecnologiasNegociacionEventoEnum.MEDICAMENTO_NO_EXISTE);
                        errores.add(error);
                        error = new ConcurrentHashMap<>();
                    }
                    if (medicinesByCodes.stream().anyMatch(medicamentosDto -> Objects.equals(medicamentosDto.getCums(), medicamentoArchivo.getCodigoMedicamento()) && medicamentosDto.getEstadoCums() != 1)) {
                        error.put(medicamentoArchivo, ErrorTecnologiasNegociacionEventoEnum.MEDICAMENTO_VENCIDO_INACTIVO);
                        errores.add(error);
                        error = new ConcurrentHashMap<>();
                    }
                    if (medicinesByCodes.stream().anyMatch(medicamentosDto -> Objects.equals(medicamentosDto.getCums(), medicamentoArchivo.getCodigoMedicamento()) && medicamentosDto.getMuestraMedica().equals(Boolean.TRUE))) {
                        error.put(medicamentoArchivo, ErrorTecnologiasNegociacionEventoEnum.MEDICAMENTO_MUESTRA_MEDICA);
                        errores.add(error);
                        error = new ConcurrentHashMap<>();
                    }
                    if (Objects.nonNull(medicamentoArchivo.getValorNegociado())) {
                        try {
                            medicamentoArchivo.setValorNegociado(medicamentoArchivo.getValorNegociado().replace(",", "."));
                            medicamentoArchivo.setValorImportado(medicamentoArchivo.getValorImportado().replace(",", "."));
                            BigDecimal valor = new BigDecimal(medicamentoArchivo.getValorNegociado()).setScale(0, BigDecimal.ROUND_HALF_UP);
                            BigDecimal valorImp = new BigDecimal(medicamentoArchivo.getValorImportado()).setScale(0, BigDecimal.ROUND_HALF_UP);
                            medicamentoArchivo.setValorNegociado(valor.toString());
                            medicamentoArchivo.setValorImportado(valorImp.toString());

                            if (medicamentoArchivo.getValorNegociado().isEmpty()
                                    || Objects.isNull(medicamentoArchivo.getValorNegociado())
                                    || medicamentoArchivo.getValorNegociado().equals(" ")) {
                                error.put(medicamentoArchivo, ErrorTecnologiasNegociacionEventoEnum.VALOR_NEGOCIADO_NO_PERMITIDO);
                                errores.add(error);
                                error = new ConcurrentHashMap<>();
                            } else if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                                error.put(medicamentoArchivo, ErrorTecnologiasNegociacionEventoEnum.VALOR_NEGOCIADO_NO_PERMITIDO);
                                errores.add(error);
                                error = new ConcurrentHashMap<>();
                            } else if (valorImp.compareTo(BigDecimal.ZERO) <= 0) {
                                error.put(medicamentoArchivo, ErrorTecnologiasNegociacionEventoEnum.VALOR_NEGOCIADO_NO_PERMITIDO);
                                errores.add(error);
                                error = new ConcurrentHashMap<>();
                            }

                        } catch (NumberFormatException excepcion) {
                            error.put(medicamentoArchivo, ErrorTecnologiasNegociacionEventoEnum.VALOR_NEGOCIADO_NO_PERMITIDO);
                            errores.add(error);
                            error = new ConcurrentHashMap<>();
                        }
                    }
                    if (medicinesByCodes.stream().anyMatch(medicamentosDto -> Objects.equals(medicamentosDto.getCums(), medicamentoArchivo.getCodigoMedicamento()))) {
                        if (sedeNegociacionMedicamentoDtos.stream().anyMatch(sedeNegociacionMedicamento -> Objects.equals(sedeNegociacionMedicamento.getSedeNegociacion().getId(), sede.getId())
                                && Objects.equals(sedeNegociacionMedicamento.getMedicamento().getCums(), medicamentoArchivo.getCodigoMedicamento()))) {
                            registrosCorrectosUpdate.add(medicamentoArchivo);
                            sedesHabilitadas.add(sede.getId());
                        } else {
                            registrosCorrectosInsert.add(medicamentoArchivo);
                            sedesHabilitadas.add(sede.getId());
                        }
                    }
                    if (medicinesByCodes.stream().anyMatch(medicamentosDto -> Objects.equals(medicamentosDto.getCums(), medicamentoArchivo.getCodigoMedicamento()) && medicamentosDto.getRegulado().equals(Boolean.TRUE))) {
                        registrosCorrectosInsertRegulados.add(medicamentoArchivo);
                        registrosCorrectosUpdateRegulados.add(medicamentoArchivo);
                    }
                    erroresFinal.addAll(errores);
                    errores.clear();
                    noProcesados.addAll(erroresFinal);
                    erroresFinal.clear();
                }
            } else {
                erroresFinal.addAll(errores);
                errores.clear();
                noProcesados.addAll(erroresFinal);
                erroresFinal.clear();
            }
        }
        List<Long> sedesHabilitasFinal = sedesHabilitadas.stream().distinct().collect(Collectors.toList());
        if (Objects.nonNull(importRegulados) && importRegulados.equals(Boolean.TRUE)) {
            if (registrosCorrectosInsert.size() > 0) {
                this.control.insertarMedicamentosImportNegociacionEvento(registrosCorrectosInsert, sedesHabilitasFinal, userId);
            }
            if (registrosCorrectosUpdate.size() > 0) {
                this.control.actualizarMedicamentosImportEvento(registrosCorrectosUpdate, sedesHabilitasFinal, userId);
            }
        }
        registrosCorrectosInsert.removeAll(registrosCorrectosInsertRegulados);
        registrosCorrectosUpdate.removeAll(registrosCorrectosUpdateRegulados);
        if (registrosCorrectosInsert.size() > 0) {
            this.control.insertarMedicamentosImportNegociacionEvento(registrosCorrectosInsert, sedesHabilitasFinal, userId);
        }
        if (registrosCorrectosUpdate.size() > 0) {
            this.control.actualizarMedicamentosImportEvento(registrosCorrectosUpdate, sedesHabilitasFinal, userId);
        }
        registrosCorrectosInsert.clear();
        registrosCorrectosUpdate.clear();
        return noProcesados.stream().distinct().collect(Collectors.toList());
    }

    public List<ArchivoTecnologiasNegociacionEventoDto> retornarMedicamentosReguladosImport(List<ArchivoTecnologiasNegociacionEventoDto> medicamentosImport) throws ConexiaBusinessException {
        List<ArchivoTecnologiasNegociacionEventoDto> medicamentosRegulados = new ArrayList<>();
        for (ArchivoTecnologiasNegociacionEventoDto medicamento : medicamentosImport) {
            MedicamentosDto medicamentoEvento = control.consultarMedicamentoByCodigoCUM(medicamento.getCodigoMedicamento());
            if (Objects.nonNull(medicamentoEvento) && medicamentoEvento.getRegulado().equals(Boolean.TRUE)) {
                medicamentosRegulados.add(medicamento);
            }
        }
        return medicamentosRegulados;

    }

    private void addRegistroValido(Integer opcion, List<Long> sedeCapituloIds, ArchivoTecnologiasNegociacionPgpDto procedimientoArchivo, MedicamentosDto procedimientoPGP, NegociacionDto negociacion) {
        ConcurrentHashMap<List<Long>, MedicamentoNegociacionDto> registro = new ConcurrentHashMap<>();
        registro.put(sedeCapituloIds, construirMedicamentoNegociacion(procedimientoArchivo,
                procedimientoPGP,
                negociacion));

        switch (opcion) {
            case 1: //insert
                registrosCorrectosInsert.add(registro);
                break;
            case 2: //update
                registrosCorrectosUpdate.add(registro);
                break;
            default:
                break;
        }
        medicamentosCorrectos.add(procedimientoPGP.getCums());
    }

    private void actualizarGruposEnNegociacion(Long negociacionId, Integer userId) throws ConexiaBusinessException {
        Map<Long, Long> mapSngtIds = new HashMap<Long, Long>();
        allSedeGrupoIds.stream().forEach(
                sngtId -> {
                    if (!mapSngtIds.containsKey(sngtId)) {
                        mapSngtIds.put(sngtId, sngtId);
                    }
                }
        );

        if (Objects.nonNull(mapSngtIds)) {

            allSedeGrupoIds.clear();
            allSedeGrupoIds = (List<Long>) mapSngtIds.values().stream()
                    .sorted((sngt1, sngt2) -> Long.compare(sngt1, sngt2))
                    .collect(Collectors.toList());

            //Actualizar calculo de agrupador(sedeNegociacionGrupoTerapeutico)
            this.control.actualizarValoresGruposBySngtId(negociacionId, allSedeGrupoIds, userId);
        }
    }

    private void eliminarMedicamentosEnNegociacion(List<MedicamentosDto> medicamentos, Long negociacionId, Integer userId) throws ConexiaBusinessException {
        List<Long> gruposEliminarIds = new ArrayList<Long>();
        List<Long> medicamentosEliminarIds = new ArrayList<Long>();

        for (MedicamentosDto mtoEliminar : medicamentos) {
            medicamentosEliminarIds.add(mtoEliminar.getId());
            if (!gruposEliminarIds.contains(mtoEliminar.getCategoriaMedicamento().getId())) {
                gruposEliminarIds.add(mtoEliminar.getCategoriaMedicamento().getId());
            }
        }

        //Eliminar procedimientos
        this.control.eliminarMedicamentosNegociacionByImport(
                medicamentosEliminarIds,
                gruposEliminarIds,
                negociacionId,
                userId);
        //Eliminar grupo que no tengan procedimientos
        this.control.eliminarSedeGruposSinMedicamentos(gruposEliminarIds, negociacionId, userId);
    }

    private void insertarMedicamentosEnNegociacion(List<ConcurrentHashMap<List<Long>, MedicamentoNegociacionDto>> medicamentos, NegociacionDto negociacion) throws ConexiaBusinessException {
        if (negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)) {
            this.control.insertarMedicamentosNegociacionPGP(medicamentos, negociacion.getReferenteDto().getId());
        }
    }

    private void actualizarMedicamentosEnNegociacion(List<ConcurrentHashMap<List<Long>, MedicamentoNegociacionDto>> medicamentos, NegociacionDto negociacion) throws ConexiaBusinessException {
        if (negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)) {
            this.control.actualizarMedicamentosNegociacionPGP(medicamentos, negociacion.getReferenteDto().getId());
        }
    }

    private MedicamentoNegociacionDto construirMedicamentoNegociacion(ArchivoTecnologiasNegociacionPgpDto medicamentoArchivo, MedicamentosDto mtoPGP, NegociacionDto negociacion) {
        MedicamentoNegociacionDto mtoImport = new MedicamentoNegociacionDto();
        MedicamentosDto mto = new MedicamentosDto();
        mto.setId(mtoPGP.getId());
        mtoImport.setMedicamentoDto(mto);

        if (Objects.nonNull(medicamentoArchivo.getFrecuencia())) {
            mtoImport.setFrecuencia(new Double(medicamentoArchivo.getFrecuencia()));
        }

        if (Objects.nonNull(medicamentoArchivo.getCmu())) {
            mtoImport.setCostoMedioUsuario(new BigDecimal(medicamentoArchivo.getCmu()));
        }

        if (Objects.nonNull(medicamentoArchivo.getFranjaInicio()) && Objects.nonNull(medicamentoArchivo.getFranjaFin())) {
            mtoImport.setFranjaInicio(new BigDecimal(medicamentoArchivo.getFranjaInicio()));
            mtoImport.setFranjaFin(new BigDecimal(medicamentoArchivo.getFranjaFin()));
        }

        if (Objects.nonNull(medicamentoArchivo.getFrecuencia())
                && Objects.nonNull(medicamentoArchivo.getCmu())
                && Objects.nonNull(negociacion.getPoblacion())) {
            mtoImport.setValorNegociado(new BigDecimal(mtoImport.getFrecuencia())
                    .multiply(mtoImport.getCostoMedioUsuario())
                    .multiply(new BigDecimal(negociacion.getPoblacion())));
            mtoImport.setNegociado(true);
        } else {
            mtoImport.setNegociado(false);
        }

        return mtoImport;
    }

    @Override
    public void clearMaps() {
        registrosCorrectosInsert.clear();
        registrosCorrectosUpdate.clear();
        allSedeGrupoIds.clear();
        newGrupoIds.clear();
        medicamentosCorrectos.clear();
    }
}
