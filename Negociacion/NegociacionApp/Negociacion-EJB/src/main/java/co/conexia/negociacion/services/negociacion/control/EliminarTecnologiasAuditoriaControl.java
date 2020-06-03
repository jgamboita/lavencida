package co.conexia.negociacion.services.negociacion.control;


/**
 * Control para generar los querys de las inserciones en las tablas de auditoria de negociación al eliminar tecnologías en todas las
 * modalidades de contratación
 * @author clozano
 *
 */
public class EliminarTecnologiasAuditoriaControl {

	/**
	 * Genera la base para insertar en auditoría registros asociados a la eliminación de tecnologías de la tabla
	 * contratacion.sede_negociacion_procedimiento para la modalidad de contratación Pgp
	 * @return el encabezado de la consulta
	 */
	public String generarEncabezadoEliminarProcedimientosPgp() {
		StringBuilder query = new StringBuilder();

		query.append(" insert into auditoria.sede_negociacion_procedimiento")
		.append(" (sede_negociacion_procedimiento_id, sede_negociacion_servicio_id, procedimiento_servicio_id, ")
		.append(" tarifario_contrato_id, tarifario_propuesto_id, tarifario_negociado_id,")
		.append(" porcentaje_contrato, porcentaje_propuesto, porcentaje_negociado, valor_contrato, valor_propuesto,")
		.append(" valor_negociado, negociado, requiere_autorizacion, ")
		.append(" frecuencia_referente, costo_medio_usuario_referente, costo_medio_usuario, peso_porcentual_referente,")
		.append(" valor_referente, negociacion_ria_rango_poblacion_id, actividad_id, user_id,")
		.append(" fecha_cambio, operacion,ultimo_modificado, frecuencia, sede_negociacion_capitulo_id, pto_id, franja_inicio, franja_fin)")
		.append(" select snp.id, snp.sede_negociacion_servicio_id, snp.procedimiento_id, snp.tarifario_contrato_id, snp.tarifario_propuesto_id, snp.tarifario_negociado_id,")
		.append(" snp.porcentaje_contrato, snp.porcentaje_propuesto, snp.porcentaje_negociado, snp.valor_contrato, snp.valor_propuesto,")
		.append(" snp.valor_negociado, snp.negociado, snp.requiere_autorizacion,")
		.append(" snp.frecuencia_referente, snp.costo_medio_usuario_referente, snp.costo_medio_usuario, snp.peso_porcentual_referente,")
		.append(" snp.valor_referente, snp.negociacion_ria_rango_poblacion_id, snp.actividad_id, :userId,")
		.append(" now(), 'DELETE', true, snp.frecuencia, snp.sede_negociacion_capitulo_id, snp.pto_id, snp.franja_inicio, snp.franja_fin")
		.append(" from contratacion.sede_negociacion_procedimiento snp")
		.append(" join contratacion.sede_negociacion_capitulo snc on snc.id = snp.sede_negociacion_capitulo_id")
		.append(" join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id");

		return query.toString();
	}


	/**
	 * Genera la base para insertar en auditoría registros asociados a la eliminación de tecnologías de la tabla
	 * contratacion.sede_negociacion_procedimiento para las modalidades diferentes de pgp
	 * @return el encabezado de la consulta
	 */
	public String generarEncabezadoEliminarProcedimientos() {
		StringBuilder query = new StringBuilder();

		query.append(" insert into auditoria.sede_negociacion_procedimiento")
		.append(" (sede_negociacion_procedimiento_id, sede_negociacion_servicio_id, procedimiento_servicio_id, ")
		.append(" tarifario_contrato_id, tarifario_propuesto_id, tarifario_negociado_id,")
		.append(" porcentaje_contrato, porcentaje_propuesto, porcentaje_negociado, valor_contrato, valor_propuesto,")
		.append(" valor_negociado, negociado, requiere_autorizacion, ")
		.append(" frecuencia_referente, costo_medio_usuario_referente, costo_medio_usuario, peso_porcentual_referente,")
		.append(" valor_referente, negociacion_ria_rango_poblacion_id, actividad_id, user_id,")
		.append(" fecha_cambio, operacion,ultimo_modificado, frecuencia, sede_negociacion_capitulo_id, pto_id, franja_inicio, franja_fin)")
		.append(" select snp.id, snp.sede_negociacion_servicio_id, snp.procedimiento_id, snp.tarifario_contrato_id, snp.tarifario_propuesto_id, snp.tarifario_negociado_id,")
		.append(" snp.porcentaje_contrato, snp.porcentaje_propuesto, snp.porcentaje_negociado, snp.valor_contrato, snp.valor_propuesto,")
		.append(" snp.valor_negociado, snp.negociado, snp.requiere_autorizacion,")
		.append(" snp.frecuencia_referente, snp.costo_medio_usuario_referente, snp.costo_medio_usuario, snp.peso_porcentual_referente,")
		.append(" snp.valor_referente, snp.negociacion_ria_rango_poblacion_id, snp.actividad_id, :userId,")
		.append(" now(), 'DELETE', true, snp.frecuencia, snp.sede_negociacion_capitulo_id, snp.pto_id, snp.franja_inicio, snp.franja_fin")
		.append(" from contratacion.sede_negociacion_procedimiento snp")
		.append(" join contratacion.sede_negociacion_servicio sns on sns.id = snp.sede_negociacion_servicio_id")
		.append(" join contratacion.sedes_negociacion sn on sn.id = sns.sede_negociacion_id ");

		return query.toString();
	}


	/**
	 * Genera la base del query para insertar registros en las tablas de auditoria al eliminar servicios
	 * @return
	 */
	public String generarEncabezadoEliminarServicios() {
		StringBuilder query = new StringBuilder();

		query.append(" INSERT INTO auditoria.sede_negociacion_servicio")
		.append(" (sede_negociacion_servicio_id, sede_negociacion_id, servicio_id, ")
		.append(" tarifa_diferencial, tarifario_contrato_id, tarifario_propuesto_id, tarifario_negociado_id,")
		.append(" porcentaje_contrato, porcentaje_propuesto, porcentaje_negociado, ")
		.append(" negociado, valor_contrato , es_transporte, valor_negociado, poblacion,")
		.append(" valor_upc_contrato, frecuencia_referente, costo_medio_usuario_referente, costo_medio_usuario,")
		.append(" user_id, fecha_cambio, operacion)")
		.append(" select sns.id, sns.sede_negociacion_id, sns.servicio_id, sns.tarifa_diferencial, sns.tarifario_contrato_id,")
		.append(" sns.tarifario_propuesto_id, sns.tarifario_negociado_id, sns.porcentaje_contrato, sns.porcentaje_propuesto,")
		.append(" sns.porcentaje_negociado, sns.negociado, sns.valor_contrato, sns.es_transporte, sns.valor_negociado, sns.poblacion,")
		.append(" sns.valor_upc_contrato, sns.frecuencia_referente, sns.costo_medio_usuario_referente, sns.costo_medio_usuario,")
		.append(" :userId, now(), 'DELETE'")
		.append(" from contratacion.sede_negociacion_servicio sns")
		.append(" join contratacion.sedes_negociacion sn on sn.id = sns.sede_negociacion_id ");

		return query.toString();
	}



	/**
	 * Permite generar la base para registrar en auditoria tras eliminar capítulos en negociaciones de la modalidad de Pgp
	 * @return
	 */
	public String generarEncabezadoEliminarCapitulos() {
		StringBuilder query = new StringBuilder();

		query.append(" INSERT INTO auditoria.sede_negociacion_capitulo")
		.append(" (sede_negociacion_capitulo_id, sede_negociacion_id, capitulo_id, negociado, valor_negociado, poblacion, ")
		.append(" frecuencia_referente, costo_medio_usuario_referente, frecuencia, costo_medio_usuario, user_id, fecha_cambio, ")
		.append(" operacion, numero_atenciones, numero_usuarios, franja_inicio, franja_fin, valor_referente)")
		.append(" select snc.id, snc.sede_negociacion_id, snc.capitulo_id, snc.negociado, snc.valor_negociado, snc.poblacion, ")
		.append(" snc.frecuencia_referente, snc.costo_medio_usuario_referente, snc.frecuencia, snc.costo_medio_usuario, :userId, now(), ")
		.append(" 'DELETE', snc.numero_atenciones, snc.numero_usuarios, snc.franja_inicio, snc.franja_fin, snc.valor_referente")
		.append(" from contratacion.sede_negociacion_capitulo snc ")
		.append(" join contratacion.sedes_negociacion sn on sn.id = snc.sede_negociacion_id ");

		return query.toString();
	}


	/**
	 * Permite generar la base para registrar en auditoria tras eliminar medicamentos en todas las modalidades de negociación
	 * diferentes de pgp
	 * @return
	 */
	public String generarEncabezadoEliminarMedicamentos() {
		StringBuilder query = new StringBuilder();

		query.append(" INSERT INTO auditoria.sede_negociacion_medicamento")
		.append(" (sede_negociacion_medicamento_id, sede_negociacion_id, medicamento_id, valor_contrato, valor_propuesto, ")
		.append(" valor_negociado, requiere_autorizacion, frecuencia_referente, costo_medio_usuario_referente, costo_medio_usuario, ")
		.append(" negociacion_ria_rango_poblacion_id, actividad_id, porcentaje_negociado, peso_porcentual_referente, valor_referente,")
		.append(" user_id, fecha_cambio,operacion,ultimo_modificado, sede_neg_grupo_t_id, frecuencia, franja_inicio, franja_fin)")
		.append(" select snm.id, snm.sede_negociacion_id, snm.medicamento_id, snm.valor_contrato, snm.valor_propuesto, ")
		.append(" snm.valor_negociado, snm.requiere_autorizacion, snm.frecuencia_referente, snm.costo_medio_usuario_referente, snm.costo_medio_usuario, ")
		.append(" snm.negociacion_ria_rango_poblacion_id, snm.actividad_id, snm.porcentaje_negociado, snm.peso_porcentual_referente, ")
		.append(" snm.valor_referente, :userId, now(),'DELETE', true, snm.sede_neg_grupo_t_id, snm.frecuencia, snm.franja_inicio, snm.franja_fin")
		.append(" from contratacion.sede_negociacion_medicamento snm ")
		.append(" join contratacion.sedes_negociacion sn on sn.id = snm.sede_negociacion_id ");

		return query.toString();
	}


	/**
	 * Permite generar la base para registrar en auditoria tras eliminar medicamentos en
	 * la modalidad de negociación Pgp
	 * @return
	 */
	public String generarEncabezadoEliminarMedicamentosPgp() {
		StringBuilder query = new StringBuilder();

		query.append(" INSERT INTO auditoria.sede_negociacion_medicamento")
		.append(" (sede_negociacion_medicamento_id, sede_negociacion_id, medicamento_id, valor_contrato, valor_propuesto, ")
		.append(" valor_negociado, requiere_autorizacion, frecuencia_referente, costo_medio_usuario_referente, costo_medio_usuario, ")
		.append(" negociacion_ria_rango_poblacion_id, actividad_id, porcentaje_negociado, peso_porcentual_referente, valor_referente,")
		.append(" user_id, fecha_cambio,operacion,ultimo_modificado, sede_neg_grupo_t_id, frecuencia, franja_inicio, franja_fin)")
		.append(" select snm.id, snm.sede_negociacion_id, snm.medicamento_id, snm.valor_contrato, snm.valor_propuesto, ")
		.append(" snm.valor_negociado, snm.requiere_autorizacion, snm.frecuencia_referente, snm.costo_medio_usuario_referente, snm.costo_medio_usuario, ")
		.append(" snm.negociacion_ria_rango_poblacion_id, snm.actividad_id, snm.porcentaje_negociado, snm.peso_porcentual_referente, ")
		.append(" snm.valor_referente, :userId, now(),'DELETE', true, snm.sede_neg_grupo_t_id, snm.frecuencia, snm.franja_inicio, snm.franja_fin")
		.append(" from contratacion.sede_negociacion_medicamento snm ")
		.append(" join contratacion.sede_negociacion_grupo_terapeutico sngt on sngt.id = snm.sede_neg_grupo_t_id ")
		.append(" join contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id ");

		return query.toString();
	}


	/**
	 * Genera la base para el query que inserta registros en auditoría el eliminar categorías de medicamentos
	 * @return
	 */
	public String generarEncabezadoEliminarCategoriaMedicamentos() {
		StringBuilder query = new StringBuilder();

		query.append(" INSERT INTO auditoria.sede_negociacion_categoria_medicamento ")
		.append(" (sede_negociacion_categoria_medicamento_id, valor_negociado, macro_categoria_medicamento_id,")
		.append(" sede_negociacion_id, porcentaje_negociado, porcentaje_contrato_anterior, valor_contrato_anterior ,")
		.append(" user_id,  fecha_cambio ,  operacion)")
		.append(" select snm.id, snm.valor_negociado, snm.macro_categoria_medicamento_id,")
		.append(" snm.sede_negociacion_id, snm.porcentaje_negociado, snm.porcentaje_contrato_anterior, snm.valor_contrato_anterior ,")
		.append(" :userId,  now(),  'DELETE'")
		.append(" from contratacion.sede_negociacion_categoria_medicamento snm")
		.append(" join contratacion.sedes_negociacion sn on sn.id = snm.sede_negociacion_id ");

		return query.toString();
	}


	/**
	 * Genera la base para el query que inserta registros en auditoría al eliminar grupos terapeuticos en negociaciones de la
	 * modalidad Pgp
	 * @return
	 */
	public String generarEncabezadoEliminarGrupoTerapeutico() {
		StringBuilder query = new StringBuilder();

		query.append(" INSERT INTO auditoria.sede_negociacion_grupo_terapeutico")
		.append(" (sede_negociacion_grupo_terapeutico_id, sede_negociacion_id, categoria_medicamento_id, negociado, valor_negociado, poblacion, ")
		.append(" frecuencia_referente, costo_medio_usuario_referente, costo_medio_usuario, frecuencia, user_id, numero_atenciones, numero_usuarios, ")
		.append(" franja_inicio, franja_fin, fecha_cambio, operacion, ultimo_modificado, valor_referente)")
		.append(" select sngt.id, sngt.sede_negociacion_id, sngt.categoria_medicamento_id, sngt.negociado, sngt.valor_negociado, sngt.poblacion, ")
		.append(" sngt.frecuencia_referente, sngt.costo_medio_usuario_referente, sngt.costo_medio_usuario, sngt.frecuencia, :userId, sngt.numero_atenciones, sngt.numero_usuarios, ")
		.append(" sngt.franja_inicio, sngt.franja_fin, now(), 'DELETE', true, sngt.valor_referente")
		.append(" from contratacion.sede_negociacion_grupo_terapeutico sngt")
		.append(" join contratacion.sedes_negociacion sn on sn.id = sngt.sede_negociacion_id ");

		return query.toString();
	}


	/**
	 * Genera la base para el query que inserta registros en auditoria el eliminar paquetes en la modalidad de negociación Evento
	 * @return
	 */
	public String generarEncabezadoEliminarPaquetes() {
		StringBuilder query = new StringBuilder();

		query.append(" INSERT INTO auditoria.sede_negociacion_paquete")
		.append(" (sede_negociacion_paquete_id, sede_negociacion_id, paquete_id, valor_contrato, valor_propuesto,")
		.append(" valor_negociado, requiere_autorizacion, user_id, fecha_cambio, operacion,ultimo_modificado)")
		.append(" select snp.id, snp.sede_negociacion_id, snp.paquete_id, snp.valor_contrato, snp.valor_propuesto,")
		.append(" snp.valor_negociado, snp.requiere_autorizacion, :userId, now(), 'DELETE', true")
		.append(" from contratacion.sede_negociacion_paquete snp")
		.append(" join contratacion.sedes_negociacion sn on sn.id = snp.sede_negociacion_id ");

		return query.toString();
	}








}
