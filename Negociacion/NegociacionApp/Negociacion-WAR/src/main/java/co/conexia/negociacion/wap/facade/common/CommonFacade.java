package co.conexia.negociacion.wap.facade.common;

import java.io.Serializable;
import java.util.List;

import javax.inject.Inject;

import com.conexia.contratacion.commons.constants.enums.EstadoPrestadorEnum;
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
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.common.CommonTransactionalServiceRemote;
import com.conexia.negociacion.definitions.common.CommonViewServiceRemote;
import com.conexia.servicefactory.CnxService;

public class CommonFacade implements Serializable {

	@Inject
	@CnxService
	private CommonViewServiceRemote commonView;

	@Inject
    @CnxService
    private CommonTransactionalServiceRemote commonTransactional;

	public List<TipoIdentificacionDto> listarTiposDocumento() {
		return this.commonView.listarTiposIdentificacion();
	}

	public List<TipoTarifarioDto> listarTiposTarifarios(){
		return commonView.listarTiposTarifarios();
	}

	public List<SedesNegociacionDto> listarSedesNegociacionById(Long negociacionId){
		return commonView.listarSedesNegociacionById(negociacionId);
	}

	public Integer contarPoblacionNegociacionById(Long negociacionId) {
		return commonView.contarPoblacionNegociacionById(negociacionId);
	}

	public PrestadorDto consultarPrestador(Long prestadorId) {
		return this.commonView.consultarPrestador(prestadorId);
	}

    public void actualizarEstadoPrestadoresByIds(EstadoPrestadorEnum estado, List<Long> prestadoresId) {
        this.commonTransactional.actualizarEstadoPrestadoresByIds(estado,
                prestadoresId);
    }

	public List<ZonaCapitaDto> listarZonasCapita(){
		return commonView.listarZonasCapita();
	}

	public List<RegimenAfiliacionDto> listarRegimenes(){
		return commonView.listarRegimenes();
	}

    public List<Integer> listarAniosCapita(){
        return commonView.listarAniosCapita();
    }

    public List<RegionalDto> listarRegionales(){
    	return commonView.listarRegionales();
    }

    public List<DepartamentoDto> listarDepartamentosPorRegional(Integer regionalId) {
    	return commonView.listarDepartamentosPorRegional(regionalId);
    }

    public List<MunicipioDto> listarMunicipiosPorDepartamento(Integer departamentoId) {
    	return commonView.listarMunicipiosPorDepartamento(departamentoId);
    }

    public List<CapituloProcedimientoDto> listarCapitulos(){
    	return commonView.listarCapitulos();
    }

    public List<CategoriaProcedimientoDto> listarCategoriasPorCapitulo(final List<CapituloProcedimientoDto>  capitulos){
    	return commonView.listarCategoriasPorCapitulo(capitulos);
    }

    public List<ZonaMunicipioDto> listarZonas(Integer regionalId){
    	return commonView.listarZonas(regionalId);
    }

    public List<MunicipioDto> listarZonasCobertura(){
    	return commonView.listarZonasCobertura();
    }

	public List<SedePrestadorDto> getSedesPrestadorByNegociacion(NegociacionDto negociacion) {
		return commonView.getSedesPrestadorByNegociacion(negociacion);
	}

	public List<PrestadorDto> getPrestadorByNombre(String nombre) {
		return commonView.getPrestadorByNombre(nombre);
	}

	public List<ReferenteDto> listarReferentePgp(){
		return commonView.listarReferentePgp();
	}

	public ReferenteDto buscarReferenteId(Long referenteId){
		return commonView.buscarReferenteId(referenteId);
	}

	public List<String> modalidadesReferente(Long referenteId){
		return commonView.modalidadesReferente(referenteId);
	}

	public ReferenteUbicacionDto buscarReferenteUbicacion(Long referenteId){
		return commonView.buscarReferenteUbicacion(referenteId);
	}

	public List<ReferentePrestadorDto> buscarSedesReferente(Long referenteId){
		return commonView.buscarSedesReferente(referenteId);
	}

	public Integer poblacionTotalReferente(ReferenteDto referente,ReferenteUbicacionDto referenteUbicacion, List<ReferentePrestadorDto> referentePrestador){
		return commonView.poblacionTotalReferente(referente,referenteUbicacion, referentePrestador);
	}

	public List<RangoPoblacionDto> listarRangoPoblacion(){
		return this.commonView.listarRangoPoblacion();
	}

	public List<RiaDto> listarRias(){
		return this.commonView.listarRias();
	}

	public List<DepartamentoDto> listarDepartamentos() {
		return this.commonView.listarDepartamentos();
	}

	public List<MunicipioDto> listarMunicipiosPorDepartameto(final Integer idDepartamento) {
        return this.commonView.listarMunicipiosPorDepartameto(idDepartamento);
    }

	public List<MunicipioDto> listarMunicipiosPorDepartametoZona(Integer idDepartamento){
		return this.commonView.listarMunicipiosPorDepartametoZona(idDepartamento);
	}

	public List<RangoPoblacionDto> listarRangoPorRia(List<Integer> listaRiaId){
		return this.commonView.listarRangoPorRia(listaRiaId);
	}

	public List<RiaDto> consultarTotalRiasByNegociacion(Long negociacionId){
		return this.commonView.consultarTotalRiasByNegociacion(negociacionId);
	}

	public void calcularTotalRiasByNegociacion(Long negociacionId){
		this.commonTransactional.calcularTotalRiasByNegociacion(negociacionId);
	}

	public void calcularTotalRiasByNegociacionPGP(Long negociacionId) throws ConexiaBusinessException{
		this.commonTransactional.calcularTotalRiasByNegociacionPGP(negociacionId);
	}

	public void calcularTotalByNegociacionPGP(Long negociacionId) throws ConexiaBusinessException{
		this.commonTransactional.calcularTotalByNegociacionPGP(negociacionId);
	}

	public List<GrupoEtnicoDto> listarGruposEtnicos() {
		return commonView.listarGruposEtnicos();
	}
}
