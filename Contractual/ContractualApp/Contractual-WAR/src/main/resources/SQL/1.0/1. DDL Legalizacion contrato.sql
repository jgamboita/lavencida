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
  poblacion integer NOT NULL,
  regional_id integer NOT NULL,
  tipo_subsidiado character varying(50) NOT NULL,
  tipo_contrato character varying(50) NOT NULL,
  duracion_meses integer NOT NULL,
  duracion_dias integer NOT NULL,
  nivel_contrato character varying(50) NOT NULL,
  CONSTRAINT pk_contrato_id PRIMARY KEY (id),
  CONSTRAINT fk_contrato_regional FOREIGN KEY (regional_id)
      REFERENCES maestros.regional (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
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
ALTER TABLE contratacion.contrato
  OWNER TO postgres;
GRANT ALL ON TABLE contratacion.contrato TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.contrato TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.contrato TO gp_emssanar_lectura;
GRANT ALL ON TABLE contratacion.contrato TO app_emssanar_usuario_web;

ALTER TABLE contratacion.contrato_id_seq   OWNER TO postgres;
GRANT ALL ON SEQUENCE contratacion.contrato_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.contrato_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.contrato_id_seq TO gp_emssanar_lectura;
GRANT ALL ON SEQUENCE contratacion.contrato_id_seq TO app_emssanar_usuario_web;

-- Table: contratacion.responsable_contrato

-- DROP TABLE contratacion.responsable_contrato;

CREATE TABLE contratacion.responsable_contrato
(
  id serial NOT NULL,
  nombre character varying(200) NOT NULL,
  tipo_responsable character varying(50) NOT NULL,
  regional_id integer,
  CONSTRAINT pk_responsable_contrato_id PRIMARY KEY (id),
  CONSTRAINT fk_responsable_contrato_regional FOREIGN KEY (regional_id)
      REFERENCES maestros.regional (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.responsable_contrato
  OWNER TO postgres;
GRANT ALL ON TABLE contratacion.responsable_contrato TO app_emssanar_dgarcia;
GRANT ALL ON TABLE contratacion.responsable_contrato TO app_emssanar_usuario_web;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.responsable_contrato TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.responsable_contrato TO gp_emssanar_lectura;

ALTER TABLE contratacion.responsable_contrato_id_seq OWNER TO postgres;
GRANT ALL ON SEQUENCE contratacion.responsable_contrato_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.responsable_contrato_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.responsable_contrato_id_seq TO gp_emssanar_lectura;
GRANT ALL ON SEQUENCE contratacion.responsable_contrato_id_seq TO app_emssanar_usuario_web;

-- Table: contratacion.objeto_contrato

-- DROP TABLE contratacion.objeto_contrato;

CREATE TABLE contratacion.objeto_contrato
(
  id serial NOT NULL,
  descripcion character varying(200),
  CONSTRAINT pk_objeto_contrato_id PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.objeto_contrato
  OWNER TO postgres;
GRANT ALL ON TABLE contratacion.objeto_contrato TO app_emssanar_usuario_web;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.objeto_contrato TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.objeto_contrato TO gp_emssanar_lectura;

ALTER TABLE contratacion.objeto_contrato_id_seq   OWNER TO postgres;
GRANT ALL ON SEQUENCE contratacion.objeto_contrato_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.objeto_contrato_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.objeto_contrato_id_seq TO gp_emssanar_lectura;
GRANT SELECT, USAGE ON SEQUENCE contratacion.objeto_contrato_id_seq TO app_emssanar_usuario_web;

INSERT INTO contratacion.objeto_contrato (id, descripcion) 
	VALUES (nextval('contratacion.objeto_contrato_id_seq'), 'PRESTACION DE SERVICIOS DE SALUD');
INSERT INTO contratacion.objeto_contrato (id, descripcion) 
	VALUES (nextval('contratacion.objeto_contrato_id_seq'), 'SUMINISTRO DE MEDICAMENTOS');
INSERT INTO contratacion.objeto_contrato (id, descripcion) 
	VALUES (nextval('contratacion.objeto_contrato_id_seq'), 'SUMINISTRO DE INSUMOS - MATERIALES - DISPOSITIVOS Y SUMINISTROS BIOMEDICOS');
INSERT INTO contratacion.objeto_contrato (id, descripcion) 
	VALUES (nextval('contratacion.objeto_contrato_id_seq'), 'SUMINISTRO DE LENTES Y MONTURAS');
INSERT INTO contratacion.objeto_contrato (id, descripcion) 
	VALUES (nextval('contratacion.objeto_contrato_id_seq'), 'PROMOCION Y FOMENTO DE LA SALUD');



-- Table: contratacion.legalizacion_contrato

-- DROP TABLE contratacion.legalizacion_contrato;

CREATE TABLE contratacion.legalizacion_contrato
(
  id bigserial NOT NULL,
  prestador_id integer NOT NULL,
  municipio_id integer NOT NULL, -- Municipio del prestador que realiza la legalizacion.
  direccion character varying(500),
  contrato_id bigint NOT NULL, -- Id del contrato.
  minuta_id integer NOT NULL,
  objeto_contrato_id integer NOT NULL,
  valor_fiscal double precision NOT NULL,
  valor_poliza integer, -- Medido en SMLV, de acuerdo al formato seleccionado se debe ingresar el campo
  dias_plazo integer, -- Esta asociado al valor de la poliza y solo es equerido si se ingresa la valor poliza. El Plazo Máximo es de 90 días
  municipio_responsable_id integer NOT NULL, -- Municipios asociados a donde se realiza la firma de la minuta.
  fecha_firma_contrato timestamp without time zone NOT NULL, -- Fecha en la que se firma el contrato
  fecha_vo_bo timestamp without time zone NOT NULL,
  responsable_firma_contrato_id integer NOT NULL,
  responsable_vo_bo integer NOT NULL,
  usuario_id integer NOT NULL,
  CONSTRAINT pk_legalizacion_contrato_id PRIMARY KEY (id),
  CONSTRAINT fk_legalizacion_contrato_contrato FOREIGN KEY (contrato_id)
      REFERENCES contratacion.contrato (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_legalizacion_contrato_minuta FOREIGN KEY (minuta_id)
      REFERENCES contratacion.minuta (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_legalizacion_contrato_municipio FOREIGN KEY (municipio_id)
      REFERENCES maestros.municipio (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_legalizacion_contrato_municipio_responsable FOREIGN KEY (municipio_responsable_id)
      REFERENCES maestros.municipio (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_legalizacion_contrato_objeto_contrato FOREIGN KEY (objeto_contrato_id)
      REFERENCES contratacion.objeto_contrato (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_legalizacion_contrato_prestador FOREIGN KEY (prestador_id)
      REFERENCES contratacion.prestador (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_legalizacion_contrato_responsable_firma FOREIGN KEY (responsable_firma_contrato_id)
      REFERENCES contratacion.responsable_contrato (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_legalizacion_contrato_responsable_vobo FOREIGN KEY (responsable_vo_bo)
      REFERENCES contratacion.responsable_contrato (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_legalizacion_contrato_usuario FOREIGN KEY (usuario_id)
      REFERENCES security."user" (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.legalizacion_contrato
  OWNER TO postgres;
GRANT ALL ON TABLE contratacion.legalizacion_contrato TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.legalizacion_contrato TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.legalizacion_contrato TO gp_emssanar_lectura;
GRANT ALL ON TABLE contratacion.legalizacion_contrato TO app_emssanar_usuario_web;
COMMENT ON COLUMN contratacion.legalizacion_contrato.municipio_id IS 'Municipio del prestador que realiza la legalizacion.';
COMMENT ON COLUMN contratacion.legalizacion_contrato.contrato_id IS 'Id del contrato.';
COMMENT ON COLUMN contratacion.legalizacion_contrato.valor_poliza IS 'Medido en SMLV, de acuerdo al formato seleccionado se debe ingresar el campo';
COMMENT ON COLUMN contratacion.legalizacion_contrato.dias_plazo IS 'Esta asociado al valor de la poliza y solo es equerido si se ingresa la valor poliza. El Plazo Máximo es de 90 días';
COMMENT ON COLUMN contratacion.legalizacion_contrato.municipio_responsable_id IS 'Municipios asociados a donde se realiza la firma de la minuta.';
COMMENT ON COLUMN contratacion.legalizacion_contrato.fecha_firma_contrato IS 'Fecha en la que se firma el contrato';

ALTER TABLE contratacion.legalizacion_contrato_id_seq   OWNER TO postgres;
GRANT ALL ON SEQUENCE contratacion.legalizacion_contrato_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.legalizacion_contrato_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.legalizacion_contrato_id_seq TO gp_emssanar_lectura;
GRANT SELECT, USAGE ON SEQUENCE contratacion.legalizacion_contrato_id_seq TO app_emssanar_usuario_web;


-- Table: contratacion.tipo_descuento

-- DROP TABLE contratacion.tipo_descuento;

CREATE TABLE contratacion.tipo_descuento
(
  id serial NOT NULL,
  descripcion character varying(500),
  CONSTRAINT pk_tipo_descuento PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.tipo_descuento
  OWNER TO postgres;
GRANT ALL ON TABLE contratacion.tipo_descuento TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.tipo_descuento TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.tipo_descuento TO gp_emssanar_lectura;
GRANT ALL ON TABLE contratacion.tipo_descuento TO app_emssanar_usuario_web;

ALTER TABLE contratacion.tipo_descuento_id_seq   OWNER TO postgres;
GRANT ALL ON SEQUENCE contratacion.tipo_descuento_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.tipo_descuento_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.tipo_descuento_id_seq TO gp_emssanar_lectura;
GRANT ALL ON SEQUENCE contratacion.tipo_descuento_id_seq TO app_emssanar_usuario_web;


insert into contratacion.tipo_descuento values(nextval('contratacion.tipo_descuento_id_seq'),'Sin Descuento');
insert into contratacion.tipo_descuento values(nextval('contratacion.tipo_descuento_id_seq'),'Por Pronto Pago');
insert into contratacion.tipo_descuento values(nextval('contratacion.tipo_descuento_id_seq'),'Por Volumen');
insert into contratacion.tipo_descuento values(nextval('contratacion.tipo_descuento_id_seq'),'Practado');
insert into contratacion.tipo_descuento values(nextval('contratacion.tipo_descuento_id_seq'),'Por Monto');


-- Table: contratacion.tipo_condicion

-- DROP TABLE contratacion.tipo_condicion;

CREATE TABLE contratacion.tipo_condicion
(
  id serial NOT NULL,
  descripcion character varying(500),
  CONSTRAINT pk_tipo_condicion PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.tipo_condicion
  OWNER TO postgres;
GRANT ALL ON TABLE contratacion.tipo_condicion TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.tipo_condicion TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.tipo_condicion TO gp_emssanar_lectura;
GRANT ALL ON TABLE contratacion.tipo_condicion TO app_emssanar_usuario_web;

ALTER TABLE contratacion.tipo_condicion_id_seq   OWNER TO postgres;
GRANT ALL ON SEQUENCE contratacion.tipo_condicion_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.tipo_condicion_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.tipo_condicion_id_seq TO gp_emssanar_lectura;
GRANT ALL ON SEQUENCE contratacion.tipo_condicion_id_seq TO app_emssanar_usuario_web;

insert into contratacion.tipo_condicion values(nextval('contratacion.tipo_condicion_id_seq'),'Dias');
insert into contratacion.tipo_condicion values(nextval('contratacion.tipo_condicion_id_seq'),'Pesos');
insert into contratacion.tipo_condicion values(nextval('contratacion.tipo_condicion_id_seq'),'%');
insert into contratacion.tipo_condicion values(nextval('contratacion.tipo_condicion_id_seq'),'Actividades');


-- Table: contratacion.tipo_valor

-- DROP TABLE contratacion.tipo_valor;

CREATE TABLE contratacion.tipo_valor
(
  id serial NOT NULL,
  descripcion character varying(500),
  CONSTRAINT pk_tipo_valor PRIMARY KEY (id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.tipo_valor OWNER TO postgres;
GRANT ALL ON TABLE contratacion.tipo_valor TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.tipo_valor TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.tipo_valor TO gp_emssanar_lectura;
GRANT ALL ON TABLE contratacion.tipo_valor TO app_emssanar_usuario_web;

ALTER TABLE contratacion.tipo_valor_id_seq   OWNER TO postgres;
GRANT ALL ON SEQUENCE contratacion.tipo_valor_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.tipo_valor_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.tipo_valor_id_seq TO gp_emssanar_lectura;
GRANT ALL ON SEQUENCE contratacion.tipo_valor_id_seq TO app_emssanar_usuario_web;

insert into contratacion.tipo_valor values(nextval('contratacion.tipo_valor_id_seq'),'Dias');
insert into contratacion.tipo_valor values(nextval('contratacion.tipo_valor_id_seq'),'Pesos');
insert into contratacion.tipo_valor values(nextval('contratacion.tipo_valor_id_seq'),'%');
insert into contratacion.tipo_valor values(nextval('contratacion.tipo_valor_id_seq'),'Actividades');

-- Table: contratacion.descuento_legalizacion_contrato

-- DROP TABLE contratacion.descuento_legalizacion_contrato;

CREATE TABLE contratacion.descuento_legalizacion_contrato
(
  id bigserial NOT NULL,
  tipo_descuento_id integer NOT NULL,
  valor_condicion double precision,
  tipo_condicion_id integer NOT NULL,
  valor_descuento double precision,
  tipo_valor_id integer NOT NULL,
  detalle text,
  legalizacion_contrato_id bigint NOT NULL,
  CONSTRAINT pk_descuento_legalizacion_contrato PRIMARY KEY (id),
  CONSTRAINT fk_descuento_legalizacion_contrato FOREIGN KEY (legalizacion_contrato_id)
      REFERENCES contratacion.legalizacion_contrato (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_descuento_legalizacion_tipo_condicion FOREIGN KEY (tipo_condicion_id)
      REFERENCES contratacion.tipo_condicion (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_descuento_legalizacion_tipo_descuento FOREIGN KEY (tipo_descuento_id)
      REFERENCES contratacion.tipo_descuento (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION,
  CONSTRAINT fk_descuento_legalizacion_tipo_valor FOREIGN KEY (tipo_valor_id)
      REFERENCES contratacion.tipo_valor (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE contratacion.descuento_legalizacion_contrato
  OWNER TO postgres;
GRANT ALL ON TABLE contratacion.descuento_legalizacion_contrato TO app_emssanar_dgarcia;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.descuento_legalizacion_contrato TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.descuento_legalizacion_contrato TO gp_emssanar_lectura;

ALTER TABLE contratacion.descuento_legalizacion_contrato_id_seq   OWNER TO postgres;

GRANT ALL ON SEQUENCE contratacion.descuento_legalizacion_contrato_id_seq TO postgres;
GRANT SELECT, USAGE ON SEQUENCE contratacion.descuento_legalizacion_contrato_id_seq TO gp_emssanar_escritura;
GRANT SELECT ON SEQUENCE contratacion.descuento_legalizacion_contrato_id_seq TO gp_emssanar_lectura;
GRANT ALL ON SEQUENCE contratacion.descuento_legalizacion_contrato_id_seq TO app_emssanar_usuario_web;
