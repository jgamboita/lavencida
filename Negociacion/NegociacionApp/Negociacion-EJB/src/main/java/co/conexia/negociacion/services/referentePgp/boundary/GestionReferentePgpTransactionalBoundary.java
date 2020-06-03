package co.conexia.negociacion.services.referentePgp.boundary;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.conexia.contratacion.commons.constants.enums.EstadoReferentePgpEnum;
import com.conexia.contratacion.commons.constants.enums.FiltroReferentePgpEnum;
import com.conexia.contratacion.commons.constants.enums.RegimenNegociacionEnum;
import com.conexia.contractual.model.contratacion.CategoriaMedicamento;
import com.conexia.contractual.model.contratacion.SedePrestador;
import com.conexia.contractual.model.contratacion.referente.Referente;
import com.conexia.contractual.model.contratacion.referente.ReferenteCapitulo;
import com.conexia.contractual.model.contratacion.referente.ReferenteCategoriaMedicamento;
import com.conexia.contractual.model.contratacion.referente.ReferenteMedicamento;
import com.conexia.contractual.model.contratacion.referente.ReferenteModalidad;
import com.conexia.contractual.model.contratacion.referente.ReferentePrestador;
import com.conexia.contractual.model.contratacion.referente.ReferenteProcedimiento;
import com.conexia.contractual.model.contratacion.referente.ReferenteUbicacion;
import com.conexia.contractual.model.maestros.CapituloProcedimiento;
import com.conexia.contractual.model.maestros.CategoriaProcedimiento;
import com.conexia.contractual.model.maestros.Departamento;
import com.conexia.contractual.model.maestros.Medicamento;
import com.conexia.contractual.model.maestros.Municipio;
import com.conexia.contractual.model.maestros.Procedimiento;
import com.conexia.contractual.model.maestros.Regional;
import com.conexia.contractual.model.maestros.ZonaMunicipio;
import com.conexia.contractual.utils.exceptions.constants.CodigoMensajeErrorEnum;
import com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteCapituloDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteMedicamentoDto;
import com.conexia.contratacion.commons.dto.referente.ReferentePrestadorDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteProcedimientoDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteUbicacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.negociacion.definitions.referentePgp.GestionReferentePgpTransactionalServiceRemote;

import co.conexia.negociacion.services.referentePgp.control.GestionReferentePgpControl;


/**
 * Boundary del referente pgp para los servicios transaccionales
 * @author dmora
 *
 */
@Stateless
@Remote(GestionReferentePgpTransactionalServiceRemote.class)
@TransactionAttribute(TransactionAttributeType.REQUIRED)
public class GestionReferentePgpTransactionalBoundary implements GestionReferentePgpTransactionalServiceRemote{

	@PersistenceContext(unitName = "contractualDS")
	private EntityManager em;

	@Inject
	private GestionReferentePgpControl gestionReferenteControl;

    private static final Logger logger = LoggerFactory.getLogger(GestionReferentePgpTransactionalBoundary.class);

	private Referente referente = new Referente();


	public void crearReferentePgp(String descripcionReferente, RegimenNegociacionEnum regimen) throws ConexiaBusinessException{

		try {
			StringBuilder query = new StringBuilder();
			query.append("INSERT INTO contratacion.referente(descripcion,tipo_referente_id,regimen,es_general,estado_referente)"
				+ "VALUES (:descripcion,:tipoReferente,:regimen, :general,:estadoReferente) ");
			if(regimen.equals(RegimenNegociacionEnum.AMBOS)){
				for(RegimenNegociacionEnum enm : RegimenNegociacionEnum.values()){
					em.createNativeQuery(query.toString())
					.setParameter("descripcion", descripcionReferente)
					.setParameter("tipoReferente", 4)
					.setParameter("regimen", enm.toString())
					.setParameter("general", Boolean.FALSE)
					.setParameter("estadoReferente", EstadoReferentePgpEnum.EN_TRAMITE.toString())
					.executeUpdate();
				}
			}
			else{
				em.createNativeQuery(query.toString())
					.setParameter("descripcion", descripcionReferente)
					.setParameter("tipoReferente", 4)
					.setParameter("regimen", regimen.toString())
					.setParameter("general", Boolean.FALSE)
					.setParameter("estadoReferente", EstadoReferentePgpEnum.EN_TRAMITE.toString())
				.executeUpdate();
			}


		}catch (PersistenceException pe)  {
			throw new ConexiaBusinessException(CodigoMensajeErrorEnum.REFERENTE_NOMBRE,
                    "Ya existe un referente con el mismo nombre");
		}

	}

	public void insertarCapitulosReferente(Long referenteId,List<ReferenteCapituloDto> referenteCapitulos){

			for (ReferenteCapituloDto dto : referenteCapitulos) {
				ReferenteCapitulo referenteCapitulo = new ReferenteCapitulo();
				CapituloProcedimiento capitulo =  new CapituloProcedimiento();
				CategoriaProcedimiento categoria = new CategoriaProcedimiento();
				referente.setId(referenteId);
				referenteCapitulo.setReferente(referente);
				capitulo.setId(dto.getCapituloProcedimiento().getId());
				referenteCapitulo.setCapituloProcedimiento(capitulo);
				categoria.setId(dto.getCategoriaProcedimiento().getId());
				referenteCapitulo.setFrecuencia(dto.getFrecuencia());
				referenteCapitulo.setCostoMedioUsuario(dto.getCostoMedioUsuaro());
				referenteCapitulo.setNumeroAtenciones(dto.getNumeroAtenciones());
				referenteCapitulo.setNumeroUsuarios(dto.getNumeroUsuarios());
				referenteCapitulo.setEstado(1);
				referenteCapitulo.setCategoriaProcedimiento(categoria);
				em.persist(referenteCapitulo);
			}
	}

	public void insertarProcedimientosReferente(Long referenteId, List<ReferenteProcedimientoDto> listReferenteProcedimiento){
		this.gestionReferenteControl.insertarProcedimientosReferente(referenteId,listReferenteProcedimiento);
	}

	public void borrarReferenteProcedeimientoGeneral(Long referenteId){
		em.createNamedQuery("ReferenteProcedimiento.eliminarReferentePgpProcedimiento")
			.setParameter("referenteId", referenteId)
			.executeUpdate();
	}

	public void borrarReferenteCapituloGeneral(Long referenteId){
		em.createNamedQuery("ReferenteCapitulo.eliminarReferentePgpCapitulo")
			.setParameter("referenteId", referenteId)
			.executeUpdate();
	}

	public void borrarReferenteSegunFiltro(Long referenteId){
		em.createNamedQuery("ReferenteUbicacion.eliminarReferenteUbicacion")
			.setParameter("referenteId", referenteId)
			.executeUpdate();

		em.createNamedQuery("ReferentePrestador.borrarReferentePrestador")
			.setParameter("referenteId", referenteId)
			.executeUpdate();

		em.createNamedQuery("ReferenteModalidad.borrarReferenteModalidad")
			.setParameter("referenteId", referenteId)
			.executeUpdate();
	}

	public void actualizarDatosReferentel(Long referenteId, ReferenteDto referente){
			em.createNamedQuery("Referente.actualizadaDatosReferente")
			.setParameter("regimen", RegimenNegociacionEnum.valueOf(referente.getRegimen().toString()))
			.setParameter("fechaInicio", referente.getFechaInicio())
			.setParameter("fechaFin", referente.getFechaFin())
			.setParameter("filtroReferente", FiltroReferentePgpEnum.valueOf(referente.getFiltroReferente().toString()))
			.setParameter("poblacionTotal",referente.getPoblacionTotal())
			.setParameter("esProcedimiento", referente.getEsProcedimiento())
			.setParameter("esMedicamento", referente.getEsMedicamento())
			.setParameter("tipoFecha", FiltroReferentePgpEnum.valueOf(referente.getFiltroReferenteFecha().toString()))
			.setParameter("referenteId", referenteId)
			.executeUpdate();
	}

	public void insertarReferenteSegunFiltro(Long referenteId,ReferenteDto referente, ReferenteUbicacionDto referenteUbicacion,
			List<ReferentePrestadorDto> referentePrestador ){
		if(referente.getFiltroReferente().equals(FiltroReferentePgpEnum.POR_UBICACION)){
			this.insertarDatosReferenteUbicacion(referenteId, referenteUbicacion);
		}
		else{
			this.insertarDatosReferentePrestador(referenteId, referentePrestador);
		}
		this.insertarReferenteModalidad(referenteId,referente);

	}

	private void insertarReferenteModalidad(Long referenteId, ReferenteDto referente){

		for(String enu : referente.getModalidad()){
			ReferenteModalidad referenteModalidad = new ReferenteModalidad();
			referenteModalidad.setReferente(new Referente());
			referenteModalidad.getReferente().setId(referenteId);
			referenteModalidad.setModalidad(enu.toUpperCase());
			em.persist(referenteModalidad);
		}
	}


	private void insertarDatosReferenteUbicacion(Long referenteId, ReferenteUbicacionDto referenteUbicacion){

			ReferenteUbicacion refUbicacion  = new ReferenteUbicacion();
			refUbicacion.setReferente(new Referente());
			refUbicacion.getReferente().setId(referenteId);
			refUbicacion.setRegional(new Regional());
			refUbicacion.getRegional().setId(referenteUbicacion.getRegional().getId());
			if(Objects.nonNull(referenteUbicacion.getDepartamento())){
				refUbicacion.setDepartamento(new Departamento());
				refUbicacion.getDepartamento().setId(referenteUbicacion.getDepartamento().getId());
			}
			if(Objects.nonNull(referenteUbicacion.getZonaMunicipio())){
				refUbicacion.setZonaMunicipio(new ZonaMunicipio());
				refUbicacion.getZonaMunicipio().setId(referenteUbicacion.getZonaMunicipio().getId());
			}
			if(Objects.nonNull(referenteUbicacion.getMunicipio())){
				refUbicacion.setMunicipio(new Municipio());
				refUbicacion.getMunicipio().setId(referenteUbicacion.getMunicipio().getId());
			}
			em.persist(refUbicacion);
	}

	private void insertarDatosReferentePrestador(Long referenteId, List<ReferentePrestadorDto> referentePrestador){
		ReferentePrestador refPrestador = new ReferentePrestador();
		for (ReferentePrestadorDto dto : referentePrestador) {
			refPrestador.setReferente(new Referente());
			refPrestador.getReferente().setId(referenteId);
			refPrestador.setSedePrestador(new SedePrestador());
			refPrestador.getSedePrestador().setId(dto.getSedePrestador().getId());
			em.persist(refPrestador);
		}
	}

	public void eliminarCapitulosReferente(List<Long> referenteCapituloIds){
		em.createNamedQuery("ReferenteProcedimiento.eliminarProcedimientosReferenteCapituloId")
			.setParameter("referenteCapituloId", referenteCapituloIds)
			.executeUpdate();

		em.createNamedQuery("ReferenteCapitulo.eliminarCapitulosReferente")
			.setParameter("Ids", referenteCapituloIds)
			.executeUpdate();
	}

	public void eliminarProcedimientosReferente(List<Long> referenteProcedimientoIds){
		em.createNamedQuery("ReferenteProcedimiento.eliminarProcedimientosReferente")
			.setParameter("referenteProcedimientoIds", referenteProcedimientoIds)
			.executeUpdate();
	}

	public void eliminarGrupoMedicamentoReferente(List<Long> referenteGruposId){
		em.createNamedQuery("ReferenteMedicamento.eliminarMedicamentosReferenteGrupoId")
			.setParameter("referenteGruposId", referenteGruposId)
			.executeUpdate();

		em.createNamedQuery("ReferenteCategoriaMedicamento.eliminarReferenteCategoriaMedicamento")
			.setParameter("Ids", referenteGruposId)
			.executeUpdate();
	}

	public void eliminarMedicamentosReferente(List<Long> referenteMedicamentoIds){
		em.createNamedQuery("ReferenteMedicamento.eliminarReferenteMedicamento")
			.setParameter("referenteMedicamentoIds", referenteMedicamentoIds)
			.executeUpdate();
	}

	public void finalizarReferente(Long referenteId){
		em.createNamedQuery("Referente.actualizarEstadoReferente")
			.setParameter("estadoReferente", EstadoReferentePgpEnum.FINALIZADO)
			.setParameter("referenteId", referenteId)
			.executeUpdate();
	}

	@Override
	public List<ProcedimientoDto> getProcedureByCodes(List<String> listCodigos) {
		TypedQuery<ProcedimientoDto> query = em.createNamedQuery("Procedimiento.getByAll", ProcedimientoDto.class);
		//query.setParameter("codigosEmssanar", listCodigos);
		return query.getResultList();
	}

	@Override
	public List<CapituloProcedimientoDto> getCapitulosByCodes(List<String> listCapitulos) {
		TypedQuery<CapituloProcedimientoDto> query = em.createNamedQuery("CapituloProcedimiento.getByCodes",
				CapituloProcedimientoDto.class);
		query.setParameter("codigosCapitulos", listCapitulos);
		return query.getResultList();
	}

	@Override
	public List<CategoriaProcedimientoDto> getCategoriasByCodes(List<String> listCategorias) {
		try {
		TypedQuery<CategoriaProcedimientoDto> query = em.createNamedQuery("CategoriaProcedimiento.getByCodes",
				CategoriaProcedimientoDto.class);
		query.setParameter("categoriasProcedimiento", listCategorias);
		return query.getResultList();
		}catch(Exception e) {
			System.out.println(e.getMessage());
		}
		return null;
	}

	@Override
	public CategoriaProcedimientoDto relationCapituloCategoria(String categoriaCode, String capituloCode) {
		TypedQuery<CategoriaProcedimientoDto> query = em
				.createNamedQuery("CategoriaProcedimiento.relationCapituloCategoria", CategoriaProcedimientoDto.class);
		query.setParameter("categoriaCode", categoriaCode);
		query.setParameter("capituloCode", capituloCode);

		return query.getSingleResult();

	}


	@Transactional
	public void insertProceduresReferenteImport(List<ReferenteProcedimientoDto> listPxReferente, ReferenteDto referenteDto){

		Referente referente = new Referente();
		referente.setId(referenteDto.getId());

		//Verify existence chapter and category in referenteProcedimiento/referente_capitulo
		  try {
		listPxReferente.stream().forEach(ref -> {
			ReferenteCapitulo refChapter = verifyExistenceChapter(ref, referenteDto);
			ReferenteProcedimiento refPx = verifyExistenceProcedureReferente(ref, referenteDto);


			if (Objects.isNull(refPx)) {
				Procedimiento pro = new Procedimiento();
				pro.setId(ref.getProcedimiento().getId());
				ReferenteProcedimiento referenteProcedimiento = new ReferenteProcedimiento();
				referenteProcedimiento.setProcedimiento(pro);
				referenteProcedimiento.setNumeroAtenciones(ref.getNumeroAtenciones());
				referenteProcedimiento.setNumeroUsuarios(ref.getNumeroUsuarios());
				referenteProcedimiento.setFrecuencia(ref.getFrecuencia());
				referenteProcedimiento.setCostoMedioUsuario(ref.getCostoMedioUsuario());
				referenteProcedimiento.setEstado(1);
				referenteProcedimiento.setPgp(ref.getPgp());


				if (Objects.isNull(refChapter)) {
                   try {
					ReferenteCapitulo referenteCapitulo = new ReferenteCapitulo();
					CapituloProcedimiento capitulo = new CapituloProcedimiento();
					capitulo.setId(ref.getReferenteCapitulo().getCapituloProcedimiento().getId());
					CategoriaProcedimiento categoria = new CategoriaProcedimiento();
					categoria.setId(ref.getReferenteCapitulo().getCategoriaProcedimiento().getId());
					referenteCapitulo.setCapituloProcedimiento(capitulo);
					referenteCapitulo.setCategoriaProcedimiento(categoria);
					referenteCapitulo.setReferente(referente);
					em.persist(referenteCapitulo);
					referenteCapitulo.setId(referenteCapitulo.getId());
					referenteProcedimiento.setReferenteCapitulo(referenteCapitulo);
					em.persist(referenteProcedimiento);
                   }catch(Exception pe) {
                	   throw new PersistenceException("Error persistiendo Referente Procedimiento", pe);
                   }
				} else {
					 try {
					referenteProcedimiento.setReferenteCapitulo(refChapter);
					em.persist(referenteProcedimiento);
					 }catch(PersistenceException e) {
						 throw new PersistenceException("Error persistiendo Referente Capitulo", e);
					 }
					}


			} else {
				try {
				ReferenteProcedimiento referenteProcedimiento = refPx;
				referenteProcedimiento.setNumeroAtenciones(ref.getNumeroAtenciones());
				referenteProcedimiento.setNumeroUsuarios(ref.getNumeroUsuarios());
				referenteProcedimiento.setFrecuencia(ref.getFrecuencia());
				referenteProcedimiento.setCostoMedioUsuario(ref.getCostoMedioUsuario());
				em.merge(referenteProcedimiento);
				}catch(Exception e) {
					 throw new PersistenceException("Error actualizando Referente Procedimiento", e);
				}
			}
		});
		}catch(Exception e) {
			throw new PersistenceException("Exepcion General", e);
		}

	}




	private ReferenteCapitulo verifyExistenceChapter(ReferenteProcedimientoDto referenteProcedimiento, ReferenteDto referente) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT rc from ReferenteCapitulo rc  ");
		query.append("where rc.categoriaProcedimiento.id = :categoryId ");
		query.append("AND rc.referente.id = :referenteId ");

		        List<ReferenteCapitulo>  l =  em.createQuery(query.toString())
		        .setParameter("categoryId" , referenteProcedimiento.getReferenteCapitulo().getCategoriaProcedimiento().getId())
		        .setParameter("referenteId", referente.getId() )
				.getResultList();

		        return l != null && !l.isEmpty() ? l.get(0) : null;
	}


	public ReferenteProcedimiento verifyExistenceProcedureReferente(ReferenteProcedimientoDto referProcedure, ReferenteDto referenteDto) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT rp from ReferenteProcedimiento rp ");
		query.append("JOIN rp.referenteCapitulo rc  ");
		query.append("where rc.referente.id = :referenteId ");
		query.append("AND  rp.procedimiento.id = :procedureId ");
		try {
		  List<ReferenteProcedimiento> l = em.createQuery(query.toString())
			    .setParameter("referenteId", referenteDto.getId() )
				.setParameter("procedureId", referProcedure.getProcedimiento().getId())
				.getResultList();
		   return l != null && !l.isEmpty() ? l.get(0) : null;
		}catch(Exception e) {
		  throw new PersistenceException("Error consultando Existencia de Procedimiento" + referProcedure.getProcedimiento().getCodigoCliente(), e);
		}

		}



	@Transactional
	public void insertMedicinesReferenteImport(List<ReferenteMedicamentoDto> listMedReferente,
			ReferenteDto referenteDto) {

		Referente referente = new Referente();
		referente.setId(referenteDto.getId());

		for (ReferenteMedicamentoDto ref : listMedReferente) {

			//Verify if exist medications
			ReferenteMedicamento refMedicine = verifyExistenceMedicineReferente(ref, referenteDto);
	        //Verify if exist category
			ReferenteCategoriaMedicamento refCat = verifyExistenceCategory(ref, referenteDto);

			if (Objects.isNull(refMedicine)) {
			//If referent medications

				Medicamento med = new Medicamento();
				med.setId(ref.getMedicamento().getId());
				ReferenteMedicamento referenteMedicamento = new ReferenteMedicamento();
				referenteMedicamento.setMedicamento(med);
				referenteMedicamento.setNumeroAtenciones(ref.getNumeroAtenciones());
				referenteMedicamento.setNumeroUsuarios(ref.getNumeroUsuarios());
				referenteMedicamento.setFrecuencia(ref.getFrecuencia());
				referenteMedicamento.setCostoMedioUsuario(ref.getCostoMedioUsuaro());
				referenteMedicamento.setReferente(referente);
				referenteMedicamento.setPgp(ref.getPgp());
				referenteMedicamento.setEstado(1);
            //If category don't exist,  persist referenteMedicamento and persist category
				if (refCat == null) {
					try {
						ReferenteCategoriaMedicamento referenteCategoriaMed = new ReferenteCategoriaMedicamento();
						CategoriaMedicamento categoria = new CategoriaMedicamento();
						categoria.setId(ref.getReferenteCategoriaMedicamento().getCategoriaMedicamento().getId());
						referenteCategoriaMed.setCategoriaMedicamento(categoria);
						referenteCategoriaMed.setReferente(referente);
						em.persist(referenteCategoriaMed);
						referenteCategoriaMed.setId(referenteCategoriaMed.getId());
						referenteMedicamento.setReferenteCategoriaMedicamento(referenteCategoriaMed);
						em.persist(referenteMedicamento);
					}catch(PersistenceException e){
						 throw new PersistenceException("Error actualizando categoria");
					}
				}
		 //If category exist since it could be associated with another medicine
				else {
					referenteMedicamento.setReferenteCategoriaMedicamento(refCat);
					em.persist(referenteMedicamento);
				}

			} else {
				//If medication exist then merge referentemedicamento, dont review category since if the procedure exist should be exist category

				try {
					ReferenteMedicamento referenteMedicamento = refMedicine;
					referenteMedicamento.setNumeroAtenciones(ref.getNumeroAtenciones());
					referenteMedicamento.setNumeroUsuarios(ref.getNumeroUsuarios());
					referenteMedicamento.setFrecuencia(ref.getFrecuencia());
					referenteMedicamento.setCostoMedioUsuario(ref.getCostoMedioUsuaro());
					em.merge(referenteMedicamento);
				}catch(PersistenceException e){
					 throw new PersistenceException("Error actualizando ReferenteMedicamento");
				}

			}

		}
	}


	private ReferenteCategoriaMedicamento verifyExistenceCategory(ReferenteMedicamentoDto referMedicine, ReferenteDto referenteDto) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT rcm from ReferenteCategoriaMedicamento rcm  ");
		query.append("where rcm.categoriaMedicamento.id = :categoryId ");
		query.append("AND rcm.referente.id = :referenteId ");

		        List<ReferenteCategoriaMedicamento>  l =  em.createQuery(query.toString())
		        .setParameter("categoryId" , referMedicine.getReferenteCategoriaMedicamento().getCategoriaMedicamento().getId())
		        .setParameter("referenteId", referenteDto.getId() )
				.getResultList();

		        return l != null && !l.isEmpty() ? l.get(0) : null;

		}



	public ReferenteMedicamento verifyExistenceMedicineReferente(ReferenteMedicamentoDto referMedicine, ReferenteDto referenteDto) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT rm from ReferenteMedicamento rm ");
		query.append("join rm.referenteCategoriaMedicamento rcm  ");
		query.append("where rm.medicamento.id = :medicineId ");
		query.append("AND rcm.referente.id = :referenteId ");


		 List<ReferenteMedicamento> listRefMedicamento =  em.createQuery(query.toString())
				.setParameter("medicineId", referMedicine.getMedicamento().getId())
		        .setParameter("referenteId", referenteDto.getId() )
				.getResultList();

		 return listRefMedicamento != null  && !listRefMedicamento.isEmpty() ? listRefMedicamento.get(0) : null;

	 }

	@Override
	public List<MedicamentosDto> getMedicinesByCodes(List<String> listCodigos) {
		TypedQuery<MedicamentosDto> query = em.createNamedQuery("Medicamento.getByCodes", MedicamentosDto.class);
		query.setParameter("codigos", listCodigos);
		return query.getResultList();
	}


	@Override
	public List<CategoriaMedicamentoDto> getCategoriasMedicamentosByCodes(List<String> listCategorias) {
		TypedQuery<CategoriaMedicamentoDto> query = em.createNamedQuery("CategoriaMedicamento.getByCodes", CategoriaMedicamentoDto.class);
		query.setParameter("codigos", listCategorias);
		return query.getResultList();
	}

	@Override
	public MedicamentosDto relationCategoriaMedicamento(Long categoriaId, Long medicamentoId) {
		TypedQuery<MedicamentosDto> query = em.createNamedQuery("Medicamento.relationCatMed", MedicamentosDto.class);
	    query.setParameter("medicamentoId", medicamentoId);
	    query.setParameter("categoriaId", categoriaId);
	    return query.getSingleResult();

	}

	@Override
	public List<MedicamentosDto> consultCategories(List<Long> listMedicinesIdWithCategoryNull) {
		TypedQuery<MedicamentosDto> query = em.createNamedQuery("Medicamento.medicamentoById" , MedicamentosDto.class);
		query.setParameter("medicamentosId", listMedicinesIdWithCategoryNull);
		return query.getResultList();
	}

	@Override
	public List<ReferenteProcedimientoDto> consultChapterCategories(List<Long> listProceduresIdWithChapterOrCategoryNull) {
		StringBuilder query = new StringBuilder();
		query.append("SELECT pro.id, cap.id as capId,  cat.id as catId  from maestros.procedimiento pro ");
		query.append("join maestros.categoria_procedimiento cat on cat.id = pro.categoria_procedimiento_id ");
		query.append("join maestros.capitulo_procedimiento cap on cat.capitulo_procedimiento_id = cap.id ");
		query.append("where pro.id in :procedimientosId");

		List<Object[]> listObject = em.createNativeQuery(query.toString())
		.setParameter("procedimientosId", listProceduresIdWithChapterOrCategoryNull).getResultList();


		List<ReferenteProcedimientoDto> listReferenteProcedimiento = new ArrayList<ReferenteProcedimientoDto>();

		for(Object[] obj :listObject){

			ReferenteProcedimientoDto referenteProcedimiento = new ReferenteProcedimientoDto();
			ProcedimientoDto pro = new ProcedimientoDto();
			pro.setId(Long.parseLong(obj[0].toString()));
			referenteProcedimiento.setProcedimiento(pro);
			ReferenteCapituloDto  referenteCapitulo = new ReferenteCapituloDto();
			CapituloProcedimientoDto capituloProcedimiento = new CapituloProcedimientoDto();
			CategoriaProcedimientoDto categoriaProcedimiento = new CategoriaProcedimientoDto();

			capituloProcedimiento.setId((Long.parseLong(obj[1].toString())));
			categoriaProcedimiento.setId((Long.parseLong(obj[2].toString())));
			referenteCapitulo.setCapituloProcedimiento(capituloProcedimiento);
			referenteCapitulo.setCategoriaProcedimiento(categoriaProcedimiento);
			referenteProcedimiento.setReferenteCapitulo(referenteCapitulo);

			listReferenteProcedimiento.add(referenteProcedimiento);

		}

		return listReferenteProcedimiento;


	}




	public List<ReferenteProcedimientoDto> createReferente(List<Object[]> listObject){

		List<ReferenteProcedimientoDto> listReferenteProcedimiento = new ArrayList<ReferenteProcedimientoDto>();

		for(Object[] obj :listObject){

			ReferenteProcedimientoDto referenteProcedimiento = new ReferenteProcedimientoDto();
			ProcedimientoDto pro = new ProcedimientoDto();
			pro.setId(Long.parseLong(obj[0].toString()));
			referenteProcedimiento.setProcedimiento(pro);
			ReferenteCapituloDto  referenteCapitulo = new ReferenteCapituloDto();
			CapituloProcedimientoDto capituloProcedimiento = new CapituloProcedimientoDto();
			CategoriaProcedimientoDto categoriaProcedimiento = new CategoriaProcedimientoDto();

			capituloProcedimiento.setId((Long.parseLong(obj[1].toString())));
			categoriaProcedimiento.setId((Long.parseLong(obj[2].toString())));
			referenteCapitulo.setCapituloProcedimiento(capituloProcedimiento);
			referenteCapitulo.setCategoriaProcedimiento(categoriaProcedimiento);
			referenteProcedimiento.setReferenteCapitulo(referenteCapitulo);

			listReferenteProcedimiento.add(referenteProcedimiento);

		}


		return listReferenteProcedimiento;

	}

}
