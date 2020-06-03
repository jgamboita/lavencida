package co.conexia.negociacion.services.referentePgp.boundary;

import co.conexia.negociacion.services.referentePgp.control.GestionReferentePgpControl;
import com.conexia.contratacion.commons.constants.enums.FiltroReferentePgpEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contratacion.commons.constants.enums.RegionalEnum;
import com.conexia.contratacion.commons.constants.enums.TipoContratoEnum;
import com.conexia.contratacion.commons.dto.maestros.*;
import com.conexia.contratacion.commons.dto.negociacion.AnexoReferenteMedicamentosDto;
import com.conexia.contratacion.commons.dto.negociacion.AnexoReferenteProcedimientosDto;
import com.conexia.contratacion.commons.dto.referente.*;
import com.conexia.negociacion.definitions.referentePgp.GestionReferentePgpViewServiceRemote;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;


/**
 * Boundary del referente pgp para los servicios de consulta
 *
 * @author dmora
 *
 */
@Stateless
@Remote(GestionReferentePgpViewServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
public class GestionReferentePgpViewBoundary implements GestionReferentePgpViewServiceRemote{

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;

	@Inject
	private GestionReferentePgpControl gestionReferenteControl;


	public List<ReferenteCapituloDto> listarCapitulosReferenteNuevo(Date fechaInicio, Date fechaFIn,
			RegimenNegociacionEnum regimen, RegionalDto regional,ZonaMunicipioDto zona,
			DepartamentoDto departamento, MunicipioDto municipio,List<ReferentePrestadorDto> sedePrestador,
			List<CapituloProcedimientoDto> capituloProcedimiento, List<CategoriaProcedimientoDto> categoriaProcedimiento,
			FiltroReferentePgpEnum filtroReferente, ReferenteDto referente, List<TipoContratoEnum> tipoContrato){

		return this.gestionReferenteControl.listarCapitulosReferenteNuevo(fechaInicio, fechaFIn, regimen, regional,
												zona, departamento, municipio, sedePrestador,capituloProcedimiento,categoriaProcedimiento,
												filtroReferente,referente,tipoContrato);
	}

	public List<ReferenteCapituloDto> cargarReferenteCapituloPorReferente(Long referenteId){
		List<ReferenteCapituloDto> listReferenteCapitulo = new ArrayList<>();
		listReferenteCapitulo = em.createNamedQuery("ReferenteCapitulo.buscarReferenteCapitulo", ReferenteCapituloDto.class)
				.setParameter("referenteId", referenteId)
				.getResultList();

		return listReferenteCapitulo;
	}

	public List<CapituloProcedimientoDto> capitulosReferente(Long referenteId){
		List<CapituloProcedimientoDto> listarCapitulos = new ArrayList<>();
		listarCapitulos = em.createNamedQuery("CapituloProcedimiento.capitulosReferente", CapituloProcedimientoDto.class)
				.setParameter("referenteId", referenteId)
				.getResultList();

		return listarCapitulos;
	}

	public List<CategoriaProcedimientoDto> categoriasReferente(Long referenteId){
		List<CategoriaProcedimientoDto> listarCategorias = new ArrayList<>();
		listarCategorias = em.createNamedQuery("CategoriaProcedimiento.categoriasReferente", CategoriaProcedimientoDto.class)
				.setParameter("referenteId", referenteId)
				.getResultList();

		return listarCategorias;
	}

	public List<ReferenteCategoriaMedicamentoDto> cargarReferenteCategotiasReferente(Long referenteId){
		List<ReferenteCategoriaMedicamentoDto> listReferenteCategorias = new ArrayList<>();
		listReferenteCategorias = em.createNamedQuery("ReferenteCategoriaMedicamento.buscarReferenteCategoriasMedicamento", ReferenteCategoriaMedicamentoDto.class)
				.setParameter("referenteId", referenteId)
				.getResultList();

		return listReferenteCategorias;
	}

	public List<ReferenteCapituloDto> cargarCapitulosPorReferente(Long referenteId){
		List<ReferenteCapituloDto> listReferenteCapitulo = new ArrayList<>();
		listReferenteCapitulo = em.createNamedQuery("ReferenteCapitulo.buscarReferenteCapitulo", ReferenteCapituloDto.class)
				.setParameter("referenteId", referenteId)
				.getResultList();

		return listReferenteCapitulo;
	}

	public  Long buscarReferenteCreado(String descripcionReferente,RegimenNegociacionEnum regimen) {
		return em.createNamedQuery("Referente.findAllDescripcion", Long.class)
				.setParameter("descripcion", descripcionReferente)
				.setParameter("regimen", regimen)
				.getSingleResult();
	}

	public List<ReferenteProcedimientoDto> cargarReferenteProcedimientoPorCapitulo(Long referenteCategoriaId){
		List<ReferenteProcedimientoDto> listReferenteProcedimiento = new ArrayList<>();
		listReferenteProcedimiento = em.createNamedQuery("ReferenteProcedimiento.buscarReferenteProcedimiento", ReferenteProcedimientoDto.class)
				.setParameter("referenteCapituloId", referenteCategoriaId)
				.getResultList();

		return listReferenteProcedimiento;
	}


	public List<ReferenteMedicamentoDto> cargarReferenteMedicamentoPorCategoria(Long referenteCategoriaId){
		List<ReferenteMedicamentoDto> listReferenteMedicamento = new ArrayList<>();
		listReferenteMedicamento = em.createNamedQuery("ReferenteMedicamento.buscarReferenteDetalleMedicamento", ReferenteMedicamentoDto.class)
				.setParameter("referenteCategoriaId", referenteCategoriaId)
				.getResultList();

		return listReferenteMedicamento;
	}


	public List<AnexoReferenteProcedimientosDto> exportarReferenteProcedimientoCapituloPGP(Long referenteId){
		List<AnexoReferenteProcedimientosDto> listReferenteProcedimiento = new ArrayList<>();
		listReferenteProcedimiento = em.createNamedQuery("ReferenteProcedimiento.exportarProcedimientosReferentePGP", AnexoReferenteProcedimientosDto.class)
				.setParameter("referenteId", referenteId)
				.getResultList();

		return listReferenteProcedimiento;
	}

	public List<AnexoReferenteMedicamentosDto> exportarReferenteMedicamentosPGP(Long referenteId){
		List<AnexoReferenteMedicamentosDto> listReferenteMedicamentos = new ArrayList<>();
		listReferenteMedicamentos = em.createNamedQuery("ReferenteMedicamento.exportarMedicamentosReferentePGP", AnexoReferenteMedicamentosDto.class)
				.setParameter("referenteId", referenteId)
				.getResultList();

		return listReferenteMedicamentos;
	}


	public List<ReferenteProcedimientoDto> listarProcedimientosPorCapituloReferenteNuevo(Date fechaInicio, Date fechaFIn,
			RegimenNegociacionEnum regimen, RegionalDto regional,ZonaMunicipioDto zona,
			DepartamentoDto departamento, MunicipioDto municipio,List<ReferentePrestadorDto> sedePrestador,
			List<CapituloProcedimientoDto> capituloProcedimiento, List<CategoriaProcedimientoDto> categoriaProcedimiento, FiltroReferentePgpEnum filtroReferente,
			ReferenteDto referente,List<TipoContratoEnum> tipoContrato){
		return this.gestionReferenteControl.listarProcedimientosPorCapituloReferenteNuevo(fechaInicio,
												fechaFIn, regimen, regional, zona, departamento,
												municipio, sedePrestador, capituloProcedimiento,categoriaProcedimiento,
												filtroReferente, referente,tipoContrato);
	}

	public int countReferenteCapitulo(Long referenteId){
		StringBuilder query = new StringBuilder();
		query.append("SELECT COUNT(0) FROM contratacion.referente_capitulo where referente_id in (:referenteId) ");
		Query q =  (Query) em.createNativeQuery(query.toString())
					.setParameter("referenteId", referenteId);

		return ((Number)q.getSingleResult()).intValue();

	}

}
