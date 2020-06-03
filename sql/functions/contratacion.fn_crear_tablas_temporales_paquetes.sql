CREATE OR REPLACE FUNCTION contratacion.fn_crear_tablas_temporales_paquetes()
 RETURNS character varying
 LANGUAGE plpgsql
 SECURITY DEFINER
AS $function$
BEGIN
    CREATE TEMPORARY TABLE paquete_maximo_sede
    (
        sede_prestador_id integer NOT NULL,
        nombre_sede       character varying(200),
        codigo_paquete    character varying(20),
        maximo_paquete_id bigint
    ) ON COMMIT DROP;
    CREATE TEMPORARY TABLE temp_procedimientos
    (
        id                  integer NOT NULL,
        paquete_id          integer NOT NULL,
        procedimiento_id    integer NOT NULL,
        principal           boolean,
        etiqueta            character varying(20),
        cantidad            integer,
        cantidad_maxima     integer,
        cantidad_minima     integer,
        costo_unitario      double precision,
        costo_total         double precision,
        observacion         character varying(500),
        ingreso_aplica      character varying(5),
        ingreso_cantidad    numeric(10, 2),
        frecuencia_unidad   character varying(5),
        frecuencia_cantidad numeric(10, 2),
        tipo_carga          character varying(50),
        user_id             integer
    ) ON COMMIT DROP;
    CREATE TEMPORARY TABLE temp_medicamentos
    (
        id                  integer NOT NULL,
        portafolio_id       INTEGER NOT NULL,
        medicamento_id      INTEGER NOT NULL,
        codigo_interno      VARCHAR(20),
        valor               numeric,
        cantidad            integer,
        etiqueta            varchar(20),
        es_capita           boolean,
        cantidad_maxima     integer,
        cantidad_minima     integer,
        costo_unitario      double precision,
        costo_total         double precision,
        observacion         varchar(500),
        ingreso_aplica      varchar(5),
        ingreso_cantidad    numeric(10, 2),
        frecuencia_unidad   varchar(5),
        frecuencia_cantidad numeric(10, 2),
        en_negociacion      boolean,
        tipo_carga          varchar(50),
        user_id             integer
    ) ON COMMIT DROP;
    CREATE TEMPORARY TABLE temp_insumos
    (
        id                  integer NOT NULL,
        insumo_id           INTEGER NOT NULL,
        portafolio_id       integer NOT NULL,
        codigo_interno      VARCHAR(20),
        valor               INTEGER,
        cantidad            integer,
        etiqueta            varchar(20),
        observacion         varchar(500),
        cantidad_maxima     integer,
        cantidad_minima     integer,
        costo_unitario      double precision,
        costo_total         double precision,
        ingreso_aplica      varchar(5),
        ingreso_cantidad    numeric(10, 2),
        frecuencia_unidad   varchar(5),
        frecuencia_cantidad numeric(10, 2),
        tipo_carga          varchar(50),
        user_id             integer
    ) ON COMMIT DROP;
    RETURN 'OK';
END;
$function$
;
