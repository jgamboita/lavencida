package co.conexia.negociacion.wap.controller.referentePgp;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.primefaces.context.RequestContext;

import com.conexia.contratacion.commons.constants.enums.ArchivosNegociacionEnum;
import com.conexia.contratacion.commons.dto.CategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.maestros.CapituloProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.CategoriaProcedimientoDto;
import com.conexia.contratacion.commons.dto.maestros.MedicamentosDto;
import com.conexia.contratacion.commons.dto.negociacion.ErroresImportTecnologiasDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteCapituloDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteCategoriaMedicamentoDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteMedicamentoDto;
import com.conexia.contratacion.commons.dto.referente.ReferenteProcedimientoDto;
import com.conexia.webutils.FacesMessagesUtils;

import co.conexia.negociacion.wap.facade.referentePgp.GestionReferentePgpFacade;

public class ValidatorImportReferente implements Serializable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	@Inject
	private GestionReferentePgpFacade gestionReferenteFacade;
	
	@Inject
	private FacesMessagesUtils facesMessagesUtils;
	
	private List<ErroresImportTecnologiasDto> listErrors;
	
	private Map<Integer, ReferenteProcedimientoDto> mapReferentePx = new HashMap<Integer, ReferenteProcedimientoDto>();
	
	private Map<Integer, ReferenteMedicamentoDto> mapReferenteMed = new HashMap<Integer, ReferenteMedicamentoDto>();
	
	private RequestContext context;
	
	private List<ReferenteProcedimientoDto> listPxReferente;
	
	private List<ReferenteMedicamentoDto> listMedReferente;
	
	private ReferenteDto referente;
	
	
	public List<ReferenteProcedimientoDto> validateProcedures(Sheet hoja, ArchivosNegociacionEnum typeImport,
			ReferenteDto refer) {

		listErrors = new ArrayList<>();
		
		referente = refer;

		DataFormatter formatter = new DataFormatter();
		Iterator<Row> iterator = hoja.iterator();
		while (iterator.hasNext()) {
			Row fila = iterator.next();
			if (fila.getRowNum() == 0) {
				continue;
			}

			Integer numeroFila = fila.getRowNum() + 1;

			ReferenteProcedimientoDto refPx = new ReferenteProcedimientoDto();
			ReferenteCapituloDto refCapitulo = new ReferenteCapituloDto();
			CapituloProcedimientoDto capituloPx = new CapituloProcedimientoDto();
			CategoriaProcedimientoDto categoriaPx = new CategoriaProcedimientoDto();
			refCapitulo.setCapituloProcedimiento(capituloPx);
			refCapitulo.setCategoriaProcedimiento(categoriaPx);

			// Capitulo
			String capitulo = formatter.formatCellValue(fila.getCell(0));
			if (!isNullorEmpty(capitulo)) {
				refCapitulo.getCapituloProcedimiento().setCodigo(capitulo.trim());
			}
			// Categoria
			String categoria = formatter.formatCellValue(fila.getCell(1));
			if (!isNullorEmpty(categoria)) {
				refCapitulo.getCategoriaProcedimiento().setCodigo(categoria.trim());
			}
			refPx.setReferenteCapitulo(refCapitulo);

			ProcedimientoDto proced = new ProcedimientoDto();

			// Codigo Tecnologia
			String tecnologia = formatter.formatCellValue(fila.getCell(2));

			if (isNullorEmpty(tecnologia)) {
				ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(numeroFila, "Código tecnología",
						tecnologia, tecnologia, "Campo requerido");
				listErrors.add(error);
			} else {
				proced.setCodigoCliente(tecnologia.trim().toUpperCase());
				refPx.setProcedimiento(proced);
			}

			validationCommonFields(fila, numeroFila, tecnologia, refPx, null, typeImport);

			mapReferentePx.put(numeroFila, refPx);
		}

		if (mapReferentePx.isEmpty()) {
			facesMessagesUtils.addInfo("El archivo se encuentra vacío");
		} else {

			if (!listErrors.isEmpty()) {
				context.getCurrentInstance().execute("PF('errorsImportReferente').show();");
			} else {
				listPxReferente = validateCoherenceProcedure(mapReferentePx);
			}
		}

		return listPxReferente;

	}
	
	
	
	public List<ReferenteProcedimientoDto> validateCoherenceProcedure(Map<Integer, ReferenteProcedimientoDto> mapReferentePx) {
		
		
		//Validar Numero atención mayor que Número usuarios
		
		mapReferentePx.forEach((k,v) ->{
			if(v.getNumeroAtenciones()!= null && v.getNumeroUsuarios()!=  null && v.getNumeroAtenciones() < v.getNumeroUsuarios()) {
				ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(k ,"Número atenciones",v.getProcedimiento().getCodigoCliente(), 
						v.getNumeroAtenciones().toString(), "El número de atenciones debe ser mayor o igual al número de usuarios");
				listErrors.add(error); 
			}
		});
		
		
		//Validar Existencia de codigoTecnologia
		//Codigos
		List<String> listCodigos = mapReferentePx.values().stream().distinct()
				.map(x -> x.getProcedimiento().getCodigoCliente()).collect(Collectors.toList());
		
		List<ProcedimientoDto> proceduresFound =  gestionReferenteFacade.getProcedureByCodes(listCodigos);
		
		
		mapReferentePx.forEach((k,v) ->{
			if(v.getProcedimiento().getCodigoCliente()!= null) {
				
				ProcedimientoDto pxFound =	proceduresFound.stream()
						                    .filter(px -> px.getCodigoCliente().equals(v.getProcedimiento().getCodigoCliente()))
						                    .findAny().orElse(null);
			
					 if(pxFound == null) {
							ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(k ,"Código tecnología",v.getProcedimiento().getCodigoCliente(), 
									v.getProcedimiento().getCodigoCliente(), "El procedimiento no existe en las maestras");
							listErrors.add(error); 
					 }else if(!pxFound.getEstadoProcedimientoId().equals(1)){
							ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(k ,"Código tecnología",v.getProcedimiento().getCodigoCliente(), 
									v.getProcedimiento().getCodigoCliente(), "El procedimiento no esta activo");
							listErrors.add(error); 
					 }else {
						 v.getProcedimiento().setId(pxFound.getId());
					 }				
			}
		});
		
		
		
		//Capitulos
		List<String> listCapitulos = mapReferentePx.values().stream()
				.filter(x ->x.getReferenteCapitulo().getCapituloProcedimiento().getCodigo() != null
				&& !x.getReferenteCapitulo().getCapituloProcedimiento().getCodigo().isEmpty() )
				.map(x -> x.getReferenteCapitulo().getCapituloProcedimiento().getCodigo()).
				collect(Collectors.toList());
		
		List<CapituloProcedimientoDto> capitulosFound = gestionReferenteFacade.getCapitulosByCodes(listCapitulos);
	    

		mapReferentePx.forEach((k,v) ->{
			if(v.getReferenteCapitulo().getCapituloProcedimiento().getCodigo()!= null) {
				CapituloProcedimientoDto capFound =
						capitulosFound.stream().filter(cap -> cap.getCodigo()
								.equals(v.getReferenteCapitulo().getCapituloProcedimiento().getCodigo())).findAny().orElse(null);
				if(capFound == null) {
					ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(k ,"Capitulo",v.getProcedimiento().getCodigoCliente(), 
							v.getReferenteCapitulo().getCapituloProcedimiento().getCodigo(), "El capitulo no existe en las maestras");
					listErrors.add(error);	
				}else{v.getReferenteCapitulo().getCapituloProcedimiento().setId(capFound.getId());}
			}
	
		});
		
       //Categorias
		List<String> listCategorias = mapReferentePx.values().stream().distinct()
				.filter(x ->x.getReferenteCapitulo().getCategoriaProcedimiento().getCodigo() != null
				&& !x.getReferenteCapitulo().getCategoriaProcedimiento().getCodigo().isEmpty())
				.map(x -> x.getReferenteCapitulo().getCategoriaProcedimiento().getCodigo()).collect(Collectors.toList());
		
//		List<String> listCategorias = new  ArrayList<String>();
//		
//         for(String  code:listCat) {
//        	 if(!listCategorias.contains(code)) {
//        		 listCategorias.add(code);
//        	 }
//         }


		List<CategoriaProcedimientoDto> categoriasFound = gestionReferenteFacade.getCategoriasByCodes(listCategorias);
		
		mapReferentePx.forEach((k,v) ->{
			if(v.getReferenteCapitulo().getCategoriaProcedimiento().getCodigo()!= null) {
				CategoriaProcedimientoDto catFound=	categoriasFound.stream()
						.filter(cat -> cat.getCodigo().equals(v.getReferenteCapitulo().getCategoriaProcedimiento().getCodigo())).findAny().orElse(null);
				if(catFound == null) {
					ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(k ,"Categoria",v.getProcedimiento().getCodigoCliente(),
							v.getReferenteCapitulo().getCategoriaProcedimiento().getCodigo(), "La categoria no existe en las maestras");
					listErrors.add(error);	
				}else {v.getReferenteCapitulo().getCategoriaProcedimiento().setId(catFound.getId());}				
			}
		});
		

		
		
		if (listErrors.isEmpty()) {
			mapReferentePx.forEach((k, v) -> {
				if (v.getReferenteCapitulo().getCategoriaProcedimiento().getCodigo() != null
						&& v.getReferenteCapitulo().getCapituloProcedimiento().getCodigo() != null
						&& v.getReferenteCapitulo().getCategoriaProcedimiento().getCodigo().isEmpty()
						&& v.getReferenteCapitulo().getCapituloProcedimiento().getCodigo().isEmpty()
						) {
					CategoriaProcedimientoDto categoria = gestionReferenteFacade.relationCapituloCategoria(
							v.getReferenteCapitulo().getCategoriaProcedimiento().getCodigo(),
							v.getReferenteCapitulo().getCapituloProcedimiento().getCodigo());

					if (categoria == null) {
						ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(k, "Capitulo - Categoria",
								v.getProcedimiento().getCodigoCliente(),
								v.getReferenteCapitulo().getCapituloProcedimiento().getCodigo() + "-"
										+ v.getReferenteCapitulo().getCategoriaProcedimiento().getCodigo(),
								"El capitulo y la categoria no estan relacionados");
						listErrors.add(error);
					}
				}
			});
		}
		
		if(!listErrors.isEmpty()) {
			Collections.sort(listErrors, byFila);
			context.getCurrentInstance().execute("PF('errorsImportReferente').show();");
		} else {
			 AssignamentChapterAndCategoryId(mapReferentePx);
			 calculateGlobalPayment(mapReferentePx, null);
			 listPxReferente = mapReferentePx.entrySet().stream()
					.map(Map.Entry:: getValue).collect(Collectors.toList());
		} 
		
		return listPxReferente;
		
	}
	


	private void calculateGlobalPayment(Map<Integer, ReferenteProcedimientoDto> mapReferentePx,
			Map<Integer, ReferenteMedicamentoDto> mapReferenteMx) {

		if (mapReferentePx != null) {
			mapReferentePx.values().stream().forEach(v -> {
				v.setPgp(v.getCostoMedioUsuario().multiply(v.getFrecuencia())
						.multiply(BigDecimal.valueOf(referente.getPoblacionTotal().longValue())));
			});
		} else if (mapReferenteMx != null) {
			mapReferenteMx.values().stream().forEach(v -> {
				v.setPgp(v.getCostoMedioUsuaro().multiply(v.getFrecuencia())
						.multiply(BigDecimal.valueOf(referente.getPoblacionTotal().longValue())));
			});

		}
	}


	private void AssignamentChapterAndCategoryId(Map<Integer, ReferenteProcedimientoDto> mapReferentePx) {
		// Assignment categoryId to insert referente
		List<Long> listProceduresIdWithChapterOrCategoryNull = mapReferentePx.values().stream()
				.filter(x -> 
						   x.getReferenteCapitulo().getCapituloProcedimiento().getCodigo() == null
						|| x.getReferenteCapitulo().getCapituloProcedimiento().getCodigo().equals("")
						|| x.getReferenteCapitulo().getCategoriaProcedimiento().getCodigo() == null
						|| x.getReferenteCapitulo().getCategoriaProcedimiento().getCodigo().equals("")
						
						)
				.map(p -> p.getProcedimiento().getId()).collect(Collectors.toList());
		

		if (!listProceduresIdWithChapterOrCategoryNull.isEmpty()) {

			List<ReferenteProcedimientoDto> referenteProcedimiento = gestionReferenteFacade
					.consultChapterCategories(listProceduresIdWithChapterOrCategoryNull);

			mapReferentePx.values().stream().forEach(r -> {
				referenteProcedimiento.stream().filter(rp -> rp.getProcedimiento().getId()
						.equals(r.getProcedimiento().getId())).forEach(p ->{
							r.getReferenteCapitulo().getCapituloProcedimiento().setId(p.getReferenteCapitulo().getCapituloProcedimiento().getId());
							r.getReferenteCapitulo().getCategoriaProcedimiento().setId(p.getReferenteCapitulo().getCategoriaProcedimiento().getId());
						});
			});

		}

	}




	public List<ReferenteMedicamentoDto> validateMedicines(Sheet hoja, ArchivosNegociacionEnum typeImport, ReferenteDto refer) {

		listErrors = new ArrayList<>();
		referente = refer;

		DataFormatter formatter = new DataFormatter();
		Iterator<Row> iterator = hoja.iterator();
		while (iterator.hasNext()) {
			Row fila = iterator.next();
			if (fila.getRowNum() == 0) {
				continue;
			}

			Integer numeroFila = fila.getRowNum() + 1;

			ReferenteMedicamentoDto refMed = new ReferenteMedicamentoDto();
			ReferenteCategoriaMedicamentoDto refCategoriaMedicamento = new ReferenteCategoriaMedicamentoDto();
			refMed.setReferenteCategoriaMedicamento(refCategoriaMedicamento);
			CategoriaMedicamentoDto categoriaMedicamento = new CategoriaMedicamentoDto();
			refCategoriaMedicamento.setCategoriaMedicamento(categoriaMedicamento);
			// GrupoTerapeutico
			String grupoTerapeutico = formatter.formatCellValue(fila.getCell(0));
			if (!isNullorEmpty(grupoTerapeutico)) {
				refMed.getReferenteCategoriaMedicamento().getCategoriaMedicamento().setCodigo(grupoTerapeutico.trim());
			}

			MedicamentosDto medicamento = new MedicamentosDto();

			// Codigo Tecnologia
			String tecnologia = formatter.formatCellValue(fila.getCell(2));

			if (isNullorEmpty(tecnologia)) {
				ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(numeroFila, "Código tecnología",
						tecnologia, tecnologia, "Campo requerido");
				listErrors.add(error);
			} else {
				medicamento.setCums(tecnologia.trim().toUpperCase());
				refMed.setMedicamento(medicamento);
			}

			validationCommonFields(fila, numeroFila, tecnologia, null, refMed, typeImport);

			mapReferenteMed.put(numeroFila, refMed);
		}

		if (mapReferenteMed.isEmpty()) {
			facesMessagesUtils.addInfo("El archivo se encuentra vacío");
		} else {

			if (!listErrors.isEmpty()) {
				context.getCurrentInstance().execute("PF('errorsImportReferente').show();");
			} else {
				listMedReferente = validateCoherenceMedicines(mapReferenteMed);
			}
		}

		return listMedReferente;

	}

	
	public List<ReferenteMedicamentoDto> validateCoherenceMedicines(Map<Integer, ReferenteMedicamentoDto> mapReferenteMed) {
		
		//Validacioin Numero Atencion Mayor Numero Usuarios
		
		mapReferenteMed.forEach((k,v) ->{
			if(v.getNumeroAtenciones() < v.getNumeroUsuarios()) {
				ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(k ,"Número atenciones",v.getMedicamento().getCums(), 
						v.getNumeroAtenciones().toString(), "El número de atenciones debe ser mayor o igual al número de usuarios");
				listErrors.add(error); 
			}	
		});
				
		//Validar Existencia de codigoTecnologia
		//Codigos
		List<String> listCodigos = mapReferenteMed.values().stream()
				.map(x -> x.getMedicamento().getCums()).collect(Collectors.toList());
		
		List<MedicamentosDto> medicinesFound =  gestionReferenteFacade.getMedicineByCodes(listCodigos);
		
		
		mapReferenteMed.forEach((k,v) ->{
			if(v.getMedicamento().getCums()!= null) {
				
				MedicamentosDto medFound =	medicinesFound.stream()
						                    .filter(med -> med.getCums().equals(v.getMedicamento().getCums()))
						                    .findAny().orElse(null);
			
					 if(medFound == null) {
							ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(k ,"Código tecnología",v.getMedicamento().getCums(), 
									v.getMedicamento().getCums(), "El medicamento no existe en las maestras");
							listErrors.add(error); 
					 }else if(medFound.getEstadoCums() != 1) {
							ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(k ,"Código tecnología",v.getMedicamento().getCums(), 
									v.getMedicamento().getCums(), "El medicamento no esta activo");
							listErrors.add(error); 
					 } else {
						 v.getMedicamento().setId(medFound.getId());
					 }				
			}
		});
		
		
       //Categorias  Existencia de la categoria si no viene null
		
		List<String> listCategorias = mapReferenteMed.values().stream()
				.filter(x ->x.getReferenteCategoriaMedicamento().getCategoriaMedicamento().getCodigo() != null
				&& !x.getReferenteCategoriaMedicamento().getCategoriaMedicamento().getCodigo().isEmpty())
				.map(x -> x.getReferenteCategoriaMedicamento().getCategoriaMedicamento().getCodigo()).collect(Collectors.toList());
		
		List<CategoriaMedicamentoDto> categoriasFound = gestionReferenteFacade.getCategoriasMedicamentosByCodes(listCategorias);
		
		mapReferenteMed.forEach((k,v) ->{
			if(v.getReferenteCategoriaMedicamento().getCategoriaMedicamento().getCodigo()!= null) {
				CategoriaMedicamentoDto catFound=	categoriasFound.stream()
						.filter(cat -> cat.getCodigo().equals(v.getReferenteCategoriaMedicamento().getCategoriaMedicamento().getCodigo())).findAny().orElse(null);
				if(catFound == null) {
					ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(k ,"Categoria",v.getMedicamento().getCums(),
							v.getReferenteCategoriaMedicamento().getCategoriaMedicamento().getCodigo(), "La categoria no existe en las maestras, puede dejar el campo vacío en la categoria y el sistema guardará los datos con la categoria correspondiente al medicamento");
					listErrors.add(error);	
				}else {v.getReferenteCategoriaMedicamento().getCategoriaMedicamento().setId(catFound.getId());}				
			}
		});
		
   //Relation Categoria Medicamento
		
		  
		if (listErrors.isEmpty()) {  
			mapReferenteMed.forEach((k, v) -> {   
				if (v.getReferenteCategoriaMedicamento().getCategoriaMedicamento().getId() != null
						&& !v.getReferenteCategoriaMedicamento().getCategoriaMedicamento().getCodigo().isEmpty()
						) {
					MedicamentosDto medicamento = gestionReferenteFacade.relationCategoriaMedicamento(
							v.getReferenteCategoriaMedicamento().getCategoriaMedicamento().getId(),
							v.getMedicamento().getId());

					if (medicamento == null) {
						ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(k, "Medicamento - Categoría",
								v.getMedicamento().getCums(),
								v.getMedicamento().getCums() + "-" + v.getReferenteCategoriaMedicamento().getCategoriaMedicamento().getCodigo(),
								"El Medicamento y la categoría no estan relacionados, puede dejar el campo vacio en la categoría y el sistema guardará los datos con la categoría correspondiente al medicamento");
						listErrors.add(error);
					}
				}
			});
		}
		
				
	
		if(!listErrors.isEmpty()) {
			Collections.sort(listErrors, byFila);
			context.getCurrentInstance().execute("PF('errorsImportReferente').show();");
		} else {	
			 AssignamentCategoryId(mapReferenteMed);
			 calculateGlobalPayment(null, mapReferenteMed);
			 listMedReferente = mapReferenteMed.entrySet().stream()
					.map(Map.Entry:: getValue).collect(Collectors.toList());
		} 
		
		return listMedReferente;
		
	}
	
	
	
	
	private void AssignamentCategoryId(Map<Integer, ReferenteMedicamentoDto> mapReferenteMed) {
		
		//Assignment categoryId to insert referente
		List<Long> listMedicinesIdWithCategoryNull = mapReferenteMed.values().stream()
				.filter(x ->x.getReferenteCategoriaMedicamento().getCategoriaMedicamento().getCodigo() == null
				|| x.getReferenteCategoriaMedicamento().getCategoriaMedicamento().getCodigo().isEmpty())
				.map(x -> x.getMedicamento().getId()).collect(Collectors.toList());
		
		if(!listMedicinesIdWithCategoryNull.isEmpty()) {
		List<MedicamentosDto> medicamentos = gestionReferenteFacade.consultCategories(listMedicinesIdWithCategoryNull);
		
		mapReferenteMed.values().stream().forEach(ref -> {
			medicamentos.stream().filter(m -> m.getId().equals(ref.getMedicamento().getId())).forEach(m -> {
				ref.getReferenteCategoriaMedicamento().getCategoriaMedicamento().setId(m.getCategoriaMedicamento().getId());
			});
		});
		
		}

	}




	public void validationCommonFields(Row fila, Integer numeroFila, String tecnologia, ReferenteProcedimientoDto refPx, ReferenteMedicamentoDto refMed, ArchivosNegociacionEnum typeImport) {
		
		DataFormatter formatter =  new DataFormatter();
		
		//Frecuencia
		String frecuencia = formatter.formatCellValue(fila.getCell(4));

		if (isNullorEmpty(frecuencia)) {
			ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(numeroFila, "Frecuencia", tecnologia,
					frecuencia, "Campo requerido");
			listErrors.add(error);
		} else if (!isBigDecimal(frecuencia)) {
			ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(numeroFila, "Frecuencia", tecnologia,
					frecuencia, "Debe ser campo numérico");
			listErrors.add(error);
		} else {
			BigDecimal frecuency = new BigDecimal(frecuencia.replace(",", "."));
			if (numberValid(frecuency, numeroFila, "Frecuencia", tecnologia)) {
				frecuency= frecuency.setScale(6, BigDecimal.ROUND_HALF_UP);
				if (typeImport.name().equals("PROCEDURE_REF_FILE")) {
					refPx.setFrecuencia(frecuency);
				} else {
					refMed.setFrecuencia(frecuency);
				}
			}
		}
		
		
		//cmu
		String cmu =formatter.formatCellValue(fila.getCell(5));
		if (isNullorEmpty(cmu)) {
			ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(numeroFila, "CMU", tecnologia, cmu,
					"Campo requerido");
			listErrors.add(error);
		} else if (!isBigDecimal(cmu)) {
			ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(numeroFila, "CMU", tecnologia, cmu,
					"Debe ser campo numérico");
			listErrors.add(error);
		} else {
			BigDecimal costMu = new BigDecimal(cmu.replace(",", "."));		
			if (numberValid(costMu, numeroFila, "CMU", tecnologia)) {
				costMu= costMu.setScale(3, BigDecimal.ROUND_HALF_UP);
				if (typeImport.name().equals("PROCEDURE_REF_FILE")) {
					refPx.setCostoMedioUsuario(costMu);
				} else {
					refMed.setCostoMedioUsuaro(costMu);
				}
			}
		}
		
		//Número Atenciones
		String numeroAtenciones = formatter.formatCellValue(fila.getCell(6));
		if (isNullorEmpty(numeroAtenciones)) {
			ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(numeroFila, "Número de atenciones",
					tecnologia, numeroAtenciones, "Campo requerido");
			listErrors.add(error);
		} else if (!isNumeric(numeroAtenciones)) {
			ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(numeroFila, "Número de atenciones",
					tecnologia, numeroAtenciones, "Debe ser campo numérico y entero");
			listErrors.add(error);
		} else {
			if (numberValid(new BigDecimal(numeroAtenciones), numeroFila, "Número de atenciones", tecnologia)) {
				if (typeImport.name().equals("PROCEDURE_REF_FILE")) {
					refPx.setNumeroAtenciones(Integer.parseInt(numeroAtenciones));
				} else {
					refMed.setNumeroAtenciones(Integer.parseInt(numeroAtenciones));
				}
			}
		}
		
		
		//Número usuarios
		String numeroUsuarios = formatter.formatCellValue(fila.getCell(7));
		if (isNullorEmpty(numeroUsuarios)) {
			ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(numeroFila, "Número de usuarios",
					tecnologia, numeroUsuarios, "Campo requerido");
			listErrors.add(error);
		} else if (!isNumeric(numeroUsuarios)) {
			ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(numeroFila, "Número de usuarios",
					tecnologia, numeroUsuarios, "Debe ser campo numérico y entero");
			listErrors.add(error);
		} else {
			if (numberValid(new BigDecimal(numeroUsuarios), numeroFila, "Número de usuarios", tecnologia)) {
				if (typeImport.name().equals("PROCEDURE_REF_FILE")) {
					refPx.setNumeroUsuarios(Integer.parseInt(numeroUsuarios));
				} else {
					refMed.setNumeroUsuarios(Integer.parseInt(numeroUsuarios));
				}
			}
		}
		
	}

	
	private Boolean numberValid(BigDecimal number, Integer numeroFila, String columna, String tecnologia) {
		Boolean valid= false;
		
		if(number.compareTo(BigDecimal.ZERO) <= 0) {
			ErroresImportTecnologiasDto error = new ErroresImportTecnologiasDto(numeroFila, columna,
					tecnologia, number.toString(), "El valor debe ser mayor a cero");
			listErrors.add(error);
		}else {
			valid= true;
		}
		return valid;
		
	}


	@SuppressWarnings("PMD.OnlyOneReturn")
	private final Comparator<ErroresImportTecnologiasDto> byFila = new Comparator<ErroresImportTecnologiasDto>() {
		public int compare(ErroresImportTecnologiasDto left, ErroresImportTecnologiasDto right) {
			try {
				if (left.getFila() < (right.getFila())) {
					return -1;
				} else {
					return 1;
				}
			} catch (NullPointerException e) {
				return 0;
			}
		}
	};

	
	//Metodo para validar campo numerico
	
	private boolean isNullorEmpty(String formatCellValue) {
		if(Objects.isNull(formatCellValue) || formatCellValue.equals("")) {
			return true;
		}else
		{
		return false;
		}
	}


	private static boolean isNumeric(String cadena) {
		try {
			Integer.parseInt(cadena);
			return true;
		} catch (NumberFormatException nfe) {
			return false;
		}
	}
	
	private static boolean isDouble(String cadena) {
		try {
			Double.parseDouble(cadena.replace(",", "."));
			return true;
		}catch(NumberFormatException nfe) {
			return false;
		}
		
	}
	
	private static boolean isBigDecimal(String cadena) {
		try {
			new BigDecimal(cadena.replace(",", "."));
			return true;
		}catch(NumberFormatException nfe) {
			return false;
		}
		
	}



	public List<ErroresImportTecnologiasDto> getListErrors() {
		return listErrors;
	}



	public void setListErrors(List<ErroresImportTecnologiasDto> listErrors) {
		this.listErrors = listErrors;
	}
	


}
