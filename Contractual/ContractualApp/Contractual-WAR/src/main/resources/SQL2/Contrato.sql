--Tabla de contrato sede

CREATE TABLE contratacion.sede_contrato (
	id serial,
	sede_prestador_id integer,
	contrato_id integer,
	CONSTRAINT sede_contrato_pkey PRIMARY KEY (id),
        CONSTRAINT "FK_sede_contrato_contrato" FOREIGN KEY (contrato_id)
		REFERENCES contratacion.contrato (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION,
        CONSTRAINT "FK_sede_contrato_sede_prestador" FOREIGN KEY (sede_prestador_id)
		REFERENCES contratacion.sede_prestador (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION        
);

--Contrato Procedimiento

CREATE TABLE contratacion.procedimiento_contrato (
	id serial,
	sede_contrato_id integer not null,
	procedimiento_id integer not null,
	tarifa_diferencial boolean,
	tarifario_contrato_id integer,
	porcentaje_contrato numeric(10,2),
	valor_contrato numeric,
	requiere_autorizacion boolean,
        estado integer,	
	CONSTRAINT procedimiento_contrato_pkey PRIMARY KEY (id),
        CONSTRAINT "FK_procedimiento_contrato_sede_contrato_id" FOREIGN KEY (sede_contrato_id)
		REFERENCES contratacion.sede_contrato (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION,
        CONSTRAINT "FK_procedimiento_contrato_procedimiento" FOREIGN KEY (procedimiento_id)
		REFERENCES maestros.procedimiento_servicio (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION,
        CONSTRAINT "FK_procedimiento_contrato_tarifario" FOREIGN KEY (tarifario_contrato_id)
		REFERENCES contratacion.tarifarios (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION,
        CONSTRAINT "FK_procedimiento_contrato_estado_item_contratado" 
                FOREIGN KEY (estado) REFERENCES maestros.estado_item_contratado (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION        
);

-- Contrato Medicamento

CREATE TABLE contratacion.medicamento_contrato (
	id serial,
	sede_contrato_id integer not null,
	medicamento_id integer not null,
	valor_contrato numeric,
	requiere_autorizacion boolean,	
        estado integer,
	CONSTRAINT medicamento_contrato_pkey PRIMARY KEY (id),
        CONSTRAINT "FK_medicamento_contrato_sede_contrato_id" FOREIGN KEY (sede_contrato_id)
		REFERENCES contratacion.sede_contrato (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION,
        CONSTRAINT "FK_medicamento_contrato_medicamento" FOREIGN KEY (medicamento_id)
		REFERENCES maestros.medicamento (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION,  
        CONSTRAINT "FK_medicamento_contrato_estado_item_contratado" 
                FOREIGN KEY (estado) REFERENCES maestros.estado_item_contratado (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- Contrato Insumo

CREATE TABLE contratacion.insumo_contrato (
	id serial,
	sede_contrato_id integer not null,
	insumo_id integer not null,
	valor_contrato numeric,
	requiere_autorizacion boolean,	
        estado integer,
	CONSTRAINT insumo_contrato_pkey PRIMARY KEY (id),
        CONSTRAINT "FK_insumo_contrato_sede_contrato_id" FOREIGN KEY (sede_contrato_id)
		REFERENCES contratacion.sede_contrato (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION,
        CONSTRAINT "FK_insumo_contrato_insumo" FOREIGN KEY (insumo_id)
		REFERENCES maestros.insumo (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION,  
        CONSTRAINT "FK_insumo_contrato_estado_item_contratado" 
                FOREIGN KEY (estado) REFERENCES maestros.estado_item_contratado (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION
);

-- Contrato Paquete

CREATE TABLE contratacion.paquete_contrato (
	id serial,
	sede_contrato_id integer not null,
	paquete_id integer,
	valor_contrato numeric,
	requiere_autorizacion boolean,	
        estado integer,
	CONSTRAINT paquete_contrato_pkey PRIMARY KEY (id),
        CONSTRAINT "FK_paquete_contrato_sede_contrato_id" FOREIGN KEY (sede_contrato_id)
		REFERENCES contratacion.sede_contrato (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION,
        CONSTRAINT "FK_paquete_contrato_procedimiento" FOREIGN KEY (paquete_id)
                REFERENCES maestros.procedimiento (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION,
        CONSTRAINT "FK_paquete_contrato_estado_item_contratado" 
                FOREIGN KEY (estado) REFERENCES maestros.estado_item_contratado (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION            
);

-- Paquete contrato medicamento

CREATE TABLE contratacion.paquete_medicamento_contrato (
	id serial,
	paquete_contrato_id integer not null,
	medicamento_id integer not null,
	cantidad integer,
	CONSTRAINT paquete_medicamento_contrato_pkey PRIMARY KEY (id),
        CONSTRAINT "FK_paquete_medicamento_contrato_paquete_contrato" FOREIGN KEY (paquete_contrato_id)
		REFERENCES contratacion.paquete_contrato (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION,
        CONSTRAINT "FK_paquete_medicamento_contrato_medicamento" FOREIGN KEY (medicamento_id)
		REFERENCES maestros.medicamento (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION            
);

-- Paquete Contrato Procedimiento

CREATE TABLE contratacion.paquete_procedimiento_contrato (
	id serial,
	paquete_contrato_id integer not null,
	procedimiento_id integer not null,
	cantidad integer,
	principal boolean,
	CONSTRAINT paquete_procedimiento_contrato_pkey PRIMARY KEY (id),
        CONSTRAINT "FK_paquete_procedimiento_contrato_paquete_contrato" FOREIGN KEY (paquete_contrato_id)
		REFERENCES contratacion.paquete_contrato (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION,
        CONSTRAINT "FK_paquete_medicamento_contrato_procedimiento" FOREIGN KEY (procedimiento_id)
		REFERENCES maestros.procedimiento_servicio (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION            
);

-- Paquete Contrato Insumo

CREATE TABLE contratacion.paquete_insumo_contrato (
	id serial,
	paquete_contrato_id integer not null,
	insumo_id integer not null,
	cantidad integer,
	CONSTRAINT paquete_insumo_contrato_pkey PRIMARY KEY (id),
        CONSTRAINT "FK_paquete_insumo_contrato_paquete_contrato" FOREIGN KEY (paquete_contrato_id)
		REFERENCES contratacion.paquete_contrato (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION,
        CONSTRAINT "FK_paquete_insumo_contrato_insumo" FOREIGN KEY (insumo_id)
		REFERENCES maestros.insumo (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION            
);

-- Paquete Contrato Traslado

CREATE TABLE contratacion.paquete_traslado_contrato (
	id serial,
	paquete_contrato_id integer not null,
	traslado_id integer not null,
	cantidad integer,
	CONSTRAINT paquete_traslado_contrato_pkey PRIMARY KEY (id),
        CONSTRAINT "FK_paquete_traslado_contrato_paquete_contrato" FOREIGN KEY (paquete_contrato_id)
		REFERENCES contratacion.paquete_contrato (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION,
        CONSTRAINT "FK_paquete_traslado_contrato_procedimiento" FOREIGN KEY (traslado_id)
		REFERENCES maestros.procedimiento (id) MATCH SIMPLE
                ON UPDATE NO ACTION ON DELETE NO ACTION            
);