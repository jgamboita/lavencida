Buen dia!!!

Seguir este orden de ejecucion de scripts:

1. ddl/ECST-57_createtable_maestros.medicamento_inactivo_referente.sql
2. ddl/ECST-57_createtable_maestros.medicamento_inactivo_referente_negociacion.sql
3. functions/maestros.sincronizar_medicamentos_inactivos_rpt_referente.sql
4. functions/maestros.sincronizar_medicamentos_inactivos_rpt_referente_negociacion.sql
5. functions/maestros.sincronizar_medicamentos_inactivos.sql
6. functions/soporte.fn_consulta_rpt_med_referente_inactivos.sql
7. functions/soporte.fn_consulta_rpt_med_referente_inactivos_negociacion.sql
8. functions/soporte.fn_sincronizar_medicamentos_inactivos_generar_reportes_ref.sql

NOTA:
* En caso de hacer un reverso, ejecutar lo siguiente:  rollbacks/ECST-57_rollbacks.sql
