Buen dia!!!

Seguir este orden de ejecucion de scripts:

1. ECST-34_createtable_contratacion.negociacion_referente.sql
2. ECST-34_altertable_contratacion.negociacion.sql
3. ECST-34_altertable_contratacion.sede_negociacion_medicamento.sql
4. contratacion.fn_actualizar_negociacion_serv_proced.sql
5. contratacion.fn_aplicar_tarifas_contrato_by_negociacion_servicios_proced.sql
6. contratacion.fn_aplicar_valor_contrato_anterior_by_negociacion_medicamentos.sql
7. contratacion.fn_cumple_nivel_complejidad.sql
8. contratacion.fn_insertar_negociacion_referente.sql
9. contratacion.fn_copiar_medicamentos_por_negociacion.sql
10. contratacion.fn_copiar_paquete_por_negociacion.sql
12. contratacion.fn_copiar_procedimientos_por_negociacion.sql
13. contratacion.fn_revertir_copiado_medicamentos_por_negociacion.sql
14. contratacion.fn_revertir_copiado_paquetes_por_negociacion.sql
15. contratacion.fn_revertir_copiado_procedimientos_por_negociacion.sql
16. contratacion.fn_crear_tablas_temporales_paquetes.sql
17. ECST-34_grant_contratacion.negociacion_referente.sql

NOTA:
* En caso de hacer un reverso, ejecutar lo siguiente:  rollbacks/ECTS-34_rollbacks.sql
