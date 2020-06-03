Buen dia!!!

Seguir este orden de ejecucion de scripts:

1. ECST-40_altertable_contratacion.negociacion.sql
2. ECST-40_altertable_contratacion.negociacion_referente.sql
3. contratacion.fn_copiar_paquete_por_negociacion.sql
4. contratacion.fn_actualizar_insumos_con_parametrizador.sql
5. contratacion.fn_actualizar_medicamentos_con_parametrizador.sql
6. contratacion.fn_actualizar_procedimientos_con_parametrizador.sql
7. contratacion.fn_copiar_insumos_por_paquete.sql
8. contratacion.fn_copiar_medicamentos_por_paquete.sql
9. contratacion.fn_copiar_procedimientos_por_paquete.sql

NOTA:
* En caso de hacer un reverso, ejecutar lo siguiente:  rollbacks/ECTS-40_rollbacks.sql
