package co.conexia.negociacion.services.negociacion.paquete.control;

import com.conexia.contractual.model.contratacion.portafolio.PaquetePortafolio;
import com.conexia.contractual.model.maestros.Procedimiento;
import com.conexia.contratacion.commons.dto.maestros.ProcedimientoDto;
import com.conexia.logfactory.impl.MyLogger;
import com.conexia.one.persistence.EntityManagerFactoryBuilder;
import com.conexia.one.persistence.JpaEntityManagerFactory;
import org.junit.jupiter.api.*;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

@DisplayName("Tecnología origen de un paquete")
class ObtenerTecnologiaOrigenPaqueteNegociadoCotrolTestIT {

    private static EntityManager em;
    private static ObtenerTecnologiaOrigenPaqueteNegociadoCotrol obtenerTecnologiaOrigenPaqueteNegociadoCotrol;

    @BeforeAll
    public static void setUpAll() {
        JpaEntityManagerFactory emf = new EntityManagerFactoryBuilder()
                .h2()
                .memory()
                .end()
                .jpaProperties()
                .put("hibernate.query.startup_check", false)
                .databaseGenerationActionCreate()
                .dataScript("escenario-tecnologia-origen-paquete.sql")
                .end()
                .using(PaquetePortafolio.class, Procedimiento.class)
                .emf();


        em = emf.getEntityManager();
        obtenerTecnologiaOrigenPaqueteNegociadoCotrol = new ObtenerTecnologiaOrigenPaqueteNegociadoCotrol(em, new MyLogger(LoggerFactory.getLogger(ObtenerTecnologiaOrigenPaqueteNegociadoCotrol.class)));
    }

    @TestFactory
    @DisplayName("Creación de escenarios para la tecnología origen de un paquete")
    Collection<DynamicTest> dynamicTestsFromCollection() {
        return Arrays.asList(
                dynamicTest("Obtener tecnología principal como tecnología origen del paquete",
                        () -> obtenerTecnologiaComoTecnologiaOrigenPaquete(1L, "01020602")),
                dynamicTest("Obtener máxima tecnología principal como tecnología origen del paquete",
                        () -> obtenerTecnologiaComoTecnologiaOrigenPaquete(2L, "01021100")),
                dynamicTest("Obtener máxima tecnología como tecnología origen del paquete",
                        () -> obtenerTecnologiaComoTecnologiaOrigenPaquete(3L, "01021103"))
        );
    }

    public void obtenerTecnologiaComoTecnologiaOrigenPaquete(Long portafolioId, String codigoTecnologia) {
        PaquetePortafolio paquetePortafolio = dadoUnPaqueteSinTecnologiaOrigen(portafolioId);
        ProcedimientoDto tecnologiaOrigenPaquete = cuandoConsultaTecnologiaOrigenDelPaquete(paquetePortafolio);
        entoncesDeboObtenerTecnologiaComoOrigen(tecnologiaOrigenPaquete, codigoTecnologia);
    }

    private ProcedimientoDto cuandoConsultaTecnologiaOrigenDelPaquete(PaquetePortafolio paquetePortafolio) {
        em.getTransaction().begin();
        ProcedimientoDto tecnologiaOrigenPaquete = obtenerTecnologiaOrigenPaqueteNegociadoCotrol.obtenerTecnologiaOrigenPaquete(paquetePortafolio.getPortafolio().getId());
        em.getTransaction().commit();
        return tecnologiaOrigenPaquete;
    }

    private PaquetePortafolio dadoUnPaqueteSinTecnologiaOrigen(Long portafolioId) {
        em.getTransaction().begin();
        PaquetePortafolio paquetePortafolio = em.find(PaquetePortafolio.class, portafolioId);
        em.getTransaction().commit();
        return paquetePortafolio;
    }

    private void entoncesDeboObtenerTecnologiaComoOrigen(ProcedimientoDto tecnologiaOrigenPaquete, String codigoTecnologia) {
        assertNotNull(tecnologiaOrigenPaquete);
        assertEquals(tecnologiaOrigenPaquete.getCodigoCliente(), codigoTecnologia);
    }

    @AfterEach
    public void tearDown() {
        em.clear();
    }

}