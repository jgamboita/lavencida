package co.conexia.negociacion.services.negociacion.importarTecnologia;

import com.conexia.exceptions.ConexiaBusinessException;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.List;

public interface Tecnologia  extends Serializable {
    public List<?> importar(EntityManager em,int usuarioId, int negociacionId, String nombreArchivo) throws ConexiaBusinessException;
}
