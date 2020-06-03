package co.conexia.negociacion.services.negociacion.medicamento.control;

import co.conexia.negociacion.services.negociacion.paquete.control.ObtenerPaquetesNegociadosSinValores;
import com.conexia.contratacion.commons.constants.enums.NegociacionModalidadEnum;
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

class ObtenerMedicamentosNegociadosConTecnologiasInactivasTestIT {

    private static EntityManager em;
    private static ObtenerMedicamentosNegociadosConTecnologiasInactivas obtenerPaquetesNegociadosConTecnologiasInactivas;

    @BeforeAll
    public static void setUpAll() {
        JpaEntityManagerFactory emf = new EntityManagerFactoryBuilder()
                .h2()
                .memory()
                .end()
                .jpaProperties()
                .put("hibernate.query.startup_check", false)
                .databaseGenerationActionCreate()
                .dataScript("escenario-medicamentos-inactivas-negociacion.sql")
                .end()
                .using(SedeNegociacionPaquete.class)
                .emf();


        em = emf.getEntityManager();
        obtenerPaquetesNegociadosConTecnologiasInactivas = new ObtenerMedicamentosNegociadosConTecnologiasInactivas(em, new MyLogger(LoggerFactory.getLogger(ObtenerPaquetesNegociadosSinValores.class)));
    }

    @Test
    void obtenerPaquetes() {
        em.getTransaction().begin();
        NegociacionDto negociacion = new NegociacionDto();
        negociacion.setId(1L);
        negociacion.setTipoModalidadNegociacion(NegociacionModalidadEnum.RIAS_CAPITA);
        List<ErroresTecnologiasDto> erroresTecnologiasDtos = obtenerPaquetesNegociadosConTecnologiasInactivas.obtenerMedicamentos(negociacion);
        assertTrue(erroresTecnologiasDtos.isEmpty());
        em.getTransaction().commit();
    }
}