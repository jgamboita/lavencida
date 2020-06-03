package co.conexia.negociacion.wap.controller.negociacion.modalidad.sedesasede;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpSession;

import com.conexia.contratacion.commons.constants.CommonConstants;
import com.conexia.contratacion.commons.constants.enums.ErrorAfiliadosPgpEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionSessionEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.dto.contractual.parametrizacion.SedePrestadorDto;
import com.conexia.contratacion.commons.dto.maestros.AfiliadoDto;
import com.conexia.contratacion.commons.dto.maestros.MunicipioDto;
import com.conexia.contratacion.commons.dto.negociacion.ArchivoAfiliadosPgpDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.ReglaNegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.logfactory.Log;

import co.conexia.negociacion.wap.controller.configuracion.NegociacionServicioQualifier;
import co.conexia.negociacion.wap.controller.negociacion.modalidad.TecnologiaController;
import co.conexia.negociacion.wap.facade.negociacion.modalidad.sedeasede.NegociacionServiciosSSFacade;

/**
 * Controller que maneja el archivo de importación afiliados en negociación PGP
 * @author cguerrero
 *
 */
@Named
@ViewScoped
@NegociacionServicioQualifier
public class NegociacionAfiliadosPgpSSController extends TecnologiaController implements Serializable {


	private static final long serialVersionUID = 7698100026065590695L;

	@Inject
	private Log logger;

	@Inject
	private NegociacionServiciosSSFacade facade;


	private List<ConcurrentHashMap<Long, Long>> registrosCorrectos = new ArrayList<ConcurrentHashMap<Long, Long>>();

	private String si = "SI";

	private String no = "NO";

	private List<ReglaNegociacionDto> reglasNegociacion;

	@PostConstruct
    public void postConstruct() {
		FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        negociacion = (NegociacionDto) session.getAttribute(NegociacionSessionEnum.NEGOCIACION.toString());
	}

	public List<ConcurrentHashMap<ArchivoAfiliadosPgpDto, ErrorAfiliadosPgpEnum>> validarAfiliadosNegociacionPgp(List<ArchivoAfiliadosPgpDto> afiliados, NegociacionDto negociacion){

		try {
			reglasNegociacion = this.negociacionFacade.getListReglasNegociacion(negociacion.getId());
		} catch (ConexiaBusinessException e) {
			this.facesMessagesUtils.addError("Error cargando reglas de la negociación");
			logger.error("Error al cargar reglas de la negociación", e);
		}


		List<ConcurrentHashMap<ArchivoAfiliadosPgpDto, ErrorAfiliadosPgpEnum>> errores = new ArrayList<ConcurrentHashMap<ArchivoAfiliadosPgpDto, ErrorAfiliadosPgpEnum>>();
		List<ConcurrentHashMap<String, Long>> afiliadosAEliminar = new ArrayList<>();
		registrosCorrectos = new ArrayList<ConcurrentHashMap<Long, Long>>();

		List<MunicipioDto> municipiosCobertura = new ArrayList<MunicipioDto>();

		for(ArchivoAfiliadosPgpDto afiliado : afiliados){
			//¿Afiliado existe?
			ConcurrentHashMap<ArchivoAfiliadosPgpDto, ErrorAfiliadosPgpEnum> error = new ConcurrentHashMap<>();
			AfiliadoDto afiliadoDB = facade.findAfiliadoByTipoYNumeroIdentificacion(afiliado.getTipoDocumentoAfiliado(), afiliado.getNumeroDocumentoAfiliado(), negociacion.getFechaCorte());
			ConcurrentHashMap<String, Long> afiliadoEliminar = new ConcurrentHashMap<>();

			if(Objects.isNull(afiliadoDB)){
				error.put(afiliado, ErrorAfiliadosPgpEnum.AFILIADO_NO_EXISTE);
			} else{

				List<SedePrestadorDto> sedesIpsDB = facade.findSedeIpsByNegociacionId(negociacion.getId());
				List<Long> sedesNegociacionIds = new ArrayList<>();
				for (SedePrestadorDto sedePrestador: sedesIpsDB) {
					sedesNegociacionIds.add(sedePrestador.getSedeNegociacionId());
				}

					try {
						municipiosCobertura = negociacionFacade.obtenerMunicipiosAreaCobertura(negociacion.getId(), sedesNegociacionIds);
					} catch (ConexiaBusinessException e) {
						logger.error("Error al cargar los municipios del área de cobertura ", e);
					}

					if(!si.equals(afiliado.getEliminarAfiliado()) && !no.equals(afiliado.getEliminarAfiliado())){
						error.put(afiliado, ErrorAfiliadosPgpEnum.VALOR_NO_PERMITIDO_ELIMINAR);
					} else{
						if(!afiliado.getCodigoUnicoAfiliado().equals(afiliadoDB.getCodigoUnicoAfiliado())){
							error.put(afiliado, ErrorAfiliadosPgpEnum.CODIGO_UNICO_AFILIADO_NO_CORRESPONDE);
						} else{
							if(si.equals(afiliado.getEliminarAfiliado())){
								for (SedePrestadorDto sedePrestador: sedesIpsDB) {
									afiliadoEliminar.put("afiliadoId", afiliadoDB.getId());
									afiliadoEliminar.put("sedeNegociacionId", sedePrestador.getSedeNegociacionId());
									afiliadosAEliminar.add(afiliadoEliminar);
								}

							} else {

								Boolean enAreaCoberturaNeg = false;
								Boolean enAreaCoberturaSede = false;
								for(MunicipioDto municipio: negociacion.getMunicipiosPGP()) {
									if(municipio.getId().equals(afiliadoDB.getMunicipio().getId())) {
										enAreaCoberturaNeg = true;
										break;
									}
								}

								for(MunicipioDto municipio: municipiosCobertura) {
									if(municipio.getId().equals(afiliadoDB.getMunicipio().getId())) {
										enAreaCoberturaSede = true;
										break;
									}
								}

								if(!(enAreaCoberturaNeg && enAreaCoberturaSede)){
									error.put(afiliado, ErrorAfiliadosPgpEnum.AFILIADO_NO_SE_ENCUENTRA_EN_ZONA_COBERTURA_SEDE_IPS);
								}

								if(!CommonConstants.ESTADO_AFILIADO_ACTIVO.equals(afiliadoDB.getEstadoAfiliacion())){
									error.put(afiliado, ErrorAfiliadosPgpEnum.AFILIADO_INACTIVO);
								}

								if(!RegimenNegociacionEnum.AMBOS.equals(negociacion.getRegimen()) &&
										!negociacion.getRegimen().getId().equals(afiliadoDB.getRegimenAfiliacionId().intValue())){
									error.put(afiliado, ErrorAfiliadosPgpEnum.AFILIADO_TIENE_REGIMEN_DIFERENTE_A_NEGOCIACION);
								}
									//APLICACIÓN DE LAS REGLAS DE PGP
								if(reglasNegociacion.size() > 0) {//si existen reglas para la negociación

									if(afiliadoDB.getGenero() != null && afiliadoDB.getFechaNacimiento() != null) {

										Long correctosRegla = validacionReglasPorAfiliado(reglasNegociacion, afiliadoDB);

										if(correctosRegla > 0 && error.size() == 0) {//Si el afiliado cumple con al menos una regla puede ser agregado
											for (SedePrestadorDto sedePrestador: sedesIpsDB) {
												ConcurrentHashMap<Long, Long> registro = new ConcurrentHashMap<>();
												registro.put(sedePrestador.getSedeNegociacionId(), afiliadoDB.getId());
												registrosCorrectos.add(registro);
											}

										} else {
											error.put(afiliado, ErrorAfiliadosPgpEnum.AFILIADO_INCUMPLE_REGLA);
										}

									} else {
										error.put(afiliado, ErrorAfiliadosPgpEnum.AFILIADO_FALTA_GENERO_EDAD);
									}
								} else {//Si no existen reglas para la negociación se añade el afiliado
									if(error.size() == 0){
										for (SedePrestadorDto sedePrestador: sedesIpsDB) {
											ConcurrentHashMap<Long, Long> registro = new ConcurrentHashMap<>();
											registro.put(sedePrestador.getSedeNegociacionId(), afiliadoDB.getId());
											registrosCorrectos.add(registro);
										}

									}
								}


							}
						}

					}
			}
			if(Objects.nonNull(error) && error.size() > 0) {//Se valida que el list de errores realmente contenga un registro de afiliado que no pasó las validaciones
				errores.add(error);
			}

		}

		if(Objects.nonNull(afiliadosAEliminar)){
			afiliadosAEliminar.forEach(afiliadoDelete -> facade.eliminarAfiliadoNegociacionPgp(afiliadoDelete.get("afiliadoId"), afiliadoDelete.get("sedeNegociacionId")));
		}

		if(Objects.nonNull(registrosCorrectos)){
			insertarAfiliadosPorSedeNegociacion(registrosCorrectos);
		}
		return errores;
	}

	private void insertarAfiliadosPorSedeNegociacion(List<ConcurrentHashMap<Long, Long>> registrosCorrectos) {
		for(ConcurrentHashMap<Long, Long> registroCorrecto : registrosCorrectos){
			for (ConcurrentHashMap.Entry<Long, Long> registro : registroCorrecto.entrySet()) {
				facade.insertarAfiliadosPorSedeNegociacion(registro.getValue(), registro.getKey());
			}
		}
	}

	public List<ConcurrentHashMap<Long, Long>> getRegistrosCorrectos() {
		return registrosCorrectos;
	}

	public void setRegistrosCorrectos(List<ConcurrentHashMap<Long, Long>> registrosCorrectos) {
		this.registrosCorrectos = registrosCorrectos;
	}

	public List<ReglaNegociacionDto> getReglasNegociacion() {
		return reglasNegociacion;
	}

	public void setReglasNegociacion(List<ReglaNegociacionDto> reglasNegociacion) {
		this.reglasNegociacion = reglasNegociacion;
	}



}
