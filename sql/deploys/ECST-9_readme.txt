Buen dia!!!

Seguir este orden de ejecucion de scripts:

1. ddl/ECST-9_altertable_contratacion_negociacion.sql
2. ddl/ECST-9_altertable_contratacion_contrato.sql
3. ddl/ECST-9_altertable_contratacion_legalizacion_contrato.sql
4. ddl/ECST-9_altertable_contratacion_sede_negociacion_servicio.sql
5. ddl/ECST-9_altertable_contratacion_sede_negociacion_medicamento.sql
6. ddl/ECST-9_altertable_contratacion_sede_negociacion_paquete.sql
7. ddl/ECST-9_altertable_contratacion_sede_negociacion_procedimiento.sql
8. functions/contratacion.validacion_es_otro_si.sql
9. functions/contratacion.insertar_contrato.sql
10. functions/contratacion.insertar_legalizacion_contrato.sql
11. ddl/ECST-9_create_trigger_solicitud_contratacion_insertar_contrato.sql
12. ddl/ECST-9_create_trigger_contrato_insertar_legalizacion_contrato.sql
13. functions/contratacion.sp_find_legalizacion_contrato_by_solicitud_contratacion.sql
14. dml/ECST-9_insert_table_configuracion.properties.sql   --> (Leer el punto 0)

NOTA:
0. De acuerdo al ambiente de que se vaya a ejecutar, habilitar la seccion en el script,
ya q en la parte superior dice el ambiente.

* En caso de hacer un reverso, ejecutar lo siguiente:
1. rollbacks/ECST-9_rollbacks.sql

