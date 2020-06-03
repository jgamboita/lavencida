--

CREATE TABLE contratacion.solicitud_contratacion
(
  id serial NOT NULL,
  estado_solicitud character varying(100),
  observaciones character varying(500),
  fecha_creacion_solicitud timestamp(0) without time zone NOT NULL,
  fecha_inicio_prestacion timestamp(0) without time zone NOT NULL,
  justificacion_tecnica character varying(500) NOT NULL,
  concepto_capacidad character varying(500) NOT NULL,
  cubrimiento_geografico character varying(8000) NOT NULL,
  crear_nuevo_contrato boolean,
  otrosi boolean,
  terminacion_contrato boolean,
  version_detalle_negociacion_id integer NOT NULL,
  comite_contratacion_id integer,
  user_id integer NOT NULL,
  calidad character varying(500) NOT NULL,
  fecha_radicacion timestamp(0) without time zone,
  estado_parametrizacion_id integer,
  CONSTRAINT solicitud_contratacion_pkey PRIMARY KEY (id),
  CONSTRAINT fk_solicitud_contratacion_estado_parametrizacion FOREIGN KEY (estado_parametrizacion_id)
      REFERENCES contratacion.estado_parametrizacion (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.solicitud_contratacion
  OWNER TO postgres;
GRANT ALL ON TABLE contratacion.solicitud_contratacion TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.solicitud_contratacion TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.solicitud_contratacion TO gp_emssanar_lectura;
GRANT ALL ON TABLE contratacion.solicitud_contratacion TO app_emssanar_usuario_web;



-- View: contratacion.v_comite_contratacion

-- DROP VIEW contratacion.v_comite_contratacion;

CREATE OR REPLACE VIEW contratacion.v_comite_contratacion AS 
 SELECT cc.id,
    cc.fecha_comite,
    cc.fecha_recepcion_solicitudes,
    count(sc.id) AS numero_solicitudes,
    cc.estado,
    cc.en_gestion,
    cc.observacion
   FROM contratacion.comite_contratacion cc
     LEFT JOIN contratacion.solicitud_contratacion sc ON cc.id = sc.comite_contratacion_id
  GROUP BY cc.id, cc.fecha_comite, cc.fecha_recepcion_solicitudes, cc.estado, cc.en_gestion, cc.observacion;

ALTER TABLE contratacion.v_comite_contratacion
  OWNER TO postgres;
GRANT ALL ON TABLE contratacion.v_comite_contratacion TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.v_comite_contratacion TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.v_comite_contratacion TO gp_emssanar_lectura;
GRANT ALL ON TABLE contratacion.v_comite_contratacion TO app_emssanar_usuario_web;


-- View: contratacion.v_solicitud_contratacion

-- DROP VIEW contratacion.v_solicitud_contratacion;

CREATE OR REPLACE VIEW contratacion.v_solicitud_contratacion AS 
 SELECT sc.id AS numero_solicitud,
    n.id AS numero_necesidad,
    sc.fecha_creacion_solicitud,
    sc.fecha_radicacion,
    sc.estado_solicitud,
    n.prioridad_necesidad AS prioridad_id,
        CASE n.prioridad_necesidad
            WHEN 1 THEN 'Baja'::text
            WHEN 2 THEN 'Media'::text
            WHEN 3 THEN 'Alta'::text
            ELSE NULL::text
        END AS prioridad,
    tn.id AS tipo_necesidad_id,
    tn.descripcion AS tipo_necesidad,
    nn.modalidad,
    p.nombre AS prestador,
    reg.id AS regional_id,
    reg.descripcion AS regional,
    concat(
        CASE n.es_medicamentos
            WHEN false THEN ''::text
            ELSE 'Medicamentos - '::text
        END,
        CASE n.es_insumos
            WHEN false THEN ''::text
            ELSE 'Insumos - '::text
        END,
        CASE n.es_servicios
            WHEN false THEN ''::text
            ELSE 'Servicios - '::text
        END,
        CASE n.es_programa_esp
            WHEN false THEN ''::text
            ELSE 'Prog.Esp. - '::text
        END,
        CASE n.es_transporte
            WHEN false THEN ''::text
            ELSE 'Transporte'::text
        END) AS tipo_servicio_negociacion,
    sc.comite_contratacion_id,
    regnec.id AS regional_nec_id,
    regnec.descripcion AS regional_nec_desc,
    u.id AS usuario_creador_id,
    concepto_comite.justificacion AS justificacion_comite,
    nn.poblacion,
    cc.fecha_comite
   FROM contratacion.solicitud_contratacion sc
     JOIN contratacion.version_detalle_negociacion vdn ON vdn.id = sc.version_detalle_negociacion_id
     JOIN contratacion.negociacion_necesidad nn ON nn.id = vdn.negociacion_id
     JOIN contratacion.necesidad n ON n.id = nn.necesidad_id
     JOIN contratacion.prestador p ON p.id = nn.prestador_id
     JOIN security."user" u ON u.id = sc.user_id
     LEFT JOIN security.usuario_entidad ue ON ue.usuario_id = u.id
     LEFT JOIN maestros.regional reg ON reg.id = ue.regional_id
     JOIN contratacion.motivo_necesidad mot ON mot.id = n.motivo_id
     JOIN contratacion.tipo_necesidad tn ON tn.id = mot.tipo_necesidad_id
     JOIN security."user" u_n ON u_n.id = n.user_id_reg
     LEFT JOIN security.usuario_entidad ue2 ON ue2.usuario_id = u_n.id
     LEFT JOIN maestros.regional regnec ON regnec.id = ue2.regional_id
     LEFT JOIN contratacion.solicitud_contratacion_concepto_comite concepto_comite ON concepto_comite.solicitud_contratacion_id = sc.id
     LEFT JOIN contratacion.comite_contratacion cc ON cc.id = sc.comite_contratacion_id;

ALTER TABLE contratacion.v_solicitud_contratacion
  OWNER TO postgres;
GRANT ALL ON TABLE contratacion.v_solicitud_contratacion TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.v_solicitud_contratacion TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.v_solicitud_contratacion TO gp_emssanar_lectura;
GRANT ALL ON TABLE contratacion.v_solicitud_contratacion TO app_emssanar_usuario_web;

-- Table: contratacion.solicitud_contratacion







-- Nueva tabla de solicitud contratacion.


