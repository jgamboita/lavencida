package co.conexia.negociacion.services.negociacion.control;

import com.conexia.contratacion.commons.constants.enums.AreaCoberturaTipoEnum;
import com.conexia.contractual.model.contratacion.Prestador;
import com.conexia.contractual.model.contratacion.ZonaCapita;
import com.conexia.contractual.model.contratacion.negociacion.Negociacion;
import com.conexia.contractual.model.contratacion.negociacion.NegociacionRia;
import com.conexia.contractual.model.contratacion.negociacion.SedeNegociacionServicio;
import com.conexia.contractual.model.contratacion.negociacion.SedesNegociacion;
import com.conexia.contractual.model.contratacion.parametrizacion.SolicitudContratacion;
import com.conexia.contractual.model.contratacion.referente.Referente;
import com.conexia.contractual.model.maestros.Departamento;
import com.conexia.contractual.model.maestros.GrupoEtnico;
import com.conexia.contractual.model.maestros.Municipio;
import com.conexia.contractual.model.maestros.Procedimiento;
import com.conexia.contractual.model.security.User;
import com.conexia.contratacion.commons.dto.negociacion.NegociacionDto;
import com.conexia.one.persistence.EntityManagerFactoryBuilder;
import com.conexia.one.persistence.JpaEntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NegociacionControlTestIT {

    private static JpaEntityManagerFactory emf;
    private EntityManager em;

    private NegociacionControl negociacionControl;

    @BeforeAll
    public static void setUpAll() {
        emf = new EntityManagerFactoryBuilder()
                .h2()
                .memory()
                .end()
                .jpaProperties()
                .put("hibernate.query.startup_check", false)
                .databaseGenerationActionDropAndCreate()
                .end()
                .using(Departamento.class, User.class, SedesNegociacion.class, Prestador.class, SedeNegociacionServicio.class,
                        SolicitudContratacion.class, ZonaCapita.class, Municipio.class, Referente.class, GrupoEtnico.class,
                        NegociacionRia.class, AreaCoberturaTipoEnum.class, Procedimiento.class)
                .emf();
    }

    @BeforeEach
    public void setUp() {
        em = emf.getEntityManager();
        this.negociacionControl = new NegociacionControl(em);
        cargarDatos();
    }

    private void cargarDatos() {
        Negociacion negociacion = new Negociacion();
        em.getTransaction().begin();
        em.persist(negociacion);
        em.getTransaction().commit();
    }

    @Test
    public void consultarContratosPorPrestadorYContrato() {
        List<NegociacionDto> negociacionDtos = this.negociacionControl.consultarNegociaciones("167-1ES190001");
        assertNotNull(negociacionDtos);
    }

    @AfterEach
    public void tearDown() {
        em.clear();
    }
}
