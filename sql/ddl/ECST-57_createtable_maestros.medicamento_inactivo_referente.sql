
create table maestros.medicamento_inactivo_referente
(
    referente             varchar(100),
    medicamento_id        integer,
    codigo                varchar(20),
    descripcion           varchar(1500),
    nombre_alterno        text,
    estado_medicamento_id integer,
    ruta                  varchar(100),
    actividad             varchar(300),
    poblacion             varchar(100)
);
