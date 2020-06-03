create trigger insertar_legalizacion_contrato
    after insert
    on contratacion.contrato
    for each row
execute procedure contratacion.insertar_legalizacion_contrato();