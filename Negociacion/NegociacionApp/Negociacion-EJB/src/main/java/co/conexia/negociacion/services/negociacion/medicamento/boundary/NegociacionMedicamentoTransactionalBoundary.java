package co.conexia.negociacion.services.negociacion.medicamento.boundary;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.ejb.Remote;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import com.conexia.contratacion.commons.constants.enums.MacroCategoriaMedicamentoEnum;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
import com.conexia.contratacion.commons.constants.enums.TipoAsignacionTarifaMedReguladoEnum;
import com.conexia.contratacion.commons.dto.negociacion.GrupoTerapeuticoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.MedicamentoNegociacionDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.exceptions.ConexiaBusinessException;
import com.conexia.exceptions.ConexiaSystemException;
import com.conexia.negociacion.definitions.negociacion.medicamento.NegociacionMedicamentoTransactionalServiceRemote;
import co.conexia.negociacion.services.negociacion.medicamento.control.NegociacionMedicamentoControl;
import co.conexia.negociacion.services.negociacion.control.EliminarTecnologiasAuditoriaControl;
import javax.persistence.ParameterMode;
import javax.persistence.StoredProcedureQuery;


@Stateless
@Remote(NegociacionMedicamentoTransactionalServiceRemote.class)
public class NegociacionMedicamentoTransactionalBoundary implements NegociacionMedicamentoTransactionalServiceRemote {

	private static final int DECIMALES_APROXIMACION = 3;
    private final int DIGITOS_APROXIMACION_PROCENTAJE = 3;

    private TipoAsignacionTarifaMedReguladoEnum tarifaImportar = TipoAsignacionTarifaMedReguladoEnum.VALOR_ARCHIVO;

    @PersistenceContext(unitName = "contractualDS")
    private EntityManager em;
    @Inject
    private NegociacionMedicamentoControl negociacionMedicamentoControl;
    @Inject
    private EliminarTecnologiasAuditoriaControl eliminarTecnologiasAuditoriaControl;

    @Override
    public int asignarTarifas(List<Long> medicamentos, Long negociacionId, String propiedad, Integer userId) {
        return em.createNativeQuery("UPDATE contratacion.sede_negociacion_medicamento "
                + "SET valor_negociado = " + propiedad
                + "negociado = true, user_id = :userId "
                + "FROM contratacion.sedes_negociacion sn "
                + "INNER JOIN contratacion.negociacion n "
                + "ON n.id = sn.negociacion_id "
                + "WHERE sn.id = sede_negociacion_id "
                + "AND n.id = :negociacionId "
                + "AND medicamento_id IN (:medicamentos)")
                .setParameter("negociacionId", negociacionId)
                .setParameter("medicamentos", medicamentos)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void asignarValorContratoAnterior(Long negociacionId, List<MedicamentoNegociacionDto> medicamento, Integer userId){
    	List<Long> medicamentoIds = new ArrayList<>();
    	medicamento.stream()
			.filter(medi -> Objects.nonNull(medi.getValorContratoAnterior())
					&& medi.getValorContratoAnterior().compareTo(BigDecimal.ZERO) >= 1)
			.forEach(med ->{
				medicamentoIds.add(med.getMedicamentoDto().getId());
			});
    	if(medicamentoIds.size() > 0 && !medicamentoIds.isEmpty()) {
    		em.createNativeQuery("UPDATE contratacion.sede_negociacion_medicamento SET "
            		+ "valor_negociado = contratoMedicamento.valor_contrato, "
            		+ "negociado = true, user_id = :userId "
            		+ "FROM (SELECT snm.id, snm.valor_contrato from contratacion.sede_negociacion_medicamento snm "
            		+ "JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id "
            		+ "where sn.negociacion_id = :negociacionId and snm.medicamento_id in (:medicamentoIds)) as contratoMedicamento "
            		+ "where contratoMedicamento.id = contratacion.sede_negociacion_medicamento.id ")
            		.setParameter("negociacionId", negociacionId)
            		.setParameter("medicamentoIds", medicamentoIds)
            		.setParameter("userId", userId)
                    .executeUpdate();
    	}

    }
    
    @Override
    public void asignarValorContratoAnteriorByNegociacionReferente(Long negociacionId, 
                                                                   Long negociacionReferenteId,
                                                                   List<MedicamentoNegociacionDto> medicamento, 
                                                                   Integer userId)
    {
    	List<Long> medicamentoIds = new ArrayList<>();
    	medicamento.stream().forEach(med ->{
				medicamentoIds.add(med.getMedicamentoDto().getId());
			});
    	ejecutarAsignacionValorContratoAnteriorByNegociacionReferente(medicamentoIds, 
                                                                      negociacionId, 
                                                                      negociacionReferenteId, 
                                                                      userId);
    }
    
    private void ejecutarAsignacionValorContratoAnteriorByNegociacionReferente(List<Long> medicamentoIds,
                                                                               Long negociacionId, 
                                                                               Long negociacionReferenteId,
                                                                               Integer userId)
    {
        if(medicamentoIds.size() > 0 && !medicamentoIds.isEmpty() && Objects.nonNull(negociacionReferenteId)) 
        {            
            String medIds = medicamentoIds.stream()
				.map(sn -> String.valueOf(sn))
				.collect(Collectors.joining(","));            
            // Actualizacion de medicamentos
            StoredProcedureQuery spQuery = em.createStoredProcedureQuery("contratacion.fn_aplicar_valor_contrato_anterior_by_negociacion_medicamentos")
                                        .registerStoredProcedureParameter("in_negociacion_id", Long.class, ParameterMode.IN)
                                        .registerStoredProcedureParameter("in_negociacion_referente_id", Long.class, ParameterMode.IN)                                                      
                                        .registerStoredProcedureParameter("in_list_servicio_id", String.class, ParameterMode.IN)                                                
                                        .registerStoredProcedureParameter("in_user_id", Integer.class, ParameterMode.IN)                                                   
                                        .setParameter("in_negociacion_id", negociacionId)
                                        .setParameter("in_negociacion_referente_id", negociacionReferenteId) 
                                        .setParameter("in_list_servicio_id", medIds)                                                 
                                        .setParameter("in_user_id", userId);
            spQuery.execute();             
    	}
    }

    @Override
    public void asignarTarifas(List<Long> medicamentos, Long negociacionId, Integer userId) {
        em.createNativeQuery("UPDATE contratacion.sede_negociacion_medicamento "
                + "SET valor_negociado = CASE WHEN m.valor_referente = 0 THEN NULL ELSE m.valor_referente END, user_id = :userId "
                + "FROM contratacion.sedes_negociacion sn "
                + "CROSS JOIN maestros.medicamento m "
                + "INNER JOIN contratacion.negociacion n "
                + "ON n.id = sn.negociacion_id "
                + "WHERE sn.id = sede_negociacion_id "
                + "AND m.id = medicamento_id "
                + "AND n.id = :negociacionId "
                + "AND medicamento_id IN (:medicamentos)")
                .setParameter("negociacionId", negociacionId)
                .setParameter("medicamentos", medicamentos)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public Integer eliminarByNegociacionAndMedicamento(final Long negociacionId, final Long medicamentoId, Integer userId) {

    	StringBuilder queryAuditoria = new StringBuilder();
    	queryAuditoria.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarMedicamentos())
    	.append(" where sn.negociacion_id = :negociacionId and snm.medicamento_id = :medicamentoId");

    	//Para registrar en auditoría los medicamentos a eliminar
    	em.createNativeQuery(queryAuditoria.toString())
	    	.setParameter("negociacionId", negociacionId)
	        .setParameter("medicamentoId", medicamentoId)
	        .setParameter("userId", userId)
	        .executeUpdate();

        Query query = em.createNamedQuery("SedeNegociacionMedicamento.deleteByNegociacionAndMedicamento");
        return query.setParameter("negociacionId", negociacionId)
                .setParameter("medicamentoId", medicamentoId).executeUpdate();
    }

    @Override
    public Integer eliminarByNegociacionAndMedicamento(Long negociacionId, List<Long> medicamentoIds, Long grupoId, Integer userId) {

    	StringBuilder queryAuditoriaMx = new StringBuilder();
    	queryAuditoriaMx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarMedicamentos())
    	.append(" where sn.negociacion_id = :negociacionId and snm.medicamento_id in (:medicamentoIds)");

    	//Para registrar en auditoría los medicamentos a eliminar
    	em.createNativeQuery(queryAuditoriaMx.toString())
    		.setParameter("userId", userId)
	    	.setParameter("negociacionId", negociacionId)
			.setParameter("medicamentoIds", medicamentoIds)
			.executeUpdate();

    	for(Long mxId: medicamentoIds) {
    		 em.createNamedQuery("SedeNegociacionMedicamento.deleteByNegociacionAndMedicamento")
				.setParameter("negociacionId", negociacionId)
				.setParameter("medicamentoId", mxId)
				.executeUpdate();
    	}

    	String conteo = em.createNamedQuery("SedeNegociacionMedicamento.contarProcedimientosByNegociacionGrupo")
				.setParameter("negociacionId", negociacionId)
				.setParameter("grupoId", grupoId)
				.getSingleResult().toString();

        Integer sinMedicamento = conteo != null ? new Integer(conteo) : 0;

        if(sinMedicamento == 0) {

        	List<Long> grupoIds = new ArrayList<Long>();
        	grupoIds.add(grupoId);

        	StringBuilder queryAuditoriaGx = new StringBuilder();
        	queryAuditoriaGx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarGrupoTerapeutico())
        	.append(" where sn.negociacion_id = :negociacionId and sngt.categoria_medicamento_id in (:grupoIds)");

        	//Para registrar los grupos terapeúticos a eliminar
        	em.createNativeQuery(queryAuditoriaGx.toString())
		        	.setParameter("negociacionId", negociacionId)
					.setParameter("grupoIds", grupoIds)
					.setParameter("userId", userId)
					.executeUpdate();

        	//Elimina los grupos terapéuticos
    		em.createNamedQuery(
    				"SedeNegociacionGrupoTerapeutico.deleteByNegociacionIdAndCapitulo")
    				.setParameter("negociacionId", negociacionId)
    				.setParameter("gruposId", grupoIds).executeUpdate();
        }

        negociacionMedicamentoControl.actualizarValorNegociadoGrupoByMedicamentosNegociacion(negociacionId, userId);

        return sinMedicamento;

    }

    @Override
    public void guardarMedicamentosNegociados(final List<MedicamentoNegociacionDto> medicamentos, final Long negociacionId, Integer userId) {
        for (MedicamentoNegociacionDto dto : medicamentos) {
        	dto.setNegociado(Objects.nonNull(dto.getValorNegociado()) ? true : false);
            em.createNamedQuery("SedeNegociacionMedicamento.updateByNegociacionAndMedicamentos")
                    .setParameter("valorNegociado", dto.getValorNegociado())
                    .setParameter("negociado", dto.getNegociado())
                    .setParameter("negociacionId", negociacionId)
                    .setParameter("medicamentoId", dto.getMedicamentoDto().getId())
                    .setParameter("userId", userId)
                    .executeUpdate();
        }

    }

    @Override
    public void guardarMedicamentoNegociado(MedicamentoNegociacionDto medicamento, Long negociacionId, Integer userId) {
        em.createNamedQuery("SedeNegociacionMedicamento.updateByNegociacionAndMedicamentos")
                .setParameter("valorNegociado", medicamento.getValorNegociado())
                .setParameter("negociado", medicamento.getNegociado())
                .setParameter("negociacionId", negociacionId)
                .setParameter("medicamentoId", medicamento.getMedicamentoDto().getId())
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void guardarMedicamentoNegociadoPgp(MedicamentoNegociacionDto medicamento, Long negociacionId, Integer userId) {
        em.createNamedQuery("SedeNegociacionMedicamento.updateByNegociacionAndMedicamentosPgp")
        		.setParameter("costoMedioUsuario", medicamento.getCostoMedioUsuario())
                .setParameter("valorNegociado", medicamento.getValorNegociado())
                .setParameter("negociado", medicamento.getNegociado())
                .setParameter("negociacionId", negociacionId)
                .setParameter("medicamentoId", medicamento.getMedicamentoDto().getId())
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void agregarMedicamentosNegociacion(List<Long> medicamentoIds, Long negociacionId, Integer userId, Long negociacionReferenteId) {
    	medicamentoIds.forEach(mx -> negociacionMedicamentoControl.agregarMedicamentoNegociacionEvento(mx, negociacionId, userId));
        ejecutarAsignacionValorContratoAnteriorByNegociacionReferente(medicamentoIds, negociacionId, negociacionReferenteId, userId);
    }

    @Override
    public void eliminarSedesNegociacionCategoriaMedicamentoPorSedeNegociacionIds(List<Long> ids, Integer userId) {

    	StringBuilder queryAuditoriaCx = new StringBuilder();
    	queryAuditoriaCx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarCategoriaMedicamentos())
    	.append(" where sn.id in (:ids)");

    	//Para registrar en auditoria las categorias de medicamentos a eliminar
    	em.createNativeQuery(queryAuditoriaCx.toString())
    		.setParameter("userId", userId)
	    	.setParameter("ids", ids)
	        .executeUpdate();


    	StringBuilder queryAuditoriaMx = new StringBuilder();
    	queryAuditoriaMx.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarMedicamentos())
    	.append(" where sn.id in (:ids)");

    	//Para registrar en auditoria los medicamentos a eliminar
    	em.createNativeQuery(queryAuditoriaMx.toString())
	    	.setParameter("userId", userId)
	    	.setParameter("ids", ids)
	        .executeUpdate();


        em.createNamedQuery("SedeNegociacionCategoriaMedicamento.deleteBySedeNegociacionIds")
                .setParameter("ids", ids)
                .executeUpdate();
        em.createNamedQuery("SedeNegociacionMedicamento.deleteBySedeNegociacionIds")
                .setParameter("ids", ids)
                .executeUpdate();
    }

    @Override
    public void eliminarCategoriaMedicamentoPorSedeNegociacionIdsAndCategoria(List<MacroCategoriaMedicamentoEnum> categorias, List<Long> ids, Integer userId) {

    	StringBuilder queryAuditoria = new StringBuilder();
    	queryAuditoria.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarCategoriaMedicamentos())
    	.append(" where sn.id in (:ids) and snm.macro_categoria_medicamento_id in (:categorias)");

    	List<Integer> categoriaIds = new ArrayList<>();
    	categorias.stream().forEach(ct-> {
    		categoriaIds.add(ct.getId());
    	});

    	//Para registrar en auditoria las categorías de medicamentos a eliminar
    	em.createNativeQuery(queryAuditoria.toString())
	    	.setParameter("userId", userId)
	    	.setParameter("ids", ids)
	        .setParameter("categorias", categoriaIds)
	        .executeUpdate();

        em.createNamedQuery("SedeNegociacionCategoriaMedicamento.deleteBySedeNegociacionIdsAndCategorias")
                .setParameter("ids", ids)
                .setParameter("categorias", categorias)
                .executeUpdate();
    }

    @Override
    public void guardarNegociacion(NegociacionDto negociacion, MedicamentoNegociacionDto categoria) {
    	if(categoria.isNegociaPorcentaje() && Objects.nonNull(categoria.getPorcentajeNegociado())){
    		categoria.setPorcentajeNegociado(Objects.nonNull(categoria.getPorcentajeNegociado()) ? categoria.getPorcentajeNegociado().setScale(DIGITOS_APROXIMACION_PROCENTAJE, BigDecimal.ROUND_HALF_UP) : null);
    		BigDecimal valorNegociado =
    				(negociacion.getValorUpcMensual().divide(BigDecimal.valueOf(100)).multiply(categoria.getPorcentajeNegociado()));
    		categoria.setValorNegociado(Objects.nonNull(valorNegociado) ? valorNegociado.setScale(0, BigDecimal.ROUND_HALF_UP) : null);
    	}else if(!categoria.isNegociaPorcentaje() && Objects.nonNull(categoria.getValorNegociado())){
    		categoria.setValorNegociado(Objects.nonNull(categoria.getValorNegociado()) ? categoria.getValorNegociado().setScale(0, BigDecimal.ROUND_HALF_UP) : null);
    		categoria.setPorcentajeNegociado(
    				categoria.getValorNegociado().divide(negociacion.getValorUpcMensual(), DIGITOS_APROXIMACION_PROCENTAJE , BigDecimal.ROUND_HALF_UP).multiply(BigDecimal.valueOf(100D)));
    		categoria.setPorcentajeNegociado(Objects.nonNull(categoria.getPorcentajeNegociado()) ? categoria.getPorcentajeNegociado().setScale(DIGITOS_APROXIMACION_PROCENTAJE, BigDecimal.ROUND_HALF_UP) : null);
    	}
        em.createNamedQuery("SedeNegociacionCategoriaMedicamento.updateValorAndPorcentajeByCategoriaIdAndSedeNegociacionIds")
                .setParameter("valorNegociado", categoria.getValorNegociado())
                .setParameter("porcentajeNegociado", categoria.getPorcentajeNegociado())
                .setParameter("ids", categoria.getSedesNegociacionMedicamentoIds())
                .setParameter("categoria", categoria.getMacroCategoriaMedicamento())
                .executeUpdate();
    }

    @Override
    public void guardarNegociacion(MedicamentoNegociacionDto negociacion, Long zonaCapitaId, Integer regimenId) {
        em.createNamedQuery("SedeNegociacionCategoriaMedicamento.asignarValorCategorias")
                .setParameter("porcentajeReferente", negociacion.getPorcentajeReferencia())
                .setParameter("sedeNegociacionId", negociacion.getIdSedeNegociacionMedicamento())
                .setParameter("valorNegociacion", negociacion.getValorNegociado())
                .setParameter("zonaCapitaId", zonaCapitaId)
                .setParameter("regimenId", regimenId)
                .executeUpdate();
    }

    @Override
    public void asignarValor(List<Long> ids, BigDecimal valor, Long zonaCapitaId, Integer regimenId, Integer userId) {
        em.createNamedQuery("SedeNegociacionCategoriaMedicamento.updateValorBySedeNegociacionIds")
                .setParameter("ids", ids)
                .setParameter("valor", valor)
                .setParameter("zonaCapitaId", zonaCapitaId)
                .setParameter("regimenId", regimenId)
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void asignarValor(NegociacionDto negociacion, List<MedicamentoNegociacionDto> medicamentosNegociados, BigDecimal valor, Integer userId) {
    	em.createNamedQuery("SedeNegociacionCategoriaMedicamento.asignarValorBySedeNegociacionIds")
                .setParameter("ids", this.negociacionMedicamentoControl.obtenerIds(medicamentosNegociados))
                .setParameter("valor", valor)
                .setParameter("porcentajeNegociado",
                		valor.divide(negociacion.getValorUpcMensual(), DIGITOS_APROXIMACION_PROCENTAJE , BigDecimal.ROUND_HALF_UP)
                		.multiply(BigDecimal.valueOf(100D)))
                .setParameter("categorias", this.negociacionMedicamentoControl.obtenerCategorias(negociacion, medicamentosNegociados))
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void asignarValorPorPorcentaje(List<Long> ids, BigDecimal porcentaje, NegociacionDto negociacion, List<MedicamentoNegociacionDto> medicamentosNegociados, Integer userId) {
        em.createNamedQuery("SedeNegociacionCategoriaMedicamento.updateValorBySedeNegociacionIdsAndPercent")
                .setParameter("ids", ids)
                .setParameter("percent", porcentaje)
                .setParameter("categorias", this.negociacionMedicamentoControl.obtenerCategorias(negociacion, medicamentosNegociados))
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void asignarValorContratoAnterior(List<MedicamentoNegociacionDto> medicamento, NegociacionDto negociacion, Integer userId){
   		medicamento.stream()
   			.filter(medi -> Objects.nonNull(medi.getValorContratoAnteriorCategoria())
   					&& medi.getValorContratoAnteriorCategoria().compareTo(BigDecimal.ZERO) >= 1)
   			.forEach(med -> {
   				em.createNamedQuery("SedeNegociacionCategoriaMedicamento.updateValoresContratoNegociado")
       			.setParameter("ids", med.getSedesNegociacionMedicamentoIds())
       			.setParameter("categorias", med.getMacroCategoriaMedicamento())
       			.setParameter("valorUpc", negociacion.getValorUpcMensual())
       			.setParameter("userId", userId)
       			.executeUpdate();
   			});
    }

    @Override
    public void asignarValorReferente(NegociacionDto negociacion, List<MedicamentoNegociacionDto> medicamentosNegociados, Integer userId) {
        em.createNamedQuery("SedeNegociacionCategoriaMedicamento.asignarValorReferente")
                .setParameter("ids", this.negociacionMedicamentoControl.obtenerIds(medicamentosNegociados))
                .setParameter("zonaCapitaId", negociacion.getZonaCapita().getId().longValue())
                .setParameter("regimenId", negociacion.getRegimen().getId())
                .setParameter("valorUpc", negociacion.getValorUpcMensual())
                .setParameter("categorias", this.negociacionMedicamentoControl.obtenerCategoriasIds(negociacion, medicamentosNegociados))
                .setParameter("userId", userId)
                .executeUpdate();
    }

    @Override
    public void actualizarValorCategorias(boolean isNegociaPorcentaje, BigDecimal valorServNegociado, BigDecimal porcentajeServNegociado, NegociacionDto negociacion,BigDecimal porcentajeAsignacion, Integer userId) {

        BigDecimal valorNegociado = BigDecimal.ZERO;
        if(isNegociaPorcentaje && Objects.nonNull(porcentajeServNegociado)){
            valorNegociado = negociacion
                    .getValorUpcMensual()
                    .multiply((porcentajeServNegociado).divide(BigDecimal.valueOf(100), MathContext.DECIMAL64),MathContext.DECIMAL64)
                    .setScale(0, BigDecimal.ROUND_HALF_UP);

        }else if(Objects.isNull(isNegociaPorcentaje) || !isNegociaPorcentaje){
            valorNegociado = valorServNegociado;
            porcentajeServNegociado = (valorServNegociado.multiply(BigDecimal.valueOf(100D), MathContext.DECIMAL64)
                    .divide(negociacion.getValorUpcMensual(), MathContext.DECIMAL64).setScale(DECIMALES_APROXIMACION, BigDecimal.ROUND_HALF_UP));
        }

        em.createNamedQuery("SedeNegociacionCategoriaMedicamento.actualizarValorByServicioCategorias")
                .setParameter("valorNegociacion", valorNegociado)
                .setParameter("zonaCapitaId", negociacion.getZonaCapita().getId().longValue())
                .setParameter("regimenId", negociacion.getRegimen().getId())
                .setParameter("upcTotalServicio", porcentajeServNegociado)
                .setParameter("porcentajeAsignadoServicio", porcentajeAsignacion)
                .setParameter("negociacionId", negociacion.getId())
                .setParameter("userId", userId)
                .executeUpdate();

    }

	@Override
	public void distribuirCategorias(BigDecimal valorServNegociado, BigDecimal porcentajeServNegociado,	NegociacionDto negociacion, Integer userId){
		em.createNamedQuery("SedeNegociacionCategoriaMedicamento.distribuirCategorias")
		.setParameter("porcentajeNegociado", porcentajeServNegociado)
        .setParameter("valorNegociacion", valorServNegociado)
        .setParameter("zonaCapitaId", negociacion.getZonaCapita().getId().longValue())
        .setParameter("regimenId", negociacion.getRegimen().getId())
        .setParameter("negociacionId", negociacion.getId())
        .setParameter("userId", userId)
        .executeUpdate();
	}

	@Override
	public void almacenarMedicamentosArchivoImportado(List<MedicamentoNegociacionDto> listMedicamentosNegociacion, Integer userId, NegociacionModalidadEnum negociacionModalidad) throws ConexiaSystemException{
		if (Objects.isNull(listMedicamentosNegociacion.isEmpty())) {
            throw new IllegalArgumentException("La lista de ofertas sede medicamentos no puede ser nula");
        }
        if (Objects.isNull(userId)) {
            throw new IllegalArgumentException("El usuario no puede ser nulo");
        }
        try{

        	if(negociacionModalidad.equals(NegociacionModalidadEnum.EVENTO)) {
    			tarifaImportar = listMedicamentosNegociacion.get(0).getTarifaImportar();
    		}

        	// Verificar si es Ria
        	List<String> actividad = listMedicamentosNegociacion
        		.stream().filter(m -> (Objects.nonNull(m.getActividad()) && Objects.nonNull(m.getActividad().getDescripcion())))
        		.map(m -> m.getActividad().getDescripcion()).collect(Collectors.toList());

        	if(Objects.nonNull(actividad) && !actividad.isEmpty()){
        		 listMedicamentosNegociacion.stream().forEach(medicamentoNegociacion -> {
        			int result = em.createNamedQuery("SedeNegociacionMedicamento.updateNegociacionMedicamentoRiasCapitaByArchivo")
  		            .setParameter("sedeNegociacionId", medicamentoNegociacion.getSedeNegociacionId())
  		            .setParameter("codigoMedicamento", medicamentoNegociacion.getMedicamentoDto().getCums())
  		            .setParameter("riasDescripcion", medicamentoNegociacion.getRangoPoblacion().getNegociacionRia().getRia().getDescripcion().toUpperCase())
  		            .setParameter("actividadDescripcion", medicamentoNegociacion.getActividad().getDescripcion().toUpperCase())
  		            .setParameter("rangoPoblacionDescripcion", medicamentoNegociacion.getRangoPoblacion().getRangoPoblacion().getDescripcion().toUpperCase())
    					.setParameter("pesoPorcentual", medicamentoNegociacion.getPorcentajePropuestoPortafolio())
    					.setParameter("userId", userId)
  		            .executeUpdate();

        			if(result == 0){
        				if("GENERAL".equals(medicamentoNegociacion.getRangoPoblacion().getRangoPoblacion().getDescripcion().toUpperCase())
        						&& "ACTIVIDAD".equals(medicamentoNegociacion.getActividad().getDescripcion().toUpperCase())) {
        					em.createNamedQuery("SedeNegociacionMedicamento.insertNegociacionMedicamentoRiasCapitaByArchivoExcluirReferente")
	        					.setParameter("sedeNegociacionId", medicamentoNegociacion.getSedeNegociacionId())
	        					.setParameter("codigoMedicamento", medicamentoNegociacion.getMedicamentoDto().getCums())
	        					.setParameter("riasDescripcion", medicamentoNegociacion.getRangoPoblacion().getNegociacionRia().getRia().getDescripcion().toUpperCase())
	        					.setParameter("actividadDescripcion", medicamentoNegociacion.getActividad().getDescripcion().toUpperCase())
	        					.setParameter("rangoPoblacionDescripcion", medicamentoNegociacion.getRangoPoblacion().getRangoPoblacion().getDescripcion().toUpperCase())
	        					.setParameter("pesoPorcentual", medicamentoNegociacion.getPorcentajePropuestoPortafolio())
	        					.setParameter("userId", userId)
	        					.executeUpdate();
        				}else {
        					em.createNamedQuery("SedeNegociacionMedicamento.insertNegociacionMedicamentoRiasCapitaByArchivo")
	        					.setParameter("sedeNegociacionId", medicamentoNegociacion.getSedeNegociacionId())
	        					.setParameter("codigoMedicamento", medicamentoNegociacion.getMedicamentoDto().getCums())
	        					.setParameter("riasDescripcion", medicamentoNegociacion.getRangoPoblacion().getNegociacionRia().getRia().getDescripcion().toUpperCase())
	        					.setParameter("actividadDescripcion", medicamentoNegociacion.getActividad().getDescripcion().toUpperCase())
	        					.setParameter("rangoPoblacionDescripcion", medicamentoNegociacion.getRangoPoblacion().getRangoPoblacion().getDescripcion().toUpperCase())
	        					.setParameter("pesoPorcentual", medicamentoNegociacion.getPorcentajePropuestoPortafolio())
	        					.setParameter("userId", userId)
	        					.executeUpdate();
        				}
        			}
 		        });
        	}else{

		        // Actualiza los registros existentes
		        listMedicamentosNegociacion.stream().forEach(medicamentoNegociacion -> {
		        	Query updt = em.createNativeQuery(negociacionMedicamentoControl.generarUpdateNegociacionMedicamentoByArchivo(negociacionModalidad))
		            .setParameter("sedeNegociacionId", medicamentoNegociacion.getSedeNegociacionId())
		            .setParameter("codigoMedicamento", medicamentoNegociacion.getMedicamentoDto().getCums())
		            .setParameter("userId", userId);

		        	switch(negociacionModalidad) {
			        	case EVENTO:
			        		if(medicamentoNegociacion.getMedicamentoDto().getRegulado()) {
			        			switch(tarifaImportar) {
				        			case VALOR_REFERENTE_MAXIMO:
						        		updt.setParameter("valorNegociado", (medicamentoNegociacion.getValorReferencia() != null ?  medicamentoNegociacion.getValorReferencia().longValue() : BigDecimal.ZERO.longValue()));
				        				break;
				        			case VALOR_REFERENTE_MINIMO:
				        				updt.setParameter("valorNegociado", (medicamentoNegociacion.getValorReferenciaMinimo() != null ?  medicamentoNegociacion.getValorReferenciaMinimo().longValue() : BigDecimal.ZERO.longValue()));
				        				break;
				        			case VALOR_ARCHIVO:
				        				updt.setParameter("valorNegociado", (medicamentoNegociacion.getValorNegociado() != null ?  medicamentoNegociacion.getValorNegociado().longValue() : BigDecimal.ZERO.longValue()));
				        				break;
				        			default:
				        				break;
				        		}
			        		} else {
			        			updt.setParameter("valorNegociado", (medicamentoNegociacion.getValorNegociado() != null ?  medicamentoNegociacion.getValorNegociado().longValue() : BigDecimal.ZERO.longValue()));
			        		}

			        		break;
			        	default:
			        		updt.setParameter("valorPropuesto", medicamentoNegociacion.getValorPropuestoPortafolio());
			        		break;
		        	}

		        	int result = updt.executeUpdate();

		        	if(result == 0){

		        		Query inst = em.createNativeQuery(negociacionMedicamentoControl.generarInsertNegociacionMedicamentoByArchivo(negociacionModalidad))
			            .setParameter("sedeNegociacionId", medicamentoNegociacion.getSedeNegociacionId())
			            .setParameter("codigoMedicamento", medicamentoNegociacion.getMedicamentoDto().getCums())
			            .setParameter("userId", userId);

		        		switch(negociacionModalidad) {
				        	case EVENTO:
				        		if(medicamentoNegociacion.getMedicamentoDto().getRegulado()) {
				        			switch(tarifaImportar) {
					        			case VALOR_REFERENTE_MAXIMO:
					        				inst.setParameter("valorNegociado", (medicamentoNegociacion.getValorReferencia() != null ?  medicamentoNegociacion.getValorReferencia().longValue() : BigDecimal.ZERO.longValue()));
					        				break;
					        			case VALOR_REFERENTE_MINIMO:
					        				inst.setParameter("valorNegociado", (medicamentoNegociacion.getValorReferenciaMinimo() != null ?  medicamentoNegociacion.getValorReferenciaMinimo().longValue() : BigDecimal.ZERO.longValue()));
					        				break;
					        			case VALOR_ARCHIVO:
					        				inst.setParameter("valorNegociado", (medicamentoNegociacion.getValorNegociado() != null ?  medicamentoNegociacion.getValorNegociado().longValue() : BigDecimal.ZERO.longValue()));
					        				break;
					        			default:
					        				break;
					        		}
				        		} else {
				        			inst.setParameter("valorNegociado", (medicamentoNegociacion.getValorNegociado() != null ?  medicamentoNegociacion.getValorNegociado().longValue() : BigDecimal.ZERO.longValue()));
				        		}

				        		break;
				        	default:
				        		inst.setParameter("valorPropuesto", medicamentoNegociacion.getValorPropuestoPortafolio());
				        		break;
			        	}

			            inst.executeUpdate();
		        	}
		        });
        	}
        }catch(Exception e){
        	throw new ConexiaSystemException("No fue posible realizar la importación del archivo. "+e.fillInStackTrace());
        }
    }

	@Override
	public void asignarValorCostoMedio(Long negociacionId, List<MedicamentoNegociacionDto> medicamento, Integer poblacion, boolean aplicarReferente, Integer userId) {
    	medicamento.stream().filter(medi ->
    			Objects.nonNull(medi.getFrecuenciaReferente()) &&
    			Objects.nonNull(medi.getCostoMedioUsuarioReferente()) &&
    			medi.getCostoMedioUsuarioReferente().compareTo(BigDecimal.ZERO) >= 1)
			.forEach(medi ->{
				if(aplicarReferente){
					medi.setValorNegociado(medi.getCostoMedioUsuarioReferente()
							.multiply(BigDecimal.valueOf(medi.getFrecuenciaReferente())
							.multiply(BigDecimal.valueOf(poblacion))));
					medi.setCostoMedioUsuario(medi.getCostoMedioUsuarioReferente());
				}else if(Objects.nonNull(medi.getCostoMedioUsuario())){
					medi.setValorNegociado(medi.getCostoMedioUsuario()
							.multiply(BigDecimal.valueOf(medi.getFrecuenciaReferente())
							.multiply(BigDecimal.valueOf(poblacion))));
					medi.setCostoMedioUsuario(medi.getCostoMedioUsuario());
				}
				if(Objects.nonNull(medi.getCostoMedioUsuario()) && Objects.nonNull(medi.getValorNegociado())){
					em.createNativeQuery("UPDATE contratacion.sede_negociacion_medicamento snm"
		            		+ " SET costo_medio_usuario =:costoMedioUsuario, "
		            		+ "		valor_negociado = :valorNegociado, "
		            		+ "		frecuencia_referente = :frecuenciaReferente, "
		            		+ "		costo_medio_usuario_referente = :costoMedioUsuarioReferente, "
		            		+ "		negociado = true, user_id = :userId "
		            		+ "FROM (SELECT snm.id "
		            		+ "		FROM contratacion.sede_negociacion_medicamento snm "
		            		+ "		JOIN contratacion.sedes_negociacion sn ON snm.sede_negociacion_id = sn.id "
		            		+ "		WHERE sn.negociacion_id = :negociacionId "
		            		+ "		AND   snm.medicamento_id = :medicamentoId) as negociacionMedicamento "
		            		+ "where negociacionMedicamento.id = snm.id ")
		            		.setParameter("negociacionId", negociacionId)
		            		.setParameter("medicamentoId", medi.getMedicamentoDto().getId())
		            		.setParameter("costoMedioUsuario", aplicarReferente ? medi.getCostoMedioUsuarioReferente() : medi.getCostoMedioUsuario())
		            		.setParameter("valorNegociado", medi.getValorNegociado())
		            		.setParameter("frecuenciaReferente", medi.getFrecuenciaReferente())
		            		.setParameter("costoMedioUsuarioReferente", medi.getCostoMedioUsuarioReferente())
		            		.setParameter("userId", userId)
		                    .executeUpdate();
				}
			});
	}

	@Override
	public void guardarValorReferenteMedicamentosPGP(Long negociacionId, Long grupoTerapeuticoId, List<MedicamentoNegociacionDto> medicamento, Integer userId) throws ConexiaBusinessException {

		//Para actualizar los medicamentos en sedeNegociacionMedicamento

		medicamento.stream()
		.forEach(dto -> {
				try {
					this.negociacionMedicamentoControl.actualizarMedicamentoValoresNegociadosPGP(dto, negociacionId, grupoTerapeuticoId);
				} catch (ConexiaBusinessException e) {
					e.printStackTrace();
				}
		});

		List<Long> grupoTerapeuticoIds = new ArrayList<Long>();
		grupoTerapeuticoIds.add(grupoTerapeuticoId);

		//Actualizar los gruposTerapeuticos
		negociacionMedicamentoControl.actualizarValorNegociadoGrupoTerapeuticoNegociacion(negociacionId, grupoTerapeuticoIds, userId, true);


	}

	@Override
	public void guardarMedicamentosFranjaPGP(Long negociacionId, List<Long> medicamentoIds, Long grupoId, BigDecimal franjaInicio, BigDecimal franjaFin, Integer userId) throws ConexiaBusinessException {
		List<Long> grupoTerapeuticoIds = new ArrayList<Long>();
		grupoTerapeuticoIds.add(grupoId);
		negociacionMedicamentoControl.actualizarMedicamentoFranjaPGP(negociacionId, medicamentoIds, grupoId, franjaInicio, franjaFin);
		negociacionMedicamentoControl.actualizarFranjaRiesgoGrupoNegociacion(negociacionId, grupoTerapeuticoIds, userId);
	}

	@Override
	public void aplicarValorNegociadoByPoblacion(Long negociacionId, Integer userId) throws ConexiaBusinessException {
		this.negociacionMedicamentoControl.actualizarMedicamentosNegociadosValorPGP(negociacionId);
		this.negociacionMedicamentoControl.actualizarValorNegociadoGrupoByMedicamentosNegociacion(negociacionId, userId);
	}

	@Override
	public void guardarGrupoTerapeuticoPGP(Long negociacionId, List<GrupoTerapeuticoNegociacionDto> gruposNegociacion, Integer poblacion, Integer userId) {

		List<Long> grupoTerapeuticoIds = new ArrayList<Long>();

		gruposNegociacion.stream()
			.forEach(grupo ->{
				grupoTerapeuticoIds.add(grupo.getCategoriaMedicamento().getId());
			});
		try {
			negociacionMedicamentoControl.actualizarMedicamentoValoresNegociados(negociacionId, grupoTerapeuticoIds, poblacion, userId);
			negociacionMedicamentoControl.actualizarValorNegociadoGrupoTerapeuticoNegociacion(negociacionId, grupoTerapeuticoIds, userId, true);
		} catch (ConexiaBusinessException e) {

		}
	}

	@Override
	public void guardarFranjaGruposPGP(Long negociacionId, List<GrupoTerapeuticoNegociacionDto> gruposNegociacion, BigDecimal franjaInicio, BigDecimal franjaFin, Integer userId) {
		List<Long> grupoTerapeuticoIds = new ArrayList<Long>();

		gruposNegociacion.stream()
			.forEach(grupo ->{
				grupoTerapeuticoIds.add(grupo.getCategoriaMedicamento().getId());
			});
		negociacionMedicamentoControl.actualizarMedicamentoFranjaRiesgo(negociacionId, grupoTerapeuticoIds, franjaInicio, franjaFin, userId);
		negociacionMedicamentoControl.actualizarFranjaRiesgoGrupoNegociacion(negociacionId, grupoTerapeuticoIds, userId);

	}

	@Override
	public void eliminarByNegociacionAndGruposAllMedicamentos(Long negociacionId, List<Long> gruposId, Integer userId) {
		//Elimina los medicamentos
		negociacionMedicamentoControl.eliminarMedicamentosByNegociacionAndGrupo(
				negociacionId,
			    gruposId,
			    userId);

		StringBuilder queryAuditoria = new StringBuilder();
		queryAuditoria.append(eliminarTecnologiasAuditoriaControl.generarEncabezadoEliminarGrupoTerapeutico())
		.append(" where sn.negociacion_id = :negociacionId and sngt.categoria_medicamento_id in (:gruposId)");

		//Para registrar en auditoria los grupos a eliminar
		em.createNativeQuery(queryAuditoria.toString())
			.setParameter("userId", userId)
			.setParameter("negociacionId", negociacionId)
			.setParameter("gruposId", gruposId)
			.executeUpdate();

		//Elimina los grupos terapéuticos
		em.createNamedQuery(
				"SedeNegociacionGrupoTerapeutico.deleteByNegociacionIdAndCapitulo")
				.setParameter("negociacionId", negociacionId)
				.setParameter("gruposId", gruposId).executeUpdate();

	}
}
