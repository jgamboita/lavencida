-- Borrar vistas.

DROP VIEW contratacion.v_comite_contratacion;
DROP VIEW contratacion.v_solicitud_contratacion;

DROP TABLE contratacion.solicitud_contratacion;

-- Sequence: contratacion.contrato_id_seq

-- DROP SEQUENCE contratacion.contrato_id_seq;

CREATE SEQUENCE contratacion.contrato_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE contratacion.contrato_id_seq
  OWNER TO postgres;
GRANT ALL ON SEQUENCE contratacion.contrato_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.contrato_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.contrato_id_seq TO gp_emssanar_lectura;
GRANT ALL ON SEQUENCE contratacion.contrato_id_seq TO app_emssanar_usuario_web;

-- Table: contratacion.contrato

-- DROP TABLE contratacion.contrato;

CREATE TABLE contratacion.contrato
(
  id bigint NOT NULL DEFAULT nextval('contratacion.contrato_id_seq'::regclass),
  numero_contrato character varying(100) NOT NULL,
  fecha_inicio timestamp without time zone NOT NULL,
  fecha_fin timestamp without time zone NOT NULL,
  regimen character varying(50) NOT NULL,
  tipo_modalidad character varying(50) NOT NULL,
  user_id integer NOT NULL,
  fecha_creacion timestamp without time zone NOT NULL,
  solicitud_contratacion_id bigint NOT NULL,
  CONSTRAINT pk_contrato_id PRIMARY KEY (id),
  CONSTRAINT fk_contrato_solicitud_contratacion FOREIGN KEY (solicitud_contratacion_id)
      REFERENCES contratacion.solicitud_contratacion (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_contrato_usuario FOREIGN KEY (user_id)
      REFERENCES security."user" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.contrato OWNER TO postgres;
GRANT ALL ON TABLE contratacion.contrato TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.contrato TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.contrato TO gp_emssanar_lectura;
GRANT ALL ON TABLE contratacion.contrato TO app_emssanar_usuario_web;


-- Sequence: contratacion.minuta_id_seq

-- DROP SEQUENCE contratacion.minuta_id_seq;

CREATE SEQUENCE contratacion.minuta_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE contratacion.minuta_id_seq
  OWNER TO postgres;
GRANT ALL ON SEQUENCE contratacion.minuta_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.minuta_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.minuta_id_seq TO gp_emssanar_lectura;
GRANT ALL ON SEQUENCE contratacion.minuta_id_seq TO app_emssanar_usuario_web;


-- Table: contratacion.minuta

-- DROP TABLE contratacion.minuta;

CREATE TABLE contratacion.minuta
(
  id integer NOT NULL DEFAULT nextval('contratacion.minuta_id_seq'::regclass),
  descripcion character varying(500) NOT NULL,
  modalidad_id integer NOT NULL,
  nombre character varying NOT NULL,
  tramite_id integer NOT NULL,
  estado_id integer NOT NULL,
  CONSTRAINT pk_minuta_id PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.minuta OWNER TO postgres;
GRANT ALL ON TABLE contratacion.minuta TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.minuta TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.minuta TO gp_emssanar_lectura;
GRANT ALL ON TABLE contratacion.minuta TO app_emssanar_usuario_web;


-- Function: insertar_variables_minuta()

-- DROP FUNCTION contratacion.insertar_variables_minuta();

CREATE OR REPLACE FUNCTION contratacion.insertar_variables_minuta()
  RETURNS trigger AS
$BODY$
  BEGIN INSERT INTO contratacion.minuta_variable SELECT NEW.id, id FROM contratacion.variable;
       RETURN NEW;
  END;
$BODY$
  LANGUAGE plpgsql VOLATILE
  COST 100;
ALTER FUNCTION contratacion.insertar_variables_minuta()
  OWNER TO postgres;


-- Trigger: insertar_variables_minuta on contratacion.minuta

-- DROP TRIGGER insertar_variables_minuta ON contratacion.minuta;

CREATE TRIGGER insertar_variables_minuta
  AFTER INSERT
  ON contratacion.minuta
  FOR EACH ROW
  EXECUTE PROCEDURE contratacion.insertar_variables_minuta();


-- Sequence: contratacion.contenido_minuta_id_seq

-- DROP SEQUENCE contratacion.contenido_minuta_id_seq;

CREATE SEQUENCE contratacion.contenido_minuta_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE contratacion.contenido_minuta_id_seq
  OWNER TO postgres;
GRANT ALL ON SEQUENCE contratacion.contenido_minuta_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.contenido_minuta_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.contenido_minuta_id_seq TO gp_emssanar_lectura;
GRANT ALL ON SEQUENCE contratacion.contenido_minuta_id_seq TO app_emssanar_usuario_web;



-- Table: contratacion.contenido_minuta

-- DROP TABLE contratacion.contenido_minuta;

CREATE TABLE contratacion.contenido_minuta
(
  id integer NOT NULL DEFAULT nextval('contratacion.contenido_minuta_id_seq'::regclass),
  descripcion character varying(200) NOT NULL,
  nivel integer NOT NULL,
  unico boolean NOT NULL DEFAULT true,
  icono character varying(30),
  tiene_hijos boolean DEFAULT true,
  CONSTRAINT pk_contenido_minuta_id PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.contenido_minuta OWNER TO postgres;
GRANT ALL ON TABLE contratacion.contenido_minuta TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.contenido_minuta TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.contenido_minuta TO gp_emssanar_lectura;
GRANT ALL ON TABLE contratacion.contenido_minuta TO app_emssanar_usuario_web;


-- Sequence: contratacion.detalle_minuta_id_seq

-- DROP SEQUENCE contratacion.detalle_minuta_id_seq;

CREATE SEQUENCE contratacion.minuta_detalle_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE contratacion.minuta_detalle_id_seq
  OWNER TO postgres;
GRANT ALL ON SEQUENCE contratacion.minuta_detalle_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.minuta_detalle_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.minuta_detalle_id_seq TO gp_emssanar_lectura;
GRANT ALL ON SEQUENCE contratacion.minuta_detalle_id_seq TO app_emssanar_usuario_web;

-- Table: contratacion.minuta_detalle

-- DROP TABLE contratacion.minuta_detalle;

CREATE TABLE contratacion.minuta_detalle
(
  id integer NOT NULL DEFAULT nextval('contratacion.minuta_detalle_id_seq'::regclass),
  minuta_id integer NOT NULL,
  contenido_minuta_id integer NOT NULL,
  ordinal integer NOT NULL,
  padre_id bigint,
  titulo character varying NOT NULL,
  descripcion text NOT NULL,
  user_id bigint NOT NULL,
  fecha_registro timestamp without time zone NOT NULL,
  CONSTRAINT pk_minuta_contenido PRIMARY KEY (id),
  CONSTRAINT fk_minuta_detalle_contenido_minuta FOREIGN KEY (contenido_minuta_id)
      REFERENCES contratacion.contenido_minuta (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_minuta_detalle_minuta FOREIGN KEY (minuta_id)
      REFERENCES contratacion.minuta (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_minuta_detalle_padre FOREIGN KEY (padre_id)
      REFERENCES contratacion.minuta_detalle (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.minuta_detalle
  OWNER TO postgres;
GRANT ALL ON TABLE contratacion.minuta_detalle TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.minuta_detalle TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.minuta_detalle TO gp_emssanar_lectura;
GRANT ALL ON TABLE contratacion.minuta_detalle TO app_emssanar_usuario_web;




-- Sequence: contratacion.variable

-- DROP SEQUENCE contratacion.variable_id_seq;

CREATE SEQUENCE contratacion.variable_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE contratacion.variable_id_seq
  OWNER TO postgres;
GRANT ALL ON SEQUENCE contratacion.variable_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.variable_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.variable_id_seq TO gp_emssanar_lectura;
GRANT ALL ON SEQUENCE contratacion.variable_id_seq TO app_emssanar_usuario_web;



-- Table: contratacion.variable

-- DROP TABLE contratacion.variable;

CREATE TABLE contratacion.variable
(
  id integer NOT NULL,
  codigo character varying(25000) NOT NULL,
  descripcion character varying(200) NOT NULL,
  tipo_variable_id integer NOT NULL,
  CONSTRAINT pk_variable PRIMARY KEY (id),
  CONSTRAINT uk_variable UNIQUE (codigo)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.variable OWNER TO postgres;
GRANT ALL ON TABLE contratacion.variable TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.variable TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.variable TO gp_emssanar_lectura;
GRANT ALL ON TABLE contratacion.variable TO app_emssanar_usuario_web;





-- Sequence: contratacion.minuta_variable_id_seq

-- DROP SEQUENCE contratacion.minuta_variable_id_seq;

CREATE SEQUENCE contratacion.minuta_variable_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE contratacion.minuta_variable_id_seq
  OWNER TO postgres;
GRANT ALL ON SEQUENCE contratacion.minuta_variable_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.minuta_variable_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.minuta_variable_id_seq TO gp_emssanar_lectura;
GRANT ALL ON SEQUENCE contratacion.minuta_variable_id_seq TO app_emssanar_usuario_web;


-- Table: contratacion.minuta_variable

-- DROP TABLE contratacion.minuta_variable;


CREATE TABLE contratacion.minuta_variable
(
  minuta_id integer NOT NULL,
  variable_id integer NOT NULL,
  CONSTRAINT pk_minuta_variable PRIMARY KEY (minuta_id, variable_id),
  CONSTRAINT fk_minuta_variable_minuta FOREIGN KEY (minuta_id)
      REFERENCES contratacion.minuta (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_minuta_variable_variable FOREIGN KEY (variable_id)
      REFERENCES contratacion.variable (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.minuta_variable OWNER TO postgres;
GRANT ALL ON TABLE contratacion.minuta_variable TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.minuta_variable TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.minuta_variable TO gp_emssanar_lectura;
GRANT ALL ON TABLE contratacion.minuta_variable TO app_emssanar_usuario_web;




-- Sequence: contratacion.solicitud_contratacion_id_seq

-- DROP SEQUENCE contratacion.solicitud_contratacion_id_seq;

CREATE SEQUENCE contratacion.solicitud_contratacion_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 22
  CACHE 1;
ALTER TABLE contratacion.solicitud_contratacion_id_seq
  OWNER TO postgres;
GRANT ALL ON SEQUENCE contratacion.solicitud_contratacion_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.solicitud_contratacion_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.solicitud_contratacion_id_seq TO gp_emssanar_lectura;
GRANT ALL ON SEQUENCE contratacion.solicitud_contratacion_id_seq TO app_emssanar_usuario_web;


-- Table: contratacion.solicitud_contratacion

-- DROP TABLE contratacion.solicitud_contratacion;

CREATE TABLE contratacion.solicitud_contratacion
(
  id bigint NOT NULL DEFAULT nextval('contratacion.solicitud_contratacion_id_seq'::regclass),
  fecha_creacion_solicitud timestamp(0) without time zone NOT NULL,
  prestador_id integer,
  tipo_modalidad_negociacion character varying(100),
  nuevo_contrato boolean,
  user_id integer NOT NULL,
  estado_parametrizacion_id character varying(20),
  negociacion_id bigint,
  estado_legalizacion_id character varying(30),
  CONSTRAINT solicitud_contratacion_pkey PRIMARY KEY (id),
  CONSTRAINT fk_solicitud_contratacion_negociacion FOREIGN KEY (negociacion_id)
      REFERENCES contratacion.negociacion (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT uk_solicitud_contratacion_negociacion UNIQUE (negociacion_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.solicitud_contratacion OWNER TO postgres;
GRANT ALL ON TABLE contratacion.solicitud_contratacion TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.solicitud_contratacion TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.solicitud_contratacion TO gp_emssanar_lectura;
GRANT ALL ON TABLE contratacion.solicitud_contratacion TO app_emssanar_usuario_web;

  

-- Sequence: contratacion.solicitud_contratacion_sede_id_seq

-- DROP SEQUENCE contratacion.solicitud_contratacion_sede_id_seq;

CREATE SEQUENCE contratacion.solicitud_contratacion_sede_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 1
  CACHE 1;
ALTER TABLE contratacion.solicitud_contratacion_sede_id_seq
  OWNER TO postgres;
GRANT ALL ON SEQUENCE contratacion.solicitud_contratacion_sede_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.solicitud_contratacion_sede_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.solicitud_contratacion_sede_id_seq TO gp_emssanar_lectura;
GRANT ALL ON SEQUENCE contratacion.solicitud_contratacion_sede_id_seq TO app_emssanar_usuario_web;

-- Table: contratacion.solicitud_contratacion_sede

-- DROP TABLE contratacion.solicitud_contratacion_sede;

CREATE TABLE contratacion.solicitud_contratacion_sede
(
  id bigint NOT NULL DEFAULT nextval('contratacion.solicitud_contratacion_sede_id_seq'::regclass),
  estado_parametrizacion_id character varying(20),
  user_id integer NOT NULL,
  fecha_estado timestamp(0) without time zone NOT NULL,
  solicitud_contratacion_id bigint,
  sede_negociacion_id bigint,
  CONSTRAINT solicitud_contratacion_sede_pkey PRIMARY KEY (id),
  CONSTRAINT "FK_solicitud_contratacion_sede_negociacion" FOREIGN KEY (sede_negociacion_id)
      REFERENCES contratacion.sedes_negociacion (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT "FK_solicitud_contratacion_sede_solicitud_contratacion" FOREIGN KEY (solicitud_contratacion_id)
      REFERENCES contratacion.solicitud_contratacion (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.solicitud_contratacion_sede  OWNER TO postgres;
GRANT ALL ON TABLE contratacion.solicitud_contratacion_sede TO postgres;
GRANT SELECT ON TABLE contratacion.solicitud_contratacion_sede TO gp_emssanar_lectura;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.solicitud_contratacion_sede TO gp_emssanar_escritura;



