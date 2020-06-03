package com.conexia.contractual.utils;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Objects;

/**
 * Class PropertiesUtil
 * @author aquintero
 */
public class PropertiesUtil
{

    @PersistenceContext(unitName = "contractualDS")
    static EntityManager em;


    public List<String> getValueProperty(String key, String application)
    {
        try
        {
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT valor ")
                    .append("FROM configuracion.properties ")
                    .append("WHERE clave = '").append(key).append("'");
            if (Objects.nonNull(application)){
                sql.append(" AND aplicacion = '").append(application).append("'");
            }
            List<String> listResulted = (List<String>) em.createNativeQuery(sql.toString()).getResultList();
            if(listResulted.isEmpty()) throw new Exception("No se encontraron datos");
            return listResulted;
        } catch (NoResultException e) {
            throw new RuntimeException("No existe la propiedad - Aplicacion: " + application +
                    ", Clave: " + key);
        }catch (Exception e) {
            throw new RuntimeException("Error!!! Ocurrio un error inesperado " + e.getMessage());
        }
    }

}
