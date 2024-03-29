package co.conexia.negociacion.services.negociacion.paquete.control;

import com.conexia.contractual.model.contratacion.negociacion.SedeNegociacionPaquete;
import com.conexia.contratacion.commons.dto.ErroresTecnologiasDto;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.logfactory.impl.MyLogger;
import com.conexia.one.persistence.EntityManagerFactoryBuilder;
import com.conexia.one.persistence.JpaEntityManagerFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ObtenerPaquetesNegociadosConTecnologiasInactivasTestIT {

    private static EntityManager em;
    private static ObtenerPaquetesNegociadosConTecnologiasInactivas obtenerPaquetesNegociadosConTecnologiasInactivas;

    @BeforeAll
    public static void setUpAll() {
        JpaEntityManagerFactory emf = new EntityManagerFactoryBuilder()
                .h2()
                .memory()
                .end()
                .jpaProperties()
                .put("hibernate.query.startup_check", false)
                .databaseGenerationActionCreate()
                .end()
                .using(SedeNegociacionPaquete.class)
                .emf();


        em = emf.getEntityManager();
        obtenerPaquetesNegociadosConTecnologiasInactivas = new ObtenerPaquetesNegociadosConTecnologiasInactivas(em, new MyLogger(LoggerFactory.getLogger(ObtenerPaquetesNegociadosSinValores.class)));
    }

    @Test
    void obtenerPaquetes() {
        em.getTransaction().begin();
        NegociacionDto negociacion = new NegociacionDto();
        negociacion.setId(1L);
        List<ErroresTecnologiasDto> erroresTecnologiasDtos = obtenerPaquetesNegociadosConTecnologiasInactivas.obtenerPaquetes(negociacion);
        assertTrue(erroresTecnologiasDtos.isEmpty());
        em.getTransaction().commit();
    }
}