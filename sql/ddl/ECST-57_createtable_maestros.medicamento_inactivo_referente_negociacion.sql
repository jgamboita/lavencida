
create table maestros.medicamento_inactivo_referente_negociacion
(
    negociacion_id             integer,
    referente                  varchar(100),
    codigo_prestador           varchar(10),
    numero_documento           varchar(14),
    prestador                  varchar(300),
    tipo_modalidad_negociacion varchar(100),
    estado_negociacion         varchar(100),
    regimen                    varchar(100),
    numero_contrato            varchar(100),
    fecha_inicio               date,
    fecha_fin                  date,
    estado_legalizacion        varchar(30),
    nombre_sede                varchar(200),
    codigo_sede                varchar(60),
    codigo_medicamento         varchar(20)
);
