CREATE TABLE contratacion.negociacion_referente (
	id serial NOT NULL,
	negociacion_id integer NOT NULL,	
	negociacion_referente_id integer NOT NULL,	
	fecha_creacion date NOT null,
	CONSTRAINT pk_negociacion_referente_pkey PRIMARY KEY (id)
);

CREATE INDEX ix_negociacion_referente_negociacion ON contratacion.negociacion_referente (negociacion_id);
CREATE INDEX ix_negociacion_referente_negociacion_referente ON contratacion.negociacion_referente (negociacion_referente_id);


