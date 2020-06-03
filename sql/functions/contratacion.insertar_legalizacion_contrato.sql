create or replace function contratacion.insertar_legalizacion_contrato() returns trigger
    language plpgsql
as
$$
DECLARE
    es_otro_si           boolean;
    nueva_negociacion_id int;
    nuevo_regimen        varchar;
BEGIN
    nuevo_regimen := (SELECT sc.regimen
                      FROM contratacion.solicitud_contratacion sc
                               INNER JOIN contratacion.contrato c ON (sc.id = c.solicitud_contratacion_id)
                      WHERE c.id = new.id);

    nueva_negociacion_id := (SELECT n.id
                             FROM contratacion.negociacion n
                                      INNER JOIN contratacion.solicitud_contratacion sc ON (n.id = sc.negociacion_id)
                                      INNER JOIN contratacion.contrato c ON (sc.id = c.solicitud_contratacion_id)
                             WHERE c.id = new.id);

    es_otro_si := (SELECT * FROM contratacion.validacion_es_otro_si(nueva_negociacion_id));

    IF es_otro_si = TRUE THEN
        INSERT INTO contratacion.legalizacion_contrato (prestador_id, municipio_id, direccion, contrato_id,
                                                        minuta_id, objeto_contrato_id, valor_fiscal, valor_poliza,
                                                        dias_plazo, municipio_responsable_id,
                                                        responsable_firma_contrato_id, usuario_id, contenido,
                                                        fecha_firma_contrato)
        SELECT leg.prestador_id,
               leg.municipio_id,
               leg.direccion,
               new.id,
               leg.minuta_id,
               leg.objeto_contrato_id,
               leg.valor_fiscal,
               leg.valor_poliza,
               leg.dias_plazo,
               leg.municipio_responsable_id,
               leg.responsable_firma_contrato_id,
               new.user_id,
               leg.contenido,
               leg.fecha_firma_contrato
        FROM contratacion.negociacion n
                 INNER JOIN contratacion.solicitud_contratacion sc ON (n.id = sc.negociacion_id)
                 INNER JOIN contratacion.contrato c ON (sc.id = c.solicitud_contratacion_id)
                 INNER JOIN contratacion.legalizacion_contrato leg ON (c.id = leg.contrato_id)
        WHERE n.id =
              (SELECT n2.negociacion_padre_id FROM contratacion.negociacion n2 WHERE n2.id = nueva_negociacion_id)
          AND sc.regimen = nuevo_regimen;
    END IF;
    RETURN NEW;
END;
$$;
