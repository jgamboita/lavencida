create trigger insertar_contrato
    after insert
    on contratacion.solicitud_contratacion
    for each row
execute procedure contratacion.insertar_contrato();

