ALTER TABLE contratacion.negociacion_referente
OWNER TO postgres;
GRANT ALL ON TABLE contratacion.negociacion_referente TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE contratacion.negociacion_referente TO gp_emssanar_escritura;
GRANT SELECT ON TABLE contratacion.negociacion_referente TO gp_emssanar_lectura;
GRANT SELECT ON TABLE contratacion.negociacion_referente TO gp_emssanar_cliente;
