
package co.conexia.negociacion.services.negociacion.procedimientoPgp.boundary;

import co.conexia.negociacion.services.negociacion.control.ImportarProcedimientosControl;
import co.conexia.negociacion.services.negociacion.control.NegociacionControl;
import co.conexia.negociacion.services.negociacion.servicio.control.ServicioNegociacionControl;
import com.conexia.contratacion.commons.constants.enums.*;
import com.conexia.contractual.model.contratacion.Tarifario;
import com.conexia.contractual.model.contratacion.negociacion.SedeNegociacionProcedimiento;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedeNegociacionServicioDto;
import com.conexia.contratacion.commons.dto.maestros.*;
import com.conexia.contratacion.commons.dto.negociacion.*;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasEventoDto;
import com.conexia.contratacion.commons.dto.negociacion.importar.ErroresImportTecnologiasRIasCapitaDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteProcedimientoDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;
import com.conexia.negociacion.definitions.negociacion.procedimientoPgp.NegociacionProcedimientoPgpTransactionalServiceRemote;
import org.jboss.ejb3.annotation.TransactionTimeout;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Boundary para importar procedimientos a una negociación PGP
 * @author clozano
 *
 */
@Stateless
@Remote(NegociacionProcedimientoPgpTransactionalServiceRemote.class)
public class NegociacionProcedimientoImportarTransactionalBoundary implements NegociacionProcedimientoPgpTransactionalServiceRemote {

    @Inject
	private ServicioNegociacionControl control;
	@Inject
	private NegociacionControl negociacionControl;
	@Inject
	private ImportarProcedimientosControl importarProcedimientosControl;
    @Inject
    private Log log;

	private List<ConcurrentHashMap<List<Long>,ProcedimientoNegociacionDto>> registrosCorrectosUpdate;
	private List<ConcurrentHashMap<List<Long>,ProcedimientoNegociacionDto>> registrosCorrectosInsert;
	private List<String> procedimientosCorrectos;
	private List<Long> allSedeCapituloIds;

    @Override
	@TransactionTimeout(value = 30, unit = TimeUnit.MINUTES)
	public List<ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum>> validarProcedimientoNegociacionPgp(List<ArchivoTecnologiasNegociacionPgpDto> procedimientos, NegociacionDto negociacion, Integer userId) throws ConexiaBusinessException {

		List<ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum>> errores =
						new ArrayList<ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum>>();
		List<ConcurrentHashMap<List<Long>, ProcedimientoDto>> procedimientosAEliminar = new ArrayList<>();
		registrosCorrectosInsert = new ArrayList<ConcurrentHashMap<List<Long>,ProcedimientoNegociacionDto>>();
		registrosCorrectosUpdate = new ArrayList<ConcurrentHashMap<List<Long>,ProcedimientoNegociacionDto>>();
		allSedeCapituloIds = new ArrayList<Long>();
        List<Long> newCapituloIds = new ArrayList<Long>();
		procedimientosCorrectos = new ArrayList<String>();


        List<Long> procedimientoIdsHabilitacion = control.consultarIdsProcedimientosHabilitacion(negociacion);
                        if(procedimientoIdsHabilitacion.isEmpty()){
                         procedimientoIdsHabilitacion = control.consultarIdsProcedimientosHabilitacionNOReps(negociacion);
                        }
        List<ProcedimientoNegociacionDto> procedimientosNegociacion = control.consultarProcedimientosNegociacionPGP(negociacion.getId());

			for(ArchivoTecnologiasNegociacionPgpDto procedimientoArchivo: procedimientos) {

				ConcurrentHashMap<ArchivoTecnologiasNegociacionPgpDto, ErrorTecnologiasNegociacionPgpEnum> error = new ConcurrentHashMap<>();

				if(!procedimientosCorrectos.contains(procedimientoArchivo.getCodigoTecnologiaUnicaEmssanar())) {
					ProcedimientoDto procedimientoPGP = control.consultarProcedimientoByCodigoEmssanar(procedimientoArchivo.getCodigoTecnologiaUnicaEmssanar());

					//Verificar si el procedimiento existe
					if(Objects.isNull(procedimientoPGP)) {
						error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.TECNOLOGIA_NO_EXISTE);
					} else {
						//valida complejidad del procedimiento respecto al prestador
						if (procedimientoPGP.getNivelComplejidad() > negociacion.getPrestador().getNivelComplejidad()) {
							error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.NIVEL_COMPLEJIDAD_PROCEDIMIENTO);
						}
						//valida complejidad negociacion respecto al prestador
						if (negociacion.getComplejidad().getNivel() > negociacion.getPrestador().getNivelComplejidad()) {
							error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.NIVEL_COMPLEJIDAD_NEGOCIAICON);
						}

                        String si = "SI";
                        String no = "NO";
                        if(!si.equals(procedimientoArchivo.getEliminarTecnologia()) && !no.equals(procedimientoArchivo.getEliminarTecnologia())){
							error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.VALOR_NO_PERMITIDO_ELIMINAR);
						} else{
							if(!procedimientoArchivo.getCodigoTecnologiaUnicaEmssanar().equals(procedimientoPGP.getCodigoCliente())){
								error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.CODIGO_UNICO_TECNOLOGIA_NO_CORRESPONDE);
							} else {
									if(1 != procedimientoPGP.getEstadoProcedimientoId()){
										error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.TECNOLOGIA_INACTIVA);
									} else {
										Boolean enHabilitacion = false;
										for(Long id: procedimientoIdsHabilitacion) {
											if(id.equals(procedimientoPGP.getId())) {
												enHabilitacion = true;
											}
										}

										if(!enHabilitacion) {
											error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.PROCEDIMIENTO_NO_HABILITADO);
										} else {

											Boolean enNegociacion = false;
											for(ProcedimientoNegociacionDto ptoNegociacion: procedimientosNegociacion) {
												if(ptoNegociacion.getProcedimientoDto().getId().equals(procedimientoPGP.getId())) {
													enNegociacion = true;
												}
											}

											//consultar capítulo
											Long capituloId = control.consultarCapituloByProcedimientoId(procedimientoArchivo.getCodigoTecnologiaUnicaEmssanar());
											if(Objects.isNull(capituloId) || capituloId == 0) {
												error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.CAPITULO_NO_ENCONTRADO);
											} else {
												CapituloProcedimientoDto capituloDto = new CapituloProcedimientoDto();
												capituloDto.setId(capituloId);
												procedimientoPGP.setCapituloProcedimiento(capituloDto);

												List<Long> sedeCapituloIds = control.consultarSedeCapituloIdsByNegociacionAndCapitulo(negociacion.getId(), capituloId);

												try {
													negociacionControl.validarNumericos(error, procedimientoArchivo);
												} catch (NumberFormatException | ParseException e) {
													error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.VALOR_NUMERICO_NO_VALIDO);
												}

													if(enNegociacion) { //Si está en negociación se valida que si exista una sedeNegociacionCapitulo para el procedimiento
														if(si.equals(procedimientoArchivo.getEliminarTecnologia())){
															ConcurrentHashMap<List<Long>, ProcedimientoDto> ptoEliminar
																	= new ConcurrentHashMap<List<Long>, ProcedimientoDto>();
															ptoEliminar.put(sedeCapituloIds, procedimientoPGP);
															procedimientosAEliminar.add(ptoEliminar);
														} else {
															if(Objects.isNull(sedeCapituloIds)) {
																error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.SEDE_NEGOCIACION_NO_ENCONTRADA);
															} else {
																if(error.size() == 0) {
																	addRegistroValido(2, sedeCapituloIds, procedimientoArchivo, procedimientoPGP, negociacion);
																}
															}
														}


													} else {
														//Si el procedimiento no está en negociación se debe crear una sedeNegociacionCapitulo para
														//ser relacionada en la sedeNegociacionProcedimiento donde se insertará en el procedimiento importado
														//sólo en el caso de que el capítulo no exista en la negociación

														if(si.equals(procedimientoArchivo.getEliminarTecnologia())){
															error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.ELIMINAR_INVALIDO_NO_NEGOCIACION);
														} else {
															Boolean existeSedeCapitulo = false;
															for(ProcedimientoNegociacionDto ptoNegociacion: procedimientosNegociacion) {
																if(ptoNegociacion.getProcedimientoDto().getCapituloProcedimiento().getId().equals(capituloId)) {
																	existeSedeCapitulo = true;
																}
															}

															if(!existeSedeCapitulo) {

																if(!newCapituloIds.contains(capituloId)) {//para que no se creen duplicados en base a los capítulos encontrados

																	newCapituloIds.add(capituloId);
																	List<Long> sedeNegociacionIds = new ArrayList<Long>();
																	negociacion.getSedesNegociacion().stream().forEach(
																				sn -> {
																					sedeNegociacionIds.add(sn.getId());
																				}
																			);

                                                                    List<Long> newSedeCapituloIds = new ArrayList<Long>();
																	//Se crean las nuevas sedeNegociacionCapitulo y se recuperan los id
																	newSedeCapituloIds = control.crearSedesNegociacionCapitulo(sedeNegociacionIds, capituloId, userId);
																	if(error.size() == 0) {
																		addRegistroValido(1, newSedeCapituloIds, procedimientoArchivo, procedimientoPGP, negociacion);
																	}
																} else {
																	if(error.size() == 0) {
																		addRegistroValido(1, sedeCapituloIds, procedimientoArchivo, procedimientoPGP, negociacion);
																	}
																}


															} else {
																//insertar procedimiento con sedeNegociacionCapituloIds encontrados
																if(error.size() == 0) {
																	addRegistroValido(1, sedeCapituloIds, procedimientoArchivo, procedimientoPGP, negociacion);
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
					error.put(procedimientoArchivo, ErrorTecnologiasNegociacionPgpEnum.TECNOLOGIA_REPETIDA);
				}

				//Se valida que el list de errores realmente contenga un registro de procedimientos que no pasó las validaciones
				if(Objects.nonNull(error) && error.size() > 0) {
					errores.add(error);
				}

			}

			/**
			 * Sección para insertar o actualizar los procedimientos importados
			 */
			if(registrosCorrectosInsert.size() > 0) {
				insertarProcedimientosEnNegociacion(registrosCorrectosInsert, negociacion);
				for(ConcurrentHashMap<List<Long>, ProcedimientoNegociacionDto> pto: registrosCorrectosInsert) {
					for (ConcurrentHashMap.Entry<List<Long>, ProcedimientoNegociacionDto> registro : pto.entrySet()) {
						for(long sncId: registro.getKey()) {
							allSedeCapituloIds.add(sncId);
						}
					}
				}
			}

			if(registrosCorrectosUpdate.size() > 0) {
				actualizarProcedimientosEnNegociacion(registrosCorrectosUpdate, negociacion);
				for(ConcurrentHashMap<List<Long>, ProcedimientoNegociacionDto> pto: registrosCorrectosUpdate) {
					for (ConcurrentHashMap.Entry<List<Long>, ProcedimientoNegociacionDto> registro : pto.entrySet()) {
						for(Long sncId: registro.getKey()) {
							allSedeCapituloIds.add(sncId);
						}
					}
				}
			}

			//---- Sección para eliminar los procedimientos
			if(procedimientosAEliminar.size() > 0) {
				List<ProcedimientoDto> ptosEliminar = new ArrayList<ProcedimientoDto>();
				for(ConcurrentHashMap<List<Long>, ProcedimientoDto> pto: procedimientosAEliminar) {
					for(ConcurrentHashMap.Entry<List<Long>, ProcedimientoDto> registro: pto.entrySet()) {
						for(Long sncId: registro.getKey()) {
							allSedeCapituloIds.add(sncId);
						}
						ptosEliminar.add(registro.getValue());
					}
				}
				eliminarProcedimientosEnNegociacion(ptosEliminar, negociacion.getId(), userId);
			}

			//----- Sección para actualizar los valores de las sedeNegociacionCapitulo que aún esten en negociación
			if(registrosCorrectosInsert.size() > 0 || registrosCorrectosUpdate.size() > 0) {
				actualizarCapitulosEnNegociacion(negociacion.getId(), userId);
			}


			procedimientosAEliminar.clear();
			registrosCorrectosInsert.clear();
			registrosCorrectosUpdate.clear();
			allSedeCapituloIds.clear();
			newCapituloIds.clear();
			procedimientosCorrectos.clear();

		return errores;

	}

    @Override
    @TransactionTimeout(value = 30, unit = TimeUnit.MINUTES)
    public List<ErroresImportTecnologiasEventoDto> validarProcedimientoNegociacionEvento(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos, NegociacionDto negociacion, Integer userId) {
        List<ErroresImportTecnologiasEventoDto> listaErroresImportacion = new ArrayList<>();

        listaErroresImportacion.addAll(validarCamposObligatorios(procedimientos));
        listaErroresImportacion.addAll(validarCamposObligatoriosSedeASede(negociacion, procedimientos));

        List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadorPorNegociacion = obtenerServiciosHabilitadosPorSede(procedimientos.stream()
                        .map(ArchivoTecnologiasNegociacionEventoDto::getCodigoServicio)
                        .distinct()
                        .collect(Collectors.toList()), negociacion);
        listaErroresImportacion.addAll(validarSedesHabilitadas(procedimientos, serviciosHabilitadorPorNegociacion));

        List<ProcedimientoDto> procedimientosMaestros = obtenerProcedimientos(procedimientos.stream()
                .map(ArchivoTecnologiasNegociacionEventoDto::getCodigoEmssanar)
                .distinct()
                .collect(Collectors.toList()));
        listaErroresImportacion.addAll(validarTecnologiaInexistente(procedimientos, procedimientosMaestros));
        listaErroresImportacion.addAll(validarTipoTecnologiaInvalida(procedimientos, procedimientosMaestros));

        listaErroresImportacion.addAll(ValidacionServiciosReps.crear(procedimientos, serviciosHabilitadorPorNegociacion, negociacion).validar());

        listaErroresImportacion.addAll(validarProcedimientosInactivos(procedimientos, procedimientosMaestros));

        List<ProcedimientoDto> tecnologiasMaestras = obtenerProcedimientosServicios(procedimientos.stream()
                        .map(ArchivoTecnologiasNegociacionEventoDto::getCodigoEmssanar)
                        .distinct()
                        .collect(Collectors.toList()),
                procedimientos.stream()
                        .map(ArchivoTecnologiasNegociacionEventoDto::getCodigoServicio)
                        .distinct()
                        .collect(Collectors.toList()));
        listaErroresImportacion.addAll(validarAsociacionServicioTecnologia(procedimientos, tecnologiasMaestras));

        listaErroresImportacion.addAll(validarTarifario(procedimientos));
        listaErroresImportacion.addAll(validarTarifaPropia(procedimientos));
        listaErroresImportacion.addAll(validarTarifarioSOAT(procedimientos));

        listaErroresImportacion.addAll(validarPorcentajeSOAT(procedimientos));
        listaErroresImportacion.addAll(validarRangoPorcentajeSOAT(procedimientos));
        listaErroresImportacion.addAll(calcularValorNegociadoSoatYIss(procedimientos));

        listaErroresImportacion.addAll(ValidacionNivelComplejidad.crear(procedimientos,serviciosHabilitadorPorNegociacion, tecnologiasMaestras, negociacion).validar());

        replicarYGuardarServiciosEvento(procedimientos, serviciosHabilitadorPorNegociacion, tecnologiasMaestras, negociacion, userId);
        eliminarServiciosSinTecnologias(negociacion);

        return listaErroresImportacion;
    }

    private void replicarYGuardarServiciosEvento(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos,
                                           List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadorPorNegociacion,
                                           List<ProcedimientoDto> tecnologiasMaestras, NegociacionDto negociacion,
                                           Integer userId) {
        if (procedimientos.isEmpty()) {
            return;
        }
        Map<Long, List<ServiciosHabilitadosRespNoRepsDto>> agrupamientoServiciosPorSedeNegociacion = serviciosHabilitadorPorNegociacion.stream()
                .collect(Collectors.groupingBy(ServiciosHabilitadosRespNoRepsDto::getSedeNegociacionId));

        List<SedeNegociacionServicioDto> sedeNegociacionServicioEnNegociacion = importarProcedimientosControl.consultarServiciosPorNegociacion(negociacion);

        agrupamientoServiciosPorSedeNegociacion.forEach((sedesNegociacionId, servicios) -> {
            List<Long> serviciosActivosPorSede = servicios.stream()
                    .map(ServiciosHabilitadosRespNoRepsDto::getServicioId)
                    .collect(Collectors.toList());

            List<Long> serviciosValidosParaInsertar = serviciosActivosPorSede.stream()
                    .filter(serviciosNoInsertadosNegociacion(sedeNegociacionServicioEnNegociacion, sedesNegociacionId))
                    .collect(Collectors.toList());

            importarProcedimientosControl.insertarSedeNegociacionServicioImportar(Collections.singletonList(sedesNegociacionId),
                    serviciosValidosParaInsertar, negociacion, userId);
        });

        replicarYGuardarTecnologiasEvento(procedimientos, serviciosHabilitadorPorNegociacion, tecnologiasMaestras, negociacion, userId);

    }

    private void eliminarServiciosSinTecnologias(NegociacionDto negociacion) {
        importarProcedimientosControl.eliminarServiciosSinTecnologias(negociacion);
    }

    private Predicate<Long> serviciosNoInsertadosNegociacion(List<SedeNegociacionServicioDto> sedeNegociacionServicioEnNegociacion, Long sedesNegociacionId) {
        if (sedeNegociacionServicioEnNegociacion.isEmpty()) {
            return idServicioArchivo -> true;
        }
        return idServicioArchivo -> !sedeNegociacionServicioEnNegociacion.stream()
                .map(SedeNegociacionServicioDto::getServicioSaludId)
                .collect(Collectors.toList())
                .contains(idServicioArchivo)
                && sedeNegociacionServicioEnNegociacion.stream()
                .map(SedeNegociacionServicioDto::getSedeNegociacionId)
                .collect(Collectors.toList())
                .contains(sedesNegociacionId);
    }

    private void replicarYGuardarTecnologiasEvento(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos,
                                             List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadorPorNegociacion,
                                             List<ProcedimientoDto> tecnologiasMaestras, NegociacionDto negociacion,
                                             Integer userId) {
        if (procedimientos.isEmpty()) {
            return;
        }

        tecnologiasMaestras.forEach(tecnologiasMaestra -> procedimientos.stream()
                .filter(procedimiento -> Objects.equals(tecnologiasMaestra.getCodigoCliente(), procedimiento.getCodigoEmssanar())
                        && Objects.equals(tecnologiasMaestra.getServicioSalud()
                        .getCodigo(), procedimiento.getCodigoServicio()))
                .forEach(procedimiento -> procedimiento.setNivelComplejidadTecnologia(tecnologiasMaestra.getComplejidad())));

        Map<String, List<ArchivoTecnologiasNegociacionEventoDto>> agrupamientoTecnologiasPorServicio = procedimientos.stream()
                .collect(Collectors.groupingBy(ArchivoTecnologiasNegociacionEventoDto::getCodigoServicio));

        List<ArchivoTecnologiasNegociacionEventoDto> tecnologiasReplicadasTodasLasSedes = new ArrayList<>();

        agrupamientoTecnologiasPorServicio.forEach((servicio, listaTecnologiasAsociadasServicio) -> {
            List<ServiciosHabilitadosRespNoRepsDto> sedesHabilitadasParaElServicio = serviciosHabilitadorPorNegociacion.stream()
                    .filter(servicioHabilitado -> Objects.equals(servicioHabilitado.getCodigoServicio(), servicio))
                    .collect(Collectors.toList());

            List<ArchivoTecnologiasNegociacionEventoDto> tecnologiasReplicadasSedes = sedesHabilitadasParaElServicio.stream().map(sedeHabilitada -> {
                List<ArchivoTecnologiasNegociacionEventoDto> listaTecnologiasSede = new ArrayList<>();
                listaTecnologiasAsociadasServicio.stream()
                        .filter(archivoDto -> Objects.nonNull(archivoDto.getNivelComplejidadTecnologia()) && archivoDto.getNivelComplejidadTecnologia() <= sedeHabilitada.getNivelComplejidadMinimo())
                        .forEach(archivo -> {
                            try {
                                listaTecnologiasSede.add(archivo.clone());
                            } catch (CloneNotSupportedException e) {
                                log.error("No se puede clonar el archivo para las diferentes servicios", e);
                            }
                        });
                listaTecnologiasSede.forEach(tecnologia -> {
                    tecnologia.setSedesNegociacionId(sedeHabilitada.getSedeNegociacionId());
                    tecnologia.setCodigoHabilitacionSede(sedeHabilitada.getCodigohabilitacion());
                    tecnologia.setNivelComplejidadServicio(sedeHabilitada.getNivelComplejidad());
                });
                return listaTecnologiasSede;
            }).collect(Collectors.toList()).stream().flatMap(Collection::parallelStream).collect(Collectors.toList());

            tecnologiasReplicadasTodasLasSedes.addAll(tecnologiasReplicadasSedes);
        });

        Map<Long, List<ArchivoTecnologiasNegociacionEventoDto>> tecnologiasPorSede = tecnologiasReplicadasTodasLasSedes.stream()
                .collect(Collectors.groupingBy(ArchivoTecnologiasNegociacionEventoDto::getSedesNegociacionId));

        tecnologiasPorSede.forEach((sedeNegociacionId, archivoTecnologiasNegociacionEventoDtos) -> insertaProcedimientoEvento(
                archivoTecnologiasNegociacionEventoDtos, Collections.singletonList(sedeNegociacionId), negociacion, userId));
    }

    private void insertaProcedimientoEvento(List<ArchivoTecnologiasNegociacionEventoDto> archivo, List<Long> sedesNegociacionId, NegociacionDto dto, Integer userId) {
        List<SedeNegociacionProcedimiento> procedimientos = this.importarProcedimientosControl.consultarProcedimientosPorNegociacion(sedesNegociacionId, archivo);
        importarProcedimientosControl.insertarProcedimientosImportEvento(sedesNegociacionId, archivo.stream()
                        .filter(archivoDto -> procedimientos.stream()
                                .noneMatch(contieneProcedimiento(archivoDto)))
                        .collect(Collectors.toList()),userId);
        importarProcedimientosControl.actualizarProcedimientoEventoImportar(sedesNegociacionId, archivo.stream()
                .filter(archivoDto -> procedimientos.stream()
                        .anyMatch(contieneProcedimiento(archivoDto)))
                .collect(Collectors.toList()), dto, userId);
    }

    private Predicate<SedeNegociacionProcedimiento> contieneProcedimiento(ArchivoTecnologiasNegociacionEventoDto archivoDto) {
        return procedimiento -> Objects.equals(procedimiento.getProcedimiento().getId(), archivoDto.getProcedimientoServicioId())
                && Objects.equals(procedimiento.getProcedimiento().getServicioSalud().getId(), archivoDto.getServicioId());
    }

    private void obtenerTarifario(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos) {
        List<Tarifario> tarifarios = importarProcedimientosControl.consultarTarifarioId(procedimientos.stream()
                .map(ArchivoTecnologiasNegociacionEventoDto::getTarifarioNegociado)
                .distinct()
                .collect(Collectors.toList()));
        procedimientos.stream()
                .filter(archivo -> tarifarios.stream()
                        .map(Tarifario::getDescripcion)
                        .collect(Collectors.toList())
                        .contains(archivo.getTarifarioNegociado()))
                .forEach(archivo -> archivo.setTarifarioNegociacionId(tarifarios.stream()
                        .filter(tarifario -> archivo.getTarifarioNegociado().equals(tarifario.getDescripcion()))
                        .findFirst()
                        .orElse(new Tarifario(0))
                        .getId()
                        .longValue()));
        procedimientos.stream()
                .filter(archivo -> Objects.nonNull(archivo.getTarifarioNegociacionId()) && archivo.getTarifarioNegociacionId().equals(5L))
                .forEach(archivo -> archivo.setPorcentajeNegociado("0.0"));
    }

    private Collection<? extends ErroresImportTecnologiasEventoDto> validarSedesHabilitadas(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos, List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadorPorNegociacion) {
        if (procedimientos.isEmpty() && serviciosHabilitadorPorNegociacion.isEmpty()) {
            return Collections.emptyList();
        }

        List<ArchivoTecnologiasNegociacionEventoDto> sedesInexistentes = procedimientos.stream()
                .filter(archivo -> Objects.nonNull(archivo.getCodigoHabilitacionSede()) && !archivo.getCodigoHabilitacionSede().isEmpty())
                .filter(archivo -> !serviciosHabilitadorPorNegociacion.stream()
                        .map(ServiciosHabilitadosRespNoRepsDto::getCodigoHabilitacionMasCodigoSede)
                        .collect(Collectors.toSet())
                        .contains(archivo.getCodigoHabilitacionSedeSoloDigitos()))
                .collect(Collectors.toList());
        procedimientos.removeAll(sedesInexistentes);

        return sedesInexistentes.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.SEDE_NO_NEGOCIACION))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasEventoDto> validarTecnologiaInexistente(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos, List<ProcedimientoDto> procedimientosMaestros) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        List<ArchivoTecnologiasNegociacionEventoDto> procedimientosInactivos = procedimientos.stream()
                .filter(procedimiento -> !procedimientosMaestros.stream()
                        .map(AbstractProcedimiento::getCodigoCliente)
                        .collect(Collectors.toList())
                        .contains(procedimiento.getCodigoEmssanar()))
                .collect(Collectors.toList());
        procedimientos.removeAll(procedimientosInactivos);
        return procedimientosInactivos.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.TECNOLOGIA_NO_EXISTE))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasEventoDto> validarProcedimientosInactivos(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos, List<ProcedimientoDto> procedimientosMaestros) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        List<ArchivoTecnologiasNegociacionEventoDto> procedimientosInactivos = procedimientos.stream()
                .filter(procedimiento -> procedimientosMaestros.stream()
                        .anyMatch(procedimientoDto -> isTecnologiaInactiva(procedimiento.getCodigoEmssanar(), procedimientoDto)))
                .collect(Collectors.toList());
        procedimientos.removeAll(procedimientosInactivos);
        return procedimientosInactivos.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.TECNOLOGIA_DEROGADA))
                .collect(Collectors.toList());
    }

    private boolean isTecnologiaInactiva(String codigoEmssanarProcedimiento, ProcedimientoDto procedimientoDto) {
        int activo = 1;
        return Objects.equals(procedimientoDto.getCodigoCliente(), codigoEmssanarProcedimiento) && procedimientoDto.getEstadoProcedimientoId() != activo;
    }

    private Collection<? extends ErroresImportTecnologiasEventoDto> validarTipoTecnologiaInvalida(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos, List<ProcedimientoDto> procedimientosMaestros) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        List<ArchivoTecnologiasNegociacionEventoDto> procedimientosInactivos = procedimientos.stream()
                .filter(procedimiento -> procedimientosMaestros.stream().anyMatch(isDiferenteDeProcedimiento(procedimiento.getCodigoEmssanar())))
                .collect(Collectors.toList());
        procedimientos.removeAll(procedimientosInactivos);
        return procedimientosInactivos.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.TIPO_TECNOLOGIA_INVALIDO))
                .collect(Collectors.toList());
    }

    private Predicate<ProcedimientoDto> isDiferenteDeProcedimiento(String codigoEmssanarProcedimiento) {
        return procedimientoDto -> Objects.equals(procedimientoDto.getCodigoCliente(), codigoEmssanarProcedimiento)
                && Objects.equals(procedimientoDto.getTipoProcedimientoEnum(), TipoProcedimientoEnum.PAQUETE);
    }

    private Collection<? extends ErroresImportTecnologiasEventoDto> validarTarifaPropia(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        List<ArchivoTecnologiasNegociacionEventoDto> valorInvalido = new ArrayList<>();
        procedimientos.stream().filter(archivo -> (Objects.nonNull(archivo.getValorNegociado()) && !archivo.getValorNegociado().isEmpty()) && (archivo.getTarifarioNegociacionId() == 5))
                .forEach(archivo -> {
                    try {
                        archivo.setValorNegociado(archivo.getValorNegociado().replace(",", "."));
                        BigDecimal valor = new BigDecimal(archivo.getValorNegociado()).setScale(-2, BigDecimal.ROUND_HALF_UP);
                        archivo.setValorNegociado(valor.toString());
                        if (Objects.isNull(archivo.getValorNegociado()) || archivo.getValorNegociado().isEmpty() || archivo.getValorNegociado().equals(" ")) {
                            valorInvalido.add(archivo);
                        }
                        if (valor.compareTo(BigDecimal.ZERO) <= 0) {
                            valorInvalido.add(archivo);
                        }
                    } catch (NumberFormatException excepDcion) {
                        valorInvalido.add(archivo);
                    }
                });
        procedimientos.removeAll(valorInvalido);
        return valorInvalido.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.VALOR_NEGOCIADO_NO_PERMITIDO))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasEventoDto> calcularValorNegociadoSoatYIss(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        List<ArchivoTecnologiasNegociacionEventoDto> valorInvalido = new ArrayList<>();
        procedimientos.stream()
                .filter(archivo -> (Objects.nonNull(archivo.getTarifarioNegociacionId()) && archivo.getTarifarioNegociacionId().intValue() != 0)
                        && (archivo.getTarifarioNegociado().contains("SOAT") || archivo.getTarifarioNegociado().contains("ISS")))
                .forEach(archivo -> {
            try {
                BigDecimal porcentaje = new BigDecimal(archivo.getPorcentajeNegociado()).setScale(0, BigDecimal.ROUND_HALF_UP);
                List<Tarifario> tarifarios = importarProcedimientosControl.consultarTarifarioId(Collections.singletonList(archivo.getTarifarioNegociado()));
                BigDecimal valorNegociadoSoat = this.importarProcedimientosControl.calcularValorProcedimiento(
                        archivo.getCups(), tarifarios.stream().findFirst().orElse(new Tarifario()).getId(),
                        porcentaje.doubleValue()).setScale(-2, BigDecimal.ROUND_HALF_UP);
                archivo.setValorNegociado(valorNegociadoSoat.toString());
            } catch (NumberFormatException excepDcion) {
                valorInvalido.add(archivo);
            }
        });
        procedimientos.removeAll(valorInvalido);
        return valorInvalido.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.PORCENTAJE_NEGOCIADO_NO_PERMITIDO))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasEventoDto> validarRangoPorcentajeSOAT(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        List<ArchivoTecnologiasNegociacionEventoDto> rengoPorcentualInvalido = procedimientos.stream()
                .filter(archivo -> (Objects.nonNull(archivo.getTarifarioNegociacionId()) && archivo.getTarifarioNegociacionId().intValue() != 0)
                        && (archivo.getTarifarioNegociado().contains("SOAT") || archivo.getTarifarioNegociado().contains("ISS")))
                .filter(archivo -> {
                    BigDecimal porcentajeNegociado = BigDecimal.valueOf(Long.parseLong(archivo.getPorcentajeNegociado()));
                    return porcentajeNegociado.compareTo(BigDecimal.ZERO) < -100 || porcentajeNegociado.compareTo(BigDecimal.ZERO) > 100;
                })
                .collect(Collectors.toList());
        procedimientos.removeAll(rengoPorcentualInvalido);
        return rengoPorcentualInvalido.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.PORCENTAJE_NEGOCIADO_NO_PERMITIDO_SOAT))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasEventoDto> validarPorcentajeSOAT(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        List<ArchivoTecnologiasNegociacionEventoDto> porcetajeInvalido = procedimientos.stream()
                .filter(archivo -> (Objects.nonNull(archivo.getTarifarioNegociacionId()) && archivo.getTarifarioNegociacionId().intValue() != 0)
                        && (archivo.getTarifarioNegociado().contains("SOAT") || archivo.getTarifarioNegociado().contains("ISS")))
                .peek(archivo -> {
                    archivo.setPorcentajeNegociado(archivo.getPorcentajeNegociado().replace(",", "."));
                    BigDecimal porcentaje = new BigDecimal(archivo.getPorcentajeNegociado()).setScale(0, BigDecimal.ROUND_HALF_UP);
                    archivo.setPorcentajeNegociado(porcentaje.toString());
                }).filter(archivo -> Objects.isNull(archivo.getPorcentajeNegociado()) || archivo.getPorcentajeNegociado().isEmpty() || archivo.getPorcentajeNegociado().equals(" "))
                .collect(Collectors.toList());
        procedimientos.removeAll(porcetajeInvalido);
        return porcetajeInvalido.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.PORCENTAJE_NEGOCIADO_NO_PERMITIDO))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasEventoDto> validarCamposObligatorios(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos) {
        List<ArchivoTecnologiasNegociacionEventoDto> procedimientosNoCumplen = procedimientos.stream()
                .filter(archivo -> archivo.isNoValidoCodigoServicio() || archivo.isNoValidoCodigoEmssanar()
                        || archivo.isNoValidoTarifarioNegociado() || archivo.isNoValidoPorcentajeNegociado()
                        || archivo.isNoValidoValorNegociado())
                .collect(Collectors.toList());
        procedimientos.removeAll(procedimientosNoCumplen);
        return procedimientosNoCumplen.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.CAMPOS_OBLIGATORIOS))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasEventoDto> validarCamposObligatoriosSedeASede(NegociacionDto negociacion, List<ArchivoTecnologiasNegociacionEventoDto> procedimientos) {
        List<ArchivoTecnologiasNegociacionEventoDto> procedimientosSedeASedeNoValidos = procedimientos.stream()
                .filter(archivo -> negociacion.getOpcionImportarSeleccionada()
                        .equals(OpcionesImportacionTecnologiaEnum.IMPORTAR_SEDE_A_SEDE) && archivo.isNoValidoCodigoHabilitacionSede())
                .collect(Collectors.toList());
        procedimientos.removeAll(procedimientosSedeASedeNoValidos);
        return procedimientosSedeASedeNoValidos.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.CAMPOS_OBLIGATORIOS))
                .collect(Collectors.toList());

    }

    private Collection<? extends ErroresImportTecnologiasEventoDto> validarAsociacionServicioTecnologia(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos, List<ProcedimientoDto> tecnologiasMaestras) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        procedimientos.stream()
                .filter(archivo -> tecnologiasMaestras.stream()
                        .map(procedimiento -> procedimiento.getServicioSalud().getCodigo())
                        .collect(Collectors.toList())
                        .contains(archivo.getCodigoServicio()))
                .forEach(archivo -> {
                    archivo.setServicioId(tecnologiasMaestras.stream()
                            .filter(procedimiento -> Objects.equals(procedimiento.getServicioSalud().getCodigo(),archivo.getCodigoServicio()))
                            .findFirst()
                            .orElse(new ProcedimientoDto())
                            .getServicioSalud()
                            .getId());
                    archivo.setProcedimientoServicioId(tecnologiasMaestras.stream()
                            .filter(procedimiento -> Objects.equals(procedimiento.getCodigoCliente(), archivo.getCodigoEmssanar()) && Objects.equals(procedimiento.getServicioSalud().getCodigo(), archivo.getCodigoServicio()))
                            .findFirst()
                            .orElse(new ProcedimientoDto())
                            .getId());
                    archivo.setCups(tecnologiasMaestras.stream()
                            .filter(procedimiento -> Objects.equals(procedimiento.getCodigoCliente(), archivo.getCodigoEmssanar()) && Objects.equals(procedimiento.getServicioSalud().getCodigo(), archivo.getCodigoServicio()))
                            .findFirst()
                            .orElse(new ProcedimientoDto())
                            .getCups());
                });

		List<ArchivoTecnologiasNegociacionEventoDto> procedimientosEnMaestra = procedimientos.stream()
				.filter(procedimiento -> Objects.isNull(procedimiento.getProcedimientoServicioId()))
				.collect(Collectors.toList());

        procedimientos.removeAll(procedimientosEnMaestra);

        return procedimientosEnMaestra.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.TECNOLOGIA_NO_SERVICIO))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasEventoDto> validarTarifario(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        obtenerTarifario(procedimientos);
        List<ArchivoTecnologiasNegociacionEventoDto> tarifariosInvalidos = procedimientos.stream()
                .filter(archivo -> Objects.isNull(archivo.getTarifarioNegociacionId()))
                .collect(Collectors.toList());
        procedimientos.removeAll(tarifariosInvalidos);
        return tarifariosInvalidos.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.TARIFARIO_NO_EXISTE))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasEventoDto> validarTarifarioSOAT(List<ArchivoTecnologiasNegociacionEventoDto> procedimientos) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        List<ArchivoTecnologiasNegociacionEventoDto> tarifariosSoatInvalidos = procedimientos.stream()
                .filter(archivo -> (Objects.nonNull(archivo.getTarifarioNegociacionId()) && archivo.getTarifarioNegociacionId().intValue() != 0)
                        && (archivo.getTarifarioNegociado().contains("SOAT") || archivo.getTarifarioNegociado().contains("ISS")))
                .filter(archivo -> archivo.getPorcentajeNegociado().isEmpty() || Objects.isNull(archivo.getPorcentajeNegociado())
                        || archivo.getPorcentajeNegociado().equals(" ") || archivo.getPorcentajeNegociado().equals("0"))
                .collect(Collectors.toList());
        procedimientos.removeAll(tarifariosSoatInvalidos);
        return tarifariosSoatInvalidos.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionEventoEnum.PORCENTAJE_NEGOCIADO_NO_PERMITIDO))
                .collect(Collectors.toList());
    }

    private ErroresImportTecnologiasEventoDto convertirArchivosAErrorImportacion(ArchivoTecnologiasNegociacionEventoDto archivo, ErrorTecnologiasNegociacionEventoEnum camposObligatorios) {
        return new ErroresImportTecnologiasEventoDto(
                camposObligatorios.getCodigo(),
                camposObligatorios.getMensaje(),
                archivo.getLineaArchivo(),
                archivo.getCodigoHabilitacionSede(),
                archivo.getCodigoServicio(),
                archivo.getCodigoEmssanar(),
                archivo.getTarifarioNegociado(),
                archivo.getPorcentajeNegociado(),
                archivo.getValorNegociado());
    }

    @Override
    @TransactionTimeout(value = 30, unit = TimeUnit.MINUTES)
    public List<ErroresImportTecnologiasRIasCapitaDto> validarProcedimientoNegociacionRiasCapita(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, NegociacionDto negociacion, Integer userId) {
        List<ErroresImportTecnologiasRIasCapitaDto> listaErroresImportacion = new ArrayList<>();

        listaErroresImportacion.addAll(validarCamposObligatoriosRias(procedimientos));
        listaErroresImportacion.addAll(validarPorcentaje(procedimientos));

        List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadorPorNegociacion = obtenerServiciosHabilitadosPorSede(procedimientos.stream()
                .map(ArchivoTecnologiasNegociacionRiasCapitaDto::getCodigoServicio)
                .distinct()
                .collect(Collectors.toList()), negociacion);
        List<ProcedimientoDto> procedimientosMaestros = obtenerProcedimientos(procedimientos.stream()
            .map(ArchivoTecnologiasNegociacionRiasCapitaDto::getCodigoEmssanar)
            .distinct()
            .collect(Collectors.toList()));

        listaErroresImportacion.addAll(validarTecnologiaInexistenteRias(procedimientos, procedimientosMaestros));
        listaErroresImportacion.addAll(validarTipoTecnologiaInvalidaRias(procedimientos, procedimientosMaestros));
        listaErroresImportacion.addAll(validarHabilitacionServiciosRepsRias(procedimientos, serviciosHabilitadorPorNegociacion));
        listaErroresImportacion.addAll(validarProcedimientosInactivosRias(procedimientos, procedimientosMaestros));

        List<ProcedimientoDto> tecnologiasMaestras = obtenerProcedimientosServicios(procedimientos.stream()
                .map(ArchivoTecnologiasNegociacionRiasCapitaDto::getCodigoEmssanar)
                .distinct()
                .collect(Collectors.toList()), procedimientos.stream()
                .map(ArchivoTecnologiasNegociacionRiasCapitaDto::getCodigoServicio)
                .distinct()
                .collect(Collectors.toList()));
        listaErroresImportacion.addAll(validarAsociacionServicioTecnologiaRias(procedimientos, tecnologiasMaestras));

        listaErroresImportacion.addAll(validarRutaInexistente(procedimientos));
        listaErroresImportacion.addAll(validarRangoPoblacionalInexistente(procedimientos));
        listaErroresImportacion.addAll(validarTemaInexistente(procedimientos));
        listaErroresImportacion.addAll(validarRutaNegociacionInexistente(procedimientos, negociacion));
        listaErroresImportacion.addAll(validarReferenteRiasCapitaInexistente(procedimientos, negociacion));

        listaErroresImportacion.addAll(validarNivelComplejidadRias(procedimientos, tecnologiasMaestras, serviciosHabilitadorPorNegociacion, negociacion));

        replicarYGuardarServiciosRiasCapita(procedimientos, serviciosHabilitadorPorNegociacion, tecnologiasMaestras, negociacion, userId);

        return listaErroresImportacion;
    }

    private List<ServiciosHabilitadosRespNoRepsDto> obtenerServiciosHabilitadosPorSede(List<String> codigosServico, NegociacionDto negociacion) {
        if (codigosServico.isEmpty()) {
            return new ArrayList<>();
        }
        return importarProcedimientosControl.consultaServicioRepsHabilitados(negociacion, codigosServico);
    }

    private List<ProcedimientoDto> obtenerProcedimientosServicios(List<String> codigosEmssanar, List<String> codigosServicios) {
        if (codigosEmssanar.isEmpty() || codigosServicios.isEmpty()) {
            return new ArrayList<>();
        }
        return importarProcedimientosControl.consultarProcedimientoServicio(codigosEmssanar, codigosServicios);
    }

    private List<ProcedimientoDto> obtenerProcedimientos(List<String> codigosEmssanar) {
        if (codigosEmssanar.isEmpty()) {
            return new ArrayList<>();
        }
        return importarProcedimientosControl.consultarProcedimiento(codigosEmssanar);
    }

    private void replicarYGuardarServiciosRiasCapita(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadorPorNegociacion, List<ProcedimientoDto> tecnologiasMaestras, NegociacionDto negociacion, Integer userId) {
        if (procedimientos.isEmpty()) {
            return;
        }
        Map<Long, List<ServiciosHabilitadosRespNoRepsDto>> agrupamientoServiciosPorSedeNegociacion = serviciosHabilitadorPorNegociacion.stream()
                .collect(Collectors.groupingBy(ServiciosHabilitadosRespNoRepsDto::getSedeNegociacionId));

        List<SedeNegociacionServicioDto> sedeNegociacionServicioEnNegociacion = importarProcedimientosControl.consultarServiciosPorNegociacion(negociacion);

        agrupamientoServiciosPorSedeNegociacion.forEach((sedesNegociacionId, servicios) -> {
            List<Long> serviciosActivosPorSede = servicios.stream()
                    .map(ServiciosHabilitadosRespNoRepsDto::getServicioId)
                    .collect(Collectors.toList());

            List<Long> serviciosValidos = serviciosActivosPorSede.stream()
                    .filter(serviciosNoInsertadosNegociacion(sedeNegociacionServicioEnNegociacion, sedesNegociacionId))
                    .collect(Collectors.toList());

            importarProcedimientosControl.insertarSedeNegociacionServicioImportar(Collections.singletonList(sedesNegociacionId),
                    serviciosValidos, negociacion, userId);
        });

        replicarYGuardarTecnologiasRiasCapita(procedimientos, serviciosHabilitadorPorNegociacion, tecnologiasMaestras, negociacion, userId);
    }

    private void replicarYGuardarTecnologiasRiasCapita(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadorPorNegociacion, List<ProcedimientoDto> tecnologiasMaestras, NegociacionDto negociacion, Integer userId) {
        if (procedimientos.isEmpty()) {
            return;
        }

        tecnologiasMaestras.forEach(tecnologiasMaestra -> procedimientos.stream()
                .filter(procedimiento -> Objects.equals(tecnologiasMaestra.getCodigoCliente(), procedimiento.getCodigoEmssanar())
                        && Objects.equals(tecnologiasMaestra.getServicioSalud()
                        .getCodigo(), procedimiento.getCodigoServicio()))
                .forEach(procedimiento -> procedimiento.setNivelComplejidadTecnologia(tecnologiasMaestra.getComplejidad())));

        Map<String, List<ArchivoTecnologiasNegociacionRiasCapitaDto>> agrupamientoTecnologiasPorServicio = procedimientos.stream()
                .collect(Collectors.groupingBy(ArchivoTecnologiasNegociacionRiasCapitaDto::getCodigoServicio));

        List<ArchivoTecnologiasNegociacionRiasCapitaDto> tecnologiasReplicadasTodasLasSedes = new ArrayList<>();

        agrupamientoTecnologiasPorServicio.forEach((servicio, listaTecnologiasAsociadasServicio) -> {
            List<ServiciosHabilitadosRespNoRepsDto> sedesHabilitadasParaElServicio = serviciosHabilitadorPorNegociacion.stream()
                    .filter(servicioHabilitado -> Objects.equals(servicioHabilitado.getCodigoServicio(), servicio))
                    .collect(Collectors.toList());

            List<ArchivoTecnologiasNegociacionRiasCapitaDto> tecnologiasReplicadasSedes = sedesHabilitadasParaElServicio.stream().map(sedeHabilitada -> {
                List<ArchivoTecnologiasNegociacionRiasCapitaDto> listaTecnologiasSede = new ArrayList<>();
                listaTecnologiasAsociadasServicio.stream()
                        .filter(archivoDto -> (Objects.nonNull(archivoDto.getNivelComplejidadTecnologia())) && archivoDto.getNivelComplejidadTecnologia() <= sedeHabilitada.getNivelComplejidadMinimo())
                        .forEach(archivo -> {
                            try {
                                listaTecnologiasSede.add(archivo.clone());
                            } catch (CloneNotSupportedException e) {
                                log.error("No se puede clonar el archivo para las diferentes servicios", e);
                            }
                        });
                listaTecnologiasSede.forEach(tecnologia -> {
                    tecnologia.setSedesNegociacionId(sedeHabilitada.getSedeNegociacionId());
                    tecnologia.setCodigoHabilitacionSede(sedeHabilitada.getCodigohabilitacion());
                    tecnologia.setNivelComplejidadServicio(sedeHabilitada.getNivelComplejidad());
                    tecnologia.setServicioId(sedeHabilitada.getServicioId());
                });
                return listaTecnologiasSede;
            }).collect(Collectors.toList()).stream().flatMap(Collection::parallelStream).collect(Collectors.toList());

            tecnologiasReplicadasTodasLasSedes.addAll(tecnologiasReplicadasSedes);
        });

        Map<Long, List<ArchivoTecnologiasNegociacionRiasCapitaDto>> tecnologiasPorSede = tecnologiasReplicadasTodasLasSedes.stream()
                .collect(Collectors.groupingBy(ArchivoTecnologiasNegociacionRiasCapitaDto::getSedesNegociacionId));

        tecnologiasPorSede.forEach((sedeNegociacionId, archivoTecnologiasNegociacionEventoDtos) -> insertaProcedimientoRiasCapita(
                archivoTecnologiasNegociacionEventoDtos, Collections.singletonList(sedeNegociacionId), negociacion, userId));
    }

    private void insertaProcedimientoRiasCapita(List<ArchivoTecnologiasNegociacionRiasCapitaDto> archivo, List<Long> sedesNegociacionId, NegociacionDto dto, Integer userId) {
        List<Long> sedePxId = control.consultarSedePxIdRiasCapita(sedesNegociacionId, archivo);
        if (!sedePxId.isEmpty()) {
            control.actualizarProcedimientosImportRiasCapita(sedesNegociacionId, archivo, userId);
        } else {
            control.insertarProcedimientosImportRiasCapita(sedesNegociacionId, archivo, userId);
        }
    }

    private Collection<? extends ErroresImportTecnologiasRIasCapitaDto> validarPorcentaje(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos) {
        List<ArchivoTecnologiasNegociacionRiasCapitaDto> porcentajeInvalido = procedimientos.stream()
                .filter(archivo -> {
                    try {
                        archivo.setPesoPorcentual(archivo.getPesoPorcentual()
                                .replace(",", "."));
                        BigDecimal valor = new BigDecimal(archivo.getPesoPorcentual());
                        archivo.setPesoPorcentual(valor.toString());
                        if (archivo.getPesoPorcentual()
                                .isEmpty() || Objects.isNull(archivo.getPesoPorcentual()) || archivo.getPesoPorcentual()
                                .equals(" ")) {
                            return true;
                        }
                        if (valor.compareTo(BigDecimal.ZERO) <= 0 || valor.compareTo(BigDecimal.ZERO) > 100) {
                            return true;
                        }
                    } catch (NumberFormatException excepcion) {
                        return true;
                    }
                    return false;
                })
                .collect(Collectors.toList());
        return porcentajeInvalido.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionRiasCapitaEnum.VALOR_NEGOCIADO_NO_PERMITIDO))
                .collect(Collectors.toList());

    }

    private Collection<? extends ErroresImportTecnologiasRIasCapitaDto> validarReferenteRiasCapitaInexistente(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, NegociacionDto negociacion) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        List<ReferenteProcedimientoDto> riaDtos = negociacionControl.consultarReferenteRiasCapita(procedimientos, negociacion);
        List<ArchivoTecnologiasNegociacionRiasCapitaDto> rutasInexistente = procedimientos.stream()
                .filter(archivoDto -> riaDtos.stream()
                        .noneMatch(referenteProcedimientoDto -> Objects.equals(referenteProcedimientoDto.getProcedimiento().getCodigoCliente(), archivoDto.getCodigoEmssanar())
                        && Objects.equals(referenteProcedimientoDto.getProcedimiento().getServicioSalud().getCodigo(), archivoDto.getCodigoServicio())
                        && Objects.equals(referenteProcedimientoDto.getRiaDto().getDescripcion(), archivoDto.getRuta())
                        && Objects.equals(referenteProcedimientoDto.getActividad().getDescripcion(), archivoDto.getTema())
                        && Objects.equals(referenteProcedimientoDto.getRangoPoblacion().getDescripcion(), archivoDto.getRangoPoblacion())))
                .collect(Collectors.toList());
        procedimientos.removeAll(rutasInexistente);
        return rutasInexistente.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionRiasCapitaEnum.TECNOLOGIA_VS_REFERENTE))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasRIasCapitaDto> validarRutaNegociacionInexistente(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, NegociacionDto negociacion) {
        List<NegociacionRiaDto> riaDtos = negociacionControl.consultarRutaaNegociacion(procedimientos.stream()
                .map(ArchivoTecnologiasNegociacionRiasCapitaDto::getRutaId)
                .distinct()
                .collect(Collectors.toList()), negociacion);
        List<ArchivoTecnologiasNegociacionRiasCapitaDto> rutasInexistente = procedimientos.stream()
                .filter(archivoDto -> !riaDtos.stream()
                        .map(negociacionRiaDto -> negociacionRiaDto.getRia().getDescripcion())
                        .collect(Collectors.toList())
                        .contains(archivoDto.getRuta()))
                .collect(Collectors.toList());
        procedimientos.removeAll(rutasInexistente);
        return rutasInexistente.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionRiasCapitaEnum.RUTA_NO_NEGOCIACION))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasRIasCapitaDto> validarTemaInexistente(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos) {
        List<ActividadDto> riaDtos = negociacionControl.consultarTemas(procedimientos.stream()
                .map(ArchivoTecnologiasNegociacionRiasCapitaDto::getTema)
                .distinct()
                .collect(Collectors.toList()));
        List<ArchivoTecnologiasNegociacionRiasCapitaDto> rutasInexistente = procedimientos.stream()
                .filter(archivoDto -> !riaDtos.stream()
                        .map(ActividadDto::getDescripcion)
                        .collect(Collectors.toList())
                        .contains(archivoDto.getTema()))
                .collect(Collectors.toList());
        procedimientos.removeAll(rutasInexistente);
        return rutasInexistente.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionRiasCapitaEnum.TEMA_NO_EXISTE))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasRIasCapitaDto> validarRangoPoblacionalInexistente(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos) {
        List<RangoPoblacionDto> riaDtos = negociacionControl.consultarRangosPoblacionales(procedimientos.stream()
                .map(ArchivoTecnologiasNegociacionRiasCapitaDto::getRangoPoblacion)
                .distinct()
                .collect(Collectors.toList()));
        List<ArchivoTecnologiasNegociacionRiasCapitaDto> rutasInexistente = procedimientos.stream()
                .filter(archivoDto -> !riaDtos.stream()
                        .map(RangoPoblacionDto::getDescripcion)
                        .collect(Collectors.toList())
                        .contains(archivoDto.getRangoPoblacion()))
                .collect(Collectors.toList());
        procedimientos.removeAll(rutasInexistente);
        return rutasInexistente.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionRiasCapitaEnum.RANGO_POBLACION_NO_EXISTE))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasRIasCapitaDto> validarRutaInexistente(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos) {
        List<RiaDto> riaDtos = negociacionControl.consultarRutas(procedimientos.stream()
                .map(ArchivoTecnologiasNegociacionRiasCapitaDto::getRuta)
                .distinct()
                .collect(Collectors.toList()));
        List<ArchivoTecnologiasNegociacionRiasCapitaDto> rutasInexistente = procedimientos.stream()
                .filter(archivoDto -> !riaDtos.stream()
                        .map(RiaDto::getDescripcion)
                        .collect(Collectors.toList())
                        .contains(archivoDto.getRuta()))
                .collect(Collectors.toList());
        procedimientos.removeAll(rutasInexistente);
        procedimientos.forEach(archivoDto -> riaDtos.stream()
                .filter(riaDto -> Objects.equals(riaDto.getDescripcion(), archivoDto.getRuta()))
                .findFirst()
                .ifPresent(riaDto -> archivoDto.setRutaId(riaDto.getId())));
        return rutasInexistente.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionRiasCapitaEnum.RUTA_NO_EXISTE))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasRIasCapitaDto> validarNivelComplejidadRias(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos,
                                                                                                    List<ProcedimientoDto> tecnologiasMaestras,
                                                                                                    List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadorPorNegociacion,
                                                                                                    NegociacionDto negociacion) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        obtenerNivelComplejidadTecnologiaRias(procedimientos, tecnologiasMaestras);
        List<ArchivoTecnologiasNegociacionRiasCapitaDto> nivelComplejidadInvalido = new ArrayList<>();
        procedimientos.stream()
                .filter(procedimiento -> contieneCodigoEmssanar(tecnologiasMaestras, procedimiento))
                .filter(procedimiento -> contieneIdServicioSalud(tecnologiasMaestras, procedimiento))
                .forEach(procedimiento -> {
                    List<ServiciosHabilitadosRespNoRepsDto> nivelesServicioPorSede = serviciosHabilitadorPorNegociacion.stream()
                            .filter(obtenerPredicadoPorTipoDeImportacion(procedimiento))
                            .collect(Collectors.toList());
                    if (isValidoNivelComplejidad(procedimiento, nivelesServicioPorSede)) {
                        nivelComplejidadInvalido.add(procedimiento);
                    }
                });
        procedimientos.removeAll(nivelComplejidadInvalido);

        List<ErroresImportTecnologiasRIasCapitaDto> nivelTecnologiaNegociacion = nivelComplejidadInvalido.stream()
                .filter(archivo -> archivo.getNivelComplejidadTecnologia() > negociacion.getComplejidad().getId())
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionRiasCapitaEnum.NIVEL_COMPLEJIDAD_NEGOCIACION))
                .collect(Collectors.toList());

        List<ErroresImportTecnologiasRIasCapitaDto> nivelTecnologiaMaestro = nivelComplejidadInvalido.stream()
                .filter(archivo -> archivo.getNivelComplejidadTecnologia() <= negociacion.getComplejidad().getId())
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionRiasCapitaEnum.NIVEL_COMPLEJIDAD_MAESTROS))
                .collect(Collectors.toList());

        return Stream.of(nivelTecnologiaNegociacion, nivelTecnologiaMaestro).flatMap(Collection::parallelStream).collect(Collectors.toList());
    }

    private boolean contieneCodigoEmssanar(List<ProcedimientoDto> tecnologiasMaestras, ArchivoTecnologiasNegociacionRiasCapitaDto procedimiento) {
        return tecnologiasMaestras.stream()
                .map(AbstractProcedimiento::getCodigoCliente)
                .collect(Collectors.toList())
                .contains(procedimiento.getCodigoEmssanar());
    }

    private boolean contieneIdServicioSalud(List<ProcedimientoDto> tecnologiasMaestras, ArchivoTecnologiasNegociacionRiasCapitaDto procedimiento) {
        return tecnologiasMaestras.stream()
                .map(procedimientoDto -> procedimientoDto.getServicioSalud().getId())
                .collect(Collectors.toList())
                .contains(procedimiento.getServicioId());
    }

    private Predicate<ServiciosHabilitadosRespNoRepsDto> obtenerPredicadoPorTipoDeImportacion(ArchivoTecnologiasNegociacionRiasCapitaDto procedimiento) {
        return serviciohabilitado -> Objects.equals(serviciohabilitado.getServicioId(), procedimiento.getServicioId());
    }

    private boolean isValidoNivelComplejidad(ArchivoTecnologiasNegociacionRiasCapitaDto procedimiento, List<ServiciosHabilitadosRespNoRepsDto> nivelesServicioPorSede) {
        return nivelesServicioPorSede.stream()
                .filter(serviciosHabilitadosRespNoRepsDto -> Objects.nonNull(procedimiento.getNivelComplejidadTecnologia())
                        && serviciosHabilitadosRespNoRepsDto.getNivelComplejidadMinimo() < procedimiento.getNivelComplejidadTecnologia())
                .count() == nivelesServicioPorSede.size();
    }

    private Collection<? extends ErroresImportTecnologiasRIasCapitaDto> validarHabilitacionServiciosRepsRias(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, List<ServiciosHabilitadosRespNoRepsDto> serviciosHabilitadorPorNegociacion) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        List<ArchivoTecnologiasNegociacionRiasCapitaDto> serviciosNoHabilitadosParaNingunaSede = procedimientos.stream()
                .filter(archivo -> !serviciosHabilitadorPorNegociacion.stream()
                        .map(ServiciosHabilitadosRespNoRepsDto::getCodigoServicio)
                        .collect(Collectors.toList())
                        .contains(archivo.getCodigoServicio()))
                .collect(Collectors.toList());
        procedimientos.removeAll(serviciosNoHabilitadosParaNingunaSede);
        return serviciosNoHabilitadosParaNingunaSede.stream()
                .filter(archivoTecnologiasNegociacionEventoDto -> !serviciosHabilitadorPorNegociacion.stream()
                        .map(ServiciosHabilitadosRespNoRepsDto::getCodigoServicio)
                        .collect(Collectors.toList())
                        .contains(archivoTecnologiasNegociacionEventoDto.getCodigoServicio()))
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionRiasCapitaEnum.SERVICIOS_NO_HABILITADOS))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasRIasCapitaDto> validarAsociacionServicioTecnologiaRias(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, List<ProcedimientoDto> tecnologiasMaestras) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        procedimientos.stream()
                .filter(archivo -> tecnologiasMaestras.stream()
                        .map(procedimiento -> procedimiento.getServicioSalud().getCodigo())
                        .collect(Collectors.toList())
                        .contains(archivo.getCodigoServicio()))
                .forEach(archivo -> {
                    archivo.setServicioId(tecnologiasMaestras.stream()
                            .filter(procedimiento -> procedimiento.getServicioSalud().getCodigo().equals(archivo.getCodigoServicio()))
                            .findFirst()
                            .orElse(new ProcedimientoDto())
                            .getServicioSalud()
                            .getId());
                    archivo.setProcedimientoServicioId(tecnologiasMaestras.stream()
                            .filter(procedimiento -> Objects.equals(procedimiento.getCodigoCliente(), archivo.getCodigoEmssanar()) && Objects.equals(procedimiento.getServicioSalud().getCodigo(), archivo.getCodigoServicio()))
                            .findFirst()
                            .orElse(new ProcedimientoDto())
                            .getId());
                });

        List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientosEnMaestra = procedimientos.stream()
                .filter(procedimiento -> Objects.isNull(procedimiento.getProcedimientoServicioId()))
                .collect(Collectors.toList());

        procedimientos.removeAll(procedimientosEnMaestra);

        return procedimientosEnMaestra.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionRiasCapitaEnum.TECNOLOGIA_NO_SERVICIO))
                .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasRIasCapitaDto> validarCamposObligatoriosRias(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos) {
        List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientosNoCumplen = procedimientos.stream()
                .filter(archivo -> archivo.isNoValidoCodigoServicio() || archivo.isNoValidoCodigoEmssanar()
                        || archivo.isNoValidoRuta() || archivo.isNoValidoRangoPoblacion()
                        || archivo.isNoValidoTema() || archivo.isNoValidoPorcentajeReferente())
                .collect(Collectors.toList());
        procedimientos.removeAll(procedimientosNoCumplen);
        return procedimientosNoCumplen.stream()
                .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionRiasCapitaEnum.CAMPOS_OBLIGATORIOS))
                .collect(Collectors.toList());
    }

    private ErroresImportTecnologiasRIasCapitaDto convertirArchivosAErrorImportacion(ArchivoTecnologiasNegociacionRiasCapitaDto archivo, ErrorTecnologiasNegociacionRiasCapitaEnum errorEnum) {
        return new ErroresImportTecnologiasRIasCapitaDto(
                errorEnum.getCodigo(),
                errorEnum.getMensaje(),
                archivo.getLineaArchivo(),
                archivo.getCodigoHabilitacionSede(),
                archivo.getCodigoEmssanar(),
                archivo.getCodigoServicio(),
                archivo.getRuta(),
                archivo.getTema(),
                archivo.getRangoPoblacion());
    }

    private void addRegistroValido(Integer opcion, List<Long> sedeCapituloIds, ArchivoTecnologiasNegociacionPgpDto procedimientoArchivo, ProcedimientoDto procedimientoPGP, NegociacionDto negociacion) {
		ConcurrentHashMap<List<Long>, ProcedimientoNegociacionDto> registro = new ConcurrentHashMap<>();
		registro.put(sedeCapituloIds, construirProcedimientoNegociacion(procedimientoArchivo,
				procedimientoPGP,
				negociacion));

		switch(opcion) {
			case 1: //insert
				registrosCorrectosInsert.add(registro);
				break;
			case 2: //update
				registrosCorrectosUpdate.add(registro);
				break;
			default:
				break;
		}
		procedimientosCorrectos.add(procedimientoPGP.getCodigoCliente());
	}

	private void actualizarCapitulosEnNegociacion(Long negociacionId, Integer userId) throws ConexiaBusinessException {
		Map<Long, Long>mapSncIds = new HashMap<Long, Long>();
		allSedeCapituloIds.stream().forEach(
					sncId -> {
						if(!mapSncIds.containsKey(sncId)) {
							mapSncIds.put(sncId, sncId);
						}
					}
				);

		if(Objects.nonNull(mapSncIds)) {

			allSedeCapituloIds.clear();
			allSedeCapituloIds = (List<Long>) mapSncIds.values().stream()
									.sorted((snc1, snc2) -> Long.compare(snc1, snc2))
									.collect(Collectors.toList());

			//Actualizar calculo de agrupador(sedeNegociacionCapitulo)
			control.actualizarValoresCapitulosBySncId(negociacionId, allSedeCapituloIds, userId);
		}
	}

	private void eliminarProcedimientosEnNegociacion(List<ProcedimientoDto> procedimientos, Long negociacionId, Integer userId) throws ConexiaBusinessException {
		List<Long> capitulosEliminarIds = new ArrayList<Long>();
		List<Long> procedimientosEliminarIds = new ArrayList<Long>();

		for(ProcedimientoDto ptoEliminar: procedimientos) {
			procedimientosEliminarIds.add(ptoEliminar.getId());
			if(!capitulosEliminarIds.contains(ptoEliminar.getCapituloProcedimiento().getId())) {
				capitulosEliminarIds.add(ptoEliminar.getCapituloProcedimiento().getId());
			}
		}

		//Eliminar procedimientos
		control.eliminarProcedimientosNegociacionByImport(
				procedimientosEliminarIds,
				capitulosEliminarIds,
				negociacionId,
				userId);
		//Eliminar capítulos que no tengan procedimientos
		control.eliminarSedeCapitulosSinProcedimientos(capitulosEliminarIds, negociacionId, userId);
	}

	private void insertarProcedimientosEnNegociacion(List<ConcurrentHashMap<List<Long>, ProcedimientoNegociacionDto>> procedimientos,NegociacionDto negociacion) throws ConexiaBusinessException {
		if(negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)){
			this.control.insertarProcedimientosNegociacionPGP(procedimientos, negociacion.getReferenteDto().getId());
		}
	}

	private void actualizarProcedimientosEnNegociacion(List<ConcurrentHashMap<List<Long>, ProcedimientoNegociacionDto>> procedimientos, NegociacionDto negociacion) throws ConexiaBusinessException {
		if(negociacion.getTipoModalidadNegociacion().equals(NegociacionModalidadEnum.PAGO_GLOBAL_PROSPECTIVO)){
			this.control.actualizarProcedimientosNegociacionPGP(procedimientos, negociacion.getId());
		}
	}

	private ProcedimientoNegociacionDto construirProcedimientoNegociacion(ArchivoTecnologiasNegociacionPgpDto procedimientoArchivo, ProcedimientoDto ptoPGP, NegociacionDto negociacion) {
		ProcedimientoNegociacionDto ptoImport = new ProcedimientoNegociacionDto();
		ProcedimientoDto pto = new ProcedimientoDto();
		pto.setId(ptoPGP.getId());
		ptoImport.setProcedimientoDto(pto);

		if(Objects.nonNull(procedimientoArchivo.getFrecuencia())) {
			ptoImport.setFrecuenciaUsuario(new Double(procedimientoArchivo.getFrecuencia()));
		}

		if(Objects.nonNull(procedimientoArchivo.getCmu())) {
			ptoImport.setCostoMedioUsuario(new BigDecimal(procedimientoArchivo.getCmu()));
		}

		if(Objects.nonNull(procedimientoArchivo.getFranjaInicio()) && Objects.nonNull(procedimientoArchivo.getFranjaFin())) {
			ptoImport.setFranjaInicio(new BigDecimal(procedimientoArchivo.getFranjaInicio()));
			ptoImport.setFranjaFin(new BigDecimal(procedimientoArchivo.getFranjaFin()));
		}

		if(Objects.nonNull(procedimientoArchivo.getFrecuencia())
				&& Objects.nonNull(procedimientoArchivo.getCmu())
				&& Objects.nonNull(negociacion.getPoblacion())) {
			ptoImport.setValorNegociado(new BigDecimal(ptoImport.getFrecuenciaUsuario())
					.multiply(ptoImport.getCostoMedioUsuario())
					.multiply(new BigDecimal(negociacion.getPoblacion())));
			ptoImport.setNegociado(true);
		} else {
			ptoImport.setNegociado(false);
		}

		return ptoImport;
	}

    private Collection<? extends ErroresImportTecnologiasRIasCapitaDto> validarTecnologiaInexistenteRias(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, List<ProcedimientoDto> procedimientosMaestros) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientosInactivos = procedimientos.stream()
            .filter(procedimiento -> !procedimientosMaestros.stream()
                .map(AbstractProcedimiento::getCodigoCliente)
                .collect(Collectors.toList())
                .contains(procedimiento.getCodigoEmssanar()))
            .collect(Collectors.toList());
        procedimientos.removeAll(procedimientosInactivos);
        return procedimientosInactivos.stream()
            .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionRiasCapitaEnum.TECNOLOGIA_NO_EXISTE))
            .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasRIasCapitaDto> validarTipoTecnologiaInvalidaRias(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, List<ProcedimientoDto> procedimientosMaestros) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientosInactivos = procedimientos.stream()
            .filter(procedimiento -> procedimientosMaestros.stream().anyMatch(isDiferenteDeProcedimiento(procedimiento.getCodigoEmssanar())))
            .collect(Collectors.toList());
        procedimientos.removeAll(procedimientosInactivos);
        return procedimientosInactivos.stream()
            .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionRiasCapitaEnum.TIPO_TECNOLOGIA_INVALIDO))
            .collect(Collectors.toList());
    }

    private Collection<? extends ErroresImportTecnologiasRIasCapitaDto> validarProcedimientosInactivosRias(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, List<ProcedimientoDto> procedimientosMaestros) {
        if (procedimientos.isEmpty()) {
            return new ArrayList<>();
        }
        List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientosInactivos = procedimientos.stream()
            .filter(procedimiento -> procedimientosMaestros.stream()
                .anyMatch(procedimientoDto -> isTecnologiaInactiva(procedimiento.getCodigoEmssanar(), procedimientoDto)))
            .collect(Collectors.toList());
        procedimientos.removeAll(procedimientosInactivos);
        return procedimientosInactivos.stream()
            .map(archivo -> convertirArchivosAErrorImportacion(archivo, ErrorTecnologiasNegociacionRiasCapitaEnum.TECNOLOGIA_DEROGADA))
            .collect(Collectors.toList());
    }

    private void obtenerNivelComplejidadTecnologiaRias(List<ArchivoTecnologiasNegociacionRiasCapitaDto> procedimientos, List<ProcedimientoDto> procedimientosMaestros) {
        for (ArchivoTecnologiasNegociacionRiasCapitaDto procedimiento : procedimientos) {
            Optional<ProcedimientoDto> primeraCoincidencia = procedimientosMaestros.stream()
                .filter(isIgualTecnologiaArchivosVsTecnologiaMaestra(procedimiento))
                .findFirst();
            primeraCoincidencia.ifPresent(procedimientoDto -> procedimiento.setNivelComplejidadTecnologia(procedimientoDto.getComplejidad()));
        }
    }

    private Predicate<ProcedimientoDto> isIgualTecnologiaArchivosVsTecnologiaMaestra(ArchivoTecnologiasNegociacionRiasCapitaDto procedimiento) {
        return procedimientoDto -> Objects.equals(procedimientoDto.getServicioSalud()
            .getId(), procedimiento.getServicioId()) && Objects.equals(procedimientoDto.getCodigoCliente(), procedimiento.getCodigoEmssanar());
    }
}