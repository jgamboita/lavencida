
create or replace function maestros.sincronizar_medicamentos_activos()
    returns TABLE
            (
                var_registrados  integer,
                var_actualizados integer
            )
    language plpgsql
as
$$
BEGIN

    /*Este metodo sincroniza los medicamentos de la maestra que otorga por el area de Salud
     * este archivo se guarda como csv por hoja, en esta función se maneja la hoja de los medicamentos activos
     * que se debe guardar en la ruta: /archivos_adjuntos/archivos_novedades/medicamento_activo.csv*/


    /*Se inactivan todos los medicamentos, las formulas magistrales se maneja con
     * el proceso de sincronización de procedimientos ya que Emssanar es el que define
     * por medio de un campo en la tabla dbo.TEMP_TECNOLOGIA en la BD de SQL Server de Emssanar
    * las formulas.
    * */
    /**
     * solo quedan activos las tecmnologias que son comodines
     * para contratacion
     */
    UPDATE maestros.medicamento m
    SET estado_medicamento_id = 2
    WHERE NOT EXISTS
        (
            SELECT medicamento_id
            FROM maestros.medicamento_comodin_contratacion
            WHERE medicamento_id = m.id
        );


    --Registro Medicamentos Nuevos
    INSERT INTO maestros.medicamento
    (codigo,
     descripcion,
     nombre_alterno,
     estado_medicamento_id,
     genero_id,
     tipo_ppm_id,
     nivel_autorizacion,
     es_comercial,
     edad_ini,
     edad_fin,
     es_recobrable,
     atc,
     concentracion,
     forma_farmaceutica,
     descripcion_atc,
     descripcion_invima,
     estado_registro,
     expediente,
     principio_activo,
     registro_sanitario,
     titular_registro,
     via_administracion,
     es_muestra_medica,
     estado_cum,
     fecha_activo,
     fecha_expedicion,
     fecha_inactivo,
     categoria_id,
     fecha_vencimiento,
     regulado,
     uso_institucional,
     lista_unirs,
     clasificacion_pbs_condicionado,
     codigo_dci,
     valor_maximo_regulado,
     valor_por_fraccion,
     nombre_dci,
     fecha_insert,
     fecha_update,
     "version",
     es_insumo,
     es_quirurgico,
     sin_auditoria_nacional,
     norma_vmr,
     vmr,
     valor_presentacion_comercial,
     valor_unidad
     )
    SELECT ma.cum_ajustado                               AS codigo,
           ma.descripcion_presentacion_comercial_invima  AS descripcion,
           ma.descripcion_estandarizada                  AS nombre_alterno,
           1                                             AS estado_medicamento_id, --Activo
           1                                             AS genero_id,             -- Siempre es Ambos
           CASE
               WHEN upper(ma.clasificacion_plan) = 'NO PBS' THEN 2
               WHEN upper(ma.clasificacion_plan) = 'PBS' THEN 1
               WHEN upper(ma.clasificacion_plan) = 'PBS CONDICIONADO' THEN 4
               ELSE 0
               END                                       AS tipo_ppm_id,
           CASE
               WHEN upper(ma.clasificacion_plan) = 'NO PBS' THEN 4
               WHEN upper(ma.clasificacion_plan) = 'PBS' THEN 2
               WHEN upper(ma.clasificacion_plan) = 'PBS CONDICIONADO' THEN 4
               ELSE 2
               END                                       AS nivel_autorizacion,
           CASE
               WHEN ma.generico_nombre_comercial = 'GENERICO' THEN 0
               ELSE 1
               END                                       AS es_comercial,
           0                                             AS edad_ini,              --Se pone siempre en 0 así estaba en Talend
           54750                                         AS edad_fin,              --Se pone siempre 54750 así estaba en Talend
           CASE
               WHEN upper(ma.clasificacion_plan) IN ('NO PBS', 'PBS CONDICIONADO') THEN 1::bit
               ELSE 0::bit
               END                                       AS es_recobrable,
           ma.atc_1                                      AS atc,
           ma.concentracion                              AS concentracion,
           ma.forma_farmaceutica                         AS forma_farmaceutica,
           ma.descripcion_atc_1                          AS descripcion_atc,
           ma.descripcion_invima                         AS descripcion_invima,
           ma.estado_del_registro                        AS estado_registro,
           ma.expediente                                 AS expediente,
           ma.principio_activo                           AS principio_activo,
           ma.registro_sanitario                         AS registro_sanitario,
           ma.laboratorio_titular_del_registro_sanitario AS titular_registro,
           ma.vias_de_administracion                     AS via_administracion,
           FALSE                                         AS es_muestra_medica,     --Siempre se pone falso
           1                                             AS estado_cum,
           TO_DATE(ma.fecha_activo, 'MM/dd/yyyy')        AS fecha_activo,
           TO_DATE(ma.fecha_expedicion, 'MM/dd/yyyy')    AS fecha_expedicion,
           TO_DATE(ma.fecha_inactivo, 'MM/dd/yyyy')      AS fecha_inactivo,
           cat.id                                        AS categoria_id,
           TO_DATE(ma.fecha_vencimiento, 'MM/dd/yyyy')   AS fecha_vencimiento,
           CASE
               WHEN ma.medicamento_regulado = 'SI' THEN TRUE
               ELSE FALSE
               END                                       AS regulado,
           CASE
               WHEN ma.proveniente_invima_uso_institucional_hospitalario = 'SI' THEN TRUE
               ELSE FALSE
               END                                       AS uso_institucional,
           ma.lista_unirs                                AS lista_unirs,
           ma.agrupador_de_condicion_pbs                 AS clasificacion_pbs_condicionado,
           ma.cod_medicamentos_dci                       AS codigo_dci,
           ma.valor_maximo_regulado::NUMERIC             AS valor_maximo_regulado,
           ma.valor_por_fraccion::NUMERIC                AS valor_por_fraccion,
           ma.descripcion_atc_1                          AS nombre_dci,
           now()                                         AS fecha_insert,
           now()                                         AS fecha_update,
           1                                             AS "version",
           0                                             AS es_insumo,             --Siempre es no
           0::BIT                                        AS es_quirurgico,
           1::BIT                                        AS sin_auditoria_nacional,
           ma.norma_vmr,
           ma.vmr,
           ma.valor_presentacion_comercial::decimal,
           ma.valor_unidad::decimal
    FROM maestros.medicamento_activo ma
             LEFT JOIN maestros.medicamento me ON (me.codigo = ma.cum_ajustado)
             JOIN contratacion.categoria_medicamento cat ON (cat.codigo = ma.grupo_terapeutico_principal)
    WHERE me.id IS NULL;

    GET DIAGNOSTICS var_registrados = ROW_COUNT;


    --Actualizacion de medicamentos
    UPDATE maestros.medicamento me
    SET codigo                         = ma.cum_ajustado,
        descripcion                    = CASE
                                             WHEN ma.descripcion_presentacion_comercial_invima IS NULL THEN ''
                                             ELSE ma.descripcion_presentacion_comercial_invima END,
        nombre_alterno                 = ma.descripcion_estandarizada,
        estado_medicamento_id          = 1,
        genero_id                      = 1,
        tipo_ppm_id                    = CASE
                                             WHEN upper(ma.clasificacion_plan) = 'NO PBS' THEN 2
                                             WHEN upper(ma.clasificacion_plan) = 'PBS' THEN 1
                                             WHEN upper(ma.clasificacion_plan) = 'PBS CONDICIONADO' THEN 4
                                             ELSE 0 END,
        nivel_autorizacion             = CASE
                                             WHEN upper(ma.clasificacion_plan) = 'NO PBS' THEN 4
                                             WHEN upper(ma.clasificacion_plan) = 'PBS' THEN 2
                                             WHEN upper(ma.clasificacion_plan) = 'PBS CONDICIONADO' THEN 4
                                             ELSE 2 END,
        es_comercial                   = CASE WHEN ma.generico_nombre_comercial = 'GENERICO' THEN 0 ELSE 1 END,
        edad_ini                       = 0,
        edad_fin                       = 54750,
        es_recobrable                  = CASE
                                             WHEN upper(ma.clasificacion_plan) IN ('NO PBS', 'PBS CONDICIONADO') THEN 1::bit
                                             ELSE 0::bit END,
        atc                            = ma.atc_1,
        concentracion                  = ma.concentracion,
        forma_farmaceutica             = ma.forma_farmaceutica,
        descripcion_atc                = ma.descripcion_atc_1,
        descripcion_invima             = ma.descripcion_invima,
        estado_registro                = ma.estado_del_registro,
        expediente                     = ma.expediente,
        principio_activo               = ma.principio_activo,
        registro_sanitario             = ma.registro_sanitario,
        titular_registro               = ma.laboratorio_titular_del_registro_sanitario,
        via_administracion             = ma.vias_de_administracion,
        es_muestra_medica              = FALSE,
        estado_cum                     = 1,
        fecha_activo                   = TO_DATE(ma.fecha_activo, 'MM/dd/yyyy'),
        fecha_expedicion               = TO_DATE(ma.fecha_expedicion, 'MM/dd/yyyy'),
        fecha_inactivo                 = TO_DATE(ma.fecha_inactivo, 'MM/dd/yyyy'),
        categoria_id                   = cat.id,
        fecha_vencimiento              = TO_DATE(ma.fecha_vencimiento, 'MM/dd/yyyy'),
        regulado                       = CASE WHEN ma.medicamento_regulado = 'SI' THEN TRUE ELSE FALSE END,
        uso_institucional              = CASE
                                             WHEN ma.proveniente_invima_uso_institucional_hospitalario = 'SI' THEN TRUE
                                             ELSE FALSE END,
        lista_unirs                    = ma.lista_unirs,
        clasificacion_pbs_condicionado = ma.agrupador_de_condicion_pbs,
        codigo_dci                     = ma.cod_medicamentos_dci,
        valor_maximo_regulado          = ma.valor_maximo_regulado::integer,
        valor_por_fraccion             = ma.valor_por_fraccion::integer,
        nombre_dci                     = ma.descripcion_atc_1,
        fecha_insert                   = now(),
        fecha_update                   = now(),
        "version"                      = 1,
        es_insumo                      = 0,
        es_quirurgico                  = 0::BIT,
        sin_auditoria_nacional         = 1::BIT,
        valor_referente_minimo         = ma.valor_por_fraccion::integer,
        valor_referente                = CASE WHEN ma.medicamento_regulado = 'SI' THEN ma.valor_maximo_regulado::integer ELSE me.valor_referente END,
        ma.norma_vmr = ma.norma_vmr,
        ma.vmr = ma.vmr,
        ma.valor_presentacion_comercial = ma.valor_presentacion_comercial::decimal,
        ma.valor_unidad = ma.valor_unidad::decimal
    FROM maestros.medicamento_activo ma
             JOIN contratacion.categoria_medicamento cat ON (cat.codigo = ma.grupo_terapeutico_principal)
    WHERE ma.cum_ajustado = me.codigo;

    GET DIAGNOSTICS var_actualizados = ROW_COUNT;

    select * from prevalidacion_rips.fn_cargue_mensual_historico_valor_recobro();

    RETURN query SELECT var_registrados, var_actualizados;

END
$$;



